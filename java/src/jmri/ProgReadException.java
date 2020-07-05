package jmri;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Represents a failure to read when programming.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
@API(status = STABLE)
public class ProgReadException extends ProgrammerException {

    public ProgReadException(String s) {
        super(s);
    }

    public ProgReadException() {
    }

}
