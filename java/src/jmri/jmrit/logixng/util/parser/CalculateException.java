package jmri.jmrit.logixng.util.parser;

/**
 * Exception thrown when the calculation failed.
 * 
 * @author Daniel Bergqvist 2019
 */
public class CalculateException extends ParserException {

    /**
     * Constructs an instance of <code>CalculateException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CalculateException(String msg) {
        super(msg);
    }
}
