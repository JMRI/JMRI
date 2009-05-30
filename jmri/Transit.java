// Transit.java

package jmri;

import jmri.Section;
import jmri.TransitSection;
import jmri.Block;
import jmri.jmrit.display.LayoutEditor;
import jmri.util.JmriJFrame;

import java.util.ArrayList;
import java.util.List;
import jmri.implementation.AbstractNamedBean;

/**
 * Class providing the basic implementation of a Transit.
 * <P>
 * Transits represent a group of Sections representing a specified path
 *  through a layout.
 * <P>
 * A Transit may have the following states.
 *		IDLE - indicating that it is available for "assignment"
 *      ASSIGNED - linked to a train to form an ActiveTrain
 * <P>
 * When assigned to a Transit, options may be set for the assigned Section.
 *  The Section and its options are kept in a TransitSection object.
 *<P>
 * To accomodate passing sidings and other track features, there may be 
 *  alternate Sections connecting two Sections in a Transit.  If so, one 
 *  Section is assigned as primary, and other Sections are assigned as 
 *  alternates.
 * <P>
 * A Section may be in a Transit more than once, for example if a train is 
 *  to make two or more loops around before going elsewhere.
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
 * @author			Dave Duchamp Copyright (C) 2008
 * 
 * @version			$Revision: 1.9 $
 */
public class Transit extends AbstractNamedBean
					implements java.io.Serializable {

    public Transit(String systemName, String userName) {
        super(systemName, userName);
    }

    public Transit(String systemName) {
        super(systemName);
    }

	/**
	 * Constants representing the state of the Transit.
	 * A Transit can be either:
	 *     IDLE - available for assignment to an ActiveTrain, or
	 *     ASSIGNED - assigned to an ActiveTrain
	 */ 
	public static final int IDLE = 0x02;
	public static final int ASSIGNED = 0x04;
	
    /**
     *  Instance variables (not saved between runs)
     */
	private int mState = Transit.IDLE;
	private ArrayList<TransitSection> mTransitSectionList = new ArrayList<TransitSection>();
	private int mMaxSequence = 0;

    /**
     * Query the state of the Transit
	 */
    public int getState() { return mState; }
    
    /**
     * Set the state of the Transit
	 */
    public void setState(int state) {
		if ( (state==Transit.IDLE) || (state==Transit.ASSIGNED) ) {
			int old = mState;
			mState = state;
			firePropertyChange("state", new Integer(old), new Integer(mState));
		}
		else
			log.error("Attempt to set Transit state to illegal value - "+state);
    }	
	
	/**
	 *  Add a TransitSection to the Transit
	 *  Section sequence numnbers are set automatically as Sections are added.
	 *	Returns "true" if Section was added.  Returns "false" if Section does not connect to 
	 *		the current Section.
	 */
	public void addTransitSection(TransitSection s) {
		mTransitSectionList.add(s);
		mMaxSequence = s.getSequenceNumber();
	}
	
	/** 
	 * Get a copy of this Transit's TransitSection list
	 */
	public ArrayList<TransitSection> getTransitSectionList() {
		ArrayList<TransitSection> list = new ArrayList<TransitSection>();
		for (int i = 0; i<mTransitSectionList.size(); i++)
			list.add(mTransitSectionList.get(i));
		return list;
	}
	
	/**
	 * Get the maximum sequence number used in this Transit
	 */
	public int getMaxSequence() {return mMaxSequence;}

	/**
	 * Remove all TransitSections
	 */
	public void removeAllSections() {
		mTransitSectionList.clear();
	}
	
	/**
	 * Test if a Section is in the Transit
	 */
	public boolean containsSection(Section s) {
		for (int i = 0; i<mTransitSectionList.size(); i++) {
			TransitSection ts = mTransitSectionList.get(i);
			if (ts.getSection()==s) return true;
		}	 
		return false;
	}
	
	/**
	 * Get a List of Sections with a given sequence number
	 */
	public ArrayList<Section> getSectionListBySeq(int seq) {
		ArrayList<Section> list = new ArrayList<Section>();
		for (int i = 0; i<mTransitSectionList.size(); i++) {
			TransitSection ts = mTransitSectionList.get(i);
			if (seq == ts.getSequenceNumber()) {
				list.add(ts.getSection());
			}
		}
		return list;
	}

	/**
	 * Get a List of sequence numbers for a given Section
	 */
	public ArrayList<Integer> getSeqListBySection(Section s) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i<mTransitSectionList.size(); i++) {
			TransitSection ts = mTransitSectionList.get(i);
			if (s == ts.getSection()) {
				list.add(new Integer(ts.getSequenceNumber()));
			}
		}
		return list;
	}
	
	/**
	 * Test if a Block is in the Transit
	 */
	public boolean containsBlock(Block b) {
		ArrayList<Block> bList = getInternalBlocksList();
		for (int i = 0; i<bList.size(); i++) {
			if (b == bList.get(i)) return true;
		}
		return false;
	}
	
	/**
	 * Count the number of times a Block is in this Transit
	 */
	public int getBlockCount(Block b) {
		ArrayList<Block> bList = getInternalBlocksList();
		int count = 0;
		for (int i = 0; i<bList.size(); i++) {
			if (b == bList.get(i)) count++;
		}
		return count;
	}
	/**
	 * Returns a Section from one of its Blocks and its sequence number
	 */
	public Section getSectionFromBlockAndSeq(jmri.Block b, int seq) {
		for (int i = 0; i<mTransitSectionList.size(); i++) {
			TransitSection ts = mTransitSectionList.get(i);
			if (ts.getSequenceNumber()==seq) {
				Section s = ts.getSection();
				if (s.containsBlock(b)) return s;
			}
		}
		return null;
	}
	/**
	 * Returns a Section from one of its EntryPoint Blocks and its sequence number
	 */
	public Section getSectionFromConnectedBlockAndSeq(jmri.Block b, int seq) {
		for (int i = 0; i<mTransitSectionList.size(); i++) {
			TransitSection ts = mTransitSectionList.get(i);
			if (ts.getSequenceNumber()==seq) {
				Section s = ts.getSection();
				if (s.connectsToBlock(b)) return s;
			}
		}
		return null;
	}
	/**
	 * Gets the direction of a Section in the transit from its sequence number
	 *    Returns 0 if direction was not found.
	 */
	public int getDirectionFromSectionAndSeq(Section s, int seq) {
		for (int i = 0; i<mTransitSectionList.size(); i++) {
			TransitSection ts = mTransitSectionList.get(i);
			if ( (ts.getSection() == s) && (ts.getSequenceNumber() == seq) ) {
				return ts.getDirection();
			}
		}
		return 0;
	}
	
	/** 
	 * Get a list of all blocks internal to this Transit
	 * Since Sections may be present more than once, blocks may be listed more than once.
	 * The sequence numbers of the Section the Block was found in are accumulated in a 
	 *   parallel list, which can be accessed by immediately calling getBlockSeqList().
	 */
	public ArrayList<Block> getInternalBlocksList() {
		ArrayList<Block> list = new ArrayList<Block>();
		blockSecSeqList.clear();
		for (int i = 0; i<mTransitSectionList.size(); i++) {
			TransitSection ts = mTransitSectionList.get(i);
			ArrayList<Block> bList = ts.getSection().getBlockList();
			for (int j = 0; j<bList.size(); j++) {
				list.add(bList.get(j));
				blockSecSeqList.add(new Integer(ts.getSequenceNumber()));
			}
		}
		return list;
	}
	// The following is mainly for internal use, but is available if requested immediately after
	//      getInternalBlocksList or getEntryBlocksList.
	private ArrayList<Integer> blockSecSeqList = new ArrayList<Integer>();
	public ArrayList<Integer> getBlockSeqList() { 
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i<blockSecSeqList.size(); i++) {
			list.add(blockSecSeqList.get(i));
		}
		return list;
	}
	
	/** 
	 * Get a list of all entry blocks to this Transit.
	 * These are Blocks that a train might enter from and be going in the 
	 *   Transit's direction.
	 * The sequence numbers of the Section the Block will enter are accumulated in a 
	 *   parallel list, which can be accessed by immediately calling getBlockSeqList().
	 */
	public ArrayList<Block> getEntryBlocksList() {
		ArrayList<Block> list = new ArrayList<Block>();
		ArrayList<Block> internalBlocks = getInternalBlocksList();
		blockSecSeqList.clear();
		for (int i = 0; i<mTransitSectionList.size(); i++) {
			TransitSection ts = mTransitSectionList.get(i);		
			List<EntryPoint> ePointList = null;
			if (ts.getDirection()==Section.FORWARD)
				ePointList = ts.getSection().getForwardEntryPointList();
			else
				ePointList = ts.getSection().getReverseEntryPointList();
			for (int j=0; j<ePointList.size(); j++) {
				Block b = (ePointList.get(j)).getFromBlock();
				boolean isInternal = false;
				for (int k = 0; k<internalBlocks.size(); k++) {
					if (b==internalBlocks.get(k)) isInternal = true;
				}
				if (!isInternal) {
					// not an internal Block, keep it
					list.add(b);
					blockSecSeqList.add(new Integer(ts.getSequenceNumber()));
				}				
			}
		}
		return list;
	}
	
	/** 
	 * Get a list of all destination blocks that can be reached from a 
	 *   specified starting block, "startBlock". "startInTransit" should be set "true" if 
	 *   "startBlock" is in the Transit, and "false" otherwise.
	 * The sequence numbers of the Section the Block was found in are accumulated in a 
	 *   parallel list, which can be accessed by immediately calling getDestBlocksSeqList().
	 * Note: A train may not terminate in the same Section in which it starts!
	 * Note: A train must terminate in a block within the transit!
	 */
	public ArrayList<Block> getDestinationBlocksList(Block startBlock, boolean startInTransit) {
		ArrayList<Block> list = new ArrayList<Block>();
		destBlocksSeqList.clear();
		if (startBlock==null) return list;
		// get the sequence number of the Section of the starting Block
		int startSeq = -1;
		ArrayList<Block> startBlocks = null;
		if (startInTransit) {
			startBlocks = getInternalBlocksList();
		}
		else {
			startBlocks = getEntryBlocksList();
		}
		// programming note: the above calls initialize blockSecSeqList.
		for (int k = 0; ((k<startBlocks.size()) && (startSeq==-1)); k++) {
			if (startBlock==startBlocks.get(k)) {
				startSeq = (blockSecSeqList.get(k)).intValue();
			}
		}
		ArrayList<Block> internalBlocks = getInternalBlocksList();
		for (int i = internalBlocks.size(); i>0; i--) {
			if ( ((blockSecSeqList.get(i-1)).intValue() ) > startSeq) {
				// could stop in this block, keep it
				list.add(internalBlocks.get(i-1));
				destBlocksSeqList.add(blockSecSeqList.get(i-1));
			}
		}
		return list;
	}
	private ArrayList<Integer> destBlocksSeqList = new ArrayList<Integer>();
	public ArrayList<Integer> getDestBlocksSeqList() { 
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i<destBlocksSeqList.size(); i++) {
			list.add(destBlocksSeqList.get(i));
		}
		return list;
	}
	
	/**
	 * Determines whether this Transit is capable of continuous running.  That is, after an  
	 *	   Active Train completes the Transit, can it automatically be set up to start again?
	 *  To be resetable, the first Section and the last Section must be the same Section, and
	 *	   the first and last Sections must be defined to run in the same direction.
	 * Returns 'true' if continuous running is possible, returns 'false' otherwise.
	 */
	public boolean canBeResetWhenDone() {
		TransitSection firstTS = mTransitSectionList.get(0);
		TransitSection lastTS = mTransitSectionList.get(mTransitSectionList.size()-1);
		if (firstTS.getSection() != lastTS.getSection())
			return false;
		// same Section, check direction
		if (firstTS.getDirection() != lastTS.getDirection())
			return false;		
		return true;
	}
	
	/**
	 * Checks that exit Signal Heads are in place for all Sections in this Transit and for 
	 *		block boundaries at turnouts or level crossings within Sections of the Transit for 
	 *		the direction defined in this Transit.
	 * Signal Heads are not required at anchor point block boundaries where both blocks are 
	 *		within the same Section, and for turnouts with two or more connections in the same Section.
	 * Returns "true" if everything is OK. Sends message to the user if a signal head is missing,
	 *		and returns 'false'. Quits looking after finding the first missing signal head.
	 */
	public boolean checkSignals(JmriJFrame frame, LayoutEditor panel) {
// djd debugging
// add code here	
		return true;
	}
	
	/**
	 * Validates connectivity through the Transit.
	 * Returns "true" if everything is OK. Sends message to the user if break in connectivity 
	 *		is detected, ,and returns 'false'. Quits looking after finding the first problem.
	 */
	public boolean validateConnectivity(JmriJFrame frame, LayoutEditor panel) {
// djd debugging
// add code here	
		return true;
	}
	    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Transit.class.getName());
	
}

/* @(#)Transit.java */
