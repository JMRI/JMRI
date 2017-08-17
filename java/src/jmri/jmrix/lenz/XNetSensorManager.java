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

    @Override
    public String getSystemPrefix() {
        return prefix;
    }
    protected String prefix = null;

    protected XNetTrafficController tc = null;

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

    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        return new XNetSensor(systemName, userName, tc);
    }

    // ctor has to register for XNetNet events
    public XNetSensorManager(XNetTrafficController controller, String prefix) {
        tc = controller;
        tc.addXNetListener(XNetInterface.FEEDBACK, this);
        this.prefix = prefix;
    }

    // listen for sensors, creating them as needed
    @Override
    public void message(XNetReply l) {
        if (log.isDebugEnabled()) {
            log.debug("received message: " + l);
        }
        if (l.isFeedbackBroadcastMessage()) {
            int numDataBytes = l.getElement(0) & 0x0f;
            for (int i = 1; i < numDataBytes; i += 2) {
                if (l.getFeedbackMessageType(i) == 2) {
                    // This is a feedback encoder message. The address of the 
                    // Feedback sensor is byte two of the message.
                    int address = l.getFeedbackEncoderMsgAddr(i);
                    if (log.isDebugEnabled()) {
                        log.debug("Message for feedback encoder " + address);
                    }

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

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    @Override
    synchronized public String createSystemName(String curAddress, String prefix) throws JmriException {
        int encoderAddress = 0;
        int input = 0;

        if (curAddress.contains(":")) {
            //Address format passed is in the form of encoderAddress:input or T:turnout address
            int seperator = curAddress.indexOf(":");
            try {
                encoderAddress = Integer.valueOf(curAddress.substring(0, seperator)).intValue();
                input = Integer.valueOf(curAddress.substring(seperator + 1)).intValue();
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + curAddress + " into the cab and input format of nn:xx");
                throw new JmriException("Hardware Address passed should be a number");
            }
            iName = ((encoderAddress - 1) * 8) + input;
        } else {
            //Entered in using the old format
            try {
                iName = Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + curAddress + " Hardware Address to a number");
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
                    showErrorMessage("Error", "Unable to convert " + curAddress + " to a valid Hardware Address", "" + ex, "", true, false);
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
     * Provide a connection system agnostic tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddInputEntryToolTip");
        return entryToolTip;
    }

    /**
     * Provide a connection system agnostic regex for the Add new item beantable pane.
     */
    @Override
    public String getEntryRegex() {
        return "^[0-9:]{1,5}$";
        // Accepts a 4 digit number + ":" + another 4 digit number
    }

    private final static Logger log = LoggerFactory.getLogger(XNetSensorManager.class.getName());

}
