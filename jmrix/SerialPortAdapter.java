// SerialPortAdapter.java

package jmri.jmrix;

/** 
 * Enables basic setup of a serial interface
 * for a jmrix implementation.
 *
 *<P>
 * To configure for operation, a 
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: SerialPortAdapter.java,v 1.2 2001-12-06 16:16:27 jacobsen Exp $
 * @see             jmri.jmrix.SerialConfigException
 */
public interface SerialPortAdapter  {
	
	/** Provide a vector of valid port names, each a String. */
	abstract public java.util.Vector getPortNames(); 
	
	/** Open a specified port.  The appname argument is to be provided to the
	 * underlying OS during startup so that it can show on status displays, etc 
	 */
	abstract public void openPort(String portName, String appName) throws jmri.jmrix.SerialConfigException;
	
	/** Configure all of the other jmrix widgets needed to work with this adapter
	 */
	 abstract public void configure();
	 
	/** Query the status of this connection.  If all OK, at least
	 * as far as is known, return true */
	public boolean status();
	
}
