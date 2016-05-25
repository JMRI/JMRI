package jmri.jmrix.roco.z21;

import jmri.jmrix.AbstractMRMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for messages in the z21/Z21 protocol.
 *
 * Messages have the following format: 2 bytes data length. 2 bytes op code. n
 * bytes data.
 *
 * All numeric values are stored in little endian format.
 *
 * Carries a sequence of characters, with accessors.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author	Paul Bender Copyright (C) 2014
 */
public class Z21Message extends AbstractMRMessage {

    public Z21Message() {
        super();
        setBinary(true);
    }

    // create a new one
    public Z21Message(int i) {
        this();
        if (i < 4) { // minimum length is 2 bytes of length, 2 bytes of opcode.
            log.error("invalid length in call to ctor");
        }
        _nDataChars = i;
        _dataChars = new int[i];
        setLength(i);
    }

    // from an XPressNet message (used for protocol tunneling)
    public Z21Message(jmri.jmrix.lenz.XNetMessage m) {
        this(m.getNumDataElements() + 4);
        this.setOpCode(0x0040);
        for (int i = 0; i < m.getNumDataElements(); i++) {
            setElement(i + 4, m.getElement(i));
        }
    }

    /**
     * This ctor interprets the String as the exact sequence to send,
     * byte-for-byte.
     *
     * @param m
     */
    public Z21Message(String m) {
        super(m);
        setBinary(true);
        // gather bytes in result
        byte b[] = jmri.util.StringUtil.bytesFromHexString(m);
        if (b.length == 0) {
            // no such thing as a zero-length message
            _nDataChars = 0;
            _dataChars = null;
            return;
        }
        _nDataChars = b.length;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i < b.length; i++) {
            setElement(i, b[i]);
        }
    }

    /**
     * This ctor interprets the byte array as a sequence of characters to send.
     *
     * @param a Array of bytes to send
     */
    public Z21Message(byte[] a, int l) {
        super(String.valueOf(a));
        setBinary(true);
    }

    public void setOpCode(int i) {
        _dataChars[2] = (i & 0x00ff);
        _dataChars[3] = ((i & 0xff00) >> 8);
    }

    public int getOpCode() {
        return (_dataChars[2] + (_dataChars[3] << 8));
    }

    public void setLength(int i) {
        _dataChars[0] = (i & 0x00ff);
        _dataChars[1] = ((i & 0xff00) >> 8);
    }

    public int getLength() {
        return (_dataChars[0] + (_dataChars[1] << 8));
    }

    /*
     * package protected method to get the _dataChars buffer as bytes.
     * @return byte array containing the low order bits of the  integer 
     *         values in _dataChars.
     */
    byte[] getBuffer() {
        byte byteData[] = new byte[_dataChars.length];
        for (int i = 0; i < _dataChars.length; i++) {
            byteData[i] = (byte) (0x00ff & _dataChars[i]);
        }
        return byteData;
    }

    /*
     * canned messages
     */

    /*
     * @return z21 message for serial number request.
     */
    public static Z21Message getSerialNumberRequestMessage() {
        Z21Message retval = new Z21Message(4);
        retval.setElement(0, 0x04);
        retval.setElement(1, 0x00);
        retval.setElement(2, 0x10);
        retval.setElement(3, 0x00);
        return retval;
    }

    /*
     * @return z21 message for serial number request.
     */
    public static Z21Message getLanGetHardwareInfoRequestMessage() {
        Z21Message retval = new Z21Message(4);
        retval.setElement(0, 0x04);
        retval.setElement(1, 0x00);
        retval.setElement(2, 0x1A);
        retval.setElement(3, 0x00);
        return retval;
    }

    /*
     * @return z21 message for LAN_LOGOFF request.
     */
    public static Z21Message getLanLogoffRequestMessage() {
        Z21Message retval = new Z21Message(4){
           @Override 
           public boolean replyExpected() {
               return false; // Loging off generates no reply.
           }
        };
        retval.setElement(0, 0x04);
        retval.setElement(1, 0x00);
        retval.setElement(2, 0x30);
        retval.setElement(3, 0x00);
        return retval;
    }

    /**
     * @return z21 message for LAN_GET_BROADCAST_FLAGS request.
     */
    public static Z21Message getLanGetBroadcastFlagsRequestMessage() {
        Z21Message retval = new Z21Message(4);
        retval.setElement(0, 0x04);
        retval.setElement(1, 0x00);
        retval.setElement(2, 0x51);
        retval.setElement(3, 0x00);
        return retval;
    }

    /**
     * Set the boradcast flags as described in section 2.16 of the 
     * Roco Z21 Protocol Manual.
     * <P>
     * Brief descriptions of the flags are as follows (losely 
     * translated from German with the aid of google translate).
     * <P>
     * <UL>
     * <LI>0x00000001 send XPressNet related information (track 
     * power on/off, programming mode, short circuit, broadcast stop, 
     * locomotive information, turnout information).</LI>
     * <LI>0x00000002 send data changes that occur on the RMBUS.</LI>
     * <LI>0x00000004 (deprecated by Roco) send Railcom Data</LI>
     * <LI>0x00000100 send changes in system state (such as track voltage)
     * <LI>0x00010000 send changes to locomotives on XPressNet (must also have
     * 0x00000001 set.</LI>
     * <LI>0x01000000 forward LocoNet data to the client.  Does not send 
     * Locomotive or turnout data.</LI>
     * <LI>0x02000000 send Locomotive specific LocoNet data to the client.</LI>
     * <LI>0x04000000 send Turnout specific LocoNet data to the client.</LI>
     * <LI>0x08000000 send Occupancy information from LocoNet to the client</LI> 
     * </UL>
     * <P>
     * @param flags integer representing the flags (32 bits).
     * @return z21 message for LAN_SET_BROADCAST_FLAGS request.
     */
    public static Z21Message getLanSetBroadcastFlagsRequestMessage(int flags) {
        Z21Message retval = new Z21Message(8){
           @Override 
           public boolean replyExpected() {
               return false; // setting the broadcast flags generates 
                             // no reply.
           }
        };
        retval.setElement(0, 0x08);
        retval.setElement(1, 0x00);
        retval.setElement(2, 0x50);
        retval.setElement(3, 0x00);
        retval.setElement(4, (flags & 0x000000ff) );
        retval.setElement(5, (flags & 0x0000ff00)>>8 );
        retval.setElement(6, (flags & 0x00ff0000)>>16 );
        retval.setElement(7, (flags & 0xff000000)>>24 );
        return retval;
    }

    public String toMonitorString() {
        return toString();
    }

    private final static Logger log = LoggerFactory.getLogger(Z21Message.class.getName());

}
