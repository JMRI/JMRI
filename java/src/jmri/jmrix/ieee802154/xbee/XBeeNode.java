package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import java.util.HashMap;
import jmri.NamedBean;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.ieee802154.IEEE802154Node;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a node for XBee networks.
 * <p>
 * Integrated with {@link XBeeTrafficController}.
 * <p>
 * Each node has 3 addresses associated with it:
 * <ol>
 * <li>A 16 bit PAN (Personal Area Network) ID assigned by the user</li>
 * <li>A 16 bit User Assigned Address</li>
 * <li>A 64 bit Globally Unique ID assigned by the manufacturer</li>
 * </ol>
 * <p>
 * All nodes in a given network must have the same PAN ID
 *
 * @author Paul Bender Copyright 2013
 */
public class XBeeNode extends IEEE802154Node {

    private String Identifier;
    private HashMap<Integer, NamedBean> pinObjects = null;
    private boolean isPolled;
    private XBeeTrafficController tc = null;
    private RemoteXBeeDevice device = null;
    private XBee16BitAddress userAddress = null;
    private XBee64BitAddress globalAddress = null;

    private final static byte DefaultPanID[] = {0x00,0x00};

    /**
     * Creates a new instance of XBeeNode
     */
    public XBeeNode() {
        Identifier = "";
        pinObjects = new HashMap<Integer, NamedBean>();
        isPolled = false;
    }

    public XBeeNode(byte pan[], byte user[], byte global[]) {
        super(pan, user, global);
        Identifier = "";
        if (log.isDebugEnabled()) {
            log.debug("Created new node with panId: "
                    + StringUtil.arrayToString(pan)
                    + " userId: " + StringUtil.arrayToString(user)
                    + " and GUID: " + StringUtil.arrayToString(global));
        }
        pinObjects = new HashMap<Integer, NamedBean>();
        isPolled = false;
        userAddress = new XBee16BitAddress(user);
        globalAddress = new XBee64BitAddress(global);
    }

    public XBeeNode(RemoteXBeeDevice rxd) throws TimeoutException, XBeeException {
        super(DefaultPanID, rxd.get16BitAddress().getValue(), rxd.get64BitAddress().getValue());
        Identifier = rxd.getNodeID();
       
        try{
           setPANAddress(rxd.getPANID());
        } catch (TimeoutException t) {
          // we dont need the PAN ID for communicaiton,so just continue.
        }
 
        if (log.isDebugEnabled()) {
            log.debug("Created new node from RemoteXBeeDevice: {}",
                    rxd.toString() );
        }
        pinObjects = new HashMap<Integer, NamedBean>();
        isPolled = false;
        device = rxd;
        userAddress = device.get16BitAddress();
        globalAddress = device.get64BitAddress();
    }

    /*
     * Set the traffic controller associated with this node.
     */
    public void setTrafficController(XBeeTrafficController controller) {
        tc = controller;
    }

    /**
     * Create the needed Initialization packet (AbstractMRMessage) for this
     * node. Returns null if not needed.
     */
    @Override
    public AbstractMRMessage createInitPacket() {
        return null;
    }

    /**
     * Create an Transmit packet (AbstractMRMessage) to send current state
     */
    @Override
    public AbstractMRMessage createOutPacket() {
        return null;
    } //TODO

    /**
     * Are there sensors present, and hence this node will need to be polled?
     * Note: returns 'true' if at least one sensor is active for this node
     */
    @Override
    public boolean getSensorsActive() {
        if (getPoll()) {
            for (Object bean : pinObjects.values()) {
                if (bean instanceof XBeeSensor) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     *  get/set the isPolled attribute;
     */
    public void setPoll(boolean poll) {
        isPolled = poll;
    }

    public boolean getPoll() {
        return isPolled;
    }

    /**
     * Deal with a timeout in the transmission controller.
     *
     * @param m message that didn't receive a reply
     * @param l listener that sent the message
     * @return true if initialization required
     */
    @Override
    public boolean handleTimeout(AbstractMRMessage m, AbstractMRListener l) {
        return false;
    }

    /**
     * A reply was received, so there was not timeout, do any needed processing.
     */
    @Override
    public void resetTimeout(AbstractMRMessage m) {
        return;
    }

    /*
     *  Convert the 16 bit user address to an XBee16BitAddress object.
     */
    public XBee16BitAddress getXBeeAddress16() {
        if(device!=null) {
           return device.get16BitAddress();
        } else {
           return userAddress;
        }
    }

    /*
     *  Convert the 64 bit address to an XBee64BitAddress object.
     */
    public XBee64BitAddress getXBeeAddress64() {
        if(device!=null) {
           return device.get64BitAddress();
        } else {
          return globalAddress;
        }
    }

    /**
     * XBee Nodes store an identifier. we want to be able to store and retrieve
     * this information.
     */
    public void setIdentifier(String id) {
        try {
           device.setNodeID(id);
        } catch(XBeeException xbe) { // includes TimeoutException
          // ignore the error, failed to set.
        }
    }

    public String getIdentifier() {
        return device.getNodeID();
    }

    /**
     * Set the bean associated with the specified pin
     *
     * @param pin  is the XBee pin assigned.
     * @param bean is the bean we are attempting to add.
     * @return true if bean added, false if previous assignment exists.
     *
     */
    public boolean setPinBean(int pin, NamedBean bean) {
        if (pinObjects.containsKey(pin)) {
            log.error("Pin {} already Assigned to object {}", pin, pinObjects.get(pin));
            return false;
        } else {
            pinObjects.put(pin, bean);
        }
        return true;
    }

    /**
     * Remove the bean associated with the specified pin
     *
     * @param pin  is the XBee pin assigned.
     * @param bean is the bean we are attempting to remove.
     * @return true if bean removed, false if specified bean was not assigned to
     *         the pin.
     *
     */
    public boolean removePinBean(int pin, NamedBean bean) {
        if (bean == getPinBean(pin)) {
            pinObjects.remove(pin);
            return true;
        }
        return false;
    }

    /**
     * Get the bean associated with the specified pin
     *
     * @param pin is the XBee pin assigned.
     * @return the bean assigned to the pin, or null if no bean is assigned.
     *
     */
    public NamedBean getPinBean(int pin) {
        return pinObjects.get(pin);
    }

    /**
     * Ask if a specified pin is assigned to a bean.
     *
     * @param pin is the XBee pin assigned.
     * @return true if the pin has a bean assigned to it, false otherwise.
     *
     */
    public boolean getPinAssigned(int pin) {
        return (pinObjects.containsKey(pin));
    }

    /**
     * Get the prefered name for this XBee Node.
     *
     * @return the Identifier string if it is not blank then a string
     *         representation of the bytes of the 16 bit address if it is not a
     *         broadcast address. Otherwise return the 64 bit GUID.
     *
     */
    public String getPreferedName() {
        if (!Identifier.equals("")) {
            return Identifier;
        } else if (!(getXBeeAddress16().equals(XBee16BitAddress.BROADCAST_ADDRESS))
                && !(getXBeeAddress16().equals(XBee16BitAddress.UNKNOWN_ADDRESS))) {
            return jmri.util.StringUtil.hexStringFromBytes(useraddress);
        } else {
            return jmri.util.StringUtil.hexStringFromBytes(globaladdress);
        }

    }

    /**
     * Get the prefered transmit address for this XBee Node.
     *
     * @return the 16 bit address if it is not a broadcast address. Otherwise
     *         return the 64 bit GUID.
     *
     */
    @Deprecated
    public Object getPreferedTransmitAddress() {
        if (!(getXBeeAddress16().equals(XBee16BitAddress.BROADCAST_ADDRESS))
                && !(getXBeeAddress16().equals(XBee16BitAddress.UNKNOWN_ADDRESS))) {
            return getXBeeAddress16();
        } else {
            return getXBeeAddress64();
        }
    }

    /*
     * @return RemoteXBeeDevice associated with this node.
     */
    public RemoteXBeeDevice getXBee() {
           if( device == null && tc !=null) {
               device = new RemoteXBeeDevice(tc.getXBee(),globalAddress,
                                    userAddress,Identifier);
           }
           return device;
    }

    /*
     * set the RemoteXBeeDevice associated with this node and
     * configure address information.
     *
     * @param RemoteXBeeDevice associated with this node.
     */
    public void setXBee(RemoteXBeeDevice rxd) {
           device=rxd;
           userAddress = device.get16BitAddress();
           globalAddress = device.get64BitAddress();
           setUserAddress(rxd.get16BitAddress().getValue());
           setGlobalAddress(rxd.get64BitAddress().getValue());
           Identifier = rxd.getNodeID();

    }

    /*
     * get the stream object associated with this node.  Create it if it does
     * not exist.
     */
    public XBeeIOStream getIOStream() {
        if (mStream == null) {
            mStream = new XBeeIOStream(this, tc);
        }
        return mStream;
    }

    private XBeeIOStream mStream = null;

    /*
     * Connect a StreamPortController object to the XBeeIOStream
     * associated with this node
     * @param cont AbstractSTreamPortController object to connect.
     */
    public void connectPortController(jmri.jmrix.AbstractStreamPortController cont) {
        connectedController = cont;
    }
    /*
     * Create a new object derived from AbstractStreamPortController and
     * connect it to the IOStream associated with this object.
     */

    public void connectPortController(Class<jmri.jmrix.AbstractStreamPortController> T) {
        try {
            java.lang.reflect.Constructor<?> ctor = T.getConstructor(java.io.DataInputStream.class, java.io.DataOutputStream.class, String.class);
            connectedController = (jmri.jmrix.AbstractStreamPortController) ctor.newInstance(getIOStream().getInputStream(), getIOStream().getOutputStream(), "XBee Node " + getPreferedName());
            connectedController.configure();
        } catch (java.lang.InstantiationException ie) {
            log.error("Unable to construct Stream Port Controller for node.");
            ie.printStackTrace();
        } catch (java.lang.NoSuchMethodException nsm) {
            log.error("Unable to construct Stream Port Controller for node.");
            nsm.printStackTrace();
        } catch (java.lang.IllegalAccessException iae) {
            log.error("Unable to construct Stream Port Controller for node.");
            iae.printStackTrace();
        } catch (java.lang.reflect.InvocationTargetException ite) {
            log.error("Unable to construct Stream Port Controller for node.");
            ite.printStackTrace();
        }
    }

    /*
     * return the StreamPortController ojbect associated with the XBeeIOStream
     * associated with this node.
     * @return jmri.jmrix.AbstractStreamPortController
     */
    public jmri.jmrix.AbstractStreamPortController getPortController() {
        return connectedController;
    }

    private jmri.jmrix.AbstractStreamPortController connectedController = null;

    /*
     * provide a string representation of this XBee Node
     */
    @Override
    public String toString(){
       return "(" + jmri.util.StringUtil.hexStringFromBytes(getUserAddress()) +
              "," + jmri.util.StringUtil.hexStringFromBytes(getGlobalAddress()) +
              "," + getIdentifier() + ")";
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeNode.class.getName());
}
