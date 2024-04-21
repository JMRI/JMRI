package jmri.jmrix.openlcb.swing;

import jmri.util.JmriJFrame;

import org.openlcb.NodeID;

/**
 * JmriJFrame subclass which stores additional information
 * for locating this frame as carrying OpenLCB node information.
 *
 * @author Bob Jacobsen Copyright (C) 2024
 */
 
public class NodeSpecificFrame extends JmriJFrame {

    public NodeSpecificFrame(NodeID nodeID) {
        super();
        this.nodeID = nodeID;
    }
    
    final NodeID nodeID;
    
    public NodeID getNodeID() {
        return nodeID;
    }
    
}
