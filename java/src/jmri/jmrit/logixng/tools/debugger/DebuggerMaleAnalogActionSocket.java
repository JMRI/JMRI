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
        ((MaleAnalogActionSocket) _maleSocket).setValue(_nextValue);
        after();
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleAnalogActionSocket) _maleSocket).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleAnalogActionSocket) _maleSocket).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleAnalogActionSocket) _maleSocket).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleAnalogActionSocket) _maleSocket).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleAnalogActionSocket) _maleSocket).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleAnalogActionSocket) _maleSocket).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleAnalogActionSocket) _maleSocket).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleAnalogActionSocket) _maleSocket).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleAnalogActionSocket) _maleSocket).compareSystemNameSuffix(suffix1, suffix2, n2);
    }
    
}
