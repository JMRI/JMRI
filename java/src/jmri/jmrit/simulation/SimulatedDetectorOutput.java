package jmri.jmrit.simulation;

import javax.annotation.Nonnull;

import jmri.InvokeOnAnyThread;
import jmri.InvokeOnLayoutThread;
import jmri.Sensor;

/**
 * Where simulated trains report their detection: a sensor going active or
 * inactive at a given time, reported as the sensor's own system would report
 * it. Each sensor is reported through the output for the connection it belongs
 * to; a sensor no output reports is left alone.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public interface SimulatedDetectorOutput {

    /**
     * Called on the layout thread (Swing thread) when the simulator copies
     * the layout's configuration.
     *
     * @param sensor a sensor
     * @return true if this output reports changes of that sensor
     */
    @InvokeOnLayoutThread
    boolean reports(@Nonnull Sensor sensor);

    /**
     * Report a change. Called on the simulator's own thread, or on the thread
     * that placed or removed a train, in time order, and never while the
     * simulator holds its lock. The output passes the change to the layout as
     * that system would. It must not wait for the layout thread (Swing
     * thread), for example with {@code ThreadingUtil.runOnLayout}: hand the
     * change over with {@code ThreadingUtil.runOnLayoutEventually} or the
     * system's own queue instead.
     *
     * @param sensor    the sensor
     * @param state     {@link Sensor#ACTIVE} or {@link Sensor#INACTIVE}
     * @param timeNanos when it happened, on the {@link System#nanoTime()}
     *                  scale
     */
    @InvokeOnAnyThread
    void report(@Nonnull Sensor sensor, int state, long timeNanos);
}
