package jmri.jmrix.rps;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Connect to a source of Readings.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
@API(status = EXPERIMENTAL)
public interface ReadingListener {

    public void notify(Reading r);

}
