package jmri.jmrix.lenz;

import jmri.Manager.NameValidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting parsing and testing of addresses for Lenz XpressNet
 * <p>
 * Two address format are supported: 
 * <ul>
 * <li> 
 * Xtxxxx where: t is the type code, 'T' for turnouts, 'S' for sensors, 
 * and 'L' for lights xxxx is a int for the hardware address (1-1024) 
 * examples: XT2 (address 2), XS1003 (address 1003), XL134 (address 134)
 * </li>
 * <li>
 * XSmm:pp where mm is the module address (1-128) and pp is the contact pin number (1-8).
 * </li>
 * </ul>
 *
 * @author Dave Duchamp, Copyright (C) 2004 - 2006
 * @author Bob Coleman Copyright (C) 2007, 2008, 2009
 * @author Egbert Broerse (C) 2017 Based on Acela example, modified for XpressNet.
 */
public class XNetAddress {

    public XNetAddress() {
    }

    static final int MINSENSORADDRESS = 1;
    static final int MAXSENSORADDRESS = 1024; // same for outputs

    /**
     * Public static method to parse a Lenz XpressNet system name.
     * Note: Bits are numbered from 1.
     *
     * @return the hardware address number, return -1 if an error is found
     */
    public static int getBitFromSystemName(String systemName, String prefix) {
        // validate the system Name leader characters
        if (!systemName.startsWith(prefix)) {
            // here if an invalid XpressNet system name
            log.error("invalid character in header field of XpressNet system name: {} wants prefix {}", 
                systemName, prefix);
            return (-1);
        }
        // name must be in the Xtnnnnn or XSmm:pp format (X is user 
        // configurable)
        int num = 0;
        try {
            String curAddress = systemName.substring(prefix.length() + 1);
            if( ( systemName.charAt(prefix.length())=='S' ||
                  systemName.charAt(prefix.length())=='s' ) && 
                  curAddress.contains(":")) {
               // Address format passed is in the form of encoderAddress:input or T:turnout address
               int seperator = curAddress.indexOf(":");
               int encoderAddress = Integer.parseInt(curAddress.substring(0, seperator));
               int input = Integer.parseInt(curAddress.substring(seperator + 1));
               num = ((encoderAddress - 1) * 8) + input;
            } else {
               num = Integer.parseInt(curAddress);
            }
        } catch (NumberFormatException e) {
            log.warn("invalid character in number field of system name: {}", systemName);
            return (-1);
        }
        if ((num >= MINSENSORADDRESS) && (num <= MAXSENSORADDRESS)) {
            return (num);
        }
        log.warn("XpressNet hardware address out of range in system name {}", systemName);
        return (-1);
    }

    /**
     * Public static method to validate system name format.
     * Logging of handled cases no higher than WARN.
     *
     * @return VALID if system name has a valid format, else return INVALID
     */
    public static NameValidity validSystemNameFormat(String systemName, char type, String prefix) {
        // validate the system Name leader characters
        if (!(systemName.startsWith(prefix + type))) {
            // here if an illegal format 
            log.error("invalid character in header field of system name: {} wants prefix {} type {}", 
                systemName, prefix, type);
            return NameValidity.INVALID;
        }
        if (getBitFromSystemName(systemName, prefix) > 0) {
            return NameValidity.VALID;
        } else {
            return NameValidity.INVALID;
        }
    }

    /**
     * Public static method to check the user name for a valid system name.
     *
     * @return "" (null string) if the system name is not valid or does not exist
     */
    public static String getUserNameFromSystemName(String systemName, String prefix) {
        // check for a valid system name
        if ((systemName.length() < (prefix.length() + 2)) || (!systemName.startsWith(prefix))) {
            // not a valid system name for XNet
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

    private final static Logger log = LoggerFactory.getLogger(XNetAddress.class);

}
