package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import jmri.NamedBean;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.ieee802154.IEEE802154Node;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a Node for XBee networks.
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

    private String identifier;
    private HashMap<Integer, NamedBean> pinObjects = null;
    private boolean isPolled;
    private XBeeTrafficController tc = null;
    private RemoteXBeeDevice device = null;
    private XBee16BitAddress userAddress = null;
    private XBee64BitAddress globalAddress = null;

    private final static byte DefaultPanID[] = {0x00,0x00};

    /**
     * Create a new instance of XBeeNode.
     */
    public XBeeNode() {
        identifier = "";
        pinObjects = new HashMap<Integer, NamedBean>();
        isPolled = false;
    }

    public XBeeNode(byte pan[], byte user[], byte global[]) {
        super(pan, user, global);
        identifier = "";
        log.debug("Created new node with panId: {} userId: {} and GUID: {}",
                StringUtil.arrayToString(pan),
                StringUtil.arrayToString(user),
                StringUtil.arrayToString(global));
        pinObjects = new HashMap<Integer, NamedBean>();
        isPolled = false;
        userAddress = new XBee16BitAddress(user);
        globalAddress = new XBee64BitAddress(global);
    }

    public XBeeNode(RemoteXBeeDevice rxd) throws TimeoutException, XBeeException {
        super(DefaultPanID, rxd.get16BitAddress().getValue(), rxd.get64BitAddress().getValue());
        identifier = rxd.getNodeID();

        try{
           setPANAddress(rxd.getPANID());
        } catch (TimeoutException t) {
          // we dont need the PAN ID for communicaiton,so just continue.
        }

        log.debug("Created new node from RemoteXBeeDevice: {}", rxd.toString() );
        pinObjects = new HashMap<Integer, NamedBean>();
        isPolled = false;
        device = rxd;
        userAddress = device.get16BitAddress();
        globalAddress = device.get64BitAddress();
    }

    /**
     * Set the Traffic Controller associated with this node.
     */
    public void setTrafficController(XBeeTrafficController controller) {
        tc = controller;
    }

    /**
     * Create the needed Initialization packet (AbstractMRMessage) for this
     * node.
     *
     * @return null because not needed for XBeeNode
     */
    @Override
    public AbstractMRMessage createInitPacket() {
        return null;
    }

    /**
     * Create an Transmit packet (AbstractMRMessage) to send current state.
     */
    @Override
    public AbstractMRMessage createOutPacket() {
        return null;
    } // TODO

    /**
     * Are sensors present, and hence will this node need to be polled?
     *
     * @return 'true' if at least one sensor is active for this node
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

    /**
     * Set the isPolled attribute.
     */
    public void setPoll(boolean poll) {
        isPolled = poll;
    }

    /**
     * Get the isPolled attribute.
     */
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
     * A reply was received, so there was not timeout; do any needed processing.
     */
    @Override
    public void resetTimeout(AbstractMRMessage m) {
        return;
    }

    /**
     * Convert the 16 bit user address to an XBee16BitAddress object.
     */
    public XBee16BitAddress getXBeeAddress16() {
        if(device!=null) {
           return device.get16BitAddress();
        } else {
           return userAddress;
        }
    }

    /**
     * Convert the 64 bit address to an XBee64BitAddress object.
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
     *
     * @param id text id for node
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
     * Set the bean associated with the specified pin.
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
     * Remove the bean associated with the specified pin.
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
     * Get the bean associated with the specified pin.
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
     */
    public boolean getPinAssigned(int pin) {
        return (pinObjects.containsKey(pin));
    }

    /**
     * Get the prefered name for this XBee Node.
     *
     * @return the identifier string if it is not blank then a string
         representation of the bytes of the 16 bit address if it is not a
         broadcast address. Otherwise return the 64 bit GUID.
     */
    public String getPreferedName() {
        if (!identifier.equals("")) {
            return identifier;
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

    /**
     * @return RemoteXBeeDevice associated with this node
     */
    public RemoteXBeeDevice getXBee() {
           if( device == null && tc !=null) {
               device = new RemoteXBeeDevice(tc.getXBee(),globalAddress,
                                    userAddress,identifier);
           }
           return device;
    }

    /**
     * Set the RemoteXBeeDevice associated with this node and
     * configure address information.
     *
     * @param rxd the RemoteXBeeDevice associated with this node.
     */
    public void setXBee(RemoteXBeeDevice rxd) {
           device=rxd;
           userAddress = device.get16BitAddress();
           globalAddress = device.get64BitAddress();
           setUserAddress(rxd.get16BitAddress().getValue());
           setGlobalAddress(rxd.get64BitAddress().getValue());
           identifier = rxd.getNodeID();

    }

    /**
     * Get the stream object associated with this node. Create it if it does
     * not exist.
     */
    public XBeeIOStream getIOStream() {
        if (mStream == null) {
            mStream = new XBeeIOStream(this, tc);
	        mStream.configure(); // start the threads for the stream.
        }
        return mStream;
    }

    private XBeeIOStream mStream = null;

    /**
     * Connect and configure a StreamPortController object to the XBeeIOStream
     * associated with this node.
     *
     * @param cont AbstractSTreamPortController object to connect
     */
    public void connectPortController(jmri.jmrix.AbstractStreamPortController cont) {
        connectedController = cont;
        connectedController.configure();
    }

    /**
     * Connect a StreamPortController object to the XBeeIOStream
     * associated with this node.
     *
     * @param cont AbstractSTreamPortController object to connect
     */
    public void setPortController(jmri.jmrix.AbstractStreamPortController cont) {
        connectedController = cont;
    }

    /**
     * Get the StreamPortController ojbect associated with the XBeeIOStream
     * associated with this node.
     *
     * @return connected {@link jmri.jmrix.AbstractStreamPortController}
     */
    public jmri.jmrix.AbstractStreamPortController getPortController() {
        return connectedController;
    }

    private jmri.jmrix.AbstractStreamPortController connectedController = null;

    /**
     * Connect and configure a StreamConnectionConfig object to the XBeeIOStream
     * associated with this node.
     *
     * @param cfg AbstractStreamConnectionConfig object to connect
     */
    public void connectPortController(jmri.jmrix.AbstractStreamConnectionConfig cfg) {
        connectedConfig = cfg;
        connectPortController(cfg.getAdapter());
    }

    /**
     * Connect a StreamConnectionConfig object to the XBeeIOStream
     * associated with this node.
     *
     * @param cfg AbstractStreamConnectionConfig object to connect
     */
    public void setPortController(jmri.jmrix.AbstractStreamConnectionConfig cfg) {
        connectedConfig = cfg;
        setPortController(cfg.getAdapter());
    }

    /**
     * Get the StreamConnectionConfig ojbect associated with the XBeeIOStream
     * associated with this node.
     *
     * @return connected {@link jmri.jmrix.AbstractStreamConnectionConfig}
     */
    public jmri.jmrix.AbstractStreamConnectionConfig getConnectionConfig() {
        return connectedConfig;
    }

    private jmri.jmrix.AbstractStreamConnectionConfig connectedConfig = null;

    /**
     * Provide a string representation of this XBee Node.
     */

    /**
     * Provide a string representation of this XBee Node.
     */
    @Override
    public String toString(){
       return "(" + jmri.util.StringUtil.hexStringFromBytes(getUserAddress()) +
              "," + jmri.util.StringUtil.hexStringFromBytes(getGlobalAddress()) +
              "," + getIdentifier() + ")";
    }


    private byte PRValue[] = null;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    /**
     * Package protected method to set the PR (Pull Resistance) parameter of the node.
     *
     * @param pin the pin number to change.
     * @param pr a jmri.Sensor.PullResistance value used to configure the pin.
     * @throws TimeoutException lock timed out
     * @throws XBeeException invalid Xbee values, pins
     */
    void setPRParameter(int pin, jmri.Sensor.PullResistance pr) throws TimeoutException, XBeeException {
       // flip the bits in the PR data byte, and then send to the node.
       if(pin>7 || pin < 0){
          throw new IllegalArgumentException("Invalid pin specified");
       }
       try {
          // always try to get the PR value when writing.
          writeLock.lock();
          PRValue = device.getParameter("PR");
          switch(pin){
          case 0:
              if(pr==jmri.Sensor.PullResistance.PULL_UP) {
                PRValue[0]=(byte) (PRValue[0] | 0x01);
              } else {
                PRValue[0]=(byte) (PRValue[0] & 0xFE);
              }
              break;
          case 1:
              if(pr==jmri.Sensor.PullResistance.PULL_UP) {
                PRValue[0]=(byte) (PRValue[0] | 0x02);
              } else {
                PRValue[0]=(byte) (PRValue[0] & 0xFD);
              }
              break;
          case 2:
              if(pr==jmri.Sensor.PullResistance.PULL_UP) {
                PRValue[0]=(byte) (PRValue[0] | 0x04);
              } else {
                PRValue[0]=(byte) (PRValue[0] & 0xFB);
              }
              break;
          case 3:
              if(pr==jmri.Sensor.PullResistance.PULL_UP) {
                PRValue[0]=(byte) (PRValue[0] | 0x08);
              } else {
                PRValue[0]=(byte) (PRValue[0] & 0xF7);
              }
              break;
          case 4:
              if(pr==jmri.Sensor.PullResistance.PULL_UP) {
                PRValue[0]=(byte) (PRValue[0] | 0x10);
              } else {
                PRValue[0]=(byte) (PRValue[0] & 0xEF);
              }
              break;
          case 5:
              if(pr==jmri.Sensor.PullResistance.PULL_UP) {
                PRValue[0]=(byte) (PRValue[0] | 0x20);
              } else {
                PRValue[0]=(byte) (PRValue[0] & 0xDF);
              }
              break;
          case 6:
              if(pr==jmri.Sensor.PullResistance.PULL_UP) {
                PRValue[0]=(byte) (PRValue[0] | 0x40);
              } else {
                PRValue[0]=(byte) (PRValue[0] & 0xBF);
              }
              break;
          case 7:
              if(pr==jmri.Sensor.PullResistance.PULL_UP) {
                PRValue[0]=(byte) (PRValue[0] |  (byte) 0x80);
              } else {
                PRValue[0]=(byte) (PRValue[0] & (byte) 0x7F);
              }
                break;
            default:
                log.warn("Unhandled pin value: {}", pin);
                break;

          }
          device.setParameter("PR",PRValue);
          device.applyChanges();  // force the XBee to start using the new value.
                                  // we may also want to use writeChanges to set
                                  // the value on the device perminantly.
       } finally {
           writeLock.unlock();
       }
    }

   /**
    * Package protected method to check to see if the PR parameter indicates
    * the specified pin has the pull-up resistor enabled.
    *
    * @param pin the pin number
    * @return a jmri.Sensor.PullResistance value indicating the current state of
    * the pullup resistor.
    * @throws TimeoutException lock timeout
    * @throws XBeeException invalid pins or values
    */
    jmri.Sensor.PullResistance getPRValueForPin(int pin) throws TimeoutException, XBeeException {
       if(pin>7 || pin < 0){
          throw new IllegalArgumentException("Invalid pin specified");
       }
       // when reading, used the cached PRValue, if it is available
       byte prbyte;
       try {
          readLock.lock();
          if(PRValue == null){
             PRValue = device.getParameter("PR");
          }
          prbyte = PRValue[0];
       } finally {
          readLock.unlock();
       }
       jmri.Sensor.PullResistance retval = jmri.Sensor.PullResistance.PULL_OFF;
       switch(pin){
       case 0:
           if((prbyte & 0x01)==0x01){
              retval = jmri.Sensor.PullResistance.PULL_UP;
           } else {
              retval = jmri.Sensor.PullResistance.PULL_OFF;
           }
           break;
       case 1:
           if((prbyte & 0x02)==0x02){
              retval = jmri.Sensor.PullResistance.PULL_UP;
           } else {
              retval = jmri.Sensor.PullResistance.PULL_OFF;
           }
           break;
       case 2:
           if((prbyte & 0x04)==0x04){
              retval = jmri.Sensor.PullResistance.PULL_UP;
           } else {
              retval = jmri.Sensor.PullResistance.PULL_OFF;
           }
           break;
       case 3:
           if((prbyte & 0x08)==0x08){
              retval = jmri.Sensor.PullResistance.PULL_UP;
           } else {
              retval = jmri.Sensor.PullResistance.PULL_OFF;
           }
           break;
       case 4:
           if((prbyte & 0x10)==0x10){
              retval = jmri.Sensor.PullResistance.PULL_UP;
           } else {
              retval = jmri.Sensor.PullResistance.PULL_OFF;
           }
           break;
       case 5:
           if((prbyte & 0x20)==0x20){
              retval = jmri.Sensor.PullResistance.PULL_UP;
           } else {
              retval = jmri.Sensor.PullResistance.PULL_OFF;
           }
           break;
       case 6:
           if((prbyte & 0x40)==0x40){
              retval = jmri.Sensor.PullResistance.PULL_UP;
           } else {
              retval = jmri.Sensor.PullResistance.PULL_OFF;
           }
           break;
       case 7:
           if((prbyte & 0x80)==0x80){
              retval = jmri.Sensor.PullResistance.PULL_UP;
           } else {
              retval = jmri.Sensor.PullResistance.PULL_OFF;
           }
           break;
       default:
          retval = jmri.Sensor.PullResistance.PULL_OFF;
       }
       return retval;
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeNode.class);

}
