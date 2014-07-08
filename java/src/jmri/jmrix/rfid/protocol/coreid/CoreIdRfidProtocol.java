// CoreIdRfidProtocol.java

package jmri.jmrix.rfid.protocol.coreid;

import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.rfid.RfidProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common routines to extract the Tag information and validate checksum for
 * implementations that use the CORE-ID / ID-Innovations protocol.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author      Matthew Harris  Copyright (C) 2014
 * @version     $Revision$
 */
public class CoreIdRfidProtocol extends RfidProtocol {

    private static final int SPECIFICMAXSIZE = 16;

    public static final int getMaxSize() {
        return SPECIFICMAXSIZE;
    }

    @Override
    public String initString() {
        // None required for CORE-ID
        return "";
    }

    @Override
    public String getTag(AbstractMRReply msg) {
        StringBuilder sb = new StringBuilder(10);

        for (int i=1; i<11; i++) {
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

        for (int i=11; i<13; i++) {
            sb.append((char) msg.getElement(i));
        }

        return sb.toString();
    }

    @Override
    public boolean isValid(AbstractMRReply msg) {
        return ((msg.getElement(0)==0x02 ||
                (msg.getElement(0)>=0x41 || msg.getElement(0)<=0x50)) &&
                ((msg.getElement(SPECIFICMAXSIZE-1)&0xFF)==0x03 ||
                 (msg.getElement(SPECIFICMAXSIZE-1)&0xFF)==0x3E ) &&
                (msg.getElement(SPECIFICMAXSIZE-2)&0xFF)==0x0A &&
                (msg.getElement(SPECIFICMAXSIZE-3)&0xFF)==0x0D);
    }

    public boolean isCheckSumValid(AbstractMRReply msg) {
        byte[] tag = convertHexString(getTag(msg));
        int checksum = 0;
        for (int i=0; i<5; i++) {
            checksum = checksum ^ tag[i];
            log.debug("read "+tag[i]);
        }
        log.debug("Checksum: " + getCheckSum(msg) + " converted: " + convertHexString(getCheckSum(msg))[0]);
        return checksum == convertHexString(getCheckSum(msg))[0];
    }

    @Override
    public boolean endOfMessage(AbstractMRReply msg) {
        if (msg.getNumDataElements()==SPECIFICMAXSIZE) {
            if (((msg.getElement(SPECIFICMAXSIZE-1)&0xFF)==0x03 ||
                 (msg.getElement(SPECIFICMAXSIZE-1)&0xFF)==0x3E ) &&
                (msg.getElement(SPECIFICMAXSIZE-2)&0xFF)==0x0A &&
                (msg.getElement(SPECIFICMAXSIZE-3)&0xFF)==0x0D) {
                return true;
            }
            if (log.isDebugEnabled()) log.debug("Not a correctly formed message");
            return true;
        }
        return false;
    }

    @Override
    public String toMonitorString(AbstractMRReply msg) {
        // check for valid message
        if (isValid(msg)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Reply from CORE-ID reader.");
            sb.append(" Tag read ");
            sb.append(getTag(msg));
            sb.append(" checksum ");
            sb.append(getCheckSum(msg));
            sb.append(" valid? ");
            sb.append(isCheckSumValid(msg)?"yes":"no");
            return sb.toString();
        } else {
            return super.toMonitorString(msg);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(CoreIdRfidProtocol.class.getName());

}

/* @(#)CoreIdRfidProtocol.java */
