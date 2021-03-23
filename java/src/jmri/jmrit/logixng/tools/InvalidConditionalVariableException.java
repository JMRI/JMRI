package jmri.jmrit.logixng.tools;

import jmri.JmriException;

/**
 * Exception thrown when the ConditionalVariable has invalid data.
 *
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public class InvalidConditionalVariableException extends JmriException {
    
    public InvalidConditionalVariableException(String s, Throwable t) {
        super(s, t);
    }

    public InvalidConditionalVariableException(String s) {
        super(s);
    }

    public InvalidConditionalVariableException(Throwable t) {
        super(t);
    }

    public InvalidConditionalVariableException() {
    }

}
