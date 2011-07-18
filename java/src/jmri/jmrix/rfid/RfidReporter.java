// RfidReporter.java

package jmri.jmrix.rfid;

import jmri.IdTag;
import jmri.implementation.AbstractReporter;

/**
 * Extend AbstractReporter for RFID systems
 * <P>
 * System names are "FRpppp", where ppp is a
 * representation of the RFID reader.
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
 * @author      Matthew Harris  Copyright (c) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class RfidReporter extends AbstractReporter
                        implements RfidTagListener {

    public RfidReporter(String systemName) {
        super(systemName);
    }

    public RfidReporter(String systemName, String userName) {
        super(systemName, userName);
    }

    public void notify(IdTag id) {
        log.debug("Notify: "+this.mSystemName);
        if (id!=null) {
            log.debug("Tag: "+id);
            RfidReporter r;
            if ((r = (RfidReporter) id.getWhereLastSeen())!=null) {
                log.debug("Previous reporter: "+r.mSystemName);
                if (r!=this && r.getCurrentReport()==id) {
                    log.debug("Notify previous");
                    r.notify(null);
                } else {
                    log.debug("Current report was: "+r.getCurrentReport());
                }
            }
            id.setWhereLastSeen(this);
            log.debug("Seen here: "+this.mSystemName);
        }
        setReport(id);
        setState(id!=null?IdTag.SEEN:IdTag.UNSEEN);
    }

    private int state = UNKNOWN;

    public void setState(int s) {
        state = s;
    }

    public int getState() {
        return state;
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RfidReporter.class.getName());

}

/* @(#)RfidReporter.java */