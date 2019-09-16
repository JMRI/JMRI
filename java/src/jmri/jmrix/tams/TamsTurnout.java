package jmri.jmrix.tams;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement a Turnout via Tams communications.
 * <p>
 * This object doesn't listen to the Tams communications. This is because it
 * should be the only object that is sending messages for this turnout; more
 * than one Turnout object pointing to a single device is not allowed.
 *
 * Based on work by Bob Jacobsen and Kevin Dickerson Copyright
 *
 * @author	 Jan Boen
 */
public class TamsTurnout extends AbstractTurnout
        implements TamsListener {

    String prefix;

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
        TamsMessage m = new TamsMessage("xT " + _number + ",,0");
        m.setBinary(false);
        m.setReplyType('T');
        tc.sendTamsMessage(m, this);

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

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification = "Only used during creation of 1st turnout")
    private void initFeedbackModes() {
        log.debug("*** initFeedbackModes ***");
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
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        log.debug("*** forwardCommandChangeToLayout ***");
        // sort out states
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
     * state change (by using a throttle), and that command has
     * already taken effect. Hence we use "newCommandedState" to indicate it's
     * taken place. Must be followed by "newKnownState" to complete the turnout
     * action.
     *
     * @param state Observed state, updated state from command station
     */
    synchronized void setCommandedStateFromCS(int state) {
        log.debug("*** setCommandedStateFromCS ***");
        if ((getFeedbackMode() != MONITORING)) {
            log.debug("Returning");
            return;
        }
        log.debug("Setting to state " + state);
        newCommandedState(state);
    }

    /**
     * Set the turnout known state to reflect what's been observed from the
     * command station messages. A change there means that somebody commanded a
     * state change (by using a throttle), and that command has
     * already taken effect. Hence we use "newKnownState" to indicate it's taken
     * place.
     *
     * @param state Observed state, updated state from command station
     */
    synchronized void setKnownStateFromCS(int state) {
        log.debug("*** setKnownStateFromCS ***");
        if ((getFeedbackMode() != MONITORING)) {
            return;
        }
        newKnownState(state);
    }

    @Override
    public void turnoutPushbuttonLockout(boolean b) {
    }

    /**
     * Tams turnouts can be inverted.
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
        log.debug("*** sendMessage ***");
        // get control
        TamsMessage m = new TamsMessage("xT " + _number + "," + (closed ? "r" : "g") + ",1");
        tc.sendTamsMessage(m, this);
    }

    // Listen for status changes from Tams system.
    @Override
    public void reply(TamsReply m) {
        log.debug("*** TamsReply ***");
        log.debug("m.match(\"T\") = " + Integer.toString(m.match("T")));
        String msg = m.toString();
        log.debug("Turnout Reply = " + msg);
        if (m.match("T") == 0) {
            String[] lines = msg.split(" ");
            if (lines[1].equals("" + _number)) {
                updateReceived = true;
                if (lines[2].equals("r") || lines[2].equals("0")) {
                    log.debug("Turnout " + _number + " = CLOSED");
                    setCommandedStateFromCS(Turnout.CLOSED);
                    setKnownStateFromCS(Turnout.CLOSED);
                } else {
                    log.debug("Turnout " + _number + " = THROWN");
                    setCommandedStateFromCS(Turnout.THROWN);
                    setKnownStateFromCS(Turnout.THROWN);
                }
            }
        }
    }

    boolean updateReceived = false;

    /*protected void pollForStatus() {
        if (_activeFeedbackType == MONITORING) {
            log.debug("*** pollForStatus ***");
            //if we received an update last time we send a request again, but if we did not we shall skip it once and try again next time.
            if (updateReceived) {
                updateReceived = false;
                TamsMessage m = new TamsMessage("xT " + _number + ",,1");
                m.setTimeout(TamsMessage.POLLTIMEOUT);
                tc.sendTamsMessage(m, this);
            } else {
                updateReceived = true;
            }
        }
    }*/

    @Override
    public void setFeedbackMode(int mode) throws IllegalArgumentException {
        log.debug("*** setFeedbackMode ***");
        TamsMessage m = new TamsMessage("xT " + _number + ",,1");
        if (mode == MONITORING) {
            tc.sendTamsMessage(m, this);//Only send a message once
            //The rest gets done via polling from TamsTurnoutManager
        }
        super.setFeedbackMode(mode);
    }

    @Override
    public void message(TamsMessage m) {
        log.debug("*** message ***");
        // messages are ignored
    }

    @Override
    public void dispose() {
        log.debug("*** dispose ***");
        TamsMessage m = new TamsMessage("xT " + _number + ",,1");
        tc.removePollMessage(m, this);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(TamsTurnout.class);

}
