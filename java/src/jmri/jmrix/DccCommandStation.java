/* 
 * DccCommandStation.java
 */

package jmri.jmrix;


/**
 * Defines standard operations for Dcc command stations.  
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision$
 */
public interface DccCommandStation {

	/**
	 * Does this command station have a "service mode", where it
	 * stops normal train operation while programming?
	 */ 
	public boolean getHasServiceMode();

	/**
	 * If this command station has a service mode, is the command
	 * station currently in that mode?
	 */ 
	public boolean getInServiceMode();
	
	/** 
	 * Provides an-implementation specific version string
	 * from the command station.  In general, this should
	 * be as close as possible to what the command station
	 * replied when asked; it should not be reformatted
	 **/
	 public String getVersionString();
}


/* @(#)DccCommandStation.java */
