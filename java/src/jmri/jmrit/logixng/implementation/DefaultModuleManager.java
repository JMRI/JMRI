package jmri.jmrit.logixng.implementation;

import java.beans.*;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Base.PrintTreeSettings;
import jmri.jmrit.logixng.Module;
import jmri.managers.AbstractManager;
import jmri.util.*;

import org.apache.commons.lang3.mutable.MutableInt;


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
    public boolean resolveAllTrees(List<String> errors) {
        boolean result = true;
        for (Module logixNG_Module : _tsys.values()) {
            result = result && logixNG_Module.setParentForAllChildren(errors);
        }
        return result;
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
        
        for (Module module : getNamedBeanSet()) {
            module.printTree(settings, locale, writer, indent, "", lineNumber);
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

    /**
     * Inform all registered listeners of a vetoable change.If the propertyName
     * is "CanDelete" ALL listeners with an interest in the bean will throw an
     * exception, which is recorded returned back to the invoking method, so
     * that it can be presented back to the user.However if a listener decides
     * that the bean can not be deleted then it should throw an exception with
     * a property name of "DoNotDelete", this is thrown back up to the user and
     * the delete process should be aborted.
     *
     * @param p   The programmatic name of the property that is to be changed.
     *            "CanDelete" will inquire with all listeners if the item can
     *            be deleted. "DoDelete" tells the listener to delete the item.
     * @param old The old value of the property.
     * @throws java.beans.PropertyVetoException If the recipients wishes the
     *                                          delete to be aborted (see above)
     */
    @OverridingMethodsMustInvokeSuper
    public void fireVetoableChange(String p, Object old) throws PropertyVetoException {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, p, old, null);
        for (VetoableChangeListener vc : vetoableChangeSupport.getVetoableChangeListeners()) {
            vc.vetoableChange(evt);
        }
    }
    
    /** {@inheritDoc} */
    @Override
//    @OverridingMethodsMustInvokeSuper
    public final void deleteBean(@Nonnull Module module, @Nonnull String property) throws PropertyVetoException {
        for (int i=0; i < module.getChildCount(); i++) {
            FemaleSocket child = module.getChild(i);
            if (child.isConnected()) {
                MaleSocket maleSocket = child.getConnectedSocket();
                maleSocket.getManager().deleteBean(maleSocket, property);
            }
        }
        
        // throws PropertyVetoException if vetoed
        fireVetoableChange(property, module);
        if (property.equals("DoDelete")) { // NOI18N
            deregister(module);
            module.dispose();
        }
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultModuleManager.class);

}
