// SerialPortAdapter.java

package jmri.jmrix;

/** 
 * Enables basic setup of a serial interface
 * for a jmrix implementation.
 *
 *<P>
 * To configure for operation, a 
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: SerialPortAdapter.java,v 1.4 2002-02-20 15:57:03 jacobsen Exp $
 * @see             jmri.jmrix.SerialConfigException
 */
public interface SerialPortAdapter  {
	
	/** Provide a vector of valid port names, each a String. */
	abstract public java.util.Vector getPortNames(); 
	
	/** Open a specified port.  The appname argument is to be provided to the
	 * underlying OS during startup so that it can show on status displays, etc 
	 */
	abstract public String openPort(String portName, String appName) throws jmri.jmrix.SerialConfigException;
	
	/** Configure all of the other jmrix widgets needed to work with this adapter
	 */
	 abstract public void configure();
	 
	/** Query the status of this connection.  If all OK, at least
	 * as far as is known, return true */
	public boolean status();
	
	/**
	 * Get an array of valid baud rates; used to display valid options.
	 */
	public String[] validBaudRates();
	
	/**
	 * Set the baud rate.  Only to be used after construction, but 
	 * before the openPort call.
	 */
	public void configureBaudRate(String rate) throws SerialConfigException;
	
	/**
	 * Get an array of valid values for "option 1"; used to display valid options.
	 * May not be null, but may have zero entries
	 */
	public String[] validOption1();
	
	/**
	 * Get a String that says what Option 1 represents
	 * May be an empty string, but will not be null
	 */
	public String option1Name();
	
	/**
	 * Set the first port option.  Only to be used after construction, but 
	 * before the openPort call
	 */
	public void configureOption1(String value) throws SerialConfigException;
	
}
