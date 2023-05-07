package jmri.jmrit.logixng.util.parser;

/**
 * The class cannot be instanciated with the supplied parameters.
 *
 * @author Daniel Bergqvist   Copyright (C) 2022
 */
public class CannotCreateInstanceException extends ParserException {

    private final String _methodName;

    /**
     * Constructs an instance of <code>CannotCreateInstanceException</code>
     * with the specified detail message.
     * @param msg the detail message.
     * @param className the name of the class
     */
    public CannotCreateInstanceException(String msg, String className) {
        super(msg);
        _methodName = className;
    }

    public String getMethodName() {
        return _methodName;
    }

}
