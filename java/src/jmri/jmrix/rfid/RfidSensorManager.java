// RfidSensorManager.java

package jmri.jmrix.rfid;

import org.apache.log4j.Logger;

/**
 * Manage the Rfid-specific Sensor implementation.
 * <P>
 * System names are "FSpppp", where ppp is a
 * representation of the RFID reader.
 * <P>
 * @author      Bob Jacobsen Copyright (C) 2007
 * @author      Matthew Harris Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
abstract public class RfidSensorManager extends jmri.managers.AbstractSensorManager implements RfidListener {

    private String prefix;

    public RfidSensorManager(String prefix) {
        super();
        this.prefix = prefix;
    }

    public String getSystemPrefix() {
        return prefix;
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        super.dispose();
    }

//    public Sensor createNewSensor(String systemName, String userName) {
//        RfidSensor r = new RfidSensor(systemName, userName);
//        return r;
//    }

    public void message(RfidMessage m) {
        log.warn("Unexpected message received"+m);
    }

    private static final Logger log = Logger.getLogger(RfidSensorManager.class.getName());

}

/* @(#)RfidSensorManager.java */
