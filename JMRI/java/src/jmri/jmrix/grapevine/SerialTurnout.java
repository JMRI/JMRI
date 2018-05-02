package jmri.jmrix.grapevine;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement Turnout for Grapevine.
 * <p>
 * This object doesn't listen to the Grapevine serial communications. This is
 * because it should be the only object that is sending messages for this
 * turnout; more than one Turnout object pointing to a single device is not
 * allowed.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 */
public class SerialTurnout extends AbstractTurnout {

    GrapevineSystemConnectionMemo memo = null;

    /**
     * Create a Turnout object, with both system and user names.
     *
     * @param systemName system name including prefix, previously validated in SerialTurnoutManager
     * @param userName free form name
     * @param _memo the associated SystemConnectionMemo
     */
    public SerialTurnout(String systemName, String userName, GrapevineSystemConnectionMemo _memo) {
        super(systemName, userName);
        memo = _memo;
        // Save systemName
        tSystemName = systemName;
        // Extract the Bit from the name
        int num = SerialAddress.getBitFromSystemName(systemName, memo.getSystemPrefix()); // bit one is address zero
        log.debug("SerialTurnout {} created, num: {} prefix: {}", systemName, num, memo.getSystemPrefix());
        // num is 101-124, 201-224, 301-324, 401-424
        output = (num % 100) - 1; // 0 - 23
        bank = (num / 100) - 1;  // 0 - 3
    }

    /**
     * Grapevine turnouts can invert their outputs.
     */
    @Override
    public boolean canInvert() {
        return true;
    }

    /**
     * Handle a request to change state by sending a turnout command.
     */
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        // implementing classes will typically have a function/listener to get
        // updates from the layout, which will then call
        //  public void firePropertyChange(String propertyName,
        //      Object oldValue,
        //      Object newValue)
        // _once_ if anything has changed state (or set the commanded state directly)

        // sort out states
        if ((s & Turnout.CLOSED) != 0) {
            // first look for the double case, which we can't handle
            if ((s & Turnout.THROWN) != 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN {}", s);
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

    @Override
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout) {
        log.debug("Send command to {} Pushbutton", (_pushButtonLockout ? "Lock" : "Unlock"));
    }

    // data members
    String tSystemName; // System Name of this turnout
    int output;         // output connector number, 0-23
    int bank;           // bank number, 0-3

    protected void sendMessage(boolean closed) {
        SerialNode tNode = SerialAddress.getNodeFromSystemName(tSystemName, memo.getTrafficController());
        if (tNode == null) {
            // node does not exist, ignore call
            log.error("Can't find node for {}, command ignored", tSystemName);
            return;
        }
        boolean high = (output >= 12);
        int tOut = output;
        if (high) {
            tOut = output - 12;
        }
        if ((bank < 0) || (bank > 4)) {
            log.error("invalid bank {}  for Turnout {}", bank, getSystemName());
            bank = 0;
        }

        SerialMessage m = new SerialMessage(high ? 8 : 4);
        int i = 0;
        if (high) {
            m.setElement(i++, tNode.getNodeAddress() | 0x80);  // address 1
            m.setElement(i++, 122);   // shift command
            m.setElement(i++, tNode.getNodeAddress() | 0x80);  // address 2
            m.setElement(i++, 0x10);  // bank 1
            m.setParity(i - 4);
        }
        m.setElement(i++, tNode.getNodeAddress() | 0x80);  // address 1
        m.setElement(i++, (tOut << 3) | (closed ? 0 : 6));  // closed is green, thrown is red
        m.setElement(i++, tNode.getNodeAddress() | 0x80);  // address 2
        m.setElement(i++, bank << 4); // bank is most significant bits
        m.setParity(i - 4);
        memo.getTrafficController().sendSerialMessage(m, null);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnout.class);

}
