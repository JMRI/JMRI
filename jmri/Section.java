// Section.java

package jmri;

import jmri.Transit;
import jmri.Block;
import jmri.Sensor;
import jmri.Timebase;
import java.util.ArrayList;

/**
 * Sections represent a group of one or more connected Blocks that may be 
 *	allocated to one or more trains travelling in a given direction.
 * <P>
 * A Block may be in multiple Sections. All Blocks contained in a given 
 *	section must be unique. Blocks are kept in order--the first block is 
 *	connected to the second, the second is connected to the third, etc.
 * <P>
 * A Block in a Section must be connected to the Block before it (if there is
 *	one) and to the Block after it (if there is one), but may not be connected 
 *	to any other Block in the Section. This restriction is enforced when a 
 *  Section is created, and checked when a Section is loaded from disk.
 * <P>
 * A Section has a "direction" defined by the sequence in which Blocks are 
 *	added to the Section. A train may run through a Section in either the 
 *	forward direction (from first block to last block) or reverse direction
 *	(from last block to first block).
 * <P>
 * A Section has two or more EntryPoints. Each EntryPoint is a Path of one 
 *	of the Blocks in the Section that defines a connection to a Block outside
 *	of the Section. EntryPoints are grouped into two lists: 
 *		"forwardEntryPoints" - entry through which will result in a train 
 *				travelling in the "forward" direction
 *		"reverseEntryPoints" - entry through which will result in a train 
 *				travelling in the "reverse" direction
 *  Note that "forwardEntryPoints" are also reverse exit points, and vice versa.
 * <P>
 * A Section has one of the following states"
 *		FREE - available for allocation by a dispatcher
 *		FORWARD - allocated for travel in the forward direction
 *		REVERSE - allocated for travel in the reverse direction
 * <P>
 * A Section of sufficient length may be allocated to more than one train provided 
 *	the trains are travelling in the same direction. There must be at least one block
 *  between trains travelling in the same direction in a section.
 * <P>
 * A Section may not contain any reverse loops. The track that is reversed in a 
 *	reverse loop must be in a separate Section.
 * <P>
 * Each Section optionally carries two direction sensors, one for the forward direction 
 *	and one for the reverse direction. These sensors force signals for travel in their
 *	respective directions to "RED" when they are active. When the Section is free, 
 *	both the sensors are Active. These internal sensors follow the state of the 
 *	Section, permitting signals to function normally in the direction of allocation.
 * <P>
 * Each Section optionally carries two stopping sensors, one for the forward direction 
 *  and one for the reverse direction.  These sensors change to active when a train 
 *  traversing the Section triggers its sensing device. Stopping sensors are 
 *  physical layout sensors, and may be either point sensors or occupancy sensors for
 *  short blocks at the end of the Section. A stopping sensor is used during automatic
 *  running to stop a train that has reached the end of its allocated Section. This is 
 *  needed, for example, to allow a train to enter a passing siding and clear the 
 *  track behind it. When not running automatically, these sensors may be used to light 
 *  panel lights to notify the dispatcher that the train has reached the end of the 
 *  Section.
 * <P>
 * This Section implementation provides for delayed initialization of blocks and 
 *	direction sensors to be independent of order of items in panel files. 
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
 * @version			$Revision: 1.1 $
 */
public class Section extends AbstractNamedBean
    implements  java.io.Serializable {

    public Section(String systemName, String userName) {
        super(systemName, userName);
    }

    public Section(String systemName) {
        super(systemName);
    }
 	
	/**
	 * Constants representing the state of the Section.
	 * A Section can be either FREE - available for allocation, FORWARD - allocated 
	 *	 for travel in the forward direction, or REVERSE - allocated for travel in
	 *   the REVERSE direction.
	 */ 
	public static final int FREE = 0x02;
	public static final int FORWARD = 0x04;
	public static final int REVERSE = 0X08;

    /**
     *  Persistant instance variables (saved between runs)
     */
	private String mForwardBlockingSensorName = "";
	private String mReverseBlockingSensorName = "";
	private String mForwardStoppingSensorName = "";
	private String mReverseStoppingSensorName = "";
	private ArrayList mBlockEntries = new ArrayList();
	private ArrayList mForwardEntryPoints = new ArrayList();
	private ArrayList mReverseEntryPoints = new ArrayList();
	
    /**
     *  Operational instance variables (not saved between runs)
     */
	private int mState = Section.FREE;
	private Block mFirstBlock = null;
	private Block mLastBlock = null;
	private Sensor mForwardBlockingSensor = null;
	private Sensor mReverseBlockingSensor = null;
	private Sensor mForwardStoppingSensor = null;
	private Sensor mReverseStoppingSensor = null;
	
	/**
	 * Query the state of the Section
	 */
	public  int getState() { return mState; }
	
	/** 
	 * Set the state of the Section
	 */
	public void setState(int state) {
		if ( (state==Section.FREE) || (state==FORWARD) || (state==Section.REVERSE) ) {
			mState = state;
			// update the forward/reverse blocking sensors as needed
			if (state==FORWARD) {
				try {
					if ( (getForwardBlockingSensor()!=null) && (mForwardBlockingSensor.getState()!=Sensor.INACTIVE) ) 
						mForwardBlockingSensor.setState(Sensor.INACTIVE);
					if ( (getReverseBlockingSensor()!=null) && (mReverseBlockingSensor.getState()!=Sensor.ACTIVE) ) 
						mReverseBlockingSensor.setKnownState(Sensor.ACTIVE);
				} catch (jmri.JmriException reason) {
					log.error ("Exception when setting Sensors for Section "+getSystemName());
				}
			}
			else if (state==REVERSE) {
				try {
					if ( (getReverseBlockingSensor()!=null) && (mReverseBlockingSensor.getState()!=Sensor.INACTIVE) ) 
						mReverseBlockingSensor.setKnownState(Sensor.INACTIVE);
					if ( (getForwardBlockingSensor()!=null) && (mForwardBlockingSensor.getState()!=Sensor.ACTIVE) ) 
						mForwardBlockingSensor.setKnownState(Sensor.ACTIVE);
				} catch (jmri.JmriException reason) {
					log.error ("Exception when setting Sensors for Section "+getSystemName());
				}
			}
			else if (state==FREE) {
				try {
					if ( (getForwardBlockingSensor()!=null) && (mForwardBlockingSensor.getState()!=Sensor.ACTIVE) ) 
						mForwardBlockingSensor.setKnownState(Sensor.ACTIVE);
					if ( (getReverseBlockingSensor()!=null) && (mReverseBlockingSensor.getState()!=Sensor.ACTIVE) ) 
						mReverseBlockingSensor.setKnownState(Sensor.ACTIVE);
				} catch (jmri.JmriException reason) {
					log.error ("Exception when setting Sensors for Section "+getSystemName());
				}
			}
		}
		else 
			log.error("Attempt to set state of Section "+getSystemName()+" to illegal value - "+state);
	}
	
	/**
	 * Access methods for forward and reverse blocking sensors
	 *	The set methods return a Sensor object if successful, or else they
	 *		return "null";
	 */
	public String getForwardBlockingSensorName() { return mForwardBlockingSensorName; }
	public Sensor getForwardBlockingSensor() { 
		if ( (mForwardBlockingSensor==null) && (mForwardBlockingSensorName!=null) && 
							(!mForwardBlockingSensorName.equals("")) ) {
			mForwardBlockingSensor = InstanceManager.sensorManagerInstance().
												getSensor(mForwardBlockingSensorName);
			if (mForwardBlockingSensor==null) {
				log.error("Missing Sensor - "+mForwardBlockingSensorName+" - when initializing Section - "+
									getSystemName());
			}
		}
		return mForwardBlockingSensor; 
	}
	public Sensor setForwardBlockingSensorName(String forwardSensor) {
		if ( (forwardSensor==null) || (forwardSensor.length()<=0) ) {
			mForwardBlockingSensor = null;
			mForwardBlockingSensorName = "";
			return null;
		}
		tempSensorName = forwardSensor;
		Sensor s = validateSensor();
		if (s==null) {
			// sensor name not correct or not in sensor table
			log.error("Sensor name -"+forwardSensor+"invalid when setting forward sensor in Section "+getSystemName());
			return null;
		}
		mForwardBlockingSensorName = tempSensorName;
		mForwardBlockingSensor = s;
		return s;
	}
	public void delayedSetForwardBlockingSensorName(String forwardSensor) {
		mForwardBlockingSensorName = forwardSensor;
	}
	public String getReverseBlockingSensorName() { return mReverseBlockingSensorName; }
	public Sensor setReverseBlockingSensorName(String reverseSensor) {
		if ( (reverseSensor==null) || (reverseSensor.length()<=0) ) {
			mReverseBlockingSensor = null;
			mReverseBlockingSensorName = "";
			return null;
		}
		tempSensorName = reverseSensor;
		Sensor s = validateSensor();
		if (s==null) {
			// sensor name not correct or not in sensor table
			log.error("Sensor name -"+reverseSensor+"invalid when setting reverse sensor in Section "+getSystemName());
			return null;
		}
		mReverseBlockingSensorName = tempSensorName;
		mReverseBlockingSensor = s;
		return s;
	}
	public void delayedSetReverseBlockingSensorName(String reverseSensor) {
		mReverseBlockingSensorName = reverseSensor;
	}
	public Sensor getReverseBlockingSensor() { 
		if ( (mReverseBlockingSensor==null) && (mReverseBlockingSensorName!=null) && 
							(!mReverseBlockingSensorName.equals("")) ) {
			mReverseBlockingSensor = InstanceManager.sensorManagerInstance().
												getSensor(mReverseBlockingSensorName);
			if (mReverseBlockingSensor==null) {
				log.error("Missing Sensor - "+mReverseBlockingSensorName+" - when initializing Section - "+
									getSystemName());
			}
		}	
		return mReverseBlockingSensor; 
	}

	String tempSensorName = "";
	private Sensor validateSensor() {
		// check if anything entered	
		if (tempSensorName.length()<1) {
			// no sensor specified
			return null;
		}
		// get the sensor corresponding to this name
		Sensor s = InstanceManager.sensorManagerInstance().getSensor(tempSensorName);
		if (s==null) return null;
		if ( !tempSensorName.equals(s.getUserName()) ) {
			tempSensorName = tempSensorName.toUpperCase();
		}
		return s;
	}
	
	/**
	 * Access methods for forward and reverse stopping sensors
	 *	The set methods return a Sensor object if successful, or else they
	 *		return "null";
	 */
	public String getForwardStoppingSensorName() { return mForwardStoppingSensorName; }
	public Sensor getForwardStoppingSensor() { 
		if ( (mForwardStoppingSensor==null) && (mForwardStoppingSensorName!=null) && 
							(!mForwardStoppingSensorName.equals("")) ) {
			mForwardStoppingSensor = InstanceManager.sensorManagerInstance().
												getSensor(mForwardStoppingSensorName);
			if (mForwardStoppingSensor==null) {
				log.error("Missing Sensor - "+mForwardStoppingSensorName+" - when initializing Section - "+
									getSystemName());
			}
		}
		return mForwardStoppingSensor; 
	}
	public Sensor setForwardStoppingSensorName(String forwardSensor) {
		if ( (forwardSensor==null) || (forwardSensor.length()<=0) ) {
			mForwardStoppingSensor = null;
			mForwardStoppingSensorName = "";
			return null;
		}
		tempSensorName = forwardSensor;
		Sensor s = validateSensor();
		if (s==null) {
			// sensor name not correct or not in sensor table
			log.error("Sensor name -"+forwardSensor+"invalid when setting forward sensor in Section "+getSystemName());
			return null;
		}
		mForwardStoppingSensorName = tempSensorName;
		mForwardStoppingSensor = s;
		return s;
	}
	public void delayedSetForwardStoppingSensorName(String forwardSensor) {
		mForwardStoppingSensorName = forwardSensor;
	}
	public String getReverseStoppingSensorName() { return mReverseStoppingSensorName; }
	public Sensor setReverseStoppingSensorName(String reverseSensor) {
		if ( (reverseSensor==null) || (reverseSensor.length()<=0) ) {
			mReverseStoppingSensor = null;
			mReverseStoppingSensorName = "";
			return null;
		}
		tempSensorName = reverseSensor;
		Sensor s = validateSensor();
		if (s==null) {
			// sensor name not correct or not in sensor table
			log.error("Sensor name -"+reverseSensor+"invalid when setting reverse sensor in Section "+getSystemName());
			return null;
		}
		mReverseStoppingSensorName = tempSensorName;
		mReverseStoppingSensor = s;
		return s;
	}
	public void delayedSetReverseStoppingSensorName(String reverseSensor) {
		mReverseStoppingSensorName = reverseSensor;
	}
	public Sensor getReverseStoppingSensor() { 
		if ( (mReverseStoppingSensor==null) && (mReverseStoppingSensorName!=null) && 
							(!mReverseStoppingSensorName.equals("")) ) {
			mReverseStoppingSensor = InstanceManager.sensorManagerInstance().
												getSensor(mReverseStoppingSensorName);
			if (mReverseStoppingSensor==null) {
				log.error("Missing Sensor - "+mReverseStoppingSensorName+" - when initializing Section - "+
									getSystemName());
			}
		}	
		return mReverseStoppingSensor; 
	}

	/**
	 *  Add a Block to the Section
	 *  Block and sequence number must be unique within the Section.
	 *  Block sequence numnbers are set automatically as blocks are added.
	 *	Returns "true" if Block was added.  Returns "false" if Block does not connect to 
	 *		the current Block, or the Block is not unique.
	 */
	public boolean addBlock( Block b ) {
		// validate that this entry is unique, if not first.
		if (mBlockEntries.size()==0) {
			mFirstBlock = b;			
		}
		else {
			// check that block is unique 
			for (int i=0;i<mBlockEntries.size();i++) {
				if (mBlockEntries.get(i) == b) {
					// block is already present
					return false;
				}
			}
			// Note: connectivity to current block is assumed to have been checked		
		}
		// add Block to the Block list
		mBlockEntries.add((Object)b);
		mLastBlock = b;
		return true;
	}
	private boolean initializationNeeded = false;
	private ArrayList blockNameList = new ArrayList();
	public void delayedAddBlock(String blockName) {
		initializationNeeded = true;
		blockNameList.add(blockName);
	}
	private void initializeBlocks() {
		for (int i = 0; i<blockNameList.size(); i++) {
			Block b = InstanceManager.blockManagerInstance().getBlock((String)blockNameList.get(i));
			if (b==null) {
				log.error("Missing Block - "+(String)blockNameList.get(i)+" - when initializing Section - "+
									getSystemName());
			}
			else {
				if (mBlockEntries.size()==0) {
					mFirstBlock = b;			
				}
				mBlockEntries.add((Object)b);
				mLastBlock = b;
			}
		}
		initializationNeeded = false;
	}
			
	/**
	 * Get Block by its Sequence number in the Block list
	 *  Blocks are numbered 0 to size-1; 
	 */
	public Block getBlockBySequenceNumber (int seqNumber) {
		if (initializationNeeded) initializeBlocks();
		if ( (seqNumber<mBlockEntries.size()) && (seqNumber>=0) ) 
			return (Block)mBlockEntries.get(seqNumber);
		return null;
	}
	/**
	 * Remove all Blocks and Entry Points
	 */
	public void removeAllBlocksFromSection () {
		for (int i = mBlockEntries.size();i>0;i--) {
			mBlockEntries.remove(i-1);
		}
		for (int i = mForwardEntryPoints.size();i>0;i--) {
			mForwardEntryPoints.remove(i-1);
		}
		for (int i = mReverseEntryPoints.size();i>0;i--) {
			mReverseEntryPoints.remove(i-1);
		}
		initializationNeeded = false;
	}
	/**
	 * Gets Blocks in order
	 *	If state is FREE or FORWARD, returns Blocks in forward order
	 *  If state is REVERSE, returns Blocks in reverse order
	 *	First call getEntryBlock, then call getNextBlock until null is returned.
	 */
	private int blockIndex = 0;  // index of last block returned
	public Block getEntryBlock() {
		if (initializationNeeded) initializeBlocks();
		if (mBlockEntries.size() <=0) return null;
		if (mState==REVERSE) blockIndex=mBlockEntries.size();
		else blockIndex = 1;
		return (Block)mBlockEntries.get(blockIndex-1);
	}
	public Block getNextBlock() {
		if (initializationNeeded) initializeBlocks();
		if (mState==REVERSE) blockIndex --;
		else blockIndex ++;
		if ( (blockIndex>mBlockEntries.size()) || (blockIndex<=0) ) return null;
		return (Block)mBlockEntries.get(blockIndex-1);
	}
	
	/** 
	 * Access methods for beginning and ending block names
	 */
	public String getBeginBlockName() {
		if (initializationNeeded) initializeBlocks();
		String s = mFirstBlock.getSystemName();
		String uName = mFirstBlock.getUserName();
		if ( (uName!=null) && (!uName.equals("")) )
			return (s+"( "+uName+" )");
		return s;
	}
	public String getEndBlockName() {
		if (initializationNeeded) initializeBlocks();
		String s = mLastBlock.getSystemName();
		String uName = mLastBlock.getUserName();
		if ( (uName!=null) && (!uName.equals("")) )
			return (s+"( "+uName+" )");
		return s;
	}
	
	/** 
	 * Access methods for EntryPoints within the Section
	 */
	public void addToForwardList(EntryPoint ep) {
		if (ep!=null) mForwardEntryPoints.add((Object)ep);
	}
	public void addToReverseList(EntryPoint ep) {
		if (ep!=null) mReverseEntryPoints.add((Object)ep);
	}
	public void removeEntryPoint(EntryPoint ep) {
		for (int i = mForwardEntryPoints.size();i>0;i--) {
			if (mForwardEntryPoints.get(i-1)==(Object)ep)
				mForwardEntryPoints.remove(i-1);
		}
		for (int i = mReverseEntryPoints.size();i>0;i--) {
			if (mReverseEntryPoints.get(i-1)==(Object)ep)
				mReverseEntryPoints.remove(i-1);
		}
	}
	public java.util.List getForwardEntryPointList() {
		ArrayList list = new ArrayList();
		for (int i = 0; i<mForwardEntryPoints.size(); i++) {
			list.add(mForwardEntryPoints.get(i));
		}
		return list;
	}
	public java.util.List getReverseEntryPointList() {
		ArrayList list = new ArrayList();
		for (int i = 0; i<mReverseEntryPoints.size(); i++) {
			list.add(mReverseEntryPoints.get(i));
		}
		return list;
	}
	public java.util.List getEntryPointList() {
		ArrayList list = new ArrayList();
		for (int i = 0; i<mForwardEntryPoints.size(); i++) {
			list.add(mForwardEntryPoints.get(i));
		}
		for (int j = 0; j<mReverseEntryPoints.size(); j++) {
			list.add(mReverseEntryPoints.get(j));
		}
		return list;
	}	
	
	/**
	 * Validate and initialize the Section. 
	 * This checks block connectivity, removes redundant EntryPoints,
	 *  and otherwise checks internal consistency of the Section.
	 */
	public boolean validate(jmri.util.JmriJFrame frame) {
// add code
		return true;
	}
		    
    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Section.class.getName());
	
}

/* @(#)Section.java */
