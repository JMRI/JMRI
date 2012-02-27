package jmri.jmrix.loconet;

import jmri.DccThrottle;
import jmri.ThrottleManager;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.ThrottleListener;
import jmri.jmrix.AbstractThrottleManager;

/**
 * LocoNet implementation of a ThrottleManager.
 * <P>
 * Works in cooperation with the SlotManager, which actually
 * handles the communications.
 *
 * @see SlotManager
 * @author		Bob Jacobsen  Copyright (C) 2001
 * @version 		$Revision$
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
	protected boolean singleUse() { return false; }


	/** 
	 * Start creating a Throttle object.
	 *
	 * This returns directly, having arranged for the Throttle
	 * object to be delivered via callback
	 */
	public void requestThrottleSetup(LocoAddress address, boolean control) {
            slotManager.slotFromLocoAddress(((DccLocoAddress)address).getNumber(), this);
	}
	

    /**
     * LocoNet does have a Dispatch function
     **/
    public boolean hasDispatchFunction(){ return true; }     

    /**
     * SlotListener contract. Get notification that an address has changed slot.
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     */
    public void notifyChangedSlot(LocoNetSlot s) {
    	DccThrottle throttle = new LocoNetThrottle((LocoNetSystemConnectionMemo)adapterMemo, s);
    	notifyThrottleKnown(throttle, new DccLocoAddress(s.locoAddr(),isLongAddress(s.locoAddr()) ) );
    }

    /**
     * Address 128 and above is a long address
     **/
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }
    
    /**
     * Address 127 and below is a short address
     **/
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() { return true; }

    /*
     * Local method for deciding short/long address
     */
    protected static boolean isLongAddress(int num) {
        return (num>=128);
    }

    public boolean disposeThrottle(DccThrottle t, ThrottleListener l){
        if(super.disposeThrottle(t, l)){
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

    public void releaseThrottle(DccThrottle t, ThrottleListener l){
        LocoNetThrottle lnt = (LocoNetThrottle) t;
        LocoNetSlot tSlot = lnt.getLocoNetSlot();
        if (tSlot != null)
        	tc.sendLocoNetMessage(
        			tSlot.writeStatus(LnConstants.LOCO_COMMON));
        super.releaseThrottle(t, l);
    }

}
