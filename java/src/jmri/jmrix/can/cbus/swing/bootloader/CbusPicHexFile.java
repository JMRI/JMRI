package jmri.jmrix.can.cbus.swing.bootloader;

import static jmri.jmrix.can.cbus.CbusConstants.MANU_MERG;
import static jmri.jmrix.can.cbus.node.CbusNodeConstants.*;

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
    public static final int PARAM_NEW_END = 0x839;
    public static final int PARAM_NEW_LEN = 32;
    
    private CbusParameters newParams = new CbusParameters();
    private CbusParameters oldParams = new CbusParameters();
    
    private boolean checkValid = false;
    
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
        int checksum = 0;
        
        if (r.type == HexRecord.TYPE_DATA) {
            // Look for "old" 8-byte paraneter block at 0x810, assuming aligned hex address
            if (r.address == PARAM_OLD_START) {
                for (int i = 0; i <= PARAM_OLD_LEN; i++) {
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
                    if (r.len > PARAM_NEW_LEN) {
                        rEnd = rStart + PARAM_NEW_LEN;
                    } else {
                        rEnd = rStart + r.len;
                    }
                    checkValid = true;
                } else if ((r.address >= PARAM_NEW_START) && (r.address < PARAM_NEW_END)) {
                    // Record starts within the parameter block. Should only happen for
                    // 32, 16 or 8-byte records and we want all the data in the record.
                    rStart = 0;
                    rEnd = r.len;
                    pStart = r.address - 0x820 + 1;
                    checkValid = true;
                }
                
                if (checkValid) {
                    // Copy data to parameters and add to checksum
                    for (int i = rStart; i < rEnd; i++) {
                        newParams.setParam(pStart++, r.getData(i) & 0xFF);
                        checksum += r.getData(i) & 0xFF;
                    }
                }
            }

            if ((r.address >= PARAM_NEW_END) && (checkValid == true)) {
                // First record after parameter block so check if parameters are
                checkValid = false;
                // Copy new parameter count to parameter
                newParams.setParam(NUM_PARAM_IDX, newParams.getParam(PARAM_COUNT_IDX));
                if (checksum == 0) {
                    newParams.setValid(true);
                } else if (oldParams.getParam(MANU_ID_IDX) == (byte)MANU_MERG) {
                    // Assume old style parameter block @ 0x810 and assume only MERG made these
                    oldParams.setParam(NUM_PARAM_IDX, PARAM_OLD_LEN);
                    oldParams.setValid(true);
                }
            }
        }
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
    
//    private final static Logger log = LoggerFactory.getLogger(CbusPicHexFile.class);

}
