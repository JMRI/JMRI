package jmri.jmrix.loconet;

import jmri.ThrottleManager;
import jmri.ThrottleListener;
import jmri.DccThrottle;
import java.util.HashMap;

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
     * Get a throttle for a given address.
     *
     * @param address The decoder address the throttle will use.
     * @return A LocoNet throttle using the given address or null if no
     * such throttle can be created. For example,
     * Null is returned if the decoder address
     * cannot be found.
     */
    public void requestThrottle(int address, ThrottleListener l)
    {
        if (throttleListeners == null)
        {
            throttleListeners = new HashMap(5);
        }
        throttleListeners.put(new Integer(address), l);
        slotManager.slotFromLocoAddress(address, this);
        System.out.println("Requesting loco for address: " + address);
    }

    public void notifyChangedSlot(LocoNetSlot s)
    {
        Integer address = new Integer(s.locoAddr());
        ThrottleListener listener = (ThrottleListener)
                                    throttleListeners.get(address);
        System.out.println("Found decoder with addr: " +s.locoAddr() + " in slot " + s.getSlot());
        if (listener != null)
        {
            LocoNetThrottle throttle = new LocoNetThrottle(s);
            listener.notifyThrottleFound(throttle);
        }
    }
}