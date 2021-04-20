package jmri.jmrix.cmri.serial;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractNode;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the C/MRI serial-specific Sensor implementation.
 * <p>
 * System names are "CSnnnn", where C is the user-configurable system prefix,
 * nnnn is the sensor number without padding.
 * <p>
 * Sensors are numbered from 1.
 * <p>
 * This is a SerialListener to handle the replies to poll messages. Those are
 * forwarded to the specific SerialNode object corresponding to their origin for
 * processing of the data.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2007
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
     *
     */
    static final int SENSORSPERUA = 1000;

    public SerialSensorManager(CMRISystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public CMRISystemConnectionMemo getMemo() {
        return (CMRISystemConnectionMemo) memo;
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
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        Sensor s;
        // validate the system name, and normalize it
        String sName = getMemo().normalizeSystemName(systemName);
        if (sName.isEmpty()) {
            // system name is not valid
            throw new IllegalArgumentException("Invalid C/MRI Sensor system name - " +  // NOI18N
                    systemName);
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s != null) {
            throw new IllegalArgumentException("C/MRI Sensor with this name already exists - " +  // NOI18N
                    systemName);
        }
        // check under alternate name
        String altName = getMemo().convertSystemNameToAlternate(sName);
        s = getBySystemName(altName);
        if (s != null) {
            throw new IllegalArgumentException("C/MRI Sensor with name  " +  // NOI18N
                    systemName + " already exists as " + altName);
        }
        // check bit number
        int bit = getMemo().getBitFromSystemName(sName);
        if ((bit <= 0) || (bit >= SENSORSPERUA)) {
            throw new IllegalArgumentException("Sensor bit number " +  // NOI18N
                    Integer.toString(bit) + " is outside the supported range 1-" +
                    Integer.toString(SENSORSPERUA - 1));
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null) {
            s = new SerialSensor(sName);
        } else {
            s = new SerialSensor(sName, userName);
        }

        // ensure that a corresponding Serial Node exists
        SerialNode node = (SerialNode) getMemo().getNodeFromSystemName(sName,getMemo().getTrafficController());
        if (node == null) {
            log.warn("Sensor {} refers to an undefined Serial Node.", sName);
            return s;
        }
        // register this sensor with the Serial Node
        node.registerSensor(s, bit - 1);
        return s;
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
        SerialNode node = (SerialNode) getMemo().getTrafficController().getNodeFromAddress(r.getUA());
        if (node != null) {
            node.markChanges(r);
        }
    }

    /**
     * Method to register any orphan Sensors when a new Serial Node is created.
     * @param node the node with potential orphan sensors.
     */
    public void registerSensorsForNode(SerialNode node) {
        // get list containing all Sensors
        AbstractNode tNode;
        for (Sensor s : getNamedBeanSet()) {
            String sName = s.getSystemName();
            log.debug("system name is {}", sName);
            if ( sName.startsWith(getSystemNamePrefix()) ){
                // This is a C/MRI Sensor
                tNode = getMemo().getNodeFromSystemName(sName, getMemo().getTrafficController());
                if (tNode == node) {
                    // This sensor is for this new Serial Node - register it
                    node.registerSensor(s,
                            (getMemo().getBitFromSystemName(sName) - 1));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        String tmpSName = "";
        if (curAddress.contains(":")) {
            //Address format passed is in the form node:address
            int seperator = curAddress.indexOf(":");
            try {
                nAddress = Integer.parseInt(curAddress.substring(0, seperator));
                bitNum = Integer.parseInt(curAddress.substring(seperator + 1));
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert " + curAddress + " to a number.");
            }
            tmpSName = getMemo().makeSystemName("S", nAddress, bitNum);
        } else if (curAddress.contains("B") || (curAddress.contains("b"))) {
            curAddress = curAddress.toUpperCase();
            try {
                //We do this to simply check that we have numbers in the correct places ish
                Integer.parseInt(curAddress.substring(0, 1));
                int b = (curAddress.toUpperCase()).indexOf("B") + 1;
                Integer.parseInt(curAddress.substring(b));
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert " + curAddress + " to a number");
            }
            tmpSName = prefix + typeLetter() + curAddress;
            bitNum = getMemo().getBitFromSystemName(tmpSName);
            nAddress = getMemo().getNodeAddressFromSystemName(tmpSName);
        } else {
            try {
                //We do this to simply check that the value passed is a number!
                Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert " + curAddress + " to a valid Hardware Address");
            }
            tmpSName = prefix + typeLetter() + curAddress;
            bitNum = getMemo().getBitFromSystemName(tmpSName);
            nAddress = getMemo().getNodeAddressFromSystemName(tmpSName);
        }

        return tmpSName;
    }

    private int bitNum = 0;
    private int nAddress = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws JmriException {
        //If the hardware address passed does not already exist then this can
        //be considered the next valid address.

        String tmpSName = createSystemName(curAddress, prefix);
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if (s != null || ignoreInitialExisting) {
            for (int x = 1; x < 10; x++) {
                bitNum++;
                tmpSName = getMemo().makeSystemName("S", nAddress, bitNum);
                s = getBySystemName(tmpSName);
                if (s == null) {
                    int seperator = tmpSName.lastIndexOf("S") + 1;
                    curAddress = tmpSName.substring(seperator);
                    return curAddress;
                }
            }
            throw new JmriException(Bundle.getMessage("InvalidNextValidTenInUse",getBeanTypeHandled(true),curAddress,tmpSName));
        } else {
            int seperator = tmpSName.lastIndexOf("S") + 1;
            curAddress = tmpSName.substring(seperator);
            return curAddress;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String systemName, @Nonnull Locale locale) {
        return getMemo().validateSystemNameFormat(super.validateSystemNameFormat(systemName, locale), typeLetter(), locale);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return getMemo().validSystemNameFormat(systemName, typeLetter());
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
