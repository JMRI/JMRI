package jmri.jmrit.logixng.tools.debugger;

import java.util.Set;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.BaseManager;
import jmri.jmrit.logixng.MaleDigitalExpressionSocket;

/**
 *
 * @author daniel
 */
public class DebuggerMaleDigitalExpressionSocket extends AbstractDebuggerMaleSocket implements MaleDigitalExpressionSocket {
    
    private boolean _lastResult;
    
    public DebuggerMaleDigitalExpressionSocket(BaseManager<MaleDigitalExpressionSocket> manager, MaleDigitalExpressionSocket maleSocket) {
        super(manager, maleSocket);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyChangedResult(boolean oldResult, boolean newResult) {
        ((MaleDigitalExpressionSocket) _maleSocket).notifyChangedResult(oldResult, newResult);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeforeInfo() {
        return Bundle.getMessage("AnalogExpression_InfoBefore");
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAfterInfo() {
        return Bundle.getMessage("AnalogExpression_InfoAfter", _lastResult ? Bundle.getMessage("True") : Bundle.getMessage("False"));
    }
    
    @Override
    public boolean evaluate() throws JmriException {
        before();
        _lastResult = ((MaleDigitalExpressionSocket) _maleSocket).evaluate();
        after();
        return _lastResult;
    }

    @Override
    public boolean getLastResult() {
        return ((MaleDigitalExpressionSocket) _maleSocket).getLastResult();
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleDigitalExpressionSocket) _maleSocket).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleDigitalExpressionSocket) _maleSocket).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleDigitalExpressionSocket) _maleSocket).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleDigitalExpressionSocket) _maleSocket).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleDigitalExpressionSocket) _maleSocket).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleDigitalExpressionSocket) _maleSocket).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleDigitalExpressionSocket) _maleSocket).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleDigitalExpressionSocket) _maleSocket).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleDigitalExpressionSocket) _maleSocket).compareSystemNameSuffix(suffix1, suffix2, n2);
    }

    @Override
    public void setTriggerOnChange(boolean triggerOnChange) {
        ((MaleDigitalExpressionSocket) _maleSocket).setTriggerOnChange(triggerOnChange);
    }

    @Override
    public boolean getTriggerOnChange() {
        return ((MaleDigitalExpressionSocket) _maleSocket).getTriggerOnChange();
    }
    
}
