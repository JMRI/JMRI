package jmri.jmrit.logixng.implementation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.FemaleStringExpressionSocket;
import jmri.jmrit.logixng.MaleStringExpressionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.StringExpressionManager;

/**
 * Default implementation of the Female String Expression socket
 */
public class DefaultFemaleStringExpressionSocket extends AbstractFemaleSocket
        implements FemaleStringExpressionSocket {

    public DefaultFemaleStringExpressionSocket(Base parent, FemaleSocketListener listener, String name) {
        super(parent, listener, name);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isCompatible(MaleSocket socket) {
        return socket instanceof MaleStringExpressionSocket;
    }
    
    /** {@inheritDoc} */
    @Override
    public String evaluate() throws JmriException {
        if (isConnected()) {
            return ((MaleStringExpressionSocket)getConnectedSocket()).evaluate();
        } else {
            return "";
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean getTriggerOnChange() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTriggerOnChange(boolean triggerOnChange) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "DefaultFemaleStringExpressionSocket_Short");
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "DefaultFemaleStringExpressionSocket_Long", getName());
    }

    /** {@inheritDoc} */
    @Override
    public Map<Category, List<Class<? extends Base>>> getConnectableClasses() {
        return InstanceManager.getDefault(StringExpressionManager.class).getExpressionClasses();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        // Do nothing
    }

}
