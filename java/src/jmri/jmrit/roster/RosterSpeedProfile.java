package jmri.jmrit.roster;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
import jmri.Block;
import jmri.DccThrottle;
import jmri.NamedBean;
import jmri.Section;
import jmri.implementation.SignalSpeedMap;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple class to store a speed profile for a given loco The speed steps
 * against the profile are on a scale of 0 to 1000, this equates to the float
 * speed x 1000. This allows a single profile to cover different throttle speed
 * step settings. So a profile generate for a loco using 28 steps can be used
 * for a throttle using 126 steps.
 */
public class RosterSpeedProfile {

    RosterEntry _re = null;

    float overRunTimeReverse = 0.0f;
    float overRunTimeForward = 0.0f;

    boolean _hasForwardSpeeds = false;
    boolean _hasReverseSpeeds = false;

    public RosterSpeedProfile(RosterEntry re) {
        _re = re;
    }

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

    public boolean hasForwardSpeeds() {
        return _hasForwardSpeeds;
    }

    public boolean hasReverseSpeeds() {
        return _hasReverseSpeeds;
    }

    /* for speed conversions */
    static public final float MMS_TO_MPH = 0.00223694f;
    static public final float MMS_TO_KPH = 0.0036f;

    /**
     * Returns the scale speed as a numeric. if warrent prefernces are not a
     * speed value returned unchanged.
     *
     * @param mms MilliMetres per second
     * @return scale speed in units specified by Warrant Preferences. if warrent
     *         prefernces are not a speed
     */
    public float MMSToScaleSpeed(float mms) {
        int interp = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getInterpretation();
        float scale = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();

        switch (interp) {
            case SignalSpeedMap.SPEED_MPH:
                return mms * scale * MMS_TO_MPH;
            case SignalSpeedMap.SPEED_KMPH:
                return mms * scale * MMS_TO_KPH;
            case SignalSpeedMap.PERCENT_THROTTLE:
            case SignalSpeedMap.PERCENT_NORMAL:
                return mms;
            default:
                log.warn("MMSToScaleSpeed: Signal Speed Map is not in a scale speed, not modifing.");
                return mms;
        }
    }

    /**
     * Returns the scale speed format as a string with the units added given
     * MilliMetres per Second. If the warrant preference is a percentage of
     * normal or throttle will use metres per second.
     *
     * @param mms MilliMetres per second
     * @return a string with scale speed and units
     */
    public String convertMMSToScaleSpeedWithUnits(float mms) {
        int interp = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getInterpretation();
        float scale = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();
        String formattedWithUnits;
        switch (interp) {
            case SignalSpeedMap.SPEED_MPH:
                formattedWithUnits = String.format("%.2f mph", mms * scale * MMS_TO_MPH);
                break;
            case SignalSpeedMap.SPEED_KMPH:
                formattedWithUnits = String.format("%.2f kph", mms * scale * MMS_TO_KPH);
                break;
            case SignalSpeedMap.PERCENT_THROTTLE:
            case SignalSpeedMap.PERCENT_NORMAL:
                formattedWithUnits = String.format("%.2f mms", mms);
                break;
            default:
                log.warn("ScaleSpeedToMMS: Signal Speed Map has no interp, not modifing.");
                formattedWithUnits = String.format("%.2f", mms);
        }
        return formattedWithUnits;
    }

    /**
     * Returns the scale speed format as a string with the units added given a
     * throttle setting. and direction
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
     *
     * @param scaleSpeed in MPH or KPH
     * @return MilliMetres per second
     */
    public float convertScaleSpeedToMMS(float scaleSpeed) {
        int interp = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getInterpretation();
        float scale = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();
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
     * Converts from signal map speed to a throttle setting
     *
     * @param signalMapSpeed value from warrants preferences
     * @param isForward      direction of travel
     * @return throttle setting
     */
    public float getThrottleSettingFromSignalMapSpeed(float signalMapSpeed, boolean isForward) {
        int interp = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getInterpretation();
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
        //int iSpeedStep = Math.round(speedStep*1000);
        if (!speeds.containsKey(speedStep)) {
            speeds.put(speedStep, new SpeedStep());
        }
        SpeedStep ss = speeds.get(speedStep);
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
        if (!speeds.containsKey(iSpeedStep)) {
            speeds.put(iSpeedStep, new SpeedStep());
        }
        SpeedStep ss = speeds.get(iSpeedStep);
        ss.setForwardSpeed(forward);
    }

    public void setReverseSpeed(float speedStep, float reverse) {
        if (reverse > 0.0f) {
            _hasReverseSpeeds = true;
        } else {
            return;
        }
        int iSpeedStep = Math.round(speedStep * 1000);
        if (!speeds.containsKey(iSpeedStep)) {
            speeds.put(iSpeedStep, new SpeedStep());
        }
        SpeedStep ss = speeds.get(iSpeedStep);
        ss.setReverseSpeed(reverse);
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
        log.debug("no exact match forward for {}", iSpeedStep);
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
        log.debug("lowStep={}, lower={} highStep={} higher={} for iSpeedStep={}",
                lowStep, lower, highStep, higher, iSpeedStep);
        if (lower <= 0.0f) {      // nothing lower
            if (nothingHigher) {
                log.error("Nothing in speed Profile");
                return 0.0f;       // no forward speeds at all
            }
            return higher * iSpeedStep / highStep;
        }
        if (nothingHigher) {
            return lower * (1.0f + (iSpeedStep - lowStep) / (1000.0f - lowStep));
        }

        float valperstep = (higher - lower) / (highStep - lowStep);

        float retValue = lower + (valperstep * (iSpeedStep - lowStep));
        return retValue;
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
        log.debug("no exact match reverse for {}", iSpeedStep);
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
        log.debug("lowStep={}, lower={} highStep={} higher={} for iSpeedStep={}",
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

        float retValue = lower + (valperstep * (iSpeedStep - lowStep));
        return retValue;
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
        if (spd <= 0.0f) {
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
        if (spd <= 0.01f) {
            log.error("Speed not available to compute distance travelled");
            return 0.0f;
        }
        return Math.abs(spd * duration);
    }

    float distanceRemaining = 0;
    float distanceTravelled = 0;

    TreeMap<Integer, SpeedStep> speeds = new TreeMap<>();

    DccThrottle _throttle;

    float desiredSpeedStep = -1;

    float extraDelay = 0.0f;

    NamedBean referenced = null;

    javax.swing.Timer stopTimer = null;

    long lastTimeTimerStarted = 0l;

    /**
     * reset everything back to default once the change has finished.
     */
    void finishChange() {
        if (stopTimer != null) {
            stopTimer.stop();
        }
        stopTimer = null;
        _throttle = null;
        distanceRemaining = 0;
        desiredSpeedStep = -1;
        extraDelay = 0.0f;
        referenced = null;
        synchronized (this) {
            distanceTravelled = 0;
            stepQueue = new LinkedList<>();
        }
        _throttle = null;
    }

    public void setExtraInitialDelay(float eDelay) {
        extraDelay = eDelay;
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
     * @param speed    the speed to set
     */
    public void changeLocoSpeed(DccThrottle t, float distance, float speed) {
        log.debug("Call to change speed over specific distance float {} distance {}", speed, distance);
        if (Float.compare(speed, t.getSpeedSetting()) == 0) {
            log.debug("Throttle and request speed setting are the same {} {} so will quit", speed, t.getSpeedSetting());
            //Already at correct speed setting
            finishChange();
            return;
        }

        if (Float.compare(speed, desiredSpeedStep) == 0) {
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

        log.debug("Desired Speed Step {} asked for {}", desiredSpeedStep, speed);
        desiredSpeedStep = speed;

        log.debug("calculated current step {} required {} current {}", _throttle.getSpeedSetting(), speed, _throttle.getSpeedSetting());
        if (_throttle.getSpeedSetting() < speed) {
            log.debug("Going for acceleration");
        } else {
            log.debug("Going for deceleration");
        }

        calculateStepDetails(speed, distance);
    }

    int extraTime = 0;

    void calculateStepDetails(float speedStep, float distance) {

        float stepIncrement = _throttle.getSpeedIncrement();
        log.debug("Desired Speed Step {} asked for {}", desiredSpeedStep, speedStep);
        desiredSpeedStep = speedStep;
        //int step = Math.round(_throttle.getSpeedSetting()*1000);
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

        float calculatingStep = _throttle.getSpeedSetting();

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
            /*if(stopTimer==null){
             log.debug("time before remove over run " + time);
             time = calculateInitialOverRun(time);//At the start we will deduct the over run time if configured
             log.debug("time after remove over run " + time);
             }*/
            float speeddiff = calculatingStep - desiredSpeedStep;
            float noSteps = speeddiff / stepIncrement;
            log.debug("Speed diff {} number of Steps {} step increment {}", speeddiff, noSteps, stepIncrement);

            int timePerStep = Math.abs((int) (time / noSteps));
            float calculatedStepInc = stepIncrement;
            if (calculatingStep > (stepIncrement * 2)) {
                //We do not get reliable time results if the duration per speed step is less than 500ms
                //therefore we calculate how many speed steps will fit in to 750ms.
                if (timePerStep <= 500 && timePerStep > 0) {
                    //thing tIncrement should be different not sure about this bit
                    float tmp = (750.0f / timePerStep);
                    calculatedStepInc = stepIncrement * tmp;
                    log.debug("time per step was {} no of increments in 750 ms is {} new step increment in {}", timePerStep, tmp, calculatedStepInc);

                    timePerStep = 750;
                }
            }
            log.debug("per interval {}", timePerStep);

            //Calculate the new speed setting
            if (increaseSpeed) {
                calculatingStep = calculatingStep + calculatedStepInc;
                if (calculatingStep > 1.0f) {
                    calculatingStep = 1.0f;
                    calculated = true;
                }
                if (calculatingStep > desiredSpeedStep) {
                    calculatingStep = desiredSpeedStep;
                    calculated = true;
                }
            } else {
                calculatingStep = calculatingStep - calculatedStepInc;
                if (calculatingStep < _throttle.getSpeedIncrement()) {
                    calculatingStep = 0.0f;
                    calculated = true;
                    timePerStep = 0;
                }
                if (calculatingStep < desiredSpeedStep) {
                    calculatingStep = desiredSpeedStep;
                    calculated = true;
                }
            }
            log.debug("Speed Step current {} speed to set {}", _throttle.getSpeedSetting(), calculatingStep);

            SpeedSetting ss = new SpeedSetting(calculatingStep, timePerStep);
            synchronized (this) {
                stepQueue.addLast(ss);
            }
            if (stopTimer == null) { //If this is the first time round then kick off the speed change
                setNextStep();
            }

            // The throttle can disappear during a stop situation
            if (_throttle != null) {
                calculatedDistance = calculatedDistance - getDistanceTravelled(_throttle.getIsForward(), calculatingStep, ((float) (timePerStep / 1000.0)));
            } else {
                log.warn("Throttle destroyed before zero length[{}] remaining.",calculatedDistance);
                calculatedDistance = 0;
            }
            if (calculatedDistance < 0 && !calculated) {
                log.error("distance remaining is now 0, but we have not reached desired speed setting {} v {}", desiredSpeedStep, calculatingStep);
                ss = new SpeedSetting(desiredSpeedStep, 10);
                synchronized (this) {
                    stepQueue.addLast(ss);
                }
                calculated = true;
            }
        }
    }

    //The bit with the distance is not used
    float calculateInitialOverRun(float distance) {
        log.debug("Stop timer not configured so will add overrun {}", distance);
        if (_throttle.getIsForward()) {
            float extraAsDouble = (getOverRunTimeForward() + extraDelay) / 1000;
            if (log.isDebugEnabled()) {
                log.debug("Over run time to remove (Forward) {}", getOverRunTimeForward());
                log.debug("{}", extraAsDouble);
            }
            float olddistance = getDistanceTravelled(true, _throttle.getSpeedSetting(), extraAsDouble);
            distance = distance - olddistance;
            //time = time-getOverRunTimeForward();
            //time = time-(extraAsDouble*1000);
        } else {
            float extraAsDouble = (getOverRunTimeReverse() + extraDelay) / 1000;
            if (log.isDebugEnabled()) {
                log.debug("Over run time to remove (Reverse) {}", getOverRunTimeReverse());
                log.debug("{}", extraAsDouble);
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

    void stopLocoTimeOut(DccThrottle t) {
        log.debug("Stopping loco");
        t.setSpeedSetting(0f);
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
        if (stepQueue.isEmpty()) {
            log.debug("No more results");
            finishChange();
            return;
        }
        SpeedSetting ss = stepQueue.getFirst();
        if (ss.getDuration() == 0) {
            _throttle.setSpeedSetting(0);
            finishChange();
            return;
        }
        if (stopTimer != null) {
            //Reduce the distanceRemaining and calculate the distance travelling
            float distanceTravelledThisStep = getDistanceTravelled(_throttle.getIsForward(), _throttle.getSpeedSetting(), ((float) (stopTimer.getDelay() / 1000.0)));
            distanceTravelled = distanceTravelled + distanceTravelledThisStep;
            distanceRemaining = distanceRemaining - distanceTravelledThisStep;
        }
        stepQueue.removeFirst();
        _throttle.setSpeedSetting(ss.getSpeedStep());
        stopTimer = new javax.swing.Timer(ss.getDuration(), (java.awt.event.ActionEvent e) -> {
            setNextStep();
        });
        stopTimer.setRepeats(false);
        lastTimeTimerStarted = System.nanoTime();
        stopTimer.start();

    }

    LinkedList<SpeedSetting> stepQueue = new LinkedList<>();

    static class SpeedSetting {

        float step = 0.0f;
        int duration = 0;

        SpeedSetting(float step, int duration) {
            this.step = step;
            this.duration = duration;
        }

        float getSpeedStep() {
            return step;
        }

        int getDuration() {
            return duration;
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
        speeds.keySet().stream().forEachOrdered((i) -> {
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
        e.getChild("speeds").getChildren("speed").forEach((spd) -> {
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

    static public class SpeedStep {

        float forward = 0.0f;
        float reverse = 0.0f;

        public SpeedStep() {
        }

        public void setForwardSpeed(float speed) {
            forward = speed;
        }

        public void setReverseSpeed(float speed) {
            reverse = speed;
        }

        public float getForwardSpeed() {
            return forward;
        }

        public float getReverseSpeed() {
            return reverse;
        }
    }

    /* If there are too few SpeedSteps to get reasonable distances and speeds
     * over a good range of throttle settings get whatever SpeedSteps exist.
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
        log.debug("slowerKey={}, slowerValue={} fasterKey={} fasterValue={} for speed={}",
                slowerKey, slowerValue, fasterKey, fasterValue, speed);
        if (entry == null) {
            // faster does not exists use slower...
            if (slowerValue <= 0.0f) { // neither does slower
                return (0.0f);
            }
            //return slowerKey / 1000;
            // extrapolate instead
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
        float setting = (slowerKey + ((fasterKey - slowerKey) * ratio)) / 1000.0f;
        return setting;
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

    private final static Logger log = LoggerFactory.getLogger(RosterSpeedProfile.class);
}
