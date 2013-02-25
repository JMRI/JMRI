// AllocatedSection.java

package jmri.jmrit.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * This class holds information and options for an AllocatedSection, a Section 
 *	that is currently allocated to an ActiveTrain. 
 * <P>
 * AllocatedSections are referenced via a list in DispatcherFrame, which serves as 
 *	a manager for AllocatedSection objects. Each ActiveTrain also maintains a list 
 *	of AllocatedSections currently assigned to it.
 * <P>
 * AllocatedSections are transient, and are not saved to disk.
 * <P>
 * AllocatedSections keep track of whether they have been entered and exited.
 * <P>
 * If the Active Train this Section is assigned to is being run automatically, 
 *	support is provided for monitoring Section changes and changes for Blocks 
 *	within the Section.
 *
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
 * @author	Dave Duchamp  Copyright (C) 2008-2011
 * @version	$Revision$
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
		setStoppingSensors();
		if ((mActiveTrain.getAutoActiveTrain()==null) && !(DispatcherFrame.instance().getSupportVSDecoder())) {
			// for manual running, monitor block occupancy for selected Blocks only
			if ( mActiveTrain.getReverseAtEnd() && 
					( (mSequence==mActiveTrain.getEndBlockSectionSequenceNumber()) ||
						( mActiveTrain.getResetWhenDone() && 
						(mSequence==mActiveTrain.getStartBlockSectionSequenceNumber()) ) ) ) {
				initializeMonitorBlockOccupancy();				
			}
		}
		else {
			// monitor block occupancy for all Sections of automatially running trains
			initializeMonitorBlockOccupancy();
		}
		listenerList = new javax.swing.event.EventListenerList();
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
    private javax.swing.event.EventListenerList listenerList;
	
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
	private ArrayList<jmri.Block> mActiveBlockList = new ArrayList<jmri.Block>();
	
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
	protected void setStoppingSensors() {
		if (mSection.getState()==jmri.Section.FORWARD) {
			mForwardStoppingSensor = mSection.getForwardStoppingSensor();
			mReverseStoppingSensor = mSection.getReverseStoppingSensor();
		}
		else {
			mForwardStoppingSensor = mSection.getReverseStoppingSensor();
			mReverseStoppingSensor = mSection.getForwardStoppingSensor();
		}
	}
	protected jmri.TransitSection getTransitSection() {
		return (mActiveTrain.getTransit().getTransitSectionFromSectionAndSeq(mSection,mSequence));
	}
	public int getDirection() {return mSection.getState();}
	public int getLength() {
		return mSection.getLengthI(DispatcherFrame.instance().getUseScaleMeters(),
									DispatcherFrame.instance().getScale());
	}
	public void reset() {
		mExited = false;
		mEntered = false;
		if (mSection.getOccupancy() == jmri.Section.OCCUPIED) {
			mEntered = true;
		}
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
    private synchronized void handleBlockChange(int index, java.beans.PropertyChangeEvent e) {
		if (e.getPropertyName().equals("state")) {
			if (mBlockList == null) mBlockList = mSection.getBlockList();
			if (mBlockList!=null) {
				jmri.Block b = mBlockList.get(index);
				if (!isInActiveBlockList(b)) {
					int occ = b.getState();
					Runnable handleBlockChange = new RespondToBlockStateChange(b,occ,this);
					Thread tBlockChange = new Thread(handleBlockChange);
					tBlockChange.start();
					addToActiveBlockList(b);
					if (DispatcherFrame.instance().getSupportVSDecoder())
					    firePropertyChangeEvent("BlockStateChange", null, b.getSystemName()); // NOI18N
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
	protected synchronized void addToActiveBlockList(jmri.Block b) {
		if (b!=null) {
			mActiveBlockList.add(b);
		}
	}
	protected synchronized void removeFromActiveBlockList(jmri.Block b) {
		if (b!=null) {
			for (int i=0;i<mActiveBlockList.size();i++) {
				if (b==mActiveBlockList.get(i)) {
					mActiveBlockList.remove(i);
					return;
				}
			}
		}
	}
	protected synchronized boolean isInActiveBlockList(jmri.Block b) {
		if (b!=null) {
			for (int i=0;i<mActiveBlockList.size();i++) {
				if (b==mActiveBlockList.get(i)) return true;
			}
		}
		return false;
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
    }

// _________________________________________________________________________________________

	// This class responds to Block state change in a separate thread
	class RespondToBlockStateChange implements Runnable
	{
		public RespondToBlockStateChange (jmri.Block b, int occ, AllocatedSection as) {
			_block = b;
			_aSection = as;
			_occ = occ;
		}
		public void run() {
			// delay to insure that change is not a short spike
			try {
				Thread.sleep(_delay);
			} catch (InterruptedException exc) {
						// ignore this exception
			}
			if (_occ == _block.getState()) {
				// occupancy has not changed, must be OK
				if (mActiveTrain.getAutoActiveTrain()!=null) {
					// automatically running train
					mActiveTrain.getAutoActiveTrain().handleBlockStateChange(_aSection, _block);
				}
				else if (_occ==jmri.Block.OCCUPIED) {
					// manual running train - block newly occupied
					if ( (_block==mActiveTrain.getEndBlock()) && mActiveTrain.getReverseAtEnd() ) {
						// reverse direction of Allocated Sections
						mActiveTrain.reverseAllAllocatedSections();
					}
					else if ( (_block==mActiveTrain.getStartBlock()) && mActiveTrain.getResetWhenDone() ) {
						// reset the direction of Allocated Sections 
						mActiveTrain.resetAllAllocatedSections();
					}
				}
			}
			// remove from lists
			removeFromActiveBlockList(_block);
		}
		private int _delay = 250;
		private jmri.Block _block = null;
		private int _occ = 0;
		private AllocatedSection _aSection = null;
	}
			    

    public void addPropertyChangeListener(PropertyChangeListener listener) {
	log.debug("Adding listener " + listener.getClass().getName() + " to " + this.getClass().getName());
	listenerList.add(PropertyChangeListener.class, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
	listenerList.remove(PropertyChangeListener.class, listener);
    }

    protected void firePropertyChangeEvent(PropertyChangeEvent evt) {
	//Object[] listeners = listenerList.getListenerList();

	for (PropertyChangeListener l : listenerList.getListeners(PropertyChangeListener.class)) {
	    l.propertyChange(evt);
	}
    }

    protected void firePropertyChangeEvent(String name, Object oldVal, Object newVal) {
	log.debug("Firing property change: " + name + " " + newVal.toString());
	firePropertyChangeEvent(new PropertyChangeEvent(this, name, oldVal, newVal));
    }


    static Logger log = LoggerFactory.getLogger(AllocatedSection.class.getName());
}

/* @(#)AllocatedSection.java */
