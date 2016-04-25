// XBeeTurnout.java
package jmri.jmrix.ieee802154.xbee;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turnout impelementation for XBee systems.
 * <p>
 * @author Paul Bender Copyright (C) 2014
 * @version $Revision$
 */
public class XBeeTurnout extends AbstractTurnout {

    /**
     *
     */
    private static final long serialVersionUID = 3933452737189092378L;
    private String NodeIdentifier; /* This is a string representation of
     the XBee address in the system name
     It may be an address or it may be
     the NodeIdentifier string stored in
     the NI parameter on the node.*/

    private XBeeNode node = null; // Which node does this belong too.    

    private int address;
    private int pin;         /* Which DIO pin does this turnout represent. */

    private int pin2 = -1;   /* Which 2nd DIO pin does this turnout represent. */

    private String systemName;

    protected XBeeTrafficController tc = null;

    /**
     * Create a Turnout object, with system and user names and a reference to
     * the traffic controller.
     * <P>
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
        if( !(m instanceof XBeeConnectionMemo))
        {
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
                   NodeIdentifier = systemName.substring(prefix.length() + 1, seperator);
                   if ((node = (XBeeNode) tc.getNodeFromName(NodeIdentifier)) == null) {
                       if ((node = (XBeeNode) tc.getNodeFromAddress(NodeIdentifier)) == null) {
                           try {
                               node = (XBeeNode) tc.getNodeFromAddress(Integer.parseInt(NodeIdentifier));
                           } catch (java.lang.NumberFormatException nfe) {
                               // if there was a number format exception, we couldn't
                               // find the node.
                               node = null;
                           }
                       }
                   }
                   pin = Integer.valueOf(systemName.substring(seperator + 1, seperator2 > 0 ? seperator2 : systemName.length())).intValue();
                   if (seperator2 > 0) {
                       pin2 = Integer.valueOf(systemName.substring(seperator2 + 1)).intValue();
                   }
               } catch (NumberFormatException ex) {
                   log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
               }
           } else {
               try {
                   NodeIdentifier = systemName.substring(prefix.length() + 1, id.length() - 1);
                   address = Integer.parseInt(systemName.substring(prefix.length() + 1));
                   node = (XBeeNode) tc.getNodeFromAddress(address / 10);
                   // calculate the pin to use. 
                   pin = ((address) % 10);
               } catch (NumberFormatException ex) {
                   log.debug("Unable to convert " + systemName + " Hardware Address to a number");
               }
           }
           if (log.isDebugEnabled()) {
               log.debug("Created Turnout " + systemName
                    + " (NodeIdentifier " + NodeIdentifier
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
    protected void forwardCommandChangeToLayout(int s) {
        // get message 
        XBeeMessage message = XBeeMessage.getRemoteDoutMessage(node.getPreferedTransmitAddress(), pin, s == Turnout.THROWN);
        // send the message
        tc.sendXBeeMessage(message, null);
        if (pin2 >= 0) {
            XBeeMessage message2 = XBeeMessage.getRemoteDoutMessage(node.getPreferedTransmitAddress(), pin2, s == Turnout.CLOSED);
            // send the message
            tc.sendXBeeMessage(message2, null);
        }
    }

    protected void turnoutPushbuttonLockout(boolean locked) {
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeTurnout.class.getName());
}
