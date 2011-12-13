// AbstractSignalHead.java

package jmri.implementation;

import java.util.ResourceBundle;
import jmri.*;
 /**
 * Abstract class providing the basic logic of the SignalHead interface.
 * <p>
 * SignalHead system names are always upper case.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version     $Revision$
 */
public abstract class AbstractSignalHead extends AbstractNamedBean
    implements SignalHead, java.io.Serializable {

    public AbstractSignalHead(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractSignalHead(String systemName) {
        super(systemName);
    }

	static final ResourceBundle rbx = ResourceBundle
			.getBundle("jmri.jmrit.beantable.LogixTableBundle");

    public String getAppearanceName(int appearance) {
        String ret = jmri.util.StringUtil.getNameFromState(
                appearance, getValidStates(), getValidStateNames());
		if (ret != null) return ret;
		else return ("");
    }
    public String getAppearanceName() {
        return getAppearanceName(getAppearance());
    }
    
    protected int mAppearance = DARK;
    public int getAppearance() { return mAppearance; }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //						Object oldValue,
    //						Object newValue)
    // _once_ if anything has changed state


    /**
     * By default, signals are lit.
     */
	protected boolean mLit = true;
	/**
	 * Default behavior for "lit" parameter is
	 * to track value and return it.
	 */
	public boolean getLit() {return mLit;}
	
	/** 
	 * By default, signals are not held.
	 */
	protected boolean mHeld = false;
	/**
	 * "Held" parameter is just tracked and notified.
	 */
	public boolean getHeld() {return mHeld;}
	
    /**
     * Implement a shorter name for setAppearance.
     *<P>
     * This generally shouldn't be used by Java code; use 
     * setAppearance instead.  The is provided to make Jython
     * script access easier to read.  
     */
    public void setState(int s) { setAppearance(s); }
    
    /**
     * Implement a shorter name for getAppearance.
     *<P>
     * This generally shouldn't be used by Java code; use 
     * getAppearance instead.  The is provided to make Jython
     * script access easier to read.  
     */
    public int getState() { return getAppearance(); }
        
    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"EI_EXPOSE_REP", "MS_EXPOSE_REP"}) // OK until Java 1.6 allows return of cheap array copy
    public static int[] getDefaultValidStates() {
        return validStates;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"EI_EXPOSE_REP", "MS_EXPOSE_REP"}) // OK until Java 1.6 allows return of cheap array copy
    public static String[] getDefaultValidStateNames() {
        return validStateNames;
    }
    public static String getDefaultStateName(int appearance) {
        String ret = jmri.util.StringUtil.getNameFromState(
                appearance, getDefaultValidStates(), getDefaultValidStateNames());
		if (ret != null) return ret;
		else return ("");
    }
    
    private static final ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");
    private static final  int[] validStates = new int[]{
        DARK, 
        RED, 
        YELLOW,
        GREEN,
        LUNAR,
        FLASHRED, 
        FLASHYELLOW,
        FLASHGREEN,
        FLASHLUNAR
    };
   private static final String[] validStateNames = new String[]{
        rb.getString("SignalHeadStateDark"),
        rb.getString("SignalHeadStateRed"),
        rb.getString("SignalHeadStateYellow"),
        rb.getString("SignalHeadStateGreen"),
        rb.getString("SignalHeadStateLunar"),
        rb.getString("SignalHeadStateFlashingRed"),
        rb.getString("SignalHeadStateFlashingYellow"),
        rb.getString("SignalHeadStateFlashingGreen"),
        rb.getString("SignalHeadStateFlashingLunar"),
    };
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK until Java 1.6 allows return of cheap array copy
    public int[] getValidStates() {
        return validStates;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK until Java 1.6 allows return of cheap array copy
    public String[] getValidStateNames() {
        return validStateNames;
    }

}

/* @(#)AbstractSignalHead.java */
