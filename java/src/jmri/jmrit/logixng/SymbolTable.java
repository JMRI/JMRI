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
    public void printSymbolTable(java.io.PrintStream stream);
    
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
     * An enum that defines the types of initial value.
     */
    public enum InitialValueType {
        
        None(Bundle.getMessage("InitialValueType_None")),
        Integer(Bundle.getMessage("InitialValueType_Integer")),
        FloatingNumber(Bundle.getMessage("InitialValueType_FloatingNumber")),
        String(Bundle.getMessage("InitialValueType_String")),
        LocalVariable(Bundle.getMessage("InitialValueType_LocalVariable")),
        Memory(Bundle.getMessage("InitialValueType_Memory")),
        Reference(Bundle.getMessage("InitialValueType_Reference")),
        Formula(Bundle.getMessage("InitialValueType_Formula"));
        
        private final String _descr;
        
        private InitialValueType(String descr) {
            _descr = descr;
        }
        
        @Override
        public String toString() {
            return _descr;
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
        public InitialValueType _initalValueType;
        public String _initialValueData;
        
        public VariableData(
                String name,
                InitialValueType initalValueType,
                String initialValueData) {
            
            _name = name;
            _initalValueType = initalValueType;
            _initialValueData = initialValueData;
        }
        
        public VariableData(VariableData variableData) {
            _name = variableData._name;
            _initalValueType = variableData._initalValueType;
            _initialValueData = variableData._initialValueData;
        }
        
        /**
         * The name of the variable
         * @return the name
         */
        public String getName() {
            return _name;
        }
        
        public InitialValueType getInitalValueType() {
            return _initalValueType;
        }
        
        public String getInitialValueData() {
            return _initialValueData;
        }
        
    }
    
    
}
