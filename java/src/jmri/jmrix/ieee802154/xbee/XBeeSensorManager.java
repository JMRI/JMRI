// XBeeSensorManager.java
package jmri.jmrix.ieee802154.xbee;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.RemoteAtResponse;
import com.rapplogic.xbee.api.XBeeAddress64;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the XBee specific Sensor implementation.
 *
 * System names are "ZSnnn", where nnn is the sensor number without padding. or
 * "ZSstring:pin", where string is a node address and pin is the io pin used.
 *
 * @author	Paul Bender Copyright (C) 2003-2010
 * @version	$Revision$
 */
public class XBeeSensorManager extends jmri.managers.AbstractSensorManager implements XBeeListener {

    public String getSystemPrefix() {
        return prefix;
    }
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
        XBeeNode curNode = null;
        String name = addressFromSystemName(systemName);
        if ((curNode = (XBeeNode) tc.getNodeFromName(name)) == null) {
            if ((curNode = (XBeeNode) tc.getNodeFromAddress(name)) == null) {
                try {
                    curNode = (XBeeNode) tc.getNodeFromAddress(Integer.parseInt(name));
                } catch (java.lang.NumberFormatException nfe) {
                    // if there was a number format exception, we couldn't
                    // find the node.
                    curNode = null;
                }
            }
        }
        int pin = pinFromSystemName(systemName);
        if (curNode != null && !curNode.getPinAssigned(pin)) {
            log.debug("Adding sensor to pin " + pin);
            curNode.setPinBean(pin, new XBeeSensor(systemName, userName, tc));
            return (XBeeSensor) curNode.getPinBean(pin);
        } else {
            log.debug("Failed to create sensor " + systemName);
            return null;
        }
    }

    // ctor has to register for XBee events
    public XBeeSensorManager(XBeeTrafficController controller, String prefix) {
        tc = controller;
        tc.addXBeeListener(this);
        this.prefix = prefix;
    }

    // listen for sensors, creating them as needed
    public void reply(XBeeReply l) {
        if (log.isDebugEnabled()) {
            log.debug("recieved message: " + l);
        }

        com.rapplogic.xbee.api.XBeeResponse response = l.getXBeeResponse();

        if (response.getApiId() == ApiId.RX_64_IO_RESPONSE
                || response.getApiId() == ApiId.RX_16_IO_RESPONSE) {
            com.rapplogic.xbee.api.wpan.RxResponseIoSample ioSample = (com.rapplogic.xbee.api.wpan.RxResponseIoSample) response;

            int address[] = ioSample.getSourceAddress().getAddress();
            XBeeNode node = (XBeeNode) tc.getNodeFromAddress(address);

            for (int i = 0; i <= 8; i++) {
                if (!node.getPinAssigned(i)
                        && ioSample.isDigitalEnabled(i)) {
                    // request pin direction.
                    tc.sendXBeeMessage(XBeeMessage.getRemoteDoutMessage(node.getPreferedTransmitAddress(), i), this);
                }
            }
        } else if (response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
            com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse ioSample
                    = (com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse) response;

            XBeeAddress64 xBeeAddr = ioSample.getRemoteAddress64();
            XBeeNode node = (XBeeNode) tc.getNodeFromAddress(xBeeAddr.getAddress());

            // series 2 xbees can go up to 12.  We'll leave it at 8 like
            // the series 1 xbees to start with.
            for (int i = 0; i <= 7; i++) {
                if (!node.getPinAssigned(i)
                        && ioSample.isDigitalEnabled(i)) {
                    // request pin direction.
                    tc.sendXBeeMessage(XBeeMessage.getRemoteDoutMessage(node.getPreferedTransmitAddress(), i), this);
                }
            }
        } else if (response instanceof RemoteAtResponse) {
            RemoteAtResponse atResp = (RemoteAtResponse) response;
            // check to see if this is a Dx responsponse.
            for (int i = 0; i < 7; i++) {
                String cmd = "D" + i;
                if (atResp.getCommand().equals(cmd)) {
                    // check the data to see if it is 3 (digital input).
                    if (atResp.getValue().length > 0
                            && atResp.getValue()[0] == 0x03) {
                        // create the sensor.
                        XBeeNode node = null;
                        if ((node = (XBeeNode) tc.getNodeFromAddress(atResp.getRemoteAddress64().getAddress())) == null) {
                            node = (XBeeNode) tc.getNodeFromAddress(atResp.getRemoteAddress16().getAddress());
                        }

                        // Sensor name is prefix followed by NI/address
                        // followed by the bit number.
                        String sName = prefix + typeLetter()
                                + node.getPreferedName() + ":" + i;
                        XBeeSensor s = (XBeeSensor) getSensor(sName);
                        if (s == null) {
                            // the sensor doesn't exist, so provide a new one.
                            try {
                               provideSensor(sName);
                               if (log.isDebugEnabled()) {
                                   log.debug("DIO " + sName + " enabled as sensor");
                               }
                            } catch(java.lang.IllegalArgumentException iae){
                               // if provideSensor fails, it will throw an IllegalArgumentException, so catch that,log it if debugging is enabled, and then re-throw it.
                               if (log.isDebugEnabled()) {
                                   log.debug("Attempt to enable DIO " + sName + " as sensor failed");
                               }
                               throw iae;
                            }
                        }
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
    public void notifyTimeout(XBeeMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    // for now, set this to false. multiple additions currently works
    // partially, but not for all possible cases.
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        String encoderAddress = addressFromSystemName(prefix + typeLetter() + curAddress);
        int input = pinFromSystemName(prefix + typeLetter() + curAddress);

        if (encoderAddress.equals("")) {
            throw new JmriException("I unable to determine hardware address");
        }
        return prefix + typeLetter() + encoderAddress + ":" + input;
    }

    private String addressFromSystemName(String systemName) {
        String encoderAddress;

        if (systemName.contains(":")) {
            //Address format passed is in the form of encoderAddress:input or S:light address
            int seperator = systemName.indexOf(":");
            encoderAddress = systemName.substring(getSystemPrefix().length() + 1, seperator);
        } else {
            encoderAddress = systemName.substring(getSystemPrefix().length() + 1, systemName.length() - 1);
        }
        if (log.isDebugEnabled()) {
            log.debug("Converted " + systemName + " to hardware address " + encoderAddress);
        }
        return encoderAddress;
    }

    private int pinFromSystemName(String systemName) {
        int input = 0;
        int iName = 0;

        if (systemName.contains(":")) {
            //Address format passed is in the form of encoderAddress:input or L:light address
            int seperator = systemName.indexOf(":");
            try {
                input = Integer.valueOf(systemName.substring(seperator + 1)).intValue();
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert " + systemName + " into the cab and input format of nn:xx");
                return -1;
            }
        } else {
            try {
                iName = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
                input = iName % 10;
            } catch (NumberFormatException ex) {
                log.debug("Unable to convert " + systemName + " Hardware Address to a number");
                return -1;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Converted " + systemName + " to pin number" + input);
        }
        return input;
    }

    @Override
    public void deregister(jmri.NamedBean s) {
        super.deregister(s);
        // remove the specified sensor from the associated XBee pin.
        String systemName = s.getSystemName();
        String name = addressFromSystemName(systemName);
        int pin = pinFromSystemName(systemName);
        XBeeNode curNode;
        if ((curNode = (XBeeNode) tc.getNodeFromName(name)) == null) {
            if ((curNode = (XBeeNode) tc.getNodeFromAddress(name)) == null) {
                try {
                    curNode = (XBeeNode) tc.getNodeFromAddress(Integer.parseInt(name));
                } catch (java.lang.NumberFormatException nfe) {
                    // if there was a number format exception, we couldn't
                    // find the node.
                    curNode = null;
                }
            }
        }
        if (curNode != null) {
            if (curNode.removePinBean(pin, s)) {
                log.debug("Removing sensor from pin " + pin);
            } else {
                log.debug("Failed to removing sensor from pin " + pin);
            }
        }

    }

    private final static Logger log = LoggerFactory.getLogger(XBeeSensorManager.class.getName());

}

/* @(#)XBeeSensorManager.java */
