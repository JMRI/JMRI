package jmri.jmrit.logixng.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;

import javax.annotation.Nonnull;

/**
 * Every StringActionBean has an DefaultMaleStringActionSocket as its parent.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultMaleStringActionSocket extends AbstractMaleSocket implements MaleStringActionSocket {

//    private final StringActionBean ((StringActionBean)getObject());
    private DebugConfig _debugConfig = null;
    private boolean _enabled = true;
    
    
    public DefaultMaleStringActionSocket(@Nonnull BaseManager<? extends NamedBean> manager, @Nonnull StringActionBean stringAction) {
        super(manager, stringAction);
    }
    
    /** {@inheritDoc} */
    @Override
    /**
     * Set a string value.
     */
    public void setValue(String value) throws JmriException {
        if (! _enabled) {
            return;
        }
        
        if ((_debugConfig != null)
                && ((StringActionDebugConfig)_debugConfig)._dontExecute) {
            return;
        }
        
        ConditionalNG conditionalNG = getConditionalNG();
        
        int currentStackPos = conditionalNG.getStack().getCount();
        
        try {
            conditionalNG.getSymbolTable().createSymbols(_localVariables);
            ((StringActionBean)getObject()).setValue(value);
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
        return ((StringActionBean)getObject()).getDisplayName();
    }

    @Override
    public void disposeMe() {
        ((StringActionBean)getObject()).dispose();
    }

    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void registerListenersForThisClass() {
        ((StringActionBean)getObject()).registerListeners();
    }
    
    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void unregisterListenersForThisClass() {
        ((StringActionBean)getObject()).unregisterListeners();
    }
    
    @Override
    public void setState(int s) throws JmriException {
        ((StringActionBean)getObject()).setState(s);
    }

    @Override
    public int getState() {
        return ((StringActionBean)getObject()).getState();
    }

    @Override
    public String describeState(int state) {
        return ((StringActionBean)getObject()).describeState(state);
    }

    @Override
    public String getComment() {
        return ((StringActionBean)getObject()).getComment();
    }

    @Override
    public void setComment(String comment) {
        ((StringActionBean)getObject()).setComment(comment);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((StringActionBean)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((StringActionBean)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((StringActionBean)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((StringActionBean)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((StringActionBean)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((StringActionBean)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
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
        return new StringActionDebugConfig();
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



    public static class StringActionDebugConfig implements MaleSocket.DebugConfig {
        
        // If true, the socket is not executing the action.
        // It's useful if you want to test the LogixNG without affecting the
        // layout (turnouts, sensors, and so on).
        public boolean _dontExecute = false;
        
        @Override
        public DebugConfig getCopy() {
            StringActionDebugConfig config = new StringActionDebugConfig();
            config._dontExecute = _dontExecute;
            return config;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMaleStringActionSocket.class);

}
