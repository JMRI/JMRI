package jmri.jmrit.logixng.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Every AnalogActionBean has an DefaultMaleAnalogActionSocket as its parent.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultMaleAnalogActionSocket extends AbstractMaleSocket implements MaleAnalogActionSocket {

    private final AnalogActionBean _action;
    private DebugConfig _debugConfig = null;
    private boolean _enabled = true;
    
    
    public DefaultMaleAnalogActionSocket(@Nonnull BaseManager<? extends NamedBean> manager, @Nonnull AnalogActionBean action) {
        super(manager);
        _action = action;
    }
    
    /** {@inheritDoc} */
    @Override
    public final Base getRoot() {
        return _action.getRoot();
    }
    
    /** {@inheritDoc} */
    @Override
    public Lock getLock() {
        return _action.getLock();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setLock(Lock lock) {
        _action.setLock(lock);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return _action.getCategory();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return _action.isExternal();
    }
    
    /**
     * Set the value of the AnalogActionBean.
     */
    private void internalSetValue(double value) throws JmriException {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("The value is NaN");
        }
        if (value == Double.NEGATIVE_INFINITY) {
            throw new IllegalArgumentException("The value is negative infinity");
        }
        if (value == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("The value is positive infinity");
        }
        _action.setValue(value);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setValue(double value) throws JmriException {
        if (! _enabled) {
            return;
        }
        
        if ((_debugConfig != null)
                && ((AnalogActionDebugConfig)_debugConfig)._dontExecute) {
            return;
        }
        
        ConditionalNG conditionalNG = getConditionalNG();
        
        int currentStackPos = conditionalNG.getStack().getCount();
        
        try {
            conditionalNG.getSymbolTable().createSymbols(_localVariables);
            internalSetValue(value);
        } catch (JmriException e) {
            handleError(this, Bundle.getMessage("ExceptionSetValue", e.getLocalizedMessage()), e, log);
        } catch (RuntimeException e) {
            handleError(this, Bundle.getMessage("ExceptionSetValue", e.getLocalizedMessage()), e, log);
        }
        
        conditionalNG.getStack().setCount(currentStackPos);
        conditionalNG.getSymbolTable().removeSymbols(_localVariables);
    }

    @Override
    public String getShortDescription(Locale locale) {
        return _action.getShortDescription(locale);
    }

    @Override
    public String getLongDescription(Locale locale) {
        return _action.getLongDescription(locale);
    }

    @Override
    public FemaleSocket getChild(int index)
            throws IllegalArgumentException, UnsupportedOperationException {
        return _action.getChild(index);
    }

    @Override
    public int getChildCount() {
        return _action.getChildCount();
    }

    @Override
    public String getUserName() {
        return _action.getUserName();
    }

    @Override
    public void setUserName(String s) throws BadUserNameException {
        _action.setUserName(s);
    }

    @Override
    public String getSystemName() {
        return _action.getSystemName();
    }

    @Override
    public String getDisplayName() {
        return _action.getDisplayName();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l, String name, String listenerRef) {
        _action.addPropertyChangeListener(l, name, listenerRef);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l, String name, String listenerRef) {
        _action.addPropertyChangeListener(propertyName, l, name, listenerRef);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        _action.addPropertyChangeListener(l);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        _action.addPropertyChangeListener(propertyName, l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        _action.removePropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        _action.removePropertyChangeListener(propertyName, l);
    }

    @Override
    public void updateListenerRef(PropertyChangeListener l, String newName) {
        _action.updateListenerRef(l, newName);
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        _action.vetoableChange(evt);
    }

    @Override
    public String getListenerRef(PropertyChangeListener l) {
        return _action.getListenerRef(l);
    }

    @Override
    public ArrayList<String> getListenerRefs() {
        return _action.getListenerRefs();
    }

    @Override
    public int getNumPropertyChangeListeners() {
        return _action.getNumPropertyChangeListeners();
    }

    @Override
    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        return _action.getPropertyChangeListeners();
    }

    @Override
    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return _action.getPropertyChangeListeners(propertyName);
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        return _action.getPropertyChangeListenersByReference(name);
    }

    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void registerListenersForThisClass() {
        _action.registerListeners();
    }
    
    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void unregisterListenersForThisClass() {
        _action.unregisterListeners();
    }
    
    @Override
    public void setState(int s) throws JmriException {
        _action.setState(s);
    }

    @Override
    public int getState() {
        return _action.getState();
    }

    @Override
    public String describeState(int state) {
        return _action.describeState(state);
    }

    @Override
    public String getComment() {
        return _action.getComment();
    }

    @Override
    public void setComment(String comment) {
        _action.setComment(comment);
    }

    @Override
    public void setProperty(String key, Object value) {
        _action.setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return _action.getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        _action.removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return _action.getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return _action.getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return _action.compareSystemNameSuffix(suffix1, suffix2, n2);
    }

    /** {@inheritDoc} */
    @Override
    public void setDebugConfig(DebugConfig config) {
        _debugConfig = config;
    }

    /** {@inheritDoc} */
    @Override
    public DebugConfig getDebugConfig() {
        return _debugConfig;
    }

    /** {@inheritDoc} */
    @Override
    public DebugConfig createDebugConfig() {
        return new AnalogActionDebugConfig();
    }

    /** {@inheritDoc} */
    @Override
    public Base getObject() {
        return _action;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setEnabled(boolean enable) {
        _enabled = enable;
        if (isActive()) {
            registerListeners();
        } else {
            unregisterListeners();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setEnabledFlag(boolean enable) {
        _enabled = enable;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
        return _enabled;
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    public static class AnalogActionDebugConfig implements MaleSocket.DebugConfig {
        
        // If true, the socket is not executing the action.
        // It's useful if you want to test the LogixNG without affecting the
        // layout (turnouts, sensors, and so on).
        public boolean _dontExecute = false;
        
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMaleAnalogActionSocket.class);

}
