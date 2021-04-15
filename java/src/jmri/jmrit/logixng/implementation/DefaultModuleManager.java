package jmri.jmrit.logixng.implementation;

import java.io.PrintWriter;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Base.PrintTreeSettings;
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

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return LOGIXNGS;
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        return 'Q';
    }

    /** {@inheritDoc} */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return LogixNG_Manager.validSystemNameFormat(
                getSubSystemNamePrefix(), systemName);
    }

    /** {@inheritDoc} */
    @Override
    public Module createModule(String systemName, String userName,
            FemaleSocketManager.SocketType socketType)
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
        x = new DefaultModule(systemName, userName, socketType);
        // save in the maps
        register(x);
        
        // Keep track of the last created auto system name
        updateAutoNumber(systemName);
        
        return x;
    }

    /** {@inheritDoc} */
    @Override
    public Module createModule(String userName, FemaleSocketManager.SocketType socketType) throws IllegalArgumentException {
        return createModule(getAutoSystemName(), userName, socketType);
    }
    
    /** {@inheritDoc} */
    @Override
    public Module getModule(String name) {
        Module x = getByUserName(name);
        if (x != null) {
            return x;
        }
        return getBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    public Module getByUserName(String name) {
        return _tuser.get(name);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public void deleteModule(Module x) {
        // delete the Module
        deregister(x);
        x.dispose();
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(PrintTreeSettings settings, PrintWriter writer, String indent) {
        printTree(settings, Locale.getDefault(), writer, indent);
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(PrintTreeSettings settings, Locale locale, PrintWriter writer, String indent) {
        for (Module module : getNamedBeanSet()) {
            module.printTree(settings, locale, writer, indent, "");
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

    /** {@inheritDoc} */
    @Override
    public Class<Module> getNamedBeanClass() {
        return Module.class;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultModuleManager.class);

}
