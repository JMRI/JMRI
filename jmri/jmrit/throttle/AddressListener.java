package jmri.jmrit.throttle;

import java.util.EventListener;

public interface AddressListener extends EventListener
{
    public void notifyAddressChanged(int previousAddress, int newAddress);
}