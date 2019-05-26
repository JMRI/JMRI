package jmri.jmrix.nce;

import jmri.NmraPacket;
import jmri.PushbuttonPacket;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement a Turnout via NCE communications.
 * <p>
 * This object doesn't listen to the NCE communications. This is because it
 * should be the only object that is sending messages for this turnout; more
 * than one Turnout object pointing to a single device is not allowed.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau (C) 2007
 */
public class NceTurnout extends AbstractTurnout {

    NceTrafficController tc = null;
    String prefix = "";

    /**
     * NCE turnouts use the NMRA number (0-2044) as their numerical
     * identification.
     * @param tc traffic controller for connection
     * @param p system connection prefix
     * @param i NMRA turnout number
     */
    public NceTurnout(NceTrafficController tc, String p, int i) {
        super(p + "T" + i);
        this.tc = tc;
        this.prefix = p + "T";
        _number = i;
        if (_number < NmraPacket.accIdLowLimit || _number > NmraPacket.accIdHighLimit) {
            throw new IllegalArgumentException("Turnout value: " + _number 
                    + " not in the range " + NmraPacket.accIdLowLimit + " to " 
                    + NmraPacket.accIdHighLimit);
        }
        // At construction, register for messages
        initialize();
    }

    private synchronized void initialize() {
        numNtTurnouts++; // increment the total number of NCE turnouts
        // update feedback modes, MONITORING requires PowerPro system with new EPROM    
        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006 && tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_NONE) {
            if (modeNames == null) {
                if (_validFeedbackNames.length != _validFeedbackModes.length) {
                    log.error("int and string feedback arrays different length");
                }
                modeNames = new String[_validFeedbackNames.length + 1];
                modeValues = new int[_validFeedbackNames.length + 1];
                for (int i = 0; i < _validFeedbackNames.length; i++) {
                    modeNames[i] = _validFeedbackNames[i];
                    modeValues[i] = _validFeedbackModes[i];
                }
                modeNames[_validFeedbackNames.length] = "MONITORING";
                modeValues[_validFeedbackNames.length] = MONITORING;
            }
            _validFeedbackTypes |= MONITORING;
            _validFeedbackNames = modeNames;
            _validFeedbackModes = modeValues;
        }
        _enableCabLockout = true;
        _enablePushButtonLockout = true;
    }
    static String[] modeNames = null;
    static int[] modeValues = null;
    private static int numNtTurnouts = 0;

    public int getNumber() {
        return _number;
    }

    public static int getNumNtTurnouts() {
        return numNtTurnouts;
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

    /**
     * Send a message to the layout to lock or unlock the turnout pushbuttons if
     * true, pushbutton lockout enabled
     */
    @Override
    protected void turnoutPushbuttonLockout(boolean pushButtonLockout) {
        if (log.isDebugEnabled()) {
            log.debug("Send command to "
                    + (pushButtonLockout ? "Lock" : "Unlock")
                    + " Pushbutton " + prefix + _number);
        }

        byte[] bl = PushbuttonPacket.pushbuttonPkt(prefix, _number, pushButtonLockout);
        NceMessage m = NceMessage.sendPacketMessage(tc, bl);
        tc.sendNceMessage(m, null);
    }

    // data members
    int _number;   // turnout number

    /**
     * Set the turnout known state to reflect what's been observed from the
     * command station polling. A change there means that somebody commanded a
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
     * command station polling. A change there means that somebody commanded a
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

    /**
     * NCE turnouts can be inverted
     */
    @Override
    public boolean canInvert() {
        return true;
    }
    /**
     * NCE turnouts can provide both modes when properly configured
     *
     * @return Both cab and pushbutton (decoder) modes
     */
    @Override
    public int getPossibleLockModes() { return CABLOCKOUT | PUSHBUTTONLOCKOUT ; }

    /**
     * NCE turnouts support two types of lockouts, pushbutton and cab. Cab
     * lockout requires the feedback mode to be Monitoring
     */
    @Override
    public boolean canLock(int turnoutLockout) {
        // can not lock if using a USB
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
            return false;
        }
        // check to see if push button lock is enabled and valid decoder
        String dn = getDecoderName();
        if ((turnoutLockout & PUSHBUTTONLOCKOUT) != 0 && _enablePushButtonLockout
                && dn != null && !dn.equals(PushbuttonPacket.unknown)) {
            return true;
        }
        // check to see if cab lockout is enabled
        if ((turnoutLockout & CABLOCKOUT) != 0
                && getFeedbackMode() == MONITORING && _enableCabLockout) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Control which turnout locks are enabled
     */
    @Override
    public void enableLockOperation(int turnoutLockout, boolean enabled) {
        if ((turnoutLockout & CABLOCKOUT) != 0) {
            if (enabled) {
                _enableCabLockout = true;
            } else {
                // unlock cab before disabling
                _cabLockout = false;
                _enableCabLockout = false;
                // pushbutton lockout has to be enabled if cab lockout is disabled
                _enablePushButtonLockout = true;
            }
        }
        if ((turnoutLockout & PUSHBUTTONLOCKOUT) != 0) {
            if (enabled) {
                _enablePushButtonLockout = true;
            } else {
                // only time we can disable pushbuttons is if we can lockout cabs
                if (getFeedbackMode() != MONITORING) {
                    return;
                }
                // pushbutton lockout has to be enabled if cab lockout is disabled
                if (_enableCabLockout) {
                    _enablePushButtonLockout = false;
                }
            }
        }
    }

    protected void sendMessage(boolean closed) {
        // get the packet
        // dBoudreau  Added support for new accessory binary command

        if (tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {

            byte[] bl = NceBinaryCommand.accDecoder(_number, closed);

            if (log.isDebugEnabled()) {
                log.debug("Command: "
                        + Integer.toHexString(0xFF & bl[0])
                        + " " + Integer.toHexString(0xFF & bl[1])
                        + " " + Integer.toHexString(0xFF & bl[2])
                        + " " + Integer.toHexString(0xFF & bl[3])
                        + " " + Integer.toHexString(0xFF & bl[4]));
            }

            NceMessage m = NceMessage.createBinaryMessage(tc, bl);

            tc.sendNceMessage(m, null);

        } else {

            byte[] bl = NmraPacket.accDecoderPkt(_number, closed);

            if (log.isDebugEnabled()) {
                log.debug("packet: "
                        + Integer.toHexString(0xFF & bl[0])
                        + " " + Integer.toHexString(0xFF & bl[1])
                        + " " + Integer.toHexString(0xFF & bl[2]));
            }

            NceMessage m = NceMessage.sendPacketMessage(tc, bl);

            tc.sendNceMessage(m, null);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NceTurnout.class);
}
