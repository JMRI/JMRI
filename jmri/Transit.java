// Transit.java

package jmri;

import jmri.Section;
import jmri.TransitSection;
import jmri.Block;
import jmri.Timebase;
import java.util.ArrayList;
import java.util.List;

/**
 * Class providing the basic implementation of a Transit.
 * <P>
 * Transits represent a group of Sections representing a specified path
 *  through a layout.
 * <P>
 * A Transit may have the following states.
 *		IDLE - indicating that it is available for "assignment"
 *      ASSIGNED - linked to a train with its mode of running set
 *      RUNNING - a train is being run through the transit path 
 *				according to the assigned  mode
 * <P>
 * A Transit may be assigned one of the following modes, which specify 
 *	how the assigned train will be run through the transit:
 *		UNKNOWN - indicates that a mode has not yet been assigned to this
 *				transit. All transits are set to UNKNOWN mode when loaded.
 *		AUTOMATIC - indicates the assigned train will be run under automatic 
 *				control of the computer.
 *		MANUAL - indicates the assigned train will be run by an operator  
 *				using a throttle. The computer will allocate sections to 
 *				the train as needed, and arbitrate any conflicts between 
 *				trains. The operator is expected to follow signals set 
 *				by the computer. (Automated Computer Dispatching)
 *		DISPATCHED - indicates the assigned train will be run by an operator  
 *				using a throttle. A dispatcher will allocate sections to 
 *				trains as needed, control signals, using a CTC panel,  and 
 *				arbitrate any conflicts between trains. (Human Dispatcher).
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
 * @version			$Revision: 1.2 $
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
	 * A Transit can be either IDLE - available), ASSIGNED - assigned to 
	 *   a train (throttle), or RUNNING - running a train.
	 */ 
	public static final int IDLE = 0x02;
	public static final int ASSIGNED = 0x04;
	public static final int RUNNING = 0X08;	
	
	/** 
	 * Constants representing the mode of running
	 * The mode of a transit can be UNKNOWN - all IDLE transits have no 
	 *  assigned mode, AUTOMATIC - set up to be automatically run by the 
	 *  computer, MANUAL - set up to be run manually by a throttle following
	 *  computer-controlled layout signals and computer-controlled section 
	 *  allocation, or DISPATCHED - run manually by a throttle with signals 
	 *  and section allocation controlled by a dispatcher.
	 */
	public static final int UNKNOWN = 0x01;
	public static final int AUTOMATIC = 0x02;
	public static final int MANUAL = 0x04;
	public static final int DISPATCHED = 0x08;

    /**
     *  Instance variables (not saved between runs)
     */
	private int mState = Transit.IDLE;
	private int mMode = Transit.UNKNOWN;
	private ArrayList mTransitSectionList = new ArrayList();
	private int mMaxSequence = 0;

    /**
     * Query the state of the Transit
	 */
    public int getState() { return mState; }
    
    /**
     * Set the state of the Transit
	 */
    public void setState(int state) {
		if ( (state==Transit.IDLE) || (state==Transit.ASSIGNED) || (state==Transit.RUNNING) )
			mState = state;
		else
			log.error("Attempt to set Transit state to illegal value - "+state);
    }	
	
	/**
	 * Query the mode of the Transit
	 */
	public  int getMode() { return mMode; }
	
	/** 
	 * Set the mode of the Transit
	 */
	public void setMode(int mode) {
		if ( (mode==Transit.UNKNOWN) || (mode==Transit.AUTOMATIC) || (mode==Transit.MANUAL) ||
				(mode==Transit.DISPATCHED) )
			mMode = mode;
		else
			log.error("Attempt to set Transit mode to illegal value - "+mode);
    }
	
	/**
	 *  Add a TransitSection to the Transit
	 *  Section sequence numnbers are set automatically as Sections are added.
	 *	Returns "true" if Section was added.  Returns "false" if Section does not connect to 
	 *		the current Section.
	 */
	public void addTransitSection( TransitSection s ) {
		mTransitSectionList.add((Object)s);
		mMaxSequence = s.getSequenceNumber();
	}
	
	/** 
	 * Get a copy of this Transit's TransitSection list
	 */
	public ArrayList getTransitSectionList() {
		ArrayList list = new ArrayList();
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
			TransitSection ts = (TransitSection)mTransitSectionList.get(i);
			if (ts.getSection()==s) return true;
		}	 
		return false;
	}
	
	/**
	 * Get a List of Sections with a given sequence number
	 */
	public ArrayList getSectionListBySeq(int seq) {
		ArrayList list = new ArrayList();
		for (int i = 0; i<mTransitSectionList.size(); i++) {
			TransitSection ts = (TransitSection)mTransitSectionList.get(i);
			if (seq == ts.getSequenceNumber()) {
				list.add(ts.getSection());
			}
		}
		return list;
	}
	
	/**
	 * Test if a Block is in the Transit
	 */
	public boolean containsBlock(Block b) {
		ArrayList bList = getInternalBlocksList();
		for (int i = 0; i<bList.size(); i++) {
			if (b == (Block)bList.get(i)) return true;
		}
		return false;
	}
	
	/**
	 * Count the number of times a Block is in this Transit
	 */
	public int getBlockCount(Block b) {
		ArrayList bList = getInternalBlocksList();
		int count = 0;
		for (int i = 0; i<bList.size(); i++) {
			if (b == (Block)bList.get(i)) count++;
		}
		return count;
	}
	/**
	 * Returns a Section from one of its Blocks and its sequence number
	 */
	public Section getSectionFromBlockAndSeq(jmri.Block b, int seq) {
		for (int i = 0; i<mTransitSectionList.size(); i++) {
			TransitSection ts = (TransitSection)mTransitSectionList.get(i);
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
			TransitSection ts = (TransitSection)mTransitSectionList.get(i);
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
			TransitSection ts = (TransitSection)mTransitSectionList.get(i);
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
	public ArrayList getInternalBlocksList() {
		ArrayList list = new ArrayList();
		blockSecSeqList.clear();
		for (int i = 0; i<mTransitSectionList.size(); i++) {
			TransitSection ts = (TransitSection)mTransitSectionList.get(i);
			ArrayList bList = ts.getSection().getBlockList();
			for (int j = 0; j<bList.size(); j++) {
				list.add(bList.get(j));
				blockSecSeqList.add((Object) new Integer(ts.getSequenceNumber()));
			}
		}
		return list;
	}
	// The following is mainly for internal use, but is available if requested immediately after
	//      getInternalBlocksList or getEntryBlocksList.
	private ArrayList blockSecSeqList = new ArrayList();
	public ArrayList getBlockSeqList() { 
		ArrayList list = new ArrayList();
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
	public ArrayList getEntryBlocksList() {
		ArrayList list = new ArrayList();
		ArrayList internalBlocks = getInternalBlocksList();
		blockSecSeqList.clear();
		for (int i = 0; i<mTransitSectionList.size(); i++) {
			TransitSection ts = (TransitSection)mTransitSectionList.get(i);		
			List ePointList = null;
			if (ts.getDirection()==Section.FORWARD)
				ePointList = ts.getSection().getForwardEntryPointList();
			else
				ePointList = ts.getSection().getReverseEntryPointList();
			for (int j=0; j<ePointList.size(); j++) {
				Block b = ((EntryPoint)ePointList.get(j)).getFromBlock();
				boolean isInternal = false;
				for (int k = 0; k<internalBlocks.size(); k++) {
					if (b==(Block)internalBlocks.get(k)) isInternal = true;
				}
				if (!isInternal) {
					// not an internal Block, keep it
					list.add((Object)b);
					blockSecSeqList.add((Object) new Integer(ts.getSequenceNumber()));
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
	public ArrayList getDestinationBlocksList(Block startBlock, boolean startInTransit) {
		ArrayList list = new ArrayList();
		destBlocksSeqList.clear();
		if (startBlock==null) return list;
		// get the sequence number of the Section of the starting Block
		int startSeq = -1;
		ArrayList startBlocks = null;
		if (startInTransit) {
			startBlocks = getInternalBlocksList();
		}
		else {
			startBlocks = getEntryBlocksList();
		}
		// programming note: the above calls initialize blockSecSeqList.
		for (int k = 0; ((k<startBlocks.size()) && (startSeq==-1)); k++) {
			if (startBlock==(Block)startBlocks.get(k)) {
				startSeq = ((Integer)blockSecSeqList.get(k)).intValue();
			}
		}
		ArrayList internalBlocks = getInternalBlocksList();
		for (int i = internalBlocks.size(); i>0; i--) {
			if ( (((Integer)blockSecSeqList.get(i-1)).intValue() ) > startSeq) {
				// could stop in this block, keep it
				list.add(internalBlocks.get(i-1));
				destBlocksSeqList.add(blockSecSeqList.get(i-1));
			}
		}
		return list;
	}
	private ArrayList destBlocksSeqList = new ArrayList();
	public ArrayList getDestBlocksSeqList() { 
		ArrayList list = new ArrayList();
		for (int i = 0; i<destBlocksSeqList.size(); i++) {
			list.add(destBlocksSeqList.get(i));
		}
		return list;
	}
					
	    
    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Transit.class.getName());
	
}

/* @(#)Transit.java */
