// SerialAddress.java

package jmri.jmrix.maple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting parsing and testing of addresses
 * <P>
 * Two address formats are supported:
 *   Ktxxxx 
 *      where:  t is the type code, 'T' for turnouts, 'S' for sensors, and
 *                      'L' for lights
 *              xxxx is a bit number of the input or output bit (001-9999)
 * Note: with Maple, all panels (nodes) have the same address space, so there is no
 *		node number in the address.
  * <P>
 * @author	Dave Duchamp, Copyright (C) 2004 - 2009
 * @version     $Revision$
 */
public class SerialAddress {

    public SerialAddress() {
    }
	    
    /**
     * Public static method to parse a system name and return the bit number
     *   Notes: Bits are numbered from 1.
     *          If an error is found, 0 is returned.
     */
    public static int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ( (systemName.charAt(0) != 'K') || ( (systemName.charAt(1) != 'L') &&
                (systemName.charAt(1) != 'S') && (systemName.charAt(1) != 'T') ) ) {
            // here if an illegal format 
            log.error("illegal character in header field of system name: "+systemName);
            return (0);
        }
        // bit number field begins at 2 - third character
		int num = 0;
		try {
			num = Integer.valueOf(systemName.substring(2)).intValue();
		}
		catch (Exception e) {
			log.error("illegal character in number field of system name: "+systemName);
			return (0);
		}
		if (num<=0) {
			log.error("invalid system name: "+systemName);
			return (0);
        }
        return (num);
    }

    /**
     * Public static method to validate system name format
     *   returns 'true' if system name has a valid format, else returns 'false'
     */
    public static boolean validSystemNameFormat(String systemName,char type) {
        // validate the system Name leader characters
        if ( (systemName.charAt(0) != 'K') || (systemName.charAt(1) != type) ) {
            // here if an illegal format 
            log.error("illegal character in header field of system name: "
                                                                +systemName);
            return (false);
        }

		// This is a KLxxxx (or KTxxxx or KSxxxx) address, make sure xxxx is OK 
		try {
		    // we're justing using this to check, and failure is interesting
			Integer.valueOf(systemName.substring(2)).intValue();
		}
		catch (Exception e) {
			log.error("illegal character in number field of system name: "
                                                    +systemName);
			return false;
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
            return false;
        }
        int bit = getBitFromSystemName(systemName);
        if ( ( type=='T' ) || (type=='L') ) {
            if ( ( bit <= 0 ) || ( bit > OutputBits.getNumOutputBits() ) ) {
                // The bit is not valid for this configuration
                return false;
            }
        }
        else if ( type=='S' ) {
            if ( ( bit <= 0 ) || ( bit > InputBits.getNumInputBits() ) ) {
                // The bit is not valid for this configuration
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
     * Public static method to normalize a system name
     * <P>
     * This routine is used to ensure that each system name is uniquely linked to
     *       a bit, by removing extra zeros inserted by the user.
     * <P>
     * If the supplied system name does not have a valid format, an empty string is 
     *      returned.  If the address in the system name is not within the legal 
	 *      maximum range for the type of item (L, T, or S), an empty string 
	 *		is returned. Otherwise a normalized name is returned in the 
     *      same format as the input name.
     */
    public static String normalizeSystemName(String systemName) {
        // ensure that input system name has a valid format
        if ( !validSystemNameFormat(systemName,systemName.charAt(1)) ) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }
		// check if bit number is within the valid range
		int bitNum = Integer.valueOf(systemName.substring(2)).intValue();
		if ( (bitNum<=0) || ((systemName.charAt(1)=='S') && bitNum>1000) || (bitNum>8000) ) {
			log.error ("node address field out of range in system name - "+systemName);
			return "";
		}
		// everything OK, normalize the address
        String nName = "";
		nName = systemName.substring(0,2)+bitNum;
        return nName;
    }
        
    /**
     * Public static method to construct a system name from type character 
	 *		and bit number
     * <P>
     * This routine returns a system name in the CLxxxx, CTxxxx, or CSxxxx
     *      format. The returned name is normalized.
     * <P>
     * If the supplied character is not valid, or the bit number is out of 
	 *		the 1 - 9000 range, an error message is
	 *      logged and the null string "" is returned.
	 */
    public static String makeSystemName(String type, int bitNum) {
		String nName = "";
		// check the type character
        if ( (!type.equals("S")) && (!type.equals("L")) && (!type.equals("T")) ) {
            // here if an illegal type character 
            log.error("illegal type character proposed for system name - "+type);
            return (nName);
        }
		// check the bit number
        if ( (bitNum < 1) || ((type.equals("S")) && (bitNum>1000)) || (bitNum > 8000) ) {
            // here if an illegal bit number 
            log.error("illegal address range proposed for system name - "+bitNum);
            return (nName);
        }
		// construct the address
		nName = "K"+type+Integer.toString(bitNum);	
		return (nName);
	}

    /**
     * Public static method to test if a output bit is free for assignment 
     *   Returns "" (null string) if the specified output bit is free for assignment,
     *      else returns the system name of the conflicting assignment.
	 *   Test is not performed if the node address or bit number are illegal.
     */
    public static String isOutputBitFree(int bitNum) {
		// check the bit number
        if ( (bitNum < 1) || (bitNum > 8000) ) {
            // here if an illegal bit number 
            log.error("illegal bit number in free bit test - "+bitNum);
            return ("");
        }
		
		// check for a turnout using the bit
		jmri.Turnout t = null;
		String	sysName = "";
		sysName = makeSystemName("T",bitNum);
		t = jmri.InstanceManager.turnoutManagerInstance().getBySystemName(sysName);
		if (t!=null) return (sysName);
		
		// check for a two-bit turnout assigned to the previous bit
		if (bitNum > 1) {
			sysName = makeSystemName("T",bitNum-1);
			t = jmri.InstanceManager.turnoutManagerInstance().getBySystemName(sysName);
			if (t!=null) {
				if (t.getNumberOutputBits() == 2) {
					// bit is second bit for this Turnout
					return (sysName);
				}
			}
		}
		
		// check for a light using the bit
		jmri.Light lgt = null;
		sysName = makeSystemName("L",bitNum);
		lgt = jmri.InstanceManager.lightManagerInstance().getBySystemName(sysName);
		if (lgt!=null) return (sysName);
		
		// not assigned to a turnout or a light
		return("");
	}

    /**
     * Public static method to test if a input bit is free for assignment 
     *   Returns "" (null string) if the specified input bit is free for assignment,
     *      else returns the system name of the conflicting assignment.
	 *   Test is not performed if the node address is illegal or bit number is greater
	 *      than 2048.
     */
    public static String isInputBitFree(int bitNum) {
		// check the bit number
        if ( (bitNum < 1) || (bitNum > 1000) ) {
            // here if an illegal bit number 
            log.error("illegal bit number in free bit test");
            return ("");
        }
		
		// check for a sensor using the bit
		jmri.Sensor s = null;
		String	sysName = "";
		sysName = makeSystemName("S",bitNum);
		s = jmri.InstanceManager.sensorManagerInstance().getBySystemName(sysName);
		if (s!=null) return (sysName);
		// not assigned to a sensor
		return("");
	}

    /**
     * Public static method to the user name for a valid system name 
     *   Returns "" (null string) if the system name is not valid or does not exist
     */
    public static String getUserNameFromSystemName(String systemName) {
		// check for a valid system name
		if ( (systemName.length() < 3) || (systemName.charAt(0) != 'K') ) {
			// not a valid system name
			return("");
		}
		// check for a sensor
		if (systemName.charAt(1) == 'S') {
			jmri.Sensor s = null;
			s = jmri.InstanceManager.sensorManagerInstance().getBySystemName(systemName);
			if (s!=null) {
				return s.getUserName();
			}
			else {
				return ("");
			}
		}
		// check for a turnout
		else if (systemName.charAt(1) == 'T') {
			jmri.Turnout t = null;
			t = jmri.InstanceManager.turnoutManagerInstance().getBySystemName(systemName);
			if (t!=null) {
				return t.getUserName();
			}
			else {
				return ("");
			}
		}
		// check for a light
		else if (systemName.charAt(1) == 'L') {
			jmri.Light lgt = null;
			lgt = jmri.InstanceManager.lightManagerInstance().getBySystemName(systemName);
			if (lgt!=null) {
				return lgt.getUserName();
			}
			else {
				return ("");
			}
		}
		
		// not any known sensor, light, or turnout
		return ("");
	}

    static Logger log = LoggerFactory.getLogger(SerialAddress.class.getName());
}

/* @(#)SerialAddress.java */
