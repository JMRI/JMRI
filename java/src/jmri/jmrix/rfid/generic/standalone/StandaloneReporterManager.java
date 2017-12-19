package jmri.jmrix.rfid.generic.standalone;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.jmrix.rfid.RfidMessage;
import jmri.jmrix.rfid.RfidReply;
import jmri.jmrix.rfid.RfidReporterManager;
import jmri.jmrix.rfid.RfidTrafficController;
import jmri.jmrix.rfid.TimeoutRfidReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rfid implementation of a ReporterManager.
 * <P>
 * System names are "FRpppp", where ppp is a representation of the RFID reader.
 * <P>
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class StandaloneReporterManager extends RfidReporterManager {

    private final RfidTrafficController tc;
    private final String prefix;

    public StandaloneReporterManager(RfidTrafficController tc, String prefix) {
        super(prefix);
        this.tc = tc;
        this.prefix = prefix;
        attach();
    }

    private void attach() {
        tc.addRfidListener(this);
    }

    @Override
    protected Reporter createNewReporter(String systemName, String userName) {
        log.debug("Create new Reporter: " + systemName);
        if (!systemName.matches(prefix + typeLetter() + "[" + tc.getRange() + "]")) {
            log.warn("Invalid Reporter name: " + systemName + " - out of supported range " + tc.getRange());
            throw new IllegalArgumentException("Invalid Reporter name: " + systemName + " - out of supported range " + tc.getRange());
        }
        TimeoutRfidReporter r;
        r = new TimeoutRfidReporter(systemName, userName);
        r.addPropertyChangeListener(this);
        return r;
    }

    @Override
    public void message(RfidMessage m) {
        if (m.toString().equals(new StandaloneMessage(tc.getAdapterMemo().getProtocol().initString(), 0).toString())) {
            log.info("Sent init string: " + m);
        } else {
            super.message(m);
        }
    }

    @Override
    public synchronized void reply(RfidReply r) {
        if (r instanceof StandaloneReply) {
            processReply((StandaloneReply) r);
        }
    }

    private void processReply(StandaloneReply r) {
        if (!tc.getAdapterMemo().getProtocol().isValid(r)) {
            log.warn("Invalid message - skipping " + r);
            return;
        }
        IdTag idTag = InstanceManager.getDefault(IdTagManager.class).provideIdTag(tc.getAdapterMemo().getProtocol().getTag(r));
        TimeoutRfidReporter report = (TimeoutRfidReporter) provideReporter(prefix + typeLetter() + "1");
        report.notify(idTag);
    }

    private static final Logger log = LoggerFactory.getLogger(StandaloneReporterManager.class);

}
