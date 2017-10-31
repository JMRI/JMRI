
package jmri.jmrix.anyma_dmx;

import jmri.Sensor;

/**
 * Manage the AnymaDMX_ specific Sensor implementation.
 *
 * System names are "PSnnn", where nnn is the sensor number without padding.
 *
 * @author   Paul Bender Copyright (C) 2015
 * @author George Warner Copyright (C) 2017
 * @since       4.9.6
 */
public class AnymaDMX_SensorManager extends jmri.managers.AbstractSensorManager {

    // ctor has to register for AnymaDMX_ events
    public AnymaDMX_SensorManager(String prefix) {
        super();
        this.prefix=prefix.toUpperCase();
    }

    /**
     * Provides access to the system prefix string.
     * This was previously called the "System letter"
     */
    @Override
    public String getSystemPrefix(){ return prefix; }

    private String prefix = null;

    // to free resources when no longer used
    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        return new AnymaDMX_Sensor(systemName, userName);
    }

    /**
     * Do the sensor objects provided by this manager support configuring
     * an internal pullup or pull down resistor?
     * <p>
     * For Anyma DMX systems, it is possible to set the pullup or
     * pulldown resistor, so return true.
     *
     * @return true if pull up/pull down configuration is supported.
     */
    @Override
    public boolean isPullResistanceConfigurable(){
       return true;
    }

}
