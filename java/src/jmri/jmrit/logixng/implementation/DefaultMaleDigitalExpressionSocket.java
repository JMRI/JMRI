package jmri.jmrit.logixng.implementation;

import jmri.jmrit.logixng.Category;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
/**
 * Every DigitalExpressionBean has an DefaultMaleDigitalExpressionSocket as its parent.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultMaleDigitalExpressionSocket extends AbstractMaleSocket implements MaleDigitalExpressionSocket {

    private final DigitalExpressionBean _expression;
    private boolean lastEvaluationResult = false;
    private DebugConfig _debugConfig = null;
    private boolean _enabled = true;


    public DefaultMaleDigitalExpressionSocket(@Nonnull BaseManager<? extends NamedBean> manager, @Nonnull DigitalExpressionBean expression) {
        super(manager);
        _expression = expression;
    }

    /** {@inheritDoc} */
    @Override
    public void notifyChangedResult(boolean oldResult, boolean newResult) {
        _expression.notifyChangedResult(oldResult, newResult);
    }
    
    /** {@inheritDoc} */
    @Override
    public final Base getRoot() {
        return _expression.getRoot();
    }
    
    /** {@inheritDoc} */
    @Override
    public Lock getLock() {
        return _expression.getLock();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setLock(Lock lock) {
        _expression.setLock(lock);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean getTriggerOnChange() {
        return _expression.getTriggerOnChange();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTriggerOnChange(boolean triggerOnChange) {
        _expression.setTriggerOnChange(triggerOnChange);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return _expression.getCategory();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return _expression.isExternal();
    }
    
    private void checkChangedLastResult(boolean savedLastResult) {
        if (savedLastResult != lastEvaluationResult) {
            _expression.notifyChangedResult(savedLastResult, lastEvaluationResult);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        boolean saveLastResult = lastEvaluationResult;
        if (! _enabled) {
            lastEvaluationResult = false;
            checkChangedLastResult(saveLastResult);
            return false;
        }
        
        if ((_debugConfig != null)
                && ((DigitalExpressionDebugConfig)_debugConfig)._forceResult) {
            lastEvaluationResult = ((DigitalExpressionDebugConfig)_debugConfig)._result;
            checkChangedLastResult(saveLastResult);
            return lastEvaluationResult;
        }
        
        ConditionalNG conditionalNG = getConditionalNG();
        
        int currentStackPos = conditionalNG.getStack().getCount();
        
        try {
            conditionalNG.getSymbolTable().createSymbols(_localVariables);
            lastEvaluationResult = _expression.evaluate();
        } catch (JmriException e) {
            handleError(this, Bundle.getMessage("ExceptionEvaluateExpression", e), e, log);
            lastEvaluationResult = false;
        } catch (RuntimeException e) {
            handleError(this, Bundle.getMessage("ExceptionEvaluateExpression", e), e, log);
            lastEvaluationResult = false;
        }
        
        conditionalNG.getStack().setCount(currentStackPos);
        conditionalNG.getSymbolTable().removeSymbols(_localVariables);
        
        checkChangedLastResult(saveLastResult);
        return lastEvaluationResult;
    }

    @Override
    public boolean getLastResult() {
        return lastEvaluationResult;
    }

    @Override
    public int getState() {
        return _expression.getState();
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        return _expression.getChild(index);
    }

    @Override
    public int getChildCount() {
        return _expression.getChildCount();
    }

    @Override
    public String getShortDescription(Locale locale) {
        return _expression.getShortDescription(locale);
    }

    @Override
    public String getLongDescription(Locale locale) {
        return _expression.getLongDescription(locale);
    }

    @Override
    public String getUserName() {
        return _expression.getUserName();
    }

    @Override
    public void setUserName(String s) throws BadUserNameException {
        _expression.setUserName(s);
    }

    @Override
    public String getSystemName() {
        return _expression.getSystemName();
    }

    @Override
    public String getDisplayName() {
        return _expression.getDisplayName();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l, String name, String listenerRef) {
        _expression.addPropertyChangeListener(l, name, listenerRef);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l, String name, String listenerRef) {
        _expression.addPropertyChangeListener(propertyName, l, name, listenerRef);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        _expression.addPropertyChangeListener(l);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        _expression.addPropertyChangeListener(propertyName, l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        _expression.removePropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        _expression.removePropertyChangeListener(propertyName, l);
    }

    @Override
    public void updateListenerRef(PropertyChangeListener l, String newName) {
        _expression.updateListenerRef(l, newName);
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        _expression.vetoableChange(evt);
    }

    @Override
    public String getListenerRef(PropertyChangeListener l) {
        return _expression.getListenerRef(l);
    }

    @Override
    public ArrayList<String> getListenerRefs() {
        return _expression.getListenerRefs();
    }

    @Override
    public int getNumPropertyChangeListeners() {
        return _expression.getNumPropertyChangeListeners();
    }

    @Override
    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        return _expression.getPropertyChangeListeners();
    }

    @Override
    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return _expression.getPropertyChangeListeners(propertyName);
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        return _expression.getPropertyChangeListenersByReference(name);
    }

    @Override
    public void disposeMe() {
        _expression.dispose();
    }

    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void registerListenersForThisClass() {
        _expression.registerListeners();
    }
    
    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void unregisterListenersForThisClass() {
        _expression.unregisterListeners();
    }
    
    @Override
    public void setState(int s) throws JmriException {
        _expression.setState(s);
    }

    @Override
    public String describeState(int state) {
        return _expression.describeState(state);
    }

    @Override
    public String getComment() {
        return _expression.getComment();
    }

    @Override
    public void setComment(String comment) {
        _expression.setComment(comment);
    }

    @Override
    public void setProperty(String key, Object value) {
        _expression.setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return _expression.getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        _expression.removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return _expression.getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return _expression.getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return _expression.compareSystemNameSuffix(suffix1, suffix2, n2);
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
        return new DigitalExpressionDebugConfig();
    }

    /** {@inheritDoc} */
    @Override
    public Base getObject() {
        return _expression;
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



    public static class DigitalExpressionDebugConfig implements MaleSocket.DebugConfig {
        
        // If true, the socket is returning the value of "result" instead of
        // executing the expression.
        public boolean _forceResult = false;
        
        // The result if the result is forced.
        public boolean _result = false;
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMaleDigitalExpressionSocket.class);

}
