package jmri.jmrix.rps;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Connect to a source of Measurements.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
@API(status = EXPERIMENTAL)
public interface MeasurementListener {

    public void notify(Measurement r);

}
