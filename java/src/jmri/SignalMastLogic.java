package jmri;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LevelXing;

/**
 * Generic interface for Signal Mast Logic.
 * Signal Mast Logic allows to build up a set of criteria for a Signal Mast as
 * to what Aspect it should be displaying for a specific route through to a
 * destination Signal Mast.
 *
 *  * @see jmri.implementation.DefaultSignalMastLogic
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 */
public interface SignalMastLogic {

    /**
     * Constant representing that all the user entered details relating to a
     * signal mast logic are stored. Automatically generated details that have been
     * entered via the setAutoBean are not stored.
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
     * @param destination The destination Signal Mast
     * @return true if this is allowed.
     */
    public boolean allowAutoMaticSignalMastGeneration(SignalMast destination);

    /**
     * Sets whether we should allow the system to automatically generate a list
     * of signal masts that could cause a conflicting route.
     *
     * @param allow       set true if we are to allow automatic generation.
     * @param destination The destination Signal Mast
     */
    public void allowAutoMaticSignalMastGeneration(boolean allow, SignalMast destination);

    /**
     * Sets whether we should lock all turnouts between the source and
     * destination signal masts when the logic goes active, to prevent them from
     * being changed. This is dependant upon the hardware allowing for this.
     *
     * @param lock        set true if the system should lock the turnout.
     * @param destination The destination Signal Mast
     */
    public void allowTurnoutLock(boolean lock, SignalMast destination);

    /**
     * Returns true if any of the blocks in the supplied list are included in
     * any of the logics that set this signal.
     *
     * @param blks A list of Layout Blocks to query against
     * @return whether all supplied blocks are in at least one of the logics
     */
    public boolean areBlocksIncluded(ArrayList<Block> blks);

    /**
     * Replace the existing source Signal Mast with another signal mast.
     * This is for use with such tools as the Layout Editor where a
     * signal mast in a certain location can be replaced with another,
     * while the remainder of the configuration stays the same.
     *
     * @param oldMast Signal Mast currently configured as the source mast
     * @param newMast Signal Mast to act as the replacement source mast
     */
    public void replaceSourceMast(SignalMast oldMast, SignalMast newMast);

    /**
     * Replace the existing destination Signal Mast with another signal mast.
     * This is for use with such tools as the Layout Editor where a
     * signalmast in a certain location can be replaced with another,
     * while the remainder of the configuration stays the same.
     *
     * @param oldMast Signal Mast currently configured as the destination mast
     * @param newMast Signal Mast to act as the replacement destination mast
     */
    public void replaceDestinationMast(SignalMast oldMast, SignalMast newMast);

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    public void dispose();

    /**
     * Return the Section configured between the source and destination mast.
     *
     * @param destination The destination Signal Mast
     * @return The section object
     */
    public Section getAssociatedSection(SignalMast destination);

    /**
     * Define a Section between the source and destination mast.
     *
     * @param sec         The section
     * @param destination The destination Signal Mast
     */
    public void setAssociatedSection(Section sec, SignalMast destination);

    /**
     * Return the Set State of a control block as it is configured between
     * the source and destination mast.
     *
     * @param block       The Control Layout Block.
     * @param destination The destination Signal Mast
     * @return The int value representing the occupancy state that the block should show
     */
    public int getAutoBlockState(Block block, SignalMast destination);

    /**
     * Return all the blocks that have been detected as being in use for this
     * logic. This includes blocks on level xings that are not directly in the
     * path but do have an effect on the logic.
     *
     * @param destination The destination Signal Mast
     * @return A list of Block objects
     */
    public ArrayList<Block> getAutoBlocks(SignalMast destination);

    /**
     * Return a list of blocks just that have been detected as being directly between
     * the source and destination mast. The order of the blocks in the list is
     * the order in which they are connected.
     *
     * @param destination The destination Signal Mast
     * @return A list of block objects
     */
    public ArrayList<Block> getAutoBlocksBetweenMasts(SignalMast destination);

    /**
     * Return a list of control masts that have been automatically detected as being
     * directly between the source and destination mast. The order of the control
     * masts in the list is the order in which they are connected.
     *
     * @param destination The destination Signal Mast
     * @return A list of signal mast objects
     */
    public ArrayList<SignalMast> getAutoMasts(SignalMast destination);

    /**
     * Return the Set State (Aspect) of a control mast as it is configured between
     * the source and destination mast.
     *
     * @param mast        The Control Signal Mast
     * @param destination The destination Signal Mast
     * @return The name of the Aspect the Control Mast should display
     */
    public String getAutoSignalMastState(SignalMast mast, SignalMast destination);

    /**
     * Return the Set State of a control turnout as it is configured between
     * the source and destination mast.
     *
     * @param mast        The Control Signal Mast
     * @param destination The destination Signal Mast
     * @return The name of the Aspect the Control Mast should display
     */
    public int getAutoTurnoutState(Turnout turnout, SignalMast destination);

    /**
     * Return only the turnouts that have been detected as being directly between
     * the source and destination mast. The order of the turnouts in the list is
     * the order in which they are connected.
     * @param destination The destination Signal Mast
     * @return A list of turnout objects
     */
    public ArrayList<Turnout> getAutoTurnouts(SignalMast destination);

    /**
     * Return the Set To State of a control block as it is configured between
     * the source and destination mast.
     *
     * @param block       The Control Layout Block
     * @param destination The destination Signal Mast
     * @return Integer representing the state the control block should be in
     */
    public int getBlockState(Block block, SignalMast destination);

    /**
     * Return the Layout Blocks that have been defined by the user to
     * control the SML to the destination mast.
     *
     * @param destination The destination Signal Mast
     * @return A list of Block objects
     */
    public ArrayList<Block> getBlocks(SignalMast destination);

    /**
     * Get the comment set on this SML.
     *
     * @return Text of the comment
     */
    public String getComment(SignalMast dest);

    /**
     * Return a list of all Signal Masts that have been configured
     * as Desitnation Masts on this SML.
     *
     * @return A list of Signal Mast objects
     */
    public ArrayList<SignalMast> getDestinationList();

    /**
     * Get the Maximum Speed set for the destination Signal Mast
     * in this SML.
     *
     * @return A number representing the speed
     */
    public float getMaximumSpeed(SignalMast destination);

     /**
     * Return the number of current listeners defined on this SML.
     *
     * @return the number of listeners; -1 if the information is not
      * available for some reason.
     */
    @CheckReturnValue
    public int getNumPropertyChangeListeners();

    /**
     * Return the Set To State of a control Sensor as it is configured between
     * the source and destination mast.
     *
     * @param sensor      The Control Sensor
     * @param destination The destination Signal Mast
     * @return Integer representing the state the control Sensor should be in
     */
    public int getSensorState(Sensor sensor, SignalMast destination);

    /**
     * Return the Sensors that have been defined by the user to
     * control the SML to the destination mast.
     *
     * @param destination The destination Signal Mast
     * @return A list of Sensor objects
     */
    public ArrayList<Sensor> getSensors(SignalMast destination);

    /**
     * Return the Sensors that have been defined by the user to
     * control the SML to the destination mast as NamedBeanHandles.
     *
     * @param destination The destination Signal Mast
     * @return A list of Sensor NamedBeanHandles
     */
    public ArrayList<NamedBeanHandle<Sensor>> getNamedSensors(SignalMast destination);

    /**
     * Return the Set To State (Aspect) of a control Signal Mast as it is
     * configured between the source and destination mast.
     *
     * @param mast        The Control Signal Mast
     * @param destination The destination Signal Mast
     * @return Integer representing the state the control Signal Mast should be in
     */
    public String getSignalMastState(SignalMast mast, SignalMast destination);

    /**
     * Return the Signal Masts that have been defined by the user to
     * control the SML to the destination mast.
     *
     * @param destination The destination Signal Mast
     * @return A list of Signal Mast objects
     */
    public ArrayList<SignalMast> getSignalMasts(SignalMast destination);

    public SignalMast getSourceMast();

    /**
     * Return the Set State of a control Turnout as it is configured between
     * the source and destination mast.
     *
     * @param turnout     The Control Turnout
     * @param destination The destination Signal Mast
     * @return Integer representing the state the control Sensor should be in
     */
    public int getTurnoutState(Turnout turnout, SignalMast destination);

    /**
     * Return the Turnouts that have been defined by the user to
     * control the SML to the destination mast.
     *
     * @param destination The destination Signal Mast
     * @return A list of Turnout objects
     */
    public ArrayList<Turnout> getTurnouts(SignalMast destination);

    /**
     * Return the Turnouts that have been defined by the user to
     * control the SML to the destination mast as NamedBeanHandles.
     *
     * @param destination The destination Signal Mast
     * @return A list of Turnout NamedBeanHandles
     */
    public ArrayList<NamedBeanHandle<Turnout>> getNamedTurnouts(SignalMast destination);

    /**
     * General method to initialise all SMLs on the source SIgnal Mast using destList
     */
    public void initialise();

    /**
     * Initialise the signalmast after all the parameters have been set.
     *
     * @param destination The destination Signal Mast
     */
    public void initialise(SignalMast destination);

    /**
     * Query if the Signal Mast Logic from the current source signal mast
     * to the destination signal mast is active.
     *
     * @param dest The destination Signal Mast
     */
    public boolean isActive(SignalMast dest);

    /**
     * Return the active destination Signal Mast for this Signal Mast Logic.
     */
    public SignalMast getActiveDestination();

    /**
     * Returns true if a given control Block is included in any of the Signal Mast
     * Logics that set this source Signal Mast.
     *
     * @param block A Layout Block
     * @return whether the Block is part of at least one of the logics
     */
    public boolean isBlockIncluded(Block block, SignalMast destination);

    /**
     * Returns true if a given control Signal Mast is used as a destination mast
     * in this Signal Mast Logic.
     *
     * @param dest The Signal Mast to be checked
     * @return whether the mast is set up as a destination Signal Mast in one of the logics
     */
    public boolean isDestinationValid(SignalMast dest);

    /**
     * Query if the Signal Mast Logic from the current source signal mast
     * to the specified destination signal mast is enabled.
     *
     * @param dest The destination Signal Mast
     * @return true if enabled
     */
    public boolean isEnabled(SignalMast dest);

    /**
     * Returns true if a given control Sensor is included in any of the Signal Mast
     * Logics that set this source Signal Mast.
     *
     * @param sensor A Sensor
     * @return whether the Sensor is part of at least one of the logics
     */
    public boolean isSensorIncluded(Sensor sensor, SignalMast destination);

    /**
     * Returns true if a given Signal Mast is included in any of the Signal Mast
     * Logics that set this source Signal Mast.
     *
     * @param signal A Signal Mast
     * @return whether the Signal Mast is part of at least one of the logics
     */
    public boolean isSignalMastIncluded(SignalMast signal, SignalMast destination);

    /**
     * Returns true if a given control Turnout is included in any of the Signal Mast
     * Logics that set this source Signal Mast.
     *
     * @param turnout A Turnout
     * @return whether the Turnout is part of at least one of the logics
     */
    public boolean isTurnoutIncluded(Turnout turnout, SignalMast destination);

    /**
     * Query if we are allowing the system to lock turnouts when the logic goes
     * active.
     *
     * @param destination The destination Signal Mast
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
     * @param dest The destination Signal Mast
     * @return true if there are no more destination signal masts
     */
    public boolean removeDestination(SignalMast dest);

    /**
     * Set which blocks must be in a given state for the signal mast not to be
     * set to a Stop aspect. These blocks are not stored in the panel file.
     *
     * @param blocks A hashlist of {@link Block}s and their respective set to state to be checked
     */
    public void setAutoBlocks(LinkedHashMap<Block, Integer> blocks, SignalMast destination);

    /**
     * Set which control signal masts must be in a given state before our source mast can be set.
     * These Signal Masts are not stored in the panel file.
     *
     * @param masts       A list of control signal masts and their respective set to state to be checked
     * @param destination The destination Signal {@link SignalMast}s
     */
    public void setAutoMasts(Hashtable<SignalMast, String> masts, SignalMast destination);

    /**
     * Set which turnouts must be set to a given state for the signal mast not to be
     * set to a Stop aspect. These Turnouts are not stored in the panel file.
     *
     * @param turnouts    A hashlist of turnouts and their respective set to state
     * @param destination The destination Signal Mast
     */
    public void setAutoTurnouts(Hashtable<Turnout, Integer> turnouts, SignalMast destination);

    /**
     * Set which blocks must be in a given state for the signal mast not to be set to a Stop
     * aspect.
     *
     * @param blocks      A hashlist of Blocks and their respective set to state
     * @param destination The destination Signal Mast
     */
    public void setBlocks(Hashtable<Block, Integer> blocks, SignalMast destination);

    /**
     * Set the comment for this SML.
     *
     * @param comment Text to add as comment
     * @param dest    The destination Signal Mast
     */
    public void setComment(String comment, SignalMast dest);

    /**
     * Add control elements for a SML pair containing a destination signal
     * mast that itself is skipped as it is incompatible with an SML around
     * a level crossing.
     *
     * @param sm The destination Signal Mast
     * @param lx The LevelXing Layout Editor element
     */
    public void setConflictingLogic(SignalMast sm, LevelXing lx);

    /**
     * Set the destination signal mast for this SML.
     *
     * @param dest The destination Signal Mast
     */
    public void setDestinationMast(SignalMast dest);

    /**
     * Set the logic to the destination signal mast to Disabled.
     *
     * @param dest The destination Signal Mast
     */
    public void setDisabled(SignalMast dest);

    /**
     * Set the logic to the destination signal mast to Enabled.
     *
     * @param dest The destination Signal Mast
     */
    public void setEnabled(SignalMast dest);

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
     * Set which control signal masts must be in a given state before our source mast can be set.
     *
     * @param masts Hashtable of control signal masts and respective set to states to be checked
     */
    public void setMasts(Hashtable<SignalMast, String> masts, SignalMast destination);

    /**
     * Set which sensors must be in a given state before our source signal mast can be set.
     *
     * @param sensors     The {@link Sensor}s to be checked
     * @param destination The destination Signal Mast
     */
    public void setSensors(Hashtable<NamedBeanHandle<Sensor>, Integer> sensors, SignalMast destination);

    /**
     * Add an individual control Sensor and its set to state to the Signal Mast Logic.
     *
     * @param sensorName The sensor to be removed
     * @param state Integer representing the state the control Sensor should be in
     * @param destination The destination Signal Mast
     */
    public void addSensor(String sensorName, int state, SignalMast destination);

    /**
     * Remove an individual control Sensor from the Signal Mast Logic.
     *
     * @param sensorName The sensor to be removed
     * @param destination The destination Signal Mast
     */
    public void removeSensor(String sensorName, SignalMast destination);

    /**
     * Determine if the signal mast logic is stored in the panel file
     * and if all the information is stored.
     *
     * @param store one of {@link #STOREALL}, {@link #STOREMASTSONLY} or
     *              {@link #STORENONE}
     * @param destination The destination Signal Mast
     */
    public void setStore(int store, SignalMast destination);

    /**
     * Return where the signal mast logic should be stored, if so how much.
     *
     * @param destination The destination Signal Mast
     * @return one of {@link #STOREALL}, {@link #STOREMASTSONLY} or
     *              {@link #STORENONE}
     */
    public int getStoreState(SignalMast destination);

    /**
     * Set the states that each control turnout must be in for the source
     * signal mast not to be set to a Stop aspect.
     *
     * @param turnouts    A list of named turnouts and their respective set to
     *                    state to check
     * @param destination The destination Signal Mast
     */
    public void setTurnouts(Hashtable<NamedBeanHandle<Turnout>, Integer> turnouts, SignalMast destination);

    /**
     * Set up a Signal Mast Logic from the Layout Editor panel where its source
     * Signal Mast is present, when useLayoutEditor is set to true.
     */
    public void setupLayoutEditorDetails();

    /**
     * Set whether this logic should use the details stored in the Layout
     * Editor to determine which blocks, turnouts will make up the logic
     * between the source and destination signal mast.
     *
     * @param boo         Use the Layout Editor details to determine logic
     *                    details.
     * @param destination the Destination Signal Mast
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
     *                    gathered from the layouteditor
     * @param blocks      set false if not to use the block information gathered
     *                    from the layouteditor
     * @param destination Destination Signal Mast
     */
    public void useLayoutEditorDetails(boolean turnouts, boolean blocks, SignalMast destination) throws JmriException;

    /**
     * Query if we are using the Layout Editor turnout information in the signal
     * mast logic.
     *
     * @param destination The destination Signal Mast
     * @return true if we are using the turnout information from the Layout
     *         Editor.
     */
    public boolean useLayoutEditorTurnouts(SignalMast destination);

    public void disableLayoutEditorUse();

    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Get the block that the source signal is protecting on the path to the
     * destination signal mast.
     *
     * @param destination The destination Signal Mast
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

    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException;

}
