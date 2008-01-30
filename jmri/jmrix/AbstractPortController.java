// AbstractPortController.java

package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.swing.JOptionPane;

/**
 * Provide an abstract base for *PortController classes.
 * <P>
 * This is complicated by the lack of multiple inheritance.
 * SerialPortAdapter is an Interface, and it's implementing
 * classes also inherit from various PortController types.  But we
 * want some common behaviours for those, so we put them here.
 *
 * @see jmri.jmrix.SerialPortAdapter
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.9 $
 */
abstract public class AbstractPortController implements SerialPortAdapter {

    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    // check that this object is ready to operate
    public abstract boolean status();

    /**
     * Standard error handling for port-busy case
     */
    public String handlePortBusy(javax.comm.PortInUseException p,
                            String portName,
                            org.apache.log4j.Category log) {
				log.error(portName+" port is in use: "+p.getMessage());
                JOptionPane.showMessageDialog(null, "Port is in use",
                                                "Error", JOptionPane.ERROR_MESSAGE);
    			ConnectionStatus.instance().setConnectionState(portName, ConnectionStatus.CONNECTION_DOWN);
                return portName+" port is in use";
    }

    /**
     * Standard error handling for port-not-found case
     */
    public String handlePortNotFound(javax.comm.NoSuchPortException p,
                            String portName,
                            org.apache.log4j.Category log) {
				log.error("Serial port "+portName+" not found");
                JOptionPane.showMessageDialog(null, "Serial port "+portName+" not found",
                                                "Error", JOptionPane.ERROR_MESSAGE);
    			ConnectionStatus.instance().setConnectionState(portName, ConnectionStatus.CONNECTION_DOWN);
                return portName+" not found";
    }

    public void setPort(String port) { mPort= port;}
    protected String mPort = null;
    public String getCurrentPortName() {
        if (mPort == null) {
            if (getPortNames().size()<=0) {
                log.error("No usable ports returned");
                return null;
            }
            return (String)getPortNames().elementAt(0);
        }
        return mPort;
    }

    /**
     * Set the baud rate.  This records it for later.
     */
    public void configureBaudRate(String rate) { mBaudRate = rate;}
    protected String mBaudRate = null;
    public String getCurrentBaudRate() {
        if (mBaudRate == null) return validBaudRates()[0];
        return mBaudRate;
    }

    /**
     * Get an array of valid baud rates as integers. This allows subclasses
     * to change the arrays of speeds.
     * 
     * This method need not be reimplemented unless the subclass is using
     * currentBaudNumber, which requires it.
     */
    public int[] validBaudNumber() { 
        log.error("default validBaudNumber implementation should not be used");
        new Exception().printStackTrace();
        return null;
    }

    /**
     * Convert a baud rate string to a number.
     * 
     * Uses the validBaudNumber and validBaudRates methods to do this.
     * @return -1 if no match (configuration system should prevent this)
     */
    public int currentBaudNumber(String currentBaudRate) {
        String[] rates = validBaudRates();
        int[] numbers = validBaudNumber();
        
        // return if arrays invalid
        if (numbers == null) {
            log.error("numbers array null in currentBaudNumber");
            return -1;
        }
        if (rates == null) {
            log.error("rates array null in currentBaudNumber");
            return -1;
        }
        if (numbers.length<1 || (numbers.length != rates.length) ) {
            log.error("arrays wrong length in currentBaudNumber: "+numbers.length+","+rates.length);
            return -1;
        }
        
        // find the baud rate value, configure comm options
        int baud = numbers[0];  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i<numbers.length; i++ )
            if (rates[i].equals(currentBaudRate))
                return numbers[i];
        
        // no match
        log.error("no match to ("+currentBaudRate+") in currentBaudNumber");
        return -1;
    }    
    
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

    static protected org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractPortController.class.getName());

}
