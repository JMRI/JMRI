package jmri.jmrit.logixng.util.parser.variables;

import jmri.jmrit.logixng.FemaleDigitalExpressionSocket;
import jmri.jmrit.logixng.util.parser.Variable;

/**
 * A variable that evaluates a digital expression
 */
public class DigitalExpressionVariable implements Variable {

    private final FemaleDigitalExpressionSocket _socket;
    
    public DigitalExpressionVariable(FemaleDigitalExpressionSocket socket) {
        _socket = socket;
    }
    
    @Override
    public String getName() {
        return _socket.getName();
    }

    @Override
    public Object getValue() throws Exception {
        return _socket.evaluate();
    }

}
