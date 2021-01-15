package jmri.jmrit.logixng.util.parser;

import java.util.Map;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression
 */
public class ExpressionNodeIdentifier implements ExpressionNode {

    private final Token _token;
    private final Variable _variable;
    
    public ExpressionNodeIdentifier(Token token, Map<String, Variable> variables) throws IdentifierNotExistsException {
        _token = token;
        Variable variable = variables.get(token.getString());
        
        if (variable == null) {
            // Assume the identifier is a local variable.
            // Local variables may not be known when the expression is parsed.
            
            variable = new LocalVariableExpressionVariable(token.getString());
        }
        
        _variable = variable;
    }
    
    public String getIdentifier() {
        return _token.getString();
    }
    
    @Override
    public Object calculate(SymbolTable symbolTable) throws JmriException {
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
        if (_variable != null) {
            _variable.setValue(symbolTable, value);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "Identifier:"+_token.getString();
    }
    
}
