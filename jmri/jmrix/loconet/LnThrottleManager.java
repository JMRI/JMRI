package jmri.jmrix.loconet;

import jmri.ThrottleManager;
import jmri.ThrottleListener;
import jmri.DccThrottle;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * LocoNet implementation of a ThrottleManager
 */
public class LnThrottleManager implements ThrottleManager, SlotListener
{
    private SlotManager slotManager;
    private HashMap throttleListeners;

    public LnThrottleManager()
    {
        slotManager = SlotManager.instance();
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
            System.out.println("Added throttle listener for " + l.getClass());
        }
        throttleListeners.put(addressKey, list);
        if (list.size() == 1)
        {
            // Only need to do this once per address.
            slotManager.slotFromLocoAddress(address, this);
            System.out.println("Requesting a slot from address: " + address);
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
                System.out.println("Removed request for " + l.getClass());
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
            LocoNetThrottle throttle = new LocoNetThrottle(s);
            for (int i=0; i<list.size(); i++)
            {
                ThrottleListener listener = (ThrottleListener)list.get(i);
                listener.notifyThrottleFound(throttle);
                System.out.println("Sent throttle to " + listener.getClass());
            }
        }
    }
}