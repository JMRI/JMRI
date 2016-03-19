package jmri.jmrix.roco.z21;

import jmri.implementation.AbstractTurnout;
import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTurnout;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.jmrix.lenz.XNetTurnout for Roco Z21/z21 systems.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Z21XNetTurnout extends XNetTurnout implements XNetListener {

    public Z21XNetTurnout(String prefix, int pNumber, XNetTrafficController controller) {  
        super(prefix,pNumber,controller);
    }

    // Handle a request to change state by sending an XPressNet command
    @Override
    synchronized protected void forwardCommandChangeToLayout(int s) {
        if (s != _mClosed && s != _mThrown) {
            log.warn("Turnout " + mNumber + ": state " + s + " not forwarded to layout.");
            return;
        }
        // get the right packet
        XNetMessage msg = Z21XNetMessage.getSetTurnoutRequestMessage(mNumber,
                (s & _mThrown) != 0,
                true, false ); // for now always active and not queued.
        if (getFeedbackMode() == SIGNAL) {
            msg.setTimeout(0); // Set the timeout to 0, so the off message can
            // be sent imediately.
            // leave the next line commented out for now.
            // it may be enabled later to allow SIGNAL mode to ignore
            // directed replies, which lets the traffic controller move on
            // to the next message without waiting.
            //msg.setBroadcastReply();
            tc.sendXNetMessage(msg, null);
            sendOffMessage();
        } else {
            tc.sendXNetMessage(msg, this);
            internalState = COMMANDSENT;
        }
    }

    /**
     * request an update on status by sending an XPressNet message
     */
    @Override
    public void requestUpdateFromLayout() {
        // On the z21, we send a LAN_X_GET_TURNOUT_INFO message 
        // (see section 5.1 of the protocol documenation ).
        XNetMessage msg = Z21XNetMessage.getTurnoutInfoRequestMessage(mNumber);
        synchronized (this) {
            internalState = STATUSREQUESTSENT;
        }
        tc.sendXNetMessage(msg, null); //status is returned via the manager.
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
        // If we're in the OFFSENT state, we need to send another OFF message.
        if (internalState == OFFSENT) {
            sendOffMessage();
        }

    }

    /**
     * initmessage is a package proteceted class which allows the Manger to send
     * a feedback message at initilization without changing the state of the
     * turnout with respect to whether or not a feedback request was sent. This
     * is used only when the turnout is created by on layout feedback.
     *
     * @param l
     *
     */
    synchronized void initmessage(XNetReply l) {
        int oldState = internalState;
        message(l);
        internalState = oldState;
    }

    public void message(XNetReply l) {
        if (log.isDebugEnabled()) {
            log.debug("recieved message: " + l);
        }
        if (l.getElement(0)==Z21Constants.LAN_X_TURNOUT_INFO) {
          // bytes 2 and 3 are the address.
          int address = (l.getElement(1) << 8) + l.getElement(2);
          if(log.isDebugEnabled()) {
               log.debug("message has address: {}",address);
          }
          // if this is for this turnout, check the turnout state.
          if(mNumber==address) {

             // this is very basic right now.  We need to handle
             // at least monitoring mode feedback properly.

             synchronized(this) {
                switch(l.getElement(3)){
                   case 0x03: newKnownState(INCONSISTENT);
                              break;
                   case 0x02: newKnownState(THROWN);
                              break;
                   case 0x01: newKnownState(CLOSED);
                              break;
                   case 0x00:
                   default:
                              newKnownState(UNKNOWN);
                }
             }
             if(internalState == COMMANDSENT) {
                sendOffMessage();  // turn off the repition on the track.
             } else if(internalState == OFFSENT ) {
                /* the command was successfully recieved */
                synchronized (this) {
                    newKnownState(getCommandedState());
                    internalState = IDLE;
                }
             }
          }
          
        } else {
          super.message(l); // the the XPressNetTurnoutManager code 
                            // handle any other replies.
        }
    }

    protected XNetMessage getOffMessage() {
        return( Z21XNetMessage.getSetTurnoutRequestMessage(mNumber,
                (getCommandedState() ==  _mThrown),
                false, false ) );// for now always not active and not queued.
    }

    private final static Logger log = LoggerFactory.getLogger(Z21XNetTurnout.class.getName());

}
