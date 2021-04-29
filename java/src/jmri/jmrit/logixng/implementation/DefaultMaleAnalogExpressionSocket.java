package jmri.jmrit.logixng.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Every AnalogExpressionBean has an DefaultMaleAnalogExpressionSocket as its parent.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultMaleAnalogExpressionSocket extends AbstractMaleSocket implements MaleAnalogExpressionSocket {

//    private final AnalogExpressionBean ((AnalogExpressionBean)getObject());
    private DebugConfig _debugConfig = null;
    private boolean _enabled = true;


    public DefaultMaleAnalogExpressionSocket(@Nonnull BaseManager<? extends NamedBean> manager, @Nonnull AnalogExpressionBean expression) {
        super(manager, expression);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getTriggerOnChange() {
        return ((AnalogExpressionBean)getObject()).getTriggerOnChange();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTriggerOnChange(boolean triggerOnChange) {
        ((AnalogExpressionBean)getObject()).setTriggerOnChange(triggerOnChange);
    }
    
    /**
     * Get the value of the AnalogExpressionBean.
     */
    private double internalEvaluate() throws JmriException {
        double result = ((AnalogExpressionBean)getObject()).evaluate();
        
        if (Double.isNaN(result)) {
            throw new IllegalArgumentException("The result is NaN");
        }
        if (result == Double.NEGATIVE_INFINITY) {
            throw new IllegalArgumentException("The result is negative infinity");
        }
        if (result == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("The result is positive infinity");
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public double evaluate() throws JmriException {
        if (! _enabled) {
            return 0.0;
        }
        
        if ((_debugConfig != null)
                && ((AnalogExpressionDebugConfig)_debugConfig)._forceResult) {
            return ((AnalogExpressionDebugConfig)_debugConfig)._result;
        }
        
        ConditionalNG conditionalNG = getConditionalNG();
        
        int currentStackPos = conditionalNG.getStack().getCount();
        
        double result = 0.0;
        try {
            conditionalNG.getSymbolTable().createSymbols(_localVariables);
            result = internalEvaluate();
        } catch (JmriMultiLineException e) {
            handleError(this, Bundle.getMessage("ExceptionEvaluateMulti"), e.getErrors(), e, log);
        } catch (JmriException e) {
            handleError(this, Bundle.getMessage("ExceptionEvaluate", e.getLocalizedMessage()), e, log);
        } catch (RuntimeException e) {
            handleError(this, Bundle.getMessage("ExceptionEvaluate", e.getLocalizedMessage()), e, log);
        }
        
        conditionalNG.getStack().setCount(currentStackPos);
        conditionalNG.getSymbolTable().removeSymbols(_localVariables);
        
        return result;
    }
    
    @Override
    public int getState() {
        return ((AnalogExpressionBean)getObject()).getState();
    }

    @Override
    public String getDisplayName() {
        return ((AnalogExpressionBean)getObject()).getDisplayName();
    }

    @Override
    public void disposeMe() {
        ((AnalogExpressionBean)getObject()).dispose();
    }

    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void registerListenersForThisClass() {
        ((AnalogExpressionBean)getObject()).registerListeners();
    }
    
    /**
     * Register listeners if this object needs that.
     */
    @Override
    public void unregisterListenersForThisClass() {
        ((AnalogExpressionBean)getObject()).unregisterListeners();
    }
    
    @Override
    public void setState(int s) throws JmriException {
        ((AnalogExpressionBean)getObject()).setState(s);
    }

    @Override
    public String describeState(int state) {
        return Bundle.getMessage("BeanStateUnknown");
    }

    @Override
    public String getComment() {
        return ((AnalogExpressionBean)getObject()).getComment();
    }

    @Override
    public void setComment(String comment) {
        ((AnalogExpressionBean)getObject()).setComment(comment);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((AnalogExpressionBean)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((AnalogExpressionBean)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((AnalogExpressionBean)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((AnalogExpressionBean)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((AnalogExpressionBean)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((AnalogExpressionBean)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
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
        return new AnalogExpressionDebugConfig();
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



    public static class AnalogExpressionDebugConfig implements MaleSocket.DebugConfig {
        
        // If true, the socket is returning the value of "result" instead of
        // executing the expression.
        public boolean _forceResult = false;
        
        // The result if the result is forced.
        public double _result = 0.0f;
        
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMaleAnalogExpressionSocket.class);

}
