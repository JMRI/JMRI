package jmri.jmrit.logixng.util.parser.functions;

import java.util.ArrayList;
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
 * Implementation of string functions.
 * 
 * @author Daniel Bergqvist 2020
 */
@ServiceProvider(service = FunctionFactory.class)
public class StringFunctions implements FunctionFactory {

    @Override
    public String getModule() {
        return "String";
    }
    
    @Override
    public Set<Function> getFunctions() {
        Set<Function> functionClasses = new HashSet<>();
        functionClasses.add(new FormatFunction());
        return functionClasses;
    }
    
    
    
    public static class FormatFunction implements Function {
        
        @Override
        public String getModule() {
            return new StringFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "format";
        }
        
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws CalculateException, JmriException {
            if (parameterList.isEmpty()) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName(), 1));
            }
            
            String formatStr = TypeConversionUtil.convertToString(
                    parameterList.get(0).calculate(symbolTable), false);
            
            List<Object> list = new ArrayList<>();
            for (int i=1; i < parameterList.size(); i++) {
                list.add(parameterList.get(i).calculate(symbolTable));
            }
            
            return String.format(formatStr, list.toArray());
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("String.format_Descr");
        }
        
    }
    
}
