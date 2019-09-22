package jmri.jmrit.logixng.logixemulator.implementation;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.LogixEmulatorActionManager;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.FemaleLogixEmulatorActionSocket;
import jmri.jmrit.logixng.MaleLogixEmulatorActionSocket;
import jmri.jmrit.logixng.implementation.AbstractFemaleSocket;

/**
 *
 */
public final class DefaultFemaleLogixEmulatorActionSocket
        extends AbstractFemaleSocket
        implements FemaleLogixEmulatorActionSocket {


    public DefaultFemaleLogixEmulatorActionSocket(Base parent, FemaleSocketListener listener, String name) {
        super(parent, listener, name);
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate() {
        // Female sockets have special handling
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isCompatible(MaleSocket socket) {
        return socket instanceof MaleLogixEmulatorActionSocket;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean supportsEnableExecution() {
        
        if (isConnected()) {
            return ((MaleLogixEmulatorActionSocket)getConnectedSocket())
                    .supportsEnableExecution();
        } else {
            throw new UnsupportedOperationException("Socket is not connected");
        }
    }
    
    @Override
    public void execute(boolean hasChangedToTrue) {
        if (isConnected()) {
            ((MaleLogixEmulatorActionSocket)getConnectedSocket())
                    .execute(hasChangedToTrue);
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "DefaultFemaleLogixEmulatorActionSocket_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "DefaultFemaleLogixEmulatorActionSocket_Long", getName());
    }

    /** {@inheritDoc} */
    @Override
    public String getExampleSystemName() {
        return InstanceManager.getDefault(LogixEmulatorActionManager.class).getSystemNamePrefix() + "DA10";
    }

    /** {@inheritDoc} */
    @Override
    public String getNewSystemName() {
        return InstanceManager.getDefault(LogixEmulatorActionManager.class)
                .getNewSystemName();
    }

    @Override
    public Map<Category, List<Class<? extends Base>>> getConnectableClasses() {
        return InstanceManager.getDefault(LogixEmulatorActionManager.class).getActionClasses();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        // Do nothing
    }

}
