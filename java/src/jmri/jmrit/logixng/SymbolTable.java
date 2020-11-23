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
     * Get the value of a symbol
     * @param name the name
     * @return the value
     */
    public Object getValue(String name);
    
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
    public void createSymbols(Collection<? extends SymbolTable.VariableData> symbolDefinitions) throws JmriException;
    
    /**
     * Removes symbols from the symbol table
     * @param symbolDefinitions the definitions of the symbols to be removed
     * @throws JmriException if an exception is thrown
     */
    public void removeSymbols(Collection<? extends SymbolTable.VariableData> symbolDefinitions) throws JmriException;
    
    /**
     * Print the symbol table on a stream
     * @param stream the stream
     */
    public void printSymbolTable(java.io.PrintStream stream);
    
    
    
    /**
     * An enum that defines the types of initial value.
     */
    public enum InitialValueType {
        
        None(Bundle.getMessage("InitialValueType_None")),
        LocalVariable(Bundle.getMessage("InitialValueType_LocalVariable")),
        Memory(Bundle.getMessage("InitialValueType_Memory")),
        Reference(Bundle.getMessage("InitialValueType_Reference")),
        Formula(Bundle.getMessage("InitialValueType_Formula"));
        
        private final String _descr;
        
        private InitialValueType(String descr) {
            _descr = descr;
        }
        
        public String getDescr() {
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
     * Data for a parameter.
     */
    public interface VariableData {
        
        /**
         * The name of the variable
         * @return the name
         */
        public String getName();
        
        public InitialValueType getInitalValueType();
        
        public String getInitialValueData();
        
    }
    
}
