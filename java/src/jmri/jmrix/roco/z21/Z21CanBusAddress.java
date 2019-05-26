package jmri.jmrix.roco.z21;

import jmri.Manager.NameValidity;
import jmri.ReporterManager;

/**
 * Utility Class supporting parsing and testing of addresses for Z21 CanBus  
 * <p>
 * One address format is supported for Reporters and Sensors: 
 * <ul>
 * <li>
 * Ztmm:pp where t is either R or S, mm is the module address and pp is the contact pin number (1-8).
 * </li>
 * </ul>
 *
 * @author Dave Duchamp, Copyright (C) 2004 - 2006
 * @author Bob Coleman Copyright (C) 2007, 2008, 2009
 * @author Egbert Broerse (C) 2017 Based on Acela example, modified for XpressNet.
 */
public class Z21CanBusAddress {

    public Z21CanBusAddress() {
    }

    /**
     * Public static method to parse a Z21CanBus system name.
     * Note: Bits are numbered from 1.
     *
     * @return the hardware address number, return -1 if an error is found
     */
    public static int getBitFromSystemName(String systemName, String prefix) {
        // validate the system Name leader characters
        if (!systemName.startsWith(prefix)) {
            // here if an invalid Z21 Can Bus system name
            log.error("invalid character in header field of Z21 Can Bus system name: {}", systemName);
            return (-1);
        }
        // name must be in the Ztmm:pp format (Z is user 
        // configurable)
        int num = 0;
        try {
            String curAddress = systemName.substring(prefix.length() + 1);
            if( ( systemName.charAt(prefix.length())=='R' ||
                  systemName.charAt(prefix.length())=='r' || 
                   systemName.charAt(prefix.length())=='S' ||
                  systemName.charAt(prefix.length())=='s' ) && 
                  curAddress.contains(":")) {
               //Address format passed is in the form of encoderAddress:input
               int encoderAddress;
               int seperator = curAddress.indexOf(":");
               try {
                  encoderAddress = Integer.parseInt(curAddress.substring(0, seperator));
               } catch (NumberFormatException ex) {
                   // didn't parse as a decimal, check to see if network ID 
                   // was used instead.
                   encoderAddress = Integer.parseInt(curAddress.substring(0,seperator),16);
               }
               log.debug("found module address {}",encoderAddress);
               int input = Integer.parseInt(curAddress.substring(seperator + 1));
               // since we aren't supporting bit number, just return the contact
               // since we know now the module address is valid.
               num = input;
            } else {
               log.warn("system name {} is in the wrong format.  Should be mm:pp.",systemName);
               return (-1);
            }
        } catch (NumberFormatException e) {
            log.warn("invalid character in number field of system name: {}", systemName);
            return (-1);
        }
        return (num);
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
            // not a valid system name for Z21 Can Bus
            return ("");
        }
        // check for a Reporter 
        if (systemName.charAt(prefix.length() + 1) == 'R') {
            jmri.Reporter r = null;
            r = jmri.InstanceManager.getDefault(ReporterManager.class).getBySystemName(systemName);
            if (r != null) {
                return r.getUserName();
            } else {
                return ("");
            }
        } 
        // check for a Sensor 
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Z21CanBusAddress.class);

}
