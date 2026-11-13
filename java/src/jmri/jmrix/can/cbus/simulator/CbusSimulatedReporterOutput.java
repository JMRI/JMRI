package jmri.jmrix.can.cbus.simulator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InvokeOnAnyThread;
import jmri.InvokeOnLayoutThread;
import jmri.Manager;
import jmri.Reporter;
import jmri.jmrit.simulation.SimulatedReporterOutput;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusReporter;
import jmri.util.ThreadingUtil;

/**
 * Reports simulated trains' ID tags on a CAN Simulation connection's CBUS
 * reporters, as a classic MERG RFID reader on the bus does: each read is an
 * accessory data event (ACDAT) from the reader's number, carrying the tag's
 * five bytes, received at the time the train came in. A reader reports reads
 * only, not that a train has gone; the reporter clears itself after its
 * timeout, if that is set.
 * <p>
 * A tag is 10 hex digits, its five bytes high first, as classic RFID tags
 * are named; a train with any other tag is not read, with a warning.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@javax.annotation.concurrent.ThreadSafe
public class CbusSimulatedReporterOutput implements SimulatedReporterOutput {

    private final CanSystemConnectionMemo memo;
    private final CbusSimulatedSend send;
    private volatile boolean disposed = false;

    /**
     * @param memo the CAN Simulation connection
     */
    public CbusSimulatedReporterOutput(@Nonnull CanSystemConnectionMemo memo) {
        this.memo = memo;
        this.send = new CbusSimulatedSend(memo);
    }

    /**
     * {@inheritDoc}
     * <p>
     * True for a CBUS reporter of this connection, by its system name's
     * prefix.
     */
    @Override
    @InvokeOnLayoutThread
    public boolean reports(@Nonnull Reporter reporter) {
        return !disposed && reporter instanceof CbusReporter
                && memo.getSystemPrefix().equals(Manager.getSystemPrefix(reporter.getSystemName()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends the read as an ACDAT from the reader's number, received at the
     * time given, handed to the layout thread (Swing thread) first as the
     * sensor output's frames are; nothing when the train has gone.
     */
    @Override
    @InvokeOnAnyThread
    public void report(@Nonnull Reporter reporter, @CheckForNull String tagId, long timeNanos) {
        if (disposed || tagId == null) {
            return;
        }
        if (!tagId.matches("[0-9A-Fa-f]{10}")) {
            log.warn("Tag \"{}\" is not 10 hex digits, so a CBUS RFID reader can't read it.", tagId);
            return;
        }
        String name = reporter.getSystemName();
        int number = Integer.parseInt(name.substring(Manager.getSystemPrefix(name).length() + 1));
        CanReply r = new CanReply(8);
        r.setElement(0, CbusConstants.CBUS_ACDAT);
        r.setElement(1, (number >> 8) & 0xff);
        r.setElement(2, number & 0xff);
        for (int i = 0; i < 5; i++) {
            r.setElement(3 + i, Integer.parseInt(tagId.substring(2 * i, 2 * i + 2), 16));
        }
        // by way of the layout thread (Swing thread), as the sensor output sends its frames, so
        // reads and detection reach the bus in the order they happened
        ThreadingUtil.runOnLayoutEventually(() -> {
            if (!disposed) {
                send.receiveAt(r, timeNanos);
            }
        });
    }

    /**
     * Report nothing more.
     */
    @InvokeOnAnyThread
    public void dispose() {
        disposed = true;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusSimulatedReporterOutput.class);
}
