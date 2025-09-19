package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import java.util.List;
import jmri.Block;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.Turnout;
import jmri.TransitSection;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.layoutEditor.ConnectivityUtil;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTurntable;
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
    boolean userInformed = false;

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
        // initialize response structure
        List<LayoutTrackExpectedState<LayoutTurnout>> turnoutListForAllocatedSection = new ArrayList<>();
        // validate input and initialize
        Transit tran = at.getTransit();
        if ((s == null) || (seqNum > tran.getMaxSequence()) || (!tran.containsSection(s))) {
            log.error("Invalid argument when checking or setting turnouts in Section.");
            return null;
        }

        // DIAGNOSTIC: Dump the transit structure the first time we process a section for this train.
        if (prevSection == null) {
            log.debug("DIAGNOSTIC:: (turnoutUtil): Dumping Transit structure for train '{}'", at.getTrainName());
            List<TransitSection> transitSections = tran.getTransitSectionList();
            for (int i = 0; i < transitSections.size(); i++) {
                Section transitSection = transitSections.get(i).getSection();
                log.debug("DIAGNOSTIC:: (turnoutUtil):   - Transit Section {}: '{}'", i, transitSection.getDisplayName(USERSYS));
                List<Block> blocks = transitSection.getBlockList();
                for (int j = 0; j < blocks.size(); j++) {
                    Block transitBlock = blocks.get(j);
                    log.debug("DIAGNOSTIC:: (turnoutUtil):     - Block {}: '{}'", j, transitBlock.getDisplayName(USERSYS));
                }
            }
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
        boolean isTurntableStart = false;
        if (entryPt != null) {
            curBlock = entryPt.getBlock();
            prevBlock = entryPt.getFromBlock();
            curBlockSeqNum = s.getBlockSequenceNumber(curBlock);
        } else if ( !at.isAllocationReversed() && s.containsBlock(at.getStartBlock())) {
            curBlock = at.getStartBlock();
            curBlockSeqNum = s.getBlockSequenceNumber(curBlock);
            //Get the previous block so that we can set the turnouts in the current block correctly.

            // Isolate special handling for a train starting on a turntable.
            var lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
            LayoutBlock curLBlock = lbm.getLayoutBlock(curBlock);
            if (curLBlock != null) {
                for (LayoutEditor editor : InstanceManager.getDefault(jmri.jmrit.display.EditorManager.class).getAll(LayoutEditor.class)) { // NOI18N
                    for (LayoutTurntable turntable : editor.getLayoutTurntables()) { // NOI18N
                        if (turntable.getLayoutBlock() == curLBlock) {
                            isTurntableStart = true;
                            break;
                        }
                    }
                    if (isTurntableStart) break;
                }
            }

            if (isTurntableStart) {
                // A train starting on a turntable spur has no previous block.
                // prevBlock = null;   // null by default
            } else {
                // Standard logic for trains starting in a regular section.
                if (direction == Section.FORWARD) {
                    prevBlock = s.getBlockBySequenceNumber(curBlockSeqNum - 1);
                } else if (direction == Section.REVERSE) {
                    prevBlock = s.getBlockBySequenceNumber(curBlockSeqNum + 1);
                }
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
        if (log.isTraceEnabled()){
            log.trace("DIAGNOSTIC:: (turnoutUtil): For train '{}', in Section '{}'", at.getTrainName(), s.getDisplayName(USERSYS));
            log.trace("DIAGNOSTIC:: (turnoutUtil):   - Direction: {}", (direction == Section.FORWARD ? "FORWARD" : "REVERSE"));
            log.trace("DIAGNOSTIC:: (turnoutUtil):   - PrevBlock: {}", (prevBlock != null ? prevBlock.getDisplayName(USERSYS) : "null"));
            log.trace("DIAGNOSTIC:: (turnoutUtil):   - CurBlock: {}", (curBlock != null ? curBlock.getDisplayName(USERSYS) : "null"));
            log.trace("DIAGNOSTIC:: (turnoutUtil):   - CurBlockSeqNum in Section: {}", curBlockSeqNum);
            log.trace("DIAGNOSTIC:: (turnoutUtil):   - ExitPt: {}", (exitPt != null ? exitPt.getBlock().getDisplayName(USERSYS) : "null"));
            log.trace("DIAGNOSTIC:: (turnoutUtil):   - Train StartBlock: {}", (at.getStartBlock() != null ? at.getStartBlock().getDisplayName(USERSYS) : "null"));
            log.trace("DIAGNOSTIC:: (turnoutUtil):   - Train EndBlock: {}", (at.getEndBlock() != null ? at.getEndBlock().getDisplayName(USERSYS) : "null"));
            log.trace("DIAGNOSTIC:: (turnoutUtil):   - Allocation Reversed: {}", at.isAllocationReversed());
        }

        Block nextBlock = null;
        // may be either in the section or the first block in the next section
        int nextBlockSeqNum = -1;   // sequence number of nextBlock in Section (-1 indicates outside Section)

        // Special handling for a train starting on a turntable.
        if (isTurntableStart && exitPt != null) {
            nextBlock = exitPt.getBlock();
            nextBlockSeqNum = s.getBlockSequenceNumber(nextBlock);
            log.debug("DIAGNOSTIC:: (turnoutUtil):   - Turntable start detected. Setting nextBlock to exit point: '{}'", nextBlock.getDisplayName(USERSYS));
        } else if (exitPt != null && curBlock == exitPt.getBlock()) {
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
                log.error("[{}]Error in block sequence numbers when setting/checking turnouts.", (curBlock != null ? curBlock.getDisplayName(USERSYS) : "null"));
                return null;
            }
        }

        List<LayoutTrackExpectedState<LayoutTurnout>> turnoutList = new ArrayList<>();
        // get turnouts by Block
        boolean turnoutsOK = true;

        var layoutBlockManger = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        while (curBlock != null) {
            /*No point in getting the list if the previous block is null as it will return empty and generate an error,
             this will only happen on the first run.  Plus working on the basis that the turnouts in the current block would have already of
             been set correctly for the train to have arrived in the first place.
             */

            if (prevBlock != null) {
                var blockName = curBlock.getUserName();
                if (blockName != null) {
                    var lblock = layoutBlockManger.getLayoutBlock(blockName);
                    if (lblock != null) {
                        var panel = lblock.getMaxConnectedPanel();
                        if (panel != null) {
                            var connection = new ConnectivityUtil(panel);
                            turnoutList = connection.getTurnoutList(curBlock, prevBlock, nextBlock, true);
                        }
                    }
                }
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
            // *** Turntable Alignment Logic ***
            // This logic handles aligning a turntable when a train is entering or exiting it.
            // It checks the transition from the previous block to the current block.
            //if (turnoutsOK && curBlock != null && prevBlock != null) {     // curBlock cannot be null here
            if (turnoutsOK && prevBlock != null) {
                var lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
                LayoutBlock curLBlock = lbm.getLayoutBlock(curBlock);
                LayoutBlock prevLBlock = lbm.getLayoutBlock(prevBlock);

                if (curLBlock != null && prevLBlock != null) {
                    for (LayoutEditor editor : InstanceManager.getDefault(jmri.jmrit.display.EditorManager.class).getAll(LayoutEditor.class)) { // NOI18N
                        for (LayoutTurntable turntable : editor.getLayoutTurntables()) { // NOI18N
                            int rayIndex = -1;
                            
                            log.debug("DIAGNOSTIC: Checking turntable '{}'. Previous block: '{}', Current block: '{}'",
                                    turntable.getName(),
                                    prevLBlock.getDisplayName(),
                                    curLBlock.getDisplayName());

                            // Case 1: Exiting turntable (previous is turntable, current is ray)
                            if (turntable.getLayoutBlock() == prevLBlock && turntable.isRayBlock(curLBlock)) {
                                log.debug("DIAGNOSTIC:   - Matched Case 1 (Exiting Turntable).");
                                rayIndex = turntable.getRayIndexForBlock(curLBlock);
                            } // Case 2: Entering turntable (previous is ray, current is turntable)
                            else if (turntable.getLayoutBlock() == curLBlock && turntable.isRayBlock(prevLBlock)) {
                                log.debug("DIAGNOSTIC:   - Matched Case 2 (Entering Turntable).");
                                rayIndex = turntable.getRayIndexForBlock(prevLBlock);
                            }

                            if (rayIndex != -1) {
                                Turnout positionTurnout = turntable.getTurnoutForRay(rayIndex);
                                // Determine if we are exiting the turntable. This is true for Case 1.
                                boolean isExiting = (turntable.getLayoutBlock() == prevLBlock);

                                if (positionTurnout != null) {
                                    log.debug("{}: Path requires turntable {} to be set for ray {}", at.getTrainName(), turntable.getName(), rayIndex); // NOI18N
                                    if (!trustKnownTurnouts || positionTurnout.getKnownState() != Turnout.THROWN) {
                                        if (set) {
                                            // The condition to check before setting the turntable.
                                            // The section being allocated must be FREE.
                                            boolean canSetTurntable = (s.getState() == Section.FREE);
                                            if (!isExiting) {
                                                // When entering, the turntable block must also be unoccupied.
                                                canSetTurntable = canSetTurntable && (turntable.getLayoutBlock().getBlock().getState() != Block.OCCUPIED);
                                            }
                                            // When exiting, we allow the turntable block to be occupied by the current train.
                                            if (canSetTurntable) {
                                                log.debug("{}: Setting turntable {} position via turnout {}", // NOI18N
                                                        at.getTrainName(), turntable.getName(), positionTurnout.getSystemName()); // NOI18N
                                                if (useTurnoutConnectionDelay) {
                                                    positionTurnout.setCommandedStateAtInterval(Turnout.THROWN);
                                                } else {
                                                    positionTurnout.setCommandedState(Turnout.THROWN);
                                                }
                                            } else {
                                                log.warn("{}: Cannot set turntable {} because section is not FREE or turntable block is OCCUPIED.", at.getTrainName(), turntable.getName());
                                                turnoutsOK = false; // NOI18N
                                            }
                                        } else { // just checking, not setting
                                            log.debug("{}: Turntable {} is not aligned correctly.", at.getTrainName(), turntable.getName());
                                            turnoutsOK = false; // NOI18N
                                        }
                                    }
                                } else {
                                    // This is an error. A turntable is on the path but has no configured turnout for this ray.
                                    log.error("{}: Turntable '{}' is on the path, but no virtual turnout is configured for ray index {}. Dispatch cannot proceed.", // NOI18N
                                            at.getTrainName(), turntable.getName(), rayIndex); // NOI18N
                                    turnoutsOK = false; // NOI18N
                                }
                                break; // Found the turntable for this path, no need to check others in this editor.
                            }
                        }
                        if (!turnoutsOK) break; // Exit editor loop if something failed
                    }
                }
            }
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

    private final static Logger log = LoggerFactory.getLogger(AutoTurnouts.class);
}
