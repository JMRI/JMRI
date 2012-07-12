// AbstractPortController.java

package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Provide an abstract base for *PortController classes.
 * <P>
 * This is complicated by the lack of multiple inheritance.
 * SerialPortAdapter is an Interface, and its implementing
 * classes also inherit from various PortController types.  But we
 * want some common behaviors for those, so we put them here.
 *
 * @see jmri.jmrix.SerialPortAdapter
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision$
 */
abstract public class AbstractPortController implements PortAdapter {

    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    // check that this object is ready to operate
    public boolean status() {return opened;}
    
    protected boolean opened = false;
    
    protected void setOpened() {opened = true; }
    protected void setClosed() {opened = false; }

    abstract public String getCurrentPortName();
    
    /**
     * Get an array of valid values for "option 1"; used to display valid options.
     * May not be null, but may have zero entries
     */
    public String[] validOption1() { return new String[]{""}; }

    /**
     * Get a String that says what Option 1 represents
     * May be an empty string, but will not be null
     */
    public String option1Name() { return ""; }

    /**
     * Set the second port option.
     */
    public void configureOption1(String value) { mOpt1 = value; }
    protected String mOpt1 = null;
    public String getCurrentOption1Setting() {
        if (mOpt1 == null) return validOption1()[0];
        return mOpt1;
    }

    /**
     * Get an array of valid values for "option 2"; used to display valid options.
     * May not be null, but may have zero entries
     */
    public String[] validOption2() { return new String[]{""}; }

    /**
     * Get a String that says what Option 2 represents
     * May be an empty string, but will not be null
     */
    public String option2Name() { return ""; }

    /**
     * Set the second port option.
     */
    public void configureOption2(String value) { mOpt2 = value; }
    protected String mOpt2  = null;
    public String getCurrentOption2Setting() {
        if (mOpt2 == null) return validOption2()[0];
        return mOpt2;
    }
    
    /**
     * Get an array of valid values for "option 3"; used to display valid options.
     * May not be null, but may have zero entries
     */
    public String[] validOption3() { return new String[]{""}; }

    /**
     * Get a String that says what Option 3 represents
     * May be an empty string, but will not be null
     */
    public String option3Name() { return ""; }

    /**
     * Set the third port option.
     */
    public void configureOption3(String value) { mOpt3 = value; }
    protected String mOpt3  = null;
    public String getCurrentOption3Setting() {
        if (mOpt3 == null) return validOption3()[0];
        return mOpt3;
    }
    
    /**
     * Get an array of valid values for "option 4"; used to display valid options.
     * May not be null, but may have zero entries
     */
    public String[] validOption4() { return new String[]{""}; }

    /**
     * Get a String that says what Option 4 represents
     * May be an empty string, but will not be null
     */
    public String option4Name() { return ""; }

    /**
     * Set the fourth port option.
     */
    public void configureOption4(String value) { mOpt4 = value; }
    protected String mOpt4  = null;
    public String getCurrentOption4Setting() {
        if (mOpt4 == null) return validOption4()[0];
        return mOpt4;
    }
    
    /**
    * Get and set of the Manufacturer for network (TCP/IP) based
    * connections is handled by the ConnectionConfig code in each
    * connector.  this is here as we implement the serialdriveradpter.
    */
    public String getManufacturer() { return mManufacturer; }
    public void setManufacturer(String manufacturer) { 
        log.debug("update manufacturer from "+mManufacturer+" to "+manufacturer);
        mManufacturer = manufacturer; 
    }
    protected String mManufacturer = null;
    
    public boolean getDisabled() { return mDisabled; }
   
    /* The set disabled is handled within the local port controller for each system
    this is because it needs to also needs to set a disabled flag in the system connection memo*/
    
    abstract public void setDisabled(boolean disabled);
    protected boolean mDisabled = false;
    
    abstract public SystemConnectionMemo getSystemConnectionMemo();

    protected boolean allowConnectionRecovery = false;

    abstract public void recover();

    protected int reconnectinterval = 1000;
    protected int retryAttempts = 10;

    protected static void safeSleep(long milliseconds, String s) {
          try {
             Thread.sleep(milliseconds);
          }
          catch (InterruptedException e) {
             log.error("Sleep Exception raised during reconnection attempt" +s);
          }
    }

    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractPortController.class.getName());

}
