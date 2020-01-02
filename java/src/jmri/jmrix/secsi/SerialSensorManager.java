package jmri.jmrix.secsi;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the system-specific SECSI Sensor implementation.
 * <p>
 * System names are "VSnnnn", where V is the user configurable system prefix,
 * nnnn is the sensor number without padding.
 * <p>
 * Sensors are numbered from 1.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
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
     *
     */
    static final int SENSORSPERNODE = 1000;

    public SerialSensorManager(SecsiSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public SecsiSystemConnectionMemo getMemo() {
        return (SecsiSystemConnectionMemo) memo;
    }

    // Free resources when no longer used
    @Override
    public void dispose() {
    }

    /**
     * {@inheritDoc}
     * <p>
     * System name is normalized to ensure uniqueness.
     *
     * @throws IllegalArgumentException when SystemName can't be converted
     */
    @Override
    @Nonnull
    public Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        Sensor s;
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName, getSystemPrefix());
        if (sName.equals("")) {
            // system name is not valid
            throw new IllegalArgumentException("Invalid Secsi Sensor system name - " +  // NOI18N
                    systemName);
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s != null) {
            throw new IllegalArgumentException("Secsi Sensor with this name already exists - " +  // NOI18N
                    systemName);
        }
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName, getSystemPrefix());
        s = getBySystemName(altName);
        if (s != null) {
            throw new IllegalArgumentException("Secsi Sensor with name  " +  // NOI18N
                    systemName + " already exists as " + altName);
        }
        // check bit number
        int bit = SerialAddress.getBitFromSystemName(sName, getSystemPrefix());
        if ((bit <= 0) || (bit >= SENSORSPERNODE)) {
            throw new IllegalArgumentException("Sensor bit number " +  // NOI18N
                    Integer.toString(bit) + " is outside the supported range 1-" +
                    Integer.toString(SENSORSPERNODE - 1));
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
            log.warn("Sensor '{}' refers to an undefined Serial Node.", sName);
            return s;
        }
        // register this sensor with the Serial Node
        node.registerSensor(s, bit - 1);
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String systemName, @Nonnull Locale locale) {
        return SerialAddress.validateSystemNameFormat(systemName, getSystemNamePrefix(), locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, typeLetter(), this.getSystemPrefix()));
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
        SerialNode node = (SerialNode) getMemo().getTrafficController().getNodeFromAddress(r.getAddr());
        if (node != null) {
            node.markChanges(r);
        }
    }

    /**
     * Register any orphan Sensors when a new Serial Node is created.
     */
    @SuppressWarnings("deprecation") // needs careful unwinding for Set operations
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
                    tNode = SerialAddress.getNodeFromSystemName(sName, getMemo().getTrafficController());
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
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(SerialSensorManager.class);

}
