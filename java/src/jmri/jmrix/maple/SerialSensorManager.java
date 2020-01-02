package jmri.jmrix.maple;

import java.util.Locale;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the specific Sensor implementation.
 * <p>
 * System names are "KSnnnn", where K is the user configurable system prefix,
 * nnnn is the sensor number without padding.
 * <p>
 * Sensors are numbered from 1.
 * <p>
 * This is a SerialListener to handle the replies to poll messages. Those are
 * forwarded to the specific SerialNode object corresponding to their origin for
 * processing of the data.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2007, 2008
 * @author Dave Duchamp, multi node extensions, 2004
 */
public class SerialSensorManager extends jmri.managers.AbstractSensorManager
        implements SerialListener {

    /**
     * Number of sensors per UA in the naming scheme.
     * <p>
     * The first UA (node address) uses sensors from 1 to SENSORSPERUA-1, the
     * second from SENSORSPERUA+1 to SENSORSPERUA+(SENSORSPERUA-1), etc.
     * <p>
     * Must be more than, and is generally one more than,
     * {@link SerialNode#MAXSENSORS}
     */
    static final int SENSORSPERUA = 1000;

    public SerialSensorManager(MapleSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapleSystemConnectionMemo getMemo() {
        return (MapleSystemConnectionMemo) memo;
    }

    /**
     * Create a new sensor if all checks are passed System name is normalized to
     * ensure uniqueness.
     */
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s;
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName, getSystemPrefix());
        if (sName.equals("")) {
            // system name is not valid
            log.error("Invalid sensor system name - {}", systemName);
            return null;
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s != null) {
            log.warn("Sensor with this name already exists - {}", systemName);
            return null;
        }
        // check bit number
        int bit = SerialAddress.getBitFromSystemName(sName, getSystemPrefix());
        if ((bit <= 0) || (bit > 1000)) {
            log.warn("Sensor bit number '{}' is outside the supported range, 1-1000", Integer.toString(bit));
            return null;
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null) {
            s = new SerialSensor(sName); // prefix not passed
        } else {
            s = new SerialSensor(sName, userName); // prefix not passed
        }
        // check configured
        if (!SerialAddress.validSystemNameConfig(sName, 'S', getMemo())) {
            log.warn("Sensor system Name '" + sName + "' does not address configured hardware.");
            javax.swing.JOptionPane.showMessageDialog(null, "WARNING - The Sensor just added, "
                    + sName + ", refers to an unconfigured input bit.", "Configuration Warning",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE, null);
        }
        // register this sensor 
        getMemo().getTrafficController().inputBits().registerSensor(s, bit - 1);
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        return SerialAddress.validateSystemNameFormat(name, this, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, typeLetter(), getSystemPrefix()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    /**
     * Dummy routine
     */
    @Override
    public void message(SerialMessage r) {
        log.warn("unexpected message");
    }

    /**
     * Process a reply to a poll of Sensors of one panel node.
     */
    @Override
    public void reply(SerialReply r) {
        getMemo().getTrafficController().inputBits().markChanges(r);
    }

    /**
     * Method to register any orphan Sensors when a new Serial Node is created
     */
    @SuppressWarnings("deprecation") // needs careful unwinding for Set operations
    public void registerSensorsForNode(SerialNode node) {
        // get list containing all Sensors
        java.util.Iterator<String> iter
                = getSystemNameList().iterator();
        // Iterate through the sensors
        while (iter.hasNext()) {
            String sName = iter.next();
            if (sName == null) {
                log.error("System name null during register Sensor");
            } else {
                log.debug("system name is {}", sName);
                if ((sName.charAt(0) == 'K') && (sName.charAt(1) == 'S')) { // TODO multichar prefix
                    // This is a valid Sensor - make sure it is registered
                    getMemo().getTrafficController().inputBits().registerSensor(getBySystemName(sName),
                            (SerialAddress.getBitFromSystemName(sName, getSystemPrefix()) - 1));
                }
            }
        }
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        if (curAddress.contains(":")) {
            //Address format passed is in the form of sysNode:address or T:turnout address
            int seperator = curAddress.indexOf(":");
            try {
                sysNode = Integer.parseInt(curAddress.substring(0, seperator));
                address = Integer.parseInt(curAddress.substring(seperator + 1));
            } catch (NumberFormatException ex) {
                log.error("Unable to convert {} into the cab and address format of nn:xx", curAddress);
                throw new JmriException("Hardware Address passed should be a number");
            }
            iName = (sysNode * 1000) + address;
        } else {
            //Entered in using the old format
            try {
                iName = Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) {
                log.error("Unable to convert {} Hardware Address to a number", curAddress);
                throw new JmriException("Hardware Address passed should be a number");
            }
        }
        return prefix + typeLetter() + iName;
    }

    int sysNode = 0;
    int address = 0;
    int iName = 0;

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {

        String tmpSName = "";

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("ErrorTitle"),
                            Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false);
            return null;
        }
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if (s != null) {
            for (int x = 1; x < 10; x++) {
                iName = iName + 1;
                s = getBySystemName(prefix + typeLetter() + iName);
                if (s == null) {
                    return Integer.toString(iName);
                }
            }
            return null;
        } else {
            return Integer.toString(iName);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialSensorManager.class);

}
