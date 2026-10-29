package jmri.jmrit.dispatcher;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;
import java.util.function.DoubleSupplier;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;
import jmri.EntryPoint;
import jmri.Section;

/**
 * Guards a stop that waits for a stopping sensor, in case the sensor never
 * goes active. While the train approaches its stop, it must not leave the
 * Section it is stopping in. The backstop stops it if it does:
 * <ul>
 * <li><b>A Block beyond the Section goes occupied.</b> Each Block just beyond
 * the end of the Section, in the direction of travel, is watched if it is
 * unoccupied and not allocated to another train when the stop starts. Nothing
 * else should enter it, so its going occupied means this train has run past
 * its stop. This is exact, and needs no estimate.</li>
 * <li><b>Its head enters a Block outside the Section</b> that is reported to
 * the train, such as one allocated to it beyond the Section.</li>
 * <li><b>The train would pass the end of the Section.</b> When the train's
 * speed is given, the distance it has travelled since its head entered its
 * current Block is reckoned from that speed. When it reaches the end of the
 * Section, the train is stopped. The reckoning starts again each time the
 * head enters a Block, so its error is at most that over one Block. This
 * covers a Block beyond that is occupied or allocated to another train, where
 * its going occupied cannot be seen.</li>
 * </ul>
 * Every such stop is reported, since the stopping sensor has failed or is in
 * the wrong place.
 * <p>
 * The backstop does nothing while the sensor works: the train stops on the
 * sensor first, and the backstop is then disposed of.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class StopBackstop {

    /** How often the distance travelled is reckoned, in milliseconds. */
    static final long RECKON_INTERVAL_MS = 100;

    private final List<Block> watched = new ArrayList<>();
    private final List<PropertyChangeListener> watchers = new ArrayList<>();
    private final Section section;
    private final ToDoubleFunction<Block> lengthRemainingFrom;
    private final DoubleSupplier speedMmPerSecond;
    private final Consumer<String> stop;
    private TimerTask reckoner = null;
    private Block anchor;
    private double travelledMm = 0.0;
    private long lastReckonNanos;
    private boolean finished = false;

    /**
     * Start guarding a stop.
     *
     * @param section             the Section the train is stopping in
     * @param beyond              the Blocks just beyond the end of the
     *                            Section that are unoccupied and not
     *                            allocated to another train; watched for
     *                            going occupied
     * @param headBlock           the Block the train's head is in
     * @param lengthRemainingFrom the length of the Section from the start of a
     *                            Block to its end, in mm
     * @param speedMmPerSecond    the train's current speed in mm per second,
     *                            or null if it is not known; with null, only
     *                            the Blocks beyond are watched
     * @param stop                stops the train, given the reason
     */
    StopBackstop(@Nonnull Section section, @Nonnull Collection<Block> beyond, @Nonnull Block headBlock,
            @Nonnull ToDoubleFunction<Block> lengthRemainingFrom,
            @CheckForNull DoubleSupplier speedMmPerSecond, @Nonnull Consumer<String> stop) {
        this.section = section;
        this.lengthRemainingFrom = lengthRemainingFrom;
        this.speedMmPerSecond = speedMmPerSecond;
        this.stop = stop;
        this.anchor = headBlock;
        this.lastReckonNanos = System.nanoTime();
        for (Block b : beyond) {
            PropertyChangeListener l = e -> {
                if ("state".equals(e.getPropertyName()) && e.getNewValue() instanceof Integer
                        && (Integer) e.getNewValue() == Block.OCCUPIED) {
                    fire("Block " + b.getDisplayName() + " beyond the stopping Section went occupied");
                }
            };
            watched.add(b);
            watchers.add(l);
            b.addPropertyChangeListener(l);
        }
        if (speedMmPerSecond != null) {
            reckoner = new TimerTask() {
                @Override
                public void run() {
                    reckon();
                }
            };
            jmri.util.TimerUtil.scheduleAtFixedRate(reckoner, RECKON_INTERVAL_MS, RECKON_INTERVAL_MS);
        }
    }

    /**
     * The Blocks just beyond the end of a Section, in the direction of travel:
     * those from which a train would enter it travelling the other way.
     *
     * @param section   the Section
     * @param direction the direction of travel, {@link Section#FORWARD} or
     *                  {@link Section#REVERSE}
     * @return the Blocks beyond its end
     */
    @Nonnull
    static List<Block> blocksBeyond(@Nonnull Section section, int direction) {
        List<EntryPoint> farEnd = direction == Section.REVERSE
                ? section.getForwardEntryPointList()
                : section.getReverseEntryPointList();
        List<Block> blocks = new ArrayList<>();
        for (EntryPoint ep : farEnd) {
            Block b = ep.getFromBlock();
            if (b != null && !blocks.contains(b)) {
                blocks.add(b);
            }
        }
        return blocks;
    }

    /**
     * The train's head has entered a Block: reckon from its start, or stop the
     * train if the Block is beyond the stopping Section.
     *
     * @param block the Block
     */
    void headEntered(@Nonnull Block block) {
        if (!section.containsBlock(block)) {
            fire("its head entered Block " + block.getDisplayName() + ", beyond the stopping Section");
            return;
        }
        synchronized (this) {
            anchor = block;
            travelledMm = 0.0;
            lastReckonNanos = System.nanoTime();
        }
    }

    /**
     * Add the distance travelled since the last reckoning, and stop the train
     * if it has reached the end of the Section.
     */
    void reckon() {
        String reason = null;
        synchronized (this) {
            if (finished) {
                return;
            }
            long now = System.nanoTime();
            double seconds = (now - lastReckonNanos) / 1.0e9;
            lastReckonNanos = now;
            travelledMm += Math.max(0.0, speedMmPerSecond.getAsDouble()) * seconds;
            double remaining = lengthRemainingFrom.applyAsDouble(anchor);
            if (travelledMm >= remaining) {
                reason = String.format("by reckoning it has reached the end of the stopping Section, %.0f mm after"
                        + " entering Block %s", travelledMm, anchor.getDisplayName());
            }
        }
        if (reason != null) {
            fire(reason);
        }
    }

    /**
     * Stop the train, once, outside this object's lock: the train's own lock
     * may be held by a thread waiting to dispose of this backstop.
     */
    private void fire(String reason) {
        if (finish()) {
            stop.accept(reason);
        }
    }

    /**
     * Stop guarding: remove the listeners and the reckoning. Called when the
     * train stops, or the stop is cancelled.
     */
    void dispose() {
        finish();
    }

    /**
     * Finish guarding, once.
     *
     * @return true if this call finished it; false if it was already finished
     */
    private synchronized boolean finish() {
        if (finished) {
            return false;
        }
        finished = true;
        for (int i = 0; i < watched.size(); i++) {
            watched.get(i).removePropertyChangeListener(watchers.get(i));
        }
        watched.clear();
        watchers.clear();
        if (reckoner != null) {
            reckoner.cancel();
            reckoner = null;
        }
        return true;
    }

    /**
     * @return true once the backstop has been disposed of or has stopped the
     *         train
     */
    synchronized boolean isFinished() {
        return finished;
    }
}
