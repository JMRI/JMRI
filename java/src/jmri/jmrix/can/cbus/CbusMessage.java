package jmri.jmrix.can.cbus;

import jmri.ProgrammingMode;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanFrame;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanMutableFrame;
import jmri.jmrix.can.CanReply;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to allow use of CBUS concepts to access the underlying can message.
 * <p>
 * Methods that take a CanMessage or CanReply as argument:
 * <ul>
 * <li>CanMessage - Can Frame being sent by JMRI
 * <li>CanReply - Can Frame being received by JMRI
 * </ul>
 * https://github.com/MERG-DEV/CBUSlib.
 * 
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young (C) 2018
 */
public class CbusMessage {

    /**
     * Return a CanReply for use in sensors, turnouts + light
     * If a response event, set to normal event
     * In future, this may also translate extended messages down to normal messages.
     *
     * @param original CanReply to be coverted to normal opc
     * @return new CanReply perhaps converted from response OPC to normal OPC.
     */
    public static CanReply opcRangeToStl(CanReply original){
        CanReply msg = new CanReply(original);
        int opc = getOpcode(msg);
        // log.debug(" about to check opc {} ",opc);
        switch (opc) {
            case CbusConstants.CBUS_ARON:
                msg.setElement(0, CbusConstants.CBUS_ACON);
                break;
            case CbusConstants.CBUS_AROF:
                msg.setElement(0, CbusConstants.CBUS_ACOF);
                break;
            case CbusConstants.CBUS_ARSON:
                msg.setElement(0, CbusConstants.CBUS_ASON);
                break;
            case CbusConstants.CBUS_ARSOF:
                msg.setElement(0, CbusConstants.CBUS_ASOF);
                break;
            default:
                break;
        }
        return msg;
    }
    

    /**
     * Get the Op Code from the CanMessage
     *
     * @param am CanMessage or CanReply
     * @return OPC of the message
     */
    public static int getOpcode(AbstractMessage am) {
        return  am.getElement(0);
    }

    /**
     * Get the Data Length from the CanMessage
     *
     * @param am CanMessage or CanReply
     * @return the message data length
     */
    public static int getDataLength(AbstractMessage am) {
        return am.getElement(0) >> 5;
    }
    
    /**
     * Get the Node Number from a CanFrame Event
     *
     * @param am CanMessage or CanReply
     * @return the node number if not a short event
     */
    public static int getNodeNumber(AbstractMessage am) {
        if (isEvent(am) && !isShort(am) ) {
            return am.getElement(1) * 256 + am.getElement(2);
        } else {
            return 0;
        }
    }
    
    /**
     * Get the Event Number from a CBUS Event
     *
     * @param m CanMessage or CanReply
     * @return the message event ( device ) number, else -1 if not an event.
     */
    public static int getEvent(AbstractMessage m) {
        if (isEvent(m)) {
            return m.getElement(3) * 256 + m.getElement(4);
        } else {
            return -1;
        }
    }
    
    /**
     * Get the Event Type ( on or off ) from a CanFrame
     *
     * @param am CanFrame or CanReply
     * @return CbusConstant EVENT_ON or EVENT_OFF
     */
    public static int getEventType(AbstractMessage am) {
        if ( CbusOpCodes.isOnEvent(am.getElement(0))) {
            return CbusConstants.EVENT_ON;
        } else {
            return CbusConstants.EVENT_OFF;
        }
    }

    /**
     * Tests if a CanMessage or CanReply is an Event.
     * Performs Extended and RTR check.
     * Adheres to cbus spec, ie on off responses to an AREQ are events.
     *
     * @param am CanMessage or CanReply
     * @return True if event, else False.
     */
    public static boolean isEvent(AbstractMessage am) {
        if ( am instanceof CanFrame && ((CanFrame)am).extendedOrRtr()){
            return false;
        }
        return CbusOpCodes.isEvent(am.getElement(0));
    }
    
    /**
     * Tests if CanFrame is a short event
     *
     * @param am CanReply or CanMessage
     * @return true if Short Event, else false
     */
    public static boolean isShort(AbstractMessage am) {
        return CbusOpCodes.isShortEvent(am.getElement(0));
    }

    /**
     * Set the CAN ID within a CanMessage or CanReply Header
     *
     * @param am CanMessage or CanReply
     * @param id CAN ID
     */
    public static void setId(AbstractMessage am, int id) throws IllegalArgumentException {
        if (am instanceof CanMutableFrame){
            CanMutableFrame m = (CanMutableFrame) am;
            int update = m.getHeader();
            if (m.isExtended()) {
                throw new IllegalArgumentException("No CAN ID Concept on Extended CBUS CAN Frame.");
            } else {
                if ((id & ~0x7f) != 0) {
                    throw new IllegalArgumentException("invalid standard ID value: " + id);
                }
                m.setHeader((update & ~0x07f) | id);
            }
        }
        else {
            throw new IllegalArgumentException(am + " is Not a CanMutableFrame");
        }
    }

    /**
     * Set the priority within a CanMessage or CanReply Header.
     *
     * @param am CanMessage or CanReply
     * @param pri Priority
     */
    public static void setPri(AbstractMessage am, int pri) throws IllegalArgumentException {
        if (am instanceof CanMutableFrame){
            CanMutableFrame m = (CanMutableFrame) am;
            if ((pri & ~0x0F) != 0) {
                throw new IllegalArgumentException("Invalid CBUS Priority value: " + pri);
            }
            if (m.isExtended()) {
                throw new IllegalArgumentException("Extended CBUS CAN Frames do not have a priority concept.");
            } else {
                m.setHeader((m.getHeader() & ~0x780) | (pri << 7));
            }
        }
        else {
            throw new IllegalArgumentException(am + " is Not a CanMutableFrame");
        }
    }

    /**
     * Returns string form of a CanMessage ( a Can Frame sent by JMRI )
     * Short / Long events converted to Sensor / Turnout / Light hardware address
     * message priority not indicated
     * @param  m CanReply or CanMessage
     * @return String of hardware address form
     */
    public static String toAddress(AbstractMessage m) {
        switch (m.getElement(0)) {
            case CbusConstants.CBUS_ACON:
                // + form
                return "+n" + (m.getElement(1) * 256 + m.getElement(2)) + "e" + (m.getElement(3) * 256 + m.getElement(4));
            case CbusConstants.CBUS_ACOF:
                // - form
                return "-n" + (m.getElement(1) * 256 + m.getElement(2)) + "e" + (m.getElement(3) * 256 + m.getElement(4));
            case CbusConstants.CBUS_ASON:
                // + short form
                return "+" + (m.getElement(3) * 256 + m.getElement(4));
            case CbusConstants.CBUS_ASOF:
                // - short form
                return "-" + (m.getElement(3) * 256 + m.getElement(4));
            default:
                // hex form
                String tmp = m.toString().replaceAll("\\s*\\[[^\\]]*\\]\\s*", ""); // remove the [header]
                return "X" + tmp.replaceAll(" ", "");
        }
    }

    /**
     * Checks if a CanMessage is requesting Track Power Off
     * 
     * @param  m Can Frame Message
     * @return boolean
     */
    public static boolean isRequestTrackOff(CanMessage m) {
        return m.getOpCode() == CbusConstants.CBUS_RTOF;
    }

    /**
     * Checks if a CanMessage is requesting Track Power On
     * 
     * @param  m Can Frame Message
     * @return True if outgoing track power on request
     */
    public static boolean isRequestTrackOn(CanMessage m) {
        return m.getOpCode() == CbusConstants.CBUS_RTON;
    }

    /**
     * Get the CAN ID within a CanReply or CanMessage Header
     *
     * @param f CanReply or CanMessage
     * @return CAN ID of the outgoing message
     */
    public static int getId(AbstractMessage f) throws IllegalArgumentException {
        if (f instanceof CanFrame){
            CanFrame cfMsg = (CanFrame) f;
            if (cfMsg.isExtended()) {
                return cfMsg.getHeader() & 0x1FFFFF;
            } else {
                return cfMsg.getHeader() & 0x7f;
            }
        }
        else {
            throw new IllegalArgumentException(f + " is Not a CanFrame");
        }
    }

    /**
     * Get the priority from within the CanReply or CanMessage Header
     *
     * @param r CanReply or CanMessage
     * @return Priority of the outgoing message
     */
    public static int getPri(AbstractMessage r) throws IllegalArgumentException {
        if (r instanceof CanFrame){
            CanFrame cfMsg = (CanFrame) r;
            if (cfMsg.isExtended()) {
                return (cfMsg.getHeader() >> 25) & 0x0F;
            } else {
                return (cfMsg.getHeader() >> 7) & 0x0F;
            }
        }
        else {
            throw new IllegalArgumentException(r + " is Not a CanFrame");
        }
    }

    /**
     * Tests if CanReply is confirming Track Power Off.
     *
     * @param m CanReply
     * @return True if is a Track Off notification
     */
    public static boolean isTrackOff(CanReply m) {
        return m.getOpCode() == CbusConstants.CBUS_TOF;
    }

    /**
     * Tests if CanReply is confirming Track Power On.
     *
     * @param m CanReply
     * @return True if is a Track On notification
     */
    public static boolean isTrackOn(CanReply m) {
        return m.getOpCode() == CbusConstants.CBUS_TON;
    }

    /**
     * Tests if CanReply is a System Reset
     *
     * @param m CanReply
     * @return True if emergency Stop
     */
    public static boolean isArst(CanReply m) {
        return m.getOpCode() == CbusConstants.CBUS_ARST;
    }

    /**
     * CBUS programmer commands
     * @param cv CV to read
     * @param mode Programming Mode
     * @param header CAN ID
     * @return CanMessage ready to send
     */
    static public CanMessage getReadCV(int cv, ProgrammingMode mode, int header) {
        CanMessage m = new CanMessage(5, header);
        m.setElement(0, CbusConstants.CBUS_QCVS);
        m.setElement(1, CbusConstants.SERVICE_HANDLE);
        m.setElement(2, (cv / 256) & 0xff);
        m.setElement(3, cv & 0xff);
        if (mode.equals(ProgrammingMode.PAGEMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_PAGED);
        } else if (mode.equals(ProgrammingMode.DIRECTBITMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BIT);
        } else if (mode.equals(ProgrammingMode.DIRECTBYTEMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BYTE);
        } else {
            m.setElement(4, CbusConstants.CBUS_PROG_REGISTER);
        }
        setPri(m, 0xb);
        return m;
    }

    /**
     * CBUS programmer commands
     * 
     * CBUS VCVS works like a QCVS read but the programmer will first check if
     * the CV contents are equal to the startVal. This can speed up CV reads by
     * skipping reading of other values.
     * 
     * @param cv CV to read
     * @param mode Programming Mode
     * @param startVal Hint of current CV value
     * @param header CAN ID
     * @return CanMessage ready to send
     */
    static public CanMessage getVerifyCV(int cv, ProgrammingMode mode, int startVal, int header) {
        CanMessage m = new CanMessage(6, header);
        m.setElement(0, CbusConstants.CBUS_VCVS);
        m.setElement(1, CbusConstants.SERVICE_HANDLE);
        m.setElement(2, (cv / 256) & 0xff);
        m.setElement(3, cv & 0xff);
        if (mode.equals(ProgrammingMode.PAGEMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_PAGED);
        } else if (mode.equals(ProgrammingMode.DIRECTBITMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BIT);
        } else if (mode.equals(ProgrammingMode.DIRECTBYTEMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BYTE);
        } else {
            m.setElement(4, CbusConstants.CBUS_PROG_REGISTER);
        }
        m.setElement(5, startVal & 0xff);
         setPri(m, 0xb);
        return m;
    }

    /**
     * Get a CanMessage to write a CV.
     * @param cv Which CV, 0-65534
     * @param val New CV value, 0-255
     * @param mode Programming Mode
     * @param header CAN ID
     * @return ready to send CanMessage
     */
    static public CanMessage getWriteCV(int cv, int val, ProgrammingMode mode, int header) {
        CanMessage m = new CanMessage(6, header);
        m.setElement(0, CbusConstants.CBUS_WCVS);
        m.setElement(1, CbusConstants.SERVICE_HANDLE);
        m.setElement(2, (cv / 256) & 0xff);
        m.setElement(3, cv & 0xff);
        if (mode.equals(ProgrammingMode.PAGEMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_PAGED);
        } else if (mode.equals(ProgrammingMode.DIRECTBITMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BIT);
        } else if (mode.equals(ProgrammingMode.DIRECTBYTEMODE)) {
            m.setElement(4, CbusConstants.CBUS_PROG_DIRECT_BYTE);
        } else {
            m.setElement(4, CbusConstants.CBUS_PROG_REGISTER);
        }
        m.setElement(5, val);
        setPri(m, 0xb);
        return m;
    }

    /**
     * CBUS Ops mode programmer commands
     * @param mAddress Loco Address, non-DCC format
     * @param mLongAddr If Loco Address is a long address
     * @param header CAN ID
     * @param val New CV value
     * @param cv Which CV, 0-65534
     * @return ready to send CanMessage
     */
    static public CanMessage getOpsModeWriteCV(int mAddress, boolean mLongAddr, int cv, int val, int header) {
        CanMessage m = new CanMessage(7, header);
        int address = mAddress;
        m.setElement(0, CbusConstants.CBUS_WCVOA);
        if (mLongAddr) {
            address = address | 0xc000;
        }
        m.setElement(1, address / 256);
        m.setElement(2, address & 0xff);
        m.setElement(3, (cv / 256) & 0xff);
        m.setElement(4, cv & 0xff);
        m.setElement(5, CbusConstants.CBUS_OPS_BYTE);
        m.setElement(6, val);
        setPri(m, 0xb);
        return m;
    }

    /**
     * Get a CanMessage to send track power on
     *
     * @param header for connection CAN ID
     * @return the CanMessage to send to request track power on
     */
    static public CanMessage getRequestTrackOn(int header) {
        CanMessage m = new CanMessage(1, header);
        m.setElement(0, CbusConstants.CBUS_RTON);
        setPri(m, 0xb);
        return m;
    }

    /**
     * Get a CanMessage to send track power off
     *
     * @param header for connection CAN ID
     * @return the CanMessage to send to request track power off
     */
    static public CanMessage getRequestTrackOff(int header) {
        CanMessage m = new CanMessage(1, header);
        m.setElement(0, CbusConstants.CBUS_RTOF);
        setPri(m, 0xb);
        return m;
    }


    // CBUS bootloader commands
    
    /**
     * This is a strict CBUS message to put a node into boot mode.
     * @param nn Node Number 1-65534
     * @param header CAN ID
     * @return ready to send CanMessage
     */
    static public CanMessage getBootEntry(int nn, int header) {
        CanMessage m = new CanMessage(3, header);
        m.setElement(0, CbusConstants.CBUS_BOOTM);
        m.setElement(1, (nn / 256) & 0xFF);
        m.setElement(2, nn & 0xFF);
        setPri(m, 0xb);
        return m;
    }

    /**
     * Microchip AN247 format NOP message to set address.
     * <p>
     * The CBUS bootloader uses extended ID frames
     * 
     * @param a address
     * @param header CAN ID - overridden by call to setHeader
     * @return ready to send CanMessage
     */
    static public CanMessage getBootNop(int a, int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x4);
        m.setElement(0, a & 0xFF);
        m.setElement(1, (a / 256) & 0xFF);
        m.setElement(2, (a / 65536) & 0xFF);
        m.setElement(3, 0);
        m.setElement(4, 0x0D);
        m.setElement(5, CbusConstants.CBUS_BOOT_NOP);
        m.setElement(6, 0);
        m.setElement(7, 0);
        return m;
    }

    /**
     * Microchip AN247 format message to reset and enter normal mode.
     * 
     * @param header CAN ID - overridden by call to setHeader
     * @return ready to send CanMessage
     */
    static public CanMessage getBootReset(int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x4);
        m.setElement(0, 0);
        m.setElement(1, 0);
        m.setElement(2, 0);
        m.setElement(3, 0);
        m.setElement(4, 0x0D);
        m.setElement(5, CbusConstants.CBUS_BOOT_RESET);
        m.setElement(6, 0);
        m.setElement(7, 0);
        return m;
    }

    /**
     * Microchip AN247 format message to initialise the bootloader and set the
     * start address.
     * 
     * @param a start address
     * @param header CAN ID - overridden by call to setHeader
     * @return ready to send CanMessage
     */
    static public CanMessage getBootInitialise(int a, int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x4);
        m.setElement(0, a & 0xFF);
        m.setElement(1, (a / 256) & 0xFF);
        m.setElement(2, (a / 65536) & 0xFF);
        m.setElement(3, 0);
        m.setElement(4, 0x0D);
        m.setElement(5, CbusConstants.CBUS_BOOT_INIT);
        m.setElement(6, 0);
        m.setElement(7, 0);
        return m;
    }

    /**
     * Microchip AN247 format message to send the checksum for comparison.
     * 
     * At time of writing [6th Feb '20] The MERG bootloader doc is incorrect and
     * shows the checksum as being byte swapped.
     * 
     * @param c 0-65535 2's complement of sum of all program bytes sent
     * @param header CAN ID - overridden by call to setHeader
     * @return ready to send CanMessage
     */
    static public CanMessage getBootCheck(int c, int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x4);
        m.setElement(0, 0);
        m.setElement(1, 0);
        m.setElement(2, 0);
        m.setElement(3, 0);
        m.setElement(4, 0x0D);
        m.setElement(5, CbusConstants.CBUS_BOOT_CHECK);
        m.setElement(6, c & 0xff);
        m.setElement(7, (c >> 8) & 0xff);
        return m;
    }

    /**
     * Microchip AN247 format message to check if a module is in boot mode.
     * 
     * @param header CAN ID - overridden by call to setHeader
     * @return ready to send CanMessage
     */
    static public CanMessage getBootTest(int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x4);
        m.setElement(0, 0);
        m.setElement(1, 0);
        m.setElement(2, 0);
        m.setElement(3, 0);
        m.setElement(4, 0x0D);
        m.setElement(5, CbusConstants.CBUS_BOOT_TEST);
        m.setElement(6, 0);
        m.setElement(7, 0);
        return m;
    }

    /**
     * Microchip AN247 format message to write 8 bytes of data
     * 
     * @param d data array, 8 length, values 0-255
     * @param header CAN ID - overridden by call to setHeader
     * @return ready to send CanMessage
     */
    static public CanMessage getBootWriteData(int[] d, int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x5);
        try {
            m.setElement(0, d[0] & 0xff);
            m.setElement(1, d[1] & 0xff);
            m.setElement(2, d[2] & 0xff);
            m.setElement(3, d[3] & 0xff);
            m.setElement(4, d[4] & 0xff);
            m.setElement(5, d[5] & 0xff);
            m.setElement(6, d[6] & 0xff);
            m.setElement(7, d[7] & 0xff);
        } catch (Exception e) {
            log.error("Exception in bootloader data {}", e);
        }
        return m;
    }

    /**
     * Microchip AN247 format message to write 8 bytes of data
     * 
     * @param d data array, 8 length, values 0-255
     * @param header CAN ID - overridden by call to setHeader
     * @return ready to send CanMessage
     */
    static public CanMessage getBootWriteData(byte[] d, int header) {
        CanMessage m = new CanMessage(8, header);
        m.setExtended(true);
        m.setHeader(0x5);
        try {
            m.setElement(0, d[0] & 0xff);
            m.setElement(1, d[1] & 0xff);
            m.setElement(2, d[2] & 0xff);
            m.setElement(3, d[3] & 0xff);
            m.setElement(4, d[4] & 0xff);
            m.setElement(5, d[5] & 0xff);
            m.setElement(6, d[6] & 0xff);
            m.setElement(7, d[7] & 0xff);
        } catch (Exception e) {
            log.error("Exception in bootloader data {}", e);
        }
        return m;
    }

    /**
     * Tests if a message is a bootloader data write
     * 
     * @param m message
     * @return true if the message is a bootloader data write
     */
    public static boolean isBootWriteData(CanMessage m) {
        if (m.isExtended() && (m.getHeader() == 0x5)) {
            return (true);
        } 
        return (false);
    }

    /**
     * Tests if incoming CanReply is a Boot Error.
     *
     * @param r CanReply
     * @return True if is a Boot Error
     */
    public static boolean isBootError(CanReply r) {
        if (r.isExtended() && (r.getHeader() == 0x10000004) && (r.getElement(0) == CbusConstants.CBUS_EXT_BOOT_ERROR)) {
            return (true);
        } 
        return (false);
    }

    /**
     * Tests if incoming CanReply is a Boot OK.
     *
     * @param r CanReply
     * @return True if is a Boot OK
     */
    public static boolean isBootOK(CanReply r) {
        if (r.isExtended() && (r.getHeader() == 0x10000004) && (r.getElement(0) == CbusConstants.CBUS_EXT_BOOT_OK)) {
            return (true);
        }
        return (false);
    }
    
    /**
     * Tests if incoming CanReply is a Boot Confirm.
     *
     * @param r CanReply
     * @return True if is a Boot Confirm
     */
    public static boolean isBootConfirm(CanReply r) {
        if (r.isExtended() && (r.getHeader() == 0x10000004) && (r.getElement(0) == CbusConstants.CBUS_EXT_BOOTC)) {
            return (true);
        }
        return (false);
    }

    private final static Logger log = LoggerFactory.getLogger(CbusMessage.class);
}
