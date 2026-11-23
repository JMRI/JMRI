package jmri.jmrit.roster.swing.speedprofile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The speeds measured for speed steps that are run again at the end of a
 * profile, when the loco has warmed up.
 * <p>
 * The lowest steps matter most for stopping, and a loco's speed at them
 * changes as it warms up. Each step's first measurement, taken during the
 * profile, is kept with its repeats, so the change from the first run and
 * the spread of the repeats can be shown.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class SpeedStepRepeats {

    /**
     * One run of a speed step, in both directions.
     */
    static final class Run {

        final float speed;
        final int step;

        /**
         * @param speed the throttle setting, 0.0 to 1.0
         * @param step  the speed step, or 0 if the setting is not a step
         */
        Run(float speed, int step) {
            this.speed = speed;
            this.step = step;
        }

        int key() {
            return Math.round(speed * 1000);
        }
    }

    /**
     * The measurements of one speed step in one direction, in mm per second.
     */
    static final class Samples {

        private Float first = null;
        private final List<Float> repeats = new ArrayList<>();

        /**
         * @return the speed measured in the profile itself, or null if none
         */
        @CheckForNull
        Float getFirst() {
            return first;
        }

        /**
         * @return the speeds measured when the step was repeated
         */
        @Nonnull
        List<Float> getRepeats() {
            return Collections.unmodifiableList(repeats);
        }

        /**
         * @return the mean of the repeats, or 0 if there are none
         */
        float mean() {
            if (repeats.isEmpty()) {
                return 0.0f;
            }
            double sum = 0.0;
            for (float s : repeats) {
                sum += s;
            }
            return (float) (sum / repeats.size());
        }

        /**
         * @return the lowest repeat, or 0 if there are none
         */
        float min() {
            return repeats.isEmpty() ? 0.0f : Collections.min(repeats);
        }

        /**
         * @return the highest repeat, or 0 if there are none
         */
        float max() {
            return repeats.isEmpty() ? 0.0f : Collections.max(repeats);
        }

        /**
         * The spread of the repeats: half their range, as a percentage of
         * their mean, so that "±" this figure covers them all.
         *
         * @return the spread, or null with fewer than two repeats
         */
        @CheckForNull
        Float spreadPercent() {
            float mean = mean();
            if (repeats.size() < 2 || mean <= 0.0f) {
                return null;
            }
            return (max() - min()) / 2.0f / mean * 100.0f;
        }

        /**
         * The change from the first run to the mean of the repeats, as a
         * percentage of the first run.
         *
         * @return the change, or null with no first run or no repeats
         */
        @CheckForNull
        Float changePercent() {
            if (first == null || first <= 0.0f || repeats.isEmpty()) {
                return null;
            }
            return (mean() - first) / first * 100.0f;
        }
    }

    /**
     * A speed step's measurements in both directions.
     */
    static final class Entry {

        final int step;
        final Samples forward = new Samples();
        final Samples reverse = new Samples();

        Entry(int step) {
            this.step = step;
        }

        Samples get(boolean isForward) {
            return isForward ? forward : reverse;
        }
    }

    private final TreeMap<Integer, Entry> entries = new TreeMap<>();

    /**
     * Record a speed measured in the profile itself.
     *
     * @param run       the run
     * @param isForward true if measured going forward
     * @param speed     the speed, in mm per second
     */
    void recordFirst(@Nonnull Run run, boolean isForward, float speed) {
        entry(run).get(isForward).first = speed;
    }

    /**
     * Record a speed measured when a step was repeated.
     *
     * @param run       the run
     * @param isForward true if measured going forward
     * @param speed     the speed, in mm per second
     * @return the mean of the repeats so far, in that direction
     */
    float recordRepeat(@Nonnull Run run, boolean isForward, float speed) {
        Samples s = entry(run).get(isForward);
        s.repeats.add(speed);
        return s.mean();
    }

    private Entry entry(Run run) {
        return entries.computeIfAbsent(run.key(), k -> new Entry(run.step));
    }

    /**
     * @return the steps measured, keyed by throttle setting × 1000, for those
     *         with repeats
     */
    @Nonnull
    Map<Integer, Entry> getRepeated() {
        TreeMap<Integer, Entry> repeated = new TreeMap<>();
        for (Map.Entry<Integer, Entry> e : entries.entrySet()) {
            if (!e.getValue().forward.repeats.isEmpty() || !e.getValue().reverse.repeats.isEmpty()) {
                repeated.put(e.getKey(), e.getValue());
            }
        }
        return repeated;
    }

    /**
     * @return true if any step has been repeated
     */
    boolean hasRepeats() {
        return !getRepeated().isEmpty();
    }

    void clear() {
        entries.clear();
    }

    /**
     * The runs that repeat the lowest steps of a profile: each of the lowest
     * {@code count} steps in turn, slowest first, and the whole set
     * {@code times} times, so each step's repeats are spread over the time
     * the repeats take.
     *
     * @param profiled the runs of the profile, in any order
     * @param count    how many of the lowest steps to repeat
     * @param times    how many times to repeat each
     * @return the runs, in the order to make them; empty if there are none
     */
    @Nonnull
    static List<Run> plan(@Nonnull Collection<Run> profiled, int count, int times) {
        TreeMap<Integer, Run> bySpeed = new TreeMap<>();
        for (Run r : profiled) {
            bySpeed.putIfAbsent(r.key(), r);
        }
        List<Run> lowest = new ArrayList<>(bySpeed.values());
        if (lowest.size() > count) {
            lowest = lowest.subList(0, Math.max(0, count));
        }
        List<Run> runs = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            runs.addAll(lowest);
        }
        return runs;
    }
}
