package jmri;

import jmri.jmrit.throttle.ThrottleFrame;
import java.util.Iterator;

/**
 * Interface for controlling throttles
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.5 $
 */
public interface ThrottleManager
{

    /**
     * Request a throttle, given a decoder address. When the decoder address
     * is located, the ThrottleListener gets a callback via the ThrottleListener.notifyThrottleFound
     * method.
     * @param address The decoder address desired.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     */
    public void requestThrottle(int address, ThrottleListener l);


    /**
     * Cancel a request for a throttle
     * @param address The decoder address desired.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    public void cancelThrottleRequest(int address, ThrottleListener l);


    public void notifyNewThrottleFrame(ThrottleFrame tf);

    public Iterator getThrottleFrames();

}