// XBeeSensor.java
package jmri.jmrix.ieee802154.xbee;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.RemoteAtResponse;
import com.rapplogic.xbee.api.wpan.IoSample;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;
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
public class XBeeSensor extends AbstractSensor implements XBeeListener {

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
           tc.addXBeeListener(this);
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

    /**
     * implementing classes will typically have a function/listener to get
     * updates from the layout, which will then call public void
     * firePropertyChange(String propertyName, Object oldValue, Object newValue)
     * _once_ if anything has changed state (or set the commanded state
     * directly)
     *
     */
    public synchronized void reply(XBeeReply l) {
        if (log.isDebugEnabled()) {
            log.debug("recieved message: " + l);
        }

        com.rapplogic.xbee.api.XBeeResponse response = l.getXBeeResponse();

        if (response.getApiId() == ApiId.RX_64_IO_RESPONSE
                || response.getApiId() == ApiId.RX_16_IO_RESPONSE) {
            // This message is an IO response.
            RxResponseIoSample ioSample = (RxResponseIoSample) response;

            int address[] = ioSample.getSourceAddress().getAddress();
            XBeeNode sourcenode = (XBeeNode) tc.getNodeFromAddress(address);

            if (node.equals(sourcenode)) {
                for (IoSample sample : ioSample.getSamples()) {
                    if (sample.isDigitalOn(pin) ^ _inverted) {
                        setOwnState(Sensor.ACTIVE);
                    } else {
                        setOwnState(Sensor.INACTIVE);
                    }
                }
            }
        } else if (response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
            com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse ioSample
                    = (com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse) response;

            int address[] = ioSample.getRemoteAddress64().getAddress();
            XBeeNode sourcenode = (XBeeNode) tc.getNodeFromAddress(address);

            if (node.equals(sourcenode)) {
                if (ioSample.isDigitalOn(pin) ^ _inverted) {
                    setOwnState(Sensor.ACTIVE);
                } else {
                    setOwnState(Sensor.INACTIVE);
                }
            } else {
                // not what we expected
                log.debug("Ignoring mystery packet " + response.toString());
            }
        } else if (response instanceof RemoteAtResponse) {
            RemoteAtResponse atResp = (RemoteAtResponse) response;
            XBeeNode sourcenode = (XBeeNode) tc.getNodeFromAddress(atResp.getRemoteAddress64().getAddress());
            if (node.equals(sourcenode)) {
                if (atResp.getCommand().equals("IS")) {
                    try {
                        ZNetRxIoSampleResponse ioSample = ZNetRxIoSampleResponse.parseIsSample(atResp);
                        if (ioSample.isDigitalOn(pin) ^ _inverted) {
                            setOwnState(Sensor.ACTIVE);
                        } else {
                            setOwnState(Sensor.INACTIVE);
                        }
                    } catch (java.io.IOException ioe) {
                        // parse error, wrong format.
                        log.debug("Caught IOException parsing IS packet");
                    } catch (java.lang.IllegalStateException ise) {
                        // is this a series 1 packet?
                        log.debug("Caught IllegalStateException parsing IS packet");
                        RxResponseIoSample rxSample = new RxResponseIoSample();
                        // don't need sampleSize now, might later.
                        //int sampleSize = atResp.getValue()[0];
                        rxSample.setChannelIndicator1(atResp.getValue()[1]);
                        rxSample.setChannelIndicator2(atResp.getValue()[2]);
                        IoSample sample = new IoSample(rxSample);
                        sample.setDioMsb(atResp.getValue()[3]);
                        sample.setDioLsb(atResp.getValue()[4]);
                        if (sample.isDigitalOn(pin) ^ _inverted) {
                           setOwnState(Sensor.ACTIVE);
                        } else {
                           setOwnState(Sensor.INACTIVE);
                        }
                    }
                }
            }
        }
        return;
    }

    // listen for the messages to the Xbee 
    public void message(XBeeMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(XBeeMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    public void dispose() {
        super.dispose();
        tc.removeXBeeListener(this);
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeSensor.class.getName());

}


/* @(#)XBeeSensor.java */
