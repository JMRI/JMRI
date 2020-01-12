package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.exceptions.InterfaceNotOpenException;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.io.IOLine;
import com.digi.xbee.api.io.IOValue;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turnout implementation for XBee systems.
 *
 * @author Paul Bender Copyright (C) 2014
 */
public class XBeeTurnout extends AbstractTurnout {

    private String nodeIdentifier;
    /* This is a string representation of
     the XBee address in the system name
     It may be an address or it may be
     the NodeIdentifier string stored in
     the NI parameter on the node.*/

    private XBeeNode node = null; // Which node does this belong too.

    private int address;
    private int pin;
    /* Which DIO pin does this turnout represent. */

    private int pin2 = -1;
    /* Which 2nd DIO pin does this turnout represent. */

    private String systemName;

    protected XBeeTrafficController tc = null;

    /**
     * Create a Turnout object, with system and user names and a reference to
     * the traffic controller.
     *
     * @param systemName Xbee id : pin
     * @param userName   friendly text name
     * @param controller tc for node connection
     */
    public XBeeTurnout(String systemName, String userName, XBeeTrafficController controller) {
        super(systemName, userName);
        tc = controller;
        init(systemName);
    }

    public XBeeTurnout(String systemName, XBeeTrafficController controller) {
        super(systemName);
        tc = controller;
        init(systemName);
    }

    /**
     * Common initialization for both constructors
     */
    private void init(String id) {
        // store address
        systemName = id;
        jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo m = tc.getAdapterMemo();
        if (!(m instanceof XBeeConnectionMemo)) {
            log.error("Memo associated with the traffic controller is not the right type");
            throw new IllegalArgumentException("Memo associated with the traffic controller is not the right type");
        } else {
            XBeeConnectionMemo memo = (XBeeConnectionMemo) m;
            String prefix = memo.getTurnoutManager().getSystemPrefix();
            if (systemName.contains(":")) {
                //Address format passed is in the form of encoderAddress:output or T:turnout address
                int seperator = systemName.indexOf(":");
                int seperator2 = systemName.indexOf(":", seperator + 1);
                try {
                    nodeIdentifier = systemName.substring(prefix.length() + 1, seperator);
                    if ((node = (XBeeNode) tc.getNodeFromName(nodeIdentifier)) == null) {
                        if ((node = (XBeeNode) tc.getNodeFromAddress(nodeIdentifier)) == null) {
                            try {
                                node = (XBeeNode) tc.getNodeFromAddress(Integer.parseInt(nodeIdentifier));
                            } catch (java.lang.NumberFormatException nfe) {
                                // if there was a number format exception, we couldn't
                                // find the node.
                                node = null;
                                throw new IllegalArgumentException("Node not defined");
                            }
                        }
                    }
                    pin = Integer.parseInt(systemName.substring(seperator + 1, seperator2 > 0 ? seperator2 : systemName.length()));
                    if (seperator2 > 0) {
                        pin2 = Integer.parseInt(systemName.substring(seperator2 + 1));
                    }
                } catch (NumberFormatException ex) {
                    log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
                    throw new IllegalArgumentException("Unable to convert " + systemName + " into the cab and input format of nn:xx");
                }
            } else {
                try {
                    nodeIdentifier = systemName.substring(prefix.length() + 1, id.length() - 1);
                    address = Integer.parseInt(systemName.substring(prefix.length() + 1));
                    node = (XBeeNode) tc.getNodeFromAddress(address / 10);
                    // calculate the pin to use.
                    pin = ((address) % 10);
                } catch (NumberFormatException ex) {
                    log.debug("Unable to convert " + systemName + " Hardware Address to a number");
                    throw new IllegalArgumentException("Unable to convert " + systemName + " Hardware Address to a number");
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Created Turnout " + systemName
                        + " (NodeIdentifier " + nodeIdentifier
                        + " D" + pin
                        + (pin2 > 0 ? " D" + pin2 : "")
                        + ")");
            }
        }
    }

    /**
     * Handle a request to change state, typically by sending a message to the
     * layout in some child class. Public version (used by TurnoutOperator)
     * sends the current commanded state without changing it.
     *
     * @param s new state value
     */
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        try {
            if ((s == Turnout.THROWN) ^ getInverted()) {
                node.getXBee().setDIOValue(IOLine.getDIO(pin), IOValue.HIGH);
            } else {
                node.getXBee().setDIOValue(IOLine.getDIO(pin), IOValue.LOW);
            }

            if (pin2 >= 0) {
                if ((s == Turnout.CLOSED) ^ getInverted()) {
                    node.getXBee().setDIOValue(IOLine.getDIO(pin), IOValue.HIGH);
                } else {
                    node.getXBee().setDIOValue(IOLine.getDIO(pin), IOValue.LOW);
                }

            }
        } catch (TimeoutException toe) {
            log.error("Timeout setting IO line value for turnout {} on {}", getUserName(), node.getXBee());
        } catch (InterfaceNotOpenException ino) {
            log.error("Interface Not Open setting IO line value for turnout {} on {}", getUserName(), node.getXBee());
        } catch (XBeeException xbe) {
            log.error("Error setting IO line value for turout {} on {}", getUserName(), node.getXBee());
        }

    }

    /**
     * XBee turnouts do support inversion
     */
    @Override
    public boolean canInvert() {
        return true;
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean locked) {
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeTurnout.class);
}
