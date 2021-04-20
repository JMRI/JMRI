package jmri.jmrit.logixng;

import java.util.Map;

import jmri.Manager;
import jmri.NamedBean;

/**
 * Manager for FemaleSockets
 * 
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public interface FemaleSocketManager {
    
    public interface SocketType {
        
        public String getName();
        
        public String getDescr();
        
        public BaseManager<? extends MaleSocket> getManager();
        
        public FemaleSocket createSocket(Base parent, FemaleSocketListener listener, String name);
        
    }
    
    public Map<String, SocketType> getSocketTypes();
    
    public SocketType getSocketTypeByType(String type);
    
}
