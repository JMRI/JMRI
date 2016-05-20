// CbusAddress.java

package jmri.jmrix.can.cbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.*;

import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanMessage;

/**
 * Utilities for handling CBUS addresses.
 * <P>
 * CBUS frames have a one byte command and length, optionally followed by
 * data bytes.
 * JMRI maps these into address strings.
 * <p>
 * Forms:
 * <dl>
 * <dt>Full hex string preceeded by "X"<dd>Needs to be pairs of digits:  0123, not 123
 * <dt>+/-ddd<dd>ddd is node*100,000 (a.k.a NODEFACTOR) + event
 * <dt>+/-nNNNeEEE<dd>where NNN is a node number and EEE is an event number
 * </dl>
 * If ddd &lt; 65536 then the CBUS address is taken to represent a short event.
 * 
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2008
 * @author	Andrew Crosland Copyright (C) 2011
 * @version     $Revision$
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
    
	private Matcher hCode = Pattern.compile("^"+singleAddressPattern+"$").matcher("");

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
                if (aString.substring(0,1).equals("+")) {
                    if (node > 0) {
                        aFrame[0] = CbusConstants.CBUS_ACON;
                    } else {
                        aFrame[0] = CbusConstants.CBUS_ASON;
                    }
                }  else if (aString.substring(0, 1).equals("-")) {
                    if (node > 0) {
                        aFrame[0] = CbusConstants.CBUS_ACOF;
                    } else {
                        aFrame[0] = CbusConstants.CBUS_ASOF;
                    }
                }  else {   // default
                    if (node > 0) {
                        aFrame[0] = CbusConstants.CBUS_ACON;
                    } else {
                        aFrame[0] = CbusConstants.CBUS_ASON;
                    }
                }
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
            } else if (hCode.group(5)!=null) {
                // hit on EN form
                aFrame = new int[5];

                int node = Integer.parseInt(hCode.group(8));
                int event = Integer.parseInt(hCode.group(9));

                aFrame[4] = event&0xff;
                aFrame[3] = (event>>8)&0xff;
                aFrame[2] = node&0xff;
                aFrame[1] = (node>>8)&0xff;
                
                 // add command
                if (hCode.group(6)==null)
                    aFrame[0] = CbusConstants.CBUS_ACON;
                else if (hCode.group(6).equals("+"))
                    aFrame[0] = CbusConstants.CBUS_ACON;
                else if (hCode.group(6).equals("-"))
                    aFrame[0] = CbusConstants.CBUS_ACOF;
                else // default
                    aFrame[0] = CbusConstants.CBUS_ACON;
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
//        if (! (r.getClass().equals(CbusAddress.class))) return false;
        if (! (r.getClass().equals(this.getClass()))) return false;
        CbusAddress opp = (CbusAddress) r;
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
  
    public CanMessage makeMessage(int header) {
        return new CanMessage(aFrame, header);
    }
        
    public boolean check() {
        return hCode.reset(aString).matches();
    }
    
    boolean match(CanReply r) {
        if (r.getNumDataElements() != aFrame.length) return false;
        int opc = CbusMessage.getOpcode(r);
        if (CbusOpCodes.isShortEvent(opc)) {
            // Skip node number for short events
            if (aFrame[0]!=r.getElement(0)) return false;
            for (int i = 3; i<aFrame.length; i++) {
                if (aFrame[i]!=r.getElement(i)) return false;
            }
        } else {
            for (int i = 0; i<aFrame.length; i++) {
                if (aFrame[i]!=r.getElement(i)) return false;
            }
        }
        return true;
    }
    
    boolean match(CanMessage r) {
        if (r.getNumDataElements() != aFrame.length) return false;
        int opc = CbusMessage.getOpcode(r);
        if (CbusOpCodes.isShortEvent(opc)) {
            // Skip node number for short events
            if (aFrame[0]!=r.getElement(0)) return false;
            for (int i = 3; i<aFrame.length; i++) {
                if (aFrame[i]!=r.getElement(i)) return false;
            }
        } else {
            for (int i = 0; i<aFrame.length; i++) {
                if (aFrame[i]!=r.getElement(i)) return false;
            }
        }
        return true;
    }
    
    /**
     * Split a string containing one or more addresses
     * into individual ones.
     * @return null if entire string can't be parsed.
     */
    public CbusAddress[] split() {  
        // reject strings ending in ";"
        if (aString.endsWith(";")) return null;
        
        // split string at ";" points
        String[] pStrings = aString.split(";");

        CbusAddress[] retval = new CbusAddress[pStrings.length];

        for (int i = 0; i<pStrings.length; i++) {
            // check validity of each
            if (pStrings[i].equals("")) return null;
            if (!hCode.reset(pStrings[i]).matches()) return null;
            
            retval[i] = new CbusAddress(pStrings[i]);
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

    static Logger log = LoggerFactory.getLogger(CbusAddress.class.getName());

}


/* @(#)CbusAddress.java */
