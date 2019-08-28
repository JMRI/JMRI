package jmri.jmrit.logixng.util.parser;

/**
 * Exception thrown when the calculation failed.
 * 
 * @author Daniel Bergqvist 2019
 */
public class WrongNumberOfParametersException extends ParserException {

    /**
     * Constructs an instance of <code>CalculateException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public WrongNumberOfParametersException(String msg) {
        super(msg);
    }
}
