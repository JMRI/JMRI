package jmri.jmrix.loconet.hexfile.simulatedtrains;

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
import jmri.jmrix.loconet.hexfile.LnSimulatedCommandStation;

/**
 * Speed commands for simulated trains from a LocoNet Simulator connection's
 * simulated command station: each loco does what its slot says, as the
 * command station would send it.
 * <p>
 * A slot's speed is 0 stop, 1 emergency stop, 2 to 127 moving. A loco in 128
 * speed steps (decoder type 3) gets steps 1 to 126; in 14 steps (type 2) or
 * 28 (the other types) the speed is scaled to those steps, any speed above
 * 1 being at least step 1, as the command station's conversion is taken to
 * be. LocoNet addresses 1 to 127 are short and others long, so a loco is
 * found by its number alone.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@javax.annotation.concurrent.ThreadSafe
public class LnSimulatedSpeedSource implements SimulatedSpeedSource, LnSimulatedCommandStation.SlotListener {

    private final Map<Integer, Command> commands = new ConcurrentHashMap<>();
    private final List<CommandListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    @InvokeOnAnyThread
    public Command getCommand(@Nonnull DccLocoAddress address) {
        return commands.get(address.getNumber());
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
    public void slotChanged(@Nonnull LnSimulatedCommandStation.Slot slot, long timeNanos) {
        int address = slot.getAddress();
        if (address <= 0) {
            return;
        }
        received(address, command(slot.getSpeed(), slot.isForward(), slot.getDecoderType(), timeNanos));
    }

    /**
     * @param speed       the slot's speed, 0 to 127
     * @param forward     true for forward
     * @param decoderType the slot's decoder type bits
     * @param timeNanos   when it changed
     * @return the command the loco gets
     */
    @Nonnull
    static Command command(int speed, boolean forward, int decoderType, long timeNanos) {
        SpeedStepMode mode = decoderType == 3 || decoderType == 7 ? SpeedStepMode.NMRA_DCC_128
                : decoderType == 2 ? SpeedStepMode.NMRA_DCC_14 : SpeedStepMode.NMRA_DCC_28;
        boolean estop = speed == 1;
        int step = 0;
        if (speed > 1) {
            step = mode == SpeedStepMode.NMRA_DCC_128 ? speed - 1
                    : Math.max(1, (int) Math.round((speed - 1) * mode.numSteps / 126.0));
        }
        return new Command(Math.min(step, mode.numSteps), mode, forward, estop, timeNanos);
    }

    private void received(int address, Command now) {
        Command old = commands.get(address);
        if (old != null && old.getSpeedStep() == now.getSpeedStep() && old.getMode() == now.getMode()
                && old.isForward() == now.isForward() && old.isEmergencyStop() == now.isEmergencyStop()) {
            return;
        }
        commands.put(address, now);
        DccLocoAddress a = new DccLocoAddress(address, address > 127);
        for (CommandListener l : listeners) {
            l.commandChanged(a);
        }
    }
}
