package jmri.jmrix.powerline;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.*;

/**
 * Manage the system-specific Sensor implementation.
 * <p>
 * System names are: Powerline - "PSann", where a is the house code and nn is
 * the unit number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author Ken Cameron, (C) 2009, sensors from poll replies. Converted to
 * multiple connection support
 * @author kcameron Copyright (C) 2011
 */
abstract public class SerialSensorManager extends jmri.managers.AbstractSensorManager implements SerialListener {

    private SerialTrafficController tc = null;

    public SerialSensorManager(SerialTrafficController tc) {
        super(tc.getAdapterMemo());
        this.tc = tc;
        tc.addSerialListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public SerialSystemConnectionMemo getMemo() {
        return (SerialSystemConnectionMemo) memo;
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
        Sensor s;
        // validate the system name, and normalize it
        String sName = tc.getAdapterMemo().getSerialAddress().normalizeSystemName(systemName);
        if (sName.isEmpty()) {
            // system name is not valid
            throw new IllegalArgumentException("Invalid Powerline Sensor system name - " +  // NOI18N
                    systemName);
        }
        // does this Sensor already exist?
        s = getBySystemName(sName);
        if (s != null) {
            throw new IllegalArgumentException("Powerline Sensor with this name already exists - " +  // NOI18N
                    systemName);
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null) {
            s = new SerialSensor(sName, tc);
        } else {
            s = new SerialSensor(sName, tc, userName);
        }
        return s;
    }

    /**
     * Dummy routine
     */
    @Override
    public void message(SerialMessage r) {
        // this happens during some polls from sensor messages
        //log.warn("unexpected message");
    }

    /**
     * Process a reply to a poll of Sensors of one node
     */
    @Override
    abstract public void reply(SerialReply r);

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return false;
    }

    @Override
    @javax.annotation.Nonnull
    @javax.annotation.CheckReturnValue
    public String getNextValidSystemName(@Nonnull NamedBean currentBean) throws JmriException {
        throw new jmri.JmriException("getNextValidSystemName should not have been called");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        return tc.getAdapterMemo().getSerialAddress().validateSystemNameFormat(name, typeLetter(), locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return tc.getAdapterMemo().getSerialAddress().validSystemNameFormat(systemName, typeLetter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialSensorManager.class);

}
