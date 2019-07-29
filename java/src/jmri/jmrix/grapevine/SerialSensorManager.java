package jmri.jmrix.grapevine;

import java.util.Locale;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the system-specific Sensor implementation.
 * <p>
 * System names are "GSnnnn", where G is the (multichar) system connection prefix,
 * nnnn is the sensor number without padding.
 * <p>
 * Sensors are numbered from 1.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author Dave Duchamp, multi node extensions, 2004
 */
public class SerialSensorManager extends jmri.managers.AbstractSensorManager
        implements SerialListener {

    /**
     * Number of sensors per address in the naming scheme.
     * <p>
     * The first node address uses sensors from 1 to SENSORSPERNODE-1, the
     * second from SENSORSPERNODE+1 to SENSORSPERNODE+(SENSORSPERNODE-1), etc.
     * <p>
     * Must be more than, and is generally one more than,
     * {@link SerialNode#MAXSENSORS}
     */
    static final int SENSORSPERNODE = 1000;

    public SerialSensorManager(GrapevineSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GrapevineSystemConnectionMemo getMemo() {
        return (GrapevineSystemConnectionMemo) memo;
    }

    /**
     * Create a new sensor if all checks are passed.
     * System name is normalized to ensure uniqueness.
     *
     * @return null if sensor already exists by that name or an alternate
     */
    @Override
    protected Sensor createNewSensor(String systemName, String userName) {
        String prefix = getSystemPrefix();
        log.debug("createNewSensor {} {}", systemName, userName);
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
            log.error("Sensor bit number {} is outside the supported range 1-{}", Integer.toString(bit),
                    Integer.toString(SENSORSPERNODE - 1));
            return null;
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null) {
            s = new SerialSensor(sName, getMemo());
        } else {
            s = new SerialSensor(sName, userName, getMemo());
        }

        // ensure that a corresponding Serial Node exists
        SerialNode node = SerialAddress.getNodeFromSystemName(sName, getMemo().getTrafficController());
        if (node == null) {
            log.warn("Sensor {} refers to an undefined Serial Node.", sName);
            return s;
        }
        // register this sensor with the Serial Node
        node.registerSensor(s, bit);
        log.debug("registered {}  in node {}", s.getSystemName(), node);
        return s;
    }

    /** {@inheritDoc} */
    @Override
    public String createSystemName(String curAddress, String prefix) throws jmri.JmriException {
        String tmpSName = prefix + "S" + curAddress;
        // first, check validity
        try {
            validSystemNameFormat(tmpSName);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        log.debug("createSystemName {}", tmpSName);
        return tmpSName;
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
        return SerialAddress.validSystemNameFormat(systemName, typeLetter(), getSystemPrefix());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    /**
     * Dummy routine.
     */
    @Override
    public void message(SerialMessage r) {
    }

    /**
     * Process a reply to a poll of Sensors of one node.
     */
    @Override
    public void reply(SerialReply r) {
        // determine which node
        SerialNode node = (SerialNode) getMemo().getTrafficController().getNodeFromAddress(r.getAddr());
        if (node != null) {
            node.markChanges(r);
        }
        log.debug("node {} marked as changed by SerialSensorManager", r.getAddr());
    }

    /**
     * Register any orphan Sensors when a new Serial Node is created.
     */
    @SuppressWarnings("deprecation") // needs careful unwinding for Set operations
    public void registerSensorsForNode(SerialNode node) {
        // get list containing all Sensors
        java.util.Iterator<String> iter
                = getSystemNameList().iterator();
        // Iterate through the sensors
        SerialNode tNode = null;
        while (iter.hasNext()) {
            String sName = iter.next();
            if (sName == null) {
                log.error("System Name null during register Sensor");
            } else {
                log.debug("System Name is {}", sName);
                if ((sName.startsWith(getSystemPrefix())) && (sName.charAt(getSystemPrefix().length()) == 'S')) { // multichar prefix
                    // This is a Sensor
                    tNode = SerialAddress.getNodeFromSystemName(sName, getMemo().getTrafficController());
                    if (tNode == node) {
                        // This sensor is for this new Serial Node - register it
                        node.registerSensor(getBySystemName(sName),
                                SerialAddress.getBitFromSystemName(sName, getSystemPrefix()));
                    }
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialSensorManager.class);

}
