package jmri.jmrit.logixng.util.parser;

import java.util.Map;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression
 * 
 * @author Daniel Bergqvist 2021
 */
public class ExpressionNodeInstanceVariable implements ExpressionNode {

    private final String _variableName;
    private final String _instanceVariableName;
    private final Variable _variable;
    
    public ExpressionNodeInstanceVariable(String variableName, String instanceVariableToken, Map<String, Variable> variables) throws IdentifierNotExistsException {
        _variableName = variableName;
        _instanceVariableName = instanceVariableToken;
        Variable variable = variables.get(variableName);
        
        if (variable == null) {
            // Assume the identifier is a local variable.
            // Local variables may not be known when the expression is parsed.
            
            variable = new LocalVariableExpressionVariable(_variableName);
        }
        
        _variable = variable;
    }
    
    public String getIdentifier() {
        return _variableName;
    }
    
    @Override
    public Object calculate(SymbolTable symbolTable) throws JmriException {
        if (1==1) throw new RuntimeException("Not implemented yet");
        return _variable.getValue(symbolTable);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean canBeAssigned() {
        // If the identifier is a local variable, assignment is possible. And
        // we don't know if the identifier is a valid local variable until the
        // expression is calculated. So we assume that it is.
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void assignValue(SymbolTable symbolTable, Object value) throws JmriException {
        if (1==1) throw new RuntimeException("Not implemented yet");
        if (_variable != null) {
            _variable.setValue(symbolTable, value);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "InstanceVariable:"+_variableName+"."+_instanceVariableName;
    }
    
}
