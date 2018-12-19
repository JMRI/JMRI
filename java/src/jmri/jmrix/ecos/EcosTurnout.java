package jmri.jmrix.ecos;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement a Turnout via ECoS communications.
 * <p>
 * This object doesn't listen to the Ecos communications. This is because it
 * should be the only object that is sending messages for this turnout; more
 * than one Turnout object pointing to a single device is not allowed.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau (C) 2007
 */
public class EcosTurnout extends AbstractTurnout
        implements EcosListener {

    String prefix;

    int objectNumber = 0;
    boolean masterObjectNumber = true;
    String slaveAddress;
    int extended = 0;

    /**
     * ECoS turnouts use the NMRA number (0-2044) as their numerical
     * identification in the system name.
     *
     * @param number DCC address of the turnout
     */
    public EcosTurnout(int number, String prefix, EcosTrafficController etc, EcosTurnoutManager etm) {
        super(prefix + "T" + number);
        _number = number;
        /*if (_number < NmraPacket.accIdLowLimit || _number > NmraPacket.accIdHighLimit) {
            throw new IllegalArgumentException("Turnout value: " + _number 
                    + " not in the range " + NmraPacket.accIdLowLimit + " to " 
                    + NmraPacket.accIdHighLimit);
        }*/
        this.prefix = prefix;
        tc = etc;
        tm = etm;
        /* All messages from the ECoS regarding turnout status updates
         are initally handled by the TurnoutManager, this then forwards the message
         on to the correct Turnout */
        
        // update feedback modes
        _validFeedbackTypes |= MONITORING | EXACT | INDIRECT;
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
        if (_validFeedbackNames.length != _validFeedbackModes.length) {
            log.error("int and string feedback arrays different length");
        }
        String[] tempModeNames = new String[_validFeedbackNames.length + 3];
        int[] tempModeValues = new int[_validFeedbackNames.length + 3];
        for (int i = 0; i < _validFeedbackNames.length; i++) {
            tempModeNames[i] = _validFeedbackNames[i];
            tempModeValues[i] = _validFeedbackModes[i];
        }
        tempModeNames[_validFeedbackNames.length] = "MONITORING";
        tempModeValues[_validFeedbackNames.length] = MONITORING;
        tempModeNames[_validFeedbackNames.length + 1] = "INDIRECT";
        tempModeValues[_validFeedbackNames.length + 1] = INDIRECT;
        tempModeNames[_validFeedbackNames.length + 2] = "EXACT";
        tempModeValues[_validFeedbackNames.length + 2] = EXACT;

        modeNames = tempModeNames;
        modeValues = tempModeValues;
    }

    static String[] modeNames = null;
    static int[] modeValues = null;    
    
    EcosTrafficController tc;
    EcosTurnoutManager tm;
    /*Extended is used to indicate that this Ecos accessory has a secondary address assigned to it.
     the value determines the symbol/icon used on the ecos.
     2 - Three Way Point
     4 - Double Slip*/
    public static final int THREEWAY = 2;
    public static final int DOUBLESLIP = 4;

    void setExtended(int e) {
        extended = e;
    }

    void setObjectNumber(int o) {
        objectNumber = o;
    }

    void setSlaveAddress(int o) {
        slaveAddress = prefix + "T" + o;
    }

    void setMasterObjectNumber(boolean o) {
        masterObjectNumber = o;
    }

    public int getNumber() {
        return _number;
    }

    public int getObject() {
        return objectNumber;
    }

    public int getExtended() {
        return extended;
    }

    public String getSlaveAddress() {
        return slaveAddress;
    }

    // Handle a request to change state by sending a turnout command
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        // implementing classes will typically have a function/listener to get
        // updates from the layout, which will then call
        //  public void firePropertyChange(String propertyName,
        //          Object oldValue,
        //          Object newValue)
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
     * state change (by using a throttle), and that command has
     * already taken effect. Hence we use "newCommandedState" to indicate it's
     * taken place. Must be followed by "newKnownState" to complete the turnout
     * action.
     *
     * @param state Observed state, updated state from command station
     */
    synchronized void setCommandedStateFromCS(int state) {
        if ((getFeedbackMode() != MONITORING)) {
            return;
        }

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
        if ((getFeedbackMode() != MONITORING)) {
            return;
        }

        newKnownState(state);
    }

    @Override
    public void turnoutPushbuttonLockout(boolean b) {
    }

    /**
     * @return ECoS turnouts can be inverted
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
        newKnownState(Turnout.UNKNOWN);
        if (getInverted()) {
            closed = !closed;
        }
        if ((masterObjectNumber) && (extended == 0)) {
            EcosMessage m;
            // get control
            m = new EcosMessage("request(" + objectNumber + ", control)");
            tc.sendEcosMessage(m, null);
            // set state
            m = new EcosMessage("set(" + objectNumber + ", state[" + (closed ? "0" : "1") + "])");
            tc.sendEcosMessage(m, null);
            // release control
            m = new EcosMessage("release(" + objectNumber + ", control)");
            tc.sendEcosMessage(m, null);
        } else { //we have a 3 way or double slip!
            //Working upon the basis that if the materObjectNumber is false than this is the second
            //decoder address assigned, while if it is true then we are the first decoder address.
            boolean firststate;
            boolean secondstate;
            if (!masterObjectNumber) {
                //Here we are dealing with the second address
                int turnaddr = _number - 1;
                Turnout t = tm.getTurnout(prefix + "T" + turnaddr);
                secondstate = closed;
                if (t.getKnownState() == CLOSED) {
                    firststate = true;
                } else {
                    firststate = false;
                }

            } else {
                Turnout t = tm.getTurnout(slaveAddress);
                firststate = closed;

                if (t.getKnownState() == CLOSED) {
                    secondstate = true;
                } else {
                    secondstate = false;
                }
            }
            int setState = 0;
            if (extended == THREEWAY) {
                if ((firststate) && (secondstate)) {
                    setState = 0;
                } else if ((firststate) && (!secondstate)) {
                    setState = 1;
                } else {
                    setState = 2;
                }
            } else if (extended == DOUBLESLIP) {
                if ((firststate) && (secondstate)) {
                    setState = 0;
                } else if ((!firststate) && (!secondstate)) {
                    setState = 1;
                } else if ((!firststate) && (secondstate)) {
                    setState = 2;
                } else {
                    setState = 3;
                }
            }

            if (setState == 99) {
                // log.debug("Invalid selection old state " + getKnownState() + " " + getCommandedState());
                if (closed) {
                    setCommandedState(THROWN);
                } else {
                    setCommandedState(CLOSED);
                }
                // log.debug("After - " + getKnownState() + " " + getCommandedState() + " " + "Is consistant " + isConsistentState());
            } else {

                EcosMessage m = new EcosMessage("request(" + objectNumber + ", control)");
                tc.sendEcosMessage(m, this);
                // set state
                m = new EcosMessage("set(" + objectNumber + ", state[" + setState + "])");
                tc.sendEcosMessage(m, this);
                // release control
                m = new EcosMessage("release(" + objectNumber + ", control)");
                tc.sendEcosMessage(m, this);
            }
        }

    }

    // Listen for status changes from ECoS system.
    int newstate = UNKNOWN;
    int newstateext = UNKNOWN;

    @Override
    public void reply(EcosReply m) {

        String msg = m.toString();
        if (m.getResultCode() != 0) {
            return; //The result is not valid therefore we can not set it.
        }
        if (m.getEcosObjectId() != objectNumber) {
            return; //message is not for our turnout address
        }
        if (msg.contains("switching[0]")) {
            log.debug("Turnout switched - new state="+newstate);
            /*log.debug("see new state "+newstate+" for "+_number);*/
            //newCommandedState(newstate);
            /*Using newKnownState, as any changes made on the ecos do not show
              up on the panel. Therefore if an ecos route is fired the panel
              doesn't change to reflect it.*/
            if (extended == 0) {
                newKnownState(newstate);
            } else {
                //The masterObjectNumber is used to determine if this the master or slave decoder
                //address in an extended accessory object on the ecos.
                if (masterObjectNumber) {
                    newKnownState(newstate);
                } else {
                    newKnownState(newstateext);
                }
            }
        }
        if ((m.isUnsolicited()) || (m.getReplyType().equals("get")) || (m.getReplyType().equals("set"))) {
            //if (msg.startsWith("<REPLY get("+objectNumber+",") || msg.startsWith("<EVENT "+objectNumber+">")) {
            int start = msg.indexOf("state[");
            int end = msg.indexOf("]");
            if (start > 0 && end > 0) {
                String val = msg.substring(start + 6, end);
                // log.debug("Extended - " + extended + " " + objectNumber);
                if (extended == 0) {
                    if (val.equals("0")) {
                        newstate = CLOSED;
                    } else if (val.equals("1")) {
                        newstate = THROWN;
                    } else {
                        log.warn("val |" + val + "| from " + msg);
                    }
                    log.debug("newstate found: "+newstate);
                    if (m.getReplyType().equals("set")) {
                       // wait to set the state until ECOS tells us to (by an event with the contents "switching[0]")
                    } else {
                        newKnownState(newstate);
                    }
                } else {
                    if (extended == THREEWAY) { //Three way Point.
                        if (val.equals("0")) {
                            newstate = CLOSED;
                            newstateext = CLOSED;
                        } else if (val.equals("1")) {
                            newstate = CLOSED;
                            newstateext = THROWN;
                        } else if (val.equals("2")) {
                            newstate = THROWN;
                            newstateext = CLOSED;
                        }
                    } else if (extended == DOUBLESLIP) { //Double Slip
                        if (val.equals("0")) {
                            newstate = CLOSED;
                            newstateext = CLOSED;
                        } else if (val.equals("1")) {
                            newstate = THROWN;
                            newstateext = THROWN;
                        } else if (val.equals("2")) {
                            newstate = THROWN;
                            newstateext = CLOSED;
                        } else if (val.equals("3")) {
                            newstate = CLOSED;
                            newstateext = THROWN;
                        }
                    }
                    if (m.getReplyType().equals("set")) {
                       // wait to set the state until ECoS tells us to (by an event with the contents "switching[0]")
                    } else {
                        if (masterObjectNumber) {
                            newKnownState(newstate);
                        } else {
                            newKnownState(newstateext);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void message(EcosMessage m) {
        // messages are ignored
    }

    private final static Logger log = LoggerFactory.getLogger(EcosTurnout.class);

}
