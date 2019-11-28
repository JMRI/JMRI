package jmri.jmrix.rfid.merg.concentrator;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.jmrix.rfid.RfidMessage;
import jmri.jmrix.rfid.RfidReply;
import jmri.jmrix.rfid.RfidReporterManager;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.jmrix.rfid.RfidTrafficController;
import jmri.jmrix.rfid.TimeoutRfidReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rfid implementation of a ReporterManager.
 * <p>
 * System names are "FnRpppp", where Fn is the connection prefix, ppp is a representation of the RFID reader.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class ConcentratorReporterManager extends RfidReporterManager {

    private final RfidTrafficController tc;

    public ConcentratorReporterManager(RfidSystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getTrafficController();
        attach();
    }

    private void attach() {
        tc.addRfidListener(this);
    }

    @Override
    protected Reporter createNewReporter(String systemName, String userName) {
        log.debug("Create new Reporter: " + systemName);
        if (!systemName.matches(getSystemNamePrefix() + "[" + tc.getRange() + "]")) {
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
        if (m.toString().equals(new ConcentratorMessage(tc.getAdapterMemo().getProtocol().initString(), 0).toString())) {
            log.info("Sent init string: " + m);
        } else {
            super.message(m);
        }
    }

    @Override
    public synchronized void reply(RfidReply r) {
        if (r instanceof ConcentratorReply) {
            processReply((ConcentratorReply) r);
        }
    }

    private void processReply(ConcentratorReply r) {
        if (!tc.getAdapterMemo().getProtocol().isValid(r)) {
            log.warn("Invalid message - skipping " + r);
            return;
        }
        if (!r.isInRange()) {
            log.warn("Invalid concentrator reader range - skipping " + r);
            return;
        }
        IdTag idTag = InstanceManager.getDefault(IdTagManager.class).provideIdTag(tc.getAdapterMemo().getProtocol().getTag(r));
        TimeoutRfidReporter report = (TimeoutRfidReporter) provideReporter(getSystemNamePrefix() + r.getReaderPort());
        report.notify(idTag);
    }

    private static final Logger log = LoggerFactory.getLogger(ConcentratorReporterManager.class);

}
