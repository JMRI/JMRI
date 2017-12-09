package jmri.jmrix.maple;

import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the specific Sensor implementation.
 * <P>
 * System names are "KSnnnn", where nnnn is the sensor number without padding.
 * <P>
 * Sensors are numbered from 1.
 * <P>
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
     * <P>
     * The first UA (node address) uses sensors from 1 to SENSORSPERUA-1, the
     * second from SENSORSPERUA+1 to SENSORSPERUA+(SENSORSPERUA-1), etc.
     * <P>
     * Must be more than, and is generally one more than,
     * {@link SerialNode#MAXSENSORS}
     */
    static final int SENSORSPERUA = 1000;

    MapleSystemConnectionMemo _memo = null;
    protected String prefix = "M";

    public SerialSensorManager(MapleSystemConnectionMemo memo) {
        super();
        _memo = memo;
        prefix = memo.getSystemPrefix();
    }

    /**
     * Get the configured system prefix for this connection.
     */
    @Override
    public String getSystemPrefix() {
        return prefix;
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
        if (!SerialAddress.validSystemNameConfig(sName, 'S', _memo)) {
            log.warn("Sensor system Name '" + sName + "' does not address configured hardware.");
            javax.swing.JOptionPane.showMessageDialog(null, "WARNING - The Sensor just added, "
                    + sName + ", refers to an unconfigured input bit.", "Configuration Warning",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE, null);
        }
        // register this sensor 
        _memo.getTrafficController().inputBits().registerSensor(s, bit - 1);
        return s;
    }

    /**
     * Public method to validate system name format.
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'S', getSystemPrefix()));
    }

    /**
     * Public method to normalize a system name.
     * <P>
     * Returns a normalized system name if system name has a valid format, else
     * returns "".
     */
    @Override
    public String normalizeSystemName(String systemName) {
        return (SerialAddress.normalizeSystemName(systemName, getSystemPrefix()));
    }

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddInputEntryToolTip");
        return entryToolTip;
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
        _memo.getTrafficController().inputBits().markChanges(r);
    }

    /**
     * Method to register any orphan Sensors when a new Serial Node is created
     */
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
                    _memo.getTrafficController().inputBits().registerSensor(getBySystemName(sName),
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
                sysNode = Integer.valueOf(curAddress.substring(0, seperator)).intValue();
                address = Integer.valueOf(curAddress.substring(seperator + 1)).intValue();
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

    /**
     * Static function returning the SerialSensorManager instance to use.
     *
     * @return The registered SerialSensorManager instance for general use, if
     *         need be creating one.
     * @deprecated since 4.9.7
     */
    @Deprecated
    static public SerialSensorManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialSensorManager.class);

}
