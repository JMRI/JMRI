package jmri.jmrix.jmriclient;

import jmri.Sensor;

/**
 * Implement sensor manager for JMRIClient systems.
 * <p>
 * System names are "prefixnnn", where prefix is the system prefix and nnn is
 * the sensor number without padding.
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class JMRIClientSensorManager extends jmri.managers.AbstractSensorManager {

    private JMRIClientSystemConnectionMemo memo = null;
    private String prefix = null;

    public JMRIClientSensorManager(JMRIClientSystemConnectionMemo memo) {
        this.memo = memo;
        this.prefix = memo.getSystemPrefix();
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor t;
        int addr = Integer.parseInt(systemName.substring(prefix.length() + 1));
        t = new JMRIClientSensor(addr, memo);
        t.setUserName(userName);
        return t;
    }

    /*
     * JMRIClient Sensors can take arbitrary names to match the names used
     * on the server.
     */
    @Override
    public String createSystemName(String curAddress, String prefix) throws jmri.JmriException {
        return prefix + typeLetter() + curAddress;
    }

}
