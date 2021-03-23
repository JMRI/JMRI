package jmri.jmrit.logixng.util.parser.functions;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.Function;
import jmri.jmrit.logixng.util.parser.FunctionFactory;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.jmrit.logixng.util.parser.Variable;
import jmri.jmrit.logixng.util.parser.WrongNumberOfParametersException;
import jmri.util.TypeConversionUtil;

import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of NamedBean functions.
 * 
 * @author Daniel Bergqvist 2020
 */
@ServiceProvider(service = FunctionFactory.class)
public class NamedBeanFunctions implements FunctionFactory {

    @Override
    public String getModule() {
        return "NamedBean";
    }
    
    @Override
    public Set<Function> getFunctions() {
        Set<Function> functionClasses = new HashSet<>();
        functionClasses.add(new ReadMemoryFunction());
        functionClasses.add(new EvaluateMemoryFunction());
        return functionClasses;
    }
    
    
    
    /**
     * Reads the value of a memory if the memory exists.
     * Return null if the memory does not exists or if the parameter cannot
     * be evaluated to a String.
     */
    public static class ReadMemoryFunction implements Function {
        
        @Override
        public String getModule() {
            return new NamedBeanFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "readMemory";
        }
        
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws JmriException {
            
            if (parameterList.size() != 1) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
            }
            
            Object value = parameterList.get(0).calculate(symbolTable);
            if (value == null) return null;
            
            String s = TypeConversionUtil.convertToString(value, false);
            if (s.isEmpty()) return null;
            
            Memory m = InstanceManager.getDefault(MemoryManager.class).getNamedBean(s);
            if (m == null) return null;
            return m.getValue();
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("NamedBean.readMemory_Descr");
        }
        
    }
    
    /**
     * Reads the value of a memory if the memory exists and then evaluates that
     * value.
     * If the value is a reference, it evaluates that reference. Else it
     * evaluates the value as a formula.
     * Return null if the memory does not exists or if the parameter cannot
     * be evaluated to a String.
     */
    public static class EvaluateMemoryFunction implements Function {
        
        @Override
        public String getModule() {
            return new NamedBeanFunctions().getModule();
        }
        
        @Override
        public String getName() {
            return "evaluateMemory";
        }
        
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws JmriException {
            
            if (parameterList.size() != 1) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
            }
            
            Object value = parameterList.get(0).calculate(symbolTable);
            if (value == null) return null;
            
            String s = TypeConversionUtil.convertToString(value, false);
            if (s.isEmpty()) return null;
            
            Memory m = InstanceManager.getDefault(MemoryManager.class).getNamedBean(s);
            if (m == null) return null;
            value = m.getValue();
            
            if ((value instanceof String) && ReferenceUtil.isReference((String)value)) {
                return ReferenceUtil.getReference(symbolTable, (String)value);
            }
            
            s = TypeConversionUtil.convertToString(value, false);
            
            Map<String, Variable> variables = new HashMap<>();
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            ExpressionNode expressionNode = parser.parseExpression(s);
            return expressionNode.calculate(symbolTable);
        }
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("NamedBean.evaluateMemory_Descr");
        }
        
    }
    
}
