package jmri.jmrix.grapevine.simulator;

import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from Grapevine messages. The "GrapevineInterface"
 * side sends/receives message objects.
 * <p>
 * The connection to a Grapevine SerialPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <p>
 * This handles the state transitions, based on the necessary state in each
 * message.
 * <p>
 * Migrated for multiple connections, multi char connection prefix and Simulator.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class GrapevineSimulatorTrafficController extends SerialTrafficController {

    /**
     * Ctor
     *
     * @param adaptermemo the associated SystemConnectionMemo
     */
    public GrapevineSimulatorTrafficController(GrapevineSystemConnectionMemo adaptermemo) {
        super(adaptermemo);
        setAllowUnexpectedReply(true); // there is some command sent during
                                       // testing we treat as unexpected...
    }

    private final static Logger log = LoggerFactory.getLogger(GrapevineSimulatorTrafficController.class);

}
