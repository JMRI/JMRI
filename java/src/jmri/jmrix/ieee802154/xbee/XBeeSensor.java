// XBeeSensor.java
package jmri.jmrix.ieee802154.xbee;

import com.rapplogic.xbee.api.ApiId;
import com.digi.xbee.api.packet.common.RemoteATCommandResponsePacket;
import com.digi.xbee.api.io.IOSample;
import com.digi.xbee.api.packet.common.IODataSampleRxIndicatorPacket;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.io.IOLine;
import com.digi.xbee.api.io.IOValue;
import com.digi.xbee.api.listeners.IIOSampleReceiveListener;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;

import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractSensor for XBee connections.
 * <P>
 * @author	Paul Bender Copyright (C) 2013
 * @version $Revision$
 */
public class XBeeSensor extends AbstractSensor implements IIOSampleReceiveListener {

    /**
     *
     */
    private static final long serialVersionUID = -1015365837178480016L;
    private String NodeIdentifier; /* This is a string representation of
     the XBee address in the system name
     It may be an address or it may be
     the NodeIdentifier string stored in
     the NI parameter on the node.*/

    private int pin;         /* Which DIO pin does this sensor represent. */

    private XBeeNode node = null; // Which node does this belong too.
    private String systemName;

    protected XBeeTrafficController tc = null;

    public XBeeSensor(String systemName, String userName, XBeeTrafficController controller) {
        super(systemName, userName);
        tc = controller;
        init(systemName);
    }

    public XBeeSensor(String systemName, XBeeTrafficController controller) {
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
           String prefix = memo.getSensorManager().getSystemPrefix();

           if (systemName.contains(":")) {
               //Address format passed is in the form of encoderAddress:input or S:sensor address
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
                   int address = Integer.parseInt(id.substring(prefix.length() + 1, id.length()));
                   node = (XBeeNode) tc.getNodeFromAddress(address / 10);
                   // calculate the pin to examine
                   pin = ((address) % 10);
               } catch (NumberFormatException ex) {
                   log.debug("Unable to convert " + systemName + " Hardware Address to a number");
               }
           }
           if (log.isDebugEnabled()) {
               log.debug("Created Sensor " + systemName
                    + " (NodeIdentifier " + NodeIdentifier
                    + " ,D" + pin
                    + ")");
           }
           // Finally, request the current state from the layout.
           this.requestUpdateFromLayout();
        } 
    }

    /**
     * request an update on status by sending an XBee message
     */
    public void requestUpdateFromLayout() {
        // Request the sensor status from the XBee Node this sensor is
        // attached to.  NOTE: This requests status for all sensor inputs
        // on the device.
        XBeeMessage msg = XBeeMessage.getForceSampleMessage(node.getPreferedTransmitAddress());
        // send the message
        tc.sendXBeeMessage(msg, null);

    }


    // IIOSampleReceiveListener methods
    public synchronized void ioSampleReceived(RemoteXBeeDevice remoteDevice,IOSample ioSample) {
        if (log.isDebugEnabled()) {
            log.debug("recieved io sample {} from {}",ioSample,remoteDevice);
        }

        int address[] = remoteDevice.get16BitSourceAddress().getValue();
        XBeeNode sourcenode = (XBeeNode) tc.getNodeFromXBeeDevice(remoteDevice);

        if (node.equals(sourcenode)) {
          if ( ioSample.hasDigitalValues()){
              if ((ioSample.getDigitalValue(IOLine.getDIO(pin))==IOValue.HIGH) ^ _inverted) {
                 setOwnState(Sensor.ACTIVE);
             } else {
                 setOwnState(Sensor.INACTIVE);
             }
          }
        }
        return;
    }

    // Handle a timeout notification
    public void notifyTimeout(XBeeMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    public void dispose() {
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeSensor.class.getName());

}


/* @(#)XBeeSensor.java */
