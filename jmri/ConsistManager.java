/**
 * ConsistManager.java
 *
 * Description:         Interface for a Consist Manager Object
 *
 * @author              Paul Bender Copyright (C) 2003
 * @version             $ version 1.00 $      
 */


package jmri;

import com.sun.java.util.collections.ArrayList;

public interface  ConsistManager {
	
	/**
	 *    Find a Consist with this consist address, and return it.
         *    if the Consist doesn't exit, create it.
	 **/
	public Consist getConsist(int address);
	
	/**
	 *    Remove an old Consist
         **/
	public void delConsist(int address);

	/**
	 *    Does this implementation support Command Station Consists?
	 **/
	public boolean isCommandStationConsistPossible();

	/**
	 *    Get an ArrayList object containning the string representation 
	 *    of the consist addresses we know about.
         **/
	public ArrayList getConsistList();

}
