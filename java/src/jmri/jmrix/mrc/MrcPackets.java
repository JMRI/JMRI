package jmri.jmrix.mrc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.text.DecimalFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some of the message formats used in this class are Copyright MRC, Inc. and
 * used with permission as part of the JMRI project. That permission does not
 * extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Mrc Inc
 * for separate permission.
 *
 * @author Kevin Dickerson 2014
 * @author Ken Cameron 2014
 */
public class MrcPackets {

    private static final Logger log = LoggerFactory.getLogger(MrcPackets.class);
    public static final int THROTTLEPACKETCMD = 37;
    static final int[] THROTTLEPACKETHEADER = new int[]{THROTTLEPACKETCMD, 0, THROTTLEPACKETCMD, 0};
    static final int THROTTLEPACKETLENGTH = 10; //length of packet less the header

    public static final int FUNCTIONGROUP1PACKETCMD = 52;
    static final int[] FUNCTIONGROUP1PACKETHEADER = new int[]{FUNCTIONGROUP1PACKETCMD, 0, FUNCTIONGROUP1PACKETCMD, 0};

    public static final int FUNCTIONGROUP2PACKETCMD = 68;
    static final int[] FUNCTIONGROUP2PACKETHEADER = new int[]{FUNCTIONGROUP2PACKETCMD, 0, FUNCTIONGROUP2PACKETCMD, 0};

    public static final int FUNCTIONGROUP3PACKETCMD = 84;
    static final int[] FUNCTIONGROUP3PACKETHEADER = new int[]{FUNCTIONGROUP3PACKETCMD, 0, FUNCTIONGROUP3PACKETCMD, 0};

    public static final int FUNCTIONGROUP4PACKETCMD = 116;
    static final int[] FUNCTIONGROUP4PACKETHEADER = new int[]{FUNCTIONGROUP4PACKETCMD, 0, FUNCTIONGROUP4PACKETCMD, 0};

    public static final int FUNCTIONGROUP5PACKETCMD = 132;
    static final int[] FUNCTIONGROUP5PACKETHEADER = new int[]{FUNCTIONGROUP5PACKETCMD, 0, FUNCTIONGROUP5PACKETCMD, 0};

    public static final int FUNCTIONGROUP6PACKETCMD = 164;
    static final int[] FUNCTIONGROUP6PACKETHEADER = new int[]{FUNCTIONGROUP6PACKETCMD, 0, FUNCTIONGROUP6PACKETCMD, 0};

    static final int FUNCTIONGROUPLENGTH = 8;

    public static final int ADDTOCONSISTPACKETCMD = 100;
    static final int[] ADDTOCONSISTPACKETHEADER = new int[]{ADDTOCONSISTPACKETCMD, 0, ADDTOCONSISTPACKETCMD, 0};
    static final int ADDTOCONSISTPACKETLENGTH = 4;

    public static final int CLEARCONSISTPACKETCMD = 98;
    static final int[] CLEARCONSISTPACKETHEADER = new int[]{CLEARCONSISTPACKETCMD, 0, CLEARCONSISTPACKETCMD, 0};
    static final int CLEARCONSISTPACKETLENGTH = 4;

    public static final int ROUTECONTROLPACKETCMD = 195;
    static final int[] ROUTECONTROLPACKETHEADER = new int[]{ROUTECONTROLPACKETCMD, 0, ROUTECONTROLPACKETCMD, 0};
    static final int ROUTECONTROLPACKETLENGTH = 6; //Need to check.

    public static final int CLEARROUTEPACKETCMD = 210;
    static final int[] CLEARROUTEPACKETHEADER = new int[]{CLEARROUTEPACKETCMD, 0, CLEARROUTEPACKETCMD, 0};
    static final int CLEARROUTEPACKETLENGTH = 4;

    public static final int ADDTOROUTEPACKETCMD = 211;
    static final int[] ADDTOROUTEPACKETHEADER = new int[]{ADDTOROUTEPACKETCMD, 0, ADDTOROUTEPACKETCMD, 0};
    static final int ADDTOROUTEPACKETLENGTH = 6;

    public static final int ACCESSORYPACKETCMD = 115;
    static final int[] ACCESSORYPACKETHEADER = new int[]{ACCESSORYPACKETCMD, 0, ACCESSORYPACKETCMD, 0};
    static final int ACCESSORYPACKETLENGTH = 6;

    public static final int WRITECVPOMCMD = 86;
    static final int[] WRITECVPOMHEADER = new int[]{WRITECVPOMCMD, 0, WRITECVPOMCMD, 0};
    private static final int WRITECVPOMLENGTH = 12;

    public static final int WRITECVPROGCMD = 36;
    static final int[] WRITECVPROGHEADER = new int[]{WRITECVPROGCMD, 0, WRITECVPROGCMD, 0};
    private static final int WRITECVPROGLENGTH = 8;

    public static final int READDECODERADDRESSCMD = 66;
    static final int[] READDECODERADDRESS = new int[]{READDECODERADDRESSCMD, 0, READDECODERADDRESSCMD, 0, READDECODERADDRESSCMD, 0};

    public static final int READCVCMD = 67;
    static final int[] READCVHEADER = new int[]{READCVCMD, 0, READCVCMD, 0};
    private static final int READCVLENGTH = 6;

    public static final int PROGCMDSENTCODE = 51;
    static final int[] PROGCMDSENT = new int[]{PROGCMDSENTCODE, 0, PROGCMDSENTCODE, 0};

    public static final int READCVHEADERREPLYCODE = 102;
    static final int[] READCVHEADERREPLY = new int[]{READCVHEADERREPLYCODE, 0, READCVHEADERREPLYCODE, 0};
    static final int READCVPACKETLENGTH = 4; //need to double check the length of this packet

    public static final int SETCLOCKRATIOCMD = 18;
    static final int[] SETCLOCKRATIOHEADER = new int[]{SETCLOCKRATIOCMD, 0, SETCLOCKRATIOCMD, 0};
    private static final int SETCLOCKRATIOLENGTH = 4;

    public static final int SETCLOCKTIMECMD = 19;
    static final int[] SETCLOCKTIMEHEADER = new int[]{SETCLOCKTIMECMD, 0, SETCLOCKTIMECMD, 0};
    private static final int SETCLOCKTIMELENGTH = 6;

    public static final int SETCLOCKAMPMCMD = 50;
    static final int[] SETCLOCKAMPMHEADER = new int[]{SETCLOCKAMPMCMD, 0, SETCLOCKAMPMCMD, 0};
    private static final int SETCLOCKAMPMLENGTH = 4;

    public static final int LOCOSOLECONTROLCODE = 34;
    static final int[] LOCOSOLECONTROL = new int[]{LOCOSOLECONTROLCODE, 0, LOCOSOLECONTROLCODE, 0}; //Reply indicates that we are the sole controller of the loco

    public static final int LOCODBLCONTROLCODE = 221;
    static final int[] LOCODBLCONTROL = new int[]{LOCODBLCONTROLCODE, 0, LOCODBLCONTROLCODE, 0}; //Reply indicates that another throttle also has controll of the loco

    public static final int GOODCMDRECEIVEDCODE = 85;
    static final int[] GOODCMDRECEIVED = new int[]{GOODCMDRECEIVEDCODE, 0, GOODCMDRECEIVEDCODE, 0};

    public static final int BADCMDRECEIVEDCODE = 238; //Or unable to read from decoder
    static final int[] BADCMDRECEIVED = new int[]{BADCMDRECEIVEDCODE, 0, BADCMDRECEIVEDCODE, 0};

    public static final int POWERONCMD = 130;
    static final int[] POWERON = new int[]{POWERONCMD, 0, POWERONCMD, 0, POWERONCMD, 0, POWERONCMD, 0};

    public static final int POWEROFFCMD = 146;
    static final int[] POWEROFF = new int[]{POWEROFFCMD, 0, POWEROFFCMD, 0, POWEROFFCMD, 0, POWEROFFCMD, 0};

    public static int getAddToConsistPacketLength() {
        return ADDTOCONSISTPACKETHEADER.length + ADDTOCONSISTPACKETLENGTH;
    }

    public static int getClearConsistPacketLength() {
        return CLEARCONSISTPACKETHEADER.length + CLEARCONSISTPACKETLENGTH;
    }

    public static int getRouteControlPacketLength() {
        return ROUTECONTROLPACKETHEADER.length + ROUTECONTROLPACKETLENGTH;
    }

    public static int getClearRoutePacketLength() {
        return CLEARROUTEPACKETHEADER.length + CLEARROUTEPACKETLENGTH;
    }

    public static int getAddToRoutePacketLength() {
        return ADDTOROUTEPACKETHEADER.length + ADDTOROUTEPACKETLENGTH;
    }

    public static int getAccessoryPacketLength() {
        return ACCESSORYPACKETHEADER.length + ACCESSORYPACKETLENGTH;
    }

    public static int getWriteCVPROGPacketLength() {
        return WRITECVPROGHEADER.length + WRITECVPROGLENGTH;
    }

    public static int getWriteCVPOMPacketLength() {
        return WRITECVPOMHEADER.length + WRITECVPOMLENGTH;
    }

    public static int getSetClockRatioPacketLength() {
        return SETCLOCKRATIOHEADER.length + SETCLOCKRATIOLENGTH;
    }

    public static int getSetClockAmPmPacketLength() {
        return SETCLOCKAMPMHEADER.length + SETCLOCKAMPMLENGTH;
    }

    public static int getFunctionPacketLength() {
        return FUNCTIONGROUP1PACKETHEADER.length + FUNCTIONGROUPLENGTH;
    }

    public static int getReadDecoderAddressLength() {
        return READDECODERADDRESS.length;
    }

    public static int getSetClockTimePacketLength() {
        return SETCLOCKTIMEHEADER.length + SETCLOCKTIMELENGTH;
    }

    public static int getThrottlePacketLength() {
        return THROTTLEPACKETHEADER.length + THROTTLEPACKETLENGTH;
    }

    public static int getReadCVPacketLength() {
        return READCVHEADER.length + READCVLENGTH;
    }

    public static int getReadCVPacketReplyLength() {
        return READCVHEADERREPLY.length + READCVPACKETLENGTH;
    }

    public static int getPowerOnPacketLength() {
        return POWERON.length;
    }

    public static int getPowerOffPacketLength() {
        return POWERON.length;
    }

    public static boolean startsWith(MrcMessage source, int[] match) {
        if (match.length > (source.getNumDataElements())) {
            return false;
        }
        for (int i = 0; i < match.length; i++) {
            if ((source.getElement(i) & 255) != (match[i] & 255)) {
                return false;
            }
        }
        return true;
    }

    private static final DecimalFormat TWO_DIGITS = new DecimalFormat("00");
    private static final String TXT_ON = Bundle.getMessage("MrcPacketsFunctionOn"); // NOI18N
    private static final String TXT_OFF = Bundle.getMessage("MrcPacketsFunctionOff"); // NOI18N
    //Need to test toString() for POM

    static public String toString(MrcMessage m) {
        StringBuilder txt = new StringBuilder();
        if ((m.getNumDataElements() < 4)
                || (m.getNumDataElements() >= 4 && m.getElement(0) != m.getElement(2) && m.getElement(1) != 0x01)) {  // is && right there?
            // byte 0 and byte 2 should always be the same except for a clock update packet.
            if (m.getNumDataElements() < 4) {
                txt.append(Bundle.getMessage("MrcPacketsShort")); // NOI18N
            } else {
                txt.append(Bundle.getMessage("MrcPacketsError")); // NOI18N
            }
            for (int i = 0; i < m.getNumDataElements(); i++) {
                txt.append(" ");
                txt.append(jmri.util.StringUtil.twoHexFromInt(m.getElement(i) & 0xFF));
            }
        } else {
            switch (m.getElement(0) & 0xFF) {
                case SETCLOCKRATIOCMD:
                    txt.append(Bundle.getMessage("MrcPacketsSetClockRatio")).append(m.getElement(4)); // NOI18N
                    break;
                case SETCLOCKTIMECMD:
                    txt.append(Bundle.getMessage("MrcPacketsSetClockTime")).append(m.getElement(4) // NOI18N
                    ).append(   Bundle.getMessage("MrcPacketsClockTimeSep")).append(m.getElement(6)); // NOI18N
                    break;
                case SETCLOCKAMPMCMD:
                    txt.append(Bundle.getMessage("MrcPacketsSetClockMode")); // NOI18N
                    break;
                case MrcPackets.THROTTLEPACKETCMD:
                    if (m.getElement(4) != 0) {
                        txt.append(Bundle.getMessage("MrcPacketsLoco")).append(" ").append(Bundle.getMessage("MrcPacketsLocoLong")).append(" "); // NOI18N
                    } else {
                        txt.append(Bundle.getMessage("MrcPacketsLoco")).append(" ").append(Bundle.getMessage("MrcPacketsLocoShort")).append(" "); // NOI18N
                    }
                    txt.append(Integer.toString(m.getLocoAddress()));
                    if (m.getElement(10) == 0x02) {
                        txt.append(" ").append(Bundle.getMessage("MrcPackets128ss")); // NOI18N
                        //128 speed step
                        if ((m.getElement(8) & 0x80) == 0x80) {
                            txt.append(" ").append(Bundle.getMessage("MrcPacketsForward")); // NOI18N
                        } else {
                            txt.append(" ").append(Bundle.getMessage("MrcPacketsReverse")); // NOI18N
                        }
                        txt.append(" ").append(Bundle.getMessage("MrcPacketsSpeed")); // NOI18N
                        int speed = (m.getElement(8) & 0x7F) - 1;
                        switch (speed) {
                            case 0:
                                txt.append(" ").append(Bundle.getMessage("MrcPacketsEStop")); // NOI18N
                                break;
                            case -1:
                                txt.append(" ").append(Bundle.getMessage("MrcPacketsStop")); // NOI18N
                                break;
                            default:
                                txt.append(" ").append(Integer.toString(speed));
                                break;
                        }
                    } else if (m.getElement(10) == 0x00) {
                        int value = m.getElement(8);
                        txt.append(" ").append(Bundle.getMessage("MrcPackets28ss")); // NOI18N
                        //28 Speed Steps
                        if ((m.getElement(8) & 0x60) == 0x60) {
                            //Forward
                            value = value - 0x60;
                            txt.append(" ").append(Bundle.getMessage("MrcPacketsForward")); // NOI18N
                        } else {
                            value = value - 0x40;
                            txt.append(" ").append(Bundle.getMessage("MrcPacketsReverse")); // NOI18N
                        }
                        if (((value >> 4) & 0x01) == 0x01) {
                            value = value - 0x10;
                            value = (value << 1) + 1;
                        } else {
                            value = value << 1;
                        }
                        value = value - 3; //Turn into user expected 0-28
                        if (value == -1) {
                            txt.append(" ").append(Bundle.getMessage("MrcPacketsEStop")); // NOI18N
                        } else {
                            if (value < 0) {
                                value = 0;
                            }
                            txt.append(" ").append(Bundle.getMessage("MrcPacketsSpeed")).append(" ").append(Integer.toString(value)); // NOI18N
                        }
                    }

                    break;
                case FUNCTIONGROUP1PACKETCMD:
                    txt.append(Bundle.getMessage("MrcPacketsLoco")).append(" ").append(Integer.toString(m.getLocoAddress()))
                            .append(" ").append(Bundle.getMessage("MrcPacketsGroup1")); // NOI18N
                    txt.append(" F0 ").append(((m.getElement(8) & 0x10) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F1 ").append(((m.getElement(8) & 0x01) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F2 ").append(((m.getElement(8) & 0x02) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F3 ").append(((m.getElement(8) & 0x04) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F4 ").append(((m.getElement(8) & 0x08) != 0 ? TXT_ON : TXT_OFF));
                    break;
                case FUNCTIONGROUP2PACKETCMD:
                    txt.append(Bundle.getMessage("MrcPacketsLoco")).append(" ").append(Integer.toString(m.getLocoAddress()))
                            .append(" ").append(Bundle.getMessage("MrcPacketsGroup2")); // NOI18N
                    txt.append(" F5 ").append(((m.getElement(8) & 0x01) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F6 ").append(((m.getElement(8) & 0x02) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F7 ").append(((m.getElement(8) & 0x04) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F8 ").append(((m.getElement(8) & 0x08) != 0 ? TXT_ON : TXT_OFF));
                    break;
                case FUNCTIONGROUP3PACKETCMD:
                    txt.append(Bundle.getMessage("MrcPacketsLoco")).append(" ").append(Integer.toString(m.getLocoAddress()))
                            .append(" ").append(Bundle.getMessage("MrcPacketsGroup3")); // NOI18N
                    txt.append(" F9 ").append(((m.getElement(8) & 0x01) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F10 ").append(((m.getElement(8) & 0x02) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F11 ").append(((m.getElement(8) & 0x04) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F12 ").append(((m.getElement(8) & 0x08) != 0 ? TXT_ON : TXT_OFF));
                    break;
                case FUNCTIONGROUP4PACKETCMD:
                    txt.append(Bundle.getMessage("MrcPacketsLoco")).append(" ").append(Integer.toString(m.getLocoAddress()))
                            .append(" ").append(Bundle.getMessage("MrcPacketsGroup4")); // NOI18N
                    txt.append(" F13 ").append(((m.getElement(8) & 0x01) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F14 ").append(((m.getElement(8) & 0x02) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F15 ").append(((m.getElement(8) & 0x04) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F16 ").append(((m.getElement(8) & 0x08) != 0 ? TXT_ON : TXT_OFF));
                    break;
                case FUNCTIONGROUP5PACKETCMD:
                    txt.append(Bundle.getMessage("MrcPacketsLoco")).append(" ").append(Integer.toString(m.getLocoAddress()))
                            .append(" ").append(Bundle.getMessage("MrcPacketsGroup5")); // NOI18N
                    txt.append(" F17 ").append(((m.getElement(8) & 0x01) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F18 ").append(((m.getElement(8) & 0x02) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F19 ").append(((m.getElement(8) & 0x04) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F20 ").append(((m.getElement(8) & 0x08) != 0 ? TXT_ON : TXT_OFF));
                    break;
                case FUNCTIONGROUP6PACKETCMD:
                    txt.append(Bundle.getMessage("MrcPacketsLoco")).append(" ").append(Integer.toString(m.getLocoAddress()))
                            .append(" ").append(Bundle.getMessage("MrcPacketsGroup6")); // NOI18N
                    txt.append(" F21 ").append(((m.getElement(8) & 0x01) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F22 ").append(((m.getElement(8) & 0x02) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F23 ").append(((m.getElement(8) & 0x04) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F24 ").append(((m.getElement(8) & 0x08) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F25 ").append(((m.getElement(8) & 0x10) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F26 ").append(((m.getElement(8) & 0x20) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F27 ").append(((m.getElement(8) & 0x40) != 0 ? TXT_ON : TXT_OFF));
                    txt.append(" F28 ").append(((m.getElement(8) & 0x80) != 0 ? TXT_ON : TXT_OFF));
                    break;
                case READCVCMD:
                    int cv = ((m.getElement(4) & 0xff) << 8) + (m.getElement(6) & 0xff);
                    txt.append(Bundle.getMessage("MrcPacketsReadCv")).append(Integer.toString(cv)); // NOI18N
                    break;
                case READDECODERADDRESSCMD:
                    txt.append(Bundle.getMessage("MrcPacketsReadLocoAddr")); // NOI18N
                    break;
                case WRITECVPOMCMD:
                    txt.append(Bundle.getMessage("MrcPacketsWriteOpsCvLoco")).append(" ").append(Integer.toString(m.getLocoAddress())); // NOI18N
                    txt.append(" ").append(Integer.toString((m.getElement(10) & 0xff) + 1));
                    txt.append("=");
                    txt.append(Integer.toString(m.getElement(12) & 0xff));
                    break;
                case WRITECVPROGCMD:
                    txt.append(Bundle.getMessage("MrcPacketsWriteSvrCv")); // NOI18N
                    txt.append(" ").append(Integer.toString(m.getElement(4) << 8)).append(m.getElement(6));
                    txt.append("=");
                    txt.append(Integer.toString(m.getElement(8) & 0xff));
                    break;
                case READCVHEADERREPLYCODE:
                    txt.append(Bundle.getMessage("MrcPacketsReadCvValue")); // NOI18N
                    txt.append(Integer.toString(m.value()));
                    break;
                case BADCMDRECEIVEDCODE:
                    txt.append(Bundle.getMessage("MrcPacketBadCmdAck")); // NOI18N
                    break;
                case GOODCMDRECEIVEDCODE:
                    txt.append(Bundle.getMessage("MrcPacketGoodCmdAck")); // NOI18N
                    break;
                case PROGCMDSENTCODE:
                    txt.append(Bundle.getMessage("MrcPacketsPgmCmdSent")); // NOI18N
                    break;
                case LOCOSOLECONTROLCODE:
                    txt.append(Bundle.getMessage("MrcPacketsSingleThrottle")); // NOI18N
                    break;
                case LOCODBLCONTROLCODE:
                    txt.append(Bundle.getMessage("MrcPacketsMultipleThrottle")); // NOI18N
                    break;
                case POWERONCMD:
                    txt.append(Bundle.getMessage("MrcPacketsTrkPwrOn")); // NOI18N
                    break;
                case POWEROFFCMD:
                    txt.append(Bundle.getMessage("MrcPacketsTrkPwrOff")); // NOI18N
                    break;
                case ADDTOCONSISTPACKETCMD:
                    txt.append(Bundle.getMessage("MrcPacketsLocoAddConsist")); // NOI18N
                    break;
                case CLEARCONSISTPACKETCMD:
                    txt.append(Bundle.getMessage("MrcPacketsClearedConsist")); // NOI18N
                    break;
                case ROUTECONTROLPACKETCMD:
                    txt.append(Bundle.getMessage("MrcPacketsRoute")); // NOI18N
                    txt.append(" ").append(Integer.toString(m.getElement(4)));
                    txt.append(" ").append(Bundle.getMessage("MrcPacketsRouteSet")); // NOI18N
                    break;
                case CLEARROUTEPACKETCMD:
                    txt.append(Bundle.getMessage("MrcPacketsClearedRoute")); // NOI18N
                    break;
                case ADDTOROUTEPACKETCMD:
                    txt.append(Bundle.getMessage("MrcPacketsAddedRoute")); // NOI18N
                    break;
                case ACCESSORYPACKETCMD:
                    txt.append(Bundle.getMessage("MrcPacketsAccy")); // NOI18N
                    txt.append(" ").append(Integer.toString(m.getAccAddress())).append(" ");
                    switch (m.getAccState()) {
                        case jmri.Turnout.CLOSED:
                            txt.append(Bundle.getMessage("MrcPacketsAccyClosed")); // NOI18N
                            break;
                        case jmri.Turnout.THROWN:
                            txt.append(Bundle.getMessage("MrcPacketsAccyThrown")); // NOI18N
                            break;
                        default:
                            txt.append(Bundle.getMessage("MrcPacketsAccyUnk")); // NOI18N
                    }
                    break;
                default:
                    if (m.getNumDataElements() == 6) {
                        if (m.getElement(0) == m.getElement(2) && m.getElement(0) == m.getElement(4)) {
                            txt.append(Bundle.getMessage("MrcPacketsPollToCab")).append(" "); // NOI18N
                            txt.append(m.getElement(0));
                        } else if (m.getElement(0) == 0 && m.getElement(1) == 0x01) {
                            txt.append(Bundle.getMessage("MrcPacketsClockUpdate")).append(" "); // NOI18N

                            appendClockMessage(m, txt);

                            txt.append(" ");
                        }
                    } else if (m.getNumDataElements() == 4 && m.getElement(0) == 0x00 && m.getElement(1) == 0x00) {
                        txt.append("No Data From Last Cab"); // NOI18N
                    } else {
                        txt.append("Unk Cmd Code:"); // NOI18N
                        for (int i = 0; i < m.getNumDataElements(); i++) {
                            txt.append(" ");
                            txt.append(jmri.util.StringUtil.twoHexFromInt(m.getElement(i) & 0xFF));
                        }
                    }
                    break;
            }
        }
        return txt.toString();
    }

    /**
     * Adds the description of the clock's mode to a message being built
     *
     * @param m   clock info message
     * @param txt build description of clock info onto this
     */
    @SuppressFBWarnings(value = "SF_SWITCH_NO_DEFAULT", justification = "covers all possible values")
    static void appendClockMessage(MrcMessage m, StringBuilder txt) {
        int clockModeBits = m.getElement(2) & 0xC0;
        switch (clockModeBits) {
            case 0x00: // AM format
                txt.append((m.getElement(2) & 0x1F))
                        .append(Bundle.getMessage("MrcPacketsClockTimeSep"))
                        .append(TWO_DIGITS.format(m.getElement(4)))
                        .append(Bundle.getMessage("MrcPacketsClockModeAm")); // NOI18N
                break;
            case 0x40: // PM format
                txt.append((m.getElement(2) & 0x1F))
                        .append(Bundle.getMessage("MrcPacketsClockTimeSep"))
                        .append(TWO_DIGITS.format(m.getElement(4)))
                        .append(Bundle.getMessage("MrcPacketsClockModePm")); // NOI18N
                break;
            case 0x80: // 24 hour format
                txt.append(TWO_DIGITS.format(m.getElement(2) & 0x1F))
                        .append(Bundle.getMessage("MrcPacketsClockTimeSep"))
                        .append(TWO_DIGITS.format(m.getElement(4)))
                        .append(Bundle.getMessage("MrcPacketsClockMode24"));//IN18N
                break;
            case 0xC0: // Unk format
                txt.append(TWO_DIGITS.format(m.getElement(2) & 0x1F))
                        .append(Bundle.getMessage("MrcPacketsClockTimeSep"))
                        .append(TWO_DIGITS.format(m.getElement(4)))
                        .append(Bundle.getMessage("MrcPacketsClockModeUnk")); // NOI18N
                break;
            default:
                log.warn("Unhandled clock mode code: {}", clockModeBits);
                break;
        }
    }

    //In principle last two are the checksum, the first four indicate the packet type and ignored.
    //the rest should be XOR'd.
    static public boolean validCheckSum(MrcMessage m) {
        if (m.getNumDataElements() > 6) {
            int result = 0;
            for (int i = 4; i < m.getNumDataElements() - 2; i++) {
                result = (m.getElement(i) & 255) ^ result;
            }
            if (result == (m.getElement(m.getNumDataElements() - 2) & 255)) {
                return true;
            }
        }
        return false;
    }

}
