// TamsTurnout.java
package jmri.jmrix.tams;

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
 *
 * Based on work by Bob Jacobsen and Kevin Dickerson Copyright
 *
 * @author	Jan Boen
 * @version	$Revision: 20160622 $
 */
public class TamsTurnout extends AbstractTurnout
        implements TamsListener {

    /**
     *
     */
    private static final long serialVersionUID = 1921305278634163107L;
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
        TamsMessage m = new TamsMessage("xT " + _number + ",,1");
        m.setBinary(false);
        m.setReplyType('T');
        tc.sendTamsMessage(m, this);
        //tc.addPollMessage(m, this);//Not adding a poll message as status updates will come via TamsTurnoutManager

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
        //log.info("*** initFeedbackModes ***");
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

    // to hear of changes - copied from PowerManager
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    // Handle a request to change state by sending a turnout command
    protected void forwardCommandChangeToLayout(int s) {
        //log.info("*** forwardCommandChangeToLayout ***");
        // implementing classes will typically have a function/listener to get
        // updates from the layout, which will then call
        //		public void firePropertyChange(String propertyName,
        //										Object oldValue,
        //										Object newValue)
        // _once_ if anything has changed state (or set the commanded state directly)

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
     * state change (e.g. somebody holding a throttle), and that command has
     * already taken effect. Hence we use "newCommandedState" to indicate it's
     * taken place. Must be followed by "newKnownState" to complete the turnout
     * action.
     *
     * @param state Observed state, updated state from command station
     */
    synchronized void setCommandedStateFromCS(int state) {
        log.info("*** setCommandedStateFromCS ***");
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
    synchronized void setKnownStateFromCS(int state) {
        log.info("*** setKnownStateFromCS ***");
        if ((getFeedbackMode() != MONITORING)) {
            return;
        }

        newKnownState(state);
    }

    public void turnoutPushbuttonLockout(boolean b) {
    }

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
        //log.info("*** sendMessage ***");
        if (getInverted()) {
            closed = !closed;
        }
        // get control
        TamsMessage m = new TamsMessage("xT " + _number + "," + (closed ? "r" : "g"));
        tc.sendTamsMessage(m, this);

    }

    // to listen for status changes from Tams system
    public void reply(TamsReply m) {
        //log.info("*** TamsReply ***");
        log.info(Integer.toString(m.match("T")));
        log.info(Integer.toString(m.match("ERROR")));
        String msg = m.toString();
        if (m.match("T") == 0) {
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
            //log.info("*** pollForStatus ***");
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
    }

    @Override
    public void setFeedbackMode(int mode) throws IllegalArgumentException {
        //log.info("*** setFeedbackMode ***");
        TamsMessage m = new TamsMessage("xT " + _number + ",,1");
        if (mode == MONITORING) {
            tc.addPollMessage(m, this);
        } else {
            tc.removePollMessage(m, this);
        }
        super.setFeedbackMode(mode);
    }

    public void message(TamsMessage m) {
        //log.info("*** message ***");
        // messages are ignored
    }

    public void dispose() {
        //log.info("*** dispose ***");
        TamsMessage m = new TamsMessage("xT " + _number + ",,1");
        tc.removePollMessage(m, this);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(TamsTurnout.class.getName());
}

/* @(#)TamsTurnout.java */
