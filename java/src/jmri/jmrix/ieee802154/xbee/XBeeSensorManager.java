package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.exceptions.InterfaceNotOpenException;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.io.IOLine;
import com.digi.xbee.api.io.IOMode;
import com.digi.xbee.api.io.IOSample;
import com.digi.xbee.api.listeners.IIOSampleReceiveListener;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the XBee specific Sensor implementation.
 *
 * System names are "ZSnnn", where nnn is the sensor number without padding.
 * Or "ZSstring:pin", where string is a node address and pin is the io pin used.
 *
 * @author Paul Bender Copyright (C) 2003-2016
 */
public class XBeeSensorManager extends jmri.managers.AbstractSensorManager implements IIOSampleReceiveListener{

    // ctor has to register for XBee events
    public XBeeSensorManager(XBeeTrafficController controller, String prefix) {
        tc = controller;
        this.prefix = prefix;
        tc.getXBee().addIOSampleListener(this);
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }
    protected String prefix = null;

    protected XBeeTrafficController tc = null;

    @Deprecated
    static public XBeeSensorManager instance() {
        return mInstance;
    }
    @Deprecated
    static private XBeeSensorManager mInstance = null;

    // to free resources when no longer used
    @Override
    public void dispose() {
        tc.getXBee().removeIOSampleListener(this);
        super.dispose();
    }

    // XBee specific methods

    @Override
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

    /**
     * Public method to validate system name format.
     *
     * @param systemName Xbee id format with pins to be checked
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        if (tc.getNodeFromName(addressFromSystemName(systemName)) == null
                && tc.getNodeFromAddress(addressFromSystemName(systemName)) == null) {
            try {
                if (tc.getNodeFromAddress(Integer.parseInt(addressFromSystemName(systemName))) == null) {
                    return NameValidity.INVALID;
                } else {
                    return (pinFromSystemName(systemName) >= 0
                            && pinFromSystemName(systemName) <= 7) ? NameValidity.VALID : NameValidity.INVALID;
                }
            } catch (java.lang.NumberFormatException nfe) {
                // if there was a number format exception, we couldn't find the node.
                log.debug("Unable to convert {} into the XBee node and pin format of nn:xx", systemName);
                return NameValidity.INVALID;
            }

        } else {
            return (pinFromSystemName(systemName) >= 0
                    && pinFromSystemName(systemName) <= 7) ? NameValidity.VALID : NameValidity.INVALID;
        }
    }

    // IIOSampleReceiveListener methods

    @Override
    public synchronized void ioSampleReceived(RemoteXBeeDevice remoteDevice,IOSample ioSample) {
        if (log.isDebugEnabled()) {
            log.debug("received io sample {} from {}",ioSample,remoteDevice);
        }

        XBeeNode node = (XBeeNode) tc.getNodeFromXBeeDevice(remoteDevice);
        for (int i = 0; i <= 8; i++) {
            if (!node.getPinAssigned(i)
                && ioSample.hasDigitalValue(IOLine.getDIO(i))) {
                   // get pin direction
                   IOMode mode = IOMode.DISABLED;  // assume disabled as default.
                   try  {
                       mode = remoteDevice.getIOConfiguration(IOLine.getDIO(i));
                   } catch (TimeoutException toe) {
                      log.debug("Timeout retrieving IO line mode for {} on {}",IOLine.getDIO(i),remoteDevice);
                      // is this a hidden terminal?  This was triggered by an 
                      // IO Sample, so we know we can hear the other node, but
                      // it may not hear us.  In this case, assume we are 
                      // working with an input pin.
                      mode = IOMode.DIGITAL_IN;
                   } catch (InterfaceNotOpenException ino) {
                      log.error("Interface Not Open retrieving IO line mode for {} on {}",IOLine.getDIO(i),remoteDevice);
                   } catch (XBeeException xbe) {
                      log.error("Error retrieving IO line mode for {} on {}",IOLine.getDIO(i),remoteDevice);
                   }
               
                   if(mode == IOMode.DIGITAL_IN ) {
                        // thisis an input, check to see if it exists as a sensor.
                        node = (XBeeNode) tc.getNodeFromXBeeDevice(remoteDevice);

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
                                   log.debug("DIO {} enabled as sensor",sName);
                               }
                            } catch(java.lang.IllegalArgumentException iae){
                               // if provideSensor fails, it will throw an IllegalArgumentException, so catch that,log it if debugging is enabled, and then re-throw it.
                               if (log.isDebugEnabled()) {
                                   log.debug("Attempt to enable DIO {} as sensor failed",sName);
                               }
                               throw iae;
                            }
                        }
                    }
               }
            }
        }

    // for now, set this to false. multiple additions currently works
    // partially, but not for all possible cases.
    @Override
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
    public void deregister(jmri.Sensor s) {
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

    /**
     * Do the sensor objects provided by this manager support configuring
     * an internal pullup or pull down resistor?
     * <p>
     * For Raspberry Pi systems, it is possible to set the pullup or
     * pulldown resistor, so return true.
     *
     * @return true if pull up/pull down configuration is supported.
     */
    @Override
    public boolean isPullResistanceConfigurable(){
       return true;
    }

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddInputEntryToolTip");
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeSensorManager.class);

}
