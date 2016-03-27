package jmri;

/**
 * Represents a failure to write when programming.
 * <p>
 * No ACK is not a failure if the implementation does not expect to see one.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision$
 */
public class ProgWriteException extends ProgrammerException {

    /**
     *
     */
    private static final long serialVersionUID = 2398622573540131588L;

    public ProgWriteException(String s) {
        super(s);
    }

    public ProgWriteException() {
    }

}
