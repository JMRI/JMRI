package jmri.jmrit.logixng.util.parser.functions;

import jmri.jmrit.logixng.util.parser.*;

/**
 * Abstract class to help writing LogixNG functions easier.
 *
 * @author Daniel Bergqvist 2024
 */
public abstract class AbstractFunction implements Function {

    private final FunctionFactory _functionFactory;
    private final String _name;
    private final String _description;

    public AbstractFunction(FunctionFactory functionFactory, String name, String description) {
        _functionFactory = functionFactory;
        _name = name;
        _description = description;
    }

    @Override
    public String getModule() {
        return _functionFactory.getModule();
    }

    @Override
    public String getConstantDescriptions() {
        return _functionFactory.getConstantDescription();
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getDescription() {
        return _description;
    }
}
