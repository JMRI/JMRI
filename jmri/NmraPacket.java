// NmraPacket.java

package jmri;

/**
 * Utilities for coding/decoding NMRA S&RP DCC packets.
 *<P>
 * Packets are (now) represented by an array of bytes. Preamble/postamble
 * not included. Note that this is a data representation, _not_ a representation
 * of the waveform!  But this is a class, which might eventually also
 * form a representation object.
 *<P>
 * This is meant to be a general Java NMRA implementation, so does NOT use
 * JMRI utilities. In particular, it returns null instead of throwing JmriException
 * for invalid requests. Callers need to check upstream.
 *<P>
 * Note that this is structured by packet type, not by what want to do.  E.g.
 * there are functions to specific packet formats instead of a general "loco speed
 * packet" routine which figures out which type of packet to use.  Those decisions
 * are to be made somewhere else.
 * <P>
 * Range and value checking is intended to be aggressive; if we can check, we
 * should.  Problems are reported as warnings.
 *<P>
 * Conventions:
 *<P>
 * "S" vs "L" in the last character of the loco function name selects
 *            short vs long address type
 *
 * @author      Bob Jacobsen Copyright (C) 2001, 2003
 * @version     $Revision: 1.10 $
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

    /**
     * Provide an accessory control packet via a simplified interface
     * @param number Address of accessory output, starting with 1
     * @param thrown true if the output is to be configured to the "closed", a.k.a. the
     * "normal" or "unset" position
     */
    public static byte[] accDecoderPkt(int number, boolean closed) {
        // dBit is the "channel" info, least 7 bits, for the packet
        // The lowest channel bit represents CLOSED (1) and THROWN (0)
        int dBits = (( (number-1) & 0x03) << 1 );  // without the low CLOSED vs THROWN bit
        dBits = closed ? (dBits | 1) : dBits;

        // aBits is the "address" part of the nmra packet, which starts with 1
        // 07/01/05 R.Scheffler - Removed the mask, this will allow any 'too high' numbers
        // through to accDecoderPkt() above which will log the error if out of bounds. If we
        // mask it here, then the number will 'wrap' without any indication that it did so.
        int aBits = (number-1) >> 2;      // Divide by 4 to get the 'base'
        aBits += 1;                       // Base is +1

        // cBit is the control bit, we're always setting it active
        int cBit = 1;

        // get the packet
        return NmraPacket.accDecoderPkt(aBits, cBit, dBits);
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
        log.error("locoSpeed14S not fully implemented");
        return new byte[2];
    }

    public static byte[] opsCvWriteByte(int address, boolean longAddr, int cvNum, int data ) {
        if (log.isDebugEnabled()) log.debug("opswrite "+address+" "+cvNum+" "+data);
        if (address < 0 ) {  // zero is valid broadcast
            log.error("invalid address "+address);
            return null;
        }
        if (longAddr&& (address> (255+(231-192)*256)) ) {
            log.error("invalid address "+address);
            return null;
        }
        if (!longAddr&& (address> 127) ) {
            log.error("invalid address "+address);
            return null;
        }
        if (data<0 || data>255) {
            log.error("invalid data "+data);
            return null;
        }
        if (cvNum<1 || cvNum>512) {
            log.error("invalid CV number "+cvNum);
            return null;
        }

        // end sanity checks, format output
        byte[] retVal;
        int arg1 = 0xEC + (((cvNum-1)>>8)&0x03);
        int arg2 = (cvNum-1)&0xFF;
        int arg3 = data&0xFF;

        if (longAddr) {
            // long address form
            retVal = new byte[6];
            retVal[0] = (byte) (192+((address/256)&0x3F));
            retVal[1] = (byte) (address&0xFF);
            retVal[2] = (byte) arg1;
            retVal[3] = (byte) arg2;
            retVal[4] = (byte) arg3;
            retVal[5] = (byte) (retVal[0]^retVal[1]^retVal[2]^retVal[3]^retVal[4]);
        } else {
            // short address form
            retVal = new byte[5];
            retVal[0] = (byte) (address&0xFF);
            retVal[1] = (byte) arg1;
            retVal[2] = (byte) arg2;
            retVal[3] = (byte) arg3;
            retVal[4] = (byte) (retVal[0]^retVal[1]^retVal[2]^retVal[3]);
        }
        return retVal;
    }

    public static byte[] speedStep128Packet(int address, boolean longAddr, int speed, boolean fwd ) {
        if (log.isDebugEnabled()) log.debug("128 step packet "+address+" "+speed);
        if (address < 0 ) {  // zero is valid broadcast
            log.error("invalid address "+address);
            return null;
        }
        if (longAddr&& (address> (255+(231-192)*256)) ) {
            log.error("invalid address "+address);
            return null;
        }
        if (!longAddr&& (address> 127) ) {
            log.error("invalid address "+address);
            return null;
        }
        if (speed<0 || speed>127) {
            log.error("invalid speed "+speed);
            return null;
        }

        // end sanity checks, format output
        byte[] retVal;
        int arg1 = 0x3F;
        int arg2 = (speed&0x7F) | (fwd ? 0x80 : 0);

        if (longAddr) {
            // long address form
            retVal = new byte[5];
            retVal[0] = (byte) (192+((address/256)&0x3F));
            retVal[1] = (byte) (address&0xFF);
            retVal[2] = (byte) arg1;
            retVal[3] = (byte) arg2;
            retVal[4] = (byte) (retVal[0]^retVal[1]^retVal[2]^retVal[3]);
        } else {
            // short address form
            retVal = new byte[4];
            retVal[0] = (byte) (address&0xFF);
            retVal[1] = (byte) arg1;
            retVal[2] = (byte) arg2;
            retVal[3] = (byte) (retVal[0]^retVal[1]^retVal[2]);
        }
        return retVal;
    }

    public static byte[] function0Through4Packet(int address, boolean longAddr,
                        boolean f0, boolean f1, boolean f2, boolean f3, boolean f4 ) {
        if (log.isDebugEnabled()) log.debug("f0 through f4 packet "+address);
        if (address < 0 ) {  // zero is valid broadcast
            log.error("invalid address "+address);
            return null;
        }
        if (longAddr&& (address> (255+(231-192)*256)) ) {
            log.error("invalid address "+address);
            return null;
        }
        if (!longAddr&& (address> 127) ) {
            log.error("invalid address "+address);
            return null;
        }

        // end sanity checks, format output
        byte[] retVal;
        int arg1 = 0x80 |
                    ( f0 ? 0x10 : 0) |
                    ( f1 ? 0x01 : 0) |
                    ( f2 ? 0x02 : 0) |
                    ( f3 ? 0x04 : 0) |
                    ( f4 ? 0x08 : 0);

        if (longAddr) {
            // long address form
            retVal = new byte[4];
            retVal[0] = (byte) (192+((address/256)&0x3F));
            retVal[1] = (byte) (address&0xFF);
            retVal[2] = (byte) arg1;
            retVal[3] = (byte) (retVal[0]^retVal[1]^retVal[2]);
        } else {
            // short address form
            retVal = new byte[3];
            retVal[0] = (byte) (address&0xFF);
            retVal[1] = (byte) arg1;
            retVal[2] = (byte) (retVal[0]^retVal[1]);
        }
        return retVal;
    }

    public static byte[] function5Through8Packet(int address, boolean longAddr,
                        boolean f5, boolean f6, boolean f7, boolean f8 ) {
        if (log.isDebugEnabled()) log.debug("f5 through f8 packet "+address);
        if (address < 0 ) {  // zero is valid broadcast
            log.error("invalid address "+address);
            return null;
        }
        if (longAddr&& (address> (255+(231-192)*256)) ) {
            log.error("invalid address "+address);
            return null;
        }
        if (!longAddr&& (address> 127) ) {
            log.error("invalid address "+address);
            return null;
        }

        // end sanity checks, format output
        byte[] retVal;
        int arg1 = 0xB0 |
                    ( f8 ? 0x08 : 0) |
                    ( f7 ? 0x04 : 0) |
                    ( f6 ? 0x02 : 0) |
                    ( f5 ? 0x01 : 0);

        if (longAddr) {
            // long address form
            retVal = new byte[4];
            retVal[0] = (byte) (192+((address/256)&0x3F));
            retVal[1] = (byte) (address&0xFF);
            retVal[2] = (byte) arg1;
            retVal[3] = (byte) (retVal[0]^retVal[1]^retVal[2]);
        } else {
            // short address form
            retVal = new byte[3];
            retVal[0] = (byte) (address&0xFF);
            retVal[1] = (byte) arg1;
            retVal[2] = (byte) (retVal[0]^retVal[1]);
        }
        return retVal;
    }

    public static byte[] function9Through12Packet(int address, boolean longAddr,
                        boolean f9, boolean f10, boolean f11, boolean f12 ) {
        if (log.isDebugEnabled()) log.debug("f9 through f12 packet "+address);
        if (address < 0 ) {  // zero is valid broadcast
            log.error("invalid address "+address);
            return null;
        }
        if (longAddr&& (address> (255+(231-192)*256)) ) {
            log.error("invalid address "+address);
            return null;
        }
        if (!longAddr&& (address> 127) ) {
            log.error("invalid address "+address);
            return null;
        }

        // end sanity checks, format output
        byte[] retVal;
        int arg1 = 0xA0 |
                    ( f12 ? 0x08 : 0) |
                    ( f11 ? 0x04 : 0) |
                    ( f10 ? 0x02 : 0) |
                    ( f9  ? 0x01 : 0);

        if (longAddr) {
            // long address form
            retVal = new byte[4];
            retVal[0] = (byte) (192+((address/256)&0x3F));
            retVal[1] = (byte) (address&0xFF);
            retVal[2] = (byte) arg1;
            retVal[3] = (byte) (retVal[0]^retVal[1]^retVal[2]);
        } else {
            // short address form
            retVal = new byte[3];
            retVal[0] = (byte) (address&0xFF);
            retVal[1] = (byte) arg1;
            retVal[2] = (byte) (retVal[0]^retVal[1]);
        }
        return retVal;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NmraPacket.class.getName());
}


/* @(#)NmraPacket.java */
