package jmri.jmrix.xpa;

import jmri.ThrottleListener;
import jmri.ThrottleManager;
import com.sun.java.util.collections.HashMap;

/**
 * XPA implementation of a ThrottleManager
 * @author     Paul Bender Copyright (C) 2004
 * @version    $Revision: 1.1 $
 */

public class XpaThrottleManager implements ThrottleManager {
    private HashMap throttleListeners;
    private HashMap throttleMap;

    /**
     * Constructor.
     */
    public XpaThrottleManager()
    {
       super();
    }

    /**
     * Request a throttle, given a decoder address. When the decoder address
     * is located, the ThrottleListener gets a callback via the ThrottleListener.notifyThrottleFound
     * method.
     * @param address The decoder address desired.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     * @return True if the request will not continue, false if the request
     * will be made.  False may be returned if the throttle is already in use.
     */
    public boolean requestThrottle(int address, ThrottleListener l)
    {
	boolean throttleInUse = false;
        if (throttleListeners == null)
        {
            throttleListeners = new HashMap(5);
        }

        Integer addressKey = new Integer(address);
        if (throttleListeners.containsKey(addressKey))
        {
 	    throttleInUse=true;
        }
	else
	{
        	throttleListeners.put(addressKey, l);
		XpaThrottle throttle=new XpaThrottle(address);
		l.notifyThrottleFound(throttle);
        }
        return(throttleInUse);
    }

    /**
     * Cancel a request for a throttle
     * @param address The decoder address desired.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    public void cancelThrottleRequest(int address, ThrottleListener l)
    {
        if (throttleListeners != null)
        {
            Integer addressKey = new Integer(address);
            throttleListeners.remove(addressKey);
        }
    }

    /*
     * Lenz Systems DO NOT use the Dispatch Function
     */
    public boolean hasDispatchFunction() { return false; }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XpaThrottleManager.class.getName());

}

