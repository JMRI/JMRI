// SRCPSensorManager.java

package jmri.jmrix.srcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    SRCPSystemConnectionMemo _memo = null;

    public SRCPSensorManager(SRCPSystemConnectionMemo memo) {
    	_memo=memo;
    }

    public String getSystemPrefix() { return "D"; }

    public Sensor createNewSensor(String systemName, String userName) {
        Sensor t;
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new SRCPSensor(addr,_memo);
        t.setUserName(userName);

        return t;
    }

    static public SRCPSensorManager instance() {
        return null;
    }

    static Logger log = LoggerFactory.getLogger(SRCPSensorManager.class.getName());

}

/* @(#)SRCPSensorManager.java */
