package jmri.jmrix.powerline.cp290;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.powerline.SerialListener;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.jmrix.powerline.X10Sequence;
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
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2005, 2006, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SpecificTrafficController extends SerialTrafficController {

    private boolean cmdOutstanding;

    public SpecificTrafficController(SerialSystemConnectionMemo memo) {
        super();
        this.memo = memo;
        logDebug = log.isDebugEnabled();

        // not polled at all, so allow unexpected messages, and
        // use poll delay just to spread out startup
        setAllowUnexpectedReply(true);
        mWaitBeforePoll = 1000;  // can take a long time to send

    }

    /**
     * Send a sequence of X10 messages
     * <p>
     * Makes them into the local messages and then queues in order
     */
    @Override
    synchronized public void sendX10Sequence(X10Sequence s, SerialListener l) {
        s.reset();
        X10Sequence.Command c;
        // index through address commands
        int devicemask = 0;
        // there should be at least one address
        c = s.getCommand();
        if (c == null) {
            return;  // nothing!
        }
        int housecode = c.getHouseCode();
        devicemask = setDeviceBit(devicemask, ((X10Sequence.Address) c).getAddress());

        // loop through other addresses, if any
        while (((c = s.getCommand()) != null) && (c.isAddress())) {
            if (housecode != ((X10Sequence.Address) c).getHouseCode()) {
                log.error("multiple housecodes found: " + housecode + ", " + c.getHouseCode());
                return;
            }
            devicemask = setDeviceBit(devicemask, ((X10Sequence.Address) c).getAddress());
        }
        // at this point, we've picked up all the addresses, start
        // to process functions; there should be at least one
        if (c == null) {
            log.warn("no command");
            return;
        }
        formatAndSend(housecode, devicemask, (X10Sequence.Function) c, l);

        // loop through other functions, if any
        while (((c = s.getCommand()) != null) && (c.isFunction())) {
            if (housecode != ((X10Sequence.Function) c).getHouseCode()) {
                log.error("multiple housecodes found: " + housecode + ", " + c.getHouseCode());
                return;
            }
            formatAndSend(housecode, devicemask, (X10Sequence.Function) c, l);
        }
    }

    /**
     * Turn a 1-16 device number into a mask bit
     * @param devicemask mask value
     * @param device     X10 device code
     * @return           bit mask for device code
     */
    int setDeviceBit(int devicemask, int device) {
        return devicemask | (0x10000 >> device);
    }

    /**
     * Format a message and send it
     * @param housecode  X10 housecode value
     * @param devicemask X10 devicemask
     * @param c          X10 cmd code
     * @param l          listener
     */
    void formatAndSend(int housecode, int devicemask,
            X10Sequence.Function c, SerialListener l) {
        SpecificMessage m = new SpecificMessage(22);  // will be 22 bytes
        for (int i = 0; i < 16; i++) {
            m.setElement(i, 0xFF);
        }
        int level = c.getDimCount();
        if (level > 16) {
            log.warn("can't handle dim counts > 15?");
            level = 16;
        }
        if (logDebug) {
            log.debug("dim level: " + level);
        }
        level = 16 - level;
        int function = c.getFunction();

        // need to encode the housecode into line code
        int lineHouseCode = X10Sequence.encode(housecode);

        m.setElement(16, 1);
        m.setElement(17, level * 16 + function);
        m.setElement(18, lineHouseCode * 16 + 0);
        m.setElement(19, devicemask & 0xFF);
        m.setElement(20, (devicemask >> 8) & 0xFF);
        m.setElement(21, 0xFF & (m.getElement(17) + m.getElement(18) + m.getElement(19) + m.getElement(20))); // checksum

        sendSerialMessage(m, l);
        cmdOutstanding = true;
    }

    /**
     * This system provides 16 dim steps
     */
    @Override
    public int getNumberOfIntensitySteps() {
        return 16;
    }

    /**
     * Get a message of a specific length for filling in.
     */
    @Override
    public SerialMessage getSerialMessage(int length) {
        return new SpecificMessage(length);
    }

    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (logDebug) {
            log.debug("forward " + m);
        }
        super.forwardToPort(m, reply);
    }

    @Override
    protected AbstractMRReply newReply() {
        SpecificReply reply = new SpecificReply(memo.getTrafficController());
        return reply;
    }

    /**
     * Decide if a reply have been completely received.
     *
     * @return true if the reply is complete
     */
    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        // count number of FF bytes
        // if 16 FF, byte 17 is 0x01, expect total of 22 bytes, direct msg
        // if 16 FF, byte 17 is 0x02, expect total of 21 bytes, clock msg
        // if 16 FF, byte 17 is 0x03, expect total of 28 bytes, timer msg
        // if 16 FF, byte 17 is 0x03, expect total of 22 bytes, graphic msg
        // if 16 FF, byte 17 is 0x04, expect total of 18 bytes, time/housecode msg
        // if 6 FF, byte 7 is 0x00, means AC line was off
        // if 6 FF, byte 7 is 0x01, means Ack of cmd, if command just sent
        // if 6 FF, byte 7 is 0x01 or 0x00, but 12 bytes total is record of command on AC line
        int syncCount = 0;
        for (int i = 0; i < msg.getNumDataElements(); i++) {
            if ((msg.getElement(i) & 0xFF) == 0xFF) {
                syncCount++;
            } else {
                break;
            }
        }
        if (cmdOutstanding) {
            if (syncCount == 6) {
                if (msg.getNumDataElements() == 7) {
                    cmdOutstanding = false;
                    return true;
                }
            }
        } else {
            if (syncCount == 6) {
                if (msg.getNumDataElements() == 12) {
                    return true;
                }
            }
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(SpecificTrafficController.class);
}
