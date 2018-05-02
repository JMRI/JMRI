package jmri.jmrix.secsi;

import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the system-specific SECSI Sensor implementation.
 * <p>
 * System names are "ViSnnnn", where nnnn is the sensor number without padding.
 * <p>
 * Sensors are numbered from 1.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author Dave Duchamp, multi node extensions, 2004
  */
public class SerialSensorManager extends jmri.managers.AbstractSensorManager
        implements SerialListener {

    private SecsiSystemConnectionMemo memo = null;

    /**
     * Number of sensors per address in the naming scheme.
     * <p>
     * The first node address uses sensors from 1 to SENSORSPERNODE-1, the
     * second from SENSORSPERNODE+1 to SENSORSPERNODE+(SENSORSPERNODE-1), etc.
     * <p>
     * Must be more than, and is generally one more than,
     * {@link SerialNode#MAXSENSORS}
     *
     */
    static final int SENSORSPERNODE = 1000;

    public SerialSensorManager(SecsiSystemConnectionMemo _memo) {
        super();
        memo = _memo;
    }

    /**
     * Return the Secsi system prefix.
     */
    @Override
    public String getSystemPrefix() {
        return memo.getSystemPrefix();
    }

    // Free resources when no longer used
    @Override
    public void dispose() {
    }

    /**
     * Create a new sensor if all checks are passed. System name is normalized to
     * ensure uniqueness.
     */
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s;
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName, getSystemPrefix());
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
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName, getSystemPrefix());
        s = getBySystemName(altName);
        if (s != null) {
            log.error("Sensor with name '{}' already exists as '{}'", systemName, altName);
            return null;
        }
        // check bit number
        int bit = SerialAddress.getBitFromSystemName(sName, getSystemPrefix());
        if ((bit <= 0) || (bit >= SENSORSPERNODE)) {
            log.error("Sensor bit number '{}' is outside the supported range, 1-{}",
                    Integer.toString(bit), Integer.toString(SENSORSPERNODE - 1));
            return null;
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null) {
            s = new SerialSensor(sName, memo);
        } else {
            s = new SerialSensor(sName, userName, memo);
        }

        // ensure that a corresponding Serial Node exists
        SerialNode node = SerialAddress.getNodeFromSystemName(sName, memo.getTrafficController());
        if (node == null) {
            log.warn("Sensor '{}' refers to an undefined Serial Node.", sName);
            return s;
        }
        // register this sensor with the Serial Node
        node.registerSensor(s, bit - 1);
        return s;
    }

    /**
     * Public method to validate system name format.
     *
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'S', getSystemPrefix()));
    }

    /**
     * Dummy routine
     */
    @Override
    public void message(SerialMessage r) {
        log.warn("unexpected message");
    }

    /**
     * Process a reply to a poll of Sensors of one node.
     */
    @Override
    public void reply(SerialReply r) {
        // determine which node
        log.debug("received node poll reply '{}'", r.toString());
        SerialNode node = (SerialNode) memo.getTrafficController().getNodeFromAddress(r.getAddr());
        if (node != null) {
            node.markChanges(r);
        }
    }

    /**
     * Register any orphan Sensors when a new Serial Node is created.
     */
    public void registerSensorsForNode(SerialNode node) {
        log.debug("registering node {}", node.getNodeAddress());
        // get list containing all Sensors
        java.util.Iterator<String> iter
                = getSystemNameList().iterator();
        // Iterate through the sensors
        SerialNode tNode = null;
        while (iter.hasNext()) {
            String sName = iter.next();
            if (sName == null) {
                log.error("System name null during register Sensor");
            } else {
                log.debug("system name is {}", sName);
                if ((sName.startsWith(getSystemPrefix())) && (sName.charAt(getSystemPrefix().length()) == 'S')) { // multichar prefix
                    // This is a Sensor
                    tNode = SerialAddress.getNodeFromSystemName(sName, memo.getTrafficController());
                    if (tNode == node) {
                        // This sensor is for this new Serial Node - register it
                        log.debug("register sensor on node {}", node.getNodeAddress());
                        node.registerSensor(getBySystemName(sName),
                                (SerialAddress.getBitFromSystemName(sName, getSystemPrefix()) - 1));
                    }
                }
            }
        }
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
     * Static function returning the SerialSensorManager instance to use.
     *
     * @return The registered SerialSensorManager instance for general use.
     * @deprecated since 4.9.7
     */
    @Deprecated
    static public SerialSensorManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialSensorManager.class);

}
