package jmri.jmrix.powerline;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * TODO : Get this method working then enable multiple additions
     * {@inheritDoc}
     */
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws JmriException {
        log.warn("getNextValidAddress called but system does not yet support multiple additions");
        //If the hardware address passed does not already exist then this can
        //be considered the next valid address.
        Sensor s = getBySystemName(prefix + typeLetter() + curAddress);
        if (s == null && !ignoreInitialExisting) {
            return curAddress;
        }

        // This bit deals with handling the curAddress, and how to get the next address.
        int iName = 0;
        //Address starts with a single letter called a house code.
        String houseCode = curAddress.substring(0, 1);
        try {
            iName = Integer.parseInt(curAddress.substring(1));
        } catch (NumberFormatException ex) {
            throw new JmriException("Unable to convert "+curAddress+" to a number after the house code");
        }

        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        s = getBySystemName(prefix + typeLetter() + curAddress);
        if (s != null) {
            for (int x = 1; x < 10; x++) {
                iName++;
                s = getBySystemName(prefix + typeLetter() + houseCode + (iName));
                if (s == null) {
                    return houseCode + iName;
                }
            }
            throw new JmriException(Bundle.getMessage("InvalidNextValidTenInUse",getBeanTypeHandled(true),curAddress,houseCode + iName));
        } else {
            return houseCode + iName;
        }
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

    private final static Logger log = LoggerFactory.getLogger(SerialSensorManager.class);

}
