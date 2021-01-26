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
    
    public DebuggerMaleDigitalBooleanActionSocket(BaseManager<MaleDigitalBooleanActionSocket> manager, MaleDigitalBooleanActionSocket maleSocket) {
        super(manager, maleSocket);
    }

    @Override
    public void execute(boolean hasChangedToTrue) throws JmriException {
        before();
        ((MaleDigitalBooleanActionSocket) _maleSocket).execute(hasChangedToTrue);
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
