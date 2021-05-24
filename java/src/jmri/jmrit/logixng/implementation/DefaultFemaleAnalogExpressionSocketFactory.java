package jmri.jmrit.logixng.implementation;

import jmri.*;
import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * Factory class for DefaultFemaleAnalogExpressionSocket class.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
@ServiceProvider(service = FemaleSocketFactory.class)
public class DefaultFemaleAnalogExpressionSocketFactory implements FemaleSocketFactory {

    private static final FemaleSocketManager.SocketType _femaleSocketType = new SocketType();
    
    
    @Override
    public FemaleSocketManager.SocketType getFemaleSocketType() {
        return _femaleSocketType;
    }


    private static class SocketType implements FemaleSocketManager.SocketType {
        
        @Override
        public String getName() {
            return "DefaultFemaleAnalogExpressionSocket";
        }

        @Override
        public String getDescr() {
            return Bundle.getMessage("FemaleAnalogExpressionSocket_Descr");
        }

        @Override
        public BaseManager<? extends MaleSocket> getManager() {
            return InstanceManager.getDefault(AnalogExpressionManager.class);
        }

        @Override
        public FemaleSocket createSocket(Base parent, FemaleSocketListener listener, String name) {
            return new DefaultFemaleAnalogExpressionSocket(parent, listener, name);
        }
        
        @Override
        public String toString() {
            return getDescr();
        }
    }
    
}
