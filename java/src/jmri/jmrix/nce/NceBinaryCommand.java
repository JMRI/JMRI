package jmri.jmrix.nce;

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
 * Constants used in NCE Binary command messages.
 *
 * @author Daniel Boudreau (C) 2007, 2010
 * @author ken cameron (C) 2013
 * @see jmri.jmrix.nce.NceMessageUtil
 */
public class NceBinaryCommand {
    public static final int NOOP_CMD = 0x80;            // NCE No Op Command, NCE-USB yes
    public static final int ASSIGN_CAB_CMD = 0x81;      // NCE Assign loco to cab command, NCE-USB no
    public static final int READ_CLOCK_CMD = 0x82;      // NCE read clock command, NCE-USB no
    public static final int STOP_CLOCK_CMD = 0x83;      // NCE stop clock command, NCE-USB no
    public static final int START_CLOCK_CMD = 0x84;     // NCE start clock command, NCE-USB no
    public static final int SET_CLOCK_CMD = 0x85;       // NCE set clock command, NCE-USB no
    public static final int CLOCK_1224_CMD = 0x86;      // NCE change clock 12/24 command, NCE-USB no
    public static final int CLOCK_RATIO_CMD = 0x87;     // NCE set clock ratio command, NCE-USB no
    public static final int DEQUEUE_CMD = 0x88;         // NCE dequeue packets based on loco addr, NCE-USB no
    public static final int ENABLE_TRACK_CMD = 0x89;    // NCE enable track/kill programm track, NCE-USB no
    public static final int READ_AUI4_CMD = 0x8A;       // NCE read status of AUI yy, returns four bytes, NCE-USB no
    public static final int DISABLE_TRACK_CMD = 0x89;   // NCE enable program/kill main track, NCE-USB no
    public static final int DUMMY_CMD = 0x8C;           // NCE Dummy instruction, NCE-USB yes
    public static final int SPEED_MODE_CMD = 0x8D;      // NCE set speed mode, NCE-USB no
    public static final int WRITE_N_CMD = 0x8E;         // NCE write up to 16 bytes of memory command, NCE-USB no
    /**
     * @deprecated since 4.7.2; use {@link #WRITE_N_CMD} instead
     */
    @Deprecated
    public static final int WRITEn_CMD = WRITE_N_CMD;
    public static final int READ16_CMD = 0x8F;          // NCE read 16 bytes of memory command, NCE-USB no
    public static final int DISPLAY3_CMD = 0x90;        // NCE write 16 char to cab display line 3, NCE-USB no
    public static final int DISPLAY4_CMD = 0x91;        // NCE write 16 char to cab display line 4, NCE-USB no
    public static final int DISPLAY2_CMD = 0x92;        // NCE write 8 char to cab display line 2 right, NCE-USB no
    public static final int QUEUE3_TMP_CMD = 0x93;      // NCE queue 3 bytes to temp queue, NCE-USB no
    public static final int QUEUE4_TMP_CMD = 0x94;      // NCE queue 4 bytes to temp queue, NCE-USB no
    public static final int QUEUE5_TMP_CMD = 0x95;      // NCE queue 5 bytes to temp queue, NCE-USB no
    public static final int QUEUE6_TMP_CMD = 0x96;      // NCE queue 6 bytes to temp queue, NCE-USB no
    public static final int WRITE1_CMD = 0x97;          // NCE write 1 bytes of memory command, NCE-USB no
    public static final int WRITE2_CMD = 0x98;          // NCE write 2 bytes of memory command, NCE-USB no
    public static final int WRITE4_CMD = 0x99;          // NCE write 4 bytes of memory command, NCE-USB no
    public static final int WRITE8_CMD = 0x9A;          // NCE write 8 bytes of memory command, NCE-USB no
    public static final int READ_AUI2_CMD = 0x9B;       // NCE read status of AUI yy, returns two bytes, NCE-USB >= 1.65
    public static final int MACRO_CMD = 0x9C;           // NCE execute macro n, NCE-USB yes
    public static final int READ1_CMD = 0x9D;           // NCE read 1 byte of memory command, NCE-USB no
    public static final int PGM_TRK_ON_CMD = 0x9E;      // NCE enter program track  command, NCE-USB yes
    public static final int PGM_TRK_OFF_CMD = 0x9F;     // NCE exit program track  command, NCE-USB yes
    public static final int PGM_PAGE_WRITE_CMD = 0xA0;  // NCE program track, page mode write command, NCE-USB yes
    public static final int PGM_PAGE_READ_CMD = 0xA1;   // NCE program track, page mode read command, NCE-USB yes
    public static final int LOCO_CMD = 0xA2;            // NCE loco control command, NCE-USB yes
    public static final int QUEUE3_TRK_CMD = 0xA3;      // NCE queue 3 bytes to track queue, NCE-USB no
    public static final int QUEUE4_TRK_CMD = 0xA4;      // NCE queue 4 bytes to track queue, NCE-USB no
    public static final int QUEUE5_TRK_CMD = 0xA5;      // NCE queue 5 bytes to track queue, NCE-USB no
    public static final int PGM_REG_WRITE_CMD = 0xA6;   // NCE program track, register mode write command, NCE-USB yes
    public static final int PGM_REG_READ_CMD = 0xA7;    // NCE program track, register mode read command, NCE-USB yes
    public static final int PGM_DIR_WRITE_CMD = 0xA8;   // NCE program track, direct mode write command, NCE-USB yes
    public static final int PGM_DIR_READ_CMD = 0xA9;    // NCE program track, direct mode read command, NCE-USB yes
    public static final int SW_REV_CMD = 0xAA;          // NCE get EPROM revision cmd, Reply Format: VV.MM.mm, NCE-USB yes
    public static final int RESET_SOFT_CMD = 0xAB;      // NCE soft reset command, NCE-USB no
    public static final int RESET_HARD_CMD = 0xAC;      // NCE hard reset command, NCE-USB no
    public static final int ACC_CMD = 0xAD;             // NCE accessory command, NCE-USB yes
    public static final int OPS_PROG_LOCO_CMD = 0xAE;   // NCE ops mode program loco, NCE-USB yes
    public static final int OPS_PROG_ACCY_CMD = 0xAF;   // NCE ops mode program accessories, NCE-USB yes
    public static final int FACTORY_TEST_CMD = 0xB0;    // NCE factory test, NCE-USB yes
    public static final int USB_SET_CAB_CMD = 0xB1;     // NCE set cab address in USB, NCE-USB yes
    public static final int USB_MEM_POINTER_CMD = 0xB3; // NCE set memory context pointer, NCE-USB >= 1.65
    public static final int USB_MEM_WRITE_CMD = 0xB4;   // NCE write memory, NCE-USB >= 1.65
    public static final int USB_MEM_READ_CMD = 0xB5;    // NCE read memory, NCE-USB >= 1.65

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

    /**
     *
     * @param number the value of number
     * @param closed the value of closed
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#accDecoder}
     */
    @Deprecated
    public static byte[] accDecoder(int number, boolean closed) {
        return NceMessageUtil.accDecoder(number, closed);
    }

    /**
     *
     * @param address the value of address
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#accMemoryRead}
     */
    @Deprecated
    public static byte[] accMemoryRead(int address) {
        return NceMessageUtil.accMemoryRead(address);

    }

    /**
     * Read one byte from NCE command station memory
     *
     * @param address address to read from
     * @return binary command to read one byte
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#accMemoryRead1}
     */
    @Deprecated
    public static byte[] accMemoryRead1(int address) {
        return NceMessageUtil.accMemoryRead1(address);

    }

    /**
     *
     * @param address the value of address
     * @param num     the value of num
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#accMemoryWriteN}
     */
    @Deprecated
    public static byte[] accMemoryWriteN(int address, int num) {
        return NceMessageUtil.accMemoryWriteN(address, num);

    }

    /**
     *
     * @param address the value of address
     * @deprecated since 4.7.3; moved to
     * @return command as a byte sequence
     *         {@link jmri.jmrix.nce.NceMessageUtil#accMemoryWrite8}
     */
    @Deprecated
    public static byte[] accMemoryWrite8(int address) {
        return NceMessageUtil.accMemoryWrite8(address);

    }

    /**
     *
     * @param address the value of address
     * @deprecated since 4.7.3; moved to
     * @return command as a byte sequence
     *         {@link jmri.jmrix.nce.NceMessageUtil#accMemoryWrite4}
     */
    @Deprecated
    public static byte[] accMemoryWrite4(int address) {
        return NceMessageUtil.accMemoryWrite4(address);
    }

    /**
     *
     * @param address the value of address
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#accMemoryWrite2}
     */
    @Deprecated
    public static byte[] accMemoryWrite2(int address) {
        return NceMessageUtil.accMemoryWrite2(address);
    }

    /**
     *
     * @param address the value of address
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#accMemoryWrite1}
     */
    @Deprecated
    public static byte[] accMemoryWrite1(int address) {
        return NceMessageUtil.accMemoryWrite1(address);
    }

    /**
     *
     * @param cabId the value of cabId
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#accAiu2Read}
     */
    @Deprecated
    public static byte[] accAiu2Read(int cabId) {
        return NceMessageUtil.accAiu2Read(cabId);
    }

    /**
     *
     * @param cab the value of cab
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#usbSetCabId}
     */
    @Deprecated
    public static byte[] usbSetCabId(int cab) {
        return NceMessageUtil.usbSetCabId(cab);
    }

    /**
     *
     * @param data the value of data
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#usbMemoryWrite1}
     */
    @Deprecated
    public static byte[] usbMemoryWrite1(byte data) {
        return NceMessageUtil.usbMemoryWrite1(data);
    }

    /**
     *
     * @param num the value of num
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#usbMemoryRead}
     */
    @Deprecated
    public static byte[] usbMemoryRead(int num) {
        return NceMessageUtil.usbMemoryRead(num);
    }

    /**
     *
     * @param cab the value of cab
     * @param loc the value of loc
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#usbMemoryPointer}
     */
    @Deprecated
    public static byte[] usbMemoryPointer(int cab, int loc) {
        return NceMessageUtil.usbMemoryPointer(cab, loc);
    }

    /**
     *
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#accStopClock}
     */
    @Deprecated
    public static byte[] accStopClock() {
        return NceMessageUtil.accStopClock();
    }

    /**
     *
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#accStartClock}
     */
    @Deprecated
    public static byte[] accStartClock() {
        return NceMessageUtil.accStartClock();
    }

    /**
     *
     * @param hours   the value of hours
     * @param minutes the value of minutes
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#accSetClock}
     */
    @Deprecated
    public static byte[] accSetClock(int hours, int minutes) {
        return NceMessageUtil.accSetClock(hours, minutes);
    }

    /**
     *
     * @param flag the value of flag
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#accSetClock1224}
     */
    @Deprecated
    public static byte[] accSetClock1224(boolean flag) {
        return NceMessageUtil.accSetClock1224(flag);
    }

    /**
     *
     * @param ratio the value of ratio
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#accSetClockRatio}
     */
    @Deprecated
    public static byte[] accSetClockRatio(int ratio) {
        return NceMessageUtil.accSetClockRatio(ratio);
    }

    /**
     *
     * @param locoAddr   the value of locoAddr
     * @param locoSubCmd the value of locoSubCmd
     * @param locoData   the value of locoData
     * @return command as a byte sequence
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#nceLocoCmd}
     */
    @Deprecated
    public static byte[] nceLocoCmd(int locoAddr, byte locoSubCmd, byte locoData) {
        return NceMessageUtil.nceLocoCmd(locoAddr, locoSubCmd, locoData);
    }

    /**
     * Create NCE EPROM revision message. The reply format is:
     * {@literal VV.MM.mm}
     *
     * @return the revision message
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#getNceEpromRev}
     */
    @Deprecated
    public static byte[] getNceEpromRev() {
        return NceMessageUtil.getNceEpromRev();
    }

    /**
     * Create a NCE USB compatible ops mode loco message.
     *
     * @param tc       traffic controller; ignored
     * @param locoAddr locomotive address
     * @param cvAddr   CV to set
     * @param cvData   value to set CV to
     * @return ops mode message
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#usbOpsModeLoco}
     */
    @Deprecated
    public static byte[] usbOpsModeLoco(NceTrafficController tc, int locoAddr, int cvAddr, int cvData) {
        return NceMessageUtil.usbOpsModeLoco(tc, locoAddr, cvAddr, cvData);
    }

    /**
     * Create a NCE USB compatible ops mode accessory message.
     *
     * @param accyAddr locomotive address
     * @param cvAddr   CV to set
     * @param cvData   value to set CV to
     * @return ops mode message
     * @deprecated since 4.7.3; moved to
     * {@link jmri.jmrix.nce.NceMessageUtil#usbOpsModeAccy}
     */
    @Deprecated
    public static byte[] usbOpsModeAccy(int accyAddr, int cvAddr, int cvData) {
        return NceMessageUtil.usbOpsModeAccy(accyAddr, cvAddr, cvData);
    }
}
