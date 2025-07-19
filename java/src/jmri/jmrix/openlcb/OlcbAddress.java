package jmri.jmrix.openlcb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;

import jmri.NamedBean.BadSystemNameException;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.openlcb.EventID;

import javax.annotation.Nonnull;

/**
 * Utilities for handling OpenLCB event messages as addresses.
 * <p>
 * OpenLCB event messages have header information, plus an EventID in the data
 * part. JMRI maps these into address strings.
 * <p>
 * String forms:
 * <dl>
 * <dt>Special case for DCC Turnout addressing:  Tnnn where nnn is a decimal number
 *
 * <dt>Full hex string preceeded by "x"<dd>Needs to be pairs of digits: 0123,
 * not 123
  *
 * <dt>Full 8 byte ID as pairs separated by "."
 * </dl>
 * <p>
 * Note: the {@link #check()} routine does a full, expensive
 * validity check of the name.  All other operations
 * assume correctness, diagnose some invalid-format strings, but
 * may appear to successfully handle other invalid forms.
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2010, 2018, 2024
 */
public final class OlcbAddress {

    static final String singleAddressPattern = "([xX](\\p{XDigit}\\p{XDigit}){1,8})|((\\p{XDigit}?\\p{XDigit}.){7}\\p{XDigit}?\\p{XDigit})";

    private Matcher hCode = null;

    private Matcher getMatcher() {
        if (hCode == null)  hCode = Pattern.compile("^" + singleAddressPattern + "$").matcher("");
        return hCode;
    }

    private String aString;         // String value of the address
    private int[] aFrame = null;    // int[8] of event ID; if null, aString might be two addresses
    private boolean match = false;  // true if address properly parsed; false (may) mean two-part address
    private boolean fromName = false; // true if this originate as an event name
    /**
     * Construct from OlcbEvent.
     *
     * @param e the event ID.
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
     * @param input hex coded string of address
     */
    public OlcbAddress(String input, final CanSystemConnectionMemo memo) {
        // This is done manually, rather than via regular expressions, for performance reasons.

        String s = input.strip();
        
        OlcbEventNameStore nameStore = null;
        if (memo != null) { 
            nameStore = memo.get(OlcbEventNameStore.class);
        }
         if (nameStore != null && nameStore.hasEventID(s)) {
            EventID eid = nameStore.getEventID(s);
            // name form
            // load the event ID into the aFrame c.f. OlcbAddress(EventID) ctor
            byte[] contents = eid.getContents();
            aFrame = new int[contents.length];
            int i = 0;
            for (byte b : contents) {
                aFrame[i++] = b;
            }
            match = true;
            fromName = true;
            // leave aString as original argument
            aString = s;
            return;
        }
        
        // check for special addressing forms
        if (s.startsWith("T")) {
            // leading T, so convert to numeric form from turnout number
            int from;
            try {
                from = Integer.parseInt(s.substring(1));
            } catch (NumberFormatException e) {
                from = 0;
            }

            if (from >= 2045) from = from-2045;
            else from = from + 3;
            long event = 0x0101020000FF0000L | (from<<1);
            
            s = String.format("%016X;%016X", event, event+1);
            log.trace(" Turnout form converted to {}", s);
        } else if (s.startsWith("S")) {
            // leading S, so convert to numeric form from sensor number
            int from;
            try {
                from = Integer.parseInt(s.substring(1));
            } catch (NumberFormatException e) {
                from = 0;
            }

            from = 0xFFF & (from - 1); // 1 based name to 0 based network, 12 bit value
            
            long event1 = 0x0101020000FB0000L | from; // active/on
            long event2 = 0x0101020000FA0000L | from; // inactive/off
 
            s = String.format("%016X;%016X", event1, event2);
            log.trace(" Sensor form converted to {}", s);
        }

        aString = s;

        // numeric address string format
        if (aString.contains(";")) {
            // multi-part address; leave match false and aFrame null; only aString has content
            // will later be split up and parsed with #split() call
            return;
        }
        
        // check for name vs numeric address formats
        
        if (aString.contains(".")) {
            // dotted form, 7 dots
            String[] terms = s.split("\\.");
            if (terms.length != 8) {
                log.debug("unexpected number of terms: {}, address is {}", terms.length, s);
            }
            int[] tFrame = new int[terms.length];
            int i = -1;
            try {
                for (i = 0; i < terms.length; i++) {
                    tFrame[i] = Integer.parseInt(terms[i].strip(), 16);
                }
            } catch (NumberFormatException ex) {
                // leaving the string unparsed
                log.debug("failed to parse EventID \"{}\" at {} due to {}; might be a partial value", s, i, terms[i].strip());
                return; 
            } 
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
            } catch (NumberFormatException ex) { 
                log.debug("failed to parse EventID \"{}\"; might be a partial value", s);
                return;
            }  // leaving the string unparsed
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
        if (!(r.getClass().equals(this.getClass()))) { // final class simplifies this
            return false;
        }
        OlcbAddress opp = (OlcbAddress) r;
        if (this.aFrame == null || opp.aFrame == null) {
            // one or the other has just a string, e.g A;B form.
            // compare strings
            return this.aString.equals(opp.aString);
        }
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
        for (int value : this.aFrame) {
            ret += value*8; // don't want to overflow int, do want to spread out
        }
        return ret;
    }

    public int compare(@Nonnull OlcbAddress opp) {
        // if neither matched, just do a lexical sort
        if (!match && !opp.match) return aString.compareTo(opp.aString);

        // match (single address) sorts before non-matched (double address)
        if (match && !opp.match) return -1;
        if (!match && opp.match) return +1;

        // both matched, usual case: comparing on content
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
     * valid.
     * <p>
     * This is an expensive call. It's complete-compliance done
     * using a regular expression. It can reject some
     * forms that the code will normally handle OK.
     * @return true if valid, else false.
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
        return (r.getHeader() & 0x1FFFF000) == 0x195B4000;
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
        return (r.getHeader() & 0x1FFFF000) == 0x195B4000;
    }

    /**
     * Split a string containing one or more addresses into individual ones.
     *
     * @return null if entire string can't be parsed.
     */
     @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
        justification = "Documented API, no resources to improve")
    public OlcbAddress[] split(final CanSystemConnectionMemo memo) {
        // reject strings ending in ";"
        if (aString == null || aString.endsWith(";")) {
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

            retval[i] = new OlcbAddress(pStrings[i], memo);
            if (!retval[i].match) {
                return null;
            }
        }
        return retval;
    }

    public boolean checkSplit( final CanSystemConnectionMemo memo) {
        return (split(memo) != null);
    }

    int[] elements() {
        return aFrame;
    }

    @Override
    /**
     * @return The string that was used to create this address
     */
    public String toString() {
        return aString;
    }

    /**
     * @return The canonical form of 0x1122334455667788
     */
    public String toCanonicalString() {
        String retval = "x";
        for (int value : aFrame) {
            retval = jmri.util.StringUtil.appendTwoHexFromInt(value, retval);
        }
        return retval;
    }

    /**
     * Provide as dotted pairs.
     * @return dotted pair form off string.
     */
    public String toDottedString() {
        String retval = "";
        if (aFrame == null) return retval;
        for (int value : aFrame) {
            if (!retval.isEmpty())
                retval += ".";
            retval = jmri.util.StringUtil.appendTwoHexFromInt(value, retval);
        }
        return retval;
    }

    /**
     * @return null if no valid address was parsed earlier, e.g. there was a ; in the data
     */
    public EventID toEventID() {
        if (aFrame == null) return null;
        byte[] b = new byte[8];
        for (int i = 0; i < Math.min(8, aFrame.length); ++i) b[i] = (byte)aFrame[i];
        return new EventID(b);
    }

    /**
     * Was this parsed from a name (e.g. not explicit ID, not pair)
     * @return true if constructed from an event name
     */
    public boolean isFromName() { return fromName; }
    /**
     * Validates Strings for OpenLCB format.
     * @param name   the system name to validate.
     * @param locale the locale for a localized exception.
     * @param prefix system prefix, eg. MT for OpenLcb turnout.
     * @return the unchanged value of the name parameter.
     * @throws jmri.NamedBean.BadSystemNameException if provided name is an invalid format.
     */
    @Nonnull
    public static String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale,
        @Nonnull String prefix, final CanSystemConnectionMemo memo) throws BadSystemNameException {
        String oAddr = name.substring(prefix.length());
        OlcbAddress a = new OlcbAddress(oAddr, memo);
        OlcbAddress[] v = a.split(memo);
        if (v == null) {
            throw new BadSystemNameException(locale,"InvalidSystemNameCustom","Did not find usable system name: " + name + " does not convert to a valid Olcb address");
        }
        switch (v.length) {
            case 1:
            case 2:
                break;
            default:
                throw new BadSystemNameException(locale,"InvalidSystemNameCustom","Wrong number of events in address: " + name);
        }
        return name;
    }

    /**
     * Validates 2 part Hardware Address Strings for OpenLCB format.
     * @param name   the system name to validate.
     * @param locale the locale for a localized exception.
     * @param prefix system prefix, eg. MT for OpenLcb turnout.
     * @return the unchanged value of the name parameter.
     * @throws jmri.NamedBean.BadSystemNameException if provided name is an invalid format.
     */
    @Nonnull
    public static String validateSystemNameFormat2Part(@Nonnull String name, @Nonnull java.util.Locale locale,
        @Nonnull String prefix, final CanSystemConnectionMemo memo) throws BadSystemNameException {
        String oAddr = name.substring(prefix.length());
        OlcbAddress a = new OlcbAddress(oAddr, memo);
        OlcbAddress[] v = a.split(memo);
        if (v == null) {
            throw new BadSystemNameException(locale,"InvalidSystemNameCustom","Did not find usable system name: " + name + " to a valid Olcb address");
        }
        if ( v.length == 2 ) {
            return name;
        }
        throw new BadSystemNameException(locale,"InvalidSystemNameCustom","Address requires 2 Events: " + name);
    }

    /**
     * See {@link jmri.NamedBean#compareSystemNameSuffix} for background.
     * This is a common implementation for OpenLCB Sensors and Turnouts
     * of the comparison method.
     *
     * @param suffix1 1st suffix to compare.
     * @param suffix2 2nd suffix to compare.
     * @return true if suffixes match, else false.
     */
    @CheckReturnValue
    public static int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, final CanSystemConnectionMemo memo) {

        // extract addresses
        OlcbAddress[] array1 = new OlcbAddress(suffix1, memo).split(memo);
        OlcbAddress[] array2 = new OlcbAddress(suffix2, memo).split(memo);

        // compare on content
        for (int i = 0; i < Math.min(array1.length, array2.length); i++) {
            int c = array1[i].compare(array2[i]);
            if (c != 0) return c;
        }
        // check for different length (shorter sorts first)
        return Integer.signum(array1.length - array2.length);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbAddress.class);

}



