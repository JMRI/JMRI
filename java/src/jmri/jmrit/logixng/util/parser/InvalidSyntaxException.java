package jmri.jmrit.logixng.util.parser;

/**
 * Invalid syntax.
 *
 * @author Daniel Bergqvist   Copyright (C) 2020
 */
public class InvalidSyntaxException extends ParserException {

    private final int _position;

    /**
     * Constructs an instance of <code>InvalidExpressionException</code> with the specified detail message.
     * @param msg the detail message.
     * @param position the position
     */
    public InvalidSyntaxException(String msg, int position) {
        super(msg);
        _position = position;
    }

    public int getPosition() {
        return _position;
    }

}
