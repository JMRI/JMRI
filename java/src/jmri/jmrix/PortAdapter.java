// PortAdapter.java

package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Enables basic setup of a interface
 * for a jmrix implementation.
 *<P>
 * This has no e.g. serial-specific information.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2003, 2008, 2010
 * @version	$Revision$
 * @see         jmri.jmrix.SerialConfigException
 * @since 2.3.1
 */
public interface PortAdapter  {

	/** 
	 * Configure all of the other jmrix widgets needed to work with this adapter
	 */
	public void configure();

	/** 
	 * Query the status of this connection.  If all OK, at least
	 * as far as is known, return true 
	 */
	public boolean status();
    
    /**
     *  Open the connection
     */
    public void connect() throws Exception;
    
    public String getCurrentPortName();

	// returns the InputStream from the port
	public DataInputStream getInputStream();
    // returns the outputStream to the port
    public DataOutputStream getOutputStream();

    /**         
     * Get an array of valid values for "option 1"; 
     * used to display valid options.
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
    * Get an array of valid values for "option 2"; 
    * used to display valid options.         
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


    /**         
    * Get an array of valid values for "option 3"; 
    * used to display valid options.         
    * May not be null, but may have zero entries
    */
    public String[] validOption3();

    /**
     * Get a String that says what Option 3 represents
     * May be an empty string, but will not be null
    */
    public String option3Name();

    /**
    * Set the third port option.  Only to be used after construction, but
    * before the openPort call
    */
    public void configureOption3(String value);

    /**
    * Get current option 3 value
    */
    public String getCurrentOption3Setting();


    /**         
    * Get an array of valid values for "option 4"; 
    * used to display valid options.         
    * May not be null, but may have zero entries
    */
    public String[] validOption4();

    /**
     * Get a String that says what Option 4 represents
     * May be an empty string, but will not be null
    */
    public String option4Name();

    /**
    * Set the fourth port option.  Only to be used after construction, but
    * before the openPort call
    */
    public void configureOption4(String value);

    /**
    * Get current option 4 value
    */
    public String getCurrentOption4Setting();




  
     /**
     * Return the System Manufacturers Name
     */
    public String getManufacturer();
    
    /**
    * Set the System Manufacturers Name
    */
    public void setManufacturer(String Manufacturer);
    
    /**
     * Return the disabled state of the adapter
     */
    public boolean getDisabled();
    
    /**
    * Sets whether the connection is disabled
    */
    public void setDisabled(boolean disabled);
    
    public SystemConnectionMemo getSystemConnectionMemo();

    public void dispose();

    public void recover();

}
