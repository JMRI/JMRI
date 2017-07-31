package jmri;

import java.util.ArrayList;
import javax.annotation.Nonnull;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

/**
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public interface SignalMastLogicManager extends Manager<SignalMastLogic> {

    /*public void addDestinationMastToLogic(SignalMastLogic src, SignalMast destination);*/
    /**
     * Replace all instances of an old SignalMast (either source or destination)
     * with the new signal mast instance. This is for use with such tools as the
     * Layout Editor where a signal mast at a certain location can be replaced
     * with another, while the remainder of the configuration stays the same.
     *
     * @param oldMast Current Signal Mast
     * @param newMast Replacement (new) Signal Mast
     */
    public void replaceSignalMast(SignalMast oldMast, SignalMast newMast);

    /**
     * Discover all possible valid source + destination signal mast pairs on all
     * Layout Editor Panels and create the corresponding SMLs.
     *
     * @throws jmri.JmriException if there is an error discovering signaling
     *                            pairs
     */
    public void automaticallyDiscoverSignallingPairs() throws JmriException;

    /**
     * Use the Layout Editor to check if the destination signal mast is
     * reachable from the source signal mast.
     *
     * @param sourceMast Source Signal Mast
     * @param destMast   Destination Signal Mast
     * @return true if valid, false if not valid
     */
    // public boolean checkValidDest(SignalMast sourceMast, SignalMast destMast) throws JmriException;
    /**
     * Discover valid destination signal masts for a given source Signal Mast on
     * a given Layout Editor Panel.
     *
     * @param source Source Signal Mast
     * @param layout Layout Editor panel to check
     * @throws jmri.JmriException if there is an error discovering signaling
     *                            destinations
     */
    public void discoverSignallingDest(SignalMast source, LayoutEditor layout) throws JmriException;

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    public void dispose();

    /**
     * Gather a list of all the Signal Mast Logics, by destination Signal Mast.
     *
     * @param destination The destination Signal Mast
     * @return a list of logics for destination or an empty list if none
     */
    @Nonnull
    public ArrayList<SignalMastLogic> getLogicsByDestination(SignalMast destination);

    /**
     * Return the Signal Mast Logic for a specific Source Signal Mast.
     *
     * @param source The Source Signal Mast
     * @return The Signal Mast Logic for that mast
     */
    public SignalMastLogic getSignalMastLogic(SignalMast source);

    /**
     * Return a list of all existing Signal Mast Logics
     *
     * @return An ArrayList of all Signal Mast Logics
     */
    public ArrayList<SignalMastLogic> getSignalMastLogicList();

    /**
     * Initialise all the Signal Mast Logics. Primarily used after loading a
     * configuration.
     */
    public void initialise();

    /**
     * Create a new Signal Mast Logic for a source Signal Mast.
     *
     * @param source The source Signal Mast
     * @return source The new SML instance
     */
    public SignalMastLogic newSignalMastLogic(SignalMast source);

    //public void removeDestinationMastToLogic(SignalMastLogic src, SignalMast destination);
    /**
     * Remove a destination Signal Mast and its settings from a Signal Mast
     * Logic.
     *
     * @param sml  The Signal Mast Logic
     * @param dest The destination mast
     */
    public void removeSignalMastLogic(SignalMastLogic sml, SignalMast dest);

    /**
     * Completely remove a specific Signal Mast Logic by name.
     *
     * @param sml The Signal Mast Logic to be removed
     */
    public void removeSignalMastLogic(SignalMastLogic sml);

    /**
     * Completely remove a Signal Mast from all the SMLs that use it.
     *
     * @param mast The Signal Mast to be removed
     */
    public void removeSignalMast(SignalMast mast);

    /**
     * Disable the use of info from the Layout Editor Panels to configure a
     * Signal Mast Logic for a specific Signal Mast.
     *
     * @param mast The Signal Mast for which LE info is to be disabled
     */
    public void disableLayoutEditorUse(SignalMast mast);

    /**
     * Replace the complete Signal Mast Logic configurations between two Source
     * Signal Masts.
     *
     * @param mastA Signal Mast A
     * @param mastB Signal Mast B
     */
    public void swapSignalMasts(SignalMast mastA, SignalMast mastB);

    /**
     * Check if a Signal Mast is in use as either a Source or Destination mast
     * in any Signal Mast Logic
     *
     * @param mast the signal mast to check
     * @return true if mast is used by at least one Signal Mast Logic
     */
    public boolean isSignalMastUsed(SignalMast mast);

    public long getSignalLogicDelay();

    public void setSignalLogicDelay(long l);

}
