/** 
 * NmraPacket.java
 *
 * Description:		<describe the NmraPacket class here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;

/**
 * Provide utilities for coding/decoding NMRA S&RP DCC packets
 *
 * Packets are (now) represented by an array of bytes. Preamble/postamble
 * not included. Note that this is a data representation, _not_ a representation
 * of the waveform!  But this is a class, which might eventually also
 * form a representation object.
 *
 * This is meant to be a general Java NMRA implementation, so does NOT use
 * JMRI utilities. In particular, it returns null instead of throwing JmriException
 * for invalid requests. Callers need to check upstream.
 *
 * Note that this is structured by packet type, not by what want to do.  E.g.
 * there are functions to specific packet formats instead of a general "loco speed
 * packet" routine which figures out which type of packet to use.  Those decisions
 * are to be made somewhere else.
 * 
 * Range and value checking is intended to be aggressive; if we can check, we 
 * should.  Problems are reported as warnings.
 *
 * Conventions:
 *
 * "S" vs "L" in the last character of the loco function name selects
 *            short vs long address type
 *
 */
public class NmraPacket {
	
	public static byte[] accDecoderPkt(int addr, int active, int outputChannel) {
		// From the NMRA RP:
		// 0 10AAAAAA 0 1AAACDDD 0 EEEEEEEE 1
		// Accessory Digital Decoders can be designed to control momentary or 
		// constant-on devices, the duration of time each output is active being controlled 
		// by configuration variables CVs #515 through 518. Bit 3 of the second byte "C" is 
		// used to activate or deactivate the addressed device. (Note if the duration the 
		// device is intended to be on is less than or equal the set duration, no deactivation 
		// is necessary.) Since most devices are paired, the convention is that bit "0" of 
		// the second byte is used to distinguish between which of a pair of outputs the 
		// accessory decoder is activating or deactivating. Bits 1 and 2 of byte two is used 
		// to indicate which of 4 pairs of outputs the packet is controlling. The significant 
		// bits of the 9 bit address are bits 4-6 of the second data byte. By convention 
		// these three bits are in ones complement. The use of bit 7 of the second byte 
		// is reserved for future use. 
		
		// Note that A=1 is the first (lowest) valid address field, and the
		// largest is 512!  I don't know why this is, but it gets the
		// right hardware addresses

		if (addr < 1 || addr>511) {
			log.error("invalid address "+addr);
			return null;
		} 
		if (active < 0 || active>1) {
			log.error("invalid active (C) bit "+addr);
			return null;
		} 
		if (outputChannel < 0 || outputChannel>7) {
			log.error("invalid output channel "+addr);
			return null;
		} 
		
		int lowAddr = addr & 0x3F;
		int highAddr = ( (~addr) >> 6) & 0x07;
		
		byte[] retVal = new byte[3];
		
		retVal[0] = (byte) (0x80 | lowAddr);
		retVal[1] = (byte) (0x80 | (highAddr << 4 ) | ( active << 3) | outputChannel&0x07);
		retVal[2] = (byte) (retVal[0] ^ retVal[1]);
		
		return retVal;
	}
	
	static byte[] locoSpeed14S(int address, int speedStep, boolean F0 ) { 
		if (log.isDebugEnabled()) log.debug("create "+address+" "+speedStep+" "+F0);
		if (speedStep < 0 || speedStep>14) {
			log.error("invalid speedStep "+speedStep);
			return null;
		} 
		if (address < 0 || address>14) {
			log.error("invalid address "+speedStep);
			return null;
		} 
		return new byte[2];
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NmraPacket.class.getName());
}


/* @(#)NmraPacket.java */
