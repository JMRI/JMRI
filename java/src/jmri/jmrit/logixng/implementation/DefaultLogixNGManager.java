package jmri.jmrit.logixng.implementation;

import java.io.PrintWriter;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Base.PrintTreeSettings;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.managers.AbstractManager;
import jmri.util.LoggingUtil;
import jmri.util.ThreadingUtil;

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Class providing the basic logic of the LogixNG_Manager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultLogixNGManager extends AbstractManager<LogixNG>
        implements LogixNG_Manager {

    
    private final Map<String, Manager<? extends MaleSocket>> _managers = new HashMap<>();
    private final Clipboard _clipboard = new DefaultClipboard();
    private boolean _isActive = false;
    
    
    public DefaultLogixNGManager() {
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
     * Method to create a new LogixNG if the LogixNG does not exist.
     * <p>
     * Returns null if
     * a Logix with the same systemName or userName already exists, or if there
     * is trouble creating a new LogixNG.
     */
    @Override
    public LogixNG createLogixNG(String systemName, String userName)
            throws IllegalArgumentException {
        
        // Check that LogixNG does not already exist
        LogixNG x;
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
        // LogixNG does not exist, create a new LogixNG
        x = new DefaultLogixNG(systemName, userName);
        // save in the maps
        register(x);
        
        // Keep track of the last created auto system name
        updateAutoNumber(systemName);
        
        return x;
    }

    @Override
    public LogixNG createLogixNG(String userName) throws IllegalArgumentException {
        return createLogixNG(getAutoSystemName(), userName);
    }
    
    @Override
    public LogixNG getLogixNG(String name) {
        LogixNG x = getByUserName(name);
        if (x != null) {
            return x;
        }
        return getBySystemName(name);
    }

    @Override
    public LogixNG getByUserName(String name) {
        return _tuser.get(name);
    }

    @Override
    public LogixNG getBySystemName(String name) {
        return _tsys.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameLogixNGs" : "BeanNameLogixNG");
    }

    /** {@inheritDoc} */
    @Override
    public void resolveAllTrees() {
        for (LogixNG logixNG : _tsys.values()) {
            logixNG.setParentForAllChildren();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setupAllLogixNGs() {
        for (LogixNG logixNG : _tsys.values()) {
            logixNG.setup();
            logixNG.setParentForAllChildren();
        }
        for (Module module : InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet()) {
            module.setup();
            module.setParentForAllChildren();
        }
        _clipboard.setup();
    }

    /**
     * Set whenether execute() should run on the LogixNG thread at once or
     * should dispatch the call until later, for all the ConditionalNGs.
     * 
     * @param value true if execute() should run on LogixNG thread delayed,
     *              false otherwise.
     */
    private void setRunDelayed(boolean value) {
        Set<ConditionalNG> conditionalNGs =
                InstanceManager.getDefault(ConditionalNG_Manager.class).getNamedBeanSet();
        for (ConditionalNG conditionalNG : conditionalNGs) {
            conditionalNG.setRunDelayed(value);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void activateAllLogixNGs() {
        activateAllLogixNGs(true, true);
    }
    
    /** {@inheritDoc} */
    @Override
    public void activateAllLogixNGs(boolean runDelayed, boolean runOnSeparateThread) {
        
        setRunDelayed(false);
        
        _isActive = true;
        
        // This may take a long time so it must not be done on the GUI thread.
        // Therefore we create a new thread for this task.
        Runnable runnable = () -> {
            Set<LogixNG> activeLogixNGs = new HashSet<>();
            
            // Activate and execute the initialization LogixNGs first.
            List<LogixNG> initLogixNGs =
                    InstanceManager.getDefault(LogixNG_InitializationManager.class)
                            .getList();
            
            for (LogixNG logixNG : initLogixNGs) {
                if (logixNG.isActive()) {
                    logixNG.registerListeners();
                    logixNG.execute();
                    activeLogixNGs.add(logixNG);
                } else {
                    logixNG.unregisterListeners();
                }
            }
            
            // Activate and execute all the rest of the LogixNGs.
            _tsys.values().stream()
                    .sorted()
                    .filter((logixNG) -> !(activeLogixNGs.contains(logixNG)))
                    .forEachOrdered((logixNG) -> {
                
                if (logixNG.isActive()) {
                    logixNG.registerListeners();
                    logixNG.execute();
                } else {
                    logixNG.unregisterListeners();
                }
            });
            
            setRunDelayed(runDelayed);
        };
        
        if (runOnSeparateThread) new Thread(runnable).start();
        else runnable.run();
    }

    /** {@inheritDoc} */
    @Override
    public void deActivateAllLogixNGs() {
        for (LogixNG logixNG : _tsys.values()) {
            logixNG.unregisterListeners();
        }
        _isActive = false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return _isActive;
    }

    /** {@inheritDoc} */
    @Override
    public void deleteLogixNG(LogixNG x) {
        // delete the LogixNG
        deregister(x);
        x.dispose();
    }

    /** {@inheritDoc} */
    @Override
    public void setLoadDisabled(boolean s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {
        
        printTree(settings, Locale.getDefault(), writer, indent, lineNumber);
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(
            PrintTreeSettings settings,
            Locale locale,
            PrintWriter writer,
            String indent,
            MutableInt lineNumber) {
        
        for (LogixNG logixNG : getNamedBeanSet()) {
            logixNG.printTree(settings, locale, writer, indent, "", lineNumber);
            writer.println();
        }
        InstanceManager.getDefault(ModuleManager.class).printTree(settings, locale, writer, indent, lineNumber);
        InstanceManager.getDefault(NamedTableManager.class).printTree(locale, writer, indent);
    }
    
    
    static volatile DefaultLogixNGManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultLogixNGManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            LoggingUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultLogixNGManager();
        }
        return (_instance);
    }

    /** {@inheritDoc} */
    @Override
    public Class<LogixNG> getNamedBeanClass() {
        return LogixNG.class;
    }
    
    /** {@inheritDoc} */
    @Override
    public Clipboard getClipboard() {
        return _clipboard;
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerManager(Manager<? extends MaleSocket> manager) {
        _managers.put(manager.getClass().getName(), manager);
    }
    
    /** {@inheritDoc} */
    @Override
    public Manager<? extends MaleSocket> getManager(String className) {
        return _managers.get(className);
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultLogixNGManager.class);

}
