package jmri.jmrit.logixng;

import java.util.Collection;

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
    
    public void addParameter(String name, boolean isInput, boolean isOutput);
    
    public void removeParameter(String name);
    
    public void addLocalVariable(
            String name,
            SymbolTable.InitialValueType initialValueType,
            String initialValueData);
    
    public void removeLocalVariable(String name);
    
    public Collection<SymbolTable.Parameter> getParameters();
    
    public Collection<SymbolTable.ParameterData> getLocalVariables();
    
}
