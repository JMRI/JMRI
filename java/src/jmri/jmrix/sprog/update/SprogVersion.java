//SprogVersion.java
package jmri.jmrix.sprog.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to hold SPROG type and firmware version
 *
 * @author	Andrew Crosland Copyright (C) 2012
 * 
 */
public class SprogVersion {

    public SprogType sprogType = null;
    public String sprogVersion = "";

    /**
     * Construct a new SPROG version object from a SPROG type
     *
     * @param t SprogType the type of SPROG
     */
    public SprogVersion(SprogType t) {
        if (log.isDebugEnabled()) {
            log.debug("SprogVersion(SprogType) ctor: " + t.toString());
        }
        sprogType = t;
        sprogVersion = "";
    }

    /**
     * Construct a new SPROG version object from a SPROG type and version
     *
     * @param t SprogType the type of SPROG
     * @param s String version in "major.minor" format, e.g. "3.1"
     */
    public SprogVersion(SprogType t, String s) {
        if (log.isDebugEnabled()) {
            log.debug("SprogVersion(SprogType, String) ctor: " + t.toString() + "v" + s);
        }
        if (log.isDebugEnabled()) {
            log.debug("sprogType: " + t.sprogType);
        }
        sprogType = t;
        sprogVersion = s;
    }

    /**
     * Return major version number for a known SPROG
     *
     * @return major version
     */
    public int getMajorVersion() {
        int major = 0;
        if (sprogType.isSprog()) {
            try {
                major = Integer.parseInt(sprogVersion.substring(sprogVersion.indexOf(".")
                        - 1, sprogVersion.indexOf(".")));
            } catch (NumberFormatException e) {
                log.error("Cannot parse SPROG major version number");
            }
        }
        return major;
    }

    /**
     * Return minor version number for a known SPROG
     *
     * @return minor version
     */
    public int getMinorVersion() {
        int minor = 0;
        if (sprogType.isSprog()) {
            try {
                minor = Integer.parseInt(sprogVersion.substring(sprogVersion.indexOf(".")
                        + 1, sprogVersion.indexOf(".") + 2));
            } catch (NumberFormatException e) {
                log.error("Cannot parse SPROG minor version number");
            }
        }
        return minor;
    }

    /**
     * Check if the SPROG has various extra features that were not present in
     * the original firmware. This means later SPROG II versions or any type
     * equal or higher than SPROG 3
     *
     * @return boolean if the current SPROG has extra features
     */
    public boolean hasExtraFeatures() {
        int major = this.getMajorVersion();
        int minor = this.getMinorVersion();
        if (log.isDebugEnabled()) {
            log.debug("Major: " + major + " Minor: " + minor);
        }
        if (this.sprogType.isSprogII() && (((major == 1) && (minor >= 6))
                || ((major == 2) && (minor >= 1))
                || (major >= 3))
                || ((this.sprogType.sprogType >= SprogType.SPROGIIv3)
                    && (this.sprogType.sprogType < SprogType.NANO))) {
            if (log.isDebugEnabled()) {
                log.debug("This version has extra features");
            }
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("This version does not have extra features");
        }
        return false;
    }

    /**
     * Check if the SPROG has blueline decoder mode.
     *
     * @return true if the SPROG has blueline decoder support
     */
    public boolean hasBlueLine() {
        return this.hasExtraFeatures();
    }

    /**
     * Check if the SPROG has an adjustable current limit.
     *
     * @return true if the SPROG has adjustable current limit
     */
    public boolean hasCurrentLimit() {
        return this.hasExtraFeatures();
    }

    /**
     * Check if the SPROG has an interlock for the bootloader.
     *
     * @return true if the SPROG has firmware interlock
     */
    public boolean hasFirmwareLock() {
        return (this.hasExtraFeatures() || (this.sprogType.sprogType <= SprogType.NANO));
    }

    public boolean hasZTCMode() {
        return (this.sprogType.sprogType < SprogType.NANO);
    }

    /**
     *
     * @return String representation of SPROG version
     */
    @Override
    public String toString() {
        return this.toString(this);
    }

    /**
     *
     * @return String representation of SPROG version
     */
    public String toString(SprogVersion s) {
        if (log.isDebugEnabled()) {
            log.debug("sprogType: " + s.sprogType.sprogType);
        }
        return (s.sprogType.toString() +" v"+ sprogVersion);
    }

    private final static Logger log = LoggerFactory.getLogger(SprogVersion.class);
}
