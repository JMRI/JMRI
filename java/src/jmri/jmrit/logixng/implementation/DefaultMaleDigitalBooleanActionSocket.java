package jmri.jmrit.logixng.implementation;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Every DigitalBooleanActionBean has an DefaultMaleDigitalBooleanActionSocket as its parent.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultMaleDigitalBooleanActionSocket
        extends AbstractMaleSocket implements MaleDigitalBooleanActionSocket {

//    private final DigitalBooleanActionBean ((DigitalBooleanActionBean)getObject());
    private DebugConfig _debugConfig = null;
    private boolean _enabled = true;
    
    
    public DefaultMaleDigitalBooleanActionSocket(@Nonnull BaseManager<? extends NamedBean> manager, @Nonnull DigitalBooleanActionBean action) {
        super(manager, action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(boolean hasChangedToTrue, boolean hasChangedToFalse) throws JmriException {
        if (! _enabled) {
            return;
        }
        
        if ((_debugConfig != null)
                && ((DigitalBooleanActionDebugConfig)_debugConfig)._dontExecute) {
            return;
        }
        
        ConditionalNG conditionalNG = getConditionalNG();
        
        int currentStackPos = conditionalNG.getStack().getCount();
        
        try {
            conditionalNG.getSymbolTable().createSymbols(_localVariables);
            ((DigitalBooleanActionBean)getObject()).execute(hasChangedToTrue, hasChangedToFalse);
        } catch (JmriException e) {
            handleError(this, Bundle.getMessage("ExceptionExecuteBooleanAction", e.getLocalizedMessage()), e, log);
        } catch (RuntimeException e) {
            handleError(this, Bundle.getMessage("ExceptionExecuteBooleanAction", e.getLocalizedMessage()), e, log);
        }
        
        conditionalNG.getStack().setCount(currentStackPos);
        conditionalNG.getSymbolTable().removeSymbols(_localVariables);
    }

    @Override
    public String getDisplayName() {
        return ((DigitalBooleanActionBean)getObject()).getDisplayName();
    }

    @Override
    public void disposeMe() {
        ((DigitalBooleanActionBean)getObject()).dispose();
    }

    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void registerListenersForThisClass() {
        ((DigitalBooleanActionBean)getObject()).registerListeners();
    }
    
    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void unregisterListenersForThisClass() {
        ((DigitalBooleanActionBean)getObject()).unregisterListeners();
    }
    
    @Override
    public void setState(int s) throws JmriException {
        ((DigitalBooleanActionBean)getObject()).setState(s);
    }

    @Override
    public int getState() {
        return ((DigitalBooleanActionBean)getObject()).getState();
    }

    @Override
    public String describeState(int state) {
        return ((DigitalBooleanActionBean)getObject()).describeState(state);
    }

    @Override
    public String getComment() {
        return ((DigitalBooleanActionBean)getObject()).getComment();
    }

    @Override
    public void setComment(String comment) {
        ((DigitalBooleanActionBean)getObject()).setComment(comment);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((DigitalBooleanActionBean)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((DigitalBooleanActionBean)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((DigitalBooleanActionBean)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((DigitalBooleanActionBean)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((DigitalBooleanActionBean)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((DigitalBooleanActionBean)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
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
        return new DigitalBooleanActionDebugConfig();
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
    


    public static class DigitalBooleanActionDebugConfig implements MaleSocket.DebugConfig {
        
        // If true, the socket is not executing the action.
        // It's useful if you want to test the LogixNG without affecting the
        // layout (turnouts, sensors, and so on).
        public boolean _dontExecute = false;
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMaleDigitalBooleanActionSocket.class);

}
