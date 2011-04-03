// AllocatedSection.java

package jmri.jmrit.dispatcher;

import java.util.ResourceBundle;
import java.util.ArrayList;

/**
 * This class holds information and options for an AllocatedSection. 
 * <P>
 * An AllocatedSection holds the following information about this allocation:
  * <P>
 * A AllocatedSections is referenced via a list in DispatcherFrame, which serves as 
 *	a manager for AllocatedSection objects.
 * <P>
 * AllocatedSections are transient, and are not saved to disk.
 * <P>
 * AllocatedSections keep track of whether they have been entered and exited.
 * <P>
 * If the Active Train this Section is assigned to is being run automatically, support is provided
 *   for monitoring Section changes and changes for Blocks within the Section.
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
 *
 * @author	Dave Duchamp  Copyright (C) 2008-2010
 * @version	$Revision: 1.9 $
 */
public class AllocatedSection {

	/**
	 * Main constructor method
	 * @param s cannot be null
	 */
	public AllocatedSection(jmri.Section s, ActiveTrain at, int seq, jmri.Section next, int nextSeqNo) {
        mSection = s;
        mActiveTrain = at;
		mSequence = seq;
        mNextSection = next;
		mNextSectionSequence = nextSeqNo;
		if (mSection.getOccupancy() == jmri.Section.OCCUPIED) {
			mEntered = true;
		}
		// listen for changes in Section occupancy
        mSection.addPropertyChangeListener(mSectionListener = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) { handleSectionChange(e); }
        });
		if (mSection.getState()==jmri.Section.FORWARD) {
			mForwardStoppingSensor = mSection.getForwardStoppingSensor();
			mReverseStoppingSensor = mSection.getReverseStoppingSensor();
		}
		else {
			mForwardStoppingSensor = mSection.getReverseStoppingSensor();
			mReverseStoppingSensor = mSection.getForwardStoppingSensor();
		}		
	}

	static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");
	    
	// instance variables
	private jmri.Section mSection = null;
	private ActiveTrain mActiveTrain = null;
	private int mSequence = 0;
	private jmri.Section mNextSection = null;
	private int mNextSectionSequence = 0;
	private java.beans.PropertyChangeListener mSectionListener = null;
	private boolean mEntered = false;
	private boolean mExited = false;
	private int mAllocationNumber = 0;     // used to keep track of allocation order
	private jmri.Sensor mForwardStoppingSensor = null;
	private jmri.Sensor mReverseStoppingSensor = null;
	
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
	public int getNextSectionSequence() {return mNextSectionSequence;}	
	public boolean getEntered() {return mEntered;}
	public boolean getExited() {return mExited;}
	public int getAllocationNumber() {return mAllocationNumber;}
	public void setAllocationNumber(int n) {mAllocationNumber = n;}
	public jmri.Sensor getForwardStoppingSensor() {return mForwardStoppingSensor;}
	public jmri.Sensor getReverseStoppingSensor() {return mReverseStoppingSensor;}

	// instance variables used with automatic running of trains
	private int mIndex = 0;
	private java.beans.PropertyChangeListener mExitSignalListener = null;
	private ArrayList<java.beans.PropertyChangeListener> mBlockListeners = 
													new ArrayList<java.beans.PropertyChangeListener>();
	private ArrayList<jmri.Block> mBlockList = null;
	
	/**
     * Access methods for automatic running instance variables
     */
	public void setIndex(int i) {mIndex = i;}
	public int getIndex() {return mIndex;}
	public void setExitSignalListener(java.beans.PropertyChangeListener xSigListener) {
		mExitSignalListener = xSigListener;
	}
	public java.beans.PropertyChangeListener getExitSignalListener() {return mExitSignalListener;}
	
	/** 
	 * Methods
	 */
	protected jmri.TransitSection getTransitSection() {
		return (mActiveTrain.getTransit().getTransitSectionFromSectionAndSeq(mSection,mSequence));
	}
	public int getDirection() {return mSection.getState();}
	public int getLength() {
		return mSection.getLengthI(DispatcherFrame.instance().getUseScaleMeters(),
									DispatcherFrame.instance().getScale());
	}
		
	private synchronized void handleSectionChange(java.beans.PropertyChangeEvent e) {
		if (mSection.getOccupancy()==jmri.Section.OCCUPIED) {
			mEntered = true;
		}
		else if (mSection.getOccupancy()==jmri.Section.UNOCCUPIED) {
			if (mEntered) mExited = true;
		}
		if (mActiveTrain.getAutoActiveTrain()!=null) {
			if (e.getPropertyName().equals("state")) {
				mActiveTrain.getAutoActiveTrain().handleSectionStateChange(this);
			}
			else if (e.getPropertyName().equals("occupancy")) {
				mActiveTrain.getAutoActiveTrain().handleSectionOccupancyChange(this);
			}
		}
		DispatcherFrame.instance().sectionOccupancyChanged();
	}
	public synchronized void initializeMonitorBlockOccupancy() {
		if (mBlockList != null) return;
		mBlockList = mSection.getBlockList();
		for (int i = 0; i<mBlockList.size(); i++) {
			java.beans.PropertyChangeListener listener = null;
			jmri.Block b = mBlockList.get(i);
			if (b!=null) {
				final int index = i;  // block index
				b.addPropertyChangeListener(listener = new java.beans.PropertyChangeListener() {
					public void propertyChange(java.beans.PropertyChangeEvent e) 
							{ handleBlockChange(index, e); }
					});
				mBlockListeners.add(listener);
			}
		}			
	}
	boolean handlingBlockChange = false; 
	
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SWL_SLEEP_WITH_LOCK_HELD",
                justification="used only by thread that can be stopped, no conflict with other threads expected")	
    private synchronized void handleBlockChange(int index, java.beans.PropertyChangeEvent e) {
		if (e.getPropertyName().equals("state")) {
			if (mBlockList == null) mBlockList = mSection.getBlockList();
			if (mBlockList!=null) {
				jmri.Block b = mBlockList.get(index);
				if ( (mActiveTrain.getAutoActiveTrain()!=null) && (!handlingBlockChange) ) {
					// filter to insure that change is not a short spike
					int occ = b.getState();
					handlingBlockChange = true;
					if (Thread.currentThread().getName().startsWith("AWT-EventQueue"))
					    log.error("handleBlockChange will be calling Thread.sleep on AWT Event Queue");
					try {
						Thread.sleep(250);
					} catch (InterruptedException exc) {
						// ignore this exception
					}
					if (occ == b.getState()) {
						// occupancy has not changed, must be OK
						mActiveTrain.getAutoActiveTrain().handleBlockStateChange(this, b);
					}
					handlingBlockChange = false;
				}
			}
		}
	}
	
	protected jmri.Block getExitBlock() {
		if (mNextSection==null) return null;
		jmri.EntryPoint ep = mSection.getExitPointToSection(mNextSection,mSection.getState());
		if (ep!=null) {
			return ep.getBlock();
		}
		return null;
	}
	protected jmri.Block getEnterBlock(AllocatedSection previousAllocatedSection) {
		if (previousAllocatedSection==null) return null;
		jmri.Section sPrev = previousAllocatedSection.getSection();
		jmri.EntryPoint ep = mSection.getEntryPointFromSection(sPrev,mSection.getState());
		if (ep!=null) {
			return ep.getBlock();
		}
		return null;
	}
		
	public synchronized void dispose() {
		if ( (mSectionListener!=null) && (mSection!=null) ) {
			mSection.removePropertyChangeListener(mSectionListener);
		}
		mSectionListener = null;
		for (int i = mBlockListeners.size(); i>0; i--) {
			jmri.Block b = mBlockList.get(i-1);
			b.removePropertyChangeListener(mBlockListeners.get(i-1));
		}
//		mSection = null;
//		mActiveTrain = null;
    }
			    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AllocatedSection.class.getName());
}

/* @(#)AllocatedSection.java */
