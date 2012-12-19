// NceBinaryCommand.java

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
 
  Op_1 Data_1 		Operation description
 	01 	0-255 		NCE macro number 0-255
 	02 	0-255 		Duplicate of Op_1 command 01
 	03 	0 			Accessory Normal direction (ON)
 	04 	0 			Accessory Reverse direction (OFF)
 	05 	0-1f 		Signal Aspect 0-31
 	06-7f 			reserved reserved
 
 	Returns: ! = success
 	1 = bad accy/signal address
 
  0xA2 sends speed or function packets to a locomotive.
 
  Command Format: 0xA2 <addr_h> <addr_l> <op_1> <data_1>
  Addr_h and Addr_l are the loco address in DCC format.
  If a long address is in use, bits 6 and 7 of the high byte are set.
  Example: Long address 3 = 0xc0 0x03
  Short address 3 = 0x00 0x03
  
  op_1 data_1 		Operation description
   01  0-7f 		Reverse 28 speed command
   02  0-7f 		Forward 28 speed command
   03  0-7f 		Reverse 128 speed command
   04  0-7f 		Forward 128 speed command
   05  0 			Estop reverse command
   06  0 			Estop forward command
   07  0-1f 		Function group 1 (same format as DCC packet for FG1
   08  0-0f 		Function group 2 (same format as DCC packet for FG2
   09  0-0f 		Function group 3 (same format as DCC packet for FG3
   0a  0-7f 		Set reverse consist address for lead loco
   0b  0-7f 		Set forward consist address for lead loco
   0c  0-7f 		Set reverse consist address for rear loco
   0d  0-7f 		Set forward consist address for rear loco
   0e  0-7f 		Set reverse consist address for additional loco
   0f  0-7f 		Set forward consist address for additional loco
   10  0 			Del loco from consist
   11  0 			Kill consist
   12  0-9 		Set momentum
   13  0-7f 		No action, always returns success
   14  0-7f 		No action, always returns success
   15  0-ff 		Functions 13-20 control (bit 0=F13, bit 7=F20)
   16  0-ff 		Functions 21-28 control (bit 0=F21, bit 7=F28)
   17  0-3f 		Assign this loco to cab number in data_1
   18-7f 			reserved reserved
  
   Returns: ! = success
   1 = bad loco address
   
 */

 /**
  * NCE Binary Commands
  * 
  * Also see NceMessage.java for additional commands
  * 
  * @author Daniel Boudreau (C) 2007, 2010
  * @version     $Revision$
  */

public class NceBinaryCommand {
    
	// NOTE: NCE USB does not support any clock commands
	
	public static final int READ_CLOCK_CMD = 0x82;	//NCE read clock command
	public static final int STOP_CLOCK_CMD = 0x83;	//NCE stop clock command
    public static final int START_CLOCK_CMD = 0x84;	//NCE start clock command
    public static final int SET_CLOCK_CMD = 0x85;	//NCE set clock command
    public static final int CLOCK_1224_CMD = 0x86;	//NCE change clock 12/24 command
    public static final int CLOCK_RATIO_CMD = 0x87;	//NCE set clock ratio command

    // NOTE: NCE USB does not support the following AUI command
    public static final int READ_AUI4_CMD = 0x8A;	//NCE read status of AUI yy, returns four bytes
    
    public static final int DUMMY_CMD = 0x8C;		//NCE Dummy instruction
    
    // NOTE: NCE USB does not support any read or write memory commands
    
    public static final int WRITEn_CMD = 0x8E;		//NCE write up to 16 bytes of memory command
    public static final int READ16_CMD = 0x8F;		//NCE read 16 bytes of memory command
    public static final int WRITE1_CMD = 0x97;		//NCE write 1 bytes of memory command
    public static final int WRITE2_CMD = 0x98;		//NCE write 2 bytes of memory command
    public static final int WRITE4_CMD = 0x99;		//NCE write 4 bytes of memory command
    public static final int WRITE8_CMD = 0x9A;		//NCE write 8 bytes of memory command
    public static final int READ1_CMD = 0x9D;		//NCE read 1 byte of memory command
    
    public static final int READ_AUI2_CMD = 0x9B;	//NCE read status of AUI yy, returns two bytes
    public static final int MACRO_CMD = 0x9C;		//NCE execute macro n

    public static final int ACC_CMD = 0xAD;			//NCE accessory command
    public static final int LOCO_CMD = 0xA2;		//NCE Loco control command 
    public static final int SW_REV_CMD = 0xAA; 		//NCE get EPROM revision cmd, Reply Format: VV.MM.mm

    // NOTE: ONLY NCE USB connected to PowerCab or SB3 supports the following commands
    
    public static final int OPS_PROG_LOCO_CMD = 0xAE; // NCE ops mode program loco
    public static final int OPS_PROG_ACCY_CMD = 0xAF; // NCE ops mode program accessories

    
    public static byte[] accDecoder(int number, boolean closed) {
        
        if (number < 1 || number > 2044) {
            log.error("invalid NCE accessory address "+number);
            return null;
        }
        
        /* Moved to NceMessageCheck
        // USB connected to PowerCab or SB3 can only access addresses up to 250
		if (number > 250
				&& ((NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_POWERCAB) || 
						(NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_SB3))) {
			log.error("invalid NCE accessory address for USB " + number);
			return null;
		}
		*/
        
        byte op_1;
        if (closed) op_1 = 0x03; else op_1 = 0x04;
        
        int addr_h = number/256;
        int addr_l = number & 0xFF;
        
        byte [] retVal = new byte[5];
        retVal[0] = (byte) (ACC_CMD); 	//NCE accessory command
        retVal[1] = (byte) (addr_h);	//high address
        retVal[2] = (byte) (addr_l);	//low address
        retVal[3] = op_1;				//command
        retVal[4] = (byte) 0; 			//zero out last byte for acc
        
        return retVal;
    }
    
    public static byte[] accMemoryRead(int address){
 
        /* Moved to NceMessageCheck
        // this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command to NCE USB");
			return null;
    	}
    	*/
        
        int addr_h = address/256;
        int addr_l = address & 0xFF;
        
        byte []retVal = new byte [3];
        retVal[0] = (byte) (READ16_CMD);//read 16 bytes command
        retVal[1] = (byte) (addr_h);	//high address
        retVal[2] = (byte) (addr_l);	//low address
        
        return retVal;
        
    }
   
    /**
     * Read one byte from NCE command station memory
     * @param address
     * @return binary command to read one byte
     */
    public static byte[] accMemoryRead1(int address) {
    	/* Moved to NceMessageCheck
    	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE) {
    		log.error("attempt to send unsupported binary command to NCE USB");
    		return null;
    	}
    	*/

    	int addr_h = address/256;
    	int addr_l = address & 0xFF;

    	byte []retVal = new byte [3];
    	retVal[0] = (byte) (READ1_CMD);	//read 1 byte command
    	retVal[1] = (byte) (addr_h);	//high address
    	retVal[2] = (byte) (addr_l);	//low address

    	return retVal;

    }

    public static byte[] accMemoryWriteN(int address, int num) {
    	/* Moved to NceMessageCheck
    	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command to NCE USB");
    		return null;
    	}
    	*/

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
    	/* Moved to NceMessageCheck
    	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command to NCE USB");
    		return null;
    	}
    	*/

    	int addr_h = address / 256;
    	int addr_l = address & 0xFF;

    	byte[] retVal = new byte[3+8];
    	retVal[0] = (byte) (WRITE8_CMD);// write 8 bytes command
    	retVal[1] = (byte) (addr_h); 	// high address
    	retVal[2] = (byte) (addr_l); 	// low address

    	return retVal;

    }

    public static byte[] accMemoryWrite4(int address) {
    	/* Moved to NceMessageCheck
    	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command to NCE USB");
			return null;
    	}
    	*/

		int addr_h = address / 256;
		int addr_l = address & 0xFF;

		byte[] retVal = new byte[3+4];
		retVal[0] = (byte) (WRITE4_CMD);// write 4 bytes command
		retVal[1] = (byte) (addr_h); 	// high address
		retVal[2] = (byte) (addr_l); 	// low address

		return retVal;
	}

    public static byte[] accMemoryWrite2(int address) {
    	/* Moved to NceMessageCheck
    	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command to NCE USB");
			return null;
    	}
    	*/

		int addr_h = address / 256;
		int addr_l = address & 0xFF;

		byte[] retVal = new byte[3+2];
		retVal[0] = (byte) (WRITE2_CMD);// write 4 bytes command
		retVal[1] = (byte) (addr_h); 	// high address
		retVal[2] = (byte) (addr_l); 	// low address

		return retVal;
	}
    
    public static byte[] accMemoryWrite1(int address) {
    	/* Moved to NceMessageCheck
    	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command to NCE USB");
			return null;
    	}
    	*/

		int addr_h = address / 256;
		int addr_l = address & 0xFF;

		byte[] retVal = new byte[3+1];
		retVal[0] = (byte) (WRITE1_CMD);// write 4 bytes command
		retVal[1] = (byte) (addr_h); 	// high address
		retVal[2] = (byte) (addr_l); 	// low address

		return retVal;
	}

	public static byte[] accStopClock() {
		/* Moved to NceMessageCheck
	   	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command to NCE USB");
			return null;
    	}
    	*/

		byte[] retVal = new byte[1];
		retVal[0] = (byte) (STOP_CLOCK_CMD);// stop clock command

		return retVal;
	}

	public static byte[] accStartClock() {
		/* Moved to NceMessageCheck
	   	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command to NCE USB");
			return null;
    	}
    	*/

		byte[] retVal = new byte[1];
		retVal[0] = (byte) (START_CLOCK_CMD);// start clock command

		return retVal;
	}

	public static byte[] accSetClock(int hours, int minutes) {
		/* Moved to NceMessageCheck
	   	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command to NCE USB");
			return null;
    	}
    	*/

		byte[] retVal = new byte[3];
		retVal[0] = (byte) (SET_CLOCK_CMD);// set clock command
		retVal[1] = (byte) (hours); 	// hours
		retVal[2] = (byte) (minutes);	// minutes

		return retVal;
	}

	public static byte[] accSetClock1224(boolean flag) {
		/* Moved to NceMessageCheck
	   	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command to NCE USB");
			return null;
    	}
    	*/

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
		/* Moved to NceMessageCheck
	   	// this command isn't supported by the NCE USB
    	if (NceUSB.getUsbSystem() != NceUSB.USB_SYSTEM_NONE){
    		log.error("attempt to send unsupported binary command to NCE USB");
			return null;
    	}
    	*/

		byte[] retVal = new byte[2];
		retVal[0] = (byte) (CLOCK_RATIO_CMD);// set clock command
		retVal[1] = (byte) (ratio); 	// fast clock ratio

		return retVal;
	}
	
	// NCE Command 0xA2 sends speed or function packets to a locomotive
	// 0xA2 sub commands speed and functions
	public static final byte LOCO_CMD_REV_28SPEED = 0x01;			//set loco speed 28 steps reverse
	public static final byte LOCO_CMD_FWD_28SPEED = 0x02;			//set loco speed 28 steps forward
	public static final byte LOCO_CMD_REV_128SPEED = 0x03;			//set loco speed 128 steps reverse
	public static final byte LOCO_CMD_FWD_128SPEED = 0x04;			//set loco speed 128 steps forward
	public static final byte LOCO_CMD_REV_ESTOP = 0x05;				//emergency stop reverse
	public static final byte LOCO_CMD_FWD_ESTOP = 0x06;				//emergency stop forward
	public static final byte LOCO_CMD_FG1 = 0x07;					//function group 1
	public static final byte LOCO_CMD_FG2 = 0x08;					//function group 2
	public static final byte LOCO_CMD_FG3 = 0x09;					//function group 3
	public static final byte LOCO_CMD_FG4 = 0x15;					//function group 4
	public static final byte LOCO_CMD_FG5 = 0x16;					//function group 5
	
	// OxA2 sub commands consist
	public static final byte LOCO_CMD_REV_CONSIST_LEAD = 0x0A;		//reverse consist address for lead loco
	public static final byte LOCO_CMD_FWD_CONSIST_LEAD = 0x0B;		//forward consist address for lead loco 
	public static final byte LOCO_CMD_REV_CONSIST_REAR = 0x0C;		//reverse consist address for rear loco 
	public static final byte LOCO_CMD_FWD_CONSIST_REAR = 0x0D;		//forward consist address for rear loco
	public static final byte LOCO_CMD_REV_CONSIST_MID = 0x0E;		//reverse consist address for additional loco 
	public static final byte LOCO_CMD_FWD_CONSIST_MID = 0x0F;		//forward consist address for additional loco 
	public static final byte LOCO_CMD_DELETE_LOCO_CONSIST = 0x10;	//Delete loco from consist
	public static final byte LOCO_CMD_KILL_CONSIST = 0x11;			//Kill consist
	
	public static byte[] nceLocoCmd (int locoAddr, byte locoSubCmd, byte locoData){
        if (locoSubCmd < 1 || locoSubCmd > 0x17) {
            log.error("invalid NCE loco command "+locoSubCmd);
            return null;
        }
		
		int locoAddr_h = locoAddr/256;
        int locoAddr_l = locoAddr & 0xFF;
		
		byte[] retVal = new byte[5];
        retVal[0] = (byte) (LOCO_CMD); 		//NCE Loco command
        retVal[1] = (byte) (locoAddr_h);	//loco high address
        retVal[2] = (byte) (locoAddr_l);	//loco low address
        retVal[3] = locoSubCmd;				//sub command
        retVal[4] = locoData; 				//sub data
		
		return retVal;
	}
	
	/**
	 * create NCE EPROM revision message, Reply Format: VV.MM.mm
	 * 
	 */
	public static byte[] getNceEpromRev() {
		byte[] retVal = new byte[1];
		retVal[0] = (byte) (SW_REV_CMD);
		return retVal;
	}
	
	/**
	 * create an NCE USB compatible ops mode loco message
	 * 
	 * @param locoAddr
	 * @param cvAddr
	 * @param cvData
	 * @return byte[] containing message
	 */
	public static byte[] usbOpsModeLoco(NceTrafficController tc, int locoAddr, int cvAddr, int cvData) {
		/* Moved to NceMessageCheck
		// ONLY USB connected to PowerCab or SB3 can send this message
		if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERCAB
				|| tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3) {
				*/

			byte[] retVal = new byte[6];
			int locoAddr_h = locoAddr / 256;
			int locoAddr_l = locoAddr & 0xFF;
			int cvAddr_h = cvAddr / 256;
			int cvAddr_l = cvAddr & 0xFF;

			retVal[0] = (byte) (OPS_PROG_LOCO_CMD); // NCE ops mode loco command
			retVal[1] = (byte) (locoAddr_h); // loco high address
			retVal[2] = (byte) (locoAddr_l); // loco low address
			retVal[3] = (byte) (cvAddr_h); // CV high address
			retVal[4] = (byte) (cvAddr_l); // CV low address
			retVal[5] = (byte) (cvData); // CV data

			return retVal;
			/* Moved to NceMessageCheck
		} else {
			log.error("attempt to send unsupported binary command");
			return null;
		}
		*/
	}

	/**
	 * create an NCE USB compatible ops mode accy message
	 * 
	 * @param accyAddr
	 * @param cvAddr
	 * @param cvData
	 * @return byte[] containing message
	 */
	public static byte[] usbOpsModeAccy(int accyAddr, int cvAddr, int cvData) {
		/* Moved to NceMessageCheck
		// ONLY USB connected to PowerCab or SB3 can send this message
		if (NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_POWERCAB
				|| NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_SB3) {
				*/

			byte[] retVal = new byte[6];
			int accyAddr_h = accyAddr / 256;
			int accyAddr_l = accyAddr & 0xFF;
			int cvAddr_h = cvAddr / 256;
			int cvAddr_l = cvAddr & 0xFF;

			retVal[0] = (byte) (OPS_PROG_ACCY_CMD); // NCE ops mode accy command
			retVal[1] = (byte) (accyAddr_h); // accy high address
			retVal[2] = (byte) (accyAddr_l); // accy low address
			retVal[3] = (byte) (cvAddr_h); // CV high address
			retVal[4] = (byte) (cvAddr_l); // CV low address
			retVal[5] = (byte) (cvData); // CV data

			return retVal;
			/* Moved to NceMessageCheck
		} else {
			log.error("attempt to send unsupported binary command");
			return null;
		}
		*/
	}
	
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceBinaryCommand.class.getName());
}
/* @(#)NceBinaryCommand.java */


