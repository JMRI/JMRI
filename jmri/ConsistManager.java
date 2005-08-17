/**
 * ConsistManager.java
 *
 * Description:         Interface for a Consist Manager Object
 *
 * @author              Paul Bender Copyright (C) 2003
 * @version             $Revision: 1.4 $      
 */


package jmri;

import com.sun.java.util.collections.ArrayList;

public interface  ConsistManager {
	
	/**
	 *    Find a Consist with this consist address, and return it.
         *    if the Consist doesn't exit, create it.
	 **/
	public Consist getConsist(DccLocoAddress address);
	
	/**
	 *    Remove an old Consist
         **/
	public void delConsist(DccLocoAddress address);

	/**
	 *    Does this implementation support Command Station Consists?
	 **/
	public boolean isCommandStationConsistPossible();

	/**
	 *    Does a CS consist require a seperate consist address?
	 **/
	public boolean csConsistNeedsSeperateAddress();

	/**
	 *    Get an ArrayList object containning the string representation 
	 *    of the consist addresses we know about.
         **/
	public ArrayList getConsistList();

	/**
	 *   Translate Error Codes recieved by a consistListener into
	 *   Strings
	 **/
	public String decodeErrorCode(int ErrorCode);

}
