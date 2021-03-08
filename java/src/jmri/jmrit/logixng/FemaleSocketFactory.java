package jmri.jmrit.logixng;

/**
 * Factory class for FemaleSocket classes.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public interface FemaleSocketFactory {
    
    public FemaleSocketManager.SocketType getFemaleSocketType();
    
}
