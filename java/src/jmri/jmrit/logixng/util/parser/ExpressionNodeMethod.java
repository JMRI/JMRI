package jmri.jmrit.logixng.util.parser;

import java.lang.reflect.*;
import java.util.*;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression
 *
 * @author Daniel Bergqvist 2021
 */
public class ExpressionNodeMethod implements ExpressionNodeWithParameter {

    private final String _method;
    private final List<ExpressionNode> _parameterList;

    public ExpressionNodeMethod(String method, Map<String, Variable> variables,
            List<ExpressionNode> parameterList) throws FunctionNotExistsException {
        _method = method;
        _parameterList = parameterList;
    }

    private boolean isAssignableFrom(Class<?> type, Object param) {
        if (param == null) return true;
        if (type.isAssignableFrom(param.getClass())) return true;

        if ((type == Byte.TYPE) && (param instanceof Byte)) return true;
        if ((type == Short.TYPE) && (param instanceof Byte)) return true;
        if ((type == Integer.TYPE) && (param instanceof Byte)) return true;
        if ((type == Long.TYPE) && (param instanceof Byte)) return true;
        if ((type == Float.TYPE) && (param instanceof Byte)) return true;
        if ((type == Double.TYPE) && (param instanceof Byte)) return true;

        if ((type == Byte.TYPE) && (param instanceof Short)) return true;
        if ((type == Short.TYPE) && (param instanceof Short)) return true;
        if ((type == Integer.TYPE) && (param instanceof Short)) return true;
        if ((type == Long.TYPE) && (param instanceof Short)) return true;
        if ((type == Float.TYPE) && (param instanceof Short)) return true;
        if ((type == Double.TYPE) && (param instanceof Short)) return true;

        if ((type == Byte.TYPE) && (param instanceof Integer)) return true;
        if ((type == Short.TYPE) && (param instanceof Integer)) return true;
        if ((type == Integer.TYPE) && (param instanceof Integer)) return true;
        if ((type == Long.TYPE) && (param instanceof Integer)) return true;
        if ((type == Float.TYPE) && (param instanceof Integer)) return true;
        if ((type == Double.TYPE) && (param instanceof Integer)) return true;

        if ((type == Byte.TYPE) && (param instanceof Long)) return true;
        if ((type == Short.TYPE) && (param instanceof Long)) return true;
        if ((type == Integer.TYPE) && (param instanceof Long)) return true;
        if ((type == Long.TYPE) && (param instanceof Long)) return true;
        if ((type == Float.TYPE) && (param instanceof Long)) return true;
        if ((type == Double.TYPE) && (param instanceof Long)) return true;

        if ((type == Float.TYPE) && (param instanceof Float)) return true;
        if ((type == Double.TYPE) && (param instanceof Float)) return true;

        if ((type == Float.TYPE) && (param instanceof Double)) return true;
        return ((type == Double.TYPE) && (param instanceof Double));
    }

    private boolean canCall(Method m, Object[] params) {
        Class<?>[] paramTypes = m.getParameterTypes();
        if (paramTypes.length != params.length) return false;
        for (int i=0; i < paramTypes.length; i++) {
            if (!isAssignableFrom(paramTypes[i], params[i])) return false;
        }
        return true;
    }

    @SuppressWarnings("rawtypes")   // We don't know the generic types of Map.Entry in this method
    private Object callMethod(Method method, Object obj, Object[] params)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] newParams = new Object[params.length];
        for (int i=0; i < params.length; i++) {
            Object newParam;
            if ((params[i] == null) || (paramTypes[i].isAssignableFrom(params[i].getClass()))) {
                newParam = params[i];
            }

            else if ((paramTypes[i] == Byte.TYPE) && (params[i] instanceof Byte)) newParam = params[i];
            else if ((paramTypes[i] == Short.TYPE) && (params[i] instanceof Byte)) newParam = (short)(byte)params[i];
            else if ((paramTypes[i] == Integer.TYPE) && (params[i] instanceof Byte)) newParam = (int)(byte)params[i];
            else if ((paramTypes[i] == Long.TYPE) && (params[i] instanceof Byte)) newParam = (long)(byte)params[i];
            else if ((paramTypes[i] == Float.TYPE) && (params[i] instanceof Byte)) newParam = (float)(byte)params[i];
            else if ((paramTypes[i] == Double.TYPE) && (params[i] instanceof Byte)) newParam = (double)(byte)params[i];

            else if ((paramTypes[i] == Byte.TYPE) && (params[i] instanceof Short)) newParam = (byte)(short)params[i];
            else if ((paramTypes[i] == Short.TYPE) && (params[i] instanceof Short)) newParam = params[i];
            else if ((paramTypes[i] == Integer.TYPE) && (params[i] instanceof Short)) newParam = (int)(short)params[i];
            else if ((paramTypes[i] == Long.TYPE) && (params[i] instanceof Short)) newParam = (long)(short)params[i];
            else if ((paramTypes[i] == Float.TYPE) && (params[i] instanceof Short)) newParam = (float)(short)params[i];
            else if ((paramTypes[i] == Double.TYPE) && (params[i] instanceof Short)) newParam = (double)(short)params[i];

            else if ((paramTypes[i] == Byte.TYPE) && (params[i] instanceof Integer)) newParam = (byte)(int)params[i];
            else if ((paramTypes[i] == Short.TYPE) && (params[i] instanceof Integer)) newParam = (short)(int)params[i];
            else if ((paramTypes[i] == Integer.TYPE) && (params[i] instanceof Integer)) newParam = params[i];
            else if ((paramTypes[i] == Long.TYPE) && (params[i] instanceof Integer)) newParam = (long)(int)params[i];
            else if ((paramTypes[i] == Float.TYPE) && (params[i] instanceof Integer)) newParam = (float)(int)params[i];
            else if ((paramTypes[i] == Double.TYPE) && (params[i] instanceof Integer)) newParam = (double)(int)params[i];

            else if ((paramTypes[i] == Byte.TYPE) && (params[i] instanceof Long)) newParam = (byte)(long)params[i];
            else if ((paramTypes[i] == Short.TYPE) && (params[i] instanceof Long)) newParam = (short)(long)params[i];
            else if ((paramTypes[i] == Integer.TYPE) && (params[i] instanceof Long)) newParam = (int)(long)params[i];
            else if ((paramTypes[i] == Long.TYPE) && (params[i] instanceof Long)) newParam = params[i];
            else if ((paramTypes[i] == Float.TYPE) && (params[i] instanceof Long)) newParam = (float)(long)params[i];
            else if ((paramTypes[i] == Double.TYPE) && (params[i] instanceof Long)) newParam = (double)(long)params[i];

            else if ((paramTypes[i] == Float.TYPE) && (params[i] instanceof Float)) newParam = params[i];
            else if ((paramTypes[i] == Double.TYPE) && (params[i] instanceof Float)) newParam = (double)(float)params[i];

            else if ((paramTypes[i] == Float.TYPE) && (params[i] instanceof Double)) newParam = (float)(double)params[i];
            else if ((paramTypes[i] == Double.TYPE) && (params[i] instanceof Double)) newParam = params[i];

            else throw new RuntimeException(String.format("%s can not be assigned to %s", params[i].getClass().getName(), paramTypes[i].getName()));

            newParams[i] = newParam;
        }
        try {
            return method.invoke(obj, newParams);
        } catch (IllegalAccessException ex) {
            // https://stackoverflow.com/questions/50306093/java-9-calling-map-entrygetvalue-via-reflection#comment87628501_50306192
            // https://stackoverflow.com/a/12038265
            if (obj instanceof Map.Entry && newParams.length == 0) {
                switch (method.getName()) {
                    case "toString": return obj.toString();
                    case "getKey": return ((Map.Entry)obj).getKey();
                    case "getValue": return ((Map.Entry)obj).getValue();
                    default: throw ex;
                }
            }
            throw ex;
        }
    }

    @Override
    public Object calculate(Object parameter, SymbolTable symbolTable) throws JmriException {
        if (parameter == null) throw new NullPointerException("Parameter is null");

        Method[] methods = parameter.getClass().getMethods();
        List<Object> parameters = new ArrayList<>();
        for (ExpressionNode exprNode : _parameterList) {
            parameters.add(exprNode.calculate(symbolTable));
        }
        Object[] params = parameters.toArray();

        Exception exception = null;
        for (Method m : methods) {
            if (!m.getName().equals(_method)) continue;
            try {
                if (canCall(m, params)) return callMethod(m, parameter, params);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                exception = ex;
            }
        }

        if (exception != null) throw new ReflectionException("Reflection exception", exception);

        List<String> paramList = new ArrayList<>();
        for (Object o : params) paramList.add(String.format("%s:%s", o, o != null ? o.getClass().getName() : "null"));
        throw new CannotCallMethodException(String.format("Can not call method %s(%s) on object %s", _method, String.join(", ", paramList), parameter), _method);
    }

    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        StringBuilder str = new StringBuilder();
        str.append("Method:");
        str.append(_method);
        str.append("(");
        for (int i=0; i < _parameterList.size(); i++) {
            if (i > 0) {
                str.append(",");
            }
            str.append(_parameterList.get(i).getDefinitionString());
        }
        str.append(")");
        return str.toString();
    }

}
