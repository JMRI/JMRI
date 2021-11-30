package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;

/**
 * The parent of all parser exceptions.
 * 
 * @author Daniel Bergqvist 2019
 */
public class ParserException extends JmriException {

    /**
     * Creates a new instance of <code>ParserException</code> without detail message.
     */
    public ParserException() {
    }

    /**
     * Creates a new instance of <code>ParserException</code> without detail message.
     * @param t the cause
     */
    public ParserException(Throwable t) {
        super(t);
    }

    /**
     * Constructs an instance of <code>ParserException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ParserException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>ParserException</code> with the specified detail message.
     * @param msg the detail message.
     * @param t the cause
     */
    public ParserException(String msg, Throwable t) {
        super(msg, t);
    }
}
