// SerialAddress.java

package jmri.jmrix.powerline;

import java.util.regex.*;

/**
 * Utility Class supporting parsing and testing of addresses
 * <P>
 * Two address formats are supported:
 *   Gtxxx 
 *      where:  t is the type code, 'T' for turnouts, 'S' for sensors, and
 *                      'L' for lights
 *              xxx is a bit number of the input or output bit (1-256)
 *      examples: PT2 (House Code A, Unit 2), PS161 (House Code K, Unit 1)
 *   Gtnxx 
 *      where:  t is the type code, 'T' for turnouts, 'S' for sensors, and
 *                      'L' for lights
 *              n is the house code of the input or output bit (A - P)
 *              xx is a bit number of the input or output bit (1-16)
 *      examples: PTA2 (House Code A, Unit 2), PSK1 (House Code K, Unit 1)
 * <P>
 * @author	Dave Duchamp, Copyright (C) 2004
 * @author  Bob Jacobsen, Copyright (C) 2006, 2007, 2008
 * @author Ken Cameron, Copyright (C) 2008
 * @version     $Revision: 1.2 $
 */
public class SerialAddress {

	private static  Matcher hCodes = Pattern.compile("^P[LTS]([A-P])(\\d++)$").matcher("");
	private static	Matcher nCodes = Pattern.compile("^P[LTS](\\d++)$").matcher("");
	private static	Matcher aCodes = Pattern.compile("^P[LTS].*$").matcher("");
	private static	Character minHouseCode = 'A';
	private static	Character maxHouseCode = 'P';

    public SerialAddress() {
    }

    /**
     * Public static method to parse a system name and return the Serial Node
     *  Note:  Returns 'NULL' if illegal systemName format or if the node is not found
     */
    public static SerialNode getNodeFromSystemName(String systemName) {
        // validate the system Name leader characters
    	if ( !aCodes.reset(systemName).matches() ) {
            // here if an illegal format 
            log.error("illegal character in header field of system name: " + systemName);
            return (null);
        }
        int ua = -1;
        if (nCodes.reset(systemName).matches() && nCodes.groupCount() == 1) {
            // This is a PLxxx address
            int num = Integer.parseInt(nCodes.group(1)) - 1;
            if (num > 0) {
                ua = num / 16;
            } else {
                log.error("invalid system name: " + systemName);
                return (null);
            }
        }
        if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 2) {
        	String s = hCodes.group(1);
            if (s.length() == 0) {
                log.error("no house code in system name: " + systemName);
                return (null);
            }
            if (s.length() != 1) {
                log.error("house code too big in system name: " + systemName);
                return (null);
            }
            if ((int)s.charAt(0) >= (int)minHouseCode && (int) s.charAt(0) <= (int)maxHouseCode) {
                log.error("house code: " + s.charAt(0) + " invalid in system name: " + systemName);
            	return (null);
            }
            try {
                ua = (int)s.charAt(0) - (int)minHouseCode;
            }
            catch (Exception e) {
                log.error("illegal character in system name: " + systemName);
                return (null);
            }
        }
        if (ua < 0 || ua > 15) {
        	log.error("House Code out of range: " + systemName);
        }
        return (SerialTrafficController.instance().getNodeFromAddress(ua));
    }
    
    /**
     * Public static method to parse a system name and return the bit number
     *   Notes: Bits are numbered from 1.
     *          If an error is found, 0 is returned.
     */
    public static int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ( !aCodes.reset(systemName).matches() ) {
            // here if an illegal format 
            log.error("illegal character in header field of system name: "+systemName);
            return (0);
        }
        int num = -1;
        if (nCodes.reset(systemName).matches() && nCodes.groupCount() == 1) {
            // name must be PLnnn format
            try {
                num = (Integer.parseInt(nCodes.group(1)) - 1) & 0x0F;
            }
            catch (Exception e) {
                log.error("illegal character in unit number field of system name: " + systemName);
                return (0);
            }
        }
        if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 2){
            // This is a PLaxx address
            try {
                num = (Integer.parseInt(hCodes.group(2)) - 1) & 0x0F;
            }
            catch (Exception e) {
                log.error("illegal character in unit number field system name: " + systemName);
                return (0);
            }
        }
        if (num < 0 || num > 15) {
            log.error("invalid system name: " + systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Public static method to validate system name format
     * @return 'true' if system name has a valid format, else returns 'false'
     * @param type Letter indicating device type expected
     */
    public static boolean validSystemNameFormat(String systemName, char type) {
    	// validate the system Name leader characters
        if ( (!aCodes.reset(systemName).matches()) || (systemName.charAt(1) != type) ) {
            // here if an illegal format 
            log.error("illegal character in header field system name: " + systemName);
            return (false);
        }
        // Is this a PLnnn address
        if (nCodes.reset(systemName).matches() && nCodes.groupCount() == 1) {
            int num;
            try {
                num = Integer.parseInt(nCodes.group(1));
            }
            catch (Exception e) {
                log.error("illegal character in number field system name: " + systemName);
                return (false);
            }
            if ( (num < 1) || (num > 256) ) {
                log.error("number field out of range in system name: "
                                                    + systemName);
                return (false);
            }
            return(true);
        }
        if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 2) {
            // This is a PLaxx address - validate the house code and unit address fields
            if ((int)hCodes.group(1).charAt(0) < (int)minHouseCode || (int)hCodes.group(1).charAt(0) > (int)maxHouseCode) {
                log.error("house code field out of range in system name: "
                        + systemName);
                return (false);
            }
            int num;
            try {
                num = Integer.parseInt(hCodes.group(2)) - 1;
            }
            catch (Exception e) {
                log.error("illegal character in unit address field of system name: "
                                                    + systemName);
                return (false);
            }
            if ( (num < 0) || (num > 15) ) {
                log.error("unit address field out of range in system name: "
                                                    + systemName);
                return (false);
            }
            return (true);
        }
        log.error("address did not match any valid forms: " + systemName);
        return false;
    }

    /**
     * Public static method to validate system name for configuration
     *   returns 'true' if system name has a valid meaning in current configuration, 
     *      else returns 'false'
     */
    public static boolean validSystemNameConfig(String systemName, char type) {
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
            if ( ( bit <= 0 ) || ( bit > SerialNode.outputBits[node.nodeType] ) ) {
                // The bit is not valid for this defined Serial node
                log.warn(systemName+" invalid; bad bit number");
                return false;
            }
        }
        else if ( type=='S' ) {
            if ( ( bit <= 0 ) || ( bit > SerialNode.inputBits[node.nodeType] ) ) {
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
        if (nCodes.reset(systemName).matches() && nCodes.groupCount() == 1) {
            // This is a PLnnn address
            int num = Integer.parseInt(nCodes.group(1));
            char c = (char)((num / 16) + minHouseCode);
            altName = systemName.substring(0,2) + c + Integer.toString(num % 16);
        }
        if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 2) {
            // This is a PLann address 
            int house = (int)hCodes.group(1).charAt(0) - minHouseCode;
            int unit = Integer.parseInt(hCodes.group(2));
            altName = systemName.substring(0,2) + Integer.toString((house * 16) + unit);
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
        // check for the presence of a char to differentiate the two address formats
        if (nCodes.reset(systemName).matches() && nCodes.groupCount() == 1) {
            // This is a PLnnn address
            nName = systemName.substring(0,2) + Integer.toString(Integer.parseInt(nCodes.group(1)));
        }
        if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 1) {
            // This is a PLaxx address 
            nName = systemName.substring(0,3) + Integer.toString(Integer.parseInt(nCodes.group(2)));
        }        
        return nName;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialAddress.class.getName());
}

/* @(#)SerialAddress.java */
