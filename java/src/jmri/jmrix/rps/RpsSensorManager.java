package jmri.jmrix.rps;

import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the RPS-specific Sensor implementation.
 * <p>
 * System names are "RSpppp", where ppp is a CSV representation of the region.
 *
 * @author	Bob Jacobsen Copyright (C) 2007, 2019
 */
public class RpsSensorManager extends jmri.managers.AbstractSensorManager {

    private RpsSystemConnectionMemo memo = null;
    protected String prefix = "R";

    public RpsSensorManager(RpsSystemConnectionMemo memo) {
        super();
        this.memo = memo;
        prefix = memo.getSystemPrefix();
    }

    /**
     * Get the configured system prefix for this connection.
     */
    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Create a new sensor if all checks are passed.
     * System name is normalized to ensure uniqueness.
     */
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        try {
           RpsSensor r = new RpsSensor(systemName, userName, prefix);
           Distributor.instance().addMeasurementListener(r);
           return r;
       } catch(java.lang.StringIndexOutOfBoundsException sioe){
         throw new IllegalArgumentException("Invalid System Name: " + systemName);
       }
    }

    /**
     * Static function returning the RpsSensorManager instance to use.
     *
     * @return The registered RpsSensorManager instance for general use
     * @deprecated since 4.9.7
     */
    @Deprecated
    static public RpsSensorManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(RpsSensorManager.class);

}
