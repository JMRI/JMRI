package jmri.jmrit.logixng.util.parser.functions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmri.JmriException;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.Function;
import jmri.jmrit.logixng.util.parser.FunctionFactory;
import jmri.jmrit.logixng.util.parser.WrongNumberOfParametersException;
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
        functionClasses.add(new IntFunction());
        functionClasses.add(new StrFunction());
        return functionClasses;
    }
    
    
    
    public static class IntFunction implements Function {
        
        @Override
        public String getModule() {
            return new ConvertFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "int";
        }
        
        @Override
        public Object calculate(List<ExpressionNode> parameterList) throws JmriException {
            if (parameterList.size() != 1) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
            }
            return (int) TypeConversionUtil.convertToLong(parameterList.get(0).calculate());
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("Convert.int");
        }
        
    }
    
    public static class StrFunction implements Function {
        
        @Override
        public String getModule() {
            return new ConvertFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "str";
        }
        
        @Override
        public Object calculate(List<ExpressionNode> parameterList) throws JmriException {
            switch (parameterList.size()) {
                case 1:
                    return TypeConversionUtil.convertToString(parameterList.get(0).calculate(), false);
                case 2:
                    boolean do_i18n = TypeConversionUtil.convertToBoolean(parameterList.get(0).calculate(), false);
                    return TypeConversionUtil.convertToString(parameterList.get(0).calculate(), do_i18n);
                default:
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
            }
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("Convert.str_Descr");
        }
        
    }
    
}
