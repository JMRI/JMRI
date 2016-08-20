package jmri.jmrix.dccpp;

import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the DCC++ specific Sensor implementation.
 *
 * System names are "DCCppSnnn", where nnn is the sensor number without padding.
 *
 * @author	Paul Bender Copyright (C) 2003-2010
 * @author	Mark Underwood Copyright (C) 2015
 */
public class DCCppSensorManager extends jmri.managers.AbstractSensorManager implements DCCppListener {

    public String getSystemPrefix() {
        return prefix;
    }
    protected String prefix = null;

    protected DCCppTrafficController tc = null;

    @Deprecated
    static public DCCppSensorManager instance() {
        return mInstance;
    }
    static private DCCppSensorManager mInstance = null;

    // to free resources when no longer used
    public void dispose() {
        tc.removeDCCppListener(DCCppInterface.FEEDBACK, this);
        super.dispose();
    }

    // XPressNet specific methods
    public Sensor createNewSensor(String systemName, String userName) {
        return new DCCppSensor(systemName, userName, tc);
    }

    // ctor has to register for DCC++ events
    public DCCppSensorManager(DCCppTrafficController controller, String prefix) {
        tc = controller;
        tc.addDCCppListener(DCCppInterface.FEEDBACK, this);
        this.prefix = prefix;
        DCCppMessage msg = DCCppMessage.makeSensorListMsg();
        //Then Send the version request to the controller
        tc.sendDCCppMessage(msg, this);

    }

    // listen for sensors, creating them as needed
    public void message(DCCppReply l) {
        int addr = 0;
        if (log.isDebugEnabled()) {
            log.debug("recieved message: " + l);
        }
        if (l.isSensorDefReply()) {
            addr = l.getSensorDefNumInt();
            if (log.isDebugEnabled()) {
                log.debug("SensorDef Reply for Encoder " + Integer.toString(addr));
            }
            
        } else if (l.isSensorReply()) {
            addr = l.getSensorNumInt();
            log.debug("Sensor Status Reply for Encoder" + Integer.toString(addr));
        }
        String s = prefix + typeLetter() + (addr);
        if (null == getBySystemName(s)) {
            // The sensor doesn't exist.  We need to create a 
            // new sensor, and forward this message to it.
            ((DCCppSensor) provideSensor(s)).initmessage(l);
        } else {
            // The sensor exists.  We need to forward this 
            // message to it.
            ((DCCppSensor) getBySystemName(s)).message(l);
        }

    }

    // listen for the messages to the LI100/LI101
    public void message(DCCppMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

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

    private final static Logger log = LoggerFactory.getLogger(DCCppSensorManager.class.getName());

}
