package jmri.jmrit.logixng.tools.debugger;

import java.util.Set;

import javax.annotation.Nonnull;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.BaseManager;
import jmri.jmrit.logixng.MaleStringActionSocket;

/**
 *
 * @author daniel
 */
public class DebuggerMaleStringActionSocket extends AbstractDebuggerMaleSocket implements MaleStringActionSocket {
    
    private String _nextValue;
    
    public DebuggerMaleStringActionSocket(BaseManager<MaleStringActionSocket> manager, MaleStringActionSocket maleSocket) {
        super(manager, maleSocket);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeforeInfo() {
        return Bundle.getMessage("StringAction_InfoBefore", _nextValue);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAfterInfo() {
        return Bundle.getMessage("StringAction_InfoAfter");
    }
    
    @Override
    public void setValue(@Nonnull String value) throws JmriException {
        _nextValue = value;
        before();
        ((MaleStringActionSocket) _maleSocket).setValue(_nextValue);
        after();
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleStringActionSocket) _maleSocket).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleStringActionSocket) _maleSocket).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleStringActionSocket) _maleSocket).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleStringActionSocket) _maleSocket).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleStringActionSocket) _maleSocket).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleStringActionSocket) _maleSocket).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleStringActionSocket) _maleSocket).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleStringActionSocket) _maleSocket).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleStringActionSocket) _maleSocket).compareSystemNameSuffix(suffix1, suffix2, n2);
    }
    
}
