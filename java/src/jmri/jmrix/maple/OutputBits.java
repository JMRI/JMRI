package jmri.jmrix.maple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class supporting output to Maple HMI's
 * <P>
 * All of the Maple HMI panels receive the same output bits. This keeps them
 * synchronized. Output is sent in "broadcast" mode to Station Address 0. All
 * HMI's receive the output. Output is sent at the end of each polling cycle,
 * whether or not anything has changed. That way, if an HMI panel is plugged in,
 * it will be up-to-date within one polling cycle. Serial systems with unique
 * output bits for each node keep their output array in each node. That code has
 * been moved to this utility class for Maple Systems because all nodes share
 * the same output bits. Coil bits within Maple Systems HMI's are divided into
 * input (1-1000) and output (1001-9000), so input bits are read starting from
 * HMI address 1, and output bits are written starting at HMI address 1001.
 * <P>
 * @author Dave Duchamp, Copyright (C) 2009
 */
public class OutputBits {

    private SerialTrafficController tc = null;

    public OutputBits(SerialTrafficController _tc) {
        // clear all output bits
        for (int i = 0; i < 256; i++) {
            outputArray[i] = 0;
        }
        tc = _tc;
    }

    // operational variables
// private int mPulseWidth = 500;
    private static int mSendDelay = 200;
    private static int mNumOutputBits = 98;
    protected byte[] outputArray = new byte[256]; // current values of the output bits

    // access routines
    public static void setNumOutputBits(int n) {
        mNumOutputBits = n;
    }

    public static int getNumOutputBits() {
        return mNumOutputBits;
    }
// public int getPulseWidth() {return (mPulseWidth);}
// public void setPulseWidth(int width) {mPulseWidth = width;}

    public static void setSendDelay(int n) {
        mSendDelay = n;
    }

    public static int getSendDelay() {
        return mSendDelay;
    }

    /**
     * Public method setting an output bit. Note: state = 'true' for 0, 'false'
     * for 1 bits are numbered from 1 (not 0)
     */
    public void setOutputBit(int bitNumber, boolean state) {
        // validate that this bitNumber is defined
        if (bitNumber > mNumOutputBits) {
            log.warn("Output bit out-of-range for configured bits");
            return;
        }
        // locate in the outputArray
        int byteNumber = (bitNumber - 1) / 8;
        // update the byte
        byte bit = (byte) (1 << ((bitNumber - 1) % 8));
        if (state) {
            outputArray[byteNumber] &= (~bit);
        } else {
            outputArray[byteNumber] |= bit;
        }
    }

    /**
     * Public method get the current state of an output bit. Note: returns
     * 'true' for 0, 'false' for 1 bits are numbered from 1 (not 0)
     */
    public boolean getOutputBit(int bitNumber) {
        // locate in the outputArray
        int byteNumber = (bitNumber - 1) / 8;
        // validate that this byte number is defined
        if (byteNumber >= 256) {
            byteNumber = 255;
        }
        // update the byte
        byte bit = (byte) (1 << ((bitNumber - 1) % 8));
        byte testByte = outputArray[byteNumber];
        testByte &= bit;
        if (testByte == 0) {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Public Method to create an Transmit packet (SerialMessage)
     */
    public SerialMessage createOutPacket(int startBitNum, int endBitNum) {
        int nBits = endBitNum - startBitNum + 1;
        if (nBits > 99) {
            log.error("Number of bits for this packet greater than 99 - " + nBits);
            return null;
        }
        int sAdd = 1000 + startBitNum;  // write starting at 1001 for output 1 in HMI's  
        // create message
        SerialMessage m = new SerialMessage(14 + nBits);
        m.setElement(0, 02);
        m.setElement(1, '0');  // send in broadcast mode so all devices update      
        m.setElement(2, '0');
        m.setElement(3, 'W');
        m.setElement(4, 'C');
        m.setElement(5, '0' + (sAdd / 1000));
        m.setElement(6, '0' + ((sAdd - ((sAdd / 1000) * 1000)) / 100));
        m.setElement(7, '0' + ((sAdd - ((sAdd / 100) * 100)) / 10));
        m.setElement(8, '0' + (sAdd - ((sAdd / 10) * 10)));
        m.setElement(9, '0' + (nBits / 10));
        m.setElement(10, '0' + (nBits - ((nBits / 10) * 10)));
        for (int i = 0; i < nBits; i++) {
            int j = i - ((i / 8) * 8);
            int val = outputArray[i / 8];
            if (j == 0) {
                m.setElement(11 + i, ((val & 0x01) != 0) ? '1' : '0');
            } else if (j == 1) {
                m.setElement(11 + i, ((val & 0x02) != 0) ? '1' : '0');
            } else if (j == 2) {
                m.setElement(11 + i, ((val & 0x04) != 0) ? '1' : '0');
            } else if (j == 3) {
                m.setElement(11 + i, ((val & 0x08) != 0) ? '1' : '0');
            } else if (j == 4) {
                m.setElement(11 + i, ((val & 0x10) != 0) ? '1' : '0');
            } else if (j == 5) {
                m.setElement(11 + i, ((val & 0x20) != 0) ? '1' : '0');
            } else if (j == 6) {
                m.setElement(11 + i, ((val & 0x40) != 0) ? '1' : '0');
            } else if (j == 7) {
                m.setElement(11 + i, ((val & 0x80) != 0) ? '1' : '0');
            }
        }
        m.setElement(11 + nBits, 03);
        m.setChecksum(12 + nBits);
        m.setTimeout(mSendDelay);
        m.setNoReply();
        return m;
    }

    @Deprecated
    public static OutputBits instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(OutputBits.class);
}


