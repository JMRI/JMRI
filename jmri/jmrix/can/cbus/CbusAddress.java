// CbusAddress.java

package jmri.jmrix.can.cbus;

import java.util.regex.*;

import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanMessage;

/**
 * Utilities for handling CBUS addresses.
 * <P>
 * CBus frames have a one byte command and length, optionally followed by
 * data bytes.
 * JMRI maps these into address strings.
 * <p>
 * Forms:
 * <dl>
 * <dt>Full hex string<dd>Needs to be pairs of digits:  0123, not 123
 * <dt>+/-ddd
 * <dt>
 * </dl>
 *
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version     $Revision: 1.1 $
 */
public class CbusAddress {

    // groups
    // 1: +ddd/-ddd  where ddd is node*NODEFACTOR + event
    // 2: the +/- from that
    // 3: xhhhhhh
    static final String singleAddressPattern = "((\\+|-)?\\d++)|(x(\\p{XDigit}\\p{XDigit}){1,8})";
    
	private Matcher hCode = Pattern.compile("^"+singleAddressPattern+"$").matcher("");
	private Matcher nCodes = Pattern.compile("^("+singleAddressPattern+")(;"
	                                    +singleAddressPattern+")*$").matcher("");

    String aString = null;
    int[] aFrame = null;
    boolean match = false;

    static final int NODEFACTOR = 100000;
    
    /**
     * Construct from string without leading system or 
     * type letters
     */
    public CbusAddress(String s) {
        aString = s;
        // now parse
        match = hCode.reset(aString).matches();
        if (match) {
            if (hCode.group(1)!=null) {
                // hit on +/-ddd
                aFrame = new int[5];

                int n = Integer.parseInt(aString.substring(1, aString.length()));  // skip +/-
                int node = n/NODEFACTOR;
                int event = n%NODEFACTOR;

                aFrame[4] = event&0xff;
                aFrame[3] = (event>>8)&0xff;
                aFrame[2] = node&0xff;
                aFrame[1] = (node>>8)&0xff;
                
                 // add command
                if (aString.substring(0,1).equals("+"))
                    aFrame[0] = CbusConstants.CBUS_OP_EV_ON;
                else if (aString.substring(0,1).equals("-"))
                    aFrame[0] = CbusConstants.CBUS_OP_EV_OFF;
                else // default
                    aFrame[0] = CbusConstants.CBUS_OP_EV_ON;
            } else if (hCode.group(3)!=null) {
                // hit on hex form
                String l = hCode.group(3);
                int len = (l.length()-1)/2;
                aFrame = new int[len];
                // get the frame data
                for (int i = 0; i<len; i++) {
                    String two = l.substring(1+2*i, 1+2*i+2);
                    aFrame[i] = Integer.parseInt(two, 16);
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
        if (! (r.getClass().equals(CbusAddress.class))) return false;
        CbusAddress opp = (CbusAddress) r;
        if (opp.aFrame.length != this.aFrame.length) return false;
        for (int i = 0; i<this.aFrame.length; i++) {
            if (this.aFrame[i]!=opp.aFrame[i]) return false;
        }
        return true;
    }
    
    public CanMessage makeMessage() {
        return new CanMessage(aFrame);
    }
        
    boolean check() {
        return hCode.reset(aString).matches();
    }
    
    boolean match(CanReply r) {
        if (r.getNumDataElements() != aFrame.length) return false;
        for (int i = 0; i<aFrame.length; i++) {
            if (aFrame[i]!=r.getElement(i)) return false;
        }
        return true;
    }
    
    boolean match(CanMessage r) {
        if (r.getNumDataElements() != aFrame.length) return false;
        for (int i = 0; i<aFrame.length; i++) {
            if (aFrame[i]!=r.getElement(i)) return false;
        }
        return true;
    }
    
    /**
     * Split a string containing one or more addresses
     * into individual ones.
     * @return null if entire string can't be parsed.
     */
    public CbusAddress[] split() {  
        // check validity at start
        if (!checkSplit()) return null;
        
        // split string at ";" points
        String[] pStrings = aString.split(";");
        
        CbusAddress[] retval = new CbusAddress[pStrings.length];
        
        for (int i = 0; i<pStrings.length; i++) {
            retval[i] = new CbusAddress(pStrings[i]);
        }
        return retval;
    }
    
    public boolean checkSplit() {
        return nCodes.reset(aString).matches();
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CbusAddress.class.getName());

}


/* @(#)CbusAddress.java */
