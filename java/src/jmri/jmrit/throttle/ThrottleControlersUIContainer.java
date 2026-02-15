package jmri.jmrit.throttle;


import jmri.DccLocoAddress;

/**
 * 
 * An interface for containers of throttle controlers user interface
 *  (ThrottleWindow for Swing throttles for instance)
 * 
 * @author Lionel Jeanson 2026
 */
public interface ThrottleControlersUIContainer {

    /**
     * Return the number of thottle controlS containerS (ThrottleWindows forinstance)
     *
     * @return the number of active thottle controls containers.
     */    
    int getNbThrottlesControlers();
    
    /**
     * Created a new throttle controler
     *
     * @return the newly created throttle controler
     */    
    ThrottleControlerUI newThrottleControler();
    
    /**
     * Adds an existing throttle controler to that container list at position n
     *
     * @param tf the throttle controler to add
     * @param n position that it will inserted at
     */
    void addThrottleControlerAt(ThrottleControlerUI tf, int n);
    
    /**
     * Remove a throttle controler from that container
     *
     * @param tf the throttle controler to add
     */
    void removeThrottleControler(ThrottleControlerUI tf);
    
    /**
     * Get the throttle controler at position n
     *
     * @param n position
     * @return the throttle controler
     */
    ThrottleControlerUI getThrottleControlerAt(int n);
    
    /**
     * Force estop all throttles managed by that controlers container
     *
     */    
    void eStopAll();

    /**
     * Get the number of usages of a particular Loco Address.
     * @param la the Loco Address, can be null.
     * @return 0 if no usages, else number of AddressPanel usages.
     */    
    public int getNumberOfEntriesFor(DccLocoAddress la);

}
