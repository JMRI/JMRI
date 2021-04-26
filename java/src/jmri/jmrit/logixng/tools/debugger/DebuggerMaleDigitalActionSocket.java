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
        ((MaleDigitalActionSocket)getObject()).execute();
        after();
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleDigitalActionSocket)getObject()).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleDigitalActionSocket)getObject()).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleDigitalActionSocket)getObject()).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleDigitalActionSocket)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleDigitalActionSocket)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleDigitalActionSocket)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleDigitalActionSocket)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleDigitalActionSocket)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleDigitalActionSocket)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
    }
    
}
