package jmri.jmrix.lenz;

import jmri.ThrottleManager;
import jmri.ThrottleListener;
import jmri.DccThrottle;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.FunctionButtonPropertyEditor;
import jmri.JmriException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * XNet implementation of a ThrottleManager
 */
public class XNetThrottleManager implements ThrottleManager
{
    private HashMap throttleListeners;
    private HashMap throttleMap;
    private ArrayList throttleFrames;
    private FunctionButtonPropertyEditor functionButtonEditor;

    /**
     * Constructor.
     */
    public XNetThrottleManager()
    {
       log.error("XNetThrottleManger constructor");
    }


    /**
     * Tell this manager that a new ThrottleFrame was created.
     * @param tf The new ThrottleFrame.
     */
    public void notifyNewThrottleFrame(ThrottleFrame tf)
    {
        if (throttleFrames == null)
        {
            throttleFrames = new ArrayList(2);
        }
        throttleFrames.add(tf);
    }

    /**
     * Retrieve an Iterator over all the ThrottleFrames in existence.
     * @return The Iterator on the list of ThrottleFrames.
     */
    public Iterator getThrottleFrames()
    {
        return throttleFrames.iterator();
    }


    /**
     * Request a throttle, given a decoder address. When the decoder address
     * is located, the ThrottleListener gets a callback via the ThrottleListener.notifyThrottleFound
     * method.
     * @param address The decoder address desired.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     */
    public void requestThrottle(int address, ThrottleListener l)
    {
        if (throttleListeners == null)
        {
            throttleListeners = new HashMap(5);
        }

        Integer addressKey = new Integer(address);
        ArrayList list = (ArrayList)throttleListeners.get(addressKey);
        if (list == null)
        {
            list = new ArrayList(2);
        }
        if (!list.contains(l))
        {
            list.add(l);
        }
        throttleListeners.put(addressKey, list);
        if (list.size() == 1)
        {
            // Only need to do this once per address.
            //slotManager.slotFromLocoAddress(address, this);
        }
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
            ArrayList list = (ArrayList)throttleListeners.get(addressKey);
            if (list != null)
            {
                list.remove(l);
                throttleListeners.put(addressKey, list);
            }
        }

    }

   /**
     * Get a reference to the Function
     */
    public jmri.jmrit.throttle.FunctionButtonPropertyEditor getFunctionButtonEditor()
    {
        if (functionButtonEditor == null)
        {
            functionButtonEditor = new FunctionButtonPropertyEditor();
        }
        return functionButtonEditor;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetThrottleManager.class.getName());

}
