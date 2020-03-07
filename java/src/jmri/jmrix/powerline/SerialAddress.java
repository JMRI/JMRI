package jmri.jmrix.powerline;

import java.util.Locale;
import jmri.Manager.NameValidity;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jmri.NamedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting parsing and testing of addresses.
 * <p>
 * Two address formats are supported: For X10: Ptnxx where: t is the type code,
 * 'S' for sensors, and 'L' for lights n is the house code of the input or
 * output bit (A - P) xx is a bit number of the input or output bit (1-16)
 * examples: PLA2 (House Code A, Unit 2), PSK1 (House Code K, Unit 1) For
 * Insteon: Pthh.hh.hh where: t is the type code, 'S' for sensors, and 'L' for
 * lights aa is two hexadecimal digits examples: PLA2.43.CB
 *
 * @author Dave Duchamp, Copyright (C) 2004
 * @author Bob Jacobsen, Copyright (C) 2006, 2007, 2008, 2009
 * @author Ken Cameron, Copyright (C) 2008, 2009, 2010
 */
public class SerialAddress {

    private Matcher hCodes = null;
    private Matcher aCodes = null;
    private Matcher iCodes = null;
    private static final char MIN_HOUSE_CODE = 'A';
    private static final char MAX_HOUSE_CODE = 'P';

    public SerialAddress(SerialSystemConnectionMemo m) {
        this.memo = m;
        hCodes = Pattern.compile("^(" + memo.getSystemPrefix() + ")([LTS])([" + MIN_HOUSE_CODE + "-" + MAX_HOUSE_CODE + "])(\\d++)$").matcher("");
        aCodes = Pattern.compile("^(" + memo.getSystemPrefix() + ")([LTS]).*$").matcher("");
        iCodes = Pattern.compile("^(" + memo.getSystemPrefix() + ")([LTS])(\\p{XDigit}\\p{XDigit})[.](\\p{XDigit}\\p{XDigit})[.](\\p{XDigit}\\p{XDigit})$").matcher("");
    }

    SerialSystemConnectionMemo memo = null;

    /**
     * Validate the format for a system name.
     *
     * @param name   the name to validate
     * @param type   the type letter for the name
     * @param locale the locale for messages to the user
     * @return the name, unchanged
     */
    String validateSystemNameFormat(String name, char type, Locale locale) {
        boolean aTest = aCodes.reset(name).matches();
        boolean hTest = hCodes.reset(name).matches();
        boolean iTest = iCodes.reset(name).matches();
        if (!aTest || aCodes.group(2).charAt(0) != type) {
            throw new NamedBean.BadSystemNameException(
                    Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameInvalidPrefix", memo.getSystemPrefix() + type),
                    Bundle.getMessage(locale, "InvalidSystemNameInvalidPrefix", memo.getSystemPrefix() + type));
        } else if (hTest && hCodes.groupCount() == 4) {
            // This is a PLaxx address - validate the house code and unit address fields
            if (hCodes.group(3).charAt(0) < MIN_HOUSE_CODE || hCodes.group(3).charAt(0) > MAX_HOUSE_CODE) {
                throw new NamedBean.BadSystemNameException(
                        Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameInvalidHouseCode", name),
                        Bundle.getMessage(locale, "InvalidSystemNameInvalidHouseCode", name));
            }
            try {
                int num;
                num = Integer.parseInt(hCodes.group(4));
                if ((num < 1) || (num > 16)) {
                    throw new NamedBean.BadSystemNameException(
                            Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameInvalidDevice", name),
                            Bundle.getMessage(locale, "InvalidSystemNameInvalidDevice", name));
                }
            } catch (NumberFormatException e) {
                throw new NamedBean.BadSystemNameException(
                        Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameInvalidDevice", name),
                        Bundle.getMessage(locale, "InvalidSystemNameInvalidDevice", name));
            }
        } else if (iTest) {
            // This is a PLaa.bb.cc address - validate the Insteon address fields
            if (iCodes.groupCount() != 5) {
                throw new NamedBean.BadSystemNameException(
                        Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameInvalidInsteon", name),
                        Bundle.getMessage(locale, "InvalidSystemNameInvalidInsteon", name));
            }
        } else {
            throw new NamedBean.BadSystemNameException(
                    Bundle.getMessage(Locale.ENGLISH, "InvalidSystemNameInvalidFormat", name),
                    Bundle.getMessage(locale, "InvalidSystemNameInvalidFormat", name));
        }
        return name;
    }

    /**
     * Public static method to validate system name format.
     *
     * @param systemName name to test
     * @param type Letter indicating device type expected
     * @return VALID if system name has a valid format, else return INVALID
     */
    public NameValidity validSystemNameFormat(String systemName, char type) {
        try {
            validateSystemNameFormat(systemName, type, Locale.getDefault());
        } catch (IllegalArgumentException ex) {
            // TODO: match possible prefixes as VALID_AS_PREFIX
            return NameValidity.INVALID;
        }
        return NameValidity.VALID;
    }

    /**
     * Public static method to validate system name for configuration returns
     * 'true' if system name has a valid meaning in current configuration, else
     * returns 'false'.
     *
     * @param systemName name to test
     * @param type       type to test
     * @return  true for valid names
     */
    public boolean validSystemNameConfig(String systemName, char type) {
        return validSystemNameFormat(systemName, type) == NameValidity.VALID;
    }

    /**
     * Public static method determines whether a systemName names an Insteon
     * device.
     *
     * @param systemName name to test
     * @return true if system name corresponds to Insteon device
     */
    public boolean isInsteon(String systemName) {
        // ensure that input system name has a valid format
        if ((!aCodes.reset(systemName).matches()) || (validSystemNameFormat(systemName, aCodes.group(2).charAt(0)) != NameValidity.VALID)) {
            // No point in normalizing if a valid system name format is not present
            return false;
        } else {
            if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 4) {
                // This is a PLaxx address
                try {
                    return false; // is X10, or at least not Insteon
                } catch (Exception e) {
                    log.error("illegal character in house code field system name: {}", systemName);
                    return false;  // can't be parsed, isn't Insteon
                }
            }
        }
        return true;
    }

    /**
     * Public static method to normalize a system name.
     * <p>
     * This routine is used to ensure that each system name is uniquely linked
     * to one bit, by removing extra zeros inserted by the user.
     * <p>
     * If the supplied system name does not have a valid format, an empty string
     * is returned. Otherwise a normalized name is returned in the same format
     * as the input name.
     *
     * @param systemName name to process
     * @return If the supplied system name does not have a valid format, an empty string
     * is returned. Otherwise a normalized name is returned in the same format
     * as the input name.
     */
    public String normalizeSystemName(String systemName) {
        // ensure that input system name has a valid format, test all formats
        boolean aMatch = aCodes.reset(systemName).matches();
        int aCount = aCodes.groupCount();
        boolean hMatch = hCodes.reset(systemName).matches();
        int hCount = hCodes.groupCount();
        boolean iMatch = iCodes.reset(systemName).matches();
        int iCount = iCodes.groupCount();
        if (!aMatch || aCount != 2 || (validSystemNameFormat(systemName, aCodes.group(2).charAt(0)) != NameValidity.VALID)) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }
        String nName = "";
        // check for the presence of a char to differentiate the two address formats
        if (hMatch && hCount == 4) {
            // This is a PLaxx address
            nName = hCodes.group(1) + hCodes.group(2) + hCodes.group(3) + Integer.toString(Integer.parseInt(hCodes.group(4)));
        }
        if (nName.equals("")) {
            // check for the presence of a char to differentiate the two address formats
            if (iMatch && iCount == 5) {
                // This is a PLaa.bb.cc Insteon address
                nName = iCodes.group(1) + iCodes.group(2) + iCodes.group(3) + "." + iCodes.group(4) + "." + iCodes.group(5);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("valid name doesn't normalize: " + systemName + " hMatch: " + hMatch + " hCount: " + hCount);
                }
            }
        }
        return nName;
    }

    /**
     * Extract housecode from system name, as a letter A-P.
     * <p>
     * If the supplied system name does not have a valid format, an empty string
     * is returned.
     *
     * @param systemName system name
     * @return house code letter
     */
    public String houseCodeFromSystemName(String systemName) {
        String hCode = "";
        // ensure that input system name has a valid format
        if ((!aCodes.reset(systemName).matches()) || (validSystemNameFormat(systemName, aCodes.group(2).charAt(0)) != NameValidity.VALID)) {
            // No point in normalizing if a valid system name format is not present
        } else {
            if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 2) {
                // This is a PLaxx address
                try {
                    hCode = hCodes.group(1);
                } catch (Exception e) {
                    log.error("illegal character in house code field system name: " + systemName);
                    return "";
                }
            }
        }
        return hCode;
    }

    /**
     * Extract devicecode from system name, as a string 1-16.
     * 
     * @param systemName name
     * @return If the supplied system name does not have a valid format, an empty string
     * is returned. X10 type device code
     */
    public String deviceCodeFromSystemName(String systemName) {
        String dCode = "";
        // ensure that input system name has a valid format
        if ((!aCodes.reset(systemName).matches()) || (validSystemNameFormat(systemName, aCodes.group(2).charAt(0)) != NameValidity.VALID)) {
            // No point in normalizing if a valid system name format is not present
        } else {
            if (hCodes.reset(systemName).matches()) {
                if (hCodes.groupCount() == 2) {
                    // This is a PLaxx address
                    try {
                        dCode = hCodes.group(2);
                    } catch (Exception e) {
                        log.error("illegal character in number field system name: " + systemName);
                        return "";
                    }
                }
            } else {
                if (iCodes.reset(systemName).matches()) {
                    dCode = iCodes.group(3) + iCodes.group(4) + iCodes.group(5);
                } else {
                    log.error("illegal insteon address: " + systemName);
                    return "";
                }
            }
        }
        return dCode;
    }

    /**
     * Extract housecode from system name, as a value 1-16.
     * <p>
     * If the supplied system name does not have a valid format, an -1 is
     * returned.
     *
     * @param systemName name
     * @return valid 1-16, invalid, return -1
     */
    public int houseCodeAsValueFromSystemName(String systemName) {
        int hCode = -1;
        // ensure that input system name has a valid format
        if ((!aCodes.reset(systemName).matches()) || (validSystemNameFormat(systemName, aCodes.group(2).charAt(0)) != NameValidity.VALID)) {
            // No point in normalizing if a valid system name format is not present
        } else {
            if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 4) {
                // This is a PLaxx address
                try {
                    hCode = hCodes.group(3).charAt(0) - 0x40;
                } catch (Exception e) {
                    log.error("illegal character in number field system name: " + systemName);
                    return -1;
                }
            }
        }
        return hCode;
    }

    /**
     * Extract devicecode from system name, as a value 1-16.
     * <p>
     * If the supplied system name does not have a valid format, an -1 is
     * returned.
     *
     * @param systemName name
     * @return value of X10 device code, -1 if invalid
     */
    public int deviceCodeAsValueFromSystemName(String systemName) {
        int dCode = -1;
        // ensure that input system name has a valid format
        if ((!aCodes.reset(systemName).matches()) || (validSystemNameFormat(systemName, aCodes.group(2).charAt(0)) != NameValidity.VALID)) {
            // No point in normalizing if a valid system name format is not present
        } else {
            if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 4) {
                // This is a PLaxx address
                try {
                    dCode = Integer.parseInt(hCodes.group(4));
                } catch (NumberFormatException e) {
                    log.error("illegal character in number field system name: " + systemName);
                    return -1;
                }
            }
        }
        return dCode;
    }

    /**
     * Extract Insteon high device id from system name.
     * <p>
     * If the supplied system name does not have a valid format, an empty string
     * is returned.
     *
     * @param systemName name
     * @return Insteon high byte value
     */
    public int idHighCodeAsValueFromSystemName(String systemName) {
        int dCode = -1;
        // ensure that input system name has a valid format
        if (!iCodes.reset(systemName).matches() || validSystemNameFormat(systemName, iCodes.group(2).charAt(0)) != NameValidity.VALID) {
            // No point in normalizing if a valid system name format is not present
        } else {
            if (iCodes.groupCount() == 5) {
                // This is a PLhh.mm.ll address
                try {
                    dCode = Integer.parseInt(iCodes.group(3), 16);
                } catch (NumberFormatException e) {
                    log.error("illegal character in high id system name: " + systemName);
                    return -1;
                }
            }
        }
        return dCode;
    }

    /**
     * Extract Insteon middle device id from system name.
     * <p>
     * If the supplied system name does not have a valid format, an empty string
     * is returned.
     *
     * @param systemName name
     * @return Insteon middle id value, -1 if invalid
     */
    public int idMiddleCodeAsValueFromSystemName(String systemName) {
        int dCode = -1;
        // ensure that input system name has a valid format
        if (!iCodes.reset(systemName).matches() || validSystemNameFormat(systemName, iCodes.group(2).charAt(0)) != NameValidity.VALID) {
            // No point in normalizing if a valid system name format is not present
        } else {
            if (iCodes.groupCount() == 5) {
                // This is a PLhh.mm.ll address
                try {
                    dCode = Integer.parseInt(iCodes.group(4), 16);
                } catch (NumberFormatException e) {
                    log.error("illegal character in high id system name: " + systemName);
                    return -1;
                }
            }
        }
        return dCode;
    }

    /**
     * Extract Insteon low device id from system name.
     * <p>
     * If the supplied system name does not have a valid format, an empty string
     * is returned.
     *
     * @param systemName name
     * @return Insteon low value id, -1 if invalid
     */
    public int idLowCodeAsValueFromSystemName(String systemName) {
        int dCode = -1;
        // ensure that input system name has a valid format
        if (!iCodes.reset(systemName).matches() || validSystemNameFormat(systemName, iCodes.group(2).charAt(0)) != NameValidity.VALID) {
            // No point in normalizing if a valid system name format is not present
        } else {
            if (iCodes.groupCount() == 5) {
                // This is a PLhh.mm.ll address
                try {
                    dCode = Integer.parseInt(iCodes.group(5), 16);
                } catch (NumberFormatException e) {
                    log.error("illegal character in high id system name: " + systemName);
                    return -1;
                }
            }
        }
        return dCode;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialAddress.class);

}
