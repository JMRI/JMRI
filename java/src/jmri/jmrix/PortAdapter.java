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
    
    public String getOption1Name();
    
    public String getOption2Name();
    
    public String getOption3Name();

    public String getOption4Name();

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
    * Set the third port option.  Only to be used after construction, but
    * before the openPort call
    */
    public void configureOption3(String value);
    
    /**
    * Set the fourth port option.  Only to be used after construction, but
    * before the openPort call
    */
    public void configureOption4(String value);
    
    public String[] getOptions();
    
    public boolean isOptionAdvanced(String option);
    
    public String getOptionDisplayName(String option);
    
    public void setOptionState(String option, String value);
    
    public String getOptionState(String option);
    
    public String[] getOptionChoices(String option);

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
