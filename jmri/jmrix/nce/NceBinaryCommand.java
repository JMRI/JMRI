// NceBinaryCommand.java

package jmri.jmrix.nce;

/**
 *
 * From NCE System notes for version March 1, 2007
 *
 * New 0xAD command sends accessory or signal packets.
 * This command can also issue NCE macros
 * Command Format: 0xAD <addr_h> <addr_l> <op_1> <data_1>
 * Addr_h and Addr_l are the accessory/signal address as a
 * normal binary number (NOT in DCC format).
 * Ex: Accessory Address 1 = 0x00 0x01 (hi byte first)
 * Ex: Accessory Address 2 = 0x00 0x02 (hi byte first)
 * Ex: Accessory Address 513 = 0x02 0x01 (hi byte first)
 * NOTE: accy/signal address 0 is not a valid address
 *
 * Op_1 Data_1 		Operation description
 *	01 	0-255 		NCE macro number 0-255
 *	02 	0-255 		Duplicate of Op_1 command 01
 *	03 	0 			Accessory Normal direction (ON)
 *	04 	0 			Accessory Reverse direction (OFF)
 *	05 	0-1f 		Signal Aspect 0-31
 *	06-7f 			reserved reserved
 *
 *	Returns: ! = success
 *	1 = bad accy/signal address
 *
 * 0xA2 sends speed or function packets to a locomotive.
 *
 * Command Format: 0xA2 <addr_h> <addr_l> <op_1> <data_1>
 * Addr_h and Addr_l are the loco address in DCC format.
 * If a long address is in use, bits 6 and 7 of the high byte are set.
 * Example: Long address 3 = 0xc0 0x03
 * Short address 3 = 0x00 0x03
 * 
 * op_1 data_1 		Operation description
 *  01  0-7f 		Reverse 28 speed command
 *  02  0-7f 		Forward 28 speed command
 *  03  0-7f 		Reverse 128 speed command
 *  04  0-7f 		Forward 128 speed command
 *  05  0 			Estop reverse command
 *  06  0 			Estop forward command
 *  07  0-1f 		Function group 1 (same format as DCC packet for FG1
 *  08  0-0f 		Function group 2 (same format as DCC packet for FG2
 *  09  0-0f 		Function group 3 (same format as DCC packet for FG3
 *  0a  0-7f 		Set reverse consist address for lead loco
 *  0b  0-7f 		Set forward consist address for lead loco
 *  0c  0-7f 		Set reverse consist address for rear loco
 *  0d  0-7f 		Set forward consist address for rear loco
 *  0e  0-7f 		Set reverse consist address for additional loco
 *  0f  0-7f 		Set forward consist address for additional loco
 *  10  0 			Del loco from consist
 *  11  0 			Kill consist
 *  12  0-9 		Set momentum
 *  13  0-7f 		No action, always returns success
 *  14  0-7f 		No action, always returns success
 *  15  0-ff 		Functions 13-20 control (bit 0=F13, bit 7=F20)
 *  16  0-ff 		Functions 21-28 control (bit 0=F21, bit 7=F28)
 *  17  0-3f 		Assign this loco to cab number in data_1
 *  18-7f 			reserved reserved
 * 
 *  Returns: ! = success
 *  1 = bad loco address
 *
 * @author Daniel Boudreau (C) 2007
 * @version     $Revision: 1.8 $
 */

public class NceBinaryCommand {
    
    public static final int ACC_CMD = 0xAD;		//NCE accessory command
    public static final int LOC_CMD = 0xA2;		//NCE Loco control command 
    public static final int READ_CMD = 0x8F;	//NCE read 16 bytes of memory command
    public static final int WRITEn_CMD = 0x8E;	//NCE write up to 16 bytes of memory command
    public static final int WRITE8_CMD = 0x9A;	//NCE write 8 bytes of memory command
    public static final int WRITE4_CMD = 0x99;	//NCE write 4 bytes of memory command
    public static final int STOP_CLOCK_CMD = 0x83;	//NCE stop clock command
    public static final int START_CLOCK_CMD = 0x84;	//NCE start clock command
    public static final int SET_CLOCK_CMD = 0x85;	//NCE set clock command
    public static final int CLOCK_1224_CMD = 0x86;	//NCE change clock 12/24 command
    public static final int CLOCK_RATIO_CMD = 0x87;	//NCE set clock ratio command
    
    public static byte[] accDecoder(int number, boolean closed) {
        
        if (number < 1 || number > 2044) {
            log.error("invalid NCE accessory address "+number);
            return null;
        }
        
        byte op_1;
        if (closed) op_1 = 0x03; else op_1 = 0x04;
        
        int addr_h = number/256;
        int addr_l = number & 0xFF;
        
        byte [] retVal = new byte[5];
        retVal[0] = (byte) (ACC_CMD); 	//NCE accessory command
        retVal[1] = (byte) (addr_h);	//high address
        retVal[2] = (byte) (addr_l);	//low address
        retVal[3] = (byte) (op_1);		//command
        retVal[4] = (byte) 0; 			//zero out last byte for acc
        
        return retVal;
    }
    
    public static byte[] accMemoryRead(int address){
        
        int addr_h = address/256;
        int addr_l = address & 0xFF;
        
        byte []retVal = new byte [3];
        retVal[0] = (byte) (READ_CMD);	//read 16 bytes command
        retVal[1] = (byte) (addr_h);	//high address
        retVal[2] = (byte) (addr_l);	//low address
        
        return retVal;
        
    }

	public static byte[] accMemoryWriteN(int address, int num) {

		int addr_h = address / 256;
		int addr_l = address & 0xFF;

		byte[] retVal = new byte[4+16];
		retVal[0] = (byte) (WRITEn_CMD);// write n bytes command
		retVal[1] = (byte) (addr_h); 	// high address
		retVal[2] = (byte) (addr_l); 	// low address
		retVal[3] = (byte) num;			// number of bytes to write

		return retVal;

	}
	
	public static byte[] accMemoryWrite8(int address) {

		int addr_h = address / 256;
		int addr_l = address & 0xFF;

		byte[] retVal = new byte[3+8];
		retVal[0] = (byte) (WRITE8_CMD);// write 8 bytes command
		retVal[1] = (byte) (addr_h); 	// high address
		retVal[2] = (byte) (addr_l); 	// low address

		return retVal;
    
}

	public static byte[] accMemoryWrite4(int address) {

		int addr_h = address / 256;
		int addr_l = address & 0xFF;

		byte[] retVal = new byte[3+4];
		retVal[0] = (byte) (WRITE4_CMD);// write 4 bytes command
		retVal[1] = (byte) (addr_h); 	// high address
		retVal[2] = (byte) (addr_l); 	// low address

		return retVal;
	}

	public static byte[] accStopClock() {

		byte[] retVal = new byte[1];
		retVal[0] = (byte) (STOP_CLOCK_CMD);// stop clock command

		return retVal;
	}

	public static byte[] accStartClock() {

		byte[] retVal = new byte[1];
		retVal[0] = (byte) (START_CLOCK_CMD);// start clock command

		return retVal;
	}

	public static byte[] accSetClock(int hours, int minutes) {

		byte[] retVal = new byte[3];
		retVal[0] = (byte) (SET_CLOCK_CMD);// set clock command
		retVal[1] = (byte) (hours); 	// hours
		retVal[2] = (byte) (minutes);	// minutes

		return retVal;
	}

	public static byte[] accSetClock1224(boolean flag) {

		int bit = 0;
		if (flag) {
			bit = 1;
		} else {
			bit = 0;
		}
		byte[] retVal = new byte[2];
		retVal[0] = (byte) (CLOCK_1224_CMD);	// set clock 12/24 command
		retVal[1] = (byte) (bit);			// 12 - 0, 24 - 1

		return retVal;
	}

	public static byte[] accSetClockRatio(int ratio) {

		byte[] retVal = new byte[2];
		retVal[0] = (byte) (CLOCK_RATIO_CMD);// set clock command
		retVal[1] = (byte) (ratio); 	// fast clock ratio

		return retVal;
	}
	
	// NCE Command 0xA2 sends speed or function packets to a locomotive
	// 0xA2 sub commands
	public static final int LOC_CMD_REV_CONSIST_LEAD = 0x0A;		//reverse consist address for lead loco
	public static final int LOC_CMD_FWD_CONSIST_LEAD = 0x0B;		//forward consist address for lead loco 
	public static final int LOC_CMD_REV_CONSIST_REAR = 0x0C;		//reverse consist address for rear loco 
	public static final int LOC_CMD_FWD_CONSIST_REAR = 0x0D;		//forward consist address for rear loco
	public static final int LOC_CMD_REV_CONSIST_MID = 0x0E;			//reverse consist address for additional loco 
	public static final int LOC_CMD_FWD_CONSIST_MID = 0x0F;			//forward consist address for additional loco 
	public static final int LOC_CMD_DELETE_LOC_CONSIST = 0x10;		//Delete loco from consist
	public static final int LOC_CMD_KILL_CONSIST = 0x10;			//Kill consist
	
	public static byte[] nceLocoCmd (int locoAddr, byte locoCmd, byte locoData){
		
        if (locoCmd < 1 || locoCmd > 0x17) {
            log.error("invalid NCE loco command "+locoCmd);
            return null;
        }
		
		int locoAddr_h = locoAddr/256;
        int locoAddr_l = locoAddr & 0xFF;
		
		byte[] retVal = new byte[5];
        retVal[0] = (byte) (LOC_CMD); 		//NCE Loco command
        retVal[1] = (byte) (locoAddr_h);	//loco high address
        retVal[2] = (byte) (locoAddr_l);	//loco low address
        retVal[3] = (byte) (locoCmd);		//sub command
        retVal[4] = (byte) (locoData); 		//sub data
		
		return retVal;
	}
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceBinaryCommand.class.getName());
}
/* @(#)NceBinaryCommand.java */


