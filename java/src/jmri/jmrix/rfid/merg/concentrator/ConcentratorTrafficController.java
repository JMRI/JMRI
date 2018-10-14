package jmri.jmrix.rfid.merg.concentrator;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.rfid.RfidMessage;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.jmrix.rfid.RfidTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from messages. The "SerialInterface" side
 * sends/receives message objects.
 * <p>
 * The connection to a SerialPortController is via a pair of *Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <p>
 * This maintains a list of nodes, but doesn't currently do anything with it.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2005, 2006, 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class ConcentratorTrafficController extends RfidTrafficController {

    private final String range;

    public ConcentratorTrafficController(RfidSystemConnectionMemo memo, String range) {
        super();
        adapterMemo = memo;
        this.range = range;
        logDebug = log.isDebugEnabled();

        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 1000;  // can take a long time to send

    }

    @Override
    public void sendInitString() {
        String init = adapterMemo.getProtocol().initString();
        if (init.length() > 0) {
            sendRfidMessage(new ConcentratorMessage(init, 0), null);
        }
    }

    @Override
    public RfidMessage getRfidMessage(int length) {
        return new ConcentratorMessage(length);
    }

    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (logDebug) {
            log.debug("forward " + m);
        }
        sendInterlock = ((RfidMessage) m).getInterlocked();
        super.forwardToPort(m, reply);
    }

    @Override
    protected AbstractMRReply newReply() {
        ConcentratorReply reply = new ConcentratorReply(adapterMemo.getTrafficController());
        return reply;
    }

    @Override
    public String getRange() {
        return range;
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        return adapterMemo.getProtocol().endOfMessage(msg);
    }

    boolean sendInterlock = false; // send the 00 interlock when CRC received

    private static final Logger log = LoggerFactory.getLogger(ConcentratorTrafficController.class);

}
