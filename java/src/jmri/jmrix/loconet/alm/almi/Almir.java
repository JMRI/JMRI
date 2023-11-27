package jmri.jmrix.loconet.alm.almi;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.alm.Alm;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * ALM Interpretation for Routes
 *
 * @author Bob Milhaupt  Copyright (C) 2022
 */
public class Almir {
    private Almir () {
        throw new IllegalStateException("Utility class"); // NOI18N
    }

    static String interpretAlmRoutes(LocoNetMessage l) {
        if ((l.getElement(2) != 1) && (l.getElement(2) != 2)) {
            return EMPTY;
        }
        if (l.getNumDataElements() != 0x10) {
            return EMPTY;
        }
        if (l.equals(DESEL)) {
            return Bundle.getMessage("MSG_LN_ALM_DEVICE_DESEL");
        }

        String ret;
        ret = cardq(l);
        if (ret.length() > 1) {
            return ret;
        }
        String key = EMPTY;
        if ((l.getOpCode() == 0xE6) && (l.getElement(3) == 0x02)) {
            if (l.getElement(2) == 1) {
                key = "LN_MSG_ALM_ROUTE_CMD_STN_REPORT"; // NOI18N
            } else if (l.getElement(2) != 2) {
                return EMPTY;
            } else {
                key = "LN_MSG_ALM_ROUTE_DEV_REPORT"; // NOI18N
            }
        } else if ((l.getOpCode() == 0xEE) && (l.getElement(3) == 3)) {
            if (l.getElement(2) == 1) {
                key = "LN_MSG_ALM_ROUTE_CMD_STN_WRITE"; // NOI18N
            } else if (l.getElement(2) != 2) {
                return EMPTY;
            } else {
                key = "LN_MSG_ALM_ROUTE_DEV_WRITE"; // NOI18N
            }
        }
        if (key.length() > 1) {
            return Bundle.getMessage(key,
                    1 + (((l.getElement(4) + l.getElement(5)*128)/2) & 0x7f),
                    1 + ((l.getElement(4) & 0x1)<< 2),
                    4 + ((l.getElement(4) & 0x1)<< 2),
                    1 + (((l.getElement(4) + l.getElement(5)*128)/4) & 0x3F),
                    1 + ((l.getElement(4) & 0x3) << 2),
                    4 + ((l.getElement(4) & 0x3) << 2),
                    getTurnoutNum(l, 0), getTurnoutStat(l, 0),
                    getTurnoutNum(l, 1), getTurnoutStat(l, 1),
                    getTurnoutNum(l, 2), getTurnoutStat(l, 2),
                    getTurnoutNum(l, 3), getTurnoutStat(l, 3));
        }

        ret = checkarcq(l);
        if (ret.length() > 1) {
            return ret;
        }

        ret = checkCsrc1(l);
        if (ret.length()>1) {
            return ret;
        }

        ret = checkCsrc2(l);
        if (ret.length()>1) {
            return ret;
        }

        ret = dealWithAlmStyle2(l);
        if (ret.length() > 1) {
            return ret;
        }

        return EMPTY;
    }

    private static String checkarcq(LocoNetMessage l) {
        if ((l.equals(almRouteCapabilitiesQuery)) || (l.equals(almRouteCapabilitiesQuery2))) {
            return Bundle.getMessage("LN_MSG_ALM_RTS_CAP_Q");
        }
        return EMPTY;
    }

    private static String cardq(LocoNetMessage l) {
        if (l.equals(AlmRoutesDataQuery, ardqm)) {
            return Bundle.getMessage("LN_MSG_CMD_STN_ROUTE_QUERY",
                    getRouteNum(l),
                    getTurnoutGroup(l),
                    (getTurnoutGroup(l) + 3),
                    getAltRouteNum(l),
                    getAltTurnoutGroup(l),
                    (getAltTurnoutGroup(l) + 3));
            }
        return EMPTY;
    }

    private static int getTurnoutGroup(LocoNetMessage l) {
        return 1 + ((l.getElement(4) & 0x1)<< 2);
    }

    private static int getAltTurnoutGroup(LocoNetMessage l) {
        return 1 + ((l.getElement(4) & 0x3) << 2);
    }

    private static int getRouteNum(LocoNetMessage l) {
        return 1 + (((l.getElement(4) + l.getElement(5)*128)/2) & 0x7f);
    }

    private static int getAltRouteNum(LocoNetMessage l) {
        return 1 + (((l.getElement(4) + l.getElement(5)*128)/4) & 0x3F);
    }

    private static String checkCsrc1 (LocoNetMessage l) {
        if (l.equals(cmdStnRoutesCap)) {
            return Bundle.getMessage("LN_MSG_ALM_RTS_CAP_R");
        }
        return EMPTY;
    }

    private static String checkCsrc2 (LocoNetMessage l) {
        if (l.equals(cmdStnRoutesCap2)) {
            return Bundle.getMessage("LN_MSG_ALM_RTS_CAP_R2");
        }
        return EMPTY;
    }

    private static String getTurnoutNum(LocoNetMessage l, int num) {
        if ((l.getElement(7+(num*2)) == 0x7f) && (l.getElement(8+(num*2)) == 0x7f)) {
            return Bundle.getMessage("LN_ROUTE_UNUSED_ENTRY_HELPER");
        }
        if ((num < 0) || (num > 3)) {
            throw new java.lang.IllegalArgumentException();
        }
        int val = 1 + l.getElement(7+(num*2)) + ((l.getElement(8+(num*2)) & 15)<<7);
        return Integer.toString(val);
    }

    private static String getTurnoutStat(LocoNetMessage l, int num) {
        if ((l.getElement(7+(num*2)) == 0x7f) && (l.getElement(8+(num*2)) == 0x7f)) {
            return "";
        }
        if ((num <0) || (num > 3)) {
            throw new java.lang.IllegalArgumentException();
        }
        boolean isClosed;
        isClosed = (l.getElement(8 + (num*2)) & 0x20) == 0x20;
        return isClosed ? Bundle.getMessage("LN_SW_CLOSED")
                        :Bundle.getMessage("LN_SW_THROWN");
    }

    private static String dealWithAlmStyle2(LocoNetMessage l) {
        int sn = getSN(l);
        String ser = Integer.toHexString(sn);
        int bs = getBS(l); // starting address
        int be;            // ending address
        String enable;
        boolean enb = getEnb(l);
        enable = (enb ? Bundle.getMessage("LN_MSG_HELPER_DISABLED")
                : Bundle.getMessage("LN_MSG_HELPER_ENABLED"));
        DevMode mod = getMode(l);
        String mode;
        String dev;
        int rts;            // number of routes
        int ents;           // number of entries in routes
        switch (l.getElement(9)) {
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS74:
                dev = "DS74"; //NOI18N
                rts = 8;
                ents = 8;
                be = ((mod == DevMode.DS74_LIGHT)?(bs + 7):(bs + 3));
                break;
            case LnConstants.RE_IPL_DIGITRAX_HOST_DS78V:
                dev = "DS78V"; //NOI18N
                rts = 16;
                ents = 8;
                be = ((mod == DevMode.DS78V_3_POS)?(bs + 15):(bs + 7));
                break;
            case LnConstants.RE_IPL_DIGITRAX_HOST_SE74:
                dev = "SE74"; //NOI18N
                rts = 64;
                ents = 16;
                be = bs + 36;
                break;
            case LnConstants.RE_IPL_DIGITRAX_HOST_PM74:
                dev = "PM74"; //NOI18N
                rts = 0;
                ents = 0;
                be = bs + 7;
                break;

            default:
                dev = Bundle.getMessage("LN_MSG_ALM_HELPER_DEVICE_UNKNOWN");
                be = bs;
                rts = 0;
                ents = 0;
        }

        int rn = 1 + (l.getElement(4) / 2) + ((l.getElement(5) & 3)<<6);
        int re = ((l.getElement(4)& 1) == 1)?5:1;
        if (Alm.isDs7xRQ(l)) {
            // This code (and associated key/value pair) will require update if
            // any device is introduced which supports ALM-based routes with anything
            // other than 16 entries.
            return Bundle.getMessage("LN_MSG_ALM_SEL_ROUTE_QUERY", rn, re, re+3);
        }

        if (Alm.isDs74CapsRpt(l) || Alm.isDs78vCapsRpt(l) ||
                Alm.isSe74CapsRpt(l) || Alm.isPm74CapsRpt(l) ) {
            if (Alm.isDs74CapsRpt(l) || Alm.isDs78vCapsRpt(l) ) {
                switch ((l.getElement(10) & 0x1e) >>1) {
                    case 0:
                        mode = "LN_MSG_ALM_HELPER_DEV_MODE_PS"; // NOI18N
                        be = bs + 3;
                        break;
                    case 1:
                        mode = "LN_MSG_ALM_HELPER_DEV_MODE_SM"; // NOI18N
                        be = bs + 3;
                        break;
                    case 2:
                        mode = "LN_MSG_ALM_HELPER_DEV_MODE_S2"; // NOI18N
                        be = bs + 7;
                        break;
                    case 5:
                        mode = "LN_MSG_ALM_HELPER_DEV_MODE_LT"; // NOI18N
                        be = bs + 7;
                        break;
                    case 6:
                        mode = "LN_MSG_ALM_HELPER_DEV_MODE_S3"; // NOI18N
                        be = bs + 15;
                        break;
                    default:
                        mode = "LN_MSG_ALM_HELPER_DEV_MODE_UNDEF"; // NOI18N
                        be = bs;
                        break;
                }
            } else if (Alm.isSe74CapsRpt(l)) { // element 10 observed at 0
                mode = "LN_MSG_ALM_HELPER_DEV_MODE_UNDEF"; // NOI18N
                // addressing has already been set above
            } else if (Alm.isPm74CapsRpt(l)) { // element 10 observed at 0
                mode = "LN_MSG_ALM_HELPER_DEV_MODE_UNDEF"; // NOI18N
                // addressing has already been set above
            } else {
                be = bs;  // only show one address
                mode = "LN_MSG_ALM_HELPER_DEV_MODE_UNDEF"; // NOI18N
            }

            mode = Bundle.getMessage(mode);

            if (Alm.isPm74CapsRpt(l)) {
                return Bundle.getMessage("LN_MSG_DEVICE_NO_ROUTES_CAPABILITIES_REPLY",
                   dev, ser, bs );
            }

            return Bundle.getMessage("LN_MSG_DEVICE_ROUTES_CAPABILITIES_REPLY",
                   dev, ser, mode, enable, bs, be, rts, ents );
        }

        LocoNetMessage seldev = new LocoNetMessage(new int[] {
            0xee, 0x10, 0x02, 0x0e, 0,0,0,0,0,0,0,0,0,0,0,0
        });
        int sdm[] = {255,255,255,255,255,255,255,255,255,0,0,0,0,0,0,0};
        if (l.equals(seldev, sdm)) {
            return Bundle.getMessage("LN_MSG_DEVICE_ROUTES_SELECT_REQUEST",
                    dev, ser, bs, be);
        }
        seldev.setElement(0, LnConstants.OPC_ALM_READ);
        sdm[4] = 0; sdm[5] = 0; sdm[6] = 0; sdm[7] = 0; sdm[8] = 0;
        if (l.equals(seldev, sdm)) {
           return Bundle.getMessage("LN_MSG_DEV_ROUTES_SELECT_REPLY", dev, ser, Integer.toString(bs), Integer.toString(be));
        }

        if (Alm.isDevBAW(l)) {
            // change starting turnout address
            return Bundle.getMessage("LN_MSG_ALM_DEVICE_CHG_SA", dev, ser, Integer.toString(bs));
        }

        return EMPTY;
    }

    private static int getSN(LocoNetMessage l) {
        return l.getElement(11) + (l.getElement(12)<<7);
    }

    private static int getBS(LocoNetMessage l) {
        return l.getElement(13) + (l.getElement(14)<<7) + 1;
    }

    private static boolean getEnb(LocoNetMessage l) {
        return (l.getElement(10)& 0x40) == 0x40;
    }
    private static DevMode getMode(LocoNetMessage l) {
        if (l.getElement(9) == 0x74) {
            switch (l.getElement(10) & 0x1E) {
                case 0:
                    return DevMode.DS74_SOLE;
                case 2:
                    return DevMode.DS74_STALL;
                case 0xA:
                    return DevMode.DS74_LIGHT;
                default:
                    break;
            }
        } else if (l.getElement(9) == 0x7c) {
            switch (l.getElement(10) & 0xF) {
                case 4:
                    return DevMode.DS78V_2_POS;
                case 0xC:
                    return DevMode.DS78V_3_POS;
                default:
                    break;
            }
        }
        return DevMode.UNKN;
    }

    private static final String EMPTY = "";

    private static final LocoNetMessage DESEL = new LocoNetMessage(new int[] {
    0xEE, 0x10, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3});

    private static final LocoNetMessage almRouteCapabilitiesQuery = new LocoNetMessage(new int[] {
        0xEE, 0x10, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0});

    private static final LocoNetMessage almRouteCapabilitiesQuery2 = new LocoNetMessage(new int[] {
        0xEE, 0x10, 1, 0, 0, 0, 15, 0,
        0, 0, 0, 0, 0, 0, 0, 0});

    private static final LocoNetMessage cmdStnRoutesCap = new LocoNetMessage(new int[] {
            0xE6, 0x10, 1, 0, 0x40, 0, 3, 2, 8,
            0x7F, 0, 0, 0, 0, 0, 0x64});

    private static final LocoNetMessage cmdStnRoutesCap2 = new LocoNetMessage(new int[] {
            0xE6, 0x10, 1, 0, 0, 2, 3, 2, 0x10,
            0x7F, 0, 0, 0, 0, 0, 0x64});

    private static final LocoNetMessage AlmRoutesDataQuery = new LocoNetMessage(new int[] {
        0xEE, 0x10, 1, 2, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0});

    private static final int[] ardqm = new int[] {255, 255, 255, 255, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0};

    private enum DevMode {
        DS74_SOLE,
        DS74_STALL,
        DS74_LIGHT,
        DS78V_2_POS,
        DS78V_3_POS,
        UNKN
    }
//    private final static Logger log = LoggerFactory.getLogger(Almir.class);

}
