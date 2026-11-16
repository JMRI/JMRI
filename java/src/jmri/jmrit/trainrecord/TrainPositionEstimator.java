package jmri.jmrit.trainrecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import jmri.BasicRosterEntry;
import jmri.Block;
import jmri.InvokeOnLayoutThread;
import jmri.LocoAddress;
import jmri.Path;
import jmri.TrainRecord;
import jmri.TrainRecord.SpeedCommand;
import jmri.TrainRecord.TrailEntry;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;

import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * Estimates where a train's leading end is, from when it entered its Block
 * and the speed commands for its lead loco since, using that loco's speed
 * profile and decoder momentum. A train of several locos is taken to run as
 * its lead loco, the loco Dispatcher and Warrants drive, is commanded: the
 * other locos' commands are not used, as their speed settings belong to
 * their own speed profiles.
 * <p>
 * The estimate starts from the last time the leading end was seen to enter a
 * Block (the time its sensor first reported, as the record keeps it), and
 * adds the distance the loco would have run since, at the speeds its speed
 * profile gives for the settings commanded. A decoder doesn't change speed at
 * once: it ramps at the rate its momentum CVs set. Two runs are worked out,
 * one with the decoder's momentum and one with none, and the estimate is the
 * range between them; while the commanded speed is steady the two agree.
 * The detected part is taken to have gone no further than the length of the
 * way on through the Block, since it hasn't been seen to enter the next
 * Block: the way from the Block it came from towards the neighbour its paths
 * are set for, as the Block's Routes tab gives it, or the Block's own length
 * where no length is set for the way. Where the paths set lead on to more
 * than one neighbour, or to none, the longest way on from where it came in is
 * taken, so that the estimate never stops short of where the train may be.
 * <p>
 * Detection sees wheels, not buffers: a Block's sensor reports when the
 * train's first detected wheel enters it, and the train's leading end is then
 * already its overhang further on. The estimate for a record adds the
 * overhang at the leading end (see {@link TrainOverhang}): small for a train
 * detected at that end, the rest of the train for one detected only at the
 * other, such as a loco propelling its train, and a range where its
 * detected part is isn't known.
 * <p>
 * There is no estimate when the record isn't placed, its leading end hasn't
 * been seen to enter a Block, its lead loco has no speed profile for the
 * direction, no speed command was known when the leading end entered the
 * Block, the commanded direction has changed since, as the train may then
 * have reversed, or its overhang at the leading end can't be known, as for a
 * train detected only at its other end whose length isn't known.
 * <p>
 * What is not yet taken into account: the time a detector takes to report,
 * and the profile's own accuracy. Each makes the true position differ from the estimate by a
 * fixed amount at a given speed, which later measurements are to find.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public final class TrainPositionEstimator {

    /** Seconds per unit of momentum CV, from NMRA S-9.2.2 (CV3 and CV4). */
    static final double SECONDS_PER_CV_UNIT = 0.896;

    /** Longest step when integrating a speed ramp, in seconds. */
    static final double RAMP_STEP_SECONDS = 0.05;

    private TrainPositionEstimator() {
    }

    /**
     * A loco's speed, in millimetres per second, at a speed setting and
     * direction, as its speed profile gives it.
     */
    public interface SpeedTable {

        /**
         * @param setting the speed setting, 0 to 1
         * @param forward true if the direction is forward
         * @return the speed in millimetres per second, 0 if not known
         */
        float getSpeed(float setting, boolean forward);
    }

    /**
     * A decoder's momentum: how long it takes to change its speed setting
     * over the whole range, accelerating and decelerating.
     */
    @Immutable
    public static final class Momentum {

        /** No momentum: the decoder changes speed at once. */
        public static final Momentum NONE = new Momentum(0, 0);

        private final double accelerationSeconds;
        private final double decelerationSeconds;

        /**
         * @param accelerationSeconds seconds to go from stopped to full speed
         * @param decelerationSeconds seconds to go from full speed to
         *                            stopped
         */
        public Momentum(double accelerationSeconds, double decelerationSeconds) {
            if (!(accelerationSeconds >= 0) || !(decelerationSeconds >= 0)) {
                throw new IllegalArgumentException("momentum times can't be negative");
            }
            this.accelerationSeconds = accelerationSeconds;
            this.decelerationSeconds = decelerationSeconds;
        }

        /**
         * Momentum from a decoder's CV3 and CV4. By NMRA S-9.2.2, the time to
         * change speed by one step is the CV's value times 0.896 seconds,
         * divided by the number of speed steps in use, so the whole range
         * takes the CV's value times 0.896 seconds, whatever the speed steps.
         * CV23 and CV24, which adjust momentum mostly for a decoder in a
         * consist, are not used, as Warrants don't use them: a roster entry
         * can't say what the decoders in a consist apply.
         *
         * @param cv3 CV3, the acceleration rate, 0 to 255
         * @param cv4 CV4, the deceleration rate, 0 to 255
         * @return the momentum
         */
        @Nonnull
        public static Momentum fromCvs(int cv3, int cv4) {
            return new Momentum(Math.max(0, Math.min(255, cv3)) * SECONDS_PER_CV_UNIT,
                    Math.max(0, Math.min(255, cv4)) * SECONDS_PER_CV_UNIT);
        }

        /**
         * Momentum from the CV3 and CV4 values in a roster entry's file, as
         * DecoderPro last read or wrote them. A CV not in the file is taken
         * as 0, as the decoder's default; no file, as no momentum.
         *
         * @param entry the roster entry
         * @return the momentum
         */
        @Nonnull
        public static Momentum fromRoster(@Nonnull RosterEntry entry) {
            String fileName = entry.getFileName();
            if (fileName == null || fileName.isEmpty()) {
                return NONE;
            }
            File file = new File(Roster.getDefault().getRosterFilesLocation() + fileName);
            if (!file.isFile()) {
                return NONE;
            }
            Element root;
            try {
                root = new XmlFile() {
                }.rootFromFile(file);
            } catch (IOException | JDOMException e) {
                log.warn("Can't read the momentum CVs from {}: {}", file, e.getMessage());
                return NONE;
            }
            Element values = root.getChild("locomotive");
            values = values == null ? null : values.getChild("values");
            if (values == null) {
                return NONE;
            }
            int cv3 = 0;
            int cv4 = 0;
            for (Element cv : values.getChildren("CVvalue")) {
                String name = cv.getAttributeValue("name");
                if ("3".equals(name)) {
                    cv3 = parse(cv.getAttributeValue("value"));
                } else if ("4".equals(name)) {
                    cv4 = parse(cv.getAttributeValue("value"));
                }
            }
            return fromCvs(cv3, cv4);
        }

        private static int parse(@CheckForNull String value) {
            if (value == null) {
                return 0;
            }
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        /**
         * @return seconds to go from stopped to full speed
         */
        public double getAccelerationSeconds() {
            return accelerationSeconds;
        }

        /**
         * @return seconds to go from full speed to stopped
         */
        public double getDecelerationSeconds() {
            return decelerationSeconds;
        }

        @Override
        public String toString() {
            return "Momentum[" + accelerationSeconds + " s up, " + decelerationSeconds + " s down]";
        }
    }

    /**
     * Estimate where a train record's leading end is.
     *
     * @param record    the record
     * @param momentum  its lead loco's decoder momentum, as from
     *                  {@link Momentum#fromRoster}
     * @param nowNanos  the moment, on the {@link System#nanoTime()} scale
     * @return the estimate, or null if there is none (see the class
     *         description)
     */
    @CheckForNull
    @InvokeOnLayoutThread
    public static TrainPositionEstimate estimate(@Nonnull TrainRecord record, @Nonnull Momentum momentum,
            long nowNanos) {
        if (!record.isPlaced() || record.getLeadingEnd() == null) {
            return null;
        }
        List<TrailEntry> trail = record.getTrail();
        if (trail.isEmpty()) {
            return null;
        }
        TrailEntry fix = trail.get(trail.size() - 1);
        TrainRecord.End end = fix.getEnd();
        if (fix.getFrom() == null || end == null) {
            return null; // placed there, not seen to enter
        }
        BasicRosterEntry entry = record.getFirstRosterEntry();
        if (!(entry instanceof RosterEntry)) {
            return null;
        }
        RosterSpeedProfile profile = ((RosterEntry) entry).getSpeedProfile();
        LocoAddress lead = entry.getDccLocoAddress();
        if (profile == null || lead == null) {
            return null;
        }
        TrainOverhang overhang = TrainOverhang.of(record, end);
        if (overhang == null) {
            return null;
        }
        TrainPositionEstimate detected = estimate(fix.getBlock(), fix.getFrom(), end, fix.getTimeNanos(),
                record.getCommands(lead), profile::getSpeed, momentum, nowNanos);
        if (detected == null) {
            return null;
        }
        return new TrainPositionEstimate(detected.getBlock(), end, detected.getMinMm() + overhang.getMinMm(),
                detected.getMaxMm() + overhang.getMaxMm(), detected.isLimitedByBlock(), detected.getMinSpeed(),
                detected.getMaxSpeed(), detected.getTimeNanos());
    }

    /**
     * Estimate where a train's detected part is, from when it entered a Block
     * and the speed commands for its loco: the distance its first detected
     * wheel has gone past the boundary, without the train's overhang.
     *
     * @param block    the Block the leading end entered
     * @param from     the Block it entered from, or null if not known
     * @param end      the end of the train that entered it
     * @param fixNanos when it entered, on the {@link System#nanoTime()} scale
     * @param commands the speed commands for the loco, including at least the
     *                 one in force when it entered
     * @param speeds   the loco's speed for each setting
     * @param momentum the decoder's momentum
     * @param nowNanos the moment to estimate for, on the same scale
     * @return the estimate, or null if there is none: no command was in force
     *         when it entered, the direction has changed since, or the speed
     *         table has no speeds for the direction
     */
    @CheckForNull
    public static TrainPositionEstimate estimate(@Nonnull Block block, @CheckForNull Block from,
            @Nonnull TrainRecord.End end, long fixNanos, @Nonnull List<SpeedCommand> commands,
            @Nonnull SpeedTable speeds, @Nonnull Momentum momentum, long nowNanos) {
        long now = Math.max(nowNanos, fixNanos);
        List<SpeedCommand> cs = new ArrayList<>(commands);
        cs.removeIf(c -> c.getTimeNanos() > now);
        cs.sort(Comparator.comparingLong(SpeedCommand::getTimeNanos)); // stable: arrival order kept for ties
        int atFix = -1;
        for (int i = 0; i < cs.size(); i++) {
            if (cs.get(i).getTimeNanos() <= fixNanos) {
                atFix = i;
            }
        }
        if (atFix < 0) {
            return null; // the speed when it entered isn't known
        }
        boolean forward = cs.get(atFix).isForward();
        for (int i = atFix + 1; i < cs.size(); i++) {
            if (!cs.get(i).isRelease() && cs.get(i).isForward() != forward) {
                return null; // the direction has changed: it may have reversed
            }
        }
        if (!(speeds.getSpeed(1f, forward) > 0)) {
            return null; // no speeds for this direction
        }

        Ramp ramp = new Ramp(cs.get(0), speeds, forward, momentum, fixNanos, now);
        for (int i = 1; i < cs.size(); i++) {
            ramp.advanceTo(cs.get(i).getTimeNanos());
            ramp.apply(cs.get(i));
        }
        ramp.advanceTo(now);

        double withMomentum = ramp.distanceWithMomentum;
        double without = ramp.distanceWithout;
        double min = Math.min(withMomentum, without);
        double max = Math.max(withMomentum, without);
        boolean limited = false;
        double length = lengthOn(block, from);
        if (length > 0 && max > length) {
            max = length;
            min = Math.min(min, length);
            limited = true;
        }
        double speedNow = ramp.speed(ramp.setting);
        double speedTarget = ramp.speed(ramp.target);
        return new TrainPositionEstimate(block, end, min, max, limited, Math.min(speedNow, speedTarget),
                Math.max(speedNow, speedTarget), now);
    }

    /**
     * How far a train entering a Block may go through it before it reaches
     * the next Block: the length of the way from the Block it came from to
     * the neighbour its paths are set for. Where the paths set lead on to
     * more than one neighbour, or to none, or where it came from isn't known,
     * the longest way that could be taken is used, so the result is never
     * less than the train may run. A way with no length set counts as the
     * Block's own length, as on the Block's Routes tab.
     *
     * @param block the Block entered
     * @param from  the Block it was entered from, or null if not known
     * @return the length, mm, or 0 if the Block has no length
     */
    public static double lengthOn(@Nonnull Block block, @CheckForNull Block from) {
        double longest = 0;
        double longestSet = 0;
        boolean anySet = false;
        for (Block.WayThrough w : block.getWaysThrough()) {
            Block on;
            if (from == null) {
                on = null;
            } else if (w.getOneEnd() == from) {
                on = w.getOtherEnd();
            } else if (w.getOtherEnd() == from) {
                on = w.getOneEnd();
            } else {
                continue;
            }
            double mm = block.getRouteLengthMm(w.getOneEnd(), w.getOtherEnd());
            longest = Math.max(longest, mm);
            if (on != null && isSet(block, from, on)) {
                anySet = true;
                longestSet = Math.max(longestSet, mm);
            }
        }
        if (anySet) {
            return longestSet;
        }
        return longest > 0 ? longest : block.getLengthMm();
    }

    /**
     * @return true if the Block's paths towards both neighbours are set
     */
    private static boolean isSet(Block block, Block a, Block b) {
        boolean toA = false;
        boolean toB = false;
        for (Path p : block.getPaths()) {
            if (p.getBlock() == a && p.checkPathSet()) {
                toA = true;
            } else if (p.getBlock() == b && p.checkPathSet()) {
                toB = true;
            }
        }
        return toA && toB;
    }

    /**
     * A decoder's speed setting ramping towards each commanded setting, and
     * the distances run within the time of interest, with and without the
     * ramp.
     */
    private static final class Ramp {

        private final SpeedTable speeds;
        private final boolean forward;
        private final Momentum momentum;
        private final long fromNanos;
        private final long toNanos;
        private long timeNanos;
        double setting;
        double target;
        double distanceWithMomentum = 0;
        double distanceWithout = 0;

        Ramp(SpeedCommand first, SpeedTable speeds, boolean forward, Momentum momentum, long fromNanos,
                long toNanos) {
            this.speeds = speeds;
            this.forward = forward;
            this.momentum = momentum;
            this.fromNanos = fromNanos;
            this.toNanos = toNanos;
            this.timeNanos = first.getTimeNanos();
            // the first command known: taken as reached, having nothing before it
            this.target = Math.max(0, first.getSpeedSetting());
            this.setting = target;
        }

        /**
         * A new command: an emergency stop stops the decoder at once.
         *
         * @param c the command
         */
        void apply(SpeedCommand c) {
            if (c.isRelease()) {
                return; // releasing commands nothing: the loco goes on
            }
            if (c.isEmergencyStop()) {
                setting = 0;
                target = 0;
            } else {
                target = c.getSpeedSetting();
            }
        }

        /**
         * Run on to a later time, towards the target.
         *
         * @param t1 the later time, on the {@link System#nanoTime()} scale
         */
        void advanceTo(long t1) {
            if (t1 <= timeNanos) {
                return;
            }
            double seconds = (t1 - timeNanos) / 1e9;
            double fullRange = target > setting ? momentum.getAccelerationSeconds()
                    : momentum.getDecelerationSeconds();
            double rampSeconds = fullRange * Math.abs(target - setting);
            double reached = Math.min(seconds, rampSeconds);
            long rampEnd = timeNanos + (long) (reached * 1e9);
            double settingAtEnd = rampSeconds > 0
                    ? setting + (target - setting) * (reached / rampSeconds) : target;
            // without momentum: the target all the way
            distanceWithout += speed(target) * overlapSeconds(timeNanos, t1);
            // with: the ramp, then the target
            distanceWithMomentum += rampDistance(timeNanos, setting, rampEnd, settingAtEnd);
            distanceWithMomentum += speed(settingAtEnd) * overlapSeconds(rampEnd, t1);
            setting = settingAtEnd;
            timeNanos = t1;
        }

        /** Distance while the setting moves in a straight line, within the time of interest. */
        private double rampDistance(long t0, double s0, long t1, double s1) {
            long a = Math.max(t0, fromNanos);
            long b = Math.min(t1, toNanos);
            if (b <= a) {
                return 0;
            }
            double span = t1 - t0;
            double sa = s0 + (s1 - s0) * ((a - t0) / span);
            double sb = s0 + (s1 - s0) * ((b - t0) / span);
            double seconds = (b - a) / 1e9;
            int n = Math.max(4, (int) Math.ceil(seconds / RAMP_STEP_SECONDS));
            double sum = 0;
            for (int i = 0; i < n; i++) {
                double x0 = sa + (sb - sa) * i / n;
                double x1 = sa + (sb - sa) * (i + 1) / n;
                sum += (speed(x0) + speed(x1)) / 2;
            }
            return sum * seconds / n;
        }

        private double overlapSeconds(long t0, long t1) {
            long a = Math.max(t0, fromNanos);
            long b = Math.min(t1, toNanos);
            return b > a ? (b - a) / 1e9 : 0;
        }

        double speed(double s) {
            return s <= 0 ? 0 : Math.max(0, speeds.getSpeed((float) Math.min(1, s), forward));
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainPositionEstimator.class);
}
