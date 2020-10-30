package jmri.jmrit.logixng;

import jmri.NamedBean;

/**
 * Represent a LogixNG module.
 * A module is similar to a ConditionalNG, except that it can be used by
 * both ConditionalNGs and modules.
 *
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public interface Module extends Base, NamedBean {
    
    public void setRootSocketType(FemaleSocketManager.SocketType socketType);
    
    public FemaleSocketManager.SocketType getRootSocketType();
    
    public FemaleSocket getRootSocket();
    
}
