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

    public JMRIClientSensorManager(JMRIClientSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JMRIClientSystemConnectionMemo getMemo() {
        return (JMRIClientSystemConnectionMemo) memo;
    }

    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor t;
        int addr = Integer.parseInt(systemName.substring(getSystemNamePrefix().length()));
        t = new JMRIClientSensor(addr, getMemo());
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
