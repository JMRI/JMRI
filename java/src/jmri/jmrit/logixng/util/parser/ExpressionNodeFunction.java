package jmri.jmrit.logixng.util.parser;

import java.util.List;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression
 * 
 * @author Daniel Bergqvist 2019
 */
public class ExpressionNodeFunction implements ExpressionNode {

    private final String _identifier;
    private final Function _function;
    private final List<ExpressionNode> _parameterList;
    
    
    public ExpressionNodeFunction(String identifier, List<ExpressionNode> parameterList) throws FunctionNotExistsException {
        _identifier = identifier;
        _function = InstanceManager.getDefault(FunctionManager.class).get(identifier);
        _parameterList = parameterList;
        
        if (_function == null) {
            throw new FunctionNotExistsException(Bundle.getMessage("FunctionNotExists", identifier), identifier);
        }
        
//        System.err.format("Function %s, %s%n", _function.getName(), _function.getClass().getName());
    }
    
    @Override
    public Object calculate(SymbolTable symbolTable) throws JmriException {
        return _function.calculate(symbolTable, _parameterList);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        StringBuilder str = new StringBuilder();
        str.append("Function:");
        str.append(_identifier);
        str.append("(");
        for (int i=0; i < _parameterList.size(); i++) {
            if (i > 0) {
                str.append(",");
            }
            str.append(_parameterList.get(i).getDefinitionString());
        }
        str.append(")");
        return str.toString();
    }
    
}
