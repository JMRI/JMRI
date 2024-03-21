package jmri.jmrit.logixng.util.parser.functions;

import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.util.parser.ExpressionNode;

/**
 * ExpressionNodeConstant Scaffold
 *
 * @author Daniel Bergqvist 2024
 */
public class ExpressionNodeConstantScaffold implements ExpressionNode {

    private final Object _value;

    public ExpressionNodeConstantScaffold(Object value) {
        _value = value;
    }

    @Override
    public Object calculate(SymbolTable symbolTable) {
        return _value;
    }

    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return null; // This value is never used
    }

}
