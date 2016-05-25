// SerialLightManager.java
package jmri.jmrix.powerline;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement light manager for powerline serial systems
 * <P>
 * System names are "PLnnn", where nnn is the bit number without padding.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Bob Jacobsen Copyright (C) 2006, 2007, 2008 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
abstract public class SerialLightManager extends AbstractLightManager {

    /**
     *
     */
    private static final long serialVersionUID = 8251688284977659469L;
    SerialTrafficController tc = null;

    public SerialLightManager(SerialTrafficController tc) {
        super();
        this.tc = tc;
    }

    /**
     * Returns the system letter
     */
    public String getSystemPrefix() {
        return tc.getAdapterMemo().getSystemPrefix();
    }

    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    /**
     * Method to create a new Light based on the system name Returns null if the
     * system name is not in a valid format Assumes calling method has checked
     * that a Light with this system name does not already exist
     */
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
        // Validate the systemName
        if (tc.getAdapterMemo().getSerialAddress().validSystemNameFormat(systemName, 'L')) {
            lgt = createNewSpecificLight(systemName, userName);
            if (!tc.getAdapterMemo().getSerialAddress().validSystemNameConfig(systemName, 'L')) {
                log.warn("Light system Name does not refer to configured hardware: "
                        + systemName);
            }
        } else {
            log.error("Invalid Light system Name format: " + systemName);
        }
        return lgt;
    }

    /**
     * Create light of a specific type for the interface
     */
    abstract protected Light createNewSpecificLight(String systemName, String userName);

    /**
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'
     */
    public boolean validSystemNameFormat(String systemName) {
        return (tc.getAdapterMemo().getSerialAddress().validSystemNameFormat(systemName, 'L'));
    }

    /**
     * Public method to validate system name for configuration returns 'true' if
     * system name has a valid meaning in current configuration, else returns
     * 'false'
     */
    public boolean validSystemNameConfig(String systemName) {
        return (tc.getAdapterMemo().getSerialAddress().validSystemNameConfig(systemName, 'L'));
    }

    /**
     * Public method to normalize a system name
     * <P>
     * Returns a normalized system name if system name has a valid format, else
     * returns "".
     */
    public String normalizeSystemName(String systemName) {
        return (tc.getAdapterMemo().getSerialAddress().normalizeSystemName(systemName));
    }

    /**
     * Returns 'true' to indicate this system can support variable lights
     */
    public boolean supportsVariableLights(String systemName) {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLightManager.class.getName());

}

/* @(#)SerialLightManager.java */
