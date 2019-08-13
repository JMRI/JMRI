package jmri.jmrix.acela;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.Manager.NameValidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting parsing and testing of addresses for Acela.
 * <p>
 * One address format is supported: Atxxxx where: t is the type code, 'T' for
 * turnouts, 'S' for sensors, and 'L' for lights xxxx is a bit number of the
 * input or output bit (0-16383) examples: AT2 (bit 2), AS1003 (bit 1003), AL134
 * (bit134).<p>
 * Note: Not fully supporting long system connection prefix yet
 *
 * @author Dave Duchamp, Copyright (C) 2004 - 2006
 * @author Bob Coleman Copyright (C) 2007, 2008, 2009 Based on CMRI serial
 * example, modified to establish Acela support.
 */
public class AcelaAddress {

    public AcelaAddress() {
    }
    
    static final int MINSENSORADDRESS = 0;
    static final int MAXSENSORADDRESS = AcelaNode.MAXSENSORBITS * AcelaNode.MAXNODE -1;
    static final int MINOUTPUTADDRESS = 0;
    static final int MAXOUTPUTADDRESS = AcelaNode.MAXOUTPUTBITS * AcelaNode.MAXNODE -1;

    /**
     * Public static method to parse an Acela system name and return the Acela
     * Node Address Note: Returns '-1' if illegal systemName format or if the
     * node is not found. Nodes are numbered from 0 - {@value AcelaNode#MAXNODE}.
     */
    public static int getNodeAddressFromSystemName(String systemName, AcelaSystemConnectionMemo memo) {
        // validate the system Name leader characters
        if (validSystemNameFormat(systemName, systemName.charAt(memo.getSystemPrefix().length()), memo.getSystemPrefix()) != NameValidity.VALID) {
            // No point in trying if a valid system name format is not present
            return (-1);
        }
        int num = getBitFromSystemName(systemName, memo.getSystemPrefix());
        if (num < 0) {
            log.error("invalid Acela system name: {}", systemName);
            return (-1);
        }
        // This is a ALnnxxx address
        int nodeaddress = -1;
        if (systemName.charAt(memo.getSystemPrefix().length()) == 'S') {
            // Acela has two address spaces: true == sensor address space; false == output address space
            nodeaddress = memo.getTrafficController().lookupAcelaNodeAddress(num, true);
        } else {
            // Acela has two address spaces: true == sensor address space; false == output address space
            nodeaddress = memo.getTrafficController().lookupAcelaNodeAddress(num, false);
        }
        return (nodeaddress);
    }

    /**
     * Public static method to parse an Acela system name.
     *
     * @return the Acela Node number, return 'null' if illegal systemName format or if the node is
     * not found
     */
    public static AcelaNode getNodeFromSystemName(String systemName, AcelaSystemConnectionMemo memo) {
        // get the node address
        int ua;

        ua = getNodeAddressFromSystemName(systemName, memo);
        if (ua == -1) {
            // error messages have already been issued by getNodeAddressFromSystemName
            return null;
        }

        AcelaNode tempnode;
        tempnode = (AcelaNode) (memo.getTrafficController().getNodeFromAddress(ua));

        return tempnode;
    }

    /**
     * Public static method to parse an Acela system name and return the bit number.
     * Note: Bits are numbered from 1.
     *
     * @return the bit number, return -1 if an error is found (0 is a valid bit?)
     */
    public static int getBitFromSystemName(String systemName, String prefix) {
        // validate the System Name leader characters
        if (!(systemName.startsWith(prefix)) || ((systemName.charAt(prefix.length()) != 'L')
                && (systemName.charAt(prefix.length()) != 'S') && (systemName.charAt(prefix.length()) != 'T')
                && (systemName.charAt(prefix.length()) != 'H'))) {
            // here if an invalid Acela format
            log.error("illegal character in header field of system name: {}", systemName);
            return (-1);
        }
        // try to parse remaining system name part
        int num = -1;
        try {
            num = Integer.parseInt(systemName.substring(prefix.length() + 1)); // multi char prefix
        } catch (NumberFormatException e) {
            log.warn("invalid character in number field of system name: {}", systemName);
            return (-1);
        }
        if (num < 0) {
            log.warn("invalid Acela system name: {}", systemName);
            return (-1);
        }
        return (num);
    }

    /**
     * Public static method to validate system name format.
     * Logging of handled cases no higher than WARN.
     *
     * @return 'true' if system name has a valid format, else return 'false'
     */
    public static NameValidity validSystemNameFormat(String systemName, char type, String prefix) {
        // validate the system Name leader characters
        if (!systemName.startsWith(prefix + type )) {
            // here if an illegal format 
            log.error("invalid character in header field of system name: {}", systemName);
            return NameValidity.INVALID;
        }
        int num;
        try {
            num = Integer.parseInt(systemName.substring(prefix.length() + 1));
        } catch (NumberFormatException e) {
            log.debug("invalid character in number field of system name: {}", systemName);
            return NameValidity.INVALID;
        }
        if (num >= 0) {
            // This is a ALnnxxx address
            return NameValidity.VALID;
        } else {
            log.debug("invalid Acela system name: {}", systemName);
            return NameValidity.INVALID;
        }
    }

    /**
     * Public static method to validate Acela system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current
     * configuration, else return 'false'
     */
    @SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES", justification="additional check for valid bit value")
    public static boolean validSystemNameConfig(String systemName, char type, AcelaSystemConnectionMemo memo) {
        if (validSystemNameFormat(systemName, type, memo.getSystemPrefix()) != NameValidity.VALID) {
            // No point in trying if a valid system name format is not present
            return false;
        }
        AcelaNode node = getNodeFromSystemName(systemName, memo);
        if (node == null) {
            // The node indicated by this system address is not present
            return false;
        }
        int bit = getBitFromSystemName(systemName, memo.getSystemPrefix());
        switch (type) {
            case 'T':
            case 'L':
                if ((bit >= MINOUTPUTADDRESS) && (bit <= MAXOUTPUTADDRESS)) {
                    // The bit is within valid range for this defined Acela node
                    return true;
                }
                break;
            case 'S':
                if ((bit >= MINSENSORADDRESS) && (bit <= MAXSENSORADDRESS)) {
                    // The bit is within valid range for this defined Acela node
                    return true;
                }
                break;
            default:
                log.error("Invalid type specification in validSystemNameConfig call");
                return false;
        }
        // System name has failed all tests
        log.warn("Acela hardware address out of range in system name: {}", systemName);
        return false;
    }

    public static boolean validSystemNameConfig(String systemName, AcelaSystemConnectionMemo memo) {
        char type = systemName.charAt(memo.getSystemPrefix().length());
        return validSystemNameConfig(systemName, type, memo);
    }

    /**
     * Public static method to convert one format Acela system name for the
     * alternate format.
     *
     * @return name (string) in alternate format, or empty string if the supplied
     * system name does not have a valid format, or if there is no representation
     * in the alternate naming scheme.
     */
    public static String convertSystemNameToAlternate(String systemName, String prefix) {
        // ensure that input system name has a valid format
        if (validSystemNameFormat(systemName, systemName.charAt(prefix.length()), prefix) != NameValidity.VALID) {
            // No point in trying if a valid system name format is not present
            return "";
        }
        String altName = "";
        altName = systemName;
        return altName;
    }

    /**
     * Public static method to normalize an Acela system name.
     * <p>
     * This routine is used to ensure that each system name is uniquely linked
     * to one Acela bit, by removing extra zeros inserted by the user.
     *
     * @return a normalized name is returned in the same format as the input name,
     * or an empty string if the supplied system name does not have a valid format.
     */
    public static String normalizeSystemName(String systemName, String prefix) {
        // ensure that input system name has a valid format
        if (validSystemNameFormat(systemName, systemName.charAt(prefix.length()), prefix) != NameValidity.VALID) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }
        // check if bit number is within the valid range
        int bitNum = getBitFromSystemName(systemName, prefix);
        char type = systemName.charAt(prefix.length());
        if (bitNum < 0) {
            return "";
        }
        // everything OK, normalize the address
        String nName = "";
        nName = prefix + type + Integer.toString(bitNum);
        return nName;
    }

    /**
     * Public static method to construct an Acela system name from type
     * character, node address, and bit number
     *
     * @return a system name in the ALxxxx, ATxxxx, or ASxxxx
     * format. The returned name is normalized.
     * Return the null string "" if the supplied character is not valid,
     * or if the node address is out of the 0 - 127 range, or the bit number is
     * out of the 1 - 2048 range and an error message is logged.
     */
    public static String makeSystemName(String type, int nAddress, int bitNum, AcelaSystemConnectionMemo memo) {
        String nName = "";
        // check the type character
        if (!type.equalsIgnoreCase("S") && !type.equalsIgnoreCase("L") && !type.equalsIgnoreCase("T")) {
            // here if an illegal type character 
            log.error("invalid type character proposed for system name");
            return (nName);
        }
        // check the node address
        if ((nAddress < memo.getTrafficController().getMinimumNodeAddress()) || (nAddress > memo.getTrafficController().getMaximumNumberOfNodes())) {
            // here if an illegal node address 
            log.warn("invalid node adddress proposed for system name");
            return (nName);
        }
        // check the bit number
        if (type.equalsIgnoreCase("S") && ((bitNum < 0) || (bitNum > MAXSENSORADDRESS))) {
            // here if an illegal bit number 
            log.warn("invalid bit number proposed for Acela Sensor");
            return (nName);
        }
        if ((type.equalsIgnoreCase("L") || type.equalsIgnoreCase("T")) && ((bitNum < 0) || (bitNum > MAXOUTPUTADDRESS))) {
            // here if an illegal bit number 
            log.warn("invalid bit number proposed for Acela Turnout or Light");
            return (nName);
        }
        // construct the address
        nName = memo.getSystemPrefix() + type + Integer.toString(bitNum);
        return (nName);
    }

    /**
     * Public static method to check the user name for a valid system name.
     *
     * @return "" (null string) if the system name is not valid or does not exist
     */
    public static String getUserNameFromSystemName(String systemName, String prefix) {
        // check for a valid system name
        if ((systemName.length() < (prefix.length() + 2)) || (!systemName.startsWith(prefix))) {
            // not a valid system name for Acela
            return ("");
        }
        // check for a sensor
        if (systemName.charAt(prefix.length() + 1) == 'S') {
            jmri.Sensor s = null;
            s = jmri.InstanceManager.sensorManagerInstance().getBySystemName(systemName);
            if (s != null) {
                return s.getUserName();
            } else {
                return ("");
            }
        } // check for a turnout
        else if (systemName.charAt(prefix.length() + 1) == 'T') {
            jmri.Turnout t = null;
            t = jmri.InstanceManager.turnoutManagerInstance().getBySystemName(systemName);
            if (t != null) {
                return t.getUserName();
            } else {
                return ("");
            }
        } // check for a light
        else if (systemName.charAt(prefix.length() + 1) == 'L') {
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

    private final static Logger log = LoggerFactory.getLogger(AcelaAddress.class);

}
