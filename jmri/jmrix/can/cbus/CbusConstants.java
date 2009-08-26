// CbusConstants.java

package jmri.jmrix.can.cbus;

/**
 * CbusConstants.java
 *
 * Description:		Constants to represent CBUS protocol
 *
 * @author		Andrew Crosland   Copyright (C) 2008
 * @version $Revision: 1.6 $
 */
public final class CbusConstants {
    
    /**
     * CBUS Opcodes
     */
    public static final int CBUS_CVNAK = 0x12;
    public static final int CBUS_CVACK = 0x13;

    public static final int CBUS_QCVS = 0x61;
    public static final int CBUS_PCVS = 0x62;

    public static final int CBUS_WCVS = 0x81;

    public static final int CBUS_ACON = 0x90;
    public static final int CBUS_ACOF = 0x91;
    public static final int CBUS_AREQ = 0x92;
    
    public static final int CBUS_ASON = 0x98;
    public static final int CBUS_ASOF = 0x99;
    
    public static final int CBUS_ACON1 = 0xB0;
    public static final int CBUS_ACOF1 = 0xB1;

    /**
     * Programming modes
     */
    public static final int CBUS_PROG_DIRECT_BYTE = 0;
    public static final int CBUS_PROG_DIRECT_BIT = 1;
    public static final int CBUS_PROG_PAGED = 2;
    public static final int CBUS_PROG_REGISTER = 3;
    public static final int CBUS_PROG_ADDRESS = 4;

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
