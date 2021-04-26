package jmri.jmrix.ieee802154.xbee;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for XBee connections
 *
 * @author Paul Bender Copyright (C) 2014
 */
public class XBeeTurnoutManager extends AbstractTurnoutManager {

    protected XBeeTrafficController tc = null;

    public XBeeTurnoutManager(XBeeConnectionMemo memo) {
        super(memo);
        tc = (XBeeTrafficController) memo.getTrafficController();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public XBeeConnectionMemo getMemo() {
        return (XBeeConnectionMemo) memo;
    }

    // for now, set this to false. Multiple additions currently works
    // partially, but not for all possible cases.
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        XBeeNode curNode;
        String name = addressFromSystemName(systemName);
        if ((curNode = (XBeeNode) tc.getNodeFromName(name)) == null) {
            if ((curNode = (XBeeNode) tc.getNodeFromAddress(name)) == null) {
                try {
                    curNode = (XBeeNode) tc.getNodeFromAddress(Integer.parseInt(name));
                } catch (java.lang.NumberFormatException nfe) {
                    // if there was a number format exception, we couldn't
                    // find the node.
                    log.debug("failed to create turnout {}", systemName);
                    throw new IllegalArgumentException("Cannot find Node, check Turnout System Name " + systemName);
                }
            }
        }
        int pin = pinFromSystemName(systemName);
        int pin2 = pin2FromSystemName(systemName);
        if (!curNode.getPinAssigned(pin)
                && (pin2 == -1 || !curNode.getPinAssigned(pin2))) {
            log.debug("Adding turnout to pin {}", pin);
            curNode.setPinBean(pin, new XBeeTurnout(systemName, userName, tc));
            if (pin2 != -1) {
                curNode.setPinBean(pin2, curNode.getPinBean(pin));
            }
            return (XBeeTurnout) curNode.getPinBean(pin);
        } else {
            log.debug("failed to create turnout {}", systemName);
            throw new IllegalArgumentException("Cannot create Turnout " + systemName + ", check pins.");
        }
    }

    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        return prefix + typeLetter() + curAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        super.validateSystemNameFormat(name, locale);
        int pin = pinFromSystemName(name);
        int pin2 = pin2FromSystemName(name);
        if (pin < 0 || pin > 7) {
            throw new NamedBean.BadSystemNameException(
                    Bundle.getMessage(Locale.ENGLISH, "SystemNameInvalidPin", name),
                    Bundle.getMessage(locale, "SystemNameInvalidPin", name));
        }
        if (pin2 != -1 && (pin2 < 0 || pin2 > 7)) {
            throw new NamedBean.BadSystemNameException(
                    Bundle.getMessage(Locale.ENGLISH, "SystemNameInvalidPin", name),
                    Bundle.getMessage(locale, "SystemNameInvalidPin", name));
        }
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        if (tc.getNodeFromName(addressFromSystemName(systemName)) == null
                && tc.getNodeFromAddress(addressFromSystemName(systemName)) == null) {
            try {
                if (tc.getNodeFromAddress(Integer.parseInt(addressFromSystemName(systemName))) == null) {
                    return NameValidity.INVALID;
                } else {
                    return (pinFromSystemName(systemName) >= 0
                            && pinFromSystemName(systemName) <= 7
                            && (pin2FromSystemName(systemName) == -1
                            || (pin2FromSystemName(systemName) >= 0
                            && pin2FromSystemName(systemName) <= 7))) ? NameValidity.VALID : NameValidity.INVALID;
                }
            } catch (java.lang.NumberFormatException nfe) {
                // if there was a number format exception, we couldn't
                // find the node.
                log.error("Unable to convert {} into the Xbee node and pin format of nn:xx", systemName);
                return NameValidity.INVALID;
            }

        } else {
            return (pinFromSystemName(systemName) >= 0
                    && pinFromSystemName(systemName) <= 7
                    && (pin2FromSystemName(systemName) == -1
                    || (pin2FromSystemName(systemName) >= 0
                    && pin2FromSystemName(systemName) <= 7))) ? NameValidity.VALID : NameValidity.INVALID;
        }
    }

    private String addressFromSystemName(@Nonnull String systemName) {
        String encoderAddress;

        if (systemName.contains(":")) {
            //Address format passed is in the form of encoderAddress:input or S:turnout address
            int seperator = systemName.indexOf(":");
            encoderAddress = systemName.substring(getSystemPrefix().length() + 1, seperator);
        } else {
            encoderAddress = systemName.substring(getSystemPrefix().length() + 1, systemName.length() - 1);
        }
        log.debug("Converted {} to hardware address {}", systemName, encoderAddress);
        return encoderAddress;
    }

    private int pinFromSystemName(@Nonnull String systemName) {
        int input = 0;
        int iName = 0;

        if (systemName.contains(":")) {
            //Address format passed is in the form of encoderAddress:input or T:turnout address
            int seperator = systemName.indexOf(":");
            int seperator2 = systemName.indexOf(":", seperator + 1);
            int len = systemName.length();
            try {
                if ((seperator2 >= 0) && (seperator2 <= len)) {
                    input = Integer.parseInt(systemName.substring(seperator + 1, seperator2));
                } else {
                    input = Integer.parseInt(systemName.substring(seperator + 1, len));
                }
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

    private int pin2FromSystemName(@Nonnull String systemName) {
        int input = 0;

        if (systemName.contains(":")) {
            //Address format passed is in the form of encoderAddress:input or T:turnout address
            int seperator = systemName.indexOf(":");
            int seperator2 = systemName.indexOf(":", seperator + 1);
            try {
                input = Integer.parseInt(systemName.substring(seperator2 + 1));
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert {} into the cab and input format of nn:xx", systemName);
                return -1;
            }
        } else {
            // no ":" means only one pin.
            input = -1;
        }
        log.debug("Converted {} to pin number {}", systemName, input);
        return input;
    }

    @Override
    public void deregister(@Nonnull jmri.Turnout t) {
        super.deregister(t);
        // remove the specified turnout from the associated XBee pin.
        String systemName = t.getSystemName();
        String name = addressFromSystemName(systemName);
        int pin = pinFromSystemName(systemName);
        XBeeNode curNode;
        if ((curNode = (XBeeNode) tc.getNodeFromName(name)) == null) {
            if ((curNode = (XBeeNode) tc.getNodeFromAddress(name)) == null) {
                try {
                    curNode = (XBeeNode) tc.getNodeFromAddress(Integer.parseInt(name));
                } catch (java.lang.NumberFormatException nfe) {
                    // if there was a number format exception, we couldn't find the node.
                    curNode = null;
                }
            }
        }
        if (curNode != null) {
            if (curNode.removePinBean(pin, t)) {
                log.debug("Removing turnout from pin {}", pin);
            } else {
                log.debug("Failed to removing turnout from pin {}", pin);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddTurnoutEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeTurnoutManager.class);

}
