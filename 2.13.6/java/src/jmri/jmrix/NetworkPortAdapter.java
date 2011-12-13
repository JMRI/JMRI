// NetworkPortAdapter.java

package jmri.jmrix;

/**
 * Enables basic setup of a network interface
 * for a jmrix implementation.
 * Based upon work by Bob Jacobsen from SerialPortAdapter
 *
 * @author  Kevin Dickerson Copyright (C) 2010
 * @author	Bob Jacobsen   Copyright (C) 2010
 * @version	$Revision$
 * @see         jmri.jmrix.NetworkConfigException
 */
public interface NetworkPortAdapter extends PortAdapter {

    /** Connects to the end device using a hostname/ip address and port
     */
    public void connect(String host, int port) throws Exception;
    
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
    public void setPort(int s);
    public int getPort();
    public String getCurrentPortName();
    
    public void setHostName(String hostname);
    
    public String getHostName();

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
	public void configureOption1(String value);

    public String getCurrentOption1Setting();

	/**
	 * Get an array of valid values for "option 2"; used to display valid options.
	 * May not be null, but may have zero entries
	 */
	public String[] validOption2();

	/**
	 * Get a String that says what Option 2 represents
	 * May be an empty string, but will not be null
	 */
	public String option2Name();

	/**
	 * Set the second port option.  Only to be used after construction, but
	 * before the openPort call
	 */
	public void configureOption2(String value);

    /**
     * Get current option 2 value
     */
    public String getCurrentOption2Setting();                           
    
}
