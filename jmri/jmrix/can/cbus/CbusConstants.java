// CbusConstants.java

package jmri.jmrix.can.cbus;

/**
 * CbusConstants.java
 *
 * Description:		Constants to represent CBUS protocol
 *
 * @author		Andrew Crosland   Copyright (C) 2008
 * @version $Revision: 1.2 $
 */
public final class CbusConstants {
    
    /**
     * CBUS Opcodes
     */
    public static final int CBUS_OP_EV_ON = 0x90;
    public static final int CBUS_OP_EV_OFF = 0x91;
    public static final int CBUS_OP_EV_ON_DATA = 0xB0;
    public static final int CBUS_OP_EV_OFF_DATA = 0xB1;

    /**
     * Event types
     */
    public static final int EVENT_ON = 0;
    public static final int EVENT_OFF = 1;
    public static final int EVENT_EITHER = 2;
}

/* @(#)CbusConstants.java */
