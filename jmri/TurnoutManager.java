/** 
 * TurnoutManager.java
 *
 * Description:		Interface for controlling turnouts
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;


public interface TurnoutManager {

	// locate an instance
	public Turnout getByAddress(TurnoutAddress key);
	public Turnout getBySystemName(String systemName);
	public Turnout getByUserName(String userName);
	
	public Turnout newTurnout(String systemName, String userName);
	
	// to free resources when no longer used
	public void dispose() throws JmriException;

}


/* @(#)TurnoutManager.java */
