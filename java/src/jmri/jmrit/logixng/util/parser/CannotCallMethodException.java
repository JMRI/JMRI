package jmri.jmrit.logixng.util.parser;

/**
 * The method can't be called
 * 
 * @author Daniel Bergqvist   Copyright (C) 2021
 */
public class CannotCallMethodException extends ParserException {

    private final String _variableName;
    private final String _methodName;
    
    /**
     * Constructs an instance of <code>CannotCallMethodException</code> with the specified detail message.
     * @param msg the detail message.
     * @param variableName the name of the identifier
     * @param methodName the name of the method
     */
    public CannotCallMethodException(String msg, String variableName, String methodName) {
        super(msg);
        _variableName = variableName;
        _methodName = methodName;
    }
    
    public String getIdentifierName() {
        return _variableName;
    }
    
    public String getMethodName() {
        return _methodName;
    }
    
}
