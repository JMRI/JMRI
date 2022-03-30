package jmri.jmrix.can.cbus.swing.bootloader;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CBUS Node parameters
 * 
 * @author Andrew Crosland Copyright (C) 2020
 */
public class CbusParameters {

    /**
     * Node Parameters
     *
     * Para 0 Number of parameters
     * Para 1 The manufacturer ID
     * Para 2 Minor code version as an alphabetic character (ASCII)
     * Para 3 Manufacturer module identifier as a HEX numeric
     * Para 4 Number of supported events as a HEX numeric
     * Para 5 Number of Event Variables per event as a HEX numeric
     * Para 6 Number of supported Node Variables as a HEX numeric
     * Para 7 Major version
     * Para 8 Node flags
     * Para 9 Processor type
     * Para 10 Bus type
     * Para 11 load address, 4 bytes
     * Para 15 CPU manufacturer's id as read from the chip config space, 4 bytes, only 2 bytes used for PICs
     * Para 19 CPU manufacturer code
     * Para 20 Beta revision (numeric), or 0 if release
     *                
     */
    public static final int NUM_PARAM_IDX = 0;      // Para 0 Number of parameters
    public static final int MANU_ID_IDX = 1;        // Para 1 The manufacturer ID
    public static final int MINOR_VER_IDX = 2;      // Para 2 Minor code version as an alphabetic character (ASCII)
    public static final int MODULE_ID_IDX = 3;      // Para 3 Manufacturer module identifier as a HEX numeric
    public static final int NUM_EV_IDX = 4;         // Para 4 Number of supported events as a HEX numeric
    public static final int EV_PER_EN_IDX = 5;      // Para 5 Number of Event Variables per event as a HEX numeric
    public static final int NUM_NV_IDX = 6;         // Para 6 Number of supported Node Variables as a HEX numeric
    public static final int MAJOR_VER_IDX = 7;      // Para 7 Major version
    public static final int FLAGS_IDX = 8;          // Para 8 Node flags
    public static final int PROC_TYPE_IDX = 9;      // Para 9 Processor type
    public static final int BUS_TYPE_IDX = 10;      // Para 10 Bus type
    public static final int LOAD_ADDR_IDX = 11;     // Para 11 load address, 4 bytes
    public static final int CPU_ID_IDX = 15;        // Para 15 CPU manufacturer's id as read from the chip config space, 4 bytes, only firs two used for PIC18
    public static final int CPU_CODE_IDX = 19;      // Para 19 CPU manufacturer code
    public static final int BETA_REV_IDX = 20;      // Para 20 Beta revision (numeric), or 0 if release
    
    protected int [] paramData = null;
    protected boolean valid = false;
//    protected boolean newVersion = false;

    
    /**
     * Create blank parameters
     */
    public CbusParameters() {
        paramData = new int[33];
        Arrays.fill(paramData, -1);
    }
    
    
//    /**
//     * Create parameters from a hex file
//     * 
//     * @param f hex file already read
//     */
//    public CbusParameters(HexFile f) {
//        this();
//        
//        int checksum = 0;
//        byte [] d;
//
//        // Look for new style parameter block @ 0x800
//        d = f.getData(0x820, 32);
//        
//        // Copy to params array and calculate checksum.
//        // Copy is offset by 1 to match CBUS parameter numbering
//        for (int i = 0; i < 30; i++) {
//            paramData[i + 1] = d[i] & 0xFF;
//            checksum += d[i];
//        }
//        // Copy checksum and parameter count
//        paramData[31] = d[30];
//        paramData[32] = d[31];
//        paramData[0] = d[24] & 0xFF;
//        
//        int paramCheck = ((d[31] & 0xFF)<<8) + (d[30] & 0xFF);
//        if ((checksum & 0xFFFF) == paramCheck) {
//            valid = true;
//            return;
//        }
//
//        // Assume old style parameter block @ 0x810 and assume only MERG made these
//        // as a check
//        d = f.getData(0x810, 8);
//        if (d[MANU_ID_IDX] == (byte)MANU_MERG) {
//            for (int i = 0; i < 7; i++) {
//                paramData[i + 1] = d[i];
//            }
//            paramData[0] = 7;
//            valid = true;
//        }
//    }
    
    
    /**
     * Create parameters from byte []
     * 
     * @param d byte [] array 
     */
    public CbusParameters(byte [] d) {
        this();
        
        if (d.length > 32) {
            log.error("Too many parameters");
            valid = false;
        } else {
            paramData[0] = d.length;
            for (int i = 0; i < paramData[0]; i++) {
                paramData[i+1] = d[i] & 0xFF;
            }
            valid = true;
        }
    }


    /**
     * Get a parameter
     * @param i index of parameter to get
     * @return parameter value
     */
    public int getParam(int i) {
        return paramData[i];
    }
    
    
    /**
     * Set a parameter
     * @param i index of the parameter to set
     * @param v value for the parameter
     */
    public void setParam(int i, int v) {
        paramData[i] = v;
    }
    
    
    /**
     * Valid parameter block in a hex file by comparing against one
     * read from hardware (or some other source)
     * 
     * @param fp Parameters from hex file
     * @param hp parameters to validate against
     * @return true if parameter blocks match
     */
    public boolean validate(CbusParameters fp, CbusParameters hp) {

        if (!fp.valid || !hp.valid) {
            return false;
        }

        // As a minimum, check manufacturer ID, module ID and processor type
        if (fp.paramData[MANU_ID_IDX] != hp.paramData[MANU_ID_IDX]) {
            log.error("Manufacturer ID mismatch {} {}", fp.paramData[MANU_ID_IDX], hp.paramData[MANU_ID_IDX]);
            return false;
        }
        
        if (fp.paramData[MODULE_ID_IDX] != hp.paramData[MODULE_ID_IDX]) {
            log.error("Module ID mismatch {} {}", fp.paramData[MODULE_ID_IDX], hp.paramData[MODULE_ID_IDX]);
            return false;
        }
        
//        if (!checkVersion(fp, hp)) {
//            log.error("Version mismatch {}.{} {}.{}", , fp.paramData[MAJOR_VER_IDX], fp.paramData[MINOR_VER_IDX],
//                                hp.paramData[MAJOR_VER_IDX], hp.paramData[MINOR_VER_IDX]);
//            return false;
//        }

        if (hp.paramData[NUM_PARAM_IDX] > 7) {
            if (fp.paramData[PROC_TYPE_IDX] != hp.paramData[PROC_TYPE_IDX]) {
                log.error("Processor type mismatch {} {}", fp.paramData[PROC_TYPE_IDX], hp.paramData[PROC_TYPE_IDX]);
                return false;
            }
        }
        
        return true;
    }
    
    
// Not yet used in any meaningful way.
// Comment out to make spotbugs happy for now
//    /**
//     * Compare two parameter blocks to see if one is a new version
//     * 
//     * @param pNew possible new version
//     * @param pOld original parameters
//     */
//    public void checkVersion(CbusParameters pNew, CbusParameters pOld) {
//        if (pNew.paramData[MAJOR_VER_IDX] > pOld.paramData[MAJOR_VER_IDX]) {
//            newVersion = true;
//        } else if (pNew.paramData[MAJOR_VER_IDX] < pOld.paramData[MAJOR_VER_IDX]) {
//            newVersion = false;
//        } else newVersion = pNew.paramData[MINOR_VER_IDX] > pOld.paramData[MINOR_VER_IDX];
//    }
    
    
    /**
     * Return the load address which is stored in little endian order in four
     * parameters
     * 
     * @return the load address
     */
    public int getLoadAddress() {
        if (paramData[NUM_PARAM_IDX] == 7) {
            return 0x800;
        } else {
            int la = (paramData[LOAD_ADDR_IDX]
                    + paramData[LOAD_ADDR_IDX+1]*256
                    + paramData[LOAD_ADDR_IDX+2]*256*256
                    + paramData[LOAD_ADDR_IDX+3]*256*256*256);
            log.debug("Load address is {}", la);
            return la;
        }
    }
    
    
    /**
     * Return String representation of CBUS parameters
     * 
     * @return String
     */
    @Override
    public String toString() {
        if (valid == false) {
            return Bundle.getMessage("ParamsInvalid");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < paramData[0]; i++) {
                sb.append(paramData[i] + " ");
            }
            return sb.toString();
        }
    }
    
    
    /**
     * Are the parameters valid?
     * 
     * @return true if valid
     */
    public boolean areValid() {
        return valid;
    }
    
    
    /**
     * Set parameter valid status
     * 
     * @param s true or false valid status
     */
    public void setValid(boolean s) {
        valid = s;
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusParameters.class);
    
}
