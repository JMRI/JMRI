package jmri.jmrix.srcp;

import javax.annotation.Nonnull;
import jmri.Sensor;

/**
 * Implement SensorMmanager for SRCP systems.
 * <p>
 * System names are "DSnnn", where D is the user configurable system prefix,
 * nnn is the sensor number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class SRCPSensorManager extends jmri.managers.AbstractSensorManager {

    public SRCPSensorManager(SRCPBusConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public SRCPBusConnectionMemo getMemo() {
        return (SRCPBusConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     * <p>
     * System name is normalized to ensure uniqueness.
     * @throws IllegalArgumentException when SystemName can't be converted
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        Sensor t;
        int addr;
        try {
            addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to convert " +  // NOI18N
                    systemName.substring(getSystemPrefix().length() + 1) +
                    " to SRCP sensor address"); // NOI18N
        }
        t = new SRCPSensor(addr, getMemo());
        t.setUserName(userName);

        return t;
    }

    /**
     * Validates to only numeric.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        return validateSystemNameFormatOnlyNumeric(name,locale);
    }

}
