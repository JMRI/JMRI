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
        functionClasses.add(new AbsFunction());
        functionClasses.add(new RandomFunction());
        functionClasses.add(new SinFunction());
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



    public static class RandomFunction implements Function {

        @Override
        public String getModule() {
            return new MathFunctions().getModule();
        }

        @Override
        public String getConstantDescriptions() {
            return new MathFunctions().getConstantDescription();
        }

        @Override
        public String getName() {
            return "random";
        }

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

        @Override
        public String getDescription() {
            return Bundle.getMessage("Math.random_Descr");
        }

    }

    public static class AbsFunction implements Function {

        @Override
        public String getModule() {
            return new MathFunctions().getModule();
        }

        @Override
        public String getConstantDescriptions() {
            return new MathFunctions().getConstantDescription();
        }

        @Override
        public String getName() {
            return "abs";
        }

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

        @Override
        public String getDescription() {
            return Bundle.getMessage("Math.abs_Descr");
        }

    }

    public static class SinFunction implements Function {

        @Override
        public String getModule() {
            return new MathFunctions().getModule();
        }

        @Override
        public String getConstantDescriptions() {
            return new MathFunctions().getConstantDescription();
        }

        @Override
        public String getName() {
            return "sin";
        }

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

        @Override
        public String getDescription() {
            return Bundle.getMessage("Math.sin_Descr");
        }

    }

}
