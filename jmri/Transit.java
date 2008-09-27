// Transit.java

package jmri;

import jmri.Section;
import jmri.TransitSection;
import jmri.Block;
import jmri.Timebase;
import java.util.ArrayList;

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
 * @version			$Revision: 1.1 $
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
	 * Remove all TransitSections
	 */
	public void removeAllSections() {
		mTransitSectionList.clear();
	}		
	    
    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Transit.class.getName());
	
}

/* @(#)Transit.java */
