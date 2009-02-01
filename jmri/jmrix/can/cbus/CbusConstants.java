// CbusConstants.java

package jmri.jmrix.can.cbus;

/**
 * CbusConstants.java
 *
 * Description:		Constants to represent CBUS protocol
 *
 * @author		Andrew Crosland   Copyright (C) 2008
 * @version $Revision: 1.4 $
 */
public final class CbusConstants {
    
    /**
     * CBUS Opcodes
     */
    public static final int CBUS_ACON = 0x90;
    public static final int CBUS_ACOF = 0x91;
    public static final int CBUS_AREQ = 0x92;
    
    public static final int CBUS_ASON = 0x98;
    public static final int CBUS_ASOF = 0x99;
    
    public static final int CBUS_ACON1 = 0xB0;
    public static final int CBUS_ACOF1 = 0xB1;

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
}

/* @(#)CbusConstants.java */
