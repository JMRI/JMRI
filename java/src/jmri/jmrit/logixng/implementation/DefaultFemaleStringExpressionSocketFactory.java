package jmri.jmrit.logixng.implementation;

import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * Factory class for DefaultFemaleStringExpressionSocket class.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
@ServiceProvider(service = FemaleSocketFactory.class)
public class DefaultFemaleStringExpressionSocketFactory implements FemaleSocketFactory {

    private static final FemaleSocketManager.SocketType _femaleSocketType = new SocketType();
    
    
    @Override
    public FemaleSocketManager.SocketType getFemaleSocketType() {
        return _femaleSocketType;
    }


    private static class SocketType implements FemaleSocketManager.SocketType {
        
        @Override
        public String getName() {
            return "DefaultFemaleStringExpressionSocket";
        }

        @Override
        public String getDescr() {
            return Bundle.getMessage("FemaleStringExpressionSocket_Descr");
        }

        @Override
        public FemaleSocket createSocket(Base parent, FemaleSocketListener listener, String name) {
            return new DefaultFemaleStringExpressionSocket(parent, listener, name);
        }
        
        @Override
        public String toString() {
            return getDescr();
        }
    }
    
}
