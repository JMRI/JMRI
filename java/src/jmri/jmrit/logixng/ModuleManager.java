package jmri.jmrit.logixng;

import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import jmri.Manager;
import jmri.jmrit.logixng.Base.PrintTreeSettings;

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Manager for LogixNG modules
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2020
 */
public interface ModuleManager extends Manager<Module> {

    /**
     * Create a new Module if the Module does not exist.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @param socketType the socket type
     * @return a new Module or null if unable to create
     */
    public Module createModule(String systemName, String userName,
            FemaleSocketManager.SocketType socketType)
            throws IllegalArgumentException;
    
    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     *
     * @param userName the user name
     * @param socketType the socket type
     * @return a new Module or null if unable to create
     */
    public Module createModule(String userName, FemaleSocketManager.SocketType socketType)
            throws IllegalArgumentException;
    
    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    public Module getModule(String name);
    
    /** {@inheritDoc} */
    @Override
    public Module getByUserName(String name);
    
    /** {@inheritDoc} */
    @Override
    public Module getBySystemName(String name);
    
    /**
     * Create a new system name for a Module.
     * @return a new system name
     */
    public String getAutoSystemName();
    
    /**
     * Resolve all the Module trees.
     * <P>
     * This method ensures that everything in the Module tree has a pointer
     * to its parent.
     */
    public boolean resolveAllTrees(List<String> errors);

    /**
     * Setup all Modules. This method is called after a configuration file is
     * loaded.
     */
    public void setupAllModules();

    /*.*
     * Activate all Modules, starts Module processing by connecting all
     * inputs that are included the ConditionalNGs in this Module.
     * <p>
     * A Module must be activated before it will calculate any of its
     * ConditionalNGs.
     *./
    public void activateAllModules();
*/    
    /**
     * Delete Module by removing it from the manager. The Module must first
     * be deactivated so it stops processing.
     *
     * @param x the Module to delete
     */
    void deleteModule(Module x);

    /*.*
     * Support for loading Modules in a disabled state
     * 
     * @param s true if Module should be disabled when loaded
     *./
    public void setLoadDisabled(boolean s);
*/    
    /**
     * Print the tree to a stream.
     * 
     * @param settings settings for what to print
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     * @param lineNumber the line number
     */
    public void printTree(
            PrintTreeSettings settings,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber);
    
    /**
     * Print the tree to a stream.
     * 
     * @param settings settings for what to print
     * @param locale The locale to be used
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     * @param lineNumber the line number
     */
    public void printTree(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber);
    
    /**
     * Test if parameter is a properly formatted system name.
     * <P>
     * This method should only be used by the managers of the Module system.
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
//            LoggerFactory.getLogger(Module_Manager.class)
//                    .warn("system name {} is invalid for sub system prefix {}",
//                            systemName, subSystemNamePrefix);
            return NameValidity.INVALID;
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * The sub system prefix for the DigitalActionManager is
     * {@link #getSystemNamePrefix() } and "DA";
     */
    @Override
    public default String getSubSystemNamePrefix() {
        return getSystemNamePrefix() + "M";
    }
    
}
