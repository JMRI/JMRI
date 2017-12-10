package jmri.jmrix.grapevine;

import jmri.implementation.DefaultSignalHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SerialSignalHead.java
 *
 * This object doesn't listen to the Grapevine serial communications. It
 * probably should, however, in case
 *
 * Description: extend jmri.AbstractSignalHead for grapevine serial signals
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007
  */
public class SerialSignalHead extends DefaultSignalHead {

    GrapevineSystemConnectionMemo memo = null;

    /**
     * Create a SignalHead object, with only a system name.
     * <P>
     * 'systemName' should have been previously validated
     */
    public SerialSignalHead(String systemName,GrapevineSystemConnectionMemo _memo) {
        super(systemName);
        memo = _memo;
        // Save system Name
        tSystemName = systemName;
        // Extract the Bit from the name
        int num = SerialAddress.getBitFromSystemName(systemName); // bit one is address zero
        // num is 101-124, 201-224, 301-324, 401-424
        output = (num % 100) - 1; // 0-23
        bank = (num / 100) - 1;  // 0 - 3
    }

    /**
     * Create a SignalHead object, with both system and user names.
     * <P>
     * 'systemName' should have been previously validated
     */
    public SerialSignalHead(String systemName, String userName,GrapevineSystemConnectionMemo _memo) {
        super(systemName, userName);
        memo = _memo;
        // Save system Name
        tSystemName = systemName;
        // Extract the Bit from the name
        int num = SerialAddress.getBitFromSystemName(systemName); // bit one is address zero
        // num is 101-124, 201-224, 301-324, 401-424
        output = (num % 100) - 1; // 0-23
        bank = (num / 100) - 1;  // 0 - 3
    }

    /**
     * Handle a request to change state on layout
     */
    @Override
    protected void updateOutput() {
        SerialNode tNode = SerialAddress.getNodeFromSystemName(tSystemName,memo.getTrafficController());
        if (tNode == null) {
            // node does not exist, ignore call
            log.error("Can't find node for " + tSystemName + ", command ignored");
            return;
        }

        boolean high = (output >= 12);
        int tOut = output;
        if (high) {
            tOut = output - 12;
        }
        if ((bank < 0) || (bank > 4)) {
            log.error("invalid bank " + bank + " for signal " + getSystemName());
            bank = 0;
        }

        // sort out states
        int cmd;
        if (mLit) {
            switch (mAppearance) {
                case RED:
                    cmd = 6;
                    break;
                case FLASHRED:
                    cmd = 7;
                    break;
                case YELLOW:
                    cmd = 2;
                    break;
                case FLASHYELLOW:
                    cmd = 3;
                    break;
                case GREEN:
                    cmd = 0;
                    break;
                case FLASHGREEN:
                    cmd = 1;
                    break;
                case DARK:
                    cmd = 4;
                    break;
                default:
                    log.warn("Unexpected new appearance: " + mAppearance);
                    cmd = 7;
                    break;  // flash red for error
            }
        } else {
            cmd = 4; // set dark if not lit
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
        m.setElement(i++, (tOut << 3) | cmd);
        m.setElement(i++, tNode.getNodeAddress() | 0x80);  // address 2
        m.setElement(i++, bank << 4); // bank is most significant bits
        m.setParity(i - 4);
        memo.getTrafficController().sendSerialMessage(m, null);
    }

    // flashing is done on the cards, so we don't have to
    // do it manually
    @Override
    public void startFlash() {
    }

    @Override
    public void stopFlash() {
    }

    // data members
    String tSystemName; // System Name of this signal head
    int output;         // output connector number, 0-23
    int bank;           // bank number, 0-3

    private final static Logger log = LoggerFactory.getLogger(SerialSignalHead.class);
}


