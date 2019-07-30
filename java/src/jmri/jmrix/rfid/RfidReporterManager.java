package jmri.jmrix.rfid;

import jmri.managers.AbstractReporterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rfid implementation of a ReporterManager.
 * <p>
 * System names are "FRpppp", where ppp is a representation of the RFID reader.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
abstract public class RfidReporterManager extends AbstractReporterManager implements RfidListener {

    public RfidReporterManager(RfidSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RfidSystemConnectionMemo getMemo() {
        return (RfidSystemConnectionMemo) memo;
    }
    @Override
    public void message(RfidMessage m) {
        log.warn("Unexpected message received: " + m);
    }

    private static final Logger log = LoggerFactory.getLogger(RfidReporterManager.class);

}
