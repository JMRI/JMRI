package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import java.util.List;
import jmri.Block;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.Turnout;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.layoutEditor.ConnectivityUtil;
import jmri.jmrit.display.layoutEditor.LayoutDoubleXOver;
import jmri.jmrit.display.layoutEditor.LayoutLHXOver;
import jmri.jmrit.display.layoutEditor.LayoutRHXOver;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTrackExpectedState;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles automatic checking and setting of turnouts when Dispatcher allocates
 * a Section in a specific direction.
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is open source software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Dave Duchamp Copyright (C) 2008-2009
 */
public class AutoTurnouts {

    public AutoTurnouts(DispatcherFrame d) {
        _dispatcher = d;
    }

    private static final DisplayOptions USERSYS = DisplayOptions.USERNAME_SYSTEMNAME;
    private final String closedText = InstanceManager.turnoutManagerInstance().getClosedText();
    private final String thrownText = InstanceManager.turnoutManagerInstance().getThrownText();

    // operational variables
    protected DispatcherFrame _dispatcher = null;

    /**
     * Check that all turnouts are correctly set for travel in the designated
     * Section to the next Section. NOTE: This method requires use of the
     * connectivity stored in a Layout Editor panel.
     *
     * NOTE: This method removes the need to specify the LayoutEditor panel.
     *
     * @param s           the section to check
     * @param seqNum      sequence number for the section
     * @param nextSection the following section
     * @param at          the associated train
     * @param prevSection the prior section
     * @param useTurnoutConnectionDelay true if the turnout connection delay should be applied
     * @return list of turnouts and their expected states if affected turnouts are correctly set; null otherwise.
     */
    protected List<LayoutTrackExpectedState<LayoutTurnout>> checkTurnoutsInSection(Section s, int seqNum, Section nextSection,
            ActiveTrain at, Section prevSection, boolean useTurnoutConnectionDelay) {
        return turnoutUtil(s, seqNum, nextSection, at, false, false, prevSection, useTurnoutConnectionDelay);
    }


    /**
     * Set all turnouts for travel in the designated Section to the next
     * Section.
     *
     * Checks that all turnouts are correctly set for travel in this Section to
     * the next Section, and sets any turnouts that are not correct. The Section
     * must be FREE to set its turnouts. Testing for FREE only occurs if a
     * command needs to be issued. For a command to be issued to set a turnout,
     * the Block containing that turnout must be unoccupied. NOTE: This method
     * does not wait for turnout feedback--it assumes the turnout will be set
     * correctly if a command is issued.
     *
     * NOTE: This method removes the need to specify the LayoutEditor panel.
     *
     *
     * @param s                  the section to check
     * @param seqNum             sequence number for the section
     * @param nextSection        the following section
     * @param at                 the associated train
     * @param trustKnownTurnouts true to trust known turnouts
     * @param prevSection        the prior section
     * @param useTurnoutConnectionDelay true if the turnout connection delay should be applied
     *
     * @return list of turnouts and their expected states if affected turnouts are correctly set or commands have been
     *         issued to set any that aren't set correctly; null if a needed
     *         command could not be issued because the turnout's Block is
     *         occupied
     */
    protected List<LayoutTrackExpectedState<LayoutTurnout>> setTurnoutsInSection(Section s, int seqNum, Section nextSection,
            ActiveTrain at, boolean trustKnownTurnouts,  Section prevSection, boolean useTurnoutConnectionDelay) {
        return turnoutUtil(s, seqNum, nextSection, at, trustKnownTurnouts, true, prevSection, useTurnoutConnectionDelay);
    }

    protected Turnout checkStateAgainstList(List<LayoutTrackExpectedState<LayoutTurnout>> turnoutList) {
        if (turnoutList != null) {
            for (LayoutTrackExpectedState<LayoutTurnout> tes : turnoutList) {
                Turnout to = tes.getObject().getTurnout();
                int setting = tes.getExpectedState();
                if (tes.getObject() instanceof LayoutSlip) {
                    setting = ((LayoutSlip) tes.getObject()).getTurnoutState(tes.getExpectedState());
                }
                if (to.getKnownState() != setting) {
                    return to;
                }
                if (tes.getObject() instanceof LayoutSlip) {
                    //Look at the state of the second turnout in the slip
                    setting = ((LayoutSlip) tes.getObject()).getTurnoutBState(tes.getExpectedState());
                    to = ((LayoutSlip) tes.getObject()).getTurnoutB();
                    if (to.getKnownState() != setting) {
                        return to;
                    }
                }
             }
        }
        return null;
    }

    /**
     * Internal method implementing the above two methods Returns 'true' if
     * turnouts are set correctly, 'false' otherwise If 'set' is 'true' this
     * routine will attempt to set the turnouts, if 'false' it reports what it
     * finds.
     */
    private List<LayoutTrackExpectedState<LayoutTurnout>> turnoutUtil(Section s, int seqNum, Section nextSection,
          ActiveTrain at, boolean trustKnownTurnouts, boolean set, Section prevSection, boolean useTurnoutConnectionDelay ) {
        log.trace("{}:Checking LayoutTrackExpectedState Section[{}]  NextSection[{}] PrevSection[{}]",
                at.getTrainName(), (s != null) ? s.getDisplayName() : "null",
                nextSection== null ? "Null" : nextSection.getDisplayName(),
                prevSection == null ? "Null" : prevSection.getDisplayName());
        // initialize response structure
        List<LayoutTrackExpectedState<LayoutTurnout>> turnoutListForAllocatedSection = new ArrayList<>();
        // validate input and initialize
        Transit tran = at.getTransit();
        if ((s == null) || (seqNum > tran.getMaxSequence()) || (!tran.containsSection(s))) {
            log.error("Invalid argument when checking or setting turnouts in Section.");
            return null;
        }
        int direction = at.getAllocationDirectionFromSectionAndSeq(s, seqNum);
        if (direction == 0) {
            log.error("Invalid Section/sequence arguments when checking or setting turnouts");
            return null;
        }
        // Did have this set to include SignalMasts as part of the && statement
        //Sections created using Signal masts will generally only have a single entry/exit point.
        // check for no turnouts in this section
        if (_dispatcher.getSignalType() == DispatcherFrame.SIGNALHEAD && (s.getForwardEntryPointList().size() <= 1) && (s.getReverseEntryPointList().size() <= 1)) {
            log.debug("No entry points lists");
            // no possibility of turnouts
            return turnoutListForAllocatedSection;
        }
        // initialize connectivity utilities and beginning block pointers
        EntryPoint entryPt = null;
        if (prevSection != null) {
            entryPt = s.getEntryPointFromSection(prevSection, direction);
        } else if (!s.containsBlock(at.getStartBlock())) {
            entryPt = s.getEntryPointFromBlock(at.getStartBlock(), direction);
        }
        EntryPoint exitPt = null;
        if (nextSection != null) {
            exitPt = s.getExitPointToSection(nextSection, direction);
        }
        Block curBlock;         // must be in the section
        Block prevBlock = null; // must start outside the section or be null
        int curBlockSeqNum;     // sequence number of curBlock in Section
        if (entryPt != null) {
            curBlock = entryPt.getBlock();
            prevBlock = entryPt.getFromBlock();
            curBlockSeqNum = s.getBlockSequenceNumber(curBlock);
        } else if ( !at.isAllocationReversed() && s.containsBlock(at.getStartBlock())) {
            curBlock = at.getStartBlock();
            curBlockSeqNum = s.getBlockSequenceNumber(curBlock);
            //Get the previous block so that we can set the turnouts in the current block correctly.
            if (direction == Section.FORWARD) {
                prevBlock = s.getBlockBySequenceNumber(curBlockSeqNum - 1);
            } else if (direction == Section.REVERSE) {
                prevBlock = s.getBlockBySequenceNumber(curBlockSeqNum + 1);
            }
        } else if (at.isAllocationReversed() && s.containsBlock(at.getEndBlock())) {
            curBlock = at.getEndBlock();
            curBlockSeqNum = s.getBlockSequenceNumber(curBlock);
            //Get the previous block so that we can set the turnouts in the current block correctly.
            if (direction == Section.REVERSE) {
                prevBlock = s.getBlockBySequenceNumber(curBlockSeqNum + 1);
            } else if (direction == Section.FORWARD) {
                prevBlock = s.getBlockBySequenceNumber(curBlockSeqNum - 1);
            }
        } else {

            //if (_dispatcher.getSignalType() == DispatcherFrame.SIGNALMAST) {
            //    //This can be considered normal where SignalMast Logic is used.
            //    return true;
            //}
            // this is an error but is it? It only happens when system is under stress
            // which would point to a threading issue.
            try {
                log.error("[{}]direction[{}] Section[{}]Error in turnout check/set request - initial Block[{}] and Section[{}] mismatch",
                        at.getActiveTrainName(),at.isAllocationReversed(),s.getDisplayName(USERSYS),
                        at.getStartBlock().getUserName(),at.getEndBlock().getDisplayName(USERSYS));
            } catch (Exception ex ) {
                log.warn("Exception while creating log error : {}", ex.getLocalizedMessage());
            }
            return turnoutListForAllocatedSection;
        }

        Block nextBlock = null;
        // may be either in the section or the first block in the next section
        int nextBlockSeqNum = -1;   // sequence number of nextBlock in Section (-1 indicates outside Section)
        if (exitPt != null && curBlock == exitPt.getBlock()) {
            // next Block is outside of the Section
            nextBlock = exitPt.getFromBlock();
        } else {
            // next Block is inside the Section
            if (direction == Section.FORWARD) {
                nextBlock = s.getBlockBySequenceNumber(curBlockSeqNum + 1);
                nextBlockSeqNum = curBlockSeqNum + 1;
            } else if (direction == Section.REVERSE) {
                nextBlock = s.getBlockBySequenceNumber(curBlockSeqNum - 1);
                nextBlockSeqNum = curBlockSeqNum - 1;
            }
            if ((nextBlock == null &&
                    ((!at.isAllocationReversed() && curBlock != at.getEndBlock()) ||
                            (at.isAllocationReversed() && curBlock != at.getStartBlock())))) {
                log.error("[{}]Error in block sequence numbers when setting/checking turnouts.",
                        curBlock.getDisplayName(USERSYS));
                return null;
            }
        }

        List<LayoutTrackExpectedState<LayoutTurnout>> turnoutList;
        // get turnouts by Block
        boolean turnoutsOK = true;

        var layoutBlockManger = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        // with no entry point, the first Block is the one the train is standing in (its start, or its end when reversed)
        boolean standingBlock = (entryPt == null);
        while (curBlock != null) {
            /* The turnouts in the Block the train is standing in are checked, not set, while the Block is
             occupied: the train may be standing on them. With no previous Block the way it came in is not
             known, so the turnouts it needs are those every way into the Block in this direction agrees on.
             */
            log.trace("curBlock {}: PrevBlock[{}]", curBlock.getDisplayName(),
                    prevBlock == null ?   "Null" : prevBlock.getDisplayName());
            ConnectivityUtil connection = null;
            var blockName = curBlock.getUserName();
            if (blockName != null) {
                var lblock = layoutBlockManger.getLayoutBlock(blockName);
                if (lblock != null) {
                    var panel = lblock.getMaxConnectedPanel();
                    if (panel != null) {
                        connection = new ConnectivityUtil(panel);
                    }
                }
            }
            if (connection == null) {
                turnoutList = new ArrayList<>();
            } else if (prevBlock != null) {
                // note turnouts for turntables are added to the list in getTurnoutList
                turnoutList = connection.getTurnoutList(curBlock, prevBlock, nextBlock, true);
            } else {
                turnoutList = standingBlockTurnouts(connection, s, direction, curBlock, nextBlock);
            }
            // loop over turnouts checking and optionally setting turnouts
            for (int i = 0; i < turnoutList.size(); i++) {
                Turnout to = turnoutList.get(i).getObject().getTurnout();
                if (to == null ) {
                    // this should not happen due to prior selection
                    log.error("Found null Turnout reference at {}: {}", i, turnoutList.get(i).getObject());
                    continue; // move to next loop, what else can we do?
                }
                // save for return
                turnoutListForAllocatedSection.add(turnoutList.get(i));
                int setting = turnoutList.get(i).getExpectedState();
                if (turnoutList.get(i).getObject() instanceof LayoutSlip) {
                    setting = ((LayoutSlip) turnoutList.get(i).getObject()).getTurnoutState(turnoutList.get(i).getExpectedState());
                }
                if (standingBlock) {
                    if (!standingBlockTurnoutOK(turnoutList.get(i), setting, set, s, curBlock, at, useTurnoutConnectionDelay)) {
                        turnoutsOK = false;
                    }
                    continue;
                }
                // check or ignore current setting based on flag, set in Options
                if (!trustKnownTurnouts && set) {
                    log.debug("{}: setting turnout {} to {}", at.getTrainName(), to.getDisplayName(USERSYS),
                            (setting == Turnout.CLOSED ? closedText : thrownText));
                    if (checkTurnoutsCanBeSet(turnoutList.get(i).getObject(), setting, s, curBlock, at)) {
                        log.debug("{}: setting turnout {} to {}", at.getTrainName(), to.getDisplayName(USERSYS),
                                (setting == Turnout.CLOSED ? closedText : thrownText));
                        if (useTurnoutConnectionDelay) {
                            to.setCommandedStateAtInterval(setting);
                        } else {
                            to.setCommandedState(setting);
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                        } //TODO: Check if this is needed, shouldnt turnout delays be handled at a lower level.
                    }
                } else {
                    if (to.getKnownState() != setting) {
                        // turnout is not set correctly
                        if (set) {
                            // setting has been requested, is Section free and Block unoccupied
                            if (checkTurnoutsCanBeSet(turnoutList.get(i).getObject(), setting, s, curBlock, at)) {
                                // send setting command
                                log.debug("{}: turnout {} commanded to {}", at.getTrainName(), to.getDisplayName(),
                                        (setting == Turnout.CLOSED ? closedText : thrownText));
                                if (useTurnoutConnectionDelay) {
                                    to.setCommandedStateAtInterval(setting);
                                } else {
                                    to.setCommandedState(setting);
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {
                                }  //TODO: move this to separate thread
                            } else {
                                turnoutsOK = false;
                            }
                        } else {
                            turnoutsOK = false;
                        }
                    } else {
                        log.debug("{}: turnout {} already {}, skipping", at.getTrainName(), to.getDisplayName(USERSYS),
                                (setting == Turnout.CLOSED ? closedText : thrownText));
                    }
                }
                if (turnoutList.get(i).getObject() instanceof LayoutSlip) {
                    //Look at the state of the second turnout in the slip
                    setting = ((LayoutSlip) turnoutList.get(i).getObject()).getTurnoutBState(turnoutList.get(i).getExpectedState());
                    to = ((LayoutSlip) turnoutList.get(i).getObject()).getTurnoutB();
                    if (!trustKnownTurnouts) {
                        if (useTurnoutConnectionDelay) {
                            to.setCommandedStateAtInterval(setting);
                        } else {
                            to.setCommandedState(setting);
                        }
                    } else if (to.getKnownState() != setting) {
                        // turnout is not set correctly
                        if (set) {
                            // setting has been requested, is Section free and Block unoccupied
                            if ((s.getState() == Section.FREE) && (curBlock.getState() != Block.OCCUPIED)) {
                                // send setting command
                                if (useTurnoutConnectionDelay) {
                                    to.setCommandedStateAtInterval(setting);
                                } else {
                                    to.setCommandedState(setting);
                                }
                            } else {
                                turnoutsOK = false;
                            }
                        } else {
                            turnoutsOK = false;
                        }
                    }
                }
            }
            standingBlock = false;
            if (turnoutsOK) {
                // move to next Block if any
                if (nextBlockSeqNum >= 0) {
                    prevBlock = curBlock;
                    curBlock = nextBlock;
                    if ((exitPt != null) && (curBlock == exitPt.getBlock())) {
                        // next block is outside of the Section
                        nextBlock = exitPt.getFromBlock();
                        nextBlockSeqNum = -1;
                    } else {
                        if (direction == Section.FORWARD) {
                            nextBlockSeqNum++;
                        } else {
                            nextBlockSeqNum--;
                        }
                        nextBlock = s.getBlockBySequenceNumber(nextBlockSeqNum);
                        if (nextBlock == null) {
                            // there is no next Block
                            nextBlockSeqNum = -1;
                        }
                    }
                } else {
                    curBlock = null;
                }
            } else {
                curBlock = null;
            }
        }
        if (turnoutsOK) {
            return turnoutListForAllocatedSection;
        }
        return null;
    }

    /**
     * The turnouts needed to leave the Block a train is standing in for the
     * next Block, when the way it came into the Block is not known. Each entry
     * into the Block in this direction gives a list; a turnout is needed only
     * if every entry needs it, in the same state.
     *
     * @param connection the connectivity of the Block's panel
     * @param s          the Section being allocated
     * @param direction  the direction of travel through the Section
     * @param curBlock   the Block the train is standing in
     * @param nextBlock  the Block the train goes to next, or null if none
     * @return the turnouts and their states, in order; empty if none can be
     *         worked out
     */
    private List<LayoutTrackExpectedState<LayoutTurnout>> standingBlockTurnouts(ConnectivityUtil connection,
            Section s, int direction, Block curBlock, Block nextBlock) {
        List<LayoutTrackExpectedState<LayoutTurnout>> result = null;
        if (nextBlock == null) {
            return new ArrayList<>();
        }
        List<EntryPoint> entries = (direction == Section.FORWARD) ? s.getForwardEntryPointList()
                : s.getReverseEntryPointList();
        for (EntryPoint ep : entries) {
            if (ep.getBlock() != curBlock || ep.getFromBlock() == null) {
                continue;
            }
            List<LayoutTrackExpectedState<LayoutTurnout>> fromEntry
                    = connection.getTurnoutList(curBlock, ep.getFromBlock(), nextBlock, true);
            if (result == null) {
                result = new ArrayList<>(fromEntry);
            } else {
                result.removeIf(needed -> fromEntry.stream().noneMatch(e -> e.getObject() == needed.getObject()
                        && e.getExpectedState().equals(needed.getExpectedState())));
            }
        }
        if (result == null) {
            log.debug("Block {} has no entry in this direction, its turnouts are not checked", curBlock.getDisplayName());
            return new ArrayList<>();
        }
        return result;
    }

    /**
     * Check, and set if it is safe, a turnout in the Block a train is standing
     * in. A turnout that is not known to be set right is changed only if the
     * Block is not occupied; otherwise the train may be standing on it, so the
     * Section is not allocated until the turnout is set by hand.
     *
     * @param tes      the turnout and the state it is needed in
     * @param setting  the state needed (for a slip, its first turnout)
     * @param set      true to set the turnout if it is safe, false to check only
     * @param s        the Section being allocated
     * @param curBlock the Block the train is standing in
     * @param at       the train
     * @param useTurnoutConnectionDelay true if the turnout connection delay should be applied
     * @return true if the turnout is set right, or has been commanded to be
     */
    // package access for testing
    boolean standingBlockTurnoutOK(LayoutTrackExpectedState<LayoutTurnout> tes, int setting, boolean set,
            Section s, Block curBlock, ActiveTrain at, boolean useTurnoutConnectionDelay) {
        List<Turnout> turnouts = new ArrayList<>();
        List<Integer> settings = new ArrayList<>();
        turnouts.add(tes.getObject().getTurnout());
        settings.add(setting);
        if (tes.getObject() instanceof LayoutSlip) {
            turnouts.add(((LayoutSlip) tes.getObject()).getTurnoutB());
            settings.add(((LayoutSlip) tes.getObject()).getTurnoutBState(tes.getExpectedState()));
        }
        boolean ok = true;
        for (int i = 0; i < turnouts.size(); i++) {
            Turnout to = turnouts.get(i);
            int needed = settings.get(i);
            if (to == null || to.getKnownState() == needed) {
                continue;
            }
            if (set && checkTurnoutsCanBeSet(tes.getObject(), needed, s, curBlock, at)) {
                log.debug("{}: turnout {} in starting Block commanded to {}", at.getTrainName(), to.getDisplayName(USERSYS),
                        (needed == Turnout.CLOSED ? closedText : thrownText));
                if (useTurnoutConnectionDelay) {
                    to.setCommandedStateAtInterval(needed);
                } else {
                    to.setCommandedState(needed);
                }
                continue;
            }
            jmri.util.LoggingUtil.warnOnce(log, "{}: turnout {} in {}, where the train is standing, is not set {} for its route;"
                    + " set it by hand to let the train go", at.getTrainName(), to.getDisplayName(USERSYS),
                    curBlock.getDisplayName(USERSYS), (needed == Turnout.CLOSED ? closedText : thrownText));
            String neededText = (needed == Turnout.CLOSED ? closedText : thrownText);
            at.setWaitingFor(Bundle.getMessage("WaitingForTurnout", to.getDisplayName()));
            if (retryWhenChanged(to, at)) {
                tellDispatcher(at, to, curBlock, neededText);
            }
            ok = false;
        }
        return ok;
    }

    // turnouts trains are waiting for, and the trains waiting for each
    private final java.util.Map<Turnout, java.util.Set<ActiveTrain>> awaitedTurnouts = new java.util.HashMap<>();
    private final java.beans.PropertyChangeListener awaitedTurnoutListener = this::awaitedTurnoutChanged;

    /**
     * Try again when a turnout a train is waiting for changes. Nothing else
     * would prompt it: the train's Block is occupied already and stays so, and
     * a train's first Section is not in the queue of allocation requests.
     *
     * @param to the turnout
     * @param at the train waiting for it
     * @return true if the train has just started waiting for the turnout,
     *         false if it was waiting for it already
     */
    private boolean retryWhenChanged(Turnout to, ActiveTrain at) {
        synchronized (awaitedTurnouts) {
            java.util.Set<ActiveTrain> trains = awaitedTurnouts.get(to);
            if (trains == null) {
                trains = new java.util.HashSet<>();
                awaitedTurnouts.put(to, trains);
                to.addPropertyChangeListener("KnownState", awaitedTurnoutListener);
            }
            return trains.add(at);
        }
    }

    /**
     * Tell whoever is at the Dispatcher window that a train can't start
     * until a turnout is set by hand. The window doesn't wait for an answer,
     * so allocation carries on for the other trains. Nothing is shown where
     * there is no display.
     *
     * @param at         the train
     * @param to         the turnout it is waiting for
     * @param curBlock   the Block it is standing in
     * @param neededText the state the turnout is needed in, in words
     */
    private void tellDispatcher(ActiveTrain at, Turnout to, Block curBlock, String neededText) {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }
        String message = Bundle.getMessage("TurnoutUnderTrainMessage", at.getTrainName(), to.getDisplayName(),
                curBlock.getDisplayName(), neededText);
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> jmri.util.swing.JmriJOptionPane.showMessageDialogNonModal(
                _dispatcher, message, Bundle.getMessage("TurnoutUnderTrainTitle"),
                jmri.util.swing.JmriJOptionPane.WARNING_MESSAGE, null));
    }

    private void awaitedTurnoutChanged(java.beans.PropertyChangeEvent e) {
        Turnout to = (Turnout) e.getSource();
        java.util.Set<ActiveTrain> trains;
        synchronized (awaitedTurnouts) {
            trains = awaitedTurnouts.remove(to);
            to.removePropertyChangeListener("KnownState", awaitedTurnoutListener);
        }
        if (trains == null) {
            return;
        }
        for (ActiveTrain at : trains) {
            at.setWaitingFor(null); // said again if the turnout is still not right for it
            if (!_dispatcher.getActiveTrainsList().contains(at)) {
                continue; // terminated while waiting
            }
            if (at.getAllocatedSectionList().isEmpty()) {
                // its first Section: asked for again, as a failed first request is not kept
                at.initializeFirstAllocation();
            } else {
                _dispatcher.queueScanOfAllocationRequests();
            }
        }
    }

    /*
     * Check that the turnout is safe to change.
     */
    private boolean checkTurnoutsCanBeSet(LayoutTurnout layoutTurnout, int setting, Section s, Block b, ActiveTrain at) {
        if (layoutTurnout instanceof LayoutDoubleXOver) {
            LayoutDoubleXOver lds = (LayoutDoubleXOver) layoutTurnout;
            if ((lds.getLayoutBlock().getBlock().getState() == Block.OCCUPIED)
                    || (lds.getLayoutBlockB().getBlock().getState() == Block.OCCUPIED)
                    || (lds.getLayoutBlockC().getBlock().getState() == Block.OCCUPIED)
                    || (lds.getLayoutBlockD().getBlock().getState() == Block.OCCUPIED)) {
                log.debug("{}: turnout {} cannot be set to {} DoubleXOver occupied.",
                        at.getTrainName(),layoutTurnout.getTurnout().getDisplayName(),
                        (setting == Turnout.CLOSED ? closedText : thrownText));
                return(false);
            }
            if ((_dispatcher.checkForBlockInAllocatedSection(lds.getLayoutBlock().getBlock(), s))
                    || (_dispatcher.checkForBlockInAllocatedSection(lds.getLayoutBlockB().getBlock(), s))
                    || (_dispatcher.checkForBlockInAllocatedSection(lds.getLayoutBlockC().getBlock(), s))
                    || (_dispatcher.checkForBlockInAllocatedSection(lds.getLayoutBlockD().getBlock(), s))) {
                log.debug("{}: turnout {} cannot be set to {} DoubleXOver already allocated to another train.",
                        at.getTrainName(), layoutTurnout.getTurnout().getDisplayName(),
                        (setting == Turnout.CLOSED ? closedText : thrownText));
                return(false);
            }
        } else if (layoutTurnout instanceof LayoutRHXOver) {
            LayoutRHXOver lds = (LayoutRHXOver) layoutTurnout;
            if ((lds.getLayoutBlock().getBlock().getState() == Block.OCCUPIED)
                    || (lds.getLayoutBlockC().getBlock().getState() == Block.OCCUPIED)) {
                log.debug("{}: turnout {} cannot be set to {} RHXOver occupied.",
                        at.getTrainName(),layoutTurnout.getTurnout().getDisplayName(),
                        (setting == Turnout.CLOSED ? closedText : thrownText));
                return(false);
            }
            if ((_dispatcher.checkForBlockInAllocatedSection(lds.getLayoutBlock().getBlock(), s))
                    || (_dispatcher.checkForBlockInAllocatedSection(lds.getLayoutBlockC().getBlock(), s))) {
                log.debug("{}: turnout {} cannot be set to {} RHXOver already allocated to another train.",
                        at.getTrainName(), layoutTurnout.getTurnout().getDisplayName(),
                        (setting == Turnout.CLOSED ? closedText : thrownText));
                return(false);
            }
        } else if (layoutTurnout instanceof LayoutLHXOver) {
            LayoutLHXOver lds = (LayoutLHXOver) layoutTurnout;
            if ((lds.getLayoutBlockB().getBlock().getState() == Block.OCCUPIED)
                    || (lds.getLayoutBlockD().getBlock().getState() == Block.OCCUPIED)) {
                log.debug("{}: turnout {} cannot be set to {} LHXOver occupied.",
                        at.getTrainName(),layoutTurnout.getTurnout().getDisplayName(),
                        (setting == Turnout.CLOSED ? closedText : thrownText));
                return(false);
            }
            if ((_dispatcher.checkForBlockInAllocatedSection(lds.getLayoutBlockB().getBlock(), s))
                    || (_dispatcher.checkForBlockInAllocatedSection(lds.getLayoutBlockD().getBlock(), s))) {
                log.debug("{}: turnout {} cannot be set to {} RHXOver already allocated to another train.",
                        at.getTrainName(), layoutTurnout.getTurnout().getDisplayName(),
                        (setting == Turnout.CLOSED ? closedText : thrownText));
                return(false);
            }
        }

        if (s.getState() == Section.FREE && b.getState() != Block.OCCUPIED) {
            return true;
        }
        return false;
    }

    private static final Logger log = LoggerFactory.getLogger(AutoTurnouts.class);
}
