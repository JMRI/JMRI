package jmri.jmrix.oaktree;

import jmri.Manager.NameValidity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting parsing and testing of addresses.
 * <P>
 * Two address formats are supported: Otnnnxxx where: t is the type code, 'T'
 * for turnouts, 'S' for sensors, and 'L' for lights nn is the node address
 * (0-127) xxx is a bit number of the input or output bit (001-999) nnxxx =
 * (node address x 1000) + bit number examples: CT2 (node address 0, bit 2),
 * CS1003 (node address 1, bit 3), CL11234 (node address 11, bit234) OtnnnBxxxx
 * where: t is the type code, 'T' for turnouts, 'S' for sensors, and 'L' for
 * lights nnn is the node address of the input or output bit (0-127) xxxx is a
 * bit number of the input or output bit (1-2048) examples: CT0B2 (node address
 * 0, bit 2), CS1B3 (node address 1, bit 3), CL11B234 (node address 11, bit234)
 *
 * @author Dave Duchamp, Copyright (C) 2004
 * @author Bob Jacobsen, Copyright (C) 2006
 */
public class SerialAddress {

    OakTreeSystemConnectionMemo _memo = null;

    public SerialAddress(OakTreeSystemConnectionMemo memo) {
       _memo = memo;
    }

    /**
     * Public static method to parse a system name and return the Serial Node.
     *
     * @return 'NULL' if illegal systemName format or if the node is not found
     */
    public static SerialNode getNodeFromSystemName(String systemName, String prefix,SerialTrafficController tc) {
        if (prefix.length() < 1) {
            return null;
        }
        log.debug("systemName = {}", systemName);
        log.debug("prefix = {}", prefix);
        // validate the system Name leader characters
        if (!(systemName.startsWith(prefix)) || ((systemName.charAt(prefix.length()) != 'L')
                && (systemName.charAt(prefix.length()) != 'S') && (systemName.charAt(prefix.length()) != 'T'))) {
            // here if an illegal format 
            log.error("illegal character in header field of system name: {}", systemName);
            return (null);
        }
        String s = "";
        boolean noB = true;
        for (int i = prefix.length() + 1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(prefix.length() + 1, i);
                noB = false;
            }
        }
        int ua;
        if (noB) {
            // This is a OLnnxxx address
            // try to parse remaining system name part
            int num = 0;
            try {
                num = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue(); // multi char prefix
            } catch (NumberFormatException ex) {
                log.warn("invalid character in number field of system name: {}", systemName);
                return (null);
            }
            if (num > 0) {
                ua = num / 1000;
            } else {
                log.error("invalid system name: {}", systemName);
                return (null);
            }
        } else {
            if (s.length() == 0) {
                log.error("no node address before 'B' in system name: {}", systemName);
                return (null);
            } else {
                try {
                    ua = Integer.parseInt(s);
                } catch (Exception e) {
                    log.error("illegal character in system name: {}", systemName);
                    return (null);
                }
            }
        }
        return ((SerialNode) tc.getNodeFromAddress(ua));
    }

    /**
     * Public static method to parse a system name and return the bit number.
     * Note: Bits are numbered from 1.
     *
     * @return 0 if an error is found
     */
    public static int getBitFromSystemName(String systemName, String prefix) {
        // validate the system Name leader characters
        if (!(systemName.startsWith(prefix)) || ((systemName.charAt(prefix.length()) != 'L')
                && (systemName.charAt(prefix.length()) != 'S') && (systemName.charAt(prefix.length()) != 'T'))) {
            // here if an illegal format
            log.error("illegal character in header field of system name: {}", systemName);
            return (0);
        }
        // Find the beginning of the bit number field
        int k = 0;
        for (int i = prefix.length() + 1; ((i < systemName.length()) && (k == 0)); i++) {
            if (systemName.charAt(i) == 'B') {
                k = i + 1;
            }
        }
        int n = 0;
        if (k == 0) {
            // here if 'B' not found, name must be CLnnxxx format
            int num;
            try {
                num = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue();
            } catch (Exception e) {
                log.error("illegal character in number field of system name: {}", systemName);
                return (0);
            }
            if (num > 0) {
                n = num - ((num / 1000) * 1000);
            } else {
                log.error("invalid system name: {}", systemName);
                return (0);
            }
        } else {
            // This is a OLnnBxxxx address
            try {
                n = Integer.parseInt(systemName.substring(k, systemName.length()));
            } catch (Exception e) {
                log.error("illegal character in bit number field system name: {}", systemName);
                return (0);
            }
        }
        return (n);
    }

    /**
     * Public static method to validate system name format.
     *
     * @param type Letter indicating device type expected
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    public static NameValidity validSystemNameFormat(String systemName, char type, String prefix) {
        // validate the system Name leader characters
        if (!(systemName.startsWith(prefix)) || (systemName.charAt(prefix.length()) != type )) {
            // here if an illegal format 
            log.error("invalid character in header field system name: {}", systemName);
            return NameValidity.INVALID;
        }
        // check for the presence of a 'B' to differentiate the two address formats
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = prefix.length() + 1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(prefix.length() + 1, i);
                k = i + 1;
                noB = false;
            }
        }
        if (noB) {
            // This is a OLnnnxxx address
            int num;
            try {
                num = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue();
            } catch (Exception e) {
                log.warn("invalid character in number field system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ((num < 1) || (num >= 256000)) {
                log.warn("number field out of range in system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ((num - ((num / 1000) * 1000)) == 0) {
                log.warn("bit number not in range 1 - 999 in system name: {}", systemName);
                return NameValidity.INVALID;
            }
        } else {
            // This is a OLnnnBxxxx address - validate the node address field
            if (s.length() == 0) {
                log.warn("no node address before 'B' in system name: {}", systemName);
                return NameValidity.INVALID;
            }
            int num;
            try {
                num = Integer.valueOf(s).intValue();
            } catch (Exception e) {
                log.warn("invalid character in node address field of system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ((num < 0) || (num >= 128)) {
                log.warn("node address field out of range in system name: {}", systemName);
                return NameValidity.INVALID;
            }
            // validate the bit number field
            try {
                num = Integer.parseInt(systemName.substring(k, systemName.length()));
            } catch (Exception e) {
                log.warn("invalid character in bit number field of system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ((num < 1) || (num > 2048)) {
                log.warn("bit number field out of range in system name: {}", systemName);
                return NameValidity.INVALID;
            }
        }
        return NameValidity.VALID;
    }

    /**
     * Public static method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current configuration, else
     * return 'false'
     */
    public static boolean validSystemNameConfig(String systemName, char type, OakTreeSystemConnectionMemo memo) {
        if (validSystemNameFormat(systemName, type, memo.getSystemPrefix()) != NameValidity.VALID) {
            // No point in trying if a valid system name format is not present
            log.warn("invalid system name {}; bad format", systemName);
            return false;
        }
        SerialNode node = getNodeFromSystemName(systemName, memo.getSystemPrefix(),memo.getTrafficController());
        if (node == null) {
            log.warn("invalid system name {}; no such node", systemName);
            // The node indicated by this system address is not present
            return false;
        }
        int bit = getBitFromSystemName(systemName, memo.getSystemPrefix());
        if ((type == 'T') || (type == 'L')) {
            if ((bit <= 0) || (bit > SerialNode.outputBytes[node.nodeType] * 8)) {
                // The bit is not valid for this defined Serial node
                log.warn("invalid system name {}; bad bit number", systemName);
                return false;
            }
        } else if (type == 'S') {
            if ((bit <= 0) || (bit > SerialNode.inputBytes[node.nodeType] * 8)) {
                // The bit is not valid for this defined Serial node
                log.warn("invalid system name {}; bad bit number", systemName);
                return false;
            }
        } else {
            log.error("Invalid type specification in validSystemNameConfig call");
            return false;
        }
        // System name has passed all tests
        return true;
    }

    /**
     * Public static method to convert one format system name for the alternate
     * format.
     *
     * @return an empty string if the supplied system name does not have a valid
     * format, or if there is no representation in the alternate naming scheme
     */
    public static String convertSystemNameToAlternate(String systemName, String prefix) {
        // ensure that input system name has a valid format
        if (validSystemNameFormat(systemName, systemName.charAt(prefix.length()), prefix) != NameValidity.VALID) {
            // No point in trying if a valid system name format is not present
            return "";
        }
        String altName = "";
        // check for the presence of a 'B' to differentiate the two address formats
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = prefix.length() + 1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(prefix.length() + 1, i);
                k = i + 1;
                noB = false;
            }
        }
        if (noB) {
            // This is a OLnnnxxx address, convert to B-style
            int num = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue();
            int nAddress = num / 1000;
            int bitNum = num - (nAddress * 1000);
            altName = prefix + systemName.charAt(prefix.length()) + Integer.toString(nAddress) + "B"
                    + Integer.toString(bitNum);
        } else {
            // This is a OLnnnBxxxx address, convert to num-style
            int nAddress = Integer.valueOf(s).intValue();
            int bitNum = Integer.parseInt(systemName.substring(k, systemName.length()));
            if (bitNum > 999) {
                // bit number is out-of-range for a CLnnnxxx address
                return "";
            }
            altName = prefix + systemName.charAt(prefix.length()) + Integer.toString((nAddress * 1000) + bitNum);
        }
        return altName;
    }

    /**
     * Public static method to normalize a system name.
     * <P>
     * This routine is used to ensure that each system name is uniquely linked
     * to one bit, by removing extra zeros inserted by the user.
     *
     * @return an empty string if the supplied system name does not have a valid format.
     * Otherwise a normalized name is returned in the same format as the input name.
     */
    public static String normalizeSystemName(String systemName, String prefix) {
        if (prefix.length() < 1) {
            log.error("invalid system name prefix: {}", prefix);
            return "";
        }
        // ensure that input system name has a valid format
        if (validSystemNameFormat(systemName, systemName.charAt(prefix.length()), prefix) != NameValidity.VALID) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }
        String nName = "";
        // check for the presence of a 'B' to differentiate the two address formats
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = prefix.length() + 1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(prefix.length() + 1, i);
                k = i + 1;
                noB = false;
            }
        }
        char type = systemName.charAt(prefix.length());
        if (noB) {
            // This is a OLnnnxxx address
            int num = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue();
            int nAddress = num / 1000;
            int bitNum = num - (nAddress * 1000);
            nName = prefix + type + Integer.toString((nAddress * 1000) + bitNum);
        } else {
            // This is a OLnnnBxxxx address 
            int nAddress = Integer.valueOf(s).intValue();
            int bitNum = Integer.parseInt(systemName.substring(k, systemName.length()));
            nName = prefix + type + Integer.toString(nAddress) + "B"
                    + Integer.toString(bitNum);
        }
        return nName;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialAddress.class);

}
