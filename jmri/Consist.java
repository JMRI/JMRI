/**
 * Consist.java
 *
 * Description:         Interface for a Consist Object
 *
 * @author              Paul Bender Copyright (C) 2003
 * @version             $Revision: 1.7 $
 */

package jmri;

import com.sun.java.util.collections.ArrayList;

/*
 * This is the interface for consist classes.
 */
public interface Consist {

	// Constants for the ConsistType
	// For Advanced Consists
        public final static int ADVANCED_CONSIST = 0;

	// For Command Station Consists
        // This is for a: Digitrax Universal Consist, 
        // or Lenz Double Header,or NCE "old Style Consist",etc
        public final static int CS_CONSIST = 1;  

	// A method for cleaning up the consist
	public void dispose();

	// Set the Consist Type
	public void setConsistType(int consist_type);

	// get the Consist Type
	public int getConsistType();

	// get the Consist Address
	public DccLocoAddress getConsistAddress();

	// is the specific address allowed? (needed for system specific 
 	// restrictions)
	public boolean isAddressAllowed(DccLocoAddress address);

	/**
	 * is there a size limit for this type of consist?
	 * returns -1 if no limit
	 * returns 0 if the Consist Type is not supported
	 * returns the total number of useable spaces if the consist has a 
         * limit (do not subtract used spaces).
	 */
	public int sizeLimit();

	// get a list of the locomotives in the consist
	public ArrayList getConsistList();
	
	// does the consist contain the specified locomotive address?
	public boolean contains(DccLocoAddress address);

	// get the relative direction setting for a specific 
	// locomotive in the consist
	public boolean getLocoDirection(DccLocoAddress address);
	
        /*
	 * Add a Locomotive to an Advanced Consist
	 *  @param address is the Locomotive address to add to the locomotive
	 *  @param directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	public void add(DccLocoAddress LocoAddress, boolean directionNormal);

        /*
	 *  Remove a Locomotive from this Consist
	 *  @param address is the Locomotive address to add to the locomotive
         */
	public void remove(DccLocoAddress LocoAddress);

	/* 
	 * Add a Listener for consist events
	 * @param Listener is a consistListener object
         */
	public void addConsistListener(jmri.ConsistListener Listener);

	/* 
	 * Remove a Listener for consist events
	 * @param Listener is a consistListener object
         */
	public void removeConsistListener(jmri.ConsistListener Listener);

}
