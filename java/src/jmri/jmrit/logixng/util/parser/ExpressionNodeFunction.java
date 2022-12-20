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

    private final Token _token;
    private final String _identifier;
    private final Function _function;
    private final List<ExpressionNode> _parameterList;
    private final int _startPos;
    private final int _endPos;


    public ExpressionNodeFunction(Token token, List<ExpressionNode> parameterList, int startPos, int endPos)
            throws FunctionNotExistsException {
        _token = token;
        _identifier = token.getString();
        _function = InstanceManager.getDefault(FunctionManager.class).get(_identifier);
        _parameterList = parameterList;
        _startPos = startPos;
        _endPos = endPos;

        if (_function == null) {
            throw new FunctionNotExistsException(Bundle.getMessage("FunctionNotExists", _identifier), _identifier);
        }

//        System.err.format("Function %s, %s%n", _function.getName(), _function.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public Token getToken() {
        return _token;
    }

    /** {@inheritDoc} */
    @Override
    public int getStartPos() {
        return _startPos;
    }

    /** {@inheritDoc} */
    @Override
    public int getEndPos() {
        return _endPos;
    }

    @Override
    public ExpressionNode getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        if (index >= 0 && index < _parameterList.size()) {
            return _parameterList.get(index);
        } else {
            throw new IllegalArgumentException(
                    String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return _parameterList.size();
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
