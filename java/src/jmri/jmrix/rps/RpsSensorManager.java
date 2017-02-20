package jmri.jmrix.rps;

import jmri.Sensor;

/**
 * Manage the RPS-specific Sensor implementation.
 * <P>
 * System names are "RSpppp", where ppp is a CSV representation of the region.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2007
  */
public class RpsSensorManager extends jmri.managers.AbstractSensorManager {

    public RpsSensorManager() {
        super();
    }

    @Override
    public String getSystemPrefix() {
        return "R";
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        try {
           RpsSensor r = new RpsSensor(systemName, userName);
           Distributor.instance().addMeasurementListener(r);
           return r;
       } catch(java.lang.StringIndexOutOfBoundsException sioe){
         throw new IllegalArgumentException("Invaid System Name: " + systemName);
       }
    }

    static public RpsSensorManager instance() {
        if (_instance == null) {
            _instance = new RpsSensorManager();
        }
        return _instance;
    }

    static RpsSensorManager _instance = null;

    static { // class initialization
        // now want a SensorManager always, not just when RPS is created
        if (_instance == null) {
            _instance = new RpsSensorManager();
            jmri.InstanceManager.setSensorManager(jmri.jmrix.rps.RpsSensorManager.instance());
            // log.warn("Setting RpsSensorManager instance at startup time!");
        }
    }
}


