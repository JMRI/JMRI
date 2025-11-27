package jmri.jmrit.logixng;

/**
 * This exception is thrown by the ValidationError action.
 *
 * @author Daniel Bergqvist 2025
 */
public class ValidationErrorException extends PassThruException {

    /**
     * Creates a new instance of <code>ValidationError</code> without detail message.
     */
    public ValidationErrorException() {
    }

    /**
     * Creates a new instance of <code>ValidationError</code> with a detail message.
     * @param msg the message
     */
    public ValidationErrorException(String msg) {
        super(msg);
    }

}
