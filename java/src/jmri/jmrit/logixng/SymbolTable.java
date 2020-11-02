package jmri.jmrit.logixng;

import java.util.Map;

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
    public interface ParameterData {
        
        /**
         * The name of the parameter
         * @return the name
         */
        public String getName();
        
        public InitialValueType getInitalValueType();
        
        public String getInitialValueData();
        
    }
    
}
