package jmri.jmrit.logixng.implementation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.FemaleAnalogActionSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.MaleAnalogActionSocket;
import jmri.jmrit.logixng.MaleSocket;

/**
 * Default implementation of the Female Analog Action socket
 */
public final class DefaultFemaleAnalogActionSocket
        extends AbstractFemaleSocket
        implements FemaleAnalogActionSocket {

    public DefaultFemaleAnalogActionSocket(Base parent, FemaleSocketListener listener, String name) {
        super(parent, listener, name);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isCompatible(MaleSocket socket) {
        return socket instanceof MaleAnalogActionSocket;
    }
    
    /** {@inheritDoc} */
    @Override
    public Map<Category, List<Class<? extends Base>>> getConnectableClasses() {
        return InstanceManager.getDefault(AnalogActionManager.class).getActionClasses();
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(double value) throws JmriException {
        if (isConnected()) {
            ((MaleAnalogActionSocket)getConnectedSocket()).setValue(value);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "DefaultFemaleAnalogActionSocket_Short");
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "DefaultFemaleAnalogActionSocket_Long", getName());
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        // Do nothing
    }
    
}
