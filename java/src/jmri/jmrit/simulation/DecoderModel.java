package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * The simulator's own model of a DCC decoder's momentum, written from NMRA
 * S-9.2.2.
 * <p>
 * Deliberately not shared with Warrants' ramp calculations or anything else in
 * JMRI: the simulator checks JMRI against an independent implementation, so a
 * mistake in one shows as a difference instead of both agreeing.
 * <p>
 * The decoder holds a signed throttle setting, the fraction of full speed with
 * the sign giving the loco's own direction (positive is forward). It moves
 * towards the commanded setting at the rate CV3 (accelerating) or CV4
 * (decelerating) gives. S-9.2.2: the time per speed step is the CV value times
 * 0.896 s divided by the number of speed steps in use, so going from stop to
 * full speed takes CV x 0.896 s whatever the number of steps. A CV of 0 means
 * no momentum. Reversing while moving slows to a stand at the CV4 rate, then
 * accelerates the other way at the CV3 rate.
 * <p>
 * CV23 and CV24 adjust CV3 and CV4 by a value in bits 0 to 6, added when bit 7
 * is 0 and subtracted when it is 1, before the time per step is worked out;
 * on overflow the largest value (255) is used, on underflow no momentum. They
 * apply only while the loco is in a consist (CV19 bits 0 to 6 not 0), as
 * decoder makers' manuals (SoundTraxx, TCS) and JMRI's decoder definitions
 * ("Consist Acceleration/Deceleration Adjustment") describe; S-9.2.2 itself
 * does not say. A train can be told to apply them outside a consist too, as a
 * truth option, for decoders whose manuals don't say.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class DecoderModel {

    /** Seconds per CV unit for the whole range of speed, from S-9.2.2. */
    static final double SECONDS_PER_UNIT = 0.896;

    /**
     * A setting this close to its target is at it: the end of a ramp, worked
     * out in floating point, can miss the target by rounding.
     */
    private static final double AT_TARGET = 1e-12;

    private final int accelerationCv;
    private final int decelerationCv;
    private final double accelerationPerSecond;
    private final double decelerationPerSecond;
    private double setting = 0.0;

    /**
     * A decoder with the given momentum.
     *
     * @param cv3 acceleration CV, 0 to 255
     * @param cv4 deceleration CV, 0 to 255
     */
    DecoderModel(int cv3, int cv4) {
        accelerationCv = cv3;
        decelerationCv = cv4;
        accelerationPerSecond = ratePerSecond(cv3);
        decelerationPerSecond = ratePerSecond(cv4);
    }

    /**
     * The decoder of a loco, with CV23 and CV24 applied while it is in a
     * consist, or always if the train is set to apply them outside one.
     *
     * @param loco           the loco
     * @param outsideConsist true to apply CV23 and CV24 outside a consist too
     * @return its decoder
     */
    static DecoderModel of(SimulatedLoco loco, boolean outsideConsist) {
        int cv3 = loco.getCv3();
        int cv4 = loco.getCv4();
        if (loco.isInConsist() || outsideConsist) {
            cv3 = adjusted(cv3, loco.getCv23());
            cv4 = adjusted(cv4, loco.getCv24());
        }
        return new DecoderModel(cv3, cv4);
    }

    /**
     * @return the acceleration CV in effect, after any adjustment
     */
    int getAccelerationCv() {
        return accelerationCv;
    }

    /**
     * @return the deceleration CV in effect, after any adjustment
     */
    int getDecelerationCv() {
        return decelerationCv;
    }

    /**
     * Time per speed step, as S-9.2.2 gives it.
     *
     * @param cv         the CV3 or CV4 value
     * @param speedSteps the number of speed steps in use
     * @return seconds per speed step
     */
    static double secondsPerStep(int cv, int speedSteps) {
        return cv * SECONDS_PER_UNIT / speedSteps;
    }

    /**
     * The momentum CV to use once an adjustment CV (CV23 or CV24) has been
     * applied, as S-9.2.2 describes.
     *
     * @param base       CV3 or CV4
     * @param adjustment CV23 or CV24: bits 0-6 the value, bit 7 set to subtract
     * @return the effective value, 0 to 255
     */
    static int adjusted(int base, int adjustment) {
        int value = adjustment & 0x7F;
        int result = ((adjustment & 0x80) != 0) ? base - value : base + value;
        if (result > 255) {
            return 255; // overflow: the maximum rate
        }
        return Math.max(result, 0); // underflow: no momentum
    }

    /**
     * Change of throttle setting per second for a momentum CV; infinite for 0.
     */
    private static double ratePerSecond(int cv) {
        if (cv <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return 1.0 / (cv * SECONDS_PER_UNIT);
    }

    /**
     * @return the current signed throttle setting
     */
    double getSetting() {
        return setting;
    }

    /**
     * Stop at once, as for an emergency stop.
     */
    void stopNow() {
        setting = 0.0;
    }

    /**
     * A straight-line part of the decoder's setting over time.
     */
    static final class Ramp {

        final double t0;
        final double t1;
        final double u0;
        final double u1;

        Ramp(double t0, double t1, double u0, double u1) {
            this.t0 = t0;
            this.t1 = t1;
            this.u0 = u0;
            this.u1 = u1;
        }

        @Override
        public String toString() {
            return "Ramp[" + t0 + ".." + t1 + ": " + u0 + ".." + u1 + "]";
        }
    }

    /**
     * Move the decoder's setting towards a target from time t0 to t1, adding
     * the straight-line parts it follows to a list. A setting with no momentum
     * changes at once, so consecutive parts may not join.
     *
     * @param target the commanded signed setting
     * @param t0     start time, seconds
     * @param t1     end time, seconds
     * @param ramps  the list to add to
     */
    void advance(double target, double t0, double t1, List<Ramp> ramps) {
        double t = t0;
        while (t < t1) {
            if (Math.abs(setting - target) <= AT_TARGET) {
                setting = target;
                ramps.add(new Ramp(t, t1, setting, setting));
                return;
            }
            double goal;
            double rate;
            boolean reversing = (setting > 0 && target < 0) || (setting < 0 && target > 0);
            if (reversing) {
                goal = 0.0;
                rate = decelerationPerSecond;
            } else if (Math.abs(target) < Math.abs(setting)) {
                goal = target;
                rate = decelerationPerSecond;
            } else {
                goal = target;
                rate = accelerationPerSecond;
            }
            if (Double.isInfinite(rate)) {
                setting = goal; // no momentum: changes at once
                continue;
            }
            double duration = Math.abs(goal - setting) / rate;
            double direction = Math.signum(goal - setting);
            if (t + duration >= t1) {
                double end = setting + direction * rate * (t1 - t);
                ramps.add(new Ramp(t, t1, setting, end));
                setting = end;
                return;
            }
            ramps.add(new Ramp(t, t + duration, setting, goal));
            setting = goal;
            t += duration;
        }
    }

    /**
     * Convenience for tests: the parts followed from t0 to t1.
     *
     * @param target the commanded signed setting
     * @param t0     start time, seconds
     * @param t1     end time, seconds
     * @return the parts
     */
    List<Ramp> advance(double target, double t0, double t1) {
        List<Ramp> ramps = new ArrayList<>();
        advance(target, t0, t1, ramps);
        return ramps;
    }
}
