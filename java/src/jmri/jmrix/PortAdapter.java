package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import jmri.SystemConnectionMemo;

/**
 * Enables basic setup of a interface for a jmrix implementation.
 * <p>
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
     * Configure all of the other jmrix widgets needed to work with this adapter.
     */
    void configure();

    /**
     * Query the status of this connection.
     * This is a question of configuration, not transient hardware status.
     *
     * @return true if OK, at least as far as known
     */
    boolean status();

    /**
     * Open the connection.
     *
     * @throws java.io.IOException if unable to connect
     */
    void connect() throws IOException;

    String getCurrentPortName();

    /**
     * Get the InputStream from the port.
     *
     * @return the InputStream from the port
     */
    DataInputStream getInputStream();

    /**
     * Get the outputStream to the port.
     *
     * @return the outputStream to the port
     */
    DataOutputStream getOutputStream();

    String getOption1Name();

    String getOption2Name();

    String getOption3Name();

    String getOption4Name();

    /**
     * Set the first port option. Only to be used after construction,
     * but before the openPort call.
     *
     * @param value to set the option to
     */
    void configureOption1(String value);

    /**
     * Set the second port option. Only to be used after construction,
     * but before the openPort call.
     *
     * @param value to set the option to
     */
    void configureOption2(String value);

    /**
     * Set the third port option. Only to be used after construction,
     * but before the openPort call.
     *
     * @param value to set the option to
     */
    void configureOption3(String value);

    /**
     * Set the fourth port option. Only to be used after construction,
     * but before the openPort call.
     *
     * @param value to set the option to
     */
    void configureOption4(String value);

    /**
     * Get a list of all the options configured against this adapter.
     *
     * @return Array of option identifier strings
     */
    String[] getOptions();

    boolean isOptionAdvanced(String option);

    String getOptionDisplayName(String option);

    /**
     * Set the value of an option.
     *
     * @param option the name string of the option
     * @param value the string value to set the option to
     */
    void setOptionState(String option, String value);

    /**
     * Get the string value of a specific option.
     *
     * @param option the name of the option to query
     * @return the option value
     */
    String getOptionState(String option);

    /**
     * Get a list of the various choices allowed with an given option.
     *
     * @param option the name of the option to query
     * @return list of valid values for the option
     */
    String[] getOptionChoices(String option);

    /**
     * Should this option be represented by a text field
     * (as opposed to a JCombobox)
     * @param option Name of the option to check
     * @return true for text representation preferred
     */
    boolean isOptionTypeText(String option);
    
    /**
     * Should this option be represented by a password field
     * @param option Name of the option to check
     * @return true for text representation preferred
     */
    boolean isOptionTypePassword(String option);
    
    /**
     * Get the system manufacturer's name.
     *
     * @return manufacturer's name
     */
    String getManufacturer();

    /**
     * Set the system manufacturer's name.
     *
     * @param Manufacturer the manufacturer's name
     */
    void setManufacturer(String Manufacturer);

    /**
     * Return the disabled state of the adapter.
     *
     * @return true if disabled
     */
    boolean getDisabled();

    /**
     * Set whether the connection is disabled.
     *
     * @param disabled When true, disables operation
     */
    void setDisabled(boolean disabled);

    /**
     * Get the user name for this adapter.
     *
     * @return the username or null
     */
    String getUserName();

    /**
     * Set the user name for this adapter.
     *
     * @param userName the new user name
     * @throws IllegalArgumentException if another adapter has this user name
     */
    void setUserName(String userName) throws IllegalArgumentException;

    /**
     * Get the system prefix for this adapter.
     *
     * @return the system prefix or null
     */
    String getSystemPrefix();

    /**
     * Set the system prefix for this adapter.
     *
     * @param systemPrefix the new system prefix
     * @throws IllegalArgumentException if another adapter has this system
     *                                  prefix
     */
    void setSystemPrefix(String systemPrefix) throws IllegalArgumentException;

    SystemConnectionMemo getSystemConnectionMemo();

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
    void setSystemConnectionMemo(SystemConnectionMemo connectionMemo) throws IllegalArgumentException;

    /**
     * This is called when a connection is to be disposed.
     */
    void dispose();

    /**
     * This is called when a connection is initially lost.
     */
    void recover();

    /**
     * Determine if configuration needs to be written to disk.
     *
     * @return true if configuration needs to be saved, false otherwise
     */
    boolean isDirty();

    /**
     * Determine if application needs to be restarted for configuration changes
     * to be applied.
     *
     * @return true if application needs to restart, false otherwise
     */
    boolean isRestartRequired();
    
    /**
     * Set the maximum interval between reconnection attempts.
     * @param maxInterval in seconds.
     */
    void setReconnectMaxInterval(int maxInterval);
    
    /**
     * Set the maximum number of reconnection attempts.
     * -1 will set an infinite number of attempts.
     * @param maxAttempts total maximum reconnection attempts.
     */
    void setReconnectMaxAttempts(int maxAttempts);
    
    /**
     * Get the maximum interval between reconnection attempts.
     * @return maximum interval in seconds.
     */
    int getReconnectMaxInterval();
    
    /**
     * Get the maximum number of reconnection attempts which should be made.
     * A value of -1 means no maximum value, i.e. infinite attempts.
     * @return total number of attempts which should be made.
     */
    int getReconnectMaxAttempts();

}
