package jmri.jmrix.rfid.protocol.em18;

import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.rfid.RfidProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common routines to extract the Tag information and validate checksum for
 * implementations that use the EM-18 protocol.
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
 * @author Oscar A. Pruitt Copyright (C) 2015
 * @since 4.1.2
 */
public class Em18RfidProtocol extends RfidProtocol {

    private static final int SPECIFICMAXSIZE = 12;

    /**
     * Constructor for EM-18 RFID Protocol. Used when a single reader is
     * connected directly to a port, not via a concentrator.
     */
    public Em18RfidProtocol() {
        super();
    }

    /**
     * Constructor for EM-18 RFID Protocol. Supports the use of concentrators
     * where a character range is used to determine the specific reader port.
     *
     * @param concentratorFirst  character representing first concentrator port
     * @param concentratorLast   character representing last concentrator port
     * @param portPosition       position of port character in reply string
     */
    public Em18RfidProtocol(char concentratorFirst, char concentratorLast, int portPosition) {
        super(concentratorFirst, concentratorLast, portPosition);
    }

    public static final int getMaxSize() {
        return SPECIFICMAXSIZE;
    }

    @Override
    public String initString() {
        // None required for EM-18
        return "";
    }

    @Override
    public String getTag(AbstractMRReply msg) {
        StringBuilder sb = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            sb.append((char) msg.getElement(i));
        }

        return sb.toString();
    }

    @Override
    public boolean providesChecksum() {
        return true;
    }

    @Override
    public String getCheckSum(AbstractMRReply msg) {
        StringBuilder sb = new StringBuilder(2);

        for (int i = 10; i < 12; i++) {
            sb.append((char) msg.getElement(i));
        }

        return sb.toString();
    }

    @Override
    public boolean isValid(AbstractMRReply msg) {
        return (((!isConcentrator && msg.getElement(0) != 0x02
                && (msg.getElement(SPECIFICMAXSIZE - 1) & 0xFF) != 0x03)
                || (isConcentrator
                && msg.getElement(portPosition) >= concentratorFirst
                && msg.getElement(portPosition) <= concentratorLast
                && (msg.getElement(SPECIFICMAXSIZE - 1) & 0xFF) != 0x3E))
                && isCheckSumValid(msg));
    }

    public boolean isCheckSumValid(AbstractMRReply msg) {
        byte[] tag = convertHexString(getTag(msg));
        int checksum = 0;
        for (int i = 0; i < 5; i++) {
            checksum = checksum ^ tag[i];
            log.debug("read " + tag[i]);
        }
        log.debug("Checksum: " + getCheckSum(msg) + " converted: " + convertHexString(getCheckSum(msg))[0]);
        return checksum == convertHexString(getCheckSum(msg))[0];
    }

    @Override
    public boolean endOfMessage(AbstractMRReply msg) {
        if (msg.getNumDataElements() == SPECIFICMAXSIZE) {
            if (((msg.getElement(SPECIFICMAXSIZE - 1) & 0xFF) == 0x03
                    || (msg.getElement(SPECIFICMAXSIZE - 1) & 0xFF) == 0x3E)
                    && (msg.getElement(SPECIFICMAXSIZE - 2) & 0xFF) == 0x0A
                    && (msg.getElement(SPECIFICMAXSIZE - 3) & 0xFF) == 0x0D) {
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
            sb.append("Reply from EM-18 reader.");
            if (isConcentrator) {
                sb.append(" Reply from port ");
                sb.append(getReaderPort(msg));
            }
            sb.append(" Tag read ");
            sb.append(getTag(msg));
            sb.append(" checksum ");
            sb.append(getCheckSum(msg));
            sb.append(" valid? ");
            sb.append(isCheckSumValid(msg) ? "yes" : "no");
            return sb.toString();
        } else {
            return super.toMonitorString(msg);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(Em18RfidProtocol.class);

}
