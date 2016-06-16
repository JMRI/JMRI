package jmri.jmrix.tams;

import java.util.LinkedList;
import java.util.Queue;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement a Turnout via Tams communications.
 * <P>
 * This object doesn't listen to the Tams communications. This is because it
 * should be the only object that is sending messages for this turnout; more
 * than one Turnout object pointing to a single device is not allowed.
 * Reworked to support binary messages
 *
 * Based on work by Bob Jacobsen & Kevin Dickerson
 *
 * @author	Jan Boen
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public class TamsTurnout extends AbstractTurnout
        implements TamsListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    String prefix;

    //Create a local TamsMessage Queue which we will use in combination with TamsReplies
    private Queue<TamsMessage> tmq = new LinkedList<TamsMessage>();
        
    //This dummy message is used in case we expect a reply from polling
    static private TamsMessage myDummy() {
        //log.info("*** myDummy ***");
        TamsMessage m = new TamsMessage();
        m.setBinary(false);
        m.setReplyType('T');
        return m;
    }
    //A local TamsMessage is held at all time
    //When no TamsMessage is being generated via the UI this dummy is used which means the TamsReply is a result of polling
    TamsMessage tm = myDummy();

    /**
     * Tams turnouts use the NMRA number (0-2040) as their numerical
     * identification in the system name.
     *
     * @param number DCC address of the turnout
     */
    public TamsTurnout(int number, String prefix, TamsTrafficController etc) {
        super(prefix + "T" + number);
        _number = number;
        this.prefix = prefix;
        tc = etc;
        //Request status of turnout
        tm = new TamsMessage("xT " + _number + ",,1");
        tm.setBinary(false);
        tm.setReplyType('T');
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);
        //tc.addPollMessage(m, this);

        _validFeedbackTypes |= MONITORING;
        _activeFeedbackType = MONITORING;

        // if needed, create the list of feedback mode
        // names with additional LocoNet-specific modes
        if (modeNames == null) {
            initFeedbackModes();
        }
        _validFeedbackNames = modeNames;
        _validFeedbackModes = modeValues;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Only used during creation of 1st turnout")
    private void initFeedbackModes() {
        if (_validFeedbackNames.length != _validFeedbackModes.length) {
            log.error("int and string feedback arrays different length");
        }
        String[] tempModeNames = new String[_validFeedbackNames.length + 1];
        int[] tempModeValues = new int[_validFeedbackNames.length + 1];
        for (int i = 0; i < _validFeedbackNames.length; i++) {
            tempModeNames[i] = _validFeedbackNames[i];
            tempModeValues[i] = _validFeedbackModes[i];
        }
        tempModeNames[_validFeedbackNames.length] = "MONITORING";
        tempModeValues[_validFeedbackNames.length] = MONITORING;

        modeNames = tempModeNames;
        modeValues = tempModeValues;
    }

    static String[] modeNames = null;
    static int[] modeValues = null;

    TamsTrafficController tc;

    // Handle a request to change state by sending a turnout command
    protected void forwardCommandChangeToLayout(int s) {
        // implementing classes will typically have a function/listener to get
        // updates from the layout, which will then call
        //		public void firePropertyChange(String propertyName,
        //										Object oldValue,
        //										Object newValue)
        // _once_ if anything has changed state (or set the commanded state directly)

        // sort out states
        log.info("Change state from JMRI - 1");
        if ((s & Turnout.CLOSED) != 0) {
            // first look for the double case, which we can't handle
            if ((s & Turnout.THROWN) != 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN " + s);
                return;
            } else {
                // send a CLOSED command
                sendMessage(true ^ getInverted());
            }
        } else {
            // send a THROWN command
            sendMessage(false ^ getInverted());
        }
    }

    // data members
    int _number;   // turnout number

    /**
     * Set the turnout known state to reflect what's been observed from the
     * command station messages. A change there means that somebody commanded a
     * state change (e.g. somebody holding a throttle), and that command has
     * already taken effect. Hence we use "newCommandedState" to indicate it's
     * taken place. Must be followed by "newKnownState" to complete the turnout
     * action.
     *
     * @param state Observed state, updated state from command station
     */
    synchronized void setCommandedStateFromCS(int state) {
        log.info("Processing turnout state as received from TamsTurnoutManager, state= " + state);
        if ((getFeedbackMode() != MONITORING)) {
            return;
        }

        newCommandedState(state);
    }

    /**
     * Set the turnout known state to reflect what's been observed from the
     * command station messages. A change there means that somebody commanded a
     * state change (e.g. somebody holding a throttle), and that command has
     * already taken effect. Hence we use "newKnownState" to indicate it's taken
     * place.
     * <P>
     * @param state Observed state, updated state from command station
     */
    /*synchronized void setKnownStateFromCS(int state) {
        log.info("Processing turnout state as received from TamsTurnoutManager, state= " + state);
        if ((getFeedbackMode() != MONITORING)) {
            return;
        }

        newKnownState(state);
    }*/

    /**
     * Public access to changing turnout state. Sets the commanded state and, if
     * appropriate starts a TurnoutOperator to do its thing. If there is no
     * TurnoutOperator (not required or nothing suitable) then just tell the
     * layout and hope for the best.
     */
    /*public synchronized void setCommandedState(int s) {
        log.info("set commanded state for turnout " + getSystemName() + " to " + s);
        //newCommandedState(s);
        newKnownState(s);
    }*/
    
    /**
     * Tams turnouts can be inverted
     */
    @Override
    public boolean canInvert() {
        return true;
    }

    /**
     * Tell the layout to go to new state.
     *
     * @param closed State of the turnout to be sent to the command station
     */
    protected void sendMessage(boolean closed) {
        if (getInverted()) {
            closed = !closed;
        }
        // get control
        // added trailing ,1 on observation from Jan Boen 24 Aug 2015
        tm = new TamsMessage("xT " + _number + "," + (closed ? "1" : "0") + ",1");
        tm.setBinary(false);
        tm.setReplyType('T');
        tc.sendTamsMessage(tm, this);
        tmq.add(tm);

    }

    // to listen for status changes from Tams system
    public void reply(TamsReply tr) {
       log.info("*** TamsTurnout process TamsReply ***");
       if(tmq.isEmpty()){
           tm = myDummy();
       } else
       {
           tm = tmq.poll();
       }
       String msg = tr.toString();
       log.info("reply = " +  msg );
        if (tr.match("T") >= 0) {
            String[] lines = msg.split(" ");
            if (lines[1].equals("" + _number)) {
                updateReceived = true;
                if (lines[2].equals("g") || lines[2].equals("1")) {
                    setCommandedStateFromCS(Turnout.CLOSED);
                } else {
                    setCommandedStateFromCS(Turnout.THROWN);

                }
            }
        }
    }

    boolean updateReceived = false;

    protected void pollForStatus() {
        if (_activeFeedbackType == MONITORING) {
            //if we received an update last time we send a request again, but if we did not we shall skip it once and try again next time.
            if (updateReceived) {
                updateReceived = false;
                tm = new TamsMessage("xT " + _number + ",,1");
                tm.setBinary(false);
                tm.setReplyType('T');
                tm.setTimeout(TamsMessage.POLLTIMEOUT);
                tc.sendTamsMessage(tm, this);
                tmq.add(tm);
            } else {
                updateReceived = true;
            }
        }
    }

    @Override
    public void setFeedbackMode(int mode) throws IllegalArgumentException {
        tm = new TamsMessage("xT " + _number + ",,1");
        if (mode == MONITORING) {
            tc.addPollMessage(tm, this);
        } else {
            tc.removePollMessage(tm, this);
        }
        super.setFeedbackMode(mode);
    }

    public void message(TamsMessage m) {
        // messages are ignored
    }

    public void dispose() {
        tm = new TamsMessage("xT " + _number + ",,1");
        tc.removePollMessage(tm, this);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(TamsTurnout.class.getName());

    @Override
    protected void turnoutPushbuttonLockout(boolean locked) {
        // TODO Auto-generated method stub
        
    }
}
