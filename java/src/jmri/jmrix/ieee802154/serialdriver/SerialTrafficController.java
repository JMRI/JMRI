// SerialTrafficController

package jmri.jmrix.ieee802154.serialdriver;

import jmri.jmrix.ieee802154.IEEE802154Message;
import jmri.jmrix.ieee802154.IEEE802154Reply;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.ieee802154.IEEE802154TrafficController;

/**
 * Traffic Controller interface for communicating with IEEE 802.15.4 devices
 * directly using IEEE 802.15.4 formated messages.
 * @author Paul Bender Copyright (C) 2013
 * @version $Revision$
 */
public class SerialTrafficController extends IEEE802154TrafficController{

    /**
     * Get a message of a specific length for filling in.
     * <p>
     * This is a default, null implementation, which must be overridden
     * in an adapter-specific subclass.
     */
    public IEEE802154Message getIEEE802154Message(int length) {return new IEEE802154Message(length);}

    /**
     * <p>
     * This is a default, null implementation, which must be overridden
     * in an adapter-specific subclass.
     */
    protected AbstractMRReply newReply() {return new IEEE802154Reply(this);}

    /*
     * Build a new IEEE802154 Node.
     * @return new IEEE802154Node.
     */
    public jmri.jmrix.ieee802154.IEEE802154Node newNode() { return new SerialNode(); }

}
