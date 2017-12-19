package jmri.jmrix.lenz;

import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the XpressNet specific Sensor implementation.
 * <p>
 * System names are "XSnnn", where nnn is the sensor number without padding.
 *
 * @author Paul Bender Copyright (C) 2003-2010
 * @navassoc 1 - * jmri.jmrix.lenz.XNetSensor
 */
public class XNetSensorManager extends jmri.managers.AbstractSensorManager implements XNetListener {

    // ctor has to register for XNet events
    public XNetSensorManager(XNetTrafficController controller, String prefix) {
        tc = controller;
        tc.addXNetListener(XNetInterface.FEEDBACK, this);
        this.prefix = prefix;
    }

    protected XNetTrafficController tc = null;
    protected String prefix = null;

    /**
     * Return the system letter for XpressNet.
     */
    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Deprecated
    static public XNetSensorManager instance() {
        return mInstance;
    }
    static private XNetSensorManager mInstance = null;

    // to free resources when no longer used
    @Override
    public void dispose() {
        tc.removeXNetListener(XNetInterface.FEEDBACK, this);
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
        int bitNum = XNetAddress.getBitFromSystemName(systemName, prefix);
        if (bitNum == -1) {
            return (null);
        }
        // normalize system name
        String sName = prefix + typeLetter() + bitNum;
        // create the new Sensor object
        return new XNetSensor(sName, userName, tc, prefix);
    }

    // listen for sensors, creating them as needed
    @Override
    public void message(XNetReply l) {
        log.debug("received message: {}", l);
        if (l.isFeedbackBroadcastMessage()) {
            int numDataBytes = l.getElement(0) & 0x0f;
            for (int i = 1; i < numDataBytes; i += 2) {
                if (l.getFeedbackMessageType(i) == 2) {
                    // This is a feedback encoder message. The address of the 
                    // Feedback sensor is byte two of the message.
                    int address = l.getFeedbackEncoderMsgAddr(i);
                    log.debug("Message for feedback encoder {}", address);

                    int firstaddress = ((address) * 8) + 1;
                    // Each Feedback encoder includes 8 addresses, so register 
                    // a sensor for each address.
                    for (int j = 0; j < 8; j++) {
                        String s = prefix + typeLetter() + (firstaddress + j);
                        if (null == getBySystemName(s)) {
                            // The sensor doesn't exist.  We need to create a 
                            // new sensor, and forward this message to it.
                            ((XNetSensor) provideSensor(s)).initmessage(l);
                        } else {
                            // The sensor exists.  We need to forward this 
                            // message to it.
                            Sensor xns = getBySystemName(s);
                            if (xns == null) {
                                log.error("Failed to get sensor for {}", s);
                            } else {
                                ((XNetSensor) xns).message(l);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Listen for the messages to the LI100/LI101.
     */
    @Override
    public void message(XNetMessage l) {
    }

    /**
     * Handle a timeout notification.
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    /**
     * Validate Sensor system name format.
     * Logging of handled cases no higher than WARN.
     *
     * @return VALID if system name has a valid format, else return INVALID
     */
    public NameValidity validSystemNameFormat(String systemName) {
        return (XNetAddress.validSystemNameFormat(systemName, 'S', getSystemPrefix()));
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
                encoderAddress = Integer.valueOf(curAddress.substring(0, seperator)).intValue();
                input = Integer.valueOf(curAddress.substring(seperator + 1)).intValue();
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

    private final static Logger log = LoggerFactory.getLogger(XNetSensorManager.class);

}
