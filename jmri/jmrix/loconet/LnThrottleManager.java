package jmri.jmrix.loconet;

import jmri.DccThrottle;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import com.sun.java.util.collections.HashMap;

import jmri.jmrix.AbstractThrottleManager;

/**
 * LocoNet implementation of a ThrottleManager.
 * <P>
 * Works in cooperation with the SlotManager, which actually
 * handles the communications.
 *
 * @see SlotManager
 * @author		Bob Jacobsen  Copyright (C) 2001
 * @version 		$Revision: 1.17 $
 */
public class LnThrottleManager extends AbstractThrottleManager implements ThrottleManager, SlotListener {
    private SlotManager slotManager;
    private HashMap throttleListeners;

    /**
     * Constructor. Gets a reference to the LocoNet SlotManager.
     */
    public LnThrottleManager() {
    	super();
        slotManager = SlotManager.instance();
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
	public void requestThrottleSetup(int address) {
    	slotManager.slotFromLocoAddress(address, this);
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
    	DccThrottle throttle = new LocoNetThrottle(s);
    	notifyThrottleKnown(throttle, s.locoAddr());
    }


}
