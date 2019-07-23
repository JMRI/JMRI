package jmri.jmrix.maple;

import java.util.Locale;
import jmri.Manager;
import jmri.Manager.NameValidity;
import jmri.NamedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting parsing and testing of Maple addresses.
 * <p>
 * One address format is supported: Ktxxxx where:
 * <ul>
 *   <li>K is (user configurable) system prefix for Maple</li>
 *   <li>t is the type code: 'T' for turnouts, 'S' for sensors,
 *   and 'L' for lights</li>
 *   <li>xxxx is a bit number of the input or output bit (001-9999)</li>
 * </ul>
 * Note: with Maple, all panels (nodes) have the
 * same address space, so there is no node number in the address.
 *
 * @author Dave Duchamp, Copyright (C) 2004 - 2009
 * @author Egbert Broerse, Copyright (C) 2017
 */
public class SerialAddress {

    public SerialAddress() {
    }

    /**
     * Public static method to parse a Maple system name and return the bit number.
     * Notes: Bits are numbered from 1.
     *
     * @return the bit number, return 0 if an error is found
     */
    public static int getBitFromSystemName(String systemName, String prefix) {
        if (prefix.length() < 1) {
            return 0;
        }
        log.debug("systemName = {}", systemName);
        log.debug("prefix = {}", prefix);
        // validate the system Name leader characters
        if (!(systemName.startsWith(prefix)) || ((systemName.charAt(prefix.length()) != 'L')
                && (systemName.charAt(prefix.length()) != 'S') && (systemName.charAt(prefix.length()) != 'T'))) {
            // here if an illegal format 
            log.debug("invalid character in header field of system name: {}", systemName);
            return (0);
        }
        // try to parse remaining system name part
        int num = 0;
        try {
            num = Integer.parseInt(systemName.substring(prefix.length() + 1)); // multi char prefix
        } catch (NumberFormatException ex) {
            log.warn("invalid character in number field of system name: {}", systemName);
            return (0);
        }
        if (num <= 0) {
            log.debug("invalid Maple system name: {}", systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Validate the system name.
     * 
     * @param name the name to validate
     * @param manager the manager requesting validation
     * @param locale the locale for user messages
     * @return the name; unchanged
     * @throws IllegalArgumentException if name is not valid
     * @see Manager#validateSystemNameFormat(java.lang.String, java.util.Locale)
     */
    public static String validateSystemNameFormat(String name, Manager<?> manager, Locale locale) throws IllegalArgumentException {
        int max = manager.typeLetter() == 'S' ? 1000 : 8000;
        return manager.validateIntegerSystemNameFormat(name, 0, max, locale);
    }

    /**
     * Public static method to validate system name format.
     *
     * @return 'true' if system name has a valid format,
     * else returns 'false'
     */
    public static NameValidity validSystemNameFormat(String systemName, char type, String prefix) {
        // validate the system Name leader characters
        if (!(systemName.startsWith(prefix)) || (systemName.charAt(prefix.length()) != type )) {
            // here if an illegal format
            log.error("invalid character in header field of system name: {}", systemName);
            return NameValidity.INVALID;
        }
        if (systemName.length() <= prefix.length() + 1) {
            log.warn("missing numerical node address in system name: {}", systemName);
            return NameValidity.INVALID;
        }
        // This is a KLxxxx (or KTxxxx or KSxxxx) address, make sure xxxx is OK
        int bit = getBitFromSystemName(systemName, prefix);
        // now check range
        if ((bit <= 0) || (type == 'S' && bit > 1000) || (bit > 8000)) {
            log.warn("node address field out of range in system name - {}", systemName);
            return NameValidity.INVALID;
        }
        return NameValidity.VALID;
    }

    /**
     * Public static method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current configuration,
     * else returns 'false'
     */
    public static boolean validSystemNameConfig(String systemName, char type, MapleSystemConnectionMemo memo) {
        if (validSystemNameFormat(systemName, type, memo.getSystemPrefix()) != NameValidity.VALID) {
            // No point in trying if a valid system name format is not present
            return false;
        }
        int bit = getBitFromSystemName(systemName, memo.getSystemPrefix());
        switch (type) {
            case 'T':
            case 'L':
                if ((bit > 0) && (bit <= OutputBits.getNumOutputBits())) {
                    // The bit is within valid range for this Maple configuration
                    return true;
                }
                break;
            case 'S':
                if ((bit > 0) && (bit <= InputBits.getNumInputBits())) {
                    // The bit is within valid range for this Maple configuration
                    return true;
                }
                break;
            default:
                log.error("Invalid type specification in validSystemNameConfig call");
                return false;
        }
        // System name has failed all tests
        log.warn("Maple hardware address out of range in system name: {}", systemName);
        return false;
    }

    /**
     * Public static method to normalize a system name.
     * <p>
     * This routine is used to ensure that each system name is uniquely linked
     * to a bit, by removing extra zeros inserted by the user.
     * It's not applied to sensors (whick might be addressed using the KS3:5 format.
     *
     * @return if the supplied system name does not have a valid format, an empty string
     * is returned. If the address in the system name is not within the legal
     * maximum range for the type of item (L, T, or S), an empty string is
     * returned. Otherwise a normalized name is returned in the same format as
     * the input name.
     */
    public static String normalizeSystemName(String systemName, String prefix) {
        if (prefix.length() < 1) {
            log.error("invalid system name prefix: {}", prefix);
            return "";
        }
        // ensure that input system name has a valid format
        // precheck startsWith(prefix) to pass jmri.managers.AbstractSensorMgrTestBase line 95/96 calling "foo" and "bar"
        if ((systemName.length() < prefix.length() + 1) || (!systemName.startsWith(prefix)) ||
                (validSystemNameFormat(systemName, systemName.charAt(prefix.length()), prefix) != NameValidity.VALID)) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }
        // check if bit number is within the valid range
        int bitNum = getBitFromSystemName(systemName, prefix);
        char type = systemName.charAt(prefix.length());
        if ((bitNum <= 0) || ((type == 'S') && bitNum > 1000) || (bitNum > 8000)) {
            log.warn("node address field out of range in system name - {}", systemName);
            return "";
        }
        // everything OK, normalize the address
        String nName = "";
        nName = prefix + type + bitNum;
        return nName;
    }

    /**
     * Public static method to construct a system name from type character and
     * bit number.
     * <p>
     * This routine returns a system name in the KLxxxx, KTxxxx, or KSxxxx
     * format. The returned name is normalized.
     *
     * @return "" (null string) if the supplied type character is not valid,
     * or the bit number is out of the 1 - 9000 range, and an error message is
     * logged.
     */
    public static String makeSystemName(String type, int bitNum, String prefix) {
        if (prefix.length() < 1) {
            log.error("invalid system name prefix: {}", prefix);
            return "";
        }
        String nName = "";
        // check the type character
        if ((!type.equals("S")) && (!type.equals("L")) && (!type.equals("T"))) {
            // here if an illegal type character 
            log.error("illegal type character proposed for system name - {}", type);
            return (nName);
        }
        // check the bit number
        if ((bitNum < 1) || ((type.equals("S")) && (bitNum > 1000)) || (bitNum > 8000)) {
            // here if an illegal bit number 
            log.warn("illegal address range proposed for system name - {}", bitNum);
            return (nName);
        }
        // construct the address
        nName = prefix + type + Integer.toString(bitNum);
        return (nName);
    }

    /**
     * Public static method to test if an output bit is free for assignment.
     *
     * @return "" (null string) if the specified output bit is free for
     * assignment, else returns the system name of the conflicting assignment.
     * Test is not performed if the node address or bit number are valid.
     */
    public static String isOutputBitFree(int bitNum, String prefix) {
        if (prefix.length() < 1) {
            log.error("invalid system name prefix: {}", prefix);
            return "";
        }
        // check the bit number
        if ((bitNum < 1) || (bitNum > 8000)) {
            // here if an illegal bit number 
            log.error("illegal bit number in free bit test - {}", bitNum);
            return ("");
        }
        // check for a turnout using the bit
        jmri.Turnout t = null;
        String sysName = "";
        sysName = makeSystemName("T", bitNum, prefix);
        t = jmri.InstanceManager.turnoutManagerInstance().getBySystemName(sysName);
        if (t != null) {
            return (sysName);
        }
        // check for a two-bit turnout assigned to the previous bit
        if (bitNum > 1) {
            sysName = makeSystemName("T", bitNum - 1, prefix);
            t = jmri.InstanceManager.turnoutManagerInstance().getBySystemName(sysName);
            if (t != null) {
                if (t.getNumberOutputBits() == 2) {
                    // bit is second bit for this Turnout
                    return (sysName);
                }
            }
        }
        // check for a light using the bit
        jmri.Light lgt = null;
        sysName = makeSystemName("L", bitNum, prefix);
        lgt = jmri.InstanceManager.lightManagerInstance().getBySystemName(sysName);
        if (lgt != null) {
            return (sysName);
        }
        // not assigned to a turnout or a light
        return ("");
    }

    /**
     * Public static method to test if an input bit is free for assignment.
     *
     * @return "" (null string) if the specified input bit is free for
     * assignment, else returns the system name of the conflicting assignment.
     * Test is not performed if the node address is illegal or bit number is
     * valid.
     */
    public static String isInputBitFree(int bitNum, String prefix) {
        if (prefix.length() < 1) {
            log.error("invalid system name prefix: {}", prefix);
            return "";
        }
        // check the bit number
        if ((bitNum < 1) || (bitNum > 1000)) {
            // here if an illegal bit number 
            log.error("illegal bit number in free bit test");
            return ("");
        }
        // check for a sensor using the bit
        jmri.Sensor s = null;
        String sysName = "";
        sysName = makeSystemName("S", bitNum, prefix);
        s = jmri.InstanceManager.sensorManagerInstance().getBySystemName(sysName);
        if (s != null) {
            return (sysName);
        }
        // not assigned to a sensor
        return ("");
    }

    /**
     * Public static method to get the user name for a valid system name.
     *
     * @return "" (null string) if the system name is not valid or does not exist
     */
    public static String getUserNameFromSystemName(String systemName, String prefix) {
        if (prefix.length() < 1) {
            log.error("invalid system name prefix: {}", prefix);
            return "";
        }
        // check for a valid system name
        if ((systemName.length() < (prefix.length() + 2)) || (!systemName.startsWith(prefix))) { // use multi char prefix
            // not a valid system name
            return ("");
        }
        // check for a sensor
        if (systemName.charAt(prefix.length()) == 'S') {
            jmri.Sensor s = null;
            s = jmri.InstanceManager.sensorManagerInstance().getBySystemName(systemName);
            if (s != null) {
                return s.getUserName();
            } else {
                return ("");
            }
        } // check for a turnout
        else if (systemName.charAt(prefix.length()) == 'T') {
            jmri.Turnout t = null;
            t = jmri.InstanceManager.turnoutManagerInstance().getBySystemName(systemName);
            if (t != null) {
                return t.getUserName();
            } else {
                return ("");
            }
        } // check for a light
        else if (systemName.charAt(prefix.length()) == 'L') {
            jmri.Light lgt = null;
            lgt = jmri.InstanceManager.lightManagerInstance().getBySystemName(systemName);
            if (lgt != null) {
                return lgt.getUserName();
            } else {
                return ("");
            }
        }
        // not any known sensor, light, or turnout
        return ("");
    }

    private final static Logger log = LoggerFactory.getLogger(SerialAddress.class);

}
