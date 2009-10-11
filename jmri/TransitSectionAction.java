// TransitSectionAction.java

package jmri;

/**
 * This class holds information and options for a Special Action to be applied when an automated train 
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
 *
 * @author	Dave Duchamp  Copyright (C) 2009
 * @version	$Revision: 1.1 $
 */
public class TransitSectionAction {
	/**
	 * Constants representing the "when" (when the action is to be initiated) of the Special Action
	 */
	public static final int NUM_WHENS = 9; // Must correspond to the number of entries below
	public static final int ENTRY  = 1;   // On entry to Section
	public static final int EXIT   = 2;	  // On exit from Section
	public static final int BLOCKENTRY = 3; // On entry to specified Block in the Section
	public static final int BLOCKEXIT = 4; // On exit from specified Block in the Section
	public static final int TRAINSTOP = 5;  // When train stops
	public static final int TRAINSTART = 6; // When train starts 
	public static final int SENSORACTIVE = 7; // When specified Sensor changes to Active
	public static final int SENSORINACTIVE = 8; // When specified Sensor changtes to Inactive
	public static final int CONTAINED = 9; // When train is entirely within the Section 
	// other special action 'whens" may be defined here
	/**
	 * Constants designating the "what" (the action to be taken) of the Special Action
	 */ 
	public static final int NUM_WHATS = 13; // Must correspond to the number of entries below
	public static final int PAUSE = 1;    // pause for the number of fast minutes in mDataWhat (e.g. station stop)
	public static final int SETMAXSPEED = 2; // set maximum train speed to value entered
	public static final int SETCURRENTSPEED = 3; // set current speed either higher or lower that current value
	public static final int RAMPTRAINSPEED = 4; // set current speed to target over specified time period
	public static final int TOMANUALMODE = 5; // drop out of automated mode, and allow manual throttle control
	public static final int RESUMEAUTO = 6; // resume automatic throttle operation
	public static final int STARTBELL = 7;  // start bell (only works with sound decoder)
	public static final int STOPBELL = 8;   // stop bell (only works with sound decoder)
	public static final int SOUNDHORN = 9;  // sound horn for specified number of milliseconds
	public static final int SOUNDHORNPATTERN = 10; // sound horn according to specified pattern
	public static final int LOCOFUNCTION = 11;  // execute the specified decoder function
	public static final int SETSENSORACTIVE = 12; // set specified sensor active (offers access to Logix)
	public static final int SETSENSORINACTIVE = 13; // set specified sensor inactive
	// other special action 'whats" may be defined here

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

	public void dispose() {
		// if this object has registered any listeners, dispose of them
	}
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TransitSectionAction.class.getName());
}

/* @(#)TransitSectionAction.java */
