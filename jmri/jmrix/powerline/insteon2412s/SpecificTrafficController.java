// SpecificTrafficController.java

package jmri.jmrix.powerline.insteon2412s;

import java.util.logging.Level;
import java.util.logging.Logger;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.jmrix.powerline.X10Sequence;
import jmri.jmrix.powerline.InsteonSequence;
import jmri.jmrix.powerline.SerialListener;
import jmri.jmrix.powerline.SerialMessage;

import java.io.DataInputStream;

/**
 * Converts Stream-based I/O to/from messages.  The "SerialInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a SerialPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This maintains a list of nodes, but doesn't currently do anything
 * with it.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001, 2003, 2005, 2006, 2008, 2009
 * @version			$Revision: 1.5 $
 */
public class SpecificTrafficController extends SerialTrafficController {

	public SpecificTrafficController() {
        super();
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
    synchronized public void sendX10Sequence(X10Sequence s, SerialListener l) {
        s.reset();
        X10Sequence.Command c;
        while ( (c = s.getCommand() ) !=null) {
            SpecificMessage m;
            if (c.isAddress()) 
                m = SpecificMessage.getAddress(c.getHouseCode(), ((X10Sequence.Address)c).getAddress());
            else {
                X10Sequence.Function f = (X10Sequence.Function)c;
                if (f.getDimCount() > 0)
                    m = SpecificMessage.getFunctionDim(f.getHouseCode(), f.getFunction(), f.getDimCount());
                else
                    m = SpecificMessage.getFunction(f.getHouseCode(), f.getFunction());
            }
            sendSerialMessage(m, l);
            // Someone help me improve this
            // Without this wait, the commands are too close together and will return
            // an 0x15 which means they failed.
            // But there must be a better way to delay the sending of the next command.
            try {
                wait(250);
            } catch (InterruptedException ex) {
                Logger.getLogger(SpecificTrafficController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Send a sequence of Insteon messages
     * <p>
     * Makes them into the local messages and then queues in order
     */
    synchronized public void sendInsteonSequence(InsteonSequence s, SerialListener l) {
        s.reset();
        InsteonSequence.Command c;
        while ( (c = s.getCommand() ) !=null) {
            SpecificMessage m;
            if (c.isAddress()) {
                // We should not get here
                // Clean this up later
                m = SpecificMessage.getInsteonAddress("Error");
            } else {
                InsteonSequence.Function f = (InsteonSequence.Function)c;
                if (f.getDimCount() > 0) {
                    m = SpecificMessage.getInsteonFunctionDim(f.getAddress(), f.getFunction(), f.getDimCount());
                } else {
                    m = SpecificMessage.getInsteonFunction(f.getAddress(), f.getFunction());
                }
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
                Logger.getLogger(SpecificTrafficController.class.getName()).log(Level.SEVERE, null, ex);
            }
 */
        }
    }

    /**
     * This system provides 256 dim steps
     */
    public int getNumberOfIntensitySteps() { return 255; }
    
    /**
     * Get a message of a specific length for filling in.
     */
    public SerialMessage getSerialMessage(int length) {
        return new SpecificMessage(length);
    }

    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        if (logDebug) log.debug("forward "+m);
        sendInterlock = ((SerialMessage)m).getInterlocked();
        super.forwardToPort(m, reply);
    }
        
    protected AbstractMRReply newReply() { 
        SpecificReply reply = new SpecificReply();
        return reply;
    }

    boolean sendInterlock = false; // send the 00 interlock when CRC received
    boolean expectLength = false;  // next byte is length of read
    boolean countingBytes = false; // counting remainingBytes into reply buffer
    int remainingBytes = 0;        // count of bytes _left_
    
    protected boolean endOfMessage(AbstractMRReply msg) {
        // check if this byte is length
        if (expectLength) {
            expectLength = false;
            countingBytes = true;
            remainingBytes = msg.getElement(1)&0xF; // 0 was the read command; max 9, really
            if (logDebug) log.debug("Receive count set to "+remainingBytes);
            return false;
        }
        if (remainingBytes>0) {
            if (remainingBytes>8) {
                log.error("Invalid remainingBytes: "+remainingBytes);
                remainingBytes = 0;
                return true;
            }
            remainingBytes--;
            if (remainingBytes == 0) {
                countingBytes = false;
                return true;  // done
            }
            return false; // wait for one more
        }
        // check for data available
        if ((msg.getElement(0)&0xFF)==Constants.POLL_REQ) {
            // get message
            SerialMessage m = new SpecificMessage(1);
            m.setElement(0, Constants.POLL_ACK);
            expectLength = true;  // next byte is length
            forwardToPort(m, null);
            return false;  // reply message will get data appended            
        }
        // check for request time
        if ((msg.getElement(0)&0xFF)==Constants.TIME_REQ) {
            SerialMessage m = SpecificMessage.setCM11Time(X10Sequence.encode(1));
            forwardToPort(m, null);
            return true;  // message done
        }
        // if the interlock is present, send it
        if (sendInterlock) {
        	if (logDebug) log.debug("Send interlock");
            sendInterlock = false;
            SerialMessage m = new SpecificMessage(1);
            m.setElement(0,0); // not really needed, but this is a slow protocol anyway
            forwardToPort(m, null);
            return false; // just leave in buffer
        }
        if (logDebug) log.debug("end of message: "+msg);
        return true;
    }

    protected void loadChars(AbstractMRReply msg, DataInputStream istream) throws java.io.IOException {
        byte char1 = readByteProtected(istream);
        if (char1 == 0x02) {  // 0x02 means start of command.
            msg.setElement(0, char1);
            byte char2 = readByteProtected(istream);
            if ((char2&0xFF) == 0x62) {  // 0x62 means send command.
                msg.setElement(1, char2);
                byte addr1 = readByteProtected(istream);
                msg.setElement(2, addr1);
                byte addr2 = readByteProtected(istream);
                msg.setElement(3, addr2);
                byte addr3 = readByteProtected(istream);
                msg.setElement(4, addr3);
                byte flag1 = readByteProtected(istream);
                msg.setElement(5, flag1);
                byte cmd1 = readByteProtected(istream);
                msg.setElement(6, cmd1);
                byte cmd2 = readByteProtected(istream);
                msg.setElement(7, cmd2);
                byte ack1 = readByteProtected(istream);
                if (ack1 == 0x06) {  // 0x06 means command sent.
                    msg.setElement(8, ack1);
                }
            } else {
                if ((char2&0xFF) == 0x50) {  // 0x50 means reply command.
                    msg.setElement(1, char2);
                    byte addrt1 = readByteProtected(istream);
                    msg.setElement(2, addrt1);
                    byte addrt2 = readByteProtected(istream);
                    msg.setElement(3, addrt2);
                    byte addrt3 = readByteProtected(istream);
                    msg.setElement(4, addrt3);
                    byte addrf1 = readByteProtected(istream);
                    msg.setElement(5, addrf1);
                    byte addrf2 = readByteProtected(istream);
                    msg.setElement(6, addrf2);
                    byte addrf3 = readByteProtected(istream);
                    msg.setElement(7, addrf3);
                    byte flag1 = readByteProtected(istream);
                    msg.setElement(8, flag1);
                    byte cmd1 = readByteProtected(istream);
                    msg.setElement(9, cmd1);
                    byte cmd2 = readByteProtected(istream);
                    msg.setElement(10, cmd2);
                    byte ack1 = readByteProtected(istream);
                    if (ack1 == 0x06) {  // 0x06 means command sent.
                       msg.setElement(11, ack1);
                    }
                } else {
                    if ((char2&0xFF) == 0x63) {  // 0x63 means X10 command.
                        msg.setElement(1, char2);
                        byte addrx1 = readByteProtected(istream);
                        msg.setElement(2, addrx1);
                        byte cmd1 = readByteProtected(istream);
                        msg.setElement(3, cmd1);
                        byte ack1 = readByteProtected(istream);
                        if (ack1 == 0x06) {  // 0x06 means command sent.
                           msg.setElement(4, ack1);
                        }
                    } else {
                        msg.setElement(1, char2);
                    }
                }
            }
        } else {
            if ((char1 == 0x15)) {
                msg.setElement(0, char1);
            } else {
                if ((char1&0xFF) == 0x62) {  // 0x62 means send command.
                    msg.setElement(0, char1);
                    byte addr1 = readByteProtected(istream);
                    msg.setElement(1, addr1);
                    byte addr2 = readByteProtected(istream);
                    msg.setElement(2, addr2);
                    byte addr3 = readByteProtected(istream);
                    msg.setElement(3, addr3);
                    byte flag1 = readByteProtected(istream);
                    msg.setElement(4, flag1);
                    byte cmd1 = readByteProtected(istream);
                    msg.setElement(5, cmd1);
                    byte cmd2 = readByteProtected(istream);
                    msg.setElement(6, cmd2);
                    byte ack1 = readByteProtected(istream);
//                    if (ack1 == 0x06) {  // 0x06 means command sent.
                        msg.setElement(7, ack1);
//                    }
                } else {
                    if ((char1&0xFF) == 0x50) {  // 0x62 means send command.
                        msg.setElement(0, char1);
                        byte addr1 = readByteProtected(istream);
                        msg.setElement(1, addr1);
                        byte addr2 = readByteProtected(istream);
                        msg.setElement(2, addr2);
                        byte addr3 = readByteProtected(istream);
                        msg.setElement(3, addr3);
                        byte faddr1 = readByteProtected(istream);
                        msg.setElement(4, faddr1);
                        byte faddr2 = readByteProtected(istream);
                        msg.setElement(5, faddr2);
                        byte faddr3 = readByteProtected(istream);
                        msg.setElement(6, faddr3);
                        byte flag1 = readByteProtected(istream);
                        msg.setElement(7, flag1);
                        byte cmd1 = readByteProtected(istream);
                        msg.setElement(8, cmd1);
                        byte cmd2 = readByteProtected(istream);
                        msg.setElement(9, cmd2);
                        byte ack1 = readByteProtected(istream);
                        msg.setElement(10, ack1);
                    } else {
                        if ((char1&0xFF) == 0x63) {  // 0x62 means send command.
                            msg.setElement(0, char1);
                            byte addr1 = readByteProtected(istream);
                            msg.setElement(1, addr1);
                            byte addr2 = readByteProtected(istream);
                            msg.setElement(2, addr2);
                            byte addr3 = readByteProtected(istream);
                            msg.setElement(3, addr3);
                        } else {
                            msg.setElement(0, char1);
                        }
                    }
                }
            }
        }
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpecificTrafficController.class.getName());
}


/* @(#)SpecificTrafficController.java */
