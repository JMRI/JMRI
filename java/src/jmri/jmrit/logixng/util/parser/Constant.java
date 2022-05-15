package jmri.jmrit.logixng.util.parser;

import java.util.List;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * Definition of a constant used in expressions.
 * 
 * @author Daniel Bergqvist 2021
 */
public final class Constant {

    private final String _module;
    private final String _name;
    private final  Object _value;
    
    
    public Constant(String m, String name, Object value) {
        this._module = m;
        this._name = name;
        this._value = value;
    }

    /**
     * Get the module of the constant, for example "Math" or "Conversion".
     * @return the module name
     */
    public String getModule() {
        return _module;
    }
    
    /**
     * Get name of the function, for example "MathPI" or "MathE"
     * @return the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Return the value of the constant.
     * @return the result
     */
    public Object getValue() {
        return _value;
    }
    
}
