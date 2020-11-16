package jmri.jmrit.logixng.implementation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Default implementation of the Female Analog Expression socket
 */
public class DefaultFemaleAnalogExpressionSocket extends AbstractFemaleSocket
        implements FemaleAnalogExpressionSocket {


    public DefaultFemaleAnalogExpressionSocket(Base parent, FemaleSocketListener listener, String name) {
        super(parent, listener, name);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isCompatible(MaleSocket socket) {
        return socket instanceof MaleAnalogExpressionSocket;
    }
    
    /** {@inheritDoc} */
    @Override
    public double evaluate() throws JmriException {
        if (isConnected()) {
            return ((MaleAnalogExpressionSocket)getConnectedSocket()).evaluate();
        } else {
            return 0.0;
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
        return Bundle.getMessage(locale, "DefaultFemaleAnalogExpressionSocket_Short");
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "DefaultFemaleAnalogExpressionSocket_Long", getName());
    }

    /** {@inheritDoc} */
    @Override
    public Map<Category, List<Class<? extends Base>>> getConnectableClasses() {
        return InstanceManager.getDefault(AnalogExpressionManager.class).getExpressionClasses();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        // Do nothing
    }

}
