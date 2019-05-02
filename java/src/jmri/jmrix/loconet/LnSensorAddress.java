package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for handling LocoNet sensor addresses.
 * <p>
 * There are three addressing spaces for LocoNet sensors:
 * <ul>
 *   <li>The space used for DS54 inputs, where the least-significant-bit in the
 *   address refers to the "Aux" and "Switch" inputs. These are represented by
 *   system names of the form LSnnnA and LSnnnS respectively. nnn is then the
 *   turnout number of the DS54 channel.
 *   <li>The space used for BDL16 inputs, where the card and section numbers are
 *   part of the address. These are represented by names of the form LScccA1
 *   through LScccA4, LScccB1 through LScccB4, and on through LScccD4. ccc is the
 *   BDL16 card number.
 *   <li>A straight-forward numeric space, represented by LSmmm. Note that this is
 *   a 1-4096 scheme, not a 0-4095.
 * </ul>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public class LnSensorAddress {

    int _low;
    int _high;
    int _as;
    String prefix;

    boolean _valid;

    public LnSensorAddress(int sw1, int sw2, String prefix) {
        _as = sw2 & 0x20;  // should be a LocoNet constant?
        _high = sw2 & 0x0F;
        _low = sw1 & 0x7F;
        _valid = true;
        this.prefix = prefix;
    }

    public LnSensorAddress(String s, String prefix) {
        _valid = false;
        this.prefix = prefix;

        // check valid
        if (s.startsWith(prefix + "S")) {
            // parse out and decode the name
            if (s.charAt(s.length() - 1) == 'A') {
                // DS54 addressing, Aux input
                _as = 0x20;
                int n = Integer.parseInt(s.substring(prefix.length() + 1, s.length() - 1));
                _high = n / 128;
                _low = n & 0x7F;
                _valid = true;
            } else if (s.charAt(s.length() - 1) == 'S') {
                // DS54 addressing, Switch input
                _as = 0x00;
                int n = Integer.parseInt(s.substring(prefix.length() + 1, s.length() - 1));
                _high = n / 128;
                _low = n & 0x7F;
                _valid = true;
            } else {
                // BDL16?
                char c = s.charAt(s.length() - 2);
                if (c >= 'A' && c <= 'D') {
                    // BDL16 addressing
                    int d = 0;
                    switch (c) {
                        case 'A':
                            d = 0;
                            break;
                        case 'B':
                            d = 1;
                            break;
                        case 'C':
                            d = 2;
                            break;
                        case 'D':
                            d = 3;
                            break;
                        default:
                            log.warn("Unhandled addr code: {}", c);
                            break;
                    }
                    int n = Integer.parseInt(s.substring(prefix.length() + 1, s.length() - 2)) * 16 + d * 4
                            + Integer.parseInt(s.substring(s.length() - 1, s.length()));
                    _high = n / 128;
                    _low = (n & 0x7F) / 2;
                    _as = (n & 0x01) * 0x20;
                    _valid = true;
                } else {
                    // assume that its LSnnn style
                    int n = Integer.parseInt(s.substring(prefix.length() + 1, s.length())) - 1;
                    _high = n / 256;
                    _low = (n & 0xFE) / 2;
                    _as = (n & 0x01) * 0x20;
                    _valid = true;
                }
            }
        } else {
            // didn't find a leading LS, complain
            reportParseError(s);
        }
    }

    void reportParseError(String s) {
        log.error("Can't parse sensor address string: " + s);
    }

    /**
     * Update a LocoNet message to have this address.
     * 
     * It is assumed that the sensor address may be encoded into bytes 1 and 2 of the 
     * message.
     *
     * @param m a LocoNetmessage to be updated to contain this object's sensor address
     */
    public void insertAddress(LocoNetMessage m) {
        m.setElement(1, getLowBits());
        m.setElement(2, getHighBits() | getASBit());
    }

    // convenient calculations
    public boolean matchAddress(int a1, int a2) { // a1 is byte 1 of ln msg, a2 is byte 2
        if (getHighBits() != (a2 & 0x0f)) {
            return false;
        }
        if (getLowBits() != (a1 & 0x7f)) {
            return false;
        }
        if (getASBit() != (a2 & 0x20)) {
            return false;
        }
        return true;
    }

    /**
     * @return integer value of this address in 0-4095 space
     */
    protected int asInt() {
        return _high * 256 + _low * 2 + (_as != 0 ? 1 : 0);
    }

    // accessors for parsed data
    public int getLowBits() {
        return _low;
    }

    public int getHighBits() {
        return _high;
    }

    /**
     * The bit representing the Aux or Sensor input
     *
     * @return 0x20 for aux input, 0x00 for switch input
     */
    public int getASBit() {
        return _as;
    }

    public boolean isValid() {
        return _valid;
    }

    @Override
    public String toString() {
        return getNumericAddress() + ":"
                + getDS54Address() + ":"
                + getBDL16Address();
    }

    /**
     * Name in the 1-4096 space
     *
     * @return LSnnn
     */
    public String getNumericAddress() {
        return prefix + "S" + (asInt() + 1);
    }

    /**
     * Name in the DS54 space
     *
     * @return LSnnnA or LSnnnS, depending on Aux or Switch input
     */
    public String getDS54Address() {
        if (_as != 0) {
            return prefix + "S" + (_high * 128 + _low) + "A";
        } else {
            return prefix + "S" + (_high * 128 + _low) + "S";
        }
    }

    /**
     * Name in the BDL16 space
     *
     * @return e.g. LSnnnA3, with nnn the BDL16 number, A the section number,
     *         and 3 the channel number
     */
    public String getBDL16Address() {
        String letter = null;
        String digit = null;

        switch (asInt() & 0x03) {
            case 0:
                digit = "0";
                break;
            case 1:
                digit = "1";
                break;
            case 2:
                digit = "2";
                break;
            case 3:
                digit = "3";
                break;
            default:
                digit = "X";
                log.error("Unexpected digit value: " + asInt());
        }
        switch ((asInt() & 0x0C) / 4) {
            case 0:
                letter = "A";
                break;
            case 1:
                letter = "B";
                break;
            case 2:
                letter = "C";
                break;
            case 3:
                letter = "D";
                break;
            default:
                letter = "X";
                log.error("Unexpected letter value: " + asInt());
        }
        return prefix + "S" + (asInt() / 16) + letter + digit;
    }

    private final static Logger log = LoggerFactory.getLogger(LnSensorAddress.class);

}
