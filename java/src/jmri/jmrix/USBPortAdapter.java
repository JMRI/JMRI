package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables basic setup of a USB interface for a jmrix implementation.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2008
 * @author George Warner Copyright (C) 2017
 */
public class USBPortAdapter implements PortAdapter {

    /**
     * {@inheritDoc}
     */
    public void connect() throws java.io.IOException {
        log.info("*connect()");
    }

    private String manufacturer = null;

    /**
     * {@inheritDoc}
     */
    public String getManufacturer() {
        log.info("*getManufacturer()");
        return manufacturer;
    }

    /**
     * {@inheritDoc}
     */
    public void setManufacturer(String manufacturer) {
        log.info("*setManufacturer('{}')", manufacturer);
        this.manufacturer = manufacturer;
    }

    /**
     * {@inheritDoc}
     */
    public DataInputStream getInputStream() {
        log.info("*getInputStream()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public DataOutputStream getOutputStream() {
        log.info("*getOutputStream()");
        return null;
    }

    boolean disabled = false;

    /**
     * {@inheritDoc}
     */
    public boolean getDisabled() {
        log.info("*getDisabled()");
        return disabled;
    }

    /**
     * {@inheritDoc}
     */
    public void setDisabled(boolean disabled) {
        log.info("*setDisabled()");
        this.disabled = disabled;
    }

    private String userName = null;

    /**
     * {@inheritDoc}
     */
    public String getUserName() {
        log.info("*getUserName()");
        return userName;
    }

    /**
     * {@inheritDoc}
     */
    public void setUserName(String userName) throws IllegalArgumentException {
        log.info("*setUserName('{}')", userName);
        this.userName = userName;
    }

    private String systemPrefix = null;

    /**
     * {@inheritDoc}
     */
    public String getSystemPrefix() {
        log.info("*getSystemPrefix()");
        return systemPrefix;
    }

    /**
     * {@inheritDoc}
     */
    public void setSystemPrefix(String systemPrefix) throws IllegalArgumentException {
        log.info("*setSystemPrefix('{}')", systemPrefix);
        this.systemPrefix = systemPrefix;
    }

    private SystemConnectionMemo systemConnectionMemo = null;

    /**
     * {@inheritDoc}
     */
    public SystemConnectionMemo getSystemConnectionMemo() {
        log.info("*getSystemConnectionMemo()");
        return systemConnectionMemo;
    }

    /**
     * {@inheritDoc}
     */
    public void setSystemConnectionMemo(SystemConnectionMemo connectionMemo) throws IllegalArgumentException {
        log.info("*setSystemConnectionMemo()");
        systemConnectionMemo = connectionMemo;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        log.info("*dispose()");
    }

    /**
     * {@inheritDoc}
     */
    public void recover() {
        log.info("*recover()");
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDirty() {
        log.info("*isDirty()");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRestartRequired() {
        log.info("*isRestartRequired()");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getPortNames() {
        log.info("*getPortNames()");
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    public String openPort(String portName, String appName) {
        log.info("*openPort('{}')", manufacturer);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void configure() {
        log.info("*configure()");
    }

    private boolean status = true;

    /**
     * {@inheritDoc}
     */
    public boolean status() {
        log.info("*status()");
        return status;
    }

    /**
     * {@inheritDoc}
     */
    public void setPort(String s) {
        log.info("*setPort('{}')", manufacturer);
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentPortName() {
        log.info("*getCurrentPortName()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getOption1Name() {
        log.info("*getOption1Name()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getOption2Name() {
        log.info("*getOption2Name()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getOption3Name() {
        log.info("*getOption3Name()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getOption4Name() {
        log.info("*getOption4Name()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void configureOption1(String value) {
        log.info("*configureOption1('{}')", value);
    }

    /**
     * {@inheritDoc}
     */
    public void configureOption2(String value) {
        log.info("*configureOption2('{}')", value);
    }

    /**
     * {@inheritDoc}
     */
    public void configureOption3(String value) {
        log.info("*configureOption3('{}')", value);
    }

    /**
     * {@inheritDoc}
     */
    public void configureOption4(String value) {
        log.info("*configureOption4('{}')", value);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getOptions() {
        log.info("*getOptions()");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOptionAdvanced(String option) {
        log.info("*isOptionAdvanced('{}')", option);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getOptionDisplayName(String option) {
        log.info("*getOptionDisplayName('{}')", option);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setOptionState(String option, String value) {
        log.info("*setOptionState('{}')", value);
    }

    /**
     * {@inheritDoc}
     */
    public String getOptionState(String option) {
        log.info("*getOptionState('{}')", option);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getOptionChoices(String option) {
        log.info("*getOptionChoices('{}')", option);
        return new String[]{};
    }
    private final static Logger log = LoggerFactory.getLogger(USBPortAdapter.class);

}
