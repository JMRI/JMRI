package jmri.jmrix.cmri.serial;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractNode;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the C/MRI serial-specific Sensor implementation.
 * <P>
 * System names are "CSnnnn", where nnnn is the sensor number without padding.
 * <P>
 * Sensors are numbered from 1.
 * <P>
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
     * <P>
     * The first UA (node address) uses sensors from 1 to SENSORSPERUA-1, the
     * second from SENSORSPERUA+1 to SENSORSPERUA+(SENSORSPERUA-1), etc.
     * <P>
     * Must be more than, and is generally one more than,
     * {@link SerialNode#MAXSENSORS}
     *
     */
    static final int SENSORSPERUA = 1000;

    private CMRISystemConnectionMemo _memo = null;

    public SerialSensorManager(CMRISystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSystemPrefix() {
        return _memo.getSystemPrefix();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s;
        // validate the system name, and normalize it
        String sName = _memo.normalizeSystemName(systemName);
        if (sName.equals("")) {
            // system name is not valid
            log.error("Invalid C/MRI Sensor system name - {}", systemName);
            return null;
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s != null) {
            log.error("Sensor with this name already exists - {}", systemName);
            return null;
        }
        // check under alternate name
        String altName = _memo.convertSystemNameToAlternate(sName);
        s = getBySystemName(altName);
        if (s != null) {
            log.error("Sensor with name '{}' already exists as '{}'", systemName, altName);
            return null;
        }
        // check bit number
        int bit = _memo.getBitFromSystemName(sName);
        if ((bit <= 0) || (bit >= SENSORSPERUA)) {
            log.error("Sensor bit number, " + Integer.toString(bit)
                    + ", is outside the supported range, 1-" + Integer.toString(SENSORSPERUA - 1));
            return null;
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null) {
            s = new SerialSensor(sName);
        } else {
            s = new SerialSensor(sName, userName);
        }

        // ensure that a corresponding Serial Node exists
        SerialNode node = (SerialNode) _memo.getNodeFromSystemName(sName,_memo.getTrafficController());
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
        SerialNode node = (SerialNode) _memo.getTrafficController().getNodeFromAddress(r.getUA());
        if (node != null) {
            node.markChanges(r);
        }
    }

    /**
     * Method to register any orphan Sensors when a new Serial Node is created
     */
    public void registerSensorsForNode(SerialNode node) {
        // get list containing all Sensors
        java.util.Iterator<String> iter
                = getSystemNameList().iterator();
        // Iterate through the sensors
        AbstractNode tNode = null;
        while (iter.hasNext()) {
            String sName = iter.next();
            if (sName == null) {
                log.error("System name null during register Sensor");
            } else {
                log.debug("system name is {}", sName);
                if ((sName.charAt(0) == 'C') && (sName.charAt(1) == 'S')) { // TODO multichar prefix
                    // This is a C/MRI Sensor
                    tNode = _memo.getNodeFromSystemName(sName, _memo.getTrafficController());
                    if (tNode == node) {
                        // This sensor is for this new Serial Node - register it
                        node.registerSensor(getBySystemName(sName),
                                (_memo.getBitFromSystemName(sName) - 1));
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        String tmpSName = "";
        if (curAddress.contains(":")) {
            //Address format passed is in the form node:address
            int seperator = curAddress.indexOf(":");
            try {
                nAddress = Integer.valueOf(curAddress.substring(0, seperator)).intValue();
                bitNum = Integer.valueOf(curAddress.substring(seperator + 1)).intValue();
            } catch (NumberFormatException ex) {
                log.error("Unable to convert {} Hardware Address to a number", curAddress);
                throw new JmriException("Unable to convert " + curAddress + " to a valid Hardware Address");
            }
            tmpSName = _memo.makeSystemName("S", nAddress, bitNum);
        } else if (curAddress.contains("B") || (curAddress.contains("b"))) {
            curAddress = curAddress.toUpperCase();
            try {
                //We do this to simply check that we have numbers in the correct places ish
                Integer.parseInt(curAddress.substring(0, 1));
                int b = (curAddress.toUpperCase()).indexOf("B") + 1;
                Integer.parseInt(curAddress.substring(b));
            } catch (NumberFormatException ex) {
                log.error("Unable to convert {} Hardware Address to a number", curAddress);
                throw new JmriException("Unable to convert " + curAddress + " to a valid Hardware Address");
            }
            tmpSName = prefix + typeLetter() + curAddress;
            bitNum = _memo.getBitFromSystemName(tmpSName);
            nAddress = _memo.getNodeAddressFromSystemName(tmpSName);
        } else {
            try {
                //We do this to simply check that the value passed is a number!
                Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) {
                log.error("Unable to convert {} Hardware Address to a number", curAddress);
                throw new JmriException("Unable to convert " + curAddress + " to a valid Hardware Address");
            }
            tmpSName = prefix + typeLetter() + curAddress;
            bitNum = _memo.getBitFromSystemName(tmpSName);
            nAddress = _memo.getNodeAddressFromSystemName(tmpSName);
        }

        return tmpSName;
    }

    int bitNum = 0;
    int nAddress = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextValidAddress(String curAddress, String prefix) {
        //If the hardware address passed does not already exist then this can
        //be considered the next valid address.

        String tmpSName = "";
        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            log.error("Unable to convert {} Hardware Address to a number", curAddress);
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
                bitNum++;
                tmpSName = _memo.makeSystemName("S", nAddress, bitNum);
                s = getBySystemName(tmpSName);
                if (s == null) {
                    int seperator = tmpSName.lastIndexOf("S") + 1;
                    curAddress = tmpSName.substring(seperator);
                    return curAddress;
                }
            }
            return null;
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
    public NameValidity validSystemNameFormat(String systemName) {
        return _memo.validSystemNameFormat(systemName, 'S');
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String normalizeSystemName(String systemName) {
        return _memo.normalizeSystemName(systemName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddInputEntryToolTip");
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialSensorManager.class);

}
