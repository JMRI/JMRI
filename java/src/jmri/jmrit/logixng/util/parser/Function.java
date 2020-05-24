package jmri.jmrit.logixng.util.parser;

import jmri.jmrit.logixng.util.parser.expressionnode.ExpressionNode;
import java.util.List;
import jmri.JmriException;

/**
 * Definition of a function used in expressions.
 * 
 * @author Daniel Bergqvist 2019
 */
public interface Function {

    public String getName();
    
    public Object calculate(List<ExpressionNode> parameterList) throws JmriException;
    
}
