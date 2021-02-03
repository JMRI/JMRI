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
        _lastResult = ((MaleAnalogExpressionSocket) _maleSocket).evaluate();
        after();
        return _lastResult;
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleAnalogExpressionSocket) _maleSocket).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleAnalogExpressionSocket) _maleSocket).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleAnalogExpressionSocket) _maleSocket).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleAnalogExpressionSocket) _maleSocket).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleAnalogExpressionSocket) _maleSocket).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleAnalogExpressionSocket) _maleSocket).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleAnalogExpressionSocket) _maleSocket).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleAnalogExpressionSocket) _maleSocket).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleAnalogExpressionSocket) _maleSocket).compareSystemNameSuffix(suffix1, suffix2, n2);
    }

    @Override
    public void setTriggerOnChange(boolean triggerOnChange) {
        ((MaleAnalogExpressionSocket) _maleSocket).setTriggerOnChange(triggerOnChange);
    }

    @Override
    public boolean getTriggerOnChange() {
        return ((MaleAnalogExpressionSocket) _maleSocket).getTriggerOnChange();
    }
    
}
