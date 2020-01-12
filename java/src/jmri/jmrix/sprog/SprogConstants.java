package jmri.jmrix.sprog;

/**
 * Constants to represent values seen in SPROG traffic.
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

    /* The following parameters may be overridden by scripts if the user desires */
    /**
     * Maximum number of slots for soft command station 
     * 
     * More slots allows more throttles to be opened but the refresh rate for
     * each throttle will reduce.
     * 
     * The code limits the value used to between 8 and 32 inclusive
     *
     */
    public static int MAX_SLOTS = 16;

    /**
     * Threshold to warn of long delays between DCC packets to the rails.
     * 
     * Worst case DCC packet transmission time is ~10 ms, which equates to 100
     * packets/s. Wait for a somewhat arbitrary time before reporting a possible
     * issue with the system performance. A delay of 50 ms equates to 20 packets/s
     * if sustained.
     * 
     * Slower systems such as Raspberry Pi with flash based file systems are
     * more likely to exhibit longer delays between packets.
     */
    public static int PACKET_DELAY_WARN_THRESHOLD = 50;

    /**
     * Timeout for command station to wait for reply from hardware.
     * 
     * Slower systems such as Raspberry Pi with flash based file systems are more
     * likely to exhibit longer delays.
     */
    public static int CS_REPLY_TIMEOUT = 2500;

    /**
     * Timeout for traffic controller to wait for reply from hardware.
     * 
     * Most replies are received from SPROG hardware with a few seconds, but
     * paged mode programming operations can take considerably longer when
     * reading a high value from a CV. Therefore we set a very long timeout,
     * which should longer than the programmer timeout.
     */
    public static int TC_PROG_REPLY_TIMEOUT = 70*1000;
    public static int TC_OPS_REPLY_TIMEOUT = 200;
    public static int TC_BOOT_REPLY_TIMEOUT = 200;
    
    /* The following should be altered only if you know what you are doing */
    /* How many times to repeat an accessory or function packet in the S queue */
    public final static int S_REPEATS = 1;

    /* How many times to repeat an ops mode programming packet */
    public final static int OPS_REPEATS = 3;

    /* Longest packet possible */
    public final static int MAX_PACKET_LENGTH = 6;

    /* Slot status */
    public final static int SLOT_FREE = 0;
    public final static int SLOT_IN_USE = 1;

    /* Minimum number of slots */
    public final static int MIN_SLOTS = 8;
    
    /* Maimum number of slots */
    public final static int SLOTS_LIMIT = 32;
    
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

    public final static int DEFAULT_I = 996;            /* milliAmps */

    public final static int MAX_ACC_DECODER_JMRI_ADDR = 2044; // copied from DCCppConstants

}
