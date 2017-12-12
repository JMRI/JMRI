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
 * <li>Gtnnnxxx where: t is the type code,
 * 'T' for turnouts, 'S' for sensors, 'H' for signals and 'L' for lights; nn is
 * the node address (0-127); xxx is a bit number of the input or output bit
 * (001-999)</li>
 * <li>Gtnnxxx = (node address x 1000) + bit number.<br>Examples: GT2 (node
 * address 0, bit 2), GS1003 (node address 1, bit 3), GL11234 (node address 11,
 * bit234)</li>
 * <li>Gtnnnaxxxx where: t is the type code, 'T' for turnouts, 'S' for
 * sensors, 'H' for signals and 'L' for lights; nnn is the node address of the
 * input or output bit (0-127); xxxx is a bit number of the input or output bit
 * (1-2048); a is a subtype-specific letter: 'B' for a bit number (e.g. GT12B3 is
 * a shorter form of GT12003), 'a' is for advanced serial occupancy sensors, 'm'
 * is for advanced serial motion sensors, 'p' is for parallel sensors, 's' is for
 * serial occupancy sensors.<br>
 * Examples: GT0B2 (node address 0, bit 2), GS1B3 (node address 1, bit 3),
 * GL11B234 (node address 11, bit234)
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
     * <li>1 - System letter
     * <li>2 - Type letter
     * <li>3 - suffix, if of nnnAnnn form
     * <li>4 - node number in nnnAnnn form
     * <li>5 - address type in nnnAnnn form
     * <li>6 - bit number in nnnAnnn form
     * <li>7 - combined number in nnnnnn form
     * </ul>
     */
    static final String turnoutRegex = "^(G)(T)(?:((\\d++)(B)(\\d++))|(\\d++))$";
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
     * <li>1 - System letter
     * <li>2 - Type letter
     * <li>3 - suffix, if of nnnAnnn form
     * <li>4 - node number in nnnAnnn form
     * <li>5 - address type in nnnAnnn form
     * <li>6 - bit number in nnnAnnn form
     * <li>7 - combined number in nnnnnn form
     * </ul>
     */
    static final String lightRegex = "^(G)(L)(?:((\\d++)(B)(\\d++))|(\\d++))$";
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
     * <li>1 - System letter
     * <li>2 - Type letter
     * <li>3 - suffix, if of nnnAnnn form
     * <li>4 - node number in nnnAnnn form
     * <li>5 - address type in nnnAnnn form
     * <li>6 - bit number in nnnAnnn form
     * <li>7 - combined number in nnnnnn form
     * </ul>
     */
    static final String headRegex = "^(G)(H)(?:((\\d++)(B)(\\d++))|(\\d++))$";
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
     * <li>1 - System letter
     * <li>2 - Type letter
     * <li>3 - suffix, if of nnnAnnn form
     * <li>4 - node number in nnnAnnn form
     * <li>5 - address type in nnnAnnn form
     * <li>6 - bit number in nnnAnnn form
     * <li>7 - combined number in nnnnnn form
     * </ul>
     */
    static final String sensorRegex = "^(G)(S)(?:((\\d++)([BbAaMmPpSs])(\\d++))|(\\d++))$";
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
     * <li>1 - System letter
     * <li>2 - Type letter
     * <li>3 - suffix, if of nnnAnnn form
     * <li>4 - node number in nnnAnnn form
     * <li>5 - address type in nnnAnnn form
     * <li>6 - bit number in nnnAnnn form
     * <li>7 - combined number in nnnnnn form
     * </ul>
     */
    static final String allRegex = "^(G)([SHLT])(?:((\\d++)([BbAaMmPpSs])(\\d++))|(\\d++))$";
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
     * @return 'NULL' if illegal systemName format or if the node is not
     * found
     */
    public static SerialNode getNodeFromSystemName(String systemName,SerialTrafficController tc) {
        // validate the System Name leader characters
        Matcher matcher = getAllPattern().matcher(systemName);
        if (!matcher.matches()) {
            // here if an illegal format 
            log.error("illegal system name format: " + systemName);
            return (null);
        }

        // start decode
        int ua;
        if (matcher.group(7) != null) {
            // This is a Gtnnxxx address
            int num = Integer.valueOf(matcher.group(7)).intValue();
            if (num > 0) {
                ua = num / 1000;
            } else {
                log.error("invalid value in system name: " + systemName);
                return (null);
            }
        } else {
            ua = Integer.valueOf(matcher.group(4)).intValue();
        }
        return (SerialNode) tc.getNodeFromAddress(ua);
    }

    /**
     * Public static method to parse a system name and return the bit number.
     * Notes: Bits are numbered from 1. If an error is found, 0 is returned.
     */
    public static int getBitFromSystemName(String systemName) {
        // validate the System Name leader characters
        Matcher matcher = getAllPattern().matcher(systemName);
        if (!matcher.matches()) {
            // here if an illegal format 
            log.error("illegal system name format: {}", systemName);
            return 0;
        }

        // start decode
        int n = 0;
        if (matcher.group(7) != null) {
            // name in be Gtnnxxx format
            int num = Integer.valueOf(matcher.group(7)).intValue();
            if (num > 0) {
                n = num % 1000;
            } else {
                log.error("invalid value in system name: {}", systemName);
                return (0);
            }
        } else {
            // This is a Gtnnaxxxx address
            n = Integer.valueOf(matcher.group(6)).intValue();
        }
        return (n);
    }

    /**
     * Public static method to parse a system name and return the node number.
     * Notes: Nodes are numbered from 1. If an error is found, -1 is returned.
     */
    public static int getNodeAddressFromSystemName(String systemName) {
        // validate the System Name leader characters
        Matcher matcher = getAllPattern().matcher(systemName);
        if (!matcher.matches()) {
            // here if an illegal format 
            log.error("illegal system name format: {}", systemName);
            return (-1);
        }

        // start decode
        int ua;
        if (matcher.group(7) != null) {
            // This is a Gtnnxxx address
            int num = Integer.valueOf(matcher.group(7)).intValue();
            if (num > 0) {
                ua = num / 1000;
            } else {
                log.error("invalid value in system name: {}", systemName);
                return (-1);
            }
        } else {
            ua = Integer.valueOf(matcher.group(4)).intValue();
        }
        return ua;
    }

    /**
     * Public static method to validate system name format.
     *
     * @return 'true' if system name has a valid format, else returns 'false'
     * @param systemName name to check
     * @param type       expected device type letter
     */
    public static NameValidity validSystemNameFormat(String systemName, char type) {
        // validate the System Name leader characters
        Matcher matcher = getAllPattern().matcher(systemName);
        if (!matcher.matches()) {
            // here if an illegal format, e.g. another system letter
            // which happens all the time due to how proxy managers work
            return NameValidity.INVALID;
        }
        if (matcher.group(2).charAt(0) != type) {
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
        Matcher m2 = p.matcher(systemName);
        if (!m2.matches()) {
            // here if cannot parse specifically
            log.warn("invalid system name format: {} for type {}", systemName, type);
            return NameValidity.INVALID;
        }

        // check for the two different formats
        int node = -1;
        int bit = -1;
        if (matcher.group(7) != null) {
            // name in be Gtnnxxx format
            int num = Integer.valueOf(matcher.group(7)).intValue();
            if (num > 0) {
                node = num / 1000;
                bit = num % 1000;
            } else {
                log.warn("invalid value in system name: {}", systemName);
                return NameValidity.INVALID;
            }
        } else {
            // This is a Gtnnaxxxx address, get values
            node = Integer.valueOf(matcher.group(4)).intValue();
            bit = Integer.valueOf(matcher.group(6)).intValue();

        }

        // check values
        if ((node < 1) || (node > 127)) {
            log.warn("invalid node number {} in {}", node, systemName);
            return NameValidity.INVALID;
        }

        // check bit numbers
        if ((type == 'T') || (type == 'H') || (type == 'L')) {
            if (!((bit >= 101 && bit <= 124)
                    || (bit >= 201 && bit <= 224)
                    || (bit >= 301 && bit <= 324)
                    || (bit >= 401 && bit <= 424))) {
                log.warn("invalid bit number {} in {}", bit, systemName);
                return NameValidity.INVALID;
            }
        } else { 
            assert type == 'S'; // see earlier decoding
            // sort on subtype
            String subtype = matcher.group(5);
            if (subtype == null) { // no subtype, just look at total
                if ((bit < 1) || (bit > 224)) {
                    log.warn("invalid bit number {} in {}", bit, systemName);
                    return NameValidity.INVALID;
                } else {
                    return NameValidity.VALID;
                }
            }
            subtype = subtype.toUpperCase();
            if (subtype.equals("A")) {
                // advanced serial occ
                if ((bit < 1) || (bit > 24)) {
                    log.warn("invalid bit number {} in {}", bit, systemName);
                    return NameValidity.INVALID;
                }
            } else if (subtype.equals("M")) { 
                // advanced serial motion 
                if ((bit < 1) || (bit > 24)) {
                    log.warn("invalid bit number {} in  {}", bit, systemName);
                    return NameValidity.INVALID;
                }
            } else if (subtype.equals("S")) {// old serial
                if ((bit < 1) || (bit > 24)) {
                    log.warn("invalid bit number {} in {}", bit, systemName);
                    return NameValidity.INVALID;
                }
            } else if (subtype.equals("P")) { // parallel
                if ((bit < 1) || (bit > 96)) {
                    log.warn("invalid bit number {} in {}", bit, systemName);
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
    public static boolean validSystemNameConfig(String systemName, char type,SerialTrafficController tc) {
        if (validSystemNameFormat(systemName, type) != NameValidity.VALID) {
            // No point in trying if a valid system name format is not present
            log.warn("invalid system name {}", systemName);
            return false;
        }
        SerialNode node = getNodeFromSystemName(systemName,tc);
        if (node == null) {
            log.warn("invalid system name {}; no such node", systemName);
            // The node indicated by this system address is not present
            return false;
        }
        int bit = getBitFromSystemName(systemName);
        if ((type == 'T') || (type == 'L')) {
            if ((bit <= 0) || (bit > SerialNode.outputBits[node.nodeType])) {
                // The bit is not valid for this defined Serial node
                log.warn("invalid system name {}; bad output bit number {} > {}", systemName, bit, SerialNode.outputBits[node.nodeType]);
                return false;
            }
        } else if (type == 'S') {
            if ((bit <= 0) || (bit > SerialNode.inputBits[node.nodeType])) {
                // The bit is not valid for this defined Serial node
                log.warn("invalid system name {}; bad input bit number {} > {}", systemName, bit, SerialNode.inputBits[node.nodeType]);
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
    public static String convertSystemNameToAlternate(String systemName) {
        // ensure that input system name has a valid format
        if (validSystemNameFormat(systemName, systemName.charAt(1)) != NameValidity.VALID) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }

        Matcher matcher = getAllPattern().matcher(systemName);
        matcher.matches(); // known to work, just need values
        // check format
        if (matcher.group(7) != null) {
            int num = Integer.valueOf(matcher.group(7)).intValue();
            return matcher.group(1) + matcher.group(2) + (num / 1000) + "B" + (num % 1000);
        } else {
            int node = Integer.valueOf(matcher.group(4)).intValue();
            int bit = Integer.valueOf(matcher.group(6)).intValue();
            return matcher.group(1) + matcher.group(2) + node + "B" + bit;
        }
    }

    /**
     * Public static method to normalize a system name
     * <P>
     * This routine is used to ensure that each system name is uniquely linked
     * to one bit, by removing extra zeros inserted by the user.
     * <P>
     * If the supplied system name does not have a valid format, an empty string
     * is returned. Otherwise a normalized name is returned in the same format
     * as the input name.
     */
    public static String normalizeSystemName(String systemName) {
        // ensure that input system name has a valid format
        try {
           if (validSystemNameFormat(systemName, systemName.charAt(1)) != NameValidity.VALID) {
               // No point in normalizing if a valid system name format is not present
               return "";
           }

           Matcher matcher = getAllPattern().matcher(systemName);
           matcher.matches(); // known to work, just need values

           // check format
           if (matcher.group(7) != null) {
              int num = Integer.valueOf(matcher.group(7)).intValue();
              return matcher.group(1) + matcher.group(2) + num;
           } else {
              // there are alternate forms...
              int offset = typeOffset(matcher.group(5));
              int node = Integer.valueOf(matcher.group(4)).intValue();
              int bit = Integer.valueOf(matcher.group(6)).intValue();
              return matcher.group(1) + matcher.group(2) + (node * 1000 + bit + offset);
           }
       } catch(java.lang.StringIndexOutOfBoundsException sobe){
             throw new IllegalArgumentException("Invalid System Name Format: " + systemName);
       }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialAddress.class);

}
