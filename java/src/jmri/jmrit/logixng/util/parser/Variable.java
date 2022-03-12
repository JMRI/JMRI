package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * A variable
 */
public interface Variable {

    public String getName();
    
    public Object getValue(SymbolTable symbolTable) throws JmriException;
    
    public void setValue(SymbolTable symbolTable, Object value) throws JmriException;
    
}
