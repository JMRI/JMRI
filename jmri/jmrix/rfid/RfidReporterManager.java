// RfidReporterManager.java

package jmri.jmrix.rfid;

import jmri.Reporter;
import jmri.jmrix.rfid.merg.MergRfidReporter;
import jmri.managers.AbstractReporterManager;

/**
 * Rfid implementation of a ReporterManager.
 * <P>
 * System names are "FRpppp", where ppp is a
 * representation of the RFID reader.
 * <P>
 * @author      Bob Jacobsen    Copyright (C) 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision: 1.1 $
 * @since       2.11.4
 */
abstract public class RfidReporterManager extends AbstractReporterManager implements RfidListener {

    private RfidTrafficController tc;
    private String prefix;

    public RfidReporterManager(RfidTrafficController tc, String prefix) {
        super();
        this.tc = tc;
        this.prefix = prefix;
    }

    public String getSystemPrefix() {
        return prefix;
    }

    protected Reporter createNewReporter(String systemName, String userName) {
        log.debug("Create new Reporter: "+systemName);
        if (!systemName.matches(prefix+typeLetter()+"["+tc.getRange()+"]")) {
            log.warn("Invalid Reporter name: " + systemName + " - out of supported range " + tc.getRange());
            throw new IllegalArgumentException("Invalid Reporter name: " + systemName + " - out of supported range " + tc.getRange());
        }
        MergRfidReporter r;
        r = new MergRfidReporter(systemName, userName);
        r.addPropertyChangeListener(this);
        return r;
    }

    public void message(RfidMessage m) {
        log.warn("Unexpected message received"+m);
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RfidReporterManager.class.getName());

}

/* @(#)RfidReporterManager.java */
