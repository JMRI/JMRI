package jmri.jmrit.logixng.util.parser.expressionnode;

import java.util.Map;

import jmri.JmriException;
import jmri.jmrit.logixng.util.parser.IdentifierNotExistsException;
import jmri.jmrit.logixng.util.parser.Token;
import jmri.jmrit.logixng.util.parser.Variable;
import jmri.jmrit.logixng.util.parser.variables.LocalVariableExpressionVariable;

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
    public Object calculate() throws JmriException {
        return _variable.getValue();
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "Identifier:"+_token.getString();
    }
    
}
