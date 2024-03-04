package jmri.jmrit.logixng.util.parser.functions;

import java.util.*;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.util.parser.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of common functions.
 *
 * @author Daniel Bergqvist 2024
 */
@ServiceProvider(service = FunctionFactory.class)
public class CommonFunctions implements FunctionFactory {

    @Override
    public String getModule() {
        return "Common";
    }

    @Override
    public Set<Function> getFunctions() {
        Set<Function> functionClasses = new HashSet<>();
        functionClasses.add(new LengthFunction());
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



    public static class LengthFunction implements Function {

        @Override
        public String getModule() {
            return new CommonFunctions().getModule();
        }

        @Override
        public String getConstantDescriptions() {
            return new CommonFunctions().getConstantDescription();
        }

        @Override
        public String getName() {
            return "length";
        }

        @SuppressWarnings("rawtypes")   // We don't know the generic types of Collection and Map in this method
        @Override
        public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                throws CalculateException, JmriException {
            if (parameterList.size() != 1) {
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName(), 1));
            }

            Object parameter = parameterList.get(0).calculate(symbolTable);

            if (parameter == null) {
                throw new NullPointerException("Parameter is null");
            } else if (parameter instanceof String) {
                return ((String)parameter).length();
            } else if (parameter.getClass().isArray()) {
                return ((Object[])parameter).length;
            } else if (parameter instanceof Collection) {
                return ((Collection)parameter).size();
            } else if (parameter instanceof Map) {
                return ((Map)parameter).size();
            }

            throw new IllegalArgumentException("Parameter is not a String, Array or a List, Set or Map: "+parameter.getClass().getName());
        }

        @Override
        public String getDescription() {
            return Bundle.getMessage("Common.length_Descr");
        }

    }

}
