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
 * Implementation of mathematical functions.
 *
 * @author Daniel Bergqvist 2019
 */
@ServiceProvider(service = FunctionFactory.class)
public class MathFunctions implements FunctionFactory {

    @Override
    public String getModule() {
        return "Math";
    }

    @Override
    public Set<Function> getFunctions() {
        Set<Function> functionClasses = new HashSet<>();

        addRandomFunction(functionClasses);
        addAbsFunction(functionClasses);
        addSinFunction(functionClasses);
        addCosFunction(functionClasses);
        addTanFunction(functionClasses);
        addArctanFunction(functionClasses);
        addSqrFunction(functionClasses);
        addSqrtFunction(functionClasses);

        return functionClasses;
    }

    @Override
    public Set<Constant> getConstants() {
        Set<Constant> constantClasses = new HashSet<>();
        constantClasses.add(new Constant(getModule(), "MathPI", Math.PI));
        constantClasses.add(new Constant(getModule(), "MathE", Math.E));
        return constantClasses;
    }

    @Override
    public String getConstantDescription() {
        return Bundle.getMessage("Math.ConstantDescriptions");
    }

    private void addRandomFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "random", Bundle.getMessage("Math.random_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws CalculateException, JmriException {

                double min;
                double max;
                switch (parameterList.size()) {
                    case 0:
                        return Math.random();
                    case 1:
                        max = TypeConversionUtil.convertToDouble(
                                parameterList.get(0).calculate(symbolTable), false);
                        return Math.random() * max;
                    case 2:
                        min = TypeConversionUtil.convertToDouble(
                                parameterList.get(0).calculate(symbolTable), false);
                        max = TypeConversionUtil.convertToDouble(
                                parameterList.get(1).calculate(symbolTable), false);
                        return min + Math.random() * (max-min);
                    default:
                        throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
                }
            }
        });
    }

    private void addAbsFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "abs", Bundle.getMessage("Math.abs_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws CalculateException, JmriException {

                switch (parameterList.size()) {
                    case 1:
                        Object value = parameterList.get(0).calculate(symbolTable);
                        if ((value instanceof java.util.concurrent.atomic.AtomicInteger)
                                || (value instanceof java.util.concurrent.atomic.AtomicLong)
                                || (value instanceof java.math.BigInteger)
                                || (value instanceof Byte)
                                || (value instanceof Short)
                                || (value instanceof Integer)
                                || (value instanceof Long)
                                || (value instanceof java.util.concurrent.atomic.LongAccumulator)
                                || (value instanceof java.util.concurrent.atomic.LongAdder)) {
                            return Math.abs(((Number)value).longValue());
                        }
                        if ((value instanceof java.math.BigDecimal)
                                || (value instanceof Float)
                                || (value instanceof Double)
                                || (value instanceof java.util.concurrent.atomic.DoubleAccumulator)
                                || (value instanceof java.util.concurrent.atomic.DoubleAdder)) {
                            return Math.abs(((Number)value).doubleValue());
                        }
                        return Math.abs(TypeConversionUtil.convertToDouble(value, false));
                    default:
                        throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
                }
            }
        });
    }

    private void addSinFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "sin", Bundle.getMessage("Math.sin_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                if (parameterList.size() == 1) {
                    double param = TypeConversionUtil.convertToDouble(
                            parameterList.get(0).calculate(symbolTable), false);
                    return Math.sin(param);
                } else if (parameterList.size() >= 2) {
                    double param0 = TypeConversionUtil.convertToDouble(
                            parameterList.get(0).calculate(symbolTable), false);
                    Object param1 = parameterList.get(1).calculate(symbolTable);
                    double result;
                    if (param1 instanceof String) {
                        switch ((String)param1) {
                            case "rad":
                                result = Math.sin(param0);
                                break;
                            case "deg":
                                result = Math.sin(Math.toRadians(param0));
                                break;
                            default:
                                throw new CalculateException(Bundle.getMessage("IllegalParameter", 2, param1, getName()));
                        }
                    } else if (param1 instanceof Number) {
                        double p1 = TypeConversionUtil.convertToDouble(param1, false);
                        double angle = param0 / p1 * 2.0 * Math.PI;
                        result = Math.sin(angle);
                    } else {
                        throw new CalculateException(Bundle.getMessage("IllegalParameter", 2, param1, getName()));
                    }

                    switch (parameterList.size()) {
                        case 2:
                            return result;
                        case 4:
                            double min = TypeConversionUtil.convertToDouble(
                                    parameterList.get(2).calculate(symbolTable), false);
                            double max = TypeConversionUtil.convertToDouble(
                                    parameterList.get(3).calculate(symbolTable), false);
                            return (result+1.0)/2.0 * (max-min) + min;
                        default:
                            throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
                    }
                }
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
            }
        });
    }

    private void addCosFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "cos", Bundle.getMessage("Math.cos_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                if (parameterList.size() == 1) {
                    double param = TypeConversionUtil.convertToDouble(
                            parameterList.get(0).calculate(symbolTable), false);
                    return Math.cos(param);
                } else if (parameterList.size() >= 2) {
                    double param0 = TypeConversionUtil.convertToDouble(
                            parameterList.get(0).calculate(symbolTable), false);
                    Object param1 = parameterList.get(1).calculate(symbolTable);
                    double result;
                    if (param1 instanceof String) {
                        switch ((String)param1) {
                            case "rad":
                                result = Math.cos(param0);
                                break;
                            case "deg":
                                result = Math.cos(Math.toRadians(param0));
                                break;
                            default:
                                throw new CalculateException(Bundle.getMessage("IllegalParameter", 2, param1, getName()));
                        }
                    } else if (param1 instanceof Number) {
                        double p1 = TypeConversionUtil.convertToDouble(param1, false);
                        double angle = param0 / p1 * 2.0 * Math.PI;
                        result = Math.cos(angle);
                    } else {
                        throw new CalculateException(Bundle.getMessage("IllegalParameter", 2, param1, getName()));
                    }

                    switch (parameterList.size()) {
                        case 2:
                            return result;
                        case 4:
                            double min = TypeConversionUtil.convertToDouble(
                                    parameterList.get(2).calculate(symbolTable), false);
                            double max = TypeConversionUtil.convertToDouble(
                                    parameterList.get(3).calculate(symbolTable), false);
                            return (result+1.0)/2.0 * (max-min) + min;
                        default:
                            throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
                    }
                }
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
            }
        });
    }

    private void addTanFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "tan", Bundle.getMessage("Math.tan_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                if (parameterList.size() == 1) {
                    double param = TypeConversionUtil.convertToDouble(
                            parameterList.get(0).calculate(symbolTable), false);
                    return Math.tan(param);
                } else if (parameterList.size() == 2) {
                    double param0 = TypeConversionUtil.convertToDouble(
                            parameterList.get(0).calculate(symbolTable), false);
                    Object param1 = parameterList.get(1).calculate(symbolTable);
                    if (param1 instanceof String) {
                        switch ((String)param1) {
                            case "rad":
                                return Math.tan(param0);
                            case "deg":
                                return Math.tan(Math.toRadians(param0));
                            default:
                                throw new CalculateException(Bundle.getMessage("IllegalParameter", 2, param1, getName()));
                        }
                    } else if (param1 instanceof Number) {
                        double p1 = TypeConversionUtil.convertToDouble(param1, false);
                        double angle = param0 / p1 * 2.0 * Math.PI;
                        return Math.tan(angle);
                    } else {
                        throw new CalculateException(Bundle.getMessage("IllegalParameter", 2, param1, getName()));
                    }
                }
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
            }
        });
    }

    private void addArctanFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "atan", Bundle.getMessage("Math.atan_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                if (parameterList.size() == 1) {
                    double param = TypeConversionUtil.convertToDouble(
                            parameterList.get(0).calculate(symbolTable), false);
                    return Math.atan(param);
                } else if (parameterList.size() == 2) {
                    double param0 = TypeConversionUtil.convertToDouble(
                            parameterList.get(0).calculate(symbolTable), false);
                    Object param1 = parameterList.get(1).calculate(symbolTable);
                    if (param1 instanceof String) {
                        switch ((String)param1) {
                            case "rad":
                                return Math.atan(param0);
                            case "deg":
                                return Math.toDegrees(Math.atan(param0));
                            default:
                                throw new CalculateException(Bundle.getMessage("IllegalParameter", 2, param1, getName()));
                        }
                    } else if (param1 instanceof Number) {
                        double p1 = TypeConversionUtil.convertToDouble(param1, false);
                        double angle = Math.atan(param0);
                        return angle * p1 / 2.0 / Math.PI;
                    } else {
                        throw new CalculateException(Bundle.getMessage("IllegalParameter", 2, param1, getName()));
                    }
                }
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
            }
        });
    }

    private void addSqrFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "sqr", Bundle.getMessage("Math.sqr_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws CalculateException, JmriException {

                switch (parameterList.size()) {
                    case 1:
                        Object value = parameterList.get(0).calculate(symbolTable);
                        if ((value instanceof java.util.concurrent.atomic.AtomicInteger)
                                || (value instanceof java.util.concurrent.atomic.AtomicLong)
                                || (value instanceof java.math.BigInteger)
                                || (value instanceof Byte)
                                || (value instanceof Short)
                                || (value instanceof Integer)
                                || (value instanceof Long)
                                || (value instanceof java.util.concurrent.atomic.LongAccumulator)
                                || (value instanceof java.util.concurrent.atomic.LongAdder)) {

                            long val = ((Number)value).longValue();
                            return val * val;
                        }
                        if ((value instanceof java.math.BigDecimal)
                                || (value instanceof Float)
                                || (value instanceof Double)
                                || (value instanceof java.util.concurrent.atomic.DoubleAccumulator)
                                || (value instanceof java.util.concurrent.atomic.DoubleAdder)) {

                            double val = ((Number)value).doubleValue();
                            return val * val;
                        }
                        double val = TypeConversionUtil.convertToDouble(value, false);
                        return val * val;
                    default:
                        throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
                }
            }
        });
    }

    private void addSqrtFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "sqrt", Bundle.getMessage("Math.sqrt_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                if (parameterList.size() == 1) {
                    double param = TypeConversionUtil.convertToDouble(
                            parameterList.get(0).calculate(symbolTable), false);
                    return Math.sqrt(param);
                } else {
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
                }
            }
        });
    }

}
