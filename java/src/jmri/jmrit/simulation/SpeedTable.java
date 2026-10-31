package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.annotation.CheckForNull;

import jmri.jmrit.roster.RosterSpeedProfile;

/**
 * Track speed for a throttle setting in one direction of a loco, from the
 * speeds measured for its roster speed profile, or in proportion to the
 * throttle when there are none.
 * <p>
 * The speed between measured settings is worked out here, by straight lines
 * between the measured points and from a stand at setting 0, and in proportion
 * above the highest measured setting. This is deliberately not
 * {@link RosterSpeedProfile}'s own calculation: the simulator must not share
 * the logic it tests. Only the profile's stored measurements are read.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class SpeedTable {

    /** Throttle settings (fraction of full), from 0, rising. */
    private final double[] settings;
    /** Speeds in mm/s at those settings. */
    private final double[] speeds;
    private final boolean measured;

    private SpeedTable(double[] settings, double[] speeds, boolean measured) {
        this.settings = settings;
        this.speeds = speeds;
        this.measured = measured;
    }

    /**
     * A table in proportion to the throttle setting.
     *
     * @param fullSpeed speed at full throttle, mm/s
     * @return the table
     */
    static SpeedTable proportional(double fullSpeed) {
        return new SpeedTable(new double[]{0.0, 1.0}, new double[]{0.0, fullSpeed}, false);
    }

    /**
     * A table from measured speeds.
     *
     * @param points measured speed in mm/s by throttle setting (fraction of
     *               full); settings and speeds of 0 or less are ignored
     * @return the table, or null if there is no measured point
     */
    @CheckForNull
    static SpeedTable measured(SortedMap<Double, Double> points) {
        List<Double> s = new ArrayList<>();
        List<Double> v = new ArrayList<>();
        s.add(0.0);
        v.add(0.0);
        for (Map.Entry<Double, Double> e : points.entrySet()) {
            if (e.getKey() > 0.0 && e.getValue() > 0.0) {
                s.add(e.getKey());
                v.add(e.getValue());
            }
        }
        if (s.size() < 2) {
            return null;
        }
        double[] sa = new double[s.size()];
        double[] va = new double[v.size()];
        for (int i = 0; i < sa.length; i++) {
            sa[i] = s.get(i);
            va[i] = v.get(i);
        }
        return new SpeedTable(sa, va, true);
    }

    /**
     * Read the measured speeds in one direction from a roster speed profile's
     * stored steps. The profile stores each throttle setting times 1000, with
     * forward and reverse speeds in mm/s; a speed of 0 means not measured.
     *
     * @param profile the profile, may be null
     * @param forward true for the forward speeds
     * @return the table, or null if the profile has no measured speed that way
     */
    @CheckForNull
    static SpeedTable fromProfile(@CheckForNull RosterSpeedProfile profile, boolean forward) {
        if (profile == null) {
            return null;
        }
        SortedMap<Double, Double> points = new java.util.TreeMap<>();
        for (Map.Entry<Integer, RosterSpeedProfile.SpeedStep> e : profile.getProfileSpeeds().entrySet()) {
            RosterSpeedProfile.SpeedStep step = e.getValue();
            points.put(e.getKey() / 1000.0, (double) (forward ? step.getForwardSpeed() : step.getReverseSpeed()));
        }
        return measured(points);
    }

    /**
     * @return true if from measured speeds, false if proportional
     */
    boolean isMeasured() {
        return measured;
    }

    /**
     * Speed at a throttle setting.
     *
     * @param setting fraction of full throttle, 0 or more
     * @return mm/s
     */
    double speed(double setting) {
        int last = settings.length - 1;
        if (setting >= settings[last]) {
            return speeds[last] * setting / settings[last];
        }
        for (int i = 1; i <= last; i++) {
            if (setting <= settings[i]) {
                double f = (setting - settings[i - 1]) / (settings[i] - settings[i - 1]);
                return speeds[i - 1] + f * (speeds[i] - speeds[i - 1]);
            }
        }
        return speeds[last]; // not reached
    }

    /**
     * The settings where the speed's slope changes, between two settings.
     *
     * @param from one setting
     * @param to   the other
     * @return the measured settings strictly between them, in the order met
     *         going from one to the other
     */
    List<Double> breakpointsBetween(double from, double to) {
        List<Double> result = new ArrayList<>();
        double lo = Math.min(from, to);
        double hi = Math.max(from, to);
        for (int i = 1; i < settings.length; i++) {
            if (settings[i] > lo && settings[i] < hi) {
                result.add(settings[i]);
            }
        }
        if (from > to) {
            java.util.Collections.reverse(result);
        }
        return result;
    }
}
