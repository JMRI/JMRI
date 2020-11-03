package jmri.jmrit.logixng.util.parser.variables;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.util.parser.Variable;

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
