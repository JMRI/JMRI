package jmri.util.prefs;

/**
 * An exception thrown then there is a configured connection, but JMRI is
 * unable to open that connection. For example, a LocoNet connection with a
 * LocoBufferUSB is configured but the LocoBufferUSB is not connected.
 */
public class HasConnectionButUnableToConnectException extends InitializationException {

    public HasConnectionButUnableToConnectException(String message, String localized) {
        super(message, localized);
    }

    public HasConnectionButUnableToConnectException(String message, String localized, Throwable cause) {
        super(message, localized, cause);
    }

    public HasConnectionButUnableToConnectException(Throwable cause) {
        super(cause);
    }
}
