package jmri;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Base for exceptions indicating problems in {@link Programmer} operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = STABLE)
public class ProgrammerException extends JmriException {

    public ProgrammerException(String s) {
        super(s);
    }

    public ProgrammerException() {
    }

}
