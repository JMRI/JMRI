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

    // from an XpressNet message (used for protocol tunneling)
    public Z21Message(jmri.jmrix.lenz.XNetMessage m) {
        this(m.getNumDataElements() + 4);
        this.setOpCode(0x0040);
        for (int i = 0; i < m.getNumDataElements(); i++) {
            setElement(i + 4, m.getElement(i));
        }
    }

    // from an LocoNetNet message (used for protocol tunneling)
    public Z21Message(jmri.jmrix.loconet.LocoNetMessage m) {
        this(m.getNumDataElements() + 4);
        this.setOpCode(0x00A2);
        for (int i = 0; i < m.getNumDataElements(); i++) {
            setElement(i + 4, m.getElement(i));
        }
    }

    /**
     * This ctor interprets the String as the exact sequence to send,
     * byte-for-byte.
     *
     */
    public Z21Message(String m) {
        super(m);
        setBinary(true);
        // gather bytes in result
        byte[] b = jmri.util.StringUtil.bytesFromHexString(m);
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

    @Override
    public void setOpCode(int i) {
        _dataChars[2] = (i & 0x00ff);
        _dataChars[3] = ((i & 0xff00) >> 8);
    }

    @Override
    public int getOpCode() {
        return ( (0xff & _dataChars[2]) + ((0xff & _dataChars[3]) << 8));
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
        byte[] byteData = new byte[_dataChars.length];
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
     * @return z21 message for a hardware information request.
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
     * Set the broadcast flags as described in section 2.16 of the 
     * Roco Z21 Protocol Manual.
     * <p>
     * Brief descriptions of the flags are as follows (losely 
     * translated from German with the aid of google translate).
     * <ul>
     * <li>0x00000001 send XpressNet related information (track
     * power on/off, programming mode, short circuit, broadcast stop, 
     * locomotive information, turnout information).</li>
     * <li>0x00000002 send data changes that occur on the RMBUS.</li>
     * <li>0x00000004 (deprecated by Roco) send Railcom Data</li>
     * <li>0x00000100 send changes in system state (such as track voltage)
     * <li>0x00010000 send changes to locomotives on XpressNet (must also have
     * 0x00000001 set.</li>
     * <li>0x01000000 forward LocoNet data to the client.  Does not send
     * Locomotive or turnout data.</li>
     * <li>0x02000000 send Locomotive specific LocoNet data to the client.</li>
     * <li>0x04000000 send Turnout specific LocoNet data to the client.</li>
     * <li>0x08000000 send Occupancy information from LocoNet to the client</li>
     * <li>0x00040000 Automatically send updates for Railcom data to the client</li>
     * <li>0x00080000 send can detector messages to the client</li>
     * </ul>
     *
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


    /**
     * @return z21 message for LAN_RAILCOM_GETDATA request.
     */
    public static Z21Message getLanRailComGetDataRequestMessage() {
        Z21Message retval = new Z21Message(4);
        retval.setElement(0, 0x04);
        retval.setElement(1, 0x00);
        retval.setElement(2, 0x89);
        retval.setElement(3, 0x00);
        return retval;
    }

    /**
     * @return z21 message for LAN_SYSTEMSTATE_GETDATA
     */
    public static Z21Message getLanSystemStateDataChangedRequestMessage(){
        Z21Message retval = new Z21Message(4);
        retval.setElement(0, 0x04);
        retval.setElement(1, 0x00);
        retval.setElement(2, 0x85);
        retval.setElement(3, 0x00);
        return retval;
    }

    @Override
    public String toMonitorString() {
        switch(getOpCode()){
           case 0x0010:
               return Bundle.getMessage("Z21MessageStringSerialNoRequest");
           case 0x001A:
               return Bundle.getMessage("Z21MessageStringVersionRequest");
           case 0x0040:
               return Bundle.getMessage("Z21MessageXpressNetTunnelRequest",new Z21XNetMessage(this).toMonitorString());
           case 0x0050:
               return Bundle.getMessage("Z21MessageSetBroadcastFlags",Z21MessageUtils.interpretBroadcastFlags(_dataChars));
           case 0x0051:
               return Bundle.getMessage("Z21MessageRequestBroadcastFlags");
           case 0x00A2:
               return Bundle.getMessage("Z21LocoNetLanMessage", getLocoNetMessage().toMonitorString());
           case 0x0081:
               return Bundle.getMessage("Z21RMBusGetDataRequest", getElement(4));
           case 0x0082:
               return Bundle.getMessage("Z21RMBusProgramModuleRequest", getElement(4));
           case 0x0089:
               return Bundle.getMessage("Z21_RAILCOM_GETDATA");
           case 0x00C4:
               int networkID = ( getElement(4) & 0xFF) + ((getElement(5) & 0xFF) << 8);
               return Bundle.getMessage("Z21CANDetectorRequest",networkID);
           default:
        }
        return toString();
    }

    // handle LocoNet messages tunneled in Z21 messages
    boolean isLocoNetTunnelMessage() {
        return( getOpCode() == 0x00A2);
    }

    boolean isLocoNetDispatchMessage() {
       return (getOpCode() == 0x00A3);
    }

    boolean isLocoNetDetectorMessage() {
       return (getOpCode() == 0x00A4);
    }

    jmri.jmrix.loconet.LocoNetMessage getLocoNetMessage() {
        jmri.jmrix.loconet.LocoNetMessage lnr = null;
        if (isLocoNetTunnelMessage()) {
            int i = 4;
            lnr = new jmri.jmrix.loconet.LocoNetMessage(getLength()-4);
            for (; i < getLength(); i++) {
                lnr.setElement(i - 4, getElement(i));
            }
        }
        return lnr;
    }

    /**
     * @param group the RM Bus group number to request.
     * @return z21 message for LAN_RMBUS_GETDATA 
     */
    public static Z21Message getLanRMBusGetDataRequestMessage(int group){
        if(group!=0 && group!=1){
           throw new IllegalArgumentException("RMBus Group not 0 or 1");
        }
        Z21Message retval = new Z21Message(5);
        retval.setElement(0, 0x04);
        retval.setElement(1, 0x00);
        retval.setElement(2, 0x81);
        retval.setElement(3, 0x00);
        retval.setElement(4, (group & 0xff));
        return retval;
    }

    /**
     * @param address the RM Bus address to write.
     * @return z21 message for LAN_RMBUS_PROGRAMMODULE
     */
    public static Z21Message getLanRMBusProgramModuleMessage(int address){
        if(address>20){
           throw new IllegalArgumentException("RMBus Address > 20");
        }
        Z21Message retval = new Z21Message(5);
        retval.setElement(0, 0x05);
        retval.setElement(1, 0x00);
        retval.setElement(2, 0x82);
        retval.setElement(3, 0x00);
        retval.setElement(4, (address & 0xff));
        return retval;
    }

    // handle CAN Feedback/Railcom Messages
    boolean isCanDetectorMessage() {
        return (getOpCode() == 0x00C4);
    }

    /**
     * @param address CAN NetworkID of the module to request data from.
     * @return z21 message for LAN_CAN_DETECTOR request message
     */
    public static Z21Message getLanCanDetector(int address){
        Z21Message retval = new Z21Message(7);
        retval.setElement(0, 0x07);
        retval.setElement(1, 0x00);
        retval.setElement(2, 0xC4);
        retval.setElement(3, 0x00);
        retval.setElement(4, 0x00);// type, currently fixed.
        retval.setElement(5, (address & 0xff));
        retval.setElement(6, ((address & 0xff00)>>8));
        return retval;
    }

    private static final Logger log = LoggerFactory.getLogger(Z21Message.class);

}
