/**
 * MakePacket.java
 *
 * Description:		<describe the MakePacket class here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version
 */

package jmri.jmrix.direct;

/**
 * Provide utilities for coding/decoding NMRA S&RP DCC packets into
 * sequences to send through a standard serial port.
 *
 * This is strongly based (e.g. copied and translated) on the makepckt.c file
 * from the PacketScript 1.1. package of Kenneth Rice. The original header
 * comment from that file follows here.
 *
 */

 /* makepckt.c
 *
 * Send an nmra packet out the serial port in such a way that the signal can
 * just be amplified and put on the track.
 *
 * Copyright 1993 Kenneth Rice
 *
 * You may freely distribute this source code, and use all or part of
 * this source code in all software including commercial, freeware,
 * shareware and private applications.
 *
 * Please report bugs, fixes, etc to me at:
 *	kenr@xis.xerox.com
 * or
 *	73577,1653 (compuserve)
 *
 * Created	02/08/93
 * 			03/05/93	Works for all 3 byte packets. Still errors for 4 byte.
 *			07/01/93	Renamed to makepckt.c to be nice to dos users.
 *			10/23/93	Added backtracking and max length.
 */


public class MakePacket {

    static final int PREAMBLE_LENGTH = 15;		/* This should be a multiple of 5. */
    static final int MAX_PACKET_BYTES = 12;		/* Longest NMRA packet is 10 bytes. */
    static final int BITSTREAM_BITS_PER_BYTE = 9;	/* counts start bit */

    static final int MAX_BITS_IN_PACKET = (PREAMBLE_LENGTH
                                    + MAX_PACKET_BYTES * BITSTREAM_BITS_PER_BYTE
                                    + 10);		/* 1 stop bit, some spares. */


    /* nmra   s01234567s (hex equiv - note that in signal, 0 bit is left) */
    static final int BITS_0	  = 0xF0;	/* 0      _____----- (0xF0) */
    static final int BITS_00  = 0xC6;	/* 00     __--___--- (0xC6) */
    static final int BITS_01  = 0x78;	/* 01     ____----_- (0x78) */
    static final int BITS_10  = 0xE1;	/* 10     _-____---- (0xE1) */
    static final int BITS_001 = 0x66;	/* 001    __--__--_- (0x66) */
    static final int BITS_010 = 0x96;	/* 010    __--_-__-- (0x96) */
    static final int BITS_011 = 0x5C;	/* 011    ___---_-_- (0x5C) */
    static final int BITS_100 = 0x99;	/* 100    _-__--__-- (0x99) */
	static final int BITS_101 = 0x71;	/* 101    _-___---_- (0x71) */
	static final int BITS_110 = 0xC5;	/* 110    _-_-___--- (0xC5) */
	static final int BITS_0111  = 0x56;	/* 0111   __--_-_-_- (0x56) */
    static final int BITS_1011  = 0x59;	/* 1011   _-__--_-_- (0x59) */
    static final int BITS_1101  = 0x65;	/* 1101   _-_-__--_- (0x65) */
    static final int BITS_1110  = 0x95;	/* 1110   _-_-_-__-- (0x95) */
    static final int BITS_11111 = 0x55;	/* 11111  _-_-_-_-_- (0x55) */


	/* BuildPacketVA
	 *
	 * This routine generates a string of bytes to be sent out the serial port from
	 * the bytes that make up an nmra packet.
	 *
	 * Arguments:
	 *	pSerialBuf		- Where to put serial bytes.
	 *	pSerialBufCnt	- Where to put number of serial bytes put in pSerialBuf.
	 *  ...				- The bytes that make up the packet, terminated by a -1.
	 *
	 * The serial bytes should be sent out a serial port set to somewhere between
	 * 19.2K baud and 14.7K baud. 19.2K baud us necessary to program Lenz recievers.
	 * The serial port must be set to 8 data bits, 1 stop bit, and no parity.
	 *
	 * Example code fragment to call this function for a baseline packet:
	 *
	 * int	serialBuf[1000];
	 * int	serialBufCnt;
	 * int	addr = 3;
	 * int	speed = 0x75;
	 *
	 * if (BuildPacket(serialBuf, &serialBufCnt, addr, speed, addr^speed, -1) < 0)
	 * 		* handle error - should never happen for 3 byte packets. *
	 * else
	 *		* send bytes in serialBuf out serial port repeatedly. *
	 */
	//int BuildPacketVA(int[] pSerialBuf, int[] pSerialBufCnt,
        //                  int maxSerialBytes ) {
	//	int     	bytes[MAX_PACKET_BYTES];
	//	int				t;
	//	long			numBytes = 0;
	//	va_list			ap;
        //
	//	va_start(ap, maxSerialBytes);
	//	while ((t = va_arg(ap, int)) != -1) {
	//		bytes[numBytes] = t;
	//		numBytes++;
	//	}
	//	va_end(ap);
        //
	//	return BuildPacket(pSerialBuf, pSerialBufCnt, maxSerialBytes,bytes,numBytes);
	//}


	/* Same as BuildPacketVA above, except takes an array of numBytes packet bytes
	 * instead of the variable arguments. Called by BuildPacketVA to actually do
	 * the work.
	 *
	 * This function makes the bit stream from the packet bytes, and then calls
	 * BitStreamToSerialBytes to actually get the serial bytes.
	 *
	 * Returns 0 on success, -1 on failure.
	 */
    int BuildPacket(int[] pSerialBuf, int pSerialBufCnt[],  // secretly just one
										int maxSerialBytes,
										int[] pBytes, int numBytes) {
        int		i;
		int		bitsInStream = 0;
		int[] bitStream = new int[MAX_BITS_IN_PACKET];

		/* Make into an array of ints for easier processing. */
		/* do preamble */
		for (bitsInStream=0; bitsInStream<PREAMBLE_LENGTH; bitsInStream++)
			bitStream[bitsInStream] = 1;
		/* Do packet bytes. */
		for (i=0; i<numBytes; i++) {
			MakeBitValuesFromByte(bitStream, bitsInStream, pBytes[i]);
			bitsInStream += BITSTREAM_BITS_PER_BYTE;
		}
		/* do stop bit */
		bitStream[bitsInStream++] = 1;

		pSerialBufCnt[0] = 0;
		if (BitStreamToSerialBytes(pSerialBuf, pSerialBufCnt, maxSerialBytes,
												bitStream, bitsInStream) < 0) {
			return -1;		/* failed. */
		}
		return 0;
	}


	/* BitStreamToSerialBytes
	 *
	 * Generate the serial bytes from the bit stream.
	 *
	 * Bassically this is a depth first tree search, always going down the subtree
	 * that uses the most bits for the next byte. If we get an error, backtrack up
	 * the tree until we reach a node that we have not completely traversed all the
	 * subtrees for and try going down the subtree that uses the second most bits.
	 * Keep going until we finish converting the packet or run out of things to try.
	 *
	 * In addition to the above, we also backtrack if we hit maxSerialBytes
	 * bytes without having finished. This serves two purposes - first it
	 * guarantees we will not overflow the serial buffer, and second it lets
	 * you force a search for a packet that fits even if a longer one is
	 * found first.
	 *
	 * This is not guaranteed to find the shortest serial stream for a given
	 * packet, but it is guaranteed to find a stream if one exists. Also, it
	 * usually does come up with the shortest packet.
	 */
	static int BitStreamToSerialBytes(int[] pSerialBuf,
										int[] pSerialBufCnt,  // really just one value
										int maxSerialBytes,
										int bitStream[], int bitsInStream) {
		int		i;
		int		tempByte;
		int[]   used = new int[1];
		int 	serCnt = 0;

		/* Now generate the actual serial byte stream from the array of bits. */
		i = 0;
		while (i < bitsInStream) {
			if (serCnt >= maxSerialBytes)
				tempByte = -1;		/* serial buffer full - cause error to back up. */
			else if ((tempByte = TranslateByte(bitStream, i, bitsInStream-i, used)) > 0)
				pSerialBuf[serCnt++] = tempByte;
			else if (i+4 >= bitsInStream) {
				int		j;

				/* If whats left of bitstream is all 1's then add more
				 * ones so it converts without having to backtrack and
				 * possibly fail.
				 */
				tempByte = BITS_11111;
				for (j=i; j<bitsInStream; j++)
					if (bitStream[j]==0)
						tempByte = -1;
				if (tempByte > 0) {
					pSerialBuf[serCnt++] = tempByte;
					used[0] = 5;
				}
			}
			if (tempByte <= 0) {
				used[0] = 0;
				while (serCnt > 0)	{ /* The backup loop. */
					/* Back up one byte and see if there are alternatives for it.
					 * If so, break out of the backup loop and start down the new
					 * branch of the tree. If there are no alternatives, back up
					 * some more. For alternatives, strategy is to find something
					 * one bit shorter with the same leading bits.
					 */
					serCnt--;
					switch ( pSerialBuf[serCnt] ) {
						/* Success - back up one bit in input and go down another
					 	* subtree from this level.
					 	*/
						case BITS_00:		tempByte = BITS_0;		break;
						case BITS_01:		tempByte = BITS_0;		break;
						case BITS_001:		tempByte = BITS_00;		break;
						case BITS_010:		tempByte = BITS_01;		break;
						case BITS_011:		tempByte = BITS_01;		break;
						case BITS_100:		tempByte = BITS_10;		break;
						case BITS_0111:		tempByte = BITS_011;	break;
						case BITS_1011:		tempByte = BITS_101;	break;
						case BITS_1101:		tempByte = BITS_110;	break;
						/* We have exhausted all subtrees on this level, go up one
						 * level and try it's other subtrees.
						 */
						case BITS_0:		used[0] -= 1;		break;
						case BITS_10:		used[0] -= 2;		break;
						case BITS_101:		used[0] -= 3;		break;
						case BITS_110:		used[0] -= 3;		break;
						case BITS_1110:		used[0] -= 4;		break;
						case BITS_11111:	used[0] -= 5;		break;
					}
					if (tempByte > 0) {
						used[0] -= 1;		/* We backed up one bit. */
						pSerialBuf[serCnt++] = tempByte;
						break;				/* break out of the backup loop. */
					}
				}
				if (serCnt <= 0) {
					/* We traversed the entire tree with no luck. */
            		pSerialBufCnt[0] = 0;
		            return -1;
                }
			}
			i += used[0];
		}

		pSerialBufCnt[0] = serCnt;
		return 0;	/* Success */
	}



	/* Turn the byte into a stream of bits, preceeded by a zero start bit.
         * pBitStream[] is the output bit stream
         * offset is the starting index in the bit stream
	 */
	static void MakeBitValuesFromByte(int[] pBitStream, int offset, int inByte) {
		int		i;
		int		mask = 0x80;

		pBitStream[0+offset] = 0;
		for (i=1; i<BITSTREAM_BITS_PER_BYTE; i++) {
			pBitStream[i+offset] = (inByte & mask)!=0 ? 1 : 0;
			mask = (mask >> 1);
		}
	}


	/* TranslateByte
	 *
	 * This routine finds the serial byte that corresponds to the most possible
	 * bits from the head of the bit stream pointed to by pBS.
	 *
	 * pBS			- (INPUT) Bit stream array
     * offset       - (INPUT) index of the first bit to be used in the bit stream array
	 * validBits	- (INPUT) number of valid bits in the bit stream.
	 * pBitsUsed	- (OUTPUT) where to put number of bits converted from stream.
	 *
	 * Return -1 on error, otherwise serial byte.
	 */
	static int TranslateByte(int[] pBS, int offset, int validBits, int[] pBitsUsed) {
		boolean b0 = (pBS[0+offset]!=0);
        boolean b1 = (pBS[1+offset]!=0);
        boolean b2 = (pBS[2+offset]!=0);
        boolean b3 = (pBS[3+offset]!=0);
        boolean b4 = (pBS[4+offset]!=0);

		if (validBits <= 0)		/* just in case. */
			return -1;

		switch (validBits) {	/* Note all cases fall through on purpose. */
			default:
				pBitsUsed[0] = 5;
				if (b0 && b1 && b2 && b3 && b4)	return BITS_11111;
			case 4:
				pBitsUsed[0] = 4;
				if (!b0 && b1 && b2 && b3)		return BITS_0111;
				if (b0 && !b1 && b2 && b3)		return BITS_1011;
				if (b0 && b1 && !b2 && b3)		return BITS_1101;
				if (b0 && b1 && b2 && !b3)		return BITS_1110;
			case 3:
				pBitsUsed[0] = 3;
				if (! b0 && ! b1 && b2)		return BITS_001;
				if (! b0 && b1 && ! b2)		return BITS_010;
				if (! b0 && b1 && b2)		return BITS_011;
				if (b0 && ! b1 && ! b2)		return BITS_100;
				if (b0 && !b1 && b2)		return BITS_101;
				if (b0 && b1 && !b2)		return BITS_110;
			case 2:
				pBitsUsed[0] = 2;
				if (!b0 && !b1)	return BITS_00;
				if (!b0 && b1)		return BITS_01;
				if (b0 && !b1)		return BITS_10;
			case 1:
				pBitsUsed[0] = 1;
				if (b0)	return BITS_0;
		}

		return -1;
	}


	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MakePacket.class.getName());
}


/* @(#)NmraPacket.java */
