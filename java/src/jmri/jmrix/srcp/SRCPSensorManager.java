// SRCPSensorManager.java
package jmri.jmrix.srcp;

import jmri.Sensor;

/**
 * Implement Sensor manager for SRCP systems
 * <P>
 * System names are "DSnnn", where nnn is the sensor number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision$
 */
public class SRCPSensorManager extends jmri.managers.AbstractSensorManager {

    SRCPBusConnectionMemo _memo = null;
    int _bus;

    public SRCPSensorManager(SRCPBusConnectionMemo memo, int bus) {
        _memo = memo;
        _bus = bus;
    }

    public String getSystemPrefix() {
        return _memo.getSystemPrefix();
    }

    public Sensor createNewSensor(String systemName, String userName) {
        Sensor t;
        int addr = Integer.valueOf(systemName.substring(getSystemPrefix().length() + 1)).intValue();
        t = new SRCPSensor(addr, _memo);
        t.setUserName(userName);

        return t;
    }

    static public SRCPSensorManager instance() {
        return null;
    }

}

/* @(#)SRCPSensorManager.java */
