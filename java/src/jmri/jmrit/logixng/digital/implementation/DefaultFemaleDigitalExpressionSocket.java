package jmri.jmrit.logixng.digital.implementation;

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
import jmri.jmrit.logixng.implementation.AbstractFemaleSocket;

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
    public void reset() {
        if (isConnected()) {
            ((MaleDigitalExpressionSocket)getConnectedSocket()).reset();
        }
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

    /** {@inheritDoc} */
    @Override
    public String getNewSystemName() {
        return InstanceManager.getDefault(DigitalExpressionManager.class)
                .getAutoSystemName();
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
