package jmri;

import java.util.List;
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
     * Discover valid destination signal masts for a given source Signal Mast on
     * a given Layout Editor Panel.
     *
     * @param source Source Signal Mast
     * @param layout Layout Editor panel to check
     * @throws jmri.JmriException if there is an error discovering signaling
     *                            destinations
     */
    public void discoverSignallingDest(@Nonnull SignalMast source, @Nonnull LayoutEditor layout) throws JmriException;

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
    public List<SignalMastLogic> getLogicsByDestination(@Nonnull SignalMast destination);

    /**
     * Return the Signal Mast Logic for a specific Source Signal Mast.
     *
     * @param source The Source Signal Mast
     * @return The Signal Mast Logic for that mast
     */
    public SignalMastLogic getSignalMastLogic(@Nonnull SignalMast source);

    /**
     * Return a list of all existing Signal Mast Logics
     *
     * @return An List of all Signal Mast Logics
     */
    @Nonnull
    public List<SignalMastLogic> getSignalMastLogicList();

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
    @Nonnull
    public SignalMastLogic newSignalMastLogic(SignalMast source);

    /**
     * Remove a destination Signal Mast and its settings from a Signal Mast
     * Logic.
     *
     * @param sml  The Signal Mast Logic
     * @param dest The destination mast
     */
    public void removeSignalMastLogic(@Nonnull SignalMastLogic sml, @Nonnull SignalMast dest);

    /**
     * Completely remove a specific Signal Mast Logic by name.
     *
     * @param sml The Signal Mast Logic to be removed
     */
    public void removeSignalMastLogic(@Nonnull SignalMastLogic sml);

    /**
     * Completely remove a Signal Mast from all the SMLs that use it.
     *
     * @param mast The Signal Mast to be removed
     */
    public void removeSignalMast(@Nonnull SignalMast mast);

    /**
     * Disable the use of info from the Layout Editor Panels to configure a
     * Signal Mast Logic for a specific Signal Mast.
     *
     * @param mast The Signal Mast for which LE info is to be disabled
     */
    public void disableLayoutEditorUse(@Nonnull SignalMast mast);

    /**
     * Replace the complete Signal Mast Logic configurations between two Source
     * Signal Masts.
     *
     * @param mastA Signal Mast A
     * @param mastB Signal Mast B
     */
    public void swapSignalMasts(@Nonnull SignalMast mastA, @Nonnull SignalMast mastB);

    /**
     * Check if a Signal Mast is in use as either a Source or Destination mast
     * in any Signal Mast Logic
     *
     * @param mast the signal mast to check
     * @return true if mast is used by at least one Signal Mast Logic
     */
    public boolean isSignalMastUsed(@Nonnull SignalMast mast);

    /**
     * @return characteristic delay time in msec, used to control roughly
     *          when signal system computations are done. (Some are half this, some twice)
     */
    public int getSignalLogicDelay();

    /**
     * @param l characteristic delay time in msec, used to control roughly
     *          when signal system computations are done. (Some are half this, some twice)
     */
    public void setSignalLogicDelay(int l);

}
