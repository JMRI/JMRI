package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * This exception is thrown when the current excection of a ConditionalNG
 * should be aborted.
 *
 * @author Daniel Bergqvist 2020
 */
public class AbortConditionalNGExecutionException extends JmriException {

    private final MaleSocket _maleSocket;

    /**
     * Constructs an instance of <code>AbortConditionalNGExecutionException</code>
     * with a cause exception.
     * @param maleSocket  the male socket where the exception is thrown
     * @param e           the cause of this exception
     */
    public AbortConditionalNGExecutionException(MaleSocket maleSocket, Exception e) {
        super(e);
        _maleSocket = maleSocket;
    }

    public MaleSocket getMaleSocket() {
        return _maleSocket;
    }
}
