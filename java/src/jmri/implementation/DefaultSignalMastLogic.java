package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import jmri.Block;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.Section;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.ConnectivityUtil;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LevelXing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 */
public class DefaultSignalMastLogic implements jmri.SignalMastLogic, java.beans.VetoableChangeListener {

    SignalMast source;
    SignalMast destination;
    String stopAspect;

    Hashtable<SignalMast, DestinationMast> destList = new Hashtable<SignalMast, DestinationMast>();
    LayoutEditor editor;

    boolean useAutoGenBlock = true;
    boolean useAutoGenTurnouts = true;

    LayoutBlock facingBlock = null;

    boolean disposing = false;

    /**
     * Initialise the signal mast logic
     *
     * @param source - The signalmast we are configuring
     */
    public DefaultSignalMastLogic(@Nonnull SignalMast source) {
        this.source = source;
        try {
            this.stopAspect = source.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER);
            this.source.addPropertyChangeListener(propertySourceMastListener);
            if (source.getAspect() == null) {
                source.setAspect(stopAspect);
            }
        } catch (Exception ex) {
            log.error("Error while creating Signal Logic " + ex.toString());
        }
    }

    public void setFacingBlock(LayoutBlock facing) {
        facingBlock = facing;
    }

    public LayoutBlock getFacingBlock() {
        return facingBlock;
    }

    public LayoutBlock getProtectingBlock(@Nonnull SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return null;
        }
        return destList.get(dest).getProtectingBlock();
    }

    public SignalMast getSourceMast() {
        return source;
    }

    public void replaceSourceMast(SignalMast oldMast, SignalMast newMast) {
        if (oldMast != source) {
            //Old mast does not match new mast so will exit
            return;
        }
        source.removePropertyChangeListener(propertySourceMastListener);
        source = newMast;
        stopAspect = source.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER);
        source.addPropertyChangeListener(propertySourceMastListener);
        if (source.getAspect() == null) {
            source.setAspect(stopAspect);
        }
        for (SignalMast sm : getDestinationList()) {
            DestinationMast destMast = destList.get(sm);
            if (destMast.getAssociatedSection() != null) {
                String oldUserName = destMast.getAssociatedSection().getUserName();
                String newUserName = source.getDisplayName() + ":" + sm.getDisplayName();
                jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).renameBean(oldUserName, newUserName, ((NamedBean) destMast.getAssociatedSection()));
            }
        }
        firePropertyChange("updatedSource", oldMast, newMast);
    }

    public void replaceDestinationMast(SignalMast oldMast, SignalMast newMast) {
        if (!destList.containsKey(oldMast)) {
            return;
        }
        DestinationMast destMast = destList.get(oldMast);
        destMast.updateDestinationMast(newMast);
        if (destination == oldMast) {
            oldMast.removePropertyChangeListener(propertyDestinationMastListener);
            newMast.addPropertyChangeListener(propertyDestinationMastListener);
            destination = newMast;
            setSignalAppearance();
        }
        destList.remove(oldMast);
        if (destMast.getAssociatedSection() != null) {
            String oldUserName = destMast.getAssociatedSection().getUserName();
            String newUserName = source.getDisplayName() + ":" + newMast.getDisplayName();
            jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).renameBean(oldUserName, newUserName, destMast.getAssociatedSection());
        }
        destList.put(newMast, destMast);
        firePropertyChange("updatedDestination", oldMast, newMast);
    }

    public void setDestinationMast(SignalMast dest) {
        if (destList.containsKey(dest)) {
            return;
        }
        int oldSize = destList.size();
        destList.put(dest, new DestinationMast(dest));
        //InstanceManager.signalMastLogicManagerInstance().addDestinationMastToLogic(this, dest);
        firePropertyChange("length", oldSize, Integer.valueOf(destList.size()));
    }

    public boolean isDestinationValid(SignalMast dest) {
        if (dest == null) {
            return false;
        }
        return destList.containsKey(dest);
    }

    public ArrayList<SignalMast> getDestinationList() {
        ArrayList<SignalMast> out = new ArrayList<SignalMast>();
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    public String getComment(SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return "";
        }
        return destList.get(dest).getComment();
    }

    public void setComment(String comment, SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return;
        }
        destList.get(dest).setComment(comment);
    }

    /**
     * Use this to determine if the signalmast logic is stored in the panel file
     * and if all the information is stored.
     *
     * @param store
     */
    public void setStore(int store, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setStore(store);
    }

    /**
     * returns where the signalmast logic should be stored, if so how much.
     */
    public int getStoreState(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return STORENONE;
        }
        return destList.get(destination).getStoreState();
    }

    /**
     * Sets the logic to the destination signal mast to be enabled.
     */
    public void setEnabled(SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return;
        }
        destList.get(dest).setEnabled();
    }

    /**
     * Sets the logic to the destination signal mast to be disabled.
     */
    public void setDisabled(SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return;
        }
        destList.get(dest).setDisabled();
    }

    /**
     * Query if the signalmast logic to the destination signal mast is enabled
     * or disabled.
     */
    public boolean isEnabled(SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return false;
        }
        return destList.get(dest).isEnabled();
    }

    /**
     * Query if the signalmast logic to the destination signal mast is active.
     */
    public boolean isActive(SignalMast dest) {
        if (!destList.containsKey(dest)) {
            return false;
        }
        return destList.get(dest).isActive();
    }

    public SignalMast getActiveDestination() {
        for (SignalMast sm : getDestinationList()) {
            if (destList.get(sm).isActive()) {
                return sm;
            }
        }
        return null;
    }

    /**
     *
     * @param dest Destination SignalMast.
     * @return true if there are no more destination signal masts
     */
    public boolean removeDestination(SignalMast dest) {
        int oldSize = destList.size();
        if (destList.containsKey(dest)) {
            //InstanceManager.signalMastLogicManagerInstance().removeDestinationMastToLogic(this, dest);
            destList.get(dest).dispose();
            destList.remove(dest);
            firePropertyChange("length", oldSize, Integer.valueOf(destList.size()));
        }
        if (destList.isEmpty()) {
            return true;
        }
        return false;
    }

    public void disableLayoutEditorUse() {
        for (DestinationMast dest : destList.values()) {
            try {
                dest.useLayoutEditor(false);
            } catch (jmri.JmriException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Sets whether this logic should use the details stored in the layout
     * editor to determine the which blocks, turnouts will make up the logic
     * between the source and destination signal mast.
     *
     * @param boo         Use the layout editor details to determine logic
     *                    details.
     * @param destination Destination SignalMast.
     *
     */
    public void useLayoutEditor(boolean boo, SignalMast destination) throws jmri.JmriException {
        if (!destList.containsKey(destination)) {
            return;
        }

        if (boo) {
            log.debug("Set use layout editor");
            ArrayList<LayoutEditor> layout = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
            /*We don't care which layout editor panel the signalmast is on, just so long as
             as the routing is done via layout blocks*/
            // TODO: what is this?
            log.debug("userLayoutEditor finds layout size is {}", Integer.toString(layout.size()));
            for (int i = 0; i < layout.size(); i++) {
                if (log.isDebugEnabled()) {
                    log.debug(layout.get(i).getLayoutName());
                }
                if (facingBlock == null) {
                    facingBlock = InstanceManager.getDefault(LayoutBlockManager.class).getFacingBlockByMast(getSourceMast(), layout.get(i));
                }
            }
        }
        try {
            destList.get(destination).useLayoutEditor(boo);
        } catch (jmri.JmriException e) {
            throw e;
        }
    }

    /**
     * Query if we are using the layout editor panels to build the signal mast
     * logic, blocks, turnouts .
     *
     * @param destination Destination SignalMast.
     * @return true if we are using the layout editor to build the signal mast
     *         logic.
     */
    public boolean useLayoutEditor(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).useLayoutEditor();
    }

    /**
     * Sets whether we should use the information from the layout editor for
     * either blocks or turnouts.
     *
     * @param destination Destination SignalMast.
     * @param blocks      set false if not to use the block information gathered
     *                    from the layouteditor
     * @param turnouts    set false if not to use the turnout information
     *                    gathered from the layouteditor
     */
    public void useLayoutEditorDetails(boolean turnouts, boolean blocks, SignalMast destination) throws jmri.JmriException {
        if (!destList.containsKey(destination)) {
            return;
        }
        try {
            destList.get(destination).useLayoutEditorDetails(turnouts, blocks);
        } catch (jmri.JmriException e) {
            throw e;
        }
    }

    /**
     * Query if we are using the layout editor turnout information in the signal
     * mast logic.
     *
     * @param destination Destination SignalMast.
     * @return true if we are using the turnout information from the layout
     *         editor.
     */
    public boolean useLayoutEditorTurnouts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).useLayoutEditorTurnouts();
    }

    /**
     * Query if we are using the layout editor block information in the signal
     * mast logic.
     *
     * @param destination Destination SignalMast.
     * @return true if we are using the block information from the layout
     *         editor.
     */
    public boolean useLayoutEditorBlocks(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).useLayoutEditorBlocks();
    }

    public Section getAssociatedSection(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return null;
        }
        return destList.get(destination).getAssociatedSection();
    }

    public void setAssociatedSection(Section sec, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setAssociatedSection(sec);
    }

    /**
     * Query if we are allowing the system to automatically generated a list of
     * conflicting SignalMast that have a direct effect on our logic.
     *
     * @param destination Destination SignalMast.
     * @return true if this is allowed.
     */
    public boolean allowAutoMaticSignalMastGeneration(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).allowAutoSignalMastGen();
    }

    /**
     * Sets whether we should allow the system to automatically generate a list
     * of signal masts that could cause a conflicting route.
     *
     * @param destination Destination SignalMast.
     * @param allow       set true if we are to allow automatic generation.
     */
    public void allowAutoMaticSignalMastGeneration(boolean allow, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).allowAutoSignalMastGen(allow);
    }

    /**
     * Sets whether we should lock all turnouts between the source and
     * destination signal masts when the logic goes active, to prevent them from
     * being changed. This is dependant upon the hardware allowing for this.
     *
     * @param destination Destination SignalMast.
     * @param lock        set true if the system should lock the turnout.
     */
    public void allowTurnoutLock(boolean lock, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).allowTurnoutLock(lock);
    }
    
    /**
    * Query if we are allowing the system to lock turnouts when the logic goes 
    * active.
    *
    * @param destination Destination SignalMast.
    * @return true if locking is allowed.
    */

    /**
     * Query if we are allowing the system to lock turnouts when the logic goes
     * active.
     *
     * @param destination Destination SignalMast.
     * @return true if locking is allowed.
     */
    public boolean isTurnoutLockAllowed(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).isTurnoutLockAllowed();
    }

    /**
     * Sets the states that each turnout must be in for signal not to be set at
     * a stop aspect
     *
     * @param turnouts
     */
    public void setTurnouts(Hashtable<NamedBeanHandle<Turnout>, Integer> turnouts, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setTurnouts(turnouts);
    }

    /**
     * Sets which blocks must be inactive for the signal not to be set at a stop
     * aspect These Turnouts are not stored in the panel file.
     */
    public void setAutoTurnouts(Hashtable<Turnout, Integer> turnouts, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setAutoTurnouts(turnouts);
    }

    /**
     * Sets which blocks must be inactive for the signal not to be set at a stop
     * aspect
     *
     * @param blocks
     */
    public void setBlocks(Hashtable<Block, Integer> blocks, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setBlocks(blocks);
    }

    /**
     * Sets which blocks must be inactive for the signal not to be set at a stop
     * aspect These blocks are not stored in the panel file.
     *
     * @param blocks
     */
    //public void setLayoutBlocks
    public void setAutoBlocks(LinkedHashMap<Block, Integer> blocks, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setAutoBlocks(blocks);
    }

    /**
     * Sets which masts must be in a given state before our mast can be set.
     *
     * @param masts
     */
    public void setMasts(Hashtable<SignalMast, String> masts, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setMasts(masts);
    }

    /**
     * Sets which masts must be in a given state before our mast can be set.
     * These masts are not stored in the panel file.
     *
     * @param masts
     */
    public void setAutoMasts(Hashtable<SignalMast, String> masts, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setAutoMasts(masts, true);
    }

    /**
     * Sets which sensors must be in a given state before our mast can be set.
     *
     * @param sensors
     */
    public void setSensors(Hashtable<NamedBeanHandle<Sensor>, Integer> sensors, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).setSensors(sensors);
    }

    public void addSensor(String sensorName, int state, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        Sensor sen = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        if (sen != null) {
            NamedBeanHandle<Sensor> namedSensor = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sen);
            destList.get(destination).addSensor(namedSensor, state);
        }
    }

    public void removeSensor(String sensorName, SignalMast destination) {
        Sensor sen = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        removeSensor(sen, destination);
    }

    public void removeSensor(Sensor sen, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }
        if (sen != null) {
            destList.get(destination).removeSensor(sen);
        }

    }

    public ArrayList<Block> getBlocks(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<Block>();
        }
        return destList.get(destination).getBlocks();
    }

    public ArrayList<Block> getAutoBlocks(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<Block>();
        }
        return destList.get(destination).getAutoBlocks();
    }

    public ArrayList<Block> getAutoBlocksBetweenMasts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<Block>();
        }
        return destList.get(destination).getAutoBlocksBetweenMasts();
    }

    public ArrayList<Turnout> getTurnouts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<Turnout>();
        }
        return destList.get(destination).getTurnouts();
    }

    public ArrayList<NamedBeanHandle<Turnout>> getNamedTurnouts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<NamedBeanHandle<Turnout>>();
        }
        return destList.get(destination).getNamedTurnouts();
    }

    public void removeTurnout(Turnout turn, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return;
        }

        if (turn != null) {
            destList.get(destination).removeTurnout(turn);
        }
    }

    public ArrayList<Turnout> getAutoTurnouts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<Turnout>();
        }
        return destList.get(destination).getAutoTurnouts();
    }

    public ArrayList<Sensor> getSensors(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<Sensor>();
        }
        return destList.get(destination).getSensors();
    }

    public ArrayList<NamedBeanHandle<Sensor>> getNamedSensors(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<NamedBeanHandle<Sensor>>();
        }
        return destList.get(destination).getNamedSensors();
    }

    public ArrayList<SignalMast> getSignalMasts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<SignalMast>();
        }
        return destList.get(destination).getSignalMasts();
    }

    public ArrayList<SignalMast> getAutoMasts(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return new ArrayList<SignalMast>();
        }
        return destList.get(destination).getAutoSignalMasts();
    }

    /*general method to initialise all*/
    public void initialise() {
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            destList.get(en.nextElement()).initialise();
        }
    }

    /**
     * Initialise the signalmast after all the parameters have been set.
     */
    public void initialise(SignalMast destination) {
        if (disposing) {
            return;
        }

        if (!destList.containsKey(destination)) {
            return;
        }
        destList.get(destination).initialise();
    }

    public LinkedHashMap<Block, Integer> setupLayoutEditorTurnoutDetails(List<LayoutBlock> blks, SignalMast destination) {
        if (disposing) {
            return new LinkedHashMap<Block, Integer>();
        }

        if (!destList.containsKey(destination)) {
            return new LinkedHashMap<Block, Integer>();
        }
        return destList.get(destination).setupLayoutEditorTurnoutDetails(blks);
    }

    public void setupLayoutEditorDetails() {
        if (disposing) {
            return;
        }

        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            try {
                destList.get(en.nextElement()).setupLayoutEditorDetails();
            } catch (jmri.JmriException e) {
                //Considered normal if no route is valid on the layout editor
            }
        }
    }

    /**
     *
     * @return true if the path to the next signal is clear
     */
    boolean checkStates() {
        SignalMast oldActiveMast = destination;
        if (destination != null) {
            firePropertyChange("state", oldActiveMast, null);
            log.debug("Remove listener from destination");
            destination.removePropertyChangeListener(propertyDestinationMastListener);
            if (destList.containsKey(destination)) {
                destList.get(destination).clearTurnoutLock();
            }
        }

        Enumeration<SignalMast> en = destList.keys();
        log.debug("checkStates enumerates over {} masts", destList.size());
        while (en.hasMoreElements()) {
            SignalMast key = en.nextElement();
            log.debug("  Destination mast {}", key.getDisplayName());
            log.debug("    isEnabled: {}", (destList.get(key)).isEnabled());
            log.debug("    isActive: {}", destList.get(key).isActive());

            if ((destList.get(key)).isEnabled() && (destList.get(key).isActive())) {
                destination = key;
                log.debug("      Add listener to destination");
                destination.addPropertyChangeListener(propertyDestinationMastListener);
                log.debug("      firePropertyChange: \"state\"");
                firePropertyChange("state", oldActiveMast, destination);
                destList.get(key).lockTurnouts();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if any of the blocks in the supplied list are included in
     * any of the logics that set this signal.
     */
    public boolean areBlocksIncluded(ArrayList<Block> blks) {
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast dm = en.nextElement();
            boolean included = false;
            for (int i = 0; i < blks.size(); i++) {
                included = destList.get(dm).isBlockIncluded(blks.get(i));
                if (included) {
                    return true;
                }
                included = destList.get(dm).isAutoBlockIncluded(blks.get(i));
                if (included) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getBlockState(Block block, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return -1;
        }
        return destList.get(destination).getBlockState(block);
    }

    public boolean isBlockIncluded(Block block, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).isBlockIncluded(block);
    }

    public boolean isTurnoutIncluded(Turnout turnout, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).isTurnoutIncluded(turnout);
    }

    public boolean isSensorIncluded(Sensor sensor, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).isSensorIncluded(sensor);
    }

    public boolean isSignalMastIncluded(SignalMast signal, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return false;
        }
        return destList.get(destination).isSignalMastIncluded(signal);
    }

    public int getAutoBlockState(Block block, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return -1;
        }
        return destList.get(destination).getAutoBlockState(block);
    }

    public int getSensorState(Sensor sensor, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return -1;
        }
        return destList.get(destination).getSensorState(sensor);
    }

    public int getTurnoutState(Turnout turnout, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return -1;
        }
        return destList.get(destination).getTurnoutState(turnout);
    }

    public int getAutoTurnoutState(Turnout turnout, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return -1;
        }
        return destList.get(destination).getAutoTurnoutState(turnout);
    }

    public String getSignalMastState(SignalMast mast, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return null;
        }
        return destList.get(destination).getSignalMastState(mast);
    }

    public String getAutoSignalMastState(SignalMast mast, SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return null;
        }
        return destList.get(destination).getAutoSignalMastState(mast);
    }

    public float getMaximumSpeed(SignalMast destination) {
        if (!destList.containsKey(destination)) {
            return -1;
        }
        return destList.get(destination).getMinimumSpeed();
    }

    volatile boolean inWait = false;
    Thread thr = null;

    /* 
     * Before going active or checking that we can go active, we will wait 500ms
     * for things to settle down to help prevent a race condition
     */
    synchronized void setSignalAppearance() {
        log.debug("setSignalAppearance called for {}", source.getDisplayName());
        if (inWait) {
            log.debug("setSignalAppearance called with inWait set, returning");
            return;
        }
        inWait = true;

        Runnable r = new Runnable() {
            public void run() {
                try {
                    Thread.sleep((InstanceManager.signalMastLogicManagerInstance().getSignalLogicDelay() / 2));
                    inWait = false;
                    setMastAppearance();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    inWait = false;
                }
            }
        };

        thr = new Thread(r, getSourceMast().getDisplayName());
        //thr.setName(getSourceMast().getDisplayName());
        try {
            thr.start();
        } catch (java.lang.IllegalThreadStateException ex) {
            log.error("exception while starting setSignalAppearance thread: ", ex.toString());
        }
    }

    /**
     * Evaluates the destination signal mast appearance and sets ours
     * accordingly
     */
    void setMastAppearance() {
        synchronized (this) {
            if (inWait) {
                log.error("setMastAppearance called while still in wait, returning");
                return;
            }
        }
        log.debug("Set Signal Appearances");
        if (getSourceMast().getHeld()) {
            log.debug("Signal is at a held state so will set to the aspect defined for held or danger");

            String heldAspect = getSourceMast().getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD);
            if (heldAspect != null) {
                log.debug("  Setting to HELD value of {}", heldAspect);
                getSourceMast().setAspect(heldAspect);
            } else {
                String dangerAspect = getSourceMast().getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER);
                log.debug("  Setting to DANGER value of {}", dangerAspect);
                getSourceMast().setAspect(dangerAspect);
            }
            return;
        }
        if (!checkStates()) {
            log.debug("Advanced routes not clear, set stop aspect");
            getSourceMast().setAspect(stopAspect);
            return;
        }
        String[] advancedAspect;
        if (destination.getHeld()) {
            if (destination.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD) != null) {
                advancedAspect = getSourceMast().getAppearanceMap().getValidAspectsForAdvancedAspect(destination.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD));
            } else {
                advancedAspect = getSourceMast().getAppearanceMap().getValidAspectsForAdvancedAspect(destination.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER));
            }
        } else {
            advancedAspect = getSourceMast().getAppearanceMap().getValidAspectsForAdvancedAspect(destination.getAspect());
        }

        log.debug("distant aspect is {}", destination.getAspect());
        log.debug("advanced aspect is {}", advancedAspect != null ? advancedAspect : "<null>");

        if (advancedAspect != null) {
            String aspect = stopAspect;
            if (destList.get(destination).permissiveBlock) {
                //if a block is in a permissive state then we set the permissive appearance
                aspect = getSourceMast().getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.PERMISSIVE);
            } else {
                for (int i = 0; i < advancedAspect.length; i++) {
                    if (!getSourceMast().isAspectDisabled(advancedAspect[i])) {
                        aspect = advancedAspect[i];
                        break;
                    }
                }
                ArrayList<Integer> divergAspects = new ArrayList<Integer>();
                ArrayList<Integer> nonDivergAspects = new ArrayList<Integer>();
                ArrayList<Integer> eitherAspects = new ArrayList<Integer>();
                if (advancedAspect.length > 1) {
                    float maxSigSpeed = -1;
                    float maxPathSpeed = destList.get(destination).getMinimumSpeed();
                    boolean divergRoute = destList.get(destination).turnoutThrown;

                    log.debug("Diverging route? " + divergRoute);
                    boolean divergFlagsAvailable = false;
                    //We split the aspects into two lists, one with divering flag set, the other without.
                    for (int i = 0; i < advancedAspect.length; i++) {
                        String div = null;
                        if (!getSourceMast().isAspectDisabled(advancedAspect[i])) {
                            div = (String) getSourceMast().getSignalSystem().getProperty(advancedAspect[i], "route");
                        }
                        if (div != null) {
                            if (div.equals("Diverging")) {
                                log.debug("Aspect " + advancedAspect[i] + " added as Diverging Route");
                                divergAspects.add(i);
                                divergFlagsAvailable = true;
                                log.debug("Using Diverging Flag");
                            } else if (div.equals("Either")) {
                                log.debug("Aspect " + advancedAspect[i] + " added as Both Diverging and Normal Route");
                                nonDivergAspects.add(i);
                                divergAspects.add(i);
                                divergFlagsAvailable = true;
                                eitherAspects.add(i);
                                log.debug("Using Diverging Flag");
                            } else {
                                log.debug("Aspect " + advancedAspect[i] + " added as Normal Route");
                                nonDivergAspects.add(i);
                                log.debug("Aspect " + advancedAspect[i] + " added as Normal Route");
                            }
                        } else {
                            nonDivergAspects.add(i);
                            log.debug("Aspect " + advancedAspect[i] + " added as Normal Route");
                        }
                    }
                    if ((eitherAspects.equals(divergAspects)) && (divergAspects.size() < nonDivergAspects.size())) {
                        //There are no unique diverging aspects 
                        log.debug("'Either' aspects equals divergAspects and is less than non-diverging aspects");
                        divergFlagsAvailable = false;
                    }
                    log.debug("path max speed : " + maxPathSpeed);
                    for (int i = 0; i < advancedAspect.length; i++) {
                        if (!getSourceMast().isAspectDisabled(advancedAspect[i])) {
                            String strSpeed = (String) getSourceMast().getSignalSystem().getProperty(advancedAspect[i], "speed");
                            if (log.isDebugEnabled()) {
                                log.debug("Aspect Speed = " + strSpeed + " for aspect " + advancedAspect[i]);
                            }
                            /*  if the diverg flags available is set and the diverg aspect 
                             array contains the entry then we will check this aspect.
                                
                             If the diverg flag has not been set then we will check.
                             */
                            log.debug(advancedAspect[i]);
                            if ((divergRoute && (divergFlagsAvailable) && (divergAspects.contains(i))) || ((divergRoute && !divergFlagsAvailable) || (!divergRoute)) && (nonDivergAspects.contains(i))) {
                                log.debug("In list");
                                if ((strSpeed != null) && (!strSpeed.equals(""))) {
                                    float speed = 0.0f;
                                    try {
                                        speed = Float.valueOf(strSpeed);
                                    } catch (NumberFormatException nx) {
                                        // not a number, perhaps a name?
                                        try {
                                            speed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getSpeed(strSpeed);
                                        } catch (Exception ex) {
                                            // not a name either
                                            log.warn("Using speed = 0.0 because could not understand \"{}\"", strSpeed);
                                        }
                                    }
                                    //Integer state = Integer.parseInt(strSpeed);
                                    /* This pics out either the highest speed signal if there
                                     * is no block speed specified or the highest speed signal
                                     * that is under the minimum block speed.
                                     */
                                    if (log.isDebugEnabled()) {
                                        log.debug(destination.getDisplayName() + " signal state speed " + speed + " maxSigSpeed " + maxSigSpeed + " max Path Speed " + maxPathSpeed);
                                    }
                                    if (maxPathSpeed == 0) {
                                        if (maxSigSpeed == -1) {
                                            log.debug("min speed on this route is equal to 0 so will set this as our max speed");
                                            maxSigSpeed = speed;
                                            aspect = advancedAspect[i];
                                            log.debug("Aspect to set is " + aspect);
                                        } else if (speed > maxSigSpeed) {
                                            log.debug("new speed is faster than old will use this");
                                            maxSigSpeed = speed;
                                            aspect = advancedAspect[i];
                                            log.debug("Aspect to set is " + aspect);
                                        }
                                    } else if ((speed > maxSigSpeed) && (maxSigSpeed < maxPathSpeed) && (speed <= maxPathSpeed)) {
                                        //Only set the speed to the lowest if the max speed is greater than the path speed
                                        //and the new speed is less than the last max speed
                                        log.debug("our minimum speed on this route is less than our state speed, we will set this as our max speed");
                                        maxSigSpeed = speed;
                                        aspect = advancedAspect[i];
                                        log.debug("Aspect to set is " + aspect);
                                    } else if ((maxSigSpeed > maxPathSpeed) && (speed < maxSigSpeed)) {
                                        log.debug("our max signal speed is greater than our path speed on this route, our speed is less that the maxSigSpeed");
                                        maxSigSpeed = speed;
                                        aspect = advancedAspect[i];
                                        log.debug("Aspect to set is " + aspect);

                                    } else if (maxSigSpeed == -1) {
                                        log.debug("maxSigSpeed returned as -1");
                                        maxSigSpeed = speed;
                                        aspect = advancedAspect[i];
                                        log.debug("Aspect to set is " + aspect);
                                    }
                                }
                            }
                        } else if (log.isDebugEnabled()) {
                            log.debug("Aspect has been disabled " + advancedAspect[i]);
                        }
                    }
                }
            }
            if ((aspect != null) && (!aspect.equals(""))) {
                log.debug("setMastAppearance setting aspect \"{}\"", aspect);
                try {
                    getSourceMast().setAspect(aspect);
                } catch (Exception ex) {
                    log.error("Exception while setting Signal Logic ", ex.toString());
                }
                return;
            }
        }
        log.debug("Aspect returned is not valid, setting stop");
        getSourceMast().setAspect(stopAspect);
    }

    public void setConflictingLogic(SignalMast sm, LevelXing lx) {
        if (sm == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("setConflicting logic mast " + sm.getDisplayName());
        }
        if (sm == source) {
            log.debug("source is us so exit");
            return;
        }
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast dm = en.nextElement();
            if (destList.get(dm).isBlockIncluded(lx.getLayoutBlockAC())) {
                destList.get(dm).addAutoSignalMast(sm);
            } else if (destList.get(dm).isBlockIncluded(lx.getLayoutBlockBD())) {
                destList.get(dm).addAutoSignalMast(sm);
            } else if (destList.get(dm).isAutoBlockIncluded(lx.getLayoutBlockAC())) {
                destList.get(dm).addAutoSignalMast(sm);
            } else if (destList.get(dm).isAutoBlockIncluded(lx.getLayoutBlockBD())) {
                destList.get(dm).addAutoSignalMast(sm);
            } else {
                log.debug("Block not found");
            }
        }
    }

    public void removeConflictingLogic(SignalMast sm, LevelXing lx) {
        if (sm == source) {
            return;
        }

        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast dm = en.nextElement();
            if (destList.get(dm).isBlockIncluded(lx.getLayoutBlockAC())) {
                destList.get(dm).removeAutoSignalMast(sm);
            } else if (destList.get(dm).isBlockIncluded(lx.getLayoutBlockBD())) {
                destList.get(dm).removeAutoSignalMast(sm);
            }
        }
    }

    private class DestinationMast {

        LayoutBlock destinationBlock = null;
        LayoutBlock protectingBlock = null; //this is the block that the source signal is protecting

        ArrayList<NamedBeanSetting> userSetTurnouts = new ArrayList<NamedBeanSetting>(0);
        Hashtable<Turnout, Integer> autoTurnouts = new Hashtable<Turnout, Integer>(0);
        //Hashtable<Turnout, Boolean> turnoutThroats = new Hashtable<Turnout, Boolean>(0);
        //Hashtable<Turnout, Boolean> autoTurnoutThroats = new Hashtable<Turnout, Boolean>(0);

        ArrayList<NamedBeanSetting> userSetMasts = new ArrayList<NamedBeanSetting>(0);
        Hashtable<SignalMast, String> autoMasts = new Hashtable<SignalMast, String>(0);
        ArrayList<NamedBeanSetting> userSetSensors = new ArrayList<NamedBeanSetting>(0);
        ArrayList<NamedBeanSetting> userSetBlocks = new ArrayList<NamedBeanSetting>(0);
        boolean turnoutThrown = false;
        boolean permissiveBlock = false;
        boolean disposed = false;

        ArrayList<LevelXing> blockInXings = new ArrayList<LevelXing>();

        //autoBlocks are for those automatically generated by the system.
        LinkedHashMap<Block, Integer> autoBlocks = new LinkedHashMap<Block, Integer>(0);

        ArrayList<Block> xingAutoBlocks = new ArrayList<Block>(0);
        ArrayList<Block> dblCrossOverAutoBlocks = new ArrayList<Block>(0);
        SignalMast destination;
        boolean active = false;
        boolean destMastInit = false;

        float minimumBlockSpeed = 0.0f;

        boolean useLayoutEditor = false;
        boolean useLayoutEditorTurnouts = false;
        boolean useLayoutEditorBlocks = false;
        boolean lockTurnouts = false;

        NamedBeanHandle<Section> associatedSection = null;

        DestinationMast(SignalMast destination) {
            this.destination = destination;
            if (destination.getAspect() == null) {
                try {
                    destination.setAspect(destination.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER));
                } catch (Exception ex) {
                    log.error("Error while creating Signal Logic " + ex.toString());
                }
            }
        }

        void updateDestinationMast(SignalMast newMast) {
            destination = newMast;
            if (destination.getAspect() == null) {
                try {
                    destination.setAspect(destination.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER));
                } catch (Exception ex) {
                    log.error("Error while creating Signal Logic " + ex.toString());
                }
            }
        }

        LayoutBlock getProtectingBlock() {
            return protectingBlock;
        }

        String comment;

        String getComment() {
            return comment;
        }

        void setComment(String comment) {
            String old = this.comment;
            this.comment = comment;
            firePropertyChange("Comment", old, comment);
        }

        boolean isActive() {
            if (disposed) {
                log.error("checkState called even though this has been disposed of");
                return false;
            }
            return active;
        }

        float getMinimumSpeed() {
            return minimumBlockSpeed;
        }

        boolean enable = true;

        void setEnabled() {
            enable = true;
            firePropertyChange("Enabled", false, this.destination);
        }

        void setDisabled() {
            enable = false;
            firePropertyChange("Enabled", true, this.destination);
        }

        boolean isEnabled() {
            return enable;
        }

        int store = STOREALL;

        /**
         * Use this to determine if the signalmast logic is stored in the panel
         * file and if all the information is stored.
         *
         * @param store
         */
        void setStore(int store) {
            this.store = store;
        }

        int getStoreState() {
            return store;
        }

        void setAssociatedSection(Section section) {
            if (section != null && (!useLayoutEditor || !useLayoutEditorBlocks)) {
                log.warn("This Logic " + source.getDisplayName() + " to " + destination.getDisplayName() + " is not using the layout editor or its blocks, the associated section will not be populated correctly");
            }
            if (section == null) {
                associatedSection = null;
                return;
            }
            associatedSection = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(section.getDisplayName(), section);
            if (!autoBlocks.isEmpty() && associatedSection != null) {
                createSectionDetails();
            }
        }

        Section getAssociatedSection() {
            if (associatedSection != null) {
                return associatedSection.getBean();
            }
            return null;
        }

        void createSectionDetails() {
            getAssociatedSection().removeAllBlocksFromSection();
            for (Block key : getAutoBlocksBetweenMasts()) {
                getAssociatedSection().addBlock(key);
            }
            String dir = jmri.Path.decodeDirection(getFacingBlock().getNeighbourDirection(getProtectingBlock()));
            jmri.EntryPoint ep = new jmri.EntryPoint(getProtectingBlock().getBlock(), getFacingBlock().getBlock(), dir);
            ep.setTypeForward();
            getAssociatedSection().addToForwardList(ep);

            LayoutBlock proDestLBlock = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getProtectedBlockByNamedBean(destination, destinationBlock.getMaxConnectedPanel());
            if (proDestLBlock != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Add protecting Block " + proDestLBlock.getDisplayName());
                }
                dir = jmri.Path.decodeDirection(proDestLBlock.getNeighbourDirection(destinationBlock));
                ep = new jmri.EntryPoint(destinationBlock.getBlock(), proDestLBlock.getBlock(), dir);
                ep.setTypeReverse();
                getAssociatedSection().addToReverseList(ep);
            } else if (log.isDebugEnabled()) {
                log.debug(" ### Protecting Block not found ### ");
            }
        }

        boolean isTurnoutLockAllowed() {
            return lockTurnouts;
        }

        void allowTurnoutLock(boolean lock) {
            if (lockTurnouts == lock) {
                return;
            }
            if (!lock) {
                clearTurnoutLock();
            }
            lockTurnouts = lock;
        }

        void setTurnouts(Hashtable<NamedBeanHandle<Turnout>, Integer> turnouts) {
            if (this.userSetTurnouts != null) {
                for (NamedBeanSetting nbh : userSetTurnouts) {
                    nbh.getBean().removePropertyChangeListener(propertyTurnoutListener);
                }
            }
            destMastInit = false;
            if (turnouts == null) {
                userSetTurnouts = new ArrayList<NamedBeanSetting>(0);
            } else {
                userSetTurnouts = new ArrayList<NamedBeanSetting>();
                Enumeration<NamedBeanHandle<Turnout>> e = turnouts.keys();
                while (e.hasMoreElements()) {
                    NamedBeanHandle<Turnout> nbh = e.nextElement();
                    NamedBeanSetting nbs = new NamedBeanSetting(nbh, turnouts.get(nbh));
                    userSetTurnouts.add(nbs);
                }
            }
            firePropertyChange("turnouts", null, this.destination);
        }

        /*void setTurnoutThroats(Hashtable<Turnout, Boolean> turnouts){
         if(this.turnoutThroats!=null){
         Enumeration<Turnout> keys = this.turnouts.keys();
         while ( keys.hasMoreElements() )
         {
         Turnout key = keys.nextElement();
         key.removePropertyChangeListener(propertyTurnoutListener);
         }
         }
         destMastInit = false;
         if(turnouts==null){
         this.turnoutThroats = new Hashtable<Turnout, Boolean>(0);
         } else {
         this.turnoutThroats=turnouts;
         }
         firePropertyChange("turnouts", null, this.destination);
         }*/
        /*void setAutoTurnoutThroats(Hashtable<Turnout, Boolean> turnouts){
         if(this.turnoutThroats!=null){
         Enumeration<Turnout> keys = this.turnouts.keys();
         while ( keys.hasMoreElements() )
         {
         Turnout key = keys.nextElement();
         key.removePropertyChangeListener(propertyTurnoutListener);
         }
         }
         destMastInit = false;
         if(turnouts==null){
         this.autoTurnoutThroats = new Hashtable<Turnout, Boolean>(0);
         } else {
         this.autoTurnoutThroats=turnouts;
         }
         firePropertyChange("turnouts", null, this.destination);
         }*/
        /**
         * Sets which blocks must be inactive for the signal not to be set at a
         * stop aspect
         */
        void setAutoTurnouts(Hashtable<Turnout, Integer> turnouts) {
            log.debug("{} called setAutoTurnouts with {}", destination.getDisplayName(), (turnouts != null ? "" + turnouts.size() + " turnouts in hash table" : "null hash table reference"));
            if (this.autoTurnouts != null) {
                Enumeration<Turnout> keys = this.autoTurnouts.keys();
                while (keys.hasMoreElements()) {
                    Turnout key = keys.nextElement();
                    key.removePropertyChangeListener(propertyTurnoutListener);
                }
                //minimumBlockSpeed = 0;
            }
            destMastInit = false;
            if (turnouts == null) {
                this.autoTurnouts = new Hashtable<Turnout, Integer>(0);
            } else {
                this.autoTurnouts = turnouts;
            }
            firePropertyChange("autoturnouts", null, this.destination);
        }

        /**
         * Sets which blocks must be inactive for the signal not to be set at a
         * stop aspect
         *
         * @param blocks
         */
        void setBlocks(Hashtable<Block, Integer> blocks) {
            log.debug(destination.getDisplayName() + " Set blocks called");
            if (this.userSetBlocks != null) {
                for (NamedBeanSetting nbh : userSetBlocks) {
                    nbh.getBean().removePropertyChangeListener(propertyBlockListener);
                }
            }
            destMastInit = false;

            userSetBlocks = new ArrayList<NamedBeanSetting>(0);
            if (blocks != null) {
                userSetBlocks = new ArrayList<NamedBeanSetting>();
                Enumeration<Block> e = blocks.keys();
                while (e.hasMoreElements()) {
                    Block blk = e.nextElement();
                    NamedBeanHandle<?> nbh = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(blk.getDisplayName(), blk);
                    NamedBeanSetting nbs = new NamedBeanSetting(nbh, blocks.get(blk));
                    userSetBlocks.add(nbs);
                }
            }
            firePropertyChange("blocks", null, this.destination);
        }

        /**
         * Sets which blocks must be inactive for the signal not to be set at a
         * stop aspect
         *
         * @param blocks
         */
        //public void setLayoutBlocks
        public void setAutoBlocks(LinkedHashMap<Block, Integer> blocks) {
            if (log.isDebugEnabled()) {
                log.debug("{} called setAutoBlocks with ", destination.getDisplayName(), (blocks != null ? "" + blocks.size() + " blocks in hash table" : "null hash table reference"));
            }
            if (this.autoBlocks != null) {
                for (Block key : autoBlocks.keySet()) {
                    key.removePropertyChangeListener(propertyBlockListener);
                }
            }
            destMastInit = false;
            if (blocks == null) {
                this.autoBlocks = new LinkedHashMap<Block, Integer>(0);

            } else {
                this.autoBlocks = blocks;
                //We shall remove the facing block in the list.
                if (facingBlock != null) {
                    if (autoBlocks.containsKey(facingBlock.getBlock())) {
                        autoBlocks.remove(facingBlock.getBlock());
                    }
                }
                if (getAssociatedSection() != null) {
                    createSectionDetails();
                }
            }
            firePropertyChange("autoblocks", null, this.destination);
        }

        /**
         * Sets which masts must be in a given state before our mast can be set.
         *
         * @param masts
         */
        void setMasts(Hashtable<SignalMast, String> masts) {
            if (this.userSetMasts != null) {
                for (NamedBeanSetting nbh : userSetMasts) {
                    nbh.getBean().removePropertyChangeListener(propertySignalMastListener);
                }
            }

            destMastInit = false;

            if (masts == null) {
                userSetMasts = new ArrayList<NamedBeanSetting>(0);
            } else {
                userSetMasts = new ArrayList<NamedBeanSetting>();
                Enumeration<SignalMast> e = masts.keys();
                while (e.hasMoreElements()) {
                    SignalMast mast = e.nextElement();
                    NamedBeanHandle<?> nbh = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(mast.getDisplayName(), mast);
                    NamedBeanSetting nbs = new NamedBeanSetting(nbh, masts.get(mast));
                    userSetMasts.add(nbs);
                }
            }
            firePropertyChange("masts", null, this.destination);
        }

        /**
         * Sets which signalMasts must be at Danager for the signal not to be
         * set at a stop aspect
         */
        void setAutoMasts(Hashtable<SignalMast, String> newAutoMasts, boolean overright) {
            if (log.isDebugEnabled()) {
                log.debug(destination.getDisplayName() + " setAutoMast Called");
            }
            if (this.autoMasts != null) {
                Enumeration<SignalMast> keys = this.autoMasts.keys();
                while (keys.hasMoreElements()) {
                    SignalMast key = keys.nextElement();
                    key.removePropertyChangeListener(propertySignalMastListener);
                }
                //minimumBlockSpeed = 0;
            }
            destMastInit = false;
            if (overright) {
                if (newAutoMasts == null) {
                    this.autoMasts = new Hashtable<SignalMast, String>(0);
                } else {
                    this.autoMasts = newAutoMasts;
                }
            } else {
                if (newAutoMasts == null) {
                    this.autoMasts = new Hashtable<SignalMast, String>(0);
                } else {
                    Enumeration<SignalMast> keys = newAutoMasts.keys();
                    while (keys.hasMoreElements()) {
                        SignalMast key = keys.nextElement();
                        this.autoMasts.put(key, newAutoMasts.get(key));
                    }
                }
            }
            //kick off the process to add back in signalmasts at crossings.
            for (int i = 0; i < blockInXings.size(); i++) {
                blockInXings.get(i).addSignalMastLogic(source);
            }

            firePropertyChange("automasts", null, this.destination);
        }

        /**
         * Sets which sensors must be in a given state before our mast can be
         * set.
         *
         * @param sensors
         */
        void setSensors(Hashtable<NamedBeanHandle<Sensor>, Integer> sensors) {
            if (this.userSetSensors != null) {
                for (NamedBeanSetting nbh : userSetSensors) {
                    nbh.getBean().removePropertyChangeListener(propertySensorListener);
                }
            }
            destMastInit = false;

            if (sensors == null) {
                userSetSensors = new ArrayList<NamedBeanSetting>(0);
            } else {
                userSetSensors = new ArrayList<NamedBeanSetting>();
                Enumeration<NamedBeanHandle<Sensor>> e = sensors.keys();
                while (e.hasMoreElements()) {
                    NamedBeanHandle<?> nbh = e.nextElement();
                    NamedBeanSetting nbs = new NamedBeanSetting(nbh, sensors.get(nbh));
                    userSetSensors.add(nbs);
                }
            }
            firePropertyChange("sensors", null, this.destination);
        }

        void addSensor(NamedBeanHandle<Sensor> sen, int state) {
            for (NamedBeanSetting nbh : userSetSensors) {
                if (nbh.getBean().equals(sen.getBean())) {
                    return;
                }
            }
            sen.getBean().addPropertyChangeListener(propertySensorListener);
            NamedBeanSetting nbs = new NamedBeanSetting(sen, state);
            userSetSensors.add(nbs);
            firePropertyChange("sensors", null, this.destination);
        }

        void removeSensor(NamedBeanHandle<Sensor> sen) {
            for (NamedBeanSetting nbh : userSetSensors) {
                if (nbh.getBean().equals(sen.getBean())) {
                    sen.getBean().removePropertyChangeListener(propertySensorListener);
                    userSetSensors.remove(nbh);
                    firePropertyChange("sensors", null, this.destination);
                    return;
                }
            }
        }

        void removeSensor(Sensor sen) {
            for (NamedBeanSetting nbh : userSetSensors) {
                if (nbh.getBean().equals(sen)) {
                    sen.removePropertyChangeListener(propertySensorListener);
                    userSetSensors.remove(nbh);
                    firePropertyChange("sensors", null, this.destination);
                    return;
                }
            }
        }

        ArrayList<Block> getBlocks() {
            ArrayList<Block> out = new ArrayList<Block>();
            for (NamedBeanSetting nbh : userSetBlocks) {
                out.add((Block) nbh.getBean());
            }
            return out;
        }

        ArrayList<Block> getAutoBlocks() {
            ArrayList<Block> out = new ArrayList<Block>();
            Set<Block> blockKeys = autoBlocks.keySet();
            //while ( blockKeys.hasMoreElements() )
            for (Block key : blockKeys) {
                //Block key = blockKeys.nextElement();
                out.add(key);
            }
            return out;
        }

        ArrayList<Block> getAutoBlocksBetweenMasts() {
            if (destList.get(destination).xingAutoBlocks.size() == 0 && destList.get(destination).dblCrossOverAutoBlocks.size() == 0) {
                return getAutoBlocks();
            }
            ArrayList<Block> returnList = getAutoBlocks();
            for (Block blk : getAutoBlocks()) {
                if (xingAutoBlocks.contains(blk)) {
                    returnList.remove(blk);
                }
            }
            for (Block blk : getAutoBlocks()) {
                if (dblCrossOverAutoBlocks.contains(blk)) {
                    returnList.remove(blk);
                }
            }

            return returnList;
        }

        ArrayList<Turnout> getTurnouts() {
            ArrayList<Turnout> out = new ArrayList<Turnout>();
            for (NamedBeanSetting nbh : userSetTurnouts) {
                out.add((Turnout) nbh.getBean());
            }
            return out;
        }

        void removeTurnout(Turnout turn) {
            Iterator<NamedBeanSetting> nbh = userSetTurnouts.iterator();
            while (nbh.hasNext()) {
                NamedBeanSetting i = nbh.next();
                if (i.getBean().equals(turn)) {
                    turn.removePropertyChangeListener(propertyTurnoutListener);
                    nbh.remove();
                    firePropertyChange("turnouts", null, this.destination);
                }
            }
        }

        @SuppressWarnings("unchecked") // (NamedBeanHandle<Turnout>) nbh.getNamedBean() is unchecked cast
        ArrayList<NamedBeanHandle<Turnout>> getNamedTurnouts() {
            ArrayList<NamedBeanHandle<Turnout>> out = new ArrayList<NamedBeanHandle<Turnout>>();
            for (NamedBeanSetting nbh : userSetTurnouts) {
                out.add((NamedBeanHandle<Turnout>) nbh.getNamedBean());
            }
            return out;
        }

        ArrayList<Turnout> getAutoTurnouts() {
            ArrayList<Turnout> out = new ArrayList<Turnout>();
            Enumeration<Turnout> en = autoTurnouts.keys();
            while (en.hasMoreElements()) {
                out.add(en.nextElement());
            }
            return out;
        }

        ArrayList<SignalMast> getSignalMasts() {
            ArrayList<SignalMast> out = new ArrayList<SignalMast>();
            for (NamedBeanSetting nbh : userSetMasts) {
                out.add((SignalMast) nbh.getBean());
            }
            return out;
        }

        ArrayList<SignalMast> getAutoSignalMasts() {
            ArrayList<SignalMast> out = new ArrayList<SignalMast>();
            Enumeration<SignalMast> en = autoMasts.keys();
            while (en.hasMoreElements()) {
                out.add(en.nextElement());
            }
            return out;
        }

        ArrayList<Sensor> getSensors() {
            ArrayList<Sensor> out = new ArrayList<Sensor>();
            for (NamedBeanSetting nbh : userSetSensors) {
                out.add((Sensor) nbh.getBean());
            }
            return out;
        }

        @SuppressWarnings("unchecked") // (NamedBeanHandle<Sensor>) nbh.getNamedBean() is unchecked cast
        ArrayList<NamedBeanHandle<Sensor>> getNamedSensors() {
            ArrayList<NamedBeanHandle<Sensor>> out = new ArrayList<NamedBeanHandle<Sensor>>();
            for (NamedBeanSetting nbh : userSetSensors) {
                out.add((NamedBeanHandle<Sensor>) nbh.getNamedBean());
            }
            return out;
        }

        boolean isBlockIncluded(Block block) {
            for (NamedBeanSetting nbh : userSetBlocks) {
                if (nbh.getBean().equals(block)) {
                    return true;
                }
            }
            return false;
        }

        boolean isAutoBlockIncluded(LayoutBlock block) {
            if (block != null) {
                return autoBlocks.containsKey(block.getBlock());
            }
            return false;
        }

        boolean isAutoBlockIncluded(Block block) {
            return autoBlocks.containsKey(block);
        }

        boolean isBlockIncluded(LayoutBlock block) {
            for (NamedBeanSetting nbh : userSetBlocks) {
                if (nbh.getBean().equals(block.getBlock())) {
                    return true;
                }
            }
            return false;
        }

        boolean isTurnoutIncluded(Turnout turnout) {
            for (NamedBeanSetting nbh : userSetTurnouts) {
                if (nbh.getBean().equals(turnout)) {
                    return true;
                }
            }
            return false;
        }

        boolean isSensorIncluded(Sensor sensor) {
            for (NamedBeanSetting nbh : userSetSensors) {
                if (nbh.getBean().equals(sensor)) {
                    return true;
                }
            }
            return false;
        }

        boolean isSignalMastIncluded(SignalMast signal) {
            for (NamedBeanSetting nbh : userSetMasts) {
                if (nbh.getBean().equals(signal)) {
                    return true;
                }
            }
            return false;
        }

        int getAutoBlockState(Block block) {
            if (autoBlocks == null) {
                return -1;
            }
            return autoBlocks.get(block);
        }

        int getBlockState(Block block) {
            if (userSetBlocks == null) {
                return -1;
            }
            for (NamedBeanSetting nbh : userSetBlocks) {
                if (nbh.getBean().equals(block)) {
                    return nbh.getSetting();
                }
            }
            return -1;
        }
        /*boolean isBlockManualAssigned(Block block){
         return true;
         }*/

        int getSensorState(Sensor sensor) {
            if (userSetSensors == null) {
                return -1;
            }
            for (NamedBeanSetting nbh : userSetSensors) {
                if (nbh.getBean().equals(sensor)) {
                    return nbh.getSetting();
                }
            }
            return -1;
        }

        int getTurnoutState(Turnout turnout) {
            if (userSetTurnouts == null) {
                return -1;
            }
            for (NamedBeanSetting nbh : userSetTurnouts) {
                if (nbh.getBean().equals(turnout)) {
                    return nbh.getSetting();
                }
            }
            return -1;
        }

        int getAutoTurnoutState(Turnout turnout) {
            if (autoTurnouts == null) {
                return -1;
            }
            if (autoTurnouts.containsKey(turnout)) {
                return autoTurnouts.get(turnout);
            }
            return -1;
        }

        String getSignalMastState(SignalMast mast) {
            if (userSetMasts == null) {
                return null;
            }
            for (NamedBeanSetting nbh : userSetMasts) {
                if (nbh.getBean().equals(mast)) {
                    return nbh.getStringSetting();
                }
            }
            return null;
        }

        String getAutoSignalMastState(SignalMast mast) {
            if (autoMasts == null) {
                return null;
            }
            return autoMasts.get(mast);
        }

        boolean inWait = false;

        /* 
         * Before going active or checking that we can go active, we will wait 500ms
         * for things to settle down to help prevent a race condition
         */
        void checkState() {
            if (disposed) {
                log.error("checkState called even though this has been disposed of " + getSourceMast().getDisplayName());
                return;
            }

            if (!enable) {
                return;
            }
            if (inWait) {
                return;
            }

            log.debug("check Signal Dest State called");
            inWait = true;

            Runnable r = new Runnable() {
                public void run() {
                    try {
//                    log.debug("Wait started");
                        Thread.sleep(InstanceManager.signalMastLogicManagerInstance().getSignalLogicDelay());
//                    log.debug("wait is over");
                        inWait = false;
                        checkStateDetails();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        inWait = false;
                    }
                }
            };
            thr = new Thread(r, getSourceMast().getDisplayName() + " " + destination.getDisplayName());
            /*try{
             thr.join();
             } catch (InterruptedException ex) {
             //            log.debug("interrupted at join " + ex);
             inWait=false;
             }*/
            //thr.setName(getSourceMast().getDisplayName() + " " + destination.getDisplayName());
            thr.start();
        }

        Thread thr = null;

        void checkStateDetails() {
            turnoutThrown = false;
            permissiveBlock = false;
            if (disposed) {
                log.error("checkStateDetails called even though this has been disposed of " + getSourceMast().getDisplayName() + " " + destination.getDisplayName());
                return;
            }
            if (inWait) {
                log.error("checkStateDetails called while we are waiting for things to settle");
                return;
            }
            if (!enable) {
                return;
            }
            log.debug("From " + getSourceMast().getDisplayName() + " to " + destination.getDisplayName() + " internal check state");
            active = false;
            if ((useLayoutEditor) && (autoTurnouts.size() == 0) && (autoBlocks.size() == 0)) {
                return;
            }
            boolean state = true;
            Enumeration<Turnout> keys = autoTurnouts.keys();
            while (keys.hasMoreElements()) {
                Turnout key = keys.nextElement();
                if (key.getKnownState() != autoTurnouts.get(key)) {
                    if (key.getState() != autoTurnouts.get(key)) {
                        if (isTurnoutIncluded(key)) {
                            if (key.getState() != getTurnoutState(key)) {
                                state = false;
                            } else if (key.getState() == Turnout.THROWN) {
                                turnoutThrown = true;
                            }
                        } else {
                            state = false;
                        }
                    }
                } else if (key.getState() == Turnout.THROWN) {
                    turnoutThrown = true;
                }
            }

            for (NamedBeanSetting nbh : userSetTurnouts) {
                Turnout key = (Turnout) nbh.getBean();
                if (key.getKnownState() != nbh.getSetting()) {
                    state = false;
                } else if (key.getState() == Turnout.THROWN) {
                    turnoutThrown = true;
                }
            }

            Enumeration<SignalMast> mastKeys = autoMasts.keys();
            while (mastKeys.hasMoreElements()) {
                SignalMast key = mastKeys.nextElement();
                if (log.isDebugEnabled()) {
                    log.debug(key.getDisplayName() + " " + key.getAspect() + " " + autoMasts.get(key));
                }
                if ((key.getAspect() != null) && (!key.getAspect().equals(autoMasts.get(key)))) {
                    if (isSignalMastIncluded(key)) {
                        //Basically if we have a blank aspect, we don't care about the state of the signalmast
                        if (!getSignalMastState(key).equals("")) {
                            if (!key.getAspect().equals(getSignalMastState(key))) {
                                state = false;
                            }
                        }
                    } else {
                        state = false;
                    }
                }
            }
            for (NamedBeanSetting nbh : userSetMasts) {
                SignalMast key = (SignalMast) nbh.getBean();
                if ((key.getAspect() == null) || (!key.getAspect().equals(nbh.getStringSetting()))) {
                    state = false;
                }
            }

            for (NamedBeanSetting nbh : userSetSensors) {
                Sensor key = (Sensor) nbh.getBean();
                if (key.getKnownState() != nbh.getSetting()) {
                    state = false;
                }
            }

            Set<Block> blockAutoKeys = autoBlocks.keySet();
            for (Block key : blockAutoKeys) {
                if (log.isDebugEnabled()) {
                    log.debug(key.getDisplayName() + " " + key.getState() + " " + autoBlocks.get(key));
                }
                if (key.getState() != autoBlocks.get(key)) {
                    if (isBlockIncluded(key)) {
                        if (getBlockState(key) != 0x03) {
                            if (key.getState() != getBlockState(key)) {
                                if (key.getState() == Block.OCCUPIED && key.getPermissiveWorking()) {
                                    permissiveBlock = true;
                                } else {
                                    state = false;
                                }
                            }
                        }
                    } else {
                        if (key.getState() == Block.OCCUPIED && key.getPermissiveWorking()) {
                            permissiveBlock = true;
                        } else if (key.getState() == Block.UNDETECTED) {
                            if (log.isDebugEnabled()) {
                                log.debug("Block " + key.getDisplayName() + " is UNDETECTED so treat as unoccupied");
                            }
                        } else {
                            state = false;
                        }
                    }
                }
            }

            for (NamedBeanSetting nbh : userSetBlocks) {
                Block key = (Block) nbh.getBean();
                if (nbh.getSetting() != 0x03) {
                    if (key.getState() != nbh.getSetting()) {
                        if (key.getState() == Block.OCCUPIED && key.getPermissiveWorking()) {
                            permissiveBlock = true;
                        } else {
                            state = false;
                        }
                    }
                }
            }
            if (permissiveBlock) {
                /*If a block has been found to be permissive, but the source signalmast
                 does not support a call-on/permissive aspect then the route can not be set*/
                if (getSourceMast().getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.PERMISSIVE) == null) {
                    state = false;
                }
            }

            /*This check is purely for use with the dispatcher, it will check to see if any of the blocks are set to "useExtraColor" 
             which is a means to determine if the block is in a section that is occupied and it not ours thus we can set the signal to danger.*/
            if (state && getAssociatedSection() != null
                    && jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame.class) != null
                    && jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class) != null
                    && getAssociatedSection().getState() != Section.FORWARD) {

                LayoutBlockManager lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
                blockAutoKeys = autoBlocks.keySet();
                for (Block key : blockAutoKeys) {
                    LayoutBlock lb = lbm.getLayoutBlock(key);
                    if (lb != null && lb.getUseExtraColor()) {
                        state = false;
                        break;
                    }
                }
                if (!state) {
                    for (NamedBeanSetting nbh : userSetBlocks) {
                        Block key = (Block) nbh.getBean();
                        LayoutBlock lb = lbm.getLayoutBlock(key);
                        if (lb != null && lb.getUseExtraColor()) {
                            state = false;
                            break;
                        }
                    }
                }
            }

            if (!state) {
                turnoutThrown = false;
                permissiveBlock = false;
            }

            active = state;
            setSignalAppearance();
        }

        void initialise() {
            if ((destMastInit) || (disposed)) {
                return;
            }

            active = false;
            turnoutThrown = false;
            permissiveBlock = false;
            boolean routeclear = true;
            if ((useLayoutEditor) && (autoTurnouts.size() == 0) && (autoBlocks.size() == 0) && (autoMasts.size() == 0)) {
                return;
            }

            calculateSpeed();

            Enumeration<Turnout> keys = autoTurnouts.keys();
            while (keys.hasMoreElements()) {
                Turnout key = keys.nextElement();
                key.addPropertyChangeListener(propertyTurnoutListener);

                if (key.getKnownState() != autoTurnouts.get(key)) {
                    if (key.getState() != autoTurnouts.get(key)) {
                        if (isTurnoutIncluded(key)) {
                            if (key.getState() != getTurnoutState(key)) {
                                routeclear = false;
                            } else if (key.getState() == Turnout.THROWN) {
                                turnoutThrown = true;
                            }
                        } else {
                            routeclear = false;
                        }
                    }
                } else if (key.getState() == Turnout.THROWN) {
                    turnoutThrown = true;
                }
            }

            for (NamedBeanSetting nbh : userSetTurnouts) {
                Turnout key = (Turnout) nbh.getBean();
                key.addPropertyChangeListener(propertyTurnoutListener, nbh.getBeanName(), "Signal Mast Logic:" + source.getDisplayName() + " to " + destination.getDisplayName());
                if (key.getKnownState() != nbh.getSetting()) {
                    routeclear = false;
                } else if (key.getState() == Turnout.THROWN) {
                    turnoutThrown = true;
                }
            }

            Enumeration<SignalMast> mastKeys = autoMasts.keys();
            while (mastKeys.hasMoreElements()) {
                SignalMast key = mastKeys.nextElement();
                //log.debug(destination.getDisplayName() + " auto mast add list " + key.getDisplayName());
                key.addPropertyChangeListener(propertySignalMastListener);
                if (!key.getAspect().equals(autoMasts.get(key))) {
                    if (isSignalMastIncluded(key)) {
                        if (key.getAspect().equals(getSignalMastState(key))) {
                            routeclear = false;
                        }
                    } else {
                        routeclear = false;
                    }
                }
            }

            for (NamedBeanSetting nbh : userSetMasts) {
                SignalMast key = (SignalMast) nbh.getBean();
                key.addPropertyChangeListener(propertySignalMastListener);
                //log.debug(destination.getDisplayName() + " key asepct " + key.getAspect());
                //log.debug(destination.getDisplayName() + " key exepcted aspect " + masts.get(key));
                if ((key.getAspect() == null) || (!key.getAspect().equals(nbh.getStringSetting()))) {
                    routeclear = false;
                }
            }
            for (NamedBeanSetting nbh : userSetSensors) {
                Sensor sensor = (Sensor) nbh.getBean();
                sensor.addPropertyChangeListener(propertySensorListener, nbh.getBeanName(), "Signal Mast Logic:" + source.getDisplayName() + " to " + destination.getDisplayName());
                if (sensor.getKnownState() != nbh.getSetting()) {
                    routeclear = false;
                }
            }

            Set<Block> autoBlockKeys = autoBlocks.keySet();
            for (Block key : autoBlockKeys) {
                //log.debug(destination.getDisplayName() + " auto block add list " + key.getDisplayName());
                key.addPropertyChangeListener(propertyBlockListener);
                if (key.getState() != autoBlocks.get(key)) {
                    if (isBlockIncluded(key)) {
                        if (key.getState() != getBlockState(key)) {
                            if (key.getState() == Block.OCCUPIED && key.getPermissiveWorking()) {
                                permissiveBlock = true;
                            } else {
                                routeclear = false;
                            }
                        }
                    } else {
                        if (key.getState() == Block.OCCUPIED && key.getPermissiveWorking()) {
                            permissiveBlock = true;
                        } else if (key.getState() == Block.UNDETECTED) {
                            if (log.isDebugEnabled()) {
                                log.debug("Block " + key.getDisplayName() + " is UNDETECTED so treat as unoccupied");
                            }
                        } else {
                            routeclear = false;
                        }
                    }
                }
            }

            for (NamedBeanSetting nbh : userSetBlocks) {
                Block key = (Block) nbh.getBean();
                key.addPropertyChangeListener(propertyBlockListener);
                if (key.getState() != getBlockState(key)) {
                    if (key.getState() == Block.OCCUPIED && key.getPermissiveWorking()) {
                        permissiveBlock = true;
                    } else {
                        routeclear = false;
                    }
                }
            }
            if (permissiveBlock) {
                /*If a block has been found to be permissive, but the source signalmast
                 does not support a call-on/permissive aspect then the route can not be set*/
                if (getSourceMast().getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.PERMISSIVE) == null) {
                    routeclear = false;
                }
            }
            if (routeclear) {
                active = true;
                setSignalAppearance();
            } else {
                permissiveBlock = false;
                turnoutThrown = false;
            }
            destMastInit = true;
        }

        void useLayoutEditor(boolean boo) throws jmri.JmriException {
            if (log.isDebugEnabled()) {
                log.debug(destination.getDisplayName() + " called useLayoutEditor(" + boo + "), is " + useLayoutEditor);
            }
            if (useLayoutEditor == boo) {
                return;
            }
            useLayoutEditor = boo;
            if ((boo) && (InstanceManager.getDefault(LayoutBlockManager.class).routingStablised())) {
                try {
                    setupLayoutEditorDetails();
                } catch (jmri.JmriException e) {
                    throw e;
                    //Considered normal if there is no vlaid path using the layout editor.
                }
            } else {
                destinationBlock = null;
                facingBlock = null;
                protectingBlock = null;
                setAutoBlocks(null);
                setAutoTurnouts(null);
            }
        }

        void useLayoutEditorDetails(boolean turnouts, boolean blocks) throws jmri.JmriException {
            if (log.isDebugEnabled()) {
                log.debug(destination.getDisplayName() + " use layout editor details called " + useLayoutEditor);
            }
            useLayoutEditorTurnouts = turnouts;
            useLayoutEditorBlocks = blocks;
            if ((useLayoutEditor) && (InstanceManager.getDefault(LayoutBlockManager.class).routingStablised())) {
                try {
                    setupLayoutEditorDetails();
                } catch (jmri.JmriException e) {
                    throw e;
                    //Considered normal if there is no valid path using the layout editor.
                }
            }
        }

        void setupLayoutEditorDetails() throws jmri.JmriException {

            if (log.isDebugEnabled()) {
                log.debug(useLayoutEditor + " " + disposed);
            }
            if ((!useLayoutEditor) || (disposed)) {
                return;
            }
            LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
            if ((destinationBlock != null) && (log.isDebugEnabled())) {
                log.debug(destination.getDisplayName() + " Set use layout editor");
            }
            ArrayList<LayoutEditor> layout = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
            List<LayoutBlock> protectingBlocks = new ArrayList<LayoutBlock>();
            // We don't care which layout editor panel the signalmast is on, just so long as
            // as the routing is done via layout blocks
            LayoutBlock remoteProtectingBlock = null;
            for (int i = 0; i < layout.size(); i++) {
                if (log.isDebugEnabled()) {
                    log.debug(destination.getDisplayName() + " Layout name " + layout.get(i).getLayoutName());
                }
                if (facingBlock == null) {
                    facingBlock = lbm.getFacingBlockByNamedBean(getSourceMast(), layout.get(i));
                }
                if (protectingBlock == null && protectingBlocks.isEmpty()) {
                    //This is wrong
                    protectingBlocks = lbm.getProtectingBlocksByNamedBean(getSourceMast(), layout.get(i));
                }
                if (destinationBlock == null) {
                    destinationBlock = lbm.getFacingBlockByNamedBean(destination, layout.get(i));
                }
                if (remoteProtectingBlock == null) {
                    remoteProtectingBlock = lbm.getProtectedBlockByNamedBean(destination, layout.get(i));
                }
            }
            //At this point if we are not using the layout editor turnout or block
            //details then there is no point in trying to gather them
            if ((!useLayoutEditorTurnouts) && (!useLayoutEditorBlocks)) {
                return;
            }
            if (facingBlock == null) {
                log.error("No facing block found for source mast " + getSourceMast().getDisplayName());
                throw new jmri.JmriException("No facing block found for source mast " + getSourceMast().getDisplayName());
            }
            if (destinationBlock == null) {
                log.error("No facing block found for destination mast " + destination.getDisplayName());
                throw new jmri.JmriException("No facing block found for destination mast " + destination.getDisplayName());
            }
            ArrayList<LayoutBlock> lblks = new ArrayList<LayoutBlock>();
            if (protectingBlock == null) {
                String pBlkNames = "";
                StringBuffer lBlksNamesBuf = new StringBuffer();
                for (LayoutBlock pBlk : protectingBlocks) {
                    pBlkNames = pBlkNames + " " + pBlk.blockName + " " + lbm.getLayoutBlockConnectivityTools().checkValidDest(facingBlock, pBlk, destinationBlock, remoteProtectingBlock, LayoutBlockConnectivityTools.MASTTOMAST) + ", ";
                    if (lbm.getLayoutBlockConnectivityTools().checkValidDest(facingBlock, pBlk, destinationBlock, remoteProtectingBlock, LayoutBlockConnectivityTools.MASTTOMAST)) {
                        try {
                            lblks = lbm.getLayoutBlockConnectivityTools().getLayoutBlocks(facingBlock, destinationBlock, pBlk, true, jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools.MASTTOMAST);
                            protectingBlock = pBlk;
                            for (LayoutBlock lBlk : lblks) {
                                lBlksNamesBuf.append(" ");
                                lBlksNamesBuf.append(lBlk.blockName);
                            }
                            break;
                        } catch (jmri.JmriException ee) {
                            log.debug("path not found this time");
                        }
                    }
                }
                String lBlksNames = new String(lBlksNamesBuf);
                
                if (protectingBlock == null) {
                    throw new jmri.JmriException("Path not valid, protecting block null. Protecting block: " + pBlkNames + " not connected to " + facingBlock.blockName + " layout block names: " + lBlksNames);
                }
            }
            try {
                if (!lbm.getLayoutBlockConnectivityTools().checkValidDest(facingBlock, protectingBlock, destinationBlock, remoteProtectingBlock, LayoutBlockConnectivityTools.MASTTOMAST)) {
                    throw new jmri.JmriException("Path not valid, destination check failed.");
                }
            } catch (jmri.JmriException e) {
                throw e;
            }
            if (log.isDebugEnabled()) {
                log.debug(destination.getDisplayName() + " face " + facingBlock);
                log.debug(destination.getDisplayName() + " prot " + protectingBlock);
                log.debug(destination.getDisplayName() + " dest " + destinationBlock);
            }

            if (destinationBlock != null && protectingBlock != null && facingBlock != null) {
                setAutoMasts(null, true);
                if (log.isDebugEnabled()) {
                    log.debug(destination.getDisplayName() + " face " + facingBlock.getDisplayName());
                    log.debug(destination.getDisplayName() + " prot " + protectingBlock.getDisplayName());
                    log.debug(destination.getDisplayName() + " dest " + destinationBlock.getDisplayName());
                }

                try {
                    lblks = lbm.getLayoutBlockConnectivityTools().getLayoutBlocks(facingBlock, destinationBlock, protectingBlock, true, jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools.MASTTOMAST);
                } catch (jmri.JmriException ee) {
                    log.error("No blocks found by the layout editor for pair " + source.getDisplayName() + " " + destination.getDisplayName());
                }
                LinkedHashMap<Block, Integer> block = setupLayoutEditorTurnoutDetails(lblks);

                for (int i = 0; i < blockInXings.size(); i++) {
                    blockInXings.get(i).removeSignalMastLogic(source);
                }
                blockInXings = new ArrayList<LevelXing>(0);
                xingAutoBlocks = new ArrayList<Block>(0);
                for (int i = 0; i < layout.size(); i++) {
                    LayoutEditor lay = layout.get(i);
                    for (int j = 0; j < lay.xingList.size(); j++) {
                        //Looking for a crossing that both layout blocks defined and they are individual.
                        if ((lay.xingList.get(j).getLayoutBlockAC() != null) && (lay.xingList.get(j).getLayoutBlockBD() != null) && (lay.xingList.get(j).getLayoutBlockAC() != lay.xingList.get(j).getLayoutBlockBD())) {
                            if (lblks.contains(lay.xingList.get(j).getLayoutBlockAC())) {
                                block.put(lay.xingList.get(j).getLayoutBlockBD().getBlock(), Block.UNOCCUPIED);
                                xingAutoBlocks.add(lay.xingList.get(j).getLayoutBlockBD().getBlock());
                                blockInXings.add(lay.xingList.get(j));
                            } else if (lblks.contains(lay.xingList.get(j).getLayoutBlockBD())) {
                                block.put(lay.xingList.get(j).getLayoutBlockAC().getBlock(), Block.UNOCCUPIED);
                                xingAutoBlocks.add(lay.xingList.get(j).getLayoutBlockAC().getBlock());
                                blockInXings.add(lay.xingList.get(j));
                            }
                        }
                    }
                }
                if (useLayoutEditorBlocks) {
                    setAutoBlocks(block);
                } else {
                    setAutoBlocks(null);
                }
                if (!useLayoutEditorTurnouts) {
                    setAutoTurnouts(null);
                }

                setupAutoSignalMast(null, false);
            }
            initialise();
        }

        LinkedHashMap<Block, Integer> setupLayoutEditorTurnoutDetails(List<LayoutBlock> lblks) {
            ConnectivityUtil connection;
            ArrayList<LayoutTurnout> turnoutlist;
            ArrayList<Integer> throwlist;
            Hashtable<Turnout, Integer> turnoutSettings = new Hashtable<Turnout, Integer>();
            LinkedHashMap<Block, Integer> block = new LinkedHashMap<Block, Integer>();
            for (int i = 0; i < lblks.size(); i++) {
                if (log.isDebugEnabled()) {
                    log.debug(lblks.get(i).getDisplayName());
                }
                block.put(lblks.get(i).getBlock(), Block.UNOCCUPIED);
                if ((i > 0)) {
                    int nxtBlk = i + 1;
                    int preBlk = i - 1;
                    if (i == lblks.size() - 1) {
                        nxtBlk = i;
                    }
                    //We use the best connectivity for the current block;
                    connection = new ConnectivityUtil(lblks.get(i).getMaxConnectedPanel());
                    turnoutlist = connection.getTurnoutList(lblks.get(i).getBlock(), lblks.get(preBlk).getBlock(), lblks.get(nxtBlk).getBlock());
                    throwlist = connection.getTurnoutSettingList();
                    for (int x = 0; x < turnoutlist.size(); x++) {
                        if (turnoutlist.get(x) instanceof LayoutSlip) {
                            int slipState = throwlist.get(x);
                            LayoutSlip ls = (LayoutSlip) turnoutlist.get(x);
                            int taState = ls.getTurnoutState(slipState);
                            turnoutSettings.put(ls.getTurnout(), taState);

                            int tbState = ls.getTurnoutBState(slipState);
                            turnoutSettings.put(ls.getTurnoutB(), tbState);
                        } else {
                            String t = turnoutlist.get(x).getTurnoutName();
                            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(t);
                            if (log.isDebugEnabled()) {
                                if ((turnoutlist.get(x).getTurnoutType() <= 3) && (!turnoutlist.get(x).getBlockName().equals(""))) {
                                    log.debug("turnout in list is straight left/right wye");
                                    log.debug("turnout block Name " + turnoutlist.get(x).getBlockName());
                                    log.debug("current " + lblks.get(i).getBlock().getDisplayName() + " - pre " + lblks.get(preBlk).getBlock().getDisplayName());
                                    log.debug("A " + turnoutlist.get(x).getConnectA());
                                    log.debug("B " + turnoutlist.get(x).getConnectB());
                                    log.debug("C " + turnoutlist.get(x).getConnectC());
                                    log.debug("D " + turnoutlist.get(x).getConnectD());
                                }
                            }
                            turnoutSettings.put(turnout, throwlist.get(x));
                            LayoutTurnout lt = turnoutlist.get(x);
                            if (lt.getSecondTurnout() != null) {
                                turnoutSettings.put(lt.getSecondTurnout(), throwlist.get(x));
                            }
                            /* TODO: We could do with a more inteligent way to deal with double crossovers, other than just looking at the state of the other conflicting blocks
                             such as looking at Signalmasts that protect the other blocks and the settings of any other turnouts along the way.
                             */
                            if (lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER) {
                                if (throwlist.get(x) == jmri.Turnout.THROWN) {
                                    if (lt.getLayoutBlock() == lblks.get(i) || lt.getLayoutBlockC() == lblks.get(i)) {
                                        if (lt.getLayoutBlockB() != null) {
                                            dblCrossOverAutoBlocks.add(lt.getLayoutBlockB().getBlock());
                                            block.put(lt.getLayoutBlockB().getBlock(), Block.UNOCCUPIED);
                                        }
                                        if (lt.getLayoutBlockD() != null) {
                                            dblCrossOverAutoBlocks.add(lt.getLayoutBlockD().getBlock());
                                            block.put(lt.getLayoutBlockD().getBlock(), Block.UNOCCUPIED);
                                        }
                                    } else if (lt.getLayoutBlockB() == lblks.get(i) || lt.getLayoutBlockD() == lblks.get(i)) {
                                        if (lt.getLayoutBlock() != null) {
                                            dblCrossOverAutoBlocks.add(lt.getLayoutBlock().getBlock());
                                            block.put(lt.getLayoutBlock().getBlock(), Block.UNOCCUPIED);
                                        }
                                        if (lt.getLayoutBlockC() != null) {
                                            dblCrossOverAutoBlocks.add(lt.getLayoutBlockC().getBlock());
                                            block.put(lt.getLayoutBlockC().getBlock(), Block.UNOCCUPIED);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (useLayoutEditorTurnouts) {
                setAutoTurnouts(turnoutSettings);
            }
            return block;
        }

        /*
         * The generation of auto signalmast, looks through all the other logics
         * to see if there are any blocks that are in common and thus will add
         * the other signalmast protecting that block.
         */
        void setupAutoSignalMast(jmri.SignalMastLogic sml, boolean overright) {
            if (!allowAutoSignalMastGeneration) {
                return;
            }
            ArrayList<jmri.SignalMastLogic> smlList = InstanceManager.signalMastLogicManagerInstance().getLogicsByDestination(destination);
            ArrayList<Block> allBlock = new ArrayList<Block>();

            for (NamedBeanSetting nbh : userSetBlocks) {
                allBlock.add((Block) nbh.getBean());
            }

            Set<Block> blockKeys = autoBlocks.keySet();
            for (Block key : blockKeys) {
                if (!allBlock.contains(key)) {
                    allBlock.add(key);
                }
            }
            Hashtable<SignalMast, String> masts;
            if (sml != null) {
                masts = autoMasts;
                if (sml.areBlocksIncluded(allBlock)) {
                    SignalMast mast = sml.getSourceMast();
                    String danger = mast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER);
                    masts.put(mast, danger);
                } else {
                    //No change so will leave.
                    return;
                }
            } else {
                masts = new Hashtable<SignalMast, String>();
                for (int i = 0; i < smlList.size(); i++) {
                    if (smlList.get(i).areBlocksIncluded(allBlock)) {
                        SignalMast mast = smlList.get(i).getSourceMast();
                        String danger = mast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER);
                        masts.put(mast, danger);
                    }
                }
            }
            setAutoMasts(masts, overright);
        }

        void addAutoSignalMast(SignalMast mast) {
            if (log.isDebugEnabled()) {
                log.debug(destination.getDisplayName() + " add mast to auto list " + mast);
            }
            String danger = mast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER);
            if (danger == null) {
                log.error("Can not add SignalMast " + mast.getDisplayName() + " to logic for " + source.getDisplayName() + " to " + destination.getDisplayName() + " as it does not have a danger appearance configured");
                return;
            }
            this.autoMasts.put(mast, danger);
            if (destMastInit) {
                mast.addPropertyChangeListener(propertySignalMastListener);
            }
            firePropertyChange("automasts", null, this.destination);
        }

        void removeAutoSignalMast(SignalMast mast) {
            this.autoMasts.remove(mast);
            if (destMastInit) {
                mast.removePropertyChangeListener(propertySignalMastListener);
            }
            firePropertyChange("automasts", this.destination, null);
        }

        boolean useLayoutEditor() {
            return useLayoutEditor;
        }

        boolean useLayoutEditorBlocks() {
            return useLayoutEditorBlocks;
        }

        boolean useLayoutEditorTurnouts() {
            return useLayoutEditorTurnouts;
        }

        boolean allowAutoSignalMastGeneration = false;

        boolean allowAutoSignalMastGen() {
            return allowAutoSignalMastGeneration;
        }

        void allowAutoSignalMastGen(boolean gen) {
            if (allowAutoSignalMastGeneration == gen) {
                return;
            }
            allowAutoSignalMastGeneration = gen;
        }

        void dispose() {
            if (thr != null) {
                thr.interrupt();
            }
            disposed = true;
            clearTurnoutLock();
            destination.removePropertyChangeListener(propertyDestinationMastListener);
            setBlocks(null);
            setAutoBlocks(null);
            setTurnouts(null);
            setAutoTurnouts(null);
            setSensors(null);
            setMasts(null);
            setAutoMasts(null, true);
        }

        void lockTurnouts() {
            //We do not allow the turnouts to be locked, if we are disposed the logic, 
            //the logic is not active, or if we do not allow the turnouts to be locked
            if ((disposed) || (!lockTurnouts) || (!active)) {
                return;
            }

            for (NamedBeanSetting nbh : userSetTurnouts) {
                Turnout key = (Turnout) nbh.getBean();
                key.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
            }
            Enumeration<Turnout> keys = autoTurnouts.keys();
            while (keys.hasMoreElements()) {
                Turnout key = keys.nextElement();
                key.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
            }
        }

        void clearTurnoutLock() {
            //We do not allow the turnout lock to be cleared, if we are not active, 
            //and the lock flag has not been set.
            if ((!lockTurnouts) && (!active)) {
                return;
            }

            Enumeration<Turnout> keys = autoTurnouts.keys();
            while (keys.hasMoreElements()) {
                Turnout key = keys.nextElement();
                key.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
            }

            for (NamedBeanSetting nbh : userSetTurnouts) {
                Turnout key = (Turnout) nbh.getBean();
                key.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
            }
        }

        protected void calculateSpeed() {
            if (log.isDebugEnabled()) {
                log.debug(destination.getDisplayName() + " calculate the speed setting for this logic ie what the signalmast will display");
            }
            minimumBlockSpeed = 0.0f;
            Enumeration<Turnout> keys = autoTurnouts.keys();
            while (keys.hasMoreElements()) {
                Turnout key = keys.nextElement();
                if (log.isDebugEnabled()) {
                    log.debug(destination.getDisplayName() + " turnout " + key.getDisplayName());
                }
                if (!isTurnoutIncluded(key)) {
                    if (autoTurnouts.get(key) == Turnout.CLOSED) {
                        if (((key.getStraightLimit() < minimumBlockSpeed) || (minimumBlockSpeed == 0)) && (key.getStraightLimit() != -1)) {
                            minimumBlockSpeed = key.getStraightLimit();
                            if (log.isDebugEnabled()) {
                                log.debug(destination.getDisplayName() + " turnout " + key.getDisplayName() + " set speed to " + minimumBlockSpeed);
                            }
                        }
                    } else {
                        if (((key.getDivergingLimit() < minimumBlockSpeed) || (minimumBlockSpeed == 0)) && (key.getDivergingLimit() != -1)) {
                            minimumBlockSpeed = key.getDivergingLimit();
                            if (log.isDebugEnabled()) {
                                log.debug(destination.getDisplayName() + " turnout " + key.getDisplayName() + " set speed to " + minimumBlockSpeed);
                            }
                        }
                    }
                }
            }

            for (NamedBeanSetting nbh : userSetTurnouts) {
                Turnout key = (Turnout) nbh.getBean();
                if (key.getState() == Turnout.CLOSED) {
                    if (((key.getStraightLimit() < minimumBlockSpeed) || (minimumBlockSpeed == 0)) && (key.getStraightLimit() != -1)) {
                        minimumBlockSpeed = key.getStraightLimit();
                        if (log.isDebugEnabled()) {
                            log.debug(destination.getDisplayName() + " turnout " + key.getDisplayName() + " set speed to " + minimumBlockSpeed);
                        }
                    }
                } else if (key.getState() == Turnout.THROWN) {
                    if (((key.getDivergingLimit() < minimumBlockSpeed) || (minimumBlockSpeed == 0)) && (key.getDivergingLimit() != -1)) {
                        minimumBlockSpeed = key.getDivergingLimit();
                        if (log.isDebugEnabled()) {
                            log.debug(destination.getDisplayName() + " turnout " + key.getDisplayName() + " set speed to " + minimumBlockSpeed);
                        }
                    }
                }
            }

            Set<Block> autoBlockKeys = autoBlocks.keySet();
            for (Block key : autoBlockKeys) {
                log.debug(destination.getDisplayName() + " auto block add list " + key.getDisplayName());
                if (!isBlockIncluded(key)) {
                    if (((key.getSpeedLimit() < minimumBlockSpeed) || (minimumBlockSpeed == 0)) && (key.getSpeedLimit() != -1)) {
                        minimumBlockSpeed = key.getSpeedLimit();
                        if (log.isDebugEnabled()) {
                            log.debug(destination.getDisplayName() + " block " + key.getDisplayName() + " set speed to " + minimumBlockSpeed);
                        }
                    }
                }
            }
            for (NamedBeanSetting nbh : userSetBlocks) {
                Block key = (Block) nbh.getBean();
                if (((key.getSpeedLimit() < minimumBlockSpeed) || (minimumBlockSpeed == 0)) && (key.getSpeedLimit() != -1)) {
                    if (log.isDebugEnabled()) {
                        log.debug(destination.getDisplayName() + " block " + key.getDisplayName() + " set speed to " + minimumBlockSpeed);
                    }
                    minimumBlockSpeed = key.getSpeedLimit();
                }
            }
            /*if(minimumBlockSpeed==-0.1f)
             minimumBlockSpeed==0.0f;*/
        }

        protected PropertyChangeListener propertySensorListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                Sensor sen = (Sensor) e.getSource();
                log.debug(source.getDisplayName() + " to " + destination.getDisplayName() + " destination sensor " + sen.getDisplayName() + "trigger " + e.getPropertyName());
                if (e.getPropertyName().equals("KnownState")) {
                    int now = ((Integer) e.getNewValue()).intValue();
                    log.debug("current value " + now + " value we want " + getSensorState(sen));
                    if (isSensorIncluded(sen) && getSensorState(sen) != now) {
                        //if(log.isDebugEnabled())
                        log.debug("Sensor " + sen.getDisplayName() + " caused the signalmast to be set to danger");
                        //getSourceMast().setAspect(stopAspect);
                        if (active == true) {
                            active = false;
                            setSignalAppearance();
                        }
                    } else if (getSensorState(sen) == now) {
                        //if(log.isDebugEnabled())
                        log.debug(destination.getDisplayName() + " sensor " + sen.getDisplayName() + " triggers a calculation of change");
                        checkState();
                    }
                }
            }
        };

        protected PropertyChangeListener propertyTurnoutListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                Turnout turn = (Turnout) e.getSource();
                //   log.debug(destination.getDisplayName() + " destination sensor "+ sen.getDisplayName() + "trigger");
                if (e.getPropertyName().equals("KnownState")) {
                    //Need to check this against the manual list vs auto list
                    //The manual list should over-ride the auto list
                    int now = ((Integer) e.getNewValue()).intValue();
                    if (isTurnoutIncluded(turn)) {
                        if (getTurnoutState(turn) != now) {
                            if (log.isDebugEnabled()) {
                                log.debug("Turnout " + turn.getDisplayName() + " caused the signalmast to be set");
                                log.debug("From " + getSourceMast().getDisplayName() + " to " + destination.getDisplayName() + " Turnout " + turn.getDisplayName() + " caused the signalmast to be set to danger");
                            }
                            if (active == true) {
                                active = false;
                                setSignalAppearance();
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug(destination.getDisplayName() + " turnout " + turn.getDisplayName() + " triggers a calculation of change");
                            }
                            checkState();
                        }
                    } else if (autoTurnouts.containsKey(turn)) {
                        if (getAutoTurnoutState(turn) != now) {
                            if (log.isDebugEnabled()) {
                                log.debug("Turnout " + turn.getDisplayName() + " auto caused the signalmast to be set");
                                log.debug("From " + getSourceMast().getDisplayName() + " to" + destination.getDisplayName() + " Auto Turnout " + turn.getDisplayName() + " auto caused the signalmast to be set to danger");
                            }
                            if (active == true) {
                                active = false;
                                setSignalAppearance();
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("From " + getSourceMast().getDisplayName() + " to " + destination.getDisplayName() + " turnout " + turn.getDisplayName() + " triggers a calculation of change");
                            }
                            checkState();
                        }
                    }

                } else if ((e.getPropertyName().equals("TurnoutStraightSpeedChange")) || (e.getPropertyName().equals("TurnoutDivergingSpeedChange"))) {
                    calculateSpeed();
                }
            }
        };

        protected PropertyChangeListener propertyBlockListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                Block block = (Block) e.getSource();
                if (log.isDebugEnabled()) {
                    log.debug(destination.getDisplayName() + " destination block " + block.getDisplayName() + " trigger " + e.getPropertyName() + " " + e.getNewValue());
                }
                if (e.getPropertyName().equals("state") || e.getPropertyName().equals("allocated")) {
                    if (log.isDebugEnabled()) {
                        // TODO: what is this?
                        log.debug("Included in user entered block " + Boolean.toString(isBlockIncluded(block)));
                        log.debug("Included in AutoGenerated Block " + Boolean.toString(autoBlocks.containsKey(block)));
                    }
                    if (isBlockIncluded(block)) {
                        if (log.isDebugEnabled()) {
                            log.debug(destination.getDisplayName() + " in manual block");
                            log.debug(getBlockState(block) + "  " + block.getState());
                        }
                        checkState();
                    } else if (autoBlocks.containsKey(block)) {
                        if (log.isDebugEnabled()) {
                            log.debug(destination.getDisplayName() + " in auto block");
                            log.debug(getAutoBlockState(block) + "  " + block.getState());
                        }
                        checkState();
                    } else if (log.isDebugEnabled()) {
                        log.debug(destination.getDisplayName() + " Not found");
                    }
                } else if (e.getPropertyName().equals("BlockSpeedChange")) {
                    calculateSpeed();
                }
            }
        };

        protected PropertyChangeListener propertySignalMastListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {

                SignalMast mast = (SignalMast) e.getSource();
                if (log.isDebugEnabled()) {
                    log.debug(destination.getDisplayName() + " signalmast change " + mast.getDisplayName() + " " + e.getPropertyName());
                }
                //   log.debug(destination.getDisplayName() + " destination sensor "+ sen.getDisplayName() + "trigger");
                if (e.getPropertyName().equals("Aspect")) {

                    String now = ((String) e.getNewValue());
                    if (log.isDebugEnabled()) {
                        log.debug(destination.getDisplayName() + " match property " + now);
                    }
                    if (isSignalMastIncluded(mast)) {
                        if (!now.equals(getSignalMastState(mast))) {
                            if (log.isDebugEnabled()) {
                                log.debug(destination.getDisplayName() + " in mast list SignalMast " + mast.getDisplayName() + " caused the signalmast to be set");
                                log.debug("SignalMast " + mast.getDisplayName() + " caused the signalmast to be set");
                            }
                            if (active) {
                                active = false;
                                setSignalAppearance();
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug(destination.getDisplayName() + " in mast list signalmast change");
                            }
                            checkState();
                        }
                    } else if (autoMasts.containsKey(mast)) {
                        if (!now.equals(getAutoSignalMastState(mast))) {
                            if (log.isDebugEnabled()) {
                                log.debug("SignalMast " + mast.getDisplayName() + " caused the signalmast to be set");
                                log.debug(destination.getDisplayName() + " in auto mast list SignalMast " + mast.getDisplayName() + " caused the signalmast to be set");
                            }
                            if (active) {
                                active = false;
                                setSignalAppearance();
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug(destination.getDisplayName() + " in auto mast list signalmast change");
                            }
                            checkState();
                        }
                    }
                }
            }
        };

        private class NamedBeanSetting {

            NamedBeanHandle<?> namedBean;
            int setting = 0;
            String strSetting = null;

            NamedBeanSetting(NamedBeanHandle<?> namedBean, int setting) {
                this.namedBean = namedBean;
                this.setting = setting;
            }

            NamedBeanSetting(NamedBeanHandle<?> namedBean, String setting) {
                this.namedBean = namedBean;
                strSetting = setting;
            }

            NamedBean getBean() {
                return (NamedBean) namedBean.getBean();
            }

            NamedBeanHandle<?> getNamedBean() {
                return namedBean;
            }

            int getSetting() {
                return setting;
            }

            String getStringSetting() {
                return strSetting;
            }

            String getBeanName() {
                return namedBean.getName();
            }
        }
    }

    //This is the listener on the destination signalMast
    protected PropertyChangeListener propertyDestinationMastListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            SignalMast mast = (SignalMast) e.getSource();
            if (mast == destination) {
                if (log.isDebugEnabled()) {
                    log.debug("destination mast change " + mast.getDisplayName());
                }
                setSignalAppearance();
            }
        }
    };

    //This is the listener on the source signalMast
    protected PropertyChangeListener propertySourceMastListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            SignalMast mast = (SignalMast) e.getSource();
            if ((mast == source) && (e.getPropertyName().equals("Held"))) {
                if (log.isDebugEnabled()) {
                    log.debug("source mast change " + mast.getDisplayName() + " " + e.getPropertyName());
                }
                setSignalAppearance();
            }
        }
    };

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public synchronized int getNumPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners().length;
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    //@todo need to think how we deal with auto generated lists based upon the layout editor.
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        NamedBean nb = (NamedBean) evt.getOldValue();
        if ("CanDelete".equals(evt.getPropertyName())) { //IN18N
            boolean found = false;
            StringBuilder message = new StringBuilder();
            if (nb instanceof SignalMast) {
                if (nb.equals(source)) {
                    message.append("Has SignalMast Logic attached which will be <b>Deleted</b> to <ul>");
                    for (SignalMast sm : getDestinationList()) {
                        message.append("<li>");
                        message.append(sm.getDisplayName());
                        message.append("</li>");
                    }
                    message.append("</ul>");
                    throw new java.beans.PropertyVetoException(message.toString(), evt);

                } else if (isDestinationValid((SignalMast) nb)) {
                    throw new java.beans.PropertyVetoException("Is the end point mast for logic attached to signal mast " + source.getDisplayName() + " which will be <b>Deleted</b> ", evt);
                }
                for (SignalMast sm : getDestinationList()) {
                    if (isSignalMastIncluded((SignalMast) nb, sm)) {
                        message.append("<li>");
                        message.append("Used in conflicting logic of " + source.getDisplayName() + " & " + sm.getDisplayName());
                        message.append("</li>");
                    }
                }
            }
            if (nb instanceof Turnout) {
                for (SignalMast sm : getDestinationList()) {
                    if (isTurnoutIncluded((Turnout) nb, sm)) {
                        message.append("<li>Is in logic between Signal Masts " + source.getDisplayName() + " " + sm.getDisplayName() + "</li>");
                        found = true;
                    }
                }
            }
            if (nb instanceof Sensor) {
                for (SignalMast sm : getDestinationList()) {
                    if (isSensorIncluded((Sensor) nb, sm)) {
                        message.append("<li>");
                        message.append("Is in logic between Signal Masts " + source.getDisplayName() + " " + sm.getDisplayName());
                        message.append("</li>");
                        found = true;
                    }
                }
            }
            if (found) {
                throw new java.beans.PropertyVetoException(message.toString(), evt);
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { //IN18N
            if (nb instanceof SignalMast) {
                if (nb.equals(source)) {
                    dispose();
                }
                if (isDestinationValid((SignalMast) nb)) {
                    removeDestination((SignalMast) nb);
                }
                for (SignalMast sm : getDestinationList()) {
                    if (isSignalMastIncluded((SignalMast) nb, sm)) {
                        log.warn("Unhandled condition: signal mast included during DoDelete");
                        // @todo need to deal with this situation
                    }
                }
            }
            if (nb instanceof Turnout) {
                Turnout t = (Turnout) nb;
                for (SignalMast sm : getDestinationList()) {
                    if (isTurnoutIncluded(t, sm)) {
                        removeTurnout(t, sm);
                    }
                }
            }
            if (nb instanceof Sensor) {
                Sensor s = (Sensor) nb;
                for (SignalMast sm : getDestinationList()) {
                    if (isSensorIncluded(s, sm)) {
                        removeSensor(s, sm);
                    }
                }
            }
        }
    }

    public void dispose() {
        if (thr != null) {
            thr.interrupt();
        }
        disposing = true;
        getSourceMast().removePropertyChangeListener(propertySourceMastListener);
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast dm = en.nextElement();
            destList.get(dm).dispose();
        }
    }

    public String getBeanType() {
        return Bundle.getMessage("BeanNameSignalMastLogic");
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastLogic.class.getName());
}
