package jmri;

import java.util.ArrayList;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

/**
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 */
public interface SignalMastLogicManager extends Manager {

    /*public void addDestinationMastToLogic(SignalMastLogic src, SignalMast destination);*/
    /**
     * This will replace all instances of an old SignalMast (either source or
     * destination) with the new signal mast instance. This is for use with such
     * tools as the Layout Editor where a signalmast at a certain location
     * can be replaced with another, while the remainder of the configuration
     * stays the same.
     */
    public void replaceSignalMast(SignalMast oldMast, SignalMast newMast);

    /**
     * Discover all possible valid source and destination signalmasts past pairs
     * on all Layout Editor Panels.
     */
    public void automaticallyDiscoverSignallingPairs() throws JmriException;

    /**
     * This uses the Layout Editor to check if the destination signalmast is
     * reachable from the source signalmast
     *
     * @param sourceMast Source SignalMast
     * @param destMast   Destination SignalMast
     * @return true if valid, false if not valid.
     */
    // public boolean checkValidDest(SignalMast sourceMast, SignalMast destMast) throws JmriException;
    /**
     * Discover valid destination signalmasts for a given source signal on a
     * given Layout Editor Panel.
     *
     * @param source Source SignalMast
     * @param layout Layout Editor panel to check.
     */
    public void discoverSignallingDest(SignalMast source, LayoutEditor layout) throws JmriException;

    public void dispose();

    /**
     * Gather a list of all the signal mast logics, by destination signal mast
     */
    public ArrayList<SignalMastLogic> getLogicsByDestination(SignalMast destination);

    public long getSignalLogicDelay();

    public SignalMastLogic getSignalMastLogic(SignalMast source);

    /**
     * Returns an arraylist of signalmastlogic
     *
     * @return An ArrayList of SignalMast logics
     */
    public ArrayList<SignalMastLogic> getSignalMastLogicList();

    /**
     * Used to initialise all the signalmast logics. primarily used after
     * loading.
     */
    public void initialise();

    public SignalMastLogic newSignalMastLogic(SignalMast source);

    //public void removeDestinationMastToLogic(SignalMastLogic src, SignalMast destination);
    /**
     * Remove a destination mast from the signalmast logic
     *
     * @param sml  The signalmast logic of the source signal
     * @param dest The destination mast
     */
    public void removeSignalMastLogic(SignalMastLogic sml, SignalMast dest);

    /**
     * Completely remove the signalmast logic.
     */
    public void removeSignalMastLogic(SignalMastLogic sml);

    /**
     * Completely remove the signalmast logic, for a specific signal mast
     */
    public void removeSignalMast(SignalMast mast);

    public void disableLayoutEditorUse(SignalMast mast);

    public void swapSignalMasts(SignalMast mastA, SignalMast mastB);

    public boolean isSignalMastUsed(SignalMast mast);

    public void setSignalLogicDelay(long l);

}
