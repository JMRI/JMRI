// SerialTrafficController.java

package jmri.jmrix.powerline;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

/**
 * Converts Stream-based I/O to/from messages.  The "SerialInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a SerialPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This maintains a list of nodes, but doesn't currently do anything
 * with it.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001, 2003, 2005, 2006, 2008
 * @version			$Revision: 1.2 $
 */
public class SerialTrafficController extends AbstractMRTrafficController implements SerialInterface {

	public SerialTrafficController() {
        super();
        logDebug = log.isDebugEnabled();
        
        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 1000;  // can take a long time to send

        // clear the array of SerialNodes
        for (int i=0; i<=MAXNODE; i++) {
            nodeArray[i] = null;
            mustInit[i] = true;
        }
    }

    // have several debug statements in tight loops, e.g. every character;
    // only want to check once
    boolean logDebug = false;

    private int numNodes = 0;       // Incremented as Serial Nodes are created and registered
                                    // Corresponds to next available address in nodeArray
    private static int MINNODE = 0;
    private static int MAXNODE = 255;
    private SerialNode[] nodeArray = new SerialNode[MAXNODE+1];  // numbering from 0
    private boolean[] mustInit = new boolean[MAXNODE+1];
    int curSerialNodeIndex = 0;   // cycles over defined nodes when pollMessage is called

    /**
     *  Public method to register a Serial node
     */
     public void registerSerialNode(SerialNode node) {
        synchronized (this) {
            // no node validity checking because at this point the node may not be fully defined
            // eventually, should check for duplicate node numbers, which is a bad error
            nodeArray[numNodes] = node;
            mustInit[numNodes] = true;
            numNodes++;
        }
    }

    /**
     *  Public method to set up for initialization of a Serial node
     */
     public void initializeSerialNode(SerialNode node) {
        synchronized (this) {
            // find the node in the registered node list
            for (int i=0; i<numNodes; i++) {
                if (nodeArray[i] == node) {
                    // found node - set up for initialization
                    mustInit[i] = true;
                    return;
                }
            }
        }
    }

    /**
     * Public method to identify a SerialNode from its node address
     *      Note:   'addr' is the node address, numbered from 0.
     *              Returns 'null' if a SerialNode with the specified address
     *                  was not found
     */
    public SerialNode getNodeFromAddress(int addr) {
        for (int i=0; i<numNodes; i++) {
            if (nodeArray[i].getNodeAddress() == addr) {
                return(nodeArray[i]);
            }
        }
    	return (null);
    }

    /**
     *  Public method to delete a Serial node by node address
     */
     public synchronized void deleteSerialNode(int nodeAddress) {
        // find the serial node
        int index = 0;
        for (int i=0; i<numNodes; i++) {
            if (nodeArray[i].getNodeAddress() == nodeAddress) {
                index = i;
            }
        }
        if (index==curSerialNodeIndex) {
            log.warn("Deleting the serial node active in the polling loop");
        }
        // Delete the node from the node list
        numNodes --;
        if (index<numNodes) {
            // did not delete the last node, shift 
            for (int j=index; j<numNodes; j++) {
                nodeArray[j] = nodeArray[j+1];
            }
        }
        nodeArray[numNodes] = null;
    }

    /**
     *  Public method to return the Serial node with a given index
     *  Note:   To cycle through all nodes, begin with index=0, 
     *              and increment your index at each call.  
     *          When index exceeds the number of defined nodes,
     *              this routine returns 'null'.
     */
     public SerialNode getSerialNode(int index) {
        if (index >= numNodes) {
            return null;
        }
        return nodeArray[index];
    }

    // The methods to implement the SerialInterface

    public synchronized void addSerialListener(SerialListener l) {
        this.addListener(l);
    }

    public synchronized void removeSerialListener(SerialListener l) {
        this.removeListener(l);
    }

	protected int enterProgModeDelayTime() {
		// we should to wait at least a second after enabling the programming track
		return 1000;
	}

    /**
     * Forward a SerialMessage to all registered SerialInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((SerialListener)client).message((SerialMessage)m);
    }

    /**
     * Forward a reply to all registered SerialInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((SerialListener)client).reply((SerialReply)r);
    }

    SerialSensorManager mSensorManager = null;
    public void setSensorManager(SerialSensorManager m) { mSensorManager = m; }
    public SerialSensorManager getSensorManager() { return mSensorManager; }
    
    
    /**
	 * Eventually, do initialization if needed
	 */
	protected AbstractMRMessage pollMessage() {
		return null;

	}

    protected AbstractMRListener pollReplyHandler() {
        return null;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendSerialMessage(SerialMessage m, SerialListener reply) {
        sendMessage(m, reply);
    }

    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (logDebug) log.debug("forward "+m);
        sendInterlock = ((SerialMessage)m).getInterlocked();
        super.forwardToPort(m, reply);
    }
        
    protected AbstractMRMessage enterProgMode() {
        return null;
    }
    protected AbstractMRMessage enterNormalMode() {
        return null;
    }

    /**
     * static function returning the SerialTrafficController instance to use.
     * @return The registered SerialTrafficController instance for general use,
     *         if need be creating one.
     */
    static public SerialTrafficController instance() {
        if (self == null) {
            if (log.isDebugEnabled()) log.debug("creating a new SerialTrafficController object");
            self = new SerialTrafficController();
        }
        return self;
    }

    static protected SerialTrafficController self = null;
    protected void setInstance() { self = this; }

    protected AbstractMRReply newReply() { 
        SerialReply reply = new SerialReply();
        return reply;
    }

    boolean sendInterlock = false;
    
    protected boolean endOfMessage(AbstractMRReply msg) {
        // messages are one byte long
        // if the interlock is present, send it
        if (sendInterlock) {
            log.debug("Send interlock");
            sendInterlock = false;
            SerialMessage m = new SerialMessage(1);
            m.setElement(0,0); // not really needed, but this is a slow protocol anyway
            forwardToPort(m, null);
            return false; // just leave in buffer
        }
        log.debug("end of message: "+msg);
        return true;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTrafficController.class.getName());
}


/* @(#)SerialTrafficController.java */
