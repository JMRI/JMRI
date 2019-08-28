package jmri.jmrit.logixng.util.parser;

/**
 * The parent of all parser exceptions.
 * 
 * @author Daniel Bergqvist 2019
 */
public class ParserException extends Exception {

    /**
     * Creates a new instance of <code>ParserException</code> without detail message.
     */
    public ParserException() {
    }


    /**
     * Constructs an instance of <code>ParserException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ParserException(String msg) {
        super(msg);
    }
}
