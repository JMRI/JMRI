package jmri.jmrix.rfid.protocol.parallax;

import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.rfid.RfidProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common routines to extract the Tag information and validate checksum for
 * implementations that use the Parallax protocol.
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
 * <p>
 *
 * @author Matthew Harris Copyright (C) 2014
 * @since 3.9.2
 */
public class ParallaxRfidProtocol extends RfidProtocol {

    public static final int SPECIFICMAXSIZE = 12;

    public static final int getMaxSize() {
        return SPECIFICMAXSIZE;
    }

    @Override
    public String initString() {
        // None required for Parallax
        return "";
    }

    @Override
    public String getTag(AbstractMRReply msg) {
        StringBuilder sb = new StringBuilder(10);

        for (int i = 1; i < 11; i++) {
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
        return msg.getElement(0) == 0x0A
                && msg.getElement(SPECIFICMAXSIZE - 1) == 0x0D;
    }

    @Override
    public boolean endOfMessage(AbstractMRReply msg) {
        if (msg.getNumDataElements() == SPECIFICMAXSIZE) {
            if ((msg.getElement(0)) == 0x0A
                    && (msg.getElement(SPECIFICMAXSIZE - 1)) == 0x0D) {
                return true;
            }
            if (log.isDebugEnabled()) {
                log.debug("Not a correctly formed message");
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
            sb.append("Reply from Parallax reader.");
            sb.append(" Tag read ");
            sb.append(getTag(msg));
            return sb.toString();
        } else {
            return super.toMonitorString(msg);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ParallaxRfidProtocol.class);

}
