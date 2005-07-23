package jmri.jmrix;

import jmri.DccThrottle;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import com.sun.java.util.collections.HashMap;
import com.sun.java.util.collections.ArrayList;

/**
 * Abstract implementation of a ThrottleManager.
 * <P>
 * Based on Glen Oberhauser's original LnThrottleManager implementation.
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version     $Revision: 1.12 $
 */
abstract public class AbstractThrottleManager implements ThrottleManager {
	
	/**
	 * throttleListeners is indexed by the address, and
	 * contains as elements an ArrayList of ThrottleListener
	 * objects.  This allows more than one to request a throttle
	 * at a time, but @see{singleUse}.
	 */
    private HashMap throttleListeners;

	/**
	 * Does this DCC system allow a Throttle (e.g. an address) to be used
	 * by only one user at a time?
	 */
	protected boolean singleUse() { return true; }

    /**
     * Request a throttle, given a decoder address. When the decoder address
     * is located, the ThrottleListener gets a callback via the ThrottleListener.notifyThrottleFound
     * method.
     * @param address The decoder address desired.
     * @param isLong True if this is a request for a DCC long (extended) address.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     * @return True if the request will continue, false if the request will not
     * be made. False may be returned if a the throttle is already in use.
     */
    public boolean requestThrottle(int address, boolean isLong, ThrottleListener l) {
        boolean throttleFree = true;
        if (throttleListeners == null) {
            throttleListeners = new HashMap(5);
        }

		// put the list in if not present
        Integer addressKey = new Integer(address);
		if (!throttleListeners.containsKey(addressKey))
			throttleListeners.put(addressKey, new ArrayList());

		// get the corresponding list to check length
		ArrayList a = (ArrayList)throttleListeners.get(addressKey);
		
		// check length
        if (singleUse() && (a.size()>0)) {
            throttleFree= false;
        } else if (a.size() == 0) {
        	a.add(l);
            requestThrottleSetup(address);
        } else {
        	a.add(l);
        }
        return throttleFree;
    }

    /**
     * Request a throttle, given a decoder address. When the decoder address
     * is located, the ThrottleListener gets a callback via the ThrottleListener.notifyThrottleFound
     * method.
     * <P>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     * @param address The decoder address desired.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     * @return True if the request will continue, false if the request will not
     * be made. False may be returned if a the throttle is already in use.
     */
    public boolean requestThrottle(int address, ThrottleListener l) {
        boolean isLong = true;
        if (canBeShortAddress(address)) isLong = false;
        return requestThrottle(address, isLong, l);
    }

    /**
     * Abstract member to actually do the work of configuring a new throttle,
     * perhaps via interaction with the DCC system
     */
    abstract public void requestThrottleSetup(int address);

    /**
     * Cancel a request for a throttle
     * @param address The decoder address desired.
     * @param isLong True if this is a request for a DCC long (extended) address.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    public void cancelThrottleRequest(int address, boolean isLong, ThrottleListener l) {
        if (throttleListeners != null) {
            Integer addressKey = new Integer(address);
			ArrayList a = (ArrayList)throttleListeners.get(addressKey);
			if (a==null) return;
			for (int i = 0; i<a.size(); i++) {
				if (l == a.get(i))
					a.remove(i);
			}
        }
    }

    /**
     * Cancel a request for a throttle.
     * <P>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     * @param address The decoder address desired.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    public void cancelThrottleRequest(int address, ThrottleListener l) {
        boolean isLong = true;
        if (canBeShortAddress(address)) isLong = false;
        cancelThrottleRequest(address, isLong, l);
    }

    /**
     * Handle throttle information when it's finally available, e.g. when
     * a new Throttle object has been created.
     * <P>
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     */
    public void notifyThrottleKnown(DccThrottle throttle, int addr) {
        log.debug("notifyThrottleKnown for "+addr);
        Integer addressKey = new Integer(addr);
		ArrayList a = (ArrayList)throttleListeners.get(addressKey);
		for (int i = 0; i<a.size(); i++) {
      	 	ThrottleListener l = (ThrottleListener)a.get(i);
            log.debug("Notify listener");
            l.notifyThrottleFound(throttle);
        }
        throttleListeners.remove(addressKey);
    }
  
    /**
     * Check to see if the Dispatch Button should be enabled or not 
     * Default to true, override if necessary
     **/
    public boolean hasDispatchFunction() { return true; }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractThrottleManager.class.getName());
}
