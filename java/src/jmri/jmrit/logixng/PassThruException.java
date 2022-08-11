package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * This exception should not be handled unless in some special conditions.
 *
 * @author Daniel Bergqvist 2022
 */
public abstract class PassThruException extends JmriException {

    /**
     * Creates a new instance of <code>BreakException</code> without detail message.
     */
    public PassThruException() {
    }

}
