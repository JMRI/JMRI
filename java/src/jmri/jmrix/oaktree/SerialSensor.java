package jmri.jmrix.oaktree;

import jmri.implementation.AbstractSensor;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Extend jmri.AbstractSensor for serial systems
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006
 */
@API(status = EXPERIMENTAL)
public class SerialSensor extends AbstractSensor {

    public SerialSensor(String systemName) {
        super(systemName);
        _knownState = UNKNOWN;
    }

    public SerialSensor(String systemName, String userName) {
        super(systemName, userName);
        _knownState = UNKNOWN;
    }

    /**
     * Request an update on status.
     * <p>
     * Since status is continually being updated, this isn't active now.
     * Eventually, we may want to have this move the related AIU to the top of
     * the polling queue.
     */
    @Override
    public void requestUpdateFromLayout() {
    }

}
