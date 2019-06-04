package jmri.jmrix.nce;

/**
 * USB {@literal ->} Cab bus adapter: When used with PowerCab V1.28 - 6.3.0 When used with
 * SB3 V1.28 - 6.3.1 (No program track on an SB3) When used with PH-Pro or PH-10
 * - 6.3.2 (limited set of features available through cab bus)
 * <p>
 * From NCE 2007 (with some minor corrections, formatting {@literal &} additional info):
 * <p>
 * I've added two new binary commands on the Power Cab. They are for OPs
 * programming of locomotives and OPs programming of accessories/signals The
 * commands are 0xAE and 0xAF.
 * <p>
 * The USB interface when configured for Power Cab Ver 1.28 returns software
 * version 6.3.0. When configured for the SB3 V1.28 it returns version 6.3.1.
 * (SB3 doesn't have a program track) And when configured for the Power Pro 2007
 * it returns version 6.3.2. Not all binary commands are useful or available
 * with the PowerCab, SB3, or Power Pro. PowerCab/SB3/Power Pro commands
 * supported below:
 * <p>
 * *************************************************************************
 * <p>
 * The RS-232 port binary commands are designed to work in a computer friendly
 * mode.
 * <p>
 * Command format is: (cmd number) (data) (data) ...
 * <p>
 * Commands range from 0x80 to 0xBF
 * <p>
 * The number of bytes in the command is determined by using the lookup table
 * "BIN_TABLE". the number returned from the table is inclusive of the command
 * number.
 * <p>
 * Commands and formats supported:
 * <p>
 * CMD FORMAT DESCRIPTION # OF BYTES RESPONSES RETURNED
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0x80 NOP, dummy instruction (1) !
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0x8C Dummy instruction returns "!" (3) !,0x0D,0x0A followed by CR/LF
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0x9C xx Execute macro number xx (1) !,0,3
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0x9E Enter Programming track mode (1) ! = success 3 = short circuit. Not
 * supported by SB3 or PH
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0x9F Exit Programming track mode (1) ! = success. Not supported by SB3 or PH
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0xA0 aaaa xx Program CV aa with data xx in (1) ! = success paged mode 0 =
 * program track not enabled. Not supported by SB3 or PH
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0xA1 aaaa Read CV aa in paged mode (2) !,0,3 NOTE: cv data followed ! for ok,
 * 0xff followed by 3 for can't read. Not supported by SB3 or PH
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0xA2 (4 data bytes) Locomotive control command (1) !,1.
 * <p>
 * Sends a speed or function packet to a locomotive. Command Format: 0xA2
 * (addr_h) (addr_l) (op_1) (data_1)
 * <p>
 * addr_h and addr_l are the loco address in DCC format. If a long address is in
 * use, bits 6 and 7 of the high byte are set. Ex: Long address 3 = 0xc0 0x03
 * Short address 3 = 0x00 0x03
 * <p>
 *
 * op_1 data_1 Operation description
 * <p>
 * 00 0-7f nop
 * <p>
 * 01 0-7f Reverse 28 speed command
 * <p>
 * 02 0-7f Forward 28 speed command
 * <p>
 * 03 0-7f Reverse 128 speed command
 * <p>
 * 04 0-7f Forward 128 speed command
 * <p>
 * 05 0 Estop reverse command
 * <p>
 * 06 0 Estop forward command
 * <p>
 * 07 0-1f Function group 1 (same format as DCC packet for FG1)
 * <p>
 * 08 0-0f Function group 2 (same format as DCC packet for FG2)
 * <p>
 * 09 0-0f Function group 3 (same format as DCC packet for FG3)
 * <p>
 * 0a-14 not supported in PowerCab or SB3 version 1.28
 * <p>
 * 15 0-ff Functions 13-20 control (bit 0=F13, bit 7=F20)
 * <p>
 * 16 0-ff Functions 21-28 control (bit 0=F21, bit 7=F28)
 * <p>
 * 17-7f reserved reserved
 *
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0xA6 rr xx Program register rr with data xx (1) ! = success in register mode
 * 0 = program track not enabled. Not supported by SB3 or PH
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0xA7 rr Read register rr in register mode(2) !,3 NOTE: cv data followed ! for
 * ok, 0 = program track not enabled 0xff followed by 3 for can't read. Not
 * supported by SB3 or PH
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0xA8 aaaa xx Program CV aaaa with data xx (1) ! = success in direct mode 0 =
 * program track not enabled. Not supported by SB3 or PH
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0xA9 aaaa Read CV aaaa in direct mode (2) !,3 NOTE: cv data followed ! for
 * ok, 0 = program track not enabled 0xff followed by 3 for can't read. Not
 * supported by SB3 or PH
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0xAA Return software revision number (3) (data1),(data2),(data3) FORMAT:
 * VV.MM.mm
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0xAD {@code <4 data bytes>} Accy/signal and macro commands (1) !,1
 * <p>
 * Command Format: 0xAD (addr_h) (addr_l) (op_1) (data_1)
 * <p>
 * addr_h and addr_l are the accessory/signal address (NOT in DCC format). Ex:
 * Accessory Address 513 = 0x02 0x01 (hi byte first)
 * <p>
 * NOTE: accy/signal address 0 is not a valid address
 * <p>
 * SPECIAL NOTE: PowerCab/SB3 version 1.28 only supports up to accessory address
 * 250
 * <p>
 * Op_1 Data_1 Operation description
 * <p>
 * 01 0-255 NCE macro number 0-255
 * <p>
 * 02 0-255 Duplicate of Op_1 command
 * <p>
 * 03 0 Accessory Normal direction (ON)
 * <p>
 * 04 0 Accessory Reverse direction (OFF)
 * <p>
 * 05 0-1f Signal Aspect 0-31
 * <p>
 * 05-7f reserved reserved
 *
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0xAE (5 data bytes) OPs program loco CV (1) !,1,3. Not supported by PH
 * <p>
 * Command Format: 0xAE (addr_h) (addr_l) (CV_h) (CV_l) (data)
 * <p>
 * addr_h,addr_l are loco address (same as 0xA2 command) CV_h, CV_l are cv
 * address high byte first data is 8 bit data for CV
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 * 0xAF {@code <5 data bytes>} OPs program accessory/signal (1) !,1,3. Not 
 * supported by PH
 * <p>
 * Command Format: 0xAF (addr_h) (addr_l) (CV_h) (CV_l) (data)
 * <p>
 * addr_h,addr_l are accy/sig address (same as 0xAD command) CV_h, CV_l are CV
 * address high byte first data is 8 bit data for CV
 * <p>
 * --------------------------------------------------------------------------
 * <p>
 *
 * NOTE: a single byte of 0 will be returned if not in programming mode for
 * commands 0x9F,0xA0,0xA1 and 0xA6-0xA9
 *
 * <p>
 * Errors returned:
 * <p>
 * '0'= command not supported
 * <p>
 * '1'= loco/accy/signal address out of range
 * <p>
 * '2'= cab address or op code out of range
 * <p>
 * '3'= CV address or data out of range
 * <p>
 * '4'= byte count out of range
 * <p>
 * '!'= command completed successfully
 * <p>
 * *************************************************************************
 * <p>
 * Commands not supported by USB, will return ASCII '0':
 * <p>
 * 0x81 through 0x8B
 * <p>
 * 0x8D through 0x9B
 * <p>
 * 0x9D
 * <p>
 * 0xA3 through 0xA5
 * <p>
 * 0xAB, 0xAC
 * <p>
 * 0xB2 through 0xBF
 *
 * @author Daniel Boudreau Copyright (C) 2007
 */
@Deprecated
public class NceUSB {

}



