package jmri.jmrit.logixng.implementation;

import jmri.*;
import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * Factory class for DefaultFemaleAnalogActionSocket class.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
@ServiceProvider(service = FemaleSocketFactory.class)
public class DefaultFemaleAnalogActionSocketFactory implements FemaleSocketFactory {

    private static final FemaleSocketManager.SocketType _femaleSocketType = new SocketType();
    
    
    @Override
    public FemaleSocketManager.SocketType getFemaleSocketType() {
        return _femaleSocketType;
    }


    private static class SocketType implements FemaleSocketManager.SocketType {
        
        @Override
        public String getName() {
            return "DefaultFemaleAnalogActionSocket";
        }

        @Override
        public String getDescr() {
            return Bundle.getMessage("FemaleAnalogActionSocket_Descr");
        }

        @Override
        public BaseManager<? extends MaleSocket> getManager() {
            return InstanceManager.getDefault(AnalogActionManager.class);
        }

        @Override
        public FemaleSocket createSocket(Base parent, FemaleSocketListener listener, String name) {
            return new DefaultFemaleAnalogActionSocket(parent, listener, name);
        }
        
        @Override
        public String toString() {
            return getDescr();
        }
    }
    
}
