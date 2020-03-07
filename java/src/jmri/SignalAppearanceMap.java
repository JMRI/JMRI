package jmri;

import java.util.Vector;

/**
 * Access to signal appearance information.
 * <p>
 * Maps to an appearance file in a signal system.
 *
 * This interface does not provide any methods to change the map.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public interface SignalAppearanceMap {

    /**
     * Check if an aspect can be displayed.
     *
     * @param aspect the aspect to check
     * @return true if the aspect can be displayed; false otherwise
     */
    public boolean checkAspect(String aspect);

    /**
     * Get all available aspect names.
     *
     * @return an enumeration of available aspects
     */
    public java.util.Enumeration<String> getAspects();

    /**
     * Get the associated signal system and the common information it contains.
     *
     * @return the signal system
     */
    public SignalSystem getSignalSystem();

    /**
     * Get a property associated with a specific aspect
     *
     * @param aspect the aspect containing the property
     * @param key    the property key
     * @return the property value or null if none is defined for key
     */
    public String getProperty(String aspect, String key);

    /**
     * Get an Image Link associated with a specific aspect and type
     *
     * @param aspect the aspect
     * @param key    the image link key
     * @return the image link or an empty String if none is defined
     */
    public String getImageLink(String aspect, String key);

    /**
     * Get a list of valid icon sets.
     *
     * @param aspect the aspect to get icon sets for
     * @return a list of sets or an empty list if none are defined
     */
    public Vector<String> getImageTypes(String aspect);

    /**
     * Return the aspect for a specific appearance.
     *
     * @param appearance the appearance
     * @return the aspect
     */
    public String getSpecificAppearance(int appearance);

    /**
     * Constant representing the "held" aspect for a signal
     */
    public final static int HELD = 0;

    /**
     * Constant representing the "permissive" aspect for a signal
     */
    public final static int PERMISSIVE = 1;

    /**
     * Constant representing the "danger" aspect for a signal
     */
    public final static int DANGER = 2;

    /**
     * Constant representing the "dark" aspect for a signal
     */
    public final static int DARK = 3;

    /**
     * Get a list of potential aspects that we could set the SignalMast to,
     * given the state of the advanced signal mast.
     *
     * @param advancedAspect the aspect
     * @return a string array of potential aspects or null if none defined
     */
    public String[] getValidAspectsForAdvancedAspect(String advancedAspect);

    /**
     * Provide a multi-line summary of the signal system content,
     * typically for printing. Not intended for further parsing, 
     * i.e. for persistance, as format likely to differ from type 
     * to type, and to change often.
     */
    public String summary();

}
