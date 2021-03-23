package jmri.jmrit.logixng.implementation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.FemaleDigitalExpressionSocket;
import jmri.jmrit.logixng.MaleDigitalExpressionSocket;

/**
 * Default implementation of the Female Digital Expression socket
 */
public class DefaultFemaleDigitalExpressionSocket extends AbstractFemaleSocket
        implements FemaleDigitalExpressionSocket {

    public DefaultFemaleDigitalExpressionSocket(Base parent, FemaleSocketListener listener, String name) {
        super(parent, listener, name);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isCompatible(MaleSocket socket) {
        return socket instanceof MaleDigitalExpressionSocket;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        if (isConnected()) {
            return ((MaleDigitalExpressionSocket)getConnectedSocket())
                    .evaluate();
        } else {
            return false;
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
        return Bundle.getMessage(locale, "DefaultFemaleDigitalExpressionSocket_Short");
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "DefaultFemaleDigitalExpressionSocket_Long", getName());
    }

    @Override
    public Map<Category, List<Class<? extends Base>>> getConnectableClasses() {
        return InstanceManager.getDefault(DigitalExpressionManager.class).getExpressionClasses();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        // Do nothing
    }

}
