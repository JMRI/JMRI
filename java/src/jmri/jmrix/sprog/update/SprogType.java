//SprogType.java
package jmri.jmrix.sprog.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to hold SPROG type
 *
 * @author	Andrew crosland Copyright (C) 2012
 */
public class SprogType {

    // NOTE the numerical order of thes constants is used in the bootloader
    // to test for various SPROG types
    public static final int UNKNOWN = 0;
    public static final int NO_PROMPT_FOUND = 1;
    public static final int NOT_A_SPROG = 2;
    public static final int NOT_RECOGNISED = 3;
    public static final int TIMEOUT = 4;
    public static final int SPROGV4 = 10;
    public static final int SPROGII = 20;
    public static final int SPROGIIUSB = 21;
    public static final int SPROGIIv3 = 23;
    public static final int SPROGIIv4 = 24;
    public static final int SPROG3 = 30;
    public static final int SPROGIV = 40;
    public static final int SPROG5 = 50;
    public static final int PISPROGONE = 60;
    public static final int NANO = 1000;
    public static final int PISPROGNANO = 1001;
    public static final int SNIFFER = 2000;
    public int sprogType = UNKNOWN;

    /**
     * Construct a new SPROG type of a given type.
     *
     * @param type int, one of SprogType.xxx constants
     */
    public SprogType(int type) {
        if (log.isDebugEnabled()) {
            log.debug("SprogType(int) ctor, type: " + type);
        }
        sprogType = type;
    }

    /**
     * Check for any SPROG type
     *
     * @return true if this object holds a SPROG type
     */
    public boolean isSprog() {
        if (sprogType < SPROGV4) {
            return false;
        }
        return true;
    }

    /**
     * Check for a SPROG II type
     *
     * @return true if this object holds a SPROG II type
     */
    public boolean isSprogII() {
        if ((sprogType >= SPROGII) && (sprogType <= SPROGIIv4)) {
            return true;
        }
        return false;
    }

    /**
     * Return the multiplier for scaling the current limit from hardware units
     * to physical units (mA).
     *
     * @return the multiplier for the current limit
     */
    public double getCurrentMultiplier() {
        switch (sprogType) {
            case PISPROGONE:
                // Value returned is number of ADC steps 0f 3.22mV across 0.47
                // ohms, or equivalent
                return 3220.0/470;
                
            default:
                // Value returned is number of ADC steps 0f 4.88mV across 0.47
                // ohms, or equivalent
                return 4880.0/470;
        }
    }
    
    /**
     * Get the Flash memory block Length for bootloader.
     *
     * @return blocklen
     */
    public int getBlockLen() {
        switch (sprogType) {
            case NO_PROMPT_FOUND:
            case NOT_A_SPROG:
            case NOT_RECOGNISED:
            case TIMEOUT:
            default:
                return -1;

            case SPROGV4:
            case SPROGIIUSB:
            case SPROGII:
                return 8;

            case SPROGIIv3:
            case SPROGIIv4:
            case SPROG3:
            case SPROGIV:
            case SPROG5:
            case PISPROGONE:
            case NANO:
            case PISPROGNANO:
            case SNIFFER:
                return 16;
        }
    }

    /**
     * Get the Flash memory block Length for bootloader.
     *
     * @param bootVer the bootloader version
     * @return length in bytes
     */
    static public int getBlockLen(int bootVer) {
        switch (bootVer) {
            case 10:
            case 11:
                return 8;
            case 13:
                return 16;
            default:
                return -1;
        }
    }

    /**
     * Check if an address is one which should be reprogrammed during bootloader
     * operation. Checks if address is above the bootloader's address space and
     * below the debug executive's address space.
     *
     * @param addr int
     * @return true or false
     */
    public Boolean isValidFlashAddress(int addr) {
        switch (sprogType) {
            case SPROGV4:

            case SPROGIIUSB:
            case SPROGII:
                if (addr >= 0x200) {
                    return true;
                }
                break;

            case SPROGIIv3:
            case SPROGIIv4:
            case SPROG3:
            case SPROGIV:
            case SPROG5:
            case NANO:
            case SNIFFER:
                if ((addr >= 0x2200) && (addr < 0x3F00)) {
                    return true;
                }
                break;

            case PISPROGNANO:
                if ((addr >= 0x0C00) && (addr < 0x1FF0)) {
                    return true;
                }
                break;
                
            case PISPROGONE:
                if ((addr >= 0x1000) && (addr < 0x3F00)) {
                    return true;
                }
                break;

            default:
                return false;
        }
        return false;
    }

    public int getEraseStart() {
        switch (sprogType) {
            case SPROGIIUSB:
            case SPROGII:
                return 0x200;

            case SPROGIIv3:
            case SPROGIIv4:
            case SPROG3:
            case SPROGIV:
            case SPROG5:
            case NANO:
            case SNIFFER:
                return 0x2200;

            case PISPROGNANO:
                return 0x0C00;

            case PISPROGONE:
                return 0x1000;

            default:
                break;
        }
        log.error("Can't determine erase start adress");
        return -1;
    }

    /**
     *
     * @return String representation of a SPROG type
     */
    @Override
    public String toString() {
        return this.toString(sprogType);
    }

    /**
     *
     * @return String representation of a SPROG type
     */
    public String toString(int t) {
        //if (log.isDebugEnabled()) { log.debug("Integer {}", t); }
        switch (t) {
            case NO_PROMPT_FOUND:
                return Bundle.getMessage("TypeNoSprogPromptFound");
            case NOT_A_SPROG:
                return Bundle.getMessage("TypeNotConnectedToSPROG");
            case NOT_RECOGNISED:
                return Bundle.getMessage("TypeUnrecognisedSPROG");
            case TIMEOUT:
                return Bundle.getMessage("TypeTimeoutTalkingToSPROG");
            case SPROGV4:
                return "SPROG ";
            case SPROGIIUSB:
                return "SPROG II USB ";
            case SPROGII:
                return "SPROG II ";
            case SPROGIIv3:
                return "SPROG IIv3 ";
            case SPROGIIv4:
                return "SPROG IIv4 ";
            case SPROG3:
                return "SPROG 3 ";
            case SPROGIV:
                return "SPROG IV ";
            case SPROG5:
                return "SPROG 5 ";
            case PISPROGONE:
                return "Pi-SPROG One ";
            case NANO:
                return "SPROG Nano ";
            case PISPROGNANO:
                return "Pi-SPROG Nano ";
            case SNIFFER:
                return "SPROG Sniffer ";
            default:
                return Bundle.getMessage("TypeUnknownHardware");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SprogType.class);

}
