// CoreIdRfidReply.java

package jmri.jmrix.rfid.coreid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.rfid.RfidReply;
import jmri.jmrix.rfid.RfidTrafficController;

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
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 */
abstract public class CoreIdRfidReply extends RfidReply {

    public static final int SPECIFICMAXSIZE = 16;

    RfidTrafficController tc = null;

    // create a new one
    public CoreIdRfidReply(RfidTrafficController tc) {
        super(tc);
        this.tc = tc;
        setBinary(true);
        setUnsolicited();
    }
    public CoreIdRfidReply(RfidTrafficController tc, String s) {
        super(tc, s);
        this.tc = tc;
        setBinary(true);
        setUnsolicited();
    }
    public CoreIdRfidReply(RfidTrafficController tc, RfidReply l) {
        super(tc, l);
        this.tc = tc;
        setBinary(true);
        setUnsolicited();
    }

    @Override
    public String getTag() {
        StringBuilder sb = new StringBuilder(10);

        for (int i=1; i<11; i++) {
            sb.append((char) getElement(i));
        }

        return sb.toString();
    }
    
    public String getCheckSum() {
        StringBuilder sb = new StringBuilder(2);

        for (int i=11; i<13; i++) {
            sb.append((char) getElement(i));
        }

        return sb.toString();        
    }

    public boolean isCheckSumValid() {
        byte[] tag = convertHexString(getTag());
        int checksum = 0;
        for (int i=0; i<5; i++) {
            checksum = checksum ^ tag[i];
            log.debug("read "+tag[i]);
        }
        log.debug("Checksum: " + getCheckSum() + " converted: " + convertHexString(getCheckSum())[0]);
        return checksum == convertHexString(getCheckSum())[0];
    }

    private static final Logger log = LoggerFactory.getLogger(CoreIdRfidReply.class.getName());

}

/* @(#)CoreIdRfidReply.java */
