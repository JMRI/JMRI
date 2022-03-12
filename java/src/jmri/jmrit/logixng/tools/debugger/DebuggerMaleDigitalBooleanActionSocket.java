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
    
    private boolean _nextHasChangedToTrue;
    private boolean _nextHasChangedToFalse;
    
    public DebuggerMaleDigitalBooleanActionSocket(BaseManager<MaleDigitalBooleanActionSocket> manager, MaleDigitalBooleanActionSocket maleSocket) {
        super(manager, maleSocket);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeforeInfo() {
        return Bundle.getMessage("DigitalBooleanAction_InfoBefore",
                _nextHasChangedToTrue ? Bundle.getMessage("True") : Bundle.getMessage("False"),
                _nextHasChangedToFalse ? Bundle.getMessage("True") : Bundle.getMessage("False"));
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAfterInfo() {
        return Bundle.getMessage("DigitalBooleanAction_InfoAfter");
    }
    
    @Override
    public void execute(boolean hasChangedToTrue, boolean hasChangedToFalse) throws JmriException {
        _nextHasChangedToTrue = hasChangedToTrue;
        _nextHasChangedToFalse = hasChangedToFalse;
        before();
        ((MaleDigitalBooleanActionSocket)getObject()).execute(hasChangedToTrue, hasChangedToFalse);
        after();
    }

    @Override
    public void setState(int s) throws JmriException {
        ((MaleDigitalBooleanActionSocket)getObject()).setState(s);
    }

    @Override
    public int getState() {
        return ((MaleDigitalBooleanActionSocket)getObject()).getState();
    }

    @Override
    public String describeState(int state) {
        return ((MaleDigitalBooleanActionSocket)getObject()).describeState(state);
    }

    @Override
    public void setProperty(String key, Object value) {
        ((MaleDigitalBooleanActionSocket)getObject()).setProperty(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return ((MaleDigitalBooleanActionSocket)getObject()).getProperty(key);
    }

    @Override
    public void removeProperty(String key) {
        ((MaleDigitalBooleanActionSocket)getObject()).removeProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return ((MaleDigitalBooleanActionSocket)getObject()).getPropertyKeys();
    }

    @Override
    public String getBeanType() {
        return ((MaleDigitalBooleanActionSocket)getObject()).getBeanType();
    }

    @Override
    public int compareSystemNameSuffix(String suffix1, String suffix2, NamedBean n2) {
        return ((MaleDigitalBooleanActionSocket)getObject()).compareSystemNameSuffix(suffix1, suffix2, n2);
    }
    
}
