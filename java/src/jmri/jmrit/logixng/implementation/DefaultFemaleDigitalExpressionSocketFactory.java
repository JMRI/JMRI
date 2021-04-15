package jmri.jmrit.logixng.implementation;

import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * Factory class for DefaultFemaleDigitalExpressionSocket class.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
@ServiceProvider(service = FemaleSocketFactory.class)
public class DefaultFemaleDigitalExpressionSocketFactory implements FemaleSocketFactory {

    private static final FemaleSocketManager.SocketType _femaleSocketType = new SocketType();
    
    
    @Override
    public FemaleSocketManager.SocketType getFemaleSocketType() {
        return _femaleSocketType;
    }


    private static class SocketType implements FemaleSocketManager.SocketType {
        
        @Override
        public String getName() {
            return "DefaultFemaleDigitalExpressionSocket";
        }

        @Override
        public String getDescr() {
            return Bundle.getMessage("FemaleDigitalExpressionSocket_Descr");
        }

        @Override
        public FemaleSocket createSocket(Base parent, FemaleSocketListener listener, String name) {
            return new DefaultFemaleDigitalExpressionSocket(parent, listener, name);
        }
        
        @Override
        public String toString() {
            return getDescr();
        }
    }
    
}
