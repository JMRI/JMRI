/** 
 * PowerManager.java
 *
 * Description:		Interface for controlling layout power
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;


public interface PowerManager {

	public void setPowerOn();
	public void setPowerOff();

	// to free resources when no longer used
	public void dispose() throws JmriException;

}


/* @(#)PowerManager.java */
