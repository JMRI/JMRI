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
 * @author Bob Jacobsen Copyright (C) 2001
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
        slotForAddress = new Hashtable<>();
        throttleRequests = new java.util.HashSet<>(5);
    }

    /**
     * LocoNet allows multiple throttles for the same device
     */
    @Override
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
    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        log.debug("begin of request for LnThrottle for address {}", address);
        queueThrottleRequest(address, control);
    }

    // fields which help manage the process of serialization of overlapping 
    // throttle requests
    java.util.HashSet<Integer> throttleRequests;
    Integer inProgressThrottleAddress = -1;
  
    Hashtable<Integer, LocoNetSlot> slotForAddress;    
    
    private void queueThrottleRequest(LocoAddress address, boolean control) {
        if (throttleRequests.contains(address.getNumber())) {
                return;
        }
        throttleRequests.add(address.getNumber());
        tryBeginNextThrottleRequest();
    }
    
    private void removeThrottleRequestEntry(Integer address) {
        if (throttleRequests.contains(address)) {
            log.debug("Removing throttle request for address {}", address);
            throttleRequests.remove(address);
        } else {
            log.warn("Cannot remove a thottle request for address {} because there is not a request pending.", address);
        }
        if (!throttleRequests.isEmpty()) {
            log.debug("queueing request for loco acquire from the list of unfulfilled throttle requests");
            tryBeginNextThrottleRequest();
        }

    }
    
    private void tryBeginNextThrottleRequest() {
        if (inProgressThrottleAddress == -1) {
            inProgressThrottleAddress = throttleRequests.iterator().next();
            log.debug("tryBeginNextTrottleRequest starting acquisition process for address {}", inProgressThrottleAddress);
            beginThrottleRequest(inProgressThrottleAddress);
        } else {
            log.debug("tryBeginNextTrottleRequest - a request is already in progress so cannot start a new one.");
        }
    }
    
    private void beginThrottleRequest(Integer address) {
        log.debug("beginThrottleRequest- beginning actual acquisition of loco address {}", address);
        
        slotManager.slotFromLocoAddress(address, this);  //first try

        setupRetryThread(new DccLocoAddress(address, isLongAddress(address)), this);
       
        waitingForNotification.put(address, retrySetupThread);
    }

    volatile Thread retrySetupThread;

    Hashtable<Integer, Thread> waitingForNotification = new Hashtable<Integer, Thread>(5);

    /**
     * LocoNet does have a Dispatch function
     *
     */
    @Override
    public boolean hasDispatchFunction() {
        return true;
    }

    /**
     * What speed modes are supported by this system? value should be xor of
     * possible modes specifed by the DccThrottle interface
     */
    @Override
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
     * @param s LocoNet slot which has been changed
     */
    @Override
    public void notifyChangedSlot(LocoNetSlot s) {
        log.debug("notifyChangedSlot - slot {}, slotStatus {}, address {}", s.getSlot(), Integer.toHexString(s.slotStatus()), s.locoAddr());
        if (s.getSlot() >= 120) {
            // system slots may contain data which may appear as a valid loco 
            // address, but is not, so must be ignored.
            log.debug("notifyChangedSlot - ignoring 'system' slot notification");
            return;
        }
        
        if (waitingForNotification.containsKey(s.locoAddr())) {
            // This is invoked only if the SlotManager knows that the LnThrottleManager is
            // interested in the address associated with this slot.
            
            // NOTE: NEED TO REMOVE duplicate slot numbers from slotForAddress 
            // before adding this slot, to prevent a single slot from being inaccurately 
            // associated with multiple different loco addresses.
            // Note that the command station may "change to a different slot number" 
            // during the loco acquire process.
            log.debug("notifychangedSlot - is waiting on notifications from {}", s.locoAddr());

            java.util.Enumeration keySet = slotForAddress.keys();

            while (keySet.hasMoreElements()) {
                Object key = keySet.nextElement();
                if (key.getClass() == Integer.class) {
                    int addr = (Integer)key;
                    LocoNetSlot s2 = slotForAddress.get(addr);
                    if ((s2.getSlot() != s.getSlot()) && (addr != s.locoAddr())) {
                        log.debug("removing slotForAddress for address {} slot {}", addr, s2.getSlot());
                        slotForAddress.remove(addr);
                    }
                }

            }

            // need to check to see if the slot is in a suitable state for creating a throttle.
            if (s.slotStatus() == LnConstants.LOCO_IN_USE) {
                // loco is already in-use or is consist-mid or consist-sub
                log.warn("notifyChangedSlot: slot is marked as 'in-use' for slot {} address {}", s.getSlot(), s.locoAddr());
                // is the throttle ID the same as for this JMRI instance?  If not, do not accept the slot.
                if ((s.id() != throttleID) && (s.id() != 0)) {
                    // notify the LnThrottleManager about failure of acquisition.
                    // NEED TO TRIGGER THE NEW "STEAL REQUIRED" FUNCITONALITY HERE
                    //note: throttle listener expects to have "callback" method notifyStealThrottleRequired
                    //invoked if a "steal" is required.  Make that happen as part of the "acquisition" process
                    log.debug("notifyChangedSlot: slot {} for address {} is for a different throttle - need to steal.", s.getSlot(), s.locoAddr());
                    slotForAddress.put(s.locoAddr(),s);
                    notifyStealRequest(s.locoAddr());
                    return;
                } else {
                    log.debug("notifyChangedSlot for slot {} address {} is being sent to 'commitToAcquireThrottle'", s.getSlot(), s.locoAddr());
                    commitToAcquireThrottle(s);
                    log.debug("deleting entry in waitingForNotification for address {}", s.locoAddr());
                    waitingForNotification.remove(s.locoAddr());
                    inProgressThrottleAddress = -1;
                    removeThrottleRequestEntry(s.locoAddr());
                }
            } else {
                log.debug("notifyChangedSlot for slot {} address {} is other than 'in-use'",s.getSlot(), s.locoAddr());
                log.debug("performing NULL MOVE for for slot {} address {}", s.getSlot(), s.locoAddr());
                LocoNetMessage m = new LocoNetMessage(4);
                m.setElement(0, LnConstants.OPC_MOVE_SLOTS);
                m.setElement(1, s.getSlot());
                m.setElement(2, s.getSlot());
                m.setElement(3,0); // let the send routine figure the checksum
                tc.sendLocoNetMessage(m);
            }
            if ((s.consistStatus() == LnConstants.CONSIST_MID) ||
                    (s.consistStatus() == LnConstants.CONSIST_SUB)) {
                // cannot acquire loco account is consist-mid or consist-sub
                log.warn("slot {} address {} cannot be acquired for loco control account already in-use, consist-mid or consist-sub.",
                        s.getSlot(), s.locoAddr());
                // notify the LnThrottleManager about failure of acquisition.
                notifyRefused(s.locoAddr(), "Locomotive burried in a consist cannot be acquired.");
                waitingForNotification.remove(s.locoAddr());
                inProgressThrottleAddress = -1;
                removeThrottleRequestEntry(s.locoAddr());
                return;
            }
        } else {
            log.warn("was notified of a slot change for slot {} loco address {} which is not currently being acquired.", s.getSlot(), s.locoAddr());
            log.debug("New slot status for address {} is {}", s.locoAddr(), LnConstants.LOCO_STAT(s.slotStatus()));
        }
    }

    private void commitToAcquireThrottle(LocoNetSlot s) {
        // haven't identified a particular reason to refuse throttle acquisition at this time...
        if (waitingForNotification.containsKey(s.locoAddr())) {
            if (s.slotStatus() == LnConstants.LOCO_IN_USE ) {
                DccThrottle throttle = createThrottle((LocoNetSystemConnectionMemo) adapterMemo, s);
                s.notifySlotListeners();    // make sure other listeners for this slot know about what's going on!
                log.debug("Notifying throttle user of concrete throttle for address {}", s.locoAddr());
                notifyThrottleKnown(throttle, new DccLocoAddress(s.locoAddr(), isLongAddress(s.locoAddr())));
                //end the waiting thread since we got a response
                log.debug("LnThrottleManager.commitToAcquireThrottle() - removing throttle acquisition notification flagging for address {}", s.locoAddr() );
                waitingForNotification.get(s.locoAddr()).interrupt();
                waitingForNotification.remove(s.locoAddr());
            } else {
                log.debug("performing NULL MOVE for for slot {} address {}", s.getSlot(), s.locoAddr());
                LocoNetMessage m = new LocoNetMessage(4);
                m.setElement(0, LnConstants.OPC_MOVE_SLOTS);
                m.setElement(1, s.getSlot());
                m.setElement(2, s.getSlot());
                m.setElement(3,0); // let the send routine figure the checksum
                tc.sendLocoNetMessage(m);
            }
        } else {
            log.debug("got commit for slot {} address {} but do not have a pending request for the address", s.getSlot(), s.locoAddr());
        }
    }

    public void notifyRefused(int address, String cause) {
        //end the waiting thread since we got a failure response
        if (waitingForNotification.containsKey(address)) {
            waitingForNotification.get(address).interrupt();
            waitingForNotification.remove(address);
            // notify the throttle - in some other thread!

            class InformRejection implements Runnable {
                // inform the throttle from a new thread, so that
                // the modal dialog box doesn't block other LocoNet
                // message handling

                int address;
                String cause;

                InformRejection(int address, String s) {
                    this.address = address;
                    this.cause = s;
                }

                public void run() {

                    log.debug("New thread launched to inform throttle user of failure to acquire loco {} - {}", address, cause);
                    failedThrottleRequest(new DccLocoAddress(address, isLongAddress(address)), cause);
                }
            }
            Thread thr = new Thread(new InformRejection( address, cause));
            thr.start();
        }

    }


    DccThrottle createThrottle(LocoNetSystemConnectionMemo memo, LocoNetSlot s) {
        log.debug("createThrottle: slot {}", s.getSlot());
        return new LocoNetThrottle(memo, s);
    }

    /**
     * Address 128 and above is a long address
     *
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }

    /**
     * Address 127 and below is a short address
     *
     */
    @Override
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    @Override
    public boolean addressTypeUnique() {
        return true;
    }

    /*
     * Local method for deciding short/long address
     */
    protected static boolean isLongAddress(int num) {
        return (num >= 128);
    }

    @Override
    public boolean disposeThrottle(DccThrottle t, ThrottleListener l) {
        int address = t.getLocoAddress().getNumber();
        log.debug("disposeThrottle - throttle for loco address {}", address);
        if (t instanceof LocoNetThrottle) {
            if (super.disposeThrottle(t, l)) {
                log.debug("disposeThrottle: super.disposeThrottle returned true for loco address {}", address);
                LocoNetThrottle lnt = (LocoNetThrottle) t;
                lnt.throttleDispose();
                return true;
            } else {
                log.debug("disposeThrottle: super.disposeThrottle returned false, so did not dispose the LocoNet throttle for address {}", address);
            }
        }
        log.debug("throttle for address {} is not an instance of LocoNetThrottle, so did not dispose it.", address);
        return false;
    }

    @Override
    public void dispatchThrottle(DccThrottle t, ThrottleListener l) {
        log.debug("dispatchThrottle - throttle {}", t.getLocoAddress());
        if (t instanceof LocoNetThrottle){
            LocoNetThrottle lnt = (LocoNetThrottle) t;
            LocoNetSlot tSlot = lnt.getLocoNetSlot();

            // set status to idle
            // Note: the DT400 throttle marks slot "Idle" when the user
            // "DISP"atches a locomotive

            tc.sendLocoNetMessage(
                    tSlot.writeStatus(LnConstants.LOCO_IDLE));

            // and dispatch to slot 0
            tc.sendLocoNetMessage(tSlot.dispatchSlot());
        }
        setupDispatchDelay(t, l);

    }

    private void finishTheDispatch(DccThrottle t, ThrottleListener l) {
        super.dispatchThrottle(t,l);
    }
    javax.swing.Timer delayedDispatch = null;

    private void setupDispatchDelay(DccThrottle t, ThrottleListener l) {
        delayedDispatch = new javax.swing.Timer(200, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                log.debug("delayed throttle dispatch timer triggered.");
            delayedDispatch.stop();
            finishTheDispatch(t,l);
            }
        });
        delayedDispatch.setRepeats(false);
        delayedDispatch.start();

    }
    @Override
    public void releaseThrottle(DccThrottle t, ThrottleListener l) {
        log.debug("releaseThrottle - throttle {}", t.getLocoAddress());
        if (t instanceof LocoNetThrottle) {
            LocoNetThrottle lnt = (LocoNetThrottle) t;
            LocoNetSlot tSlot = lnt.getLocoNetSlot();
            if (tSlot != null) {
                if (tSlot.slotStatus() == LnConstants.LOCO_IN_USE) {
                    // set status to Common
                    // Note: the DT400 throttle marks slot "Common" when the user
                    // "EXIT"s a locomotive

                    tc.sendLocoNetMessage(
                            tSlot.writeStatus(LnConstants.LOCO_COMMON));
                }
            }
        }
        super.releaseThrottle(t, l);
    }

    @Override
    public void failedThrottleRequest(DccLocoAddress address, String reason) {
        super.failedThrottleRequest(address, reason);
        log.debug("failedThrottleRequest - address {}, reason {}", address, reason);
        //now end and remove any waiting thread
        if (waitingForNotification.containsKey(address.getNumber())) {
            waitingForNotification.get(address.getNumber()).interrupt();
            waitingForNotification.remove(address.getNumber());
        }
        inProgressThrottleAddress = -1;
        removeThrottleRequestEntry(address.getNumber());
                
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
        log.debug("cancelThrottleRequest - address {}", address);
        if (waitingForNotification.containsKey(address)) {
            waitingForNotification.get(address).interrupt();
            waitingForNotification.remove(address);
        }
        inProgressThrottleAddress = -1;
        removeThrottleRequestEntry(address);
    }

    protected int throttleID = 0x0171;
    /**
     * Dispose of this manager, typically for testing
     */
    void dispose() {
        if (retrySetupThread != null) {
            try {
                retrySetupThread.interrupt();
                retrySetupThread.join();
            } catch (InterruptedException ex) {
                log.warn("dispose interrupted");
            }
        }
    }

    /**
     * Inform the requesting throttle object (not the connection-specific throttle
     * implementation!)  that the address is in-use and the throttle user may
     * either choose to "steal" the address, or quit the acquisition process.
     * The LocoNet acquisition process "retry" timer is stopped as part of this
     * process, since a positive response has been received from the command station
     * and since user intervention is required.
     *
     * Reminder: for LocoNet throttles which are not using "expanded slot"
     * functionality, "steal" really means "share".  For those LocoNet throttles
     * which are using "expanded slots", "steal" really means take control and
     * let the command station issue a "StealZap" LocoNet message to the other throttle.
     *
     * @param locoAddr address of DCC loco or consist
     */
    public void notifyStealRequest(int locoAddr) {
        // first, stop the retry timer thread - don't want it running while waiting 
        // for the user input!
        retrySetupThread.interrupt();
        try {
            retrySetupThread.join();
        } catch (InterruptedException ex) {
            log.debug("InterruptedException happened while killing the retry thread; this is to be ignored.");
        }

        // need to find the "throttleListener" associated with the request for locoAddr, and
        // send that "throttleListener" a notification that the command station needs
        // permission to "steal" the loco address.
        if (waitingForNotification.containsKey(locoAddr)) {
            notifyStealRequest(new DccLocoAddress(locoAddr, isLongAddress(locoAddr)));
        }
    }

    /**
     * Perform the actual "Steal" of the requested throttle.
     * <p>
     * This is a call-back, as a result of the throttle user's agreement to
     * "steal" the locomotive.
     * <p>
     * Reminder: for LocoNet throttles which are not using "expanded slot"
     * functionality, "steal" really means "share".  For those LocoNet throttles
     * which are using "expanded slots", "steal" really means "force any other
     * throttle running that address to drop the loco".
     * <p>
     * @param address desired DccLocoAddress
     * @param l  ThrottleListener requesting the throttle steal occur.
     * @param steal true if the request should continue, false otherwise.
     * @since 4.9.2
     */
    @Override
    public void stealThrottleRequest(DccLocoAddress address, ThrottleListener l, boolean steal){
       log.debug("stealThrottleRequest() invoked for address {}, with steal boolean = {}",address.getNumber(),steal);
       if (steal == false) {
            failedThrottleRequest(address, "User chose not to 'steal' the throttle.");
       } else {
           log.warn("user agreed to steal address {}, but no code is in-place to handle the 'steal' (yet)",address.getNumber());
           setupRetryThread(address, this);
        commitToAcquireThrottle(slotForAddress.get(address.getNumber()));
       }
    }

    private void setupRetryThread(DccLocoAddress address, SlotListener listen) {
        log.debug("setupRetryThread - setting up retry thread for address {}", address.getNumber());
        retrySetupThread = new Thread(new RetrySetup(address, listen));
        retrySetupThread.setName("LnThrottleManager RetrySetup "+address);
        retrySetupThread.start();
    }
    
    private class RetrySetup implements Runnable {  //setup for retries and failure check

        DccLocoAddress address;
        SlotListener list;

        RetrySetup(DccLocoAddress address, SlotListener list) {
            this.address = address;
            this.list = list;
        }

        @Override
        public void run() {
            int attempts = 1;  //already tried once above
            int maxAttempts = 10;
            while (attempts <= maxAttempts) {
                try {
                    Thread.sleep(1000);  //wait one second
                } catch (InterruptedException ex) {
                    log.debug("ending because interrupted");
                    return;  //stop waiting if slot is found or error occurs
                }
                String msg = "No response to slot request for {}, attempt {}"; // NOI18N
                if (attempts < maxAttempts) {
                    slotManager.slotFromLocoAddress(address.getNumber(), list);
                    msg += ", trying again."; // NOI18N
                }
                log.debug(msg, address, attempts);
                attempts++;
            }
            log.error("No response to slot request for {} after {} attempts.", address, attempts - 1); // NOI18N
            failedThrottleRequest(address, "Failed to get response from command station");
        }
    }
    private final static Logger log = LoggerFactory.getLogger(LnThrottleManager.class);
}
