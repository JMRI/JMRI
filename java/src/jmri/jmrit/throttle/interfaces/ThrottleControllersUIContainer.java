package jmri.jmrit.throttle.interfaces;


import jmri.DccLocoAddress;

/**
 * 
 * An interface for containers of throttle controllers user interface
 *  (ThrottleWindow for Swing throttles for instance)
 * 
 * @author Lionel Jeanson 2026
 */
public interface ThrottleControllersUIContainer {

    /**
     * Return the number of thottle controlS containerS (ThrottleWindows forinstance)
     *
     * @return the number of active thottle controls containers.
     */    
    int getNbThrottlesControllers();
    
    /**
     * Created a new throttle controller
     *
     * @return the newly created throttle controller
     */    
    ThrottleControllerUI newThrottleController();
    
    /**
     * Adds an existing throttle controller to that container list at position n
     *
     * @param tf the throttle controller to add
     * @param n position that it will inserted at
     */
    void addThrottleControllerAt(ThrottleControllerUI tf, int n);
    
    /**
     * Remove a throttle controller from that container
     *
     * @param tf the throttle controller to add
     */
    void removeThrottleController(ThrottleControllerUI tf);
    
    /**
     * Get the throttle controller at position n
     *
     * @param n position
     * @return the throttle controller
     */
    ThrottleControllerUI getThrottleControllerAt(int n);

    /**
     * Get the throttle controller being used now (or on top)
     *
     * @return the throttle controller
     */    
    ThrottleControllerUI getCurentThrottleController();
    
    /**
     * Force estop all throttles managed by that controllers container
     *
     */    
    void emergencyStopAll();

    /**
     * Get the number of usages of a particular Loco Address.
     * @param la the Loco Address, can be null.
     * @return 0 if no usages, else number of AddressPanel usages.
     */    
    public int getNumberOfEntriesFor(DccLocoAddress la);

    /**
     * Dispose of the container and its resources.
     * This is called when the container is closed, and should clean up any resources used by the container, such as closing windows, releasing memory, etc.
     * Particularly will release an ythrottle still in use.
     * 
     */
    public void dispose();
}
