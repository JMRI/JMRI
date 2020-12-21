package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.jmrit.logixng.FemaleGenericExpressionSocket;

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
    public Object getValue() throws JmriException {
        return _socket.evaluateGeneric();
    }

    @Override
    public void setValue(Object value) throws JmriException {
        log.error("An expression cannot be assigned a value");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GenericExpressionVariable.class);
}
