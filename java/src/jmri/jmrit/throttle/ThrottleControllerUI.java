package jmri.jmrit.throttle;


import jmri.DccLocoAddress;
import jmri.jmrit.roster.RosterEntry;

/**
 * 
 * An interface to abstract UI controllers of throttle
 *  (ThrottleFrame for Swing throttles for instance)
 * 
 * @author Lionel Jeanson 2026
 */
public interface ThrottleControllerUI {
    
    /**
     * Return that throttle control container
     *
     * @return the throttle control container
     */
    ThrottleControllersUIContainer getThrottleControllersContainer();
    
    /**
     * Set that throttle control container
     *
     * @param tw the throttle control container to set
     */
    void setThrottleControllersContainer(ThrottleControllersUIContainer tw);
    
    /**
     * Set that throttle control roster entry
     *
     * @param re the roster entry
     */
    void setRosterEntry(RosterEntry re);
    
    /**
     * Set that throttle control address
     *
     * @param number address number
     * @param isLong is long address
     */    
    void setAddress(int number, boolean isLong);
    
    /**
     * Set that throttle control address
     *
     * @param la DccLocoAddress
     */     
    void setAddress(DccLocoAddress la);
    
    /**
     * Emergency stop that throttle
     *
     */        
    void eStop();
    
    /**
     * Bring that throttle control to front
     *
     */        
    void toFront();
}
