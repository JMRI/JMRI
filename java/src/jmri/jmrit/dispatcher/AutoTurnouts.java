package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import java.util.List;
import jmri.Block;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.ConnectivityUtil;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTrackExpectedState;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles automatic checking and setting of turnouts when Dispatcher allocates
 * a Section in a specific direction.
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is open source software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
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
     * @param s           the section to check
     * @param seqNum      sequence number for the section
     * @param nextSection the following section
     * @param at          the associated train
     * @param le          the associated layout panel
     * @param prevSection the prior section
     * @return true if affected turnouts are correctly set; false otherwise.
     */
    protected boolean checkTurnoutsInSection(Section s, int seqNum, Section nextSection,
            ActiveTrain at, LayoutEditor le, Section prevSection) {
        return turnoutUtil(s, seqNum, nextSection, at, le, false, false, prevSection);
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
     * NOTE: This method requires use of the connectivity stored in a Layout
     * Editor panel.
     *
     * @param s                  the section to check
     * @param seqNum             sequence number for the section
     * @param nextSection        the following section
     * @param at                 the associated train
     * @param le                 the associated layout panel
     * @param trustKnownTurnouts true to trust known turnouts
     * @param prevSection        the prior section
     *
     * @return true if affected turnouts are correctly set or commands have been
     *         issued to set any that aren't set correctly; false if a needed
     *         command could not be issued because the turnout's Block is
     *         occupied
     */
    protected boolean setTurnoutsInSection(Section s, int seqNum, Section nextSection,
            ActiveTrain at, LayoutEditor le, boolean trustKnownTurnouts, Section prevSection) {
        return turnoutUtil(s, seqNum, nextSection, at, le, trustKnownTurnouts, true, prevSection);
    }

    /**
     * Internal method implementing the above two methods Returns 'true' if
     * turnouts are set correctly, 'false' otherwise If 'set' is 'true' this
     * routine will attempt to set the turnouts, if 'false' it reports what it
     * finds.
     */
    private boolean turnoutUtil(Section s, int seqNum, Section nextSection,
            ActiveTrain at, LayoutEditor le, boolean trustKnownTurnouts, boolean set, Section prevSection) {
        // validate input and initialize
        Transit tran = at.getTransit();
        if ((s == null) || (seqNum > tran.getMaxSequence()) || (!tran.containsSection(s)) || (le == null)) {
            log.error("Invalid argument when checking or setting turnouts in Section.");
            return false;
        }
        int direction = at.getAllocationDirectionFromSectionAndSeq(s, seqNum);
        if (direction == 0) {
            log.error("Invalid Section/sequence arguments when checking or setting turnouts");
            return false;
        }
        // Did have this set to include SignalMasts as part of the && statement
        //Sections created using Signal masts will generally only have a single entry/exit point.
        // check for no turnouts in this section
        if (_dispatcher.getSignalType() == DispatcherFrame.SIGNALHEAD && (s.getForwardEntryPointList().size() <= 1) && (s.getReverseEntryPointList().size() <= 1)) {
            log.debug("No entry points lists");
            // no possibility of turnouts
            return true;
        }
        // initialize connectivity utilities and beginning block pointers
        ConnectivityUtil ct = le.getConnectivityUtil();
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
                    at.getActiveTrainName(),at.isAllocationReversed(),s.getUserName(),
                    at.getStartBlock().getUserName(),at.getEndBlock().getUserName());
            } catch (Exception ex ) {
                log.warn(ex.getLocalizedMessage());
            }
            return true;
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
            if ((nextBlock == null) && (curBlock != at.getEndBlock())) {
                log.error("Error in block sequence numbers when setting/checking turnouts");
                return false;
            }
        }

        List<LayoutTrackExpectedState<LayoutTurnout>> turnoutList = new ArrayList<>();
        // get turnouts by Block
        boolean turnoutsOK = true;
        while (curBlock != null) {
            /*No point in getting the list if the previous block is null as it will return empty and generate an error,
             this will only happen on the first run.  Plus working on the basis that the turnouts in the current block would have already of
             been set correctly for the train to have arrived in the first place.
             */
            if (prevBlock != null) {
                turnoutList = ct.getTurnoutList(curBlock, prevBlock, nextBlock);
            }
            // loop over turnouts checking and optionally setting turnouts
            for (int i = 0; i < turnoutList.size(); i++) {
                Turnout to = turnoutList.get(i).getObject().getTurnout();
                int setting = turnoutList.get(i).getExpectedState();
                if (turnoutList.get(i).getObject() instanceof LayoutSlip) {
                    setting = ((LayoutSlip) turnoutList.get(i).getObject()).getTurnoutState(turnoutList.get(i).getExpectedState());
                }
                // check or ignore current setting based on flag, set in Options
                if (!trustKnownTurnouts) {
                    log.debug("{}: setting turnout {} to {}", at.getTrainName(), to.getFullyFormattedDisplayName(),
                            (setting == Turnout.CLOSED ? closedText : thrownText));
                    to.setCommandedState(setting);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }  //TODO: move this to separate thread
                } else {
                    if (to.getKnownState() != setting) {
                        // turnout is not set correctly
                        if (set) {
                            // setting has been requested, is Section free and Block unoccupied
                            if ((s.getState() == Section.FREE) && (curBlock.getState() != Block.OCCUPIED)) {
                                // send setting command
                                log.debug("{}: turnout {} commanded to {}", at.getTrainName(), to.getFullyFormattedDisplayName(),
                                        (setting == Turnout.CLOSED ? closedText : thrownText));
                                to.setCommandedState(setting);
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
                        log.debug("{}: turnout {} already {}, skipping", at.getTrainName(), to.getFullyFormattedDisplayName(),
                                (setting == Turnout.CLOSED ? closedText : thrownText));
                    }
                }
                if (turnoutList.get(i).getObject() instanceof LayoutSlip) {
                    //Look at the state of the second turnout in the slip
                    setting = ((LayoutSlip) turnoutList.get(i).getObject()).getTurnoutBState(turnoutList.get(i).getExpectedState());
                    to = ((LayoutSlip) turnoutList.get(i).getObject()).getTurnoutB();
                    if (!trustKnownTurnouts) {
                        to.setCommandedState(setting);
                    } else if (to.getKnownState() != setting) {
                        // turnout is not set correctly
                        if (set) {
                            // setting has been requested, is Section free and Block unoccupied
                            if ((s.getState() == Section.FREE) && (curBlock.getState() != Block.OCCUPIED)) {
                                // send setting command
                                to.setCommandedState(setting);
                            } else {
                                turnoutsOK = false;
                            }
                        } else {
                            turnoutsOK = false;
                        }
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
        return turnoutsOK;
    }

    private final static Logger log = LoggerFactory.getLogger(AutoTurnouts.class);
}
