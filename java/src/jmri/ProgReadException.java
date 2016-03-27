package jmri;

/**
 * Represents a failure to read when programming.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 */
public class ProgReadException extends ProgrammerException {

    /**
     *
     */
    private static final long serialVersionUID = 7027364754748803248L;

    public ProgReadException(String s) {
        super(s);
    }

    public ProgReadException() {
    }

}
