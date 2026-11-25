package jmri.jmrix.mrc.simulator.simulatedtrains;

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
import jmri.jmrix.mrc.simulator.SimulatorAdapter;

/**
 * Speed commands for simulated trains from an MRC Simulator connection: what
 * each loco is told by the throttle packets the simulated command station is
 * sent, read here, not with JMRI's MRC code, so a fault there shows as a
 * train moving differently from what was asked. A throttle packet (command
 * 37) carries the address, high byte then low byte (a high byte of 0xC0 or
 * more marks a long address, as in a DCC packet), a speed byte and a mode
 * byte. In 128 speed step mode (mode 2) the speed byte is read as the DCC
 * 128 speed step byte (0 stop, 1 emergency stop, 2 to 127 steps 1 to 126,
 * bit 7 forward); otherwise as the DCC 28 speed step instruction (01DCSSSS:
 * 0 and 1 stop, 2 and 3 emergency stop, 4 to 31 steps 1 to 28, with C the
 * least significant bit).
 * <p>
 * A loco keeps its last command when it is sent nothing more, as a decoder
 * keeps running.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@javax.annotation.concurrent.ThreadSafe
public class MrcSimulatedSpeedSource implements SimulatedSpeedSource, SimulatorAdapter.MessageListener {

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
        try {
            int[] b = new int[w.length];
            for (int i = 0; i < w.length; i++) {
                b[i] = Integer.parseInt(w[i], 16);
            }
            // header 37 0 37 0, then high 0 low 0 speed 0 mode 0 checksum 0
            if (b.length < 13 || b[0] != 37 || b[2] != 37) {
                return;
            }
            int hi = b[4];
            int lo = b[6];
            int speed = b[8];
            int mode = b[10];
            DccLocoAddress address = hi >= 0xC0
                    ? new DccLocoAddress(((hi & 0x3F) << 8) | lo, true)
                    : new DccLocoAddress(lo, false);
            if (mode == 0x02) {
                received(address, speed(speed & 0x7F, (speed & 0x80) != 0, SpeedStepMode.NMRA_DCC_128, timeNanos));
            } else if ((speed & 0xC0) == 0x40) {
                int v = ((speed & 0x0F) << 1) | ((speed >> 4) & 1);
                received(address, speed(v, (speed & 0x20) != 0, SpeedStepMode.NMRA_DCC_28, timeNanos));
            }
        } catch (NumberFormatException e) {
            // not a packet that moves a loco
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
