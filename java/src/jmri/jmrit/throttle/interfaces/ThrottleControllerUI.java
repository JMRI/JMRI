package jmri.jmrit.throttle.interfaces;


import javax.swing.JLabel;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.jmrit.throttle.implementation.SimpleThrottlePanel;

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
     * Get that throttle control roster entry  
     * 
     * @return the roster entry or null
     */
    RosterEntry getRosterEntry();

    /**
     * Set that throttle control address
     *
     * @param la locomotive DccLocoAddress
     */     
    void setAddress(DccLocoAddress la);

    /**
     * Get that throttle control address
     *
     * @return the throttle control address or null
     */
    DccLocoAddress getAddress();

    /**
     * Set that throttle consist control address
     * 
     * @param la consist DccLocoAddress
     */
    void setConsistAddress(DccLocoAddress la);

    /**
     * Check if that throttle control is using that address
     * 
     * @param la the DccLocoAddress to check
     * @return  true if that throttle control is using that address, false otherwise
     */
    boolean isUsingAddress(DccLocoAddress la);

    /**
     * Dispatch that throttle UI, will dispatch for that throttle control UI only.
     * Throttle will actully be released only if that throttle control UI is the only one using that address.
     * 
     */
    void dispatchAddress();

    /**
     * Get that throttle control UI throttle object
     * 
     * @return the throttle or null
     */
    DccThrottle getThrottle();

    /**
     * Get that throttle control function throttle object (for consists)
     * 
     * @return the function throttle or null
     */
    DccThrottle getFunctionThrottle();

    /**
     * Get that throttle control roster entry for the function locomotive
     * 
     * @return the roster entry or null
     */
    RosterEntry getFunctionRosterEntry();
    
    /**
     * Emergency stop that throttle
     *
     */        
    void eStop();

    /**
     * Check if that throttle control is running (non null speed)
     * 
     * @return true if that throttle control is running, false otherwise
     */
    boolean isRunning();

    /**
     * Check if that throttle control is active (non null speed and at least one active function)
      *
     * @return true if that throttle control is active, false otherwise
     */
    boolean isActive();

    /**
     * Bring that throttle UI control to front (window will be activated and raised if it is not already the case).
     *
     */        
    void toFront();

    /**
     * Check if that throttle control UI is selected (active and visible)
     *
     * @return true if that throttle control UI is visible, false otherwise
     */    
    boolean isVisible();

    /**
     * 
     * Update that throttle control UI containing frame title (if any) with the current address and roster entry.
     * Called by the throttle core UI when the controlled throttle address or roster entry is updated to update the frame title with the new information.
     *
     */
    void updateFrameTitle();

    /**
     * 
     * Update that throttle control UI.
     * Called by the throttle core UI when the controlled throttle address or roster entry is updated
     *
     */
    void updateGUI();

    /**
     * Load that throttle control UI from a throttle file.
      *
     * @param sfile the throttle file to load, if null a file chooser will be prompted to select the throttle file to load
     */
    void loadThrottleFile(String sfile);

    /**
     * Get a copy of that throttle control UI label (a text or the roster entry icon, for consits a consist icon will be built from the locomotives in the consist).     
     * See {@link SimpleThrottlePanel} for an example implementation
     * 
     * @return  the label or null
     */
    JLabel getLabel();

    /**
     * Check if that throttle control UI is using continuous speed display (a speed slider from -100 to 100)
     * (Used by external controls like USB game controllers)
     * See {@link SimpleThrottlePanel} for an example implementation
     * 
     * @return true if that throttle control UI is using continuous speed display, false otherwise
     */
    boolean isSpeedDisplayContinuous();

    /**
     * 
     * Get that throttle control UI roster entry selector panel, or null if that throttle control UI does not have one.
     * (Used by external controls like USB game controllers)
     * See {@link SimpleThrottlePanel} for an example implementation
     * 
     * @return the roster entry selector panel or null
     */
    public RosterEntrySelectorPanel getRosterEntrySelector();
    
    /**
     * Add an address listener to that throttle control UI.
     * See {@link SimpleThrottlePanel} for an example implementation
     *
     * @param l the address listener to add
     */
    public void addAddressListener(AddressListener l);

    /**
     * Remove an address listener from that throttle control UI.
     * See {@link SimpleThrottlePanel} for an example implementation 
     *
     * @param l the address listener to remove
     */
    public void removeAddressListener(AddressListener l);

}
