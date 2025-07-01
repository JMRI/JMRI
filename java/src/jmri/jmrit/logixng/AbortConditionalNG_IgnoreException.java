package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * This exception is thrown when the current excection of a ConditionalNG
 * should be aborted but the error should not be logged.
 *
 * @author Daniel Bergqvist 2025
 */
public class AbortConditionalNG_IgnoreException extends JmriException {

    private final MaleSocket _maleSocket;

    /**
     * Constructs an instance of <code>AbortConditionalNG_IgnoreException</code>
     * with a cause exception.
     * @param maleSocket  the male socket where the exception is thrown
     * @param e           the cause of this exception
     */
    public AbortConditionalNG_IgnoreException(MaleSocket maleSocket, Exception e) {
        super(e);
        _maleSocket = maleSocket;
    }

    public MaleSocket getMaleSocket() {
        return _maleSocket;
    }
}
