package jmri.jmrix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an abstract base for *PortController classes.
 * <p>
 * This is complicated by the lack of multiple inheritance. SerialPortAdapter is
 * an Interface, and its implementing classes also inherit from various
 * PortController types. But we want some common behaviors for those, so we put
 * them here.
 *
 * @see jmri.jmrix.SerialPortAdapter
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
abstract public class AbstractPortController implements PortAdapter {

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DataInputStream getInputStream();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DataOutputStream getOutputStream();

    protected String manufacturerName = null;

    // By making this private, and not protected, we are able to require that
    // all access is through the getter and setter, and that subclasses that
    // override the getter and setter must call the super implementations of the
    // getter and setter. By channelling setting through a single method, we can
    // ensure this is never null.
    private SystemConnectionMemo connectionMemo;

    protected AbstractPortController(SystemConnectionMemo connectionMemo) {
        AbstractPortController.this.setSystemConnectionMemo(connectionMemo);
    }

    /**
     * Clean up before removal.
     *
     * Overriding methods must call <code>super.dispose()</code> or document why
     * they are not calling the overridden implementation. In most cases,
     * failure to call the overridden implementation will cause user-visible
     * error.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void dispose() {
        this.getSystemConnectionMemo().dispose();
    }

    /**
     * {@inheritDoc}
     */
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
     * The next set of configureOptions are to support the old configuration files.
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
     * The next set of getOption Names are to support legacy configuration files
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
     * @return Array of option identifier strings
     */
    @Override
    public String[] getOptions() {
        Set<String> keySet = options.keySet();
        String[] result = keySet.toArray(new String[keySet.size()]);
        java.util.Arrays.sort(result);
        return result;
    }

    /**
     * Set the value of an option.
     *
     * @param option the name string of the option
     * @param value the string value to set the option to
     */
    @Override
    public void setOptionState(String option, String value) {
        if (options.containsKey(option)) {
            options.get(option).configure(value);
        }
    }

    /**
     * Get the string value of a specific option.
     *
     * @param option the name of the option to query
     * @return the option value
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "availability was checked before, should never get here")
    public String getOptionState(String option) {
        if (options.containsKey(option)) {
            return options.get(option).getCurrent();
        }
        return null;
    }

    /**
     * Get a list of the various choices allowed with a given option.
     *
     * @param option the name of the option to query
     * @return list of valid values for the option, null if none are available
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "availability was checked before, should never get here")
    public String[] getOptionChoices(String option) {
        if (options.containsKey(option)) {
            return options.get(option).getOptions();
        }
        return null;
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "availability was checked before, should never get here")
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
        
        /** 
         * As a heuristic, we consider the 1st non-null
         * currentValue as the configured value. Changes away from that
         * mark an Option object as "dirty".
         */
        String configuredValue = null;
        
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
            if (configuredValue == null ) configuredValue = value;
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
            return (currentValue != null && !currentValue.equals(configuredValue));
        }
    }

    @Override
    public String getManufacturer() {
        return manufacturerName;
    }

    @Override
    public void setManufacturer(String manufacturer) {
        log.debug("update manufacturer from {} to {}", this.manufacturerName, manufacturer);
        this.manufacturerName = manufacturer;
    }

    @Override
    public boolean getDisabled() {
        return this.getSystemConnectionMemo().getDisabled();
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
        this.getSystemConnectionMemo().setDisabled(disabled);
    }

    @Override
    public String getSystemPrefix() {
        return this.getSystemConnectionMemo().getSystemPrefix();
    }

    @Override
    public void setSystemPrefix(String systemPrefix) {
        if (!this.getSystemConnectionMemo().setSystemPrefix(systemPrefix)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getUserName() {
        return this.getSystemConnectionMemo().getUserName();
    }

    @Override
    public void setUserName(String userName) {
        if (!this.getSystemConnectionMemo().setUserName(userName)) {
            throw new IllegalArgumentException();
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
        boolean isDirty = this.getSystemConnectionMemo().isDirty();
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

    /**
     * Service method to purge a stream of initial contents
     * while opening the connection.
     */
     @SuppressFBWarnings(value = "SR_NOT_CHECKED", justification = "skipping all, don't care what skip() returns")
     protected void purgeStream(@Nonnull java.io.InputStream serialStream) throws java.io.IOException {
        int count = serialStream.available();
        log.debug("input stream shows " + count + " bytes available");
        while (count > 0) {
            serialStream.skip(count);
            count = serialStream.available();
        }
    }
    
    /**
     * Get the {@link jmri.jmrix.SystemConnectionMemo} associated with this
     * object.
     * <p>
     * This method should only be overridden to ensure that a specific subclass
     * of SystemConnectionMemo is returned. The recommended pattern is: <code>
     * public MySystemConnectionMemo getSystemConnectionMemo() {
     *  return (MySystemConnectionMemo) super.getSystemConnectionMemo();
     * }
     * </code>
     *
     * @return the currently associated SystemConnectionMemo
     */
    @Override
    public SystemConnectionMemo getSystemConnectionMemo() {
        return this.connectionMemo;
    }

    /**
     * Set the {@link jmri.jmrix.SystemConnectionMemo} associated with this
     * object.
     * <p>
     * Overriding implementations must call
     * <code>super.setSystemConnectionMemo(memo)</code> at some point to ensure
     * the SystemConnectionMemo gets set.
     *
     * @param connectionMemo the SystemConnectionMemo to associate with this PortController
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void setSystemConnectionMemo(@Nonnull SystemConnectionMemo connectionMemo) {
        if (connectionMemo == null) {
            throw new NullPointerException();
        }
        this.connectionMemo = connectionMemo;
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractPortController.class);

}
