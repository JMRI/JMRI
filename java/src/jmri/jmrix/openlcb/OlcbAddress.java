// OlcbAddress.java

package jmri.jmrix.openlcb;

import java.util.regex.*;

import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanMessage;

/**
 * Utilities for handling OpenLCB event messages as addresses.
 * <P>
 * OpenLCB event messages have header information, plus
 * an EventID in the data part.
 * JMRI maps these into address strings.
 * <p>
 * Forms:
 * <dl>
 * <dt>Full hex string preceeded by "x"<dd>Needs to be pairs of digits:  0123, not 123
 * <dt>Full 8 byte ID as pairs separated by "."
 * </dl>
 *
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2008, 2010
 * @version     $Revision$
 */
public class OlcbAddress {

    // groups
    static final int GROUP_FULL_HEX = 1; // xhhhhhh
    static final int GROUP_DOT_HEX = 3; // dotted hex form

    static final String singleAddressPattern = "(x(\\p{XDigit}\\p{XDigit}){1,8})|((\\p{XDigit}?\\p{XDigit}.){7}\\p{XDigit}?\\p{XDigit})";
    
	private Matcher hCode = Pattern.compile("^"+singleAddressPattern+"$").matcher("");

    String aString = null;
    int[] aFrame = null;
    boolean match = false;

    static final int NODEFACTOR = 100000;
    
    /**
     * Construct from string without leading system or 
     * type letters
     */
    public OlcbAddress(String s) {
        aString = s;
        // now parse
        match = hCode.reset(aString).matches();
        if (match) {
            if (hCode.group(GROUP_FULL_HEX)!=null) {
                // hit on hex form
                String l = hCode.group(GROUP_FULL_HEX);
                int len = (l.length()-1)/2;
                aFrame = new int[len];
                // get the frame data
                for (int i = 0; i<len; i++) {
                    String two = l.substring(1+2*i, 1+2*i+2);
                    aFrame[i] = Integer.parseInt(two, 16);
                }
            } else if (hCode.group(GROUP_DOT_HEX)!=null) {
                // dotted form, 7 dots
                String[] terms = s.split("\\.");
                if (terms.length != 8) log.error("unexpected number of terms: "+terms.length);
                aFrame = new int[terms.length];
                for (int i = 0; i<terms.length; i++) {
                    aFrame[i] = Integer.parseInt(terms[i],16);
                }
            }
        } else {
            // no match, leave match false and aFrame null
        }
    }

    /**
     * Two addresses are equal if they result in the same numeric contents
     */
    public boolean equals(Object r) {
        if (r == null) return false;
        if (! (r.getClass().equals(this.getClass()))) return false;
        OlcbAddress opp = (OlcbAddress) r;
        if (opp.aFrame.length != this.aFrame.length) return false;
        for (int i = 0; i<this.aFrame.length; i++) {
            if (this.aFrame[i]!=opp.aFrame[i]) return false;
        }
        return true;
    }
    
    public int hashCode() {
        int ret = 0;
        for (int i = 0; i<this.aFrame.length; i++) {
            ret += this.aFrame[i];
        }
        return ret;
    }
  
    public CanMessage makeMessage() {
        CanMessage c = new CanMessage(aFrame, 0x195B4000);
        c.setExtended(true);
        return c;
    }
        
    public boolean check() {
        return hCode.reset(aString).matches();
    }
    
    boolean match(CanReply r) {
        // check address first
        if (r.getNumDataElements() != aFrame.length) return false;
        for (int i = 0; i<aFrame.length; i++) {
            if (aFrame[i]!=r.getElement(i)) return false;
        }
        // check for event message type
        if (! r.isExtended()) return false;        
        if ( (r.getHeader() & 0x1FFFF000) != 0x195B4000) return false;
        return true;
    }
    
    boolean match(CanMessage r) {
        // check address first
        if (r.getNumDataElements() != aFrame.length) return false;
        for (int i = 0; i<aFrame.length; i++) {
            if (aFrame[i]!=r.getElement(i)) return false;
        }
        // check for event message type
        if (! r.isExtended()) return false;
        if ( (r.getHeader() & 0x1FFFF000) != 0x195B4000) return false;
        return true;
    }
    
    /**
     * Split a string containing one or more addresses
     * into individual ones.
     * @return null if entire string can't be parsed.
     */
    public OlcbAddress[] split() {  
        // reject strings ending in ";"
        if (aString.endsWith(";")) return null;
        
        // split string at ";" points
        String[] pStrings = aString.split(";");

        OlcbAddress[] retval = new OlcbAddress[pStrings.length];

        for (int i = 0; i<pStrings.length; i++) {
            // check validity of each
            if (pStrings[i].equals("")) return null;
            if (!hCode.reset(pStrings[i]).matches()) return null;
            
            retval[i] = new OlcbAddress(pStrings[i]);
            if (retval[i] == null) return null;
        }
        return retval;
    }
    
    public boolean checkSplit() {
        return (split()!=null);
    }
    
    int[] elements() {
        return aFrame;
    }
    
    public String toString() {
        return aString;
    }

    public String toCanonicalString() {
        String retval = "x";
        for (int i=0; i<aFrame.length; i++) {
            retval = jmri.util.StringUtil.appendTwoHexFromInt(aFrame[i], retval);
        }
        return retval;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OlcbAddress.class.getName());

}


/* @(#)OlcbAddress.java */
