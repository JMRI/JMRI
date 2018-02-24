package jmri.jmrix.maple;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Models a serial node, consisting of one Maple Systems HMI touch screen panel.
 * <P>
 * Nodes are numbered ala the Station number, from 1 to 99.
 * <P>
 * The array of sensor states is used to update sensor known state only when
 * there's a change on the serial bus. This allows for the sensor state to be
 * updated within the program, keeping this updated state until the next change
 * on the serial bus. E.g. you can manually change a state via an icon, and not
 * have it change back the next time that node is polled.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2008
 * @author Bob Jacobsen, Dave Duchamp, multiNode extensions, 2004
 * @author Bob Jacobsen, Dave Duchamp, revised for Maple, 2009
  */
public class SerialNode extends AbstractNode {

    /**
     * Maximum number of sensors for Maple.
     */
    static final int MAXSENSORS = 1000;

    // class constants
    // node definition instance variables (must persist between runs)
// protected int pulseWidth = 500;    // Pulse width for pulsed turnout control (milliseconds)
    private int _address = 0;

    // operational instance variables (should not be preserved between runs)
    /**
     * Assumes a node address of 1, and a node type of 0. If this constructor is
     * used, actual node address must be set using setNodeAddress.
     */
    public SerialNode(SerialTrafficController tc) {
        this(1, 0, tc);
    }

    /**
     * Creates a new SerialNode and initialize default instance variables
     * @param address Address of node on serial bus (0-99)
     * @param type 0 (ignored)
     */
    public SerialNode(int address, int type, SerialTrafficController tc) {
        // set address 
        setNodeAddress(address);
        _address = address;
        // register this node
        tc.registerNode(this);
    }

    /**
     * Public method to return state of Sensors. Note: returns 'true' since at
     * least one sensor is defined
     */
    @Override
    public boolean getSensorsActive() {
        return true;
    }

    /**
     * Check valid node address, must match value configured in the Maple HMI.
     * Allowed values are 1-99
     */
    @Override
    protected boolean checkNodeAddress(int address) {
        return (address > 0) && (address <= 99);
    }

    /**
     * Public access to this node's address
     */
    public int getAddress() {
        return _address;
    }

    /**
     * Public Method to create an Initialization packet (SerialMessage) for this
     * node Note: Maple Systems devices do not need initialization. This is here
     * for completion.
     */
    @Override
    public AbstractMRMessage createInitPacket() {
        return null;
    }

    /**
     * Public Method to create a Transmit packet (SerialMessage) Not used in
     * Maple.
     */
    @Override
    public AbstractMRMessage createOutPacket() {
        return null;
    }

    boolean warned = false;

    void warn(String s) {
        if (warned) {
            return;
        }
        warned = true;
        log.warn(s);
    }

    int timeout = 0;

    /**
     *
     * @return true if initialization required
     */
    @Override
    public boolean handleTimeout(AbstractMRMessage m, AbstractMRListener l) {
        // increment timeout count
        timeout++;
        log.warn("Poll of node " + _address + " timed out. Timeout count = " + timeout);
        return false;
    }

    @Override
    public void resetTimeout(AbstractMRMessage m) {
        if (timeout > 0) {
            log.debug("Reset " + timeout + " timeout count");
        }
        timeout = 0;
    }

    public int getTimeoutCount() {
        return timeout;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialNode.class);

}
