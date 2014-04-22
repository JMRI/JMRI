// XBeeSensor.java

package jmri.jmrix.ieee802154.xbee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.implementation.AbstractSensor;
import jmri.Sensor;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;

/**
 * Extend jmri.AbstractSensor for XBee connections.
 * <P>
 * @author			Paul Bender Copyright (C) 2013
 * @version         $Revision$
 */
public class XBeeSensor extends AbstractSensor implements XBeeListener {


    private boolean statusRequested=false;

    private int baseaddress; /* The XBee Address */
    private String NodeIdentifier; /* This is a string representation of
                                      the XBee address in the system name
                                      It may be an address or it may be
                                      the NodeIdentifier string stored in
                                      the NI parameter on the node.*/
    private int pin;         /* Which DIO pin does this sensor represent. */
    private String systemName;

    protected XBeeTrafficController tc = null;

    public XBeeSensor(String systemName, String userName,XBeeTrafficController controller) {
        super(systemName, userName);
        tc=controller;
        init(systemName);
    }

    public XBeeSensor(String systemName,XBeeTrafficController controller) {
        super(systemName);
        tc=controller;
        init(systemName);
    }

    /**
     * Common initialization for both constructors
     */
    private void init(String id) {
        // store address
	systemName=id;
        String prefix = ((XBeeConnectionMemo)tc.getAdapterMemo())
                                         .getSensorManager().getSystemPrefix();

        if(systemName.contains(":")){
            //Address format passed is in the form of encoderAddress:input or S:sensor address
            int seperator = systemName.indexOf(":");
            try {
                NodeIdentifier = systemName.substring(prefix.length()+1,seperator);
                pin = Integer.valueOf(systemName.substring(seperator+1)).intValue();
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
            }
        } else {
           try{
              NodeIdentifier = systemName.substring(prefix.length()+1,id.length()-1);
              int address = Integer.parseInt(id.substring(prefix.length()+1,id.length()));
	      // calculate the pin to examine
              pin = ((address)%10);
           } catch (NumberFormatException ex) {
              log.debug("Unable to convert " + systemName + " Hardware Address to a number");
           }
        }
        if (log.isDebugEnabled())
        	    log.debug("Created Sensor " + systemName  + 
 				  " (NodeIdentifier " + NodeIdentifier +
                                  " ,D" + pin +
				  ")");
        // Finally, request the current state from the layout.
        //this.requestUpdateFromLayout();
        //tc.getFeedbackMessageCache().requestCachedStateFromLayout(this);
        tc.addXBeeListener(this);
    }

    /**
     * request an update on status by sending an XBee message
     */
    public void requestUpdateFromLayout() {
       // Request the sensor status from the XBee Node this sensor is
       // attached to.  NOTE: This requests status for all sensor inputs
       // on the device.
    }

    /**
     * implementing classes will typically have a function/listener to get
     * updates from the layout, which will then call
     *      public void firePropertyChange(String propertyName,
     *      					Object oldValue,
     *                                          Object newValue)
     * _once_ if anything has changed state (or set the commanded state directly)
     * @param l
     */
    public synchronized void reply(XBeeReply l) {
	if(log.isDebugEnabled()) log.debug("recieved message: " +l);

        com.rapplogic.xbee.api.XBeeResponse response=l.getXBeeResponse();

        if (response.getApiId() == ApiId.RX_64_IO_RESPONSE ||
            response.getApiId() == ApiId.RX_16_IO_RESPONSE) {
           // This message is an IO response.
           com.rapplogic.xbee.api.wpan.RxResponseIoSample ioSample = (com.rapplogic.xbee.api.wpan.RxResponseIoSample)response;

            int address[]=ioSample.getSourceAddress().getAddress();
            byte baddr[]=new byte[address.length];
            for(int i=0;i<address.length;i++)
                baddr[i]=(byte)address[i];
                             
            if(NodeIdentifier.equals(jmri.util.StringUtil.hexStringFromBytes(baddr))) {
             for (com.rapplogic.xbee.api.wpan.IoSample sample: ioSample.getSamples()) {          
                if( sample.isDigitalOn(pin) ^ _inverted) {
                   setOwnState(Sensor.ACTIVE);
                }
                else setOwnState(Sensor.INACTIVE);
             }
          }
        } else if(response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
           com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse ioSample = 
           (com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse) response;

            int address[]=ioSample.getRemoteAddress64().getAddress();
            byte baddr[]=new byte[address.length];
            for(int i=0;i<address.length;i++)
                baddr[i]=(byte)address[i];

            if(NodeIdentifier.equals(jmri.util.StringUtil.hexStringFromBytes(baddr))) {
              if( ioSample.isDigitalOn(pin) ^ _inverted) {
                  setOwnState(Sensor.ACTIVE);
              }
              else setOwnState(Sensor.INACTIVE);
           } else {
              // not what we expected
              log.debug("Ignoring mystery packet " + response.toString());
           }
        }
        return;
    }

    // listen for the messages to the Xbee 
    public void message(XBeeMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(XBeeMessage msg)
    {
       if(log.isDebugEnabled()) log.debug("Notified of timeout on message" + msg.toString());
    }

    public void dispose() {
        super.dispose();
        tc.removeXBeeListener(this);
    }

    static Logger log = LoggerFactory.getLogger(XBeeSensor.class.getName());

}


/* @(#)XBeeSensor.java */
