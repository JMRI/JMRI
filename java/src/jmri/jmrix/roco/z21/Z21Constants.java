package jmri.jmrix.roco.z21;
/**
 * Constants to represent values seen in z21 traffic
 *
 * @author	Paul Bender Copyright (C) 2003-2009
 *
 * Variable prefix abreviation keys (Some of these are from the z21 protocol
 * documentation): LAN_X_ is for constants specific to the XpressNet tunnel
 * LAN_LOCONET_ is for constants specific to the LocoNet tunnel
 *
 * A few variables don't have a prefix. The name should be self explanitory, but
 * a prefix may be added later.
 */
public final class Z21Constants {

    /* XpressNet Tunnel Constants */

    public final static int LAN_X_MESSAGE_TUNNEL = 0x0040;

    /* XpressNet Programming Constants */
    public final static int LAN_X_CV_READ_XHEADER = 0x23;
    public final static int LAN_X_CV_READ_DB0 = 0x11;
    public final static int LAN_X_CV_WRITE_XHEADER = 0x24;
    public final static int LAN_X_CV_WRITE_DB0 = 0x12;
    public final static int LAN_X_CV_RESULT_XHEADER = 0x64;
    public final static int LAN_X_CV_RESULT_DB0 = 0x14;

    /* XpressNet Locomotive Message Constants */
    public final static int LAN_X_LOCO_INFO_REQUEST_Z21 = 0xF0; // defined in section 4.1 of the protocol documentation.
    public final static int LAN_X_LOCO_INFO_RESPONSE = 0xEF; // defined in section 4.4 of the protocol documentation.
    public final static int LAN_X_SET_LOCO_FUNCTION = 0xF8; // defined in section 4.3 of the protocol documentation.
    public final static int LAN_X_GET_TURNOUT_INFO = 0x43; // defined in section 5.1 of the protocol documentation.
    public final static int LAN_X_SET_TURNOUT= 0x53; // defined in section 5.2 of the protocol documentation.
    public final static int LAN_X_TURNOUT_INFO = 0x43; // defined in section 5.3 of the protocol documentation.

    /* LocoNet Tunnel Constants */
}
