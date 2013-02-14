package jmri;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LevelXing;

/**
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author			Kevin Dickerson Copyright (C) 2011
 * @version			$Revision$
 */
public interface SignalMastLogic {
    /**
     * Constant representing that all the user entered details relating to a
     * signal logic are stored.
     * Automatically generated details that have been entered via the setAutoBean
     * are not stored.
     */
    public int STOREALL = 0;
    /**
     * Constant representing that only the basic signal mast logic details are stored.
     * All details that determine the triggering of the logic are not stored.
     */
    public int STOREMASTSONLY = 2;
    /**
     * Constant representing that this signal mast logic is not stored with the
     * panel file.
     * This is used where another piece of code uses handles the dynamic creation
     * of signalmast logic
     */
    public int STORENONE = 4;

    /**
     * Query if we are allowing the system to automatically generated a list of
     * conflicting SignalMast that have a direct effect on our logic.
     *
     * @param destination Destination SignalMast.
     * @return true if this is allowed.
     */
    public boolean allowAutoMaticSignalMastGeneration(SignalMast destination);

    /**
     * Sets whether we should allow the system to automatically generate a list of
     * signal masts that could cause a conflicting route.
     *
     * @param destination Destination SignalMast.
     * @param allow set true if we are to allow automatic generation.
     */
    public void allowAutoMaticSignalMastGeneration(boolean allow, SignalMast destination);

    /**
     * Sets whether we should lock all turnouts between the source and destination
     * signal masts when the logic goes active, to prevent them from being changed.
     * This is dependant upon the hardware allowing for this.
     *
     * @param destination Destination SignalMast.
     * @param lock set true if the system should lock the turnout.
     */
    public void allowTurnoutLock(boolean lock, SignalMast destination);

    /**
     * Returns true if any of the blocks in the supplied list are included in any
     * of the logics that set this signal.
     */
    public boolean areBlocksIncluded(ArrayList<Block> blks);

    /**
     * This will replace the existing source SignalMast with a new signal mast instance.
     * This is for use with such tools as the layout editor
     * where a signalmast can at a certain location can be replaced with another, while the
     * remainder of the configuration stays the same.
     */
    public void replaceSourceMast(SignalMast oldMast, SignalMast newMast);
    
    /**
     * This will replace the existing destination SignalMast with a new signal mast instance.
     * This is for use with such tools as the layout editor
     * where a signalmast can at a certain location can be replaced with another, while the
     * remainder of the configuration stays the same.
     */
     public void replaceDestinationMast(SignalMast oldMast, SignalMast newMast);
    
    public void dispose();

    public int getAutoBlockState(Block block, SignalMast destination);

    /**
    * returns all the blocks that have been detected as being in use for this logic,
    * this includes blocks on level xings that are not directly in the path but do
    * have an affect on the logic
    */
    public ArrayList<Block> getAutoBlocks(SignalMast destination);
    
    /**
    * returns only the blocks that have been detected as being directly between
    * the source and destination mast.  The order of the block in the list, is the 
    * order that they are connected.
    */
    public ArrayList<Block> getAutoBlocksBetweenMasts(SignalMast destination);

    public ArrayList<SignalMast> getAutoMasts(SignalMast destination);

    public String getAutoSignalMastState(SignalMast mast, SignalMast destination);

    public int getAutoTurnoutState(Turnout turnout, SignalMast destination);

    public ArrayList<Turnout> getAutoTurnouts(SignalMast destination);

    public int getBlockState(Block block, SignalMast destination);

    public ArrayList<Block> getBlocks(SignalMast destination);

    public String getComment(SignalMast dest);

    public ArrayList<SignalMast> getDestinationList();

    public float getMaximumSpeed(SignalMast destination);

    public int getNumPropertyChangeListeners();

    public int getSensorState(Sensor sensor, SignalMast destination);

    public ArrayList<Sensor> getSensors(SignalMast destination);
    
    public ArrayList<NamedBeanHandle<Sensor>> getNamedSensors(SignalMast destination);

    public String getSignalMastState(SignalMast mast, SignalMast destination);

    public ArrayList<SignalMast> getSignalMasts(SignalMast destination);

    public SignalMast getSourceMast();

    /**
     * returns where the signalmast logic should be stored, if so how much.
     */
    public int getStoreState(SignalMast destination);

    public int getTurnoutState(Turnout turnout, SignalMast destination);

    ArrayList<Turnout> getTurnouts(SignalMast destination);
    
    public ArrayList<NamedBeanHandle<Turnout>> getNamedTurnouts(SignalMast destination);

    public void initialise();

    /**
     * Initialise the signalmast after all the parameters have been set.
     */
    public void initialise(SignalMast destination);

    /**
     * Query if the signalmast logic to the destination signal mast is active.
     */
    public boolean isActive(SignalMast dest);

    public boolean isBlockIncluded(Block block, SignalMast destination);

    public boolean isDestinationValid(SignalMast dest);

    /**
     * Query if the signalmast logic to the destination signal mast is enabled or disabled.
     */
    public boolean isEnabled(SignalMast dest);

    public boolean isSensorIncluded(Sensor sensor, SignalMast destination);

    public boolean isSignalMastIncluded(SignalMast signal, SignalMast destination);

    public boolean isTurnoutIncluded(Turnout turnout, SignalMast destination);

    /**
     * Query if we are allowing the system to lock turnouts when the logic goes
     * active.
     *
     * @param destination Destination SignalMast.
     * @return true if locking is allowed.
     */
    public boolean isTurnoutLockAllowed(SignalMast destination);

    public void removeConflictingLogic(SignalMast sm, LevelXing lx);

    /**
     *
     * @param dest Destination SignalMast.
     * @return true if there are no more destination signal masts
     */
    public boolean removeDestination(SignalMast dest);

    /**
     * Sets which blocks must be inactive for the signal not to be set at a stop aspect
     * These blocks are not stored in the panel file.
     * @param blocks
     */
    public void setAutoBlocks(LinkedHashMap<Block, Integer> blocks, SignalMast destination);

    /**
     * Sets which masts must be in a given state before our mast can be set.
     * These masts are not stored in the panel file.
     * @param masts
     */
    public void setAutoMasts(Hashtable<SignalMast, String> masts, SignalMast destination);

    /**
     * Sets which blocks must be inactive for the signal not to be set at a stop aspect
     * These Turnouts are not stored in the panel file.
     */
    public void setAutoTurnouts(Hashtable<Turnout, Integer> turnouts, SignalMast destination);

    /**
     * Sets which blocks must be inactive for the signal not to be set at a stop aspect
     * @param blocks
     */
    public void setBlocks(Hashtable<Block, Integer> blocks, SignalMast destination);

    public void setComment(String comment, SignalMast dest);

    public void setConflictingLogic(SignalMast sm, LevelXing lx);

    public void setDestinationMast(SignalMast dest);

    /**
     * Sets the logic to the destination signal mast to be disabled.
     */
    public void setDisabled(SignalMast dest);

    /**
     * Sets the logic to the destination signal mast to be enabled.
     */
    public void setEnabled(SignalMast dest);

    public void setFacingBlock(LayoutBlock facing);

    /**
     * Sets which masts must be in a given state before our mast can be set.
     * @param masts
     */
    public void setMasts(Hashtable<SignalMast, String> masts, SignalMast destination);

    /**
     * Sets which sensors must be in a given state before our mast can be set.
     * @param sensors
     */
    public void setSensors(Hashtable<NamedBeanHandle<Sensor>, Integer> sensors, SignalMast destination);
    
    /**
	 * Add an individual sensor and its state to the logic
     */
    public void addSensor(String sensorName, int state, SignalMast destination);
    
    /**
	 * Remove an individual sensor from the logic
     */    
    public void removeSensor(String sensorName, SignalMast destination);
    
    /**
     * Use this to determine if the signalmast logic is stored in the panel file
     * and if all the information is stored.
     * @param store
     */
    public void setStore(int store, SignalMast destination);

    /**
     * Sets the states that each turnout must be in for signal not to be set at a stop aspect
     * @param turnouts
     */
    public void setTurnouts(Hashtable<NamedBeanHandle<Turnout>, Integer> turnouts, SignalMast destination);

    public void setupLayoutEditorDetails();

    /**
     * Sets whether this logic should use the details stored in the layout editor
     * to determine the which blocks, turnouts will make up the logic between
     * the source and destination signal mast.
     *
     * @param boo Use the layout editor details to determine logic details.
     * @param destination Destination SignalMast.
     *
     */
    public void useLayoutEditor(boolean boo, SignalMast destination) throws JmriException;

    /**
     * Query if we are using the layout editor panels to build the signal mast
     * logic, blocks, turnouts .
     *
     * @param destination Destination SignalMast.
     * @return true if we are using the layout editor to build the signal mast logic.
     */
    public boolean useLayoutEditor(SignalMast destination);

    /**
     * Query if we are using the layout editor block information in the
     * signal mast logic.
     *
     * @param destination Destination SignalMast.
     * @return true if we are using the block information from the layout editor.
     */
    public boolean useLayoutEditorBlocks(SignalMast destination);

    /**
     * Sets whether we should use the information from the layout editor for either
     * blocks or turnouts.
     *
     * @param destination Destination SignalMast.
     * @param blocks set false if not to use the block information gathered from the layouteditor
     * @param turnouts set false if not to use the turnout information gathered from the layouteditor
     */
    public void useLayoutEditorDetails(boolean turnouts, boolean blocks, SignalMast destination) throws JmriException;

    /**
     * Query if we are using the layout editor turnout information in the
     * signal mast logic.
     *
     * @param destination Destination SignalMast.
     * @return true if we are using the turnout information from the layout editor.
     */
    public boolean useLayoutEditorTurnouts(SignalMast destination);
    
    public void disableLayoutEditorUse();

    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);
    
    /**
    *  Get the block facing our source signal
    *  
    */
    public LayoutBlock getFacingBlock();
    
    /**
    * Get the block that the source signal is protecting on the path to the 
    * destination signal mast
    */
    public LayoutBlock getProtectingBlock(SignalMast destination);
    
}
