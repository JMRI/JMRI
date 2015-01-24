// AbstractPortController.java
package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an abstract base for *PortController classes.
 * <P>
 * This is complicated by the lack of multiple inheritance. SerialPortAdapter is
 * an Interface, and its implementing classes also inherit from various
 * PortController types. But we want some common behaviors for those, so we put
 * them here.
 *
 * @see jmri.jmrix.SerialPortAdapter
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 * @version	$Revision$
 */
abstract public class AbstractPortController implements PortAdapter {

    // returns the InputStream from the port
    @Override
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    @Override
    public abstract DataOutputStream getOutputStream();

    // Ideally, this would be final, but two classes expect to be
    // able to set this after object construction.
    private SystemConnectionMemo connectionMemo;

    protected AbstractPortController(SystemConnectionMemo connectionMemo) {
        if (connectionMemo == null) {
            throw new NullPointerException();
        }
        this.connectionMemo = connectionMemo;
    }

    @Override
    public void dispose() {
        if (this.getSystemConnectionMemo() != null) {
            this.getSystemConnectionMemo().dispose();
        }
    }

    // check that this object is ready to operate
    @Override
    public boolean status() {
        return opened;
    }

    protected boolean opened = false;

    protected void setOpened() {
        opened = true;
    }

    protected void setClosed() {
        opened = false;
    }

    //These are to support the old legacy files.
    protected String option1Name = "1";
    protected String option2Name = "2";
    protected String option3Name = "3";
    protected String option4Name = "4";

    @Override
    abstract public String getCurrentPortName();
    /*
     The next set of configureOptions are to support the old configuration files.
     */

    @Override
    public void configureOption1(String value) {
        if (options.containsKey(option1Name)) {
            options.get(option1Name).configure(value);
        }
    }

    @Override
    public void configureOption2(String value) {
        if (options.containsKey(option2Name)) {
            options.get(option2Name).configure(value);
        }
    }

    @Override
    public void configureOption3(String value) {
        if (options.containsKey(option3Name)) {
            options.get(option3Name).configure(value);
        }
    }

    @Override
    public void configureOption4(String value) {
        if (options.containsKey(option4Name)) {
            options.get(option4Name).configure(value);
        }
    }

    /*
     The next set of getOption Names are to support legacy configuration files
     */
    @Override
    public String getOption1Name() {
        return option1Name;
    }

    @Override
    public String getOption2Name() {
        return option2Name;
    }

    @Override
    public String getOption3Name() {
        return option3Name;
    }

    @Override
    public String getOption4Name() {
        return option4Name;
    }

    /**
     * Get a list of all the options configured against this adapter.
     *
     * @return Array of option identifiers
     */
    @Override
    public String[] getOptions() {
        String[] arr = options.keySet().toArray(new String[0]);
        java.util.Arrays.sort(arr);
        return arr;

    }

    /**
     * Set the value of an option
     *
     * @param option
     * @param value
     */
    @Override
    public void setOptionState(String option, String value) {
        if (options.containsKey(option)) {
            options.get(option).configure(value);
        }
    }

    /**
     * Get the value of a specific option.
     *
     * @param option
     * @return the option value
     */
    @Override
    public String getOptionState(String option) {
        if (options.containsKey(option)) {
            return options.get(option).getCurrent();
        }
        return null;
    }

    /**
     * Return a list of the various choices allowed with an option.
     *
     * @param option
     * @return list of valid values for the option
     */
    @Override
    public String[] getOptionChoices(String option) {
        if (options.containsKey(option)) {
            return options.get(option).getOptions();
        }
        return null;
    }

    @Override
    public String getOptionDisplayName(String option) {
        if (options.containsKey(option)) {
            return options.get(option).getDisplayText();
        }
        return null;
    }

    @Override
    public boolean isOptionAdvanced(String option) {
        if (options.containsKey(option)) {
            return options.get(option).isAdvanced();
        }
        return false;
    }

    protected HashMap<String, Option> options = new HashMap<>();

    static protected class Option {

        String currentValue = null;
        String displayText;
        String[] options;
        Boolean advancedOption = true;

        public Option(String displayText, String[] options, boolean advanced) {
            this(displayText, options);
            this.advancedOption = advanced;
        }

        public Option(String displayText, String[] options) {
            this.displayText = displayText;
            this.options = new String[options.length];
            System.arraycopy(options, 0, this.options, 0, options.length);
        }

        void configure(String value) {
            currentValue = value;
        }

        String getCurrent() {
            if (currentValue == null) {
                return options[0];
            }
            return currentValue;
        }

        String[] getOptions() {
            return options;
        }

        String getDisplayText() {
            return displayText;
        }

        boolean isAdvanced() {
            return advancedOption;
        }

        boolean isDirty() {
            return (currentValue != null && !currentValue.equals(options[0]));
        }
    }

    /**
     * Get and set of the Manufacturer for network (TCP/IP) based connections is
     * handled by the ConnectionConfig code in each connector. this is here as
     * we implement the serialdriveradpter.
     */
    @Override
    public String getManufacturer() {
        return mManufacturer;
    }

    @Override
    public void setManufacturer(String manufacturer) {
        log.debug("update manufacturer from " + mManufacturer + " to " + manufacturer);
        mManufacturer = manufacturer;
    }
    protected String mManufacturer = null;

    @Override
    public boolean getDisabled() {
        if (this.getSystemConnectionMemo() != null) {
            return this.getSystemConnectionMemo().getDisabled();
        }
        return this.mDisabled;
    }

    /**
     * Set the connection disabled or enabled. By default connections are
     * enabled.
     *
     * If the implementing class does not use a
     * {@link jmri.jmrix.SystemConnectionMemo}, this method must be overridden.
     * Overriding methods must call <code>super.setDisabled(boolean)</code> to
     * ensure the configuration change state is correctly set.
     *
     * @param disabled true if connection should be disabled
     */
    @Override
    public void setDisabled(boolean disabled) {
        if (this.loadedDisabled == null) {
            this.loadedDisabled = disabled;
        }
        if (this.getSystemConnectionMemo() != null) {
            this.getSystemConnectionMemo().setDisabled(disabled);
        }
        this.mDisabled = disabled;
    }

    protected boolean mDisabled = false;
    private Boolean loadedDisabled = null;

    @Override
    public String getSystemPrefix() {
        if (this.getSystemConnectionMemo() != null) {
            return this.getSystemConnectionMemo().getSystemPrefix();
        }
        return null;
    }

    @Override
    public void setSystemPrefix(String systemPrefix) {
        if (this.getSystemConnectionMemo() != null) {
            if (!this.getSystemConnectionMemo().setSystemPrefix(systemPrefix)) {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public String getUserName() {
        if (this.getSystemConnectionMemo() != null) {
            return this.getSystemConnectionMemo().getUserName();
        }
        return null;
    }

    @Override
    public void setUserName(String userName) {
        if (this.getSystemConnectionMemo() != null) {
            if (!this.getSystemConnectionMemo().setUserName(userName)) {
                throw new IllegalArgumentException();
            }
        }
    }

    protected boolean allowConnectionRecovery = false;

    @Override
    abstract public void recover();

    protected int reconnectinterval = 1000;
    protected int retryAttempts = 10;

    protected static void safeSleep(long milliseconds, String s) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.error("Sleep Exception raised during reconnection attempt" + s);
        }
    }

    @Override
    public boolean isDirty() {
        boolean isDirty = (this.loadedDisabled == null || this.loadedDisabled != this.getDisabled());
        if (!isDirty) {
            for (Option option : this.options.values()) {
                isDirty = option.isDirty();
                if (isDirty) {
                    break;
                }
            }
        }
        return isDirty;
    }

    @Override
    public boolean isRestartRequired() {
        // Override if any option should not be considered when determining if a
        // change requires JMRI to be restarted.
        return this.isDirty();
    }

    @Override
    public SystemConnectionMemo getSystemConnectionMemo() {
        return this.connectionMemo;
    }

    protected void setSystemConnectionMemo(SystemConnectionMemo connectionMemo) {
        if (connectionMemo == null) {
            throw new NullPointerException();
        }
        this.connectionMemo = connectionMemo;
    }
    
    static private Logger log = LoggerFactory.getLogger(AbstractPortController.class.getName());

}
