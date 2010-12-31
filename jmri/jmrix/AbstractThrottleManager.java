package jmri.jmrix;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Abstract implementation of a ThrottleManager.
 * <P>
 * Based on Glen Oberhauser's original LnThrottleManager implementation.
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version     $Revision: 1.25 $
 */
abstract public class AbstractThrottleManager implements ThrottleManager {
	
	/**
	 * throttleListeners is indexed by the address, and
	 * contains as elements an ArrayList of ThrottleListener
	 * objects.  This allows more than one to request a throttle
	 * at a time, but @see{singleUse}.
	 */
    private HashMap<DccLocoAddress,ArrayList<ThrottleListener>> throttleListeners = new HashMap<DccLocoAddress,ArrayList<ThrottleListener>>(5);

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
     * @param isLongAddress True if this is a request for a DCC long (extended) address.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     * @return True if the request will continue, false if the request will not
     * be made. False may be returned if a the throttle is already in use.
     */
    public boolean requestThrottle(int address, boolean isLongAddress, ThrottleListener l) {
        boolean throttleFree = true;

		// put the list in if not present
		DccLocoAddress la = new DccLocoAddress(address, isLongAddress);
		if (!throttleListeners.containsKey(la))
			throttleListeners.put(la, new ArrayList<ThrottleListener>());

		// get the corresponding list to check length
		ArrayList<ThrottleListener> a = throttleListeners.get(la);
		
		if (log.isDebugEnabled()) log.debug("After request in ATM: "+a.size());
		// check length
        if (singleUse() && (a.size()>0)) {
            throttleFree= false;
            if (log.isDebugEnabled()) log.debug("case 1");           
        } else if (a.size() == 0) {
        	a.add(l);
            if (log.isDebugEnabled()) log.debug("case 2: "+la+";"+a);
            requestThrottleSetup(la);
        } else {
        	a.add(l);
            if (log.isDebugEnabled()) log.debug("case 3");
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
     * usually via interaction with the DCC system
     */
    abstract public void requestThrottleSetup(LocoAddress a);

    /**
     * Cancel a request for a throttle
     * @param address The decoder address desired.
     * @param isLong True if this is a request for a DCC long (extended) address.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    public void cancelThrottleRequest(int address, boolean isLong, ThrottleListener l) {
        if (throttleListeners != null) {
		    DccLocoAddress la = new DccLocoAddress(address, isLong);
			ArrayList<ThrottleListener> a = throttleListeners.get(la);
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
     * If the system-specific ThrottleManager has been unable to create the DCC
     * throttle then it needs to be removed from the throttleListeners, otherwise
     * any subsequent request for that address results in the address being reported
     * as already in use, if singleUse is set.
     * @param address The DCC Loco Address that the request failed on.
     */
    public void failedThrottleRequest(DccLocoAddress address, String reason) {
            ArrayList<ThrottleListener> a = throttleListeners.get(address);
        if (a==null) {
		    log.warn("notifyThrottleKnown with zero-length listeners: "+address);
		} else {
            for (int i = 0; i<a.size(); i++) {
                ThrottleListener l = a.get(i);
                l.notifyFailedThrottleRequest(address, reason);
            }
        }
        throttleListeners.remove(address);

        if ((throttleListeners.containsKey(address)) && (singleUse())){
            throttleListeners.remove(address);
        }
    }

    /**
     * Handle throttle information when it's finally available, e.g. when
     * a new Throttle object has been created.
     * <P>
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     */
    public void notifyThrottleKnown(DccThrottle throttle, LocoAddress addr) {
        log.debug("notifyThrottleKnown for "+addr);
		ArrayList<ThrottleListener> a = throttleListeners.get(addr);
		if (a==null) {
		    log.warn("notifyThrottleKnown with zero-length listeners: "+addr);
		    return;
		}
		for (int i = 0; i<a.size(); i++) {
      	 	ThrottleListener l = a.get(i);
            log.debug("Notify listener");
            l.notifyThrottleFound(throttle);
        }
        throttleListeners.remove(addr);
    }
  
    /**
     * Check to see if the Dispatch Button should be enabled or not 
     * Default to true, override if necessary
     **/
    public boolean hasDispatchFunction() { return true; }

    /**
     * What speed modes are supported by this system?                       
     * value should be xor of possible modes specifed by the 
     * DccThrottle interface
     */
    public int supportedSpeedModes() {
	return(DccThrottle.SpeedStepMode128);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractThrottleManager.class.getName());
}
