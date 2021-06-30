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
        ((MaleDigitalExpressionSocket)getObject()).notifyChangedResult(oldResult, newResult);
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
        _lastResult = ((MaleDigitalExpressionSocket)getObject()).evaluate();
        after();
        return _lastResult;
    }

    @Override
    public boolean getLastResult() {
        return ((MaleDigitalExpressionSocket)getObject()).getLastResult();
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleDigitalExpressionSocket)getObject()).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleDigitalExpressionSocket)getObject()).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleDigitalExpressionSocket)getObject()).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleDigitalExpressionSocket)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleDigitalExpressionSocket)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleDigitalExpressionSocket)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleDigitalExpressionSocket)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleDigitalExpressionSocket)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleDigitalExpressionSocket)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
    }

}
