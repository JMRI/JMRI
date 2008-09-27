// TransitSection.java

package jmri;

/**
 * This class holds information and options for a Section when assigned to a Transit. 
 * Corresponds to an allocatable "Section" of track assigned to a Transit.
 * <P>
 * An TransitSection holds the following information: 
 *  Section ID
 *  Section Direction 
 *  Sequence number of Section within the Transit
 *  Special action for train in this Section, if requested
 *  Data for special action, if needed
 *  Whether this Section is a primary section or an alternate section
 * <P>
 * A TransitSection is referenced via a list in its parent Transit, and is 
 *	stored on disk when its parent Transit is stored.
 * <P>
 * Two Special Actions are currently defines, but more may be added
 *  Pause for a user set number of fast clock minutes after reaching the end of the assigned section.
 *	Wait after reaching the end of the assigned section until an external restart occurs.
 * <P>
 * Provides for delayed initializatio of Section when loading panel files, so that this is not 
 *	dependent on order of items in the panel file.
 *
 * @author	Dave Duchamp  Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public class TransitSection {

	/**
	 * Main constructor method
	 */
	public TransitSection(jmri.Section s, int seq, int direction) {
        mSection = s;
        mSequence = seq;
		mDirection = direction;
	}
	
	/** 
	 * Convenience constructor
	 */
	public TransitSection(jmri.Section s, int seq, int direction, int act, int data, boolean alt) {
        mSection = s;
        mSequence = seq;
		mDirection = direction;
		mAction = act;
		mData = data;
		mAlternate = alt;
// djd debugging
// String alternateString = "false";
// if (alt) alternateString = "true";
// log.warn("TransitSection Section,seq,direction,action,data,alternate - "+s.getSystemName()+", "+seq+", "+
//				direction+", "+act+", "+data+", "+alternateString);
// end debugging					
    }
	
	/**
	 * Special constructor to delay Section initialization
	 */
	public TransitSection(String secName, int seq, int direction, int act, int data, boolean alt) {
        tSectionName = secName;
        mSequence = seq;
		mDirection = direction;
		mAction = act;
		mData = data;
		mAlternate = alt;
		needsInitialization = true;
	}
	
	/**
	 * Constants representing the Special Action requested for this Section
	 */ 
	public static final int NONE = 0x02;     // no special action
	public static final int PAUSE = 0x04;    // pause for the number of fast minutes in data
	public static final int WAIT = 0x08;     // wait until restarted externally
	// other special actions may be defined here
    
	// instance variables
	private Section mSection = null;
	private int mSequence = 0;
	private int mDirection = 0;
	private int mAction = NONE;
	private int mData = -1;    // negative number signified no data 
	private boolean mAlternate = false;
	
	// temporary variables and method for delayed initialization of Section
	private String tSectionName = "";
	private boolean needsInitialization = false;
	private void initialize() {
		mSection = InstanceManager.sectionManagerInstance().getSection(tSectionName);
		if (mSection==null) 
			log.error("Missing Section - "+tSectionName+" - when initializing a TransitSection");
		needsInitialization = false;
	}
	
	/**
     * Access methods
     */
    public Section getSection() { 
		if (needsInitialization) initialize();
		return mSection; 
	}
	public String getSectionName() {
		if (needsInitialization) initialize();
		String s = mSection.getSystemName();
		String u = mSection.getUserName();
		if ( (u!=null) && (!u.equals("")) ) {
			return (s+"( "+u+" )");
		}
		return s;
	}
	// Note: once TransitSection is created, Section and its sequence and direction may not be changed.
	public int getDirection() { return mDirection; }
	public int getSequenceNumber() { return mSequence; }
	public int getAction() { return mAction; }
	public void setAction( int act ) { mAction = act; }
	public int getData() { return mData; }
	public void setData( int data ) { mData = data; }
	public boolean isAlternate() { return mAlternate; }
	public void setAlternate( boolean alt )	{ mAlternate = alt; }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TransitSection.class.getName());
}

/* @(#)TransitSection.java */
