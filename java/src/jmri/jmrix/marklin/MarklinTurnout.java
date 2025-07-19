package jmri.jmrix.marklin;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;

/**
 * Implement a Turnout via Marklin communications.
 * <p>
 * This object doesn't listen to the Marklin communications. This is because it
 * should be the only object that is sending messages for this turnout; more
 * than one Turnout object pointing to a single device is not allowed.
 * <p>
 * Based on work by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2012
 *
 */
public class MarklinTurnout extends AbstractTurnout implements MarklinListener {

    /**
     * Marklin turnouts use the NMRA number (0-2040) as their numerical
     * identification in the system name.
     *
     * @param number address of the turnout
     * @param prefix system prefix
     * @param etc connection traffic controller
     */
    public MarklinTurnout(int number, String prefix, MarklinTrafficController etc) {
        super(prefix + "T" + number);
        _number = number;
        tc = etc;
        tc.addMarklinListener(MarklinTurnout.this);
    }

    private final MarklinTrafficController tc;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void forwardCommandChangeToLayout(int newState) {
        // implementing classes will typically have a function/listener to get
        // updates from the layout, which will then call
        //  public void firePropertyChange(String propertyName,
        //          Object oldValue,
        //          Object newValue)
        // _once_ if anything has changed state (or set the commanded state directly)

        // sort out states
        if ((newState & Turnout.CLOSED) != 0) {
            // first look for the double case, which we can't handle
            if ((newState & Turnout.THROWN) != 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN {}", newState);
            } else {
                // send a CLOSED command
                sendMessage(!getInverted());
            }
        } else {
            // send a THROWN command
            sendMessage(getInverted());
        }
    }

    // data members
    private final int _number;   // turnout number

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
        if ((getFeedbackMode() != DIRECT)) {
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
        newCommandedState(state);
        if (getFeedbackMode() == DIRECT) {
            newKnownState(state);
        }
    }

    @Override
    public void turnoutPushbuttonLockout(boolean b) {
    }

    /**
     * Marklin turnouts can be inverted
     */
    @Override
    public boolean canInvert() {
        return true;
    }

    static final int PROTOCOL_UNKNOWN = MarklinConstants.PROTOCOL_UNKNOWN;
    static final int DCC = MarklinConstants.PROTOCOL_DCC;
    static final int MM2 = MarklinConstants.PROTOCOL_MM2;
    static final int SFX = MarklinConstants.PROTOCOL_SX;

    private int protocol = PROTOCOL_UNKNOWN;

    /**
     * Tell the layout to go to new state.
     *
     * @param newstate State of the turnout to be sent to the command station
     */
    protected void sendMessage(final boolean newstate) {
        MarklinMessage m = MarklinMessage.getSetTurnout(getCANAddress(), (newstate ? 1 : 0), 0x01);
        tc.sendMarklinMessage(m, this);

        jmri.util.TimerUtil.schedule(new java.util.TimerTask() {
            private final boolean state = newstate;

            @Override
            public void run() {
                try {
                    sendOffMessage((state ? 1 : 0));
                } catch (Exception e) {
                    log.error("Exception occurred while sending delayed off to turnout", e);
                }
            }
        }, METERINTERVAL);
    }

    int getCANAddress() {
        switch (protocol) {
            case DCC:
                return _number + MarklinConstants.DCCACCSTART - 1;
            default:
                return _number + MarklinConstants.MM1ACCSTART - 1;
        }
    }

    // to listen for status changes from Marklin system
    @Override
    public void reply(MarklinReply m) {
        if (m.getPriority() == MarklinConstants.PRIO_1 && m.getCommand() >= MarklinConstants.ACCCOMMANDSTART
            && m.getCommand() <= MarklinConstants.ACCCOMMANDEND) {
            if (protocol == PROTOCOL_UNKNOWN) {
                if (m.getAddress() == _number + MarklinConstants.MM1ACCSTART - 1) {
                    protocol = MM2;
                } else if (m.getAddress() == _number + MarklinConstants.DCCACCSTART - 1) {
                    protocol = DCC;
                } else {
                    //Message is not for us.
                    return;
                }
            }
            if (m.getAddress() == getCANAddress()) {
                switch (m.getElement(9)) {
                    case 0x00:
                        setKnownStateFromCS(Turnout.THROWN);
                        break;
                    case 0x01:
                        setKnownStateFromCS(Turnout.CLOSED);
                        break;
                    default:
                        log.warn("Unknown state command {}", m.getElement(9));
                }
            }
        }
    }

    @Override
    public void message(MarklinMessage m) {
        // messages are ignored
    }

    protected void sendOffMessage(int state) {
        MarklinMessage m = MarklinMessage.getSetTurnout(getCANAddress(), state, 0x00);
        tc.sendMarklinMessage(m, this);
    }

    static final int METERINTERVAL = 100;  // msec wait before closed

    @Override
    public void dispose() {
        tc.removeMarklinListener(this);
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MarklinTurnout.class);

}
