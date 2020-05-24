package jmri.jmrit.logixng.util.parser.expressionnode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import jmri.JmriException;
import jmri.jmrit.logixng.util.parser.Function;
import jmri.jmrit.logixng.util.parser.FunctionFactory;
import jmri.jmrit.logixng.util.parser.FunctionNotExistsException;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * A parsed expression
 * 
 * @author Daniel Bergqvist 2019
 */
public class ExpressionNodeFunction implements ExpressionNode {

    private static final Map<String, Function> functions = new HashMap<>();
    
    private final String _identifier;
    private final Function _function;
    private final List<ExpressionNode> _parameterList;
    
    
    static {
        for (FunctionFactory actionFactory : ServiceLoader.load(FunctionFactory.class)) {
            actionFactory.getFunctions().forEach((function) -> {
                if (functions.containsKey(function.getName())) {
                    throw new RuntimeException("Function " + function.getName() + " is already registered. Class: " + function.getClass().getName());
                }
//                System.err.format("Add function %s, %s%n", function.getName(), function.getClass().getName());
                functions.put(function.getName(), function);
            });
        }
    }
    
    
    public ExpressionNodeFunction(String identifier, List<ExpressionNode> parameterList) throws FunctionNotExistsException {
        _identifier = identifier;
        _function = functions.get(identifier);
        _parameterList = parameterList;
        
        if (_function == null) {
            throw new FunctionNotExistsException(Bundle.getMessage("FunctionNotExists", identifier), identifier);
        }
        
//        System.err.format("Function %s, %s%n", _function.getName(), _function.getClass().getName());
    }
    
    @Override
    public Object calculate() throws JmriException {
        return _function.calculate(_parameterList);
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
