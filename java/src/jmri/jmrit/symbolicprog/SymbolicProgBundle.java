package jmri.jmrit.symbolicprog;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Common access to the SymbolicProgBundle of properties.
 *
 * Putting this in a class allows it to be loaded only once.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.4
 */
@API(status = MAINTAINED)
public class SymbolicProgBundle {

    static public String getMessage(String key) {
        return Bundle.getMessage(key);
    }

    static public String getMessage(String key, Object... subs) {
        return Bundle.getMessage(key, subs);
    }
}
