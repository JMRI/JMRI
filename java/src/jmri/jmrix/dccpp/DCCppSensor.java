package jmri.jmrix.dccpp;

import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractSensor for DCC++ layouts.
 *
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Mark Underwood Copyright (C) 2015
 *
 * Based on XNetSensor
 */
public class DCCppSensor extends AbstractSensor implements DCCppListener {

    private boolean statusRequested = false;

    private int address; // NOTE: For DCC++ this is the Base Station index #
    //private int baseaddress; /* The result of integer division of the 
    // sensor address by 8 */
    
    private int pin;
    private boolean pullup;

    //private int nibble;      /* Is this sensor in the upper or lower 
    //nibble for the feedback encoder */

    private String systemName;

    protected DCCppTrafficController tc = null;
    
    public DCCppSensor(String systemName, String userName, DCCppTrafficController controller) {
        super(systemName, userName);
        tc = controller;
        init(systemName);
    }

    public DCCppSensor(String systemName, DCCppTrafficController controller) {
        super(systemName);
        tc = controller;
        init(systemName);
    }
    
    public boolean getPullup() { return(pullup); }
    public int getPin() { return(pin); }
    public int getIndex() { return(address); }

    /**
     * Common initialization for both constructors
     */
    private void init(String id) {
        // store address
        systemName = id;
        //prefix = jmri.InstanceManager.getDefault(jmri.jmrix.dccpp.DCCppSensorManager.class).getSystemPrefix();
        address = Integer.parseInt(id.substring(id.lastIndexOf('S')+1, id.length()));
        log.debug("New sensor system name {} address {}", this.getSystemName(), address);
        if (log.isDebugEnabled()) {
            log.debug("Created Sensor " + systemName);
        }
        // Finally, request the current state from the layout.
        //this.requestUpdateFromLayout();
        //tc.getFeedbackMessageCache().requestCachedStateFromLayout(this);

    }

    /**
     * request an update on status by sending a DCC++ message
     */
    @Override
    public void requestUpdateFromLayout() {
        // Yeah... this isn't really supported.  Yet.
        //
        // To do this, we send an DCC++ Accessory Decoder Information 
        // Request.
        // The generated message works for Feedback modules and turnouts 
        // with feedback, but the address passed is translated as though it 
        // is a turnout address.  As a result, we substitute our base 
        // address in for the address. after the message is returned.
 /*
        DCCppMessage msg = DCCppMessage.getFeedbackRequestMsg(baseaddress,
                (nibble == 0x00));
        msg.setElement(1, baseaddress);
        msg.setParity();
        synchronized (this) {
            statusRequested = true;
        }
        tc.sendDCCppMessage(msg, null); // The reply is treated as a broadcast
        // and is returned using the manager.
 */
    }

    /**
     * initmessage is a package protected class which allows the Manger to send
     * a feedback message at initialization without changing the state of the
     * sensor with respect to whether or not a feedback request was sent. This
     * is used only when the sensor is created by on layout feedback.
     */
    synchronized void initmessage(DCCppReply l) {
        boolean oldState = statusRequested;
        message(l);
        statusRequested = oldState;
    }

    /**
     * {@inheritDoc}
     * implementing classes will typically have a function/listener to get
     * updates from the layout, which will then call public void
     * firePropertyChange(String propertyName, Object oldValue, Object newValue)
     * _once_ if anything has changed state (or set the commanded state
     * directly)
     */
    @Override
    public synchronized void message(DCCppReply l) {
        if (log.isDebugEnabled()) {
            log.debug("received message: " + l);
        }

        if (l.isSensorDefReply()) {
            if (l.getSensorDefNumInt() == address) {
                if (log.isDebugEnabled()) {
                    log.debug("Def Message for sensor " + systemName
                              + " (Pin " + address + ")");
                }
                pin = l.getSensorDefPinInt();
                pullup = l.getSensorDefPullupBool();
                setOwnState(Sensor.UNKNOWN);
                // For reference set NamedBean properties for the pin # and pullup
                setProperty("Pin", pin);
                setProperty("Pullup", pullup);
            }
        } else if (l.isSensorReply() && (l.getSensorNumInt() == address)) {
                log.debug("Message for sensor {} (Pin {})", systemName, address);
            if (l.getSensorIsActive()) {
                setOwnState(_inverted ? Sensor.INACTIVE : Sensor.ACTIVE);
            } else if (l.getSensorIsInactive()){
                setOwnState(_inverted ? Sensor.ACTIVE : Sensor.INACTIVE);
            } else {
                setOwnState(Sensor.UNKNOWN);
            }
        }
        return;
    }

    /**
     * {@inheritDoc}
     * Listen for the messages to the Base Station... but ignore them.
     *
     * @param l the message heard
     */
    @Override
    public void message(DCCppMessage l) {
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    // package protected routine to get the Sensor Number
    int getNumber() {
        return address;
    }

    // package protected routine to get the Sensor Base Address
    int getBaseAddress() {
        //return baseaddress;
        return(address);
    }

    // package protected routine to get the Sensor Nibble
    int getNibble() {
        //return nibble;
        return(0);
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppSensor.class);

}
