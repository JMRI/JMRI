package jmri.jmrit.logixng.util.parser.functions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.util.parser.CalculateException;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.Function;
import jmri.jmrit.logixng.util.parser.FunctionFactory;
import jmri.jmrit.logixng.util.parser.WrongNumberOfParametersException;
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
        functionClasses.add(new RandomFunction());
        functionClasses.add(new SinFunction());
        return functionClasses;
    }
    
    
    
    public static class RandomFunction implements Function {
        
        @Override
        public String getModule() {
            return new MathFunctions().getModule();
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
    
    public static class SinFunction implements Function {
        
        @Override
        public String getModule() {
            return new MathFunctions().getModule();
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
                    double angle = param0 * p1 / 2 / Math.PI;
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
                        return result * (max-min) + min;
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
