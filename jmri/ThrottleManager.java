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
 * @version			$Revision: 1.16 $
 */
public interface ThrottleManager {

    /**
     * Request a throttle, given a decoder address. When the decoder address
     * is located, the ThrottleListener gets a callback via the ThrottleListener.notifyThrottleFound
     * method.
     * <P>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     * @param address The decoder address desired.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     * @return True if the request will continue, false if the request will not
     * be made. False may be returned if a the throttle is already in use.
     */
    public boolean requestThrottle(int address, ThrottleListener l);

    /**
     * Request a throttle, given a decoder address & whether it is a long
     * or short DCC address. When the decoder address
     * is located, the ThrottleListener gets a callback via the ThrottleListener.notifyThrottleFound
     * method.
     * @param address The decoder address desired.
     * @param isLong True if this is a request for a DCC long (extended) address.
     * @param l The ThrottleListener awaiting notification of a found throttle.
     * @return True if the request will continue, false if the request will not
     * be made. False may be returned if a the throttle is already in use.
     */
    public boolean requestThrottle(int address, boolean isLong, ThrottleListener l);

    /**
     * Cancel a request for a throttle.
     * <P>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     * @param address The decoder address desired.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    public void cancelThrottleRequest(int address, ThrottleListener l);

    /**
     * Cancel a request for a throttle.
     * @param address The decoder address desired.
     * @param isLong True if this is a request for a DCC long (extended) address.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    public void cancelThrottleRequest(int address, boolean isLong, ThrottleListener l);

    /**
     * Check to see if the Dispatch Button should be enabled or not
     **/
    public boolean hasDispatchFunction();
    
    /**
     * Check to see if a specific number is a valid long address on this system
     **/
    public boolean canBeLongAddress(int address);
    
    /**
     * Check to see if a specific number is a valid short address on this system
     **/
    public boolean canBeShortAddress(int address);
    
    /**
     * Are there not any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique();

    /**
     * What speed modes are supported by this system?                       
     * value should be xor of possible modes specifed in the throttle
     * interface
     */
    public int supportedSpeedModes();
            
}
