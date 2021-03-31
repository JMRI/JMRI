package jmri.jmrix.jmriclient;

import javax.annotation.Nonnull;
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
    @Nonnull
    public JMRIClientSystemConnectionMemo getMemo() {
        return (JMRIClientSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException when SystemName can't be converted
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        Sensor t;
        int addr;
        try {
            addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1)); // .length() only? TODO
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Can't convert " +  // NOI18N
                    systemName.substring(getSystemPrefix().length() + 1) +
                    " to JMRIClient sensor address"); // NOI18N
        }
        t = new JMRIClientSensor(addr, getMemo());
        t.setUserName(userName);
        return t;
    }

    /*
     * JMRIClient Sensors can take arbitrary names to match the names used
     * on the server.
     */
    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
        return prefix + typeLetter() + curAddress;
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
