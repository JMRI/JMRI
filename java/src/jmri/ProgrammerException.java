package jmri;

/**
 * Base for exceptions indicating problems in {@link Programmer} operations.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class ProgrammerException extends JmriException {

    /**
     *
     */
    private static final long serialVersionUID = 2951255959332461998L;

    public ProgrammerException(String s) {
        super(s);
    }

    public ProgrammerException() {
    }

}
