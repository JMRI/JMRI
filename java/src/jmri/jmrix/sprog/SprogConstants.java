package jmri.jmrix.sprog;

/**
 * SprogConstants.java
 *
 * Description:	Constants to represent values seen in SPROG traffic
 *
 * @author	Andrew Crosland Copyright (C) 2006 from LnConstants.java
 */
public final class SprogConstants {

    /* SPROG mode */
    public final static int SPROG = 0;
    public final static int SPROG_CS = 1;

    // Current SPROG state
    public enum SprogState {

        NORMAL, SIIBOOTMODE, V4BOOTMODE
    }

    public enum SprogMode {

        SERVICE, OPS
    }

    /* Maximum number of slots for soft command station */
    public final static int MAX_SLOTS = 16;

    /* How many times to repeat an accessory or function packet in the S queue */
    public final static int S_REPEATS = 1;

    /* How many times to repeat an ops mode programming packet */
    public final static int OPS_REPEATS = 3;

    /* Longest packet possible */
    public final static int MAX_PACKET_LENGTH = 6;

    /* Slot status */
    public final static int SLOT_FREE = 0;
    public final static int SLOT_IN_USE = 1;

    /* various bit masks */
    public final static int F8 = 0x100; /* Function 8 bit */

    public final static int F7 = 0x80; /* Function 7 bit */

    public final static int F6 = 0x40; /* Function 6 bit */

    public final static int F5 = 0x20; /* Function 5 bit */

    public final static int F4 = 0x10; /* Function 4 bit   */

    public final static int F3 = 0x08; /* Function 3 bit   */

    public final static int F2 = 0x04; /* Function 2 bit   */

    public final static int F1 = 0x02; /* Function 1 bit   */

    public final static int F0 = 0x01; /* Function 0 bit   */

    /* Mode word bit masks */
    public final static int UNLOCK_BIT = 0x0001;      /* Unlock bootloader */

    public final static int CALC_BIT = 0x0008;        /* Add error byte */

    public final static int POWER_BIT = 0x0010;       /* Track power */

    public final static int ZTC_BIT = 0x0020;         /* Old ZTC bit timing */

    public final static int BLUE_BIT = 0x0040;        /* Use direct byte for Blueline */

    public final static int STEP_MASK = 0x0E00;       /* Mask for speed step bits */

    public final static int STEP14_BIT = 0x0200;
    public final static int STEP28_BIT = 0x0400;
    public final static int STEP128_BIT = 0x0800;
    public final static int LONG_ADD = 0x1000;

    public final static int DEFAULT_I = 996;

}

