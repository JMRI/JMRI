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
        _lastResult = ((MaleStringExpressionSocket)getObject()).evaluate();
        after();
        return _lastResult;
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleStringExpressionSocket)getObject()).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleStringExpressionSocket)getObject()).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleStringExpressionSocket)getObject()).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleStringExpressionSocket)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleStringExpressionSocket)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleStringExpressionSocket)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleStringExpressionSocket)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleStringExpressionSocket)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleStringExpressionSocket)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
    }

    @Override
    public void setTriggerOnChange(boolean triggerOnChange) {
        ((MaleStringExpressionSocket)getObject()).setTriggerOnChange(triggerOnChange);
    }

    @Override
    public boolean getTriggerOnChange() {
        return ((MaleStringExpressionSocket)getObject()).getTriggerOnChange();
    }
    
}
