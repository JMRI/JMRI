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
        functionClasses.add(new IsIntFunction());
        functionClasses.add(new IsFloatFunction());
        functionClasses.add(new IntFunction());
        functionClasses.add(new FloatFunction());
        functionClasses.add(new StrFunction());
        functionClasses.add(new Hex2DecFunction());
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
    
    
    
    public static class IsIntFunction implements Function {
        
        @Override
        public String getModule() {
            return new ConvertFunctions().getModule();
        }
        
        @Override
        public String getConstantDescriptions() {
            return new ConvertFunctions().getConstantDescription();
        }
        
        @Override
        public String getName() {
            return "isInt";
        }
        
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
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("Convert.isInt");
        }
        
    }
    
    public static class IsFloatFunction implements Function {
        
        @Override
        public String getModule() {
            return new ConvertFunctions().getModule();
        }
        
        @Override
        public String getConstantDescriptions() {
            return new ConvertFunctions().getConstantDescription();
        }
        
        @Override
        public String getName() {
            return "isFloat";
        }
        
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
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("Convert.isFloat");
        }
        
    }
    
    public static class IntFunction implements Function {
        
        @Override
        public String getModule() {
            return new ConvertFunctions().getModule();
        }
        
        @Override
        public String getConstantDescriptions() {
            return new ConvertFunctions().getConstantDescription();
        }
        
        @Override
        public String getName() {
            return "int";
        }
        
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws JmriException {
            
            if (parameterList.size() != 1) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
            }
            return (int) TypeConversionUtil.convertToLong(
                    parameterList.get(0).calculate(symbolTable));
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("Convert.int");
        }
        
    }
    
    public static class FloatFunction implements Function {
        
        @Override
        public String getModule() {
            return new ConvertFunctions().getModule();
        }
        
        @Override
        public String getConstantDescriptions() {
            return new ConvertFunctions().getConstantDescription();
        }
        
        @Override
        public String getName() {
            return "float";
        }
        
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
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("Convert.float");
        }
        
    }
    
    public static class StrFunction implements Function {
        
        @Override
        public String getModule() {
            return new ConvertFunctions().getModule();
        }
        
        @Override
        public String getConstantDescriptions() {
            return new ConvertFunctions().getConstantDescription();
        }
        
        @Override
        public String getName() {
            return "str";
        }
        
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
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("Convert.str_Descr");
        }
        
    }
    
    public static class Hex2DecFunction implements Function {
        
        @Override
        public String getModule() {
            return new ConvertFunctions().getModule();
        }
        
        @Override
        public String getConstantDescriptions() {
            return new ConvertFunctions().getConstantDescription();
        }
        
        @Override
        public String getName() {
            return "hex2dec";
        }
        
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
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("Convert.hex2dec");
        }
        
    }
    
}
