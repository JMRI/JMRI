package jmri.jmrit.logixng.util.parser;

/**
 * Reflection error.
 * 
 * @author Daniel Bergqvist   Copyright (C) 2021
 */
public class ReflectionException extends ParserException {

    /**
     * Constructs an instance of <code>ReflectionException</code> with the specified detail message.
     * @param msg the detail message.
     * @param t the cause
     */
    public ReflectionException(String msg, Throwable t) {
        super(msg, t);
    }
    
}
