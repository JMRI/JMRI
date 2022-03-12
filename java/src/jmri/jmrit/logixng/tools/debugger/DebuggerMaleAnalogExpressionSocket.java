package jmri.jmrit.logixng.tools.debugger;

import java.util.Set;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.BaseManager;
import jmri.jmrit.logixng.MaleAnalogExpressionSocket;

/**
 *
 * @author daniel
 */
public class DebuggerMaleAnalogExpressionSocket extends AbstractDebuggerMaleSocket implements MaleAnalogExpressionSocket {
    
    private double _lastResult;
    
    public DebuggerMaleAnalogExpressionSocket(BaseManager<MaleAnalogExpressionSocket> manager, MaleAnalogExpressionSocket maleSocket) {
        super(manager, maleSocket);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeforeInfo() {
        return Bundle.getMessage("AnalogExpression_InfoBefore", _lastResult);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAfterInfo() {
        return Bundle.getMessage("AnalogExpression_InfoAfter", _lastResult);
    }
    
    @Override
    public double evaluate() throws JmriException {
        before();
        _lastResult = ((MaleAnalogExpressionSocket)getObject()).evaluate();
        after();
        return _lastResult;
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleAnalogExpressionSocket)getObject()).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleAnalogExpressionSocket)getObject()).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleAnalogExpressionSocket)getObject()).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleAnalogExpressionSocket)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleAnalogExpressionSocket)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleAnalogExpressionSocket)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleAnalogExpressionSocket)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleAnalogExpressionSocket)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleAnalogExpressionSocket)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
    }

    @Override
    public void setTriggerOnChange(boolean triggerOnChange) {
        ((MaleAnalogExpressionSocket)getObject()).setTriggerOnChange(triggerOnChange);
    }

    @Override
    public boolean getTriggerOnChange() {
        return ((MaleAnalogExpressionSocket)getObject()).getTriggerOnChange();
    }
    
}
