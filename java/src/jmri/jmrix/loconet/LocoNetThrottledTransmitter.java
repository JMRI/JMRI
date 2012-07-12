// LocoNetThrottledTransmitter

package jmri.jmrix.loconet;

import java.util.concurrent.Delayed;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * Delay LocoNet messages that need to be throttled.
 * <p>
 * A LocoNetThrottledTransmitter object sits in front of a LocoNetInterface
 * (e.g. TrafficHandler) and meters out specific LocoNet messages.
 * 
 * <p>
 * The internal Memo class is used to hold the pending
 * message and the time it's to be sent. Time computations
 * are in units of milliseconds, as that's all the 
 * accuracy that's needed here.
 * 
 * @author Bob Jacobsen Copyright (C) 2009
 * @version $Revision$
 */
public class LocoNetThrottledTransmitter implements LocoNetInterface {

    public LocoNetThrottledTransmitter(LocoNetInterface controller, boolean mTurnoutExtraSpace) {
        this.controller = controller;
        this.mTurnoutExtraSpace = mTurnoutExtraSpace;
        
        // calculation is needed time to send on DCC:
        // msec*nBitsInPacket*packetRepeat/bitRate*safetyFactor
        minInterval = 1000*(18+3*10)*3/16000*2;

        if (mTurnoutExtraSpace) minInterval = minInterval * 4;
        
        attachServiceThread();
    }
    
    boolean mTurnoutExtraSpace;
    
    /** 
     * Cease operation, no more messages can be sent
     */
    public void dispose() {
        disposed = true;
        
        // put a shutdown request on the queue after any existing
        Memo m = new Memo(null, calcSendTimeMSec(), TimeUnit.MILLISECONDS) {
                        boolean requestsShutDown() { return true; }
        };
        queue.add(m);
    }
    
    boolean disposed = false;
    boolean running = false;
    
    // interface being shadowed
    LocoNetInterface controller;
    
    // Forward methods to underlying interface
    
	public void addLocoNetListener(int mask, LocoNetListener listener) {
	    controller.addLocoNetListener(mask, listener);
	}
	
	public void removeLocoNetListener(int mask, LocoNetListener listener) {
	    controller.removeLocoNetListener(mask, listener);
	}

  	public boolean status() {
  	    return controller.status();
  	}

    /**
     * Accept a message to be sent after suitable delay.
     */  
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
        lastSendTimeMSec = Math.max(nowMSec(), minInterval+lastSendTimeMSec);
        return lastSendTimeMSec;
    }
    
    DelayQueue<Memo> queue = new DelayQueue<Memo>();
    
    private void attachServiceThread() {
        theServiceThread = new ServiceThread();
        theServiceThread.setPriority(Thread.NORM_PRIORITY);
        theServiceThread.setName("LocoNetThrottledTransmitter");
        theServiceThread.setDaemon(true);
        theServiceThread.start();
    }
    
    ServiceThread theServiceThread;
    
    class ServiceThread extends Thread {
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
                    if (log.isDebugEnabled()) 
                        log.debug("forwarding message: "+m.getMessage());
                    controller.sendLocoNetMessage(m.getMessage());
                    // and go round again
                } catch (Exception e) {
                    // just report error and continue
                    log.error("Exception in ServiceThread: "+e);
                    e.printStackTrace();
                }
            }
            running = false;
        }
    }
    
    // a separate method to ease testing by stopping clock
    long nowMSec() {
        return System.currentTimeMillis();
    }

    class Memo implements Delayed {
        public Memo(LocoNetMessage msg, long endTime, TimeUnit unit) {
            this.msg = msg;
            this.endTimeMsec = unit.toMillis(endTime);
        }
        
        LocoNetMessage getMessage() { return msg; }
        
        boolean requestsShutDown() { return false; }
        
        long endTimeMsec;
        LocoNetMessage msg;
        
        public long getDelay(TimeUnit unit) {
            long delay = endTimeMsec - nowMSec();
            return unit.convert(delay, TimeUnit.MILLISECONDS);
        }
                
        public int compareTo(Delayed d) {
            // -1 means this is less than m
            long delta = this.getDelay(TimeUnit.MILLISECONDS) 
                            - d.getDelay(TimeUnit.MILLISECONDS);
            if (delta > 0 ) return 1;
            else if (delta < 0 ) return -1;
            else return 0;
        }
        // ensure consistent with compareTo
        public boolean equals( Object o ) {
            if (o == null) return false;
            if (o instanceof Delayed)
                return (compareTo((Delayed)o) == 0);
            else return false;
        }
        public int hashCode() {
          return (int)(this.getDelay(TimeUnit.MILLISECONDS)&0xFFFFFF);
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoNetThrottledTransmitter.class.getName());

}

/* @(#)LocoNetThrottledTransmitter.java */
