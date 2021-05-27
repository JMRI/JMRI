package jmri.jmrit.logixng.implementation;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Every DigitalActionBean has an DefaultMaleDigitalActionSocket as its parent.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultMaleDigitalActionSocket
        extends AbstractMaleSocket implements MaleDigitalActionSocket {

//    private final DigitalActionBean ((DigitalActionBean)getObject());
    private DebugConfig _debugConfig = null;
    private boolean _enabled = true;
    
    
    public DefaultMaleDigitalActionSocket(@Nonnull BaseManager<? extends NamedBean> manager, @Nonnull DigitalActionBean action) {
        super(manager, action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        if (! _enabled) {
            return;
        }
        
        if ((_debugConfig != null)
                && ((DigitalActionDebugConfig)_debugConfig)._dontExecute) {
            return;
        }
        
        ConditionalNG conditionalNG = getConditionalNG();
        
        int currentStackPos = conditionalNG.getStack().getCount();
        
        try {
            conditionalNG.getSymbolTable().createSymbols(_localVariables);
            ((DigitalActionBean)getObject()).execute();
        } catch (JmriException e) {
            if (e.getErrors() != null) {
                handleError(this, Bundle.getMessage("ExceptionExecuteMulti"), e.getErrors(), e, log);
            } else {
                handleError(this, Bundle.getMessage("ExceptionExecuteAction", e.getLocalizedMessage()), e, log);
            }
        } catch (RuntimeException e) {
            handleError(this, Bundle.getMessage("ExceptionExecuteAction", e.getLocalizedMessage()), e, log);
        }

        conditionalNG.getStack().setCount(currentStackPos);
        conditionalNG.getSymbolTable().removeSymbols(_localVariables);
    }

    @Override
    public void disposeMe() {
        ((DigitalActionBean)getObject()).dispose();
    }

    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void registerListenersForThisClass() {
        ((DigitalActionBean)getObject()).registerListeners();
    }
    
    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void unregisterListenersForThisClass() {
        ((DigitalActionBean)getObject()).unregisterListeners();
    }
    
    @Override
    public void setState(int s) throws JmriException {
        ((DigitalActionBean)getObject()).setState(s);
    }

    @Override
    public int getState() {
        return ((DigitalActionBean)getObject()).getState();
    }

    @Override
    public String describeState(int state) {
        return ((DigitalActionBean)getObject()).describeState(state);
    }

    @Override
    public String getComment() {
        return ((DigitalActionBean)getObject()).getComment();
    }

    @Override
    public void setComment(String comment) {
        ((DigitalActionBean)getObject()).setComment(comment);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((DigitalActionBean)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((DigitalActionBean)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((DigitalActionBean)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((DigitalActionBean)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((DigitalActionBean)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((DigitalActionBean)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
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
        return new DigitalActionDebugConfig();
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
    
    
    public static class DigitalActionDebugConfig implements MaleSocket.DebugConfig {
        
        // If true, the socket is not executing the action.
        // It's useful if you want to test the LogixNG without affecting the
        // layout (turnouts, sensors, and so on).
        public boolean _dontExecute = false;
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMaleDigitalActionSocket.class);

}
