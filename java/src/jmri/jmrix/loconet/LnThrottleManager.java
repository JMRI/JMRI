package jmri.jmrix.loconet;

import java.util.Hashtable;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocoNet implementation of a ThrottleManager.
 * <P>
 * Works in cooperation with the SlotManager, which actually handles the
 * communications.
 *
 * @see SlotManager
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version $Revision$
 */
public class LnThrottleManager extends AbstractThrottleManager implements ThrottleManager, SlotListener {

    protected SlotManager slotManager;
    protected LnTrafficController tc;

    /**
     * Constructor. Gets a reference to the LocoNet SlotManager.
     */
    public LnThrottleManager(LocoNetSystemConnectionMemo memo) {
        super(memo);
        this.slotManager = memo.getSlotManager();//slotManager;
        this.tc = memo.getLnTrafficController();
    }

    /**
     * LocoNet allows multiple throttles for the same device
     */
    protected boolean singleUse() {
        return false;
    }

    /**
     * Start creating a Throttle object.
     *
     * This returns directly, having arranged for the Throttle object to be
     * delivered via callback since there are situations where the command
     * station does not respond, (slots full, command station powered off,
     * others?) this code will retry and then fail the request if no response
     * occurs
     */
    public void requestThrottleSetup(LocoAddress address, boolean control) {

        slotManager.slotFromLocoAddress(((DccLocoAddress) address).getNumber(), this);  //first try

        class RetrySetup implements Runnable {  //setup for retries and failure check

            DccLocoAddress address;
            SlotListener list;

            RetrySetup(DccLocoAddress address, SlotListener list) {
                this.address = address;
                this.list = list;
            }

            public void run() {
                int attempts = 1;  //already tried once above
                int maxAttempts = 2;
                while (attempts <= maxAttempts) {
                    try {
                        Thread.sleep(1000);  //wait one second
                    } catch (InterruptedException ex) {
                        return;  //stop waiting if slot is found or error occurs
                    }
                    String msg = "No response to slot request for {}, attempt {}";
                    if (attempts < maxAttempts) {
                        slotManager.slotFromLocoAddress(address.getNumber(), list);
                        msg += ", trying again.";
                    }
                    log.debug(msg, address, attempts);
                    attempts++;
                }
                log.error("No response to slot request for {} after {} attempts.", address, attempts - 1);
                failedThrottleRequest(address, "Failed to get response from command station");
            }
        }
        Thread thr = new Thread(new RetrySetup((DccLocoAddress) address, this));
        thr.start();
        waitingForNotification.put(((DccLocoAddress) address).getNumber(), thr);
    }

    Hashtable<Integer, Thread> waitingForNotification = new Hashtable<Integer, Thread>(5);

    /**
     * LocoNet does have a Dispatch function
     *
     */
    public boolean hasDispatchFunction() {
        return true;
    }

    /**
     * What speed modes are supported by this system? value should be xor of
     * possible modes specifed by the DccThrottle interface
     */
    public int supportedSpeedModes() {
        return (DccThrottle.SpeedStepMode128
                | DccThrottle.SpeedStepMode28
                | DccThrottle.SpeedStepMode28Mot
                | DccThrottle.SpeedStepMode14);
    }

    /**
     * Get notification that an address has changed slot. This method creates a
     * throttle for all ThrottleListeners of that address and notifies them via
     * the ThrottleListener.notifyThrottleFound method.
     */
    public void notifyChangedSlot(LocoNetSlot s) {
        DccThrottle throttle = createThrottle((LocoNetSystemConnectionMemo) adapterMemo, s);
        notifyThrottleKnown(throttle, new DccLocoAddress(s.locoAddr(), isLongAddress(s.locoAddr())));
        //end the waiting thread since we got a response
        if (waitingForNotification.containsKey(s.locoAddr())) {
            waitingForNotification.get(s.locoAddr()).interrupt();
            waitingForNotification.remove(s.locoAddr());
        }
    }

    DccThrottle createThrottle(LocoNetSystemConnectionMemo memo, LocoNetSlot s) {
        return new LocoNetThrottle(memo, s);
    }

    /**
     * Address 128 and above is a long address
     *
     */
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }

    /**
     * Address 127 and below is a short address
     *
     */
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() {
        return true;
    }

    /*
     * Local method for deciding short/long address
     */
    protected static boolean isLongAddress(int num) {
        return (num >= 128);
    }

    public boolean disposeThrottle(DccThrottle t, ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            LocoNetThrottle lnt = (LocoNetThrottle) t;
            lnt.throttleDispose();
            return true;
        }
        return false;
        //LocoNetSlot tSlot = lnt.getLocoNetSlot();
    }

    public void dispatchThrottle(DccThrottle t, ThrottleListener l) {
        // set status to common
        LocoNetThrottle lnt = (LocoNetThrottle) t;
        LocoNetSlot tSlot = lnt.getLocoNetSlot();

        tc.sendLocoNetMessage(
                tSlot.writeStatus(LnConstants.LOCO_COMMON));

        // and dispatch to slot 0
        tc.sendLocoNetMessage(tSlot.dispatchSlot());

        super.releaseThrottle(t, l);
    }

    public void releaseThrottle(DccThrottle t, ThrottleListener l) {
        LocoNetThrottle lnt = (LocoNetThrottle) t;
        LocoNetSlot tSlot = lnt.getLocoNetSlot();
        if (tSlot != null) {
            tc.sendLocoNetMessage(
                    tSlot.writeStatus(LnConstants.LOCO_COMMON));
        }
        super.releaseThrottle(t, l);
    }

    @Override
    public void failedThrottleRequest(DccLocoAddress address, String reason) {
        super.failedThrottleRequest(address, reason);
        //now end and remove any waiting thread
        if (waitingForNotification.containsKey(address.getNumber())) {
            waitingForNotification.get(address.getNumber()).interrupt();
            waitingForNotification.remove(address.getNumber());
        }
    }

    /**
     * Cancel a request for a throttle
     *
     * @param address The decoder address desired.
     * @param isLong  True if this is a request for a DCC long (extended)
     *                address.
     * @param l       The ThrottleListener cancelling request for a throttle.
     */
    @Override
    public void cancelThrottleRequest(int address, boolean isLong, ThrottleListener l) {
        super.cancelThrottleRequest(address, isLong, l);
        if (waitingForNotification.containsKey(address)) {
            waitingForNotification.get(address).interrupt();
            waitingForNotification.remove(address);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LnThrottleManager.class.getName());
}
