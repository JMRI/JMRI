// NceSensor.java

package jmri.jmrix.nce;

import jmri.AbstractSensor;
import jmri.Sensor;

/**
 * Extend jmri.AbstractSensor for NCE systems
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class NceSensor extends AbstractSensor {

    public NceSensor(String id) {
        super(id);
    }

    public void dispose() {}

    /**
     * Request an update on status.
     * <P>
     * Since status is continually
     * being updated, this isn't active now.  Eventually, we may
     * want to have this move the related AIU to the top of the
     * polling queue.
     */
    public void requestUpdateFromLayout() {
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceSensor.class.getName());

}

/* @(#)NceSensor.java */
