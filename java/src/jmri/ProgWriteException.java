package jmri;

/**
 * Represents a failure to write when programming.
 * <p>
 * No ACK is not a failure if the implementation does not expect to see one.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class ProgWriteException extends ProgrammerException {

    public ProgWriteException(String s) {
        super(s);
    }

    public ProgWriteException() {
    }

}
