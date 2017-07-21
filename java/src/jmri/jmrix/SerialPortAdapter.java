package jmri.jmrix;

import java.util.Vector;
import org.slf4j.Logger;
import purejavacomm.PortInUseException;

/**
 * Enables basic setup of a serial interface for a jmrix implementation.
 *
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2008
 * @see jmri.jmrix.SerialConfigException
 */
public interface SerialPortAdapter extends PortAdapter {

    /**
     * Provide a vector of valid port names, each a String.
     */
    public Vector<String> getPortNames();

    /**
     * Open a specified port. The appname argument is to be provided to the
     * underlying OS during startup so that it can show on status displays, etc
     */
    public String openPort(String portName, String appName);

    /**
     * Configure all of the other jmrix widgets needed to work with this adapter
     */
    @Override
    public void configure();

    /**
     * Query the status of this connection. If all OK, at least as far as is
     * known, return true
     */
    @Override
    public boolean status();

    /**
     * Remember the associated port name
     *
     */
    public void setPort(String s);

    @Override
    public String getCurrentPortName();

    /**
     * Get an array of valid baud rates; used to display valid options.
     */
    public String[] validBaudRates();

    /**
     * Set the baud rate. Only to be used after construction, but before the
     * openPort call.
     */
    public void configureBaudRate(String rate);

    public String getCurrentBaudRate();

    /**
     * Set the first port option. Only to be used after construction, but before
     * the openPort call
     */
    @Override
    public void configureOption1(String value);

    /**
     * Set the second port option. Only to be used after construction, but
     * before the openPort call
     */
    @Override
    public void configureOption2(String value);

    /**
     * Set the third port option. Only to be used after construction, but before
     * the openPort call
     */
    @Override
    public void configureOption3(String value);

    /**
     * Set the fourth port option. Only to be used after construction, but
     * before the openPort call
     */
    @Override
    public void configureOption4(String value);

    /**
     * Error handling for busy port at open.
     *
     * @see jmri.jmrix.AbstractSerialPortController
     */
    public String handlePortBusy(PortInUseException p, String portName, Logger log);

    /**
     * Return the System Manufacturers Name
     */
    @Override
    public String getManufacturer();

    /**
     * Set the System Manufacturers Name
     */
    @Override
    public void setManufacturer(String Manufacturer);
}
