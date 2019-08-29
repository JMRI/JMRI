package jmri.jmrix.nce;

import jmri.NmraPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 
 From NCE System notes for version March 1, 2007
 
 New 0xAD command sends accessory or signal packets.
 This command can also issue NCE macros
 Command Format: 0xAD <addr_h> <addr_l> <op_1> <data_1>
 Addr_h and Addr_l are the accessory/signal address as a
 normal binary number (NOT in DCC format).
 Ex: Accessory Address 1 = 0x00 0x01 (hi byte first)
 Ex: Accessory Address 2 = 0x00 0x02 (hi byte first)
 Ex: Accessory Address 513 = 0x02 0x01 (hi byte first)
 NOTE: accy/signal address 0 is not a valid address
 
 Op_1   Data_1       Operation description
 01     0-255        NCE macro number 0-255
 02     0-255        Duplicate of Op_1 command 01
 03     0            Accessory Normal direction (ON)
 04     0            Accessory Reverse direction (OFF)
 05     0-1f         Signal Aspect 0-31
 06-7f               reserved reserved
 
 Returns: ! = success
 1 = bad accy/signal address
 
 0xA2 sends speed or function packets to a locomotive.
 
 Command Format: 0xA2 <addr_h> <addr_l> <op_1> <data_1>
 Addr_h and Addr_l are the loco address in DCC format.
 If a long address is in use, bits 6 and 7 of the high byte are set.
 Example: Long address 3 = 0xc0 0x03
 Short address 3 = 0x00 0x03
  
 Op_1   Data_1       Operation description
 01     0-7f         Reverse 28 speed command
 02     0-7f         Forward 28 speed command
 03     0-7f         Reverse 128 speed command
 04     0-7f         Forward 128 speed command
 05     0            Estop reverse command
 06     0            Estop forward command
 07     0-1f         Function group 1 (same format as DCC packet for FG1
 08     0-0f         Function group 2 (same format as DCC packet for FG2
 09     0-0f         Function group 3 (same format as DCC packet for FG3
 0a     0-7f         Set reverse consist address for lead loco
 0b     0-7f         Set forward consist address for lead loco
 0c     0-7f         Set reverse consist address for rear loco
 0d     0-7f         Set forward consist address for rear loco
 0e     0-7f         Set reverse consist address for additional loco
 0f     0-7f         Set forward consist address for additional loco
 10     0            Del loco from consist
 11     0            Kill consist
 12     0-9          Set momentum
 13     0-7f         No action, always returns success
 14     0-7f         No action, always returns success
 15     0-ff         Functions 13-20 control (bit 0=F13, bit 7=F20)
 16     0-ff         Functions 21-28 control (bit 0=F21, bit 7=F28)
 17     0-3f         Assign this loco to cab number in data_1
 18-7f               reserved reserved
  
 Returns: ! = success
 1 = bad loco address
   
 */
/**
 * NCE Binary Commands
 *
 * Also see NceMessage.java for additional commands
 *
 * @author Daniel Boudreau (C) 2007, 2010
 * @author ken cameron (C) 2013
 */
public class NceBinaryCommand {

// all commands moved to NceMessage 07/17/2018 DAB
//    public static final int NOP_CMD = 0x80;            // NCE No Op Command, NCE-USB yes
//    public static final int ASSIGN_CAB_CMD = 0x81;      // NCE Assign loco to cab command, NCE-USB no
//    public static final int READ_CLOCK_CMD = 0x82;      // NCE read clock command, NCE-USB no
//    public static final int STOP_CLOCK_CMD = 0x83;      // NCE stop clock command, NCE-USB no
//    public static final int START_CLOCK_CMD = 0x84;     // NCE start clock command, NCE-USB no
//    public static final int SET_CLOCK_CMD = 0x85;       // NCE set clock command, NCE-USB no
//    public static final int CLOCK_1224_CMD = 0x86;      // NCE change clock 12/24 command, NCE-USB no
//    public static final int CLOCK_RATIO_CMD = 0x87;     // NCE set clock ratio command, NCE-USB no
//    public static final int DEQUEUE_CMD = 0x88;         // NCE dequeue packets based on loco addr, NCE-USB no
//    public static final int ENABLE_TRACK_CMD = 0x89;    // NCE enable track/kill programm track, NCE-USB no
//    public static final int READ_AUI4_CMD = 0x8A;       // NCE read status of AUI yy, returns four bytes, NCE-USB no
//    public static final int DISABLE_TRACK_CMD = 0x89;   // NCE enable program/kill main track, NCE-USB no
//    public static final int DUMMY_CMD = 0x8C;           // NCE Dummy instruction, NCE-USB yes
//    public static final int SPEED_MODE_CMD = 0x8D;      // NCE set speed mode, NCE-USB no
//    public static final int WRITE_N_CMD = 0x8E;         // NCE write up to 16 bytes of memory command, NCE-USB no
//    public static final int READ16_CMD = 0x8F;          // NCE read 16 bytes of memory command, NCE-USB no
//    public static final int DISPLAY3_CMD = 0x90;        // NCE write 16 char to cab display line 3, NCE-USB no
//    public static final int DISPLAY4_CMD = 0x91;        // NCE write 16 char to cab display line 4, NCE-USB no
//    public static final int DISPLAY2_CMD = 0x92;        // NCE write 8 char to cab display line 2 right, NCE-USB no
//    public static final int QUEUE3_TMP_CMD = 0x93;      // NCE queue 3 bytes to temp queue, NCE-USB no
//    public static final int QUEUE4_TMP_CMD = 0x94;      // NCE queue 4 bytes to temp queue, NCE-USB no
//    public static final int QUEUE5_TMP_CMD = 0x95;      // NCE queue 5 bytes to temp queue, NCE-USB no
//    public static final int QUEUE6_TMP_CMD = 0x96;      // NCE queue 6 bytes to temp queue, NCE-USB no
//    public static final int WRITE1_CMD = 0x97;          // NCE write 1 bytes of memory command, NCE-USB no
//    public static final int WRITE2_CMD = 0x98;          // NCE write 2 bytes of memory command, NCE-USB no
//    public static final int WRITE4_CMD = 0x99;          // NCE write 4 bytes of memory command, NCE-USB no
//    public static final int WRITE8_CMD = 0x9A;          // NCE write 8 bytes of memory command, NCE-USB no
//    public static final int READ_AUI2_CMD = 0x9B;       // NCE read status of AUI yy, returns two bytes, NCE-USB >= 1.65
//    public static final int MACRO_CMD = 0x9C;           // NCE execute macro n, NCE-USB yes
//    public static final int READ1_CMD = 0x9D;           // NCE read 1 byte of memory command, NCE-USB no
//    public static final int PGM_TRK_ON_CMD = 0x9E;      // NCE enter program track  command, NCE-USB yes
//    public static final int PGM_TRK_OFF_CMD = 0x9F;     // NCE exit program track  command, NCE-USB yes
//    public static final int PGM_PAGE_WRITE_CMD = 0xA0;  // NCE program track, page mode write command, NCE-USB yes
//    public static final int PGM_PAGE_READ_CMD = 0xA1;   // NCE program track, page mode read command, NCE-USB yes
//    public static final int LOCO_CMD = 0xA2;            // NCE loco control command, NCE-USB yes
//    public static final int QUEUE3_TRK_CMD = 0xA3;      // NCE queue 3 bytes to track queue, NCE-USB no
//    public static final int QUEUE4_TRK_CMD = 0xA4;      // NCE queue 4 bytes to track queue, NCE-USB no
//    public static final int QUEUE5_TRK_CMD = 0xA5;      // NCE queue 5 bytes to track queue, NCE-USB no
//    public static final int PGM_REG_WRITE_CMD = 0xA6;   // NCE program track, register mode write command, NCE-USB yes
//    public static final int PGM_REG_READ_CMD = 0xA7;    // NCE program track, register mode read command, NCE-USB yes
//    public static final int PGM_DIR_WRITE_CMD = 0xA8;   // NCE program track, direct mode write command, NCE-USB yes
//    public static final int PGM_DIR_READ_CMD = 0xA9;    // NCE program track, direct mode read command, NCE-USB yes
//    public static final int SW_REV_CMD = 0xAA;          // NCE get EPROM revision cmd, Reply Format: VV.MM.mm, NCE-USB yes
//    public static final int RESET_SOFT_CMD = 0xAB;      // NCE soft reset command, NCE-USB no
//    public static final int RESET_HARD_CMD = 0xAC;      // NCE hard reset command, NCE-USB no
//    public static final int ACC_CMD = 0xAD;             // NCE accessory command, NCE-USB yes
//    public static final int OPS_PROG_LOCO_CMD = 0xAE;   // NCE ops mode program loco, NCE-USB yes
//    public static final int OPS_PROG_ACCY_CMD = 0xAF;   // NCE ops mode program accessories, NCE-USB yes
//    public static final int FACTORY_TEST_CMD = 0xB0;    // NCE factory test, NCE-USB yes
//    public static final int USB_SET_CAB_CMD = 0xB1;     // NCE set cab address in USB, NCE-USB yes
//    public static final int USB_MEM_POINTER_CMD = 0xB3; // NCE set memory context pointer, NCE-USB >= 1.65
//    public static final int USB_MEM_WRITE_CMD = 0xB4;   // NCE write memory, NCE-USB >= 1.65
//    public static final int USB_MEM_READ_CMD = 0xB5;    // NCE read memory, NCE-USB >= 1.65

    // NCE Command 0xA2 sends speed or function packets to a locomotive
    // 0xA2 sub commands speed and functions
    public static final byte LOCO_CMD_REV_28SPEED = 0x01;  // set loco speed 28 steps reverse
    public static final byte LOCO_CMD_FWD_28SPEED = 0x02;  // set loco speed 28 steps forward
    public static final byte LOCO_CMD_REV_128SPEED = 0x03; // set loco speed 128 steps reverse
    public static final byte LOCO_CMD_FWD_128SPEED = 0x04; // set loco speed 128 steps forward
    public static final byte LOCO_CMD_REV_ESTOP = 0x05;    // emergency stop reverse
    public static final byte LOCO_CMD_FWD_ESTOP = 0x06;    // emergency stop forward
    public static final byte LOCO_CMD_FG1 = 0x07;          // function group 1
    public static final byte LOCO_CMD_FG2 = 0x08;          // function group 2
    public static final byte LOCO_CMD_FG3 = 0x09;          // function group 3
    public static final byte LOCO_CMD_FG4 = 0x15;          // function group 4
    public static final byte LOCO_CMD_FG5 = 0x16;          // function group 5

    // OxA2 sub commands consist
    public static final byte LOCO_CMD_REV_CONSIST_LEAD = 0x0A;    // reverse consist address for lead loco
    public static final byte LOCO_CMD_FWD_CONSIST_LEAD = 0x0B;    // forward consist address for lead loco 
    public static final byte LOCO_CMD_REV_CONSIST_REAR = 0x0C;    // reverse consist address for rear loco 
    public static final byte LOCO_CMD_FWD_CONSIST_REAR = 0x0D;    // forward consist address for rear loco
    public static final byte LOCO_CMD_REV_CONSIST_MID = 0x0E;     // reverse consist address for additional loco 
    public static final byte LOCO_CMD_FWD_CONSIST_MID = 0x0F;     // forward consist address for additional loco 
    public static final byte LOCO_CMD_DELETE_LOCO_CONSIST = 0x10; // Delete loco from consist
    public static final byte LOCO_CMD_KILL_CONSIST = 0x11;        // Kill consist

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
        justification = "Long-standing API, risky to update")
    public static byte[] accDecoder(int number, boolean closed) {

        if (number < NmraPacket.accIdLowLimit || number > NmraPacket.accIdAltHighLimit) {
            log.error("invalid NCE accessory address " + number);
            return null;
        }
        byte op_1;
        if (closed) {
            op_1 = 0x03;
        } else {
            op_1 = 0x04;
        }

        int addr_h = number / 256;
        int addr_l = number & 0xFF;

        byte[] retVal = new byte[5];
        retVal[0] = (byte) (NceMessage.SEND_ACC_SIG_MACRO_CMD); // NCE accessory command
        retVal[1] = (byte) (addr_h);  // high address
        retVal[2] = (byte) (addr_l);  // low address
        retVal[3] = op_1;             // command
        retVal[4] = (byte) 0;         // zero out last byte for acc

        return retVal;
    }

    public static byte[] accMemoryRead(int address) {

        int addr_h = address / 256;
        int addr_l = address & 0xFF;

        byte[] retVal = new byte[3];
        retVal[0] = (byte) (NceMessage.READ16_CMD); // read 16 bytes command
        retVal[1] = (byte) (addr_h);     // high address
        retVal[2] = (byte) (addr_l);     // low address

        return retVal;
    }

    /**
     * Read one byte from NCE command station memory
     *
     * @param address address to read from
     * @return binary command to read one byte
     */
    public static byte[] accMemoryRead1(int address) {

        int addr_h = address / 256;
        int addr_l = address & 0xFF;

        byte[] retVal = new byte[3];
        retVal[0] = (byte) (NceMessage.READ1_CMD); // read 1 byte command
        retVal[1] = (byte) (addr_h);    // high address
        retVal[2] = (byte) (addr_l);    // low address

        return retVal;
    }

    private final static int BUFFER_SIZE_16 = 16;
    public static byte[] accMemoryWriteN(int address, byte[] data) {

        int addr_h = address / 256;
        int addr_l = address & 0xFF;

        byte[] retVal = new byte[4 + BUFFER_SIZE_16];
        int j = 0;
        retVal[j++] = (byte) (NceMessage.WRITE_N_CMD); // write n bytes command
        retVal[j++] = (byte) (addr_h);      // high address
        retVal[j++] = (byte) (addr_l);      // low address
        retVal[j++] = (byte) data.length;   // number of bytes to write
        
        for (int i = 0; i < data.length; i++, j++) {
            retVal[j] = data[i];
        }

        return retVal;
    }

    private final static int BUFFER_SIZE_8 = 8;
    public static byte[] accMemoryWrite8(int address, byte[] data) {

        int addr_h = address / 256;
        int addr_l = address & 0xFF;

        byte[] retVal = new byte[3 + BUFFER_SIZE_8];
        int j = 0;
        retVal[j++] = (byte) (NceMessage.WRITE8_CMD); // write 8 bytes command
        retVal[j++] = (byte) (addr_h);     // high address
        retVal[j++] = (byte) (addr_l);     // low address
        
        for (int i = 0; i < data.length; i++, j++) {
            retVal[j] = data[i];
        }

        return retVal;
    }

    private final static int BUFFER_SIZE_4 = 4;
    public static byte[] accMemoryWrite4(int address, byte[] data) {

        int addr_h = address / 256;
        int addr_l = address & 0xFF;

        byte[] retVal = new byte[3 + BUFFER_SIZE_4];
        int j = 0;
        retVal[j++] = (byte) (NceMessage.WRITE4_CMD); // write 4 bytes command
        retVal[j++] = (byte) (addr_h);     // high address
        retVal[j++] = (byte) (addr_l);     // low address
        
        for (int i = 0; i < data.length; i++, j++) {
            retVal[j] = data[i];
        }

        return retVal;
    }

    private final static int BUFFER_SIZE_2 = 2;
    public static byte[] accMemoryWrite2(int address, byte[] data) {

        int addr_h = address / 256;
        int addr_l = address & 0xFF;

        byte[] retVal = new byte[3 + BUFFER_SIZE_2];
        int j = 0;
        retVal[j++] = (byte) (NceMessage.WRITE2_CMD); // write 4 bytes command
        retVal[j++] = (byte) (addr_h);     // high address
        retVal[j++] = (byte) (addr_l);     // low address
        
        for (int i = 0; i < data.length; i++, j++) {
            retVal[j] = data[i];
        }

        return retVal;
    }

    public static byte[] accMemoryWrite1(int address, byte data) {

        int addr_h = address / 256;
        int addr_l = address & 0xFF;

        byte[] retVal = new byte[3 + 1];
        retVal[0] = (byte) (NceMessage.WRITE1_CMD); // write 4 bytes command
        retVal[1] = (byte) (addr_h);     // high address
        retVal[2] = (byte) (addr_l);     // low address
        retVal[3] = data;

        return retVal;
    }

    public static byte[] accAiu2Read(int cabId) {

        byte[] retVal = new byte[1 + 1];
        retVal[0] = (byte) (NceMessage.READ_AUI2_CMD); // write 4 bytes command
        retVal[1] = (byte) (cabId);         // cab address

        return retVal;
    }

    public static byte[] usbSetCabId(int cab) {

        byte[] retVal = new byte[2];
        retVal[0] = (byte) (NceMessage.USB_SET_CAB_CMD); // read N bytes command
        retVal[1] = (byte) (cab);             // cab number

        return retVal;
    }

    public static byte[] usbMemoryWrite1(byte data) {

        byte[] retVal = new byte[2];
        retVal[0] = (byte) (NceMessage.USB_MEM_WRITE_CMD); // write 2 bytes command
        retVal[1] = (data);                     // data

        return retVal;
    }

    public static byte[] usbMemoryRead(int num) {

        byte[] retVal = new byte[2];
        retVal[0] = (byte) (NceMessage.USB_MEM_READ_CMD); // read N bytes command
        retVal[1] = (byte) (num);              // byte count

        return retVal;
    }

    public static byte[] usbMemoryPointer(int cab, int loc) {

        byte[] retVal = new byte[3];
        retVal[0] = (byte) (NceMessage.USB_MEM_POINTER_CMD); // read N bytes command
        retVal[1] = (byte) (cab);                 // cab number
        retVal[2] = (byte) (loc);                 // memory offset

        return retVal;
    }

    public static byte[] accStopClock() {

        byte[] retVal = new byte[1];
        retVal[0] = (byte) (NceMessage.STOP_CLOCK_CMD); // stop clock command

        return retVal;
    }

    public static byte[] accStartClock() {

        byte[] retVal = new byte[1];
        retVal[0] = (byte) (NceMessage.START_CLOCK_CMD); // start clock command

        return retVal;
    }

    public static byte[] accSetClock(int hours, int minutes) {

        byte[] retVal = new byte[3];
        retVal[0] = (byte) (NceMessage.SET_CLOCK_CMD); // set clock command
        retVal[1] = (byte) (hours);         // hours
        retVal[2] = (byte) (minutes);       // minutes

        return retVal;
    }

    public static byte[] accSetClock1224(boolean flag) {

        int bit = 0;
        if (flag) {
            bit = 1;
        }
        byte[] retVal = new byte[2];
        retVal[0] = (byte) (NceMessage.CLOCK_1224_CMD); // set clock 12/24 command
        retVal[1] = (byte) (bit);            // 12 - 0, 24 - 1

        return retVal;
    }

    public static byte[] accSetClockRatio(int ratio) {

        byte[] retVal = new byte[2];
        retVal[0] = (byte) (NceMessage.CLOCK_RATIO_CMD); // set clock command
        retVal[1] = (byte) (ratio);           // fast clock ratio

        return retVal;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
        justification = "Long-standing API, risky to update")
    public static byte[] nceLocoCmd(int locoAddr, byte locoSubCmd, byte locoData) {
        if (locoSubCmd < 1 || locoSubCmd > 0x17) {
            log.error("invalid NCE loco command " + locoSubCmd);
            return null;
        }

        int locoAddr_h = locoAddr / 256;
        int locoAddr_l = locoAddr & 0xFF;

        byte[] retVal = new byte[5];
        retVal[0] = (byte) (NceMessage.LOCO_CMD);   // NCE Loco command
        retVal[1] = (byte) (locoAddr_h); // loco high address
        retVal[2] = (byte) (locoAddr_l); // loco low address
        retVal[3] = locoSubCmd;          // sub command
        retVal[4] = locoData;            // sub data

        return retVal;
    }

    /**
     * Create NCE EPROM revision message. The reply format is:
     * {@literal VV.MM.mm}
     *
     * @return the revision message
     */
    public static byte[] getNceEpromRev() {
        byte[] retVal = new byte[1];
        retVal[0] = (byte) (NceMessage.SW_REV_CMD);
        return retVal;
    }

    /**
     * Create a NCE USB compatible ops mode loco message.
     *
     * @param tc       traffic controller; ignored
     * @param locoAddr locomotive address
     * @param cvAddr   CV to set
     * @param cvData   value to set CV to
     * @return ops mode message
     */
    public static byte[] usbOpsModeLoco(NceTrafficController tc, int locoAddr, int cvAddr, int cvData) {

        byte[] retVal = new byte[6];
        int locoAddr_h = locoAddr / 256;
        int locoAddr_l = locoAddr & 0xFF;
        int cvAddr_h = cvAddr / 256;
        int cvAddr_l = cvAddr & 0xFF;

        retVal[0] = (byte) (NceMessage.OPS_PROG_LOCO_CMD); // NCE ops mode loco command
        retVal[1] = (byte) (locoAddr_h);        // loco high address
        retVal[2] = (byte) (locoAddr_l);        // loco low address
        retVal[3] = (byte) (cvAddr_h);          // CV high address
        retVal[4] = (byte) (cvAddr_l);          // CV low address
        retVal[5] = (byte) (cvData);            // CV data

        return retVal;
    }

    /**
     * Create a NCE USB compatible ops mode accessory message.
     *
     * @param accyAddr locomotive address
     * @param cvAddr   CV to set
     * @param cvData   value to set CV to
     * @return ops mode message
     */
    public static byte[] usbOpsModeAccy(int accyAddr, int cvAddr, int cvData) {

        byte[] retVal = new byte[6];
        int accyAddr_h = accyAddr / 256;
        int accyAddr_l = accyAddr & 0xFF;
        int cvAddr_h = cvAddr / 256;
        int cvAddr_l = cvAddr & 0xFF;

        retVal[0] = (byte) (NceMessage.OPS_PROG_ACCY_CMD); // NCE ops mode accy command
        retVal[1] = (byte) (accyAddr_h);        // accy high address
        retVal[2] = (byte) (accyAddr_l);        // accy low address
        retVal[3] = (byte) (cvAddr_h);          // CV high address
        retVal[4] = (byte) (cvAddr_l);          // CV low address
        retVal[5] = (byte) (cvData);            // CV data

        return retVal;
    }

    private final static Logger log = LoggerFactory.getLogger(NceBinaryCommand.class);
}
