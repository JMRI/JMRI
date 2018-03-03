package jmri.jmrix.easydcc.simulator;

import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import jmri.jmrix.easydcc.EasyDccTrafficController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from EasyDcc messages. The "EasyDccInterface"
 * side sends/receives message objects.
 * <p>
 * The connection to a EasyDccPortController is via a pair of *Streams, which
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
public class EasyDccSimulatorTrafficController extends EasyDccTrafficController {

    /**
     * Ctor
     *
     * @param adaptermemo the associated SystemConnectionMemo
     */
    public EasyDccSimulatorTrafficController(EasyDccSystemConnectionMemo adaptermemo) {
        super(adaptermemo);
        setAllowUnexpectedReply(true); // there is some command sent during
                                       // testing we treat as unexpected...
    }

    // private final static Logger log = LoggerFactory.getLogger(EasyDccSimulatorTrafficController.class);

}
