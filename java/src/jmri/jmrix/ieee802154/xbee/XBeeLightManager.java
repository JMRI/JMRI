package jmri.jmrix.ieee802154.xbee;

import javax.annotation.*;
import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement LightManager for XBee connections.
 * <p>
 *
 * @author Paul Bender Copyright (C) 2014
 */
public class XBeeLightManager extends AbstractLightManager {

    protected String prefix = null;

    protected XBeeTrafficController tc = null;

    public XBeeLightManager(XBeeTrafficController controller, String prefix) {
        tc = controller;
        this.prefix = prefix;
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    // Multiple additions currently works partially, but not for all possible cases;
    // for now, return 'false'.
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    @Override
    public Light createNewLight(String systemName, String userName) {
        XBeeNode curNode = null;
        String name = addressFromSystemName(systemName);
        if ((curNode = (XBeeNode) tc.getNodeFromName(name)) == null) {
            if ((curNode = (XBeeNode) tc.getNodeFromAddress(name)) == null) {
                try {
                    curNode = (XBeeNode) tc.getNodeFromAddress(Integer.parseInt(name));
                } catch (java.lang.NumberFormatException nfe) {
                    // if there was a number format exception, we couldn't
                    // find the node.
                    curNode = null;
                    log.debug("failed to create light {}", systemName);
                    return null;
                }
            }
        }
        int pin = pinFromSystemName(systemName);
        if (!curNode.getPinAssigned(pin)) {
            log.debug("Adding sensor to pin " + pin);
            curNode.setPinBean(pin, new XBeeLight(systemName, userName, tc));
            return (XBeeLight) curNode.getPinBean(pin);
        } else {
            log.debug("failed to create light {}", systemName);
            return null;
        }
    }

    /**
     * Public method to validate system name format.
     *
     * @param systemName Xbee id format with pins to be checked
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        if (tc.getNodeFromName(addressFromSystemName(systemName)) == null
                && tc.getNodeFromAddress(addressFromSystemName(systemName)) == null) {
            try {
                if (tc.getNodeFromAddress(Integer.parseInt(addressFromSystemName(systemName))) == null) {
                    return NameValidity.INVALID;
                } else {
                    return (pinFromSystemName(systemName) >= 0
                            && pinFromSystemName(systemName) <= 7) ? NameValidity.VALID : NameValidity.INVALID;
                }
            } catch (java.lang.NumberFormatException nfe) {
                // if there was a number format exception, we couldn't find the node.
                log.error("Unable to convert " + systemName + " into the Xbee node and pin format of nn:xx");
                return NameValidity.INVALID;
            }

        } else {
            return (pinFromSystemName(systemName) >= 0
                    && pinFromSystemName(systemName) <= 7) ? NameValidity.VALID : NameValidity.INVALID;
        }
    }

    private String addressFromSystemName(String systemName) {
        String encoderAddress;

        if (systemName.contains(":")) {
            //Address format passed is in the form of encoderAddress:input or S:light address
            int seperator = systemName.indexOf(":");
            encoderAddress = systemName.substring(getSystemPrefix().length() + 1, seperator);
        } else {
            if(systemName.length()>(getSystemPrefix().length()+1)) {
               encoderAddress = systemName.substring(getSystemPrefix().length() + 1, systemName.length() - 1);
            } else {
               encoderAddress = systemName.substring(getSystemPrefix().length() + 1);
            }
        }
        log.debug("Converted {} to hardware address {}", systemName, encoderAddress);
        return encoderAddress;
    }

    private int pinFromSystemName(String systemName) {
        int input = 0;
        int iName = 0;

        if (systemName.contains(":")) {
            //Address format passed is in the form of encoderAddress:input or L:light address
            int seperator = systemName.indexOf(":");
            try {
                input = Integer.parseInt(systemName.substring(seperator + 1));
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert {} into the XBee node and pin format of nn:xx", systemName);
                return -1;
            }
        } else {
            try {
                iName = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
                input = iName % 10;
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert {} system name to a number", systemName);
                return -1;
            }
        }
        log.debug("Converted {} to pin number {}", systemName, input);
        return input;
    }

    /**
     * Public method to validate system name for configuration.
     *
     * @return 'true' if system name has a valid meaning in current configuration, else returns
     * 'false'. For now, this method always returns 'true'; it is needed for the
     * Abstract Light class
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

    @Override
    public void deregister(jmri.Light s) {
        super.deregister(s);
        // remove the specified sensor from the associated XBee pin.
        String systemName = s.getSystemName();
        String name = addressFromSystemName(systemName);
        int pin = pinFromSystemName(systemName);
        XBeeNode curNode;
        if ((curNode = (XBeeNode) tc.getNodeFromName(name)) == null) {
            if ((curNode = (XBeeNode) tc.getNodeFromAddress(name)) == null) {
                try {
                    curNode = (XBeeNode) tc.getNodeFromAddress(Integer.parseInt(name));
                } catch (java.lang.NumberFormatException nfe) {
                    // if there was a number format exception, we couldn't
                    // find the node.
                    curNode = null;
                }
            }
        }
        if (curNode != null) {
            if (curNode.removePinBean(pin, s)) {
                log.debug("Removing sensor from pin " + pin);
            } else {
                log.debug("Failed to removing sensor from pin " + pin);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeLightManager.class);

}
