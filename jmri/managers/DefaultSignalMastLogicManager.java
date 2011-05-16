package jmri.managers;

import jmri.*;
import jmri.SignalMastLogic;
import jmri.implementation.DefaultSignalMastLogic;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.util.Enumeration;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

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
 * @version			$Revision: 1.2 $
 */

public class DefaultSignalMastLogicManager implements jmri.SignalMastLogicManager {

    public DefaultSignalMastLogicManager(){
        registerSelf();
        InstanceManager.layoutBlockManagerInstance().addPropertyChangeListener(propertyBlockManagerListener);
    }
    
    private static jmri.implementation.SignalSpeedMap _speedMap;
    
    public final static jmri.implementation.SignalSpeedMap getSpeedMap() {
        if (_speedMap==null) {
            _speedMap = jmri.implementation.SignalSpeedMap.getMap();
        }
        return _speedMap;
    }

    public SignalMastLogic getSignalMastLogic(SignalMast source){
        for(int i = 0; i <signalMastLogic.size(); i++){
            if (signalMastLogic.get(i).getSourceMast()==source)
                return signalMastLogic.get(i);
        }
        return null;
    }

    public SignalMastLogic newSignalMastLogic(SignalMast source){
        for(int i = 0; i <signalMastLogic.size(); i++){
            if (signalMastLogic.get(i).getSourceMast()==source)
                return signalMastLogic.get(i);
        }
        SignalMastLogic logic = new DefaultSignalMastLogic(source);
        signalMastLogic.add(logic);
        firePropertyChange("length", null, Integer.valueOf(signalMastLogic.size()));
        return logic;
    }

    ArrayList<SignalMastLogic> signalMastLogic = new ArrayList<SignalMastLogic>();
    
    Hashtable<SignalMast, ArrayList<SignalMastLogic>> destLocationList = new Hashtable<SignalMast, ArrayList<SignalMastLogic>>();
    
    public void addDestinationMastToLogic(SignalMastLogic src, SignalMast destination){
        if(!destLocationList.contains(destination)){
            destLocationList.put(destination, new ArrayList<SignalMastLogic>());
        }
        ArrayList<SignalMastLogic> a = destLocationList.get(destination);
        if(!a.contains(src)){
            a.add(src);
            firePropertyChange("DestinationAdded", src, destination);
        }
    }
    
    public void removeDestinationMastToLogic(SignalMastLogic src, SignalMast destination){
        if(!destLocationList.contains(destination))
            return;
        ArrayList a = destLocationList.get(destination);
        int loc = a.indexOf(src);
        if(loc!=-1){
            //Remove the mast logic from the list
            a.remove(loc);
            firePropertyChange("DestinationRemoved", src, destination);
        }
        if(a.isEmpty()){
            // if the mast logic contains no entries then we can remove it.
            destLocationList.remove(destination);
        }
    }
    
    /**
    * Gather a list of all the signal mast logics, by destination signal mast
    */
    
    public ArrayList<SignalMastLogic> getLogicsByDestination(SignalMast destination){
        if(!destLocationList.contains(destination))
            return new ArrayList<SignalMastLogic>();
        return destLocationList.get(destination);
    }
    
    /**
     * Returns an arraylist of signalmastlogic
     * @return An ArrayList of SignalMast logics
     */
    public ArrayList<SignalMastLogic> getSignalMastLogicList() {
        return signalMastLogic;
    }

    /**
     * Remove a destination mast from the signalmast logic
     * @param sml The signalmast logic of the source signal
     * @param dest The destination mast
     */
    public void removeSignalMastLogic(SignalMastLogic sml, SignalMast dest){
        if(sml.removeDestination(dest)){
            removeSignalMastLogic(sml);
        }
    }
    
    /**
    * Completely remove the signalmast logic.
    */
    public void removeSignalMastLogic(SignalMastLogic sml){
        //Need to provide a method to delete and dispose.
        sml.dispose();
        
        signalMastLogic.remove(sml);
        firePropertyChange("length", null, Integer.valueOf(signalMastLogic.size()));
    }

    /**
     * By default, register this manager to store as configuration
     * information.  Override to change that.
     **/
    protected void registerSelf() {
         if (InstanceManager.configureManagerInstance()!=null) {
            InstanceManager.configureManagerInstance().registerConfig(this);
            log.debug("register for config");
        }
    }

    // abstract methods to be extended by subclasses
    // to free resources when no longer used
    public void dispose() {
        if (InstanceManager.configureManagerInstance()!= null)
            InstanceManager.configureManagerInstance().deregister(this);
        signalMastLogic.clear();
    }

    /**
     * Used to initialise all the signalmast logics. primarily used after loading.
     */
    public void initialise(){
        for(int i = 0; i <signalMastLogic.size(); i++){
            signalMastLogic.get(i).initialise();
        }
    }

    public char systemLetter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getSystemPrefix() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public char typeLetter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String makeSystemName(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getSystemNameArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<String> getSystemNameList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    public void register(NamedBean n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void deregister(NamedBean n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    long signalLogicDelay = 500L;
    public long getSignalLogicDelay(){ return signalLogicDelay; }
    public void setSignalLogicDelay(long l){ signalLogicDelay=l; }

    
    protected PropertyChangeListener propertyBlockManagerListener = new PropertyChangeListener(){
        public void propertyChange(PropertyChangeEvent e) {
            if(e.getPropertyName().equals("topology")){
                //boolean newValue = new Boolean.parseBoolean(String.valueOf(e.getNewValue()));
                boolean newValue = (Boolean) e.getNewValue();
                if(newValue){
                    for(int i = 0; i <signalMastLogic.size(); i++){
                        signalMastLogic.get(i).setupLayoutEditorDetails();
                    }
                    if(runWhenStablised){
                        try {
                            automaticallyDiscoverSignallingPairs();
                        } catch (JmriException je){
                            //Considered normal if routing not enabled
                        }
                    }
                }
            }
        }
    };
    
    boolean runWhenStablised = false;
    
    /* The following is used in conjunction with the layout editor to discover 
    signalmast source/destination pairs, these tools have yet to be initialised
    */
    
    /**
    * Discover valid destination signalmasts for a given source signal on a 
    * given layout editor panel.
    * @param source Source SignalMast
    * @param layout Layout Editor panel to check.
    */
    public void discoverSignallingDest(SignalMast source, LayoutEditor layout) throws JmriException{
        validPaths = new Hashtable<SignalMast, ArrayList<SignalMast>>();
        jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        if(!lbm.isAdvancedRoutingEnabled()){
            //log.debug("advanced routing not enabled");
            throw new JmriException("advanced routing not enabled");
        }
        if(!lbm.routingStablised()){
            throw new JmriException("routing not stablised");
        }
        LayoutBlock lFacing = lbm.getFacingBlockByMast(source, layout);
        LayoutBlock lProtecting = lbm.getProtectedBlockByMast(source, layout);
        try{
            discoverSignallingDest(source, lProtecting, lFacing);
        } catch (JmriException e){
            throw e;
        }
        
        Enumeration<SignalMast> en = validPaths.keys();
        while (en.hasMoreElements()) {
            SignalMast key = en.nextElement();
            SignalMastLogic sml = getSignalMastLogic(key);
            if(sml==null){
                sml=newSignalMastLogic(key);
            }
            ArrayList<SignalMast> validDestMast = validPaths.get(key);
            for(int i = 0; i<validDestMast.size(); i++){
                try{
                    sml.setDestinationMast(validDestMast.get(i));
                    sml.useLayoutEditorDetails(true, true, validDestMast.get(i));
                    sml.useLayoutEditor(true, validDestMast.get(i));
                } catch (JmriException e){
                    //log.debug("We shouldn't get an exception here");
                    throw e;
                }
            }
        }
    }
    
    protected void discoverSignallingDest(SignalMast source, LayoutBlock lProtecting, LayoutBlock lFacing) throws JmriException{
        jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        if(!lbm.isAdvancedRoutingEnabled()){
            //log.debug("advanced routing not enabled");
            throw new JmriException("advanced routing not enabled");
        }
        if(!lbm.routingStablised()){
            throw new JmriException("routing not stablised");
        }
        if(!validPaths.contains(source))
            validPaths.put(source, new ArrayList<SignalMast>());
        ArrayList<SignalMast> validDestMast = validPaths.get(source);
        ArrayList<FacingProtecting> signalMastList = generateBlocksWithSignals();
        SignalMastLogic sml = getSignalMastLogic(source);
        for (int j = 0; j<signalMastList.size(); j++){
            //firePropertyChange("autoGenerateState", null, location);
            if (signalMastList.get(j).getMast()!=source){
                boolean alreadyExist = false;
                SignalMast destMast = signalMastList.get(j).getMast();
                if(sml!=null){
                    alreadyExist = sml.isDestinationValid(destMast);
                }
                if(!alreadyExist){
                    if(log.isDebugEnabled())
                        log.debug("looking for pair " + source.getDisplayName() + " " + destMast.getDisplayName());
                    try {
                        if(checkValidDest(source, signalMastList.get(j).getMast())){
                            LayoutBlock ldstBlock = lbm.getLayoutBlock(signalMastList.get(j).getFacing());
                            log.debug("Past valid now getting layout block " +ldstBlock.getDisplayName());
                            try {
                                ArrayList<LayoutBlock> lblks = lbm.getLayoutBlocks(lFacing, ldstBlock, lProtecting, true, jmri.jmrit.display.layoutEditor.LayoutBlockManager.MASTTOMAST);
                                if(log.isDebugEnabled())
                                    log.debug("Size of blocks " + lblks.size());
                                validDestMast.add(destMast);
                            } catch (jmri.JmriException e){  // Considered normal if route not found.
                                log.debug("not a valid route through " + source.getDisplayName() + " - " + destMast.getDisplayName());
                            }
                        }
                    } catch (jmri.JmriException ex) {
                        log.debug(ex.toString());
                    }
                }
            }
            //location ++;
        }
    }
    
    Hashtable<SignalMast, ArrayList<SignalMast>> validPaths = new Hashtable<SignalMast, ArrayList<SignalMast>>();

    /**
    * Discover all possible valid source and destination signalmasts past pairs 
    * on all layout editor panels.
    * @return A has Hashtable, of each source signalmast, with an arraylist of 
    * all the valid destination signalmast.
    */
    
    public Hashtable<SignalMast, ArrayList<SignalMast>> automaticallyDiscoverSignallingPairs() throws JmriException{
        validPaths = new Hashtable<SignalMast, ArrayList<SignalMast>>();
        runWhenStablised=false;
        jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        if(!lbm.isAdvancedRoutingEnabled()){
            throw new JmriException("advanced routing not enabled");
        }
        if(!lbm.routingStablised()){
            runWhenStablised=true;
            throw new JmriException("routing not stablised");
        }
        ArrayList<FacingProtecting> signalMastList = generateBlocksWithSignals();
        int total = signalMastList.size()*signalMastList.size();
        firePropertyChange("autoGenerateTotal", null, total);
        for(int i = 0; i<signalMastList.size(); i++){
            if(log.isDebugEnabled())
                log.debug(signalMastList.get(i).getMast().getDisplayName() + " " + signalMastList.get(i).getFacing().getDisplayName() + " " + signalMastList.get(i).getProtecting().getDisplayName() + " " + Path.decodeDirection(signalMastList.get(i).getDirection()));
            Block facing = signalMastList.get(i).getFacing();
            LayoutBlock lFacing = lbm.getLayoutBlock(facing);
            Block protecting = signalMastList.get(i).getProtecting();
            LayoutBlock lProtecting = lbm.getLayoutBlock(protecting);
            SignalMast source = signalMastList.get(i).getMast();
            discoverSignallingDest(source, lProtecting, lFacing);
        }
        Enumeration<SignalMast> en = validPaths.keys();
        while (en.hasMoreElements()) {
            SignalMast key = en.nextElement();
            SignalMastLogic sml = getSignalMastLogic(key);
            if(sml==null){
                sml=newSignalMastLogic(key);
            }
            ArrayList<SignalMast> validDestMast = validPaths.get(key);
            for(int i = 0; i<validDestMast.size(); i++){
                try{
                    sml.setDestinationMast(validDestMast.get(i));
                    sml.useLayoutEditor(true, validDestMast.get(i));
                } catch (jmri.JmriException ex){
                    //log.debug("we shouldn't get an exception here!");
                    log.debug(ex);
                }
            }
        }
        
        firePropertyChange("autoGenerateComplete", null, null);
        return validPaths;
    }
    
    
    private ArrayList<FacingProtecting> generateBlocksWithSignals(){
        jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        ArrayList<FacingProtecting> signalMastList = new ArrayList<FacingProtecting>();
    
        List<String> lblksSysName = lbm.getSystemNameList();
        for(int i = 0; i<lblksSysName.size(); i++){
            LayoutBlock curLblk = lbm.getLayoutBlock(lblksSysName.get(i));
            Block curBlk = curLblk.getBlock();
            if(curBlk!=null){
                int noNeigh = curLblk.getNumberOfNeighbours();
                for(int x = 0; x<noNeigh; x++){
                    Block blk = curLblk.getNeighbourAtIndex(x);
                    SignalMast sourceMast = lbm.getFacingSignalMast(curBlk, blk);
                    if(sourceMast!=null){
                        FacingProtecting toadd = new FacingProtecting(curBlk, blk, sourceMast, curLblk.getNeighbourDirection(x));
                        if(!signalMastList.contains(toadd))
                            signalMastList.add(toadd);
                    }
                }
            }
        }
        return signalMastList;
    }
    
    class FacingProtecting{
        
        Block facing;
        Block protecting;
        SignalMast mast;
        int direction;
        
        FacingProtecting(Block facing, Block protecting, SignalMast mast, int direction){
            this.facing = facing;
            this.protecting = protecting;
            this.mast = mast;
            this.direction = direction;
        }
        
        Block getFacing() { return facing; }
        
        Block getProtecting() { return protecting; }
        
        SignalMast getMast() { return mast; }
        
        int getDirection() { return direction; }
    
    }
    
    /**
    * This uses the layout editor to check if the destination signalmast is 
    * reachable from the source signalmast
    *
    * @param sourceMast Source SignalMast
    * @param destMast Destination SignalMast
    * @return true if valid, false if not valid.
    */
    
    public boolean checkValidDest(SignalMast sourceMast, SignalMast destMast) throws JmriException{
        LayoutBlock facingBlock = null;
        LayoutBlock protectingBlock = null;
        LayoutBlock destFacingBlock = null;
        LayoutBlock destProtectBlock = null;
        ArrayList<LayoutEditor> layout = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
        for(int i = 0; i<layout.size(); i++){
            if(log.isDebugEnabled())
                log.debug("Layout name " + layout.get(i).getLayoutName());
            if (facingBlock==null){
                facingBlock = InstanceManager.layoutBlockManagerInstance().getFacingBlockByMast(sourceMast.getUserName(), layout.get(i));
                if (facingBlock==null)
                    facingBlock = InstanceManager.layoutBlockManagerInstance().getFacingBlockByMast(sourceMast.getSystemName(), layout.get(i));
            }
            if (protectingBlock==null){
                protectingBlock = InstanceManager.layoutBlockManagerInstance().getProtectedBlockByMast(sourceMast.getUserName(), layout.get(i));
                if (protectingBlock==null)
                    protectingBlock = InstanceManager.layoutBlockManagerInstance().getProtectedBlockByMast(sourceMast.getSystemName(), layout.get(i));
            }
            if(destFacingBlock==null){
                destFacingBlock = InstanceManager.layoutBlockManagerInstance().getFacingBlockByMast(destMast.getUserName(), layout.get(i));
                if (destFacingBlock==null)
                    destFacingBlock = InstanceManager.layoutBlockManagerInstance().getFacingBlockByMast(destMast.getSystemName(), layout.get(i));
            }
            if(destProtectBlock==null){
                destProtectBlock = InstanceManager.layoutBlockManagerInstance().getProtectedBlockByMast(destMast.getUserName(), layout.get(i));
                if(destProtectBlock==null)
                    destProtectBlock = InstanceManager.layoutBlockManagerInstance().getProtectedBlockByMast(destMast.getSystemName(), layout.get(i));
            }
            if((destProtectBlock!=null) && (destFacingBlock!=null) && (facingBlock!=null) && (protectingBlock!=null)){
                //A simple to check to see if the remote signal is in the correct direction to ours.
                try{
                    return InstanceManager.layoutBlockManagerInstance().checkValidDest(facingBlock, protectingBlock, destFacingBlock, destProtectBlock);
                } catch (jmri.JmriException e){
                    throw e;
                }
            } else {
                log.debug("blocks not found");
            }
        }
        throw new JmriException("Blocks Not Found");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalMastLogicManager.class.getName());
}
