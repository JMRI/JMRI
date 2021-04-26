package jmri.jmrix.grapevine;

import java.util.Locale;

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
    @Nonnull
    public GrapevineSystemConnectionMemo getMemo() {
        return (GrapevineSystemConnectionMemo) memo;
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
        String prefix = getSystemPrefix();
        log.debug("createNewSensor {} {}", systemName, userName);
        Sensor s;
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName, prefix);
        if (sName.isEmpty()) {
            // system name is not valid
            throw new IllegalArgumentException("Invalid Grapevine Sensor system name - " +  // NOI18N
                    systemName);
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s != null) {
            throw new IllegalArgumentException("Grapevine Sensor with this name already exists - " +  // NOI18N
                    systemName);
        }
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName, prefix);
        s = getBySystemName(altName);
        if (s != null) {
            throw new IllegalArgumentException("Grapevine Sensor with name  " +  // NOI18N
                    systemName + " already exists as " + altName);
        }
        // check bit number
        int bit = SerialAddress.getBitFromSystemName(sName, prefix);
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
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
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
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        return SerialAddress.validateSystemNameFormat(name, this, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
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
     * @param node node to register sensors for.
     */
    public void registerSensorsForNode(SerialNode node) {
        // Iterate through list containing names of all Sensors
        SerialNode tNode;
        for (Sensor s : getNamedBeanSet()) {
            String sName = s.getSystemName();
            log.debug("System Name is {}", sName);
            if (sName.startsWith(getSystemNamePrefix())) { // multichar prefix
                // This is a Sensor
                tNode = SerialAddress.getNodeFromSystemName(sName, getMemo().getTrafficController());
                if (tNode == node) {
                    // This sensor is for this new Serial Node - register it                    
                    node.registerSensor(s, SerialAddress.getBitFromSystemName(sName, getSystemPrefix()));
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialSensorManager.class);

}
