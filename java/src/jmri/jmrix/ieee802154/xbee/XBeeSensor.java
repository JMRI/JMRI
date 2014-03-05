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

    private int address;
    private int baseaddress; /* The XBee Address */
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
        address = Integer.parseInt(id.substring(2,id.length()));
	// calculate the base address, the nibble, and the bit to examine
	baseaddress = ((address) / 10);
        pin = ((address)%10);
        if (log.isDebugEnabled())
        	log.debug("Created Sensor " + systemName  + 
 				  " (Address " + baseaddress + 
                                  " D" + pin +
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

        if (response.getApiId() == ApiId.RX_16_IO_RESPONSE) {
           // This message is an IO response with a 16 bit address.
           com.rapplogic.xbee.api.wpan.RxResponseIoSample ioSample = (com.rapplogic.xbee.api.wpan.RxResponseIoSample)response;
                                               
            if(baseaddress==((XBeeAddress16)ioSample.getSourceAddress()).get16BitValue()) {
             for (com.rapplogic.xbee.api.wpan.IoSample sample: ioSample.getSamples()) {          
                if( sample.isDigitalOn(pin) ^ _inverted) {
                   setOwnState(Sensor.ACTIVE);
                }
                else setOwnState(Sensor.INACTIVE);
             }
          }
        } else if (response.getApiId() == ApiId.RX_64_IO_RESPONSE) {
           // This message is an IO response with a 64 bit address.
           com.rapplogic.xbee.api.wpan.RxResponseIoSample ioSample = (com.rapplogic.xbee.api.wpan.RxResponseIoSample)response;
                                               
            //if(baseaddress==((XBeeAddress64)ioSample.getSourceAddress()).get16BitValue()) {
            // for (com.rapplogic.xbee.api.wpan.IoSample sample: ioSample.getSamples()) {          
            //    if( sample.isDigitalOn(pin) ^ _inverted) {
            //       setOwnState(Sensor.ACTIVE);
            //    }
            //    else setOwnState(Sensor.INACTIVE);
           //  }
          //}
        } else if(response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
           com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse ioSample = 
           (com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse) response;
           if(baseaddress==ioSample.getRemoteAddress16().get16BitValue()) {
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

    // package protected routine to get the Sensor Number
    int getNumber() { return address; }

    // package protected routine to get the Sensor Base Address
    int getBaseAddress() { return baseaddress; }

    static Logger log = LoggerFactory.getLogger(XBeeSensor.class.getName());

}


/* @(#)XBeeSensor.java */
