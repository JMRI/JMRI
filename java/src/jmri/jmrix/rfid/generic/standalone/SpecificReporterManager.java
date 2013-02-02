// RfidReporterManager.java

package jmri.jmrix.rfid.generic.standalone;

import org.apache.log4j.Logger;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.jmrix.rfid.RfidReply;
import jmri.jmrix.rfid.RfidReporterManager;
import jmri.jmrix.rfid.RfidTrafficController;
import jmri.jmrix.rfid.coreid.CoreIdRfidReporter;

/**
 * Rfid implementation of a ReporterManager.
 * <P>
 * System names are "FRpppp", where ppp is a
 * representation of the RFID reader.
 * <P>
 * @author      Bob Jacobsen    Copyright (C) 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class SpecificReporterManager extends RfidReporterManager {

    private RfidTrafficController tc;
    private String prefix;

    public SpecificReporterManager(RfidTrafficController tc, String prefix) {
        super(prefix);
        this.tc = tc;
        this.prefix = prefix;
        attach();
    }

    private void attach() {
        tc.addRfidListener(this);
    }

    protected Reporter createNewReporter(String systemName, String userName) {
        log.debug("Create new Reporter: "+systemName);
        if (!systemName.matches(prefix+typeLetter()+"["+tc.getRange()+"]")) {
            log.warn("Invalid Reporter name: " + systemName + " - out of supported range " + tc.getRange());
            throw new IllegalArgumentException("Invalid Reporter name: " + systemName + " - out of supported range " + tc.getRange());
        }
        CoreIdRfidReporter r;
        r = new CoreIdRfidReporter(systemName, userName);
        r.addPropertyChangeListener(this);
        return r;
    }

    public synchronized void reply(RfidReply r) {
        if (r instanceof SpecificReply)
            processReply((SpecificReply) r);
    }

    private void processReply(SpecificReply r) {
        if (!r.isCheckSumValid()) {
            log.warn("Invalid checksum - skipping " + r);
            return;
        }
        IdTag idTag = InstanceManager.getDefault(IdTagManager.class).provideIdTag(r.getTag());
        CoreIdRfidReporter report = (CoreIdRfidReporter) provideReporter(prefix+typeLetter()+"1");
        report.notify(idTag);
    }

    private static final Logger log = Logger.getLogger(SpecificReporterManager.class.getName());

}

/* @(#)SpecificReporterManager.java */
