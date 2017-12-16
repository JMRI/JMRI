package jmri.jmrix.ecos.simulator;

import jmri.jmrix.ecos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from ECOS messages. The "EcosInterface" side
 * sends/receives message objects.
 * <P>
 * The connection to a EcosPortController is via a pair of *Streams, which then
 * carry sequences of characters for transmission. Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transitions, based on the necessary state in each
 * message.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class EcosSimulatorTrafficController extends EcosTrafficController {

    public EcosSimulatorTrafficController() {
        super();
        log.debug("creating a new EcosSimulatorTrafficController object");
    }

    public EcosSimulatorTrafficController(EcosSystemConnectionMemo memo) {
        super(memo);
    }

    // override to avoid need to have good input streams
    @Override
    public void handleOneIncomingReply() {
        // nothing to do
    }

    @Override
    public void terminate() {
        // nothing to do
    }

    private final static Logger log = LoggerFactory.getLogger(EcosSimulatorTrafficController.class);

}
