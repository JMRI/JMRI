package jmri.jmrix.can.cbus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.util.StringUtil;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Utilities for handling CBUS addresses.
 * <P>
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
    static final String singleAddressPattern = "((\\+|-)?\\d++)|([Xx](\\p{XDigit}\\p{XDigit}){1,8})|((\\+|-)?([Nn])?(\\d++)[Ee](\\d++))";

    private Matcher hCode = Pattern.compile("^" + singleAddressPattern + "$").matcher("");

    String aString = null;
    int[] aFrame = null;
    boolean match = false;

    static final int NODEFACTOR = 100000;

    /**
     * Construct from string without leading system or type letters
     */
    public CbusAddress(String s) {
        aString = s;
        // now parse
        match = hCode.reset(aString).matches();
        if (match) {
            if (hCode.group(1) != null) {
                // hit on +/-ddd
                aFrame = new int[5];

                int n = Integer.parseInt(aString.substring(1, aString.length()));  // skip +/-
                int node = n / NODEFACTOR;
                int event = n % NODEFACTOR;

                aFrame[4] = event & 0xff;
                aFrame[3] = (event >> 8) & 0xff;
                aFrame[2] = node & 0xff;
                aFrame[1] = (node >> 8) & 0xff;

                // add command
                if (aString.substring(0, 1).equals("+")) {
                    if (node > 0) {
                        aFrame[0] = CbusConstants.CBUS_ACON;
                    } else {
                        aFrame[0] = CbusConstants.CBUS_ASON;
                    }
                } else if (aString.substring(0, 1).equals("-")) {
                    if (node > 0) {
                        aFrame[0] = CbusConstants.CBUS_ACOF;
                    } else {
                        aFrame[0] = CbusConstants.CBUS_ASOF;
                    }
                } else {   // default
                    if (node > 0) {
                        aFrame[0] = CbusConstants.CBUS_ACON;
                    } else {
                        aFrame[0] = CbusConstants.CBUS_ASON;
                    }
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
                aFrame = new int[5];

                int node = Integer.parseInt(hCode.group(8));
                int event = Integer.parseInt(hCode.group(9));

                aFrame[4] = event & 0xff;
                aFrame[3] = (event >> 8) & 0xff;
                aFrame[2] = node & 0xff;
                aFrame[1] = (node >> 8) & 0xff;

                // add command
                if (( hCode.group(6)!= null ) && (hCode.group(6).equals("+"))) {
                    aFrame[0] = CbusConstants.CBUS_ACON;
                } else if (( hCode.group(6)!= null ) && (hCode.group(6).equals("-"))) {
                    aFrame[0] = CbusConstants.CBUS_ACOF;
                } else // default
                {
                    aFrame[0] = CbusConstants.CBUS_ACON;
                }
            }
        } else {
            // no match, leave match false and aFrame null
        }
    }

    /**
     * Two addresses are equal if they result in the same numeric contents.
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
        if (opp.aFrame.length != this.aFrame.length) {
            return false;
        }
        for (int i = 0; i < this.aFrame.length; i++) {
            if (this.aFrame[i] != opp.aFrame[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int ret = 0;
        for (int i = 0; i < this.aFrame.length; i++) {
            ret += this.aFrame[i];
        }
        return ret;
    }

    public CanMessage makeMessage(int header) {
        return new CanMessage(aFrame, header);
    }

    public boolean check() {
        return hCode.reset(aString).matches();
    }

    /**
     * Does the CbusAddress match a CanReply ( CanFrame being received by JMRI )
     *
     * @param r CanReply being tested
     * @return true if matches
     */
     boolean match(CanReply r) {
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
     * Does the CbusAddress match a CanMessage ( CanFrame being sent by JMRI )
     *
     * @param r CanMessage being tested
     * @return true if matches
     */
    boolean match(CanMessage r) {
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
            if (pStrings[i].equals("")) {
                return new CbusAddress[0];
            }
            if (!hCode.reset(pStrings[i]).matches()) {
                return new CbusAddress[0];
            }
            retval[i] = new CbusAddress(pStrings[i]);
        }
        return retval;
    }

    /**
     * Increments a CBUS address by 1
     * eg +123 to +124
     * eg -N123E456 to -N123E457
     * returns null if unable to make the address
     *
     */
    public static String getIncrement( @Nonnull String testAddr ) {
        CbusAddress a = new CbusAddress(testAddr);
        CbusAddress[] v = a.split();
        switch (v.length) {
            case 1:
                // get last part and increment
                int last =  StringUtil.getLastIntFromString(v[0].toString());
                if ( last < 1 ) { // event numbers should be > 0
                    return null;
                }
                return StringUtil.replaceLast(v[0].toString(), String.valueOf(last), String.valueOf(last+1));
            case 2:
                int lasta =  StringUtil.getLastIntFromString(v[0].toString());
                int lastb =  StringUtil.getLastIntFromString(v[1].toString());
                if ( ( lasta < 1 ) || ( lastb < 1 ) ) { // event numbers should be > 0
                    return null;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(StringUtil.replaceLast(v[0].toString(), String.valueOf(lasta), String.valueOf(lasta+1)));
                sb.append(";");
                sb.append(StringUtil.replaceLast(v[1].toString(), String.valueOf(lastb), String.valueOf(lastb+1)));
                return sb.toString();
            default:
                return null;
        }
    }

    /**
     * Used in Testing
     *
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
        return aFrame;
    }

    /**
     * eg. X9801D203A4 or +N123E456
     *
     */
     @Override
    public String toString() {
        return aString;
    }

    /**
     * eg. x9801D203A4 or x90007B01C8
     *
     */
     public String toCanonicalString() {
        String retval = "x";
        for (int i = 0; i < aFrame.length; i++) {
            retval = jmri.util.StringUtil.appendTwoHexFromInt(aFrame[i], retval);
        }
        return retval;
    }
    // private final static Logger log = LoggerFactory.getLogger(CbusAddress.class);
}
