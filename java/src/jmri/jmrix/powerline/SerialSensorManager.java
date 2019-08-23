package jmri.jmrix.powerline;

import java.util.Locale;
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
 * @author Ken Cameron, (C) 2009, sensors from poll replies Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
abstract public class SerialSensorManager extends jmri.managers.AbstractSensorManager implements SerialListener {

    SerialTrafficController tc = null;

    public SerialSensorManager(SerialTrafficController tc) {
        super(tc.getAdapterMemo());
        this.tc = tc;
        tc.addSerialListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SerialSystemConnectionMemo getMemo() {
        return (SerialSystemConnectionMemo) memo;
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Create a new sensor if all checks are passed System name is normalized to
     * ensure uniqueness.
     */
    @Override
    protected Sensor createNewSensor(String systemName, String userName) {
        Sensor s;
        // validate the system name, and normalize it
        String sName = tc.getAdapterMemo().getSerialAddress().normalizeSystemName(systemName);
        if (sName.equals("")) {
            // system name is not valid
            log.error("Invalid Sensor system name - {}", systemName);
            return null;
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s != null) {
            log.error("Sensor with this name already exists - {}", systemName);
            return null;
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
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {

        //If the hardware address passed does not already exist then this can
        //be considered the next valid address.
        Sensor s = getBySystemName(prefix + typeLetter() + curAddress);
        if (s == null) {
            return curAddress;
        }

        // This bit deals with handling the curAddress, and how to get the next address.
        int iName = 0;
        //Address starts with a single letter called a house code.
        String houseCode = curAddress.substring(0, 1);
        try {
            iName = Integer.parseInt(curAddress.substring(1));
        } catch (NumberFormatException ex) {
            log.error("Unable to convert {} Hardware Address to a number", curAddress);
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("ErrorTitle"),
                            Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false);
            return null;
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
            return null;
        } else {
            return houseCode + iName;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        return tc.getAdapterMemo().getSerialAddress().validateSystemNameFormat(name, typeLetter(), locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
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
