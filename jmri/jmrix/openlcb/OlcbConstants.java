// OlcbConstants.java

package jmri.jmrix.openlcb;

/**
 * OlcbConstants.java
 *
 * Left over from CBUS migration, these references
 * should go to the OpenLCB libraries instead
 *
 * @author		Andrew Crosland   Copyright (C) 2008
 * @author		Bob Jacobsen   Copyright (C) 2010
 * @version $Revision: 1.1 $
 */
public final class OlcbConstants {

    public static final int DEFAULT_STANDARD_ID = 0x7a;
    public static final int DEFAULT_EXTENDED_ID = 0x7a;

    /**
     * CBUS Opcodes
     */
    public static final int CBUS_TOF = 0x04;
    public static final int CBUS_TON = 0x05;

    public static final int CBUS_ARST = 0x07;
    public static final int CBUS_RTOF = 0x08;
    public static final int CBUS_RTON = 0x09;

    public static final int CBUS_KLOC = 0x21;

    public static final int CBUS_RLOC = 0x40;
    public static final int CBUS_STMOD = 0x44;
    public static final int CBUS_PCON = 0x45;
    public static final int CBUS_DSPD = 0x47;
    public static final int CBUS_SSTAT = 0x4C;

    public static final int CBUS_BOOT = 0x5C;

    public static final int CBUS_DFUN = 0x60;
    public static final int CBUS_ERR = 0x63;

    public static final int CBUS_RDCC3 = 0x80;
    public static final int CBUS_WCVO = 0x82;
    public static final int CBUS_WCVB = 0x83;
    public static final int CBUS_QCVS = 0x84;
    public static final int CBUS_PCVS = 0x85;

    public static final int CBUS_ACON = 0x90;
    public static final int CBUS_ACOF = 0x91;
    public static final int CBUS_AREQ = 0x92;
    
    public static final int CBUS_ASON = 0x98;
    public static final int CBUS_ASOF = 0x99;
    
    public static final int CBUS_WCVS = 0xA2;

    public static final int CBUS_ACON1 = 0xB0;
    public static final int CBUS_ACOF1 = 0xB1;

    public static final int CBUS_WCVOA = 0xC1;
    
    public static final int CBUS_PLOC = 0xE1;

    /**
     * Programming modes
     */
    public static final int CBUS_PROG_DIRECT_BYTE = 0;
    public static final int CBUS_PROG_DIRECT_BIT = 1;
    public static final int CBUS_PROG_PAGED = 2;
    public static final int CBUS_PROG_REGISTER = 3;
    public static final int CBUS_PROG_ADDRESS = 4;
    public static final int CBUS_OPS_BYTE = 5;

    /**
     * Error codes returned by CBUS_ERR
     */
    public static final int ERR_ADDR_FULL = 1;
    public static final int ERR_ADDR_TAKEN = 2;
    public static final int ERR_SESS_LOCO_NOT_FOUND = 3;
    public static final int ERR_NO_MORE_ENGINES = 4;
    public static final int ERR_ENGINE_NOT_FOUND = 5;

    /**
     * Status codes for OPC_SSTAT
     */
    public static final int SSTAT_NO_ACK = 1;
    public static final int SSTAT_OVLD = 2;
    public static final int SSTAT_WR_ACK = 3;
    public static final int SSTAT_BUSY = 4;
    public static final int SSTAT_CV_ERROR = 5;

    /**
     * Event types
     */
    public static final int EVENT_ON = 0;
    public static final int EVENT_OFF = 1;
    public static final int EVENT_EITHER = 2;
    
    /**
     * CBUS Priorities
     */
    public static final int DEFAULT_DYNAMIC_PRIORITY = 2;
    public static final int DEFAULT_MINOR_PRIORITY = 3;
    
    /**
     * Event Table
     */
    public static final int MAX_TABLE_EVENTS = 5000;

}

/* @(#)CbusConstants.java */
