package jmri.jmrit.simulation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InvokeOnAnyThread;
import jmri.InvokeOnLayoutThread;
import jmri.Reporter;

/**
 * Where simulated trains report who they are: a Block's reporter told the ID
 * tag of the train that has come into the Block, as that system's readers
 * report it (an RFID reader, RailCom). Each reporter is reported through the
 * output for the connection it belongs to; a reporter no output reports is
 * left alone.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public interface SimulatedReporterOutput {

    /**
     * Called on the layout thread (Swing thread) when the simulator copies
     * the layout's configuration.
     *
     * @param reporter a reporter
     * @return true if this output reports to that reporter
     */
    @InvokeOnLayoutThread
    boolean reports(@Nonnull Reporter reporter);

    /**
     * Report a train's tag, or that no train with a tag is there. Called as
     * {@link SimulatedDetectorOutput#report} is, in time order with the
     * sensor reports, and never while the simulator holds its lock; it must
     * not wait for the layout thread (Swing thread).
     *
     * @param reporter  the reporter
     * @param tagId     the tag the train carries, or null when the last train
     *                  with a tag has left the Block
     * @param timeNanos when it happened, on the {@link System#nanoTime()}
     *                  scale
     */
    @InvokeOnAnyThread
    void report(@Nonnull Reporter reporter, @CheckForNull String tagId, long timeNanos);
}
