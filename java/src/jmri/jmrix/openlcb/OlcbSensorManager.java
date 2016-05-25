// OlcbSensorManager.java
package jmri.jmrix.openlcb;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the OpenLCB-specific Sensor implementation.
 *
 * System names are "MSnnn", where nnn is the sensor number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2008, 2010
 * @version	$Revision$
 */
public class OlcbSensorManager extends jmri.managers.AbstractSensorManager implements CanListener {

    String prefix = "M";

    public String getSystemPrefix() {
        return prefix;
    }

    // to free resources when no longer used
    public void dispose() {
        memo.getTrafficController().removeCanListener(this);
        super.dispose();
    }

    //Implimented ready for new system connection memo
    public OlcbSensorManager(CanSystemConnectionMemo memo) {
        this.memo = memo;
        prefix = memo.getSystemPrefix();
        memo.getTrafficController().addCanListener(this);
    }

    CanSystemConnectionMemo memo;

    public Sensor createNewSensor(String systemName, String userName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        // first, check validity
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            throw e;
        }

        // OK, make
        Sensor s = new OlcbSensor(getSystemPrefix(), addr, memo.getTrafficController());
        s.setUserName(userName);
        return s;
    }

    public boolean allowMultipleAdditions() {
        return false;
    }

    public String createSystemName(String curAddress, String prefix) throws JmriException {
        try {
            validateSystemNameFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        // don't check for integer; should check for validity here
        return prefix + typeLetter() + curAddress;
    }

    public String getNextValidAddress(String curAddress, String prefix) {
        // always return this (the current) name without change
        return curAddress;
    }

    void validateSystemNameFormat(String address) throws IllegalArgumentException {
        OlcbAddress a = new OlcbAddress(address);
        OlcbAddress[] v = a.split();
        if (v == null) {
            throw new IllegalArgumentException("Did not find usable system name: " + address + " to a valid Olcb sensor address");
        }
        switch (v.length) {
            case 1:
                break;
            case 2:
                break;
            default:
                throw new IllegalArgumentException("Wrong number of events in address: " + address);
        }
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

    /**
     * No mechanism currently exists to request status updates from all layout
     * sensors.
     */
    public void updateAll() {
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbSensorManager.class.getName());

}

/* @(#)OlcbSensorManager.java */
