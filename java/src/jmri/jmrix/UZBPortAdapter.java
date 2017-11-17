package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.usb.UsbDevice;
import jmri.util.USBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables basic setup of a USB interface for a jmrix implementation.
 *
 * @author George Warner Copyright (C) 2017
 */
public class UZBPortAdapter extends AbstractPortController {

    private Short vendorID = 0;
    private Short productID = 0;
    protected UsbDevice usbDevice = null;

    public UZBPortAdapter(SystemConnectionMemo memo) {
        super(memo);
    }

    public Short getVendorID() {
        return vendorID;
    }

    public void setVendorID(Short value) {
        vendorID = value;
    }

    public Short getProductID() {
        return productID;
    }

    public void setProductID(Short value) {
        productID = value;
    }

    public UsbDevice getUsbDevice() {
        if (usbDevice == null) {
            String errorString = openPort(port, null);
            if (errorString != null) {
                log.error(errorString);
            }
        }
        return usbDevice;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect() throws java.io.IOException {
        log.debug("*	connect()");
    }

//    private String manufacturer = null;
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getManufacturer() {
//        log.debug("*	getManufacturer()");
//        return manufacturer;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setManufacturer(String manufacturer) {
//        log.debug("*	setManufacturer('{}')", manufacturer);
//        this.manufacturer = manufacturer;
//    }
    /**
     * {@inheritDoc}
     */
    @Override
    public DataInputStream getInputStream() {
        log.debug("*	getInputStream()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataOutputStream getOutputStream() {
        log.debug("*	getOutputStream()");
        return null;
    }

//    private boolean disabled = false;
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean getDisabled() {
//        log.debug("*	getDisabled()");
//        return disabled;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setDisabled(boolean disabled) {
//        log.debug("*	setDisabled()");
//        this.disabled = disabled;
//    }
//
//    private String userName = null;
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getUserName() {
//        log.debug("*	getUserName()");
//        return userName;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setUserName(String userName) throws IllegalArgumentException {
//        log.debug("*	setUserName('{}')", userName);
//        this.userName = userName;
//    }
//
//    private String systemPrefix = null;
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getSystemPrefix() {
//        log.debug("*	getSystemPrefix()");
//        return systemPrefix;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setSystemPrefix(String systemPrefix) throws IllegalArgumentException {
//        log.debug("*	setSystemPrefix('{}')", systemPrefix);
//        this.systemPrefix = systemPrefix;
//    }
//
//    private SystemConnectionMemo systemConnectionMemo = null;
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public SystemConnectionMemo getSystemConnectionMemo() {
//        log.debug("*	getSystemConnectionMemo()");
//        return systemConnectionMemo;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setSystemConnectionMemo(SystemConnectionMemo connectionMemo) throws IllegalArgumentException {
//        log.debug("*	setSystemConnectionMemo()");
//        systemConnectionMemo = connectionMemo;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void dispose() {
//        log.debug("*	dispose()");
//    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void recover() {
        log.debug("*	recover()");
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean isDirty() {
//        log.debug("*	isDirty()");
//        return false;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean isRestartRequired() {
//        log.debug("*	isRestartRequired()");
//        return false;
//    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        log.debug("*	configure()");
    }

//    private boolean status = true;
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean status() {
//        log.debug("*	status()");
//        return status;
//    }
    /**
     * {@inheritDoc}
     */
    public List<String> getPortNames() {
        log.debug("*	getPortNames()");

        List<String> results = new ArrayList<>();
        List<UsbDevice> usbDevices = USBUtil.getMatchingDevices(vendorID, productID);
        for (UsbDevice usbDevice : usbDevices) {
            results.add(USBUtil.getLocationID(usbDevice));
        }

        return results;
    }

    /**
     * {@inheritDoc}
     */
    public String openPort(String portName, String appName) {
        String result = null;   // assume success (optimist!)

        log.debug("*	openPort('{}','{}')", portName, appName);
        usbDevice = USBUtil.getMatchingDevice(vendorID, productID, portName);
        if (usbDevice == null) {
            result = String.format(
                    "USB device at location id {} not found.", portName);
        }
        return result;
    }

    private String port = null;

    /**
     * {@inheritDoc}
     */
    public void setPort(String s) {
        log.debug("*	setPort('{}')", s);
        port = s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentPortName() {
        log.debug("*	getCurrentPortName()");
        return port;
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getOption1Name() {
//        log.debug("*	getOption1Name()");
//        return "Location ID";
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getOption2Name() {
//        log.debug("*	getOption2Name()");
//        return null;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getOption3Name() {
//        log.debug("*	getOption3Name()");
//        return null;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getOption4Name() {
//        log.debug("*	getOption4Name()");
//        return null;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void configureOption1(String value) {
//        log.debug("*	configureOption1('{}')", value);
//        setPort(value);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void configureOption2(String value) {
//        log.debug("*	configureOption2('{}')", value);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void configureOption3(String value) {
//        log.debug("*	configureOption3('{}')", value);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void configureOption4(String value) {
//        log.debug("*	configureOption4('{}')", value);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String[] getOptions() {
//        log.debug("*	getOptions()");
//        Set<String> keySet = options.keySet();
//        return keySet.toArray(new String[keySet.size()]);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean isOptionAdvanced(String option) {
//        log.debug("*	isOptionAdvanced('{}')", option);
//        return false;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getOptionDisplayName(String option) {
//        log.debug("*	getOptionDisplayName('{}')", option);
//        return Bundle.getMessage(options.get(option));
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setOptionState(String option, String value) {
//        log.debug("*	setOptionState('{}', '{}')", option, value);
//        options.put(option, value);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getOptionState(String option) {
//        log.debug("*	getOptionState('{}')", option);
//        return options.get(option);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String[] getOptionChoices(String option) {
//        String[] results = new String[]{};
//        log.debug("*	getOptionChoices('{}')", option);
//        if (option.equals("XXX")) {
//            List<String> portNames = getPortNames();
//            results = portNames.toArray(new String[portNames.size()]);
//        }
//        return results;
//    }
    private final static Logger log = LoggerFactory.getLogger(UZBPortAdapter.class);
}
