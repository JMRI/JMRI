package jmri.jmrit.logixng.util.parser.variables;

import jmri.jmrit.logixng.FemaleGenericExpressionSocket;
import jmri.jmrit.logixng.util.parser.Variable;

/**
 * A variable that evaluates a generic expression
 */
public class GenericExpressionVariable implements Variable {

    private final FemaleGenericExpressionSocket _socket;
    
    public GenericExpressionVariable(FemaleGenericExpressionSocket socket) {
        _socket = socket;
    }
    
    @Override
    public String getName() {
        return _socket.getName();
    }

    @Override
    public Object getValue() throws Exception {
        return _socket.evaluateGeneric();
    }

}
