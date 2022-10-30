package jmri.jmrit.logixng.util.parser;

/**
 * The class is not found.
 *
 * @author Daniel Bergqvist   Copyright (C) 2022
 */
public class ClassIsNotFoundException extends ParserException {

    private final String _className;

    /**
     * Constructs an instance of <code>ClassIsNotFoundException</code>
     * with the specified detail message.
     * @param msg the detail message.
     * @param className the name of the class
     */
    public ClassIsNotFoundException(String msg, String className) {
        super(msg);
        _className = className;
    }

    public String getClassName() {
        return _className;
    }

}
