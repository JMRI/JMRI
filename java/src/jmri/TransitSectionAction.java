// TransitSectionAction.java

package jmri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds information and options for a Action to be applied when an automated train 
 *		enters, exits, or is inside of a Section in a Transit. 
 * <P>
 * A TransitSection holds specified TrainsitSectionActions.  A TransitSection may have as many 
 *		TransitSectionActions as appropriate. Each TransitSectionAction belongs to one and only one 
 *		TransitSection.
 * <P>
 * TransitSectionActions are specified in two parts: 
 *	1. The "When" part specifies when after the automated train enters the Section the action is to be
 *		initiated.  Optionally, each "when" may be delayed by a specified time (in milliseconds).
 *  2. The "What" part specified what action is to occur.
 * <P>
 * TransitSectionActions are created and editted in the Transit Table, when Transits are defined. 
 * <P>
 * This class provides support for SENSORACTIVE and SENSORINACTIVE "when"'s.
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is open source software; you can redistribute it and/or modify it 
 * under the terms of version 2 of the GNU General Public License as 
 * published by the Free Software Foundation. See the "COPYING" file for 
 * a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author	Dave Duchamp  Copyright (C) 2009, 2010
 * @version	$Revision$
 */
public class TransitSectionAction {
	/**
	 * Constants representing the "when" (when the action is to be initiated) of the Action
	 */
	public static final int NUM_WHENS = 8; // Must correspond to the number of entries below
	public static final int ENTRY  = 1;   // On entry to Section
	public static final int EXIT   = 2;	  // On exit from Section
	public static final int BLOCKENTRY = 3; // On entry to specified Block in the Section
	public static final int BLOCKEXIT = 4; // On exit from specified Block in the Section
	public static final int TRAINSTOP = 5;  // When train stops
	public static final int TRAINSTART = 6; // When train starts 
	public static final int SENSORACTIVE = 7; // When specified Sensor changes to Active
	public static final int SENSORINACTIVE = 8; // When specified Sensor changtes to Inactive
	// other action 'whens" may be defined here
	/**
	 * Constants designating the "what" (the action to be taken) of the Action
	 */ 
	public static final int NUM_WHATS = 13; // Must correspond to the number of entries below
	public static final int PAUSE = 1;    // pause for the number of fast minutes in mDataWhat (e.g. station stop)
	public static final int SETMAXSPEED = 2; // set maximum train speed to value entered
	public static final int SETCURRENTSPEED = 3; // set current speed to target speed immediately - no ramping
	public static final int RAMPTRAINSPEED = 4; // set current speed to target with ramping
	public static final int TOMANUALMODE = 5; // drop out of automated mode, and allow manual throttle control
	public static final int SETLIGHT = 6; // set light on or off
	public static final int STARTBELL = 7;  // start bell (only works with sound decoder, function 1 ON)
	public static final int STOPBELL = 8;   // stop bell (only works with sound decoder, function 1 OFF)
	public static final int SOUNDHORN = 9;  // sound horn for specified number of milliseconds 
	//													(only works with sound decoder, function 2)
	public static final int SOUNDHORNPATTERN = 10; // sound horn according to specified pattern
	//													(only works with sound decoder, function 2)
	public static final int LOCOFUNCTION = 11;  // execute the specified decoder function
	public static final int SETSENSORACTIVE = 12; // set specified sensor active (offers access to Logix)
	public static final int SETSENSORINACTIVE = 13; // set specified sensor inactive
	// other action 'whats" may be defined here

	/**
	 * Main constructor method
	 */
	public TransitSectionAction(int when, int what) {
        mWhen = when;
		mWhat = what;
	}
	
	/** 
	 * Convenience constructor
	 */
	public TransitSectionAction(int when, int what, int dataWhen, int dataWhat1, int dataWhat2, String sWhen, String sWhat) {
        mWhen = when;
		mWhat = what;
		mDataWhen = dataWhen;
		mDataWhat1 = dataWhat1;
		mDataWhat2 = dataWhat2;
		mStringWhen = sWhen;
		mStringWhat = sWhat;
    }
	    
	// instance variables
	private int mWhen = 0;
	private int mWhat = 0;
	private int mDataWhen = -1;	// negative number signified no data 
	private int mDataWhat1 = -1;    // negative number signified no data 
	private int mDataWhat2 = -1;    // negative number signified no data 
	private String mStringWhen = "";   
	private String mStringWhat = "";
		
	/**
     * Access methods
     */
	public int getWhenCode() { return mWhen; }
	public void setWhenCode(int n) {mWhen = n;}
	public int getWhatCode() { return mWhat; }
	public void setWhatCode(int n) {mWhat = n;}
	public int getDataWhen() { return mDataWhen; }
	public void setDataWhen( int n ) { mDataWhen = n; }
	public int getDataWhat1() { return mDataWhat1; }
	public void setDataWhat1( int n ) { mDataWhat1 = n; }
	public int getDataWhat2() { return mDataWhat2; }
	public void setDataWhat2( int n ) { mDataWhat2 = n; }
	public String getStringWhen() { return mStringWhen; }
	public void setStringWhen( String s ) { mStringWhen = s; }
	public String getStringWhat() { return mStringWhat; }
	public void setStringWhat( String s ) { mStringWhat = s; }
	
	/** 
	 * operational instance variables - flags and data for executing the action 
	 *    (see jmri.jmrit.dispatcher.AutoActiveTrain.java)
	 */
	private Thread _waitingThread = null;
	private boolean _waitingForSectionExit = false;
	private TransitSection _targetTransitSection = null;
	private boolean _waitingForBlock = false;
	private boolean _waitingForSensor = false;
	private Sensor _triggerSensor = null;
	private java.beans.PropertyChangeListener _sensorListener = null;
	/**
	 * initialize all operational instance variables (not saved between runs)
	 */
	public void initialize() {
		_waitingThread = null;
		_waitingForSectionExit = false;
		_targetTransitSection = null;		
		_waitingForBlock = false;
		_waitingForSensor = false;
		_triggerSensor = null;
		_sensorListener = null;
	}	
	/**
	 * Operational access methods
	 */
	public Thread getWaitingThread() {return _waitingThread;}
	public void setWaitingThread(Thread t) {_waitingThread = t;}
	public boolean getWaitingForSectionExit() {return _waitingForSectionExit;}
	public void setWaitingForSectionExit(boolean w) {_waitingForSectionExit = w;}
	public TransitSection getTargetTransitSection() {return _targetTransitSection;}
	public void setTargetTransitSection(TransitSection ts) {_targetTransitSection = ts;}
	public boolean getWaitingForBlock() {return _waitingForBlock;}
	public void setWaitingForBlock(boolean w) {_waitingForBlock = w;}
	public boolean getWaitingForSensor() {return _waitingForSensor;}
	public void setWaitingForSensor(boolean w) {_waitingForSensor = w;}
	public Sensor getTriggerSensor() {return _triggerSensor;}
	public void setTriggerSensor(Sensor s) {_triggerSensor = s;}
	public java.beans.PropertyChangeListener getSensorListener() {return _sensorListener;}
	public void setSensorListener(java.beans.PropertyChangeListener l) {_sensorListener = l;}

	public void disposeSensorListener() {
		// if this object has registered a listener, dispose of it
		if (_sensorListener != null) {
			_triggerSensor.removePropertyChangeListener(_sensorListener);
			_sensorListener = null;
			_waitingForSensor = false;
		}
	}
	public void dispose() {
		disposeSensorListener();
	}
    
    static Logger log = LoggerFactory.getLogger(TransitSectionAction.class.getName());
}

/* @(#)TransitSectionAction.java */
