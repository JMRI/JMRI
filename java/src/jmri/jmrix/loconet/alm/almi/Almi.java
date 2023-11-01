package jmri.jmrix.loconet.alm.almi;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.messageinterp.LocoNetMessageInterpret;


/**
 * Provides displayable interpretation of various ALM messages.
 *
 * @author Bob Milhaupt  Copyright (C) 2022
 */

public class Almi {
    private static final String EMPTY = "";
    private Almi () {
        throw new IllegalStateException("Utility class"); // NOI18N
    }

    public static String interpretAlm(LocoNetMessage l) {
        if ((l.getOpCode() != LnConstants.OPC_ALM_READ) &&
                (l.getOpCode() != LnConstants.OPC_ALM_WRITE)) {
            return EMPTY;
        }
        if ((l.getNumDataElements() != 16) || (l.getElement(1) != 0x10)) {
            return EMPTY;
        }

        String ret;
        ret = dealWithAlmAliases(l);
        if (ret.length() > 1) {
            return ret;
        }
        ret = Almir.interpretAlmRoutes(l);
        if (ret.length() > 1) {
            return ret;
        }
        return EMPTY;
    }

    private static String dealWithAlmAliases(LocoNetMessage l) {
        if (l.getElement(2) != 0) {
            return EMPTY;
        }
        if ((l.getElement(3) == 0)
                && (l.getElement(6) == 0)) {
            return Bundle.getMessage("LN_MSG_QUERY_ALIAS_INFO");
        }
        if ((l.getElement(3) == 0)
                && (l.getElement(6) == 0x0b)) {
            return Bundle.getMessage("LN_MSG_ALIAS_INFO_REPORT", l.getElement(4) * 2);
        }
        if ((l.getElement(3) == 2) && (l.getElement(6) == 0xf)
                && (l.getElement(14) == 0)) {
            // Alias read and write messages
            String message;
            if (l.getOpCode() == LnConstants.OPC_ALM_WRITE) {
                return Bundle.getMessage("LN_MSG_QUERY_ALIAS", l.getElement(4));
            }

            message = "LN_MSG_REPORT_ALIAS_2_ALIASES"; // NOI18N

            String longAddr = LocoNetMessageInterpret.convertToMixed(l.getElement(7), l.getElement(8));
            int shortAddr = l.getElement(9);
            String longAddr2 = LocoNetMessageInterpret.convertToMixed(l.getElement(11), l.getElement(12));
            int shortAddr2 = l.getElement(13);
            int pair = l.getElement(4);
            return Bundle.getMessage(message, pair,
                    longAddr, shortAddr, longAddr2, shortAddr2);
        }
        if ((l.getElement(3) == 0x43)) {
            String longAddr = LocoNetMessageInterpret.convertToMixed(l.getElement(7), l.getElement(8));
            int shortAddr = l.getElement(9);
            String longAddr2 = LocoNetMessageInterpret.convertToMixed(l.getElement(11), l.getElement(12));
            int shortAddr2 = l.getElement(13);
            int pair = l.getElement(4);
            return Bundle.getMessage("LN_MSG_SET_ALIAS_2_ALIASES",
                    pair, longAddr, shortAddr, longAddr2, shortAddr2);
        } else if ((l.getElement(6) == 0)
                && (l.getElement(14) == 0)) {
            return Bundle.getMessage("LN_MSG_QUERY_ALIAS", l.getElement(4));
        }
        return EMPTY;
    }

}
