package jmri.jmrix.marklin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;

/**
 * Implement a Turnout via Marklin communications.
 * <p>
 * This object doesn't listen to the Marklin communications for protocol
 * auto-detection; protocol is determined at construction from the system name
 * prefix. More than one Turnout object pointing to a single device is not allowed.
 * <p>
 * The system name encodes protocol and address:
 * <ul>
 *   <li>{@code MTDCC45}  — DCC accessory address 45, wire = {@code DCCACCSTART + 44}</li>
 *   <li>{@code MTMM123}  — MM/MM2 accessory address 123, wire = {@code MM1ACCSTART + 122}</li>
 *   <li>{@code MTCAN200} — raw CAN wire address 200, used verbatim</li>
 *   <li>{@code MT45}     — legacy unprefixed, treated as DCC</li>
 * </ul>
 * <p>
 * Based on work by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2012
 * @author Ralf Lang
 */
public class MarklinTurnout extends AbstractTurnout implements MarklinListener {

    /** Matches the address portion after stripping system-prefix + 'T'. */
    private static final Pattern ADDRESS_PATTERN =
            Pattern.compile("^(MM|DCC|CAN)?(\\d+)$");

    enum Protocol { MM, DCC, CAN }

    private final MarklinTrafficController tc;
    private final int _number;
    private final Protocol protocol;

    /**
     * Create a MarklinTurnout from its full system name.
     *
     * @param systemName full system name, e.g. {@code MTDCC45} or {@code MT123}
     * @param prefix     system prefix, e.g. {@code M}
     * @param tc         traffic controller
     */
    public MarklinTurnout(String systemName, String prefix, MarklinTrafficController tc) {
        super(systemName);
        this.tc = tc;

        // Strip prefix + 'T' to get the address string, e.g. "DCC45" or "123"
        String addr = systemName.substring(prefix.length() + 1);
        Matcher m = ADDRESS_PATTERN.matcher(addr);
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot parse Marklin turnout address from '" + systemName + "'");
        }
        String protocolStr = m.group(1); // null if unprefixed
        _number = Integer.parseInt(m.group(2));
        if ("MM".equals(protocolStr)) {
            protocol = Protocol.MM;
        } else if ("CAN".equals(protocolStr)) {
            protocol = Protocol.CAN;
        } else {
            protocol = Protocol.DCC; // DCC explicit or unprefixed legacy
        }

        tc.addMarklinListener(this);
    }

    /** {@inheritDoc} */
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

    /**
     * Return the wire address for the accessory command.
     * <ul>
     *   <li>DCC: {@code _number + DCCACCSTART - 1}</li>
     *   <li>MM:  {@code _number + MM1ACCSTART - 1}</li>
     *   <li>CAN: {@code _number} verbatim (raw CAN address)</li>
     * </ul>
     */
    int getCANAddress() {
        switch (protocol) {
            case MM:  return _number + MarklinConstants.MM1ACCSTART - 1;
            case CAN: return _number;
            default:  return _number + MarklinConstants.DCCACCSTART - 1; // DCC
        }
    }

    protected void sendMessage(final boolean newstate) {
        MarklinMessage msg = MarklinMessage.getSetTurnout(getCANAddress(), (newstate ? 1 : 0), 0x01);
        tc.sendMarklinMessage(msg, this);
        jmri.util.TimerUtil.schedule(new java.util.TimerTask() {
            private final boolean state = newstate;
            @Override
            public void run() {
                try {
                    sendOffMessage(state ? 1 : 0);
                } catch (Exception e) {
                    log.error("Exception sending delayed off to turnout", e);
                }
            }
        }, METERINTERVAL);
    }

    protected void sendOffMessage(int state) {
        MarklinMessage msg = MarklinMessage.getSetTurnout(getCANAddress(), state, 0x00);
        tc.sendMarklinMessage(msg, this);
    }

    @Override
    public void reply(MarklinReply m) {
        if (m.getPriority() == MarklinConstants.PRIO_1
                && m.getCommand() >= MarklinConstants.ACCCOMMANDSTART
                && m.getCommand() <= MarklinConstants.ACCCOMMANDEND
                && m.getAddress() == getCANAddress()) {
            switch (m.getElement(9)) {
                case 0x00: setKnownStateFromCS(Turnout.THROWN);  break;
                case 0x01: setKnownStateFromCS(Turnout.CLOSED); break;
                default: log.warn("Unknown state command {}", m.getElement(9));
            }
        }
    }

    @Override
    public void message(MarklinMessage m) {
    }

    @Override
    public void turnoutPushbuttonLockout(boolean b) {
    }

    /**
     * Marklin turnouts can be inverted.
     */
    @Override
    public boolean canInvert() {
        return true;
    }

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
        if (getFeedbackMode() != DIRECT) return;
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
        if (getFeedbackMode() == DIRECT) newKnownState(state);
    }

    @Override
    public void dispose() {
        tc.removeMarklinListener(this);
        super.dispose();
    }

    static final int METERINTERVAL = 100; // ms between on and off pulse

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MarklinTurnout.class);
}
