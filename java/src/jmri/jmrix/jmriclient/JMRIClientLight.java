package jmri.jmrix.jmriclient;

import jmri.Light;
import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMRIClient implementation of the Light interface.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Paul Bender Copyright (C) 2010
 */
public class JMRIClientLight extends AbstractLight implements JMRIClientListener {

    // data members
    private int _number;   // light number
    private JMRIClientTrafficController tc = null;
    private String transmitName = null;

    /**
     * JMRIClient lights use the light number on the remote host.
     */
    public JMRIClientLight(int number, JMRIClientSystemConnectionMemo memo) {
        super(memo.getSystemPrefix() + "l" + number);
        _number = number;
        transmitName = memo.getTransmitPrefix() + "L" + number;
        tc = memo.getJMRIClientTrafficController();
        // At construction, register for messages
        tc.addJMRIClientListener(this);
        // then request status
        requestUpdateFromLayout();
    }

    public int getNumber() {
        return _number;
    }

    //request a status update from the layout
    @Override
    public void requestUpdateFromLayout() {
        // create the message
        String text = "LIGHT " + transmitName + "\n";
        // create and send the message
        tc.sendJMRIClientMessage(new JMRIClientMessage(text), this);
    }

    // Handle a request to change state by sending a formatted packet
    // to the server.
    @Override
    public synchronized void doNewState(int oldState, int s) {
        if (oldState == s) {
            return; //no change, just quit.
        }  // sort out states
        if ((s & Light.ON) != 0) {
            // first look for the double case, which we can't handle
            if ((s & Light.OFF) != 0) {
                // this is the disaster case!
                log.error("Cannot command both ON and OFF " + s);
                return;
            } else {
                // send a ON command
                sendMessage(true);
            }
        } else {
            // send a OFF command
            sendMessage(false);
        }

        notifyStateChange(oldState, s);

    }

    protected void sendMessage(boolean on) {
        // get the message text
        String text;
        if (on) {
            text = "LIGHT " + transmitName + " ON\n";
        } else // thrown
        {
            text = "LIGHT " + transmitName + " OFF\n";
        }

        // create and send the message itself
        tc.sendJMRIClientMessage(new JMRIClientMessage(text), this);
    }

    // to listen for status changes from JMRIClient system
    @Override
    public synchronized void reply(JMRIClientReply m) {
        String message = m.toString();
        if (!message.contains(transmitName + " ")) {
            return; // not for us
        }
        if (m.toString().contains("OFF")) {
            notifyStateChange(mState, jmri.Light.OFF);
        } else if (m.toString().contains("ON")) {
            notifyStateChange(mState, jmri.Light.ON);
        } else {
            notifyStateChange(mState, jmri.Light.UNKNOWN);
        }
    }

    @Override
    public void message(JMRIClientMessage m) {
    }

    private final static Logger log = LoggerFactory.getLogger(JMRIClientLight.class);

}



