// SerialPortAdapter.java

package jmri.jmrix;

import org.apache.log4j.Logger;

/**
 * Enables basic setup of a serial interface
 * for a jmrix implementation.
 *
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2003, 2008
 * @version	$Revision$
 * @see         jmri.jmrix.SerialConfigException
 */
public interface SerialPortAdapter extends PortAdapter {

	/** Provide a vector of valid port names, each a String. */
	public java.util.Vector<String> getPortNames();

	/** Open a specified port.  The appname argument is to be provided to the
	 * underlying OS during startup so that it can show on status displays, etc
	 */
	public String openPort(String portName, String appName);

	/** Configure all of the other jmrix widgets needed to work with this adapter
	 */
	public void configure();

	/** Query the status of this connection.  If all OK, at least
	 * as far as is known, return true */
	public boolean status();

    /**
     * Remember the associated port name
     * @param s
     */
    public void setPort(String s);
    public String getCurrentPortName();

	/**
	 * Get an array of valid baud rates; used to display valid options.
	 */
	public String[] validBaudRates();

	/**
	 * Set the baud rate.  Only to be used after construction, but
	 * before the openPort call.
	 */
	public void configureBaudRate(String rate);

    public String getCurrentBaudRate();

	/**
	 * Set the first port option.  Only to be used after construction, but
	 * before the openPort call
	 */
	public void configureOption1(String value);

	/**
	 * Set the second port option.  Only to be used after construction, but
	 * before the openPort call
	 */
	public void configureOption2(String value);
    
    /**
	 * Set the second port option.  Only to be used after construction, but
	 * before the openPort call
	 */
	public void configureOption3(String value);
    
    /**
	 * Set the second port option.  Only to be used after construction, but
	 * before the openPort call
	 */
	public void configureOption4(String value);

    /**
     * Error handling for busy port at open.
     * @see jmri.jmrix.AbstractSerialPortController
     */
    public String handlePortBusy(gnu.io.PortInUseException p,
                            String portName,
                            Logger log);
                            
    
     /**
     * Return the System Manufacturers Name
     */
    public String getManufacturer();
    
    /**
    * Set the System Manufacturers Name
    */
    public void setManufacturer(String Manufacturer);
}
