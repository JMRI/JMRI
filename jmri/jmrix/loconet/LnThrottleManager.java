package jmri.jmrix.loconet;

import jmri.ThrottleManager;
import jmri.ThrottleListener;
import jmri.DccThrottle;
import jmri.jmrit.throttle.ThrottleFrame;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * LocoNet implementation of a ThrottleManager
 */
public class LnThrottleManager implements ThrottleManager, SlotListener
{
    private SlotManager slotManager;
    private HashMap throttleListeners;
    private HashMap throttleMap;
    private ArrayList throttleFrames;

    public LnThrottleManager()
    {
        slotManager = SlotManager.instance();
    }


    public void notifyNewThrottleFrame(ThrottleFrame tf)
    {
        if (throttleFrames == null)
        {
            throttleFrames = new ArrayList(2);
        }
        throttleFrames.add(tf);
    }

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
            slotManager.slotFromLocoAddress(address, this);
        }
    }

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
     * SlotListener contract. Get notification that an address has changed slot.
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     */
    public void notifyChangedSlot(LocoNetSlot s)
    {
        Integer address = new Integer(s.locoAddr());
        ArrayList list = (ArrayList)throttleListeners.get(address);
        if (list != null)
        {
            if (throttleMap == null)
            {
                throttleMap = new HashMap();
            }
            LocoNetThrottle throttle = (LocoNetThrottle)throttleMap.get(address);
            if (throttle == null)
            {
                throttle = new LocoNetThrottle(s);
                throttleMap.put(address, throttle);
            }
            for (int i=0; i<list.size(); i++)
            {
                ThrottleListener listener = (ThrottleListener)list.get(i);
                listener.notifyThrottleFound(throttle);
            }
        }
    }
}