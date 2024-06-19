package jmri.jmrix;

import java.util.Vector;

/**
 * Enable basic setup of a serial interface for a jmrix implementation.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2008, 2023
 * @see jmri.jmrix.SerialConfigException
 */
public interface SerialPortAdapter extends PortAdapter {

    /**
     * Provide a vector of valid port names, each a String.
     * This may be implemented differently in subclasses 
     * that e.g. do loopkac or use a custom port-access library.
     * @return Valid port names in the form used to select them later.
     */
    default Vector<String> getPortNames() {
        return AbstractSerialPortController.getActualPortNames();
    }

    /**
     * Open a specified port.
     *
     * @param portName name tu use for this port
     * @param appName provided to the underlying OS during startup so
     *                that it can show on status displays, etc.
     * @return null indicates OK return, else error message.
     */
    String openPort(String portName, String appName);

    /**
     * Configure all of the other jmrix widgets needed to work with this adapter.
     */
    @Override
    void configure();

    /**
     * {@inheritDoc}
     */
    @Override
    boolean status();

    /**
     * Remember the associated port name.
     *
     * @param s name of the port
     */
    void setPort(String s);

    @Override
    String getCurrentPortName();

    /**
     * Get an array of valid baud rate strings; used to display valid options in Connections Preferences.
     *
     * @return array of I18N display strings of port speed settings valid for this serial adapter,
     * must match order and values from {@link #validBaudNumbers()}
     */
    String[] validBaudRates();

    /**
     * Get an array of valid baud rate numbers; used to store/load adapter speed option.
     *
     * @return integer array of speeds, must match order and values from {@link #validBaudRates()}
     */
    int[] validBaudNumbers();

    /**
     * Get the index of the default port speed for this adapter from the validSpeeds and validRates arrays.
     *
     * @return -1 to indicate not supported, unless overridden in adapter
     */
    int defaultBaudIndex();

    /**
     * Set the baud rate description by port speed description.
     * <p>
     * Only to be used after construction, but before the openPort call.
     *
     * @param rate the baud rate as I18N description, eg. "28,800 baud"
     */
    void configureBaudRate(String rate);

    /**
     * Set the baud rate description by port speed number (as a string) from validBaudRates[].
     * <p>
     * Only to be used after construction, but before the openPort call.
     *
     * @param index the port speed as unformatted number string, eg. "28800"
     */
    void configureBaudRateFromNumber(String index);

    /**
     * Set the baud rate description by index (integer) from validBaudRates[].
     *
     * @param index the index to select from speeds[] array
     */
    void configureBaudRateFromIndex(int index);

    String getCurrentBaudRate();

    /**
     * To store as XML attribute, get a string to represent current port speed.
     *
     * @return speed as number string
     */
    String getCurrentBaudNumber();

    int getCurrentBaudIndex();

    /**
     * Set the first port option. Only to be used after construction, but before
     * the openPort call.
     */
    @Override
    void configureOption1(String value);

    /**
     * Set the second port option. Only to be used after construction, but
     * before the openPort call.
     */
    @Override
    void configureOption2(String value);

    /**
     * Set the third port option. Only to be used after construction, but before
     * the openPort call.
     */
    @Override
    void configureOption3(String value);

    /**
     * Set the fourth port option. Only to be used after construction, but
     * before the openPort call.
     */
    @Override
    void configureOption4(String value);

    /**
     * Get the System Manufacturers Name.
     */
    @Override
    String getManufacturer();

    /**
     * Set the System Manufacturers Name.
     */
    @Override
    void setManufacturer(String Manufacturer);

}
