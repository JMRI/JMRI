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
        throttleListeners.put(new Integer(address), l);
        slotManager.slotFromLocoAddress(address, this);
    }

    /**
     * SlotListener contract. Get notification that an address has changed slot.
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     */
    public void notifyChangedSlot(LocoNetSlot s)
    {
        Integer address = new Integer(s.locoAddr());
        ThrottleListener listener = (ThrottleListener)
                                    throttleListeners.get(address);
        if (listener != null)
        {
            LocoNetThrottle throttle = new LocoNetThrottle(s);
            listener.notifyThrottleFound(throttle);
        }
    }
}