package jmri.jmrix.powerline.dmx512;

/**
 * Constants and functions specific to the DMX512 interface.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Ken Cameron Copyright (C) 2023
 */
public class Constants {

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
