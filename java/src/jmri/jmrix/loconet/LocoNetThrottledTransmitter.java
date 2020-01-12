package jmri.jmrix.loconet;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delay LocoNet messages that need to be throttled.
 * <p>
 * A LocoNetThrottledTransmitter object sits in front of a LocoNetInterface
 * (e.g. TrafficHandler) and meters out specific LocoNet messages.
 *
 * <p>
 * The internal Memo class is used to hold the pending message and the time it's
 * to be sent. Time computations are in units of milliseconds, as that's all the
 * accuracy that's needed here.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class LocoNetThrottledTransmitter implements LocoNetInterface {

    public LocoNetThrottledTransmitter(@Nonnull LocoNetInterface controller, boolean mTurnoutExtraSpace) {
        this.controller = controller;
        this.memo = controller.getSystemConnectionMemo();
        this.mTurnoutExtraSpace = mTurnoutExtraSpace;

        // calculation is needed time to send on DCC:
        // msec*nBitsInPacket*packetRepeat/bitRate*safetyFactor
        minInterval = 1000 * (18 + 3 * 10) * 3 / 16000 * 2;

        if (mTurnoutExtraSpace) {
            minInterval = minInterval * 4;
        }

        attachServiceThread();
    }

    /**
     * Reference to the system connection memo.
     */
    LocoNetSystemConnectionMemo memo = null;

    /**
     * Set the system connection memo associated with this traffic controller.
     *
     * @param m associated systemConnectionMemo object
     */
    @Override
    public void setSystemConnectionMemo(LocoNetSystemConnectionMemo m) {
        log.debug("LnTrafficController set memo to {}", m.getUserName());
        memo = m;
    }

    /**
     * Get the system connection memo associated with this traffic controller.
     *
     * @return the associated systemConnectionMemo object
     */
    @Override
    public LocoNetSystemConnectionMemo getSystemConnectionMemo() {
        log.debug("getSystemConnectionMemo {} called in LnTC", memo.getUserName());
        return memo;
    }

    boolean mTurnoutExtraSpace;

    /**
     * Request that server thread cease operation, no more messages can be sent.
     * Note that this returns before the thread is known to be done if it still
     * has work pending.  If you need to be sure it's done, check and wait on
     * !running.
     */
    public void dispose() {
        disposed = true;

        // put a shutdown request on the queue after any existing
        Memo m = new Memo(null, nowMSec(), TimeUnit.MILLISECONDS) {
            @Override
            boolean requestsShutDown() {
                return true;
            }
        };
        queue.add(m);
    }

    volatile boolean disposed = false;
    volatile boolean running = false;

    // interface being shadowed
    LocoNetInterface controller;

    // Forward methods to underlying interface
    @Override
    public void addLocoNetListener(int mask, LocoNetListener listener) {
        controller.addLocoNetListener(mask, listener);
    }

    @Override
    public void removeLocoNetListener(int mask, LocoNetListener listener) {
        controller.removeLocoNetListener(mask, listener);
    }

    @Override
    public boolean status() {
        return controller.status();
    }

    /**
     * Accept a message to be sent after suitable delay.
     */
    @Override
    public void sendLocoNetMessage(LocoNetMessage msg) {
        if (disposed) {
            log.error("Message sent after queue disposed");
            return;
        }

        long sendTime = calcSendTimeMSec();

        Memo m = new Memo(msg, sendTime, TimeUnit.MILLISECONDS);
        queue.add(m);

    }

    // minimum time in msec between messages
    long minInterval;

    long lastSendTimeMSec = 0;

    long calcSendTimeMSec() {
        // next time is at least now or minInterval after latest so far
        lastSendTimeMSec = Math.max(nowMSec(), minInterval + lastSendTimeMSec);
        return lastSendTimeMSec;
    }

    DelayQueue<Memo> queue = new DelayQueue<Memo>();

    private void attachServiceThread() {
        theServiceThread = new ServiceThread();
        theServiceThread.setPriority(Thread.NORM_PRIORITY);
        theServiceThread.setName("LocoNetThrottledTransmitter"); // NOI18N
        theServiceThread.setDaemon(true);
        theServiceThread.start();
    }

    ServiceThread theServiceThread;

    class ServiceThread extends Thread {

        @Override
        public void run() {
            running = true;
            while (true) {
                try {
                    Memo m = queue.take();

                    // check for request to shutdown
                    if (m.requestsShutDown()) {
                        log.debug("item requests shutdown");
                        break;
                    }

                    // normal request
                    if (log.isDebugEnabled()) {
                        log.debug("forwarding message: " + m.getMessage());
                    }
                    controller.sendLocoNetMessage(m.getMessage());
                    // and go round again
                } catch (Exception e) {
                    // just report error and continue
                    log.error("Exception in ServiceThread: ", e);
                }
            }
            running = false;
        }
    }

    // a separate method to ease testing by stopping clock
    static long nowMSec() {
        return System.currentTimeMillis();
    }

    static class Memo implements Delayed {

        public Memo(LocoNetMessage msg, long endTime, TimeUnit unit) {
            this.msg = msg;
            this.endTimeMsec = unit.toMillis(endTime);
        }

        LocoNetMessage getMessage() {
            return msg;
        }

        boolean requestsShutDown() {
            return false;
        }

        long endTimeMsec;
        LocoNetMessage msg;

        @Override
        public long getDelay(TimeUnit unit) {
            long delay = endTimeMsec - nowMSec();
            return unit.convert(delay, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed d) {
            // -1 means this is less than m
            long delta;
            if (d instanceof Memo) {
                delta = this.endTimeMsec - ((Memo)d).endTimeMsec;
            } else {
                delta = this.getDelay(TimeUnit.MILLISECONDS)
                        - d.getDelay(TimeUnit.MILLISECONDS);
            }
            if (delta > 0) {
                return 1;
            } else if (delta < 0) {
                return -1;
            } else {
                return 0;
            }
        }

        // ensure consistent with compareTo
        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o instanceof Delayed) {
                return (compareTo((Delayed) o) == 0);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return (int) (this.getDelay(TimeUnit.MILLISECONDS) & 0xFFFFFF);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LocoNetThrottledTransmitter.class);

}
