package jmri.jmrix.loconet;

import java.util.concurrent.LinkedBlockingQueue;
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
        requestList = new LinkedBlockingQueue<>();
        slotForAddress = new Hashtable<>();
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
        log.debug("requestThrottleSetup: address {}, control {}", address, control);
        if(requestOutstanding) {
           try {
              // queue this request for later.
              requestList.put(new throttleRequest(address,control));
           } catch(InterruptedException ie){
              log.error("Interrupted while trying to store throttle request");
              requestOutstanding = false;
              return;
           }
        } else {
           // handle this now
           requestOutstanding = true;
           processThrottleSetupRequest(address, control);
        }
     }
 
     protected void processQueuedThrottleSetupRequest(){
        if(requestOutstanding) {
           return;
        } else if(requestList.size() != 0 ){
           requestOutstanding = true;
           try {
              throttleRequest tr = requestList.take();
              processThrottleSetupRequest(tr.getAddress(),tr.getControl());
           } catch(InterruptedException ie){
              log.error("Interrupted while trying to process process throttle request");
              requestOutstanding = false;
              return;
           }
        }
     }


     private void processThrottleSetupRequest(LocoAddress address, boolean control) {
        slotManager.slotFromLocoAddress(address.getNumber(), this);  //first try

        class RetrySetup implements Runnable {  //setup for retries and failure check

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
                requestOutstanding = false;
                processQueuedThrottleSetupRequest();
            }
        }
        
        retrySetupThread = new Thread(
                new RetrySetup(new DccLocoAddress(address.getNumber(), 
                        isLongAddress(address.getNumber())), this));
        retrySetupThread.setName("LnThrottleManager RetrySetup "+address);
        retrySetupThread.start();
        waitingForNotification.put(address.getNumber(), retrySetupThread);
    }

    volatile Thread retrySetupThread;
    
    Hashtable<Integer, Thread> waitingForNotification = new Hashtable<Integer, Thread>(5);
    
    Hashtable<Integer, LocoNetSlot> slotForAddress;
    LinkedBlockingQueue<throttleRequest> requestList;
    boolean requestOutstanding = false;

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
        log.debug("notifyChangedSlot - slot {}, slotStatus {}", s.getSlot(), Integer.toHexString(s.slotStatus()));
        // This is invoked only if the SlotManager knows that the LnThrottleManager is 
        // interested in the address associated with this slot.
                
        // need to check to see if the slot is in a suitable state for creating a throttle.
        if (s.slotStatus() == LnConstants.LOCO_IN_USE) {
            // loco is already in-use or is consist-mid or consist-sub 
            log.warn("slot {} address {} is  already in-use.",
                    s.getSlot(), s.locoAddr());
            // is the throttle ID the same as for this JMRI instance?  If not, do not accept the slot.
            if (s.id() != throttleID) {
                // notify the LnThrottleManager about failure of acquisition.
                // NEED TO TRIGGER THE NEW "STEAL REQUIRED" FUNCITONALITY HERE
                //note: throttle listener expects to have "callback" method notifyStealThrottleRequired 
                //invoked if a "steal" is required.  Make that happen as part of the "acquisition" process
                slotForAddress.put(s.locoAddr(),s);
                notifyStealRequest(s.locoAddr());
                return;
            }
        }
        commitToAcquireThrottle(s);
    }

    private void commitToAcquireThrottle(LocoNetSlot s) {
        // haven't identified a particular reason to refuse throttle acquisition at this time...
        DccThrottle throttle = createThrottle((LocoNetSystemConnectionMemo) adapterMemo, s);
        s.notifySlotListeners();    // make sure other listeners for this slot know about what's going on!
        notifyThrottleKnown(throttle, new DccLocoAddress(s.locoAddr(), isLongAddress(s.locoAddr())));
        //end the waiting thread since we got a response
        if (waitingForNotification.containsKey(s.locoAddr())) {
            log.debug("LnThrottleManager.notifyChangedSlot() - removing throttle acquisition notification flagging for address {}", s.locoAddr() );
            waitingForNotification.get(s.locoAddr()).interrupt();
            waitingForNotification.remove(s.locoAddr());
        }
        else {
            log.debug("LnThrottleManager.notifyChangedSlot() - ignoring slot notification for slot {}, address {} account not attempting to acquire that address", s.getSlot(), s.locoAddr() );
        }
        requestOutstanding = false;
        processQueuedThrottleSetupRequest();
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
        requestOutstanding = false;
        processQueuedThrottleSetupRequest();
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
        log.debug("disposeThrottle - throttle {}", t.getLocoAddress());
        if (t instanceof LocoNetThrottle) {
            if (super.disposeThrottle(t, l)) {
                LocoNetThrottle lnt = (LocoNetThrottle) t;
                lnt.throttleDispose();
                return true;
            }
        }
        return false;
    }

    @Override
    public void dispatchThrottle(DccThrottle t, ThrottleListener l) {
        log.debug("dispatchThrottle - throttle {}", t.getLocoAddress());
        // set status to common
        if (t instanceof LocoNetThrottle){
            LocoNetThrottle lnt = (LocoNetThrottle) t;
            LocoNetSlot tSlot = lnt.getLocoNetSlot();

            tc.sendLocoNetMessage(
                    tSlot.writeStatus(LnConstants.LOCO_COMMON));

            // and dispatch to slot 0
            tc.sendLocoNetMessage(tSlot.dispatchSlot());
        }
        super.releaseThrottle(t, l);
    }

    @Override
    public void releaseThrottle(DccThrottle t, ThrottleListener l) {
        log.debug("releaseThrottle - throttle {}", t.getLocoAddress());
        if (t instanceof LocoNetThrottle) {
            LocoNetThrottle lnt = (LocoNetThrottle) t;
            LocoNetSlot tSlot = lnt.getLocoNetSlot();
            if (tSlot != null) {
                tc.sendLocoNetMessage(
                        tSlot.writeStatus(LnConstants.LOCO_COMMON));
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
    }

    protected int throttleID = 0x0171;

    public int getThrottleID(){
        return throttleID;
    }

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
        // need to find the "throttleListener" associated with the request for locoAddr, and
        // send that "throttleListener" a notification that the command station needs 
        // permission to "steal" the loco address.
        if (waitingForNotification.containsKey(locoAddr)) {
            waitingForNotification.get(locoAddr).interrupt();
            waitingForNotification.remove(locoAddr);

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
    public void stealThrottleRequest(LocoAddress address, ThrottleListener l, boolean steal){
       log.debug("stealThrottleRequest() invoked for address {}, with steal boolean = {}",address.getNumber(),steal);
       if (steal == false) {
            failedThrottleRequest((DccLocoAddress) address, "User chose not to 'steal' the throttle.");
       } else {
           log.warn("user agreed to steal address {}, but no code is in-place to handle the 'steal' (yet)",address.getNumber());
        commitToAcquireThrottle(slotForAddress.get(address.getNumber()));
       }
    }   


    /*
     * internal class for holding throttleListener/LocoAddress pairs for 
     * outstanding requests.
     */
    protected class throttleRequest{
         private LocoAddress la = null;
         private boolean tc = false;
         
         throttleRequest(LocoAddress l,boolean control){
             la = l;
             tc = control;
         }

         public boolean getControl(){
            return tc;
         }
         public LocoAddress getAddress(){
            return la;
         }

    }

    private final static Logger log = LoggerFactory.getLogger(LnThrottleManager.class);
}
