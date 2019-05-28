package jmri.jmrix.roco.z21;

import jmri.Manager.NameValidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting parsing and testing of addresses for Z21 RMBus  
 * <p>
 * Two address format are supported: 
 * <ul>
 * <li> 
 * ZSxxxx where: 'S' for sensors, 
 * </li>
 * <li>
 * ZSmm:pp where mm is the module address (1-20) and pp is the contact pin number (1-8).
 * </li>
 * </ul>
 *
 * @author Dave Duchamp, Copyright (C) 2004 - 2006
 * @author Bob Coleman Copyright (C) 2007, 2008, 2009
 * @author Egbert Broerse (C) 2017 Based on Acela example, modified for XpressNet.
 */
public class Z21RMBusAddress {

    public Z21RMBusAddress() {
    }

    static final int MINSENSORADDRESS = 1;
    static final int MAXSENSORADDRESS = 160; // 20 RM bus modules with 8 contacts each.

    /**
     * Public static method to parse a Z21RMBus system name.
     * Note: Bits are numbered from 1.
     *
     * @return the hardware address number, return -1 if an error is found
     */
    public static int getBitFromSystemName(String systemName, String prefix) {
        // validate the system Name leader characters
        if (!systemName.startsWith(prefix)) {
            // here if an invalid Z21 RM Bus system name
            log.error("invalid character in header field of Z21 RM Bus system name: {}", systemName);
            return (-1);
        }
        // name must be in the ZSnnnnn or ZSmm:pp format (Z is user 
        // configurable)
        int num = 0;
        try {
            String curAddress = systemName.substring(prefix.length() + 1);
            if( ( systemName.charAt(prefix.length())=='S' ||
                  systemName.charAt(prefix.length())=='s' ) && 
                  curAddress.contains(":")) {
               //Address format passed is in the form of encoderAddress:input
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
        log.warn("Z21 RM Bus hardware address out of range in system name {}", systemName);
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
            log.error("invalid character in header field of system name: {}", systemName);
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
            // not a valid system name for Z21 RM Bus
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
        } 
        // not any known sensor
        return ("");
    }

    private final static Logger log = LoggerFactory.getLogger(Z21RMBusAddress.class);

}
