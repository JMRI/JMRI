package jmri.jmrit.logixng.util.parser.expressionnode;

import java.util.Map;
import jmri.JmriException;
import jmri.jmrit.logixng.util.parser.IdentifierNotExistsException;
import jmri.jmrit.logixng.util.parser.Token;
import jmri.jmrit.logixng.util.parser.Variable;

/**
 * A parsed expression
 */
public class ExpressionNodeIdentifier implements ExpressionNode {

    private final Token _token;
    private final Variable _variable;
    
    public ExpressionNodeIdentifier(Token token, Map<String, Variable> variables) throws IdentifierNotExistsException {
        _token = token;
        _variable = variables.get(token.getString());
        
        if (_variable == null) {
            throw new IdentifierNotExistsException(Bundle.getMessage("IdentifierNotExists", token.getString()), token.getString());
        }
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
