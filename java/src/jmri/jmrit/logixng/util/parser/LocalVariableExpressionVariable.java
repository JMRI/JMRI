package jmri.jmrit.logixng.util.parser;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.LogixNG_Manager;

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
    public Object getValue() throws JmriException {
        return InstanceManager.getDefault(LogixNG_Manager.class)
                .getSymbolTable().getValue(_name);
    }

}
