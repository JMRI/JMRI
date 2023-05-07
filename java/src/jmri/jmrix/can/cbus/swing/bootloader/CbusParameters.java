package jmri.jmrix.can.cbus.swing.bootloader;

import java.util.Arrays;

import static jmri.jmrix.can.cbus.node.CbusNodeConstants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CBUS Node parameters
 * 
 * @author Andrew Crosland Copyright (C) 2020
 */
public class CbusParameters {

    protected int [] paramData = null;
    protected boolean valid = false;

    
    /**
     * Create blank parameters
     */
    public CbusParameters() {
        paramData = new int[33];
        Arrays.fill(paramData, -1);
    }
    
    
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
