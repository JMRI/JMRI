package jmri;

/**
 * Interface for controlling throttles
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public interface ThrottleManager
{

    /**
     * Get a throttle, given a decoder address.
     * @param address The decoder address desired.
     */
    public void requestThrottle(int address, ThrottleListener l);

}