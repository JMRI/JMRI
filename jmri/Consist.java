/**
 * Consist.java
 *
 * Description:         Interface for a Consist Object
 *
 * @author              Paul Bender Copyright (C) 2003
 * @version             $ version 1.00 $
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



	// Set the Consist Type
	public void setConsistType(int consist_type);

	// get the Consist Type
	public int getConsistType();

	// get the Consist Address
	public int getConsistAddress();

	// get a list of the locomotives in the consist
	public ArrayList getConsistList();
	
	// does the consist contain the specified locomotive address?
	public boolean contains(int address);

	// get the relative direction setting for a specific 
	// locomotive in the consist
	public boolean getLocoDirection(int address);
	
        /*
	 * Add a Locomotive to an Advanced Consist
	 *  @parm address is the Locomotive address to add to the locomotive
	 *  @parm directionNormal is True if the locomotive is traveling 
         *        the same direction as the consist, or false otherwise.
         */
	public void add(int LocoAddress,boolean directionNormal);

        /*
	 *  Remove a Locomotive from this Consist
	 *  @parm address is the Locomotive address to add to the locomotive
         */
	public void remove(int LocoAddress);

}
