package jmri.jmrix.nce;

import jmri.implementation.AbstractSensor;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Extend jmri.AbstractSensor for NCE systems
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
@API(status = EXPERIMENTAL)
public class NceSensor extends AbstractSensor {

    public NceSensor(String systemName) {
        super(systemName);
    }

    public NceSensor(String systemName, String userName) {
        super(systemName, userName);
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
