// OlcbSensorManager.java

package jmri.jmrix.openlcb;

import jmri.Sensor;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;

/**
 * Manage the OpenLCB-specific Sensor implementation.
 *
 * System names are "MSnnn", where nnn is the sensor number without padding.
 *
 * @author			Bob Jacobsen Copyright (C) 2008, 2010
 * @version			$Revision$
 */
public class OlcbSensorManager extends jmri.managers.AbstractSensorManager implements CanListener {

    public String getSystemPrefix() { return "M"; }

    static public OlcbSensorManager instance() {
        if (mInstance == null) new OlcbSensorManager();
        return mInstance;
    }
    static private OlcbSensorManager mInstance = null;

    // to free resources when no longer used
    public void dispose() {
        TrafficController.instance().removeCanListener(this);
        super.dispose();
    }

    public Sensor createNewSensor(String systemName, String userName) {
        // first, check validity
        OlcbAddress a = new OlcbAddress(systemName.substring(2,systemName.length()));
        OlcbAddress[] v = a.split();
        if (v==null) {
            log.error("Did not find usable system name: "+systemName);
            return null;
        }
        if (v.length<1 || v.length>2) {
            log.error("Wrong number of events in address: "+systemName);
            return null;
        }
        // OK, make
        return new OlcbSensor(systemName, userName);
    }

    // ctor has to register for LocoNet events
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                        justification="temporary until mult-system; only set at startup")
    public OlcbSensorManager() {
        TrafficController.instance().addCanListener(this);
        mInstance = this;
    }

    // listen for sensors, creating them as needed
    public void reply(CanReply l) {
        // doesn't do anything, because for now 
        // we want you to create manually
    }
    public void message(CanMessage l) {
        // doesn't do anything, because 
        // messages come from us
    }

    /** No mechanism currently exists to request
     * status updates from all layout sensors.
	 */
	public void updateAll() {
	}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OlcbSensorManager.class.getName());

}

/* @(#)OlcbSensorManager.java */
