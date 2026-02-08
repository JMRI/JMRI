package jmri.jmrit.throttle;

import java.util.Iterator;

import javax.annotation.CheckForNull;

import jmri.DccLocoAddress;

/**
 * 
 * An interface for managers of containers of throttle controlers
 *  (ThrottleFrameManager for Swing throttles for instance)
 * 
 * @author Lionel Jeanson 2025
 */
public interface ThrottleControlersContainersManager extends Iterable<ThrottleControlersContainer> {
        
    /**
     * Return the number of active thottle controler containers for that throttle contrainer manager
     *
     * @return the number of active thottle controler containers
     */
    int getNbThrottleControlersContainers();

    /**
     * Create a new throttle controler
     *
     * @return The newly created throttle controler
     */                    
    ThrottleControler createThrottleControler();
    
    /**
     * Return the thottle controler container at nth position in the list
     *
     * @param n position of the throttle controler container
     * @return a thottle controler container
     */    
    ThrottleControlersContainer getThrottleControlersContainerAt(int n);


    /**
     * Return the number of throttle controlers for a LocoAddress,
     * usefull to kno if a layout throttle object should actually be released
     *
     * @param la locoaddrress we're looking for
     * @return the number of throttle controlers for that LocoAddress
     */        
    int getNumberOfEntriesFor(@CheckForNull DccLocoAddress la);

}
