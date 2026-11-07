package jmri.jmrix.can.cbus.simulator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.DccLocoAddress;
import jmri.InvokeOnAnyThread;
import jmri.SpeedStepMode;
import jmri.jmrit.simulation.SimulatedSpeedSource;

/**
 * Speed commands for simulated trains from a CAN Simulation connection's
 * simulated command stations: what each session's loco is told, as its
 * decoder would receive it, whichever cab set it, JMRI's or another on the
 * bus.
 * <p>
 * A session's speed and direction byte is read in the session's speed step
 * mode as the NMRA standard for the DCC speed and direction instruction
 * (S-9.2 and S-9.2.1) gives it, not with JMRI's CBUS throttle code, so a
 * fault there shows as a train moving differently from what was asked:
 * <ul>
 * <li>128 steps: 0 stop, 1 emergency stop, 2 to 127 steps 1 to 126;</li>
 * <li>28 steps: 0 and 1 stop, 2 and 3 emergency stop, 4 to 31 steps 1 to 28;</li>
 * <li>14 steps: 0 stop, 1 emergency stop, 2 to 15 steps 1 to 14.</li>
 * </ul>
 * Bit 7 is set for forward. A command is received when the command station
 * changes the session. When a session ends, its loco keeps its last command,
 * as a decoder keeps running when its packets stop.
 * <p>
 * Nothing is listened to until a placed train obeys an address
 * ({@link #watch(Set)}).
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@javax.annotation.concurrent.ThreadSafe
public class CbusSimulatedSpeedSource implements SimulatedSpeedSource {

    private final CbusSimulator simulator;
    private final Map<DccLocoAddress, Command> commands = new ConcurrentHashMap<>();
    private final List<CommandListener> listeners = new CopyOnWriteArrayList<>();
    private final List<CbusDummyCS> watched = new CopyOnWriteArrayList<>();
    private volatile boolean disposed = false;

    private final CbusDummyCS.SessionListener sessionListener = new CbusDummyCS.SessionListener() {
        @Override
        public void sessionChanged(@Nonnull CbusDummyCSSession session) {
            received(session);
        }

        @Override
        public void sessionEnded(@Nonnull CbusDummyCSSession session) {
            // the decoder keeps its last command
        }
    };

    /**
     * @param simulator the CAN Simulation connection's simulator, whose
     *                  command stations to follow
     */
    public CbusSimulatedSpeedSource(@Nonnull CbusSimulator simulator) {
        this.simulator = simulator;
    }

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
     * <p>
     * Follows the command stations' sessions while any address is obeyed,
     * reading the sessions already open when it starts.
     */
    @Override
    @InvokeOnAnyThread
    public synchronized void watch(@Nonnull Set<DccLocoAddress> addresses) {
        if (addresses.isEmpty() || disposed) {
            stopWatching();
            return;
        }
        for (CbusDummyCS cs : simulator.getCommandStations()) {
            if (!watched.contains(cs)) {
                watched.add(cs);
                cs.addSessionListener(sessionListener);
                for (CbusDummyCSSession s : cs.getSessions()) {
                    received(s);
                }
            }
        }
    }

    /**
     * @return true while following the command stations' sessions
     */
    boolean isFollowing() {
        return !watched.isEmpty();
    }

    private void stopWatching() {
        for (CbusDummyCS cs : watched) {
            cs.removeSessionListener(sessionListener);
        }
        watched.clear();
    }

    /**
     * Stop following the command stations, for good.
     */
    @InvokeOnAnyThread
    public synchronized void dispose() {
        disposed = true;
        stopWatching();
        commands.clear();
    }

    private void received(CbusDummyCSSession session) {
        DccLocoAddress address = session.getLocoAddress();
        Command now = command(session.getSpeedDirection(), session.getSpeedStepMode(), System.nanoTime());
        Command old = commands.get(address);
        if (old != null && sameInstruction(old, now)) {
            return; // the same packet again: nothing changes for the decoder
        }
        commands.put(address, now);
        for (CommandListener l : listeners) {
            l.commandChanged(address);
        }
    }

    private static boolean sameInstruction(Command a, Command b) {
        return a.getSpeedStep() == b.getSpeedStep() && a.getMode() == b.getMode()
                && a.isForward() == b.isForward() && a.isEmergencyStop() == b.isEmergencyStop();
    }

    /**
     * A session's speed and direction byte as a decoder in that speed step
     * mode receives it, by the NMRA standard. A value beyond the mode's range
     * is taken as its top step.
     *
     * @param speedDirection the byte: bit 7 set for forward, bits 0 to 6 the
     *                       speed
     * @param mode           the session's speed step mode
     * @param timeNanos      when it was received
     * @return the command
     */
    @Nonnull
    static Command command(int speedDirection, @Nonnull SpeedStepMode mode, long timeNanos) {
        boolean forward = (speedDirection & 0x80) != 0;
        int v = speedDirection & 0x7f;
        int step;
        boolean estop;
        if (mode == SpeedStepMode.NMRA_DCC_28) {
            estop = v == 2 || v == 3;
            step = v < 4 ? 0 : v - 3;
        } else {
            estop = v == 1;
            step = v < 2 ? 0 : v - 1;
        }
        SpeedStepMode m = (mode == SpeedStepMode.NMRA_DCC_28 || mode == SpeedStepMode.NMRA_DCC_14)
                ? mode : SpeedStepMode.NMRA_DCC_128;
        step = Math.min(step, m.numSteps);
        return new Command(step, m, forward, estop, timeNanos);
    }
}
