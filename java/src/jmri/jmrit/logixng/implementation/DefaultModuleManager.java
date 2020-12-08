package jmri.jmrit.logixng.implementation;

import java.io.PrintWriter;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.managers.AbstractManager;
import jmri.util.*;


/**
 * Class providing the basic logic of the LogixNG_Manager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultModuleManager extends AbstractManager<Module>
        implements ModuleManager {

    
    public DefaultModuleManager() {
        // The LogixNGPreferences class may load plugins so we must ensure
        // it's loaded here.
        InstanceManager.getDefault(LogixNGPreferences.class);
    }

    @Override
    public int getXMLOrder() {
        return LOGIXNGS;
    }

    @Override
    public char typeLetter() {
        return 'Q';
    }

    /**
     * Test if parameter is a properly formatted system name.
     *
     * @param systemName the system name
     * @return enum indicating current validity, which might be just as a prefix
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return LogixNG_Manager.validSystemNameFormat(
                getSubSystemNamePrefix(), systemName);
//        if (systemName.matches(getSubSystemNamePrefix()+"(:AUTO:)?\\d+")) {
//            return NameValidity.VALID;
//        } else {
//            return NameValidity.INVALID;
//        }
    }

    /**
     * Method to create a new Module if the Module does not exist.
     * <p>
     * Returns null if
     * a Logix with the same systemName or userName already exists, or if there
     * is trouble creating a new Module.
     */
    @Override
    public Module createModule(String systemName, String userName)
            throws IllegalArgumentException {
        
        // Check that Module does not already exist
        Module x;
        if (userName != null && !userName.equals("")) {
            x = getByUserName(userName);
            if (x != null) {
                return null;
            }
        }
        x = getBySystemName(systemName);
        if (x != null) {
            return null;
        }
        // Check if system name is valid
        if (this.validSystemNameFormat(systemName) != NameValidity.VALID) {
            throw new IllegalArgumentException("SystemName " + systemName + " is not in the correct format");
        }
        // Module does not exist, create a new Module
        x = new DefaultModule(systemName, userName);
        // save in the maps
        register(x);
        
        // Keep track of the last created auto system name
        updateAutoNumber(systemName);
        
        return x;
    }

    @Override
    public Module createModule(String userName) throws IllegalArgumentException {
        return createModule(getAutoSystemName(), userName);
    }
    
    @Override
    public Module getModule(String name) {
        Module x = getByUserName(name);
        if (x != null) {
            return x;
        }
        return getBySystemName(name);
    }

    @Override
    public Module getByUserName(String name) {
        return _tuser.get(name);
    }

    @Override
    public Module getBySystemName(String name) {
        return _tsys.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameLogixNGModules" : "BeanNameLogixNGModule");
    }

    /** {@inheritDoc} */
    @Override
    public void resolveAllTrees() {
        for (Module logixNG_Module : _tsys.values()) {
            logixNG_Module.setParentForAllChildren();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setupAllModules() {
        for (Module logixNG : _tsys.values()) {
            logixNG.setup();
        }
    }

    /*.* {@inheritDoc} *./
    @Override
    public void activateAllLogixNGs() {
        for (Module logixNG : _tsys.values()) {
            logixNG.activateLogixNG();
        }
    }
*/
    @Override
    public void deleteModule(Module x) {
        // delete the Module
        deregister(x);
        x.dispose();
    }
/*
    @Override
    public void setLoadDisabled(boolean s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
*/    
    /** {@inheritDoc} */
    @Override
    public void printTree(PrintWriter writer, String indent) {
        printTree(Locale.getDefault(), writer, indent);
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent) {
        for (Module module : getNamedBeanSet()) {
            module.printTree(locale, writer, indent, "");
            writer.println();
        }
        InstanceManager.getDefault(ModuleManager.class);
    }
    
    static volatile DefaultModuleManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultModuleManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            LoggingUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultModuleManager();
        }
        return (_instance);
    }

    @Override
    public Class<Module> getNamedBeanClass() {
        return Module.class;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultModuleManager.class);

}
