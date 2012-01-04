// CbusSensorManager.java

package jmri.jmrix.can.cbus;

import jmri.Sensor;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Manage the CBUS-specific Sensor implementation.
 *
 * System names are "MSnnn", where nnn is the sensor number without padding.
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision$
 */
public class CbusSensorManager extends jmri.managers.AbstractSensorManager implements CanListener {

    String prefix = "M";
    
    public String getSystemPrefix() { return prefix; }

    static public CbusSensorManager instance() {
        if (mInstance == null) new CbusSensorManager();
        return mInstance;
    }
    static private CbusSensorManager mInstance = null;
    
    // to free resources when no longer used
    public void dispose() {
        TrafficController.instance().removeCanListener(this);
        super.dispose();
    }
    
    //Implimented ready for new system connection memo
    public CbusSensorManager(CanSystemConnectionMemo memo){
        this.memo=memo;
        prefix = memo.getSystemPrefix();
        memo.getTrafficController().addCanListener(this);
    }
    
    
    CanSystemConnectionMemo memo;

    // CBUS-specific methods

    public Sensor createNewSensor(String systemName, String userName) {
        // first, check validity
        CbusAddress a = new CbusAddress(systemName.substring(2,systemName.length()));
        CbusAddress[] v = a.split();
        if (v==null) {
            log.error("Did not find usable system name: "+systemName);
            return null;
        }
        if (v.length<1 || v.length>2) {
            log.error("Wrong number of events in address: "+systemName);
            return null;
        }
        // OK, make
        return new CbusSensor(systemName, userName);
    }
    
    public String createSystemName(String curAddress, String prefix) throws jmri.JmriException{
        return prefix+typeLetter()+curAddress;
    }

    // ctor has to register for LocoNet events
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    // There can only be one instance
    public CbusSensorManager() {
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CbusSensorManager.class.getName());

}

/* @(#)CbusSensorManager.java */
