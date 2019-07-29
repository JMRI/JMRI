package jmri.jmrix.srcp;

import jmri.Sensor;

/**
 * Implement Sensor manager for SRCP systems.
 * <p>
 * System names are "DSnnn", where D is the user configurable system prefix,
 * nnn is the sensor number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 */
public class SRCPSensorManager extends jmri.managers.AbstractSensorManager {

    int _bus;

    public SRCPSensorManager(SRCPBusConnectionMemo memo, int bus) {
        super(memo);
        _bus = bus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SRCPBusConnectionMemo getMemo() {
        return (SRCPBusConnectionMemo) memo;
    }

    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor t;
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        t = new SRCPSensor(addr, getMemo());
        t.setUserName(userName);

        return t;
    }

}
