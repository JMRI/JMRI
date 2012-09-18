package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Set;
import jmri.Block;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.NamedBeanHandle;

import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.ConnectivityUtil;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.LayoutSlip;

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

public class DefaultSignalMastLogic implements jmri.SignalMastLogic {
    SignalMast source;
    SignalMast destination;
    String stopAspect;

    Hashtable<SignalMast, DestinationMast> destList = new Hashtable<SignalMast, DestinationMast>();
    LayoutEditor editor;

    boolean useAutoGenBlock = true;
    boolean useAutoGenTurnouts = true;

    LayoutBlock facingBlock = null;
    LayoutBlock protectingBlock = null;

    boolean disposing = false;

    /**
     * Initialise the signal mast logic
     * @param source - The signalmast we are configuring
     */
    public DefaultSignalMastLogic (SignalMast source){
        this.source = source;
        this.stopAspect = source.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER);
        this.source.addPropertyChangeListener(propertySourceMastListener);
        if(source.getAspect()==null)
            source.setAspect(stopAspect);
    }
    
    public void setFacingBlock(LayoutBlock facing){
        facingBlock = facing;
    }
    
    public void setProtectingBlock(LayoutBlock protecting){
        protectingBlock = protecting;
    }
    
    public LayoutBlock getFacingBlock(){
        return facingBlock;
    }
    
    public LayoutBlock getProtectingBlock(){
        return protectingBlock;
    }

    public SignalMast getSourceMast(){
        return source;
    }
    
    public void replaceSourceMast(SignalMast oldMast, SignalMast newMast){
        if(oldMast!=source){
            //Old mast does not match new mast so will exit
            return;
        }
        source.removePropertyChangeListener(propertySourceMastListener);
        source = newMast;
        stopAspect = source.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER);
        source.addPropertyChangeListener(propertySourceMastListener);
        if(source.getAspect()==null)
            source.setAspect(stopAspect);
        firePropertyChange("updatedSource", oldMast, newMast);
    }
    
    public void replaceDestinationMast(SignalMast oldMast, SignalMast newMast){
        if(!destList.containsKey(oldMast)){
            return;
        }
        DestinationMast destMast = destList.get(oldMast);
        destMast.updateDestinationMast(newMast);
        if(destination==oldMast){
            oldMast.removePropertyChangeListener(propertyDestinationMastListener);
            newMast.addPropertyChangeListener(propertyDestinationMastListener);
            destination=newMast;
            setSignalAppearance();
        }
        destList.remove(oldMast);
        destList.put(newMast, destMast);
        firePropertyChange("updatedDestination", oldMast, newMast);
    }
    
    public void setDestinationMast(SignalMast dest){
        if(destList.containsKey(dest)){
            return;
        }
        int oldSize = destList.size();
        destList.put(dest, new DestinationMast(dest));
        //InstanceManager.signalMastLogicManagerInstance().addDestinationMastToLogic(this, dest);
        firePropertyChange("length", oldSize, Integer.valueOf(destList.size()));
    }
    
    public boolean isDestinationValid(SignalMast dest){
        if(dest==null)
            return false;
        return destList.containsKey(dest);
    }
    
    public ArrayList<SignalMast> getDestinationList(){
        ArrayList<SignalMast> out = new ArrayList<SignalMast>();
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }
    
    public String getComment(SignalMast dest){
        if(!destList.containsKey(dest)){
            return "";
        }
        return destList.get(dest).getComment();
    }

    public void setComment(String comment,SignalMast dest){
        if(!destList.containsKey(dest)){
            return;
        }
        destList.get(dest).setComment(comment);
    }
    
    /**
     * Use this to determine if the signalmast logic is stored in the panel file
     * and if all the information is stored.
     * @param store
     */
    public void setStore(int store, SignalMast destination){
        if(!destList.containsKey(destination)){
            return;
        }
        destList.get(destination).setStore(store);
    }

    /**
     * returns where the signalmast logic should be stored, if so how much.
     */
    public int getStoreState(SignalMast destination){
        if(!destList.containsKey(destination)){
            return STORENONE;
        }
        return destList.get(destination).getStoreState();
    }

    /**
    * Sets the logic to the destination signal mast to be enabled.
    */
    public void setEnabled(SignalMast dest) {
        if(!destList.containsKey(dest)){
            return;
        }
        destList.get(dest).setEnabled();
    }
    
    /**
    * Sets the logic to the destination signal mast to be disabled.
    */    
    public void setDisabled(SignalMast dest) {
        if(!destList.containsKey(dest)){
            return;
        }
        destList.get(dest).setDisabled();
    }
    
    /**
    * Query if the signalmast logic to the destination signal mast is enabled or disabled.
    */
    public boolean isEnabled(SignalMast dest) { 
        if(!destList.containsKey(dest)){
            return false;
        }
        return destList.get(dest).isEnabled();
    }

    /**
    * Query if the signalmast logic to the destination signal mast is active.
    */
    public boolean isActive(SignalMast dest) { 
        if(!destList.containsKey(dest)){
            return false;
        }
        return destList.get(dest).isActive();
    }
    /**
     *
     * @param dest Destination SignalMast.
     * @return true if there are no more destination signal masts
     */
    public boolean removeDestination(SignalMast dest){
        int oldSize = destList.size();
        if(destList.containsKey(dest)){
            //InstanceManager.signalMastLogicManagerInstance().removeDestinationMastToLogic(this, dest);
            destList.get(dest).dispose();
            destList.remove(dest);
            firePropertyChange("length", oldSize, Integer.valueOf(destList.size()));
        }
        if (destList.isEmpty())
            return true;
        return false;
    }
    
    public void disableLayoutEditorUse(){
        for(DestinationMast dest : destList.values()){
            try {
                dest.useLayoutEditor(false);
            } catch (jmri.JmriException e){
                log.error(e);
            }
        }
    }

    /**
    * Sets whether this logic should use the details stored in the layout editor
    * to determine the which blocks, turnouts will make up the logic between
    * the source and destination signal mast.
    *
    * @param boo Use the layout editor details to determine logic details.
    * @param destination Destination SignalMast.
    * 
    */
    public void useLayoutEditor(boolean boo, SignalMast destination) throws jmri.JmriException {
        if(!destList.containsKey(destination)){
            return;
        }

        if (boo){
            log.debug("Set use layout editor");
            ArrayList<LayoutEditor> layout = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
            /*We don't care which layout editor panel the signalmast is on, just so long as
            as the routing is done via layout blocks*/
            log.debug(layout.size());
            for(int i = 0; i<layout.size(); i++){
                if(log.isDebugEnabled())
                    log.debug(layout.get(i).getLayoutName());
                if (facingBlock==null){
                    facingBlock = InstanceManager.layoutBlockManagerInstance().getFacingBlockByMast(getSourceMast().getUserName(), layout.get(i));
                    if (facingBlock==null)
                        facingBlock = InstanceManager.layoutBlockManagerInstance().getFacingBlockByMast(getSourceMast().getSystemName(), layout.get(i));
                }
                if (protectingBlock==null){
                    protectingBlock = InstanceManager.layoutBlockManagerInstance().getProtectedBlockByMast(getSourceMast().getUserName(), layout.get(i));
                    if (protectingBlock==null)
                        protectingBlock = InstanceManager.layoutBlockManagerInstance().getProtectedBlockByMast(getSourceMast().getSystemName(), layout.get(i));
                }
            }
        }
        try {
            destList.get(destination).useLayoutEditor(boo);
        } catch (jmri.JmriException e){
            throw e;
        }
    }

    /**
    * Query if we are using the layout editor panels to build the signal mast
    * logic, blocks, turnouts .
    *
    * @param destination Destination SignalMast.
    * @return true if we are using the layout editor to build the signal mast logic.
    */
    public boolean useLayoutEditor(SignalMast destination){
        if(!destList.containsKey(destination)){
            return false;
        }
        return destList.get(destination).useLayoutEditor();
    }
    
    /**
    * Sets whether we should use the information from the layout editor for either
    * blocks or turnouts.
    *
    * @param destination Destination SignalMast.
    * @param blocks set false if not to use the block information gathered from the layouteditor
    * @param turnouts set false if not to use the turnout information gathered from the layouteditor
    */
    public void useLayoutEditorDetails(boolean turnouts, boolean blocks, SignalMast destination) throws jmri.JmriException {
        if(!destList.containsKey(destination)){
            return;
        }
        try {
            destList.get(destination).useLayoutEditorDetails(turnouts, blocks);
        } catch (jmri.JmriException e){
            throw e;
        }
    }
    
    /**
    * Query if we are using the layout editor turnout information in the 
    * signal mast logic.
    *
    * @param destination Destination SignalMast.
    * @return true if we are using the turnout information from the layout editor.
    */
    public boolean useLayoutEditorTurnouts(SignalMast destination){
        if(!destList.containsKey(destination)){
            return false;
        }
        return destList.get(destination).useLayoutEditorTurnouts();
    }

    /**
    * Query if we are using the layout editor block information in the 
    * signal mast logic.
    *
    * @param destination Destination SignalMast.
    * @return true if we are using the block information from the layout editor.
    */    
    public boolean useLayoutEditorBlocks(SignalMast destination){
        if(!destList.containsKey(destination)){
            return false;
        }
        return destList.get(destination).useLayoutEditorBlocks();
    }
    
    /**
    * Query if we are allowing the system to automatically generated a list of
    * conflicting SignalMast that have a direct effect on our logic.
    *
    * @param destination Destination SignalMast.
    * @return true if this is allowed.
    */
    public boolean allowAutoMaticSignalMastGeneration(SignalMast destination){
        if(!destList.containsKey(destination)){
            return false;
        }
        return destList.get(destination).allowAutoSignalMastGen();
    }
    
    /**
    * Sets whether we should allow the system to automatically generate a list of
    * signal masts that could cause a conflicting route.
    *
    * @param destination Destination SignalMast.
    * @param allow set true if we are to allow automatic generation.
    */
    public void allowAutoMaticSignalMastGeneration(boolean allow, SignalMast destination){
        if(!destList.containsKey(destination)){
            return;
        }
        destList.get(destination).allowAutoSignalMastGen(allow);
    }

    /**
    * Sets whether we should lock all turnouts between the source and destination
    * signal masts when the logic goes active, to prevent them from being changed.
    * This is dependant upon the hardware allowing for this.
    *
    * @param destination Destination SignalMast.
    * @param lock set true if the system should lock the turnout.
    */    
    public void allowTurnoutLock(boolean lock, SignalMast destination){
        if(!destList.containsKey(destination)){
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

    public boolean isTurnoutLockAllowed(SignalMast destination){
        if(!destList.containsKey(destination)){
            return false;
        }
        return destList.get(destination).isTurnoutLockAllowed();
    }
    
    /**
     * Sets the states that each turnout must be in for signal not to be set at a stop aspect
     * @param turnouts
     */
    public void setTurnouts(Hashtable<NamedBeanHandle<Turnout>, Integer> turnouts, SignalMast destination){
        if(!destList.containsKey(destination)){
            return;
        }
        destList.get(destination).setTurnouts(turnouts);
    }
    
        /**
     * Sets which blocks must be inactive for the signal not to be set at a stop aspect
     * These Turnouts are not stored in the panel file.
     */
    public void setAutoTurnouts(Hashtable<Turnout, Integer> turnouts, SignalMast destination){
        if(!destList.containsKey(destination)){
            return;
        }
        destList.get(destination).setAutoTurnouts(turnouts);
    }

    /**
     * Sets which blocks must be inactive for the signal not to be set at a stop aspect
     * @param blocks
     */
    public void setBlocks(Hashtable<Block, Integer> blocks, SignalMast destination){
        if(!destList.containsKey(destination)){
            return;
        }
        destList.get(destination).setBlocks(blocks);
    }
    
    /**
     * Sets which blocks must be inactive for the signal not to be set at a stop aspect
     * These blocks are not stored in the panel file.
     * @param blocks
     */
    //public void setLayoutBlocks
    public void setAutoBlocks(LinkedHashMap<Block, Integer> blocks, SignalMast destination){
        if(!destList.containsKey(destination)){
            return;
        }
        destList.get(destination).setAutoBlocks(blocks);
    }

    /**
     * Sets which masts must be in a given state before our mast can be set.
     * @param masts
     */
    public void setMasts(Hashtable<SignalMast, String> masts, SignalMast destination){
        if(!destList.containsKey(destination)){
            return;
        }
        destList.get(destination).setMasts(masts);
    }
    
    /**
     * Sets which masts must be in a given state before our mast can be set.
     * These masts are not stored in the panel file.
     * @param masts
     */
    public void setAutoMasts(Hashtable<SignalMast, String> masts, SignalMast destination){
        if(!destList.containsKey(destination)){
            return;
        }
        destList.get(destination).setAutoMasts(masts, true);
    }

    /**
     * Sets which sensors must be in a given state before our mast can be set.
     * @param sensors
     */
    public void setSensors(Hashtable<NamedBeanHandle<Sensor>, Integer> sensors, SignalMast destination){
        if(!destList.containsKey(destination)){
            return;
        }
        destList.get(destination).setSensors(sensors);
    }

    public void addSensor(String sensorName, int state, SignalMast destination){
        if(!destList.containsKey(destination))
            return;
        Sensor sen = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        if(sen!=null){
            NamedBeanHandle<Sensor> namedSensor = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sen);
            destList.get(destination).addSensor(namedSensor, state);
        }
    }
    
    public void removeSensor(String sensorName, SignalMast destination){
        if(!destList.containsKey(destination))
            return;
        Sensor sen = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        if(sen!=null){
            NamedBeanHandle<Sensor> namedSensor = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sen);
            destList.get(destination).removeSensor(namedSensor);
        }
    }

    public ArrayList<Block> getBlocks(SignalMast destination){
        if(!destList.containsKey(destination)){
            return new ArrayList<Block>();
        }
        return destList.get(destination).getBlocks();
    }
    
    public ArrayList<Block> getAutoBlocks(SignalMast destination){
        if(!destList.containsKey(destination)){
            return new ArrayList<Block>();
        }
        return destList.get(destination).getAutoBlocks();
    }
    
    public ArrayList<Block> getAutoBlocksBetweenMasts(SignalMast destination){
        if(!destList.containsKey(destination)){
            return new ArrayList<Block>();
        }
        if(destList.get(destination).xingAutoBlocks.size()==0){
            return destList.get(destination).getAutoBlocks();
        }
        ArrayList<Block> returnList = destList.get(destination).getAutoBlocks();
        log.info("before " + returnList.size());
        for(Block blk:destList.get(destination).getAutoBlocks()){
            if(destList.get(destination).xingAutoBlocks.contains(blk)){
                returnList.remove(blk);
            }
        }
        log.info("After " + returnList.size());
        return returnList;
    
    }

    public ArrayList<Turnout> getTurnouts(SignalMast destination){
        if(!destList.containsKey(destination)){
            return new ArrayList<Turnout>();
        }
        return destList.get(destination).getTurnouts();
    }
    
    public ArrayList<NamedBeanHandle<Turnout>> getNamedTurnouts(SignalMast destination){
        if(!destList.containsKey(destination)){
            return new ArrayList<NamedBeanHandle<Turnout>>();
        }
        return destList.get(destination).getNamedTurnouts();
    }
    
    public ArrayList<Turnout> getAutoTurnouts(SignalMast destination){
        if(!destList.containsKey(destination)){
            return new ArrayList<Turnout>();
        }
        return destList.get(destination).getAutoTurnouts();
    }

    public ArrayList<Sensor> getSensors(SignalMast destination){
        if(!destList.containsKey(destination)){
            return new ArrayList<Sensor>();
        }
        return destList.get(destination).getSensors();
    }
    
    public ArrayList<NamedBeanHandle<Sensor>> getNamedSensors(SignalMast destination){
        if(!destList.containsKey(destination)){
            return new ArrayList<NamedBeanHandle<Sensor>>();
        }
        return destList.get(destination).getNamedSensors();
    }

    public ArrayList<SignalMast> getSignalMasts(SignalMast destination){
        if(!destList.containsKey(destination)){
            return new ArrayList<SignalMast>();
        }
        return destList.get(destination).getSignalMasts();
    }
    
    public ArrayList<SignalMast> getAutoMasts(SignalMast destination){
        if(!destList.containsKey(destination)){
            return new ArrayList<SignalMast>();
        }
        return destList.get(destination).getAutoSignalMasts();
    }

    /*general method to initialise all*/
    public void initialise(){
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            destList.get(en.nextElement()).initialise();
        }
    }
    /**
     * Initialise the signalmast after all the parameters have been set.
     */
    public void initialise(SignalMast destination){
        if (disposing) return;
        
        if(!destList.containsKey(destination)){
            return;
        }
        destList.get(destination).initialise();
    }
    
    public void setupLayoutEditorDetails(){
        if(disposing) return;
        
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            try{
                destList.get(en.nextElement()).setupLayoutEditorDetails();
            } catch (jmri.JmriException e){
                //Considered normal if no route is valid on the layout editor
            }
        }
    }
    
    /**
     *
     * @return true if the path to the next signal is clear
     */
    boolean checkStates(){
        SignalMast oldActiveMast = destination;
        if (destination!=null){
            firePropertyChange("state", oldActiveMast, null);
            log.debug("Remove listener from destination");
            destination.removePropertyChangeListener(propertyDestinationMastListener);
            if(destList.containsKey(destination))
                destList.get(destination).clearTurnoutLock();
        }
        
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast key = en.nextElement();
            if(log.isDebugEnabled()){
                log.debug("Enabled " + (destList.get(key)).isEnabled());
                log.debug("Active " + destList.get(key).isActive());
            }
            if ((destList.get(key)).isEnabled()&&(destList.get(key).isActive())){
               destination = key;
               if(log.isDebugEnabled())
                log.debug("destination mast " + key.getDisplayName() + " Add listener to destination");
               destination.addPropertyChangeListener(propertyDestinationMastListener);
               firePropertyChange("state", oldActiveMast, destination);
               destList.get(key).lockTurnouts();
               return true;
            }
        }
        return false;
    }
    /**
    * Returns true if any of the blocks in the supplied list are included in any
    * of the logics that set this signal.
    */
    public boolean areBlocksIncluded(ArrayList<Block> blks){
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast dm = en.nextElement();
            boolean included = false;
            for(int i=0; i<blks.size(); i++){
                included = destList.get(dm).isBlockIncluded(blks.get(i));
                if(included){
                    return true;
                }
                destList.get(dm).isAutoBlockIncluded(blks.get(i));
                if(included){
                    return true;
                }
            }
        }
        return false;
    }
    
    public int getBlockState(Block block, SignalMast destination){
        if(!destList.containsKey(destination)){
            return -1;
        }
        return destList.get(destination).getBlockState(block);
    }
    
    public boolean isBlockIncluded(Block block, SignalMast destination){
        if(!destList.containsKey(destination)){
            return false;
        }
        return destList.get(destination).isBlockIncluded(block);
    }
    
    public boolean isTurnoutIncluded(Turnout turnout, SignalMast destination){
        if(!destList.containsKey(destination)){
            return false;
        }
        return destList.get(destination).isTurnoutIncluded(turnout);
    }
    
    public boolean isSensorIncluded(Sensor sensor, SignalMast destination){
        if(!destList.containsKey(destination)){
            return false;
        }
        return destList.get(destination).isSensorIncluded(sensor);
    }
    
    public boolean isSignalMastIncluded(SignalMast signal, SignalMast destination){
        if(!destList.containsKey(destination)){
            return false;
        }
        return destList.get(destination).isSignalMastIncluded(signal);
    }
    
    public int getAutoBlockState(Block block, SignalMast destination){
        if(!destList.containsKey(destination)){
            return -1;
        }
        return destList.get(destination).getAutoBlockState(block);
    }
    
    public int getSensorState(Sensor sensor, SignalMast destination){
        if(!destList.containsKey(destination)){
            return -1;
        }
        return destList.get(destination).getSensorState(sensor);
    }
    
    public int getTurnoutState(Turnout turnout, SignalMast destination){
        if(!destList.containsKey(destination)){
            return -1;
        }
        return destList.get(destination).getTurnoutState(turnout);
    }
    
    public int getAutoTurnoutState(Turnout turnout, SignalMast destination){
        if(!destList.containsKey(destination)){
            return -1;
        }
        return destList.get(destination).getAutoTurnoutState(turnout);
    }

    public String getSignalMastState(SignalMast mast, SignalMast destination){
        if(!destList.containsKey(destination)){
            return null;
        }
        return destList.get(destination).getSignalMastState(mast);
    }
    
    public String getAutoSignalMastState(SignalMast mast, SignalMast destination){
        if(!destList.containsKey(destination)){
            return null;
        }
        return destList.get(destination).getAutoSignalMastState(mast);
    }
    
    public float getMaximumSpeed(SignalMast destination){
        if(!destList.containsKey(destination)){
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
    synchronized void setSignalAppearance(){
        log.debug("In appearance called " + source.getDisplayName());
        if (inWait){
            log.debug("In wait for set appearance");
            return;
        }
        inWait=true;
        
        Runnable r = new Runnable() {
            public void run() {
                try {
                    Thread.sleep((InstanceManager.signalMastLogicManagerInstance().getSignalLogicDelay()/2));
                    inWait=false;
                    setMastAppearance();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    inWait=false;
                }
            }
        };
        
        thr = new Thread(r);
        thr.setName(getSourceMast().getDisplayName());
        try{
            thr.start();
        } catch (java.lang.IllegalThreadStateException ex){
            log.error("In Setting Signal Mast Appearance " + ex.toString());
        }
    }
    
    /**
     * Evaluates the destinatin signal mast appearance and sets ours accordingly
     */
    void setMastAppearance(){
        synchronized(this){
            if(inWait){
                log.error("Set SignalMast Appearance called while still in wait");
                return;
            }
        }
        log.debug("Set Signal Appearances");
        if(getSourceMast().getHeld()){
            log.debug("Signal is at a held state so will set to the aspect defined for held or danger");
            if(getSourceMast().getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD)!=null){
                getSourceMast().setAspect(getSourceMast().getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD));
            } else {
                getSourceMast().setAspect(getSourceMast().getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER));
            }
            return;
        }
        if (!checkStates()){
            log.debug("advanced routes not clear");
            getSourceMast().setAspect(stopAspect);
            return;
        }
        String[] advancedAspect;
        if(destination.getHeld()){
            if(destination.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD)!=null){
                advancedAspect = getSourceMast().getAppearanceMap().getValidAspectsForAdvancedAspect(destination.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD));
            } else {
                advancedAspect = getSourceMast().getAppearanceMap().getValidAspectsForAdvancedAspect(destination.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER));
            }
        } else {
            advancedAspect = getSourceMast().getAppearanceMap().getValidAspectsForAdvancedAspect(destination.getAspect());
        }

        if(log.isDebugEnabled())
            log.debug("distant aspect is " + destination.getAspect());

        if (advancedAspect!=null){
            String aspect = stopAspect;
            if(destList.get(destination).permissiveBlock) {
                //if a block is in a permissive state then we set the permissive appearance
                aspect = getSourceMast().getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.PERMISSIVE);
            } else {
                for(int i = 0; i<advancedAspect.length; i++){
                    if(!getSourceMast().isAspectDisabled(advancedAspect[i])){
                        aspect = advancedAspect[i];
                        break;
                    }
                }
                ArrayList<Integer> divergAspects = new ArrayList<Integer>();
                ArrayList<Integer> nonDivergAspects = new ArrayList<Integer>();
                ArrayList<Integer> eitherAspects = new ArrayList<Integer>();
                if (advancedAspect.length>1) {
                    float maxSigSpeed = -1;
                    float maxPathSpeed = destList.get(destination).getMinimumSpeed();
                    boolean divergRoute = destList.get(destination).turnoutThrown;
                    
                    log.debug("Diverging route? " + divergRoute);
                    boolean divergFlagsAvailable = false;
                        //We split the aspects into two lists, one with divering flag set, the other without.
                    for(int i = 0; i<advancedAspect.length; i++){
                        String div = null;
                        if(!getSourceMast().isAspectDisabled(advancedAspect[i])){
                            div = (String) getSourceMast().getSignalSystem().getProperty(advancedAspect[i], "route");
                        }
                        if(div!=null){
                            if (div.equals("Diverging")){
                                log.debug("Aspect " + advancedAspect[i] + "added as Diverging Route");
                                divergAspects.add(i);
                                divergFlagsAvailable = true;
                                log.debug("Using Diverging Flag");
                            } else if (div.equals("Either")) {
                                log.debug("Aspect " + advancedAspect[i] + "added as Both Diverging and Normal Route");
                                nonDivergAspects.add(i);
                                divergAspects.add(i);
                                divergFlagsAvailable = true;
                                eitherAspects.add(i);
                                log.debug("Using Diverging Flag");
                            } else {
                                log.debug("Aspect " + advancedAspect[i] + "added as Normal Route");
                                nonDivergAspects.add(i);
                                log.debug("Aspect " + advancedAspect[i] + "added as Normal Route");
                            }
                        } else {
                            nonDivergAspects.add(i);
                            log.debug("Aspect " + advancedAspect[i] + "added as Normal Route");
                        }
                    }
                    if((eitherAspects.equals(divergAspects)) && (divergAspects.size()<nonDivergAspects.size())){
                        //There are no unique diverging aspects 
                        log.debug("'Either' aspects equals divergAspects and is less than non-diverging aspects");
                        divergFlagsAvailable = false;
                    }
                    log.debug("path max speed : " + maxPathSpeed);
                    for (int i = 0; i<advancedAspect.length; i++){
                        if(!getSourceMast().isAspectDisabled(advancedAspect[i])){
                            String strSpeed = (String) getSourceMast().getSignalSystem().getProperty(advancedAspect[i], "speed");
                            if(log.isDebugEnabled())
                                log.debug("Aspect Speed = " + strSpeed + " for aspect " + advancedAspect[i]);
                            /*  if the diverg flags available is set and the diverg aspect 
                                array contains the entry then we will check this aspect.
                                
                                If the diverg flag has not been set then we will check.
                            */
                            log.debug(advancedAspect[i]);
                            if((divergRoute && (divergFlagsAvailable) && (divergAspects.contains(i))) || ((divergRoute && !divergFlagsAvailable)||(!divergRoute)) && (nonDivergAspects.contains(i))){
                                log.debug("In list");
                                if ((strSpeed!=null) && (!strSpeed.equals(""))){
                                    float speed = 0.0f;
                                    try {
                                        speed = new Float(strSpeed);
                                    }catch (NumberFormatException nx) {
                                        try{
                                            speed = jmri.implementation.SignalSpeedMap.getMap().getSpeed(strSpeed);
                                        } catch (Exception ex){
                                            //Considered Normal if the speed does not appear in the map
                                        }
                                    }
                                    //Integer state = Integer.parseInt(strSpeed);
                                    /* This pics out either the highest speed signal if there
                                     * is no block speed specified or the highest speed signal
                                     * that is under the minimum block speed.
                                     */
                                    if(log.isDebugEnabled())
                                        log.debug(destination.getDisplayName() + " signal state speed " + speed + " maxSigSpeed " + maxSigSpeed + " max Path Speed " + maxPathSpeed);
                                    if(maxPathSpeed==0){
                                        if (maxSigSpeed ==-1){
                                            log.debug("min speed on this route is equal to 0 so will set this as our max speed");
                                            maxSigSpeed=speed;
                                            aspect = advancedAspect[i];
                                            log.debug("Aspect to set is " + aspect);
                                        } else if (speed > maxSigSpeed){
                                            log.debug("new speed is faster than old will use this");
                                            maxSigSpeed=speed;
                                            aspect = advancedAspect[i];
                                            log.debug("Aspect to set is " + aspect);
                                        }
                                    }
                                    else if ((speed>maxSigSpeed) && (maxSigSpeed<maxPathSpeed) && (speed<=maxPathSpeed)){
                                        //Only set the speed to the lowest if the max speed is greater than the path speed
                                        //and the new speed is less than the last max speed
                                        log.debug("our minimum speed on this route is less than our state speed, we will set this as our max speed");
                                        maxSigSpeed = speed;
                                        aspect = advancedAspect[i];
                                        log.debug("Aspect to set is " + aspect);
                                    } else if ((maxSigSpeed>maxPathSpeed) && (speed<maxSigSpeed)){
                                        log.debug("our max signal speed is greater than our path speed on this route, our speed is less that the maxSigSpeed");
                                        maxSigSpeed = speed;
                                        aspect = advancedAspect[i];
                                        log.debug("Aspect to set is " + aspect);
                                    
                                    } else if (maxSigSpeed == -1){
                                        log.debug("maxSigSpeed returned as -1");
                                        maxSigSpeed = speed;
                                        aspect = advancedAspect[i];
                                        log.debug("Aspect to set is " + aspect);
                                    }
                                }
                            }
                        } else if(log.isDebugEnabled()){
                            log.debug("Aspect has been disabled " + advancedAspect[i]);
                        }
                    }
                }
            }
            if ((aspect!=null) && (!aspect.equals(""))){
                log.debug("set mast aspect called from set appearance");
                getSourceMast().setAspect(aspect);
                return;
            }
        }
        log.debug("aspect returned is not valid");
        getSourceMast().setAspect(stopAspect);
    }
    
    public void setConflictingLogic(SignalMast sm, LevelXing lx){
        if(sm==null)
            return;
        if(log.isDebugEnabled())
            log.debug("setConflicting logic mast " + sm.getDisplayName());
        if(sm==source){
            log.debug("source is us so exit");
            return;
        }
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast dm = en.nextElement();
            if(destList.get(dm).isBlockIncluded(lx.getLayoutBlockAC()))
                destList.get(dm).addAutoSignalMast(sm);
            else if (destList.get(dm).isBlockIncluded(lx.getLayoutBlockBD()))
                destList.get(dm).addAutoSignalMast(sm);
            else if(destList.get(dm).isAutoBlockIncluded(lx.getLayoutBlockAC()))
                destList.get(dm).addAutoSignalMast(sm);
            else if (destList.get(dm).isAutoBlockIncluded(lx.getLayoutBlockBD()))
                destList.get(dm).addAutoSignalMast(sm);
            else
                log.debug("Block not found");
        }
    }
    
    public void removeConflictingLogic(SignalMast sm, LevelXing lx){
        if(sm==source)
            return;
        
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast dm = en.nextElement();
            if(destList.get(dm).isBlockIncluded(lx.getLayoutBlockAC()))
                destList.get(dm).removeAutoSignalMast(sm);
            else if (destList.get(dm).isBlockIncluded(lx.getLayoutBlockBD()))
                destList.get(dm).removeAutoSignalMast(sm);
        }
    }
    
    class DestinationMast{
        LayoutBlock destinationBlock = null;
        
        Hashtable<NamedBeanHandle<Turnout>, Integer> turnouts = new Hashtable<NamedBeanHandle<Turnout>, Integer>(0);
        Hashtable<Turnout, Integer> autoTurnouts = new Hashtable<Turnout, Integer>(0);
        //Hashtable<Turnout, Boolean> turnoutThroats = new Hashtable<Turnout, Boolean>(0);
        //Hashtable<Turnout, Boolean> autoTurnoutThroats = new Hashtable<Turnout, Boolean>(0);
        Hashtable<SignalMast, String> masts = new Hashtable<SignalMast, String>(0);
        Hashtable<SignalMast, String> autoMasts = new Hashtable<SignalMast, String>(0);
        Hashtable<NamedBeanHandle<Sensor>, Integer> sensors = new Hashtable<NamedBeanHandle<Sensor>, Integer>(0);
        //Blocks is used for user defined blocks between two signalmasts
        Hashtable<Block, Integer> blocks = new Hashtable<Block, Integer>(0);
        boolean turnoutThrown = false;
        boolean permissiveBlock = false;
        boolean disposed = false;
        
        ArrayList<LevelXing> blockInXings = new ArrayList<LevelXing>();
        
        //autoBlocks are for those automatically generated by the system.
        LinkedHashMap<Block, Integer> autoBlocks = new LinkedHashMap<Block, Integer>(0);
        ArrayList<Block> xingAutoBlocks = new ArrayList<Block>(0);
        SignalMast destination;
        boolean active = false;
        boolean destMastInit = false;

        float minimumBlockSpeed = 0.0f;
        
        boolean useLayoutEditor = false;
        boolean useLayoutEditorTurnouts = false;
        boolean useLayoutEditorBlocks = false;
        boolean lockTurnouts = false;
        
        DestinationMast(SignalMast destination){
            this.destination=destination;
            if(destination.getAspect()==null)
                destination.setAspect(destination.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER));
        }
        
        void updateDestinationMast(SignalMast newMast){
            destination=newMast;
            if(destination.getAspect()==null)
                destination.setAspect(destination.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER));
        }
        
        /*LayoutBlock getProtectingBlock(){
            return protectingBlock;
        }*/
        
        String comment;
        
        String getComment(){
            return comment;
        }

        void setComment(String comment){
            String old = this.comment;
            this.comment=comment;
            firePropertyChange("Comment", old, comment);
        }

        boolean isActive(){
            if(disposed){
                log.error("checkState called even though this has been disposed of");
                return false;
            }
            return active;
        }

        float getMinimumSpeed(){
            return minimumBlockSpeed;
        }
        
        boolean enable = true;
        
        void setEnabled() {
            enable=true;
            firePropertyChange("Enabled", false, this.destination);
        }
        
        void setDisabled() {
            enable=false;
            firePropertyChange("Enabled", true, this.destination);
        }
        
        boolean isEnabled() { return enable; }
        
        int store = STOREALL;
            
        /**
         * Use this to determine if the signalmast logic is stored in the panel file
         * and if all the information is stored.
         * @param store
         */
         
        void setStore(int store){
            this.store=store;
        }

        int getStoreState(){
            return store;
        }
        
        boolean isTurnoutLockAllowed() { return lockTurnouts; }
        
        void allowTurnoutLock(boolean lock) { 
            if(lockTurnouts==lock)
                return;
            if(!lock)
                clearTurnoutLock();
            lockTurnouts = lock;
        }
        
        int getBlockState(Block block){
            if(blocks==null)
                return -1;
            return blocks.get(block);
        }
        
        void setTurnouts(Hashtable<NamedBeanHandle<Turnout>, Integer> turnouts){
                if(this.turnouts!=null){
                Enumeration<NamedBeanHandle<Turnout>> keys = this.turnouts.keys();
                while ( keys.hasMoreElements() )
                {
                   Turnout key = keys.nextElement().getBean();
                   key.removePropertyChangeListener(propertyTurnoutListener);
                }
            }
            destMastInit = false;
            if(turnouts==null){
                this.turnouts = new Hashtable<NamedBeanHandle<Turnout>, Integer>(0);
            } else {
                this.turnouts=turnouts;
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
         * Sets which blocks must be inactive for the signal not to be set at a stop aspect
         * @param blocks
         */
        void setAutoTurnouts(Hashtable<Turnout, Integer> turnouts){
            log.debug(destination.getDisplayName() + " setAutoTurnouts Called");
            if (this.autoTurnouts!=null){
                Enumeration<Turnout> keys = this.autoTurnouts.keys();
                while ( keys.hasMoreElements() )
                {
                   Turnout key = keys.nextElement();
                   key.removePropertyChangeListener(propertyTurnoutListener);
                }
                //minimumBlockSpeed = 0;
            }
            destMastInit = false;
            if(turnouts==null){
                this.autoTurnouts = new Hashtable<Turnout, Integer>(0);
            } else {
                this.autoTurnouts=turnouts;
            }
            firePropertyChange("autoturnouts", null, this.destination);
        }

        /**
         * Sets which blocks must be inactive for the signal not to be set at a stop aspect
         * @param blocks
         */
        void setBlocks(Hashtable<Block, Integer> blocks){
            log.debug(destination.getDisplayName() + " Set blocks called");
            if (this.blocks!=null){
                Enumeration<Block> keys = this.blocks.keys();
                while ( keys.hasMoreElements() )
                {
                   Block key = keys.nextElement();
                   key.removePropertyChangeListener(propertyBlockListener);
                }
                //minimumBlockSpeed = 0;
            }
            destMastInit = false;
            if(blocks==null){
                this.blocks = new Hashtable<Block, Integer>(0);
            } else {
                this.blocks=blocks;
            }
            firePropertyChange("blocks", null, this.destination);
        }
        
        /**
         * Sets which blocks must be inactive for the signal not to be set at a stop aspect
         * @param blocks
         */
        //public void setLayoutBlocks
        public void setAutoBlocks(LinkedHashMap<Block, Integer> blocks){
            if(log.isDebugEnabled())
                log.debug(destination.getDisplayName() + " setAutoBlocks Called");
            if (this.autoBlocks!=null){
                Set<Block> blockKeys = autoBlocks.keySet();
                //while ( blockKeys.hasMoreElements() )
                for(Block key:blockKeys)
                {
               //Block key = blockKeys.nextElement();
                    key.removePropertyChangeListener(propertyBlockListener);
                }
                //minimumBlockSpeed = 0;
            }
            destMastInit = false;
            if (blocks==null){
                this.autoBlocks= new LinkedHashMap<Block, Integer>(0);
            } else {
                this.autoBlocks=blocks;
                //We shall remove the facing block in the list.
                if(facingBlock!=null){
                    if(autoBlocks.containsKey(facingBlock.getBlock())){
                        autoBlocks.remove(facingBlock.getBlock());
                    }
                }
            }
            
            log.debug(autoBlocks.size());
            firePropertyChange("autoblocks", null, this.destination);
        }

        /**
         * Sets which masts must be in a given state before our mast can be set.
         * @param masts
         */
        void setMasts(Hashtable<SignalMast, String> masts){
            if (this.masts!=null){
                Enumeration<SignalMast> keys = this.masts.keys();
                while ( keys.hasMoreElements() )
                {
                   SignalMast key = keys.nextElement();
                   key.removePropertyChangeListener(propertySignalMastListener);
                }
            }
            destMastInit = false;

            if(masts==null){
                this.masts = new Hashtable<SignalMast, String>(0);
            } else {
                this.masts=masts;
            }
            firePropertyChange("masts", null, this.destination);
        }
        
        /**
         * Sets which signalMasts must be at Danager for the signal not to be set at a stop aspect
         * @param blocks
         */
        void setAutoMasts(Hashtable<SignalMast, String> newAutoMasts, boolean overright){
            if(log.isDebugEnabled())
                log.debug(destination.getDisplayName() + " setAutoMast Called");
            if (this.autoMasts!=null){
                Enumeration<SignalMast> keys = this.autoMasts.keys();
                while ( keys.hasMoreElements() )
                {
                   SignalMast key = keys.nextElement();
                   key.removePropertyChangeListener(propertySignalMastListener);
                }
                //minimumBlockSpeed = 0;
            }
            destMastInit = false;
            if(overright){
                if(newAutoMasts==null){
                    this.autoMasts = new Hashtable<SignalMast, String>(0);
                } else {
                    this.autoMasts=newAutoMasts;
                }
            } else {
                if (newAutoMasts==null){
                    this.autoMasts = new Hashtable<SignalMast, String>(0);
                } else {
                    Enumeration<SignalMast> keys = newAutoMasts.keys();
                    while ( keys.hasMoreElements() )
                    {
                       SignalMast key = keys.nextElement();
                       this.autoMasts.put(key, newAutoMasts.get(key));
                    }
                }
            }
            //kick off the process to add back in signalmasts at crossings.
            for(int i = 0; i<blockInXings.size(); i++){
                blockInXings.get(i).addSignalMastLogic(source);
            }
            
            firePropertyChange("automasts", null, this.destination);
        }

        /**
         * Sets which sensors must be in a given state before our mast can be set.
         * @param sensors
         */
        void setSensors(Hashtable<NamedBeanHandle<Sensor>, Integer> sensors){
            if (this.sensors!=null){
                Enumeration<NamedBeanHandle<Sensor>> keys = this.sensors.keys();
                while ( keys.hasMoreElements() )
                {
                   Sensor key = keys.nextElement().getBean();
                   key.removePropertyChangeListener(propertySensorListener);
                }
            }
            destMastInit = false;

            if(sensors==null){
                this.sensors = new Hashtable<NamedBeanHandle<Sensor>, Integer>(0);
            } else {
                this.sensors=sensors;
            }
            firePropertyChange("sensors", null, this.destination);
            this.sensors = sensors;
        }

        void addSensor(NamedBeanHandle<Sensor> sen, int state){
            if(sensors.containsKey(sen))
                return;
            sen.getBean().addPropertyChangeListener(propertySensorListener);
            sensors.put(sen, state);
            firePropertyChange("sensors", null, this.destination);
        }
        
        void removeSensor(NamedBeanHandle<Sensor> sen){
            if(!sensors.containsKey(sen))
                return;
            sen.getBean().removePropertyChangeListener(propertySensorListener);
            sensors.remove(sen);
            firePropertyChange("sensors", null, this.destination);
        }

        ArrayList<Block> getBlocks(){
            ArrayList<Block> out = new ArrayList<Block>();
            Enumeration<Block> en = blocks.keys();
            while (en.hasMoreElements()) {
                out.add(en.nextElement());
            }
            return out;
        }
        
        ArrayList<Block> getAutoBlocks(){
            ArrayList<Block> out = new ArrayList<Block>();
            Set<Block> blockKeys = autoBlocks.keySet();
            //while ( blockKeys.hasMoreElements() )
            for(Block key:blockKeys)
            {
               //Block key = blockKeys.nextElement();
                out.add(key);
            }
            return out;
        }
        
        ArrayList<Turnout> getTurnouts(){
            ArrayList<Turnout> out = new ArrayList<Turnout>();
            Enumeration<NamedBeanHandle<Turnout>> en = turnouts.keys();
            while (en.hasMoreElements()) {
                out.add(en.nextElement().getBean());
            }
            return out;
        }
        
        ArrayList<NamedBeanHandle<Turnout>> getNamedTurnouts(){
            ArrayList<NamedBeanHandle<Turnout>> out = new ArrayList<NamedBeanHandle<Turnout>>();
            Enumeration<NamedBeanHandle<Turnout>> en = turnouts.keys();
            while (en.hasMoreElements()) {
                out.add(en.nextElement());
            }
            return out;
        }
        
        ArrayList<Turnout> getAutoTurnouts(){
            ArrayList<Turnout> out = new ArrayList<Turnout>();
            Enumeration<Turnout> en = autoTurnouts.keys();
            while (en.hasMoreElements()) {
                out.add(en.nextElement());
            }
            return out;
        }

        ArrayList<SignalMast> getSignalMasts(){
            ArrayList<SignalMast> out = new ArrayList<SignalMast>();
            Enumeration<SignalMast> en = masts.keys();
            while (en.hasMoreElements()) {
                out.add(en.nextElement());
            }
            return out;
        }
        
        ArrayList<SignalMast> getAutoSignalMasts(){
            ArrayList<SignalMast> out = new ArrayList<SignalMast>();
            Enumeration<SignalMast> en = autoMasts.keys();
            while (en.hasMoreElements()) {
                out.add(en.nextElement());
            }
            return out;
        }
        
        ArrayList<Sensor> getSensors(){
            ArrayList<Sensor> out = new ArrayList<Sensor>();
            Enumeration<NamedBeanHandle<Sensor>> en = sensors.keys();
            while (en.hasMoreElements()) {
                out.add(en.nextElement().getBean());
            }
            return out;
        }

        ArrayList<NamedBeanHandle<Sensor>> getNamedSensors(){
            ArrayList<NamedBeanHandle<Sensor>> out = new ArrayList<NamedBeanHandle<Sensor>>();
            Enumeration<NamedBeanHandle<Sensor>> en = sensors.keys();
            while (en.hasMoreElements()) {
                out.add(en.nextElement());
            }
            return out;
        }
        
        boolean isBlockIncluded(Block block){
            return blocks.containsKey(block);
        }
        
        boolean isAutoBlockIncluded(LayoutBlock block){
            if(block!=null)
                return autoBlocks.containsKey(block.getBlock());
            return false;
        }
        
        boolean isAutoBlockIncluded(Block block){
            return autoBlocks.containsKey(block);
        }
        
        boolean isBlockIncluded(LayoutBlock block){
            if(block!=null)
                return blocks.containsKey(block.getBlock());
            return false;
        }
        
        boolean isTurnoutIncluded(Turnout turnout){
            Enumeration<NamedBeanHandle<Turnout>> en = turnouts.keys();
            while (en.hasMoreElements()) {
                if(en.nextElement().getBean()==turnout)
                    return true;
            }
            return false;
        }
        
        boolean isSensorIncluded(Sensor sensor){
            Enumeration<NamedBeanHandle<Sensor>> en = sensors.keys();
            while (en.hasMoreElements()) {
                if(en.nextElement().getBean()==sensor)
                    return true;
            }
            return false;
        }
        
        boolean isSignalMastIncluded(SignalMast signal){
            return masts.containsKey(signal);
        }
        
        int getAutoBlockState(Block block){
            if(autoBlocks==null)
                return -1;
            return autoBlocks.get(block);
        }

        /*boolean isBlockManualAssigned(Block block){
            return true;
        }*/
        
        int getSensorState(Sensor sensor){
            if(sensors==null)
                return -1;
            Enumeration<NamedBeanHandle<Sensor>> en = sensors.keys();
            while (en.hasMoreElements()) {
                NamedBeanHandle<Sensor> namedSensor = en.nextElement();
                if (namedSensor.getBean()==sensor)
                    return sensors.get(namedSensor);
            }
            return -1;
        }
        
        int getTurnoutState(Turnout turnout){
            if(turnouts==null)
                return -1;
            Enumeration<NamedBeanHandle<Turnout>> en = turnouts.keys();
            while (en.hasMoreElements()) {
                NamedBeanHandle<Turnout> namedTurnout = en.nextElement();
                if (namedTurnout.getBean()==turnout)
                    return turnouts.get(namedTurnout);
            }
            return -1;
        }
        
        int getAutoTurnoutState(Turnout turnout){
            if(autoTurnouts==null)
                return -1;
            if(autoTurnouts.containsKey(turnout))
                return autoTurnouts.get(turnout);
            return -1;
        }

        String getSignalMastState(SignalMast mast){
            if(masts==null)
                return null;
            return masts.get(mast);
        }
        
        String getAutoSignalMastState(SignalMast mast){
            if(autoMasts==null)
                return null;
            return autoMasts.get(mast);
        }
        
        boolean inWait = false;
        
        /* 
         * Before going active or checking that we can go active, we will wait 500ms
         * for things to settle down to help prevent a race condition
         */
        void checkState(){
            if(disposed){
                log.error("checkState called even though this has been disposed of " + getSourceMast().getDisplayName());
                return;
            }
            
            if(!enable)
                return;
            if (inWait){
                return;
            }
            
            log.debug("check Signal Dest State called");
            inWait=true;

            Runnable r = new Runnable() {
              public void run() {
                try {
//                    log.debug("Wait started");
                    Thread.sleep(InstanceManager.signalMastLogicManagerInstance().getSignalLogicDelay());
//                    log.debug("wait is over");
                    inWait=false;
                    checkStateDetails();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    inWait=false;
                }
              }
            };
            thr = new Thread(r);
            /*try{
                thr.join();
            } catch (InterruptedException ex) {
    //            log.debug("interrupted at join " + ex);
                inWait=false;
            }*/
            thr.setName(getSourceMast().getDisplayName() + " " + destination.getDisplayName());
            thr.start();
        }
        
        Thread thr = null;
        
        void checkStateDetails() {
            turnoutThrown=false;
            permissiveBlock=false;
            if(disposed){
                log.error("checkStateDetails called even though this has been disposed of " + getSourceMast().getDisplayName() + " " + destination.getDisplayName());
                return;
            }
            if(inWait){
                log.error("checkStateDetails called while we are waiting for things to settle");
                return;
            }
            if(!enable){
                return;
            }
            log.debug("From " + getSourceMast().getDisplayName() + " to " + destination.getDisplayName() + " internal check state");
            active=false;
            if((useLayoutEditor) && (autoTurnouts.size()==0) && (autoBlocks.size()==0)){
                return;
            }
            boolean state = true;
            Enumeration<Turnout> keys =autoTurnouts.keys();
            while ( keys.hasMoreElements() )
            {
               Turnout key = keys.nextElement();
               if (key.getKnownState()!=autoTurnouts.get(key)){
                    if (key.getState()!=autoTurnouts.get(key)){
                        if (isTurnoutIncluded(key)){
                            if(key.getState()!=getTurnoutState(key)){
                                state=false;
                            } else if (key.getState()==Turnout.THROWN){
                                turnoutThrown=true;
                            }
                        }
                         else {
                            state = false;
                        }
                    }
               } else if (key.getState()==Turnout.THROWN){
                    turnoutThrown=true;
                }
            }
            
            Enumeration<NamedBeanHandle<Turnout>> turnoutKeys = turnouts.keys();
            while ( turnoutKeys.hasMoreElements() )
            {
               NamedBeanHandle<Turnout> namedTurnout = turnoutKeys.nextElement();
               Turnout key = namedTurnout.getBean();
               if (key.getKnownState()!=turnouts.get(namedTurnout))
                   state=false;
                else if (key.getState()==Turnout.THROWN){
                    turnoutThrown=true;
                }
            }

            Enumeration<SignalMast> mastKeys = autoMasts.keys();
            while ( mastKeys.hasMoreElements() )
            {
               SignalMast key = mastKeys.nextElement();
               if(log.isDebugEnabled())
                log.debug(key.getDisplayName() + " " + key.getAspect() + " " + autoMasts.get(key));
               if ((key.getAspect()!=null) && (!key.getAspect().equals(autoMasts.get(key)))){
                   if (masts.containsKey(key)){
                    //Basically if we have a blank aspect, we don't care about the state of the turnout
                        if(!masts.get(key).equals("")){
                            if(!key.getAspect().equals(masts.get(key))){
                                state=false;
                            }
                        }
                    } else {
                       state = false;
                    }
               }
            }
            
            mastKeys = masts.keys();
            while ( mastKeys.hasMoreElements() )
            {
                SignalMast key = mastKeys.nextElement();
                log.debug(key);
                if ((key.getAspect()==null) || (key.getAspect().equals(masts.get(key))))
                   state=false;
            }

            Enumeration<NamedBeanHandle<Sensor>> sensorKeys = sensors.keys();
            while ( sensorKeys.hasMoreElements() )
            {
               NamedBeanHandle<Sensor> namedSensor = sensorKeys.nextElement();
               Sensor key = namedSensor.getBean();
               if (key.getKnownState()!=sensors.get(namedSensor))
                   state=false;
            }
            Set<Block> blockAutoKeys = autoBlocks.keySet();
            //while ( blockKeys.hasMoreElements() )
            for(Block key:blockAutoKeys)
            {
               //Block key = blockKeys.nextElement();
               if(log.isDebugEnabled())
                    log.debug(key.getDisplayName() + " " + key.getState() + " " + autoBlocks.get(key));
               if (key.getState()!=autoBlocks.get(key)){
                    if (blocks.containsKey(key)){
                        if(blocks.get(key)!=0x03) {
                            if(key.getState()!=blocks.get(key)){
                                if(key.getState()==Block.OCCUPIED && key.getPermissiveWorking()){
                                    permissiveBlock=true;
                                } else {
                                    state=false;
                                }
                            }
                        }
                    } else {
                        if(key.getState()==Block.OCCUPIED && key.getPermissiveWorking()){
                            permissiveBlock=true;
                        } else {
                            state = false;
                        }
                    }
               }
            }

            Enumeration<Block> blockKeys = blocks.keys();
            while ( blockKeys.hasMoreElements() )
            {
                Block key = blockKeys.nextElement();
                if(blocks.get(key)!=0x03){
                    if (key.getState()!=blocks.get(key)) {
                        if(key.getState()==Block.OCCUPIED && key.getPermissiveWorking()){
                            permissiveBlock=true;
                        } else {
                            state=false;
                        }
                    }
                }
            }
            if (permissiveBlock){
                /*If a block has been found to be permissive, but the source signalmast
                does not support a call-on/permissive aspect then the route can not be set*/
                if(getSourceMast().getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.PERMISSIVE)==null)
                    state = false;
            }
            if(!state){
                turnoutThrown = false;
                permissiveBlock=false;
            }

            active=state;
            setSignalAppearance();
        }

        void initialise(){
            if ((destMastInit) || (disposed)) { return;}

            active=false;
            turnoutThrown=false;
            permissiveBlock=false;
            boolean routeclear = true;
            if((useLayoutEditor) && (autoTurnouts.size()==0) && (autoBlocks.size()==0) && (autoMasts.size()==0)){
                return;
            }
            
            calculateSpeed();
            
            Enumeration<Turnout> keys =autoTurnouts.keys();
            while ( keys.hasMoreElements() )
            {
               Turnout key = keys.nextElement();
               key.addPropertyChangeListener(propertyTurnoutListener);

               if (key.getKnownState()!=autoTurnouts.get(key)){
                    if (key.getState()!=autoTurnouts.get(key)){
                        if (isTurnoutIncluded(key)){
                            if(key.getState()!=getTurnoutState(key)){
                                routeclear=false;
                            } else if (key.getState()==Turnout.THROWN){
                                turnoutThrown=true;
                            }
                        } else {
                            routeclear = false;
                        }
                    }
               } else if (key.getState()==Turnout.THROWN){
                    turnoutThrown = true;
               }
            }

            Enumeration<NamedBeanHandle<Turnout>> turnoutKeys = turnouts.keys();
            while ( turnoutKeys.hasMoreElements() )
            {
               NamedBeanHandle<Turnout> namedTurnout = turnoutKeys.nextElement();
               Turnout key = namedTurnout.getBean();
               key.addPropertyChangeListener(propertyTurnoutListener, namedTurnout.getName(), "Signal Mast Logic:" + source.getDisplayName() + " to " + destination.getDisplayName());
               if (key.getKnownState()!=turnouts.get(namedTurnout))
                   routeclear=false;
                else if (key.getState()==Turnout.THROWN){
                    turnoutThrown=true;
                }
            }
            
            Enumeration<SignalMast> mastKeys = autoMasts.keys();
            while ( mastKeys.hasMoreElements() )
            {
               SignalMast key = mastKeys.nextElement();
               //log.debug(destination.getDisplayName() + " auto mast add list " + key.getDisplayName());
               key.addPropertyChangeListener(propertySignalMastListener);
               if (!key.getAspect().equals(autoMasts.get(key))){
                    if (masts.containsKey(key)){
                        if(!key.getAspect().equals(masts.get(key))){
                            routeclear = false;
                        }
                    } else {
                        routeclear = false;
                    }
                }
            }

            mastKeys = masts.keys();
            while ( mastKeys.hasMoreElements() )
            {
                SignalMast key = mastKeys.nextElement();
                key.addPropertyChangeListener(propertySignalMastListener);
                //log.debug(destination.getDisplayName() + " key asepct " + key.getAspect());
                //log.debug(destination.getDisplayName() + " key exepcted aspect " + masts.get(key));
                if ((key.getAspect()==null) || (!key.getAspect().equals(masts.get(key))))
                    routeclear=false;
            }

            Enumeration<NamedBeanHandle<Sensor>> sensorKeys = sensors.keys();
            while ( sensorKeys.hasMoreElements() )
            {
               NamedBeanHandle<Sensor> namedSensor = sensorKeys.nextElement();
               Sensor sensor = namedSensor.getBean();
               log.debug(source.getDisplayName() + " to " + destination.getDisplayName() + " Add change listener to sensor  " + namedSensor.getName());
               sensor.addPropertyChangeListener(propertySensorListener, namedSensor.getName(), "Signal Mast Logic:" + source.getDisplayName() + " to " + destination.getDisplayName());

               if (sensor.getKnownState()!=sensors.get(namedSensor))
                   routeclear = false;
            }

            Set<Block> autoBlockKeys = autoBlocks.keySet();
            for(Block key:autoBlockKeys)
            {
               //log.debug(destination.getDisplayName() + " auto block add list " + key.getDisplayName());
               key.addPropertyChangeListener(propertyBlockListener);
               if (key.getState()!=autoBlocks.get(key)){
                    if (blocks.containsKey(key)){
                        if(key.getState()!=blocks.get(key)){
                            if(key.getState()==Block.OCCUPIED && key.getPermissiveWorking()){
                                permissiveBlock=true;
                            } else {
                                routeclear=false;
                            }
                        }
                    } else {
                        if(key.getState()==Block.OCCUPIED && key.getPermissiveWorking()){
                            permissiveBlock=true;
                        } else {
                            routeclear = false;
                        }
                    }
                }
            }
            
            Enumeration<Block> blockKeys = blocks.keys();
            while ( blockKeys.hasMoreElements() )
            {
               Block key = blockKeys.nextElement();
               key.addPropertyChangeListener(propertyBlockListener);
               if (key.getState()!=blocks.get(key)){
                    if(key.getState()==Block.OCCUPIED && key.getPermissiveWorking()){
                        permissiveBlock=true;
                    } else {
                        routeclear=false;
                    }
                }
            }
            if (permissiveBlock){
                /*If a block has been found to be permissive, but the source signalmast
                does not support a call-on/permissive aspect then the route can not be set*/
                if(getSourceMast().getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.PERMISSIVE)==null)
                    routeclear = false;
            }
            if (routeclear){
                active=true;
                setSignalAppearance();
            } else {
                permissiveBlock=false;
                turnoutThrown =false;
            }
            destMastInit = true;
        }
        
        void useLayoutEditor(boolean boo) throws jmri.JmriException {
            if(log.isDebugEnabled())
                log.debug(destination.getDisplayName() + " use called " + boo + " " + useLayoutEditor);
            if (useLayoutEditor == boo)
                return;
            useLayoutEditor = boo;
            if ((boo) && (InstanceManager.layoutBlockManagerInstance().routingStablised())){
                try{
                    setupLayoutEditorDetails();
                } catch (jmri.JmriException e){
                    throw e;
                    //Considered normal if there is no vlaid path using the layout editor.
                }
            } else {
                destinationBlock= null;
                facingBlock = null;
                protectingBlock = null;
                setAutoBlocks(null);
                setAutoTurnouts(null);
            }
        }
        
        void useLayoutEditorDetails(boolean turnouts, boolean blocks) throws jmri.JmriException{
            if(log.isDebugEnabled())
                log.debug(destination.getDisplayName() + " use layout editor details called " + useLayoutEditor);
            useLayoutEditorTurnouts=turnouts;
            useLayoutEditorBlocks=blocks;
            if((useLayoutEditor) && (InstanceManager.layoutBlockManagerInstance().routingStablised())){
                try{
                    setupLayoutEditorDetails();
                } catch (jmri.JmriException e){
                    throw e;
                    //Considered normal if there is no valid path using the layout editor.
                }
            }
        }
        
        void setupLayoutEditorDetails() throws jmri.JmriException{
            if(log.isDebugEnabled())
                log.debug(useLayoutEditor + " " + disposed);
            if((!useLayoutEditor) || (disposed))
                return;
            if ((destinationBlock!=null) && (log.isDebugEnabled()))
                log.debug(destination.getDisplayName() + " Set use layout editor");
            ArrayList<LayoutEditor> layout = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
            // We don't care which layout editor panel the signalmast is on, just so long as
            // as the routing is done via layout blocks
            log.debug(layout.size());
            LayoutBlock remoteProtectingBlock = null;
            for(int i = 0; i<layout.size(); i++){
                if(log.isDebugEnabled())
                    log.debug(destination.getDisplayName() + " Layout name " + layout.get(i).getLayoutName());
                if (facingBlock==null){
                    facingBlock = InstanceManager.layoutBlockManagerInstance().getFacingBlockByMast(getSourceMast().getUserName(), layout.get(i));
                    if (facingBlock==null)
                        facingBlock = InstanceManager.layoutBlockManagerInstance().getFacingBlockByMast(getSourceMast().getSystemName(), layout.get(i));
                }
                if (protectingBlock==null){
                    protectingBlock = InstanceManager.layoutBlockManagerInstance().getProtectedBlockByMast(getSourceMast().getUserName(), layout.get(i));
                    if (protectingBlock==null)
                        protectingBlock = InstanceManager.layoutBlockManagerInstance().getProtectedBlockByMast(getSourceMast().getSystemName(), layout.get(i));
                }
                if(destinationBlock==null){
                    destinationBlock = InstanceManager.layoutBlockManagerInstance().getFacingBlockByMast(destination.getUserName(), layout.get(i));
                    if (destinationBlock==null)
                        destinationBlock = InstanceManager.layoutBlockManagerInstance().getFacingBlockByMast(destination.getSystemName(), layout.get(i));
                }
                if(remoteProtectingBlock==null){
                    remoteProtectingBlock = InstanceManager.layoutBlockManagerInstance().getProtectedBlockByMast(destination.getUserName(), layout.get(i));
                    if(remoteProtectingBlock==null)
                        remoteProtectingBlock = InstanceManager.layoutBlockManagerInstance().getProtectedBlockByMast(destination.getSystemName(), layout.get(i));
                }
            }
            //At this point if we are not using the layout editor turnout or block
            //details then there is no point in trying to gather them
            if((!useLayoutEditorTurnouts) && (!useLayoutEditorBlocks))
                return;
            try {
                if(!InstanceManager.layoutBlockManagerInstance().getLayoutBlockConnectivityTools().checkValidDest(facingBlock, protectingBlock, destinationBlock, remoteProtectingBlock))
                    throw new jmri.JmriException("Path not valid");
            } catch (jmri.JmriException e){
                throw e;
            }
            if(log.isDebugEnabled()){
                log.debug(destination.getDisplayName() + " face " + facingBlock);
                log.debug(destination.getDisplayName() + " prot " + protectingBlock);
                log.debug(destination.getDisplayName() + " dest " + destinationBlock);
            }
            
            if(destinationBlock!=null && protectingBlock !=null && facingBlock !=null){
                setAutoMasts(null, true);
                if(log.isDebugEnabled()){
                    log.debug(destination.getDisplayName() + " face " + facingBlock.getDisplayName());
                    log.debug(destination.getDisplayName() + " prot " + protectingBlock.getDisplayName());
                    log.debug(destination.getDisplayName() + " dest " + destinationBlock.getDisplayName());
                }
                LinkedHashMap<Block, Integer> block = new LinkedHashMap<Block, Integer>();
                Hashtable<Turnout, Integer> turnoutSettings = new Hashtable<Turnout, Integer>();
                
                try {
                    ArrayList<LayoutBlock> lblks = InstanceManager.layoutBlockManagerInstance().getLayoutBlockConnectivityTools().getLayoutBlocks(facingBlock, destinationBlock, protectingBlock, true, jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools.MASTTOMAST);                    
                    ConnectivityUtil connection;
                    ArrayList<LayoutTurnout> turnoutlist;
                    ArrayList<Integer> throwlist;

                    for (int i = 0; i<lblks.size(); i++){
                        if(log.isDebugEnabled())
                            log.debug(lblks.get(i).getDisplayName());
                        block.put(lblks.get(i).getBlock(), Block.UNOCCUPIED);
                        if ((i>0)) {
                            int nxtBlk = i+1;
                            int preBlk = i-1;
                            if (i==lblks.size()-1){
                                nxtBlk = i;
                            } else if (i==0){
                                preBlk=i;
                            }
                            //We use the best connectivity for the current block;
                            connection = new ConnectivityUtil(lblks.get(i).getMaxConnectedPanel());
                            turnoutlist=connection.getTurnoutList(lblks.get(i).getBlock(), lblks.get(preBlk).getBlock(), lblks.get(nxtBlk).getBlock());
                            throwlist=connection.getTurnoutSettingList();
                            for (int x=0; x<turnoutlist.size(); x++){
                                if(turnoutlist.get(x) instanceof LayoutSlip){
                                    int slipState = throwlist.get(x);
                                    LayoutSlip ls = (LayoutSlip)turnoutlist.get(x);
                                    int taState = ls.getTurnoutState(slipState);
                                    turnoutSettings.put(ls.getTurnout(), taState);

                                    int tbState = ls.getTurnoutBState(slipState);
                                    turnoutSettings.put(ls.getTurnoutB(), tbState);
                                } else {
                                    String t = turnoutlist.get(x).getTurnoutName();
                                    Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(t);
                                    if(log.isDebugEnabled()){
                                        if ((turnoutlist.get(x).getTurnoutType()<=3) && (!turnoutlist.get(x).getBlockName().equals(""))){
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
                                }
                            }
                        }
                    }
                    
                    for(int i = 0; i<blockInXings.size(); i++){
                        blockInXings.get(i).removeSignalMastLogic(source);
                    }
                    blockInXings = new ArrayList<LevelXing>(0);
                    xingAutoBlocks = new ArrayList<Block>(0);
                    for(int i = 0; i<layout.size(); i++){
                        LayoutEditor lay = layout.get(i);
                        for(int j = 0; j<lay.xingList.size(); j++){
                            //Looking for a crossing that both layout blocks defined and they are individual.
                            if((lay.xingList.get(j).getLayoutBlockAC()!=null) && (lay.xingList.get(j).getLayoutBlockBD()!=null) && (lay.xingList.get(j).getLayoutBlockAC()!=lay.xingList.get(j).getLayoutBlockBD())){
                                if(lblks.contains(lay.xingList.get(j).getLayoutBlockAC())){
                                    block.put(lay.xingList.get(j).getLayoutBlockBD().getBlock(), Block.UNOCCUPIED);
                                    xingAutoBlocks.add(lay.xingList.get(j).getLayoutBlockBD().getBlock());
                                    blockInXings.add(lay.xingList.get(j));
                                } else if (lblks.contains(lay.xingList.get(j).getLayoutBlockBD())){
                                    block.put(lay.xingList.get(j).getLayoutBlockAC().getBlock(), Block.UNOCCUPIED);
                                    xingAutoBlocks.add(lay.xingList.get(j).getLayoutBlockAC().getBlock());
                                    blockInXings.add(lay.xingList.get(j));
                                }
                            }
                        }
                    }
                    if(log.isDebugEnabled())
                        log.debug(block.size());
                    if(useLayoutEditorBlocks)
                        setAutoBlocks(block);
                    else
                        setAutoBlocks(null);
                    if(useLayoutEditorTurnouts)
                        setAutoTurnouts(turnoutSettings);
                    else
                        setAutoTurnouts(null);
                } catch (jmri.JmriException e){
                    log.debug(destination.getDisplayName() + " Valid route not found from " + facingBlock.getDisplayName() + " to " + destinationBlock.getDisplayName());
                    log.debug(e.toString());
                    throw e;
                }
                setupAutoSignalMast(null, false);
            }
            initialise();
        }
        /*
         * The generation of auto signalmast, looks through all the other logics
         * to see if there are any blocks that are in common and thus will add
         * the other signalmast protecting that block.
         */
        void setupAutoSignalMast(jmri.SignalMastLogic sml, boolean overright){
            if(!allowAutoSignalMastGeneration)
                return;
            ArrayList<jmri.SignalMastLogic> smlList = InstanceManager.signalMastLogicManagerInstance().getLogicsByDestination(destination);
            ArrayList<Block> allBlock = new ArrayList<Block>();
            
            Enumeration<Block> keys = blocks.keys();
            while ( keys.hasMoreElements() )
            {
                Block key = keys.nextElement();
                allBlock.add(key);
            }
            
            Set<Block> blockKeys = autoBlocks.keySet();
            for(Block key:blockKeys)
            {
                if(!allBlock.contains(key))
                    allBlock.add(key);
            }
             Hashtable<SignalMast, String> masts;
            if(sml!=null){
                masts = autoMasts;
                if(sml.areBlocksIncluded(allBlock)){
                    SignalMast mast = sml.getSourceMast();
                    String danger =  mast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER);
                    masts.put(mast, danger);
                } else{
                    //No change so will leave.
                    return;
                }
            } else {
                masts = new Hashtable<SignalMast, String>();
                for(int i = 0; i<smlList.size(); i++){
                    if(smlList.get(i).areBlocksIncluded(allBlock)){
                        SignalMast mast = smlList.get(i).getSourceMast();
                        String danger =  mast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER);
                        masts.put(mast, danger);
                    }
                }
            }
            setAutoMasts(masts, overright);
        }
        
        void addAutoSignalMast(SignalMast mast){
            if(log.isDebugEnabled())
                log.debug(destination.getDisplayName() + " add mast to auto list " + mast);
            String danger = mast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER);
            this.autoMasts.put(mast, danger);
            if (destMastInit)
                mast.addPropertyChangeListener(propertySignalMastListener);
            firePropertyChange("automasts", null, this.destination);
        }
        
        void removeAutoSignalMast(SignalMast mast){
            this.autoMasts.remove(mast);
            if(destMastInit)
                mast.removePropertyChangeListener(propertySignalMastListener);
            firePropertyChange("automasts", this.destination, null);
        }
        
        boolean useLayoutEditor(){
            return useLayoutEditor;
        }
        
        boolean useLayoutEditorBlocks(){
            return useLayoutEditorBlocks;
        }
        
        boolean useLayoutEditorTurnouts(){
            return useLayoutEditorTurnouts;
        }
        
        boolean allowAutoSignalMastGeneration = false;
        
        boolean allowAutoSignalMastGen(){
            return allowAutoSignalMastGeneration;
        }
        
        void allowAutoSignalMastGen(boolean gen) { 
            if(allowAutoSignalMastGeneration==gen)
                return;
            allowAutoSignalMastGeneration = gen;
        }
        
        void dispose(){
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
        
        void lockTurnouts(){
            //We do not allow the turnouts to be locked, if we are disposed the logic, 
            //the logic is not active, or if we do not allow the turnouts to be locked
            if((disposed) || (!lockTurnouts) || (!active))
                return;
                
            
            Enumeration<NamedBeanHandle<Turnout>> turnoutKeys = turnouts.keys();
            while ( turnoutKeys.hasMoreElements() )
            {
               NamedBeanHandle<Turnout> namedTurnout = turnoutKeys.nextElement();
               Turnout key = namedTurnout.getBean();
               key.setLocked(Turnout.CABLOCKOUT+Turnout.PUSHBUTTONLOCKOUT, true);
            }
            Enumeration<Turnout> keys = autoTurnouts.keys();
            while ( keys.hasMoreElements() )
            {
                Turnout key = keys.nextElement();
                key.setLocked(Turnout.CABLOCKOUT+Turnout.PUSHBUTTONLOCKOUT, true);
            }
        }
        
        void clearTurnoutLock(){
            //We do not allow the turnout lock to be cleared, if we are not active, 
            //and the lock flag has not been set.
            if((!lockTurnouts) && (!active))
                return;
                
            Enumeration<Turnout> keys = autoTurnouts.keys();
            while ( keys.hasMoreElements() )
            {
                Turnout key = keys.nextElement();
               key.setLocked(Turnout.CABLOCKOUT+Turnout.PUSHBUTTONLOCKOUT, false);
            }
            
            Enumeration<NamedBeanHandle<Turnout>> turnoutKeys = turnouts.keys();
            while ( turnoutKeys.hasMoreElements() )
            {
               NamedBeanHandle<Turnout> namedTurnout = turnoutKeys.nextElement();
               Turnout key = namedTurnout.getBean();
                key.setLocked(Turnout.CABLOCKOUT+Turnout.PUSHBUTTONLOCKOUT, false);
            }
        }
        
        protected void calculateSpeed(){
            if(log.isDebugEnabled())
                log.debug(destination.getDisplayName() + " calculate the speed setting for this logic ie what the signalmast will display");
            minimumBlockSpeed=0.0f;
            Enumeration<Turnout> keys = autoTurnouts.keys();
            while ( keys.hasMoreElements() )
            {
               Turnout key = keys.nextElement();
               if(log.isDebugEnabled())
                    log.debug(destination.getDisplayName() + " turnout " + key.getDisplayName());
               //if(!turnouts.containsKey(key)){
                for(NamedBeanHandle<Turnout> nbTurn : turnouts.keySet()){
                    if(nbTurn.getBean().equals(key)){
                       if (key.getState()==Turnout.CLOSED){
                            if (((key.getStraightLimit()<minimumBlockSpeed) || (minimumBlockSpeed==0)) && (key.getStraightLimit()!=-1)){
                                minimumBlockSpeed = key.getStraightLimit();
                                if(log.isDebugEnabled())
                                    log.debug(destination.getDisplayName() + " turnout " + key.getDisplayName() + " set speed to " + minimumBlockSpeed);
                            }
                        } else {
                            if (((key.getDivergingLimit()<minimumBlockSpeed) || (minimumBlockSpeed==0)) && (key.getDivergingLimit()!=-1)){
                                minimumBlockSpeed = key.getDivergingLimit();
                                if(log.isDebugEnabled())
                                    log.debug(destination.getDisplayName() + " turnout " + key.getDisplayName() + " set speed to " + minimumBlockSpeed);
                            }
                        }
                    }
                }
            }
            Enumeration<NamedBeanHandle<Turnout>> turnoutKeys = turnouts.keys();
            while ( turnoutKeys.hasMoreElements() )
            {
               NamedBeanHandle<Turnout> namedTurnout = turnoutKeys.nextElement();
               Turnout key = namedTurnout.getBean();
               if (key.getState()==Turnout.CLOSED){
                    if (((key.getStraightLimit()<minimumBlockSpeed) || (minimumBlockSpeed==0)) && (key.getStraightLimit()!=-1)){
                        minimumBlockSpeed = key.getStraightLimit();
                        if(log.isDebugEnabled())
                            log.debug(destination.getDisplayName() + " turnout " + key.getDisplayName() + " set speed to " + minimumBlockSpeed);
                    }
                } else if (key.getState()==Turnout.THROWN) {
                    if (((key.getDivergingLimit()<minimumBlockSpeed) || (minimumBlockSpeed==0)) && (key.getDivergingLimit()!=-1)){
                        minimumBlockSpeed = key.getDivergingLimit();
                        if(log.isDebugEnabled())
                            log.debug(destination.getDisplayName() + " turnout " + key.getDisplayName() + " set speed to " + minimumBlockSpeed);
                    }
                }
            }
            
            Set<Block> autoBlockKeys = autoBlocks.keySet();
            //while ( blockKeys.hasMoreElements() )
            for(Block key:autoBlockKeys)
            {
               //Block key = blockKeys.nextElement();
               log.debug(destination.getDisplayName() + " auto block add list " + key.getDisplayName());
               if(!blocks.containsKey(key)){
                   if (((key.getSpeedLimit()<minimumBlockSpeed) || (minimumBlockSpeed==0)) && (key.getSpeedLimit()!=-1)){
                       minimumBlockSpeed=key.getSpeedLimit();
                       if(log.isDebugEnabled())
                            log.debug(destination.getDisplayName() + " block " + key.getDisplayName() + " set speed to " + minimumBlockSpeed);
                    }
                }
            }
            Enumeration<Block> blockKeys = blocks.keys();
            while ( blockKeys.hasMoreElements() )
            {
               Block key = blockKeys.nextElement();
               if (((key.getSpeedLimit()<minimumBlockSpeed) || (minimumBlockSpeed==0)) && (key.getSpeedLimit()!=-1)){
                   if(log.isDebugEnabled())
                        log.debug(destination.getDisplayName() + " block " + key.getDisplayName() + " set speed to " + minimumBlockSpeed);
                    minimumBlockSpeed=key.getSpeedLimit();
               }
            }
            /*if(minimumBlockSpeed==-0.1f)
                minimumBlockSpeed==0.0f;*/
        }
        
        protected PropertyChangeListener propertySensorListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                Sensor sen = (Sensor) e.getSource();
                log.debug(source.getDisplayName() + " to " + destination.getDisplayName() + " destination sensor "+ sen.getDisplayName() + "trigger " + e.getPropertyName());
                if (e.getPropertyName().equals("KnownState")) {
                    int now = ((Integer) e.getNewValue()).intValue();
                    log.debug("current value " + now + " value we want " + getSensorState(sen));
                    if (IsSensorIncluded(sen) && getSensorState(sen)!=now){
                        //if(log.isDebugEnabled())
                            log.debug("Sensor " + sen.getDisplayName() + " caused the signalmast to be set to danger");
                        //getSourceMast().setAspect(stopAspect);
                        if (active==true){
                            active=false;
                            setSignalAppearance();
                        }
                    } else if (getSensorState(sen)==now) {
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
                    if(isTurnoutIncluded(turn)){
                        if (getTurnoutState(turn)!=now){
                            if(log.isDebugEnabled()){
                                log.debug("Turnout " + turn.getDisplayName() + " caused the signalmast to be set");
                                log.debug("From " + getSourceMast().getDisplayName() + " to " + destination.getDisplayName() + " Turnout " + turn.getDisplayName() + " caused the signalmast to be set to danger");
                            }
                            if (active==true){
                                active=false;
                                setSignalAppearance();
                            }
                        } else {
                            if(log.isDebugEnabled())
                                log.debug(destination.getDisplayName() + " turnout " + turn.getDisplayName() + " triggers a calculation of change");
                            checkState();
                        }
                    } else if(autoTurnouts.containsKey(turn)){
                        if (getAutoTurnoutState(turn)!=now){
                            if(log.isDebugEnabled()){
                                log.debug("Turnout " + turn.getDisplayName() + " auto caused the signalmast to be set");
                                log.debug("From " + getSourceMast().getDisplayName() + " to" + destination.getDisplayName() + " Auto Turnout " + turn.getDisplayName() + " auto caused the signalmast to be set to danger");
                            }
                            if (active==true){
                                active=false;
                                setSignalAppearance();
                            }
                        } else {
                            if(log.isDebugEnabled())
                                log.debug("From " + getSourceMast().getDisplayName() + " to " + destination.getDisplayName() + " turnout " + turn.getDisplayName() + " triggers a calculation of change");
                            checkState();
                        }
                    } 

                } else if ((e.getPropertyName().equals("TurnoutStraightSpeedChange")) || (e.getPropertyName().equals("TurnoutDivergingSpeedChange"))){
                    calculateSpeed();
                }
            }
        };

        protected PropertyChangeListener propertyBlockListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                Block block = (Block) e.getSource();
                if(log.isDebugEnabled())
                    log.debug(destination.getDisplayName() + " destination block "+ block.getDisplayName() + " trigger " +e.getPropertyName() + " " + e.getNewValue());
                if (e.getPropertyName().equals("state")) {
                    int now = ((Integer) e.getNewValue()).intValue();
                    if(log.isDebugEnabled()){
                        log.debug(blocks.containsKey(block));
                        log.debug(autoBlocks.containsKey(block));
                    }
                    if (blocks.containsKey(block)){
                        if(log.isDebugEnabled()){
                            log.debug(destination.getDisplayName() + " in manual block");
                            log.debug(getBlockState(block) + "  " + now);
                        }
                        /*if (getBlockState(block) != now){
                            if(log.isDebugEnabled()){
                                log.debug("Block " + block.getDisplayName() + " caused the signalmast to be set");
                                log.debug(destination.getDisplayName() + " Block " + block.getDisplayName() + " caused the signalmast to be set");
                            }
                            if (active==true){
                                active=false;
                                setSignalAppearance();
                            }
                        } else {
                            if(log.isDebugEnabled())
                                log.debug(destination.getDisplayName() + " block " + block.getDisplayName() + " triggers a calculation of change");
                            checkState();
                        }*/
                        checkState();
                    } else if (autoBlocks.containsKey(block)){
                        if(log.isDebugEnabled()){
                            log.debug(destination.getDisplayName() + " in auto block");
                            log.debug(getAutoBlockState(block) + "  " + now);
                        }
                        /*if (getAutoBlockState(block) != now){
                            if(log.isDebugEnabled()){
                                log.debug("Block " + block.getDisplayName() + " caused the signalmast to be set");
                                log.debug(destination.getDisplayName() + " auto Block " + block.getDisplayName() + " caused the signalmast to be set");
                            }
                            if (active==true){
                                active=false;
                                setSignalAppearance();

                            }
                        } else {
                            if(log.isDebugEnabled())
                                log.debug(destination.getDisplayName() + " auto block " + block.getDisplayName() + " triggers a calculation of change");
                            checkState();
                        }*/
                        checkState();
                    } else if(log.isDebugEnabled()) {
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
                if(log.isDebugEnabled())
                    log.debug(destination.getDisplayName() + " signalmast change " + mast.getDisplayName() + " " + e.getPropertyName());
             //   log.debug(destination.getDisplayName() + " destination sensor "+ sen.getDisplayName() + "trigger");
                if (e.getPropertyName().equals("Aspect")) {
                    
                    String now = ((String) e.getNewValue());
                    if(log.isDebugEnabled())
                        log.debug(destination.getDisplayName() + " match property " + now);
                    if(masts.containsKey(mast)){
                        if (!now.equals(getSignalMastState(mast))){
                            if(log.isDebugEnabled()) {
                                log.debug(destination.getDisplayName() + " in mast list SignalMast " + mast.getDisplayName() + " caused the signalmast to be set");
                                log.debug("SignalMast " + mast.getDisplayName() + " caused the signalmast to be set");
                            }
                            if (active){
                                active=false;
                                setSignalAppearance();
                            }
                        } else {
                            if(log.isDebugEnabled())
                                log.debug(destination.getDisplayName() + " in mast list signalmast change");
                            checkState();
                        }
                    } else if (autoMasts.containsKey(mast)){
                        if (!now.equals(getAutoSignalMastState(mast))){
                            if(log.isDebugEnabled()){
                                log.debug("SignalMast " + mast.getDisplayName() + " caused the signalmast to be set");
                                log.debug(destination.getDisplayName() + " in auto mast list SignalMast " + mast.getDisplayName() + " caused the signalmast to be set");
                            }
                            if (active){
                                active=false;
                                setSignalAppearance();
                            }
                        } else {
                            if(log.isDebugEnabled())
                                log.debug(destination.getDisplayName() + " in auto mast list signalmast change");
                            checkState();
                        }
                    }
                }
            }
        };
        
        
       /* Code currently not used commented out to remove unused error
       protected PropertyChangeListener propertySignalMastLogicManagerListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if(log.isDebugEnabled())
                    log.debug(destination.getDisplayName() + " Signal Mast Manager Listener");
             //   log.debug(destination.getDisplayName() + " destination sensor "+ sen.getDisplayName() + "trigger");
                if (e.getPropertyName().equals("DestinationAdded")) {
                    SignalMast dest = ((SignalMast) e.getNewValue());
                    if(dest==destination){
                        jmri.SignalMastLogic sml = ((jmri.SignalMastLogic) e.getOldValue());
                        setupAutoSignalMast(sml, false);
                    }
                }
            }
        };*/
        
        protected boolean IsSensorIncluded(Sensor sen){
            Enumeration<NamedBeanHandle<Sensor>> sensorKeys = sensors.keys();
            while ( sensorKeys.hasMoreElements() )
            {
                NamedBeanHandle<Sensor> namedSensor = sensorKeys.nextElement();
                if(namedSensor.getBean()==sen) {
                    return true;
                }
            }
            return false;
        }
    
    }
    
    
    //This is the listener on the destination signalMast
    protected PropertyChangeListener propertyDestinationMastListener = new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent e) {
                SignalMast mast = (SignalMast) e.getSource();
                if (mast==destination){
                    if(log.isDebugEnabled())
                        log.debug("destination mast change " +mast.getDisplayName());
                    setSignalAppearance();
                }
            }
    };
    
    //This is the listener on the source signalMast
    protected PropertyChangeListener propertySourceMastListener = new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent e) {
                SignalMast mast = (SignalMast) e.getSource();
                if ((mast==source) && (e.getPropertyName().equals("Held"))){
                    if(log.isDebugEnabled())
                        log.debug("source mast change " +mast.getDisplayName() + " " + e.getPropertyName());
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
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    public void dispose(){
        disposing=true;
        getSourceMast().removePropertyChangeListener(propertySourceMastListener);
        Enumeration<SignalMast> en = destList.keys();
        while (en.hasMoreElements()) {
            SignalMast dm = en.nextElement();
            destList.get(dm).dispose();
            //InstanceManager.signalMastLogicManagerInstance().removeDestinationMastToLogic(this, dm);
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalMastLogic.class.getName());
}