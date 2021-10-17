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
public class ExpressionNodeMethod implements ExpressionNode {

    private final String _variableName;
    private final Variable _variable;
    private final String _method;
    private final List<ExpressionNode> _parameterList;
    
    
    public ExpressionNodeMethod(String variableName, String method, Map<String, Variable> variables, List<ExpressionNode> parameterList) throws FunctionNotExistsException {
        _variableName = variableName;
        Variable variable = variables.get(variableName);
        
        if (variable == null) {
            // Assume the identifier is a local variable.
            // Local variables may not be known when the expression is parsed.
            
            variable = new LocalVariableExpressionVariable(variableName);
        }
        
        _variable = variable;
        
        
        _method = method;
        _parameterList = parameterList;
        
//        System.err.format("Function %s, %s%n", _function.getName(), _function.getClass().getName());
    }
    
    private boolean isAssignableFrom(Class<?> type, Object param) {
        if (type.isAssignableFrom(param.getClass())) return true;
        if ((type == Byte.TYPE) && (param instanceof Long)) return true;
        if ((type == Short.TYPE) && (param instanceof Long)) return true;
        if ((type == Integer.TYPE) && (param instanceof Long)) return true;
        return ((type == Float.TYPE) && (param instanceof Double));
    }
    
    private boolean canCall(Method m, Object[] params) {
        Class[] paramTypes = m.getParameterTypes();
        if (paramTypes.length != params.length) return false;
        for (int i=0; i < paramTypes.length; i++) {
            if (!isAssignableFrom(paramTypes[i], params[i])) return false;
        }
        return true;
    }
    
    private Object callMethod(Method method, Object obj, Object[] params)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] newParams = new Object[params.length];
        for (int i=0; i < params.length; i++) {
            Object newParam;
            if (paramTypes[i].isAssignableFrom(params[i].getClass())) newParam = params[i];
            else if ((paramTypes[i] == Byte.TYPE) && (params[i] instanceof Long)) newParam = (byte)(long)params[i];
            else if ((paramTypes[i] == Short.TYPE) && (params[i] instanceof Long)) newParam = (short)(long)params[i];
            else if ((paramTypes[i] == Integer.TYPE) && (params[i] instanceof Long)) newParam = (int)(long)params[i];
            else if ((paramTypes[i] == Float.TYPE) && (params[i] instanceof Double)) newParam = (float)(double)params[i];
            else throw new RuntimeException(String.format("%s cannot be assigned to %s", params[i].getClass().getName(), paramTypes[i].getName()));
            newParams[i] = newParam;
        }
        return method.invoke(obj, newParams);
    }
    
    @Override
    public Object calculate(SymbolTable symbolTable) throws JmriException {
        Object obj = _variable.getValue(symbolTable);
        if (obj == null) throw new NullPointerException("Identifier "+_variable.getName()+" is null");
        
        Method[] methods = obj.getClass().getMethods();
        List<Object> parameters = new ArrayList<>();
        for (ExpressionNode exprNode : _parameterList) {
            parameters.add(exprNode.calculate(symbolTable));
        }
        Object[] params = parameters.toArray();
        
        Exception exception = null;
        for (Method m : methods) {
            if (!m.getName().equals(_method)) continue;
            try {
                if (canCall(m, params)) return callMethod(m, obj, params);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                exception = ex;
            }
        }
        if (exception != null) throw new ReflectionException("Reflection exception", exception);
        List<String> paramList = new ArrayList<>();
        for (Object o : params) paramList.add(String.format("%s:%s", o, o != null ? o.getClass().getName() : "null"));
        throw new CannotCallMethodException(String.format("Can not call method %s.%s(%s)", _variableName, _method, String.join(", ", paramList)), _variableName, _method);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        StringBuilder str = new StringBuilder();
        str.append("Method:");
        str.append(_variableName);
        str.append(".");
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
