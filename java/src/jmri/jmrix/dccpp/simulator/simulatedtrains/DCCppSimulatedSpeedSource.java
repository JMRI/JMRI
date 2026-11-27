package jmri.jmrix.dccpp.simulator.simulatedtrains;

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
import jmri.jmrix.dccpp.simulator.DCCppSimulatorAdapter;

/**
 * Speed commands for simulated trains from a DCC-EX Simulator connection: the
 * throttle commands its simulated command station receives, {@code <t cab
 * speed dir>} or {@code <t register cab speed dir>}.
 * <p>
 * A DCC-EX speed is 0 (stop) to 126, in 128 speed step mode, or -1 for an
 * emergency stop; direction 1 is forward. A cab number above 127 is a long
 * address, as DCC-EX takes it. When a cab is sent nothing more, its loco
 * keeps its last command, as a decoder keeps running.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@javax.annotation.concurrent.ThreadSafe
public class DCCppSimulatedSpeedSource implements SimulatedSpeedSource,
        DCCppSimulatorAdapter.ThrottleCommandListener {

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
    public void throttleCommand(int cab, int speed, int direction, long timeNanos) {
        DccLocoAddress address = new DccLocoAddress(cab, cab > 127);
        Command now = command(speed, direction, timeNanos);
        Command old = commands.get(address);
        if (old != null && old.getSpeedStep() == now.getSpeedStep() && old.isForward() == now.isForward()
                && old.isEmergencyStop() == now.isEmergencyStop()) {
            return; // the same packet again: nothing changes for the decoder
        }
        commands.put(address, now);
        for (CommandListener l : listeners) {
            l.commandChanged(address);
        }
    }

    /**
     * A DCC-EX speed and direction as a decoder in 128 speed step mode
     * receives it. A speed beyond 126 is taken as 126.
     *
     * @param speed     -1 for an emergency stop, else 0 to 126
     * @param direction 1 for forward, 0 for reverse
     * @param timeNanos when it was received
     * @return the command
     */
    @Nonnull
    static Command command(int speed, int direction, long timeNanos) {
        boolean estop = speed < 0;
        int step = estop ? 0 : Math.min(speed, SpeedStepMode.NMRA_DCC_128.numSteps);
        return new Command(step, SpeedStepMode.NMRA_DCC_128, direction == 1, estop, timeNanos);
    }
}
