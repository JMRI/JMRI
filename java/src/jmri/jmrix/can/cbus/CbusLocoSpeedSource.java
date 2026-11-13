package jmri.jmrix.can.cbus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

import jmri.DccLocoAddress;
import jmri.Disposable;
import jmri.LocoAddress;
import jmri.LocoSpeedSource;
import jmri.jmrix.can.CanFrame;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;

/**
 * The speed and direction commanded for each loco on a CBUS layout, from the
 * command station's sessions as they cross the bus: every cab's, not only
 * JMRI's.
 * <p>
 * A command station gives each loco it drives a session, and every speed and
 * direction command for the loco carries the session's number, whichever cab
 * sent it. This source learns each session's loco address when the command
 * station reports the session (PLOC), and then reports, by loco address:
 * <ul>
 * <li>each speed and direction command (DSPD), and the speed and direction a
 * session starts with;</li>
 * <li>an emergency stop of every loco (ESTOP, RESTP), as an emergency stop of
 * each session's loco;</li>
 * <li>the end of a session (KLOC, or an error saying the session is not
 * present or has been cancelled) as the release of its loco.</li>
 * </ul>
 * A session's speed step mode is followed (STMOD), and speeds are read as
 * JMRI's own CBUS throttle reads them.
 * <p>
 * It only listens; it sends nothing. So a session that started before it
 * began listening is not reported until the command station reports that
 * session again: a command for a session it doesn't know is ignored. A loco
 * driven through a consist's session is reported under the consist's address.
 * <p>
 * Listeners are told on the thread the traffic controller delivers frames on.
 * The time of a frame received is the time the traffic controller received
 * it; the time of a frame JMRI sends is when this source was told of it.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class CbusLocoSpeedSource implements LocoSpeedSource, CanListener, Disposable {

    /** A session's loco, and how its speeds are coded. */
    private static final class Session {

        final DccLocoAddress address;
        int stepMode = CbusConstants.CBUS_SS_128;
        float speed;
        boolean forward;

        Session(DccLocoAddress address) {
            this.address = address;
        }
    }

    @CheckForNull
    private final TrafficController tc;
    @GuardedBy("this")
    private final Map<Integer, Session> sessions = new HashMap<>();
    private final CopyOnWriteArrayList<LocoSpeedSource.Listener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Start listening to a CBUS connection's traffic. A connection with no
     * traffic controller (as yet) is not listened to.
     *
     * @param memo the connection
     */
    public CbusLocoSpeedSource(@Nonnull CanSystemConnectionMemo memo) {
        this.tc = memo.getTrafficController();
        addTc(tc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLocoSpeedListener(@Nonnull LocoSpeedSource.Listener listener) {
        listeners.addIfAbsent(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLocoSpeedListener(@Nonnull LocoSpeedSource.Listener listener) {
        listeners.remove(listener);
    }

    /**
     * A frame JMRI sends: JMRI's own cabs' commands.
     * {@inheritDoc}
     */
    @Override
    public void message(CanMessage m) {
        handle(m, System.nanoTime());
    }

    /**
     * A frame received: other cabs' commands and the command station's
     * reports.
     * {@inheritDoc}
     */
    @Override
    public void reply(CanReply r) {
        handle(r, r.getReceivedTime());
    }

    /**
     * Follow one frame.
     *
     * @param m         the frame
     * @param timeNanos when it was received or sent
     */
    private void handle(@Nonnull CanFrame m, long timeNanos) {
        if (m.extendedOrRtr() || m.getNumDataElements() < 1) {
            return;
        }
        switch (m.getElement(0)) {
            case CbusConstants.CBUS_PLOC:
                if (m.getNumDataElements() >= 5) {
                    int number = (m.getElement(2) & 0x3f) * 256 + m.getElement(3);
                    boolean isLong = (m.getElement(2) & 0xc0) != 0;
                    sessionStarted(m.getElement(1), new DccLocoAddress(number, isLong), m.getElement(4), timeNanos);
                }
                break;
            case CbusConstants.CBUS_DSPD:
                if (m.getNumDataElements() >= 3) {
                    speedCommanded(m.getElement(1), m.getElement(2), timeNanos);
                }
                break;
            case CbusConstants.CBUS_STMOD:
                if (m.getNumDataElements() >= 3) {
                    stepModeSet(m.getElement(1), m.getElement(2));
                }
                break;
            case CbusConstants.CBUS_KLOC:
                if (m.getNumDataElements() >= 2) {
                    sessionEnded(m.getElement(1), timeNanos);
                }
                break;
            case CbusConstants.CBUS_ERR:
                if (m.getNumDataElements() >= 4 && (m.getElement(3) == CbusConstants.ERR_SESSION_NOT_PRESENT
                        || m.getElement(3) == CbusConstants.ERR_SESSION_CANCELLED)) {
                    sessionEnded(m.getElement(1), timeNanos);
                }
                break;
            case CbusConstants.CBUS_ESTOP:
            case CbusConstants.CBUS_RESTP:
                allStopped(timeNanos);
                break;
            default:
                break;
        }
    }

    private void sessionStarted(int session, @Nonnull DccLocoAddress address, int speedDir, long timeNanos) {
        Session s = new Session(address);
        synchronized (this) {
            Session old = sessions.get(session);
            if (old != null && old.address.equals(address)) {
                s.stepMode = old.stepMode;
            }
            s.speed = toSpeedSetting(speedDir & 0x7f, s.stepMode);
            s.forward = (speedDir & 0x80) != 0;
            sessions.put(session, s);
        }
        tellSpeed(s.address, s.speed, s.forward, timeNanos);
    }

    private void speedCommanded(int session, int speedDir, long timeNanos) {
        float speed;
        boolean forward;
        DccLocoAddress address;
        synchronized (this) {
            Session s = sessions.get(session);
            if (s == null) {
                return; // not known: can't say which loco
            }
            speed = toSpeedSetting(speedDir & 0x7f, s.stepMode);
            forward = (speedDir & 0x80) != 0;
            if (Float.compare(speed, s.speed) == 0 && forward == s.forward) {
                return; // a repeat, as cabs send
            }
            s.speed = speed;
            s.forward = forward;
            address = s.address;
        }
        tellSpeed(address, speed, forward, timeNanos);
    }

    private synchronized void stepModeSet(int session, int mode) {
        Session s = sessions.get(session);
        if (s != null) {
            s.stepMode = mode & 0x03;
        }
    }

    private void sessionEnded(int session, long timeNanos) {
        Session s;
        synchronized (this) {
            s = sessions.remove(session);
        }
        if (s != null) {
            for (LocoSpeedSource.Listener l : listeners) {
                try {
                    l.released(s.address, timeNanos);
                } catch (RuntimeException e) {
                    log.error("A loco speed listener failed on the release of {}", s.address, e);
                }
            }
        }
    }

    private void allStopped(long timeNanos) {
        Map<DccLocoAddress, Boolean> stopped = new HashMap<>();
        synchronized (this) {
            for (Session s : sessions.values()) {
                if (s.speed >= 0) {
                    s.speed = -1f;
                    stopped.put(s.address, s.forward);
                }
            }
        }
        stopped.forEach((address, forward) -> tellSpeed(address, -1f, forward, timeNanos));
    }

    private void tellSpeed(@Nonnull LocoAddress address, float speed, boolean forward, long timeNanos) {
        for (LocoSpeedSource.Listener l : listeners) {
            try {
                l.speedCommanded(address, speed, forward, timeNanos);
            } catch (RuntimeException e) {
                log.error("A loco speed listener failed on {}", address, e);
            }
        }
    }

    /**
     * Read a CBUS speed as a throttle's speed setting, as JMRI's CBUS throttle
     * does.
     *
     * @param speed the speed, without the direction bit: 0 to 127
     * @param mode  the session's speed step mode, as STMOD gives it
     * @return the speed setting, 0 to 1, or -1 for an emergency stop
     */
    static float toSpeedSetting(int speed, int mode) {
        switch (mode) {
            case CbusConstants.CBUS_SS_28:
            case CbusConstants.CBUS_SS_28_INTERLEAVE:
                if (speed <= 1) {
                    return 0f;
                }
                if (speed <= 3) {
                    return -1f;
                }
                return Math.min((speed - 3) / 28f, 1f);
            case CbusConstants.CBUS_SS_14:
                return speed == 0 ? 0f : speed == 1 ? -1f : Math.min((speed - 1) / 14f, 1f);
            case CbusConstants.CBUS_SS_128:
            default:
                return speed == 0 ? 0f : speed == 1 ? -1f : (speed - 1) / 126f;
        }
    }

    /**
     * The loco address of a session, if known.
     *
     * @param session the session number
     * @return the address, or null if the session isn't known
     */
    @CheckForNull
    synchronized DccLocoAddress getAddress(int session) {
        Session s = sessions.get(session);
        return s == null ? null : s.address;
    }

    /**
     * Stop listening.
     */
    @Override
    public void dispose() {
        removeTc(tc);
        listeners.clear();
        synchronized (this) {
            sessions.clear();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusLocoSpeedSource.class);
}
