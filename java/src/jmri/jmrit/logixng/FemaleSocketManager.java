package jmri.jmrit.logixng;

import java.util.Map;

/**
 * Manager for FemaleSockets
 * 
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public interface FemaleSocketManager {
    
    interface SocketType {
        
        String getName();
        
        String getDescr();
        
        BaseManager<? extends MaleSocket> getManager();
        
        FemaleSocket createSocket(Base parent, FemaleSocketListener listener, String name);
        
    }
    
    Map<String, SocketType> getSocketTypes();
    
    SocketType getSocketTypeByType(String type);
    
}
