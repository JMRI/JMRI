// XBeeLight.java
package jmri.jmrix.ieee802154.xbee;

import jmri.Light;
import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Light impelementation for XBee systems.
 * <p>
 * @author Paul Bender Copyright (C) 2014
 * @version $Revision$
 */
public class XBeeLight extends AbstractLight {

    /**
     *
     */
    private static final long serialVersionUID = 6879258557909355535L;
    private String NodeIdentifier; /* This is a string representation of
     the XBee address in the system name
     It may be an address or it may be
     the NodeIdentifier string stored in
     the NI parameter on the node.*/

    private XBeeNode node = null; // Which node does this belong too.    

    private int address;
    private int pin;         /* Which DIO pin does this light represent. */

    private String systemName;

    protected XBeeTrafficController tc = null;

    /**
     * Create a Light object, with system and user names and a reference to the
     * traffic controller.
     * <P>
     */
    public XBeeLight(String systemName, String userName, XBeeTrafficController controller) {
        super(systemName, userName);
        tc = controller;
        init(systemName);
    }

    public XBeeLight(String systemName, XBeeTrafficController controller) {
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
        if( !(tc.getAdapterMemo() instanceof XBeeConnectionMemo)) 
        {
           log.error("Memo associated with the traffic controller is not the right type");
           throw new IllegalArgumentException("Memo associated with the traffic controller is not the right type");
        }
        XBeeConnectionMemo memo = (XBeeConnectionMemo) tc.getAdapterMemo();
        String prefix = ((XBeeConnectionMemo) (tc.getAdapterMemo())).getLightManager().getSystemPrefix();
        if (systemName.contains(":")) {
            //Address format passed is in the form of encoderAddress:input or L:light address
            int seperator = systemName.indexOf(":");
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
                pin = Integer.valueOf(systemName.substring(seperator + 1)).intValue();
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
            log.debug("Created Light " + systemName
                    + " (NodeIdentifier " + NodeIdentifier
                    + " D" + pin
                    + ")");
        }
    }

    protected void doNewState(int oldState, int newState) {
        // get message 
        XBeeMessage message = XBeeMessage.getRemoteDoutMessage(node.getPreferedTransmitAddress(), pin, newState == Light.ON);
        // send the message
        tc.sendXBeeMessage(message, null);
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeLight.class.getName());
}
