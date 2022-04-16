package jmri.jmrit.logixng.implementation;

import java.beans.*;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

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
 * @author Dave Sand          Copyright (C) 2021
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
    public ConditionalNG createConditionalNG(LogixNG logixNG, String systemName, String userName)
            throws IllegalArgumentException {

        return createConditionalNG(logixNG, systemName, userName, LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG createConditionalNG(
            LogixNG logixNG, String systemName, String userName, int threadID)
            throws IllegalArgumentException {

        // Check that ConditionalNG does not already exist
        ConditionalNG x;
        if (userName != null && !userName.isEmpty()) {
            x = getByUserName(logixNG, userName);
            if (x != null) {
                log.error("username '{}' already exists, conditionalNG is '{}'", userName, x.getDisplayName());
                return null;
            }
        }

        x = getBySystemName(systemName);
        if (x != null) {
            log.error("systemname '{}' already exists, conditionalNG is '{}'", systemName, x.getDisplayName());
            return null;
        }

        // Check if system name is valid
        if (this.validSystemNameFormat(systemName) != NameValidity.VALID) {
            throw new IllegalArgumentException("SystemName '" + systemName + "' is not in the correct format");
        }

        // ConditionalNG does not exist, create a new ConditionalNG
        x = new DefaultConditionalNG(systemName, userName, threadID);

        // Add the conditional to the LogixNG map
        logixNG.addConditionalNG(x);

        // Keep track of the last created auto system name
        updateAutoNumber(systemName);

        return x;
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG createConditionalNG(LogixNG logixNG, String userName) throws IllegalArgumentException {
        return createConditionalNG(logixNG, getAutoSystemName(), userName);
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG createConditionalNG(LogixNG logixNG, String userName, int threadID) throws IllegalArgumentException {
        return createConditionalNG(logixNG, getAutoSystemName(), userName, threadID);
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG getConditionalNG(LogixNG logixNG, String name) {
        if (logixNG != null) {
            ConditionalNG x = getByUserName(logixNG, name);
            if (x != null) {
                return x;
            }
        }
        return getBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG getParentLogixNG(String systemName) {
        if (systemName == null || systemName.isEmpty()) {
            return null;
        }

        for (LogixNG logixNG : InstanceManager.getDefault(LogixNG_Manager.class).getNamedBeanSet()) {
            for (int i = 0; i < logixNG.getNumConditionalNGs(); i++) {
                if (systemName.equals(logixNG.getConditionalNG_SystemName(i))) {
                    return logixNG;
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG getByUserName(LogixNG logixNG, String name) {
        if (logixNG != null && name != null && !name.isEmpty()) {
            for (int i = 0; i < logixNG.getNumConditionalNGs(); i++) {
                ConditionalNG conditionalNG = logixNG.getConditionalNG(i);
                if (conditionalNG != null) {
                    if (name.equals(conditionalNG.getUserName())) {
                        return conditionalNG;
                    }
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG getBySystemName(String name) {
        LogixNG logixNG = getParentLogixNG(name);
        if (logixNG != null) {
            for (int i = 0; i < logixNG.getNumConditionalNGs(); i++) {
                if (name.equals(logixNG.getConditionalNG_SystemName(i))) {
                    return logixNG.getConditionalNG(i);
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameConditionalNGs" : "BeanNameConditionalNG");
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
        InstanceManager.getDefault(LogixNG_Manager.class).getNamedBeanSet().forEach(logixNG -> {
            for (int i = 0; i < logixNG.getNumConditionalNGs(); i++) {
                if (logixNG.getConditionalNG(i) != null) {
                    logixNG.getConditionalNG(i).setRunDelayed(false);
                }
            }
        });
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
    public final void deleteBean(@Nonnull ConditionalNG conditionalNG, @Nonnull String property) throws PropertyVetoException {
        for (int i=0; i < conditionalNG.getChildCount(); i++) {
            FemaleSocket child = conditionalNG.getChild(i);
            if (child.isConnected()) {
                MaleSocket maleSocket = child.getConnectedSocket();
                maleSocket.getManager().deleteBean(maleSocket, property);
            }
        }
        
        // throws PropertyVetoException if vetoed
        fireVetoableChange(property, conditionalNG);
        if (property.equals("DoDelete")) { // NOI18N
            if (conditionalNG.getLogixNG() != null) {
                conditionalNG.getLogixNG().deleteConditionalNG(conditionalNG);
            }
            conditionalNG.dispose();
        }
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultConditionalNGManager.class);
}
