package jmri.jmrix;

/**
 * An exception thrown then there is a configured connection, but JMRI is
 * unable to open that connection. For example, a LocoNet connection with a
 * LocoBufferUSB is configured but the LocoBufferUSB is not connected.
 */
public class HasConnectionButUnableToConnectException extends Exception {

    /**
     * Creates a new instance of <code>HasConnectionButUnableToConnectException</code> without detail message.
     */
    public HasConnectionButUnableToConnectException() {
    }


    /**
     * Constructs an instance of <code>HasConnectionButUnableToConnectException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public HasConnectionButUnableToConnectException(String msg) {
        super(msg);
    }
}
