// LnSensorManager.java

package jmri.jmrix.loconet;

import jmri.Sensor;

/**
 * Manage the LocoNet-specific Sensor implementation.
 *
 * System names are "LSnnn", where nnn is the sensor number without padding.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.10 $
 */
public class LnSensorManager extends jmri.AbstractSensorManager implements LocoNetListener {

    public char systemLetter() { return 'L'; }

    static public LnSensorManager instance() {
        if (mInstance == null) new LnSensorManager();
        return mInstance;
    }
    static private LnSensorManager mInstance = null;

    // to free resources when no longer used
    public void dispose() {
        LnTrafficController.instance().removeLocoNetListener(~0, this);
    }

    // LocoNet-specific methods

    public Sensor createNewSensor(String systemName, String userName) {
        return new LnSensor(systemName, userName);
    }

    // ctor has to register for LocoNet events
    public LnSensorManager() {
        LnTrafficController.instance().addLocoNetListener(~0, this);
        mInstance = this;
    }

    // listen for sensors, creating them as needed
    public void message(LocoNetMessage l) {
        // parse message type
        LnSensorAddress a;
        switch (l.getOpCode()) {
        case LnConstants.OPC_INPUT_REP: {               /* page 9 of Loconet PE */
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);
            a = new LnSensorAddress(sw1, sw2);
            if (log.isDebugEnabled()) log.debug("INPUT_REP received with address "+a);
            break;
        }
        default:  // here we didn't find an interesting command
            return;
        }
        // reach here for loconet sensor input command; make sure we know about this one
        String s = a.getNumericAddress();
        if (null == getBySystemName(s)) {
            // need to store a new one
            if (log.isDebugEnabled()) log.debug("Create new LnSensor as "+s);
            newSensor(s, null);
        }
    }

    private int address(int a1, int a2) {
        // the "+ 1" in the following converts to throttle-visible numbering
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSensorManager.class.getName());

}

/* @(#)LnSensorManager.java */
