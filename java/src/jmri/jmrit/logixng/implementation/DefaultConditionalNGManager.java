package jmri.jmrit.logixng.implementation;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.managers.AbstractManager;
import jmri.util.*;

/**
 * Class providing the basic logic of the ConditionalNG_Manager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultConditionalNGManager extends AbstractManager<ConditionalNG>
        implements ConditionalNG_Manager {

    
    public DefaultConditionalNGManager() {
        // LogixNGPreferences class may load plugins so we must ensure
        // it's loaded here.
        InstanceManager.getDefault(LogixNGPreferences.class);
    }
    
    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return LOGIXNG_CONDITIONALNGS;
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
    public ConditionalNG createConditionalNG(String systemName, String userName)
            throws IllegalArgumentException {
        
        return createConditionalNG(systemName, userName, LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
    }
    
    /** {@inheritDoc} */
    @Override
    public ConditionalNG createConditionalNG(
            String systemName, String userName, int threadID)
            throws IllegalArgumentException {
        
        // Check that ConditionalNG does not already exist
        ConditionalNG x;
        if (userName != null && !userName.equals("")) {
            x = getByUserName(userName);
            if (x != null) {
                log.error("username {} already exists", userName);
                return null;
            }
        }
        x = getBySystemName(systemName);
        if (x != null) {
            log.error("systemname {} already exists", systemName);
            return null;
        }
        // Check if system name is valid
        if (this.validSystemNameFormat(systemName) != NameValidity.VALID) {
            throw new IllegalArgumentException("SystemName " + systemName + " is not in the correct format");
        }
        // ConditionalNG does not exist, create a new ConditionalNG
        x = new DefaultConditionalNG(systemName, userName, threadID);
        // save in the maps
        register(x);
        
        // Keep track of the last created auto system name
        updateAutoNumber(systemName);
        
        return x;
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG createConditionalNG(String userName) throws IllegalArgumentException {
        return createConditionalNG(getAutoSystemName(), userName);
    }
    
    /** {@inheritDoc} */
    @Override
    public ConditionalNG createConditionalNG(String userName, int threadID) throws IllegalArgumentException {
        return createConditionalNG(getAutoSystemName(), userName, threadID);
    }
    
    /** {@inheritDoc} */
    @Override
    public ConditionalNG getConditionalNG(String name) {
        ConditionalNG x = getByUserName(name);
        if (x != null) {
            return x;
        }
        return getBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG getByUserName(String name) {
        return _tuser.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG getBySystemName(String name) {
        return _tsys.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameConditionalNGs" : "BeanNameConditionalNG");
    }

    /** {@inheritDoc} */
    @Override
    public void resolveAllTrees() {
        for (ConditionalNG conditionalNG : _tsys.values()) {
            conditionalNG.setParentForAllChildren();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setupAllConditionalNGs() {
        for (ConditionalNG conditionalNG : _tsys.values()) {
            conditionalNG.setup();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteConditionalNG(ConditionalNG x) {
        // delete the ConditionalNG
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
    public void setRunOnGUIDelayed(boolean value) {
        for (ConditionalNG conditionalNG : getNamedBeanSet()) {
            conditionalNG.setRunDelayed(false);
        }
    }
    
    static volatile DefaultConditionalNGManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultConditionalNGManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            LoggingUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultConditionalNGManager();
        }
        return (_instance);
    }

    /** {@inheritDoc} */
    @Override
    public Class<ConditionalNG> getNamedBeanClass() {
        return ConditionalNG.class;
    }
    

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultConditionalNGManager.class);
}
