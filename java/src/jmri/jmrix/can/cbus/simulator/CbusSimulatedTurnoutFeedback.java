package jmri.jmrix.can.cbus.simulator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InvokeOnLayoutThread;
import jmri.Manager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusSensor;
import jmri.jmrix.can.cbus.CbusTurnout;
import jmri.util.ThreadingUtil;

/**
 * Simulated turnout motors with feedback on a CAN Simulation connection.
 * <p>
 * When a CBUS turnout of this connection is commanded, by JMRI or by any
 * other node on the simulated bus, the turnout motor answers as a module with
 * feedback would: after its travel time, the turnout's feedback sensors report
 * the new position, with the frames those sensors themselves send. A turnout
 * with one feedback sensor ({@link Turnout#ONESENSOR}) has it active when
 * thrown and inactive when closed. A turnout with two
 * ({@link Turnout#TWOSENSOR}) has the first active and the second inactive
 * when thrown, and the other way round when closed. Only feedback sensors that
 * are CBUS sensors of this connection are reported. Turnouts with any other
 * feedback mode get no answer, as before.
 * <p>
 * Off until {@link #setEnabled(boolean) enabled}; while off it listens to
 * nothing.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusSimulatedTurnoutFeedback implements CanListener {

    /** The travel time a turnout motor takes, unless set otherwise. */
    public static final int DEFAULT_TRAVEL_MS = 300;

    private final CanSystemConnectionMemo memo;
    private final CbusSimulatedSend send;
    private volatile int travelMs = DEFAULT_TRAVEL_MS;
    private boolean enabled = false; // layout thread (Swing thread) only

    /**
     * @param memo the CAN Simulation connection
     */
    public CbusSimulatedTurnoutFeedback(@Nonnull CanSystemConnectionMemo memo) {
        this.memo = memo;
        this.send = new CbusSimulatedSend(memo);
    }

    /**
     * Start or stop answering turnout commands.
     *
     * @param on true to answer them
     */
    @InvokeOnLayoutThread
    public void setEnabled(boolean on) {
        if (on == enabled) {
            return;
        }
        enabled = on;
        if (on) {
            memo.getTrafficController().addCanListener(this);
        } else {
            memo.getTrafficController().removeCanListener(this);
        }
    }

    /**
     * @return true if turnout commands are answered
     */
    @InvokeOnLayoutThread
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set how long a turnout motor takes to move before its feedback reports.
     *
     * @param ms milliseconds, 0 or more
     */
    public void setTravelTimeMs(int ms) {
        if (ms < 0) {
            throw new IllegalArgumentException("Travel time can't be negative: " + ms);
        }
        travelMs = ms;
    }

    /**
     * @return how long a turnout motor takes to move, in milliseconds
     */
    public int getTravelTimeMs() {
        return travelMs;
    }

    /**
     * A frame JMRI sends.
     * {@inheritDoc}
     */
    @Override
    public void message(CanMessage m) {
        frameOnBus(m);
    }

    /**
     * A frame another node sends.
     * {@inheritDoc}
     */
    @Override
    public void reply(CanReply r) {
        frameOnBus(r);
    }

    private void frameOnBus(AbstractMessage frame) {
        long due = System.nanoTime() + travelMs * 1_000_000L;
        ThreadingUtil.runOnLayoutEventually(() -> {
            if (!enabled) {
                return;
            }
            for (Turnout t : InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet()) {
                if (!(t instanceof CbusTurnout) || !ofThisConnection(t.getSystemName())) {
                    continue;
                }
                CbusTurnout ct = (CbusTurnout) t;
                if (sameEvent(frame, ct.getAddrThrown())) {
                    answer(ct, true, due);
                } else if (sameEvent(frame, ct.getAddrClosed())) {
                    answer(ct, false, due);
                }
            }
        });
    }

    private void answer(CbusTurnout t, boolean thrown, long due) {
        int mode = t.getFeedbackMode();
        if (mode == Turnout.ONESENSOR) {
            report(t.getFirstSensor(), thrown, due);
        } else if (mode == Turnout.TWOSENSOR) {
            report(t.getFirstSensor(), thrown, due);
            report(t.getSecondSensor(), !thrown, due);
        }
    }

    private void report(@CheckForNull Sensor s, boolean active, long due) {
        if (!(s instanceof CbusSensor) || !ofThisConnection(s.getSystemName())) {
            return;
        }
        CbusSensor cs = (CbusSensor) s;
        CanMessage m = active ? cs.getAddrActive() : cs.getAddrInactive();
        send.receiveAt(new CanReply(m), due);
    }

    private boolean ofThisConnection(String systemName) {
        return memo.getSystemPrefix().equals(Manager.getSystemPrefix(systemName));
    }

    /**
     * Whether a frame on the bus is the given event, by its data bytes. The
     * CAN header (priority and CAN ID) is not compared, as any node may send
     * the event.
     */
    private static boolean sameEvent(AbstractMessage frame, CanMessage event) {
        int n = event.getNumDataElements();
        if (frame.getNumDataElements() != n) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (frame.getElement(i) != event.getElement(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Stop answering.
     */
    @InvokeOnLayoutThread
    public void dispose() {
        setEnabled(false);
    }

}
