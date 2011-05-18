// AutoAllocate.java

package jmri.jmrit.dispatcher;

import jmri.Block;
import jmri.Section;
import jmri.EntryPoint;
import jmri.Transit;
import jmri.TransitSection;

import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.ConnectivityUtil;
import jmri.jmrit.display.layoutEditor.LevelXing;

import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Handles automatic allocation of Sections for Dispatcher 
 * <P>
 * AutoAllocate.java is an extension of DispatcherFrame.java.  
 * <P>
 * When AutoAllocate is triggered, it scans the list of Allocation Requests, in order of 
 * the priorities of ActiveTrains with pending AllocationRequests, testing if a requested 
 * allocation can be made. AutoAllocate returns when either:
 *     A Section has been allocated  -or-
 *     All AllocationRequests have been tested, and no allocation is indicated.
 * <P>
 * If AutoAllocate needs to save information related to a plan requiring multiple allocations,
 * an AllocationPlan object is created.  When the plan is complete, the AllocationPlan object 
 * is disposed of. Multiple AllocationPlan objects may be active at any one time.
 * <P>
 * AutoAllocate is triggered by each of the following events:
 *     An AllocatedSection has been released, freeing up a Section.
 *	   A new AllocationRequest has been entered into the queue of AllocationRequests.
 *     A Section has been allocated, either by AutoAllocate or manually by the dispatcher.
 * <P>
 * AutoAllocate requires that AutoRelease is active.
 * <P>
 * AutoAllocate operates conservatively, that is, if there is any doubt that a Section should
 * be allocated, it will not allocate the Section.
 * <P>
 * The dispatcher is free to switch AutoAllocate on or off at any tine in DispatcherFrame.
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
 * @author			Dave Duchamp    Copyright (C) 2011
 * @version			$Revision: 1.2 $
 */

public class AutoAllocate {
	
	static final ResourceBundle rb = ResourceBundle
	.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");
	
	public AutoAllocate (DispatcherFrame d) {
		_dispatcher = d;
		if (_dispatcher==null) {
			log.error("null DispatcherFrame when constructing AutoAllocate");
			return;
		}
		if (_dispatcher.getLayoutEditor()==null) {
			log.error("null LayoutEditor when constructing AutoAllocate");
			return;
		}
		_conUtil = _dispatcher.getLayoutEditor().getConnectivityUtil();
		if (_conUtil==null) {
			log.error("null ConnectivityUtil when constructing AutoAllocate");
		}
	}
	
	// operational variables
	private DispatcherFrame _dispatcher = null;
	private ConnectivityUtil _conUtil = null;
	private ArrayList<AllocationPlan> _planList = new ArrayList<AllocationPlan>();
	private int nextPlanNum = 1;
	private ArrayList<AllocationRequest> orderedRequests = new ArrayList<AllocationRequest>();
	
	/** 
	 * This is the entry point to AutoAllocate when it is triggered.
	 *
	 * Returns 'true' if a Section has been allocated, returns 'false' if not.
	 */
	protected void scanAllocationRequestList(ArrayList<AllocationRequest> list) {
		if (list.size()<=0) return;
		// copy AllocationRequests in order of priority of ActiveTrain.
		copyAndSortARs(list);
		removeCompletePlans();
		for (int i = 0; i<orderedRequests.size(); i++) {
			AllocationRequest ar = orderedRequests.get(i);
			if (ar==null) {
				log.error("error in allocation request list - AllocationRequest is null");
				return;
			}
			if (getPlanThisTrain(ar.getActiveTrain())!=null) {
				// this train is in an active Allocation Plan, anything to do now?
				ActiveTrain at = ar.getActiveTrain();
				AllocationPlan ap = getPlanThisTrain(at);
				if (!at.isTransitReversed()) {
					if (ap.getTargetSectionSequenceNum(cTrainNum)>=ar.getSectionSeqNumber()) {
						if ( (ar.getSection().getState()==Section.FREE) && 
								(ar.getSection().getOccupancy()!=Section.OCCUPIED) ) {
							if (allocateIfLessThanThreeAhead(ar)) return;
						}
					}
				}
				else {
					if (ap.getTargetSectionSequenceNum(cTrainNum)<=ar.getSectionSeqNumber()) {
						if ( (ar.getSection().getState()==Section.FREE) && 
								(ar.getSection().getOccupancy()!=Section.OCCUPIED) ) {
							if (allocateIfLessThanThreeAhead(ar)) return;
						}
					}
				}
			}
			else if (!waitingForStartTime(ar)) {
				// train isn't waiting, continue only if requested Section is currently free and not occupied
				if ( (ar.getSection().getState()==Section.FREE) && 
							(ar.getSection().getOccupancy()!=Section.OCCUPIED) ) {
					// requested Section is currently free and not occupied
					ArrayList<ActiveTrain> activeTrainsList = _dispatcher.getActiveTrainsList();
					if (activeTrainsList.size()==1) {
						// this is the only ActiveTrain
						if (allocateIfLessThanThreeAhead(ar)) return;
					}  
					else {
						//check if any other ActiveTrain will need this Section
						boolean okToAllocate = true;
						ArrayList<ActiveTrain> neededByTrainList = new ArrayList<ActiveTrain>();
						for (int j=0; j<activeTrainsList.size(); j++) {
							ActiveTrain at = activeTrainsList.get(j);
							if (at!=ar.getActiveTrain()) {
								if (sectionNeeded(ar,at)) neededByTrainList.add(at);
							}
						}
						if (neededByTrainList.size()<=0) {
							// no other ActiveTrain needs this Section, any LevelXings?
							if (containsLevelXing(ar.getSection())) {							
								// check if allocating this Section might block a higher priority train
								for (int j=0; j<activeTrainsList.size(); j++) {
									ActiveTrain at = activeTrainsList.get(j);
									if ( (at!=ar.getActiveTrain()) && 
											(at.getPriority()>ar.getActiveTrain().getPriority()) ) {
										if (willLevelXingsBlockTrain(at)) {
											okToAllocate = false;
										}
									}	
								}
							}
						}
						else {
							// requested Section is needed by one or more other active Active Trains
							for (int k=0; k<neededByTrainList.size(); k++) {
								// section is also needed by this active train
								ActiveTrain nt = neededByTrainList.get(k);
								// are trains moving in same direction through the requested Section?
								if (sameDirection(ar,nt)) {					
									// trains will move in the same direction thru requested section
									if (nt.getPriority()>ar.getActiveTrain().getPriority()) {
										// a higher priority train is trailing this train, can we let it pass?
										if (checkForPassingPlan(ar,nt,neededByTrainList)) {
											// can we respond to the current allocation request
				

											okToAllocate = false;
										}
									}
								}
								else {
									// trains will move in opposite directions thru requested section
									//   explore possibility of a meet to avoid gridlock							
// djd debugging - need code here
								}							
							}
						}
						if (okToAllocate) {
							if (allocateIfLessThanThreeAhead(ar)) return;
						}
					}		
				}
			}
		}
	}
	
	/**
	 *  Entered to request a choice of Next Section when a Section is being allocated and 
	 *     there are alternate Section choices for the next Section.  sList contains the possible 
	 *     next Sections, and ar is the section being allocated when a choice is needed.
	 */
	protected Section autoNextSectionChoice(ArrayList<Section> sList, AllocationRequest ar) {
		// check if AutoAllocate has prepared for this question
		if ( (savedAR!=null) && (savedAR==ar) ) {
			for (int j=0; j<sList.size(); j++) {
				if (savedSection==sList.get(j)) {
					return savedSection;
				}
			}
			log.warn ("Failure of prepared choice of next Section in AutoAllocate");
		}
		// no prepared choice, or prepared choice failed, is there an unoccupied Section available
		for (int i=0; i<sList.size(); i++) {
			if ( (sList.get(i).getOccupancy()==Section.UNOCCUPIED) && (sList.get(i).getState()==Section.FREE) ) {
				return sList.get(i);
			}
		}
		// no unoccupied Section available, check for Section allocated in same direction as this request
		int dir = ar.getSectionDirection();
		ArrayList<AllocatedSection> allocatedSections = _dispatcher.getAllocatedSectionsList();
		for (int m=0; m<sList.size(); m++) {
			boolean notFound = true;
			for (int k=0; (k<allocatedSections.size()) && notFound; k++) {
				if (sList.get(m)==allocatedSections.get(k).getSection()) {
					notFound = false;
					if (allocatedSections.get(k).getSection().getState()==dir) {
						return sList.get(m);
					}
				}
			}
		}
		// if all else fails, return null so Dispatcher will ask the dispatcher to choose
		return null;	
	}
	private AllocationRequest savedAR = null;
	private Section savedSection = null;
	
	// private implementation methods
	private void copyAndSortARs(ArrayList<AllocationRequest> list) {
		orderedRequests.clear();
		// find highest priority train
		int priority = 0;
		for (int i = 0; i<list.size(); i++) {
			ActiveTrain at = list.get(i).getActiveTrain();
			if (at.getPriority()>priority) priority = at.getPriority();
		}
		while ( (list.size()>orderedRequests.size()) && (priority>0) ) {
			for (int i = 0; i<list.size(); i++) {
				ActiveTrain at = list.get(i).getActiveTrain();
				if (at.getPriority()==priority) orderedRequests.add(list.get(i));
			}
			priority --;
		}	
	}
	private AllocationPlan getPlanThisTrain(ActiveTrain at) {
		for (int i = 0; i<_planList.size(); i++) {
			AllocationPlan ap = _planList.get(i);
			for (int j=1; j<3; j++) {
				if (ap.getActiveTrain(j)==at) {
					cTrainNum = j;
					cPlanNum = ap.getPlanNum();
					cAT = at;
					return ap;
				}
			}
		}
		// train not in an AllocationPlan 
		cAT = null;
		cPlanNum = 0;
		cTrainNum = 0;
		return null;
	}
	private ActiveTrain cAT = null;
	private int cPlanNum = 0;
	private int cTrainNum = 0;
	
// djd debugging - may not need the following method
/*	private AllocationPlan getNextPlanThisTrain(ActiveTrain at) {
		if (at!=cAT) {
			return getPlanThisTrain(at);
		}
		for (int i=0; i<_planList.size(); i++) {
			AllocationPlan ap = _planList.get(i);
			if (ap.getPlanNum()>cPlanNum) {
				for (int j=1; j<3; j++) {
					if (ap.getActiveTrain(j)==at) {
						cTrainNum = j;
						cPlanNum = ap.getPlanNum();
						cAT = at;
						return ap;
					}
				}
			}
		}
		// train not in another AllocationPlan 
		cAT = null;
		cPlanNum = 0;
		cTrainNum = 0;
		return null;	
	}  
*/
// end djd debugging

	private void removeCompletePlans() {
		boolean foundCompletePlan = true;
		while (foundCompletePlan) {
			foundCompletePlan = false;
			for (int i=0; (!foundCompletePlan) && (i<_planList.size()); i++) {
				// remove if all planned allocations have been made
				foundCompletePlan = _planList.get(i).isComplete();
				if (foundCompletePlan) {
					_planList.get(i).dispose();
					_planList.remove(i);
				}
			}
		}
	}
			
	private boolean allocateIfLessThanThreeAhead(AllocationRequest ar) {
		// test how far ahead of occupied track this requested section is
		ArrayList<AllocatedSection> aSectionList = ar.getActiveTrain().getAllocatedSectionList();
		if (aSectionList.size()>=4) {
			int curSeq = ar.getSectionSeqNumber()-1;
			if ( (curSeq == 1) && ar.getActiveTrain().getResetWhenDone() ) {
				curSeq = ar.getActiveTrain().getTransit().getMaxSequence();
			}
			AllocatedSection curAS = null;
			for (int i = aSectionList.size()-1; i>=0; i--) {
				AllocatedSection as = aSectionList.get(i);
				if ( (as!=null) && (as.getSequence()==curSeq)) curAS = as;	
			}
			if ( (curAS!=null) && (curAS.getSection().getOccupancy()!=jmri.Section.OCCUPIED) ) {
				//last allocated section exists and is not occupied, test previous one
				curSeq = curSeq-1;
				if ( (curSeq == 1) && ar.getActiveTrain().getResetWhenDone() ) {
					curSeq = ar.getActiveTrain().getTransit().getMaxSequence();
				}
				curAS = null;
				for (int i = aSectionList.size()-1; i>=0; i--) {
					AllocatedSection as = aSectionList.get(i);
					if ( (as!=null) && (as.getSequence()==curSeq)) curAS = as;						
				}
				if ( (curAS!=null) && (curAS.getSection().getOccupancy()!=jmri.Section.OCCUPIED) ) {
					//previous allocated section exists and is not occupied, test previous one
					curSeq = curSeq-1;
					if ( (curSeq == 1) && ar.getActiveTrain().getResetWhenDone() ) {
						curSeq = ar.getActiveTrain().getTransit().getMaxSequence();
					}
					curAS = null;
					for (int i = aSectionList.size()-1; i>=0; i--) {
						AllocatedSection as = aSectionList.get(i);
						if ( (as!=null) && (as.getSequence()==curSeq)) curAS = as;						
					}
					if ( (curAS!=null) && (curAS.getSection().getOccupancy()!=jmri.Section.OCCUPIED) ) {
						// the last two AllocatedSections are not OCCUPIED, don't allocate any more yet
						return false;
					}
				}
			}
		}
// djd debugging
log.error("auto allocating Section "+ar.getSection().getUserName());
		_dispatcher.allocateSection(ar,null);
		return true;
	}
	private boolean checkForPassingPlan(AllocationRequest ar, ActiveTrain nt,
						  ArrayList<ActiveTrain> neededByTrainList) {
		// returns 'true' if an AllocationPlan has been set up, returns 'false' otherwise
		ActiveTrain at = ar.getActiveTrain();
		int aSeq = ar.getSectionSeqNumber();
		// is an alternate Section available here or ahead
		int aSecSeq = findPassingSection(at,aSeq);
		Section aSec = null;
		Section nSec = null;
		int nSecSeq = 0;
		if (aSecSeq == 0) { 
			// does higher priority train have a passing section ahead
			int nCurrentSeq = getCurrentSequenceNumber(nt);
			nSecSeq = findPassingSection(nt,nCurrentSeq);
			if (nSecSeq > 0) {
				// has passing section ahead, will this train traverse a Section in it
				ArrayList<Section> nSections = nt.getTransit().getSectionListBySeq(nSecSeq);
				for (int i = 0; (i<nSections.size()) && (aSec==null); i++) {
					aSecSeq = willTraverse(nSections.get(i), at, aSeq);
					if (aSecSeq>0) {
						aSec = at.getTransit().getSectionListBySeq(aSecSeq).get(0);
					}
				}
				if (aSec!=null) {
					// found passing Section that should work out, push higher 
					//     priority train one section further, if possible
					if (!nt.isTransitReversed()) {							
						if (nSecSeq<nt.getTransit().getMaxSequence()) nSecSeq ++;
					}
					else {
						if (nSecSeq>1) nSecSeq --;
					}
					nSec = nt.getTransit().getSectionListBySeq(nSecSeq).get(0);
					if (nSec==aSec) {
						if (nt.getTransit().getSectionListBySeq(nSecSeq).size()>1) {
							nSec = nt.getTransit().getSectionListBySeq(nSecSeq).get(1);
						}
						else {
							aSec = null;
						}
					}
				}
			}
		}
		else {
			// will the higher priority train go through any of these alternate sections
			ArrayList<Section> aSections = at.getTransit().getSectionListBySeq(aSecSeq);
			int nCurrentSeq = getCurrentSequenceNumber(nt);
			for (int i = 0; (i<aSections.size()) && (aSec==null); i++) {
				nSecSeq = willTraverse(aSections.get(i), nt, nCurrentSeq);
				if (nSecSeq>0) {
					// train will traverse one of these alternate sections
					//     find free section, not this one
					for (int j = aSections.size()-1; (j>=0) && (aSec==null); j--) {
						if (aSections.get(j).getState()==Section.FREE) {
							aSec = aSections.get(j);
						}
					}
					if (aSec==null) {
						// no free section, find one allocated in the same direction
					
					}
					if (aSec!=null) {
						if (!nt.isTransitReversed()) {
								if (nSecSeq<nt.getTransit().getMaxSequence()) nSecSeq ++;
						}
						else {
							if (nSecSeq>1) nSecSeq --;
						}
						nSec = nt.getTransit().getSectionListBySeq(nSecSeq).get(0);
						if (nSec==aSec) {
							if (nt.getTransit().getSectionListBySeq(nSecSeq).size()>1) {
								nSec = nt.getTransit().getSectionListBySeq(nSecSeq).get(1);
							}
							else {
								aSec = null;
							}
						}
					}
				}
			}
		}
		// if could not find a suitable passing siding, return 
		if ( (aSec==null) || (nSec==null) ) return false;
		// is there another train between these two trains
		if (neededByTrainList.size()>2) {
// djd debugging - need code here	
		}
		// set up allocation plan
		AllocationPlan ap = new AllocationPlan(this, nextPlanNum);
		nextPlanNum ++;
		ap.setPlanType(AllocationPlan.PASSING_MEET);
		ap.setActiveTrain(at,1);
		ap.setTargetSection(aSec,aSecSeq,1);
		ap.setActiveTrain(nt,2);
		ap.setTargetSection(nSec,nSecSeq,2);
		return true;
	}
	private int findPassingSection(ActiveTrain at,int aSeq) {
		// returns the sequence number of first area having alternate sections
		Transit t = at.getTransit();
		if (!at.isTransitReversed()) {
			for (int i = aSeq; i<=t.getMaxSequence(); i++) {
				if (t.getSectionListBySeq(i).size()>1) {
					return i;
				}
			}
		}
		else {
			for (int i = aSeq; i>=0; i--) {
				if (t.getSectionListBySeq(i).size()>1) {
					return i;
				}
			}
		}
		return 0;
	}
	private int willTraverse(Section s, ActiveTrain at, int seq) {
		Transit t = at.getTransit();
		if (!at.isTransitReversed()) {
			for (int i = seq; i<=t.getMaxSequence(); i++) {
				for (int j = 0; j<=t.getSectionListBySeq(i).size(); j++) {
					if (t.getSectionListBySeq(i).get(j) == s) return i;
				}
			}
		}
		else {
			for (int i = seq; i>=0; i--) {
				for (int j = 0; j<=t.getSectionListBySeq(i).size(); j++) {
					if (t.getSectionListBySeq(i).get(j) == s) return i;
				}
			}
		}
		return 0;
	}
	private boolean sectionNeeded (AllocationRequest ar, ActiveTrain at) {
		// returns 'true' if request section may be needed by specified train
		if ((ar==null) || (at==null)) {
			log.error("null argument on entry to 'sectionNeeded'");
			return false;
		}
		if (!(at.getTransit().containsSection(ar.getSection()))) return false;
		else if (at.getResetWhenDone()) return true;
		// this train may need this Section, has it already passed this Section?
		ArrayList<TransitSection> tsList = at.getTransit().getTransitSectionList();
		int curSeq = getCurrentSequenceNumber (at);
		if (!at.isTransitReversed()) {
			for (int i=0; i<tsList.size(); i++) {
				if ( (tsList.get(i).getSequenceNumber()>curSeq) && 
						(tsList.get(i).getSection()==ar.getSection()) ) {
					return true;
				}
			}
		}
		else {
			for (int i=tsList.size(); i>0; i--) {
				if ( (tsList.get(i).getSequenceNumber()<curSeq) && 
						(tsList.get(i).getSection()==ar.getSection()) ) {
					return true;
				}
			}
		}		
		return false;	
	}
	private boolean sameDirection(AllocationRequest ar, ActiveTrain at) {
		// returns 'true' if both trains will move thru the requested section in the same direction
		// NOTE: on return, _conTransitSection = at's Transit Section with the same Section as ar
		if ((ar==null) || (at==null)) {
			log.error("null argument on entry to 'sameDirection'");
			return false;
		}
		_conTransitSection = null;		
		ArrayList<TransitSection> tsList = at.getTransit().getTransitSectionList();
		int dir = 0;
		int curSeq = getCurrentSequenceNumber (at);
		if (!at.isTransitReversed()) {
			for (int i=0; i<tsList.size(); i++) {
				if ( (tsList.get(i).getSequenceNumber()>curSeq) && 
						(tsList.get(i).getSection()==ar.getSection()) ) {
					dir = tsList.get(i).getDirection();
					_conTransitSection = tsList.get(i);
				}
			}
		}
		else {
			for (int i=tsList.size(); i>0; i--) {
				if ( (tsList.get(i).getSequenceNumber()<curSeq) && 
						(tsList.get(i).getSection()==ar.getSection()) ) {
					dir = tsList.get(i).getDirection();
					_conTransitSection = tsList.get(i);
				}
			}
			if (dir==Section.FORWARD) dir = Section.REVERSE;
			else if (dir==Section.REVERSE) dir = Section.FORWARD;
			else {
				log.error("unknown direction for Section "+_conTransitSection.getSection().getUserName()+
									" in Transit "+at.getTransit().getUserName());
				return false;
			}
		}		
		if (dir==ar.getSectionDirection()) return true;		
		return false;
	}
	private TransitSection _conTransitSection = null;
	private int getCurrentSequenceNumber (ActiveTrain at) {
		// finds the current position of the head of the ActiveTrain in its Transit
		// returns sequence number of current position. getCurSection() returns Section.
		int seq = 0;
		curSection = null;
		if (at==null) {
			log.error("null argument on entry to 'getCurrentSeqNumber'");
			return seq;
		}
		Section temSection = null;
		ArrayList<TransitSection> tsList = at.getTransit().getTransitSectionList();
		if (!at.isTransitReversed()) {
			// find the highest numbered occupied section
			for (int i = 0; i<tsList.size(); i++) {
				if ( (tsList.get(i).getSection().getOccupancy()==Section.OCCUPIED) &&
					isSectionAllocatedToTrain( tsList.get(i).getSection(), 
							tsList.get(i).getSequenceNumber(), at) ) {
					seq = tsList.get(i).getSequenceNumber();
					temSection = tsList.get(i).getSection();
				}
			}
			if (seq == at.getTransit().getMaxSequence()) {
				if (at.getResetWhenDone()) {
					// train may have passed the last Section during continuous running
					boolean further = true;
					for (int j = 0; (j<tsList.size()) && further; j++) {
						if ( (tsList.get(j).getSection().getOccupancy()==Section.OCCUPIED) &&
								isSectionAllocatedToTrain( tsList.get(j).getSection(), 
										tsList.get(j).getSequenceNumber(), at) ) {
							seq = tsList.get(j).getSequenceNumber();
							temSection = tsList.get(j).getSection();
						}
						else further = false;
					}
				}
			}
		}
		else {
			// transit is running in reverse
			for (int i = tsList.size()-1; i>=0; i--) {
				if ( (tsList.get(i).getSection().getOccupancy()==Section.OCCUPIED) &&
					isSectionAllocatedToTrain( tsList.get(i).getSection(), 
							tsList.get(i).getSequenceNumber(), at) ) {
					seq = tsList.get(i).getSequenceNumber();
					temSection = tsList.get(i).getSection();
				}
			}
		}
		if (seq==0) log.error("ActiveTrain has no occupied Section");
		else curSection = temSection;
		return seq;
	}
	Section curSection = null;
	// Returns the Section with the sequence number returned by last call to getCurrentSequenceNumber
	private Section getCurSection() {return curSection;}
	private boolean isSectionAllocatedToTrain (Section s, int seq, ActiveTrain at) {
		if ( (s==null) || (at==null) ) {
			log.error("null argument to isSectionAllocatedToTrain");
			return false;
		}
		ArrayList<AllocatedSection> asList = at.getAllocatedSectionList();
		for (int i = 0; i<asList.size(); i++) {
			if ( (asList.get(i).getSection()==s) && asList.get(i).getSequence()==seq) {
				return true;
			}
		}
		return false;
	}		
	private boolean waitingForStartTime (AllocationRequest ar) {
		if (ar!=null) {
			ActiveTrain at = ar.getActiveTrain();
			if ( (!at.getStarted()) && at.getDelayedStart() ) return true;
		}
		return false;
	}
	private boolean willLevelXingsBlockTrain(ActiveTrain at) {
		// returns true if any LevelXings in _levelXingList will block the specified train
		if (at==null) {
			log.error("null argument on entry to 'willLevelXingsBlockTrain'");
			return true;  // returns true to be safe
		}
		if (_levelXingList.size()<=0) return false;
		for (int i=0; i<_levelXingList.size(); i++) {
			LevelXing lx = _levelXingList.get(i);
			Block bAC = lx.getLayoutBlockAC().getBlock();
			Block bBD = lx.getLayoutBlockBD().getBlock();	
			if ( at.getTransit().containsBlock(bAC) || at.getTransit().containsBlock(bBD) ) {
				return true;
			}
		}
		return false;
	}
	private boolean containsLevelXing(Section s) {
		// returns true if Section contains one or more level crossings
		// NOTE: changes _levelXingList!
		_levelXingList.clear();
		if (s==null) {
			log.error("null argument to 'containsLevelCrossing'");
			return false;
		}
		ArrayList<LevelXing> temLevelXingList = null;
		ArrayList<Block> blockList = s.getBlockList();
		for (int i=0; i<blockList.size(); i++) {
			temLevelXingList = _conUtil.getLevelCrossingsThisBlock(blockList.get(i));
			if (temLevelXingList.size()>0) {
				for (int j=0; j<temLevelXingList.size(); j++) {
					_levelXingList.add(temLevelXingList.get(j));
				}
			}
		}
		if (_levelXingList.size()>0) {
			return true;
		}
		return false;
	}
	ArrayList<LevelXing> _levelXingList = new ArrayList<LevelXing>();
					
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AutoAllocate.class.getName());
}

/* @(#)AutoAllocate.java */
