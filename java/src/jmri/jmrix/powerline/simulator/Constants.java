package jmri.jmrix.powerline.simulator;

/**
 * Constants and functions specific to the Insteon 2412S interface
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2009
 * @author Ken Cameron Copyright (C) 2010
  */
public class Constants {

    public static final int HEAD_STX = 0x02;

    public static final int POLL_REQ_STD = 0x50;
    public static final int POLL_REQ_EXT = 0x51;
    public static final int POLL_REQ_X10 = 0x52;
    public static final int POLL_REQ_BUTTON = 0x54;
    public static final int POLL_REQ_BUTTON_RESET = 0x55;
    public static final int FUNCTION_REQ_STD = 0x62;
    public static final int FUNCTION_REQ_X10 = 0x63;

    public static final int CMD_LIGHT_ON_RAMP = 0x11;
    public static final int CMD_LIGHT_ON_FAST = 0x12;
    public static final int CMD_LIGHT_OFF_RAMP = 0x13;
    public static final int CMD_LIGHT_OFF_FAST = 0x14;
    public static final int CMD_LIGHT_CHG = 0x21;

    public static final int BUTTON_TAP = 0x02;
    public static final int BUTTON_HELD = 0x03;
    public static final int BUTTON_REL = 0x04;
    public static final int BUTTON_BITS_ID = 0xF0;
    public static final int BUTTON_BITS_OP = 0x0F;

    public static final int REPLY_ACK = 0x06;
    public static final int REPLY_NAK = 0x15;

    // flag values
    public static final int FLAG_BIT_STDEXT = 0x10;
    public static final int FLAG_STD = 0x00;
    public static final int FLAG_EXT = 0x10;

    public static final int FLAG_BIT_X10_CMDUNIT = 0x80;
    public static final int FLAG_X10_RECV_CMD = 0x80;
    public static final int FLAG_X10_RECV_UNIT = 0x00;

    /**
     * Pretty-print a header code
     * @param b value for header
     * @return  formated string for type of header
     */
    public static String formatHeaderByte(int b) {
        return "Dim: " + ((b >> 3) & 0x1F)
                + ((b & 0x02) != 0 ? " function" : " address ")
                + ((b & 0x01) != 0 ? " extended" : " ");
    }

}



