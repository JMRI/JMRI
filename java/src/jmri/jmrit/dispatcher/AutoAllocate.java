package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import java.util.List;
import jmri.Block;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Sensor;
import jmri.Transit;
import jmri.TransitSection;
import jmri.jmrit.display.layoutEditor.ConnectivityUtil;
import jmri.jmrit.display.layoutEditor.LevelXing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles automatic allocation of Sections for Dispatcher
 * <p>
 * AutoAllocate.java is an extension of DispatcherFrame.java.
 * <p>
 * When AutoAllocate is triggered, it scans the list of Allocation Requests, in
 * order of the priorities of ActiveTrains with pending AllocationRequests,
 * testing if a requested allocation can be made. AutoAllocate returns when
 * either: A Section has been allocated -or- All AllocationRequests have been
 * tested, and no allocation is indicated.
 * <p>
 * If AutoAllocate needs to save information related to a plan requiring
 * multiple allocations, an AllocationPlan object is created. When the plan is
 * complete, the AllocationPlan object is disposed of. Multiple AllocationPlan
 * objects may be active at any one time.
 * <p>
 * AutoAllocate is triggered by each of the following events: An
 * AllocatedSection has been released, freeing up a Section. A new
 * AllocationRequest has been entered into the queue of AllocationRequests. A
 * Section has been allocated, either by AutoAllocate or manually by the
 * dispatcher.
 * <p>
 * AutoAllocate requires that AutoRelease is active and that Dispatcher has a
 * LayoutEditor panel.
 * <p>
 * AutoAllocate operates conservatively, that is, if there is any doubt that a
 * Section should be allocated, it will not allocate the Section.
 * <p>
 * AutoAllocate develops plans for meets when multiple ActiveTrains are using
 * the same Sections of track. These plans are automatically created and
 * removed. They are stored in AllocationPlan objects to avoid having to
 * continually recreate them, since the logic to create them is rather
 * complicated.
 * <p>
 * The dispatcher is free to switch AutoAllocate on or off at any tine in
 * DispatcherFrame. When AutoAllocate is switched off, all existing
 * AllocationPlan objects are discarded.
 * <p>
 * <br>
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Dave Duchamp Copyright (C) 2011
 */
public class AutoAllocate {

    public AutoAllocate(DispatcherFrame d) {
        _dispatcher = d;
        if (_dispatcher == null) {
            log.error("null DispatcherFrame when constructing AutoAllocate");
            return;
        }
        if (_dispatcher.getLayoutEditor() == null) {
            log.error("null LayoutEditor when constructing AutoAllocate");
            return;
        }
        _conUtil = _dispatcher.getLayoutEditor().getConnectivityUtil();
    }

    // operational variables
    private DispatcherFrame _dispatcher = null;
    private ConnectivityUtil _conUtil = null;
    private final List<AllocationPlan> _planList = new ArrayList<>();
    private int nextPlanNum = 1;
    private final List<AllocationRequest> orderedRequests = new ArrayList<>();

    /**
     * This is the entry point to AutoAllocate when it is triggered.
     *
     * @param list list to scan
     */
    protected synchronized void scanAllocationRequestList(List<AllocationRequest> list) {
        boolean okToAllocate = false;
        if (list.size() <= 0) {
            return;
        }
        // copy AllocationRequests in order of priority of ActiveTrain.
        copyAndSortARs(list);
        removeCompletePlans();
        for (int i = 0; i < orderedRequests.size(); i++) {
            try {
                okToAllocate = false;
                AllocationRequest ar = orderedRequests.get(i);
                if (ar == null) {
                    log.error("error in allocation request list - AllocationRequest is null");
                    continue;
                }
                // Check to see if there is a sensor temporarily block allocation blocking allocation
                log.debug("Allocating Train [{}] section [{}]", ar.getActiveTrainName(), ar.getSectionName());
                if (ar.getActiveTrain().getLastAllocatedSection() != null) {
                    //do stuff associated with the last allocated section
                    Transit arTransit = ar.getActiveTrain().getTransit();
                    TransitSection arCurrentTransitSection =
                            arTransit.getTransitSectionFromSectionAndSeq(ar.getActiveTrain().getLastAllocatedSection(),
                                    ar.getActiveTrain().getLastAllocatedSectionSeqNumber());
                    if (stopAllocateSensorSet(ar.getActiveTrain(),arCurrentTransitSection)) {
                        log.debug("Skipping sensor active");
                        continue;
                    }
                    if (ar.getActiveTrain().getAllocateMethod() == ActiveTrain.ALLOCATE_BY_SAFE_SECTIONS) {
                        log.debug("Allocating Train [{}] Using Safe Sections", ar.getActiveTrainName());
                        // if the last allocated section is safe but not occupied short cut out of here
                        if (arCurrentTransitSection.isSafe() &&
                                ar.getActiveTrain().getLastAllocatedSection().getState() == Section.FREE) {
                            log.debug("Allocating Train [{}] has not arrived at Passing Point",
                                    ar.getActiveTrainName());
                            continue;
                        }
                        // Check all forward sections till a passing point.
                        int itSequ = ar.getSectionSeqNumber();
                        int iIncrement = 0;
                        int iLimit = 0;
                        int ix = 0;
                        boolean skip = false;
                        int iStart = 0;
                        if (ar.getActiveTrain().isTransitReversed()) {
                            iIncrement = -1;
                            iLimit = 0;
                            iStart = itSequ; //reverse transits start allocating from the next one, they allocate the one there in already
                        } else {
                            if (ar.getActiveTrain().getStartBlockSectionSequenceNumber() == ar.getSectionSeqNumber()) {
                                skip = true;
                            }
                            iIncrement = +1;
                            iLimit = arTransit.getMaxSequence() + 1;
                            iStart = itSequ;
                        }
                        if (!skip) {
                            boolean areForwardsFree = false;
                            log.debug("index [{}] Limit [{}] transitsize [{}]", ix, iLimit,
                                    arTransit.getTransitSectionList().size());
                            for (ix = iStart; ix != iLimit; ix += iIncrement) {
                                log.debug("index [{}] Limit [{}] transitsize [{}]", ix, iLimit,
                                        arTransit.getTransitSectionList().size());
                                // ensure all blocks section and blocks free till next Passing Point, check alternates if they exist.
                                Section sS;
                                ArrayList<TransitSection> sectionsInSeq = arTransit.getTransitSectionListBySeq(ix);
                                areForwardsFree = false; //Posit will be bad
                                log.debug("Search ALternates Size[{}]", sectionsInSeq.size());
                                int seqNumberfound = 0;
                                for (int iSectionsInSeq = 0; iSectionsInSeq < sectionsInSeq.size() &&
                                        !areForwardsFree; iSectionsInSeq++) {
                                    log.debug("iSectionInSeq[{}]", iSectionsInSeq);
                                    sS = sectionsInSeq.get(iSectionsInSeq).getSection();
                                    seqNumberfound = iSectionsInSeq; // save for later
                                    //debug code
                                    log.debug("SectionName[{}] getState[{}] occupancy[{}] ", sS.getUserName(),
                                            sS.getState(), sS.getOccupancy());
                                    if (sS.getState() != Section.FREE) {
                                        log.debug("Forward section unavailable[{}]", sS.getUserName());
                                        areForwardsFree = false;
                                    } else if (sS.getOccupancy() != Section.UNOCCUPIED) {
                                        log.debug("Forward section is not unoccupied [{}]", sS.getUserName());
                                        areForwardsFree = false;
                                    } else {
                                        areForwardsFree = true;
                                    }
                                }
                                if (!areForwardsFree) {
                                    break;
                                }
                                if (sectionsInSeq.get(seqNumberfound).isSafe()) {
                                    log.debug("Safe Section Found");
                                    break;
                                }
                            }
                            log.debug("ForwardsFree[{}]", areForwardsFree);
                            if (!areForwardsFree) {
                                continue;
                            }
                        }
                        if (allocateIfLessThanThreeAhead(ar)) {
                            continue;
                        }
                    }
                }
                log.debug("Using Regular");
                if (InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALMAST &&
                        isSignalHeldAtStartOfSection(ar)) {
                    continue;
                }
                if (getPlanThisTrain(ar.getActiveTrain()) != null) {
                    // this train is in an active Allocation Plan, anything to do now?
                    if (willAllocatingFollowPlan(ar, getPlanThisTrain(ar.getActiveTrain()))) {
                        if (allocateIfLessThanThreeAhead(ar)) {
                            continue;
                        }
                    }
                } else if (!waitingForStartTime(ar)) {
                    // train isn't waiting, continue only if requested Section is currently free and not occupied
                    if ((ar.getSection().getState() == Section.FREE) &&
                            (ar.getSection().getOccupancy() != Section.OCCUPIED) &&
                            (_dispatcher.getSignalType() == DispatcherFrame.SIGNALHEAD ||
                                    (_dispatcher.getSignalType() == DispatcherFrame.SIGNALMAST &&
                                            _dispatcher.checkBlocksNotInAllocatedSection(ar.getSection(),
                                                    ar) == null))) {
                        // requested Section is currently free and not occupied
                        List<ActiveTrain> activeTrainsList = _dispatcher.getActiveTrainsList();
                        if (activeTrainsList.size() == 1) {
                            // this is the only ActiveTrain
                            if (allocateIfLessThanThreeAhead(ar)) {
                                continue;
                            }
                        } else {
                            //check if any other ActiveTrain will need this Section or its alternates, if any
                            okToAllocate = true;
                            List<ActiveTrain> neededByTrainList = new ArrayList<>();
                            for (int j = 0; j < activeTrainsList.size(); j++) {
                                ActiveTrain at = activeTrainsList.get(j);
                                if (at != ar.getActiveTrain()) {
                                    if (sectionNeeded(ar, at)) {
                                        neededByTrainList.add(at);
                                    }
                                }
                            }
                            if (neededByTrainList.size() <= 0) {
                                // no other ActiveTrain needs this Section, any LevelXings?
                                if (containsLevelXing(ar.getSection())) {
                                    // check if allocating this Section might block a higher priority train
                                    for (int j = 0; j < activeTrainsList.size(); j++) {
                                        ActiveTrain at = activeTrainsList.get(j);
                                        if ((at != ar.getActiveTrain()) &&
                                                (at.getPriority() > ar.getActiveTrain().getPriority())) {
                                            if (willLevelXingsBlockTrain(at)) {
                                                okToAllocate = false;
                                            }
                                        }
                                    }
                                }
                            } else {
                                // requested Section (or alternate) is needed by other active Active Train(s)
                                for (int k = 0; k < neededByTrainList.size(); k++) {
                                    // section is also needed by this active train
                                    ActiveTrain nt = neededByTrainList.get(k);
                                    // are trains moving in same direction through the requested Section?
                                    if (sameDirection(ar, nt)) {
                                        // trains will move in the same direction thru requested section
                                        if (firstTrainLeadsSecond(ar.getActiveTrain(), nt) &&
                                                (nt.getPriority() > ar.getActiveTrain().getPriority())) {
                                            // a higher priority train is trailing this train, can we let it pass?
                                            if (checkForPassingPlan(ar, nt, neededByTrainList)) {
                                                // PASSING_MEET plan created
                                                if (!willAllocatingFollowPlan(ar,
                                                        getPlanThisTrain(ar.getActiveTrain()))) {
                                                    okToAllocate = false;
                                                }
                                            }
                                        }
                                    } else {
                                        // trains will move in opposite directions thru requested section
                                        //   explore possibility of an XING_MEET to avoid gridlock
                                        if (willTrainsCross(ar.getActiveTrain(), nt)) {
                                            if (checkForXingPlan(ar, nt, neededByTrainList)) {
                                                // XING_MEET plan created
                                                if (!willAllocatingFollowPlan(ar,
                                                        getPlanThisTrain(ar.getActiveTrain()))) {
                                                    okToAllocate = false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (okToAllocate) {
                                if (allocateIfLessThanThreeAhead(ar)) {
                                    continue;
                                }
                            }
                        }
                    }
                }
            } catch (RuntimeException e) {
                log.warn(
                        "scanAllocationRequestList - maybe the allocationrequest was removed due to a terminating train??" +
                                e.toString());
                continue;
            }
        }
    }

    /**
     * Entered to request a choice of Next Section when a Section is being
     * allocated and there are alternate Section choices for the next Section.
     *
     * @param sList the possible next Sections
     * @param ar the section being allocated when a choice is needed
     * @return the allocated section
     */
    protected Section autoNextSectionChoice(List<Section> sList, AllocationRequest ar) {
        // check if AutoAllocate has prepared for this question
        if ((savedAR != null) && (savedAR == ar)) {
            for (int j = 0; j < sList.size(); j++) {
                if (savedSection == sList.get(j)) {
                    return savedSection;
                }
            }
            log.warn("Failure of prepared choice of next Section in AutoAllocate");
        }
        // Jay Janzen
        // If there is an AP check to see if the AP's target is on the list of choices
        // and if so, return that.
        ActiveTrain at = ar.getActiveTrain();
        AllocationPlan ap = getPlanThisTrain(at);
        Section as = null;
        if (ap != null) {
            if (ap.getActiveTrain(1) == at) {
                as = ap.getTargetSection(1);
            } else if (ap.getActiveTrain(2) == at) {
                as = ap.getTargetSection(2);
            } else {
                return null;
            }
            for (int i = 0; i < sList.size(); i++) {
                if (as != null && as == sList.get(i)) {
                    return as;
                }
            }
        }
        // If our end block section is on the list of choices
        // return that occupied or not. In the list of choices the primary occurs
        // ahead any alternates, so if our end block is an alternate and its
        // primary is unoccupied, the search will select the primary and
        // we wind up skipping right over our end section.
        for (int i = 0; i < sList.size(); i++) {
            if (at.getEndBlockSection().getSystemName().equals(sList.get(i).getSystemName())) {
                return sList.get(i);
            }
        }
        // no prepared choice, or prepared choice failed, is there an unoccupied Section available
        for (int i = 0; i < sList.size(); i++) {
            if ((sList.get(i).getOccupancy() == Section.UNOCCUPIED) &&
                    (sList.get(i).getState() == Section.FREE) &&
                    (_dispatcher.getSignalType() == DispatcherFrame.SIGNALHEAD ||
                            (_dispatcher.getSignalType() == DispatcherFrame.SIGNALMAST &&
                                    _dispatcher.checkBlocksNotInAllocatedSection(sList.get(i), ar) == null))) {
                return sList.get(i);
            }
        }
        // no unoccupied Section available, check for Section allocated in same direction as this request
        int dir = ar.getSectionDirection();
        List<AllocatedSection> allocatedSections = _dispatcher.getAllocatedSectionsList();
        for (int m = 0; m < sList.size(); m++) {
            boolean notFound = true;
            for (int k = 0; (k < allocatedSections.size()) && notFound; k++) {
                if (sList.get(m) == allocatedSections.get(k).getSection()) {
                    notFound = false;
                    if (allocatedSections.get(k).getSection().getState() == dir) {
                        return sList.get(m);
                    }
                }
            }
        }
        // if all else fails, return null so Dispatcher will ask the dispatcher to choose
        return null;
    }

    private final AllocationRequest savedAR = null;
    private final Section savedSection = null;

    // private implementation methods
    private void copyAndSortARs(List<AllocationRequest> list) {
        orderedRequests.clear();
        // copy across and then sort...
        for (int i = 0; i < list.size(); i++) {
            orderedRequests.add(list.get(i));
        }
        orderedRequests.sort((AllocationRequest e1, AllocationRequest e2) -> {
            if (e1.getActiveTrain().getPriority() < e2.getActiveTrain().getPriority()) {
                return 1;
            } else if (e1.getActiveTrain().getPriority() > e2.getActiveTrain().getPriority()) {
                return -1;
            } else {
                return e1.getActiveTrain().getTrainName().compareTo(e2.getActiveTrain().getTrainName());
            }
        });
    }

    /**
     * Check whether it is nessassary to pause/stop further allocation because a
     * specified sensor is active.
     *
     * @param lastAllocatedTransitSection
     * @return true stop allocating, false dont
     */
    private boolean stopAllocateSensorSet(ActiveTrain at, TransitSection lastAllocatedTransitSection) {
        if (lastAllocatedTransitSection.getStopAllocatingSensor() != null &&
                !lastAllocatedTransitSection.getStopAllocatingSensor().equals("")) {
            String sensorName = lastAllocatedTransitSection.getStopAllocatingSensor();
            Sensor sensor;
            try {
                sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
                if (sensor.getKnownState() == Sensor.ACTIVE) {
                    log.debug("Sensor[{}] InActive", sensor.getUserName());
                    at.initializeRestartAllocationSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor));
                    return true;
                }
            } catch (NumberFormatException ex) {
                log.error("Error with pause/stop allocation sensor[{}]", sensorName, ex);
                return false;
            }
        }
        return false;
    }

    private AllocationPlan getPlanThisTrain(ActiveTrain at) {
        for (int i = 0; i < _planList.size(); i++) {
            AllocationPlan ap = _planList.get(i);
            for (int j = 1; j < 3; j++) {
                if (ap.getActiveTrain(j) == at) {
                    return ap;
                }
            }
        }
        // train not in an AllocationPlan
        return null;
    }

    private boolean willAllocatingFollowPlan(AllocationRequest ar, AllocationPlan ap) {
        // return 'true' if this AllocationRequest is consistent with specified plan,
        //     returns 'false' otherwise
        ActiveTrain at = ar.getActiveTrain();
        int cTrainNum = 0;
        if (ap.getActiveTrain(1) == at) {
            cTrainNum = 1;
        } else if (ap.getActiveTrain(2) == at) {
            cTrainNum = 2;
        } else {
            log.error("Requesting train not in Allocation Plan");
            return false;
        }
        if (!at.isAllocationReversed()) {
            if (ap.getTargetSectionSequenceNum(cTrainNum) >= ar.getSectionSeqNumber()) {
                if ((ar.getSection().getState() == Section.FREE) &&
                        (ar.getSection().getOccupancy() != Section.OCCUPIED)) {
                    return true;
                }
            }
        } else {
            if (ap.getTargetSectionSequenceNum(cTrainNum) <= ar.getSectionSeqNumber()) {
                if ((ar.getSection().getState() == Section.FREE) &&
                        (ar.getSection().getOccupancy() != Section.OCCUPIED)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeCompletePlans() {
        boolean foundCompletePlan = true;
        while (foundCompletePlan) {
            foundCompletePlan = false;
            for (int i = 0; (!foundCompletePlan) && (i < _planList.size()); i++) {
                // remove if all planned allocations have been made
                foundCompletePlan = _planList.get(i).isComplete();
                if (foundCompletePlan) {
                    _planList.get(i).dispose();
                    _planList.remove(i);
                }
            }
        }
    }

    protected void clearAllocationPlans() {
        for (int i = _planList.size() - 1; i >= 0; i--) {
            AllocationPlan ap = _planList.get(i);
            _planList.remove(i);
            ap.dispose();
        }
    }

    // test to see how far ahead allocations have already been made
    // and go no farther than the number requested, or the next safe section.
    private boolean allocateIfLessThanThreeAhead(AllocationRequest ar) {
        int allocateSectionsAhead = ar.getActiveTrain().getAllocateMethod();
        if (allocateSectionsAhead == ActiveTrain.ALLOCATE_AS_FAR_AS_IT_CAN) {
            _dispatcher.allocateSection(ar, null);
            return true;
        }
        // test how far ahead of occupied track this requested section is
        List<AllocatedSection> aSectionList = ar.getActiveTrain().getAllocatedSectionList();
        boolean allocateBySafeSections = false;
        // check for allocating By Safe Sections
        if (allocateSectionsAhead == 0) {
            // check for type of allocating N ahead or until passing
            allocateBySafeSections = true;
        }
        if ((allocateBySafeSections && aSectionList.size() >= 1) ||
                (!allocateBySafeSections && aSectionList.size() >= (allocateSectionsAhead + 1))) {
            int curSeq = ar.getSectionSeqNumber() - 1;
            if (ar.getActiveTrain().isAllocationReversed()) {
                curSeq = ar.getSectionSeqNumber() + 1;
            }
            if ((curSeq == 1) && ar.getActiveTrain().getResetWhenDone()) {
                curSeq = ar.getActiveTrain().getTransit().getMaxSequence();
            }
            AllocatedSection curAS = null;
            for (int i = aSectionList.size() - 1; i >= 0; i--) {
                AllocatedSection as = aSectionList.get(i);
                if ((as != null) && (as.getSequence() == curSeq)) {
                    curAS = as;
                }
            }
            if (allocateBySafeSections &&
                    (curAS != null) &&
                    ((curAS.getSection().getOccupancy() != jmri.Section.OCCUPIED) &&
                            ar.getActiveTrain().getTransit().getTransitSectionFromSectionAndSeq(curAS.getSection(),
                                    curSeq).isSafe())) {
                // last allocated section exists and is not occupied but is a Passing point
                // block further allocations till occupied.
                log.debug("auto allocating Section returning false");
                return false;
            } else if (allocateBySafeSections) {
                log.debug("auto allocating Section keep going");
                _dispatcher.allocateSection(ar, null);
                return true;
            }
            log.debug("Auto allocating by count");
            int numberAllocatedButUnoccupied = 0;
            for (int i = aSectionList.size() - 1; i >= 0; i--) {
                AllocatedSection as = aSectionList.get(i);
                if ((as != null) && (as.getSection().getOccupancy() != jmri.Section.OCCUPIED && !as.getExited())) {
                    numberAllocatedButUnoccupied++;
                }
            }
            log.debug("FinalCounter[{}]", numberAllocatedButUnoccupied);
            if (numberAllocatedButUnoccupied < allocateSectionsAhead) {
                _dispatcher.allocateSection(ar, null);
                return true;
            }
            return false;

        }
        log.debug("{}: auto allocating Section {}", ar.getActiveTrain().getTrainName(),
                ar.getSectionName());
        _dispatcher.allocateSection(ar, null);
        return true;
    }

    private boolean checkForXingPlan(AllocationRequest ar, ActiveTrain nt,
            List<ActiveTrain> neededByTrainList) {
        // returns 'true' if an AllocationPlan has been set up, returns 'false' otherwise
        Section nSec = null;
        Section aSec = null;
        int nSecSeq = 0;
        int aSecSeq = 0;
        ActiveTrain at = ar.getActiveTrain();
        AllocationPlan apx = getPlanThisTrain(nt);
        if (apx != null) {
            if (apx.getPlanType() != AllocationPlan.XING_MEET) {
                return false;
            }
            // already in a XING_MEET Allocation Plan - find target Section and sequence
            if (apx.getActiveTrain(1) == nt) {
                nSecSeq = apx.getTargetSectionSequenceNum(1);
                nSec = apx.getTargetSection(1);
            } else {
                nSecSeq = apx.getTargetSectionSequenceNum(2);
                nSec = apx.getTargetSection(2);
            }
            List<Section> nSections = nt.getTransit().getSectionListBySeq(nSecSeq);
            if (nSections.size() <= 1) {
                return false;
            }
            // is a passing siding, find a suitable track
            aSec = getBestOtherSection(nSections, nSec);
            if (aSec == null) {
                return false;
            }
            aSecSeq = willTraverse(aSec, at, getCurrentSequenceNumber(at));
            if (aSecSeq == 0) {
                return false;
            }
        } else {
            // neither train is in an AllocationPlan currently, check for suitable passing siding
            int aSeq = ar.getSectionSeqNumber();
            // is an alternate Section available here or ahead
            aSecSeq = findPassingSection(at, aSeq);
            if (aSecSeq == 0) {
                // none in at's Transit, is there one in nt's Transit
                int nCurrentSeq = getCurrentSequenceNumber(nt);
                nSecSeq = findPassingSection(nt, nCurrentSeq);
                if (nSecSeq > 0) {
                    // has passing section ahead, will this train traverse a Section in it
                    List<Section> nSections = nt.getTransit().getSectionListBySeq(nSecSeq);
                    for (int i = 0; (i < nSections.size()) && (aSec == null); i++) {
                        aSecSeq = willTraverse(nSections.get(i), at, aSeq);
                        if (aSecSeq > 0) {
                            aSec = at.getTransit().getSectionListBySeq(aSecSeq).get(0);
                        }
                    }
                    if (aSec != null) {
                        // found passing Section that should work out
                        nSec = getBestOtherSection(nSections, aSec);
                    }
                }
            } else {
                // will other train go through any of these alternate sections
                List<Section> aSections = at.getTransit().getSectionListBySeq(aSecSeq);
                int nCurrentSeq = getCurrentSequenceNumber(nt);
                for (int i = 0; (i < aSections.size()) && (aSec == null); i++) {
                    nSecSeq = willTraverse(aSections.get(i), nt, nCurrentSeq);
                    if (nSecSeq > 0) {
                        nSec = aSections.get(i);
                        aSec = getBestOtherSection(aSections, nSec);
                    }
                }
            }
            // if could not find a suitable siding for a crossing meet, return
            if ((aSec == null) || (nSec == null)) {
                return false;
            }
        }
        // check for conflicting train or conflicting plan that could cause gridlock
        if (neededByTrainList.size() > 2) {
            // is there another train between these two
            if (!areTrainsAdjacent(at, nt)) {
                return false;
            }
            if (isThereConflictingPlan(at, aSec, aSecSeq, nt, nSec, nSecSeq,
                    AllocationPlan.XING_MEET)) {
                return false;
            }
        }
        // set up allocation plan
        AllocationPlan ap = new AllocationPlan(this, nextPlanNum);
        nextPlanNum++;
        ap.setPlanType(AllocationPlan.XING_MEET);
        ap.setActiveTrain(at, 1);
        ap.setTargetSection(aSec, aSecSeq, 1);
        ap.setActiveTrain(nt, 2);
        ap.setTargetSection(nSec, nSecSeq, 2);
        _planList.add(ap);
        return true;
    }

    private boolean checkForPassingPlan(AllocationRequest ar, ActiveTrain nt,
            List<ActiveTrain> neededByTrainList) {
        // returns 'true' if an AllocationPlan has been set up, returns 'false' otherwise
        Section nSec = null;
        Section aSec = null;
        int nSecSeq = 0;
        int aSecSeq = 0;
        ActiveTrain at = ar.getActiveTrain();
        AllocationPlan apx = getPlanThisTrain(nt);
        if (apx != null) {
            if (apx.getPlanType() != AllocationPlan.PASSING_MEET) {
                return false;
            }
            // already in a PASSING_MEET Allocation Plan - find target Section and sequence
            Section oSection = null;
            //            ActiveTrain oTrain = null;
            if (apx.getActiveTrain(1) == nt) {
                nSecSeq = apx.getTargetSectionSequenceNum(1);
                nSec = apx.getTargetSection(1);
                oSection = apx.getTargetSection(2);
                //                oTrain = apx.getActiveTrain(2);
            } else {
                nSecSeq = apx.getTargetSectionSequenceNum(2);
                nSec = apx.getTargetSection(2);
                oSection = apx.getTargetSection(1);
                //                oTrain = apx.getActiveTrain(1);
            }
            int aCurrentSeq = getCurrentSequenceNumber(at);
            aSecSeq = willTraverse(nSec, at, aCurrentSeq);
            if (aSecSeq == 0) {
                return false;
            }
            //            int tnSecSeq = nSecSeq;
            //            if (nt.getPriority() > oTrain.getPriority()) {
            //                if (!nt.isAllocationReversed()) {
            //                    tnSecSeq--;
            //                    if (tnSecSeq <= 0) {
            //                        tnSecSeq = nSecSeq;
            //                    }
            //                } else {
            //                    tnSecSeq++;
            //                    if (tnSecSeq > nt.getTransit().getMaxSequence()) {
            //                        tnSecSeq = nSecSeq;
            //                    }
            //                }
            //            }
            List<Section> nSections = nt.getTransit().getSectionListBySeq(nSecSeq);
            if (nSections.size() <= 1) {
                return false;
            }
            // is a passing siding, find a suitable track
            for (int i = 0; (i < nSections.size()) && (aSec == null); i++) {
                if (nSections.get(i) == oSection) {
                    aSecSeq = willTraverse(nSections.get(i), at, aCurrentSeq);
                    if (aSecSeq > 0) {
                        aSec = nSections.get(i);
                    }
                }
            }
            if (aSec == null) {
                for (int i = 0; (i < nSections.size()) && (aSec == null); i++) {
                    if (nSections.get(i) != nSec) {
                        aSecSeq = willTraverse(nSections.get(i), at, aCurrentSeq);
                        if (aSecSeq > 0) {
                            aSec = nSections.get(i);
                        }
                    }
                }
            }
            if (aSec == null) {
                return false;
            }
        } else {
            // both trains are not in Allocation plans
            int aSeq = ar.getSectionSeqNumber();
            // is an alternate Section available here or ahead
            aSecSeq = findPassingSection(at, aSeq);
            if (aSecSeq == 0) {
                // does higher priority train have a passing section ahead
                int nCurrentSeq = getCurrentSequenceNumber(nt);
                nSecSeq = findPassingSection(nt, nCurrentSeq);
                if (nSecSeq > 0) {
                    // has passing section ahead, will this train traverse a Section in it
                    List<Section> nSections = nt.getTransit().getSectionListBySeq(nSecSeq);
                    for (int i = 0; (i < nSections.size()) && (aSec == null); i++) {
                        aSecSeq = willTraverse(nSections.get(i), at, aSeq);
                        if (aSecSeq > 0) {
                            aSec = at.getTransit().getSectionListBySeq(aSecSeq).get(0);
                        }
                    }
                    if (aSec != null) {
                        // found passing Section that should work out
                        nSec = getBestOtherSection(nSections, aSec);
                    }
                }
            } else {
                // will the higher priority train go through any of these alternate sections
                List<Section> aSections = at.getTransit().getSectionListBySeq(aSecSeq);
                int nCurrentSeq = getCurrentSequenceNumber(nt);
                for (int i = 0; (i < aSections.size()) && (aSec == null); i++) {
                    nSecSeq = willTraverse(aSections.get(i), nt, nCurrentSeq);
                    if (nSecSeq > 0) {
                        nSec = aSections.get(i);
                        aSec = getBestOtherSection(aSections, nSec);
                    }
                }
            }
            // if could not find a suitable passing siding, return
            if ((aSec == null) || (nSec == null)) {
                return false;
            }
            //     push higher priority train one section further, if possible
            if (!nt.isAllocationReversed()) {
                if (nSecSeq < nt.getTransit().getMaxSequence()) {
                    nSecSeq++;
                    nSec = nt.getTransit().getSectionListBySeq(nSecSeq).get(0);
                }
            } else {
                if (nSecSeq > 1) {
                    nSecSeq--;
                    nSec = nt.getTransit().getSectionListBySeq(nSecSeq).get(0);
                }
            }
        }
        // is there another train trying to let this high priority train pass
        if (neededByTrainList.size() > 2) {
            // Note: e.g. Two lower priority trains ahead of a high priority train could cause gridlock
            //    if both try to set up a PASSING_PLAN meet at the same place, so we exclude that case.
            // is there another train between these two
            if (!areTrainsAdjacent(at, nt)) {
                return false;
            }
            if (isThereConflictingPlan(at, aSec, aSecSeq, nt, nSec, nSecSeq,
                    AllocationPlan.PASSING_MEET)) {
                return false;
            }
        }
        // set up allocation plan
        AllocationPlan ap = new AllocationPlan(this, nextPlanNum);
        nextPlanNum++;
        ap.setPlanType(AllocationPlan.PASSING_MEET);
        ap.setActiveTrain(at, 1);
        ap.setTargetSection(aSec, aSecSeq, 1);
        ap.setActiveTrain(nt, 2);
        ap.setTargetSection(nSec, nSecSeq, 2);
        _planList.add(ap);
        return true;
    }

    private boolean isThereConflictingPlan(ActiveTrain at, Section aSec, int aSecSeq,
            ActiveTrain nt, Section nSec, int nSecSeq, int type) {
        // returns 'true' if there is a conflicting plan that may result in gridlock
        //    if this plan is set up, return 'false' if not.
        // Note: may have to add other tests to this method in the future to prevent gridlock
        //   situations not currently tested for.
        if (_planList.size() == 0) {
            return false;
        }
        for (int i = 0; i < _planList.size(); i++) {
            AllocationPlan ap = _planList.get(i);
            // check if this plan involves the second train (it'll never involve the first)
            int trainNum = 0;
            if (ap.getActiveTrain(1) == nt) {
                trainNum = 1;
            } else if (ap.getActiveTrain(2) == nt) {
                trainNum = 2;
            }
            if (trainNum > 0) {
                // check consistency - same type, section, and sequence number
                if ((ap.getPlanType() != type) ||
                        (ap.getTargetSection(trainNum) != nSec) ||
                        (ap.getTargetSectionSequenceNum(trainNum) != nSecSeq)) {
                    return true;
                }
            } else {
                // different trains, does this plan use the same Passing Section?
                List<Section> aSections = at.getTransit().getSectionListBySeq(aSecSeq);
                for (int j = 0; j < aSections.size(); j++) {
                    if ((aSections.get(j) == ap.getTargetSection(1)) || (aSections.get(j) == ap.getTargetSection(2))) {
                        return true;
                    }
                }
            }
        }
        // passes all tests
        return false;
    }

    private Section getBestOtherSection(List<Section> sList, Section aSec) {
        // returns the best Section from the list that is not aSec, or else return null
        for (int i = 0; i < sList.size(); i++) {
            if ((sList.get(i) != aSec) &&
                    (sList.get(i).getState() == Section.FREE) &&
                    (sList.get(i).getOccupancy() != Section.OCCUPIED)) {
                return sList.get(i);
            }
        }
        for (int i = 0; i < sList.size(); i++) {
            if ((sList.get(i) != aSec) && (sList.get(i).getOccupancy() != Section.OCCUPIED)) {
                return sList.get(i);
            }
        }
        for (int i = 0; i < sList.size(); i++) {
            if (sList.get(i) != aSec) {
                return sList.get(i);
            }
        }
        return null;
    }

    private int findPassingSection(ActiveTrain at, int aSeq) {
        // returns the sequence number of first area having alternate sections
        Transit t = at.getTransit();
        if (!at.isTransitReversed()) {
            for (int i = aSeq; i <= t.getMaxSequence(); i++) {
                if (t.getSectionListBySeq(i).size() > 1) {
                    return i;
                }
            }
        } else {
            for (int i = aSeq; i >= 0; i--) {
                if (t.getSectionListBySeq(i).size() > 1) {
                    return i;
                }
            }
        }
        return 0;
    }

    private int willTraverse(Section s, ActiveTrain at, int seq) {
        Transit t = at.getTransit();
        if (!at.isTransitReversed()) {
            for (int i = seq; i <= t.getMaxSequence(); i++) {
                for (int j = 0; j < t.getSectionListBySeq(i).size(); j++) {
                    if (t.getSectionListBySeq(i).get(j) == s) {
                        return i;
                    }
                }
            }
        } else {
            for (int i = seq; i >= 0; i--) {
                for (int j = 0; j < t.getSectionListBySeq(i).size(); j++) {
                    if (t.getSectionListBySeq(i).get(j) == s) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    private boolean sectionNeeded(AllocationRequest ar, ActiveTrain at) {
        // returns 'true' if request section, or its alternates, will be needed by specified train
        if ((ar == null) || (at == null)) {
            log.error("null argument on entry to 'sectionNeeded'");
            return false;
        }
        List<Section> aSectionList = ar.getActiveTrain().getTransit().getSectionListBySeq(
                ar.getSectionSeqNumber());
        boolean found = false;
        for (int i = 0; i < aSectionList.size(); i++) {
            if (!(at.getTransit().containsSection(aSectionList.get(i)))) {
                found = true;
            }
        }
        if (!found) {
            return false;
        } else if ((at.getResetWhenDone()) || (at.getReverseAtEnd() && (!at.isAllocationReversed()))) {
            return true;
        }
        // this train may need this Section, has it already passed this Section?
        List<TransitSection> tsList = at.getTransit().getTransitSectionList();
        int curSeq = getCurrentSequenceNumber(at);
        if (!at.isAllocationReversed()) {
            for (int i = 0; i < tsList.size(); i++) {
                if (tsList.get(i).getSequenceNumber() > curSeq) {
                    for (int j = 0; j < aSectionList.size(); j++) {
                        if (tsList.get(i).getSection() == aSectionList.get(j)) {
                            return true;
                        }
                    }
                }
            }
        } else {
            for (int i = tsList.size() - 1; i >= 0; i--) {
                if (tsList.get(i).getSequenceNumber() < curSeq) {
                    for (int j = 0; j < aSectionList.size(); j++) {
                        if (tsList.get(i).getSection() == aSectionList.get(j)) {
                            return true;
                        }
                    }
                }
            }
        }
        if (InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALMAST) {
            if (!at.isAllocationReversed()) {
                for (int i = 0; i < tsList.size(); i++) {
                    if (tsList.get(i).getSequenceNumber() > curSeq) {
                        for (int j = 0; j < aSectionList.size(); j++) {
                            if (tsList.get(i).getSection() == aSectionList.get(j)) {
                                return true;
                            }
                        }
                    }
                }
            } else {
                for (int i = tsList.size() - 1; i >= 0; i--) {
                    if (tsList.get(i).getSequenceNumber() < curSeq) {
                        for (int j = 0; j < aSectionList.size(); j++) {
                            if (tsList.get(i).getSection() == aSectionList.get(j)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean sameDirection(AllocationRequest ar, ActiveTrain at) {
        // returns 'true' if both trains will move thru the requested section in the same direction
        if ((ar == null) || (at == null)) {
            log.error("null argument on entry to 'sameDirection'");
            return false;
        }
        List<TransitSection> tsList = at.getTransit().getTransitSectionList();
        List<TransitSection> rtsList = ar.getActiveTrain().getTransit().getTransitSectionListBySeq(
                ar.getSectionSeqNumber());
        int curSeq = getCurrentSequenceNumber(at);
        if (!at.isAllocationReversed()) {
            for (int i = 0; i < tsList.size(); i++) {
                if (tsList.get(i).getSequenceNumber() > curSeq) {
                    for (int k = 0; k < rtsList.size(); k++) {
                        if ((tsList.get(i).getSection() == rtsList.get(k).getSection()) &&
                                (tsList.get(i).getDirection() == rtsList.get(k).getDirection())) {
                            return true;
                        }
                    }
                }
            }
        } else {
            for (int i = tsList.size() - 1; i >= 0; i--) {
                if (tsList.get(i).getSequenceNumber() < curSeq) {
                    for (int k = 0; k < rtsList.size(); k++) {
                        if ((tsList.get(i).getSection() == rtsList.get(k).getSection()) &&
                                (tsList.get(i).getDirection() == rtsList.get(k).getDirection())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean firstTrainLeadsSecond(ActiveTrain at, ActiveTrain nt) {
        int aSeq = getCurrentSequenceNumber(at);
        Section aSec = getCurSection();
        int nSeq = getCurrentSequenceNumber(nt);
        Section nSec = getCurSection();
        List<TransitSection> atsList = at.getTransit().getTransitSectionList();
        if (!at.isTransitReversed()) {
            for (int i = 0; i < atsList.size(); i++) {
                if (atsList.get(i).getSequenceNumber() > aSeq) {
                    if (atsList.get(i).getSection() == nSec) {
                        // first train has not yet reached second train position
                        return false;
                    }
                }
            }
        } else {
            for (int i = atsList.size() - 1; i <= 0; i--) {
                if (atsList.get(i).getSequenceNumber() < aSeq) {
                    if (atsList.get(i).getSection() == nSec) {
                        // first train has not yet reached second train position
                        return false;
                    }
                }
            }
        }
        List<TransitSection> ntsList = nt.getTransit().getTransitSectionList();
        if (!nt.isTransitReversed()) {
            for (int i = 0; i < ntsList.size(); i++) {
                if (ntsList.get(i).getSequenceNumber() > nSeq) {
                    if (ntsList.get(i).getSection() == aSec) {
                        // second train has found first train in its on coming Sections
                        return true;
                    }
                }
            }
        } else {
            for (int i = ntsList.size() - 1; i <= 0; i--) {
                if (ntsList.get(i).getSequenceNumber() < nSeq) {
                    if (ntsList.get(i).getSection() == aSec) {
                        // second train has found first train in its on coming Sections
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean willTrainsCross(ActiveTrain at, ActiveTrain nt) {
        // returns true if both trains will eventually reach the others position
        int aSeq = getCurrentSequenceNumber(at);
        Section aSec = getCurSection();
        int nSeq = getCurrentSequenceNumber(nt);
        Section nSec = getCurSection();
        List<TransitSection> atsList = at.getTransit().getTransitSectionList();
        boolean found = false;
        if (!at.isTransitReversed()) {
            for (int i = 0; (i < atsList.size()) && (!found); i++) {
                if (atsList.get(i).getSequenceNumber() > aSeq) {
                    if (atsList.get(i).getSection() == nSec) {
                        // first train has reached second train position
                        found = true;
                    }
                }
            }
        } else {
            for (int i = atsList.size() - 1; (i <= 0) && (!found); i--) {
                if (atsList.get(i).getSequenceNumber() < aSeq) {
                    if (atsList.get(i).getSection() == nSec) {
                        // first train has reached second train position
                        found = true;
                    }
                }
            }
        }
        if (!found) {
            return false;
        }
        List<TransitSection> ntsList = nt.getTransit().getTransitSectionList();
        if (!nt.isTransitReversed()) {
            for (int i = 0; i < ntsList.size(); i++) {
                if (ntsList.get(i).getSequenceNumber() > nSeq) {
                    if (ntsList.get(i).getSection() == aSec) {
                        // second train has found first train in its on coming Sections
                        return true;
                    }
                }
            }
        } else {
            for (int i = ntsList.size() - 1; i <= 0; i--) {
                if (ntsList.get(i).getSequenceNumber() < nSeq) {
                    if (ntsList.get(i).getSection() == aSec) {
                        // second train has found first train in its on coming Sections
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean areTrainsAdjacent(ActiveTrain at, ActiveTrain nt) {
        // returns 'false' if a different ActiveTrain has allocated track between the
        //      two trains, returns 'true' otherwise
        List<AllocatedSection> allocatedSections = _dispatcher.getAllocatedSectionsList();
        List<TransitSection> atsList = at.getTransit().getTransitSectionList();
        int aSeq = getCurrentSequenceNumber(at);
        Section nSec = getCurSection();
        if (willTraverse(nSec, at, aSeq) != 0) {
            // at is moving toward nt
            if (!at.isTransitReversed()) {
                for (int i = 0; i < atsList.size(); i++) {
                    if (atsList.get(i).getSequenceNumber() > aSeq) {
                        Section tSec = atsList.get(i).getSection();
                        if (tSec == nSec) {
                            // reached second train position, no train in between
                            return true;
                        } else {
                            for (int j = 0; j < allocatedSections.size(); j++) {
                                if (allocatedSections.get(j).getSection() == tSec) {
                                    if ((allocatedSections.get(j).getActiveTrain() != at) &&
                                            (allocatedSections.get(j).getActiveTrain() != nt)) {
                                        // allocated to a third train, trains not adjacent
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                for (int i = atsList.size() - 1; i <= 0; i--) {
                    if (atsList.get(i).getSequenceNumber() < aSeq) {
                        Section tSec = atsList.get(i).getSection();
                        if (tSec == nSec) {
                            // reached second train position, no train in between
                            return true;
                        } else {
                            for (int j = 0; j < allocatedSections.size(); j++) {
                                if (allocatedSections.get(j).getSection() == tSec) {
                                    if ((allocatedSections.get(j).getActiveTrain() != at) &&
                                            (allocatedSections.get(j).getActiveTrain() != nt)) {
                                        // allocated to a third train, trains not adjacent
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // at is moving away from nt, so backtrack
            if (at.isTransitReversed()) {
                for (int i = 0; i < atsList.size(); i++) {
                    if (atsList.get(i).getSequenceNumber() > aSeq) {
                        Section tSec = atsList.get(i).getSection();
                        if (tSec == nSec) {
                            // reached second train position, no train in between
                            return true;
                        } else {
                            for (int j = 0; j < allocatedSections.size(); j++) {
                                if (allocatedSections.get(j).getSection() == tSec) {
                                    if ((allocatedSections.get(j).getActiveTrain() != at) &&
                                            (allocatedSections.get(j).getActiveTrain() != nt)) {
                                        // allocated to a third train, trains not adjacent
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                for (int i = atsList.size() - 1; i <= 0; i--) {
                    if (atsList.get(i).getSequenceNumber() < aSeq) {
                        Section tSec = atsList.get(i).getSection();
                        if (tSec == nSec) {
                            // reached second train position, no train in between
                            return true;
                        } else {
                            for (int j = 0; j < allocatedSections.size(); j++) {
                                if (allocatedSections.get(j).getSection() == tSec) {
                                    if ((allocatedSections.get(j).getActiveTrain() != at) &&
                                            (allocatedSections.get(j).getActiveTrain() != nt)) {
                                        // allocated to a third train, trains not adjacent
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private int getCurrentSequenceNumber(ActiveTrain at) {
        // finds the current position of the head of the ActiveTrain in its Transit
        // returns sequence number of current position. getCurSection() returns Section.
        int seq = 0;
        curSection = null;
        if (at == null) {
            log.error("null argument on entry to 'getCurrentSeqNumber'");
            return seq;
        }
        Section temSection = null;
        List<TransitSection> tsList = at.getTransit().getTransitSectionList();
        if (!at.isTransitReversed()) {
            // find the highest numbered occupied section
            for (int i = 0; i < tsList.size(); i++) {
                if ((tsList.get(i).getSection().getOccupancy() == Section.OCCUPIED) &&
                        isSectionAllocatedToTrain(tsList.get(i).getSection(),
                                tsList.get(i).getSequenceNumber(), at)) {
                    seq = tsList.get(i).getSequenceNumber();
                    temSection = tsList.get(i).getSection();
                }
            }
            if (seq == at.getTransit().getMaxSequence()) {
                if (at.getResetWhenDone()) {
                    // train may have passed the last Section during continuous running
                    boolean further = true;
                    for (int j = 0; (j < tsList.size()) && further; j++) {
                        if ((tsList.get(j).getSection().getOccupancy() == Section.OCCUPIED) &&
                                isSectionAllocatedToTrain(tsList.get(j).getSection(),
                                        tsList.get(j).getSequenceNumber(), at)) {
                            seq = tsList.get(j).getSequenceNumber();
                            temSection = tsList.get(j).getSection();
                        } else {
                            further = false;
                        }
                    }
                }
            }
        } else {
            // transit is running in reverse
            for (int i = tsList.size() - 1; i >= 0; i--) {
                if ((tsList.get(i).getSection().getOccupancy() == Section.OCCUPIED) &&
                        isSectionAllocatedToTrain(tsList.get(i).getSection(),
                                tsList.get(i).getSequenceNumber(), at)) {
                    seq = tsList.get(i).getSequenceNumber();
                    temSection = tsList.get(i).getSection();
                }
            }
        }
        if (seq == 0) {
            if (at.getMode() != ActiveTrain.MANUAL) {
                log.error("{}: ActiveTrain has no occupied Section. Halting immediately to avoid runaway.",
                        at.getTrainName());
                at.getAutoActiveTrain().getAutoEngineer().setHalt(true);
            } else {
                log.debug("{}: ActiveTrain has no occupied Section, running in Manual mode.", at.getTrainName());
            }
        } else {
            curSection = temSection;
        }
        return seq;
    }

    Section curSection = null;

    // Returns the Section with the sequence number returned by last call to getCurrentSequenceNumber
    private Section getCurSection() {
        return curSection;
    }

    private boolean isSectionAllocatedToTrain(Section s, int seq, ActiveTrain at) {
        if ((s == null) || (at == null)) {
            log.error("null argument to isSectionAllocatedToTrain");
            return false;
        }
        List<AllocatedSection> asList = at.getAllocatedSectionList();
        for (int i = 0; i < asList.size(); i++) {
            if ((asList.get(i).getSection() == s) && asList.get(i).getSequence() == seq) {
                return true;
            }
        }
        return false;
    }

    private boolean waitingForStartTime(AllocationRequest ar) {
        if (ar != null) {
            ActiveTrain at = ar.getActiveTrain();
            if (at == null) {
                return false;
            }
            if ((!at.getStarted()) && at.getDelayedStart() != ActiveTrain.NODELAY || at.reachedRestartPoint()) {
                return true;
            }
        }
        return false;
    }

    private boolean willLevelXingsBlockTrain(ActiveTrain at) {
        // returns true if any LevelXings in _levelXingList will block the specified train
        if (at == null) {
            log.error("null argument on entry to 'willLevelXingsBlockTrain'");
            return true; // returns true to be safe
        }
        if (_levelXingList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < _levelXingList.size(); i++) {
            LevelXing lx = _levelXingList.get(i);
            Block bAC = lx.getLayoutBlockAC().getBlock();
            Block bBD = lx.getLayoutBlockBD().getBlock();
            if (at.getTransit().containsBlock(bAC) || at.getTransit().containsBlock(bBD)) {
                if (InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALMAST) {
                    return true;
                } else {
                    // temp - return false - continious trains always meet for ever and no one moves...
                    return false;
                }
                // return true;
            }
        }
        return false;
    }

    private boolean containsLevelXing(Section s) {
        // returns true if Section contains one or more level crossings
        // NOTE: changes _levelXingList!
        _levelXingList.clear();
        if (s == null) {
            log.error("null argument to 'containsLevelCrossing'");
            return false;
        }
        List<LevelXing> temLevelXingList = null;
        List<Block> blockList = s.getBlockList();
        for (int i = 0; i < blockList.size(); i++) {
            temLevelXingList = _conUtil.getLevelCrossingsThisBlock(blockList.get(i));
            if (temLevelXingList.size() > 0) {
                for (int j = 0; j < temLevelXingList.size(); j++) {
                    _levelXingList.add(temLevelXingList.get(j));
                }
            }
        }
        return _levelXingList.size() > 0;
    }

    List<LevelXing> _levelXingList = new ArrayList<>();

    private boolean isSignalHeldAtStartOfSection(AllocationRequest ar) {

        if (ar == null) {
            return false;
        }

        Section sec = ar.getSection();
        ActiveTrain mActiveTrain = ar.getActiveTrain();

        if (sec == null || mActiveTrain == null) {
            return false;
        }

        Section lastSec = mActiveTrain.getLastAllocatedSection();

        if (lastSec == null) {
            return false;
        }

        if (!sec.equals(mActiveTrain.getNextSectionToAllocate())) {
            log.error("Allocation request section does not match active train next section to allocate");
            log.error("Section to allocate {}", sec.getDisplayName());
            if (mActiveTrain.getNextSectionToAllocate() != null) {
                log.error("Active Train expected {}", mActiveTrain.getNextSectionToAllocate().getDisplayName());
            }
            return false;
        }

        Block facingBlock;
        Block protectingBlock;
        if (ar.getSectionDirection() == jmri.Section.FORWARD) {
            protectingBlock = sec.getBlockBySequenceNumber(0);
            facingBlock = lastSec.getBlockBySequenceNumber(lastSec.getNumBlocks() - 1);
        } else {
            //Reverse
            protectingBlock = sec.getBlockBySequenceNumber(sec.getNumBlocks() - 1);
            facingBlock = lastSec.getBlockBySequenceNumber(0);
        }
        if (protectingBlock == null || facingBlock == null) {
            return false;
        }

        jmri.SignalMast sm = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class)
                .getFacingSignalMast(facingBlock, protectingBlock);
        if (sm != null && sm.getHeld() && !_dispatcher.isMastHeldByDispatcher(sm, mActiveTrain)) {
            ar.setWaitingForSignalMast(sm);
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(AutoAllocate.class);

}
