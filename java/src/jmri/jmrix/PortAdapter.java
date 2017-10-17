package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Enables basic setup of a interface for a jmrix implementation.
 * <P>
 * This is the basic interface. Subclasses provide extensions for specific
 * connection types (network, serial, etc).
 * <p>
 * For historical reasons, this provides both four specific options (option1 to option4)
 * plus a more flexible interface based on a String array.  The more flexible 
 * interface is the preferred one for new work, but the 1-4 form hasn't been 
 * deprecated yet.
 * <p>
 * General design documentation is available on the 
 * <a href="http://jmri.org/help/en/html/doc/Technical/SystemStructure.shtml">Structure of External System Connections page</a>.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2008, 2010
 * @see jmri.jmrix.SerialConfigException
 * @since 2.3.1
 */
public interface PortAdapter {

    /**
     * Configure all of the other jmrix widgets needed to work with this adapter
     */
    public void configure();

    /**
     * Query the status of this connection. If all OK, at least as far as is
     * known, return true
     *
     * @return true if OK
     */
    public boolean status();

    /**
     * Open the connection.
     *
     * @throws java.io.IOException if unable to connect
     */
    public void connect() throws java.io.IOException;

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
     * Set the first port option. Only to be used after construction, but before
     * the openPort call
     *
     */
    public void configureOption1(String value);

    /**
     * Set the second port option. Only to be used after construction, but
     * before the openPort call
     *
     */
    public void configureOption2(String value);

    /**
     * Set the third port option. Only to be used after construction, but before
     * the openPort call
     *
     */
    public void configureOption3(String value);

    /**
     * Set the fourth port option. Only to be used after construction, but
     * before the openPort call
     *
     */
    public void configureOption4(String value);

    public String[] getOptions();

    public boolean isOptionAdvanced(String option);

    public String getOptionDisplayName(String option);

    public void setOptionState(String option, String value);

    public String getOptionState(String option);

    public String[] getOptionChoices(String option);

    /**
     * Return the system manufacturer's name.
     *
     * @return Manufacturer's Name
     */
    public String getManufacturer();

    /**
     * Set the system manufacturer's name.
     *
     * @param Manufacturer the manufacturer's name
     */
    public void setManufacturer(String Manufacturer);

    /**
     * Return the disabled state of the adapter.
     *
     * @return true if disabled
     */
    public boolean getDisabled();

    /**
     * Sets whether the connection is disabled.
     *
     * @param disabled When true, disables operation
     */
    public void setDisabled(boolean disabled);

    /**
     * Get the user name for this adapter.
     *
     * @return the username or null
     */
    public String getUserName();

    /**
     * Set the user name for this adapter.
     *
     * @param userName the new user name
     * @throws IllegalArgumentException if another adapter has this user name
     */
    public void setUserName(String userName) throws IllegalArgumentException;

    /**
     * Get the system prefix for this adapter.
     *
     * @return the system prefix or null
     */
    public String getSystemPrefix();

    /**
     * Set the system prefix for this adapter.
     *
     * @param systemPrefix the new system prefix
     * @throws IllegalArgumentException if another adapter has this system
     *                                  prefix
     */
    public void setSystemPrefix(String systemPrefix) throws IllegalArgumentException;

    public SystemConnectionMemo getSystemConnectionMemo();

    /**
     * Replace the existing SystemConnectionMemo with another one. Overriding
     * methods should throw an {@link java.lang.IllegalAccessException} if the
     * overriding class requires a specific subclass of SystemConnectionMemo. A
     * {@link java.lang.NullPointerException} should be thrown if the parameter
     * is null.
     *
     * @param connectionMemo the new connection memo
     * @throws IllegalArgumentException if connectionMemo is the wrong subclass
     *                                  of SystemConnectionMemo
     * @throws NullPointerException     if connectionMemo is null
     */
    public void setSystemConnectionMemo(SystemConnectionMemo connectionMemo) throws IllegalArgumentException;

    public void dispose();

    public void recover();

    /**
     * Determine if configuration needs to be written to disk.
     *
     * @return true if configuration needs to be saved, false otherwise
     */
    public boolean isDirty();

    /**
     * Determine if application needs to be restarted for configuration changes
     * to be applied.
     *
     * @return true if application needs to restart, false otherwise
     */
    public boolean isRestartRequired();

}
