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
public class ExpressionNodeInstanceVariable implements ExpressionNode {

    private final String _variableName;
    private final String _fieldName;
    private final Variable _variable;
    
    public ExpressionNodeInstanceVariable(String variableName, String fieldName, Map<String, Variable> variables) throws IdentifierNotExistsException {
        _variableName = variableName;
        _fieldName = fieldName;
        Variable variable = variables.get(variableName);
        
        if (variable == null) {
            // Assume the identifier is a local variable.
            // Local variables may not be known when the expression is parsed.
            
            variable = new LocalVariableExpressionVariable(_variableName);
        }
        
        _variable = variable;
    }
    
    public String getIdentifier() {
        return _variableName;
    }
    
    @Override
    public Object calculate(SymbolTable symbolTable) throws JmriException {
        Object obj = _variable.getValue(symbolTable);
        if (obj == null) throw new NullPointerException("Identifier "+_variable.getName()+" is null");
        
        try {
            Field field = obj.getClass().getField(_fieldName);
            if (obj == null) throw new NullPointerException("Identifier "+_variable.getName()+" is null");
            return field.get(obj);
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
    public void assignValue(SymbolTable symbolTable, Object value) throws JmriException {
        Object obj = _variable.getValue(symbolTable);
        if (obj == null) throw new NullPointerException("Identifier "+_variable.getName()+" is null");
        
        try {
            Field field = obj.getClass().getField(_fieldName);
            if (obj == null) throw new NullPointerException("Identifier "+_variable.getName()+" is null");
            Class<?> type = field.getType();
            Object newValue;
            if (type.isAssignableFrom(value.getClass())) newValue = value;
            else if ((type == Byte.TYPE) && (value instanceof Long)) newValue = (byte)(long)value;
            else if ((type == Short.TYPE) && (value instanceof Long)) newValue = (short)(long)value;
            else if ((type == Integer.TYPE) && (value instanceof Long)) newValue = (int)(long)value;
            else if ((type == Float.TYPE) && (value instanceof Double)) newValue = (float)(double)value;
            else throw new RuntimeException(String.format("%s cannot be assigned to %s", value.getClass().getName(), type.getName()));
            field.set(obj, newValue);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new ReflectionException("Reflection exception", ex);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "InstanceVariable:"+_variableName+"."+_fieldName;
    }
    
}
