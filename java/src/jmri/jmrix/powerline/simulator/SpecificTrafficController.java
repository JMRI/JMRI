package jmri.jmrix.powerline.simulator;

import java.io.DataInputStream;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.powerline.InsteonSequence;
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
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2005, 2006, 2008, 2009
 * @author Ken Cameron Copyright (C) 2010 Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SpecificTrafficController extends SerialTrafficController {

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
     * Send a sequence of X10 messages.
     * <p>
     * Makes them into the local messages and then queues in order.
     */
    @Override
    synchronized public void sendX10Sequence(X10Sequence s, SerialListener l) {
        s.reset();
        X10Sequence.Command c;
        while ((c = s.getCommand()) != null) {
            SpecificMessage m;
            if (c.isAddress()) {
                m = SpecificMessage.getX10Address(c.getHouseCode(), ((X10Sequence.Address) c).getAddress());
            } else if (c.isFunction()) {
                X10Sequence.Function f = (X10Sequence.Function) c;
                if (f.getDimCount() > 0) {
                    m = SpecificMessage.getX10FunctionDim(f.getHouseCode(), f.getFunction(), f.getDimCount());
                } else {
                    m = SpecificMessage.getX10Function(f.getHouseCode(), f.getFunction());
                }
            } else {
                // isn't address or function
                X10Sequence.ExtData e = (X10Sequence.ExtData) c;
                m = SpecificMessage.getExtCmd(c.getHouseCode(), e.getAddress(), e.getExtCmd(), e.getExtData());
            }
            sendSerialMessage(m, l);
            // Someone help me improve this
            // Without this wait, the commands are too close together and will return
            // an 0x15 which means they failed.
            // But there must be a better way to delay the sending of the next command.
            try {
                wait(250);
            } catch (InterruptedException ex) {
                log.error(null, ex);
            }
        }
    }

    /**
     * Send a sequence of Insteon messages.
     * <p>
     * Makes them into the local messages and then queues in order.
     */
    @Override
    synchronized public void sendInsteonSequence(InsteonSequence s, SerialListener l) {
        s.reset();
        InsteonSequence.Command c;
        while ((c = s.getCommand()) != null) {
            SpecificMessage m;
            if (c.isAddress()) {
                // We should not get here
                // Clean this up later
                m = SpecificMessage.getInsteonAddress(-1, -1, -1);
            } else {
                InsteonSequence.Function f = (InsteonSequence.Function) c;
                m = SpecificMessage.getInsteonFunction(f.getAddressHigh(), f.getAddressMiddle(), f.getAddressLow(), f.getFunction(), f.getFlag(), f.getCommand1(), f.getCommand2());
            }
            sendSerialMessage(m, l);
            // Someone help me improve this
            // Without this wait, the commands are too close together and will return
            // an 0x15 which means they failed.
            // But there must be a better way to delay the sending of the next command.
 /*
             try {
             wait(250);
             } catch (InterruptedException ex) {
             log.error("", ex);
             }
             */
        }
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

    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        if (msg.getNumDataElements() >= 2) {
            if (msg.getElement(0) != Constants.HEAD_STX) {
                return false;
            }
            int cmd = msg.getElement(1);
            switch (msg.getNumDataElements()) {
                case 2:
                    if (cmd == Constants.POLL_REQ_BUTTON_RESET) {
                        return true;
                    }
                    break;
                case 3:
                    if (cmd == Constants.POLL_REQ_BUTTON) {
                        return true;
                    }
                    break;
                case 4:
                    if (cmd == Constants.POLL_REQ_X10) {
                        return true;
                    }
                    break;
                case 5: // reply from send X10 command
                    if (cmd == Constants.FUNCTION_REQ_X10) {
                        return true;
                    }
                    break;
                case 11:
                    if (cmd == Constants.POLL_REQ_STD) {
                        return true;
                    }
                    break;
                case 12: // reply from send standard Insteon command
                    if ((cmd == Constants.FUNCTION_REQ_STD) && ((msg.getElement(5) & Constants.FLAG_BIT_STDEXT) == Constants.FLAG_STD)) {
                        return true;
                    }
                    break;
                case 25:
                    if (cmd == Constants.POLL_REQ_EXT) {
                        return true;
                    }
                    break;
                case 26: // reply from send extended Insteon command
                    if ((cmd == Constants.FUNCTION_REQ_STD) && ((msg.getElement(5) & Constants.FLAG_BIT_STDEXT) == Constants.FLAG_EXT)) {
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        if (logDebug) {
            log.debug("end of message: " + msg);
        }
        return false;
    }

    /**
     * Read a stream and pick packets out of it. Knows the size of the packets
     * from the contents.
     */
    @Override
    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        byte char1 = readByteProtected(istream);
        if (logDebug) {
            log.debug("loadChars: " + char1);
        }
        if ((char1 & 0xFF) == Constants.HEAD_STX) {  // 0x02 means start of command.
            msg.setElement(0, char1);
            byte char2 = readByteProtected(istream);
            if ((char2 & 0xFF) == Constants.FUNCTION_REQ_STD) {  // 0x62 means normal send command reply.
                msg.setElement(1, char2);
                byte addr1 = readByteProtected(istream);
                msg.setElement(2, addr1);
                byte addr2 = readByteProtected(istream);
                msg.setElement(3, addr2);
                byte addr3 = readByteProtected(istream);
                msg.setElement(4, addr3);
                byte flag1 = readByteProtected(istream);
                msg.setElement(5, flag1);
                int bufsize = 2 + 1;
                if ((flag1 & Constants.FLAG_BIT_STDEXT) != 0x00) {
                    bufsize = 14 + 1;
                }
                for (int i = 6; i < (5 + bufsize); i++) {
                    byte byt = readByteProtected(istream);
                    msg.setElement(i, byt);
                }
            } else if ((char2 & 0xFF) == Constants.FUNCTION_REQ_X10) {  // 0x63 means normal send X10 command reply.
                msg.setElement(1, char2);
                byte addrx1 = readByteProtected(istream);
                msg.setElement(2, addrx1);
                byte cmd1 = readByteProtected(istream);
                msg.setElement(3, cmd1);
                byte ack1 = readByteProtected(istream);
                msg.setElement(4, ack1);
            } else if ((char2 & 0xFF) == Constants.POLL_REQ_STD) {  // 0x50 means normal command received.
                msg.setElement(1, char2);
                for (int i = 2; i < (2 + 9); i++) {
                    byte byt = readByteProtected(istream);
                    msg.setElement(2, byt);
                }
            } else if ((char2 & 0xFF) == Constants.POLL_REQ_EXT) {  // 0x51 means extended command received.
                msg.setElement(1, char2);
                for (int i = 2; i < (2 + 23); i++) {
                    byte byt = readByteProtected(istream);
                    msg.setElement(2, byt);
                }
            } else if ((char2 & 0xFF) == Constants.POLL_REQ_X10) {  // 0x52 means standard X10 received command.
                msg.setElement(1, char2);
                byte rawX10data = readByteProtected(istream);
                msg.setElement(2, rawX10data);
                byte x10Flag = readByteProtected(istream);
                msg.setElement(3, x10Flag);
                if ((x10Flag&0xFF) == Constants.FLAG_X10_RECV_CMD) {
                    if (logDebug) {
                        log.debug("loadChars: X10 Command Poll Received " + X10Sequence.houseValueToText((rawX10data & 0xF0) >> 4) + " " + X10Sequence.functionName((rawX10data & 0x0F)));
                    }
                } else {
                    if (logDebug) {
                        log.debug("loadChars: X10 Unit Poll Received " + X10Sequence.houseValueToText((rawX10data & 0xF0) >> 4) + " " + X10Sequence.formatCommandByte(rawX10data));
                    }
                }
            } else if ((char2 & 0xFF) == Constants.POLL_REQ_BUTTON) {  // 0x54 means interface button received command.
                msg.setElement(1, char2);
                byte dat = readByteProtected(istream);
                msg.setElement(2, dat);
            } else if ((char2 & 0xFF) == Constants.POLL_REQ_BUTTON_RESET) {  // 0x55 means interface button received command.
                msg.setElement(1, char2);
            } else {
                msg.setElement(1, char2);
                if (logDebug) {
                    log.debug("loadChars: Unknown cmd byte " + char2);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SpecificTrafficController.class);

}
