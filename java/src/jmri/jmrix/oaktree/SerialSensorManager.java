package jmri.jmrix.oaktree;

import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the system-specific Sensor implementation.
 * <P>
 * System names are "OSnnnn", where nnnn is the sensor number without padding.
 * <P>
 * Sensors are numbered from 1.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006
 * @author Dave Duchamp, multi node extensions, 2004
  */
public class SerialSensorManager extends jmri.managers.AbstractSensorManager
        implements SerialListener {

    OakTreeSystemConnectionMemo _memo = null;
    protected String prefix = "O";

    public SerialSensorManager() {
        this(jmri.InstanceManager.getDefault(OakTreeSystemConnectionMemo.class));
    }

    public SerialSensorManager(OakTreeSystemConnectionMemo memo) {
        super();
        _memo = memo;
        prefix = getSystemPrefix();
    }

    /**
     * Number of sensors per address in the naming scheme.
     * <P>
     * The first node address uses sensors from 1 to SENSORSPERNODE-1, the
     * second from SENSORSPERNODE+1 to SENSORSPERNODE+(SENSORSPERNODE-1), etc.
     * <P>
     * Must be more than, and is generally one more than,
     * {@link SerialNode#MAXSENSORS}
     *
     */
    static final int SENSORSPERNODE = 1000;

    /**
     * Return the Oak Tree system prefix
     */
    @Override
    public String getSystemPrefix() {
        return _memo.getSystemPrefix();

    }

    /**
     * Create a new sensor if all checks are passed System name is normalized to
     * ensure uniqueness.
     */
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s;
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName, prefix);
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
        String altName = SerialAddress.convertSystemNameToAlternate(sName, prefix);
        s = getBySystemName(altName);
        if (s != null) {
            log.error("Sensor with name '{}' already exists as '{}'", systemName, altName);
            return null;
        }
        // check bit number
        int bit = SerialAddress.getBitFromSystemName(sName, prefix);
        if ((bit <= 0) || (bit >= SENSORSPERNODE)) {
            log.error("Sensor bit number {} is outside the supported range, 1-{}",
                    Integer.toString(bit),
                    Integer.toString(SENSORSPERNODE - 1));
            return null;
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null) {
            s = new SerialSensor(sName);
        } else {
            s = new SerialSensor(sName, userName);
        }

        // ensure that a corresponding Serial Node exists
        SerialNode node = SerialAddress.getNodeFromSystemName(sName, prefix,_memo.getTrafficController());
        if (node == null) {
            log.warn("Sensor {} refers to an undefined Serial Node.", sName);
            return s;
        }
        // register this sensor with the Serial Node
        node.registerSensor(s, bit - 1);
        return s;
    }

    /**
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'S', prefix));
    }

    /**
     * Dummy routine
     */
    @Override
    public void message(SerialMessage r) {
        log.warn("unexpected message");
    }

    /**
     * Process a reply to a poll of Sensors of one node
     */
    @Override
    public void reply(SerialReply r) {
        // determine which node
        SerialNode node = (SerialNode) _memo.getTrafficController().getNodeFromAddress(r.getAddr());
        if (node != null) {
            node.markChanges(r);
        }
    }

    /**
     * Method to register any orphan Sensors when a new Serial Node is created.
     */
    public void registerSensorsForNode(SerialNode node) {
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
                    tNode = SerialAddress.getNodeFromSystemName(sName, getSystemPrefix(),_memo.getTrafficController());
                    if (tNode == node) {
                        // This sensor is for this new Serial Node - register it
                        node.registerSensor(getBySystemName(sName),
                                (SerialAddress.getBitFromSystemName(sName, getSystemPrefix()) - 1));
                    }
                }
            }
        }
    }

    /**
     * Static function returning the SerialSensorManager instance to use.
     *
     * @return The registered SerialSensorManager instance for general use, if
     *         need be creating one.
     */
    static public SerialSensorManager instance() {
        return null;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialSensorManager.class);

}
