package jmri.jmrix.sprog;

/**
 * Constants to represent values seen in SPROG traffic.
 *
 * @author Andrew Crosland Copyright (C) 2006 from LnConstants.java
 */

public final class SprogConstants {

    // prevent new instance, Class supplies static constants.
    private SprogConstants(){}

    /* SPROG mode */
    public static final int SPROG = 0;
    public static final int SPROG_CS = 1;

    // Current SPROG state
    public enum SprogState {

        NORMAL, SIIBOOTMODE, V4BOOTMODE
    }

    public enum SprogMode {

        SERVICE, OPS
    }

    /* The following parameters may be overridden by scripts if the user desires */
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
     * Number of consecutive reply timeouts before the command station
     * shuts down track power.
     * <p>
     * A single timeout can occur when the JVM is temporarily busy (e.g.
     * garbage collection or Swing rendering). Requiring multiple consecutive
     * timeouts before taking the drastic step of removing power avoids
     * false-positive shutdowns on slower systems.
     */
    public static int CS_MAX_TIMEOUT_COUNT = 3;

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
    public static final int S_REPEATS = 1;

    /* How many times to repeat an ops mode programming packet */
    public static final int OPS_REPEATS = 3;

    /* Longest packet possible */
    public static final int MAX_PACKET_LENGTH = 6;

    /* Slot status */
    public static final int SLOT_FREE = 0;
    public static final int SLOT_IN_USE = 1;

    /*
     * Maximum number of slots for soft command station 
     * 
     * More slots allows more throttles to be opened but the refresh rate for
     * each throttle will reduce.
     * 
     */
    /* Default */
    public static final int DEFAULT_MAX_SLOTS = 16;

    /* Minimum number of slots */
    public static final int MIN_SLOTS = 8;
    
    /* Maimum number of slots */
    public static final int SLOTS_LIMIT = 64;
    
    /* Number of function buttons on a throttle */
    public static int MAX_FUNCTIONS = 32; 
    
    /* various bit masks */
    public static final int F8 = 0x100; /* Function 8 bit */

    public static final int F7 = 0x80; /* Function 7 bit */

    public static final int F6 = 0x40; /* Function 6 bit */

    public static final int F5 = 0x20; /* Function 5 bit */

    public static final int F4 = 0x10; /* Function 4 bit   */

    public static final int F3 = 0x08; /* Function 3 bit   */

    public static final int F2 = 0x04; /* Function 2 bit   */

    public static final int F1 = 0x02; /* Function 1 bit   */

    public static final int F0 = 0x01; /* Function 0 bit   */

    /* Mode word bit masks */
    public static final int UNLOCK_BIT = 0x0001;      /* Unlock bootloader */

    public static final int CALC_BIT = 0x0008;        /* Add error byte */

    public static final int POWER_BIT = 0x0010;       /* Track power */

    public static final int ZTC_BIT = 0x0020;         /* Old ZTC bit timing */

    public static final int BLUE_BIT = 0x0040;        /* Use direct byte for Blueline */

    public static final int STEP_MASK = 0x0E00;       /* Mask for speed step bits */

    public static final int STEP14_BIT = 0x0200;
    public static final int STEP28_BIT = 0x0400;
    public static final int STEP128_BIT = 0x0800;
    public static final int LONG_ADD = 0x1000;

    public static final int DEFAULT_I = 996;            /* milliAmps */

    public static final int MAX_ACC_DECODER_JMRI_ADDR = 2044; // copied from DCCppConstants

}
