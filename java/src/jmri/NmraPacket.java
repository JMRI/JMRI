package jmri;

import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for coding/decoding NMRA {@literal S&RP} DCC packets.
 * <p>
 * Packets are (now) represented by an array of bytes. Preamble/postamble not
 * included. Note that this is a data representation, _not_ a representation of
 * the waveform! But this is a class, which might eventually also form a
 * representation object.
 * <p>
 * This is meant to be a general Java NMRA implementation, so does NOT use JMRI
 * utilities. In particular, it returns null instead of throwing JmriException
 * for invalid requests. Callers need to check upstream.
 * <p>
 * The function is provided by static member functions; objects of this class
 * should not be created.
 * <p>
 * Note that these functions are structured by packet type, not by what want to
 * do. E.g. there are functions to create specific packet formats instead of a
 * general "loco speed packet" routine which figures out which type of packet to
 * use. Those decisions are to be made somewhere else.
 * <p>
 * Range and value checking is intended to be aggressive; if we can check, we
 * should. Problems are reported as warnings.
 * <p>
 * The basic function is to build a packet with proper addressing, etc:
 * <ul>
 * <li>oneBytePacket
 * <li>twoBytePacket
 * <li>threeBytePacket
 * <li>fourBytePacket
 * </ul>
 * On top of those are built various special-purpose packet formats.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "null returned is documented in each method to mean no valid result")
public class NmraPacket {

    static final public int accIdLowLimit = 1;
    static final public int accIdHighLimit = 2044;
    static final public int accIdAltHighLimit = 2048;

    /**
     * Create a packet containing a decoder idle instruction.
     *
     * @return the packet as a byte array or null if the address is not valid
     */
    @CheckForNull
    public static byte[] idlePacket() {
        byte[] retVal;
        retVal = new byte[3];
        retVal[0] = (byte) (0xFF);  // address byte for decoder idle
        retVal[1] = (byte) (0);     // decoder idle instruction
        retVal[2] = (byte) (0xFF);  // checksum byte
        return retVal;
    }

    /**
     * Create a packet containing a one-byte instruction.
     *
     * @param address  the address to send the instruction to
     * @param longAddr true if address is long, false otherwise
     * @param byte1    the byte to send as an instruction
     * @return the packet as a byte array or null if the address is not valid
     */
    @CheckForNull
    public static byte[] oneBytePacket(int address, boolean longAddr, byte byte1) {
        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        // end sanity check, format output
        byte[] retVal;
        if (longAddr) {
            // long address form
            retVal = new byte[4];
            retVal[0] = (byte) (192 + ((address / 256) & 0x3F));
            retVal[1] = (byte) (address & 0xFF);
            retVal[2] = byte1;
            retVal[3] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2]);
        } else {
            // short address form
            retVal = new byte[3];
            retVal[0] = (byte) (address & 0xFF);
            retVal[1] = byte1;
            retVal[2] = (byte) (retVal[0] ^ retVal[1]);
        }
        return retVal;
    }

    /**
     * Create a packet containing a two-byte instruction.
     *
     * @param address  the address to send the instruction to
     * @param longAddr true if address is long, false otherwise
     * @param byte1    first byte in the instruction
     * @param byte2    second byte in the instruction
     * @return the packet as a byte array or null if the address is not valid
     */
    @CheckForNull
    public static byte[] twoBytePacket(int address, boolean longAddr, byte byte1, byte byte2) {
        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        // end sanity check, format output
        byte[] retVal;
        if (longAddr) {
            // long address form
            retVal = new byte[5];
            retVal[0] = (byte) (192 + ((address / 256) & 0x3F));
            retVal[1] = (byte) (address & 0xFF);
            retVal[2] = byte1;
            retVal[3] = byte2;
            retVal[4] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3]);
        } else {
            // short address form
            retVal = new byte[4];
            retVal[0] = (byte) (address & 0xFF);
            retVal[1] = byte1;
            retVal[2] = byte2;
            retVal[3] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2]);
        }
        return retVal;
    }

    /**
     * Create a packet containing a three-byte instruction.
     *
     * @param address  the address to send the instruction to
     * @param longAddr true if address is long, false otherwise
     * @param byte1    first byte in the instruction
     * @param byte2    second byte in the instruction
     * @param byte3    third byte in the instruction
     * @return the packet as a byte array or null if the address is not valid
     */
    @CheckForNull
    public static byte[] threeBytePacket(int address, boolean longAddr, byte byte1, byte byte2, byte byte3) {
        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        // end sanity check, format output
        byte[] retVal;
        if (longAddr) {
            // long address form
            retVal = new byte[6];
            retVal[0] = (byte) (192 + ((address / 256) & 0x3F));
            retVal[1] = (byte) (address & 0xFF);
            retVal[2] = byte1;
            retVal[3] = byte2;
            retVal[4] = byte3;
            retVal[5] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3] ^ retVal[4]);
        } else {
            // short address form
            retVal = new byte[5];
            retVal[0] = (byte) (address & 0xFF);
            retVal[1] = byte1;
            retVal[2] = byte2;
            retVal[3] = byte3;
            retVal[4] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3]);
        }
        return retVal;
    }

    /**
     * Create a packet containing a four-byte instruction.
     *
     * @param address  the address to send the instruction to
     * @param longAddr true if address is long, false otherwise
     * @param byte1    first byte in the instruction
     * @param byte2    second byte in the instruction
     * @param byte3    third byte in the instruction
     * @param byte4    forth byte in the instruction
     * @return the packet as a byte array or null if the address is not valid
     */
    @CheckForNull
    public static byte[] fourBytePacket(int address, boolean longAddr, byte byte1, byte byte2, byte byte3, byte byte4) {
        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        // end sanity check, format output
        byte[] retVal;
        if (longAddr) {
            // long address form
            retVal = new byte[7];
            retVal[0] = (byte) (192 + ((address / 256) & 0x3F));
            retVal[1] = (byte) (address & 0xFF);
            retVal[2] = byte1;
            retVal[3] = byte2;
            retVal[4] = byte3;
            retVal[5] = byte4;
            retVal[6] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3] ^ retVal[4] ^ retVal[5]);
        } else {
            // short address form
            retVal = new byte[6];
            retVal[0] = (byte) (address & 0xFF);
            retVal[1] = byte1;
            retVal[2] = byte2;
            retVal[3] = byte3;
            retVal[4] = byte4;
            retVal[5] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3] ^ retVal[4]);
        }
        return retVal;
    }

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
        if (addr < 1 || addr > 511) {
            log.error("invalid address {}", addr);
            //return null;
            throw new IllegalArgumentException();
        }
        if (active < 0 || active > 1) {
            log.error("invalid active (C) bit {}", addr);
            return null;
        }
        if (outputChannel < 0 || outputChannel > 7) {
            log.error("invalid output channel {}", addr);
            return null;
        }

        int lowAddr = addr & 0x3F;
        int highAddr = ((~addr) >> 6) & 0x07;

        byte[] retVal = new byte[3];

        retVal[0] = (byte) (0x80 | lowAddr);
        retVal[1] = (byte) (0x80 | (highAddr << 4) | (active << 3) | outputChannel & 0x07);
        retVal[2] = (byte) (retVal[0] ^ retVal[1]);

        return retVal;
    }

    /**
     * Provide a basic operations mode accessory CV programming packet.
     * <br><br>
     * From the NMRA Standard: Basic Accessory Decoder Packet address for
     * operations mode programming
     * <br><br>
     * 10AAAAAA 0 1AAACDDD
     * <br><br>
     * Where DDD is used to indicate the output whose CVs are being modified and
     * C=1.
     * <br>
     * If CDDD= 0000 then the CVs refer to the entire decoder.
     * <br><br>
     * The resulting packet would be
     * <br><br>
     * {preamble} 10AAAAAA 0 1AAACDDD 0 (1110CCVV 0 VVVVVVVV 0 DDDDDDDD) 0
     * EEEEEEEE 1
     *
     * @param addr          the decoder address
     * @param active        1 or 0
     * @param outputChannel the output on the accessory
     * @param cvNum         the CV
     * @param data          the data
     * @return a packet
     */
    public static byte[] accDecoderPktOpsMode(int addr, int active, int outputChannel, int cvNum, int data) {

        if (addr < 1 || addr > 511) {
            log.error("invalid address {}", addr);
            throw new IllegalArgumentException();
        }
        if (active < 0 || active > 1) {
            log.error("invalid active (C) bit {}", addr);
            return null;
        }
        if (outputChannel < 0 || outputChannel > 7) {
            log.error("invalid output channel {}", addr);
            return null;
        }

        if (cvNum < 1 || cvNum > 1024) {
            log.error("invalid CV number {}", cvNum);
            return null;
        }

        if (data < 0 || data > 255) {
            log.error("invalid data {}", data);
            return null;
        }

        int lowAddr = addr & 0x3F;
        int highAddr = ((~addr) >> 6) & 0x07;
//        log.info("addr = {} active = {} outputChannel = {} cvNum = {} data = {}", addr, active, outputChannel, cvNum, data);
//        log.info("hex lowAddr = {} highAddr = {}", String.format("%H", lowAddr), String.format("%H", highAddr));
//        log.info("lowAddr = {} highAddr = {}", lowAddr, highAddr);

        int lowCVnum = (cvNum - 1) & 0xFF;
        int highCVnum = ((cvNum - 1) >> 8) & 0x03;

        byte[] retVal = new byte[6];
        retVal[0] = (byte) (0x80 | lowAddr);
        retVal[1] = (byte) (0x80 | (highAddr << 4) | (active << 3) | outputChannel & 0x07);
        retVal[2] = (byte) (0xEC | highCVnum);
        retVal[3] = (byte) (lowCVnum);
        retVal[4] = (byte) (0xFF & data);
        retVal[5] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3] ^ retVal[4]);

        return retVal;
    }

    /**
     * Provide a legacy operations mode accessory CV programming packet via a
     * simplified interface, given a decoder address.
     * <br><br>
     * From the NMRA Standard: The format for Accessory Decoder Configuration
     * Variable Access Instructions is: {preamble} 0 10AAAAAA 0 0AAA11VV 0
     * VVVVVVVV 0 DDDDDDDD 0 EEEEEEEE 1 Where: A = Decoder address bits V =
     * Desired CV address - (CV 513 = 10 00000000) D = Data for CV
     * <br><br>
     * This is the old "legacy" format, newer decoders use the "Basic Accessory
     * Decoder Packet"
     *
     * @param decAddr Address of decoder, in the range 1 to 511
     * @param cvNum   the CV
     * @param data    the data
     * @return a packet
     */
    public static byte[] accDecPktOpsModeLegacy(int decAddr, int cvNum, int data) {

        if (decAddr < 1 || decAddr > 511) {
            log.error("invalid address {}", decAddr);
            return null;
        }

        if (cvNum < 1 || cvNum > 1024) {
            log.error("invalid CV number {}", cvNum);
            return null;
        }

        if (data < 0 || data > 255) {
            log.error("invalid data {}", data);
            return null;
        }

        int lowAddr = decAddr & 0x3F;
        int highAddr = ((~decAddr) >> 6) & 0x07;

        int lowCVnum = (cvNum - 1) & 0xFF;
        int highCVnum = ((cvNum - 1) >> 8) & 0x03;

        byte[] retVal = new byte[5];
        retVal[0] = (byte) (0x80 | lowAddr);
        retVal[1] = (byte) (0x0C | (highAddr << 4) | highCVnum);
        retVal[2] = (byte) (lowCVnum);
        retVal[3] = (byte) (0xFF & data);
        retVal[4] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3]);

        return retVal;
    }

    /**
     * Create a signal accessory instruction packet.
     * <p>
     * From the RP: Extended Accessory Decoder Control Packet Format The
     * Extended Accessory Decoder Control Packet is included for the purpose of
     * transmitting aspect control to signal decoders or data bytes to more
     * complex accessory decoders. Each signal head can display one aspect at a
     * time.
     * <p>
     * {@code {preamble} 0 10AAAAAA 0 0AAA0AA1 0 000XXXXX 0 EEEEEEEE 1}
     * <p>
     * XXXXX is for a single head. A value of 00000 for XXXXX indicates the
     * absolute stop aspect. All other aspects represented by the values for
     * XXXXX are determined by the signaling system used and the prototype being
     * modeled.
     * <p>
     * Despite this being an NMRA standard, or perhaps because of it, the
     * addressing is not clear. The other form of packet generated by
     * {@link #altAccSignalDecoderPkt(int, int)} seems to be the one thats
     * generally supported by hardware.
     *
     * @param outputAddr Address of accessory output, starting with 1 and a
     *                   maximum of 2044
     * @param aspect     Aspect Number starting with 0 and a maximum of 31
     * @return the instruction packet
     */
    public static byte[] accSignalDecoderPkt(int outputAddr, int aspect) {

        if (outputAddr < accIdLowLimit || outputAddr > accIdHighLimit) {
            log.error("invalid signal decoder address {}", outputAddr);
            return null;
        }

        outputAddr -= 1; // Make the address 0 based
        int lowAddr = (outputAddr & 0x03);  // Output Pair Address
        int boardAddr = (outputAddr >> 2) + 1; // Board Address

        return accSignalDecoderPktCommon(lowAddr, boardAddr, aspect);
    }

    /**
     * Provide an extended operations mode accessory CV programming packet via a
     * simplified interface, given a signal address.
     * <br><br>
     * From the NMRA Standard: Extended Decoder Packet address for operations
     * mode programming
     * <br><br>
     * 10AAAAAA 0 0AAA0AA1
     * <br><br>
     * <br>
     * The resulting packet would be
     * <br><br>
     * {preamble} 10AAAAAA 0 0AAA0AA1 0 (1110CCVV 0 VVVVVVVV 0 DDDDDDDD) 0
     * EEEEEEEE 1
     *
     * @param addr  the signal address
     * @param cvNum the CV
     * @param data  the data
     * @return a packet
     */
    public static byte[] accSignalDecoderPktOpsMode(int addr, int cvNum, int data) {

        if (addr < 1 || addr > 2044) {
            log.error("invalid address {}", addr);
            throw new IllegalArgumentException();
        }

        if (cvNum < 1 || cvNum > 1024) {
            log.error("invalid CV number {}", cvNum);
            return null;
        }

        if (data < 0 || data > 255) {
            log.error("invalid data {}", data);
            return null;
        }

        int outputAddr = addr - 1; // Make the address 0 based
        int lowAddr = (outputAddr & 0x03);
        int boardAddr = (outputAddr >> 2) + 1; // Board Address
        int midAddr = (boardAddr & 0x3F);
        int highAddr = (~(boardAddr >> 6)) & 0x07;

        int lowCVnum = (cvNum - 1) & 0xFF;
        int highCVnum = ((cvNum - 1) >> 8) & 0x03;

        byte[] retVal = new byte[6];
        retVal[0] = (byte) (0x80 | midAddr);
        retVal[1] = (byte) (0x01 | (highAddr << 4) | (lowAddr << 1));
        retVal[2] = (byte) (0xEC | highCVnum);
        retVal[3] = (byte) (lowCVnum);
        retVal[4] = (byte) (0xFF & data);
        retVal[5] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3] ^ retVal[4]);

        return retVal;
    }

    /**
     * Determine if a packet is an Extended Accessory Decoder Control Packet
     * otherwise known as a Signal Decoder Packet.
     * <p>
     * This inverts the computation done by the
     * {@link #accSignalDecoderPkt(int, int)} method.
     *
     * @param packet a DCC packet to inspect
     * @return true if a Signal Decoder Packet; false otherwise
     */
    public static boolean isAccSignalDecoderPkt(byte[] packet) {
        if (packet == null || packet.length != 3 && packet.length != 4) {
            return false;   // allow ECC to be present or not
        }
        if ((packet[0] & 0xC0) != 0x80) {
            return false;
        }
        if ((packet[1] & 0x01) != 0x01) {
            return false;
        }
        if ((packet[2] & 0xE0) != 0x00) {
            return false;
        }
        return true;
    }

    /**
     * Determine if a packet is a Basic Accessory Decoder Packet address for
     * operations mode programming.
     * <p>
     * This inverts the computation done by the
     * {@link #accDecPktOpsMode(int, int, int)} method.
     *
     * @param packet the packet to test
     * @return true if the packet is a basic accessory decoder packet address
     */
    public static boolean isAccDecoderPktOpsMode(byte[] packet) {
        if (packet.length != 5 && packet.length != 6) {
            return false;   // allow ECC to be present or not
        }
        if ((packet[0] & 0xC0) != 0x80) {
            return false;
        }
        if (((packet[1] & 0x88) != 0x88) && ((packet[1] & 0x8F) != 0x80)) {
            return false;
        }
        if ((packet[2] & 0xFC) != 0xEC) {
            return false;
        }
        return true;
    }

    /**
     * Determine if a packet is a Legacy Accessory Decoder Packet address for
     * operations mode programming.
     * <p>
     * This inverts the computation done by the
     * {@link #accDecoderPktOpsModeLegacy(int, int, int)} method.
     *
     * @param packet the packet to extract the address from
     * @return the address
     */
    public static boolean isAccDecoderPktOpsModeLegacy(byte[] packet) {
        if (packet.length != 4 && packet.length != 5) {
            return false;   // allow ECC to be present or not
        }
        if ((packet[0] & 0xC0) != 0x80) {
            return false;
        }
        if ((packet[1] & 0x8C) != 0x0C) {
            return false;
        }
        return true;
    }

    /**
     * Recover the decoder address from a Legacy Accessory Decoder Packet Ops
     * Mode Packet.
     *
     * @param packet the packet to extract the address from
     * @return the decoder address
     */
    public static int getAccDecPktOpsModeLegacyAddress(byte[] packet) {
        int midAddr = packet[0] & 0x3f;
        int hiAddr = ((~packet[1]) & 0x70) >> 4;

        return (hiAddr << 6 | midAddr);
    }

    /**
     * Recover the equivalent accessory address from a Legacy Accessory Decoder
     * Packet Ops Mode Packet.
     *
     * @param packet the packet to extract the address from
     * @return the accessory address
     */
    public static int getAccDecoderPktOpsModeLegacyAddress(byte[] packet) {
        int midAddr = packet[0] & 0x3f;
        int hiAddr = ((~packet[1]) & 0x70) >> 4;

        int boardAddr = (hiAddr << 6 | midAddr) - 1;

        return ((boardAddr << 2)) + 1;
    }

    /**
     * Recover the accessory address from a Basic Accessory Decoder Packet Ops
     * Mode Packet.
     *
     * @param packet the packet to extract the address from
     * @return the accessory address
     */
    public static int getAccDecoderPktOpsModeAddress(byte[] packet) {
        int midAddr = packet[0] & 0x3f;
        int lowAddr = (packet[1] & 0x06) >> 1;
        int hiAddr = ((~packet[1]) & 0x70) >> 4;

        int boardAddr = (hiAddr << 6 | midAddr) - 1;

        return ((boardAddr << 2) | lowAddr) + 1;
    }

    /**
     * Recover the equivalent decoder address from a Basic Accessory Decoder
     * Packet Ops Mode Packet.
     *
     * @param packet the packet to extract the address from
     * @return the decoder address
     */
    public static int getAccDecPktOpsModeAddress(byte[] packet) {
        int lowAddr = packet[0] & 0x3f;
        int hiAddr = ((~packet[1]) & 0x70) >> 4;

        return (hiAddr << 6 | lowAddr);
    }

    /**
     * Recover the 1-based output address from an Extended Accessory Decoder
     * Control Packet otherwise known as a Signal Decoder Packet.
     *
     * @param packet the packet to extract the address from
     * @return the address
     */
    public static int getAccSignalDecoderPktAddress(byte[] packet) {
        int midAddr = packet[0] & 0x3f;
        int lowAddr = (packet[1] & 0x0E) >> 1;
        int hiAddr = ((~packet[1]) & 0x70) >> 4;

        int boardAddr = (hiAddr << 6 | midAddr) - 1;

        return ((boardAddr << 2) | lowAddr) + 1;
    }

    /**
     * An alternative interpretation of RP-9.2.1 due to an omission in the
     * address definition of extended accessory packets. Since there is no such
     * description for the address bits of the Extended Accessory Decoder
     * Control Packet, this interpretation assumes that the least significant
     * bits of the extended packet type are still in bits 1 and 2 of byte two,
     * see Basic Accessory Packet.
     *
     * @param outputAddr Address of accessory output, starting with 1 and a
     *                   maximum of 2044
     * @param aspect     Aspect Number starting with 0 and a maximum of 31
     * @return a packet
     */
    public static byte[] altAccSignalDecoderPkt(int outputAddr, int aspect) {

        if (outputAddr < 1 || outputAddr > 2048) {
            log.error("invalid signal decoder address {}", outputAddr);
            return null;
        }

        outputAddr -= 1; // Make the address 0 based
        int lowAddr = (outputAddr & 0x03);  // Output Pair Address
        int boardAddr = (outputAddr >> 2); // Board Address

        return accSignalDecoderPktCommon(lowAddr, boardAddr, aspect);
    }

    /**
     * Provide an extended operations mode accessory CV programming packet via a
     * simplified interface, given a signal address, using the alternative
     * interpretation of S-9.2.1, due to an omission in the address definition
     * of extended accessory packets.
     *
     * @param addr  the signal address
     * @param cvNum the CV
     * @param data  the data
     * @return a packet
     */
    public static byte[] altAccSignalDecoderPktOpsMode(int addr, int cvNum, int data) {

        if (addr < 1 || addr > 2044) {
            log.error("invalid address {}", addr);
            throw new IllegalArgumentException();
        }

        if (cvNum < 1 || cvNum > 1024) {
            log.error("invalid CV number {}", cvNum);
            return null;
        }

        if (data < 0 || data > 255) {
            log.error("invalid data {}", data);
            return null;
        }

        int outputAddr = addr - 1; // Make the address 0 based
        int lowAddr = (outputAddr & 0x03);
        int boardAddr = (outputAddr >> 2); // Board Address
        int midAddr = (boardAddr & 0x3F);
        int highAddr = (~(boardAddr >> 6)) & 0x07;

        int lowCVnum = (cvNum - 1) & 0xFF;
        int highCVnum = ((cvNum - 1) >> 8) & 0x03;

        byte[] retVal = new byte[6];
        retVal[0] = (byte) (0x80 | midAddr);
        retVal[1] = (byte) (0x01 | (highAddr << 4) | (lowAddr << 1));
        retVal[2] = (byte) (0xEC | highCVnum);
        retVal[3] = (byte) (lowCVnum);
        retVal[4] = (byte) (0xFF & data);
        retVal[5] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3] ^ retVal[4]);

        return retVal;
    }

    protected static byte[] accSignalDecoderPktCommon(int lowAddr, int boardAddr, int aspect) {

        if (aspect < 0 || aspect > 31) {
            log.error("invalid signal decoder aspect {}", aspect);
            return null;
        }

        int midAddr = boardAddr & 0x3F;
        int highAddr = ((~boardAddr) >> 6) & 0x07;

        byte[] retVal = new byte[4];
        retVal[0] = (byte) (0x80 | midAddr);
        retVal[1] = (byte) (0x01 | (highAddr << 4) | (lowAddr << 1));
        retVal[2] = (byte) (0x1F & aspect);
        retVal[3] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2]);

        return retVal;
    }

    /**
     * Recover the 1-based output address from an Accessory Decoder Control
     * Packet, typically considered a turnout control packet
     *
     * @param packet the packet to get an address from
     * @return the accessory decoder address
     */
    public static int getAccDecoderPktAddress(byte[] packet) {
        // case turnout accessory decoder
        // from Alex Shepherd
        int boardAddress = (((~packet[1]) & 0x70) << 2) | (packet[0] & 0x3F);
        int outputAddress = packet[1] & 0x07;
        int outputIndex = outputAddress >> 1;
        return (((boardAddress - 1) << 2) | outputIndex) + 1;
    }

    /**
     * Provide an accessory control packet via a simplified interface
     *
     * @param number Address of accessory output, starting with 1
     * @param closed true if the output is to be configured to the "closed",
     *               a.k.a. the "normal" or "unset" position
     * @return a packet
     */
    public static byte[] accDecoderPkt(int number, boolean closed) {
        // dBit is the "channel" info, least 7 bits, for the packet
        // The lowest channel bit represents CLOSED (1) and THROWN (0)
        int dBits = (((number - 1) & 0x03) << 1);  // without the low CLOSED vs THROWN bit
        dBits = closed ? (dBits | 1) : dBits;

        // aBits is the "address" part of the nmra packet, which starts with 1
        // 07/01/05 R.Scheffler - Removed the mask, this will allow any 'too high' numbers
        // through to accDecoderPkt() above which will log the error if out of bounds. If we
        // mask it here, then the number will 'wrap' without any indication that it did so.
        int aBits = (number - 1) >> 2;      // Divide by 4 to get the 'base'
        aBits += 1;                       // Base is +1

        // cBit is the control bit, we're always setting it active
        int cBit = 1;

        // get the packet
        return NmraPacket.accDecoderPkt(aBits, cBit, dBits);
    }

    /**
     * Provide a basic operations mode accessory CV programming packet via a
     * simplified interface, given an accessory address.
     * <br><br>
     *
     * @param accAddr Address of accessory, in the range 1 to 2044
     * @param cvNum   CV number to access
     * @param data    Data to be written
     * @return a packet
     */
    public static byte[] accDecoderPktOpsMode(int accAddr, int cvNum, int data) {
        // dBit is the "channel" info, least 7 bits, for the packet
        // The lowest channel bit represents CLOSED (1) and THROWN (0)
        int dBits = (((accAddr - 1) & 0x03) << 1) | 1;  // assume CLOSED

        // aBits is the "address" part of the nmra packet, which starts with 1
        int aBits = (accAddr - 1) >> 2;      // Divide by 4 to get the 'base'
        aBits += 1;                       // Base is +1

        // cBit is the control bit, we're always setting it active
        int cBit = 1;

        // get the packet
        return NmraPacket.accDecoderPktOpsMode(aBits, cBit, dBits, cvNum, data);
    }

    /**
     * Provide a basic operations mode accessory CV programming packet via a
     * simplified interface, given a decoder address.
     * <br><br>
     * From the NMRA Standard: Basic Accessory Decoder Packet address for
     * operations mode programming
     * <br><br>
     * 10AAAAAA 0 1AAACDDD
     * <br><br>
     * Where DDD is used to indicate the output whose CVs are being modified and
     * C=1.
     * <br>
     * If CDDD= 0000 then the CVs refer to the entire decoder.
     * <br><br>
     * Hence this method uses CDDD= 0000.
     * <br><br>
     * For programming individual outputs use
     * {@link #accDecoderPktOpsMode(int accAddr, int cvNum, int data)}
     * <br><br>
     *
     * @param decAddr Address of decoder, in the range 1 to 511
     * @param cvNum   CV number to access
     * @param data    Data to be written
     * @return a packet
     */
    public static byte[] accDecPktOpsMode(int decAddr, int cvNum, int data) {
        // dBit is the "channel" info, least 7 bits, for the packet
        // The lowest channel bit represents CLOSED (1) and THROWN (0)
        int dBits = 0;  // dBits is the "channel" info, CDDD= 0000 indicates the entire decoder

        // aBits is the "address" part of the nmra packet, which starts with 1
        int aBits = decAddr;

        // cBit is the control bit, CDDD= 0000 indicates the entire decoder
        int cBit = 0;

        // get the packet
        return NmraPacket.accDecoderPktOpsMode(aBits, cBit, dBits, cvNum, data);
    }

    /**
     * Provide a legacy operations mode accessory CV programming packet via a
     * simplified interface, given an accessory address.
     * <br><br>
     * From the NMRA Standard: The format for Accessory Decoder Configuration
     * Variable Access Instructions is: {preamble} 0 10AAAAAA 0 0AAA11VV 0
     * VVVVVVVV 0 DDDDDDDD 0 EEEEEEEE 1 Where: A = Decoder address bits V =
     * Desired CV address - (CV 513 = 10 00000000) D = Data for CV
     * <br><br>
     * This is the old "legacy" format, newer decoders use the "Basic Accessory
     * Decoder Packet"
     *
     * @param accAddr Address of accessory, in the range 1 to 2044
     * @param cvNum   CV number to access
     * @param data    Data to be written
     * @return a packet
     */
    public static byte[] accDecoderPktOpsModeLegacy(int accAddr, int cvNum, int data) {

        // aBits is the "address" part of the nmra packet, which starts with 1
        int aBits = (accAddr - 1) >> 2;      // Divide by 4 to get the 'base'
        aBits += 1;                       // Base is +1

        // get the packet
        return NmraPacket.accDecPktOpsModeLegacy(aBits, cvNum, data);
    }

    public static byte[] opsCvWriteByte(int address, boolean longAddr, int cvNum, int data) {
        log.debug("opswrite {} {} {}", address, cvNum, data);

        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        if (data < 0 || data > 255) {
            log.error("invalid data {}", data);
            return null;
        }
        if (cvNum < 1 || cvNum > 1024) {
            log.error("invalid CV number {}", cvNum);
            return null;
        }

        // end sanity checks, format output
        int arg1 = 0xEC + (((cvNum - 1) >> 8) & 0x03);
        int arg2 = (cvNum - 1) & 0xFF;
        int arg3 = data & 0xFF;

        return NmraPacket.threeBytePacket(address, longAddr, (byte) arg1, (byte) arg2, (byte) arg3);
    }

    public static byte[] speedStep128Packet(int address, boolean longAddr, int speed, boolean fwd) {
        log.debug("128 step packet {} {}", address, speed);

        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        if (speed < 0 || speed > 127) {
            log.error("invalid speed {}", speed);
            return null;
        }

        // end sanity checks, format output
        byte[] retVal;
        int arg1 = 0x3F;
        int arg2 = (speed & 0x7F) | (fwd ? 0x80 : 0);

        if (longAddr) {
            // long address form
            retVal = new byte[5];
            retVal[0] = (byte) (192 + ((address / 256) & 0x3F));
            retVal[1] = (byte) (address & 0xFF);
            retVal[2] = (byte) arg1;
            retVal[3] = (byte) arg2;
            retVal[4] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3]);
        } else {
            // short address form
            retVal = new byte[4];
            retVal[0] = (byte) (address & 0xFF);
            retVal[1] = (byte) arg1;
            retVal[2] = (byte) arg2;
            retVal[3] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2]);
        }
        return retVal;
    }

    /**
     * From NMRA RP 9.2.1 [A Crosland 05/02/12] There is an issue with this
     * method in that it cannot create a 28 step speed packet for maximum speed.
     * Input speed value in the range 0 - 28 is converted to speed steps, 0,
     * estop, 1, 2, ..., 27.
     * <p>
     * This method should probably be deprecated. It is used only by
     * NceThrottle.java and EasyDccThrottle.java which themselves have issues in
     * the way floating point speed values are converted to integer speed steps.
     * <p>
     * A speed and direction instruction is used send information to motors
     * connected to Multi Function Digital Decoders. Instruction "010" indicates
     * a Speed and Direction Instruction for reverse operation and instruction
     * "011" indicates a Speed and Direction Instruction for forward operation.
     * In these instructions the data is used to control speed with bits 0-3
     * being defined exactly as in S-9.2 Section B. If Bit 1 of CV#29 has a
     * value of one (1), then bit 4 is used as an intermediate speed step, as
     * defined in S-9.2, Section B. If Bit 1 of CV#29 has a value of zero (0),
     * then bit 4 shall 230 be used to control FL4. In this mode, Speed U0000 is
     * stop, speed U0001 is emergency stop, speed U0010 is the first speed step
     * and speed U1111 is full speed. This provides 14 discrete speed steps in
     * each direction.
     *
     * @param address  the DCC locomotive address
     * @param longAddr true if the address is long; false if short
     * @param speed    the speed from 0-28
     * @param fwd      true for forward direction; false for reverse
     * @return the instruction or null if address or speed is invalid
     */
    public static byte[] speedStep28Packet(int address, boolean longAddr, int speed, boolean fwd) {
        log.debug("28 step packet {} {}", address, speed);

        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        if (speed < 0 || speed > 28) {
            log.error("invalid speed {}", speed);
            return null;
        }
        int speedC = (speed & 0x1F) >> 1;
        if (speed > 0) {
            speedC = speedC + 1;
        }
        int c = (speed & 0x01) << 4; // intermediate speed step

        speedC = speedC + c;

        // end sanity checks, format output
        int arg1 = (fwd ? 0x60 : 0x40) | speedC;

        return NmraPacket.oneBytePacket(address, longAddr, (byte) arg1);
    }

    /**
     * New version of speedStep28Packet to allow access to the whole range of 28
     * step speed packets.
     * <p>
     * Simply constructs a packet using the 5 bit speed value. This is
     * consistent with the 128 and 14 step methods which do no further
     * processing of the speed value.
     *
     * @param full     must be true
     * @param address  DCC address
     * @param longAddr true if DCC address is long; false if short
     * @param speed    speed step value 0 - 31 for insertion into DC packet
     * @param fwd      true for forward direction; false for reverse
     * @return the instruction or null if address or speed is invalid
     */
    @CheckForNull
    public static byte[] speedStep28Packet(boolean full, int address, boolean longAddr, int speed, boolean fwd) {
        log.debug("28 step packet {} {}", address, speed);

        if (!full) {
            log.error("invalid method invocation");
            return null;    // failed!
        }

        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        if (speed < 0 || speed > 31) {
            log.error("invalid speed {}", speed);
            return null;
        }
        int speedC = (speed & 0x1F) >> 1;
        int c = (speed & 0x01) << 4; // intermediate speed step

        speedC = speedC + c;

        // end sanity checks, format output
        int arg1 = (fwd ? 0x60 : 0x40) | speedC;

        return NmraPacket.oneBytePacket(address, longAddr, (byte) arg1);
    }

    public static byte[] speedStep14Packet(int address, boolean longAddr,
            int speed, boolean fwd, boolean F0) {
        log.debug("14 step packet {} {} {}", address, speed, F0);

        if (speed < 0 || speed > 15) {
            log.error("invalid speed {}", speed);
            return null;
        }

        int speedC = (speed & 0xF);

        if (F0) {
            speedC = speedC + 0x10;
        }

        // end sanity checks, format output
        byte[] retVal;
        int arg1 = (fwd ? 0x60 : 0x40) | speedC;

        if (longAddr) {
            // long address form
            retVal = new byte[4];
            retVal[0] = (byte) (192 + ((address / 256) & 0x3F));
            retVal[1] = (byte) (address & 0xFF);
            retVal[2] = (byte) arg1;
            retVal[3] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3]);
        } else {
            // short address form
            retVal = new byte[3];
            retVal[0] = (byte) (address & 0xFF);
            retVal[1] = (byte) arg1;
            retVal[2] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2]);
        }

        return retVal;
    }

    public static byte[] function0Through4Packet(int address, boolean longAddr,
            boolean f0, boolean f1, boolean f2, boolean f3, boolean f4) {
        log.debug("f0 through f4 packet {}", address);

        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        // end sanity check, format output
        byte[] retVal;
        int arg1 = 0x80
                | (f0 ? 0x10 : 0)
                | (f1 ? 0x01 : 0)
                | (f2 ? 0x02 : 0)
                | (f3 ? 0x04 : 0)
                | (f4 ? 0x08 : 0);

        if (longAddr) {
            // long address form
            retVal = new byte[4];
            retVal[0] = (byte) (192 + ((address / 256) & 0x3F));
            retVal[1] = (byte) (address & 0xFF);
            retVal[2] = (byte) arg1;
            retVal[3] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2]);
        } else {
            // short address form
            retVal = new byte[3];
            retVal[0] = (byte) (address & 0xFF);
            retVal[1] = (byte) arg1;
            retVal[2] = (byte) (retVal[0] ^ retVal[1]);
        }
        return retVal;
    }

    public static byte[] function5Through8Packet(int address, boolean longAddr,
            boolean f5, boolean f6, boolean f7, boolean f8) {
        log.debug("f5 through f8 packet {}", address);

        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        // end sanity check, format output
        byte[] retVal;
        int arg1 = 0xB0
                | (f8 ? 0x08 : 0)
                | (f7 ? 0x04 : 0)
                | (f6 ? 0x02 : 0)
                | (f5 ? 0x01 : 0);

        if (longAddr) {
            // long address form
            retVal = new byte[4];
            retVal[0] = (byte) (192 + ((address / 256) & 0x3F));
            retVal[1] = (byte) (address & 0xFF);
            retVal[2] = (byte) arg1;
            retVal[3] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2]);
        } else {
            // short address form
            retVal = new byte[3];
            retVal[0] = (byte) (address & 0xFF);
            retVal[1] = (byte) arg1;
            retVal[2] = (byte) (retVal[0] ^ retVal[1]);
        }
        return retVal;
    }

    public static byte[] function9Through12Packet(int address, boolean longAddr,
            boolean f9, boolean f10, boolean f11, boolean f12) {
        log.debug("f9 through f12 packet {}", address);

        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        // end sanity check, format output
        byte[] retVal;
        int arg1 = 0xA0
                | (f12 ? 0x08 : 0)
                | (f11 ? 0x04 : 0)
                | (f10 ? 0x02 : 0)
                | (f9 ? 0x01 : 0);

        if (longAddr) {
            // long address form
            retVal = new byte[4];
            retVal[0] = (byte) (192 + ((address / 256) & 0x3F));
            retVal[1] = (byte) (address & 0xFF);
            retVal[2] = (byte) arg1;
            retVal[3] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2]);
        } else {
            // short address form
            retVal = new byte[3];
            retVal[0] = (byte) (address & 0xFF);
            retVal[1] = (byte) arg1;
            retVal[2] = (byte) (retVal[0] ^ retVal[1]);
        }
        return retVal;
    }

    public static byte[] function13Through20Packet(int address, boolean longAddr,
            boolean f13, boolean f14, boolean f15, boolean f16,
            boolean f17, boolean f18, boolean f19, boolean f20) {
        log.debug("f13 through f20 packet {}", address);

        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        // end sanity check, format output
        int arg1 = 0xDE;
        int arg2 = (f20 ? 0x80 : 0)
                | (f19 ? 0x40 : 0)
                | (f18 ? 0x20 : 0)
                | (f17 ? 0x10 : 0)
                | (f16 ? 0x08 : 0)
                | (f15 ? 0x04 : 0)
                | (f14 ? 0x02 : 0)
                | (f13 ? 0x01 : 0);

        return NmraPacket.twoBytePacket(address, longAddr, (byte) arg1, (byte) arg2);
    }

    public static byte[] function21Through28Packet(int address, boolean longAddr,
            boolean f21, boolean f22, boolean f23, boolean f24,
            boolean f25, boolean f26, boolean f27, boolean f28) {
        log.debug("f21 through f28 packet {}", address);

        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        // end sanity check, format output
        int arg1 = 0xDF;
        int arg2 = (f28 ? 0x80 : 0)
                | (f27 ? 0x40 : 0)
                | (f26 ? 0x20 : 0)
                | (f25 ? 0x10 : 0)
                | (f24 ? 0x08 : 0)
                | (f23 ? 0x04 : 0)
                | (f22 ? 0x02 : 0)
                | (f21 ? 0x01 : 0);

        return NmraPacket.twoBytePacket(address, longAddr, (byte) arg1, (byte) arg2);
    }

    /**
     * Provide an NMRA analog control instruction.
     * <p>
     * Note that the NMRA draft of Fall 2004 only defines the value of "1" for
     * the "function parameter", calling that the value for "volume control".
     * However, DCC systems in the wild have been observed to use 0x7F for the
     * function byte for volume control.
     *
     * @param address  DCC locomotive address
     * @param longAddr true if this is a long address, false if short address
     * @param function see note above
     * @param value    value to be sent in analog control instruction
     * @return the instruction or null if the address is not valid
     */
    public static byte[] analogControl(int address, boolean longAddr,
            int function, int value) {

        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        }

        // end sanity check, format output
        byte[] retVal;
        int arg1 = 0x3D;  // analog instruction tag

        if (longAddr) {
            // long address form
            retVal = new byte[6];
            retVal[0] = (byte) (192 + ((address / 256) & 0x3F));
            retVal[1] = (byte) (address & 0xFF);
            retVal[2] = (byte) arg1;
            retVal[3] = (byte) (function & 0xFF);
            retVal[4] = (byte) (value & 0xFF);
            retVal[5] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3] ^ retVal[4]);
        } else {
            // short address form
            retVal = new byte[5];
            retVal[0] = (byte) (address & 0xFF);
            retVal[1] = (byte) arg1;
            retVal[2] = (byte) (function & 0xFF);
            retVal[3] = (byte) (value & 0xFF);
            retVal[4] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3]);
        }
        return retVal;
    }

    /**
     * Provide an NMRA consist control instruction
     *
     * @param address         DCC locomotive address
     * @param longAddr        true for a long address, false if short address
     * @param consist         the consist address to set for this locomotive;
     *                        Send 00 as consist address to remove from consist
     * @param directionNormal true if the normal direction of travel for this
     *                        address is the normal direction of travel for the
     *                        consist
     * @return the instruction
     */
    public static byte[] consistControl(int address, boolean longAddr,
            int consist, boolean directionNormal) {

        if (!addressCheck(address, longAddr)) {
            return null;  // failed!
        } else if (!addressCheck(consist, false)) {
            return null;  // failed - Consist address is not a short address!
        }

        // end sanity check, format output
        byte[] retVal;
        int arg1 = 0x10;  // Consist Control instruction tag
        if (directionNormal) {
            arg1 |= 0x02;   // Forward Direction
        } else {
            arg1 |= 0x03;   // Reverse Direction
        }
        if (longAddr) {
            // long address form
            retVal = new byte[5];
            retVal[0] = (byte) (192 + ((address / 256) & 0x3F));
            retVal[1] = (byte) (address & 0xFF);
            retVal[2] = (byte) arg1;
            retVal[3] = (byte) (consist & 0xFF);
            retVal[4] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2] ^ retVal[3]);
        } else {
            // short address form
            retVal = new byte[4];
            retVal[0] = (byte) (address & 0xFF);
            retVal[1] = (byte) arg1;
            retVal[2] = (byte) (consist & 0xFF);
            retVal[3] = (byte) (retVal[0] ^ retVal[1] ^ retVal[2]);
        }
        return retVal;
    }

    /**
     * Check if an address is (possibly) valid, e.g. fits within the NMRA space
     * definition
     *
     * @param address  the address
     * @param longAddr true if address is long; false otherwise
     * @return true if address is valid; false otherwise
     */
    static public boolean addressCheck(int address, boolean longAddr) {
        if (address < 0) {  // zero is valid broadcast
            log.error("invalid address {}", address);
            return false;
        }
        if (longAddr && (address > (255 + (231 - 192) * 256))) {
            log.error("invalid address {}", address);
            return false;
        }
        if (!longAddr && (address > 127)) {
            log.error("invalid address {}", address);
            return false;
        }
        return true;  // passes test, hence OK
    }

    public enum DccAddressType {

        NO_ADDRESS,
        BROADCAST,
        IDLE,
        LOCO_SHORT_ADDRESS,
        LOCO_LONG_ADDRESS,
        ACCESSORY_ADDRESS;
    }

    /**
     * Extract the address type from an NMRA packet.
     * <p>
     * This finds and returns the type of address within a specific packet, e.g.
     * "the stationary decoder space".
     *
     * @param packet the packet
     * @return the type or {@link jmri.NmraPacket.DccAddressType#NO_ADDRESS}
     */
    static public DccAddressType extractAddressType(byte[] packet) {
        if (packet[0] == 0x00) {
            return DccAddressType.BROADCAST;
        }
        if ((packet[0] & 0xFF) == 0xFF) {
            return DccAddressType.IDLE;
        }
        if ((0x80 & packet[0]) == 0x00) {
            return DccAddressType.LOCO_SHORT_ADDRESS;
        }
        if ((0xC0 & packet[0]) == 0xC0) {
            return DccAddressType.LOCO_LONG_ADDRESS;
        }
        if ((0xC0 & packet[0]) == 0x80) {
            return DccAddressType.ACCESSORY_ADDRESS;
        }
        return DccAddressType.NO_ADDRESS;
    }

    /**
     * Extract the numerical address from an NMRA packet.
     * <p>
     * This finds and returns the numerical address within a specific type, e.g.
     * "first address within the stationary decoder space".
     * <p>
     * As a special case, IDLE is returned as -1 instead of 255. Best to check
     * the address type first....
     * <p>
     * <strong>Note:</strong> The decoding is not complete for the
     * ACCESSORY_ADDRESS type.
     *
     * @param packet the packet
     * @return the address; -1 is returned if there is no address or the case
     *         isn't considered yet
     */
    static public int extractAddressNumber(byte[] packet) {
        switch (extractAddressType(packet)) {
            case BROADCAST:
                return 0;
            case NO_ADDRESS:
            case IDLE:
                return -1;
            case LOCO_SHORT_ADDRESS:
                return packet[0] & 0xFF;
            case LOCO_LONG_ADDRESS:
                return (packet[0] & 0x3F) << 8 | (packet[1] & 0xFF);
            case ACCESSORY_ADDRESS:
                // case signal packet
                if (isAccSignalDecoderPkt(packet)) {
                    return getAccSignalDecoderPktAddress(packet);
                }

                // case turnout accessory decoder
                return getAccDecoderPktAddress(packet);
            default:
                log.error("Unhandled address type {}", extractAddressType(packet));
                break;
        }
        return -1;
    }

    /**
     * Extract the instruction from an NMRA packet.
     * <p>
     * This finds and returns the instruction byte within a specific type of
     * packet/instruction.
     *
     * @param packet the packet
     * @return the instruction or 0
     */
    static public int extractInstruction(byte[] packet) {
        switch (extractAddressType(packet)) {
            case BROADCAST:
            case NO_ADDRESS:
            case IDLE:
            case LOCO_SHORT_ADDRESS:
                return packet[1] & 0xFF;
            case LOCO_LONG_ADDRESS:
            case ACCESSORY_ADDRESS:
                return packet[2] & 0xFF;
            default:
                log.warn("Unhandled address type: {}", extractAddressType(packet));
        }
        return 0;
    }

    /**
     * Convert NMRA packet to a readable form as hexadecimal characters.
     *
     * @param p the raw packet
     * @return the readable packet
     * @see jmri.util.StringUtil#hexStringFromBytes(byte[])
     */
    static public String format(byte[] p) {
        return jmri.util.StringUtil.hexStringFromBytes(p);
    }

    /**
     * Convert NMRA packet to human-readable form
     * <p>
     * Note: Only gives a summary now, should this completely decode?
     * <p>
     * 2nd Note: The name may be a bad choice, as this is not the .toString()
     * method of an object, but rather a procedure that takes a byte-array
     * representation of a packet. But the analogy seems not so bad, until we
     * have a true class for NmraPackets.
     *
     * @param p the raw packet
     * @return the human-readable form for that packet
     * @throws IllegalArgumentException if packet array can't be decoded, e.g.
     *                                  is too short or null
     */
    static public String toString(byte[] p) throws IllegalArgumentException {
        if (p == null || p.length == 0) {
            throw new IllegalArgumentException("Content required");
        }
        return Bundle.getMessage("DccToStringFormat", extractAddressType(p), extractInstruction(p), extractAddressNumber(p));
    }

    /**
     * Objects of this class should not be created.
     */
    private NmraPacket() {
    }
    private final static Logger log = LoggerFactory.getLogger(NmraPacket.class);
}
