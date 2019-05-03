package jmri.jmrix.tmcc;

import jmri.Manager.NameValidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting parsing and testing of addresses for TMCC.
 * <p>
 * Two address formats are supported: Ttnnnxxx where: t is the type code, 'T'
 * for turnouts, 'S' for sensors, and 'L' for lights nn is the node address
 * (0-127) xxx is a bit number of the input or output bit (001-999) nnxxx =
 * (node address x 1000) + bit number examples: CT2 (node address 0, bit 2),
 * CS1003 (node address 1, bit 3), CL11234 (node address 11, bit234) TtnnnBxxxx
 * where: t is the type code, 'T' for turnouts, 'S' for sensors, and 'L' for
 * lights nnn is the node address of the input or output bit (0-127) xxxx is a
 * bit number of the input or output bit (1-2048) examples: CT0B2 (node address
 * 0, bit 2), CS1B3 (node address 1, bit 3), CL11B234 (node address 11, bit234).
 *
 * @author	Dave Duchamp, Copyright (C) 2004
 * @author Bob Jacobsen, Copyright (C) 2006
 */
public class SerialAddress {

    public SerialAddress() {
    }

    /**
     * Public static method to parse a TMCC system name and return the Serial
     * Address.
     * <p>
     * Note: Accessory addresses are numbered from 0 - 511. The B-pattern is not
     * applicable to TMCC, was copied from NCE and better removed.
     *
     * @param systemName normal turnout name
     * @param prefix     system connection prefix from memo
     * @return node part 0-127, return '-1' if illegal systemName format or if
     *         the node is not found
     */
    public static int getNodeAddressFromSystemName(String systemName, String prefix) {
        int offset = prefix.length();
        // validate the system Name leader characters
        if (validSystemNameFormat(systemName, systemName.charAt(offset), prefix) != NameValidity.VALID) {
            // No point in trying if a valid system name format is not present
            return (-1);
        }
        String s = "";
        boolean noB = true;
        for (int i = offset + 1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(offset + 1, i);
                noB = false;
            }
        }
        int ua;
        if (noB) {
            // This is a TTnnxxx address
            int num = Integer.parseInt(systemName.substring(offset + 1));
            if (num > 0) {
                ua = num / 1000;
            } else {
                log.error("invalid TMCC system name: {}", systemName);
                return (-1);
            }
        } else {
            if (s.length() == 0) {
                log.error("no node address before 'B' in TMCC system name: {}", systemName);
                return (-1);
            } else {
                try {
                    ua = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    log.error("illegal character in TMCC system name: {}", systemName);
                    return (-1);
                }
            }
        }
        return (ua);
    }

    static final int MINTURNOUTADDRESS = 1;
    static final int MAXTURNOUTADDRESS = 511; // same for outputs

    /**
     * Public static method to parse a Lionel TMCC system name. Note: Bits are
     * numbered from 1.
     *
     * @param systemName name to parse
     * @param prefix     the connection prefix
     * @return the hardware address number, return 0 if an error is found
     */
    public static int getBitFromSystemName(String systemName, String prefix) {
        int offset = prefix.length();
        // validate the system Name leader characters
        if (!systemName.startsWith(prefix)) {
            // here if an invalid XpressNet system name
            log.error("invalid character in header field of system name: {}", systemName);
            return (0);
        }
        // Find the beginning of the bit number field
        int k = 0;
        for (int i = offset + 1; ((i < systemName.length()) && (k == 0)); i++) {
            if (systemName.charAt(i) == 'B') {
                k = i + 1;
            }
        }
        int n;
        if (k == 0) {
            // here if 'B' not found, name must be CLnnxxx format
            int num;
            try {
                num = Integer.parseInt(systemName.substring(offset + 1));
            } catch (NumberFormatException e) {
                log.warn("invalid character in number field of system name: {}", systemName);
                return (0);
            }
            if (num > 0) {
                n = num - ((num / 1000) * 1000);
            } else {
                log.warn("invalid system name: {}", systemName);
                return (0);
            }
        } else {
            // This is a CLnnBxxxx address
            try {
                n = Integer.parseInt(systemName.substring(k));
            } catch (NumberFormatException e) {
                log.warn("illegal character in bit number field system name: {}", systemName);
                return (0);
            }
        }
        return (n);
    }

    /**
     * Public static method to validate system name format. Logging of handled
     * cases no higher than WARN.
     *
     * @param systemName name to test
     * @param type       S, L, T for either sensor, light, turnout
     * @param prefix     system connection prefix from memo
     * @return VALID if system name has a valid format, else returns INVALID
     */
    public static NameValidity validSystemNameFormat(String systemName, char type, String prefix) {
        int offset = prefix.length();
        // validate the system Name leader characters
        if (!(systemName.startsWith(prefix + type))) {
            // here if an illegal format
            log.error("invalid character in header field of system name: {}", systemName);
            return NameValidity.INVALID;
        }
        // check for the presence of a 'B' to differentiate the two address formats
        // may also be entered as ":" but this is not supported in Lionel TMCC
        // so TODO remove this complexity and copy a simple check like XNet
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = offset + 1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(offset + 1, i);
                k = i + 1;
                noB = false;
            }
        }
        if (noB) {
            // This is a TTnnnxxx address
            int num;
            try {
                num = Integer.parseInt(systemName.substring(offset + 1));
            } catch (NumberFormatException e) {
                log.warn("invalid character in number field system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ((num < 1) || (num >= 128000)) {
                log.warn("number field out of range in system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ((num - ((num / 1000) * 1000)) == 0) {
                log.warn("bit number not in range 1 - 999 in system name: {}", systemName);
                return NameValidity.INVALID;
            }
        } else {
            // This is a TTnnnBxxxx address - validate the node address field
            if (s.length() == 0) {
                log.warn("no node address before 'B' in system name: {}", systemName);
                return NameValidity.INVALID;
            }
            int num;
            try {
                num = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                log.warn("invalid character in node address field of system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ((num < 0) || (num >= 128)) {
                log.warn("node address field out of range in system name: {}", systemName);
                return NameValidity.INVALID;
            }
            // validate the bit number field
            try {
                num = Integer.parseInt(systemName.substring(k));
            } catch (NumberFormatException e) {
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
     * @param systemName name to test
     * @param type       S, L, T sensor, light, turnout
     * @param prefix     system connection prefix from memo
     * @return true if valid name
     */
    public static boolean validSystemNameConfig(String systemName, char type, String prefix) {
        return validSystemNameFormat(systemName, type, prefix) == NameValidity.VALID;
    }

    /**
     * Public static method to convert one format system name for the alternate
     * format. If the supplied system name does not have a valid format, or if
     * there is no representation in the alternate naming scheme, an empty
     * string is returned. TODO remove this method. Not needed, as the B-format
     * is not supported on TMCC
     *
     * @param systemName name to convert
     * @param prefix     the connection prefix
     * @return alternate form if valid, empty if not
     */
    public static String convertSystemNameToAlternate(String systemName, String prefix) {
        int offset = prefix.length();
        // ensure that input system name has a valid format
        if (validSystemNameFormat(systemName, systemName.charAt(offset), prefix) != NameValidity.VALID) {
            // No point in trying if a valid system name format is not present
            return "";
        }
        String altName;
        // check for the presence of a 'B' to differentiate the two address formats
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = offset + 1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(offset + 1, i);
                k = i + 1;
                noB = false;
            }
        }
        if (noB) {
            // This is a TTnnnxxx address
            int num = Integer.parseInt(systemName.substring(offset + 1));
            int nAddress = num / 1000;
            int bitNum = num - (nAddress * 1000);
            altName = systemName.substring(0, offset + 1) + Integer.toString(nAddress) + "B"
                    + Integer.toString(bitNum);
        } else {
            // This is a TTnnnBxxxx address
            int nAddress = Integer.parseInt(s);
            int bitNum = Integer.parseInt(systemName.substring(k));
            if (bitNum > 999) {
                // bit number is out-of-range for a CLnnnxxx address
                return "";
            }
            altName = systemName.substring(0, offset + 1) + Integer.toString((nAddress * 1000) + bitNum);
        }
        return altName;
    }

    /**
     * Public static method to normalize a system name
     * <p>
     * This routine is used to ensure that each system name is uniquely linked
     * to one bit, by removing extra zeros inserted by the user.
     * <p>
     * If the supplied system name does not have a valid format, an empty string
     * is returned. Otherwise a normalized name is returned in the same format
     * as the input name.
     *
     * @param systemName name to convert
     * @param prefix     system connection prefix from memo
     * @return normalized form of systemName
     */
    public static String normalizeSystemName(String systemName, String prefix) {
        int offset = prefix.length();
        // ensure that input system name has a valid format
        if (validSystemNameFormat(systemName, systemName.charAt(offset), prefix) != NameValidity.VALID) {
            // No point in normalizing if a valid system name format is not present
            log.debug("System Name invalid");
            return "";
        }
        String nName;
        // check for the presence of a 'B' to differentiate the two address formats
        String s = "";
        int k = 0;
        boolean noB = true;
        for (int i = offset + 1; (i < systemName.length()) && noB; i++) {
            if (systemName.charAt(i) == 'B') {
                s = systemName.substring(offset + 1, i);
                k = i + 1;
                noB = false;
            }
        }
        if (noB) {
            // This is a TTnnnxxx address
            int num = Integer.parseInt(systemName.substring(offset + 1));
            int nAddress = num / 1000;
            int bitNum = num - (nAddress * 1000);
            nName = systemName.substring(0, offset + 1) + Integer.toString((nAddress * 1000) + bitNum);
        } else {
            // This is a TTnnnBxxxx address, this is not valid for TMCC, only numbers
            log.debug("B System Name");
            int nAddress = Integer.parseInt(s);
            int bitNum = Integer.parseInt(systemName.substring(k, systemName.length()));
            nName = systemName.substring(0, offset + 1) + Integer.toString(nAddress) + "B"
                    + Integer.toString(bitNum);
        }
        log.debug("ready normalized {} to {}", systemName, nName);
        return nName;
    }

    /**
     * Public static method to construct a TMCC system name from type character,
     * node address, and bit number.
     * <p>
     * This routine returns a system name in the CLnnnxxx, CTnnnxxx, or CSnnnxxx
     * format if the bit number is 1 - 999. If the bit number is 1000 - 2048,
     * the system name is returned in the CLnnnBxxxx, CTnnnBxxxx, or CSnnnBxxxx
     * format. The returned name is normalized.
     * <p>
     * If the supplied character is not valid, or the node address is out of the
     * 0 - 127 range, or the bit number is out of the 1 - 2048 range, an error
     * message is logged and the null string "" is returned.
     *
     * @param type     S, L, T for sensor, light, turnout (TMCC only supports
     *                 Turnouts)
     * @param nAddress node value 0-127
     * @param bitNum   bit within node 1-2048
     * @param prefix   system connection prefix from memo
     * @return formated system name or empty string when invalid
     */
    public static String makeSystemName(String type, int nAddress, int bitNum, String prefix) {
        String nName = "";
        // check the type character
        if ((!type.equals("S")) && (!type.equals("L")) && (!type.equals("T"))) {
            // here if an illegal type character
            log.error("illegal type character proposed for system name");
            return (nName);
        }
        // check the node address
        if ((nAddress < 0) || (nAddress > 127)) {
            // here if an illegal node address
            log.error("illegal node adddress proposed for system name");
            return (nName);
        }
        // check the bit number
        if ((bitNum < 1) || (bitNum > 2048)) {
            // here if an illegal bit number
            log.error("illegal bit number proposed for system name");
            return (nName);
        }
        // construct the address
        if (bitNum < 1000) {
            nName = prefix + type + Integer.toString((nAddress * 1000) + bitNum);
        } else {
            // must use other address format
            nName = prefix + type + Integer.toString(nAddress) + "B"
                    + Integer.toString(bitNum);
        }
        return (nName);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialAddress.class);

}
