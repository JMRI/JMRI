package jmri.jmrix.openlcb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import org.openlcb.EventID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;

/**
 * Utilities for handling OpenLCB event messages as addresses.
 * <p>
 * OpenLCB event messages have header information, plus an EventID in the data
 * part. JMRI maps these into address strings.
 * <p>
 * Forms:
 * <dl>
 * <dt>Full hex string preceeded by "x"<dd>Needs to be pairs of digits: 0123,
 * not 123
 * <dt>Full 8 byte ID as pairs separated by "."
 * </dl>
 * Note: the {@link #check()} routine does a full, expensive
 * validity check of the name.  All other operations 
 * assume correctness, diagnose some invalid-format strings, but 
 * may appear to successfully handle other invalid forms.
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2010, 2018
 */
public class OlcbAddress {

    // groups
    static final int GROUP_FULL_HEX = 1; // xhhhhhh
    static final int GROUP_DOT_HEX = 3; // dotted hex form

    static final String singleAddressPattern = "([xX](\\p{XDigit}\\p{XDigit}){1,8})|((\\p{XDigit}?\\p{XDigit}.){7}\\p{XDigit}?\\p{XDigit})";

    private Matcher hCode = null;

    private Matcher getMatcher() {
        if (hCode == null)  hCode = Pattern.compile("^" + singleAddressPattern + "$").matcher("");
        return hCode;
    }
    
    String aString = null;
    int[] aFrame = null;
    boolean match = false;

    static final int NODEFACTOR = 100000;

    /** 
     * Construct from OlcbEvent
     *
     */
    public OlcbAddress(EventID e) {
        byte[] contents = e.getContents();
        aFrame = new int[contents.length];
        int i = 0;
        for (byte b : contents) {
            aFrame[i++] = b;
        }
        aString = toCanonicalString();
    }
    
    /**
     * Construct from string without leading system or type letters
     * @param s hex coded string of address
     */
    public OlcbAddress(String s) {
        aString = s;
        // now parse
        // This is done manually, rather than via regular expressions, for performance reasons.
        if (aString.contains(";")) {
            // multi-part address; leave match false and aFrame null
            return;
        } else if (aString.contains(".")) {
            // dotted form, 7 dots
            String[] terms = s.split("\\.");
            if (terms.length != 8) {
                log.error("unexpected number of terms: " + terms.length);
            }
            int[] tFrame = new int[terms.length];
            try {
                for (int i = 0; i < terms.length; i++) {
                    tFrame[i] = Integer.parseInt(terms[i], 16);
                }
            } catch (NumberFormatException ex) { return; } // leaving the string unparsed
            aFrame = tFrame;
            match = true;
        } else {
            // assume single hex string - drop leading x if present
            if (aString.startsWith("x")) aString = aString.substring(1);
            if (aString.startsWith("X")) aString = aString.substring(1);
            int len = aString.length() / 2;
            int[] tFrame  = new int[len];
            // get the frame data
            try {
                for (int i = 0; i < len; i++) {
                    String two = aString.substring(2 * i, 2 * i + 2);
                    tFrame[i] = Integer.parseInt(two, 16);
                }
            } catch (NumberFormatException ex) { return; }  // leaving the string unparsed  
            aFrame = tFrame;
            match = true;
        }
    }

    /**
     * Two addresses are equal if they result in the same numeric contents
     */
    @Override
    public boolean equals(Object r) {
        if (r == null) {
            return false;
        }
        if (!(r.getClass().equals(this.getClass()))) {
            return false;
        }
        OlcbAddress opp = (OlcbAddress) r;
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

    public int compare(@Nonnull OlcbAddress opp) {
        // if neither matched, just do a lexical sort
        if (!match && !opp.match) return aString.compareTo(opp.aString);
        
        // match sorts before non-matched
        if (match && !opp.match) return -1;
        if (!match && opp.match) return +1;
        
        // usual case: comparing on content
        for (int i = 0; i < Math.min(aFrame.length, opp.aFrame.length); i++) {
            if (aFrame[i] != opp.aFrame[i]) return Integer.signum(aFrame[i] - opp.aFrame[i]);
        }
        // check for different length (shorter sorts first)
        return Integer.signum(aFrame.length - opp.aFrame.length);
    }
    
    public CanMessage makeMessage() {
        CanMessage c = new CanMessage(aFrame, 0x195B4000);
        c.setExtended(true);
        return c;
    }

    /**
     * Confirm that the address string (provided earlier) is fully
     * valid.  This is an expensive call. It's complete-compliance done
     * using a regular expression. It can reject some 
     * forms that the code will normally handle OK.
     */
    public boolean check() {
        return getMatcher().reset(aString).matches();
    }

    boolean match(CanReply r) {
        // check address first
        if (r.getNumDataElements() != aFrame.length) {
            return false;
        }
        for (int i = 0; i < aFrame.length; i++) {
            if (aFrame[i] != r.getElement(i)) {
                return false;
            }
        }
        // check for event message type
        if (!r.isExtended()) {
            return false;
        }
        if ((r.getHeader() & 0x1FFFF000) != 0x195B4000) {
            return false;
        }
        return true;
    }

    boolean match(CanMessage r) {
        // check address first
        if (r.getNumDataElements() != aFrame.length) {
            return false;
        }
        for (int i = 0; i < aFrame.length; i++) {
            if (aFrame[i] != r.getElement(i)) {
                return false;
            }
        }
        // check for event message type
        if (!r.isExtended()) {
            return false;
        }
        if ((r.getHeader() & 0x1FFFF000) != 0x195B4000) {
            return false;
        }
        return true;
    }

    /**
     * Split a string containing one or more addresses into individual ones.
     *
     * @return null if entire string can't be parsed.
     */
     @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
        justification = "Documented API, no resources to improve")
    public OlcbAddress[] split() {
        // reject strings ending in ";"
        if (aString.endsWith(";")) {
            return null;
        }

        // split string at ";" points
        String[] pStrings = aString.split(";");

        OlcbAddress[] retval = new OlcbAddress[pStrings.length];

        for (int i = 0; i < pStrings.length; i++) {
            // check validity of each
            if (pStrings[i].equals("")) {
                return null;
            }
            
            // too expensive to do full regex check here, as this is used a lot in e.g. sorts
            // if (!getMatcher().reset(pStrings[i]).matches()) return null;

            retval[i] = new OlcbAddress(pStrings[i]);
            if (!retval[i].match) {
                return null;
            }
        }
        return retval;
    }

    public boolean checkSplit() {
        return (split() != null);
    }

    int[] elements() {
        return aFrame;
    }

    @Override
    public String toString() {
        return aString;
    }

    public String toCanonicalString() {
        String retval = "x";
        for (int i = 0; i < aFrame.length; i++) {
            retval = jmri.util.StringUtil.appendTwoHexFromInt(aFrame[i], retval);
        }
        return retval;
    }

    /**
     * Provide as dotted pairs
     */
    public String toDottedString() {
        String retval = "";
        for (int i = 0; i < aFrame.length; i++) {
            if (! retval.isEmpty()) retval += ".";
            retval = jmri.util.StringUtil.appendTwoHexFromInt(aFrame[i], retval);
        }
        return retval;
    }

    public EventID toEventID() {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; ++i) b[i] = (byte)aFrame[i];
        return new EventID(b);
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbAddress.class);

}



