// SerialNode.java

package jmri.jmrix.maple;

import org.apache.log4j.Logger;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractNode;

/**
 * Models a serial node, consisting of one Maple Systems HMI touch screen panel.
 * <P>
 * Nodes are numbered ala the Station number, from 1 to 99.  
 * <P>
 * The array of sensor states is used to update sensor known state
 * only when there's a change on the serial bus.  This allows for the
 * sensor state to be updated within the program, keeping this updated
 * state until the next change on the serial bus.  E.g. you can manually
 * change a state via an icon, and not have it change back the next time
 * that node is polled.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2008
 * @author      Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @author      Bob Jacobsen, Dave Duchamp, revised for Maple, 2009
 * @version	$Revision$
 */
public class SerialNode extends AbstractNode {

    /**
     * Maximum number of sensors for Maple.
     */
    static final int MAXSENSORS = 1000;
    
    // class constants
    // node definition instance variables (must persist between runs)
//	protected int pulseWidth = 500;				// Pulse width for pulsed turnout control (milliseconds)
	private int _address = 0;

    // operational instance variables  (should not be preserved between runs)

    /**
     * Assumes a node address of 1, and a node type of 0
     * If this constructor is used, actual node address must be set using
     *    setNodeAddress.
     */
    public SerialNode() {
        this (1,0);
    }

    /**
     * Creates a new SerialNode and initialize default instance variables
     *   address - Address of node on serial bus (0-99)
     *   type - 0 (ignored).
     */
    public SerialNode(int address, int type) {
        // set address 
        setNodeAddress (address);
		_address = address;
        // register this node
        SerialTrafficController.instance().registerNode(this);
    }

    /**
     * Public method to return state of Sensors.
     *  Note:  returns 'true' since at least one sensor is defined
     */
    public boolean getSensorsActive() { return true; }

    /**
     * Check valid node address, must match value configured in the Maple HMI
	 *    Allowed values are 1-99
     */
    protected boolean checkNodeAddress(int address) {
        return (address > 0) && (address <= 99);
    }
	
	/**
	 * Public access to this node's address
	 */
	public int getAddress() {return _address;}

    /**
     * Public Method to create an Initialization packet (SerialMessage) for this node
	 *  Note: Maple Systems devices do not need initialization. This is here for completion.
     */
    public AbstractMRMessage createInitPacket() {
        return null;
    }
 
	/**
     * Public Method to create an Transmit packet (SerialMessage)
	 *   Not used in Maple.
     */
    public AbstractMRMessage createOutPacket() {
		return null;
	}

    boolean warned = false;

    void warn(String s) {
    	if (warned) return;
    	warned = true;
    	log.warn(s);
    }

    int timeout = 0;
    /**
     *
     * @return true if initialization required
     */
    public boolean handleTimeout(AbstractMRMessage m,AbstractMRListener l) {
		// increment timeout count
        timeout++;
		log.warn("Poll of node "+_address+" timed out. Timeout count = "+timeout);		
        return false;
    }
    
    public void resetTimeout(AbstractMRMessage m) {
        if (timeout>0) log.debug("Reset "+timeout+" timeout count");
        timeout = 0;
    }
	
	public int getTimeoutCount(){return timeout;}
    
    static Logger log = Logger.getLogger(SerialNode.class.getName());
}

/* @(#)SerialNode.java */
