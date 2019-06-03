package jmri.jmrit.withrottle;

/**
 * Scaffold for ThrottleControllerListener.
 * 
 * @author Paul Bender Copyright (C) 2018
 */
import java.util.EventListener;
import jmri.DccLocoAddress;

public class ThrottleControllerListenerScaffold implements ThrottleControllerListener {

    private boolean addressFound = false;
    private boolean addressReleased = false;

    public void notifyControllerAddressFound(ThrottleController TC){
        addressFound = true;
    }

    public boolean hasAddressBeenFound(){
       return addressFound;
    }

    public void notifyControllerAddressReleased(ThrottleController TC){
        addressReleased = true;
    }

    public boolean hasAddressBeenReleased(){
       return addressReleased;
    }
    
    public void notifyControllerAddressDeclined(ThrottleController tc, DccLocoAddress address, String reason){
        jmri.InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, tc);
    }
}
