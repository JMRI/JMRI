package jmri.jmrit.logixng.digital.implementation;

import java.util.List;
import java.util.Map;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.FemaleDigitalExpressionSocket;
import jmri.jmrit.logixng.MaleDigitalExpressionSocket;
import jmri.jmrit.logixng.implementation.AbstractFemaleSocket;

/**
 *
 */
public class DefaultFemaleDigitalExpressionSocket extends AbstractFemaleSocket
        implements FemaleDigitalExpressionSocket {

    public DefaultFemaleDigitalExpressionSocket(Base parent, FemaleSocketListener listener, String name) {
        super(parent, listener, name);
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate(String sys) {
        // Female sockets have special handling
        throw new UnsupportedOperationException();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isCompatible(MaleSocket socket) {
        return socket instanceof MaleDigitalExpressionSocket;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() {
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
    public String getShortDescription() {
        return Bundle.getMessage("DefaultFemaleDigitalExpressionSocket_Short");
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription() {
        return Bundle.getMessage("DefaultFemaleDigitalExpressionSocket_Long", getName());
    }

    /** {@inheritDoc} */
    @Override
    public String getExampleSystemName() {
        return InstanceManager.getDefault(DigitalExpressionManager.class).getSystemNamePrefix() + "DE10";
    }

    /** {@inheritDoc} */
    @Override
    public String getNewSystemName() {
        return InstanceManager.getDefault(DigitalExpressionManager.class)
                .getNewSystemName();
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
