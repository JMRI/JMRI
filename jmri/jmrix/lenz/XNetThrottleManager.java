package jmri.jmrix.lenz;

import jmri.ThrottleManager;
import jmri.ThrottleListener;
import jmri.DccThrottle;
import jmri.JmriException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * XNet implementation of a ThrottleManager
 * @author     Paul Bender 
 * @created    December 20,2002
 * @version    $Revision: 1.4 $
 */

public class XNetThrottleManager implements ThrottleManager,XNetListener
{
    private HashMap throttleListeners;
    private HashMap throttleMap;

    /**
     * Constructor.
     */
    public XNetThrottleManager()
    {
       super();
       XNetTrafficController.instance().addXNetListener(~0, this);
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
		XNetThrottle throttle=new XNetThrottle(address);
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

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //          public void firePropertyChange(String propertyName,
    //
    // 
    // _once_ if anything has changed state (or set the commanded state directly
    public void message(XNetMessage l) {
        // check validity & addressing  
        //if (XNetTrafficController.instance()
        //    .getCommandStation()
        //    .getThrottleMsgAddr(l) != address) return;
        // is for this object, parse message type
        //log.error("message function invoked, but not yet prepared");
    }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetThrottleManager.class.getName());

}

