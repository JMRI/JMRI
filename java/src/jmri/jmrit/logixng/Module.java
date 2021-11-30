package jmri.jmrit.logixng;

import java.util.Collection;

import jmri.NamedBean;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.SymbolTable.VariableData;

/**
 * Represent a LogixNG module.
 * A module is similar to a ConditionalNG, except that it can be used by
 * both ConditionalNGs and modules.
 *
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public interface Module extends Base, NamedBean {
    
//    public void setRootSocketType(FemaleSocketManager.SocketType socketType);
    
    public FemaleSocketManager.SocketType getRootSocketType();
    
    public FemaleSocket getRootSocket();
    
    public void setCurrentConditionalNG(ConditionalNG conditionalNG);
    
    public void addParameter(String name, boolean isInput, boolean isOutput);
    
    public void addParameter(Parameter parameter);
    
//    public void removeParameter(String name);
    
    public void addLocalVariable(
            String name,
            SymbolTable.InitialValueType initialValueType,
            String initialValueData);
    
//    public void removeLocalVariable(String name);
    
    public Collection<Parameter> getParameters();
    
    public Collection<VariableData> getLocalVariables();
    
    
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
    public static class ParameterData extends VariableData {
        
        public ReturnValueType _returnValueType = ReturnValueType.None;
        public String _returnValueData;
        
        public ParameterData(
                String name,
                InitialValueType initalValueType,
                String initialValueData,
                ReturnValueType returnValueType,
                String returnValueData) {
            
            super(name, initalValueType, initialValueData);
            
            _returnValueType = returnValueType;
            _returnValueData = returnValueData;
        }
        
        public ParameterData(ParameterData data) {
            super(data._name, data._initalValueType, data._initialValueData);
            _returnValueType = data._returnValueType;
            _returnValueData = data._returnValueData;
        }
        
        public ReturnValueType getReturnValueType() {
            return _returnValueType;
        }
        
        public String getReturnValueData() {
            return _returnValueData;
        }
        
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
