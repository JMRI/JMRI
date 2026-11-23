package jmri.jmrit.simulation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InvokeOnAnyThread;
import jmri.DccLocoAddress;
import jmri.SpeedStepMode;

/**
 * A speed source that holds commands set through code, for tests driving
 * simulated trains without a throttle. Scripts drive simulated trains through
 * the Internal connection's throttles instead.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@javax.annotation.concurrent.ThreadSafe
public class InMemorySpeedSource implements SimulatedSpeedSource {

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
     * Command a speed now.
     *
     * @param address   the loco address
     * @param speedStep the speed step, 0 to the mode's number of steps
     * @param mode      the speed-step mode
     * @param forward   true for forward
     */
    @InvokeOnAnyThread
    public void setSpeed(@Nonnull DccLocoAddress address, int speedStep, @Nonnull SpeedStepMode mode,
            boolean forward) {
        setSpeed(address, speedStep, mode, forward, System.nanoTime());
    }

    /**
     * Command a speed, received at a given time.
     *
     * @param address   the loco address
     * @param speedStep the speed step, 0 to the mode's number of steps
     * @param mode      the speed-step mode
     * @param forward   true for forward
     * @param timeNanos when it was received, on the {@link System#nanoTime()}
     *                  scale
     */
    @InvokeOnAnyThread
    public void setSpeed(@Nonnull DccLocoAddress address, int speedStep, @Nonnull SpeedStepMode mode,
            boolean forward, long timeNanos) {
        set(address, new Command(speedStep, mode, forward, false, timeNanos));
    }

    /**
     * Command an emergency stop, received at a given time.
     *
     * @param address   the loco address
     * @param timeNanos when it was received
     */
    @InvokeOnAnyThread
    public void emergencyStop(@Nonnull DccLocoAddress address, long timeNanos) {
        Command old = commands.get(address);
        SpeedStepMode mode = (old != null) ? old.getMode() : SpeedStepMode.NMRA_DCC_128;
        boolean forward = (old == null) || old.isForward();
        set(address, new Command(0, mode, forward, true, timeNanos));
    }

    private void set(DccLocoAddress address, Command c) {
        commands.put(address, c);
        for (CommandListener l : listeners) {
            l.commandChanged(address);
        }
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
}
