package jmri.jmrix.roco.z21;

import java.util.Locale;
import jmri.Manager;
import jmri.Manager.NameValidity;
import jmri.NamedBean;
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

    private Z21CanBusAddress() {
        // this is a class of static methods.
    }

    /**
     * Public static method to parse a Z21CanBus system name.
     * Note: Bits are numbered from 0.
     *
     * @return the hardware address number, return -1 if an error is found
     */
    public static int getBitFromSystemName(String systemName, String prefix) {
        // validate the system Name leader characters
        if(!systemNameStartsWithPrefix(systemName,prefix)) {
            return (-1);
        }
        // name must be in the Ztmm:pp format (Z is user 
        // configurable)
        try {
            String curAddress = systemName.substring(prefix.length() + 1);
            if( ( systemName.charAt(prefix.length())=='R' ||
                  systemName.charAt(prefix.length())=='r' || 
                   systemName.charAt(prefix.length())=='S' ||
                  systemName.charAt(prefix.length())=='s' ) && 
                  curAddress.contains(":")) {
               //Address format passed is in the form of encoderAddress:input
               int seperator = curAddress.indexOf(':');
               int encoderAddress = parseEncoderAddress(curAddress,0,seperator);
               log.debug("found module address {}",encoderAddress);
                // since we aren't supporting bit number, just return the contact
                // since we know now the module address is valid.
               return Integer.parseInt(curAddress.substring(seperator + 1));
            } else {
               log.warn("system name {} is in the wrong format.  Should be mm:pp.",systemName);
            }
        } catch (NumberFormatException e) {
            log.warn("invalid character in number field of system name: {}", systemName);
        }
        return (-1);
    }

    private static boolean systemNameStartsWithPrefix(String systemName,String prefix){
        if (!systemName.startsWith(prefix)) {
            // here if an invalid Z21 Can Bus system name
            log.error("invalid character in header field of Z21 Can Bus system name: {}", systemName);
            return false;
        }
        return true;
    } 

    private static int parseEncoderAddress(String addressWithoutPrefix,int start, int end) {
       int encoderAddress = -1;
       try {
          encoderAddress = Integer.parseInt(addressWithoutPrefix.substring(start,end));
       } catch (NumberFormatException ex) {
          // didn't parse as a decimal, check to see if network ID 
          // was used instead.
          encoderAddress = Integer.parseInt(addressWithoutPrefix.substring(start,end),16);
       }
       return encoderAddress;
    }

    public static String getEncoderAddressString(String systemName, String prefix) {

        // validate the system Name leader characters
        if (!systemNameStartsWithPrefix(systemName, prefix)) {
            throw new NamedBean.BadSystemNameException(
                    Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameInvalidPrefix", prefix),
                    Bundle.getMessage("InvalidSystemNameInvalidPrefix", prefix));

        }
        int seperator = systemName.indexOf(':');
        return systemName.substring(prefix.length() + 1,seperator);
    }

    /**
     * Validate a system name format.
     *
     * @param name    the name to validate
     * @param manager the manager requesting validation
     * @param locale  the locale for user messages
     * @return name, unchanged
     * @see jmri.Manager#validateSystemNameFormat(java.lang.String,
     * java.util.Locale)
     */
    public static String validateSystemNameFormat(String name, Manager manager, Locale locale) {
        name = manager.validateSystemNamePrefix(name, locale);
        String[] parts = name.substring(manager.getSystemNamePrefix().length()).split(":");
        if (parts.length != 2) {
            throw newBadSystemNameException(name,"SystemNameInvalidMissingParts",locale);
        }
        int num;
        try {
            num = parseEncoderAddress(parts[0],0,parts[0].length());
            if (num < 0 || num > 65535) {
                throw newBadSystemNameException(name, "SysteNameInvalidCanAddress", locale);
            }
        } catch (NumberFormatException ex) {
            throw newBadSystemNameException(name,"SysteNameInvalidCanAddress",locale);
        }
        try {
            num = Integer.parseInt(parts[1]);
            if (num < 0 || num > 7) {
                throw newBadSystemNameException(name,"SystemNameInvalidPin",locale);
            }
        } catch (NumberFormatException ex) {
            throw newBadSystemNameException(name,"SystemNameInvalidPin",locale);
        }
        return name;
    }

    private static NamedBean.BadSystemNameException newBadSystemNameException(String name, String reasonKey, Locale locale){
        return new NamedBean.BadSystemNameException(
                Bundle.getMessage(Locale.ENGLISH, reasonKey, name),
                Bundle.getMessage(locale, reasonKey, name));
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
        if (getBitFromSystemName(systemName, prefix) >= 0) {
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

    public static String buildDecimalSystemNameFromParts(String prefix, char typeLetter, int userAddress,int pin){
        return String.format("%s%c%d:%d",prefix,typeLetter, userAddress,pin);
    }

    public static String buildHexSystemNameFromParts(String prefix, char typeLetter,int globalCANaddress,int pin){
            return String.format("%s%c%4X:%d",prefix,typeLetter, globalCANaddress,pin);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Z21CanBusAddress.class);

}
