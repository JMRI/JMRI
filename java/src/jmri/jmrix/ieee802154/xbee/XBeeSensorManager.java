// XBeeSensorManager.java

package jmri.jmrix.ieee802154.xbee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Sensor;
import jmri.JmriException;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;

/**
 * Manage the XBee specific Sensor implementation.
 *
 * System names are "XSnnn", where nnn is the sensor number without padding.
 *
 * @author			Paul Bender Copyright (C) 2003-2010
 * @version			$Revision$
 */
public class XBeeSensorManager extends jmri.managers.AbstractSensorManager implements XBeeListener {

    public String getSystemPrefix() { return prefix; }
    protected String prefix = null;

    protected XBeeTrafficController tc = null;

    @Deprecated
    static public XBeeSensorManager instance() {
        return mInstance;
    }
    static private XBeeSensorManager mInstance = null;

    // to free resources when no longer used
    public void dispose() {
        tc.removeXBeeListener(this);
        super.dispose();
    }

    // XBee specific methods

    public Sensor createNewSensor(String systemName, String userName) {
        return new XBeeSensor(systemName, userName,tc);
    }

    // ctor has to register for XBee events
    public XBeeSensorManager(XBeeTrafficController controller,String prefix) {
        tc=controller;
        tc.addXBeeListener(this);
        this.prefix=prefix;
    }

    // listen for sensors, creating them as needed
    public void reply(XBeeReply l) {
	if(log.isDebugEnabled()) log.debug("recieved message: " +l);

        com.rapplogic.xbee.api.XBeeResponse response=l.getXBeeResponse();

        if (response.getApiId() == ApiId.RX_16_IO_RESPONSE) {
           com.rapplogic.xbee.api.wpan.RxResponseIoSample ioSample = (com.rapplogic.xbee.api.wpan.RxResponseIoSample)response;

           for (int i=0;i<=8;i++) {
                if( ioSample.isDigitalEnabled(i)) {
                   if(log.isDebugEnabled()) log.debug("DIO"+i + "enabled as sensor");
                   // Sensor name is prefix followed by 16 bit address
                   // followed by the bit number.
                   provideSensor(prefix +typeLetter() + ((XBeeAddress16)ioSample.getSourceAddress()).get16BitValue() + "" + i);
                }
           }
        } else if(response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
           com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse ioSample =
           (com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse) response;

           // series 2 xbees can go up to 12.  We'll leave it at 8 like
           // the series 1 xbees to start with.
           for (int i=0;i<=8;i++) {
                if( ioSample.isDigitalEnabled(i)) {
                   if(log.isDebugEnabled()) log.debug("DIO"+i + "enabled as sensor");
                   // Sensor name is prefix followed by 16 bit address
                   // followed by the bit number.
                   provideSensor(prefix +typeLetter() + 
                       ((com.rapplogic.xbee.api.zigbee.ZNetRxBaseResponse)response).getRemoteAddress16() + i);
                }
           }
        } else {
           // not what we expected
           log.debug("Ignoring mystery packet " + response.toString());
        }

    }

    // listen for the messages to the XBee  
    public void message(XBeeMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(XBeeMessage msg)
    {
       if(log.isDebugEnabled()) log.debug("Notified of timeout on message" + msg.toString());
    }
    
    public boolean allowMultipleAdditions(String systemName) { return true;  }
    
    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException{
        int encoderAddress = 0;
        int input = 0;
        int iName = 0;
        
        if(curAddress.contains(":")){
            //Address format passed is in the form of encoderAddress:input or T:sensor address
            int seperator = curAddress.indexOf(":");
            try {
                encoderAddress = Integer.valueOf(curAddress.substring(0,seperator)).intValue();
                input = Integer.valueOf(curAddress.substring(seperator+1)).intValue();
            } catch (NumberFormatException ex) { 
                log.error("Unable to convert " + curAddress + " into the cab and input format of nn:xx");
                throw new JmriException("Hardware Address passed should be a number");
            }
            iName = ((encoderAddress)*10)+input;  // TODO: This will work fine for Series 1.  May not work For series 2 (can't represent address 10 and 11 on these nodes unambiguously in base 10.. i.e. is sensor 111 sensor 1 on node 11 or sensor 11 on node 1?).  Since the side effect value of iName is used in getNextValidAddress, this is an issue that needs to be addressed in a more general manner (probably not just for XBee connections).
        } else {
            //Entered in using the old format
            try {
                iName = Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) { 
                log.error("Unable to convert " + curAddress + " Hardware Address to a number");
                throw new JmriException("Hardware Address passed should be a number");
            }
        }
        
        return prefix+typeLetter()+iName;
    }
    
    static Logger log = LoggerFactory.getLogger(XBeeSensorManager.class.getName());

}

/* @(#)XBeeSensorManager.java */
