package jmri.jmrit.logixng;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;

import jmri.Manager;
import jmri.jmrit.logixng.Base.PrintTreeSettings;

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Manager for LogixNG
 *
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public interface LogixNG_Manager extends Manager<LogixNG> {

    /**
     * Create a new LogixNG if the LogixNG does not exist.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return a new LogixNG or null if unable to create
     */
    LogixNG createLogixNG(String systemName, String userName)
            throws IllegalArgumentException;

    /**
     * Create a new LogixNG if the LogixNG does not exist.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @param inline     true if this LogixNG is an inline LogixNG
     * @return a new LogixNG or null if unable to create
     */
    LogixNG createLogixNG(String systemName, String userName, boolean inline)
            throws IllegalArgumentException;

    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     *
     * @param userName the user name
     * @return a new LogixNG or null if unable to create
     */
    LogixNG createLogixNG(String userName)
            throws IllegalArgumentException;

    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     *
     * @param userName  the user name
     * @param inline    true if this LogixNG is an inline LogixNG
     * @return a new LogixNG or null if unable to create
     */
    LogixNG createLogixNG(String userName, boolean inline)
            throws IllegalArgumentException;

    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    LogixNG getLogixNG(String name);

    /** {@inheritDoc} */
    @Override
    LogixNG getByUserName(String name);

    /** {@inheritDoc} */
    @Override
    LogixNG getBySystemName(String name);

    /**
     * Create a new system name for a LogixNG.
     * @return a new system name
     */
    String getAutoSystemName();

    /**
     * Should the LogixNGs be disabled when the configuration file is loaded?
     * @param value true if they should be disabled, false otherwise.
     */
    void setLoadDisabled(boolean value);

    /**
     * Should the LogixNGs be started when the configuration file is loaded?
     * @param value true if they should be started, false otherwise.
     */
    void startLogixNGsOnLoad(boolean value);

    /**
     * Should the LogixNGs not be started when the configuration file is loaded?
     * @return true if they should be started, false otherwise.
     */
    boolean isStartLogixNGsOnLoad();

    /**
     * Setup all LogixNGs. This method is called after a configuration file is
     * loaded.
     */
    void setupAllLogixNGs();

    /**
     * Activate all LogixNGs, starts LogixNG processing by connecting all
     * inputs that are included the ConditionalNGs in this LogixNG.
     * <p>
     * A LogixNG must be activated before it will calculate any of its
     * ConditionalNGs.
     */
    void activateAllLogixNGs();

    /**
     * Activate all LogixNGs, starts LogixNG processing by connecting all
     * inputs that are included the ConditionalNGs in this LogixNG.
     * <p>
     * A LogixNG must be activated before it will calculate any of its
     * ConditionalNGs.
     *
     * @param runDelayed true if execute() should run on LogixNG thread delayed,
     *                   false otherwise.
     * @param runOnSeparateThread true if the activation should run on a
     *                            separate thread, false otherwise
     */
    void activateAllLogixNGs(boolean runDelayed, boolean runOnSeparateThread);

    /**
     * DeActivate all LogixNGs, stops LogixNG processing by disconnecting all
     * inputs that are included the ConditionalNGs in this LogixNG.
     * <p>
     * A LogixNG must be activated before it will calculate any of its
     * ConditionalNGs.
     */
    void deActivateAllLogixNGs();

    /**
     * Is LogixNGs active?
     * @return true if LogixNGs are active, false otherwise
     */
    boolean isActive();

    /**
     * Delete LogixNG by removing it from the manager. The LogixNG must first
     * be deactivated so it stops processing.
     *
     * @param x the LogixNG to delete
     */
    void deleteLogixNG(LogixNG x);

    /**
     * Print the tree to a stream.
     *
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     * @param lineNumber the line number
     */
    default void printTree(
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {

        printTree(new PrintTreeSettings(), writer, indent, lineNumber);
    }

    /**
     * Print the tree to a stream.
     *
     * @param settings settings for what to print
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     * @param lineNumber the line number
     */
    void printTree(
            PrintTreeSettings settings,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber);

    /**
     * Print the tree to a stream.
     *
     * @param locale The locale to be used
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     * @param lineNumber the line number
     */
    default void printTree(
            Locale locale,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {

        printTree(new PrintTreeSettings(), locale, writer, indent, lineNumber);
    }

    /**
     * Print the tree to a stream.
     *
     * @param settings settings for what to print
     * @param locale The locale to be used
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     * @param lineNumber the line number
     */
    void printTree(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber);

    /**
     * Test if parameter is a properly formatted system name.
     * <P>
     * This method should only be used by the managers of the LogixNG system.
     *
     * @param subSystemNamePrefix the sub system prefix
     * @param systemName the system name
     * @return enum indicating current validity, which might be just as a prefix
     */
    static NameValidity validSystemNameFormat(String subSystemNamePrefix, String systemName) {
        // System names with digits. :AUTO: is generated system names
        if (systemName.matches(subSystemNamePrefix+"(:AUTO:)?\\d+")) {
            return NameValidity.VALID;

        // System names with dollar sign allow any characters in the name
        } else if (systemName.matches(subSystemNamePrefix+"\\$.+")) {
            return NameValidity.VALID;

        // System names with :JMRI: belongs to JMRI itself
        } else if (systemName.matches(subSystemNamePrefix+":JMRI:.+")) {
            return NameValidity.VALID;

        // System names with :JMRI-LIB: belongs to software that uses JMRI as a lib
        } else if (systemName.matches(subSystemNamePrefix+":JMRI-LIB:.+")) {
            return NameValidity.VALID;

        // Other system names are not valid
        } else {
//            LoggerFactory.getLogger(LogixNG_Manager.class)
//                    .warn("system name {} is invalid for sub system prefix {}",
//                            systemName, subSystemNamePrefix);
            return NameValidity.INVALID;
        }
    }

    /**
     * Get the clipboard
     * @return the clipboard
     */
    Clipboard getClipboard();

    /**
     * Register a manager for later retrieval by getManager()
     * @param manager the manager
     */
    void registerManager(Manager<? extends MaleSocket> manager);

    /**
     * Get manager by class name
     * @param className the class name of the manager
     * @return the manager
     */
    Manager<? extends MaleSocket> getManager(String className);

    /**
     * Register a task to be run when setup LogixNGs
     * @param task the task
     */
    void registerSetupTask(Runnable task);

    /**
     * Executes a LogixNG Module.
     * Note that the module must be a Digital Action Module.
     * @param module     The module to be executed
     * @param parameter  The parameter. The module must have exactly one parameter.
     * @throws IllegalArgumentException If module is null or if module is not a
     *                   DigitalActionModule.
     */
    void executeModule(Module module, Object parameter)
            throws IllegalArgumentException;

    /**
     * Executes a LogixNG Module.
     * Note that the module must be a Digital Action Module.
     * @param module      The module to be executed
     * @param parameters  The parameters
     * @throws IllegalArgumentException If module or parameters is null or if module
     *                    is not a DigitalActionModule.
     */
    void executeModule(Module module, Map<String, Object> parameters)
            throws IllegalArgumentException;

}
