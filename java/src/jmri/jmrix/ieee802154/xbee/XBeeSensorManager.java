// XBeeSensorManager.java

package jmri.jmrix.ieee802154.xbee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Sensor;
import jmri.JmriException;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeAddress;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;

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
        XBeeNode curNode = (XBeeNode) tc.getNodeFromAddress(
                      addressFromSystemName(systemName));
        int pin = pinFromSystemName(systemName);
        if(curNode !=null && !curNode.getPinAssigned(pin)) {
           curNode.setPinBean(pin,new XBeeSensor(systemName, userName,tc));
           return (XBeeSensor) curNode.getPinBean(pin);
        } else {
           log.debug("Failed to create sensor " + systemName);
           return null;
        }
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
    				// Sensor name is prefix followed by 16 bit address
    				// followed by the bit number.
    				String sName = prefix + typeLetter() + ((XBeeAddress16)ioSample.getSourceAddress()).get16BitValue() + ":" + i;
    				XBeeSensor s = (XBeeSensor) getSensor(sName);
    				if (s == null) {
    					s = (XBeeSensor) provideSensor(sName);
    					s.reply(l);
        				if(log.isDebugEnabled()) log.debug("DIO " +  sName + " enabled as sensor");
    				}
    			}
    		}
    	} else if(response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
    		com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse ioSample =
    				(com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse) response;

    		// series 2 xbees can go up to 12.  We'll leave it at 8 like
    		// the series 1 xbees to start with.
    		for (int i=0;i<=7;i++) {
    			if( ioSample.isDigitalEnabled(i)) {
    				// Sensor name is prefix followed by 16 bit address
    				// followed by the bit number.
    				XBeeAddress16 xBeeAddr = ioSample.getRemoteAddress16();
    				int addr = ((xBeeAddr.getMsb() << 8) & 0xff00) + xBeeAddr.getLsb();
    				String sName = prefix + typeLetter() + addr + ":" + i;
    				XBeeSensor s = (XBeeSensor) getSensor(sName);
    				if (s == null) {
    					s = (XBeeSensor) provideSensor(sName);
						s.reply(l);
        				if(log.isDebugEnabled()) log.debug("DIO " +  sName + " enabled as sensor");
    				}
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
    
    // for now, set this to false. multiple additions currently works
    // partially, but not for all possible cases.
    public boolean allowMultipleAdditions(String systemName) { return false;  }
    
    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException{
        int encoderAddress = addressFromSystemName(prefix + typeLetter() +curAddress);
        int input = pinFromSystemName(prefix +typeLetter() + curAddress);
       
        if(encoderAddress <0 )
           throw new JmriException("Hardware Address passed should be a number");
        return prefix+typeLetter()+encoderAddress+":"+input;
    }

    private int addressFromSystemName(String systemName) {
        int encoderAddress = 0;
        int input = 0;
        int iName = 0;

        if(systemName.contains(":")){
            //Address format passed is in the form of encoderAddress:input or L:light address
            int seperator = systemName.indexOf(":");
            try {
                encoderAddress = Integer.valueOf(systemName.substring(getSystemPrefix().length()+1,seperator)).intValue();
                input = Integer.valueOf(systemName.substring(seperator+1)).intValue();
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
                return -1;
            }
         } else {
            try{
                iName = Integer.parseInt(systemName.substring(getSystemPrefix().length()+1));
                encoderAddress = iName/10;
                input = iName % 10;
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert " + systemName + " Hardware Address to a number");
                return -1;
            }
        }
        if (log.isDebugEnabled()) log.debug("Converted " + systemName + " to hardware address " + encoderAddress + " pin " + input);
        return encoderAddress;
    }
    
    private int pinFromSystemName(String systemName) {
        int encoderAddress = 0;
        int input = 0;
        int iName = 0;

        if(systemName.contains(":")){
            //Address format passed is in the form of encoderAddress:input or L:light address
            int seperator = systemName.indexOf(":");
            try {
                encoderAddress = Integer.valueOf(systemName.substring(getSystemPrefix().length()+1,seperator)).intValue();
                input = Integer.valueOf(systemName.substring(seperator+1)).intValue();
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
                return -1;
            }
         } else {
            try{
                iName = Integer.parseInt(systemName.substring(getSystemPrefix().length()+1));
                encoderAddress = iName/10;
                input = iName % 10;
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert " + systemName + " Hardware Address to a number");
                return -1;
            }
        }
        if (log.isDebugEnabled()) log.debug("Converted " + systemName + " to hardware address " + encoderAddress);
        return input;
    }


    
    static Logger log = LoggerFactory.getLogger(XBeeSensorManager.class.getName());

}

/* @(#)XBeeSensorManager.java */
