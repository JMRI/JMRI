package jmri.jmrix.acela;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting parsing and testing of addresses for Acela
 * <P>
 * One address format is supported: Atxxxx where: t is the type code, 'T' for
 * turnouts, 'S' for sensors, and 'L' for lights xxxx is a bit number of the
 * input or output bit (0-1023) examples: AT2 (bit 2), AS1003 (bit 1003), AL134
 * (bit134)
 *
 * @author Dave Duchamp, Copyright (C) 2004 - 2006
 *
 * @author Bob Coleman Copyright (C) 2007, 2008, 2009 Based on CMRI serial
 * example, modified to establish Acela support.
 */
public class AcelaAddress {

    public AcelaAddress() {
    }

    static final int MINSENSORADDRESS = 0;
    static final int MAXSENSORADDRESS = 1023;   //  Artifical limit but OK until someone has
    //  more than 64 sensor modules (at 16 sensors each).
    static final int MINOUTPUTADDRESS = 0;
    static final int MAXOUTPUTADDRESS = 1023;   //  Artifical limit but OK until someone has
    //  more than 64 output modules (at 16 outputs each).

    /**
     * Public static method to parse a Acela system name and return the Acela
     * Node Address Note: Returns '-1' if illegal systemName format or if the
     * node is not found. Nodes are numbered from 0 - 127.
     */
    public static int getNodeAddressFromSystemName(String systemName, AcelaSystemConnectionMemo memo) {
        // validate the system Name leader characters
        if ((systemName.charAt(0) != 'A') || ((systemName.charAt(1) != 'L')
                && (systemName.charAt(1) != 'S') && (systemName.charAt(1) != 'T')
                && (systemName.charAt(1) != 'H'))) {
            // here if an illegal format 
            log.error("illegal character in header field of system name: " + systemName);
            return (-1);
        }
        int num;
        try {
            num = Integer.valueOf(systemName.substring(2)).intValue();
        } catch (Exception e) {
            log.error("illegal character in number field of system name: " + systemName);
            return (-1);
        }
        if (num >= 0) {
            // This is a CLnnxxx address
        } else {
            log.error("invalid Acela system name: " + systemName);
            return (-1);
        }
        int nodeaddress = -1;
        if (systemName.charAt(1) == 'S') {
            // Acela has two address spaces: true == sensor address space; false == output address space
            nodeaddress = memo.getTrafficController().lookupAcelaNodeAddress(num, true);
//            log.info("For this sensor, we want to use node: " + nodeaddress);

        } else {
            // Acela has two address spaces: true == sensor address space; false == output address space
            nodeaddress = memo.getTrafficController().lookupAcelaNodeAddress(num, false);
        }
        return (nodeaddress);
    }

    /**
     * Public static method to parse a Acela system name.
     *
     * @return the Acela Node number, return 'null' if illegal systemName format or if the node is
     * not found
     */
    public static AcelaNode getNodeFromSystemName(String systemName,AcelaSystemConnectionMemo memo) {
        // get the node address
        int ua;
        //int tempaddress;
//        log.info("Trying to register sensor "+ systemName );

        ua = getNodeAddressFromSystemName(systemName,memo);
        if (ua == -1) {
            // error messages have already been issued by getNodeAddressFromSystemName
            return null;
        }

        AcelaNode tempnode;
        tempnode = (AcelaNode) (memo.getTrafficController().getNodeFromAddress(ua));
        //tempaddress = tempnode.getNodeAddress();

//        log.info("Got back node of type (expecting 1,3, or 9): " + tempnode.nodeType + " for node: " + tempaddress);
        return tempnode;
        //        return (AcelaNode)(memo.getTrafficController().getNodeFromAddress(ua));
    }

    /**
     * Public static method to parse a Acela system name.
     * Note: Bits are numbered from 1.
     *
     * @return the bit number, return 0 if an error is found
     */
    public static int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((systemName.charAt(0) != 'A') || ((systemName.charAt(1) != 'L')
                && (systemName.charAt(1) != 'S') && (systemName.charAt(1) != 'T')
                && (systemName.charAt(1) != 'H'))) {
            // here if an illegal format 
            log.error("illegal character in header field of system name: " + systemName);
            return (-1);
        }
        int num;
        try {
            num = Integer.valueOf(systemName.substring(2)).intValue();
        } catch (Exception e) {
            log.error("illegal character in number field of system name: " + systemName);
            return (-1);
        }
        if (num >= 0) {
            // This is a CLnnxxx address
        } else {
            log.error("invalid Acela system name: " + systemName);
            return (-1);
        }
        return (num);
    }

    /**
     * Public static method to validate system name format
     *
     * @return 'true' if system name has a valid format, else return 'false'
     */
    public static boolean validSystemNameFormat(String systemName, char type, String prefix) {
        // validate the system Name leader characters
        if (!(systemName.startsWith(prefix)) || (systemName.charAt(prefix.length()) != type )) {
            // here if an illegal format 
            log.error("illegal character in header field of system name: " + systemName);
            return (false);
        }
        int num;
        try {
            num = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue();
        } catch (Exception e) {
            log.error("illegal character in number field of system name: " + systemName);
            return (false);
        }
        if (num >= 0) {
            // This is a CLnnxxx address
        } else {
            log.error("invalid Acela system name: " + systemName);
            return (false);
        }
        return true;
    }

    /**
     * Public static method to validate Acela system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current
     * configuration, else return 'false'
     */
    public static boolean validSystemNameConfig(String systemName, char type,AcelaSystemConnectionMemo memo) {
        if (!validSystemNameFormat(systemName, type, memo.getSystemPrefix() )) {
            // No point in trying if a valid system name format is not present
            return false;
        }
        AcelaNode node = getNodeFromSystemName(systemName,memo);
        if (node == null) {
            // The node indicated by this system address is not present
            return false;
        }
        int bit = getBitFromSystemName(systemName);
        if ((type == 'T') || (type == 'L')) {
            if ((bit < MINOUTPUTADDRESS) || (bit > MAXOUTPUTADDRESS)) {
                // The bit is not valid for this defined Acela node
                return false;
            }
        } else if (type == 'S') {
            if ((bit < MINSENSORADDRESS) || (bit > MAXSENSORADDRESS)) {
                // The bit is not valid for this defined Acela node
                return false;
            }
        } else {
            log.error("Invalid type specification in validSystemNameConfig call");
            return false;
        }
        // System name has passed all tests
        return true;
    }

    public static boolean validSystemNameConfig(String systemName, AcelaSystemConnectionMemo memo) {
        char type = systemName.charAt(1);
        if (!validSystemNameFormat(systemName, type, memo.getSystemPrefix() )) {
            // No point in trying if a valid system name format is not present
            return false;
        }
        AcelaNode node = getNodeFromSystemName(systemName,memo);
        if (node == null) {
            // The node indicated by this system address is not present
            return false;
        }
        int bit = getBitFromSystemName(systemName);
        if ((type == 'T') || (type == 'L')) {
            if ((bit < MINOUTPUTADDRESS) || (bit > MAXOUTPUTADDRESS)) {
                // The bit is not valid for this defined Acela node
                return false;
            }
        } else if (type == 'S') {
            if ((bit < MINSENSORADDRESS) || (bit > MAXSENSORADDRESS)) {
                // The bit is not valid for this defined Acela node
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
     * Public static method to convert one format Acela system name for the
     * alternate format.
     *
     * @return name (string) in alternate format, or empty string if the supplied
     * system name does not have a valid format, or if there is no representation
     * in the alternate naming scheme.
     */
    public static String convertSystemNameToAlternate(String systemName) {
        // ensure that input system name has a valid format
        if (!validSystemNameFormat(systemName, systemName.charAt(1),"A")) {
            // No point in trying if a valid system name format is not present
            return "";
        }
        String altName = "";
        altName = systemName;
        return altName;
    }

    /**
     * Public static method to normalize a Acela system name
     * <P>
     * This routine is used to ensure that each system name is uniquely linked
     * to one Acela bit, by removing extra zeros inserted by the user.
     *
     * @return a normalized name is returned in the same format as the input name,
     * or an empty string if the supplied system name does not have a valid format.
     *
     */
    public static String normalizeSystemName(String systemName) {
        // ensure that input system name has a valid format
        if (!validSystemNameFormat(systemName, systemName.charAt(1),"A")) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }
        int num;
        try {
            num = Integer.valueOf(systemName.substring(2)).intValue();
        } catch (Exception e) {
            log.error("illegal character in number field of system name: " + systemName);
            return "";
        }
        String nName = "";
        nName = systemName.substring(0, 2) + Integer.toString(num);
        return nName;
    }

    /**
     * Public static method to construct a Acela system name from type
     * character, node address, and bit number
     *
     * @return a system name in the CLxxxx, CTxxxx, or CSxxxx
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
            log.error("illegal type character proposed for system name");
            return (nName);
        }
        // check the node address
        if ((nAddress < memo.getTrafficController().getMinimumNodeAddress()) || (nAddress > memo.getTrafficController().getMaximumNumberOfNodes())) {
            // here if an illegal node address 
            log.error("illegal node adddress proposed for system name");
            return (nName);
        }
        // check the bit number
        if (type.equalsIgnoreCase("S") && ((bitNum < 0) || (bitNum > MAXSENSORADDRESS))) {
            // here if an illegal bit number 
            log.error("illegal bit number proposed for Acela Sensor");
            return (nName);
        }
        if ((type.equalsIgnoreCase("L") || type.equalsIgnoreCase("T")) && ((bitNum < 0) || (bitNum > MAXOUTPUTADDRESS))) {
            // here if an illegal bit number 
            log.error("illegal bit number proposed for Acela Turnout or Light");
            return (nName);
        }
        // construct the address
        nName = "A" + type + Integer.toString(bitNum);
        return (nName);
    }

    /**
     * Public static method to the user name for a valid system name.
     *
     * @return "" (null string) if the system name is not valid or does not exist
     */
    public static String getUserNameFromSystemName(String systemName) {
        // check for a valid system name
        if ((systemName.length() < 3) || (systemName.charAt(0) != 'A')) {
            // not a valid system name for Acela
            return ("");
        }
        // check for a sensor

        if (systemName.charAt(1) == 'S') {
            jmri.Sensor s = null;
            s = jmri.InstanceManager.sensorManagerInstance().getBySystemName(systemName);
            if (s != null) {
                return s.getUserName();
            } else {
                return ("");
            }
        } // check for a turnout
        else if (systemName.charAt(1) == 'T') {
            jmri.Turnout t = null;
            t = jmri.InstanceManager.turnoutManagerInstance().getBySystemName(systemName);
            if (t != null) {
                return t.getUserName();
            } else {
                return ("");
            }
        } // check for a light
        else if (systemName.charAt(1) == 'L') {
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

    private final static Logger log = LoggerFactory.getLogger(AcelaAddress.class.getName());

}
