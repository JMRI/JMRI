package jmri.jmrix.srcp;

import javax.annotation.Nonnull;
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

    private int _bus;

    public SRCPSensorManager(SRCPBusConnectionMemo memo, int bus) {
        super(memo);
        _bus = bus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public SRCPBusConnectionMemo getMemo() {
        return (SRCPBusConnectionMemo) memo;
    }

    @Override
    @Nonnull
    public Sensor createNewSensor(@Nonnull String systemName, String userName) {
        Sensor t;
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        t = new SRCPSensor(addr, getMemo());
        t.setUserName(userName);

        return t;
    }

}
