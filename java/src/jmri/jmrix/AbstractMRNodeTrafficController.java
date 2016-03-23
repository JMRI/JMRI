package jmri.jmrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Traffic Controller base class for those implementations that track a
 * set of nodes.
 * <p>
 * The nodes are descendents of {@link jmri.jmrix.AbstractNode}. Provides node
 * management services, but no additional protocol.
 *
 * @author jake Copyright 2008
 */
public abstract class AbstractMRNodeTrafficController extends AbstractMRTrafficController {

    /**
     * Creates a new instance of AbstractMRNodeTrafficController
     */
    public AbstractMRNodeTrafficController() {
    }

    /**
     * Initialize based on number of first and last nodes
     */
    protected void init(int minNode, int maxNode) {
        this.minNode = minNode;
        this.maxNode = maxNode;

        nodeArray = new AbstractNode[this.maxNode + 1];  // numbering from 0
        mustInit = new boolean[this.maxNode + 1];

        // initialize content
        for (int i = 0; i <= this.maxNode; i++) {
            mustInit[i] = true;
        }
    }

    protected int minNode = -1;
    protected int maxNode = -1;

    protected volatile int numNodes = 0;  // Incremented as Serial Nodes are created and registered
    // Corresponds to next available address in nodeArray
    protected AbstractNode[] nodeArray;
    private boolean[] mustInit;

    /**
     * Does this node need to have initialization data sent?
     */
    protected boolean getMustInit(int i) {
        return mustInit[i];
    }

    /**
     * Mark whether this node needs to have initialization data sent
     */
    protected void setMustInit(int i, boolean v) {
        mustInit[i] = v;
    }

    protected void setMustInit(AbstractNode node, boolean v) {
        for (int i = 0; i < numNodes; i++) {
            if (nodeArray[i] == node) {
                // found node - set up for initialization
                mustInit[i] = v;
                return;
            }
        }
    }

    /**
     * Get the number of currently registered nodes
     */
    public int getNumNodes() {
        return numNodes;
    }

    /**
     * Public method to register a Serial node
     */
    public void registerNode(AbstractNode node) {
        synchronized (this) {
            // no validity checking because at this point the node may not be fully defined
            nodeArray[numNodes] = node;
            mustInit[numNodes] = true;
            numNodes++;
        }
    }

    /**
     * Public method to return the Serial node with a given index Note: To cycle
     * through all nodes, begin with index=0, and increment your index at each
     * call. When index exceeds the number of defined nodes, this routine
     * returns 'null'.
     */
    public synchronized AbstractNode getNode(int index) {
        if (index >= numNodes) {
            return null;
        }
        return nodeArray[index];
    }

    /**
     * Public method to identify a SerialNode from its node address Note: 'addr'
     * is the node address, numbered from 0. Returns 'null' if a SerialNode with
     * the specified address was not found
     */
    synchronized public AbstractNode getNodeFromAddress(int addr) {
        for (int i = 0; i < numNodes; i++) {
            if (getNode(i).getNodeAddress() == addr) {
                return (getNode(i));
            }
        }
        return (null);
    }

    /**
     * Working variable for keeping track of the active node, if any.
     */
    protected int curSerialNodeIndex = 0;

    /**
     * Public method to delete a Serial node by node address
     */
    public synchronized void deleteNode(int nodeAddress) {
        // find the serial node
        int index = 0;
        for (int i = 0; i < numNodes; i++) {
            if (nodeArray[i].getNodeAddress() == nodeAddress) {
                index = i;
            }
        }
        if (index == curSerialNodeIndex) {
            log.warn("Deleting the serial node active in the polling loop");
        }
        // Delete the node from the node list
        numNodes--;
        if (index < numNodes) {
            // did not delete the last node, shift 
            for (int j = index; j < numNodes; j++) {
                nodeArray[j] = nodeArray[j + 1];
            }
        }
        nodeArray[numNodes] = null;
    }

    private static Logger log = LoggerFactory.getLogger(AbstractMRNodeTrafficController.class.getName());
}
