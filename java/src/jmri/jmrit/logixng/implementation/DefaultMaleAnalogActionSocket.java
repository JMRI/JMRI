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

//    private final AnalogActionBean ((AnalogActionBean)getObject());
    private DebugConfig _debugConfig = null;
    private boolean _enabled = true;
    
    
    public DefaultMaleAnalogActionSocket(@Nonnull BaseManager<? extends NamedBean> manager, @Nonnull AnalogActionBean action) {
        super(manager, action);
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
        ((AnalogActionBean)getObject()).setValue(value);
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
            if (e.getErrors() != null) {
                handleError(this, Bundle.getMessage("ExceptionExecuteMulti"), e.getErrors(), e, log);
            } else {
                handleError(this, Bundle.getMessage("ExceptionSetValue", e.getLocalizedMessage()), e, log);
            }
        } catch (RuntimeException e) {
            handleError(this, Bundle.getMessage("ExceptionSetValue", e.getLocalizedMessage()), e, log);
        }
        
        conditionalNG.getStack().setCount(currentStackPos);
        conditionalNG.getSymbolTable().removeSymbols(_localVariables);
    }

    @Override
    public String getDisplayName() {
        return ((AnalogActionBean)getObject()).getDisplayName();
    }

    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void registerListenersForThisClass() {
        ((AnalogActionBean)getObject()).registerListeners();
    }
    
    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void unregisterListenersForThisClass() {
        ((AnalogActionBean)getObject()).unregisterListeners();
    }
    
    @Override
    public void setState(int s) throws JmriException {
        ((AnalogActionBean)getObject()).setState(s);
    }

    @Override
    public int getState() {
        return ((AnalogActionBean)getObject()).getState();
    }

    @Override
    public String describeState(int state) {
        return ((AnalogActionBean)getObject()).describeState(state);
    }

    @Override
    public String getComment() {
        return ((AnalogActionBean)getObject()).getComment();
    }

    @Override
    public void setComment(String comment) {
        ((AnalogActionBean)getObject()).setComment(comment);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((AnalogActionBean)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((AnalogActionBean)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((AnalogActionBean)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((AnalogActionBean)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((AnalogActionBean)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((AnalogActionBean)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
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
        
        @Override
        public DebugConfig getCopy() {
            AnalogActionDebugConfig config = new AnalogActionDebugConfig();
            config._dontExecute = _dontExecute;
            return config;
        }
        
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMaleAnalogActionSocket.class);

}
