package jmri.jmrix.roco.z21;

import java.util.Locale;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the Z21Specific Sensor implementation.
 * <p>
 * for RM Bus sensors, System names are "ZSnnn", where Z is the
 * user-configurable system prefix and nnn is the sensor number without padding.
 * <p>
 * for CAN Bus sensors, System names are "ZSmm:pp" where Z is the
 * user-configurable system prefix, mm is the CAN bus module id and pp is the
 * contact number.
 *
 * @author Paul Bender Copyright (C) 2003-2018
 * @navassoc 1 - * jmri.jmrix.lenz.Z21RMBusSensor
 * @navassoc 1 - * jmri.jmrix.lenz.Z21CanSensor
 */
public class Z21SensorManager extends jmri.managers.AbstractSensorManager implements Z21Listener {

    // ctor has to register for Z21 events
    public Z21SensorManager(Z21SystemConnectionMemo memo) {
        super(memo);
        // register for messages
        memo.getTrafficController().addz21Listener(this);
        // make sure we are going to get can detector and RMBus data from 
        // the command station
        // set the broadcast flags so we get messages we may want to hear
        memo.getRocoZ21CommandStation().setCanDetectorFlag(true);
        memo.getRocoZ21CommandStation().setRMBusMessagesFlag(true);
        // and forward the flags to the command station.
        memo.getTrafficController().sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(
                memo.getRocoZ21CommandStation().getZ21BroadcastFlags()), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Z21SystemConnectionMemo getMemo() {
        return (Z21SystemConnectionMemo) memo;
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        getMemo().getTrafficController().removez21Listener(this);
        super.dispose();
    }

    // Z21 specific methods
    /**
     * Create a new Sensor based on the system name. Assumes calling method has
     * checked that a Sensor with this system name does not already exist.
     *
     * @return null if the system name is not in a valid format
     */
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        if (systemName.contains(":")) {
            // check for CAN format.
            int bitNum = Z21CanBusAddress.getBitFromSystemName(systemName, getSystemPrefix());
            if (bitNum != -1) {
                return new Z21CanSensor(systemName, userName, getMemo());
            } else {
                log.warn("Invalid Sensor name: {} " + systemName);
                throw new IllegalArgumentException("Invalid Sensor name: " + systemName);
            }
        } else {
            // check if the output bit is available
            int bitNum = Z21RMBusAddress.getBitFromSystemName(systemName, getSystemPrefix());
            if (bitNum != -1) {
                // create the new RMBus Sensor object
                return new Z21RMBusSensor(systemName, userName,
                        getMemo().getTrafficController(), getSystemPrefix());
            } else {
                log.warn("Invalid Sensor name: {} " + systemName);
                throw new IllegalArgumentException("Invalid Sensor name: " + systemName);
            }
        }
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void reply(Z21Reply msg) {
        log.debug("received message: {}", msg);
        // LAN_CAN_DETECTOR message are related to CAN reporters/sensors.
        if (msg.isCanDetectorMessage()) {
            int type = (msg.getElement(9) & 0xFF);
            log.debug("Sensor message type {}", type);
            if (type == 0x01) {
                log.debug("Received LAN_CAN_DETECTOR message");
                int netID = (msg.getElement(4) & 0xFF) + ((msg.getElement(5) & 0xFF) << 8);
                int msgPort = (msg.getElement(8) & 0xFF);
                int address = (msg.getElement(6) & 0xFF) + ((msg.getElement(7) & 0xFF) << 8);
                String sysName = getSystemPrefix() + typeLetter() + address + ":" + msgPort;
                Z21CanSensor r = (Z21CanSensor) getBySystemName(sysName);
                if (null == r) {
                    // try with the module's CAN network ID
                    sysName = getSystemPrefix() + typeLetter() + String.format("%4x", netID) + ":" + msgPort;
                    r = (Z21CanSensor) getBySystemName(sysName);
                    if (null == r) {
                        log.debug("Creating reporter {}", sysName);
                        // need to create a new one, and send the message on 
                        // to the newly created object.
                        ((Z21CanSensor) provideSensor(sysName)).reply(msg);
                    }
                }
            }
        } else if (msg.isRMBusDataChangedReply()) {
            log.debug("Received RM Bus Data Changed message");
            // we could create sensors here automatically, but the 
            // feed response contains data for 80 sensors, with no way
            // to tell which of the 80 are actually connected.
        }
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void message(Z21Message l) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        name = validateSystemNamePrefix(name, locale);
        if (name.substring(getSystemNamePrefix().length()).contains(":")) {
            return Z21CanBusAddress.validateSystemNameFormat(name, this, locale);
        } else {
            return Z21RMBusAddress.validateSystemNameFormat(name, this, locale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return Z21RMBusAddress.validSystemNameFormat(systemName, 'S', getSystemPrefix()) == NameValidity.VALID
                ? NameValidity.VALID
                : Z21CanBusAddress.validSystemNameFormat(systemName, 'S', getSystemPrefix());
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    @Override
    synchronized public String createSystemName(String curAddress, String prefix) throws JmriException {
        int encoderAddress = 0;
        int input = 0;

        if (curAddress.contains(":")) {
            // Address format passed is in the form of encoderAddress:input or T:turnout address
            int seperator = curAddress.indexOf(":");
            try {
                encoderAddress = Integer.parseInt(curAddress.substring(0, seperator));
                input = Integer.parseInt(curAddress.substring(seperator + 1));
            } catch (NumberFormatException ex) {
                // system name may include hex values for CAN sensors.
                try {
                    encoderAddress = Integer.parseInt(curAddress.substring(0, seperator), 16);
                    input = Integer.parseInt(curAddress.substring(seperator + 1));
                    return getSystemPrefix() + typeLetter() + String.format("%4x", encoderAddress) + ":" + input;
                } catch (NumberFormatException ex1) {
                    log.error("Unable to convert {} into the cab and input format of nn:xx", curAddress);
                    throw new JmriException("Hardware Address passed should be a number");
                }
            }
            iName = ((encoderAddress - 1) * 8) + input;
        } else {
            // Entered in using the old format
            try {
                iName = Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) {
                log.error("Unable to convert {} Hardware Address to a number", curAddress);
                throw new JmriException("Hardware Address passed should be a number");
            }
        }
        return getSystemPrefix() + typeLetter() + iName;
    }

    int iName; // must synchronize to avoid race conditions.

    /**
     * Does not enforce any rules on the encoder or input values.
     */
    @Override
    synchronized public String getNextValidAddress(String curAddress, String prefix) {

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
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(Z21SensorManager.class);

}
