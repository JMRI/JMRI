package jmri.jmrit.logixng.implementation;

import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * Factory class for DefaultFemaleDigitalActionSocket class.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
@ServiceProvider(service = FemaleSocketFactory.class)
public class DefaultFemaleDigitalActionSocketFactory implements FemaleSocketFactory {

    private static final FemaleSocketManager.SocketType _femaleSocketType = new SocketType();
    
    
    @Override
    public FemaleSocketManager.SocketType getFemaleSocketType() {
        return _femaleSocketType;
    }


    private static class SocketType implements FemaleSocketManager.SocketType {
        
        @Override
        public String getName() {
            return "DefaultFemaleDigitalActionSocket";
        }

        @Override
        public String getDescr() {
            return Bundle.getMessage("FemaleDigitalActionSocket_Descr");
        }

        @Override
        public FemaleSocket createSocket(Base parent, FemaleSocketListener listener, String name) {
            return new DefaultFemaleDigitalActionSocket(parent, listener, name);
        }
        
        @Override
        public String toString() {
            return getDescr();
        }
    }
    
}
