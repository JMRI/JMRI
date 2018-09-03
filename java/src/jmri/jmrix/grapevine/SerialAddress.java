package jmri.jmrix.grapevine;

import jmri.Manager.NameValidity;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting parsing and testing of Grapevine addresses.
 * <p>
 * Multiple address formats are supported:
 * <ul>
 * <li>Gitnnnxxx where: Gi is the (multichar) system connection prefix,
 * t is the type code: 'T' for turnouts, 'S' for sensors, 'H' for signal
 * heads and 'L' for lights;
 * nnn is the node address (0-127); xxx is a bit number of the input or
 * output bit (001-999)</li>
 * <li>Gitnnnxxx = (node address x 1000) + bit number.<br>
 * Examples: GT2 (node address 0, bit 2), G1S1003 (node address 1, bit 3),
 * GL11234 (node address 11, bit234)</li>
 * <li>Gitnnnaxxxx where: t is the type code, 'T' for turnouts, 'S' for
 * sensors, 'H' for signal heads and 'L' for lights; nnn is the node address of the
 * input or output bit (0-127); xxxx is a bit number of the input or output bit
 * (1-2048); a is a subtype-specific letter:
 *  <ul>
 *  <li>'B' for a bit number (e.g. GT12B3 is a shorter form of GT12003)
 *  <li>'a' is for advanced serial occupancy sensors (only valid t = S)
 *  <li>'m' is for advanced serial motion sensors (only valid t = S)
 *  <li>'p' is for parallel sensors (only valid t = S)
 *  <li>'s' is for serial occupancy sensors (only valid t = S)
 *  </ul>
 * Examples: GT0B2 (node address 0, bit 2), G1S1B3 (node address 1, bit 3),
 * G22L11B234 (node address 11, bit 234)
 * </li>
 * </ul>
 *
 * @author Dave Duchamp, Copyright (C) 2004
 * @author Bob Jacobsen, Copyright (C) 2006, 2007, 2008
 */
public class SerialAddress {

    public SerialAddress() {
    }

    /**
     * Regular expression used to parse Turnout names.
     * <p>
     * Groups:
     * <ul>
     * <li> - System letter/prefix (excluded in regex since 4.11.3)
     * <li>1 - Type letter
     * <li>2 - suffix, if of nnnAnnn form
     * <li>3 - node number in nnnAnnn form
     * <li>4 - address type in nnnAnnn form
     * <li>5 - bit number in nnnAnnn form
     * <li>6 - combined number in nnnnnn form
     * </ul>
     */
    static final String turnoutRegex = "(T)(?:((\\d++)(B)(\\d++))|(\\d++))$";
    static volatile Pattern turnoutPattern = null;

    static Pattern getTurnoutPattern() {
        // defer compiling pattern until used, instead of at loading time
        if (turnoutPattern == null) {
            turnoutPattern = Pattern.compile(turnoutRegex);
        }
        return turnoutPattern;
    }

    /**
     * Regular expression used to parse Light names.
     * <p>
     * Groups:
     * <ul>
     * <li> - System letter/prefix (excluded in regex since 4.11.3)
     * <li>1 - Type letter
     * <li>2 - suffix, if of nnnAnnn form
     * <li>3 - node number in nnnAnnn form
     * <li>4 - address type in nnnAnnn form
     * <li>5 - bit number in nnnAnnn form
     * <li>6 - combined number in nnnnnn form
     * </ul>
     */
    static final String lightRegex = "(L)(?:((\\d++)(B)(\\d++))|(\\d++))$";
    static volatile Pattern lightPattern = null;

    static Pattern getLightPattern() {
        // defer compiling pattern until used, instead of at loading time
        if (lightPattern == null) {
            lightPattern = Pattern.compile(lightRegex);
        }
        return lightPattern;
    }

    /**
     * Regular expression used to parse SignalHead names.
     * <p>
     * Groups:
     * <ul>
     * <li> - System letter/prefix (excluded in regex since 4.11.3)
     * <li>1 - Type letter
     * <li>2 - suffix, if of nnnAnnn form
     * <li>3 - node number in nnnAnnn form
     * <li>4 - address type in nnnAnnn form
     * <li>5 - bit number in nnnAnnn form
     * <li>6 - combined number in nnnnnn form
     * </ul>
     */
    static final String headRegex = "(H)(?:((\\d++)(B)(\\d++))|(\\d++))$";
    static volatile Pattern headPattern = null;

    static Pattern getHeadPattern() {
        // defer compiling pattern until used, instead of at loading time
        if (headPattern == null) {
            headPattern = Pattern.compile(headRegex);
        }
        return headPattern;
    }

    /**
     * Regular expression used to parse Sensor names.
     * <p>
     * Groups:
     * <ul>
     * <li> - System letter/prefix (excluded in regex since 4.11.3)
     * <li>1 - Type letter
     * <li>2 - suffix, if of nnnAnnn form
     * <li>3 - node number in nnnAnnn form
     * <li>4 - address type in nnnAnnn form
     * <li>5 - bit number in nnnAnnn form
     * <li>6 - combined number in nnnnnn form
     * </ul>
     */
    static final String sensorRegex = "(S)(?:((\\d++)([BbAaMmPpSs])(\\d++))|(\\d++))$";
    static volatile Pattern sensorPattern = null;

    static Pattern getSensorPattern() {
        // defer compiling pattern until used, instead of at loading time
        if (sensorPattern == null) {
            sensorPattern = Pattern.compile(sensorRegex);
        }
        return sensorPattern;
    }

    /**
     * Regular expression used to parse from any type of name.
     * <p>
     * Groups:
     * <ul>
     * <li> - System letter/prefix (excluded in regex since 4.11.3)
     * <li>1 - Type letter
     * <li>2 - suffix, if of nnnAnnn form
     * <li>3 - node number in nnnAnnn form
     * <li>4 - address type in nnnAnnn form
     * <li>5 - bit number in nnnAnnn form
     * <li>6 - combined number in nnnnnn form
     * </ul>
     */
    static final String allRegex = "([SHLT])(?:((\\d++)([BbAaMmPpSs])(\\d++))|(\\d++))$";
    static volatile Pattern allPattern = null;

    static Pattern getAllPattern() {
        // defer compiling pattern until used, instead of at loading time
        if (allPattern == null) {
            allPattern = Pattern.compile(allRegex);
        }
        return allPattern;
    }

    /**
     * Parse for secondary letters.
     *
     * @return offset for type letter, or -1 if none
     */
    static int typeOffset(String type) {
        switch (type.toUpperCase().charAt(0)) {
            case 'B':
                return 0;
            case 'A':
                return SerialNode.offsetA;
            case 'M':
                return SerialNode.offsetM;
            case 'P':
                return SerialNode.offsetP;
            case 'S':
                return SerialNode.offsetS;
            default:
                return -1;
        }
    }

    /**
     * Public static method to parse a system name and return the Serial Node.
     *
     * @return 'NULL' if illegal systemName format or if the node is not found
     */
    public static SerialNode getNodeFromSystemName(String systemName, SerialTrafficController tc) {
        String prefix = tc.getSystemConnectionMemo().getSystemPrefix();
        // validate the System Name leader characters
        Matcher matcher = getAllPattern().matcher(systemName.substring(prefix.length())); // exclude multichar prefix
        if (!matcher.matches()) {
            // here if an illegal format 
            log.error("illegal system name format in getNodeFromSystemName: {}", systemName);
            return null;
        }

        // start decode
        int ua;
        if (matcher.group(6) != null) {
            // This is a Gitnnxxx address
            int num = Integer.valueOf(matcher.group(6)).intValue();
            if (num > 0) {
                ua = num / 1000;
            } else {
                log.error("invalid value in system name: {}", systemName);
                return null;
            }
        } else {
            ua = Integer.valueOf(matcher.group(3)).intValue();
        }
        return (SerialNode) tc.getNodeFromAddress(ua);
    }

    /**
     * Public static method to parse a system name and return the bit number.
     * Notes: Bits are numbered from 1.
     *
     * @return 0 if an error is found
     */
    public static int getBitFromSystemName(String systemName, String prefix) {
        // validate the System Name leader characters
        Matcher matcher = getAllPattern().matcher(systemName.substring(prefix.length())); // exclude multichar prefix
        if (!matcher.matches()) {
            // here if an illegal format 
            log.error("illegal system name format in getBitFromSystemName: {} prefix: {}", systemName, prefix, new Exception("traceback"));
            return 0;
        }

        // start decode
        int n = 0;
        if (matcher.group(6) != null) {
            // name in be Gitnnxxx format
            int num = Integer.valueOf(matcher.group(6)).intValue();
            if (num > 0) {
                n = num % 1000;
            } else {
                log.error("invalid value in system name: {}", systemName);
                return (0);
            }
        } else {
            // This is a Gitnnaxxxx address
            n = Integer.valueOf(matcher.group(5)).intValue();
        }
        return (n);
    }

    /**
     * Public static method to parse a system name to fetch the node number.
     * <p>
     * Note: Nodes are numbered from 1.
     *
     * @return node number. If an error is found, returns -1
     */
    public static int getNodeAddressFromSystemName(String systemName, String prefix) {
        // validate the System Name leader characters
        Matcher matcher = getAllPattern().matcher(systemName.substring(prefix.length())); // exclude multichar prefix
        if (!matcher.matches()) {
            // here if an illegal format 
            log.error("illegal system name format in getNodeAddressFromSystemName: {}", systemName);
            return (-1);
        }

        // start decode
        int ua;
        if (matcher.group(6) != null) {
            // This is a Gitnnxxx address
            int num = Integer.valueOf(matcher.group(6)).intValue();
            if (num > 0) {
                ua = num / 1000;
            } else {
                log.error("invalid value in system name: {}", systemName);
                return (-1);
            }
        } else {
            ua = Integer.valueOf(matcher.group(3)).intValue();
            log.debug("node ua: {}", ua);
        }
        return ua;
    }

    /**
     * Public static method to validate system name format.
     * Logging of handled cases no higher than WARN.
     *
     * @param systemName name to check
     * @param type       expected device type letter
     * @param prefix     system connection prefix from memo
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    public static NameValidity validSystemNameFormat(String systemName, char type, String prefix) {
        // validate the System Name leader characters
        Matcher matcher = getAllPattern().matcher(systemName.substring(prefix.length())); // exclude multichar prefix
        if (!matcher.matches()) {
            // here if an illegal format, e.g. another system letter
            // which happens all the time due to how proxy managers work
            return NameValidity.INVALID;
        }
        if (matcher.group(1).charAt(0) != type) { // notice we skipped the multichar prefix
            log.warn("type in {} does not match type {}", systemName, type);
            return NameValidity.INVALID;
        }
        Pattern p;
        if (type == 'L') {
            p = getLightPattern();
        } else if (type == 'T') {
            p = getTurnoutPattern();
        } else if (type == 'H') {
            p = getHeadPattern();
        } else if (type == 'S') {
            p = getSensorPattern();
        } else {
            log.error("cannot match type in {}, which is unexpected", systemName);
            return NameValidity.INVALID;
        }

        // check format
        Matcher m2 = p.matcher(systemName.substring(prefix.length()));
        if (!m2.matches()) {
            // here if cannot parse specifically (only accepts GxTnnn or GxTnnnB
            log.debug("invalid system name format: {} for type {}", systemName, type);
            return NameValidity.INVALID;
        }

        // check for the two different formats
        int node = -1;
        int bit = -1;
        if (matcher.group(6) != null) {
            // name in be Gitnnxxx format
            int num = Integer.valueOf(matcher.group(6)).intValue();
            if (num > 0) {
                node = num / 1000;
                bit = num % 1000;
            } else {
                log.debug("invalid value in system name: {}", systemName);
                return NameValidity.INVALID;
            }
        } else {
            // This is a Gitnnaxxxx address, get values
            node = Integer.valueOf(matcher.group(3)).intValue();
            bit = Integer.valueOf(matcher.group(5)).intValue();
        }

        // check values
        if ((node < 1) || (node > 127)) {
            log.debug("invalid node number {} in {}", node, systemName);
            return NameValidity.INVALID;
        }

        // check bit numbers
        if ((type == 'T') || (type == 'H') || (type == 'L')) {
            if (!((bit >= 101 && bit <= 124)
                    || (bit >= 201 && bit <= 224)
                    || (bit >= 301 && bit <= 324)
                    || (bit >= 401 && bit <= 424))) {
                log.debug("invalid bit number {} in {}", bit, systemName);
                return NameValidity.INVALID;
            }
        } else { 
            assert type == 'S'; // see earlier decoding
            // sort on subtype
            String subtype = matcher.group(4);
            if (subtype == null) { // no subtype, just look at total
                if ((bit < 1) || (bit > 224)) {
                    log.debug("invalid bit number {} in {}", bit, systemName);
                    return NameValidity.INVALID;
                } else {
                    return NameValidity.VALID;
                }
            }
            subtype = subtype.toUpperCase();
            if (subtype.equals("A")) { // advanced serial occ
                if ((bit < 1) || (bit > 24)) {
                    log.debug("invalid bit number {} in {}", bit, systemName);
                    return NameValidity.INVALID;
                }
            } else if (subtype.equals("M")) { // advanced serial motion
                if ((bit < 1) || (bit > 24)) {
                    log.debug("invalid bit number {} in  {}", bit, systemName);
                    return NameValidity.INVALID;
                }
            } else if (subtype.equals("S")) { // old serial
                if ((bit < 1) || (bit > 24)) {
                    log.debug("invalid bit number {} in {}", bit, systemName);
                    return NameValidity.INVALID;
                }
            } else if (subtype.equals("P")) { // parallel
                if ((bit < 1) || (bit > 96)) {
                    log.debug("invalid bit number {} in {}", bit, systemName);
                    return NameValidity.INVALID;
                }
            }
        }

        // finally, return VALID
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
            log.debug("invalid system name {}", systemName);
            return false;
        }
        SerialNode node = getNodeFromSystemName(systemName, tc);
        if (node == null) {
            log.warn("invalid system name {}; no such node", systemName);
            // The node indicated by this system address is not present
            return false;
        }
        int bit = getBitFromSystemName(systemName, prefix);
        if ((type == 'T') || (type == 'L')) {
            if ((bit <= 0) || (bit > SerialNode.outputBits[node.nodeType])) {
                // The bit is not valid for this defined Serial node
                log.warn("invalid system name {}; bad output bit number {} > {}",
                        systemName, bit, SerialNode.outputBits[node.nodeType]);
                return false;
            }
        } else if (type == 'S') {
            if ((bit <= 0) || (bit > SerialNode.inputBits[node.nodeType])) {
                // The bit is not valid for this defined Serial node
                log.warn("invalid system name {}; bad input bit number {} > {}",
                        systemName, bit, SerialNode.inputBits[node.nodeType]);
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
     * Public static method to convert any format system name for the alternate
     * format (nnBnn). If the supplied system name does not have a valid format,
     * or if there is no representation in the alternate naming scheme, an empty
     * string is returned.
     */
    public static String convertSystemNameToAlternate(String systemName, String prefix) {
        // ensure that input system name has a valid format
        if (validSystemNameFormat(systemName, systemName.charAt(prefix.length()), prefix) != NameValidity.VALID) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }

        Matcher matcher = getAllPattern().matcher(systemName.substring(prefix.length())); // exclude multichar prefix
        matcher.matches(); // known to work, just need values
        // check format
        if (matcher.group(6) != null) {
            int num = Integer.valueOf(matcher.group(6)).intValue();
            return prefix + matcher.group(1) + (num / 1000) + "B" + (num % 1000);
        } else {
            int node = Integer.valueOf(matcher.group(3)).intValue();
            int bit = Integer.valueOf(matcher.group(5)).intValue();
            return prefix + matcher.group(1) + node + "B" + bit;
        }
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
     */
    public static String normalizeSystemName(String systemName, String prefix) {
        // ensure that input system name has a valid format
        try {
           if (validSystemNameFormat(systemName, systemName.charAt(prefix.length()), prefix) != NameValidity.VALID) {
               // No point in normalizing if a valid system name format is not present
               return "";
           }

           Matcher matcher = getAllPattern().matcher(systemName.substring(prefix.length())); // exclude multichar prefix
           matcher.matches(); // known to work, just need values

           // check format
           if (matcher.group(6) != null) {
              int num = Integer.valueOf(matcher.group(6)).intValue();
              return prefix + matcher.group(1) + num;
           } else {
              // there are alternate forms...
              int offset = typeOffset(matcher.group(4));
              int node = Integer.valueOf(matcher.group(3)).intValue();
              int bit = Integer.valueOf(matcher.group(5)).intValue();
              return prefix + matcher.group(1) + (node * 1000 + bit + offset);
           }
       } catch(java.lang.StringIndexOutOfBoundsException sobe){
             throw new IllegalArgumentException("Invalid System Name Format: " + systemName);
       }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialAddress.class);

}
