package jmri.jmrix.can.cbus;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import jmri.JmriException;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for handling CBUS addresses.
 * <p>
 * CBUS frames have a one byte command and length, optionally followed by data
 * bytes. JMRI maps these into address strings.
 * <p>
 * Forms:
 * <dl>
 * <dt>Full hex string preceeded by "X"<dd>Needs to be pairs of digits: 0123,
 * not 123
 * <dt>+/-ddd<dd>ddd is node*100,000 (a.k.a NODEFACTOR) + event
 * <dt>+/-nNNNeEEE<dd>where NNN is a node number and EEE is an event number
 * </dl>
 * If ddd &lt; 65536 then the CBUS address is taken to represent a short event.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Andrew Crosland Copyright (C) 2011
 */
public class CbusAddress {

    // groups
    // 1: +ddd/-ddd  where ddd is node*NODEFACTOR + event
    // 2: the +/- from that
    // 3: xhhhhhh
    // 5: NE form
    // 6: the +/- from that
    // 7: optional "N"
    // 8: node number
    // 9: event number
    static final String SINGLE_ADDRESS_PATTERN = "((\\+|-)?\\d++)|([Xx](\\p{XDigit}\\p{XDigit}){1,8})|((\\+|-)?([Nn])?(\\d++)[Ee](\\d++))";

    private final Matcher hCode = Pattern.compile("^" + SINGLE_ADDRESS_PATTERN + "$").matcher("");

    private String aString = null;
    protected int[] aFrame = null;
    private boolean match = false;

    static final int NODEFACTOR = 100000;

    /**
     * Construct from string without leading system or type letters.
     * @param s CBUS Hardware Address format
     */
    public CbusAddress(String s) {
        aString = s;
        // now parse
        match = hCode.reset(aString).matches();
        if (match) {
            if (hCode.group(1) != null) {
                // hit on +/-ddd, sign is optional and an unsigned value is treated as +
                String sign = hCode.group(2);
                String digits = (sign == null) ? hCode.group(1) : hCode.group(1).substring(1);

                // parse as long then range check to avoid int overflow for node >= 21475
                long n = parseNonNegative(digits);
                long node = n / NODEFACTOR;
                long event = n % NODEFACTOR;

                if (n < 0 || !setEventFrame(node, event)) {
                    log.debug("CBUS address {} out of range, node and event must be 0-65535", s);
                    match = false;
                } else if ("-".equals(sign)) {
                    aFrame[0] = (node > 0) ? CbusConstants.CBUS_ACOF : CbusConstants.CBUS_ASOF;
                } else { // + or unsigned
                    aFrame[0] = (node > 0) ? CbusConstants.CBUS_ACON : CbusConstants.CBUS_ASON;
                }
            } else if (hCode.group(3) != null) {
                // hit on hex form
                String l = hCode.group(3);
                int len = (l.length() - 1) / 2;
                aFrame = new int[len];
                // get the frame data
                for (int i = 0; i < len; i++) {
                    String two = l.substring(1 + 2 * i, 1 + 2 * i + 2);
                    aFrame[i] = Integer.parseInt(two, 16);
                }
            } else if (hCode.group(5) != null) {
                // hit on EN form
                long node = parseNonNegative(hCode.group(8));
                long event = parseNonNegative(hCode.group(9));

                if (!setEventFrame(node, event)) {
                    log.debug("CBUS address {} out of range, node and event must be 0-65535", s);
                    match = false;
                } else if ("-".equals(hCode.group(6))) {
                    aFrame[0] = CbusConstants.CBUS_ACOF;
                } else { // + or unsigned
                    aFrame[0] = CbusConstants.CBUS_ACON;
                }
            }
        } else {
            // no match, leave match false and aFrame null
        }
    }

    /**
     * Parse a String of decimal digits.
     * @param digits the digits to parse
     * @return the value, or -1 if the value can not be parsed ( eg. too large ).
     */
    private static long parseNonNegative(String digits) {
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * Create the 5 element aFrame for a long or short event, element 0 is left as 0.
     * @param node the node number
     * @param event the event number
     * @return true if node and event are within 0-65535 and aFrame was set, else false.
     */
    private boolean setEventFrame(long node, long event) {
        if (node < 0 || node > 0xffff || event < 0 || event > 0xffff) {
            return false;
        }
        aFrame = new int[5];
        aFrame[4] = (int) (event & 0xff);
        aFrame[3] = (int) ((event >> 8) & 0xff);
        aFrame[2] = (int) (node & 0xff);
        aFrame[1] = (int) ((node >> 8) & 0xff);
        return true;
    }

    /**
     * Two addresses are equal if they result in the same numeric contents.
     * Two invalid addresses ( which have no numeric contents ) are also equal.
     * @param r The other CbusAddress to compare
     */
    @Override
    public boolean equals(Object r) {
        if (r == null) {
            return false;
        }
        if (!(r.getClass().equals(this.getClass()))) {
            return false;
        }
        CbusAddress opp = (CbusAddress) r;
        return Arrays.equals(this.aFrame, opp.aFrame);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(aFrame); // 0 for an invalid address
    }

    public CanMessage makeMessage(int header) {
        return new CanMessage(aFrame, header);
    }

    /**
     * Check if the address String was successfully parsed into a Frame.
     * @return true if valid, else false.
     */
    public boolean check() {
        return match;
    }

    /**
     * Does the CbusAddress match.
     *
     * @param r CanReply or CanMessage being tested
     * @return true if matches
     */
    public boolean match(jmri.jmrix.AbstractMessage r) {
        if (r.getNumDataElements() != aFrame.length) {
            return false;
        }
        if (CbusMessage.isShort(r)) {
            // Skip node number for short events
            if (aFrame[0] != r.getElement(0)) {
                return false;
            }
            for (int i = 3; i < aFrame.length; i++) {
                if (aFrame[i] != r.getElement(i)) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < aFrame.length; i++) {
                if (aFrame[i] != r.getElement(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Does the CbusAddress match a CanReply event request.
     *
     * @param r CanReply being tested
     * @return true if matches
     */
    public boolean matchRequest(CanReply r) {
        if (r.getNumDataElements() != aFrame.length) {
            return false;
        }
        if (CbusMessage.isShort(r)) {
            // Skip node number for short events
            if (CbusConstants.CBUS_ASRQ != r.getElement(0)) {
                return false;
            }
            for (int i = 3; i < aFrame.length; i++) {
                if (aFrame[i] != r.getElement(i)) {
                    return false;
                }
            }
        } else {
            if (CbusConstants.CBUS_AREQ != r.getElement(0)) {
                return false;
            }
            for (int i = 1; i < aFrame.length; i++) {
                if (aFrame[i] != r.getElement(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Split a string containing one or more addresses into individual ones.
     *
     * @return 0 length if entire string can't be parsed.
     */
    @Nonnull
    public CbusAddress[] split() {
        // reject strings ending in ";"
        if (aString.endsWith(";")) {
            return new CbusAddress[0];
        }

        // split string at ";" points
        String[] pStrings = aString.split(";");

        CbusAddress[] retval = new CbusAddress[pStrings.length];

        for (int i = 0; i < pStrings.length; i++) {
            // check validity of each
            if (pStrings[i].isEmpty()) {
                return new CbusAddress[0];
            }
            if (!hCode.reset(pStrings[i]).matches()) {
                return new CbusAddress[0];
            }
            retval[i] = new CbusAddress(pStrings[i]);
            if (!retval[i].check()) { // matched the pattern but out of range
                return new CbusAddress[0];
            }
        }
        return retval;
    }

    /**
     * Increments a CBUS address by 1 eg +123 to +124 eg -N123E456 to -N123E457
     *
     * @param testAddr initial CbusAddress String, eg -N123E456
     * @return incremented address. 
     * @throws jmri.JmriException if unable to make the address
     */
    @Nonnull
    public static String getIncrement(@Nonnull String testAddr) throws JmriException{
        log.debug("testing address {}", testAddr);
        validateSysName(testAddr);
        CbusAddress a = new CbusAddress(testAddr);
        CbusAddress[] v = a.split();
        String newString;
        switch (v.length) {
            case 2:
                int lasta = StringUtil.getLastIntFromString(v[0].toString());
                int lastb = StringUtil.getLastIntFromString(v[1].toString());
                StringBuilder sb = new StringBuilder();
                sb.append(StringUtil.replaceLast(v[0].toString(), String.valueOf(lasta), String.valueOf(lasta + 1)));
                sb.append(";");
                sb.append(StringUtil.replaceLast(v[1].toString(), String.valueOf(lastb), String.valueOf(lastb + 1)));
                newString = sb.toString();
                break;
            case 1:
                // get last part and increment
                int last = StringUtil.getLastIntFromString(v[0].toString());
                newString = StringUtil.replaceLast(v[0].toString(), String.valueOf(last), String.valueOf(last + 1));
                break;
            default:
                throw new JmriException("Unable to increment " + testAddr);
        }
        try {
            return validateSysName(newString);
        } catch (IllegalArgumentException e) {
            throw new JmriException("Unable to increment " + testAddr + " " + e.getMessage());
        }
    }

    // not A-F, N or X
    private static final String[] invalidChars = {
        "G","H","I","J","K","L","M","S","T","U","V","W","Y","Z",
        "?",":","++","--",",","*","NN","XX"};
    
    /**
     * Validate a CBUS hardware address validation.
     *
     * @param address the hardware address to check, excluding both system prefix and type letter.
     * @return same address if all OK.
     * @throws IllegalArgumentException when address is not validated.
     * or contains too many parts
     */
    public static String validateSysName(String address) throws IllegalArgumentException {

        if (address == null || address.isEmpty()) {
            throw new IllegalArgumentException("No Address passed ");
        }
        // address=address.toUpperCase().trim();
        for (String s : invalidChars) {
            if (address.contains(s)) {
                throw new jmri.NamedBean.BadSystemNameException(Locale.getDefault(), "InvalidSystemNameCharacter",address,s);
            }
        }
        
        if (address.endsWith(";")) {
            throw new IllegalArgumentException("Should not end with ; " + address);
        }

        // 1st set of switch cases enable strings to pass as a CbusAddress if unsigned
        String[] addressArray = address.split(";");
        switch (addressArray.length) {
            case 1:
                address = checkPartOfName(addressArray[0], "+");
                // adds sign when addressArray[0] is unsigned int (eg. "4" address is updated to "+4")
                break;
            case 2:
                address = checkPartOfName(addressArray[0], "+") + ";" + checkPartOfName(addressArray[1], "-");
                break;
            default:
                log.debug("validateSysName switch 1 found > 2 events");
                throw new IllegalArgumentException("Unable to convert Address: " + address);
        }

        CbusAddress a = new CbusAddress(address);
        CbusAddress[] v = a.split();
        switch (v.length) {
            case 1:
                if (address.startsWith("+") || address.startsWith("-")) {
                    break;
                }
                long unsigned;
                try {
                    unsigned = Long.parseLong(address); // accept unsigned integer
                    if (unsigned > 100000) {
                        break;
                    }
                } catch (NumberFormatException ex) {
                    log.debug("Unable to convert {} into Cbus format +nn", address);
                }
                throw new IllegalArgumentException("can't make 2nd event from address " + address);
            case 2:
                break;
            default:
                log.debug("validateSysName switch 2 found > 2 events");
                throw new IllegalArgumentException("Wrong number of events in address: " + address);
        }
        return address;
    }

    /**
     * Check part of a CbusAddress. Will add "+" or "-" if not present in part.
     *
     * @param testpart    string part of Cbus address to check, will accept
     *                    unsigned single integer
     * @param plusOrMinus character to add in front if not yet present
     * @return part of CBUS address including + or - (on off) sign
     */
    private static String checkPartOfName(String testpart, String plusOrMinus) {
        long unsigned = 0;
        String part = testpart;
        try {
            unsigned = Long.parseLong(part); // long so node numbers >= 21475 do not overflow
            log.debug("part {} is integer {}", part, unsigned);
            if ((part.charAt(0) != '+') && (part.charAt(0) != '-')) {
                if (unsigned > 0 && unsigned < 65536) {
                    part = plusOrMinus + part;
                } else if (unsigned == 0) {
                    // an unsigned 0 is not accepted, must be +0 or -0
                    throw new IllegalArgumentException("Unsigned 0 event requires + or - : " + part);
                }
            }
            if (unsigned > 65535 && unsigned < 100000) {
                throw new IllegalArgumentException("On Too big for an event, too low for node + event : " + part);
            }
            if (unsigned < -65535 && unsigned > -100000) {
                throw new IllegalArgumentException("Off Too big for an event, too low for node + event : " + part);
            }
        } catch (NumberFormatException ex) {
            log.debug("Unable to convert {} into Cbus format +nn", part);
        }
        if (unsigned == 0) {
            // so it's a string.
            // ignoring anything starting with x or X as it may be a HEX value
            // which is checked by core CbusAddress
            try {
                if (part.toUpperCase().charAt(0) != 'X') {
                    log.debug("not an int or hex {}", part);

                    // it's got a string in somewhere, start by checking event number
                    int lasta = StringUtil.getLastIntFromString(part);
                    log.debug("last string {}", lasta);
                    if (lasta > 65535) {
                        throw new IllegalArgumentException("Event Too Large in address: " + part);
                    }
                    int firsta = StringUtil.getFirstIntFromString(part);
                    log.debug("first string {}", firsta);
                    if (firsta > 65535) {
                        throw new IllegalArgumentException("Node Too Large in address: " + part);
                    }
                }
            } catch (StringIndexOutOfBoundsException ex) {
                throw new IllegalArgumentException("Address Too Short? : " + part);
            }
        }
        return part;
    }

    /**
     * Used in Testing.
     * @return true if split length is 1 or 2, else false.
     */
    public boolean checkSplit() {
        switch (split().length) {
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }

    int[] elements() {
        return java.util.Arrays.copyOf(aFrame, aFrame.length);
    }

    /**
     * eg. X9801D203A4 or +N123E456
     */
    @Override
    public String toString() {
        return aString;
    }

    /**
     * eg.x9801D203A4 or x90007B01C8
     * @return x followed by Can Frame Data
     */
    public String toCanonicalString() {
        String retval = "x";
        for (int i = 0; i < aFrame.length; i++) {
            retval = jmri.util.StringUtil.appendTwoHexFromInt(aFrame[i], retval);
        }
        return retval;
    }

    private static final Logger log = LoggerFactory.getLogger(CbusAddress.class);

}
