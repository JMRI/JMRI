package jmri.jmrit.logixng;

import java.util.Collection;
import java.util.Map;

import jmri.JmriException;

/**
 * A symbol table
 * 
 * @author Daniel Bergqvist 2020
 */
public interface SymbolTable {
    
    /**
     * The list of symbols in the table
     * @return the symbols
     */
    public Map<String, Symbol> getSymbols();
    
    /**
     * The list of symbols and their values in the table
     * @return the name of the symbols and their values
     */
    public Map<String, Object> getSymbolValues();
    
    /**
     * Get the value of a symbol
     * @param name the name
     * @return the value
     */
    public Object getValue(String name);
    
    /**
     * Is the symbol in the symbol table?
     * @param name the name
     * @return true if the symbol exists, false otherwise
     */
    public boolean hasValue(String name);
    
    /**
     * Set the value of a symbol
     * @param name the name
     * @param value the value
     */
    public void setValue(String name, Object value);
    
    /**
     * Add new symbols to the symbol table
     * @param symbolDefinitions the definitions of the new symbols
     * @throws JmriException if an exception is thrown
     */
    public void createSymbols(Collection<? extends VariableData> symbolDefinitions)
            throws JmriException;
    
    /**
     * Add new symbols to the symbol table.
     * This method is used for parameters, when new symbols might be created
     * that uses symbols from a previous symbol table.
     * 
     * @param symbolTable the symbol table to get existing symbols from
     * @param symbolDefinitions the definitions of the new symbols
     * @throws JmriException if an exception is thrown
     */
    public void createSymbols(
            SymbolTable symbolTable,
            Collection<? extends VariableData> symbolDefinitions)
            throws JmriException;
    
    /**
     * Removes symbols from the symbol table
     * @param symbolDefinitions the definitions of the symbols to be removed
     * @throws JmriException if an exception is thrown
     */
    public void removeSymbols(Collection<? extends VariableData> symbolDefinitions)
            throws JmriException;
    
    /**
     * Print the symbol table on a stream
     * @param stream the stream
     */
    public void printSymbolTable(java.io.PrintWriter stream);
    
    /**
     * Validates the name of a symbol
     * @param name the name
     * @return true if the name is valid, false otherwise
     */
    public static boolean validateName(String name) {
        if (name.isEmpty()) return false;
        if (!Character.isLetter(name.charAt(0))) return false;
        for (int i=0; i < name.length(); i++) {
            if (!Character.isLetterOrDigit(name.charAt(i)) && (name.charAt(i) != '_')) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get the stack.
     * This method is only used internally by DefaultSymbolTable.
     * 
     * @return the stack
     */
    public Stack getStack();
    
    
    /**
     * An enum that defines the types of initial value.
     */
    public enum InitialValueType {
        
        None(Bundle.getMessage("InitialValueType_None"), true),
        Integer(Bundle.getMessage("InitialValueType_Integer"), true),
        FloatingNumber(Bundle.getMessage("InitialValueType_FloatingNumber"), true),
        String(Bundle.getMessage("InitialValueType_String"), true),
        Array(Bundle.getMessage("InitialValueType_Array"), false),
        Map(Bundle.getMessage("InitialValueType_Map"), false),
        LocalVariable(Bundle.getMessage("InitialValueType_LocalVariable"), true),
        Memory(Bundle.getMessage("InitialValueType_Memory"), true),
        Reference(Bundle.getMessage("InitialValueType_Reference"), true),
        Formula(Bundle.getMessage("InitialValueType_Formula"), true);
        
        private final String _descr;
        private final boolean _isValidAsParameter;
        
        private InitialValueType(String descr, boolean isValidAsParameter) {
            _descr = descr;
            _isValidAsParameter = isValidAsParameter;
        }
        
        @Override
        public String toString() {
            return _descr;
        }
        
        public boolean isValidAsParameter() {
            return _isValidAsParameter;
        }
    }
    
    
    /**
     * The definition of the symbol
     */
    public interface Symbol {
        
        /**
         * The name of the symbol
         * @return the name
         */
        public String getName();
        
        /**
         * The index on the stack for this symbol
         * @return the index
         */
        public int getIndex();
        
    }
    
    
    /**
     * Data for a variable.
     */
    public static class VariableData {
        
        public String _name;
        public InitialValueType _initialValueType = InitialValueType.None;
        public String _initialValueData;
        
        public VariableData(
                String name,
                InitialValueType initialValueType,
                String initialValueData) {
            
            _name = name;
            if (initialValueType != null) {
                _initialValueType = initialValueType;
            }
            _initialValueData = initialValueData;
        }
        
        public VariableData(VariableData variableData) {
            _name = variableData._name;
            _initialValueType = variableData._initialValueType;
            _initialValueData = variableData._initialValueData;
        }
        
        /**
         * The name of the variable
         * @return the name
         */
        public String getName() {
            return _name;
        }
        
        public InitialValueType getInitialValueType() {
            return _initialValueType;
        }
        
        public String getInitialValueData() {
            return _initialValueData;
        }
        
    }
    
    
    public static class SymbolNotFound extends IllegalArgumentException {
        
        public SymbolNotFound(String message) {
            super(message);
        }
    }
    
}
