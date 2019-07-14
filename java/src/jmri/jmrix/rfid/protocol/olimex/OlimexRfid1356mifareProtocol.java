package jmri.jmrix.rfid.protocol.olimex;

import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.rfid.RfidProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common routines to extract the Tag information and validate checksum for
 * implementations that use the Olimex MOD-RFID1356MIFARE protocol.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris Copyright (C) 2014
 * @author B. Milhaupt    Copyright (C) 2017
 * @since 4.9.4
 */
public class OlimexRfid1356mifareProtocol extends RfidProtocol {

    public static final int SPECIFICMAXSIZE = 13;
    public final String initialize = "mt100\r\ne0\r\n"; //NOI18N

    public static final int getMaxSize() {
        return SPECIFICMAXSIZE;
    }

    @Override
    public String initString() {
        // Continuous scanning, single report per seen tag
        return initialize;
    }

    @Override
    public String getTag(AbstractMRReply msg) {
        StringBuilder sb = new StringBuilder(10);

        for (int i = 3; i < SPECIFICMAXSIZE-2; i++) {
            sb.append((char) msg.getElement(i));
        }

        return sb.toString();
    }

    @Override
    public String getCheckSum(AbstractMRReply msg) {
        return "";
    }

    @Override
    public boolean isValid(AbstractMRReply msg) {
        /* Typical message of "tag receive":
            \r\n-C4178b55\r\n
        */
        return ((!isConcentrator && msg.getElement(2) == 0x2D)
                || (isConcentrator
                && msg.getElement(portPosition) >= concentratorFirst
                && msg.getElement(portPosition) <= concentratorLast))
                && msg.getElement(SPECIFICMAXSIZE - 1) == 0x0A;
    }

    @Override
    public boolean endOfMessage(AbstractMRReply msg) {
        if (msg.getNumDataElements() == SPECIFICMAXSIZE) {
            /* Check end of expected response to tag read message:
                \r\n-C4178b55\r\n
            */
            if (((msg.getElement(SPECIFICMAXSIZE - 1) & 0xFF) == 0x0A)
                    && ((msg.getElement(SPECIFICMAXSIZE - 2) & 0xFF) == 0x0D)
                    && ((msg.getElement(SPECIFICMAXSIZE - 3) & 0xFF) != 0x0D)
                    && ((msg.getElement(SPECIFICMAXSIZE - 3) & 0xFF) != 0x0A)) {
                return true;
            }
            /* Check end of message of expected response to init message:
                mt100
                OK>e0
                OK>

            or, in hex:
                6Dh 74h 31h 30h 30h  0Dh0Ah
                4Fh 4Bh 0Ah  0Dh3Eh 07h 65h 30h  0Dh0Ah
                4Fh 4Bh 0Ah  0Dh3Eh 07h
            */

            int i;
            for (i = 0; i < initialize.length(); ++ i) {
                if (msg.getElement(i) != initialize.charAt(i)) {
                    return false;
                }
            }
            if (msg.getElement(i) != 'O') return false;
            if (msg.getElement(i+1) != 'K') return false;
            if (msg.getElement(i+2) != '\n') return false;
            if (msg.getElement(i+3) != '\r') return false;
            if (msg.getElement(i+4) != '>') return false;
            if (msg.getElement(i+5) != 0x07) return false;
            if (msg.getElement(i+6) != 'e') return false;
            if (msg.getElement(i+7) != '0') return false;
            if (msg.getElement(i+8) != '\n') return false;
            if (msg.getElement(i+9) != '\r') return false;
            if (msg.getElement(i+10) != 'O') return false;
            if (msg.getElement(i+11) != 'K') return false;

            if (log.isDebugEnabled()) {
                log.debug("Not a correctly formed message"); // NOI18N
            }
            return true;
        }
        return false;
    }

    @Override
    public String toMonitorString(AbstractMRReply msg) {
        // check for valid message
        if (isValid(msg)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Reply from Olimex MOD-RFID1356MIFARE reader.");
            if (isConcentrator) {
                sb.append(" Reply from port ");
                sb.append(getReaderPort(msg));
            }
            sb.append(" Tag read ");
            sb.append(getTag(msg));
            return sb.toString();
        } else {
            return super.toMonitorString(msg);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(OlimexRfid1356mifareProtocol.class.getName());

}
