package jmri.jmrit.logixng.util.parser;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.SymbolTable;

/**
 * A variable that evaluates a local variable
 */
public class LocalVariableExpressionVariable implements Variable {

    private final String _name;
    
    public LocalVariableExpressionVariable(String name) {
        _name = name;
    }
    
    @Override
    public String getName() {
        return _name;
    }

    @Override
    public Object getValue(SymbolTable symbolTable) throws JmriException {
        try {
            return symbolTable.getValue(_name);
        } catch (SymbolTable.SymbolNotFound e) {
            Constant constant = InstanceManager.getDefault(FunctionManager.class).getConstant(_name);
            if (constant != null) return constant.getValue();
            throw e;
        }
    }

    @Override
    public void setValue(SymbolTable symbolTable, Object value) throws JmriException {
        symbolTable.setValue(_name, value);
    }

}
