package jmri;

/**
 * Interface for allocating {@link Throttle} objects.
 * <P>
 * "Address" is interpreted in the context of the DCC implementation.
 * Different systems will distrinquish between short and long addresses
 * in different ways.
 * <P>
 * When the allocated Throttle is no longer needed, it is told that
 * it's released.  If a specific ThrottleManager and/or
 * Throttle implementation needs to keep track of
 * that operation, it is handled internally.
 *
 * @author			Glen Oberhauser
 * @version			$Revision: 1.13 $
 */
public interface ThrottleManager {

    /**
     * Request a throttle, given a decoder address. When the decoder address
     * is located, the ThrottleListener gets a callback via the ThrottleListener.notifyThrottleFound
     * method.
     * @param address The decoder address desired.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     * @return True if the request will continue, false if the request will not
     * be made. False may be returned if a the throttle is already in use.
     */
    public boolean requestThrottle(int address, ThrottleListener l);

    /**
     * Cancel a request for a throttle.
     * @param address The decoder address desired.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    public void cancelThrottleRequest(int address, ThrottleListener l);

}