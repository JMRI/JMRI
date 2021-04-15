package jmri.jmrit.logixng;

import java.io.PrintWriter;
import java.util.Locale;

import jmri.Manager;
import jmri.jmrit.logixng.Base.PrintTreeSettings;

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
    public LogixNG createLogixNG(String systemName, String userName)
            throws IllegalArgumentException;
    
    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     *
     * @param userName the user name
     * @return a new LogixNG or null if unable to create
     */
    public LogixNG createLogixNG(String userName)
            throws IllegalArgumentException;
    
    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    public LogixNG getLogixNG(String name);
    
    /** {@inheritDoc} */
    @Override
    public LogixNG getByUserName(String name);
    
    /** {@inheritDoc} */
    @Override
    public LogixNG getBySystemName(String name);
    
    /**
     * Create a new system name for a LogixNG.
     * @return a new system name
     */
    public String getAutoSystemName();
    
    /**
     * Resolve all the LogixNG trees.
     * <P>
     * This method ensures that everything in the LogixNG tree has a pointer
     * to its parent.
     */
    public void resolveAllTrees();

    /**
     * Setup all LogixNGs. This method is called after a configuration file is
     * loaded.
     */
    public void setupAllLogixNGs();

    /**
     * Activate all LogixNGs, starts LogixNG processing by connecting all
     * inputs that are included the ConditionalNGs in this LogixNG.
     * <p>
     * A LogixNG must be activated before it will calculate any of its
     * ConditionalNGs.
     */
    public void activateAllLogixNGs();
    
    /**
     * DeActivate all LogixNGs, stops LogixNG processing by disconnecting all
     * inputs that are included the ConditionalNGs in this LogixNG.
     * <p>
     * A LogixNG must be activated before it will calculate any of its
     * ConditionalNGs.
     */
    public void deActivateAllLogixNGs();
    
    /**
     * Is LogixNGs active?
     * @return true if LogixNGs are active, false otherwise
     */
    public boolean isActive();
    
    /**
     * Delete LogixNG by removing it from the manager. The LogixNG must first
     * be deactivated so it stops processing.
     *
     * @param x the LogixNG to delete
     */
    void deleteLogixNG(LogixNG x);

    /**
     * Support for loading LogixNGs in a disabled state
     * 
     * @param s true if LogixNG should be disabled when loaded
     */
    public void setLoadDisabled(boolean s);
    
    /**
     * Print the tree to a stream.
     * 
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     */
    public default void printTree(PrintWriter writer, String indent) {
        printTree(new PrintTreeSettings(), writer, indent);
    }
    
    /**
     * Print the tree to a stream.
     * 
     * @param settings settings for what to print
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     */
    public void printTree(PrintTreeSettings settings, PrintWriter writer, String indent);
    
    /**
     * Print the tree to a stream.
     * 
     * @param locale The locale to be used
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     */
    public default void printTree(
            Locale locale,
            PrintWriter writer,
            String indent) {
        printTree(new PrintTreeSettings(), locale, writer, indent);
    }
    
    /**
     * Print the tree to a stream.
     * 
     * @param settings settings for what to print
     * @param locale The locale to be used
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     */
    public void printTree(PrintTreeSettings settings, Locale locale, PrintWriter writer, String indent);
    
    /**
     * Test if parameter is a properly formatted system name.
     * <P>
     * This method should only be used by the managers of the LogixNG system.
     *
     * @param subSystemNamePrefix the sub system prefix
     * @param systemName the system name
     * @return enum indicating current validity, which might be just as a prefix
     */
    public static NameValidity validSystemNameFormat(String subSystemNamePrefix, String systemName) {
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
    public Clipboard getClipboard();
    
    /**
     * Register a manager for later retrieval by getManager()
     * @param manager the manager
     */
    public void registerManager(Manager<? extends MaleSocket> manager);
    
    /**
     * Get manager by class name
     * @param className the class name of the manager
     * @return the manager
     */
    public Manager<? extends MaleSocket> getManager(String className);
    
}
