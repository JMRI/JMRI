package jmri.jmrix.roco.z21;

import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the Z21 RMBus specific Sensor implementation.
 * <p>
 * System names are "XSnnn", where nnn is the sensor number without padding.
 *
 * @author Paul Bender Copyright (C) 2003-2010
 * @navassoc 1 - * jmri.jmrix.lenz.Z21RMBusSensor
 */
public class Z21RMBusSensorManager extends jmri.managers.AbstractSensorManager implements Z21Listener {

    // ctor has to register for Z21 events
    public Z21RMBusSensorManager(Z21SystemConnectionMemo memo) {
        _memo = memo;
        _memo.getTrafficController().addz21Listener(this);
        // make sure we are going to get RMBus data from the command station
        // set the broadcast flags so we get messages we may want to hear
        _memo.getRocoZ21CommandStation().setRMBusMessagesFlag(true);
        // and forward the flags to the command station.
        _memo.getTrafficController().sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(
              _memo.getRocoZ21CommandStation().getZ21BroadcastFlags()),null);
        // And then send a message requesting an update from the hardware.
        // This is required because the RailCom data currently requires polling.
        _memo.getTrafficController().sendz21Message(Z21Message.getLanRailComGetDataRequestMessage(),this);
    }

    protected Z21SystemConnectionMemo _memo = null;

    /**
     * Return the system letter for XpressNet.
     */
    @Override
    public String getSystemPrefix() {
        return _memo.getSystemPrefix();
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        _memo.getTrafficController().removez21Listener(this);
        super.dispose();
    }

    // XpressNet specific methods

    /**
     * Create a new Sensor based on the system name.
     * Assumes calling method has checked that a Sensor with this
     * system name does not already exist.
     *
     * @return null if the system name is not in a valid format
     */
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        // check if the output bit is available
        int bitNum = Z21RMBusAddress.getBitFromSystemName(systemName, getSystemPrefix());
        if (bitNum == -1) {
            return (null);
        }
        // create the new Sensor object
        return new Z21RMBusSensor(systemName, userName, 
                   _memo.getTrafficController(), getSystemPrefix());
    }

    @Override
    public void reply(Z21Reply l) {
        log.debug("received message: {}", l);
    }

    /**
     * Listen for the messages to the Z21.
     */
    @Override
    public void message(Z21Message l) {
    }

    /**
     * Validate Sensor system name format.
     * Logging of handled cases no higher than WARN.
     *
     * @return VALID if system name has a valid format, else return INVALID
     */
    public NameValidity validSystemNameFormat(String systemName) {
        return (Z21RMBusAddress.validSystemNameFormat(systemName, 'S', getSystemPrefix()));
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
                log.error("Unable to convert {} into the cab and input format of nn:xx", curAddress);
                throw new JmriException("Hardware Address passed should be a number");
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
        return prefix + typeLetter() + iName;
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
        String entryToolTip = Bundle.getMessage("AddInputEntryToolTip");
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(Z21RMBusSensorManager.class);

}
