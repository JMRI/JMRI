// SerialAddress.java

package jmri.jmrix.oaktree;

import org.apache.log4j.Logger;

/**
 * Utility Class supporting parsing and testing of addresses
 * <P>
 * Two address formats are supported:
 *   Otnnnxxx 
 *      where:  t is the type code, 'T' for turnouts, 'S' for sensors, and
 *                      'L' for lights
 *              nn is the node address (0-127)
 *              xxx is a bit number of the input or output bit (001-999)
 *              nnxxx = (node address x 1000) + bit number
 *      examples: CT2 (node address 0, bit 2), CS1003 (node address 1, bit 3), 
 *              CL11234 (node address 11, bit234)
 *   OtnnnBxxxx 
 *      where:  t is the type code, 'T' for turnouts, 'S' for sensors, and
 *                      'L' for lights
 *              nnn is the node address of the input or output bit (0-127)
 *              xxxx is a bit number of the input or output bit (1-2048)
 *      examples: CT0B2 (node address 0, bit 2), CS1B3 (node address 1, bit 3), 
 *              CL11B234 (node address 11, bit234)
 * <P>
 * @author	Dave Duchamp, Copyright (C) 2004
 * @author  Bob Jacobsen, Copyright (C) 2006
 * @version     $Revision$
 */
public class SerialAddress {

    public SerialAddress() {
    }

    /**
     * Public static method to parse a system name and return the Serial Node
     *  Note:  Returns 'NULL' if illegal systemName format or if the node is not found
     */
    public static SerialNode getNodeFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ( (systemName.charAt(0) != 'O') || ( (systemName.charAt(1) != 'L') &&
                (systemName.charAt(1) != 'S') && (systemName.charAt(1) != 'T') ) ) {
            // here if an illegal format 
            log.error("illegal character in header field of system name: "+systemName);
            return (null);
        }
        String s = "";
        boolean noB = true;
        for (int i = 2; (i<systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(2,i);
                noB = false;
            }
        }
        int ua;
        if (noB) {
            // This is a OLnnxxx address
            int num = Integer.valueOf(systemName.substring(2)).intValue();
            if (num>0) {
                ua = num/1000;
            }
            else {
                log.error("invalid system name: "+systemName);
                return (null);
            }
        }
        else {
            if (s.length()==0) {
                log.error("no node address before 'B' in system name: "+systemName);
                return (null);
            }
            else {
                try {
                    ua = Integer.parseInt(s);
                }
                catch (Exception e) {
                    log.error("illegal character in system name: "+systemName);
                    return (null);
                }
            }
        }
        return ((SerialNode) SerialTrafficController.instance().getNodeFromAddress(ua));
    }
    
    /**
     * Public static method to parse a system name and return the bit number
     *   Notes: Bits are numbered from 1.
     *          If an error is found, 0 is returned.
     */
    public static int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ( (systemName.charAt(0) != 'O') || ( (systemName.charAt(1) != 'L') &&
                (systemName.charAt(1) != 'S') && (systemName.charAt(1) != 'T') ) ) {
            // here if an illegal format 
            log.error("illegal character in header field of system name: "+systemName);
            return (0);
        }
        // Find the beginning of the bit number field
        int k = 0;
        for (int i = 2; ( (i<systemName.length()) && (k==0) ); i++) {
            if (systemName.charAt(i) == 'B') {
                k=i+1;
            }
        }
        int n = 0;
        if (k==0) {
            // here if 'B' not found, name must be CLnnxxx format
            int num;
            try {
                num = Integer.valueOf(systemName.substring(2)).intValue();
            }
            catch (Exception e) {
                log.error("illegal character in number field of system name: "+systemName);
                return (0);
            }
            if (num>0) {
                n = num - ((num/1000)*1000);
            }
            else {
                log.error("invalid system name: "+systemName);
                return (0);
            }
        }
        else {
            // This is a OLnnBxxxx address
            try {
                n = Integer.parseInt(systemName.substring(k,systemName.length()));
            }
            catch (Exception e) {
                log.error("illegal character in bit number field system name: "
                                                    +systemName);
                return (0);
            }
        }
        return (n);
    }

    /**
     * Public static method to validate system name format
     * @return 'true' if system name has a valid format, else returns 'false'
     * @param type Letter indicating device type expected
     */
    public static boolean validSystemNameFormat(String systemName,char type) {
        // validate the system Name leader characters
        if ( (systemName.charAt(0) != 'O') || (systemName.charAt(1) != type) ) {
            // here if an illegal format 
            log.error("illegal character in header field system name: "
                                                                +systemName);
            return (false);
        }
        // check for the presence of a 'B' to differentiate the two address formats
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = 2; (i<systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(2,i);
                k = i+1;
                noB = false;
            }
        }
        if (noB) {
            // This is a OLnnnxxx address
            int num;
            try {
                num = Integer.valueOf(systemName.substring(2)).intValue();
            }
            catch (Exception e) {
                log.error("illegal character in number field system name: "
                                                    +systemName);
                return (false);
            }
            if ( (num < 1) || (num >=256000) ) {
                log.error("number field out of range in system name: "
                                                    +systemName);
                return (false);
            }
            if ( (num - ((num/1000)*1000)) == 0) {
                log.error("bit number not in range 1 - 999 in system name: "
                                                    +systemName);
                return (false);
            }
        }
        else {
            // This is a OLnnnBxxxx address - validate the node address field
            if (s.length()==0) {
                log.error("no node address before 'B' in system name: "
                                                    +systemName);
                return (false);
            }
            int num;
            try {
                num = Integer.valueOf(s).intValue();
            }
            catch (Exception e) {
                log.error("illegal character in node address field of system name: "
                                                    +systemName);
                return (false);
            }
            if ( (num < 0) || (num >=128) ) {
                log.error("node address field out of range in system name: "
                                                    +systemName);
                return (false);
            }
            // validate the bit number field
            try {
                num = Integer.parseInt(systemName.substring(k,systemName.length()));
            }
            catch (Exception e) {
                log.error("illegal character in bit number field of system name: "
                                                    +systemName);
                return (false);
            }
            if ( (num < 1) || (num > 2048) ) {
                log.error("bit number field out of range in system name: "
                                                    +systemName);
                return (false);
            }
        }
        
        return true;
    }

    /**
     * Public static method to validate system name for configuration
     *   returns 'true' if system name has a valid meaning in current configuration, 
     *      else returns 'false'
     */
    public static boolean validSystemNameConfig(String systemName,char type) {
        if ( !validSystemNameFormat(systemName,type) ) {
            // No point in trying if a valid system name format is not present
            log.warn(systemName+" invalid; bad format");
            return false;
        }
        SerialNode node = getNodeFromSystemName(systemName);
        if ( node==null ) {
            log.warn(systemName+" invalid; no such node");
            // The node indicated by this system address is not present
            return false;
        }
        int bit = getBitFromSystemName(systemName);
        if ( ( type=='T' ) || (type=='L') ) {
            if ( ( bit <= 0 ) || ( bit > SerialNode.outputBytes[node.nodeType]*8 ) ) {
                // The bit is not valid for this defined Serial node
                log.warn(systemName+" invalid; bad bit number");
                return false;
            }
        }
        else if ( type=='S' ) {
            if ( ( bit <= 0 ) || ( bit > SerialNode.inputBytes[node.nodeType]*8 ) ) {
                // The bit is not valid for this defined Serial node
                log.warn(systemName+" invalid; bad bit number");
                return false;
            }
        }
        else {
            log.error("Invalid type specification in validSystemNameConfig call");
            return false;
        }
        // System name has passed all tests
        return true;
    }

    /**
     * Public static method to convert one format system name for the alternate
     *      format.
     * If the supplied system name does not have a valid format, or if there is
     *      no representation in the alternate naming scheme, an empty string is 
     *      returned.
     */
    public static String convertSystemNameToAlternate(String systemName) {
        // ensure that input system name has a valid format
        if ( !validSystemNameFormat(systemName,systemName.charAt(1)) ) {
            // No point in trying if a valid system name format is not present
            return "";
        }
        String altName = "";
        // check for the presence of a 'B' to differentiate the two address formats
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = 2; (i<systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(2,i);
                k = i+1;
                noB = false;
            }
        }
        if (noB) {
            // This is a OLnnnxxx address
            int num = Integer.valueOf(systemName.substring(2)).intValue();
            int nAddress = num/1000;
            int bitNum = num - (nAddress*1000);
            altName = systemName.substring(0,2)+Integer.toString(nAddress)+"B"+
                                                Integer.toString(bitNum);
        }
        else {
            // This is a OLnnnBxxxx address 
            int nAddress = Integer.valueOf(s).intValue();
            int bitNum = Integer.parseInt(systemName.substring(k,systemName.length()));
            if (bitNum>999) {
                // bit number is out-of-range for a CLnnnxxx address
                return "";
            }
            altName = systemName.substring(0,2)+Integer.toString((nAddress*1000)+bitNum);
        }        
        return altName;
    }
        
    /**
     * Public static method to normalize a system name
     * <P>
     * This routine is used to ensure that each system name is uniquely linked to
     *      one bit, by removing extra zeros inserted by the user.
     * <P>
     * If the supplied system name does not have a valid format, an empty string is 
     *      returned.  Otherwise a normalized name is returned in the same format
     *      as the input name.
     */
    public static String normalizeSystemName(String systemName) {
        // ensure that input system name has a valid format
        if ( !validSystemNameFormat(systemName,systemName.charAt(1)) ) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }
        String nName = "";
        // check for the presence of a 'B' to differentiate the two address formats
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = 2; (i<systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(2,i);
                k = i+1;
                noB = false;
            }
        }
        if (noB) {
            // This is a OLnnnxxx address
            int num = Integer.valueOf(systemName.substring(2)).intValue();
            int nAddress = num/1000;
            int bitNum = num - (nAddress*1000);
            nName = systemName.substring(0,2)+Integer.toString((nAddress*1000)+bitNum);
        }
        else {
            // This is a OLnnnBxxxx address 
            int nAddress = Integer.valueOf(s).intValue();
            int bitNum = Integer.parseInt(systemName.substring(k,systemName.length()));
            nName = systemName.substring(0,2)+Integer.toString(nAddress)+"B"+
                                                Integer.toString(bitNum);
        }        
        return nName;
    }

    static Logger log = Logger.getLogger(SerialAddress.class.getName());
}

/* @(#)SerialAddress.java */
