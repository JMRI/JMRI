package jmri.jmrit.logixng.tools.debugger;

import java.util.Set;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.BaseManager;
import jmri.jmrit.logixng.MaleDigitalActionSocket;

/**
 *
 * @author daniel
 */
public class DebuggerMaleDigitalActionSocket extends AbstractDebuggerMaleSocket implements MaleDigitalActionSocket {
    
    public DebuggerMaleDigitalActionSocket(BaseManager<MaleDigitalActionSocket> manager, MaleDigitalActionSocket maleSocket) {
        super(manager, maleSocket);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeforeInfo() {
        return Bundle.getMessage("DigitalAction_InfoBefore");
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAfterInfo() {
        return Bundle.getMessage("DigitalAction_InfoAfter");
    }
    
    @Override
    public void execute() throws JmriException {
        before();
        ((MaleDigitalActionSocket) _maleSocket).execute();
        after();
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleDigitalActionSocket) _maleSocket).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleDigitalActionSocket) _maleSocket).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleDigitalActionSocket) _maleSocket).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleDigitalActionSocket) _maleSocket).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleDigitalActionSocket) _maleSocket).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleDigitalActionSocket) _maleSocket).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleDigitalActionSocket) _maleSocket).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleDigitalActionSocket) _maleSocket).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleDigitalActionSocket) _maleSocket).compareSystemNameSuffix(suffix1, suffix2, n2);
    }
    
}
