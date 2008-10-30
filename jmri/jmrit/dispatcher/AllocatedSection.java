// AllocatedSection.java

package jmri.jmrit.dispatcher;

import java.util.ResourceBundle;

/**
 * This class holds information and options for an AllocatedSection. 
 * <P>
 * An AllocatedSection holds the following information:
 *	Section allocated
 *  Active Train allocated to
  * <P>
 * A AllocatedSections is referenced via a list in DispatcherFrame, which serves as 
 *	a manager for AllocatedSection objects.
 * <P>
 * AllocatedSections are transient, and are not saved to disk.
 * <P>
 *
 * @author	Dave Duchamp  Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public class AllocatedSection {

	/**
	 * Main constructor method
	 */
	public AllocatedSection(jmri.Section s, ActiveTrain at, int seq, jmri.Section next) {
        mSection = s;
        mActiveTrain = at;
		mSequence = seq;
        mNextSection = next;
		// listen for changes in Section occupancy
		if (mSection!=null) {
			mSection.addPropertyChangeListener(mSectionListener = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) { handleSectionChange(e); }
            });
        }		
	}

	static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");
	    
	// instance variables
	private jmri.Section mSection = null;
	private ActiveTrain mActiveTrain = null;
	private int mSequence = 0;
	private jmri.Section mNextSection = null;
	private java.beans.PropertyChangeListener mSectionListener = null;
	
	/**
     * Access methods 
     */
    public jmri.Section getSection() { 
		return mSection; 
	}
	public String getSectionName() {
		String s = mSection.getSystemName();
		String u = mSection.getUserName();
		if ( (u!=null) && (!u.equals("")) ) {
			return (s+"( "+u+" )");
		}
		return s;
	}
	public ActiveTrain getActiveTrain() { return mActiveTrain; }
	public String getActiveTrainName() {
		return (mActiveTrain.getTrainName()+"/"+mActiveTrain.getTransitName());
	}
	public int getSequence() {return mSequence;}
	public jmri.Section getNextSection() {return mNextSection;}	
	
	/** 
	 * Methods
	 */
	private void handleSectionChange(java.beans.PropertyChangeEvent e) {
		DispatcherFrame.instance().sectionOccupancyChanged();
	}
		
	public void dispose() {
		if ( (mSectionListener!=null) && (mSection!=null) ) {
			mSection.removePropertyChangeListener(mSectionListener);
		}
		mSectionListener = null;
		mSection = null;
		mActiveTrain = null;
    }
			    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AllocatedSection.class.getName());
}

/* @(#)AllocatedSection.java */
