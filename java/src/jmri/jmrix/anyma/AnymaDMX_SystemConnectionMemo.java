package jmri.jmrix.anyma;

import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.Manager.NameValidity;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal SystemConnectionMemo for anyma dmx systems.
 *
 * @author George Warner Copyright (c) 2017-2018
 * @since 4.9.6
 */
public class AnymaDMX_SystemConnectionMemo extends SystemConnectionMemo {

    private boolean configured = false;

    /**
     * constructor
     */
    public AnymaDMX_SystemConnectionMemo() {
        this("D", AnymaDMX_ConnectionTypeList.ANYMA_DMX); // default to "D" prefix
        log.debug("* Constructor()");
    }

    /**
     * constructor
     */
    public AnymaDMX_SystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        super(prefix, userName);

        log.debug("* Constructor ({}, {})", prefix, userName);

        register(); // registers general type
        InstanceManager.store(this, AnymaDMX_SystemConnectionMemo.class); // also register as specific type
    }

    private AnymaDMX_TrafficController trafficController = null;

    /**
     * get the traffic controller
     *
     * @return the traffic controller
     */
    protected AnymaDMX_TrafficController getTrafficController() {
        return trafficController;
    }

    /**
     * set the traffic controller
     *
     * @param trafficController the traffic controller
     */
    protected void setTrafficController(AnymaDMX_TrafficController trafficController) {
        this.trafficController = trafficController;
    }

    /**
     * public method to get the user name for a valid system name
     *
     * @param systemName the system name
     * @return "" (null string) if system name is not valid or does not exist
     */
    public String getUserNameFromSystemName(String systemName) {
        log.debug("* getUserNameFromSystemName('{}')", systemName);
        String result = "";        // not any known light
        int offset = checkSystemPrefix(systemName);
        if (offset > 0) {
            if (systemName.length() > offset) {
                if (systemName.charAt(offset) == 'L') {
                    Light lgt = null;
                    lgt = InstanceManager.lightManagerInstance().getBySystemName(systemName);
                    if (lgt != null) {
                        result = lgt.getUserName();
                    }
                }
            }
        }
        return result;
    }

    /**
     * Public static method to parse a anyma dmx system name and return the
     * channel number. Notes:
     * <ul>
     * <li>Channels are numbered from 1 to 512.</li>
     * <li>Does not check whether that node is defined on current system.</li>
     * </ul>
     *
     * @return 0 if an error is found.
     */
    public int getChannelFromSystemName(String systemName) {
        int result = 0;
        log.debug("* getChannelFromSystemName('{}')", systemName);

        int offset = checkSystemPrefix(systemName);
        if (offset > 0) {
            if (validSystemNameFormat(systemName, systemName.charAt(offset)) == NameValidity.VALID) {
                // Find the beginning of the channel number field
                int k = 0;
                for (int i = offset; i < systemName.length(); i++) {
                    if (systemName.charAt(i) == 'L') {
                        k = i + 1;
                        break;
                    }
                }
                if (k > offset) {    // k = position of "L" char in name
                    try {
                        result = Integer.parseInt(systemName.substring(k));
                    } catch (Exception e) {
                        log.warn("invalid character in channel number field of anyma dmx system name: {}", systemName);
                    }
                }
            } else {
                log.error("No point in normalizing if a valid system name format is not present");
            }
        } else {
            log.error("invalid system prefix in anyma dmx system name in getChannelFromSystemName: {}", systemName);
        }
        return result;
    }

    /**
     * Public static method to check and skip the System Prefix string on a
     * system name.
     *
     * @return offset of the 1st character past the prefix, or -1 if not valid
     *         for this connection
     */
    public int checkSystemPrefix(String systemName) {
        log.debug("* checkSystemPrefix('{}')", systemName);
        int result = -1;
        if (systemName.startsWith(getSystemPrefix())) {
            result = getSystemPrefix().length();
        }
        return result;
    }

    /**
     * Public static method to normalize a anyma dmx system name.
     * <p>
     * This routine is used to ensure that each system name is uniquely linked
     * to one anyma dmx channel, by removing any extra zeros inserted by the
     * user.
     *
     * @return "" (empty string) if the supplied system name does not have a
     *         valid format. Otherwise a normalized name is returned in the same
     *         format as the input name.
     */
    public String normalizeSystemName(String systemName) {
        String result = "";

        log.debug("* normalizeSystemName('{}')", systemName);

        int offset = checkSystemPrefix(systemName);
        if (offset > 0) {
            if (validSystemNameFormat(systemName, systemName.charAt(offset)) == NameValidity.VALID) {
                int channelNum = Integer.parseInt(systemName.substring(offset + 1));
                result = systemName.substring(0, offset + 1) + Integer.toString(channelNum);
            } else {
                // No point in normalizing if a valid system name format is not present
            }
        } else {
            log.error("invalid system prefix in anyma dmx system name in normalizeSystemName: '{}'", systemName); // fix test first
        }
        return result;
    }

    /**
     * Public static method to convert one format anyma dmx system name to the
     * alternate format.
     *
     * @return "" (empty string) if the supplied system name does not have a
     *         valid format, or if there is no representation in the alternate
     *         naming scheme
     */
    public String convertSystemNameToAlternate(String systemName) {
        log.debug("* convertSystemNameToAlternate('{}')", systemName);
        String result = "";

        int offset = checkSystemPrefix(systemName);
        if (offset > 0) {
            if (validSystemNameFormat(systemName, systemName.charAt(offset)) == NameValidity.VALID) {
                int channelNum = Integer.parseInt(systemName.substring(offset + 1));
                result = systemName.substring(0, offset + 1) + Integer.toString(channelNum);
            } else {
                log.error("valid system name format not present in anyma dmx system name: {}", systemName);
            }
        } else {
            log.error("invalid system prefix in anyma dmx system name in convertSystemNameToAlternate: {}", systemName);
        }
        return result;
    }

    /**
     * Public static method to validate system name format. Does not check
     * whether that node is defined on current system.
     *
     * @return enum indicating current validity, which might be just as a prefix
     */
    public NameValidity validSystemNameFormat(String systemName, char type) {
        log.debug("* validSystemNameFormat('{}', '{}')", systemName, type);
        NameValidity result = NameValidity.INVALID; // assume failure (pessimist!)

        int offset = checkSystemPrefix(systemName);
        if (offset > 0) {
            if (systemName.charAt(offset) == type) {
                // This is a CLnnnxxx pattern address
                int num;
                try {
                    num = Integer.parseInt(systemName.substring(offset + 1));
                    if ((num >= 1) && (num <= 512)) {
                        result = NameValidity.VALID;
                    } else {
                        log.debug("number field out of range in anyma dmx system name: {}", systemName);
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    log.debug("invalid character in number field of anyma dmx system name: {}", systemName);
                }
            } else {
                log.error("invalid type character in anyma dmx system name: {}", systemName);
            }
        } else {
            log.error("invalid system prefix in anyma dmx system name in validSystemNameFormat: {}", systemName);
        }
        return result;
    }

    /**
     * Public static method to construct a anyma dmx system name from type
     * character, node address, and channel number.
     * <p>
     * If the supplied character is not valid, or the node address is out of the
     * 0 - 127 range, or the channel number is out of the 1 - 512 range, an
     * error message is logged and the null string "" is returned.
     *
     * @return a system name in the DaLnnnxxx format
     */
    public String makeSystemName(String type, int nAddress, int channelNum) {
        log.debug("* makeSystemName('{}', {}, {})", type, nAddress, channelNum);
        String nName = "";
        if (type.equals("L")) {
            if ((nAddress >= 0) && (nAddress < 1000)) {
                if ((channelNum >= 1) && (channelNum <= 512)) {
                    nName = getSystemPrefix() + nAddress + type + Integer.toString(channelNum);
                } else {
                    log.warn("invalid channel number proposed for system name");
                    return nName;
                }
            } else {
                log.warn("invalid node address proposed for system name");
                return nName;
            }
        } else {
            log.error("invalid type character proposed for system name");
            return nName;
        }
        return nName;
    }

    /**
     * Public static method to validate anyma dmx system name for configuration.
     * Does validate node number and system prefix.
     *
     * @return 'true' if system name has a valid meaning in current
     *         configuration, else returns 'false'.
     */
    public boolean validSystemNameConfig(String systemName, char type) {
        log.debug("* validSystemNameConfig('{}', '{}')", systemName, type);
        boolean result = false; // assume failure (pessimist!)
        if (validSystemNameFormat(systemName, type) == NameValidity.VALID) {
            if (type == 'L') {
                int channel = getChannelFromSystemName(systemName);
                if ((channel >= 1) && (channel <= 512)) {
                    result = true;  // The channel is valid
                }
            } else {
                log.error("Invalid type specification in validSystemNameConfig call");
            }
        } else {
            log.error("valid system name format is not present");
        }
        return result;
    }

    /**
     * Public static method to parse a anyma dmx system name and return the Usb
     * Node Address
     * <p>
     * Nodes are numbered from 0 - 127. Does not check whether that node is
     * defined on current system.
     *
     * @return '-1' if invalid systemName format or if the node is not found.
     */
    public int getNodeAddressFromSystemName(String systemName) {
        int result = -1;    // assume failure (pessimist!)
        log.debug("* getNodeAddressFromSystemName('{}')", systemName);
        int offset = checkSystemPrefix(systemName);
        if (offset > 0) {
            if (systemName.charAt(offset) == 'L') {
                int num = Integer.parseInt(systemName.substring(offset + 1));
                if (num > 0) {
                    result = num;
                } else {
                    log.warn("invalid anyma dmx system name: " + systemName);
                }
            } else {
                log.error("invalid character in header field of system name: {}", systemName);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean provides(Class<?> c) {
        return (get(c) != null);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        T result = null; // nothing by default
        log.debug("* get({})", T.toString());
        if (!getDisabled()) {
            if (!configured) {
                configureManagers();
            }
            if (T.equals(LightManager.class)) {
                result = (T) getLightManager();
            }
        }
        return result;
    }

    /**
     * Configure the common managers for anyma dmx connections. This puts the
     * common manager config in one place.
     */
    public void configureManagers() {
        log.debug("* configureManagers()");
        InstanceManager.setLightManager(getLightManager());

        if (configured) {
            log.warn("calling configureManagers for a second time", new Exception("traceback"));
        }
        configured = true;
    }

    private UsbLightManager lightManager;

    /**
     * get the LightManager
     *
     * @return the LightManager
     */
    public UsbLightManager getLightManager() {
        log.debug("* getLightManager()");
        UsbLightManager result = null;
        if (!getDisabled()) {
            if (lightManager == null) {
                lightManager = new UsbLightManager(this);
            }
            result = lightManager;
        }
        return result;
    }

    /**
     * get the action model resource bundle
     *
     * @return the ResourceBundle
     */
    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        log.debug("* getActionModelResourceBundle()");
        return null; //ResourceBundle.getBundle("jmri.jmrix.anyma.AnymaDMX_Bundle");
    }

    /**
     * dispose
     */
    @Override
    public void dispose() {
        log.debug("* dispose()");
        InstanceManager.deregister(this, AnymaDMX_SystemConnectionMemo.class);
        if (lightManager != null) {
            InstanceManager.deregister(lightManager, UsbLightManager.class);
        }
        super.dispose();
    }

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_SystemConnectionMemo.class);
}
