package jmri.jmrix.can.cbus.simulator;

import javax.annotation.Nonnull;

import jmri.DccLocoAddress;
import jmri.InvokeOnLayoutThread;
import jmri.SpeedStepMode;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusSend;

/**
 * A simulated cab on a CAN Simulation connection: a handset outside JMRI,
 * such as a CANCAB, driving a loco through the simulated command station.
 * Its requests go onto the simulated bus as frames from another node, so JMRI
 * sees them as it would a real cab's, and the command station acts on them,
 * replying as it does to JMRI's own.
 * <p>
 * While a cab exists, its command station also acts on requests from the bus,
 * as a real command station does; it acts on JMRI's own requests as before.
 * <p>
 * For tests and scripts. Each request is sent at once; the command station's
 * reply arrives after its usual delay, so wait for {@link #getSession()}
 * after {@link #acquire}.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class CbusSimulatedCab implements CanListener {

    private final CbusDummyCS cs;
    private final boolean csProcessedIn;
    private final TrafficController tc;
    private final CbusSend send;
    private DccLocoAddress address;
    private volatile int session = -1;
    private volatile boolean refused = false;

    /**
     * A cab on a CAN Simulation connection, using its first simulated command
     * station.
     *
     * @param memo the connection
     * @throws IllegalArgumentException if the connection is not CAN
     *                                  Simulation, or has no command station
     */
    @InvokeOnLayoutThread
    public CbusSimulatedCab(@Nonnull CanSystemConnectionMemo memo) {
        // asked only of CAN Simulation: asking a connection to a real bus would make it a simulator
        CbusSimulator sim = (memo.getTrafficController() instanceof jmri.jmrix.can.adapters.loopback.LoopbackTrafficController)
                ? memo.get(CbusSimulator.class) : null;
        if (sim == null || sim.getNumCS() == 0) {
            throw new IllegalArgumentException("a simulated cab needs a CAN Simulation connection with a command station");
        }
        cs = sim.getCS(0);
        csProcessedIn = cs.getProcessIn();
        cs.setProcessIn(true);
        tc = memo.getTrafficController();
        send = new CbusSend(memo);
        tc.addCanListener(this);
    }

    /**
     * Ask the command station for a session for a loco (RLOC).
     *
     * @param loco the loco
     */
    @InvokeOnLayoutThread
    public void acquire(@Nonnull DccLocoAddress loco) {
        address = loco;
        session = -1;
        refused = false;
        int a = loco.getNumber() | (loco.isLongAddress() ? 0xC000 : 0);
        send(CbusConstants.CBUS_RLOC, a >> 8, a & 0xff);
    }

    /**
     * Set the loco's speed and direction (DSPD).
     *
     * @param speedDirection bit 7 set for forward; bits 0 to 6 the speed, in
     *                       the session's speed step mode
     * @throws IllegalStateException if the cab has no session
     */
    @InvokeOnLayoutThread
    public void setSpeedDirection(int speedDirection) {
        send(CbusConstants.CBUS_DSPD, requireSession(), speedDirection & 0xff);
    }

    /**
     * Set the session's speed step mode (STMOD).
     *
     * @param mode 128, 28 or 14 steps
     * @throws IllegalStateException if the cab has no session
     */
    @InvokeOnLayoutThread
    public void setSpeedStepMode(@Nonnull SpeedStepMode mode) {
        int m;
        switch (mode) {
            case NMRA_DCC_28:
                m = CbusConstants.CBUS_SS_28;
                break;
            case NMRA_DCC_14:
                m = CbusConstants.CBUS_SS_14;
                break;
            default:
                m = CbusConstants.CBUS_SS_128;
                break;
        }
        send(CbusConstants.CBUS_STMOD, requireSession(), m);
    }

    /**
     * Release the loco (KLOC).
     *
     * @throws IllegalStateException if the cab has no session
     */
    @InvokeOnLayoutThread
    public void release() {
        send(CbusConstants.CBUS_KLOC, requireSession());
        session = -1;
    }

    /**
     * @return the session the command station gave, or -1 if none yet
     */
    public int getSession() {
        return session;
    }

    /**
     * @return true if the command station refused the last request for a
     *         session, for example because the loco is in use
     */
    public boolean isRefused() {
        return refused;
    }

    /**
     * Stop listening, and leave the command station acting on requests from
     * the bus only if it did before.
     */
    @InvokeOnLayoutThread
    public void dispose() {
        tc.removeCanListener(this);
        cs.setProcessIn(csProcessedIn);
    }

    private int requireSession() {
        if (session < 0) {
            throw new IllegalStateException("the simulated cab has no session");
        }
        return session;
    }

    private void send(int... bytes) {
        CanReply r = new CanReply(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            r.setElement(i, bytes[i]);
        }
        send.sendWithDelay(r, true, false, 0);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Frames JMRI sends are not for this cab.
     */
    @Override
    public void message(CanMessage m) {
        // not for this cab
    }

    /**
     * {@inheritDoc}
     * <p>
     * Takes the session from the command station's report (PLOC) for this
     * cab's loco, or notes its refusal (ERR).
     */
    @Override
    public void reply(CanReply r) {
        if (r.extendedOrRtr() || address == null || r.getNumDataElements() < 4) {
            return;
        }
        int a = ((r.getElement(2) & 0x3f) << 8) | r.getElement(3);
        boolean isLong = (r.getElement(2) & 0xc0) != 0;
        int opc = r.getElement(0);
        if (opc == CbusConstants.CBUS_PLOC && session < 0 && a == address.getNumber()
                && isLong == address.isLongAddress()) {
            session = r.getElement(1);
        } else if (opc == CbusConstants.CBUS_ERR && session < 0) {
            int ea = ((r.getElement(1) & 0x3f) << 8) | r.getElement(2);
            boolean eLong = (r.getElement(1) & 0xc0) != 0;
            if (ea == address.getNumber() && eLong == address.isLongAddress()) {
                refused = true;
            }
        }
    }
}
