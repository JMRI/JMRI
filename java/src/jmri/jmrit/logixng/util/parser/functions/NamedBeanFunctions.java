package jmri.jmrit.logixng.util.parser.functions;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
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
        functionClasses.add(new GetLogixNGTableFunction());
        functionClasses.add(new ReadMemoryFunction());
        functionClasses.add(new EvaluateMemoryFunction());
        functionClasses.add(new WriteMemoryFunction());
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



    /**
     * Reads the value of a memory if the memory exists.
     * Return null if the memory does not exists or if the parameter cannot
     * be evaluated to a String.
     */
    public static class GetLogixNGTableFunction implements Function {

        @Override
        public String getModule() {
            return new NamedBeanFunctions().getModule();
        }

        @Override
        public String getConstantDescriptions() {
            return new NamedBeanFunctions().getConstantDescription();
        }

        @Override
        public String getName() {
            return "getLogixNGTable";
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

            return InstanceManager.getDefault(NamedTableManager.class).getNamedBean(s);
        }

        @Override
        public String getDescription() {
            return Bundle.getMessage("NamedBean.getLogixNGTable_Descr");
        }

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
        public String getConstantDescriptions() {
            return new NamedBeanFunctions().getConstantDescription();
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
        public String getConstantDescriptions() {
            return new NamedBeanFunctions().getConstantDescription();
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


    /**
     * Writes a value to a memory if the memory exists.
     * Does nothing if the memory does not exists or if the parameter cannot
     * be evaluated to a String.
     * Return the value.
     */
    public static class WriteMemoryFunction implements Function {

        @Override
        public String getModule() {
            return new NamedBeanFunctions().getModule();
        }

        @Override
        public String getConstantDescriptions() {
            return new NamedBeanFunctions().getConstantDescription();
        }

        @Override
        public String getName() {
            return "writeMemory";
        }

        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws JmriException {

            if (parameterList.size() != 2) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters2", getName(), 1));
            }

            Object memoryName = parameterList.get(0).calculate(symbolTable);
            if (memoryName == null) return null;

            String s = TypeConversionUtil.convertToString(memoryName, false);
            if (s.isEmpty()) return null;

            Memory m = InstanceManager.getDefault(MemoryManager.class).getNamedBean(s);
            if (m == null) return null;

            Object value = parameterList.get(1).calculate(symbolTable);
            m.setValue(value);

            return value;
        }

        @Override
        public String getDescription() {
            return Bundle.getMessage("NamedBean.writeMemory_Descr");
        }

    }

}
