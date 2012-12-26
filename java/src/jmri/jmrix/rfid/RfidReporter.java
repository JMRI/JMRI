// RfidReporter.java

package jmri.jmrix.rfid;

import jmri.IdTag;
import jmri.implementation.AbstractReporter;

import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.PhysicalLocationReporter;
import jmri.util.PhysicalLocation;
import jmri.InstanceManager;
import jmri.ReporterManager;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


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
    implements RfidTagListener, PhysicalLocationReporter {

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

    // Methods to support PhysicalLocationReporter interface

    /** getLocoAddress() 
     *
     * get the locomotive address we're reporting about from the
     * current report.
     *
     * Note: We ignore the string passed in, because rfid Reporters
     * don't send String type reports.
     */
    public LocoAddress getLocoAddress(String rep) {
	// For now, we assume the current report.
	// IdTag.getTagID() is a system-name-ized version of the loco address. I think.
	// Matcher.group(1) : loco address (I think)
	IdTag cr = (IdTag)this.getCurrentReport();
	ReporterManager rm = InstanceManager.reporterManagerInstance();
	Pattern p = Pattern.compile(""+rm.getSystemPrefix()+rm.typeLetter()+"(\\d+)");
	Matcher m = p.matcher(cr.getTagID());
	if (m.find()) {
	    log.debug("Parsed address: " + m.group(1));
	    // I have no idea what kind of loco address an Ecos reporter uses,
	    // so we'll default to DCC for now.
	    return(new DccLocoAddress(Integer.parseInt(m.group(1)), LocoAddress.Protocol.DCC));
	} else {
	    return(null);
	}
    }
    
    /** getDirection()
     *
     * Gets the direction (ENTER/EXIT) of the report.  Because of the
     * way rfid Reporters work (or appear to), all reports are ENTER type.
     */
    public PhysicalLocationReporter.Direction getDirection(String rep) {
	// TEMPORARY:  Assume we're always Entering, if asked.
	return(PhysicalLocationReporter.Direction.ENTER);
    }
    
    /** getPhysicalLocation()
     *
     * Returns the PhysicalLocation of the Reporter
     *
     * Reports its own location, for now.  Not sure if that's the right thing or not.
     * NOT DONE YET
     */
    public PhysicalLocation getPhysicalLocation() {
	return(this.getPhysicalLocation(null));
    }
    
    /** getPhysicalLocation(String s)
     *
     * Returns the PhysicalLocation of the Reporter
     *
     * Does not use the parameter s
     */
    public PhysicalLocation getPhysicalLocation(String s) {
	return(PhysicalLocation.getBeanPhysicalLocation(this));
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RfidReporter.class.getName());

}

/* @(#)RfidReporter.java */