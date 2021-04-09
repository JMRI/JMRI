package jmri.jmrix.dccpp;

import static jmri.jmrix.dccpp.DCCppConstants.MAX_SENSOR_ID;

import java.util.Locale;
import javax.annotation.Nonnull;
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

    /**
     * Create an new DCC++ SensorManager.
     * Has to register for DCC++ events.
     *
     * @param memo the supporting system connection memo
     */
    public DCCppSensorManager(DCCppSystemConnectionMemo memo) {
        super(memo);
        tc = memo.getDCCppTrafficController();
        // set up listener
        tc.addDCCppListener(DCCppInterface.FEEDBACK, this);
        // request list of sensors
        DCCppMessage msg = DCCppMessage.makeSensorListMsg();
        tc.sendDCCppMessage(msg, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public DCCppSystemConnectionMemo getMemo() {
        return (DCCppSystemConnectionMemo) memo;
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        tc.removeDCCppListener(DCCppInterface.FEEDBACK, this);
        super.dispose();
    }

    // DCCpp specific methods

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException when SystemName can't be converted
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        int addr;
        try {
            addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Can't convert " +  // NOI18N
                    systemName.substring(getSystemNamePrefix().length()) +
                    " to DCC++ sensor address"); // NOI18N
        }
        return new DCCppSensor(getSystemNamePrefix() + addr, userName, tc);
    }

    /**
     * Listen for sensors, creating them as needed.
     * 
     * @param l the message to parse
     */
    @Override
    public void message(DCCppReply l) {
        int addr = -1;  // -1 flags that no sensor address was found in reply
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
            String s = getSystemNamePrefix() + (addr);
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
     * Listen for the outgoing messages (to the command station)
     * 
     * @param l the message to parse
     */
    @Override
    public void message(DCCppMessage l) {
    }

    // Handle message timeout notification
    // If the message still has retries available, reduce retries and send it back to the traffic controller.
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        log.debug("Notified of timeout on message '{}' , {} retries available.", msg, msg.getRetries());
        if (msg.getRetries() > 0) {
            msg.setRetries(msg.getRetries() - 1);
            tc.sendDCCppMessage(msg, this);
        }        
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    @Override
    @Nonnull
    synchronized public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        int encoderAddress = 0;
        int input = 0;

        if (curAddress.contains(":")) {
            // Address format passed is in the form of encoderAddress:input or T:turnout address
            int seperator = curAddress.indexOf(":");
            try {
                encoderAddress = Integer.parseInt(curAddress.substring(0, seperator));
                input = Integer.parseInt(curAddress.substring(seperator + 1));
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert " + curAddress + " into the cab and input format of nn:xx");
            }
            iName = ((encoderAddress - 1) * 8) + input;
        } else {
            // Entered in using the old format
            try {
                iName = Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) {
                throw new JmriException("Hardware Address "+curAddress+" should be a number or cab and input format of nn:xx");
            }
        }

        return prefix + typeLetter() + iName;
    }

    int iName; // must synchronize to avoid race conditions.

    /**
     * Provide next valid DCC++ address. Does not enforce any rules on the
     * encoder or input values.
     *
     * @param curAddress the current address
     * @param prefix     the system connection prefix
     * @return the next valid address after the current address
     */
    @Override
    synchronized public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws JmriException {

        String tmpSName = createSystemName(curAddress, prefix);
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if (s != null || ignoreInitialExisting) {
            for (int x = 1; x < 10; x++) {
                iName = iName + 1;
                s = getBySystemName(prefix + typeLetter() + iName);
                if (s == null) {
                    return Integer.toString(iName);
                }
            }
            throw new JmriException(Bundle.getMessage("InvalidNextValidTenInUse",getBeanTypeHandled(true),curAddress,iName));
        } else {
            return Integer.toString(iName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String systemName, Locale locale) {
        return validateIntegerSystemNameFormat(systemName, 1, MAX_SENSOR_ID, locale);
    }

    /**
     * Get the bit address from the system name.
     * @param systemName a valid Sensor System Name
     * @return the sensor number extracted from the system name
     */
    public int getBitFromSystemName(String systemName) {
        try {
            validateSystemNameFormat(systemName, Locale.getDefault());
        } catch (IllegalArgumentException ex) {
            return 0;
        }
        return Integer.parseInt(systemName.substring(getSystemNamePrefix().length()));
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
