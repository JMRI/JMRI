package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * A variable
 */
public interface Variable {

    String getName();
    
    Object getValue(SymbolTable symbolTable) throws JmriException;
    
    void setValue(SymbolTable symbolTable, Object value) throws JmriException;
    
}
