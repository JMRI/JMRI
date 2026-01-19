package jmri.jmrit.roster;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.annotation.CheckForNull;

import jmri.Block;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;
import java.beans.PropertyChangeListener;
import jmri.Section;
import jmri.implementation.SignalSpeedMap;

import org.jdom2.Element;

/**
 * A class to store a speed profile for a given loco.
 * The speed steps against the profile are on a scale of 0 to 1000,
 * this equates to the float speed x 1000.
 * This allows a single profile to cover different throttle speed step settings.
 * A profile generated for a loco using 28 steps can be used for a throttle with 126 steps.
 */
public class RosterSpeedProfile {

    private RosterEntry _re = null;

    private float overRunTimeReverse = 0.0f;
    private float overRunTimeForward = 0.0f;

    private boolean _hasForwardSpeeds = false;
    private boolean _hasReverseSpeeds = false;

    /**
     * Create a new RosterSpeedProfile.
     * @param re the Roster Entry associated with the profile.
     */
    public RosterSpeedProfile(RosterEntry re) {
        _re = re;
    }

    /**
     * Get the RosterEntry associated with the profile.
     * @return the RosterEntry.
     */
    public RosterEntry getRosterEntry() {
        return _re;
    }

    public float getOverRunTimeForward() {
        return overRunTimeForward;
    }

    public void setOverRunTimeForward(float dt) {
        overRunTimeForward = dt;
    }

    public float getOverRunTimeReverse() {
        return overRunTimeReverse;
    }

    public void setOverRunTimeReverse(float dt) {
        overRunTimeReverse = dt;
    }

    public void clearCurrentProfile() {
        speeds = new TreeMap<>();
    }

    public void deleteStep(Integer step) {
        speeds.remove(step);
    }

    /**
     * Check if the Speed Profile contains Forward Speeds.
     * @return true if forward speeds are present, else false.
     */
    public boolean hasForwardSpeeds() {
        return _hasForwardSpeeds;
    }

    /**
     * Check if the Speed Profile contains Reverse Speeds.
     * @return true if reverse speeds are present, else false.
     */
    public boolean hasReverseSpeeds() {
        return _hasReverseSpeeds;
    }

    /**
     * place / remove SpeedProfile from test mode.
     * reinitializes speedstep trace array
     * @param value true/false
     */
    public void setTestMode(boolean value) {
        synchronized (this){
            profileInTestMode = value;
        }
        testSteps = new ArrayList<>();
    }

    /**
     * Gets the speed step trace array.
     * @return speedstep trace array
     */
    public List<SpeedSetting> getSpeedStepTrace() {
        return testSteps;
    }

    /**
     * Speed conversion Millimetres per second to Miles per hour.
     */
    public static final float MMS_TO_MPH = 0.00223694f;

    /**
     * Speed conversion Millimetres per second to Kilometres per hour.
     */
    public static final float MMS_TO_KPH = 0.0036f;

    /**
     * Returns the scale speed.
     * If Warrant preferences are not a speed, value returns unchanged.
     * @param mms MilliMetres per second.
     * @param factorFastClock true to factor in the Fast Clock ratio, else false.
     * @return scale speed in units specified by Warrant Preferences,
     *         unchanged if Warrant preferences are not a speed.
     */
    public float mmsToScaleSpeed(float mms, boolean factorFastClock) {
        int interp = InstanceManager.getDefault(SignalSpeedMap.class).getInterpretation();
        float scale = InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();
        float fastClockFactor = ( factorFastClock ?
            (float)InstanceManager.getDefault(jmri.Timebase.class).userGetRate() : 1 );

        switch (interp) {
            case SignalSpeedMap.SPEED_MPH:
                return mms * scale * MMS_TO_MPH * fastClockFactor;
            case SignalSpeedMap.SPEED_KMPH:
                return mms * scale * MMS_TO_KPH * fastClockFactor;
            case SignalSpeedMap.PERCENT_THROTTLE:
            case SignalSpeedMap.PERCENT_NORMAL:
                return mms;
            default:
                log.warn("MMSToScaleSpeed: Signal Speed Map is not in a scale speed, not modifing.");
                return mms;
        }
    }

    /**
     * Returns the scale speed as a numeric.
     * If Warrant preferences are not a speed, value returns unchanged.
     * @param mms MilliMetres per second
     * @return scale speed in units specified by Warrant Preferences,
     *         unchanged if Warrant preferences are not a speed.
     * @deprecated use {@link #mmsToScaleSpeed(float mms)}
     */
    @Deprecated (since="5.9.6",forRemoval=true)
    public float MMSToScaleSpeed(float mms) {
        jmri.util.LoggingUtil.deprecationWarning(log, "MMSToScaleSpeed");
        return mmsToScaleSpeed(mms);
    }

    /**
     * Returns the scale speed as a numeric.
     * If Warrant preferences are not a speed, value returns unchanged.
     * Does not factor Fast Clock ratio.
     * @param mms MilliMetres per second
     * @return scale speed in units specified by Warrant Preferences,
     *         unchanged if Warrant preferences are not a speed.
     */
    public float mmsToScaleSpeed(float mms) {
        return mmsToScaleSpeed(mms, false);
    }

    /**
     * Returns the scale speed format as I18N string with the units added given
     * MilliMetres per Second.
     * If the warrant preference is a percentage of
     * normal or throttle will use metres per second.
     * The Fast Clock Ratio is not used in the calculation.
     *
     * @param mms MilliMetres per second
     * @return a string with scale speed and units
     */
    public static String convertMMSToScaleSpeedWithUnits(float mms) {
        int interp = InstanceManager.getDefault(SignalSpeedMap.class).getInterpretation();
        float scale = InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();
        String formattedWithUnits;
        switch (interp) {
            case SignalSpeedMap.SPEED_MPH:
                String unitsMph = Bundle.getMessage("mph");
                formattedWithUnits = String.format(Locale.getDefault(), "%.2f %s", mms * scale * MMS_TO_MPH, unitsMph);
                break;
            case SignalSpeedMap.SPEED_KMPH:
                String unitsKph = Bundle.getMessage("kph");
                formattedWithUnits = String.format(Locale.getDefault(), "%.2f %s", mms * scale * MMS_TO_KPH, unitsKph);
                break;
            case SignalSpeedMap.PERCENT_THROTTLE:
            case SignalSpeedMap.PERCENT_NORMAL:
                String unitsMms = Bundle.getMessage("mmps");
                formattedWithUnits = String.format(Locale.getDefault(), "%.2f %s", mms, unitsMms);
                break;
            default:
                log.warn("ScaleSpeedToMMS: Signal Speed Map has no interp, not modifing.");
                formattedWithUnits = String.format( Locale.getDefault(), "%.2f", mms);
        }
        return formattedWithUnits;
    }

    /**
     * Returns the scale speed format as a string with the units added given a
     * throttle setting. and direction.
     * The Fast Clock Ratio is not used in the calculation.
     *
     * @param throttleSetting as percentage of 1.0
     * @param isForward       true or false
     * @return a string with scale speed and units
     */
    public String convertThrottleSettingToScaleSpeedWithUnits(float throttleSetting, boolean isForward) {
        return convertMMSToScaleSpeedWithUnits(getSpeed(throttleSetting, isForward));
    }

    /**
     * MilliMetres per Second given scale speed.
     * The Fast Clock Ratio is not used in the calculation.
     * @param scaleSpeed in MPH or KPH
     * @return MilliMetres per second
     */
    public float convertScaleSpeedToMMS(float scaleSpeed) {
        int interp = InstanceManager.getDefault(SignalSpeedMap.class).getInterpretation();
        float scale = InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();
        float mmsSpeed;
        switch (interp) {
            case SignalSpeedMap.SPEED_MPH:
                mmsSpeed = scaleSpeed / scale / MMS_TO_MPH;
                break;
            case SignalSpeedMap.SPEED_KMPH:
                mmsSpeed = scaleSpeed / scale / MMS_TO_KPH;
                break;
            default:
                log.warn("ScaleSpeedToMMS: Signal Speed Map is not in a scale speed, not modifing.");
                mmsSpeed = scaleSpeed;
        }
        return mmsSpeed;
    }

    /**
     * Converts from signal map speed to a throttle setting.
     * The Fast Clock Ratio is not used in the calculation.
     * @param signalMapSpeed value from warrants preferences
     * @param isForward      direction of travel
     * @return throttle setting
     */
    public float getThrottleSettingFromSignalMapSpeed(float signalMapSpeed, boolean isForward) {
        int interp = InstanceManager.getDefault(SignalSpeedMap.class).getInterpretation();
        float throttleSetting = 0.0f;
        switch (interp) {
            case SignalSpeedMap.PERCENT_NORMAL:
            case SignalSpeedMap.PERCENT_THROTTLE:
                throttleSetting = signalMapSpeed / 100.0f;
                break;
            case SignalSpeedMap.SPEED_KMPH:
            case SignalSpeedMap.SPEED_MPH:
                throttleSetting = getThrottleSetting(convertScaleSpeedToMMS(signalMapSpeed), isForward);
                break;
            default:
                log.warn("getThrottleSettingFromSignalMapSpeed: Signal Speed Map interp not supported.");
        }
        return throttleSetting;
    }

    /**
     * Set the speed for the given speed step.
     *
     * @param speedStep the speed step to set
     * @param forward   speed in meters per second for running forward at
     *                  speedStep
     * @param reverse   speed in meters per second for running in reverse at
     *                  speedStep
     */
    public void setSpeed(int speedStep, float forward, float reverse) {
        SpeedStep ss = speeds.computeIfAbsent(speedStep, k -> new SpeedStep());
        ss.setForwardSpeed(forward);
        ss.setReverseSpeed(reverse);
        if (forward > 0.0f) {
            _hasForwardSpeeds = true;
        }
        if (reverse > 0.0f) {
            _hasReverseSpeeds = true;
        }
    }

    public SpeedStep getSpeedStep(float speed) {
        int iSpeedStep = Math.round(speed * 1000);
        return speeds.get(iSpeedStep);
    }

    public void setForwardSpeed(float speedStep, float forward) {
        if (forward > 0.0f) {
            _hasForwardSpeeds = true;
        } else {
            return;
        }
        int iSpeedStep = Math.round(speedStep * 1000);
        speeds.computeIfAbsent(iSpeedStep, k -> new SpeedStep()).setForwardSpeed(forward);
    }

    /**
     * Merge raw throttleSetting value with an existing profile SpeedStep if
     * key for the throttleSetting is within the speedIncrement of the SpeedStep.
     * @param throttleSetting raw throttle setting value
     * @param speed track speed
     * @param speedIncrement throttle's speed step increment.
     */
    public void setForwardSpeed(float throttleSetting, float speed, float speedIncrement) {
        if (throttleSetting> 0.0f) {
            _hasForwardSpeeds = true;
        } else {
            return;
        }
        int key;
        Entry<Integer, SpeedStep> entry = findEquivalentEntry (throttleSetting, speedIncrement);
        if (entry != null) {    // close keys. i.e. resolve to same throttle step
            float value = entry.getValue().getForwardSpeed();
            speed = (speed + value) / 2;
            key = entry.getKey();
        } else {    // nothing close. make new entry
            key = Math.round(throttleSetting * 1000);
        }
        speeds.computeIfAbsent(key, k -> new SpeedStep()).setForwardSpeed(speed);
    }

    @CheckForNull
    private Entry<Integer, SpeedStep> findEquivalentEntry (float throttleSetting, float speedIncrement) {
        // search through table until end for an entry is found whose key / 1000
        // is within the speedIncrement of the throttleSetting
        // Note there may be zero values interspersed in the tree
        Entry<Integer, SpeedStep> entry = speeds.firstEntry();
        if (entry == null) {
            return null;
        }
        int key = entry.getKey();
        while (entry != null) {
            entry = speeds.higherEntry(key);
            if (entry != null) {
                float speed = entry.getKey();
                if (Math.abs(speed/1000.0f - throttleSetting) <= speedIncrement) {
                    return entry;
                }
                key = entry.getKey();
            }
        }
        return null;
    }

    /**
     * Merge raw throttleSetting value with an existing profile SpeedStep if
     * key for the throttleSetting is within the speedIncrement of the SpeedStep.
     * @param throttleSetting raw throttle setting value
     * @param speed track speed
     * @param speedIncrement throttle's speed step increment.
     */
    public void setReverseSpeed(float throttleSetting, float speed, float speedIncrement) {
        if (throttleSetting> 0.0f) {
            _hasReverseSpeeds = true;
        } else {
            return;
        }
        int key;
        Entry<Integer, SpeedStep> entry = findEquivalentEntry (throttleSetting, speedIncrement);
        if (entry != null) {    // close keys. i.e. resolve to same throttle step
            float value = entry.getValue().getReverseSpeed();
            speed = (speed + value) / 2;
            key = entry.getKey();
        } else {    // nothing close. make new entry
            key = Math.round(throttleSetting * 1000);
        }
        speeds.computeIfAbsent(key, k -> new SpeedStep()).setReverseSpeed(speed);
    }

    public void setReverseSpeed(float speedStep, float reverse) {
        if (reverse > 0.0f) {
            _hasReverseSpeeds = true;
        } else {
            return;
        }
        int iSpeedStep = Math.round(speedStep * 1000);
        speeds.computeIfAbsent(iSpeedStep, k -> new SpeedStep()).setReverseSpeed(reverse);
    }

    /**
     * return the forward speed in milli-meters per second for a given
     * percentage throttle
     *
     * @param speedStep which is actual percentage throttle
     * @return MilliMetres per second using straight line interpolation for
     *         missing points
     */
    public float getForwardSpeed(float speedStep) {
        int iSpeedStep = Math.round(speedStep * 1000);
        if (iSpeedStep <= 0 || !_hasForwardSpeeds) {
            return 0.0f;
        }
        // Note there may be zero values interspersed in the tree
        if (speeds.containsKey(iSpeedStep)) {
            float speed = speeds.get(iSpeedStep).getForwardSpeed();
            if (speed > 0.0f) {
                return speed;
            }
        }
        log.trace("no exact match forward for {}", iSpeedStep);
        float lower = 0.0f;
        float higher = 0.0f;
        int highStep = iSpeedStep;
        int lowStep = iSpeedStep;

        Entry<Integer, SpeedStep> entry = speeds.higherEntry(highStep);
        while (entry != null && higher <= 0.0f) {
            highStep = entry.getKey();
            float value = entry.getValue().getForwardSpeed();
            if (value > 0.0f) {
                higher = value;
            }
            entry = speeds.higherEntry(highStep);
        }
        boolean nothingHigher = (higher <= 0.0f);

        entry = speeds.lowerEntry(lowStep);
        while (entry != null && lower <= 0.0f) {
            lowStep = entry.getKey();
            float value = entry.getValue().getForwardSpeed();
            if (value > 0.0f) {
                lower = value;
            }
            entry = speeds.lowerEntry(lowStep);
        }
        log.trace("lowStep={}, lower={} highStep={} higher={} for iSpeedStep={}",
                lowStep, lower, highStep, higher, iSpeedStep);
        if (lower <= 0.0f) {      // nothing lower
            if (nothingHigher) {
                log.error("Nothing in speed Profile");
                return 0.0f;       // no forward speeds at all
            }
            return higher * iSpeedStep / highStep;
        }
        if (nothingHigher) {
//            return lower * (1.0f + (iSpeedStep - lowStep) / (1000.0f - lowStep));
            return lower + (iSpeedStep - lowStep) * lower / lowStep;
        }

        float valperstep = (higher - lower) / (highStep - lowStep);

        return lower + (valperstep * (iSpeedStep - lowStep));
    }

    /**
     * return the reverse speed in millimetres per second for a given percentage
     * throttle
     *
     * @param speedStep percentage of throttle 0.nnn
     * @return millimetres per second
     */
    public float getReverseSpeed(float speedStep) {
        int iSpeedStep = Math.round(speedStep * 1000);
        if (iSpeedStep <= 0 || !_hasReverseSpeeds) {
            return 0.0f;
        }
        if (speeds.containsKey(iSpeedStep)) {
            float speed = speeds.get(iSpeedStep).getReverseSpeed();
            if (speed > 0.0f) {
                return speed;
            }
        }
        log.trace("no exact match reverse for {}", iSpeedStep);
        float lower = 0.0f;
        float higher = 0.0f;
        int highStep = iSpeedStep;
        int lowStep = iSpeedStep;
        // Note there may be zero values interspersed in the tree

        Entry<Integer, SpeedStep> entry = speeds.higherEntry(highStep);
        while (entry != null && higher <= 0.0f) {
            highStep = entry.getKey();
            float value = entry.getValue().getReverseSpeed();
            if (value > 0.0f) {
                higher = value;
            }
            entry = speeds.higherEntry(highStep);
        }
        boolean nothingHigher = (higher <= 0.0f);
        entry = speeds.lowerEntry(lowStep);
        while (entry != null && lower <= 0.0f) {
            lowStep = entry.getKey();
            float value = entry.getValue().getReverseSpeed();
            if (value > 0.0f) {
                lower = value;
            }
            entry = speeds.lowerEntry(lowStep);
        }
        log.trace("lowStep={}, lower={} highStep={} higher={} for iSpeedStep={}",
                lowStep, lower, highStep, higher, iSpeedStep);
        if (lower <= 0.0f) {      // nothing lower
            if (nothingHigher) {
                log.error("Nothing in speed Profile");
                return 0.0f;       // no reverse speeds at all
            }
            return higher * iSpeedStep / highStep;
        }
        if (nothingHigher) {
            return lower * (1.0f + (iSpeedStep - lowStep) / (1000.0f - lowStep));
        }

        float valperstep = (higher - lower) / (highStep - lowStep);

        return lower + (valperstep * (iSpeedStep - lowStep));
    }

    /**
     * Get the approximate time a loco may travel a given distance at a given
     * speed step.
     *
     * @param isForward true if loco is running forward; false otherwise
     * @param speedStep the desired speed step
     * @param distance  the desired distance in millimeters
     * @return the approximate time in seconds
     */
    public float getDurationOfTravelInSeconds(boolean isForward, float speedStep, int distance) {
        float spd;
        if (isForward) {
            spd = getForwardSpeed(speedStep);
        } else {
            spd = getReverseSpeed(speedStep);
        }
        if (spd < 0.0f) {
            log.error("Speed not available to compute duration of travel");
            return 0.0f;
        }
        return (distance / spd);
    }

    /**
     * Get the approximate distance a loco may travel a given duration at a
     * given speed step.
     *
     * @param isForward true if loco is running forward; false otherwise
     * @param speedStep the desired speed step
     * @param duration  the desired time in seconds
     * @return the approximate distance in millimeters
     */
    public float getDistanceTravelled(boolean isForward, float speedStep, float duration) {
        float spd;
        if (isForward) {
            spd = getForwardSpeed(speedStep);
        } else {
            spd = getReverseSpeed(speedStep);
        }
        if (spd < 0.0f) {
            log.error("Speed not available to compute distance travelled");
            return 0.0f;
        }
        return Math.abs(spd * duration);
    }

    /*
     * ============================================================
     * Distance-based stopping API (public) - Stop to zero over a given distance
     * (mm) - Approach to min reliable speed over a distance, then stop at
     * sensor Notes: - Executes via RosterSpeedProfile's own stepQueue/stopTimer
     * (H2/4A). - Overrun compensation ONLY for stop-to-zero (H4). - Optional
     * speedFactor pre-divide supported (H3 / 4B).
     * ============================================================
     */

    /**
     * Plan and execute a stop-to-zero over a given distance (actual
     * millimetres).
     * 
     * @param t          The DccThrottle to drive
     * @param distanceMm Distance in mm (>= 0)
     */
    public void planStopToZeroOverDistance(DccThrottle t, float distanceMm) {
        planStopToZeroOverDistance(t, distanceMm, /* speedFactor */ 1.0f);
    }

    /**
     * Plan and execute a stop-to-zero over a given distance (actual
     * millimetres), with an optional external speed factor pre-divide (see
     * AutoActiveTrain behaviour).
     * 
     * @param t           The DccThrottle to drive
     * @param distanceMm  Distance in mm (>= 0)
     * @param speedFactor If > 0, throttle commands are divided by this factor
     *                    before enqueuing.
     */
    public void planStopToZeroOverDistance(DccThrottle t, float distanceMm, float speedFactor) {
        planDistanceSchedule(t, distanceMm, /* toMinOnly */ false, speedFactor);
    }

    /**
     * Plan and execute an approach to the minimum reliable operating speed over
     * the given distance, then stop when the supplied sensor transitions to
     * ACTIVE.
     * 
     * @param t          The DccThrottle to drive
     * @param distanceMm Distance in mm (>= 0)
     * @param stopSensor The sensor on which to stop (must not be null)
     */
    public void planApproachToMinOverDistanceThenStopBySensor(
            DccThrottle t, float distanceMm, jmri.Sensor stopSensor) {
        planApproachToMinOverDistanceThenStopBySensor(t, distanceMm, stopSensor, /*
                                                                                  * speedFactor
                                                                                  */ 1.0f);
    }

    /**
     * Plan and execute an approach to the minimum reliable operating speed over
     * the given distance, then stop when the supplied sensor transitions to
     * ACTIVE. Supports optional speed factor pre-divide.
     * 
     * @param t           The DccThrottle to drive
     * @param distanceMm  Distance in mm (>= 0)
     * @param stopSensor  The sensor on which to stop (must not be null)
     * @param speedFactor If > 0, throttle commands are divided by this factor
     *                    before enqueuing.
     */
    public void planApproachToMinOverDistanceThenStopBySensor(
            DccThrottle t, float distanceMm, jmri.Sensor stopSensor, float speedFactor) {
        if (stopSensor == null) {
            log.warn("planApproachToMin... called with null stopSensor; forcing immediate stop.");
            if (t != null)
                t.setSpeedSetting(0.0f);
            return;
        }
        // Stash the sensor + a one-shot listener; finishChange() removes any leftover listener.
        approachStopSensor = stopSensor;
        approachStopSensorListener = (java.beans.PropertyChangeEvent e) -> {
            if ("KnownState".equals(e.getPropertyName())) {
                try {
                    if (((Integer) e.getNewValue()).intValue() == jmri.Sensor.ACTIVE) {
                        if (_throttle != null)
                            lastIssuedSpeedSetting = 0.0f;
                        _throttle.setSpeedSetting(0.0f);
                        finishChange(); // also detaches this listener
                    }
                } catch (RuntimeException ex) {
                    log.warn("Stop-by-sensor handler failed; forcing stop.", ex);
                    try {
                        if (_throttle != null)
                            _throttle.setSpeedSetting(0.0f);
                    } catch (Exception ignore) {
                    }
                    finishChange();
                }
            }
        };
        approachStopSensor.addPropertyChangeListener(approachStopSensorListener);

        planDistanceSchedule(t, distanceMm, /* toMinOnly */ true, speedFactor);
    }

    /*
     * ============================================================ Helpers for
     * distance planning (mirrors inner controller logic)
     * ============================================================
     */

    /** Clamp helper for throttle percentage. */
    private static float clampPct(float pct) {
        if (pct < 0.0f)
            return 0.0f;
        if (pct > 1.0f)
            return 1.0f;
        return pct;
    }

    /**
     * Invert the roster profile: map target speed (mm/s) -> throttle % via
     * bisection.
     */
    private float throttleForSpeedMms(final float targetMms, final boolean forward,
            final float minPct, final float maxPct) {
        float lo = clampPct(minPct);
        float hi = clampPct(maxPct);
        // Guard: if target is below/above bracket, return bracket end
        float loMms = getSpeed(lo, forward);
        float hiMms = getSpeed(hi, forward);
        if (targetMms <= loMms)
            return lo;
        if (targetMms >= hiMms)
            return hi;

        float x = 0.5f * (lo + hi);
        for (int i = 0; i < 18; i++) { // ~0.004 resolution
            x = 0.5f * (lo + hi);
            float xmms = getSpeed(x, forward);
            if (xmms < targetMms)
                lo = x;
            else
                hi = x;
        }
        return clampPct(x);
    }

    /**
     * Core planner: builds a constant-deceleration throttle schedule to reach
     * either: - zero speed exactly at distance (toMinOnly=false), applying
     * overrun compensation; or - minimum reliable operating speed at distance
     * (toMinOnly=true), then waits for stopSensor. Executes via this profile's
     * own stepQueue/stopTimer (H2/4A).
     */
    private void planDistanceSchedule(DccThrottle t, float distanceMm, boolean toMinOnly, float speedFactor) {
        if (t == null) {
            log.warn("planDistanceSchedule called with null throttle; ignoring.");
            return;
        }
        // Do not clobber caller-configured min/max limits; just read them
        final float minPct = this.minReliableOperatingSpeed; // 0..1
        final float maxPct = this.maxOperatingSpeed; // 0..1
        final boolean forward = t.getIsForward();

        // Kill any running timer WITHOUT resetting limits (avoid finishChange() here).
        if (stopTimer != null) {
            stopTimer.stop();
            stopTimer = null;
        }
        synchronized (this) {
            stepQueue = new LinkedList<>();
        }

        _throttle = t;
        // Seed the "effective current" with a quantized value to avoid relying on getSpeedSetting() semantics.
        lastIssuedSpeedSetting = quantizeToSpeedStep(_throttle, clampPct(_throttle.getSpeedSetting()));

        // Apply a safe speedFactor
        float speedFactorSafe = (speedFactor > 0.0f) ? speedFactor : 1.0f;

        if (distanceMm <= 0.0f) {
            if (toMinOnly) {
                // Assert crawl and return; sensor listener (if any) will stop us.
                float vMin = getSpeed(Math.max(0.0f, minPct), forward);
                float thrMin = throttleForSpeedMms(vMin, forward, minPct, maxPct);
                thrMin = clampPct(thrMin / speedFactorSafe);
                thrMin = quantizeToSpeedStep(_throttle, thrMin);
                lastIssuedSpeedSetting = thrMin;
                _throttle.setSpeedSetting(thrMin);
                return;
            } else {
                lastIssuedSpeedSetting = 0.0f;
                _throttle.setSpeedSetting(0.0f);
                return;
        }
        }

        // Current speed (mm/s) from quantized speed setting
        float thrNow = lastIssuedSpeedSetting;
        float v0 = getSpeed(thrNow, forward);
        float vMin = getSpeed(Math.max(0.0f, minPct), forward);
        float vMax = getSpeed(Math.min(1.0f, maxPct), forward);

        // If caller asked for approach-to-min but the configured minimum is effectively zero,
        // then "approach to min" equals "stop to zero".
        if (toMinOnly && vMin <= 0.0f) {
            log.warn("planDistanceSchedule: minReliableOperatingSpeed=0; falling back to stop-to-zero over distance");
            toMinOnly = false;
        }

        // Clamp v0 into [vMin, vMax] to reflect realistic low/high bounds
        if (v0 < vMin)
            v0 = vMin;
        if (v0 > vMax)
            v0 = vMax;

        // Adjust target distance for overrun ONLY for stop-to-zero.
        float s = distanceMm;
        if (!toMinOnly) {
            float overrunSec = forward ? getOverRunTimeForward() : getOverRunTimeReverse();
            if (overrunSec < 0.0f)
                overrunSec = 0.0f;
            s = s - (vMin * overrunSec);
            if (s < Math.max(0.0f, 0.5f * vMin)) {
                s = Math.max(0.0f, 0.5f * vMin);
        }
        }

        // If no distance effectively remains, set terminal target right away
        if (s <= 0.0f) {
            if (toMinOnly) {
                float thrMin = throttleForSpeedMms(vMin, forward, minPct, maxPct);
                thrMin = clampPct(thrMin / speedFactorSafe);
                thrMin = quantizeToSpeedStep(_throttle, thrMin);
                lastIssuedSpeedSetting = thrMin;
                _throttle.setSpeedSetting(thrMin);
            } else {
                lastIssuedSpeedSetting = 0.0f;
                _throttle.setSpeedSetting(0.0f);
        }
            return;
        }

        // Constant deceleration to meet distance at v=0 (or to vMin then hold).
        final int internalSliceMs = 50; // internal integration resolution
        final float dt = internalSliceMs / 1000.0f;
        final int minCmdMs = getEffectiveMinCommandIntervalMs();

        float a; // mm/s^2
        if (!toMinOnly) {
            a = (v0 > 0.0f) ? -(v0 * v0) / (2.0f * s) : 0.0f;
        } else {
            a = (v0 > vMin && s > 0.0f) ? -((v0 * v0) - (vMin * vMin)) / (2.0f * s) : 0.0f;
        }

        java.util.LinkedList<SpeedSetting> plan = new java.util.LinkedList<>();
        float travelled = 0.0f;
        float v = v0;

        // Bucket accumulator to enforce command rate limiting without materially changing the integrated distance.
        int bucketMs = 0;
        float bucketSpeedTime = 0.0f; // sum of (mms * seconds)

        while (travelled < s) {
            float remaining = s - travelled;
            float stepDt = dt;
            if (toMinOnly && v > vMin && a != 0.0f) {
                float tToMin = (v - vMin) / Math.abs(a);
                if (tToMin > 0.0f && tToMin < stepDt)
                    stepDt = tToMin;
            }

            // Predict next speed
            float vNext;
            if (!toMinOnly) {
                vNext = Math.max(0.0f, v + a * stepDt);
            } else {
                float raw = v + a * stepDt;
                vNext = (raw >= vMin) ? raw : vMin;
            }

            float vStart = v;
            float vEnd = vNext;
            if (toMinOnly) {
                if (vStart < vMin)
                    vStart = vMin;
                if (vEnd < vMin)
                    vEnd = vMin;
            }
            float vMid = 0.5f * (vStart + vEnd);
            if (vMid < 0.0f)
                vMid = 0.0f;

            // Distance in this slice
            float deltaS;
            if (!toMinOnly) {
                deltaS = v * stepDt + 0.5f * a * stepDt * stepDt;
            } else if (a != 0.0f && v > vMin && vNext >= vMin) {
                deltaS = v * stepDt + 0.5f * a * stepDt * stepDt;
            } else {
                deltaS = vMin * stepDt;
            }
            if (deltaS < 0.0f)
                deltaS = 0.0f;

            // If this slice would overshoot, shorten to land exactly.
            if (deltaS > remaining && vMid > 0.0f) {
                float dtFinal = remaining / vMid;
                if (dtFinal < 0.001f)
                    dtFinal = 0.001f;
                int msFinal = Math.max(1, Math.round(dtFinal * 1000.0f));
                bucketMs += msFinal;
                bucketSpeedTime += vMid * (msFinal / 1000.0f);
                travelled = s;
                v = vNext;
                break;
            }

            int ms = Math.max(1, Math.round(stepDt * 1000.0f));
            bucketMs += ms;
            bucketSpeedTime += vMid * (ms / 1000.0f);

            travelled += deltaS;
            v = vNext;

            // Flush the bucket when we reach the minimum command interval or at the end.
            if (bucketMs >= minCmdMs || travelled >= s) {
                float bucketSec = bucketMs / 1000.0f;
                float avgMms = (bucketSec > 0.0f) ? (bucketSpeedTime / bucketSec) : 0.0f;
                float thr = throttleForSpeedMms(avgMms, forward, minPct, maxPct);
                thr = clampPct(thr / speedFactorSafe);
                thr = quantizeToSpeedStep(_throttle, thr);
                plan.add(new SpeedSetting(thr, bucketMs, false));
                bucketMs = 0;
                bucketSpeedTime = 0.0f;
        }

            if (!toMinOnly && v <= 0.0f && travelled < s) {
                break;
        }
        }

        // Tail: for stop-to-zero, ensure a final explicit zero command.
        if (!toMinOnly) {
            int tailMs = Math.max(minCmdMs, internalSliceMs);
            plan.add(new SpeedSetting(0.0f, tailMs, false));
        }

        // Enqueue and kick timer
        synchronized (this) {
            for (SpeedSetting ss : plan) {
                stepQueue.addLast(ss);
                if (profileInTestMode)
                    testSteps.add(ss);
        }
    }
    if (stopTimer == null) {
        setNextStep();
    }
}

    private float distanceRemaining = 0;
    private float distanceTravelled = 0;

    private TreeMap<Integer, SpeedStep> speeds = new TreeMap<>();

    private DccThrottle _throttle;

    private float desiredSpeedStep = -1;

    private float extraDelay = 0.0f;

    private float minReliableOperatingSpeed = 0.0f;

    private float maxOperatingSpeed = 1.0f;

    private NamedBean referenced = null;
    private javax.swing.Timer stopTimer = null;

    // --- Throttle command pacing / quantization ---
    // Default minimum time between speed-setting commands issued by the distance/physics planners.
    // The maintainers have found that sending more frequently than ~2/sec can become inaccurate
    // due to delays through the chain and other concurrent traffic.
    private static final int DEFAULT_MIN_COMMAND_INTERVAL_MS = 500;
    private int minCommandIntervalMs = DEFAULT_MIN_COMMAND_INTERVAL_MS;

    // Track the last speed-setting value actually issued by this class (after quantization).
    // Do not rely on throttle.getSpeedSetting() to reflect what was finally sent to track.
    private float lastIssuedSpeedSetting = -1.0f;

    /**
     * Set the minimum command interval (ms) used by the distance/physics
     * planners. Values <= 0 revert to the default. Values less than
     * DEFAULT_MIN_COMMAND_INTERVAL_MS are clamped up to the default.
     *
     * @param ms Minimum interval in milliseconds.
     */
    public void setMinCommandIntervalMs(int ms) {
        if (ms <= 0) {
            minCommandIntervalMs = DEFAULT_MIN_COMMAND_INTERVAL_MS;
        } else {
            minCommandIntervalMs = Math.max(ms, DEFAULT_MIN_COMMAND_INTERVAL_MS);
        }
    }

    private int getEffectiveMinCommandIntervalMs() {
        return Math.max(minCommandIntervalMs, DEFAULT_MIN_COMMAND_INTERVAL_MS);
    }

    private static float quantizeToSpeedStep(DccThrottle t, float pct) {
        float v = clampPct(pct);
        if (t == null)
            return v;
        float inc;
        try {
            inc = t.getSpeedIncrement();
        } catch (Throwable ex) {
            inc = 0.0f;
        }
        if (inc <= 0.0f)
            return v;
        // Round to nearest speed step.
        int steps = Math.round(v / inc);
        float q = steps * inc;
        // Ensure any non-zero request is at least one step.
        if (v > 0.0f && q < inc)
            q = inc;
        return clampPct(q);
    }

    private float getEffectiveCurrentSpeedSetting() {
        if (lastIssuedSpeedSetting >= 0.0f) {
            return lastIssuedSpeedSetting;
        }
        if (_throttle != null) {
            return clampPct(_throttle.getSpeedSetting());
        }
        return 0.0f;
    }

    // Distance-based approach-to-min: optional stop-sensor hook (cleared in finishChange()).
    private Sensor approachStopSensor = null;
    private PropertyChangeListener approachStopSensorListener = null;

    private long lastTimeTimerStarted = 0L;

    /**
     * reset everything back to default once the change has finished.
     */
    void finishChange() {
        // Remove any approach-stop sensor listener if present.
        if (approachStopSensor != null && approachStopSensorListener != null) {
            try {
                approachStopSensor.removePropertyChangeListener(approachStopSensorListener);
            } catch (Exception ex) {
                // ignore
            }
        }
        approachStopSensor = null;
        approachStopSensorListener = null;
        if (stopTimer != null) {
            stopTimer.stop();
        }
        stopTimer = null;
        _throttle = null;
        distanceRemaining = 0;
        desiredSpeedStep = -1;
        extraDelay = 0.0f;
        minReliableOperatingSpeed = 0.0f;
        maxOperatingSpeed = 1.0f;
        referenced = null;
        lastIssuedSpeedSetting = -1.0f;
        synchronized (this) {
            distanceTravelled = 0;
            stepQueue = new LinkedList<>();
        }
        _throttle = null;
    }

    public void setExtraInitialDelay(float eDelay) {
        extraDelay = eDelay;
    }

    public void setMinMaxLimits(float minReliableOperatingSpeed, float maxOperatingSpeed) {
        this.minReliableOperatingSpeed = minReliableOperatingSpeed;
        this.maxOperatingSpeed = maxOperatingSpeed;
        if (minReliableOperatingSpeed > maxOperatingSpeed) {
            log.warn("MaxOperatingSpeed [{}] < minReliableOperatingSpeed [{}] setting Max = Min",
                    minReliableOperatingSpeed, maxOperatingSpeed);
            this.maxOperatingSpeed = this.minReliableOperatingSpeed;
        }
    }

    /**
     * Set min/max throttle limits, optionally enforcing a scale km/h cap. If
     * maxSpeedScaleKmh == 0.0f, the percent maxOperatingSpeed takes precedence
     * (no effect). If maxSpeedScaleKmh > 0.0f, we convert the km/h cap to an
     * equivalent throttle% using the roster profile and the layout scale ratio,
     * then take the minimum of that and the percent cap.
     *
     * @param minReliableOperatingSpeed lowest throttle % the loco reliably
     *                                  moves (0..1)
     * @param maxOperatingSpeed         percent cap (0..1)
     * @param maxSpeedScaleKmh          scale km/h cap; 0.0f means "unused"
     * @param layoutScaleRatio          layout scale ratio (full-scale / model),
     *                                  e.g. 87.0 for HO
     * @param isForward                 direction of travel
     */
    public void setMinMaxLimitsKmh(float minReliableOperatingSpeed,
            float maxOperatingSpeed,
            float maxSpeedScaleKmh,
            float layoutScaleRatio,
            boolean isForward) {
        // Default to the percent cap
        float maxPct = maxOperatingSpeed;

        // If a km/h cap is specified and we have speeds for this direction, convert to throttle%
        boolean dirHasProfile = isForward ? hasForwardSpeeds() : hasReverseSpeeds();
        if (maxSpeedScaleKmh > 0.0f && dirHasProfile) {
            float safeScale = (layoutScaleRatio <= 0.0f) ? 1.0f : layoutScaleRatio;
            // Convert full-scale km/h -> model km/h -> model mm/s
            float modelKmh = maxSpeedScaleKmh / safeScale;
            float targetMms = modelKmh * 277.7778f; // 1 km/h = 277.7778 mm/s

            float thrCapPct = getThrottleSetting(targetMms, isForward);
            if (thrCapPct > 0.0f) {
                maxPct = Math.min(maxOperatingSpeed, thrCapPct);
            }
        }

        // Apply computed limits
        this.minReliableOperatingSpeed = minReliableOperatingSpeed;
        this.maxOperatingSpeed = maxPct;

        // Guard: if min > max, clamp max to min (preserves previous method semantics)
        if (this.minReliableOperatingSpeed > this.maxOperatingSpeed) {
            log.warn("MaxOperatingSpeed [{}] < minReliableOperatingSpeed [{}]; setting Max = Min",
                    this.maxOperatingSpeed, this.minReliableOperatingSpeed);
            this.maxOperatingSpeed = this.minReliableOperatingSpeed;
        }
    }

    /**
     * Set speed of a throttle.
     *
     * @param t     the throttle to set
     * @param blk   the block used for length details
     * @param speed the speed to set
     */
    public void changeLocoSpeed(DccThrottle t, Block blk, float speed) {
        if (blk == referenced && Float.compare(speed, desiredSpeedStep) == 0) {
            //log.debug("Already setting to desired speed step for this block");
            return;
        }
        float blockLength = blk.getLengthMm();
        if (blk == referenced) {
            distanceRemaining = distanceRemaining - getDistanceTravelled(_throttle.getIsForward(), _throttle.getSpeedSetting(), ((float) (System.nanoTime() - lastTimeTimerStarted) / 1000000000));
            blockLength = distanceRemaining;
            //Not entirely reliable at this stage as the loco could still be running and not completed the calculation of the distance, this could result in an over run
            log.debug("Block passed is the same as we are currently processing");
        } else {
            referenced = blk;
        }
        changeLocoSpeed(t, blockLength, speed);
    }

    /**
     * Set speed of a throttle.
     *
     * @param t     the throttle to set
     * @param sec   the section used for length details
     * @param speed the speed to set
     * @param usePercentage the percentage of the block to be used for stopping
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY",
        justification = "OK to compare floats, as even tiny differences should trigger update")
    public void changeLocoSpeed(DccThrottle t, Section sec, float speed, float usePercentage) {
        if (sec == referenced && speed == desiredSpeedStep) {
            log.debug("Already setting to desired speed step for this Section");
            return;
        }
        float sectionLength = sec.getActualLength() * usePercentage;
        if (sec == referenced) {
            distanceRemaining = distanceRemaining - getDistanceTravelled(_throttle.getIsForward(), _throttle.getSpeedSetting(), ((float) (System.nanoTime() - lastTimeTimerStarted) / 1000000000));
            sectionLength = distanceRemaining;
            //Not entirely reliable at this stage as the loco could still be running and not completed the calculation of the distance, this could result in an over run
            log.debug("Block passed is the same as we are currently processing");
        } else {
            referenced = sec;
        }
        changeLocoSpeed(t, sectionLength, speed);
    }

    /**
     * Set speed of a throttle.
     *
     * @param t     the throttle to set
     * @param blk   the block used for length details
     * @param speed the speed to set
     * @param usePercentage the percentage of the block to be used for stopping
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY",
        justification = "OK to compare floats, as even tiny differences should trigger update")
    public void changeLocoSpeed(DccThrottle t, Block blk, float speed, float usePercentage) {
        if (blk == referenced && speed == desiredSpeedStep) {
            //if(log.isDebugEnabled()) log.debug("Already setting to desired speed step for this block");
            return;
        }
        float blockLength = blk.getLengthMm() * usePercentage;
        if (blk == referenced) {
            distanceRemaining = distanceRemaining - getDistanceTravelled(_throttle.getIsForward(), _throttle.getSpeedSetting(), ((float) (System.nanoTime() - lastTimeTimerStarted) / 1000000000));
            blockLength = distanceRemaining;
            //Not entirely reliable at this stage as the loco could still be running and not completed the calculation of the distance, this could result in an over run
            log.debug("Block passed is the same as we are currently processing");
        } else {
            referenced = blk;
        }
        changeLocoSpeed(t, blockLength, speed);

    }

    /**
     * Set speed of a throttle to a speeed set by a float, using the section for
     * the length details
     * Set speed of a throttle.
     *
     * @param t     the throttle to set
     * @param sec   the section used for length details
     * @param speed the speed to set
     */
    //@TODO if a section contains multiple blocks then we could calibrate the change of speed based upon the block status change.
    public void changeLocoSpeed(DccThrottle t, Section sec, float speed) {
        if (sec == referenced && Float.compare(speed, desiredSpeedStep) == 0) {
            log.debug("Already setting to desired speed step for this section");
            return;
        }
        float sectionLength = sec.getActualLength();
        log.debug("call to change speed via section {}", sec.getDisplayName());
        if (sec == referenced) {
            distanceRemaining = distanceRemaining - getDistanceTravelled(_throttle.getIsForward(), _throttle.getSpeedSetting(), ((float) (System.nanoTime() - lastTimeTimerStarted) / 1000000000));
            sectionLength = distanceRemaining;
        } else {
            referenced = sec;
        }

        changeLocoSpeed(t, sectionLength, speed);
    }

    /**
     * Set speed of a throttle.
     *
     * @param t        the throttle to set
     * @param distance the distance in meters
     * @param requestedSpeed    the speed to set
     */
    public void changeLocoSpeed(DccThrottle t, float distance, float requestedSpeed) {
        float speed = 0.0f;
        log.debug("Call to change speed over specific distance: speed {} distance {}", requestedSpeed, distance);
        if (requestedSpeed  > maxOperatingSpeed) {
            speed = maxOperatingSpeed;
        } else {
            speed = requestedSpeed;
        }
        if (Float.compare(speed, desiredSpeedStep) == 0) {
            // This requires no checks for min/max.
            log.debug("Already setting to desired speed step");
            return;
        }
        log.debug("public change speed step by float {}", speed);
        log.debug("Desired Speed Step {} asked for {}", desiredSpeedStep, speed);

        if (stopTimer != null) {
            log.debug("stop timer valid so will cancel");
            cancelSpeedChange();
        }
        _throttle = t;
        desiredSpeedStep = speed;

        log.debug("Speed current {} required {} ",
                _throttle.getSpeedSetting(), speed);
        if (_throttle.getSpeedSetting() < speed) {
            log.debug("Going for acceleration");
        } else {
            log.debug("Going for deceleration");
        }

        float adjSpeed = speed;
        boolean andStop = false;
        if (speed <= 0.0) {
            andStop = true;
        }
        if (speed < minReliableOperatingSpeed) {
            adjSpeed = minReliableOperatingSpeed;
        }
        log.debug("Speed[{}] adjSpeed[{}] MinSpeed[{}]",
                speed,adjSpeed, minReliableOperatingSpeed);

        if (!andStop
                && (Float.compare(adjSpeed, t.getSpeedSetting()) == 0
                    || (Math.round(adjSpeed/t.getSpeedIncrement()) ==
                            Math.round(t.getSpeedSetting()/t.getSpeedIncrement())))) {
            log.debug("Throttle and request speed setting are the same {} {} so will quit", speed, t.getSpeedSetting());
            //Already at correct speed setting
            finishChange();
            return;
        }
        calculateStepDetails(adjSpeed, distance, andStop);
    }

    private List<SpeedSetting> testSteps = new ArrayList<>();
    private boolean profileInTestMode = false;

    void calculateStepDetails(float speedStep, float distance, boolean andStop) {

        float stepIncrement = _throttle.getSpeedIncrement();
        log.debug("Desired Speed Step {} asked for {}", desiredSpeedStep, speedStep);
        desiredSpeedStep = speedStep;
        log.debug("calculated current step {} required {} current {} increment {}", _throttle.getSpeedSetting(), speedStep, _throttle.getSpeedSetting(), stepIncrement);
        boolean increaseSpeed = false;
        if (_throttle.getSpeedSetting() < speedStep) {
            increaseSpeed = true;
            log.debug("Going for acceleration");
        } else {
            log.debug("Going for deceleration");
        }

        if (distance <= 0) {
            log.debug("Distance is less than 0 {}", distance);
            _throttle.setSpeedSetting(speedStep);
            finishChange();
            return;
        }

        float calculatedDistance = distance;

        if (stopTimer != null) {
            stopTimer.stop();
            distanceRemaining = distance;
        } else {
            calculatedDistance = calculateInitialOverRun(distance);
            distanceRemaining = calculatedDistance;
        }
        if (distanceRemaining < 0.0f) {
            if (andStop) {
                _throttle.setSpeedSetting(0.0f);
            } else {
                _throttle.setSpeedSetting(speedStep);
            }
            log.warn("There is insufficient distance [{}] after adjustments, setting speed immediately", distanceRemaining);
            return;
        }

        float calculatingStep = _throttle.getSpeedSetting();
        if (increaseSpeed) {
            if (calculatingStep < minReliableOperatingSpeed) {
                calculatingStep = minReliableOperatingSpeed;
            }
        }

        float endspd = 0;
        if (calculatingStep != 0.0 && desiredSpeedStep > 0) { // current speed
            if (_throttle.getIsForward()) {
                endspd = getForwardSpeed(desiredSpeedStep);
            } else {
                endspd = getReverseSpeed(desiredSpeedStep);
            }
        } else if (desiredSpeedStep != 0.0) {
            if (_throttle.getIsForward()) {
                endspd = getForwardSpeed(desiredSpeedStep);
            } else {
                endspd = getReverseSpeed(desiredSpeedStep);
            }
        }

        boolean calculated = false;
        while (!calculated) {
            float spd = 0;
            if (calculatingStep != 0.0) { // current speed
                if (_throttle.getIsForward()) {
                    spd = getForwardSpeed(calculatingStep);
                } else {
                    spd = getReverseSpeed(calculatingStep);
                }
            }

            log.debug("end spd {} spd {}", endspd, spd);
            double avgSpeed = Math.abs((endspd + spd) * 0.5);
            log.debug("avg Speed {}", avgSpeed);

            double time = (calculatedDistance / avgSpeed); //in seconds
            time = time * 1000; //covert it to milli seconds
            float speeddiff = calculatingStep - desiredSpeedStep;
            if (increaseSpeed) {
                speeddiff =  desiredSpeedStep - calculatingStep;
            }
            float noSteps = speeddiff / stepIncrement;
            log.debug("Speed diff {} number of Steps {} step increment {}", speeddiff, noSteps, stepIncrement);

            int timePerStep = (int) (time / noSteps);
            if (timePerStep < 0) {
                log.error("Time per speed went to zero or below, setting finale speed immediatly.");
                if (_throttle != null) {
                    addSpeedStepItem(calculated,new SpeedSetting(desiredSpeedStep, 10, andStop));
                    setNextStep();
                }
                break;
            }
            float calculatedStepInc = stepIncrement;
            boolean lastStep = false;
            if (Math.abs(speeddiff) > (stepIncrement * 2)) {
                //We do not get reliable time results if the duration per speed step is less than 500ms
                //therefore we calculate how many speed steps will fit in to 750ms.
                if (timePerStep <= 500 && timePerStep > 0) {
                    float newTime = 750.0f;
                    float tmp =(float) Math.floor(newTime / timePerStep);
                    // To avoid the lack of a stub ensure resultant speed is less than final speed by at least a step.
                    if (increaseSpeed) {
                        while (desiredSpeedStep - ( calculatingStep + (stepIncrement * tmp)) <= stepIncrement) {
                            tmp = tmp - 1;
                        }

                        if (tmp > 0 && calculatedDistance - getDistanceTravelled(_throttle.getIsForward(),
                                    calculatingStep + (stepIncrement * tmp),
                                    ((float) (newTime / 1000.0))) > 0) {
                            calculatedStepInc = stepIncrement * tmp;
                            timePerStep = (int)newTime;
                        }
                    } else {
                        while (calculatingStep - (stepIncrement * tmp) - desiredSpeedStep <= stepIncrement) {
                            tmp = tmp - 1;
                        }
                        if ( tmp > 0 && (calculatedDistance
                                - getDistanceTravelled(_throttle.getIsForward(),
                                        calculatingStep - (stepIncrement * tmp),
                                        ((float) (newTime / 1000.0)))) > 0) {
                            calculatedStepInc = stepIncrement * tmp;
                            timePerStep = (int)newTime;
                        }
                    }
                    log.debug("time per step was {} no of increments in 750 ms is {} new step increment in {}", timePerStep, tmp, calculatedStepInc);
                }
            } else {
                // last bit calculate duration from distance remaining
                if (increaseSpeed && calculatingStep == 0) {
                    calculatingStep+=calculatedStepInc;
                }
                timePerStep = Math.round(calculatedDistance/getSpeed(calculatingStep,_throttle.getIsForward())*1000);
                if (!increaseSpeed) {
                    calculatedStepInc = calculatingStep - desiredSpeedStep;
                } else {
                    calculatedStepInc = desiredSpeedStep - calculatingStep ;
                }
                lastStep=true;
            }
            calculatedStepInc=Math.abs(calculatedStepInc);
            log.debug("per interval {}, increase {} lastStep {}", timePerStep, increaseSpeed,lastStep);
            //Calculate the new speed setting
            if (increaseSpeed) {
                //if (calculatingStep + calculatedStepInc == desiredSpeedStep) {
                if (lastStep) {
                    SpeedSetting ss = new SpeedSetting(calculatingStep, timePerStep, andStop);
                    addSpeedStepItem(calculated,ss);
                    calculated = true;
                    if (!andStop) { calculatingStep = desiredSpeedStep;timePerStep=2;}
                    else {
                        calculatingStep = 0.0f;timePerStep=2;
                    }
                    ss = new SpeedSetting(calculatingStep, timePerStep, andStop);
                    addSpeedStepItem(calculated,ss);
                    if (stopTimer == null) {
                        setNextStep();
                    }
                    break;
                }
                calculatingStep = calculatingStep + calculatedStepInc;
            } else {
                if (lastStep) {
                    SpeedSetting ss = new SpeedSetting(calculatingStep, timePerStep, andStop);
                    addSpeedStepItem(calculated,ss);
                    calculated = true;
                    if (!andStop) { calculatingStep = desiredSpeedStep;timePerStep=2;}
                    else {
                        calculatingStep = 0.0f;timePerStep=2;
                    }
                    ss = new SpeedSetting(calculatingStep, timePerStep, andStop);
                    addSpeedStepItem(calculated,ss);
                    if (stopTimer == null) { //If this is the first time round then kick off the speed change
                        setNextStep();
                    }
                    break;
                }
                calculatingStep = calculatingStep - calculatedStepInc;
            }
            SpeedSetting ss = new SpeedSetting(calculatingStep, timePerStep, andStop);
            addSpeedStepItem(calculated,ss);
            if (stopTimer == null) { //If this is the first time round then kick off the speed change
                setNextStep();
            }
            if (calculated) {
               if (andStop) {
                   ss = new SpeedSetting(0.0f, 10, andStop);
               } else {
                   ss = new SpeedSetting(desiredSpeedStep, 10, andStop);
               }
               addSpeedStepItem(calculated,ss);            }
            // The throttle can disappear during a stop situation
            if (_throttle != null) {
                calculatedDistance = calculatedDistance - getDistanceTravelled(_throttle.getIsForward(), calculatingStep, ((float) (timePerStep / 1000.0)));
            } else {
                log.warn("Throttle destroyed before zero length[{}] remaining.",calculatedDistance);
                calculatedDistance = 0;
            }

            if (calculatedDistance <= 0 && !calculated) {
                log.warn("distance remaining is now 0, but we have not reached desired speed setting {} v {}", desiredSpeedStep, calculatingStep);
                calculated = true;
            }
        }
    }

    private void addSpeedStepItem(Boolean calculated, SpeedSetting ss) {
        synchronized (this) {
            stepQueue.addLast(ss);
            if (profileInTestMode) {
                testSteps.add(ss);
            }
            if (ss.andStop && calculated) {
                ss = new SpeedSetting( 0.0f, 0, ss.andStop);
                stepQueue.addLast(ss);
                if (profileInTestMode) {
                    testSteps.add(ss);
                }
            }
        }
    }

    //The bit with the distance is not used
    float calculateInitialOverRun(float distance) {
        log.debug("Stop timer not configured so will add overrun {}", distance);
        if (_throttle.getIsForward()) {
            float extraAsDouble = (getOverRunTimeForward() + extraDelay) / 1000;
            if (log.isDebugEnabled()) {
                log.debug("Over run time to remove (Forward) {} {}", getOverRunTimeForward(), extraAsDouble);
            }
            float olddistance = getDistanceTravelled(true, _throttle.getSpeedSetting(), extraAsDouble);
            distance = distance - olddistance;
            //time = time-getOverRunTimeForward();
            //time = time-(extraAsDouble*1000);
        } else {
            float extraAsDouble = (getOverRunTimeReverse() + extraDelay) / 1000;
            if (log.isDebugEnabled()) {
                log.debug("Over run time to remove (Reverse) {} {}", getOverRunTimeReverse(), extraAsDouble);
            }
            float olddistance = getDistanceTravelled(false, _throttle.getSpeedSetting(), extraAsDouble);
            distance = distance - olddistance;
            //time = time-getOverRunTimeReverse();
            //time = time-(extraAsDouble*1000);
        }
        log.debug("Distance remaining {}", distance);
        //log.debug("Time after overrun removed " + time);
        return distance;

    }

    /**
     * This method is called to cancel the existing change in speed.
     */
    public void cancelSpeedChange() {
        if (stopTimer != null && stopTimer.isRunning()) {
            stopTimer.stop();
        }
        finishChange();
    }

    synchronized void setNextStep() {
        //if (profileInTestMode) {
        //    return;
        //}
        if (stepQueue.isEmpty()) {
            log.debug("No more results");
            finishChange();
            return;
        }
        SpeedSetting ss = stepQueue.getFirst();
        if (ss.getDuration() == 0) {
            if (ss.getAndStop()) {
                _throttle.setSpeedSetting(0.0f);
            } else {
                _throttle.setSpeedSetting(desiredSpeedStep);
            }
            finishChange();
            return;
        }
        if (stopTimer != null) {
            //Reduce the distanceRemaining and calculate the distance travelling
            float distanceTravelledThisStep = getDistanceTravelled(_throttle.getIsForward(),
                    getEffectiveCurrentSpeedSetting(), ((float) (stopTimer.getDelay() / 1000.0)));
            distanceTravelled = distanceTravelled + distanceTravelledThisStep;
            distanceRemaining = distanceRemaining - distanceTravelledThisStep;
        }
        stepQueue.removeFirst();
        lastIssuedSpeedSetting = ss.getSpeedStep();
        _throttle.setSpeedSetting(lastIssuedSpeedSetting);
        stopTimer = new javax.swing.Timer(ss.getDuration(), (java.awt.event.ActionEvent e) -> {
            setNextStep();
        });
        stopTimer.setRepeats(false);
        lastTimeTimerStarted = System.nanoTime();
        stopTimer.start();

    }

    private LinkedList<SpeedSetting> stepQueue = new LinkedList<>();

    public static class SpeedSetting {

        private float step = 0.0f;
        private int duration = 0;
        private boolean andStop;

        public SpeedSetting(float step, int duration, boolean andStop) {
            log.debug("Adding step {} duration {} andStop{}", step, duration, andStop);
            this.step = step;
            this.duration = duration;
            this.andStop = andStop;
        }

        public float getSpeedStep() {
            return step;
        }

        public int getDuration() {
            return duration;
        }

        public boolean getAndStop() {
            return andStop;
        }
    }

    /*
     * The follow deals with the storage and loading of the speed profile for a roster entry.
     */
    public void store(Element e) {
        Element d = new Element("speedprofile");
        d.addContent(new Element("overRunTimeForward").addContent(Float.toString(getOverRunTimeForward())));
        d.addContent(new Element("overRunTimeReverse").addContent(Float.toString(getOverRunTimeReverse())));
        Element s = new Element("speeds");
        speeds.keySet().stream().forEachOrdered( i -> {
            Element ss = new Element("speed");
            ss.addContent(new Element("step").addContent(Integer.toString(i)));
            ss.addContent(new Element("forward").addContent(Float.toString(speeds.get(i).getForwardSpeed())));
            ss.addContent(new Element("reverse").addContent(Float.toString(speeds.get(i).getReverseSpeed())));
            s.addContent(ss);
        });
        d.addContent(s);
        e.addContent(d);
    }

    public void load(Element e) {
        try {
            setOverRunTimeForward(Float.parseFloat(e.getChild("overRunTimeForward").getText()));
        } catch (NumberFormatException ex) {
            log.error("Over run Error For {}", _re.getId());
        }
        try {
            setOverRunTimeReverse(Float.parseFloat(e.getChild("overRunTimeReverse").getText()));
        } catch (NumberFormatException ex) {
            log.error("Over Run Error Rev {}", _re.getId());
        }
        e.getChild("speeds").getChildren("speed").forEach( spd -> {
            try {
                String step = spd.getChild("step").getText();
                String forward = spd.getChild("forward").getText();
                String reverse = spd.getChild("reverse").getText();
                float forwardSpeed = Float.parseFloat(forward);
                if (forwardSpeed > 0.0f) {
                    _hasForwardSpeeds = true;
                }
                float reverseSpeed = Float.parseFloat(reverse);
                if (reverseSpeed > 0.0f) {
                    _hasReverseSpeeds = true;
                }
                setSpeed(Integer.parseInt(step), forwardSpeed, reverseSpeed);
            } catch (NumberFormatException ex) {
                log.error("Not loaded {}", ex.getMessage());
            }
        });
    }

    public static class SpeedStep {

        private float forward = 0.0f;
        private float reverse = 0.0f;

        /**
         * Create a new SpeedStep, Reverse and Forward speeds are 0.
         */
        public SpeedStep() {
        }

        /**
         * Set the Forward speed for the step.
         * @param speed the forward speed for the Step.
         */
        public void setForwardSpeed(float speed) {
            forward = speed;
        }

        /**
         * Set the Reverse speed for the step.
         * @param speed the reverse speed for the Step.
         */
        public void setReverseSpeed(float speed) {
            reverse = speed;
        }

        /**
         * Get the Forward Speed for the Step.
         * @return the forward speed.
         */
        public float getForwardSpeed() {
            return forward;
        }

        /**
         * Get the Reverse Speed for the Step.
         * @return the reverse speed.
         */
        public float getReverseSpeed() {
            return reverse;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SpeedStep ss = (SpeedStep) obj;
            return Float.compare(ss.getForwardSpeed(), forward) == 0
                && Float.compare(ss.getReverseSpeed(), reverse) == 0;
        }

            @Override
            public int hashCode() {
                int result = 17;
                result = 31 * result + Float.floatToIntBits(forward);
                result = 31 * result + Float.floatToIntBits(reverse);
                return result;
        }

    }

    /**
     * Get the number of SpeedSteps.
     * If there are too few SpeedSteps, it may be difficult to get reasonable
     * distances and speeds over a large range of throttle settings.
     * @return the number of Speed Steps in the profile.
     */
    public int getProfileSize() {
        return speeds.size();
    }

    public TreeMap<Integer, SpeedStep> getProfileSpeeds() {
        return speeds;
    }

    /**
     * Get the throttle setting to achieve a track speed
     *
     * @param speed     desired track speed in mm/sec
     * @param isForward direction
     * @return throttle setting
     */
    public float getThrottleSetting(float speed, boolean isForward) {
        if ((isForward && !_hasForwardSpeeds) || (!isForward && !_hasReverseSpeeds)) {
            return 0.0f;
        }
        int slowerKey = 0;
        float slowerValue = 0;
        float fasterKey;
        float fasterValue;
        Entry<Integer, SpeedStep> entry = speeds.firstEntry();
        if (entry == null) {
            log.warn("There is no speedprofile entries for [{}]", this.getRosterEntry().getId());
            return (0.0f);
        }
        // search through table until end or the entry is greater than
        // what we are looking for. This leaves the previous lower value in key. and slower
        // Note there may be zero values interspersed in the tree
        if (isForward) {
            fasterKey = entry.getKey();
            fasterValue = entry.getValue().getForwardSpeed();
            while (entry != null && entry.getValue().getForwardSpeed() < speed) {
                slowerKey = entry.getKey();
                float value = entry.getValue().getForwardSpeed();
                if (value > 0.0f) {
                    slowerValue = value;
                }
                entry = speeds.higherEntry(slowerKey);
                if (entry != null) {
                    fasterKey = entry.getKey();
                    value = entry.getValue().getForwardSpeed();
                    if (value > 0.0f) {
                        fasterValue = value;
                    }
                }
            }
        } else {
            fasterKey = entry.getKey();
            fasterValue = entry.getValue().getReverseSpeed();
            while (entry != null && entry.getValue().getReverseSpeed() < speed) {
                slowerKey = entry.getKey();
                float value = entry.getValue().getReverseSpeed();
                if (value > 0.0f) {
                    slowerValue = value;
                }
                entry = speeds.higherEntry(slowerKey);
                if (entry != null) {
                    fasterKey = entry.getKey();
                    value = entry.getValue().getReverseSpeed();
                    if (value > 0.0f) {
                        fasterValue = value;
                    }
                }
            }
        }
        log.trace("slowerKey={}, slowerValue={} fasterKey={} fasterValue={} for speed={}",
                slowerKey, slowerValue, fasterKey, fasterValue, speed);
        if (entry == null) {
            // faster does not exists use slower...
            if (slowerValue <= 0.0f) { // neither does slower
                return (0.0f);
            }

            // extrapolate
            float key = slowerKey * speed / slowerValue;
            if (key < 1000.0f) {
                return key / 1000.0f;
            } else {
                return 1.0f;
            }
        }
        if (Float.compare(slowerValue, speed) == 0 || fasterValue <= slowerValue) {
            return slowerKey / 1000.0f;
        }
        if (slowerValue <= 0.0f) {  // no entry had a slower speed, therefore key is invalid
            slowerKey = 0;
            if (fasterValue <= 0.0f) {  // neither is there a faster speed
                return (0.0f);
            }
        }
        // we need to interpolate
        float ratio = (speed - slowerValue) / (fasterValue - slowerValue);
        return (slowerKey + ((fasterKey - slowerKey) * ratio)) / 1000.0f;
    }

    /**
     * Get track speed in millimeters per second from throttle setting
     *
     * @param speedStep  throttle setting
     * @param isForward  direction
     * @return track speed
     */
    public float getSpeed(float speedStep, boolean isForward) {
        if (speedStep < 0.00001f) {
            return 0.0f;
        }
        float speed;
        if (isForward) {
            speed = getForwardSpeed(speedStep);
        } else {
            speed = getReverseSpeed(speedStep);
        }
        return speed;
    }

    /**
     * Physics-based acceleration to a target throttle percent (0..1).
     * Builds and runs a throttle/time schedule using this profile's stepQueue/stopTimer.
     *
     * @param t                    The DccThrottle to drive (must not be null)
     * @param targetThrottlePct    Desired throttle percent [0..1]
     * @param driverPowerPercent   Driver power/regulator percent [0..1] (limits applied power/TE during acceleration)
     * @param additionalWeightTonnes Extra consist mass in metric tonnes (>= 0)
     * @param rollingResistanceCoeff Rolling resistance coefficient c_rr (>= 0), e.g., ~0.002
     * @param layoutScaleRatio     Layout scale ratio (full-scale / model), e.g., 87.0 for HO
     * @param speedFactor          If > 0, throttle commands are divided by this factor before enqueuing
     */
    public void runPhysicsAccelerationToTargetThrottle(
            jmri.DccThrottle t,
            float targetThrottlePct,
            float driverPowerPercent,
            float additionalWeightTonnes,
            float rollingResistanceCoeff,
            float layoutScaleRatio,
            float speedFactor) {
        if (t == null) {
            log.warn("runPhysicsAccelerationToTargetThrottle called with null throttle; ignoring.");
            return;
        }
        float speedFactorSafe = (speedFactor > 0.0f) ? speedFactor : 1.0f;
        float driverPct = clampPct(driverPowerPercent);
        float crr = (rollingResistanceCoeff < 0.0f) ? 0.0f : rollingResistanceCoeff;
        float scaleRatio = (layoutScaleRatio <= 0.0f) ? 1.0f : layoutScaleRatio;

        final boolean forward = t.getIsForward();
        final float minPct = this.minReliableOperatingSpeed;
        final float maxPct = this.maxOperatingSpeed;

        // Kill any running timer and clear queue (do NOT call finishChange() which resets limits)
        if (stopTimer != null) {
            stopTimer.stop();
            stopTimer = null;
        }
        synchronized (this) {
            stepQueue = new LinkedList<>();
        }
        _throttle = t;
        lastIssuedSpeedSetting = quantizeToSpeedStep(_throttle, clampPct(_throttle.getSpeedSetting()));

        float thrNow = lastIssuedSpeedSetting;
        float v0_mms = getSpeed(thrNow, forward);
        float vTarget_mms = getSpeed(clampPct(targetThrottlePct), forward);

        float v0_fs = (v0_mms / 1000.0f) * scaleRatio;
        float vTarget_fs = (vTarget_mms / 1000.0f) * scaleRatio;

        float vMin_mms = getSpeed(Math.max(0.0f, minPct), forward);
        float vMin_fs = (vMin_mms / 1000.0f) * scaleRatio;
        if (vTarget_fs < vMin_fs)
            vTarget_fs = vMin_fs;
        if (v0_fs < vMin_fs)
            v0_fs = vMin_fs;

        float vCap_fs_roster = Float.POSITIVE_INFINITY;
        try {
            float kmhRoster = (_re != null) ? _re.getPhysicsMaxSpeedKmh() : 0.0f;
            if (kmhRoster > 0.0f)
                vCap_fs_roster = kmhRoster / 3.6f;
        } catch (Throwable ignore) {
        }
        vTarget_fs = Math.min(vTarget_fs, vCap_fs_roster);

        float massKg = 1000.0f;
        float powerW = 0.0f;
        float teN = 0.0f;
        boolean mechTransmission = false;
        boolean isSteam = false;
        try {
            float rosterKg = (_re != null) ? _re.getPhysicsWeightKg() : 0.0f;
            float extraKg = Math.max(0.0f, additionalWeightTonnes) * 1000.0f;
            massKg = Math.max(1.0f, rosterKg + extraKg);
            powerW = (_re != null) ? (_re.getPhysicsPowerKw() * 1000.0f) : 0.0f;
            teN = (_re != null) ? (_re.getPhysicsTractiveEffortKn() * 1000.0f) : 0.0f;
            mechTransmission = (_re != null) && _re.isPhysicsMechanicalTransmission();
            jmri.jmrit.roster.RosterEntry.TractionType tt =
                    (_re != null) ? _re.getPhysicsTractionType()
                            : jmri.jmrit.roster.RosterEntry.TractionType.DIESEL_ELECTRIC;
            isSteam = (tt == jmri.jmrit.roster.RosterEntry.TractionType.STEAM);
        } catch (Throwable ex) {
            log.warn("RosterEntry missing physics fields; falling back to immediate set.", ex);
        }

        final float powerExpSteam = 0.85f;
        float alphaPower =
                isSteam ? (driverPct <= 0.0f ? 0.0f : (float) Math.pow(driverPct, powerExpSteam)) : driverPct;
        float alphaTE = driverPct <= 0.0f ? 0.0f : driverPct;
        float P_avail = powerW * alphaPower;
        float TE_avail = teN * alphaTE;

        final int internalSliceMs = 50;
        final float dt = internalSliceMs / 1000.0f;
        final int minCmdMs = getEffectiveMinCommandIntervalMs();

        java.util.LinkedList<SpeedSetting> plan = new java.util.LinkedList<>();

        float v_fs = v0_fs;

        final float[] gearFsMps = new float[]{
                15f * 0.44704f,
                27f * 0.44704f,
                41f * 0.44704f
        };
        boolean[] gearPauseDone = new boolean[gearFsMps.length];
        for (int gi = 0; gi < gearPauseDone.length; gi++) {
            gearPauseDone[gi] = (v_fs >= gearFsMps[gi]);
        }

        // Bucket accumulator for rate limiting.
        int bucketMs = 0;
        float bucketSpeedTime = 0.0f; // sum(mms * seconds)

        int safety = 0;
        while (v_fs < vTarget_fs && safety < 10000) {
            float v_guard = Math.max(0.01f, v_fs);
            float F_power = (P_avail > 0.0f) ? (P_avail / v_guard) : 0.0f;
            float F_drive = (TE_avail > 0.0f) ? Math.min(TE_avail, F_power) : F_power;

            final float g = 9.80665f;
            float F_rr = crr * massKg * g;
            float a_fs = (F_drive - F_rr) / massKg;
            if (a_fs < 0.0f)
                a_fs = 0.0f;

            float stepDt = dt;
            float v_next_fs = v_fs + a_fs * stepDt;
            boolean finalStep = false;
            if (a_fs > 0.0f && v_next_fs > vTarget_fs) {
                stepDt = Math.max(0.001f, (vTarget_fs - v_fs) / a_fs);
                finalStep = true;
                v_next_fs = v_fs + a_fs * stepDt;
            }

            // Gear-change pause (coast) if mechanical transmission and crossing a threshold
            boolean pausedThisSlice = false;
            if (mechTransmission) {
                for (int gi = 0; gi < gearFsMps.length; gi++) {
                    if (!gearPauseDone[gi]) {
                        float sTrig = gearFsMps[gi];
                        if ((vTarget_fs >= sTrig) && (v_fs < sTrig) && (v_next_fs >= sTrig)) {
                            final float pauseSec = 3.5f;
                            float left = pauseSec;
                            float aCoast_fs = -(crr * g);
                            while (left > 0.0f) {
                                float chunk = Math.min(dt, left);
                                float v_next_coast = v_fs + aCoast_fs * chunk;
                                if (v_next_coast < vMin_fs)
                                    v_next_coast = vMin_fs;
                                float v_mid_coast_fs = 0.5f * (v_fs + v_next_coast);
                                float v_mid_model_ms = v_mid_coast_fs / scaleRatio;
                                float v_mid_mms = v_mid_model_ms * 1000.0f;

                                int ms = Math.max(1, Math.round(chunk * 1000.0f));
                                bucketMs += ms;
                                bucketSpeedTime += v_mid_mms * (ms / 1000.0f);

                                if (bucketMs >= minCmdMs) {
                                    float bucketSec = bucketMs / 1000.0f;
                                    float avgMms = (bucketSec > 0.0f) ? (bucketSpeedTime / bucketSec) : 0.0f;
                                    float thr = throttleForSpeedMms(avgMms, forward, minPct, maxPct);
                                    thr = clampPct(thr / speedFactorSafe);
                                    thr = quantizeToSpeedStep(_throttle, thr);
                                    plan.add(new SpeedSetting(thr, bucketMs, false));
                                    bucketMs = 0;
                                    bucketSpeedTime = 0.0f;
                                }

                                v_fs = v_next_coast;
                                left -= chunk;
                                safety++;
                                if (safety >= 10000)
                                    break;
                            }
                            gearPauseDone[gi] = true;
                            pausedThisSlice = true;
                            break;
                    }
                }
            }
        }
        if (pausedThisSlice) {
            continue;
        }

        float v_mid_fs = 0.5f * (v_fs + v_next_fs);
        float v_mid_model_ms = v_mid_fs / scaleRatio;
        float v_mid_mms = v_mid_model_ms * 1000.0f;

        int ms = Math.max(1, Math.round(stepDt * 1000.0f));
        bucketMs += ms;
        bucketSpeedTime += v_mid_mms * (ms / 1000.0f);

        // flush bucket
        if (bucketMs >= minCmdMs || finalStep) {
            float bucketSec = bucketMs / 1000.0f;
            float avgMms = (bucketSec > 0.0f) ? (bucketSpeedTime / bucketSec) : 0.0f;
            float thr = throttleForSpeedMms(avgMms, forward, minPct, maxPct);
            thr = clampPct(thr / speedFactorSafe);
            thr = quantizeToSpeedStep(_throttle, thr);
            plan.add(new SpeedSetting(thr, bucketMs, false));
            bucketMs = 0;
            bucketSpeedTime = 0.0f;
        }

        v_fs = v_next_fs;
        safety++;
        if (finalStep)
            break;
    }

    // Flush any remaining bucket content
    if (bucketMs > 0) {
        float bucketSec = bucketMs / 1000.0f;
        float avgMms = (bucketSec > 0.0f) ? (bucketSpeedTime / bucketSec) : 0.0f;
        float thr = throttleForSpeedMms(avgMms, forward, minPct, maxPct);
        thr = clampPct(thr / speedFactorSafe);
        thr = quantizeToSpeedStep(_throttle, thr);
        plan.add(new SpeedSetting(thr, bucketMs, false));
    }

    if (plan.isEmpty()) {
        float thrFinal = throttleForSpeedMms(vTarget_mms, forward, minPct, maxPct);
        thrFinal = clampPct(thrFinal / speedFactorSafe);
        thrFinal = quantizeToSpeedStep(_throttle, thrFinal);
        lastIssuedSpeedSetting = thrFinal;
        _throttle.setSpeedSetting(thrFinal);
        return;
    }

    synchronized (this) {
        for (SpeedSetting ss : plan) {
            stepQueue.addLast(ss);
            if (profileInTestMode)
                testSteps.add(ss);
        }
    }
    if (stopTimer == null) {
        setNextStep();
    }
}
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RosterSpeedProfile.class);

}
