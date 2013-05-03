// ActiveTrain.java

package jmri.jmrit.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * This class holds information and options for an ActiveTrain, that is a 
 *   train that has been linked to a Transit and activated for transit around
 *   the layout. 
 * <P>
 * An ActiveTrain may be assigned one of the following modes, which specify 
 *	how the active train will be run through its transit:
 *		AUTOMATIC - indicates the ActiveTrain will be run under automatic 
 *				control of the computer. (Automatic Running)
 *		MANUAL - indicates an ActiveTrain running in AUTOMATIC mode has reached 
 *              a Special Action in its Transit that requires MANUAL operation. 
 *				When this happens, the status changes to WORKING, and the mode 
 *				changes to MANUAL. The ActiveTrain will be run by an operator  
 *				using a throttle. AUTOMATIC running is resumed when the work has 
 *              been completed.
 *		DISPATCHED - indicates the ActiveTrain will be run by an operator  
 *				using a throttle. A dispatcher will allocate Sections to the
 *				ActiveTrain as needed, control optional signals using a CTC 
 *              panel or computer logic, and arbitrate any conflicts between 
 *              ActiveTrains. (Human Dispatcher).
 * <P>
 * An ActiveTrain will have one of the following statuses:
 *       RUNNING - Actively running on the layout, according to its mode of operation.
 *       PAUSED - Paused waiting for a user-specified number of fast clock minutes.  The
 *                  Active Train is expected to move to either RUNNING or WAITING once the
 *                  specified number of minutes has elapsed. This is intended for automatic
 *                  station stops. (automatic trains only)
 *       WAITING - Stopped waiting for a Section allocation. This is the state the Active
 *                  Train is in when it is created in Dispatcher.
 *       WORKING - Peforming work under control of a human engineer. This is the state an
 *                  Active Train assumes when an engineer is picking up or setting out cars
 *                  at industries. (automatic trains only)
 *       READY - Train has completed WORKING, and is awaiting a restart - dispatcher clearance
 *                  to resume running. (automatic trains only)
 *       STOPPED - Train was stopped by the dispatcher. Dispatcher must resume. (automatic trains only)
 *       DONE -  Train has completed its transit of the layout and is ready to be terminated 
 *                  by the dispatcher. 
 * Status is a bound property.
 * <P>
 * The ActiveTrain status should maintained (setStatus) by the running class, or if running 
 *       in DISPATCHED mode, by Dispatcher.
 * When an ActiveTrain is WAITING, and the dispatcher allocates a section to it, the status 
 *       of the ActiveTrain is automatically set to RUNNING. So an autoRun class can listen 
 *       to the status of the ActiveTrain to trigger start up if the train has been waiting
 *       for the dispatcher.
 * Npte: There is still more to be programmed here.
 * <P>
 * Train information supplied when the ActiveTrain is created can come from any of the following:
 *       ROSTER - The train was selected from the JMRI roster menu
 *       OPERATIONS - The train was selected from trains available from JMRI operations
 *       USER - Neither menu was used--the user entered a name and DCC address.
 * Train source information is recorded when an ActiveTrain is created, and may be referenced 
 *       by getTrainSource if it is needed by other objects.
 * The train source should be specified in the Dispatcher Options window prior to creating an 
 *       ActiveTrain.
 * <P>
 * ActiveTrains are referenced via a list in DispatcherFrame, which serves as 
 *		a manager for ActiveTrain objects.
 * <P>
 * ActiveTrains are transient, and are not saved to disk. Active Train information can be saved 
 *		to disk, making set up with the same options, etc very easy.
 * <P>
 * An ActiveTrain runs through its Transit in the FORWARD direction, until a Transit Action 
 *		reverses the direction of travel in the Transit.  When running with its Transit 
 *      reversed, the Active Train returns to its starting Section. Upon reaching and 
 *		stopping in its starting Section, the Transit is automatically set back to the 
 *      forward direction. If AutoRestart is set, the run is repeated.
 * The direction of travel in the Transit is maintained here.
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
 * <P>
 *
 * @author	Dave Duchamp  Copyright (C) 2008-2011
 * @version	$Revision$
 */
public class ActiveTrain {

	/**
	 * Main constructor method
	 */
	public ActiveTrain(jmri.Transit t, String name, int trainSource) {
        mTransit = t;
        mTrainName = name;
		mTrainSource = trainSource;
	}

	static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");
	
	/**
	 * Constants representing the Status of this ActiveTrain
	 * When created, the Status of an Active Train is always WAITING,
	 */ 
	public static final int RUNNING = 0x01;   // running on the layout
	public static final int PAUSED = 0x02;    // paused for a number of fast minutes
	public static final int WAITING = 0x04;   // waiting for a section allocation
	public static final int WORKING = 0x08;   // actively working
	public static final int READY = 0x10;	  // completed work, waiting for restart
	public static final int STOPPED = 0x20;   // stopped by the dispatcher (auto trains only)
	public static final int DONE = 0x40;	  // completed its transit
	
	/**
	 * Constants representing Type of ActiveTrains. 
	 */
	public static final int NONE = 0x00;               // no train type defined
	public static final int LOCAL_PASSENGER = 0x01;    // low priority local passenger train 
	public static final int LOCAL_FREIGHT =	0x02;      // low priority freight train performing local tasks 
	public static final int THROUGH_PASSENGER = 0x03;  // normal priority through passenger train
	public static final int THROUGH_FREIGHT = 0x04;    // normal priority through freight train 
	public static final int EXPRESS_PASSENGER = 0x05;  // high priority passenger train    
	public static final int EXPRESS_FREIGHT = 0x06;    // high priority freight train 
	public static final int MOW = 0x07;			       // low priority maintenance of way train  

	/** 
	 * Constants representing the mode of running of the Active Train
	 * The mode is set when the Active Train is created. The mode may be switched during a 
	 *    run.
	 */
	public static final int AUTOMATIC = 0x02;   // requires mAutoRun to be "true" (auto trains only)
	public static final int MANUAL = 0x04;    // requires mAutoRun to be "true" (auto trains only)
	public static final int DISPATCHED = 0x08;
	
	/**
	 * Constants representing the source of the train information
	 */
	public static final int ROSTER = 0x01;
	public static final int OPERATIONS = 0x02;
	public static final int USER = 0x04;
	
	// instance variables
	private jmri.Transit mTransit = null;
	private String mTrainName = "";
	private int mTrainSource = ROSTER;
    private jmri.jmrit.roster.RosterEntry mRoster = null;
	private int mStatus = WAITING;
	private int mMode = DISPATCHED;
	private boolean mTransitReversed = false;  // true if Transit is running in reverse
	private boolean mAllocationReversed = false;  // true if allocating Sections in reverse
	private AutoActiveTrain mAutoActiveTrain = null;
	private ArrayList<AllocatedSection> mAllocatedSections = new ArrayList<AllocatedSection>();
	private jmri.Section mLastAllocatedSection = null;
	private jmri.Section mSecondAllocatedSection = null;
	private int mNextAllocationNumber = 1;
	private jmri.Section mNextSectionToAllocate = null;
	private int mNextSectionSeqNumber = 0;
	private int mNextSectionDirection = 0;
	private jmri.Block mStartBlock = null;
	private int mStartBlockSectionSequenceNumber = 0;
	private jmri.Block mEndBlock = null;
	private jmri.Section mEndBlockSection = null;
	private int mEndBlockSectionSequenceNumber = 0;
	private int mPriority = 0;
	private boolean mAutoRun = false;
	private String mDccAddress = "";
	private boolean mResetWhenDone = true;
	private boolean mReverseAtEnd = false;
	private boolean mDelayedStart = false;
	private int mDepartureTimeHr = 8;
	private int mDepartureTimeMin = 0;
	private int mTrainType = LOCAL_FREIGHT;
	
	// start up instance variables
	private boolean mStarted = false;
	
	
	/**
     * Access methods 
     */
	public boolean getStarted() {return mStarted;}
	public void setStarted() {
		mStarted = true;
		mStatus = RUNNING;
		setStatus(WAITING);
	}
    public jmri.Transit getTransit() { 
		return mTransit; 
	}
	public String getTransitName() {
		String s = mTransit.getSystemName();
		String u = mTransit.getUserName();
		if ( (u!=null) && (!u.equals("")) ) {
			return (s+"( "+u+" )");
		}
		return s;
	}
	public String getActiveTrainName() {return (mTrainName+"/"+getTransitName());}
	// Note: Transit and Train may not be changed once an ActiveTrain is created.
	public String getTrainName() { return mTrainName; }
	public int getTrainSource() { return mTrainSource; }
    public void setRosterEntry(jmri.jmrit.roster.RosterEntry re){
        mRoster = re;
    }
    public jmri.jmrit.roster.RosterEntry getRosterEntry(){
        if(mRoster==null && getTrainSource()==ROSTER){
            //Try to resolve the roster based upon the train name
            mRoster = jmri.jmrit.roster.Roster.instance().getEntryForId(getTrainName());
        } else if (getTrainSource()!=ROSTER) {
            mRoster=null;
        }
        return mRoster;
    }
	public int getStatus() { return mStatus; }
	public void setStatus(int status) {  
		if ( (status==RUNNING) || (status==PAUSED) || (status==WAITING) || (status==WORKING) ||
					(status==READY) || (status==STOPPED) || (status==DONE)) {
			if (mStatus!=status) {
				int old = mStatus;
				mStatus = status;
				firePropertyChange("status", Integer.valueOf(old), Integer.valueOf(mStatus));
			}
		}
		else
			log.error("Invalid ActiveTrain status - "+status);
	}
	public String getStatusText() {
		if (mStatus==RUNNING) 
			return rb.getString("RUNNING");
		else if (mStatus==PAUSED) 
			return rb.getString("PAUSED");
		else if (mStatus==WAITING) {
			if (!mStarted && mDelayedStart) {
				return jmri.jmrit.beantable.LogixTableAction.formatTime(mDepartureTimeHr,
						mDepartureTimeMin)+" "+rb.getString("START");
			}
			return rb.getString("WAITING");
		}
		else if (mStatus==WORKING) 
			return rb.getString("WORKING");
		else if (mStatus==READY) 
			return rb.getString("READY");
		else if (mStatus==STOPPED) 
			return rb.getString("STOPPED");
		else if (mStatus==DONE) 
			return rb.getString("DONE");
		return ("");
	}
	public boolean isTransitReversed() {return mTransitReversed;}
	public void setTransitReversed(boolean set) {mTransitReversed = set;}
	public boolean isAllocationReversed() {return mAllocationReversed;}
	public void setAllocationReversed(boolean set) {mAllocationReversed = set;}
	public boolean getDelayedStart() {return mDelayedStart;}
	public void setDelayedStart(boolean set) {mDelayedStart = set;}
	public int getDepartureTimeHr() {return mDepartureTimeHr;}
	public void setDepartureTimeHr(int hr) {mDepartureTimeHr = hr;}
	public int getDepartureTimeMin() {return mDepartureTimeMin;}
	public void setDepartureTimeMin(int min) {mDepartureTimeMin = min;}
	public void setTrainType(int type) {mTrainType = type;}
	public int getTrainType() {return mTrainType;}	
	public String getTrainTypeText() {
		if (mTrainType==LOCAL_FREIGHT) 
			return rb.getString("LOCAL_FREIGHT");
		else if (mTrainType==LOCAL_PASSENGER) 
			return rb.getString("LOCAL_PASSENGER");
		else if (mTrainType==THROUGH_FREIGHT) 
			return rb.getString("THROUGH_FREIGHT");
		else if (mTrainType==THROUGH_PASSENGER) 
			return rb.getString("THROUGH_PASSENGER");
		else if (mTrainType==EXPRESS_FREIGHT) 
			return rb.getString("EXPRESS_FREIGHT");
		else if (mTrainType==EXPRESS_PASSENGER) 
			return rb.getString("EXPRESS_PASSENGER");
		else if (mTrainType==MOW) 
			return rb.getString("MOW");
		return ("");
	}
	public  int getMode() { return mMode; }
	public void setMode(int mode) {
		if ( (mode==AUTOMATIC) || (mode==MANUAL) ||
				(mode==DISPATCHED) ) {
			int old = mMode;
			mMode = mode;
			firePropertyChange("mode", Integer.valueOf(old), Integer.valueOf(mMode));
		}
		else
			log.error("Attempt to set ActiveTrain mode to illegal value - "+mode);
    }
	public String getModeText() {
		if (mMode==AUTOMATIC) 
			return rb.getString("AUTOMATIC");
		else if (mMode==MANUAL) 
			return rb.getString("MANUAL");
		else if (mMode==DISPATCHED) 
			return rb.getString("DISPATCHED");
			return ("");
	}
	public void setAutoActiveTrain(AutoActiveTrain aat) {mAutoActiveTrain=aat;}
	public AutoActiveTrain getAutoActiveTrain() {return mAutoActiveTrain;}
	public int getRunningDirectionFromSectionAndSeq(jmri.Section s, int seqNo) {
		int dir = mTransit.getDirectionFromSectionAndSeq(s,seqNo);
		if (mTransitReversed) {
			if (dir == jmri.Section.FORWARD) {
				dir = jmri.Section.REVERSE;
			}
			else {
				dir = jmri.Section.FORWARD;
			}
		}
		return dir;
	}	
	public int getAllocationDirectionFromSectionAndSeq(jmri.Section s, int seqNo) {
		int dir = mTransit.getDirectionFromSectionAndSeq(s,seqNo);
		if (mAllocationReversed) {
			if (dir == jmri.Section.FORWARD) {
				dir = jmri.Section.REVERSE;
			}
			else {
				dir = jmri.Section.FORWARD;
			}
		}
		return dir;
	}	
	public void addAllocatedSection (AllocatedSection as) {
		if (as!=null) {
			mAllocatedSections.add(as);
			if (as.getSection() == mNextSectionToAllocate) {
				// this  is the next Section in the Transit, update pointers
				mLastAllocatedSection = as.getSection();
				mNextSectionToAllocate = as.getNextSection();
				mNextSectionSeqNumber = as.getNextSectionSequence();
				mNextSectionDirection = getAllocationDirectionFromSectionAndSeq(
										mNextSectionToAllocate,mNextSectionSeqNumber);
				as.setAllocationNumber(mNextAllocationNumber);
				mNextAllocationNumber ++;
			}
			else {
				// this is an extra allocated Section
				as.setAllocationNumber(-1);
			}
			if ( (mStatus==WAITING) && mStarted ) {
				setStatus(RUNNING);
			}
			if (as.getSequence()==2) {
				mSecondAllocatedSection = as.getSection();
			}
			if (DispatcherFrame.instance().getNameInAllocatedBlock()) {
                if(DispatcherFrame.instance().getRosterEntryInBlock() && getRosterEntry()!=null){
                    as.getSection().setNameInBlocks(getRosterEntry());
                } else {
                    as.getSection().setNameInBlocks(mTrainName);
                }
				as.getSection().suppressNameUpdate(true);
			}
			if (DispatcherFrame.instance().getExtraColorForAllocated()) {
                as.getSection().setAlternateColorFromActiveBlock(true);
			}
			refreshPanel();
		}
		else {
			log.error("Null Allocated Section reference in addAllocatedSection of ActiveTrain");
		}
	}
	private void refreshPanel() {
		if (DispatcherFrame.instance().getLayoutEditor()!=null) {
			DispatcherFrame.instance().getLayoutEditor().redrawPanel();
		}
	}
	public void removeAllocatedSection (AllocatedSection as) {
		if (as==null) {
			log.error("Null AllocatedSection reference in removeAllocatedSection of ActiveTrain");
			return;
		}
		int index = -1;
		for (int i = 0; i<mAllocatedSections.size(); i++) {
			if (as == mAllocatedSections.get(i)) index = i;
		}
		if (index<0) {
			log.error("Attempt to remove an unallocated Section");
			return;
		}
		mAllocatedSections.remove(index);
		if (mAutoRun) {
			mAutoActiveTrain.removeAllocatedSection(as);
		}
		if (DispatcherFrame.instance().getNameInAllocatedBlock()) {
			as.getSection().clearNameInUnoccupiedBlocks();
			as.getSection().suppressNameUpdate(false);
		}
		as.getSection().setAlternateColor(false);
		refreshPanel();
		if (as.getSection() == mLastAllocatedSection) {
			mLastAllocatedSection = null;
			if (mAllocatedSections.size()>0) {
				mLastAllocatedSection = mAllocatedSections.get(
								mAllocatedSections.size()-1).getSection();
			}
		}
	}
	public java.util.ArrayList<AllocatedSection> getAllocatedSectionList() {
		ArrayList<AllocatedSection> list = new ArrayList<AllocatedSection>();
		for (int i = 0; i<mAllocatedSections.size(); i++) {
			list.add(mAllocatedSections.get(i));
		}
		return list;
	}
	public jmri.Section getLastAllocatedSection() {return mLastAllocatedSection;}
	public String getLastAllocatedSectionName() {
		if (mLastAllocatedSection==null) return rb.getString("None");
		return getSectionName(mLastAllocatedSection);
	}
	public jmri.Section getNextSectionToAllocate() { return mNextSectionToAllocate; }
	public int getNextSectionSeqNumber() { return mNextSectionSeqNumber; } 
	public String getNextSectionToAllocateName() {
		if (mNextSectionToAllocate==null) return rb.getString("None");
		return getSectionName(mNextSectionToAllocate);
	}
	private String getSectionName(jmri.Section sc) {
		String s = sc.getSystemName();
		String u = sc.getUserName();
		if ( (u!=null) && (!u.equals("")) ) return (s+"( "+u+" )");
		return s;
	}
	public jmri.Block getStartBlock() {return mStartBlock;}
	public void setStartBlock(jmri.Block sBlock) {mStartBlock = sBlock;}
	public int getStartBlockSectionSequenceNumber() {return mStartBlockSectionSequenceNumber;}
	public void setStartBlockSectionSequenceNumber(int sBlockSeqNum) 
										{mStartBlockSectionSequenceNumber = sBlockSeqNum;}
	public jmri.Block getEndBlock() {return mEndBlock;}
	public void setEndBlock(jmri.Block eBlock) {mEndBlock = eBlock;}
	public jmri.Section getEndBlockSection() {return mEndBlockSection;}
	public void setEndBlockSection(jmri.Section eSection) {mEndBlockSection = eSection;}
	public int getEndBlockSectionSequenceNumber() {return mEndBlockSectionSequenceNumber;}
	public void setEndBlockSectionSequenceNumber(int eBlockSeqNum) 
										{mEndBlockSectionSequenceNumber = eBlockSeqNum;}
	public int getPriority() {return mPriority;}
	public void setPriority(int priority) {mPriority = priority;}
	public boolean getAutoRun() {return mAutoRun;}
	public void setAutoRun(boolean autoRun) {mAutoRun = autoRun;}
	public String getDccAddress() {return mDccAddress;}
	public void setDccAddress(String dccAddress) {mDccAddress = dccAddress;}
	public boolean getResetWhenDone() {return mResetWhenDone;}
	public void setResetWhenDone(boolean s) {mResetWhenDone = s;}
	public boolean getReverseAtEnd() {return mReverseAtEnd;}
	public void setReverseAtEnd(boolean s) {mReverseAtEnd = s;}
	protected jmri.Section getSecondAllocatedSection() {return mSecondAllocatedSection;}
		
	/**
	 * Operating methods
	 */
	public AllocationRequest initializeFirstAllocation() {
		if (mAllocatedSections.size()>0) {
			log.error("ERROR - Request to initialize first allocation, when allocations already present");
			return null;
		}
		if ( (mStartBlockSectionSequenceNumber>0) && (mStartBlock!=null) ) {
			mNextSectionToAllocate = mTransit.getSectionFromBlockAndSeq(mStartBlock,
													mStartBlockSectionSequenceNumber);
			if (mNextSectionToAllocate==null) {
				mNextSectionToAllocate = mTransit.getSectionFromConnectedBlockAndSeq(mStartBlock,
													mStartBlockSectionSequenceNumber);
				if (mNextSectionToAllocate==null) {
					log.error("ERROR - Cannot find Section for first allocation of ActiveTrain"+
																		getActiveTrainName());
					return null;
				}
			}				
			mNextSectionSeqNumber = mStartBlockSectionSequenceNumber;
			mNextSectionDirection = getAllocationDirectionFromSectionAndSeq(mNextSectionToAllocate, 
						mNextSectionSeqNumber);
		}
		else {
			log.error("ERROR - Insufficient information to initialize first allocation");
			return null;
		}
		if (!DispatcherFrame.instance().requestAllocation(this,
				mNextSectionToAllocate, mNextSectionDirection, mNextSectionSeqNumber, true, null)) {	
			log.error("Allocation request failed for first allocation of "+getActiveTrainName());
		}
        if(DispatcherFrame.instance().getRosterEntryInBlock() && getRosterEntry()!=null){
            mStartBlock.setValue(getRosterEntry());
        } else if (DispatcherFrame.instance().getShortNameInBlock()) {
            mStartBlock.setValue(mTrainName);
		}
		AllocationRequest ar = DispatcherFrame.instance().findAllocationRequestInQueue(mNextSectionToAllocate,
				mNextSectionSeqNumber, mNextSectionDirection, this);
		return ar;
	}
	
	protected void reverseAllAllocatedSections() {
		for (int i = 0; i<mAllocatedSections.size(); i++) {
			AllocatedSection aSec = mAllocatedSections.get(i);
			int dir = mTransit.getDirectionFromSectionAndSeq(aSec.getSection(),aSec.getSequence());
			if (dir == jmri.Section.FORWARD) {
				aSec.getSection().setState(jmri.Section.REVERSE);
				}
			else {
				aSec.getSection().setState(jmri.Section.FORWARD);
			}
			aSec.setStoppingSensors();
		}			
	}
	
	protected void resetAllAllocatedSections() {
		for (int i = 0; i<mAllocatedSections.size(); i++) {
			AllocatedSection aSec = mAllocatedSections.get(i);
			int dir = mTransit.getDirectionFromSectionAndSeq(aSec.getSection(),aSec.getSequence());
			aSec.getSection().setState(dir);
			aSec.setStoppingSensors();			
		}	
	}
	
	public void terminate() {
		mTransit.setState(jmri.Transit.IDLE);
	}
    
	public void dispose() {
	
	}

	// Property Change Support
	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
		}
	protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
		}
	
    static Logger log = LoggerFactory.getLogger(ActiveTrain.class.getName());
}

/* @(#)ActiveTrain.java */
