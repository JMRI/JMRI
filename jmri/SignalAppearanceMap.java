// SignalAppearanceMap.java

package jmri;

import jmri.util.NamedBeanHandle;
import java.util.Vector;

 /**
 * Access to signal apperance information.
 * <p>
 * Maps to an appearance* file in a signal system.
 *
 * This interface does not provide any methods to change the map.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @version     $Revision: 1.4 $
 */
public interface SignalAppearanceMap  {

    /**
     * Set the associated SignalHeads to the appropriate appearance for 
     * a specified aspect.
     * <p>
     * Does not change state of map, just of the SignalHeads in the list.
     */
    public void setAppearances(String aspect, java.util.List<NamedBeanHandle<SignalHead>> heads);
    
    /**
     * Check if an aspect can be displayed by this particular Map
     */
    public boolean checkAspect(String aspect);
    
    /**
     * Get all available aspect names
     */
    public java.util.Enumeration<String> getAspects();
    
    /**
     * Get the associated signal system and the common information
     * it contains
     */
    public SignalSystem getSignalSystem();
    
    /**
     * Get a property associated with a specific aspect
     */
    public String getProperty(String aspect, String key);
    
    /**
     * Get an Image Link associated with a specific aspect and type
     */
    public String getImageLink(String aspect, String key);
    
    /**
     * Return a list of valid icon sets
     */

    public Vector<String> getImageTypes(String aspect);

}

/* @(#)SignalAppearanceMap.java */
