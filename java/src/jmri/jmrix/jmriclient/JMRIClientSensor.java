package jmri.jmrix.jmriclient;

import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMRIClient implementation of the Sensor interface.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Paul Bender Copyright (C) 2010
 */
public class JMRIClientSensor extends AbstractSensor implements JMRIClientListener {

    // data members
    private int _number;   // sensor number
    private JMRIClientTrafficController tc = null;
    private String transmitName = null;

    /**
     * JMRIClient sensors use the sensor number on the remote host.
     */
    public JMRIClientSensor(int number, JMRIClientSystemConnectionMemo memo) {
        super(memo.getSystemPrefix() + "S" + number);
        _number = number;
        transmitName = memo.getTransmitPrefix() + "S" + number;
        tc = memo.getJMRIClientTrafficController();
        // At construction, register for messages
        tc.addJMRIClientListener(this);
        // Then request status.
        requestUpdateFromLayout();
    }

    public int getNumber() {
        return _number;
    }

    // Handle a request to change state by sending a formatted packet
    // to the server.
    @Override
    public void setKnownState(int s) throws jmri.JmriException {
        // sort out states
        if ((s & Sensor.ACTIVE) != 0) {
            // first look for the double case, which we can't handle
            if ((s & Sensor.INACTIVE) != 0) {
                // this is the disaster case!
                log.error("Cannot command both ACTIVE and INACTIVE " + s);
                return;
            } else {
                // send an ACTIVE command
                sendMessage(true ^ getInverted());
            }
        } else {
            // send a INACTIVE command
            sendMessage(false ^ getInverted());
        }
        if (_knownState != s) {
            int oldState = _knownState;
            _knownState = s;
            firePropertyChange("KnownState", Integer.valueOf(oldState), Integer.valueOf(_knownState));
        }
    }

    @Override
    public void requestUpdateFromLayout() {
        // get the message text
        String text = "SENSOR " + transmitName + "\n";

        // create and send the message itself
        tc.sendJMRIClientMessage(new JMRIClientMessage(text), this);
    }

    protected void sendMessage(boolean active) {
        // get the message text
        String text;
        if (active) {
            text = "SENSOR " + transmitName + " ACTIVE\n";
        } else // thrown
        {
            text = "SENSOR " + transmitName + " INACTIVE\n";
        }

        // create and send the message itself
        tc.sendJMRIClientMessage(new JMRIClientMessage(text), this);
    }

    // to listen for status changes from JMRIClient system
    @Override
    public void reply(JMRIClientReply m) {
        String message = m.toString();
        log.debug("Message Received: " + m);
        if (!message.contains(transmitName + " ")) {
            return; // not for us
        }
        if (m.toString().contains("INACTIVE")) {
            setOwnState(!getInverted() ? jmri.Sensor.INACTIVE : jmri.Sensor.ACTIVE);
        } else if (m.toString().contains("ACTIVE")) {
            setOwnState(!getInverted() ? jmri.Sensor.ACTIVE : jmri.Sensor.INACTIVE);
        } else {
            setOwnState(jmri.Sensor.UNKNOWN);
        }
    }

    @Override
    public void message(JMRIClientMessage m) {
    }

    private final static Logger log = LoggerFactory.getLogger(JMRIClientSensor.class);

}



