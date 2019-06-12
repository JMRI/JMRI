package jmri.jmrix.dccpp;

import javax.swing.JOptionPane;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement SensorManager for DCC++ systems.
 * <p>
 * System names are "DSnnn", where D is the user configurable system prefix,
 * nnn is the sensor number without padding.
 *
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppSensorManager extends jmri.managers.AbstractSensorManager implements DCCppListener {

    protected DCCppTrafficController tc = null;
    protected String prefix = null;

    /**
     * Create an new DCC++ SensorManager.
     * Has to register for DCC++ events.
     *
     * @param controller the TrafficController to connect the SensorManager to
     * @param prefix the system connection prefix string as set for this connection in SystemConnectionMemo
     */
    public DCCppSensorManager(DCCppTrafficController controller, String prefix) {
        tc = controller;
        tc.addDCCppListener(DCCppInterface.FEEDBACK, this);
        this.prefix = prefix;
        DCCppMessage msg = DCCppMessage.makeSensorListMsg();
        // then Send the version request to the controller
        tc.sendDCCppMessage(msg, this);
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Deprecated
    static public DCCppSensorManager instance() {
        return mInstance;
    }
    static private DCCppSensorManager mInstance = null;

    // to free resources when no longer used
    @Override
    public void dispose() {
        tc.removeDCCppListener(DCCppInterface.FEEDBACK, this);
        super.dispose();
    }

    // DCCpp specific methods

    @Override
    public Sensor createNewSensor(String systemName, String userName) throws IllegalArgumentException {
        int addr;
        try {
            addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Can't convert " +  // NOI18N
                    systemName.substring(getSystemPrefix().length() + 1) +
                    " to DCC++ sensor address"); // NOI18N
        }
        Sensor s = new DCCppSensor(prefix + "S" + addr, userName, tc);
        return s;
    }

    /**
     * Listen for sensors, creating them as needed.
     */
    @Override
    public void message(DCCppReply l) {
        int addr = -1;  // -1 flags that no sensor address was found in reply
        if (log.isDebugEnabled()) {
            log.debug("received message: " + l);
        }
        if (l.isSensorDefReply()) {
            addr = l.getSensorDefNumInt();
            if (log.isDebugEnabled()) {
                log.debug("SensorDef Reply for Encoder {}", Integer.toString(addr));
            }
            
        } else if (l.isSensorReply()) {
            addr = l.getSensorNumInt();
            if (log.isDebugEnabled()) {
                log.debug("Sensor Status Reply for Encoder {}", Integer.toString(addr));
            }
        }
        if (addr >= 0) {
            String s = prefix + typeLetter() + (addr);
            if (null == getBySystemName(s)) {
                // The sensor doesn't exist.  We need to create a 
                // new sensor, and forward this message to it.
                ((DCCppSensor) provideSensor(s)).initmessage(l);
            } else {
                // The sensor exists.  We need to forward this 
                // message to it.
                Sensor sen = getBySystemName(s);
                if (sen == null) {
                    log.error("Failed to get sensor for {}", s);
                } else {
                    ((DCCppSensor) sen).message(l);
                }
            }
        }
    }

    /**
     * Listen for the messages to the LI100/LI101.
     */
    @Override
    public void message(DCCppMessage l) {
    }

    /**
     * Handle a timeout notification.
     */
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message {}", msg.toString());
        }
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
                JOptionPane.showMessageDialog(null, Bundle.getMessage("WarningAddressAsNumber"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
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
     * Provide next valid DCC++ address.
     * Does not enforce any rules on the encoder or input values.
     */
    @Override
    synchronized public String getNextValidAddress(String curAddress, String prefix) {

        String tmpSName = "";

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false);
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
     * Get the bit address from the system name.
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(getSystemPrefix() + typeLetter()))) {
            // here if an illegal DCC++ sensor system name
            log.error("illegal character in header field of DCC++ sensor system name: {} prefix {} type {}",
                    systemName, getSystemPrefix(), typeLetter());
            return (0);
        }
        // name must be in the DCCppSnnnnn format (DCCPP prefix is user configurable)
        int num = 0;
        try {
            num = Integer.parseInt(systemName.substring(
                    getSystemPrefix().length() + 1, systemName.length()));
        } catch (Exception e) {
            log.debug("invalid character in number field of system name: {}", systemName);
            return (0);
        }
        if (num <= 0) {
            log.debug("invalid DCC++ sensor system name: {}", systemName);
            return (0);
        } else if (num > DCCppConstants.MAX_ACC_DECODER_JMRI_ADDR) {
            log.debug("bit number out of range in DCC++ sensor system name: {}", systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Validate system name format.
     *
     * @return VALID if system name has a valid format, else returns INVALID
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppSensorManager.class);

}
