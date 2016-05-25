// CbusSensorManager.java
package jmri.jmrix.can.cbus;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the CBUS-specific Sensor implementation.
 *
 * System names are "MSnnn", where nnn is the sensor number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision$
 */
public class CbusSensorManager extends jmri.managers.AbstractSensorManager implements CanListener {

    public String getSystemPrefix() {
        return memo.getSystemPrefix();
    }

    // to free resources when no longer used
    public void dispose() {
        memo.getTrafficController().removeCanListener(this);
        super.dispose();
    }

    //Implimented ready for new system connection memo
    public CbusSensorManager(CanSystemConnectionMemo memo) {
        this.memo = memo;
        memo.getTrafficController().addCanListener(this);
    }

    CanSystemConnectionMemo memo;

    // CBUS-specific methods
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
        Sensor s = new CbusSensor(getSystemPrefix(), addr, memo.getTrafficController());
        s.setUserName(userName);
        return s;
    }

    public String createSystemName(String curAddress, String prefix) throws jmri.JmriException {
        try {
            validateSystemNameFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        return getSystemPrefix() + typeLetter() + curAddress;
    }

    void validateSystemNameFormat(String address) throws IllegalArgumentException {
        CbusAddress a = new CbusAddress(address);
        CbusAddress[] v = a.split();
        if (v == null) {
            throw new IllegalArgumentException("Did not find usable system name: " + address + " to a valid Cbus sensor address");
        }
        switch (v.length) {
            case 1:
                if (address.startsWith("+") || address.startsWith("-")) {
                    break;
                }
                throw new IllegalArgumentException("can't make 2nd event from systemname " + address);
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

    private final static Logger log = LoggerFactory.getLogger(CbusSensorManager.class.getName());

}

/* @(#)CbusSensorManager.java */
