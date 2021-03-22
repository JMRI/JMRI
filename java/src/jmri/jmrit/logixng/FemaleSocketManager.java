package jmri.jmrit.logixng;

import java.util.Map;

/**
 * Manager for FemaleSockets
 * 
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public interface FemaleSocketManager {
    
    public interface SocketType {
        
        public String getName();
        
        public String getDescr();
        
        public FemaleSocket createSocket(Base parent, FemaleSocketListener listener, String name);
        
    }
    
    public Map<String, SocketType> getSocketTypes();
    
    public SocketType getSocketTypeByType(String type);
    
}
