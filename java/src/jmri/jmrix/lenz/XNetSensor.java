package jmri.jmrix.lenz;

import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractSensor for XpressNet layouts.
 *
 * @author Paul Bender Copyright (C) 2003-2010
 */
public class XNetSensor extends AbstractSensor implements XNetListener {

    private boolean statusRequested = false;

    private int address;
    private int baseaddress; /* The result of integer division of the 
     sensor address by 8 */

    private int nibble;      /* Is this sensor in the upper or lower 
     nibble for the feedback encoder */

    private int nibblebit;   /* Which bit in the nibble represents this 
     sensor */

    private String systemName;

    protected XNetTrafficController tc = null;

    public XNetSensor(String systemName, String userName, XNetTrafficController controller, String prefix) {
        super(systemName, userName);
        tc = controller;
        init(systemName, prefix);
    }

    public XNetSensor(String systemName, XNetTrafficController controller, String prefix) {
        super(systemName);
        tc = controller;
        init(systemName, prefix);
    }

    /**
     * Common initialization for all constructors.
     */
    private void init(String id, String prefix) {
        // store address
        systemName = id;
        address = XNetAddress.getBitFromSystemName(systemName, prefix);
        // calculate the base address, the nibble, and the bit to examine
        baseaddress = ((address - 1) / 8);
        int temp = (address - 1) % 8;
        if (temp < 4) {
            // This address is in the lower nibble
            nibble = 0x00;
        } else {
            nibble = 0x10;
        }
        switch (temp % 4) {
            case 0:
                nibblebit = 0x01;
                break;
            case 1:
                nibblebit = 0x02;
                break;
            case 2:
                nibblebit = 0x04;
                break;
            case 3:
                nibblebit = 0x08;
                break;
            default:
                nibblebit = 0x00;
        }
        if (log.isDebugEnabled()) {
            log.debug("Created Sensor {} (Address {},  position {})",
                    systemName, baseaddress,
                    (((address - 1) % 8) + 1)
            );
        }
        // Finally, request the current state from the layout.
        tc.getFeedbackMessageCache().requestCachedStateFromLayout(this);
    }

    /**
     * Request an update on status by sending an XpressNet message.
     */
    @Override
    public void requestUpdateFromLayout() {
        // To do this, we send an XpressNet Accessory Decoder Information
        // Request.
        // The generated message works for Feedback modules and turnouts 
        // with feedback, but the address passed is translated as though it 
        // is a turnout address.  As a result, we substitute our base 
        // address in for the address. after the message is returned.
        XNetMessage msg = XNetMessage.getFeedbackRequestMsg(baseaddress,
                (nibble == 0x00));
        msg.setElement(1, baseaddress);
        msg.setParity();
        synchronized (this) {
            statusRequested = true;
        }
        tc.sendXNetMessage(msg, null); // The reply is treated as a broadcast
        // and is returned using the manager.
    }

    /**
     * initmessage is a package protected class which allows the Manger to send
     * a feedback message at initialization without changing the state of the
     * sensor with respect to whether or not a feedback request was sent. This
     * is used only when the sensor is created by on layout feedback.
     */
    synchronized void initmessage(XNetReply l) {
        boolean oldState = statusRequested;
        message(l);
        statusRequested = oldState;
    }

    /**
     * implementing classes will typically have a function/listener to get
     * updates from the layout, which will then call public void
     * firePropertyChange(String propertyName, Object oldValue, Object newValue)
     * _once_ if anything has changed state (or set the commanded state
     * directly)
     */
    @Override
    public synchronized void message(XNetReply l) {
        if (log.isDebugEnabled()) {
            log.debug("received message: " + l);
        }
        if (l.isFeedbackBroadcastMessage()) {
            int numDataBytes = l.getElement(0) & 0x0f;
            for (int i = 1; i < numDataBytes; i += 2) {
                if ((l.getFeedbackMessageType(i) == 2)
                        && baseaddress == l.getFeedbackEncoderMsgAddr(i)
                        && nibble == (l.getElement(i + 1) & 0x10)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Message for sensor " + systemName
                                + " (Address " + baseaddress
                                + " position " + (address - (baseaddress * 8))
                                + ")");
                    }
                    if (statusRequested && l.isUnsolicited()) {
                        l.resetUnsolicited();
                        statusRequested = false;
                    }
                    if (((l.getElement(i + 1) & nibblebit) != 0) ^ _inverted) {
                        setOwnState(Sensor.ACTIVE);
                    } else {
                        setOwnState(Sensor.INACTIVE);
                    }
                }
            }
        }
        return;
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
    public void dispose() {
        super.dispose();
    }

    /**
     * Package protected routine to get the Sensor Number.
     */
    int getNumber() {
        return address;
    }

    /**
     * Package protected routine to get the Sensor Base Address.
     */
    int getBaseAddress() {
        return baseaddress;
    }

    /**
     * Package protected routine to get the Sensor Nibble.
     */
    int getNibble() {
        return nibble;
    }

    private final static Logger log = LoggerFactory.getLogger(XNetSensor.class);

}
