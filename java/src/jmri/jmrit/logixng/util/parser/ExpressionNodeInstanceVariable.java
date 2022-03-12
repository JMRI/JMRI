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
public class ExpressionNodeInstanceVariable implements ExpressionNodeWithParameter {

    private final String _fieldName;
    
    public ExpressionNodeInstanceVariable(String fieldName, Map<String, Variable> variables) throws IdentifierNotExistsException {
        _fieldName = fieldName;
    }
    
    @Override
    public Object calculate(Object parameter, SymbolTable symbolTable) throws JmriException {
        if (parameter == null) throw new NullPointerException("Parameter is null");
        
        try {
            Field field = parameter.getClass().getField(_fieldName);
            return field.get(parameter);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new ReflectionException("Reflection exception", ex);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean canBeAssigned() {
        // If the identifier is a local variable, assignment is possible. And
        // we don't know if the identifier is a valid local variable until the
        // expression is calculated. So we assume that it is.
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void assignValue(Object parameter, SymbolTable symbolTable, Object value) throws JmriException {
        if (parameter == null) throw new NullPointerException("Parameter is null");
        
        try {
            Field field = parameter.getClass().getField(_fieldName);
            Class<?> type = field.getType();
            Object newValue;
            if (type.isAssignableFrom(value.getClass())) newValue = value;
            else if ((type == Byte.TYPE) && (value instanceof Long)) newValue = (byte)(long)value;
            else if ((type == Short.TYPE) && (value instanceof Long)) newValue = (short)(long)value;
            else if ((type == Integer.TYPE) && (value instanceof Long)) newValue = (int)(long)value;
            else if ((type == Float.TYPE) && (value instanceof Double)) newValue = (float)(double)value;
            else throw new RuntimeException(String.format("%s cannot be assigned to %s", value.getClass().getName(), type.getName()));
            field.set(parameter, newValue);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new ReflectionException("Reflection exception", ex);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "InstanceVariable:"+_fieldName;
    }
    
}
