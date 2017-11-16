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
public class UsbPortAdapter extends AbstractPortController {

    public UsbPortAdapter(SystemConnectionMemo memo) {
        super(memo);
    }

//    private HashMap<String, String> options = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect() throws java.io.IOException {
        log.info("*	connect()");
    }

//    private String manufacturer = null;
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getManufacturer() {
//        log.info("*	getManufacturer()");
//        return manufacturer;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setManufacturer(String manufacturer) {
//        log.info("*	setManufacturer('{}')", manufacturer);
//        this.manufacturer = manufacturer;
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataInputStream getInputStream() {
        log.info("*	getInputStream()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataOutputStream getOutputStream() {
        log.info("*	getOutputStream()");
        return null;
    }

//    private boolean disabled = false;
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean getDisabled() {
//        log.info("*	getDisabled()");
//        return disabled;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setDisabled(boolean disabled) {
//        log.info("*	setDisabled()");
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
//        log.info("*	getUserName()");
//        return userName;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setUserName(String userName) throws IllegalArgumentException {
//        log.info("*	setUserName('{}')", userName);
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
//        log.info("*	getSystemPrefix()");
//        return systemPrefix;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setSystemPrefix(String systemPrefix) throws IllegalArgumentException {
//        log.info("*	setSystemPrefix('{}')", systemPrefix);
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
//        log.info("*	getSystemConnectionMemo()");
//        return systemConnectionMemo;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setSystemConnectionMemo(SystemConnectionMemo connectionMemo) throws IllegalArgumentException {
//        log.info("*	setSystemConnectionMemo()");
//        systemConnectionMemo = connectionMemo;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void dispose() {
//        log.info("*	dispose()");
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recover() {
        log.info("*	recover()");
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean isDirty() {
//        log.info("*	isDirty()");
//        return false;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean isRestartRequired() {
//        log.info("*	isRestartRequired()");
//        return false;
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        log.info("*	configure()");
    }

//    private boolean status = true;
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean status() {
//        log.info("*	status()");
//        return status;
//    }

    /**
     * {@inheritDoc}
     */
    public List<String> getPortNames() {
        log.info("*	getPortNames()");

        List<String> results = new ArrayList<>();
        List<UsbDevice> usbDevices = USBUtil.getMatchingDevices((short) 0x16C0, (short) 0x05DC);
        for (UsbDevice usbDevice : usbDevices) {
            results.add(USBUtil.getLocationID(usbDevice));
        }

        return results;
    }

    /**
     * {@inheritDoc}
     */
    public String openPort(String portName, String appName) {
        log.info("*	openPort('{}','{}')", portName, appName);
        return null;
    }

    private String port = null;

    /**
     * {@inheritDoc}
     */
    public void setPort(String s) {
        log.info("*	setPort('{}')", s);
        port = s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentPortName() {
        log.info("*	getCurrentPortName()");
        return port;
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getOption1Name() {
//        log.info("*	getOption1Name()");
//        return "Location ID";
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getOption2Name() {
//        log.info("*	getOption2Name()");
//        return null;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getOption3Name() {
//        log.info("*	getOption3Name()");
//        return null;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getOption4Name() {
//        log.info("*	getOption4Name()");
//        return null;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void configureOption1(String value) {
//        log.info("*	configureOption1('{}')", value);
//        setPort(value);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void configureOption2(String value) {
//        log.info("*	configureOption2('{}')", value);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void configureOption3(String value) {
//        log.info("*	configureOption3('{}')", value);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void configureOption4(String value) {
//        log.info("*	configureOption4('{}')", value);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String[] getOptions() {
//        log.info("*	getOptions()");
//        Set<String> keySet = options.keySet();
//        return keySet.toArray(new String[keySet.size()]);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean isOptionAdvanced(String option) {
//        log.info("*	isOptionAdvanced('{}')", option);
//        return false;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getOptionDisplayName(String option) {
//        log.info("*	getOptionDisplayName('{}')", option);
//        return Bundle.getMessage(options.get(option));
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void setOptionState(String option, String value) {
//        log.info("*	setOptionState('{}', '{}')", option, value);
//        options.put(option, value);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String getOptionState(String option) {
//        log.info("*	getOptionState('{}')", option);
//        return options.get(option);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String[] getOptionChoices(String option) {
//        String[] results = new String[]{};
//        log.info("*	getOptionChoices('{}')", option);
//        if (option.equals("XXX")) {
//            List<String> portNames = getPortNames();
//            results = portNames.toArray(new String[portNames.size()]);
//        }
//        return results;
//    }
    private final static Logger log = LoggerFactory.getLogger(UsbPortAdapter.class);
}
