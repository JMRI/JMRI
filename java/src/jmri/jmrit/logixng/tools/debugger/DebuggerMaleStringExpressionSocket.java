package jmri.jmrit.logixng.tools.debugger;

import java.util.Set;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.BaseManager;
import jmri.jmrit.logixng.MaleStringExpressionSocket;

/**
 *
 * @author daniel
 */
public class DebuggerMaleStringExpressionSocket extends AbstractDebuggerMaleSocket implements MaleStringExpressionSocket {
    
    private String _lastResult;
    
    public DebuggerMaleStringExpressionSocket(BaseManager<MaleStringExpressionSocket> manager, MaleStringExpressionSocket maleSocket) {
        super(manager, maleSocket);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeforeInfo() {
        return Bundle.getMessage("AnalogExpression_InfoBefore");
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAfterInfo() {
        return Bundle.getMessage("AnalogExpression_InfoAfter", _lastResult);
    }
    
    @Override
    public String evaluate() throws JmriException {
        before();
        _lastResult = ((MaleStringExpressionSocket) _maleSocket).evaluate();
        after();
        return _lastResult;
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleStringExpressionSocket) _maleSocket).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleStringExpressionSocket) _maleSocket).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleStringExpressionSocket) _maleSocket).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleStringExpressionSocket) _maleSocket).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleStringExpressionSocket) _maleSocket).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleStringExpressionSocket) _maleSocket).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleStringExpressionSocket) _maleSocket).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleStringExpressionSocket) _maleSocket).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleStringExpressionSocket) _maleSocket).compareSystemNameSuffix(suffix1, suffix2, n2);
    }

    @Override
    public void setTriggerOnChange(boolean triggerOnChange) {
        ((MaleStringExpressionSocket) _maleSocket).setTriggerOnChange(triggerOnChange);
    }

    @Override
    public boolean getTriggerOnChange() {
        return ((MaleStringExpressionSocket) _maleSocket).getTriggerOnChange();
    }
    
}
