package jmri.jmrix.loconet;

import jmri.DccThrottle;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import com.sun.java.util.collections.HashMap;


/**
 * LocoNet implementation of a ThrottleManager.
 * <P>
 * Works in cooperation with the SlotManager, which actually
 * handles the communications.
 *
 * @see SlotManager
 * @author		Bob Jacobsen  Copyright (C) 2001
 * @version 		$Revision: 1.13 $
 */
public class LnThrottleManager implements ThrottleManager, SlotListener {
    private SlotManager slotManager;
    private HashMap throttleListeners;

    /**
     * Constructor. Gets a reference to the LocoNet SlotManager.
     */
    public LnThrottleManager() {
        slotManager = SlotManager.instance();
    }

    /**
     * Request a throttle, given a decoder address. When the decoder address
     * is located, the ThrottleListener gets a callback via the ThrottleListener.notifyThrottleFound
     * method.
     * @param address The decoder address desired.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     * @return True if the request will continue, false if the request will not
     * be made. False may be returned if a the throttle is already in use.
     */
    public boolean requestThrottle(int address, ThrottleListener l)
    {
        boolean throttleInUse = false;
        if (throttleListeners == null) {
            throttleListeners = new HashMap(5);
        }

        Integer addressKey = new Integer(address);
        if (throttleListeners.containsKey(addressKey)) {
            throttleInUse = true;
        } else {
            throttleListeners.put(addressKey, l);
            slotManager.slotFromLocoAddress(address, this);
        }
        return throttleInUse;

    }

    /**
     * Cancel a request for a throttle
     * @param address The decoder address desired.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    public void cancelThrottleRequest(int address, ThrottleListener l) {
        if (throttleListeners != null) {
            Integer addressKey = new Integer(address);
            throttleListeners.remove(addressKey);
        }

    }
    /**
     * SlotListener contract. Get notification that an address has changed slot.
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     */
    public void notifyChangedSlot(LocoNetSlot s) {
        Integer address = new Integer(s.locoAddr());
        ThrottleListener l = (ThrottleListener)throttleListeners.get(address);
        if (l != null) {
            DccThrottle throttle = new LocoNetThrottle(s);
            l.notifyThrottleFound(throttle);
        }
    }


}
