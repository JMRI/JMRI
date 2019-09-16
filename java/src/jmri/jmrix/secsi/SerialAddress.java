package jmri.jmrix.secsi;

import java.util.Locale;
import jmri.Manager.NameValidity;
import jmri.NamedBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting parsing and testing of addresses
 * <p>
 * Two address formats are supported:
 * <ul>
 *   <li>Vtnnnxxx where:
 *      <ul>
 *      <li>V is the system connection prefix with optional index
 *      <li>t is the type code: 'T' for turnouts, 'S' for sensors,
 *      and 'L' for lights
 *      <li>nn is the node address (0-127)
 *      <li>xxx is a bit number of the input or
 *      <li>output bit (001-999) nnxxx = (node address x 1000) + bit number.
 *      </ul>
 *      Examples: VT2 (node address 0, bit 2), V2S1003 (node address 1,
 *      bit 3), VL11234 (node address 11, bit234)
 *   </li>
 *   <li>VtnnnBxxxx where:
 *      <ul>
 *      <li>V is the system connection prefix with optional index
 *      <li>t is the type code: 'T' for turnouts, 'S' for sensors,
 *      and 'L' for lights
 *      <li>nnn is the node address of the input or output bit (0-127)
 *      <li>xxxx is a bit number of the input or output bit (1-999).
 *      </ul>
 *      Examples: VT0B2 (node address 0, bit 2), VS1B3 (node address 1, bit 3),
 *      VL11B234 (node address 11, bit234)
 *   </li>
 * </ul>
 *
 * @author Dave Duchamp, Copyright (C) 2004
 * @author Bob Jacobsen, Copyright (C) 2006, 2007, 2008
 */
public class SerialAddress {

    public SerialAddress() {
    }

    /**
     * Parse a system name and return the Serial Node.
     *
     * @return 'NULL' if illegal systemName format or if the node is not
     * found
     */
    public static SerialNode getNodeFromSystemName(String systemName, SerialTrafficController tc) {
        String prefix = tc.getSystemConnectionMemo().getSystemPrefix();
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
            // This is a ViLnnxxx address
            int num = Integer.parseInt(systemName.substring(prefix.length() + 1));
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
        return (SerialNode) tc.getNodeFromAddress(ua);
    }

    /**
     * Parse a system name and return the bit number.
     * <p>
     * Note: Bits are numbered from 1.
     *
     * @return the bit number, 0 if an error occurred
     */
    public static int getBitFromSystemName(String systemName, String prefix) {
        // validate the system Name leader characters
        if (!(systemName.startsWith(prefix)) || ((systemName.charAt(prefix.length()) != 'L')
                && (systemName.charAt(prefix.length()) != 'S') && (systemName.charAt(prefix.length()) != 'T'))) {
            // here if an illegal format 
            log.error("invalid character in header field of system name: {}", systemName);
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
            // here if 'B' not found, name must be ViLnnxxx format
            int num;
            try {
                num = Integer.parseInt(systemName.substring(prefix.length() + 1));
            } catch (Exception e) {
                log.error("invalid character in number field of system name: {}", systemName);
                return (0);
            }
            if (num > 0) {
                n = num - ((num / 1000) * 1000);
            } else {
                log.error("invalid system name: " + systemName);
                return (0);
            }
        } else {
            // This is a ViLnnBxxxx address
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
     * Validate system name format. Does not check whether that node is defined
     * on current system.
     *
     * @param systemName the system name
     * @param prefix     the system connection prefix
     * @param locale     the Locale for user messages
     * @return systemName unmodified
     * @throws IllegalArgumentException if unable to validate systemName
     */
    public static String validateSystemNameFormat(String systemName, String prefix, Locale locale) throws IllegalArgumentException {
        if (!systemName.startsWith(prefix)) {
            throw new NamedBean.BadSystemNameException(
                    Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameInvalidPrefix", systemName),
                    Bundle.getMessage(locale, "InvalidSystemNameInvalidPrefix", systemName));
        }
        String address = systemName.substring(prefix.length());
        int node = 0;
        int bit = 0;
        if (!address.contains("B")) {
            // This is a CLnnnxxx pattern address
            int num;
            try {
                num = Integer.parseInt(address);
                node = num / 1000;
                bit = num - ((num / 1000) * 1000);
            } catch (NumberFormatException ex) {
                throw new NamedBean.BadSystemNameException(
                        Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameNotInteger", systemName, prefix),
                        Bundle.getMessage(locale, "InvalidSystemNameNotInteger", systemName, prefix));
            }
        } else {
            // This is a CLnBxxx pattern address
            String[] parts = address.split("B");
            if (parts.length != 2) {
                if (address.indexOf("B") == 0) {
                    // no node
                    throw new NamedBean.BadSystemNameException(
                            Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameNodeInvalid", systemName, ""),
                            Bundle.getMessage(locale, "InvalidSystemNameNodeInvalid", systemName, ""));
                } else {
                    // no bit
                    throw new NamedBean.BadSystemNameException(
                            Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameBitInvalid", systemName, ""),
                            Bundle.getMessage(locale, "InvalidSystemNameBitInvalid", systemName, ""));
                }
            }
            try {
                node = Integer.parseInt(parts[0]);
            } catch (NumberFormatException ex) {
                throw new NamedBean.BadSystemNameException(
                        Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameNodeInvalid", systemName, parts[0]),
                        Bundle.getMessage(locale, "InvalidSystemNameNodeInvalid", systemName, parts[0]));
            }
            try {
                bit = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                throw new NamedBean.BadSystemNameException(
                        Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameBitInvalid", systemName, parts[1]),
                        Bundle.getMessage(locale, "InvalidSystemNameBitInvalid", systemName, parts[1]));
            }
        }
        if (node < 0 || node >= 128) {
            throw new NamedBean.BadSystemNameException(
                    Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameNodeInvalid", systemName, node),
                    Bundle.getMessage(locale, "InvalidSystemNameNodeInvalid", systemName, node));
        }
        if (bit < 1 || bit > 32) {
            throw new NamedBean.BadSystemNameException(
                    Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameBitInvalid", systemName, bit),
                    Bundle.getMessage(locale, "InvalidSystemNameBitInvalid", systemName, bit));
        }
        return systemName;
    }

    /**
     * Public static method to validate system name format.
     * Logging of handled cases no higher than WARN.
     *
     * @param type Letter indicating device type expected
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    public static NameValidity validSystemNameFormat(String systemName, char type, String prefix) {
        // validate the system Name leader characters
        if (!(systemName.startsWith(prefix)) || (systemName.charAt(prefix.length()) != type )) {
            // here if an illegal format 
            log.error("illegal character in header field system name: {}", systemName);
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
            // This is a ViLnnnxxx address
            int num;
            try {
                num = Integer.parseInt(systemName.substring(prefix.length() + 1));
            } catch (Exception e) {
                log.debug("invalid character in number field system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ((num < 1) || (num >= 128000)) {
                log.debug("number field out of range in system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ((num - ((num / 1000) * 1000)) == 0) {
                log.debug("bit number not in range 1 - 999 in system name: {}", systemName);
                return NameValidity.INVALID;
            }
        } else {
            // This is a ViLnnnBxxxx address - validate the node address field
            if (s.length() == 0) {
                log.debug("no node address before 'B' in system name: {}", systemName);
                return NameValidity.INVALID;
            }
            int num;
            try {
                num = Integer.parseInt(s);
            } catch (Exception e) {
                log.debug("invalid character in node address field of system name: {}",
                        systemName);
                return NameValidity.INVALID;
            }
            if ((num < 0) || (num >= 128)) {
                log.debug("node address field out of range in system name: {}", systemName);
                return NameValidity.INVALID;
            }
            // validate the bit number field
            try {
                num = Integer.parseInt(systemName.substring(k, systemName.length()));
            } catch (Exception e) {
                log.debug("invalid character in bit number field of system name: {}",
                        systemName);
                return NameValidity.INVALID;
            }
            if ((num < 1) || (num > 32)) {
                log.debug("bit number field out of range in system name: {}", systemName);
                return NameValidity.INVALID;
            }
        }

        return NameValidity.VALID;
    }

    /**
     * Public static method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current configuration, else
     * returns 'false'
     */
    public static boolean validSystemNameConfig(String systemName, char type, SerialTrafficController tc) {
        String prefix = tc.getSystemConnectionMemo().getSystemPrefix();
        if (validSystemNameFormat(systemName, type, prefix) != NameValidity.VALID) {
            // No point in trying if a valid system name format is not present
            log.warn("{} invalid; bad format", systemName);
            return false;
        }
        SerialNode node = getNodeFromSystemName(systemName, tc);
        if (node == null) {
            log.warn("{} invalid; no such node", systemName);
            // The node indicated by this system address is not present
            return false;
        }
        int bit = getBitFromSystemName(systemName, prefix);
        if ((type == 'T') || (type == 'L')) {
            if ((bit <= 0) || (bit > SerialNode.outputBits[node.nodeType])) {
                // The bit is not valid for this defined Serial node
                log.warn("{} invalid; bad bit number", systemName);
                return false;
            }
        } else if (type == 'S') {
            if ((bit <= 0) || (bit > SerialNode.inputBits[node.nodeType])) {
                // The bit is not valid for this defined Serial node
                log.warn("{} invalid; bad bit number", systemName);
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
            // This is a ViLnnnxxx address, convert to num-style
            int num = Integer.parseInt(systemName.substring(prefix.length() + 1));
            int nAddress = num / 1000;
            int bitNum = num - (nAddress * 1000);
            altName = prefix + systemName.charAt(prefix.length()) + Integer.toString(nAddress) + "B"
                    + Integer.toString(bitNum);
        } else {
            // This is a VLnnnBxxxx address 
            int nAddress = Integer.parseInt(s);
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
     * Normalize a system name.
     * <p>
     * This routine is used to ensure that each system name is uniquely linked
     * to one bit, by removing extra zeros inserted by the user.
     *
     * @return an empty string if the supplied system name does not have a valid
     * format. Otherwise a normalized name is returned in the same format
     * as the input name.
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
            // This is a ViLnnnxxx address
            int num = Integer.parseInt(systemName.substring(prefix.length() + 1));
            int nAddress = num / 1000;
            int bitNum = num - (nAddress * 1000);
            nName = prefix + type + Integer.toString((nAddress * 1000) + bitNum);
        } else {
            // This is a ViLnnnBxxxx address
            int nAddress = Integer.parseInt(s);
            int bitNum = Integer.parseInt(systemName.substring(k, systemName.length()));
            nName = prefix + type + Integer.toString(nAddress) + "B"
                    + Integer.toString(bitNum);
        }
        return nName;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialAddress.class);

}
