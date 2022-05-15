package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Sensor;
import jmri.Transit;
import jmri.TransitSection;
import jmri.jmrit.dispatcher.TaskAllocateRelease.TaskAction;

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
 * All work done within the class is queued using a blocking queue. This is
 * to ensure the integrity of arrays, both in Dispatcher and ActiveTrain,
 * and to prevent calls within calls to modify those arrays, including autorelease.
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
public class AutoAllocate implements Runnable {

    LinkedBlockingQueue<TaskAllocateRelease> taskList;

    public AutoAllocate(DispatcherFrame d, List<AllocationRequest> inAllocationRequests) {
        _dispatcher = d;
        allocationRequests = inAllocationRequests;
        if (_dispatcher == null) {
            log.error("null DispatcherFrame when constructing AutoAllocate");
            return;
        }
        if (_dispatcher.getLayoutEditor() == null) {
            log.error("null LayoutEditor when constructing AutoAllocate");
            return;
        }
        taskList = new LinkedBlockingQueue<>();
    }

    // operational variables
    private static final jmri.NamedBean.DisplayOptions USERSYS = jmri.NamedBean.DisplayOptions.USERNAME_SYSTEMNAME;
    private DispatcherFrame _dispatcher = null;
    private final List<AllocationPlan> _planList = new ArrayList<>();
    private int nextPlanNum = 1;
    private final List<AllocationRequest> orderedRequests = new ArrayList<>();
    private List<AllocationRequest> allocationRequests = null;
    private final Map<String, String> reservedSections = new HashMap<String, String>();

    private boolean abort = false;

    /**
     * Stops the autoAllocate nicely
     */
    protected void setAbort() {
        abort = true;
        scanAllocationRequests(new TaskAllocateRelease(TaskAction.ABORT)); //force queue flush
    }

    /*
     * return true when the taskList queue is Empty
     */
    protected boolean allRequestsDone() {
        return taskList.isEmpty();
    }

    protected void scanAllocationRequests(TaskAllocateRelease task) {
        taskList.add(task);
    }

    /*
     * Main loop processing queue
     */
    @Override
    public void run() {
        while (!abort) {
            try {
                TaskAllocateRelease task = taskList.take();
                try {
                    switch (task.getAction()) {
                        case AUTO_RELEASE:
                            _dispatcher.checkAutoRelease();
                            break;
                        case RELEASE_ONE:
                            _dispatcher.doReleaseAllocatedSection(task.getAllocatedSection(),
                                    task.getTerminatingTrain());
                            break;
                        case RELEASE_RESERVED:
                            removeAllReservesForTrain(task.getTrainName());
                            break;
                        case SCAN_REQUESTS:
                            scanAllocationRequestList(allocationRequests);
                            break;
                        case ALLOCATE_IMMEDIATE:
                            _dispatcher.allocateSection(task.getAllocationRequest(), null);
                            break;
                        case ABORT:
                            abort = true; //belt an braces
                            break;
                        default:
                            log.error("Unknown action in TaskAllocateRelease - ignoring");
                    }
                } catch (Exception ex) {
                    log.error("Unexpected Exeption, likely bad task request.", ex);
                }
            } catch (InterruptedException ex) {
                log.error("Blocklist killed, taking this as terminate", ex);
                abort = true;
            }
        }
    }

    /**
     * This is the entry point to AutoAllocate when it is triggered.
     *
     * @param list list to scan
     */
    private synchronized void scanAllocationRequestList(List<AllocationRequest> list) {
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
                // Check to see if there is a sensor temporarily block
                // allocation blocking allocation
                ActiveTrain activeTrain = ar.getActiveTrain();
                String trainName = activeTrain.getTrainName();
                log.trace("{}: try to allocate [{}]", trainName, ar.getSection().getDisplayName(USERSYS));
                if (activeTrain.getLastAllocatedSection() != null) {
                    // do stuff associated with the last allocated section
                    Transit arTransit = activeTrain.getTransit();
                    TransitSection arCurrentTransitSection =
                            arTransit.getTransitSectionFromSectionAndSeq(activeTrain.getLastAllocatedSection(),
                                    activeTrain.getLastAllocatedSectionSeqNumber());
                    // stop allocating sensor active?
                    if (stopAllocateSensorSet(activeTrain, arCurrentTransitSection)) {
                        log.debug("[{}]:StopAllocateSensor active", trainName);
                        continue;
                    }
                    // is the train held
                    if (activeTrain.holdAllocation()|| (!activeTrain.getStarted()) && activeTrain.getDelayedStart() != ActiveTrain.NODELAY) {
                        log.debug("[{}]:Allocation is Holding or Delayed", trainName);
                        continue;
                    }
                    // apparently holdAllocation() is not set when holding !!!
                    if (InstanceManager.getDefault(DispatcherFrame.class)
                            .getSignalType() == DispatcherFrame.SIGNALMAST &&
                            isSignalHeldAtStartOfSection(ar)) {
                        continue;
                    }
                    // this already reserved for the train, allocate.
                    String reservedTrainName = reservedSections.get(ar.getSection().getSystemName());
                    if (reservedTrainName != null) {
                        if (reservedTrainName.equals(trainName)) {
                            String sectionName = ar.getSection().getSystemName();
                            if (allocateMore(ar)) {
                                reservedSections.remove(sectionName);
                            }
                            continue;
                        }
                    }

                    if (activeTrain.getAllocateMethod() == ActiveTrain.ALLOCATE_BY_SAFE_SECTIONS) {
                        log.trace("{}: Allocating [{}] using Safe Sections", trainName,
                                ar.getSection().getDisplayName());
                        // if the last allocated section is safe but not
                        // occupied short cut out of here
                        if (arCurrentTransitSection.isSafe() &&
                                activeTrain.getLastAllocatedSection().getOccupancy() != Section.OCCUPIED) {
                            log.debug("Allocating Train [{}] has not arrived at Passing Point",
                                    trainName);
                            continue;
                        }
                        // Check all forward sections till a passing point.
                        int itSequ = ar.getSectionSeqNumber();
                        int iIncrement = 0;
                        int iLimit = 0;
                        int ix = 0;
                        boolean skip = false;
                        int iStart = 0;
                        if (activeTrain.isTransitReversed()) {
                            iIncrement = -1;
                            iLimit = 0;
                            iStart = itSequ; // reverse transits start
                                             // allocating from the next
                                             // one, they allocate the one
                                             // there in already
                        } else {
                            if (activeTrain.getStartBlockSectionSequenceNumber() == ar.getSectionSeqNumber()) {
                                skip = true;
                            }
                            iIncrement = +1;
                            iLimit = arTransit.getMaxSequence() + 1;
                            iStart = itSequ;
                        }
                        if (!skip) {
                            boolean areForwardsFree = false;
                            log.trace("index [{}] Limit [{}] transitsize [{}]", ix, iLimit,
                                    arTransit.getTransitSectionList().size());
                            for (ix = iStart; ix != iLimit; ix += iIncrement) {
                                log.trace("index [{}] Limit [{}] transitsize [{}]", ix, iLimit,
                                        arTransit.getTransitSectionList().size());
                                // ensure all blocks section and blocks free
                                // till next Passing Point, check alternates
                                // if they exist.
                                Section sS;
                                ArrayList<TransitSection> sectionsInSeq = arTransit.getTransitSectionListBySeq(ix);
                                areForwardsFree = false; // Posit will be
                                                         // bad
                                log.trace("Search ALternates Size[{}]", sectionsInSeq.size());
                                int seqNumberfound = 0;
                                for (int iSectionsInSeq = 0; iSectionsInSeq < sectionsInSeq.size() &&
                                        !areForwardsFree; iSectionsInSeq++) {
                                    log.trace("iSectionInSeq[{}]", iSectionsInSeq);
                                    sS = sectionsInSeq.get(iSectionsInSeq).getSection();
                                    seqNumberfound = iSectionsInSeq; // save
                                                                     // for
                                                                     // later
                                    // debug code
                                    log.trace("SectionName[{}] getState[{}] occupancy[{}] ",
                                            sS.getDisplayName(USERSYS),
                                            sS.getState(), sS.getOccupancy());
                                    if (!checkUnallocatedCleanly(activeTrain, sS)) {
                                        areForwardsFree = false;
                                    } else if (sS.getState() != Section.FREE) {
                                        log.debug("{}: Forward section [{}] unavailable", trainName,
                                                sS.getDisplayName(USERSYS));
                                        areForwardsFree = false;
                                    } else if (sS.getOccupancy() != Section.UNOCCUPIED) {
                                        log.debug("{}: Forward section [{}] is not unoccupied", trainName,
                                                sS.getDisplayName(USERSYS));
                                        areForwardsFree = false;
                                    } else if (_dispatcher.checkBlocksNotInAllocatedSection(sS, ar) != null) {
                                        log.debug("{}: Forward section [{}] is in conflict with [{}]",
                                                trainName, sS.getUserName(),
                                                _dispatcher.checkBlocksNotInAllocatedSection(sS, ar));
                                        areForwardsFree = false;
                                    } else if (checkBlocksNotInReservedSection(activeTrain, ar) != null) {
                                        log.debug("{}: Forward section [{}] is in conflict with [{}]",
                                                trainName, sS.getDisplayName(),
                                                checkBlocksNotInReservedSection(activeTrain, ar).getDisplayName());
                                        areForwardsFree = false;

                                    } else if (reservedSections.get(sS.getSystemName()) != null &&
                                            !reservedSections.get(sS.getSystemName()).equals(trainName)) {
                                        log.debug("{}: Forward section [{}] is reserved for [{}]",
                                                trainName, sS.getDisplayName(USERSYS),
                                                reservedSections.get(sS.getSystemName()));
                                        areForwardsFree = false;
                                    } else {
                                        log.debug("Adding [{}],[{}]", sS.getDisplayName(USERSYS), trainName);
                                        reservedSections.put(sS.getSystemName(), trainName);
                                        areForwardsFree = true;
                                    }
                                }
                                if (!areForwardsFree) {
                                    // delete all reserves for this train
                                    removeAllReservesForTrain(trainName);
                                    break;
                                }
                                if (sectionsInSeq.get(seqNumberfound).isSafe()) {
                                    log.trace("Safe Section Found");
                                    break;
                                }
                            }

                            log.trace("ForwardsFree[{}]", areForwardsFree);
                            if (!areForwardsFree) {
                                // delete all reserves for this train
                                removeAllReservesForTrain(trainName);
                                continue;
                            }
                        }
                        String sectionSystemName;
                        try {
                            sectionSystemName = ar.getSection().getSystemName();
                        } catch (Exception ex) {
                            log.error("Error", ex);
                            sectionSystemName = "Unknown";
                        }
                        if (allocateMore(ar)) {
                            // First Time thru this will in the list
                            if (!sectionSystemName.equals("Unknown")) {
                                log.debug("removing : [{}]", sectionSystemName);
                                reservedSections.remove(sectionSystemName);
                            } else {
                                log.error("{};Cannot allocate allocatable section[{}]", trainName,
                                        sectionSystemName);
                            }
                        }
                        continue;
                    } // end of allocating by safe sections
                }
                log.trace("Using Regular");
                if (!checkUnallocatedCleanly(activeTrain, ar.getSection())) {
                    okToAllocate = false;
                    continue;
                }
                if (getPlanThisTrain(activeTrain) != null) {
                    // this train is in an active Allocation Plan, anything
                    // to do now?
                    if (willAllocatingFollowPlan(ar, getPlanThisTrain(activeTrain))) {
                        if (allocateMore(ar)) {
                            continue;
                        }
                    }
                } else if (!waitingForStartTime(ar)) {
                    // train isn't waiting, continue only if requested
                    // Section is currently free and not occupied
                    if ((ar.getSection().getState() == Section.FREE) &&
                            (ar.getSection().getOccupancy() != Section.OCCUPIED) &&
                            (_dispatcher.getSignalType() == DispatcherFrame.SIGNALHEAD ||
                                    _dispatcher.getSignalType() == DispatcherFrame.SECTIONSALLOCATED ||
                                    (_dispatcher.getSignalType() == DispatcherFrame.SIGNALMAST &&
                                            _dispatcher.checkBlocksNotInAllocatedSection(ar.getSection(),
                                                    ar) == null))) {
                        // requested Section is currently free and not
                        // occupied
                        List<ActiveTrain> activeTrainsList = _dispatcher.getActiveTrainsList();
                        if (activeTrainsList.size() == 1) {
                            // this is the only ActiveTrain
                            if (allocateMore(ar)) {
                                continue;
                            }
                        } else {
                            // check if any other ActiveTrain will need this
                            // Section or its alternates, if any
                            okToAllocate = true;
                            List<ActiveTrain> neededByTrainList = new ArrayList<>();
                            for (int j = 0; j < activeTrainsList.size(); j++) {
                                ActiveTrain at = activeTrainsList.get(j);
                                if (at != activeTrain) {
                                    if (sectionNeeded(ar, at)) {
                                        neededByTrainList.add(at);
                                    }
                                }
                            }
                            // requested Section (or alternate) is
                            // needed by other active Active Train(s)
                            for (int k = 0; k < neededByTrainList.size(); k++) {
                                // section is also needed by this active
                                // train
                                ActiveTrain nt = neededByTrainList.get(k);
                                // are trains moving in same direction
                                // through the requested Section?
                                if (sameDirection(ar, nt)) {
                                    // trains will move in the same
                                    // direction thru requested section
                                    if (firstTrainLeadsSecond(activeTrain, nt) &&
                                            (nt.getPriority() > activeTrain.getPriority())) {
                                        // a higher priority train is
                                        // trailing this train, can we
                                        // let it pass?
                                        if (checkForPassingPlan(ar, nt, neededByTrainList)) {
                                            // PASSING_MEET plan created
                                            if (!willAllocatingFollowPlan(ar,
                                                    getPlanThisTrain(activeTrain))) {
                                                okToAllocate = false;
                                            }
                                        }
                                    }
                                } else {
                                    // trains will move in opposite
                                    // directions thru requested section
                                    // explore possibility of an
                                    // XING_MEET to avoid gridlock
                                    if (willTrainsCross(activeTrain, nt)) {
                                        if (checkForXingPlan(ar, nt, neededByTrainList)) {
                                            // XING_MEET plan created
                                            if (!willAllocatingFollowPlan(ar,
                                                    getPlanThisTrain(activeTrain))) {
                                                okToAllocate = false;
                                            }
                                        }
                                    }
                                }
                            }
                            if (okToAllocate) {
                                if (allocateMore(ar)) {
                                    continue;
                                }
                            }
                        }
                    }
                }
            } catch (RuntimeException e) {
                log.warn(
                        "scanAllocationRequestList - maybe the allocationrequest was removed due to a terminating train??",e);
                continue;
            }
        }
    }

    /**
     * Remove all reserved sections for a train name
     *
     * @param trainName remove reserved spaces for this train
     */
    protected void removeAllReservesForTrain(String trainName) {
        Iterator<Entry<String, String>> iterRS = reservedSections.entrySet().iterator();
        while (iterRS.hasNext()) {
            Map.Entry<String, String> pair = iterRS.next();
            if (pair.getValue().equals(trainName)) {
                iterRS.remove();
            }
        }
    }

    /**
     * Remove a specific section reservation for a train.
     *
     * @param trainName         Name of the train
     * @param sectionSystemName Systemname
     */
    protected void releaseReservation(String trainName, String sectionSystemName) {
        String reservedTrainName = reservedSections.get(sectionSystemName);
        if (reservedTrainName.equals(trainName)) {
            reservedSections.remove(sectionSystemName);
        }
    }

    /*
     * Check conflicting blocks acros reserved sections.
     */
    protected Section checkBlocksNotInReservedSection(ActiveTrain at, AllocationRequest ar) {
        String trainName = at.getTrainName();
        List<Block> lb = ar.getSection().getBlockList();
        Iterator<Entry<String, String>> iterRS = reservedSections.entrySet().iterator();
        while (iterRS.hasNext()) {
            Map.Entry<String, String> pair = iterRS.next();
            if (!pair.getValue().equals(trainName)) {
                Section s = InstanceManager.getDefault(jmri.SectionManager.class).getSection(pair.getKey());
                for (Block rb : s.getBlockList()) {
                    if (lb.contains(rb)) {
                        return s;
                    }
                }
            }
        }
        return null;
    }

    /*
     * Check each ActiveTrains sections for a given section. We need to do this
     * as Section is flagged as free before it is fully released, then when it
     * is released it updates , incorrectly, the section status and allocations.
     */
    private boolean checkUnallocatedCleanly(ActiveTrain at, Section section) {
        for (ActiveTrain atx : InstanceManager.getDefault(DispatcherFrame.class).getActiveTrainsList()) {
            for (AllocatedSection asx : atx.getAllocatedSectionList()) {
                if (asx.getSection() == section) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Entered to request a choice of Next Section when a Section is being
     * allocated and there are alternate Section choices for the next Section.
     *
     * @param sList the possible next Sections
     * @param ar    the section being allocated when a choice is needed
     * @param sectionSeqNo transit sequence number attempting to be allocated
     * @return the allocated section
     */
    protected Section autoNextSectionChoice(List<Section> sList, AllocationRequest ar, int sectionSeqNo) {
        // check if AutoAllocate has prepared for this question
        if ((savedAR != null) && (savedAR == ar)) {
            for (int j = 0; j < sList.size(); j++) {
                if (savedSection == sList.get(j)) {
                    return savedSection;
                }
            }
            log.warn("Failure of prepared choice of next Section in AutoAllocate");
        }

        // check to see if AutoAllocate by safesections has reserved a section
        // already
        ActiveTrain at = ar.getActiveTrain();
        for (Section sectionOption : sList) {
            String reservedTrainName = reservedSections.get(sectionOption.getSystemName());
            if (reservedTrainName != null) {
                if (reservedTrainName.equals(at.getTrainName())) {
                    return sectionOption;
                }
            }
        }

        // Jay Janzen
        // If there is an AP check to see if the AP's target is on the list of
        // choices
        // and if so, return that.
        at = ar.getActiveTrain();
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
        // return that occupied or not. In the list of choices the primary
        // occurs
        // ahead any alternates, so if our end block is an alternate and its
        // primary is unoccupied, the search will select the primary and
        // we wind up skipping right over our end section.
        for (int i = 0; i < sList.size(); i++) {
            if (at.getEndBlockSectionSequenceNumber() == sectionSeqNo
                     && at.getEndBlockSection().getSystemName().equals(sList.get(i).getSystemName())) {
                return sList.get(i);
            }
        }
        // no prepared choice, or prepared choice failed, is there an unoccupied
        // Section available
        for (int i = 0; i < sList.size(); i++) {
            if ((sList.get(i).getOccupancy() == Section.UNOCCUPIED) &&
                    (sList.get(i).getState() == Section.FREE) &&
                    (_dispatcher.getSignalType() == DispatcherFrame.SIGNALHEAD ||
                            _dispatcher.getSignalType() == DispatcherFrame.SECTIONSALLOCATED ||
                            (_dispatcher.getSignalType() == DispatcherFrame.SIGNALMAST &&
                                    _dispatcher.checkBlocksNotInAllocatedSection(sList.get(i), ar) == null))) {
                return sList.get(i);
            }
        }
        // no unoccupied Section available, check for Section allocated in same
        // direction as this request
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
        // if all else fails, return null so Dispatcher will ask the dispatcher
        // to choose
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
                    log.trace("Sensor[{}] InActive", sensor.getDisplayName(USERSYS));
                    at.initializeRestartAllocationSensor(jmri.InstanceManager
                            .getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor));
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
        // return 'true' if this AllocationRequest is consistent with specified
        // plan,
        // returns 'false' otherwise
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
    // return true if allocation successful, false otherwise
    private boolean allocateMore(AllocationRequest ar) {
        log.trace("in allocateMore, ar.Section={}", ar.getSection().getDisplayName(USERSYS));
        int allocateSectionsAhead = ar.getActiveTrain().getAllocateMethod();
        if (allocateSectionsAhead == ActiveTrain.ALLOCATE_AS_FAR_AS_IT_CAN) {
            if (_dispatcher.allocateSection(ar, null) == null) {
                return false;
            }
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
                            ar.getActiveTrain().getTransit()
                                    .getTransitSectionFromSectionAndSeq(curAS.getSection(), curSeq).isSafe())) {
                // last allocated section exists and is not occupied but is a
                // Passing point
                // block further allocations till occupied.
                log.trace("{}: not at end of safe allocations, [{}] not allocated", ar.getActiveTrain().getTrainName(),
                        ar.getSection().getDisplayName(USERSYS));
                return false;
            } else if (allocateBySafeSections) {
                log.trace("auto allocating Section keep going");
                if (_dispatcher.allocateSection(ar, null) != null) {
                    return true;
                } else {
                    return false;
                }
            }
            log.trace("Auto allocating by count");
            int numberAllocatedButUnoccupied = 0;
            for (int i = aSectionList.size() - 1; i >= 0; i--) {
                AllocatedSection as = aSectionList.get(i);
                if ((as != null) && (as.getSection().getOccupancy() != jmri.Section.OCCUPIED && !as.getExited())) {
                    numberAllocatedButUnoccupied++;
                }
            }
            log.trace("FinalCounter[{}]", numberAllocatedButUnoccupied);
            if (numberAllocatedButUnoccupied < allocateSectionsAhead) {
                if (_dispatcher.allocateSection(ar, null) == null) {
                    return false;
                }
                return true;
            }
            return false;

        }
        log.debug("{}: auto allocating Section {}", ar.getActiveTrain().getTrainName(),
                ar.getSection().getDisplayName(USERSYS));
        if (_dispatcher.allocateSection(ar, null) == null) {
            return false;
        }
        return true;
    }

    private boolean checkForXingPlan(AllocationRequest ar, ActiveTrain nt,
            List<ActiveTrain> neededByTrainList) {
        // returns 'true' if an AllocationPlan has been set up, returns 'false'
        // otherwise
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
            // already in a XING_MEET Allocation Plan - find target Section and
            // sequence
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
            // neither train is in an AllocationPlan currently, check for
            // suitable passing siding
            int aSeq = ar.getSectionSeqNumber();
            // is an alternate Section available here or ahead
            aSecSeq = findPassingSection(at, aSeq);
            if (aSecSeq == 0) {
                // none in at's Transit, is there one in nt's Transit
                int nCurrentSeq = getCurrentSequenceNumber(nt);
                nSecSeq = findPassingSection(nt, nCurrentSeq);
                if (nSecSeq > 0) {
                    // has passing section ahead, will this train traverse a
                    // Section in it
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
        // check for conflicting train or conflicting plan that could cause
        // gridlock
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
        // returns 'true' if an AllocationPlan has been set up, returns 'false'
        // otherwise
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
            // already in a PASSING_MEET Allocation Plan - find target Section
            // and sequence
            Section oSection = null;
            // ActiveTrain oTrain = null;
            if (apx.getActiveTrain(1) == nt) {
                nSecSeq = apx.getTargetSectionSequenceNum(1);
                nSec = apx.getTargetSection(1);
                oSection = apx.getTargetSection(2);
            } else {
                nSecSeq = apx.getTargetSectionSequenceNum(2);
                nSec = apx.getTargetSection(2);
                oSection = apx.getTargetSection(1);
            }
            int aCurrentSeq = getCurrentSequenceNumber(at);
            aSecSeq = willTraverse(nSec, at, aCurrentSeq);
            if (aSecSeq == 0) {
                return false;
            }
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
                    // has passing section ahead, will this train traverse a
                    // Section in it
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
                // will the higher priority train go through any of these
                // alternate sections
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
            // push higher priority train one section further, if possible
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
            // Note: e.g. Two lower priority trains ahead of a high priority
            // train could cause gridlock
            // if both try to set up a PASSING_PLAN meet at the same place, so
            // we exclude that case.
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
        // returns 'true' if there is a conflicting plan that may result in
        // gridlock
        // if this plan is set up, return 'false' if not.
        // Note: may have to add other tests to this method in the future to
        // prevent gridlock
        // situations not currently tested for.
        if (_planList.size() == 0) {
            return false;
        }
        for (int i = 0; i < _planList.size(); i++) {
            AllocationPlan ap = _planList.get(i);
            // check if this plan involves the second train (it'll never involve
            // the first)
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
                // different trains, does this plan use the same Passing
                // Section?
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
        // returns the best Section from the list that is not aSec, or else
        // return null
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
        // returns 'true' if request section, or its alternates, will be needed
        // by specified train
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
        // returns 'true' if both trains will move thru the requested section in
        // the same direction
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
                        // second train has found first train in its on coming
                        // Sections
                        return true;
                    }
                }
            }
        } else {
            for (int i = ntsList.size() - 1; i <= 0; i--) {
                if (ntsList.get(i).getSequenceNumber() < nSeq) {
                    if (ntsList.get(i).getSection() == aSec) {
                        // second train has found first train in its on coming
                        // Sections
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
                        // second train has found first train in its on coming
                        // Sections
                        return true;
                    }
                }
            }
        } else {
            for (int i = ntsList.size() - 1; i <= 0; i--) {
                if (ntsList.get(i).getSequenceNumber() < nSeq) {
                    if (ntsList.get(i).getSection() == aSec) {
                        // second train has found first train in its on coming
                        // Sections
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean areTrainsAdjacent(ActiveTrain at, ActiveTrain nt) {
        // returns 'false' if a different ActiveTrain has allocated track
        // between the
        // two trains, returns 'true' otherwise
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
                            // reached second train position, no train in
                            // between
                            return true;
                        } else {
                            for (int j = 0; j < allocatedSections.size(); j++) {
                                if (allocatedSections.get(j).getSection() == tSec) {
                                    if ((allocatedSections.get(j).getActiveTrain() != at) &&
                                            (allocatedSections.get(j).getActiveTrain() != nt)) {
                                        // allocated to a third train, trains
                                        // not adjacent
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
                            // reached second train position, no train in
                            // between
                            return true;
                        } else {
                            for (int j = 0; j < allocatedSections.size(); j++) {
                                if (allocatedSections.get(j).getSection() == tSec) {
                                    if ((allocatedSections.get(j).getActiveTrain() != at) &&
                                            (allocatedSections.get(j).getActiveTrain() != nt)) {
                                        // allocated to a third train, trains
                                        // not adjacent
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
                            // reached second train position, no train in
                            // between
                            return true;
                        } else {
                            for (int j = 0; j < allocatedSections.size(); j++) {
                                if (allocatedSections.get(j).getSection() == tSec) {
                                    if ((allocatedSections.get(j).getActiveTrain() != at) &&
                                            (allocatedSections.get(j).getActiveTrain() != nt)) {
                                        // allocated to a third train, trains
                                        // not adjacent
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
                            // reached second train position, no train in
                            // between
                            return true;
                        } else {
                            for (int j = 0; j < allocatedSections.size(); j++) {
                                if (allocatedSections.get(j).getSection() == tSec) {
                                    if ((allocatedSections.get(j).getActiveTrain() != at) &&
                                            (allocatedSections.get(j).getActiveTrain() != nt)) {
                                        // allocated to a third train, trains
                                        // not adjacent
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
        // finds the current position of the head of the ActiveTrain in its
        // Transit
        // returns sequence number of current position. getCurSection() returns
        // Section.
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
                    // train may have passed the last Section during continuous
                    // running
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

    // Returns the Section with the sequence number returned by last call to
    // getCurrentSequenceNumber
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
            log.error("[{}]Allocation request section does not match active train next section to allocate",mActiveTrain.getActiveTrainName());
            log.error("[{}]Section requested {}",mActiveTrain.getActiveTrainName(), sec.getDisplayName(USERSYS));
            if (mActiveTrain.getNextSectionToAllocate() != null) {
                log.error("[{}]Section expected {}",
                        mActiveTrain.getActiveTrainName(), mActiveTrain.getNextSectionToAllocate().getDisplayName(USERSYS));
            }
            if (mActiveTrain.getLastAllocatedSection() != null) {
                log.error("[{}]Last Section Allocated {}",
                        mActiveTrain.getActiveTrainName(), mActiveTrain.getLastAllocatedSection().getDisplayName(USERSYS));
            }
            return false;
        }

        Block facingBlock;
        Block protectingBlock;
        if (ar.getSectionDirection() == jmri.Section.FORWARD) {
            protectingBlock = sec.getBlockBySequenceNumber(0);
            facingBlock = lastSec.getBlockBySequenceNumber(lastSec.getNumBlocks() - 1);
        } else {
            // Reverse
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
