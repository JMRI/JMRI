package jmri.jmrit.logixng.implementation;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Every StringExpressionBean has an DefaultMaleStringExpressionSocket as its parent.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultMaleStringExpressionSocket extends AbstractMaleSocket
        implements MaleStringExpressionSocket {

//    private final StringExpressionBean ((StringExpressionBean)getObject());
    private DebugConfig _debugConfig = null;
    private boolean _enabled = true;


    public DefaultMaleStringExpressionSocket(@Nonnull BaseManager<? extends NamedBean> manager, @Nonnull StringExpressionBean stringExpression) {
        super(manager, stringExpression);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getTriggerOnChange() {
        return ((StringExpressionBean)getObject()).getTriggerOnChange();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTriggerOnChange(boolean triggerOnChange) {
        ((StringExpressionBean)getObject()).setTriggerOnChange(triggerOnChange);
    }
    
    /** {@inheritDoc} */
    @Override
    public String evaluate() throws JmriException {
        if (! _enabled) {
            return "";
        }
        
        if ((_debugConfig != null)
                && ((StringExpressionDebugConfig)_debugConfig)._forceResult) {
            return ((StringExpressionDebugConfig)_debugConfig)._result;
        }
        
        ConditionalNG currentConditionalNG = getConditionalNG();
        
        int currentStackPos = currentConditionalNG.getStack().getCount();
        
        String result = "";
        try {
            currentConditionalNG.getSymbolTable().createSymbols(_localVariables);
            result = ((StringExpressionBean)getObject()).evaluate();
        } catch (JmriException e) {
            if (e.getErrors() != null) {
                handleError(this, Bundle.getMessage("ExceptionEvaluateMulti"), e.getErrors(), e, log);
            } else {
                handleError(this, Bundle.getMessage("ExceptionEvaluate", e.getLocalizedMessage()), e, log);
            }
        } catch (RuntimeException e) {
            handleError(this, Bundle.getMessage("ExceptionEvaluate", e.getLocalizedMessage()), e, log);
        }
        
        currentConditionalNG.getStack().setCount(currentStackPos);
        currentConditionalNG.getSymbolTable().removeSymbols(_localVariables);
        
        return result;
    }
    
    @Override
    public int getState() {
        return ((StringExpressionBean)getObject()).getState();
    }

    @Override
    public String getDisplayName() {
        return ((StringExpressionBean)getObject()).getDisplayName();
    }

    @Override
    public void disposeMe() {
        ((StringExpressionBean)getObject()).dispose();
    }

    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void registerListenersForThisClass() {
        ((StringExpressionBean)getObject()).registerListeners();
    }
    
    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void unregisterListenersForThisClass() {
        ((StringExpressionBean)getObject()).unregisterListeners();
    }
    
    @Override
    public void setState(int s) throws JmriException {
        ((StringExpressionBean)getObject()).setState(s);
    }

    @Override
    public String describeState(int state) {
        return Bundle.getMessage("BeanStateUnknown");
    }

    @Override
    public String getComment() {
        return ((StringExpressionBean)getObject()).getComment();
    }

    @Override
    public void setComment(String comment) {
        ((StringExpressionBean)getObject()).setComment(comment);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((StringExpressionBean)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((StringExpressionBean)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((StringExpressionBean)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((StringExpressionBean)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((StringExpressionBean)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((StringExpressionBean)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
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
        return new StringExpressionDebugConfig();
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



    public static class StringExpressionDebugConfig implements MaleSocket.DebugConfig {
        
        // If true, the socket is returning the value of "result" instead of
        // executing the expression.
        public boolean _forceResult = false;
        
        // The result if the result is forced.
        public String _result = "";
        
        @Override
        public DebugConfig getCopy() {
            StringExpressionDebugConfig config = new StringExpressionDebugConfig();
            config._forceResult = _forceResult;
            config._result = _result;
            return config;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMaleStringExpressionSocket.class);

}
