package jmri;

/**
 * Base for exceptions indicating problems in {@link Programmer} operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class ProgrammerException extends JmriException {

    public ProgrammerException(String s) {
        super(s);
    }

    public ProgrammerException() {
    }

}
