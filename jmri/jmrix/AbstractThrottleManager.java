package jmri.jmrix;

import jmri.ThrottleManager;
import jmri.ThrottleListener;
import jmri.DccThrottle;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.FunctionButtonPropertyEditor;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Abstract implementation of a ThrottleManager.
 *
 * <P>
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version         $Revision: 1.1 $
 */
abstract public class AbstractThrottleManager implements ThrottleManager {
    private HashMap throttleListeners;
    private HashMap throttleMap;
    private ArrayList throttleFrames;
    private FunctionButtonPropertyEditor functionButtonEditor;

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
            requestThrottleSetup(address);
        }
    }

    /**
     * Abstract member to actually do the work of configuring a new throttle,
     * perhaps via interaction with the DCC system
     */
    abstract public void requestThrottleSetup(int address);

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
     * Handle throttle information when it's finally available, e.g. when
     * a new Throttle object has been created.
     * <P>
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     */
    public void notifyThrottleKnown(DccThrottle throttle, int addr)
    {
        Integer address = new Integer(addr);
        ArrayList list = (ArrayList)throttleListeners.get(address);
        if (list != null)
        {
            if (throttleMap == null)
            {
                throttleMap = new HashMap();
            }

            throttleMap.put(address, throttle);
            for (int i=0; i<list.size(); i++)
            {
                ThrottleListener listener = (ThrottleListener)list.get(i);
                listener.notifyThrottleFound(throttle);
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

}