package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.exceptions.InterfaceNotOpenException;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.io.IOLine;
import com.digi.xbee.api.io.IOValue;
import jmri.Light;
import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Light implementation for XBee systems.
 *
 * @author Paul Bender Copyright (C) 2014
 */
public class XBeeLight extends AbstractLight {

    private String nodeIdentifier; /* This is a string representation of
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
     * @param systemName Xbee system id : pin id
     * @param userName User friendly name
     * @param controller tc for connection for this node
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
        jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo m = tc.getAdapterMemo();
        if( !(m instanceof XBeeConnectionMemo))
        {
           log.error("Memo associated with the traffic controller is not the right type");
           throw new IllegalArgumentException("Memo associated with the traffic controller is not the right type");
        } else {
           XBeeConnectionMemo memo = (XBeeConnectionMemo) m;
           String prefix = memo.getLightManager().getSystemPrefix();
           if (systemName.contains(":")) {
               //Address format passed is in the form of encoderAddress:input or L:light address
               int seperator = systemName.indexOf(":");
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
                          }
                       }
                   }
                   pin = Integer.parseInt(systemName.substring(seperator + 1));
               } catch (NumberFormatException ex) {
                   log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
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
               }
           }
           if (log.isDebugEnabled()) {
               log.debug("Created Light " + systemName
                    + " (NodeIdentifier " + nodeIdentifier
                    + " D" + pin
                    + ")");
           }
        }
    }

    @Override
    protected void doNewState(int oldState, int newState) {
        try  {
            if((newState == Light.ON) ) {
              node.getXBee().setDIOValue(IOLine.getDIO(pin),IOValue.HIGH);
            } else {
              node.getXBee().setDIOValue(IOLine.getDIO(pin),IOValue.LOW);
            }
        } catch (TimeoutException toe) {
           log.error("Timeout setting IO line value for light {} on {}",getUserName(),node.getXBee());
        } catch (InterfaceNotOpenException ino) {
           log.error("Interface Not Open setting IO line value for light {} on {}",getUserName(),node.getXBee());
        } catch (XBeeException xbe) {
           log.error("Error setting IO line value for light {} on {}",getUserName(),node.getXBee());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeLight.class);
}
