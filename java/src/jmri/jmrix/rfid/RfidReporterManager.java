package jmri.jmrix.rfid;

import jmri.managers.AbstractReporterManager;
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
abstract public class RfidReporterManager extends AbstractReporterManager implements RfidListener {

    private final String prefix;

    public RfidReporterManager(String prefix) {
        super();
        this.prefix = prefix;
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public void message(RfidMessage m) {
        log.warn("Unexpected message received: " + m);
    }

    private static final Logger log = LoggerFactory.getLogger(RfidReporterManager.class.getName());

}
