package jmri.jmrit.logixng.tools.debugger;

import java.util.Set;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.BaseManager;
import jmri.jmrit.logixng.MaleDigitalBooleanActionSocket;

/**
 *
 * @author daniel
 */
public class DebuggerMaleDigitalBooleanActionSocket extends AbstractDebuggerMaleSocket implements MaleDigitalBooleanActionSocket {
    
    private boolean _nextStatus;
    
    public DebuggerMaleDigitalBooleanActionSocket(BaseManager<MaleDigitalBooleanActionSocket> manager, MaleDigitalBooleanActionSocket maleSocket) {
        super(manager, maleSocket);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeforeInfo() {
        return Bundle.getMessage("DigitalBooleanAction_InfoBefore", _nextStatus ? Bundle.getMessage("True") : Bundle.getMessage("False"));
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAfterInfo() {
        return Bundle.getMessage("DigitalBooleanAction_InfoAfter");
    }
    
    @Override
    public void execute(boolean status) throws JmriException {
        _nextStatus = status;
        before();
        ((MaleDigitalBooleanActionSocket) _maleSocket).execute(_nextStatus);
        after();
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleDigitalBooleanActionSocket) _maleSocket).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleDigitalBooleanActionSocket) _maleSocket).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleDigitalBooleanActionSocket) _maleSocket).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleDigitalBooleanActionSocket) _maleSocket).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleDigitalBooleanActionSocket) _maleSocket).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleDigitalBooleanActionSocket) _maleSocket).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleDigitalBooleanActionSocket) _maleSocket).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleDigitalBooleanActionSocket) _maleSocket).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleDigitalBooleanActionSocket) _maleSocket).compareSystemNameSuffix(suffix1, suffix2, n2);
    }
    
}
