package jmri.jmrit.logixng.util.parser.functions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.TypeConversionUtil;

import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of conversion functions.
 *
 * @author Daniel Bergqvist 2020
 */
@ServiceProvider(service = FunctionFactory.class)
public class ConvertFunctions implements FunctionFactory {

    @Override
    public String getModule() {
        return "Convert";
    }

    @Override
    public Set<Function> getFunctions() {
        Set<Function> functionClasses = new HashSet<>();

        addIsIntFunction(functionClasses);
        addIsFloatFunction(functionClasses);
        addBoolFunction(functionClasses);
        addBoolJythonFunction(functionClasses);
        addIntFunction(functionClasses);
        addFloatFunction(functionClasses);
        addStrFunction(functionClasses);
        addHex2DecFunction(functionClasses);

        return functionClasses;
    }

    @Override
    public Set<Constant> getConstants() {
        return new HashSet<>();
    }

    @Override
    public String getConstantDescription() {
        // This module doesn't define any constants
        return null;
    }

    private void addIsIntFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "isInt", Bundle.getMessage("Convert.isInt")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                if (parameterList.size() != 1) {
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }
                try {
                    TypeConversionUtil.convertToLong(
                            parameterList.get(0).calculate(symbolTable),
                            true, true);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });
    }

    private void addIsFloatFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "isFloat", Bundle.getMessage("Convert.isFloat")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                try {
                    switch (parameterList.size()) {
                        case 1:
                            TypeConversionUtil.convertToDouble(parameterList.get(0).calculate(symbolTable), false, true, true);
                            break;
                        case 2:
                            boolean do_i18n = TypeConversionUtil.convertToBoolean(
                                    parameterList.get(0).calculate(symbolTable), false);
                            TypeConversionUtil.convertToDouble(
                                    parameterList.get(0).calculate(symbolTable), do_i18n, true, true);
                            break;
                        default:
                            throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                    }
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });
    }

    private void addBoolFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "bool", Bundle.getMessage("Convert.bool")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                switch (parameterList.size()) {
                    case 1:
                        return TypeConversionUtil.convertToBoolean(parameterList.get(0).calculate(symbolTable), false);
                    case 2:
                        boolean do_i18n = TypeConversionUtil.convertToBoolean(
                                parameterList.get(0).calculate(symbolTable), false);
                        return TypeConversionUtil.convertToBoolean(
                                parameterList.get(0).calculate(symbolTable), do_i18n);
                    default:
                        throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }
            }
        });
    }

    private void addBoolJythonFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "boolJython", Bundle.getMessage("Convert.boolJython")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                switch (parameterList.size()) {
                    case 1:
                        return TypeConversionUtil.convertToBoolean_JythonRules(parameterList.get(0).calculate(symbolTable), false);
                    case 2:
                        boolean do_i18n = TypeConversionUtil.convertToBoolean_JythonRules(
                                parameterList.get(0).calculate(symbolTable), false);
                        return TypeConversionUtil.convertToBoolean(
                                parameterList.get(0).calculate(symbolTable), do_i18n);
                    default:
                        throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }
            }
        });
    }

    private void addIntFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "int", Bundle.getMessage("Convert.int")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                if (parameterList.size() != 1) {
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }
                return TypeConversionUtil.convertToLong(
                        parameterList.get(0).calculate(symbolTable));
            }
        });
    }

    private void addFloatFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "float", Bundle.getMessage("Convert.float")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                switch (parameterList.size()) {
                    case 1:
                        return TypeConversionUtil.convertToDouble(parameterList.get(0).calculate(symbolTable), false);
                    case 2:
                        boolean do_i18n = TypeConversionUtil.convertToBoolean(
                                parameterList.get(0).calculate(symbolTable), false);
                        return TypeConversionUtil.convertToDouble(
                                parameterList.get(0).calculate(symbolTable), do_i18n);
                    default:
                        throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }
            }
        });
    }

    private void addStrFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "str", Bundle.getMessage("Convert.str_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                switch (parameterList.size()) {
                    case 1:
                        return TypeConversionUtil.convertToString(parameterList.get(0).calculate(symbolTable), false);
                    case 2:
                        boolean do_i18n = TypeConversionUtil.convertToBoolean(
                                parameterList.get(0).calculate(symbolTable), false);
                        return TypeConversionUtil.convertToString(
                                parameterList.get(0).calculate(symbolTable), do_i18n);
                    default:
                        throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }
            }
        });
    }

    private void addHex2DecFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "hex2dec", Bundle.getMessage("Convert.hex2dec")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                if (parameterList.size() != 1) {
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
                }
                Object o = parameterList.get(0).calculate(symbolTable);
                if (o != null) {
                    return Long.parseLong(o.toString(), 16);
                } else {
                    throw new NullPointerException("value is null");
                }
            }
        });
    }

}
