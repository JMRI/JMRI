package jmri;

import jmri.jmrit.throttle.ThrottleFrame;
import java.util.Iterator;

/**
 * Interface for controlling throttles
 * @author			Glen Oberhauser
 * @version			$Revision: 1.7 $
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


    /**
     * Tell this manager that a new ThrottleFrame was created.
     * @param tf The new ThrottleFrame.
     */
    public void notifyNewThrottleFrame(ThrottleFrame tf);

    /**
     * Retrieve an Iterator over all the ThrottleFrames in existence.
     * @return The Iterator on the list of ThrottleFrames.
     */
    public Iterator getThrottleFrames();

    /**
     * Get a reference to the Function
     */
    public jmri.jmrit.throttle.FunctionButtonPropertyEditor getFunctionButtonEditor();
}