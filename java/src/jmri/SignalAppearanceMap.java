package jmri;

import java.util.Vector;

/**
 * Access to signal apperance information.
 * <p>
 * Maps to an appearance* file in a signal system.
 *
 * This interface does not provide any methods to change the map.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public interface SignalAppearanceMap {

    /**
     * Check if an aspect can be displayed by this particular Map
     */
    public boolean checkAspect(String aspect);

    /**
     * Get all available aspect names
     */
    public java.util.Enumeration<String> getAspects();

    /**
     * Get the associated signal system and the common information it contains
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

    /**
     * Return an aspect for a specific appearance
     */
    public String getSpecificAppearance(int appearance);

    /**
     * Constant representing the "held" apsect for a signal
     */
    public final static int HELD = 0;

    /**
     * Constant representing the "permissive" apsect for a signal
     */
    public final static int PERMISSIVE = 1;

    /**
     * Constant representing the "danager" apsect for a signal
     */
    public final static int DANGER = 2;

    /**
     * Constant representing the "dark" apsect for a signal
     */
    public final static int DARK = 3;

    /**
     * Returns a list of postential aspects that we could set the signalmast to
     * given the state of the advanced signal mast.
     */
    public String[] getValidAspectsForAdvancedAspect(String advancedAspect);

}
