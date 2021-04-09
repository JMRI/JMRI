package jmri.jmrit.logixng.tools.debugger;

import java.util.Set;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.BaseManager;
import jmri.jmrit.logixng.MaleAnalogActionSocket;

/**
 *
 * @author daniel
 */
public class DebuggerMaleAnalogActionSocket extends AbstractDebuggerMaleSocket implements MaleAnalogActionSocket {
    
    double _nextValue;
    
    public DebuggerMaleAnalogActionSocket(BaseManager<MaleAnalogActionSocket> manager, MaleAnalogActionSocket maleSocket) {
        super(manager, maleSocket);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeforeInfo() {
        return Bundle.getMessage("AnalogAction_InfoBefore", _nextValue);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAfterInfo() {
        return Bundle.getMessage("AnalogAction_InfoAfter");
    }
    
    @Override
    public void setValue(double value) throws JmriException {
        _nextValue = value;
        before();
        ((MaleAnalogActionSocket)getObject()).setValue(_nextValue);
        after();
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleAnalogActionSocket)getObject()).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleAnalogActionSocket)getObject()).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleAnalogActionSocket)getObject()).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleAnalogActionSocket)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleAnalogActionSocket)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleAnalogActionSocket)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleAnalogActionSocket)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleAnalogActionSocket)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleAnalogActionSocket)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
    }
    
}
