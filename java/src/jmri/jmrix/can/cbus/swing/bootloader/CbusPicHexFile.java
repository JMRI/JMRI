package jmri.jmrix.can.cbus.swing.bootloader;

import static jmri.jmrix.can.cbus.CbusConstants.MANU_MERG;
import static jmri.jmrix.can.cbus.node.CbusNodeConstants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend hex file class for a CBUS PIC with parameter block
 *
 * Assumes hex record addresses are "nicely" aligned, i.e., on 8, 16, 32, ...
 * -byte boundaries and that addresses increase monotonically. With Microchip
 * tools you should select the options to format the hex file for download and
 * program with default config words.
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CbusPicHexFile extends HexFile{

    public static final int PARAM_OLD_START = 0x810;
    public static final int PARAM_OLD_END = 0x817;
    public static final int PARAM_OLD_LEN = 7;
    public static final int PARAM_NEW_START = 0x820;
    public static final int PARAM_NEW_END = 0x840;
    public static final int PARAM_NEW_LEN = 32;

    private CbusParameters newParams = new CbusParameters();
    private CbusParameters oldParams = new CbusParameters();

    private boolean checkValid = false;
    // Running 16-bit sum of the new style parameter block bytes 1 - 30,
    // accumulated over all of the hex records that make up the block
    private int paramChecksum = 0;

    /**
     * Create a new HexFile object and initialize data to unprogrammed state.
     *
     * @param fileName file name to use for the hex file
     */
    public CbusPicHexFile(String fileName) {
        super(fileName);
    }


    /**
     * Check one hex record from the file
     *
     * Overridden to capture CBUS PIC parameter block.
     *
     * Assumes hex record addresses are "nicely" aligned, i.e., on 8, 16, 32, ...
     * -byte boundaries and that addresses increase monotonically. With Microchip
     * tools you should select the options to format the hex file for download
     * and program with default config words.
     *
     * @param r hex record
     */
    @Override
    protected void checkRecord(HexRecord r) {
        int rStart = 0;
        int rEnd = 0;
        int pStart = 0;

        if (r.type == HexRecord.END) {
            // Parameter block may be the last thing in the file
            if (checkValid) {
                checkParams();
            }
            return;
        }

        if (r.type == HexRecord.TYPE_DATA) {
            // Look for "old" 7-byte paraneter block at 0x810, assuming aligned hex address
            if ((r.address == PARAM_OLD_START) && (r.len >= PARAM_OLD_LEN)) {
                // Parameter 1 is the first byte of the block, index 0 is the count
                pStart = 1;
                for (int i = 0; i < PARAM_OLD_LEN; i++) {
                    oldParams.setParam(pStart++, r.getData(i) & 0xFF);
                }
                checkValid = true;
            } else {
                // Look for "new" 32-byte parameter block starting at 0x820
                if ((r.address < PARAM_NEW_START) && (r.address + r.len > PARAM_NEW_START)) {
                    // Record overlaps at start. Should only happen if record is greater than 32 bytes
                    // and encompasses the whole parameter block.
                    rStart = PARAM_NEW_START - r.address;
                    pStart = 1;
                    if ((r.len - rStart) > PARAM_NEW_LEN) {
                        rEnd = rStart + PARAM_NEW_LEN;
                    } else {
                        rEnd = r.len;
                    }
                    checkValid = true;
                } else if ((r.address >= PARAM_NEW_START) && (r.address < PARAM_NEW_END)) {
                    // Record starts within the parameter block. Should only happen for
                    // 32, 16 or 8-byte records and we want all the data in the record.
                    rStart = 0;
                    pStart = r.address - PARAM_NEW_START + 1;
                    if ((pStart - 1 + r.len) > PARAM_NEW_LEN) {
                        rEnd = PARAM_NEW_LEN - (pStart - 1);
                    } else {
                        rEnd = r.len;
                    }
                    checkValid = true;
                }

                if (pStart == 1) {
                    // Start of a new parameter block
                    paramChecksum = 0;
                }

                if (checkValid && (pStart > 0)) {
                    // Copy data to parameters and add to checksum
                    for (int i = rStart; i < rEnd; i++) {
                        int v = r.getData(i) & 0xFF;
                        newParams.setParam(pStart, v);
                        if (pStart < PARAM_CHECK_IDX) {
                            // Checksum covers parameters 1 - 30
                            paramChecksum += v;
                        }
                        pStart++;
                    }
                }
            }

            if ((r.address >= PARAM_NEW_END) && (checkValid == true)) {
                // First record after parameter block so check if parameters are valid
                checkParams();
            }
        }
    }


    /**
     * Check the parameters captured from the hex file, once the whole
     * parameter block has been read.
     * <p>
     * The last two bytes of the new style parameter block hold a 16-bit
     * (little endian) checksum which is the sum of the parameter bytes 1 - 30.
     * If that does not match, fall back to an old style block at 0x810, which
     * has no checksum, and is assumed to be valid only if it appears to be a
     * MERG module.
     */
    private void checkParams() {
        checkValid = false;
        // Copy new parameter count to parameter
        newParams.setParam(NUM_PARAM_IDX, newParams.getParam(PARAM_COUNT_IDX));
        if (newParamChecksumValid()) {
            log.debug("Valid new style parameter block found");
            newParams.setValid(true);
        } else if (oldParams.getParam(MANU_ID_IDX) == MANU_MERG) {
            // Assume old style parameter block @ 0x810 and assume only MERG made these
            log.debug("Assuming old style parameter block");
            oldParams.setParam(NUM_PARAM_IDX, PARAM_OLD_LEN);
            oldParams.setValid(true);
        } else {
            log.debug("No valid parameter block found");
        }
    }


    /**
     * Check the checksum of the new style parameter block.
     * <p>
     * The 16-bit sum of parameter bytes 1 - 30 must equal the little endian
     * 16-bit value stored in parameters 31 - 32. An all zero block, which
     * would trivially pass, is not accepted.
     *
     * @return true if the checksum is valid
     */
    private boolean newParamChecksumValid() {
        int lo = newParams.getParam(PARAM_CHECK_IDX);
        int hi = newParams.getParam(PARAM_CHECK_IDX + 1);
        if ((lo < 0) || (hi < 0)) {
            // Checksum bytes were not read
            return false;
        }
        int stored = lo + (hi * 256);
        log.debug("Parameter checksum calculated {} stored {}", paramChecksum & 0xFFFF, stored);
        return (stored != 0) && ((paramChecksum & 0xFFFF) == stored);
    }


    /**
     * Get the file parameters
     *
     * Create an invalid parameter set of necessary
     *
     * @return CBUS parameters from the file
     */
    @Override
    public CbusParameters getParams() {
        if (newParams.areValid()) {
            return newParams;
        } else if (oldParams.areValid()) {
            return oldParams;
        } else
            return new CbusParameters();
    }

    private static final Logger log = LoggerFactory.getLogger(CbusPicHexFile.class);

}
