package jmri.jmrit.logixng.util.parser.functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.util.parser.*;
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

        addFormatFunction(functionClasses);
        addRegExFunction(functionClasses);
        addStrlenFunction(functionClasses);

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

    private void addFormatFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "format", Bundle.getMessage("String.format_Descr")) {
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
        });
    }

    private void addRegExFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "regex", Bundle.getMessage("String.regex_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws CalculateException, JmriException {
                if (parameterList.size() != 2) {
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName(), 1));
                }

                Object regex = parameterList.get(0).calculate(symbolTable);
                Object value = parameterList.get(1).calculate(symbolTable);

                if (regex == null) {
                    throw new NullPointerException("Regular expression is null");
                } else if (value == null) {
                    throw new NullPointerException("Value is null");
                } else if (! (regex instanceof String)) {
                    throw new IllegalArgumentException("Parameter is not a String: "+regex.getClass().getName());
                } else if (! (value instanceof String)) {
                    throw new IllegalArgumentException("Value is not a String: "+value.getClass().getName());
                }

                Pattern p = Pattern.compile((String) regex);
                Matcher m = p.matcher((String) value);

                if (!m.matches()) {
                    return null;
                }

                List<String> list = new ArrayList<>();
                for (int i=1; i <= m.groupCount(); i++) {
                    list.add(m.group(i));
                }
                return list;
            }
        });
    }

    private void addStrlenFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "strlen", Bundle.getMessage("String.strlen_Descr")) {
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
                }

                throw new IllegalArgumentException("Parameter is not a String: "+parameter.getClass().getName());
            }
        });
    }

}
