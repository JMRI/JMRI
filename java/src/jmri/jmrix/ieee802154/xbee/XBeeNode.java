// XBeeNode.java

package jmri.jmrix.ieee802154.xbee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.ieee802154.IEEE802154Node;

import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;

import jmri.NamedBean;
import java.util.HashMap;

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
 * @version   $Revision$
 */
public class XBeeNode extends IEEE802154Node {

    private String Identifier;
    private HashMap<Integer,NamedBean> pinObjects=null;  


    /**
     * Creates a new instance of XBeeNode
     */
    public XBeeNode() {
         Identifier="";
         pinObjects = new HashMap<Integer,NamedBean>(); 
    }

    public XBeeNode(byte pan[],byte user[], byte global[]) {
        super(pan,user,global);
        Identifier="";
        if(log.isDebugEnabled()) log.debug("Created new node with panId: " +
                                pan + " userId: " + user + " and GUID: " + global);
         pinObjects = new HashMap<Integer,NamedBean>(); 
    }
   
    /**
     * Create the needed Initialization packet (AbstractMRMessage) for this node.
     * Returns null if not needed.
     */
    public AbstractMRMessage createInitPacket() {return null; }

    /**
     * Create an Transmit packet (AbstractMRMessage) to send current state
     */
    public AbstractMRMessage createOutPacket() {return null;} //TODO

    /**
     * Are there sensors present, and hence this node will need to be polled?
     *  Note:  returns 'true' if at least one sensor is active for this node
     */
    public boolean getSensorsActive() {return false;} //TODO
    
    /**
     * Deal with a timeout in the transmission controller.
     * @param m message that didn't receive a reply
     * @param l listener that sent the message 
     * @return true if initialization required
     */
    public boolean handleTimeout(AbstractMRMessage m,AbstractMRListener l) { return false; }

    /**
     * A reply was received, so there was not timeout, do any
     * needed processing.
     */
    public void resetTimeout(AbstractMRMessage m) { return; }

    /*
     *  Convert the 16 bit user address to an XBeeAddress16 object.
     */
    public XBeeAddress16 getXBeeAddress16(){
       return new XBeeAddress16(getUserAddress()[0],
                                getUserAddress()[1]);
    }

    /*
     *  Convert the 64 bit address to an XBeeAddress64 object.
     */
    public XBeeAddress64 getXBeeAddress64(){
       return new XBeeAddress64(getGlobalAddress()[0],
                                getGlobalAddress()[1],
                                getGlobalAddress()[2],
                                getGlobalAddress()[3],
                                getGlobalAddress()[4],
                                getGlobalAddress()[5],
                                getGlobalAddress()[6],
                                getGlobalAddress()[7]);
    }


    /**
     * XBee Nodes store an identifier. we want to be able to store
     * and retrieve this information.
     */
    public void setIdentifier(String id){ Identifier=id; }
    public String getIdentifier(){ return Identifier; }

   /**
    *  Set the bean associated with the specified pin
    *  @param pin is the XBee pin assigned.
    *  @param bean is the bean we are attempting to add.
    *  @return true if bean added, false if previous assignment exists.
    **/
    public boolean setPinBean(int pin, NamedBean bean){
       if(pinObjects.containsKey(pin)) {
          log.error("Pin {} already Assigned to object {}",pin,pinObjects.get(pin));
          return false; 
       } else {
          pinObjects.put(pin,bean);
       }
       return true;
    }

   /**
    *  Get the bean associated with the specified pin
    *  @param pin is the XBee pin assigned.
    *  @return the bean assigned to the pin, or null if
    *          no bean is assigned.
    **/ 
    public NamedBean getPinBean(int pin){
         return pinObjects.get(pin);
    }

   /**
    *  Ask if a specified pin is assigned to a bean.
    *  @param pin is the XBee pin assigned.
    *  @return true if the pin has a bean assigned to it, false otherwise.
    **/ 
    public boolean getPinAssigned(int pin){
      return (pinObjects.containsKey(pin));
    }


    private static Logger log = LoggerFactory.getLogger(XBeeNode.class.getName());
}
