package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.jmrix.lenz.XNetTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.jmrix.lenz.XNetTurnout for Roco Z21/z21 systems.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Z21XNetTurnout extends XNetTurnout {

    public Z21XNetTurnout(String prefix, int pNumber, XNetTrafficController controller) {  
        super(prefix,pNumber,controller);
    }

    /**
     * {@inheritDoc}
     * Sends an XpressNet command.
     */
    @Override
    protected synchronized void forwardCommandChangeToLayout(int s) {
        if (s != _mClosed && s != _mThrown) {
            log.warn("Turnout {}: state {} not forwarded to layout.", mNumber, s);
            return;
        }
        log.debug("Turnout {}: forwarding state  {} to layout.", mNumber, s);
        // get the right packet
        XNetMessage msg = Z21XNetMessage.getZ21SetTurnoutRequestMessage(mNumber,
                (s & _mThrown) != 0,
                true, false ); // for now always active and not queued.
        if (getFeedbackMode() == SIGNAL) {
            msg.setTimeout(0); // Set the timeout to 0, so the off message can
            // be sent immediately.
            // leave the next line commented out for now.
            // it may be enabled later to allow SIGNAL mode to ignore
            // directed replies, which lets the traffic controller move on
            // to the next message without waiting.
            //msg.setBroadcastReply();
            tc.sendXNetMessage(msg, null);
            sendOffMessage(s);
        } else {
            queueMessage(msg,COMMANDSENT,this);
        }
    }

    /**
     * Request an update on status by sending an XpressNet message.
     */
    @Override
    public void requestUpdateFromLayout() {
        log.debug("Turnout {} requesting update from layout",mNumber);
        // This will handle ONESENSOR and TWOSENSOR feedback modes.
        super.requestUpdateFromLayout();

        // On the z21, we send a LAN_X_GET_TURNOUT_INFO message
        // (see section 5.1 of the protocol documenation ).
        XNetMessage msg = Z21XNetMessage.getZ21TurnoutInfoRequestMessage(mNumber);
        msg.setBroadcastReply();
        queueMessage(msg,IDLE,null); //status is returned via the manager.
    }

    // Handle a timeout notification.
    @Override
    public synchronized void notifyTimeout(XNetMessage msg) {
        log.debug("Notified of timeout on message {}",msg);
        // If we're in the OFFSENT state, we need to send another OFF message.
        synchronized (this) {
            if (internalState == OFFSENT) {
               sendOffMessage(getCommandedState());
            }
        }
    }

    /**
     * initmessage is a package proteceted class which allows the Manger to send
     * a feedback message at initialization without changing the state of the
     * turnout with respect to whether or not a feedback request was sent.
     * This is used only when the turnout is created by on layout feedback.
     * @param l Init message
     */
    synchronized void initMessageZ21(XNetReply l) {
        int oldState = internalState;
        message(l);
        internalState = oldState;
    }

    @Override
    public synchronized void message(XNetReply l) {
        log.debug("received message: {}",l);
        if (l.getElement(0)==Z21Constants.LAN_X_TURNOUT_INFO) {
          // bytes 2 and 3 are the address.
          int address = (l.getElement(1) << 8) + l.getElement(2);
          // the address sent byte the Z21 is one less than what JMRI's 
          // XpressNet code (and Lenz systems) expect.
          address = address + 1; 
          if(log.isDebugEnabled()) {
               log.debug("message has address: {}",address);
          }
          // if this is for this turnout, check the turnout state.
          if(mNumber==address) {
              int messageState = decodeZ21FeedbackMessageState(l);
              if(getFeedbackMode() == MONITORING ) {
                  newKnownState(messageState);
              } else if(getFeedbackMode()==DIRECT) {
                  newKnownState(getCommandedState());
              }
              if(internalState == COMMANDSENT) {
                /* the command was successfully received */
                sendOffMessage(messageState);  // turn off the repetition on the track.
                // and check to see if there are any more queued messages.
                sendQueuedMessage();
             }
          }
        } else {
          super.message(l); // the XpressNetTurnout code
                            // handles any other replies.
        }
    }

    private int decodeZ21FeedbackMessageState(XNetReply l){
        int state;
        switch (l.getElement(3)) {
            case 0x03:
                state = INCONSISTENT;
                break;
            case 0x02:
                state = _inverted ? CLOSED : THROWN;
                break;
            case 0x01:
                state = _inverted ? THROWN : CLOSED;
                break;
            case 0x00:
            default:
                state = UNKNOWN;
        }
        return state;
    }

    @Override
    protected synchronized void sendOffMessage() {
       sendOffMessage(getCommandedState());
    }

    protected synchronized void sendOffMessage(int state) {
        // We need to tell the turnout to shut off the output.
        if (log.isDebugEnabled()) {
            log.debug("Sending off message for turnout {} commanded state={}", mNumber, getCommandedState());
            log.debug("Current Thread ID: {} Thread Name {}", java.lang.Thread.currentThread().getId(), java.lang.Thread.currentThread().getName());
        }
        XNetMessage msg = getOffMessage(state == _mThrown);
        // Set the known state to the commanded state.
        newKnownState(getCommandedState());
        internalState = IDLE;
        // Then send the message.
        tc.sendXNetMessage(msg, this);  // reply sent through loconet
    }

    protected synchronized XNetMessage getOffMessage(boolean state) {
        XNetMessage msg = Z21XNetMessage.getZ21SetTurnoutRequestMessage(mNumber,
                 state, false, false );// for now always not active and not queued.
        msg.setBroadcastReply(); // reply comes through loconet
        return msg;
    }

    private static final Logger log = LoggerFactory.getLogger(Z21XNetTurnout.class);

}
