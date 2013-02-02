// JMRIClientSensorManager.java

package jmri.jmrix.jmriclient;

import org.apache.log4j.Logger;
import jmri.Sensor;

/**
 * Implement sensor manager for JMRIClient systems
 * <P>
 * System names are "prefixnnn", where prefix is the system prefix and
 * nnn is the sensor number without padding.
 *
 * @author	Paul Bender Copyright (C) 2010
 * @version	$Revision$
 */

public class JMRIClientSensorManager extends jmri.managers.AbstractSensorManager {

    private JMRIClientSystemConnectionMemo memo=null;
    private String prefix = null;

    public JMRIClientSensorManager(JMRIClientSystemConnectionMemo memo) {
        this.memo=memo;
        this.prefix=memo.getSystemPrefix();
    }

    public String getSystemPrefix() { return prefix; }

    public Sensor createNewSensor(String systemName, String userName) {
        Sensor t;
        int addr = Integer.valueOf(systemName.substring(prefix.length()+1)).intValue();
        t = new JMRIClientSensor(addr,memo);
        t.setUserName(userName);
        return t;
    }

    static Logger log = Logger.getLogger(JMRIClientSensorManager.class.getName());

}

/* @(#)JMRIClientSensorManager.java */
