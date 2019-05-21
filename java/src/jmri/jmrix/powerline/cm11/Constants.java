package jmri.jmrix.powerline.cm11;

/**
 * Constants and functions specific to the CM11 interface.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class Constants {

    public static final int POLL_REQ = 0x5A;
    public static final int TIME_REQ_CP11 = 0xA5;
    public static final int TIME_REQ_CP10 = 0xA6;
    public static final int MACRO_INITIATED = 0x5B;
    public static final int MACRO_LOAD = 0xFB;
    public static final int CHECKSUM_OK = 0x00;
    public static final int READY_REQ = 0x55;
    public static final int FILTER_FAIL = 0xF3;
    public static final int EXT_CMD_HEADER = 0x07;

    public static final int POLL_ACK = 0xC3;
    public static final int TIMER_DOWNLOAD = 0x9B;

    /**
     * Pretty-print a header code.
     *
     * @param b header byte
     * @return  formated as text of header byte
     */
    public static String formatHeaderByte(int b) {
        return "Dim: " + ((b >> 3) & 0x1F)
                + ((b & 0x02) != 0 ? " function" : " address ")
                + ((b & 0x01) != 0 ? " extended" : " ");
    }

}
