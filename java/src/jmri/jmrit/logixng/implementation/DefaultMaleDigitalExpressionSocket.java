package jmri.jmrit.logixng.implementation;

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

    private boolean lastEvaluationResult = false;
    private DebugConfig _debugConfig = null;
    private boolean _enabled = true;


    public DefaultMaleDigitalExpressionSocket(@Nonnull BaseManager<? extends NamedBean> manager, @Nonnull DigitalExpressionBean expression) {
        super(manager, expression);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyChangedResult(boolean oldResult, boolean newResult) {
        ((DigitalExpressionBean)getObject()).notifyChangedResult(oldResult, newResult);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean getTriggerOnChange() {
        return ((DigitalExpressionBean)getObject()).getTriggerOnChange();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTriggerOnChange(boolean triggerOnChange) {
        ((DigitalExpressionBean)getObject()).setTriggerOnChange(triggerOnChange);
    }
    
    private void checkChangedLastResult(boolean savedLastResult) {
        if (savedLastResult != lastEvaluationResult) {
            ((DigitalExpressionBean)getObject())
                    .notifyChangedResult(savedLastResult, lastEvaluationResult);
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
            lastEvaluationResult = ((DigitalExpressionBean)getObject()).evaluate();
        } catch (JmriMultiLineException e) {
            handleError(this, Bundle.getMessage("ExceptionMulti"), e.getErrors(), e, log);
            lastEvaluationResult = false;
        } catch (JmriException e) {
            handleError(this, Bundle.getMessage("ExceptionEvaluateExpression", e.getLocalizedMessage()), e, log);
            lastEvaluationResult = false;
        } catch (RuntimeException e) {
            handleError(this, Bundle.getMessage("ExceptionEvaluateExpression", e.getLocalizedMessage()), e, log);
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
        return ((DigitalExpressionBean)getObject()).getState();
    }

    @Override
    public void setState(int s) throws JmriException {
        ((DigitalExpressionBean)getObject()).setState(s);
    }

    @Override
    public String describeState(int state) {
        return ((DigitalExpressionBean)getObject()).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((DigitalExpressionBean)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((DigitalExpressionBean)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((DigitalExpressionBean)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((DigitalExpressionBean)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((DigitalExpressionBean)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((DigitalExpressionBean)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
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

    @Override
    public void disposeMe() {
        ((DigitalExpressionBean)getObject()).dispose();
    }

    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void registerListenersForThisClass() {
        ((DigitalExpressionBean)getObject()).registerListeners();
    }
    
    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void unregisterListenersForThisClass() {
        ((DigitalExpressionBean)getObject()).unregisterListeners();
    }
    
    @Override
    public String getDisplayName() {
        return ((DigitalExpressionBean)getObject()).getDisplayName();
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
