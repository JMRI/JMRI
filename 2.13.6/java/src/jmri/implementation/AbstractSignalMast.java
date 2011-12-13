// AbstractSignalMast.java

package jmri.implementation;

import jmri.*;

 /**
 * Abstract class providing the basic logic of the SignalMast interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision$
 */
public abstract class AbstractSignalMast extends AbstractNamedBean
    implements SignalMast, java.io.Serializable {

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
    private String aspect = null;
    
    public String getSpeed() { return speed; }
    private String speed = null;

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

}

/* @(#)AbstractSignalMast.java */
