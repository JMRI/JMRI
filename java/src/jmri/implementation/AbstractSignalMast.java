// AbstractSignalMast.java

package jmri.implementation;

import java.util.ArrayList;
import java.util.Vector;
import java.util.List;
import jmri.*;

 /**
 * Abstract class providing the basic logic of the SignalMast interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision$
 */
public abstract class AbstractSignalMast extends AbstractNamedBean
    implements SignalMast, java.io.Serializable, java.beans.VetoableChangeListener  {

    public AbstractSignalMast(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractSignalMast(String systemName) {
        super(systemName);
    }
      
	@edu.umd.cs.findbugs.annotations.OverrideMustInvoke
    public void setAspect(String aspect) { 
        String oldAspect = this.aspect;
        this.aspect = aspect;
        this.speed = (String)getSignalSystem().getProperty(aspect, "speed");
        firePropertyChange("Aspect", oldAspect, aspect);
    }

    public String getAspect() { return aspect; }
    protected String aspect = null;
    
    public String getSpeed() { return speed; }
    protected String speed = null;

    /**
     * The state is the index of the current aspect
     * in the list of possible aspects.
     */
    public int getState() {
        return -1;
    }
    public void setState(int i) {
    }

    /**
     * By default, signals are lit.
     */
	private boolean mLit = true;
	/**
	 * Default behavior for "lit" parameter is
	 * to track value and return it.
	 */
	public boolean getLit() {return mLit;}
	
	/** 
	 * By default, signals are not held.
	 */
	private boolean mHeld = false;

	/**
	 * "Held" parameter is just tracked and notified.
	 */
	public boolean getHeld() {return mHeld;}
	
    /**
     * Set the lit parameter.
     * 
     * This acts on all the SignalHeads included 
     * in this SignalMast
     */
	@edu.umd.cs.findbugs.annotations.OverrideMustInvoke
    public void setLit(boolean newLit) {
        boolean oldLit = mLit;
        mLit = newLit;
        if (oldLit != newLit) {
            //updateOutput();
            // notify listeners, if any
            firePropertyChange("Lit", Boolean.valueOf(oldLit), Boolean.valueOf(newLit));
        }
        
    }
    
    /**
     * Set the held parameter.
     * <P>
     * Note that this does not directly effect the output on the layout;
     * the held parameter is a local variable which effects the aspect
     * only via higher-level logic.
     */
	@edu.umd.cs.findbugs.annotations.OverrideMustInvoke
    public void setHeld(boolean newHeld) {
        boolean oldHeld = mHeld;
        mHeld = newHeld;
        if (oldHeld != newHeld) {
            // notify listeners, if any
            firePropertyChange("Held", Boolean.valueOf(oldHeld), Boolean.valueOf(newHeld));
        }
        
    }
    
    DefaultSignalAppearanceMap map;
    SignalSystem systemDefn;
    
    void configureSignalSystemDefinition(String name) {
        systemDefn = InstanceManager.signalSystemManagerInstance().getSystem(name);
        if (systemDefn == null) {
            log.error("Did not find signal definition: "+name);
            throw new IllegalArgumentException("Signal definition not found: "+name);
        }
    }
    
    void configureAspectTable(String signalSystemName, String aspectMapName) {
        map = DefaultSignalAppearanceMap.getMap(signalSystemName, aspectMapName);
    }
    
    public SignalSystem getSignalSystem() {
        return systemDefn;
    }
    
    public SignalAppearanceMap getAppearanceMap() {
        return map;
    }
    
    ArrayList<String> disabledAspects = new ArrayList<String>(1);
    
   /**
    * returns a list of all the valid aspects, that have not been disabled
    */
    public Vector<String> getValidAspects() {
        java.util.Enumeration<String> e = map.getAspects();
        Vector<String> v = new Vector<String>();
        while (e.hasMoreElements()) {
            String aspect = e.nextElement();
            if(!disabledAspects.contains(aspect))
                v.add(aspect);
        }
        return v;
    }
    
    /**
    * returns a list of all the known aspects for this mast, including those that have been disabled
    */
    public Vector<String> getAllKnownAspects(){
        java.util.Enumeration<String> e = map.getAspects();
        Vector<String> v = new Vector<String>();
        while (e.hasMoreElements()) {
            v.add(e.nextElement());
        }
        return v;
    }

    public void setAspectDisabled(String aspect){
        if(aspect==null || aspect.equals(""))
            return;
        if(!map.checkAspect(aspect)){
            log.warn("attempting to disable an aspect: " + aspect + " that is not on the mast " + getDisplayName());
            return;
        }
        if(!disabledAspects.contains(aspect)){
            disabledAspects.add(aspect);
            firePropertyChange("aspectDisabled", null, aspect);
        }
    }
    
    public void setAspectEnabled(String aspect){
        if(aspect==null || aspect.equals(""))
            return;
        if(!map.checkAspect(aspect)){
            log.warn("attempting to disable an aspect: " + aspect + " that is not on the mast " + getDisplayName());
            return;
        }
        if(disabledAspects.contains(aspect)) {
            disabledAspects.remove(aspect);
            firePropertyChange("aspectEnabled", null, aspect);
        }
    }
    
    public List<String> getDisabledAspects(){
        return disabledAspects;
    }
    
    public boolean isAspectDisabled(String aspect){
        return disabledAspects.contains(aspect);
    }
    
    boolean allowUnLit = true;
    
    public void setAllowUnLit(boolean boo){
        allowUnLit = boo;
    }
    
    public boolean allowUnLit(){
        return allowUnLit;
    }
    
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        log.info("vetoable Change Called");
    }

}

/* @(#)AbstractSignalMast.java */
