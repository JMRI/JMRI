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
    
    public Collection<Parameter> getParameters();
    
    public Collection<ParameterData> getLocalVariables();
    
    
    /**
     * The definition of a parameter.
     */
    public interface Parameter {
        
        /**
         * The name of the parameter
         * @return the name
         */
        public String getName();
        
        /**
         * Answer whenether or not the parameter is input to the module.
         * @return true if the parameter is input, false otherwise
         */
        public boolean isInput();
        
        /**
         * Answer whenether or not the parameter is output to the module.
         * @return true if the parameter is output, false otherwise
         */
        public boolean isOutput();
        
    }
    
    
    /**
     * Data for a parameter.
     */
    public interface ParameterData extends SymbolTable.VariableData {
        
        public ReturnValueType getReturnValueType();
        
        public String getReturnValueData();
        
    }
    
    
    /**
     * An enum that defines the types of initial value.
     */
    public enum ReturnValueType {
        
        None(Bundle.getMessage("ReturnValueType_None")),
        LocalVariable(Bundle.getMessage("ReturnValueType_LocalVariable")),
        Memory(Bundle.getMessage("ReturnValueType_Memory"));
        
        private final String _descr;
        
        private ReturnValueType(String descr) {
            _descr = descr;
        }
        
        public String getDescr() {
            return _descr;
        }
    }
    
    
}
