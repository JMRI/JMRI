package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ALM implementation for the SE8.
 * <P>
 * LocoNet ALM messages showing a argument value N will show in the throttle
 * editor as N+1. Here, we refer to these as "arguments" and "throttle values".
 * Similarly, the addresses are "addresses" in the ALM, based on zero, and
 * "entries" in the throttle, based on 1.
 * <P>
 * Internally, this stores data as a single address-space vector from 0 to some
 * maximum value, addressed by (block)*4+item or by (SE_NUM)*ENTRYSIZE+item. The
 * item index is 0,1,2,3 for blocks, or 0,1,2,...ENTRYSIZE-1 for addressing with
 * an SE.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright 2002
 * @version $Revision$
 * @deprecated 2.13.5, Does not work with the multi-connection correctly,
 * believe not to work correctly before hand and that the feature is not used -  - left to allow old files to be read
 */
@Deprecated
public class Se8AlmImplementation extends AbstractAlmImplementation {

    public static final int ENTRYSIZE = 64; // number of arguments per entry, must be power of two

    /**
     * Create an object representing the ALM entries for a single SE8 unit
     *
     * @param pNumber Number of this ALM
     * @param pImage  Does this appear on LocoNet?
     */
    public Se8AlmImplementation(int pNumber, boolean pImage) {
        super(pNumber, pImage);
        initData();
    }

    // offsets in the ALM for various things
    static final int ACon = 0;
    static final int BCon = 1;
    static final int CCon = 2;
    static final int ALeg = 3;
    static final int BLeg = 4;
    static final int CLeg = 5;
    static final int TO = 6;
    static final int DS = 7;

    public int getACon(int se) {
        return retrieveBySE(se, ACon);
    }

    public int getBCon(int se) {
        return retrieveBySE(se, BCon);
    }

    public int getCCon(int se) {
        return retrieveBySE(se, CCon);
    }

    public int getALeg(int se) {
        return retrieveBySE(se, ALeg);
    }

    public int getBLeg(int se) {
        return retrieveBySE(se, BLeg);
    }

    public int getCLeg(int se) {
        return retrieveBySE(se, CLeg);
    }

    public int getTO(int se) {
        return retrieveBySE(se, TO);
    }

    public int getDS(int se) {
        return retrieveBySE(se, DS);
    }

    public void setACon(int se, int value) {
        storeBySE(se, ACon, value);
    }

    public void setBCon(int se, int value) {
        storeBySE(se, BCon, value);
    }

    public void setCCon(int se, int value) {
        storeBySE(se, CCon, value);
    }

    public void setALeg(int se, int value) {
        storeBySE(se, ALeg, value);
    }

    public void setBLeg(int se, int value) {
        storeBySE(se, BLeg, value);
    }

    public void setCLeg(int se, int value) {
        storeBySE(se, CLeg, value);
    }

    public void setTO(int se, int value) {
        storeBySE(se, TO, value);
    }

    public void setDS(int se, int value) {
        storeBySE(se, DS, value);
    }

    /**
     * Internal method to save a new value
     * <P>
     * block*4+item is the ALM address
     *
     * @param block The block number, starting with 0
     * @param item  Item number within the block, as 0,1,2,3
     * @param value The integer argument value to store
     */
    void store(int block, int item, int value) {
        contents[block * 4 + item] = value;
    }

    /**
     * Internal method to retrieve a value using block index
     * <P>
     * block*4+item is the ALM address
     *
     * @param block The block number, starting with 0
     * @param item  Item number within the block, as 0,1,2,3
     * @return The integer argument value
     */
    int retrieve(int block, int item) {
        return contents[block * 4 + item];
    }

    /**
     * Retrieve using SE index. Note that SEs are numbered starting from 1, not
     * 0.
     */
    public int retrieveBySE(int SE, int item) {
        return contents[(SE - 1) * ENTRYSIZE + item];
    }

    /**
     * Store using SE index. Note that SEs are numbered starting from 1, not 0.
     */
    public void storeBySE(int SE, int item, int value) {
        contents[(SE - 1) * ENTRYSIZE + item] = value;
    }

    boolean keepReading = false;
    boolean keepWriting = false;

    /**
     * Keep a read going if needed
     */
    public void noteReadReply(int block) {
        // check to see if we're doing a read of an entire SE
        int section = block % (ENTRYSIZE / 4);
        if (keepReading && (section < (ENTRYSIZE / 4 - 1))) {
            sendRead(block + 1);
        } else {
            keepReading = false;
        }
    }

    /**
     * Keep a write going if needed
     */
    public void noteWriteComplete(int block) {
        // check to see if we're doing a write of an entire SE
        int section = block % (ENTRYSIZE / 4);
        if (keepWriting && (section < (ENTRYSIZE / 4 - 1))) {
            sendWrite(block + 1);
        } else {
            keepWriting = false;
        }
    }

    /**
     * Start the process of reading the values for an SE
     * <P>
     * Note that SEs are numbered starting with 1, not zero.
     */
    public void triggerRead(int se) {
        if (!mImage) {
            log.error("Doesn't make sense to trigger a read if not image");
        }
        // mark so reads all blocks
        keepReading = true;
        // format up and send the read command
        int block = (se - 1) * (ENTRYSIZE / 4);
        sendRead(block);
    }

    /**
     * Start the process of writing the values for an SE
     * <P>
     * Note that SEs are numbered starting with 1, not zero.
     */
    public void triggerWrite(int se) {
        if (!mImage) {
            log.error("Doesn't make sense to trigger a write if not image");
        }
        // mark so writes all blocks
        keepWriting = true;
        // format up and send the read command
        int block = (se - 1) * (ENTRYSIZE / 4);
        sendWrite(block);
    }

    /**
     * Local storage for the values in this ALM
     */
    int contents[];

    /**
     * Initialize the local storage
     */
    void initData() {
        final int MAX_SE = 1024;
        final int length = MAX_SE * ENTRYSIZE;
        contents = new int[length];
        for (int j = 0; j < length; j++) {
            contents[j] = 0x3FFF;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Se8AlmImplementation.class.getName());
}
