package jmri.jmrit.throttle;

import java.util.EventListener;

/**
 * Interface for classes that wish to get notification that a new
 * decoder address has been selected.
 */
public interface AddressListener extends EventListener
{
    /**
     * Receive notification that a new address has been selected.
     * @param newAddress The address that is now selected.
     */
    public void notifyAddressChosen(int address);
	
	/**
	 * Receive notification that an address has been released/dispatched
	 * @param address The address released/dispatched
	 */
	public void notifyAddressReleased(int address);
	
}