package jmri.jmrit.throttle;


import javax.annotation.CheckForNull;

import jmri.DccLocoAddress;

/**
 * 
 * An interface for managers of containers of throttle controllers user interface
 *  (ThrottleFrameManager for Swing throttles for instance)
 * 
 * @author Lionel Jeanson 2026
 */
public interface ThrottleControllersUIContainersManager extends Iterable<ThrottleControllersUIContainer> {
        
    /**
     * Return the number of active thottle controller containers for that throttle contrainer manager
     *
     * @return the number of active thottle controller containers
     */
    int getNbThrottleControllersContainers();

    /**
     * Create a new throttle controller
     *
     * @return The newly created throttle controller
     */                    
    ThrottleControllerUI createThrottleController();
    
    /**
     * Return the thottle controller container at nth position in the list
     *
     * @param n position of the throttle controller container
     * @return a thottle controller container
     */    
    ThrottleControllersUIContainer getThrottleControllersContainerAt(int n);


    /**
     * Return the number of throttle controllers for a LocoAddress,
     * usefull to kno if a layout throttle object should actually be released
     *
     * @param la locoaddrress we're looking for
     * @return the number of throttle controllers for that LocoAddress
     */        
    int getNumberOfEntriesFor(@CheckForNull DccLocoAddress la);

}
