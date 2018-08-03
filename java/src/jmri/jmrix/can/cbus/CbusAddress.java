package jmri.jmrix.can.cbus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * but with leading + on split events
     */
    public CbusAddress(String s) {
        aString = s;
        // log.debug("58 aString  is {}  ", aString);
        // now parse
        match = hCode.reset(aString).matches();
        if (match) {
            if (hCode.group(1) != null) {
                // log.debug("CbusAddress hCode group1 hit on +/-ddd");
                aFrame = new int[5];

                int n = Integer.parseInt(aString.substring(1, aString.length()));  // skip +/-
                int node = n / NODEFACTOR;
                int event = n % NODEFACTOR;

                // log.debug("CbusAddress n  is {} node is {} event is {} ", n, node, event);
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
                if (hCode.group(6) == null) {
                    aFrame[0] = CbusConstants.CBUS_ACON;
                } else if (hCode.group(6).equals("+")) {
                    aFrame[0] = CbusConstants.CBUS_ACON;
                } else if (hCode.group(6).equals("-")) {
                    aFrame[0] = CbusConstants.CBUS_ACOF;
                } else // default
                {
                    aFrame[0] = CbusConstants.CBUS_ACON;
                }
            }
        } else {
            // no match, leave match false and aFrame null
            // does not pick up +N123E456;-N123E456
            log.debug("129 no match for {} ", s );
            
            
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

    boolean match(CanReply r) {
        if (r.getNumDataElements() != aFrame.length) {
            return false;
        }
        int opc = CbusMessage.getOpcode(r);
        if (CbusOpCodes.isShortEvent(opc)) {
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

    boolean match(CanMessage r) {
        if (r.getNumDataElements() != aFrame.length) {
            return false;
        }
        int opc = CbusMessage.getOpcode(r);
        if (CbusOpCodes.isShortEvent(opc)) {
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
     * @return null if entire string can't be parsed.
     */
    public CbusAddress[] split() {
        // reject strings ending in ";"
        if (aString.endsWith(";")) {
            log.warn(" String ends with ; ");
            return null;
        }

        // split string at ";" points
        String[] pStrings = aString.split(";");

        CbusAddress[] retval = new CbusAddress[pStrings.length];

        for (int i = 0; i < pStrings.length; i++) {
            // check validity of each
            if (pStrings[i].equals("")) {
                return null;
            }
            if (!hCode.reset(pStrings[i]).matches()) {
                return null;
            }

            retval[i] = new CbusAddress(pStrings[i]);
            if (retval[i] == null) {
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

    
    
    public static String CbusPreParseEvent(String psn, String storl) {
        
        // allows users to enter common events without leading +
        // however saves them with the leading + so duplicates etc. can be spotted
        // log.debug("281 New Parsing start psn {} ", psn);
        // psn= hardware address to parse
        psn=psn.toUpperCase();
        psn=psn.substring(psn.lastIndexOf(storl) + 1); // strip the string to everything left of and including the T, leaving just the hardware
        
        // log.debug("286 after system + T check psn is {} ", psn);
        
        int unsigned = 0;
            try {
                unsigned = Integer.valueOf(psn); // on unsigned integer, will add "+" next
            } catch (NumberFormatException ex) {
                // already warned
            }
            if (unsigned > 0 && !psn.startsWith("+")) {
                psn = "+" + psn;
            }
        
        if ( psn.length() == 1 ) {
            psn= "+" + psn;
            log.error("300 SHOULD ALREADY HAVE BEEN DONE BY ABOVE");
        }
        
        log.warn("309 unsigned is {} ", unsigned);
        
        if (psn.contains(";") && (!psn.endsWith(";"))) {
            // log.debug ("304 New Parsing psn {} ", psn);
            
            String[] vstring = psn.split("\\;");        
            String partone = vstring[0];
            String parttwo = vstring[1];

            if (partone.startsWith("+")|partone.startsWith("-")|partone.startsWith("X")) {
                // partone =  new CbusAddress(partone);
            } else {
                partone = "+" + partone;
            }
            
            if (parttwo.startsWith("+")|parttwo.startsWith("-")|parttwo.startsWith("X")) {
                // parttwo =  new CbusAddress(parttwo);
            } else {
                parttwo = "+" + parttwo;
            }
            psn = partone + ";" + parttwo;
            // log.warn("104 new psn is {}  ", psn);
        }
        
        if ((!psn.contains(";")) && (psn.contains("E")) && (!psn.startsWith("+")) && (!psn.startsWith("-"))) {
            // supposed to convert inputs like N123E456 
            psn = "+" + psn;
        }
        
        

       // log.debug ("325 preParseEvent new psn {} ", psn);

        return psn;
    } 
    
    
    private static final Logger log = LoggerFactory.getLogger(CbusAddress.class);
    
}
