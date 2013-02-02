// AutoTurnouts.java

package jmri.jmrit.dispatcher;

import org.apache.log4j.Logger;
import jmri.Block;
import jmri.Section;
import jmri.EntryPoint;
import jmri.Transit;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.ConnectivityUtil;

import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Handles automatic checking and setting of turnouts when Dispatcher 
 *		allocates a Section in a specific direction.
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
 * @author			Dave Duchamp    Copyright (C) 2008-2009
 * @version			$Revision$
 */

public class AutoTurnouts {

	static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");

	public AutoTurnouts (DispatcherFrame d) {
		_dispatcher = d;
	}
	
	// operational variables
	protected DispatcherFrame _dispatcher = null;
	boolean userInformed = false;
	
	/** 
	 * Check that all turnouts are correctly set for travel in the designated Section
	 *   to the next Section.
	 *
	 * Returns 'true' if affected turnouts are correctly set, returns 'false' otherwise.
	 * If arguments are not valid for this Section, returns 'false', and issues an error
	 *   message. 
	 * NOTE: This method requires use of the connectivity stored in a Layout Editor panel.
	 */
	protected boolean checkTurnoutsInSection(Section s, int seqNum, Section nextSection,
				ActiveTrain at, LayoutEditor le) {
		return turnoutUtil(s, seqNum, nextSection, at, le, false, false);
	}
	/** 
	 * Set all turnouts for travel in the designated Section to the next Section.
	 *
	 * Checks that all turnouts are correctly set for travel in this Section to the next
	 *   Section, and sets any turnouts that are not correct.
	 * The Section must be FREE to set its turnouts. Testing for FREE only occurs if a  
	 *   command needs to be issued.
	 * For a command to be issued to set a turnout, the Block containing that turnout must be 
	 *   unoccupied.
	 * NOTE: This method does not wait for turnout feedback--it assumes the turnout will be
	 *   set correctly if a command is issued.
	 * Returns 'true' if affected turnouts are correctly set or commands have been issued to 
	 *   set any that aren't set correctly.  If a needed command could not be issued because 
	 *   the turnout's Block is occupied, returns 'false' and sends a warn message.
	 * If arguments are not valid for this Transit, returns 'false', and issues an error
	 *   message. 
	 * NOTE: This method requires use of the connectivity stored in a Layout Editor panel.
	 */
	protected boolean setTurnoutsInSection(Section s, int seqNum, Section nextSection,
				ActiveTrain at, LayoutEditor le, boolean alwaysSet) {
		return turnoutUtil(s, seqNum, nextSection, at, le, alwaysSet, true);
	}
	/**
	 * Internal method implementing the above two methods
	 * Returns 'true' if turnouts are set correctly, 'false' otherwise
	 * If 'set' is 'true' this routine will attempt to set the turnouts, if 'false' it reports 
	 *	 what it finds. 
	 */
	private boolean turnoutUtil(Section s, int seqNum, Section nextSection,
				ActiveTrain at, LayoutEditor le, boolean alwaysSet, boolean set) {
		// validate input and initialize
		Transit tran = at.getTransit();
		if ( (s==null) || (seqNum>tran.getMaxSequence()) || (!tran.containsSection(s)) || (le==null) ) {
			log.error ("Invalid argument when checking or setting turnouts in Section.");
			return false;
		}
		int direction = at.getAllocationDirectionFromSectionAndSeq(s,seqNum);
		if (direction==0) {
			log.error("Invalid Section/sequence arguments when checking or setting turnouts");
			return false;
		}
		// check for no turnouts in this section
		if ( (s.getForwardEntryPointList().size()<=1) && (s.getReverseEntryPointList().size()<=1) ) {
			// no possibility of turnouts
			return true;
		}
		// initialize connectivity utilities and beginning block pointers
		ConnectivityUtil ct = le.getConnectivityUtil();
		Section prevSection = at.getLastAllocatedSection();
		EntryPoint entryPt = null;
		if (prevSection!=null) {
			entryPt = s.getEntryPointFromSection(prevSection, direction);
		}
		else if (!s.containsBlock(at.getStartBlock())) {
			entryPt = s.getEntryPointFromBlock(at.getStartBlock(), direction);
		}
		EntryPoint exitPt = null;
		if (nextSection!=null) exitPt = s.getExitPointToSection(nextSection, direction);
		Block curBlock = null;    // must be in the section
		Block prevBlock = null;	  // must start outside the section or be null
		if (entryPt!=null) {
			curBlock = entryPt.getBlock();
			prevBlock = entryPt.getFromBlock();
		}
		else if (s.containsBlock(at.getStartBlock())) {
			curBlock = at.getStartBlock();
		}
		else {
			log.error("Error in turnout check/set request - initial Block and Section mismatch");
			return false;
		}
		int curBlockSeqNum = s.getBlockSequenceNumber(curBlock);   // sequence number of curBlock in Section
		if (entryPt!=null) prevBlock = entryPt.getFromBlock();
		Block nextBlock = null;
		// may be either in the section or the first block in the next section
		int nextBlockSeqNum = -1;   // sequence number of nextBlock in Section (-1 indicates outside Section)
		if (exitPt!=null) {
			if (curBlock==exitPt.getBlock()) {
				// next Block is outside of the Section
				nextBlock = exitPt.getFromBlock();
			}
			else {
				// next Block is inside the Section
				if (direction==Section.FORWARD) {
					nextBlock = s.getBlockBySequenceNumber(curBlockSeqNum+1);
					nextBlockSeqNum = curBlockSeqNum+1;
				}
				else if (direction==Section.REVERSE) {
					nextBlock = s.getBlockBySequenceNumber(curBlockSeqNum-1);
					nextBlockSeqNum = curBlockSeqNum-1;
				}
				if ( (nextBlock==null) && (curBlock!=at.getEndBlock()) ) {
					log.error("Error in block sequence numbers when setting/checking turnouts");
					return false;
				}
			}
		}
		ArrayList<LayoutTurnout> turnoutList = null;
		ArrayList<Integer> settingsList = null;		
		// get turnouts by Block
		boolean turnoutsOK = true;
		while (curBlock!=null) {
			turnoutList = ct.getTurnoutList(curBlock, prevBlock, nextBlock);
			settingsList = ct.getTurnoutSettingList();
			// loop over turnouts checking and optionally setting turnouts
			for (int i = 0; i<turnoutList.size(); i++) {
				Turnout to = turnoutList.get(i).getTurnout();
				int setting = settingsList.get(i).intValue();
                if(turnoutList.get(i) instanceof LayoutSlip){
                    setting = ((LayoutSlip)turnoutList.get(i)).getTurnoutState(settingsList.get(i));
                }
				// test current setting
				if (alwaysSet) {
					to.setCommandedState(setting);
				}
				else if (to.getKnownState()!=setting) {
					// turnout is not set correctly
					if (set) {
						// setting has been requested, is Section free and Block unoccupied
						if ( (s.getState()==Section.FREE) && (curBlock.getState()!=Block.OCCUPIED) ) {
							// send setting command
							to.setCommandedState(setting);
						}
						else {
							turnoutsOK = false;
						}
					}
					else {
						turnoutsOK = false;
					}
				}
                if(turnoutList.get(i) instanceof LayoutSlip){
                    //Look at the state of the second turnout in the slip
                    setting = ((LayoutSlip)turnoutList.get(i)).getTurnoutBState(settingsList.get(i));
                    to = ((LayoutSlip)turnoutList.get(i)).getTurnoutB();
                    if (alwaysSet) {
                        to.setCommandedState(setting);
                    }
                    else if (to.getKnownState()!=setting) {
                        // turnout is not set correctly
                        if (set) {
                            // setting has been requested, is Section free and Block unoccupied
                            if ( (s.getState()==Section.FREE) && (curBlock.getState()!=Block.OCCUPIED) ) {
                                // send setting command
                                to.setCommandedState(setting);
                            }
                            else {
                                turnoutsOK = false;
                            }
                        }
                        else {
                            turnoutsOK = false;
                        }
                    }
                }
			}
			if (turnoutsOK) {
				// move to next Block if any
				if ( nextBlockSeqNum >= 0 ) {
					prevBlock = curBlock;
					curBlock = nextBlock;
					curBlockSeqNum = nextBlockSeqNum;
					if ( (exitPt!=null) && (curBlock==exitPt.getBlock()) ) {
						// next block is outside of the Section
						nextBlock = exitPt.getFromBlock();
						nextBlockSeqNum = -1;
					}
					else {
						if (direction==Section.FORWARD) nextBlockSeqNum ++;
						else nextBlockSeqNum --;
						nextBlock = s.getBlockBySequenceNumber(nextBlockSeqNum);
						if (nextBlock == null) {
							// there is no next Block
							nextBlockSeqNum = -1;
						}
					}
				}
				else {
					curBlock = null;
				}
			}
			else {
				curBlock = null;
			}
		}
		return turnoutsOK;
	}
	
   
    static Logger log = Logger.getLogger(AutoTurnouts.class.getName());
}

/* @(#)AutoTurnouts.java */
