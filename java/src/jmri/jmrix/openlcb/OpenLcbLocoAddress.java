package jmri.jmrix.openlcb;

import jmri.DccLocoAddress;
import org.openlcb.NodeID;

/**
 * Encapsulate information for an OpenLCB Locomotive Decoder Address.
 *
 * The address information is an OpenLCB node ID.
 *
 * This should not be a child of DccLocoAddress, but rather of LocoAddress. But
 * the code isn't up to that right now.
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class OpenLcbLocoAddress extends DccLocoAddress {

    public OpenLcbLocoAddress(NodeID node) {
        super(0, false); // crap, just crap
        this.node = node;
    }

    @Override
    public boolean equals(Object a) {
        if (a == null) {
            return false;
        }
        try {
            OpenLcbLocoAddress other = (OpenLcbLocoAddress) a;
            return node.equals(other.node);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    public NodeID getNode() {
        return node;
    }

    NodeID node;

}
