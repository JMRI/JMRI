package jmri.jmrix.easydcc.simulator.simulatedtrains;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.DccLocoAddress;
import jmri.InvokeOnAnyThread;
import jmri.SpeedStepMode;
import jmri.jmrit.simulation.SimulatedSpeedSource;
import jmri.jmrix.easydcc.simulator.SimulatorAdapter;

/**
 * Speed commands for simulated trains from an EasyDCC Simulator connection:
 * what each loco is told by the DCC packets the simulated command station is
 * sent, read here, not with JMRI's EasyDCC code, so a fault there shows as a
 * train moving differently from what was asked. A packet is sent with
 * {@code Q} and its bytes in hexadecimal (queued, as for a throttle) or
 * {@code S 0n} and its bytes (sent n times). Speed and direction
 * instructions are read as the NMRA standard for them (S-9.2 and S-9.2.1)
 * gives: the 128 speed step instruction (0x3F and a speed byte: 0 stop, 1
 * emergency stop, 2 to 127 steps 1 to 126, bit 7 forward), and the 14 and 28
 * speed step instruction (01DCSSSS, read in 28 steps, as JMRI sends it: 0
 * and 1 stop, 2 and 3 emergency stop, 4 to 31 steps 1 to 28, with C the least
 * significant bit).
 * <p>
 * A loco keeps its last command when it is sent nothing more, as a decoder
 * keeps running.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@javax.annotation.concurrent.ThreadSafe
public class EasyDccSimulatedSpeedSource implements SimulatedSpeedSource, SimulatorAdapter.MessageListener {

    private final Map<DccLocoAddress, Command> commands = new ConcurrentHashMap<>();
    private final List<CommandListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    @InvokeOnAnyThread
    public Command getCommand(@Nonnull DccLocoAddress address) {
        return commands.get(address);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @InvokeOnAnyThread
    public void addCommandListener(@Nonnull CommandListener listener) {
        listeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @InvokeOnAnyThread
    public void removeCommandListener(@Nonnull CommandListener listener) {
        listeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @InvokeOnAnyThread
    public synchronized void messageReceived(String message, long timeNanos) {
        String[] w = message.trim().split("\\s+");
        int first;
        if (w[0].equals("Q")) {
            first = 1;
        } else if (w[0].equals("S")) {
            first = 2; // after the repeat count
        } else {
            return; // not a packet
        }
        try {
            int[] packet = new int[Math.max(0, w.length - first)];
            for (int i = first; i < w.length; i++) {
                packet[i - first] = Integer.parseInt(w[i], 16);
            }
            packet(packet, timeNanos);
        } catch (NumberFormatException e) {
            // not a packet that moves a loco
        }
    }

    /**
     * Read a DCC packet for a speed and direction instruction.
     *
     * @param p         the packet's bytes, with or without its error byte
     * @param timeNanos when it was received
     */
    private void packet(int[] p, long timeNanos) {
        if (p.length < 2) {
            return;
        }
        DccLocoAddress address;
        int i;
        if (p[0] >= 1 && p[0] <= 127) {
            address = new DccLocoAddress(p[0], false);
            i = 1;
        } else if (p[0] >= 0xC0 && p[0] <= 0xE7) {
            address = new DccLocoAddress(((p[0] & 0x3F) << 8) | p[1], true);
            i = 2;
        } else {
            return; // broadcast, accessory or other packet
        }
        if (i >= p.length) {
            return;
        }
        int instruction = p[i];
        if (instruction == 0x3F && i + 1 < p.length) {
            int b = p[i + 1];
            received(address, speed(b & 0x7F, (b & 0x80) != 0, SpeedStepMode.NMRA_DCC_128, timeNanos));
        } else if ((instruction & 0xC0) == 0x40) {
            int v = ((instruction & 0x0F) << 1) | ((instruction >> 4) & 1);
            received(address, speed(v, (instruction & 0x20) != 0, SpeedStepMode.NMRA_DCC_28, timeNanos));
        }
    }

    /**
     * A speed value, as a decoder in that speed step mode receives it. A value
     * beyond the mode's range is taken as its top step.
     *
     * @param value     the speed value without its direction bit: in 128
     *                  steps 0 stop, 1 emergency stop, 2 to 127 steps 1 to
     *                  126; in 28 steps 0 and 1 stop, 2 and 3 emergency stop,
     *                  4 to 31 steps 1 to 28; in 14 steps 0 stop, 1
     *                  emergency stop, 2 to 15 steps 1 to 14
     * @param forward   true for forward
     * @param mode      the speed step mode
     * @param timeNanos when it was received
     * @return the command
     */
    @Nonnull
    static Command speed(int value, boolean forward, @Nonnull SpeedStepMode mode, long timeNanos) {
        boolean estop;
        int step;
        if (mode == SpeedStepMode.NMRA_DCC_28) {
            estop = value == 2 || value == 3;
            step = value < 4 ? 0 : value - 3;
        } else {
            estop = value == 1;
            step = value < 2 ? 0 : value - 1;
        }
        SpeedStepMode m = (mode == SpeedStepMode.NMRA_DCC_28 || mode == SpeedStepMode.NMRA_DCC_14)
                ? mode : SpeedStepMode.NMRA_DCC_128;
        return new Command(Math.min(step, m.numSteps), m, forward, estop, timeNanos);
    }

    private void received(DccLocoAddress address, Command now) {
        Command old = commands.get(address);
        if (old != null && old.getSpeedStep() == now.getSpeedStep() && old.getMode() == now.getMode()
                && old.isForward() == now.isForward() && old.isEmergencyStop() == now.isEmergencyStop()) {
            return; // the same packet again: nothing changes for the decoder
        }
        commands.put(address, now);
        for (CommandListener l : listeners) {
            l.commandChanged(address);
        }
    }
}
