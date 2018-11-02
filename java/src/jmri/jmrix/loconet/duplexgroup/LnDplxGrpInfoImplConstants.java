package jmri.jmrix.loconet.duplexgroup;

/**
 * Constants related to Digitrax Duplex Groups and related GUI
 * operations.
 *
 * @author B. Milhaupt Copyright 2010, 2011
 */
public final class LnDplxGrpInfoImplConstants {

    public final static int GENERAL_BYTE_MASK = 0xFF;
    public final static int GENERAL_DECIMAL_RADIX = 10;

    // helpers for defining and checking Duplex Group Information
    public final static int DPLX_NAME_LEN = 8;
    public final static int DPLX_PW_LEN = 4;
    public final static int DPLX_MAX_GR_CHANNEL_LEN = 2;
    public final static int DPLX_MAX_GR_ID_LEN = 3;
    public final static int DPLX_MIN_ID = 0;
    public final static int DPLX_MAX_ID = 127;
    public final static int DPLX_MIN_CH = 11;
    public final static int DPLX_MAX_CH = 26;

    // helpers for performing LocoNet operations
    public final static int IPL_QUERY_DELAY = 1300; // # milliseconds to wait for results from IPL query
    public final static int DPLX_QUERY_DELAY = 300; // # milliseconds to wait for response to Duplex info query
    public final static int DPLX_SCAN_DELAY = 200; // # milliseconds to wait for response to scan query
    public final static int DPLX_SCAN_LOOP_COUNT = 25;
}
