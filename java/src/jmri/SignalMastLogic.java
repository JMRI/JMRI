package jmri;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LevelXing;

/**
 * Generic interface for Signal Mast Logic. Signal Mast Logic allows to build up
 * a set of criteria for a Signal Mast as to what Aspect it should be displaying
 * for a specific route through to a destination Signal Mast.
 *
 *  * @see jmri.implementation.DefaultSignalMastLogic
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public interface SignalMastLogic extends NamedBean {

    /**
     * Constant representing that all the user entered details relating to a
     * signal mast logic are stored. Automatically generated details that have
     * been entered via the setAutoBean are not stored.
     */
    public int STOREALL = 0;
    /**
     * Constant representing that only the basic Signal Mast Logic details are
     * stored. All details that determine the triggering of the logic are not
     * stored.
     */
    public int STOREMASTSONLY = 2;
    /**
     * Constant representing that this Signal Mast Logic is not stored with the
     * panel file. This is used where another piece of code uses handles the
     * dynamic creation of signalmast logic
     */
    public int STORENONE = 4;

    /**
     * Query if we are allowing the system to automatically generate a list of
     * conflicting Signal Mast that have a direct effect on our logic.
     *
     * @param destination controlled signal mast
     * @return true if this is allowed.
     */
    public boolean allowAutoMaticSignalMastGeneration(SignalMast destination);

    /**
     * Sets whether we should allow the system to automatically generate a list
     * of signal masts that could cause a conflicting route.
     *
     * @param allow       set true if we are to allow automatic generation.
     * @param destination controlled signal mast
     */
    public void allowAutoMaticSignalMastGeneration(boolean allow, SignalMast destination);

    /**
     * Sets whether we should lock all turnouts between the source and
     * destination signal masts when the logic goes active, to prevent them from
     * being changed. This is dependant upon the hardware allowing for this.
     *
     * @param lock        set true if the system should lock the turnout.
     * @param destination controlled signal mast
     */
    public void allowTurnoutLock(boolean lock, SignalMast destination);

    /**
     * Returns true if any of the blocks in the supplied list are included in
     * any of the logics that set this signal.
     *
     * @param blks A list of Layout Blocks to query against
     * @return whether all supplied blocks are in at least one of the logics
     */
    public boolean areBlocksIncluded(List<Block> blks);

    /**
     * Replace the existing source Signal Mast with another signal mast. This is
     * for use with such tools as the Layout Editor where a signal mast in a
     * certain location can be replaced with another, while the remainder of the
     * configuration stays the same.
     *
     * @param oldMast Signal Mast currently configured as the source mast
     * @param newMast Signal Mast to act as the replacement source mast
     */
    public void replaceSourceMast(SignalMast oldMast, SignalMast newMast);

    /**
     * Replace the existing destination Signal Mast with another signal mast.
     * This is for use with such tools as the Layout Editor where a signalmast
     * in a certain location can be replaced with another, while the remainder
     * of the configuration stays the same.
     *
     * @param oldMast Signal Mast currently configured as the destination mast
     * @param newMast Signal Mast to act as the replacement destination mast
     */
    public void replaceDestinationMast(SignalMast oldMast, SignalMast newMast);

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    public void dispose();

    /**
     * Return the Section configured between the source and destination mast.
     *
     * @param destination controlled signal mast
     * @return The section object
     */
    public Section getAssociatedSection(SignalMast destination);

    /**
     * Define a Section between the source and destination mast.
     *
     * @param sec         The section
     * @param destination controlled signal mast
     */
    public void setAssociatedSection(Section sec, SignalMast destination);

    /**
     * Return the Set State of a control block as it is configured between the
     * source and destination mast.
     *
     * @param block       The Control Layout Block.
     * @param destination controlled signal mast
     * @return The int value representing the occupancy state that the block
     *         should show
     */
    public int getAutoBlockState(Block block, SignalMast destination);

    /**
     * Return all the blocks that have been detected as being in use for this
     * logic. This includes blocks on level xings that are not directly in the
     * path but do have an effect on the logic.
     *
     * @param destination controlled signal mast
     * @return A list of Block objects
     */
    public List<Block> getAutoBlocks(SignalMast destination);

    /**
     * Return a list of blocks just that have been detected as being directly
     * between the source and destination mast. The order of the blocks in the
     * list is the order in which they are connected.
     *
     * @param destination controlled signal mast
     * @return A list of block objects
     */
    public List<Block> getAutoBlocksBetweenMasts(SignalMast destination);

    /**
     * Return a list of control masts that have been automatically detected as
     * being directly between the source and destination mast. The order of the
     * control masts in the list is the order in which they are connected.
     *
     * @param destination controlled signal mast
     * @return A list of signal mast objects
     */
    public List<SignalMast> getAutoMasts(SignalMast destination);

    /**
     * Return the Set State (Aspect) of a control mast as it is configured
     * between the source and destination mast.
     *
     * @param mast        The Control Signal Mast
     * @param destination controlled signal mast
     * @return The name of the Aspect the Control Mast should display
     */
    public String getAutoSignalMastState(SignalMast mast, SignalMast destination);

    /**
     * Return the Set State of a control turnout as it is configured between the
     * source and destination mast.
     *
     * @param turnout     The Control Turnout
     * @param destination controlled signal mast
     * @return The name of the Aspect the Control Mast should display
     */
    public int getAutoTurnoutState(Turnout turnout, SignalMast destination);

    /**
     * Return only the turnouts that have been detected as being directly
     * between the source and destination mast. The order of the turnouts in the
     * list is the order in which they are connected.
     *
     * @param destination controlled signal mast
     * @return A list of turnout objects
     */
    public List<Turnout> getAutoTurnouts(SignalMast destination);

    /**
     * Return the Set To State of a control block as it is configured between
     * the source and destination mast.
     *
     * @param block       The Control Layout Block
     * @param destination controlled signal mast
     * @return Integer representing the state the control block should be in
     */
    public int getBlockState(Block block, SignalMast destination);

    /**
     * Return the Layout Blocks that have been defined by the user to control
     * the SML to the destination mast.
     *
     * @param destination controlled signal mast
     * @return A list of Block objects
     */
    public List<Block> getBlocks(SignalMast destination);

    /**
     * Get the comment set on this SML.
     *
     * @param destination the mast to get the comment from
     * @return the comment or an empty string
     */
    @Nonnull
    public String getComment(SignalMast destination);

    /**
     * Return a list of all Signal Masts that have been configured as
     * Destination Masts on this SML.
     *
     * @return A list of Signal Mast objects
     */
    public List<SignalMast> getDestinationList();

    /**
     * Get the Maximum Speed set for the destination Signal Mast in this SML.
     *
     * @param destination the destination mast
     * @return A number representing the speed
     */
    public float getMaximumSpeed(SignalMast destination);

    /**
     * Return the number of current listeners defined on this SML.
     *
     * @return the number of listeners; -1 if the information is not available
     *         for some reason.
     */
    @Override
    public int getNumPropertyChangeListeners();

    /**
     * Return the Set To State of a control Sensor as it is configured between
     * the source and destination mast.
     *
     * @param sensor      The Control Sensor
     * @param destination controlled signal mast
     * @return Integer representing the state the control Sensor should be in
     */
    public int getSensorState(Sensor sensor, SignalMast destination);

    /**
     * Return the Sensors that have been defined by the user to control the SML
     * to the destination mast.
     *
     * @param destination controlled signal mast
     * @return A list of Sensor objects
     */
    public List<Sensor> getSensors(SignalMast destination);

    /**
     * Return the Sensors that have been defined by the user to control the SML
     * to the destination mast as NamedBeanHandles.
     *
     * @param destination controlled signal mast
     * @return A list of Sensor NamedBeanHandles
     */
    public List<NamedBeanHandle<Sensor>> getNamedSensors(SignalMast destination);

    /**
     * Return the Set To State (Aspect) of a control Signal Mast as it is
     * configured between the source and destination mast.
     *
     * @param mast        The Control Signal Mast
     * @param destination controlled signal mast
     * @return Integer representing the state the control Signal Mast should be
     *         in
     */
    public String getSignalMastState(SignalMast mast, SignalMast destination);

    /**
     * Return the Signal Masts that have been defined by the user to control the
     * SML to the destination mast.
     *
     * @param destination controlled signal mast
     * @return A list of Signal Mast objects
     */
    public List<SignalMast> getSignalMasts(SignalMast destination);

    public SignalMast getSourceMast();

    /**
     * Return the Set State of a control Turnout as it is configured between the
     * source and destination mast.
     *
     * @param turnout     The Control Turnout
     * @param destination controlled signal mast
     * @return Integer representing the state the control Sensor should be in
     */
    public int getTurnoutState(Turnout turnout, SignalMast destination);

    /**
     * Return the Turnouts that have been defined by the user to control the SML
     * to the destination mast.
     *
     * @param destination controlled signal mast
     * @return A list of Turnout objects
     */
    public List<Turnout> getTurnouts(SignalMast destination);

    /**
     * Return the Turnouts that have been defined by the user to control the SML
     * to the destination mast as NamedBeanHandles.
     *
     * @param destination controlled signal mast
     * @return A list of Turnout NamedBeanHandles
     */
    public List<NamedBeanHandle<Turnout>> getNamedTurnouts(SignalMast destination);

    /**
     * General method to initialise all SMLs on the source SIgnal Mast using
     * destList
     */
    public void initialise();

    /**
     * Initialise the signal mast after all the parameters have been set.
     *
     * @param destination controlled signal mast
     */
    public void initialise(SignalMast destination);

    /**
     * Query if the Signal Mast Logic from the current source signal mast to the
     * destination signal mast is active.
     *
     * @param destination controlled signal mast
     * @return true if active; false otherwise
     */
    public boolean isActive(SignalMast destination);

    /**
     * Get the active destination Signal Mast for this Signal Mast Logic.
     *
     * @return the active signal mast or null if none
     */
    @CheckForNull
    public SignalMast getActiveDestination();

    /**
     * Check whether the Block is part of at least one of the logics.
     *
     * @param block       a layout block
     * @param destination controlled signal mast
     * @return true if block is included in any of the Signal Mast Logics that
     *         set destination
     */
    public boolean isBlockIncluded(Block block, SignalMast destination);

    /**
     * Check if signal mast is a destination signal mast in one of the logics
     *
     * @param destination controlled signal mast
     * @return true if destination is a destination mast in this object
     */
    public boolean isDestinationValid(SignalMast destination);

    /**
     * Query if the Signal Mast Logic from the current source signal mast to the
     * specified destination signal mast is enabled.
     *
     * @param destination controlled signal mast
     * @return true if enabled
     */
    public boolean isEnabled(SignalMast destination);

    /**
     * Check if a sensor is part of at least one of the logics that set a
     * SignalMast.
     *
     * @param sensor      the sensor to check
     * @param destination controlled signal
     * @return true if sensor is included in any of the Signal Mast Logics that
     *         set destination
     */
    public boolean isSensorIncluded(Sensor sensor, SignalMast destination);

    /**
     * Check if a signal mast is part of at least one of the logics that set
     * another signal mast.
     *
     * @param signal      the signal mast to check
     * @param destination controlled signal mast
     * @return true if signal is included in any of the Signal Mast Logics that
     *         set destination
     */
    public boolean isSignalMastIncluded(SignalMast signal, SignalMast destination);

    /**
     * Check if a turnout is part of at least one of the logics that set a
     * signal mast.
     *
     * @param turnout     the turnout to check
     * @param destination controlled signal mast
     * @return true if turnout is included in any of the Signal Mast Logics that
     *         set destination
     */
    public boolean isTurnoutIncluded(Turnout turnout, SignalMast destination);

    /**
     * Query if we are allowing the system to lock turnouts when the logic goes
     * active.
     *
     * @param destination controlled signal mast
     * @return true if locking is allowed.
     */
    public boolean isTurnoutLockAllowed(SignalMast destination);

    /**
     * Remove control elements for a SML pair containing a destination signal
     * mast that itself is incompatible with an SML around a level crossing.
     *
     * @param sm The destination Signal Mast
     * @param lx The LevelXing Layout Editor element
     */
    public void removeConflictingLogic(SignalMast sm, LevelXing lx);

    /**
     * Remove the destination signal mast as a pair in this SML.
     *
     * @param destination controlled signal mast
     * @return true if there are no more destination signal masts
     */
    public boolean removeDestination(SignalMast destination);

    /**
     * Set which blocks must be in a given state for the signal mast not to be
     * set to a Stop aspect. These blocks are not stored in the panel file.
     *
     * @param blocks      map of {@link Block}s and their respective set to
     *                    state to be checked
     * @param destination controlled signal mast
     */
    public void setAutoBlocks(LinkedHashMap<Block, Integer> blocks, SignalMast destination);

    /**
     * Set which control signal masts must be in a given state before our source
     * mast can be set. These Signal Masts are not stored in the panel file.
     *
     * @param masts       list of control signal masts and their respective set
     *                    to state to be checked
     * @param destination controlled signal mast
     */
    public void setAutoMasts(Hashtable<SignalMast, String> masts, SignalMast destination);

    /**
     * Set which turnouts must be set to a given state for the signal mast not
     * to be set to a Stop aspect. These Turnouts are not stored in the panel
     * file.
     *
     * @param turnouts    map of turnouts and their respective set to state
     * @param destination controlled signal mast
     */
    public void setAutoTurnouts(Hashtable<Turnout, Integer> turnouts, SignalMast destination);

    /**
     * Set which blocks must be in a given state for the signal mast not to be
     * set to a Stop aspect.
     *
     * @param blocks      map of Blocks and their respective set to state
     * @param destination controlled signal mast
     */
    public void setBlocks(Hashtable<Block, Integer> blocks, SignalMast destination);

    /**
     * Set the comment for this SML.
     *
     * @param comment     text to add as comment
     * @param destination signal mast to add comment to
     */
    public void setComment(String comment, SignalMast destination);

    /**
     * Add control elements for a SML pair containing a destination signal mast
     * that itself is skipped as it is incompatible with an SML around a level
     * crossing.
     *
     * @param sm The destination Signal Mast
     * @param lx The LevelXing Layout Editor element
     */
    public void setConflictingLogic(SignalMast sm, LevelXing lx);

    /**
     * Set the destination signal mast for this SML.
     *
     * @param destination controlled signal mast
     */
    public void setDestinationMast(SignalMast destination);

    /**
     * Set the logic to the destination signal mast to Disabled.
     *
     * @param destination controlled signal mast
     */
    public void setDisabled(SignalMast destination);

    /**
     * Set the logic to the destination signal mast to Enabled.
     *
     * @param destination controlled signal mast
     */
    public void setEnabled(SignalMast destination);

    /**
     * Set the block facing our source signal mast.
     *
     * @param facing The Layout Block facing the source Signal Mast
     */
    public void setFacingBlock(LayoutBlock facing);

    /**
     * Get the block defined as facing our source signal mast.
     *
     * @return The Layout Block facing the source Signal Mast
     */
    public LayoutBlock getFacingBlock();

    /**
     * Set which control signal masts must be in a given state before our source
     * mast can be set.
     *
     * @param masts       map of control signal masts and respective set to
     *                    states to be checked
     * @param destination controlled signal mast
     */
    public void setMasts(Hashtable<SignalMast, String> masts, SignalMast destination);

    /**
     * Set which sensors must be in a given state before our source signal mast
     * can be set.
     *
     * @param sensors     The {@link Sensor}s to be checked
     * @param destination controlled signal mast
     */
    public void setSensors(Hashtable<NamedBeanHandle<Sensor>, Integer> sensors, SignalMast destination);

    /**
     * Add an individual control Sensor and its set to state to the Signal Mast
     * Logic.
     *
     * @param sensorName  The sensor to be removed
     * @param state       Integer representing the state the control Sensor
     *                    should be in
     * @param destination controlled signal mast
     */
    public void addSensor(String sensorName, int state, SignalMast destination);

    /**
     * Remove an individual control Sensor from the Signal Mast Logic.
     *
     * @param sensorName  The sensor to be removed
     * @param destination controlled signal mast
     */
    public void removeSensor(String sensorName, SignalMast destination);

    /**
     * Determine if the signal mast logic is stored in the panel file and if all
     * the information is stored.
     *
     * @param store       one of {@link #STOREALL}, {@link #STOREMASTSONLY} or
     *                    {@link #STORENONE}
     * @param destination controlled signal mast
     */
    public void setStore(int store, SignalMast destination);

    /**
     * Return where the signal mast logic should be stored, if so how much.
     *
     * @param destination controlled signal mast
     * @return one of {@link #STOREALL}, {@link #STOREMASTSONLY} or
     *         {@link #STORENONE}
     */
    public int getStoreState(SignalMast destination);

    /**
     * Set the states that each control turnout must be in for the source signal
     * mast not to be set to a Stop aspect.
     *
     * @param turnouts    A list of named turnouts and their respective set to
     *                    state to check
     * @param destination controlled signal mast
     */
    public void setTurnouts(Hashtable<NamedBeanHandle<Turnout>, Integer> turnouts, SignalMast destination);

    /**
     * Set up a Signal Mast Logic from the Layout Editor panel where its source
     * Signal Mast is present, when useLayoutEditor is set to true.
     */
    public void setupLayoutEditorDetails();

    /**
     * Set whether this logic should use the details stored in the Layout Editor
     * to determine which blocks, turnouts will make up the logic between the
     * source and destination signal mast.
     *
     * @param boo         Use the Layout Editor details to determine logic
     *                    details
     * @param destination the Destination Signal Mast
     * @throws jmri.JmriException if a path on the layout editor is not valid
     */
    public void useLayoutEditor(boolean boo, SignalMast destination) throws JmriException;

    /**
     * Query if we are using the Layout Editor panels to build the signal mast
     * logic, blocks, turnouts.
     *
     * @param destination Destination Signal Mast
     * @return true if we are using the Layout Editor to build the signal mast
     *         logic.
     */
    public boolean useLayoutEditor(SignalMast destination);

    /**
     * Query if we are using the Layout Editor block information in the signal
     * mast logic.
     *
     * @param destination Destination Signal Mast
     * @return true if we are using the block information from the Layout
     *         Editor.
     */
    public boolean useLayoutEditorBlocks(SignalMast destination);

    /**
     * Set whether this logic should use the information from the Layout Editor
     * for either blocks or turnouts.
     *
     * @param turnouts    set false if not to use the turnout information
     *                    gathered from the layout editor
     * @param blocks      set false if not to use the block information gathered
     *                    from the layout editor
     * @param destination Destination Signal Mast
     * @throws jmri.JmriException if a path on the layout editor is not valid
     */
    public void useLayoutEditorDetails(boolean turnouts, boolean blocks, SignalMast destination) throws JmriException;

    /**
     * Query if we are using the Layout Editor turnout information in the signal
     * mast logic.
     *
     * @param destination controlled signal mast
     * @return true if we are using the turnout information from the Layout
     *         Editor.
     */
    public boolean useLayoutEditorTurnouts(SignalMast destination);

    public void disableLayoutEditorUse();

    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Get the block that the source signal is protecting on the path to the
     * destination signal mast.
     *
     * @param destination controlled signal mast
     * @return the Layout Block
     */
    public LayoutBlock getProtectingBlock(SignalMast destination);

    /**
     * Set the auto turnouts based upon a given list of layout blocks for a
     * specific destination mast.
     *
     * @param blks        List of Layout Blocks.
     * @param destination Destination Signal Mast
     * @return A LinkedHashMap of the original blocks and their required state,
     *         plus any blocks found on double cross-overs that also need to be
     *         un-occupied.
     */
    public LinkedHashMap<Block, Integer> setupLayoutEditorTurnoutDetails(List<LayoutBlock> blks, SignalMast destination);

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException;

}
