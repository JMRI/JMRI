package jmri.jmrit.logixng.tools;

import jmri.JmriException;

/**
 * Exception thrown when the ConditionalVariable has invalid data.
 *
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public class InvalidConditionalActionException extends JmriException {
    
    public InvalidConditionalActionException(String s, Throwable t) {
        super(s, t);
    }

    public InvalidConditionalActionException(String s) {
        super(s);
    }

    public InvalidConditionalActionException(Throwable t) {
        super(t);
    }

    public InvalidConditionalActionException() {
    }

}
