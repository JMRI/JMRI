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

        addGetLogixNGTableFunction(functionClasses);
        addReadMemoryFunction(functionClasses);
        addEvaluateMemoryFunction(functionClasses);
        addWriteMemoryFunction(functionClasses);
        addEvaluateReferenceFunction(functionClasses);

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

    private void addGetLogixNGTableFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "getLogixNGTable", Bundle.getMessage("NamedBean.getLogixNGTable_Descr")) {
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
        });
    }

    private void addReadMemoryFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "readMemory", Bundle.getMessage("NamedBean.readMemory_Descr")) {
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
        });
    }

    private void addEvaluateMemoryFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "evaluateMemory", Bundle.getMessage("NamedBean.evaluateMemory_Descr")) {
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

                return value;
            }
        });
    }

    private void addWriteMemoryFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "writeMemory", Bundle.getMessage("NamedBean.writeMemory_Descr")) {
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
        });
    }

    private void addEvaluateReferenceFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "evaluateReference", Bundle.getMessage("NamedBean.evaluateReference_Descr")) {
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

                if (ReferenceUtil.isReference(s)) {
                    return ReferenceUtil.getReference(symbolTable, s);
                }

                return value;
            }
        });
    }

}
