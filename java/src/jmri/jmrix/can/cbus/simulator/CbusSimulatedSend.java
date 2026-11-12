package jmri.jmrix.can.cbus.simulator;

import javax.annotation.Nonnull;

import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusSend;

/**
 * Frames from simulated hardware on a CAN Simulation connection: as
 * {@link CbusSend}, and frames received from the bus at a given time.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class CbusSimulatedSend extends CbusSend {

    /**
     * @param memo the CAN Simulation connection
     */
    public CbusSimulatedSend(@Nonnull CanSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * Have a frame received from the bus at a given time, as a simulated
     * detector does to report when its sensor changed. The frame is given that
     * received time, and arrives in its place among the frames sent with
     * {@link #sendWithDelay}: after every frame due before that time, and
     * before every frame due after it. A time already past arrives at once; a
     * time to come, when it comes.
     *
     * @param r             the frame, as an incoming CanReply
     * @param receivedNanos when it is received, on the
     *                      {@link System#nanoTime()} scale
     */
    public void receiveAt(@Nonnull CanReply r, long receivedNanos) {
        queueReceivedAt(r, receivedNanos);
    }
}
