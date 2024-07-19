package jmri.jmrix.bachrus.speedmatcher.basic;

import jmri.DccThrottle;
import jmri.jmrix.bachrus.Speed;

/**
 * This is a simple speed matcher which will speed match a locomotive to a given
 * start and top speed using ESU's complex speed table. Speed steps 1, 10, 19,
 * and 28 will be set according to values interpolated linearly between the
 * given start and to speeds. Values for the remaining CVs will interpolated
 * between these 4 CVs. This is done to reduce the time the speed match takes
 * and to increase likelihood of success.
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class BasicESUTableSpeedMatcher extends BasicSpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Constants">
    private final int INITIAL_VSTART = 1;
    private final int INITIAL_VHIGH = 255;
    private final int INITIAL_TRIM = 128;
    private final int STEP28_VALUE = 255;
    private final int STEP1_VALUE = 1;

    private final int VHIGH_MAX = 255;
    private final int VHIGH_MIN = INITIAL_VSTART + 1;
    private final int STEP19_MIN = 19;
    private final int STEP10_MIN = 10;
    private final int VSTART_MIN = 1;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Enums">
    protected enum SpeedMatcherState {
        IDLE,
        WAIT_FOR_THROTTLE,
        INIT_THROTTLE,
        INIT_ACCEL,
        INIT_DECEL,
        INIT_VSTART,
        INIT_VHIGH,
        INIT_SPEED_TABLE,
        INIT_FORWARD_TRIM,
        INIT_REVERSE_TRIM,
        POST_INIT,
        FORWARD_WARM_UP,
        FORWARD_SPEED_MATCH_VHIGH,
        FORWARD_SPEED_MATCH_VSTART,
        FORWARD_SPEED_MATCH_STEP19,
        RE_INIT_SPEED_TABLE_MIDDLE_THIRD,
        FORWARD_SPEED_MATCH_STEP10,
        INTERPOLATE_SPEED_TABLE,
        POST_INTERPOLATE,
        REVERSE_WARM_UP,
        REVERSE_SPEED_MATCH_TRIM,
        COMPLETE,
        USER_STOPPED,
        CLEAN_UP,
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    private SpeedTableStep initSpeedTableStep;
    private int initSpeedTableStepValue;

    private SpeedTableStep interpolationSpeedTableStep;

    private int speedMatchCVValue = INITIAL_VHIGH;
    private int lastSpeedMatchCVValue = INITIAL_VHIGH;

    private int reverseTrimValue = INITIAL_TRIM;
    private int lastReverseTrimValue = INITIAL_TRIM;

    private final float targetVHighSpeedKPH;
    private final float targetStep19SpeedKPH;
    private final float targetStep10SpeedKPH;
    private final float targetVStartSpeedKPH;

    private int vHigh = INITIAL_VHIGH;
    private int lastVHigh = INITIAL_VHIGH;
    private int step19CVValue;
    private int step10CVValue;
    private int vStart;
    private int lastVStart = INITIAL_VSTART;
    private int vStartMax;

    private SpeedMatcherState speedMatcherState = SpeedMatcherState.IDLE;
    //</editor-fold>

    /**
     * Constructs the BasicESUTableSpeedMatcher from a BasicSpeedMatcherConfig
     * 
     * @param config BasicSpeedMatcherConfig
     */
    public BasicESUTableSpeedMatcher(BasicSpeedMatcherConfig config) {
        super(config);

        targetVHighSpeedKPH = targetTopSpeedKPH;
        targetStep19SpeedKPH = getSpeedForSpeedStep(SpeedTableStep.STEP19, targetStartSpeedKPH, targetTopSpeedKPH);
        targetStep10SpeedKPH = getSpeedForSpeedStep(SpeedTableStep.STEP10, targetStartSpeedKPH, targetTopSpeedKPH);
        targetVStartSpeedKPH = targetStartSpeedKPH;
    }

    //<editor-fold defaultstate="collapsed" desc="SpeedMatcher Overrides">
    /**
     * Starts the speed matching process
     *
     * @return true if speed matching started successfully, false otherwise
     */
    @Override
    public boolean startSpeedMatcher() {
        if (!validate()) {
            return false;
        }

        //reset instance variables
        speedMatchCVValue = INITIAL_VHIGH;
        lastSpeedMatchCVValue = INITIAL_VHIGH;
        reverseTrimValue = INITIAL_TRIM;
        lastReverseTrimValue = INITIAL_TRIM;

        speedMatcherState = SpeedMatcherState.WAIT_FOR_THROTTLE;

        if (!initializeAndStartSpeedMatcher(e -> speedMatchTimeout())) {
            cleanUpSpeedMatcher();
            return false;
        }

        startStopButton.setText(Bundle.getMessage("SpeedMatchStopBtn"));

        return true;
    }

    /**
     * Stops the speed matching process
     */
    @Override
    public void stopSpeedMatcher() {
        if (!isSpeedMatcherIdle()) {
            logger.info("Speed matching manually stopped");
            userStop();
        } else {
            cleanUpSpeedMatcher();
        }
    }

    /**
     * Indicates if the speed matcher is idle (not currently speed matching)
     *
     * @return true if idle, false otherwise
     */
    @Override
    public boolean isSpeedMatcherIdle() {
        return speedMatcherState == SpeedMatcherState.IDLE;
    }

    /**
     * Cleans up the speed matcher when speed matching is stopped or is finished
     */
    @Override
    protected void cleanUpSpeedMatcher() {
        speedMatcherState = SpeedMatcherState.IDLE;
        super.cleanUpSpeedMatcher();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Speed Matcher State">
    /**
     * Main speed matching timeout handler. This is the state machine that
     * effectively does the speed matching process.
     */
    private synchronized void speedMatchTimeout() {
        switch (speedMatcherState) {
            case WAIT_FOR_THROTTLE:
                cleanUpSpeedMatcher();
                logger.error("Timeout waiting for throttle");
                statusLabel.setText(Bundle.getMessage("StatusTimeout"));
                break;

            case INIT_THROTTLE:
                //set throttle to 0 for init
                setThrottle(true, 0);
                initNextSpeedMatcherState(SpeedMatcherState.INIT_ACCEL);
                break;

            case INIT_ACCEL:
                //set acceleration momentum to 0 (CV 3)
                if (programmerState == ProgrammerState.IDLE) {
                    writeMomentumAccel(INITIAL_MOMENTUM);
                    initNextSpeedMatcherState(SpeedMatcherState.INIT_DECEL);
                }
                break;

            case INIT_DECEL:
                //set deceleration mementum to 0 (CV 4)
                if (programmerState == ProgrammerState.IDLE) {
                    writeMomentumDecel(INITIAL_MOMENTUM);
                    initNextSpeedMatcherState(SpeedMatcherState.INIT_VSTART);
                }
                break;

            case INIT_VSTART:
                //set vStart to 0 (CV 2)
                if (programmerState == ProgrammerState.IDLE) {
                    writeVStart(INITIAL_VSTART);
                    initNextSpeedMatcherState(SpeedMatcherState.INIT_VHIGH);
                }
                break;

            case INIT_VHIGH:
                //set vHigh to 255 (CV 5)
                if (programmerState == ProgrammerState.IDLE) {
                    writeVHigh(INITIAL_VHIGH);
                    initNextSpeedMatcherState(SpeedMatcherState.INIT_SPEED_TABLE);
                }
                break;

            case INIT_SPEED_TABLE:
                //initialize speed table steps
                //don't need to set steps 1 or 28 since they are locked to 1 and
                //255, respectively on ESU decoders
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        initSpeedTableStepValue = INITIAL_VSTART;
                        initSpeedTableStep = SpeedTableStep.STEP2;
                        stepDuration = 1;
                    }

                    if (initSpeedTableStep.getSpeedStep() > SpeedTableStep.STEP18.getSpeedStep()) {
                        initSpeedTableStepValue = INITIAL_VHIGH;
                    }

                    writeSpeedTableStep(initSpeedTableStep, initSpeedTableStepValue);

                    initSpeedTableStep = initSpeedTableStep.getNext();
                    if (initSpeedTableStep == SpeedTableStep.STEP28) {
                        initNextSpeedMatcherState(SpeedMatcherState.INIT_FORWARD_TRIM);
                    }
                }
                break;

            case INIT_FORWARD_TRIM:
                //set forward trim to 128 (CV 66)
                if (programmerState == ProgrammerState.IDLE) {
                    writeForwardTrim(INITIAL_TRIM);
                    initNextSpeedMatcherState(SpeedMatcherState.INIT_REVERSE_TRIM);
                }
                break;

            case INIT_REVERSE_TRIM:
                //set reverse trim to 128 (CV 95)
                if (programmerState == ProgrammerState.IDLE) {
                    writeReverseTrim(INITIAL_TRIM);
                    initNextSpeedMatcherState(SpeedMatcherState.POST_INIT);
                }
                break;

            case POST_INIT: {
                statusLabel.setText(Bundle.getMessage("StatRestoreThrottle"));

                //un-brick Digitrax decoders
                setThrottle(false, 0);
                setThrottle(true, 0);

                SpeedMatcherState nextState;
                if (warmUpForwardSeconds > 0) {
                    nextState = SpeedMatcherState.FORWARD_WARM_UP;
                } else {
                    nextState = SpeedMatcherState.FORWARD_SPEED_MATCH_VHIGH;
                }
                initNextSpeedMatcherState(nextState);
                break;
            }

            case FORWARD_WARM_UP:
                //Run 4 minutes at high speed forward
                statusLabel.setText(Bundle.getMessage("StatForwardWarmUp", warmUpForwardSeconds - stepDuration));

                if (stepDuration >= warmUpForwardSeconds) {
                    initNextSpeedMatcherState(SpeedMatcherState.FORWARD_SPEED_MATCH_VHIGH);
                } else {
                    if (stepDuration == 0) {
                        setSpeedMatchStateTimerDuration(5000);
                        setThrottle(true, 28);
                    }
                    stepDuration += 5;
                }
                break;

            case FORWARD_SPEED_MATCH_VHIGH:
                //Use PID Controller to adjust vHigh (Speed Step 28) to the max speed
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        statusLabel.setText(Bundle.getMessage("StatSettingSpeed", SpeedMatcherCV.VHIGH.getName()));
                        logger.info("Setting CV {} to {} KPH ({} MPH)", SpeedMatcherCV.VHIGH.getName(), String.valueOf(targetVHighSpeedKPH), String.valueOf(Speed.kphToMph(targetVHighSpeedKPH)));
                        setThrottle(true, 28);
                        setSpeedMatchStateTimerDuration(8000);
                        stepDuration = 1;
                    } else {
                        setSpeedMatchError(targetVHighSpeedKPH);

                        if (Math.abs(speedMatchError) < ALLOWED_SPEED_MATCH_ERROR) {
                            initNextSpeedMatcherState(SpeedMatcherState.FORWARD_SPEED_MATCH_VSTART);
                        } else {
                            vHigh = getNextSpeedMatchValue(lastVHigh, VHIGH_MAX, VHIGH_MIN);

                            if (((lastVHigh == VHIGH_MAX) || (lastVHigh == VHIGH_MIN)) && (vHigh == lastVHigh)) {
                                statusLabel.setText(Bundle.getMessage("StatSetSpeedFail", SpeedMatcherCV.VHIGH.getName()));
                                logger.info("Unable to achieve desired speed for CV {}", SpeedMatcherCV.VHIGH.getName());
                                abort();
                                break;
                            }

                            lastVHigh = vHigh;
                            writeVHigh(vHigh);
                        }
                    }
                }
                break;
                
            case FORWARD_SPEED_MATCH_VSTART:
                //Use PID Controller to adjust vStart (Speed Step 1) to the min speed
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        vStartMax = vHigh - 1;
                        statusLabel.setText(Bundle.getMessage("StatSettingSpeed", SpeedMatcherCV.VSTART.getName()));
                        logger.info("Setting CV {} to {} KPH ({} MPH)", SpeedMatcherCV.VSTART.getName(), String.valueOf(targetVStartSpeedKPH), String.valueOf(Speed.kphToMph(targetVStartSpeedKPH)));
                        setThrottle(true, 1);
                        setSpeedMatchStateTimerDuration(8000);
                        stepDuration = 1;
                    } else {
                        setSpeedMatchError(targetVStartSpeedKPH);

                        if (Math.abs(speedMatchError) < ALLOWED_SPEED_MATCH_ERROR) {
                            initNextSpeedMatcherState(SpeedMatcherState.FORWARD_SPEED_MATCH_STEP19);
                        } else {
                            vStart = getNextSpeedMatchValue(lastVStart, vStartMax, VSTART_MIN);

                            if (((lastVStart == vStartMax) || (lastVStart == VSTART_MIN)) && (vStart == lastVStart)) {
                                statusLabel.setText(Bundle.getMessage("StatSetSpeedFail", SpeedMatcherCV.VSTART.getName()));
                                logger.info("Unable to achieve desired speed for CV {}", SpeedMatcherCV.VSTART.getName());
                                abort();
                                break;
                            }

                            lastVStart = vStart;
                            writeVStart(vStart);
                        }
                    }
                }
                break;

            case FORWARD_SPEED_MATCH_STEP19:
                //Use PID Controller to adjust Speed Step 19 to the interpolated speed
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        lastSpeedMatchCVValue = STEP28_VALUE;
                    }
                    speedMatchSpeedTableStep(SpeedTableStep.STEP19, targetStep19SpeedKPH, INITIAL_VHIGH, STEP19_MIN, SpeedMatcherState.RE_INIT_SPEED_TABLE_MIDDLE_THIRD);
                    step19CVValue = speedMatchCVValue;
                }
                break;

            case RE_INIT_SPEED_TABLE_MIDDLE_THIRD:
                //Re-initialize Speed Steps 10-18 based off value for Step 19
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        initSpeedTableStep = SpeedTableStep.STEP18;
                        stepDuration = 1;
                    }

                    writeSpeedTableStep(initSpeedTableStep, step19CVValue);

                    if (initSpeedTableStep == SpeedTableStep.STEP10) {
                        initNextSpeedMatcherState(SpeedMatcherState.FORWARD_SPEED_MATCH_STEP10);
                    } else {
                        initSpeedTableStep = initSpeedTableStep.getPrevious();
                    }

                }
                break;

            case FORWARD_SPEED_MATCH_STEP10:
                //Use PID Controller to adjust Speed Step 10 to the interpolated speed
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        lastSpeedMatchCVValue = step19CVValue;
                    }
                    speedMatchSpeedTableStep(SpeedTableStep.STEP10, targetStep10SpeedKPH, step19CVValue - 9, STEP10_MIN, SpeedMatcherState.INTERPOLATE_SPEED_TABLE);
                    step10CVValue = speedMatchCVValue;
                }
                break;

            case INTERPOLATE_SPEED_TABLE: {
                //Interpolate the values of the intermediate speed steps
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        setThrottle(true, 0);
                        interpolationSpeedTableStep = SpeedTableStep.STEP27;
                        stepDuration = 1;
                    }

                    int interpolatedSpeedStepCVValue = getInterpolatedSpeedTableCVValue(interpolationSpeedTableStep);
                    writeSpeedTableStep(interpolationSpeedTableStep, interpolatedSpeedStepCVValue);

                    do {
                        interpolationSpeedTableStep = interpolationSpeedTableStep.getPrevious();
                    } while (interpolationSpeedTableStep == SpeedTableStep.STEP19 || interpolationSpeedTableStep == SpeedTableStep.STEP10);

                    if (interpolationSpeedTableStep == SpeedTableStep.STEP1) {
                        initNextSpeedMatcherState(SpeedMatcherState.POST_INTERPOLATE);
                    }

                }
                break;
            }

            case POST_INTERPOLATE: {
                statusLabel.setText(Bundle.getMessage("StatRestoreThrottle"));

                //un-brick Digitrax decoders
                setThrottle(false, 0);
                setThrottle(true, 0);

                SpeedMatcherState nextState;
                if (trimReverseSpeed) {
                    if (warmUpReverseSeconds > 0) {
                        nextState = SpeedMatcherState.REVERSE_WARM_UP;
                    } else {
                        nextState = SpeedMatcherState.REVERSE_SPEED_MATCH_TRIM;
                    }
                } else {
                    nextState = SpeedMatcherState.COMPLETE;
                }
                initNextSpeedMatcherState(nextState);
                break;
            }

            case REVERSE_WARM_UP:
                //Run specified reverse warm up time at high speed in reverse
                statusLabel.setText(Bundle.getMessage("StatReverseWarmUp", warmUpReverseSeconds - stepDuration));

                if (stepDuration >= warmUpReverseSeconds) {
                    initNextSpeedMatcherState(SpeedMatcherState.REVERSE_SPEED_MATCH_TRIM);
                } else {
                    if (stepDuration == 0) {
                        setSpeedMatchStateTimerDuration(5000);
                        setThrottle(false, 28);
                    }
                    stepDuration += 5;
                }

                break;

            case REVERSE_SPEED_MATCH_TRIM:
                //Use PID controller logic to adjust reverse trim until high speed reverse speed matches forward
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        statusLabel.setText(Bundle.getMessage("StatSettingReverseTrim"));
                        setThrottle(false, 28);
                        setSpeedMatchStateTimerDuration(8000);
                        stepDuration = 1;
                    } else {
                        setSpeedMatchError(targetTopSpeedKPH);

                        if (Math.abs(speedMatchError) < ALLOWED_SPEED_MATCH_ERROR) {
                            initNextSpeedMatcherState(SpeedMatcherState.COMPLETE);
                        } else {
                            reverseTrimValue = getNextSpeedMatchValue(lastReverseTrimValue, REVERSE_TRIM_MAX, REVERSE_TRIM_MIN);

                            if (((lastReverseTrimValue == REVERSE_TRIM_MAX) || (lastReverseTrimValue == REVERSE_TRIM_MIN)) && (reverseTrimValue == lastReverseTrimValue)) {
                                statusLabel.setText(Bundle.getMessage("StatSetReverseTrimFail"));
                                logger.info("Unable to trim reverse to match forward");
                                abort();
                                break;
                            }

                            lastReverseTrimValue = reverseTrimValue;
                            writeReverseTrim(reverseTrimValue);
                        }
                    }
                }
                break;

            case COMPLETE:
                if (programmerState == ProgrammerState.IDLE) {
                    statusLabel.setText(Bundle.getMessage("StatSpeedMatchComplete"));
                    setThrottle(true, 0);
                    initNextSpeedMatcherState(SpeedMatcherState.CLEAN_UP);
                }
                break;

            case USER_STOPPED:
                if (programmerState == ProgrammerState.IDLE) {
                    statusLabel.setText(Bundle.getMessage("StatUserStoppedSpeedMatch"));
                    setThrottle(true, 0);
                    initNextSpeedMatcherState(SpeedMatcherState.CLEAN_UP);
                }
                break;

            case CLEAN_UP:
                //wrap it up
                if (programmerState == ProgrammerState.IDLE) {
                    cleanUpSpeedMatcher();
                }
                break;

            default:
                cleanUpSpeedMatcher();
                logger.error("Unexpected speed match timeout");
                break;
        }

        if (speedMatcherState != SpeedMatcherState.IDLE) {
            startSpeedMatchStateTimer();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="ThrottleListener Overrides">
    /**
     * Called when a throttle is found
     *
     * @param t the requested DccThrottle
     */
    @Override
    public void notifyThrottleFound(DccThrottle t) {
        super.notifyThrottleFound(t);

        if (speedMatcherState == SpeedMatcherState.WAIT_FOR_THROTTLE) {
            logger.info("Starting speed matching");
            // using speed matching timer to trigger each phase of speed matching            
            initNextSpeedMatcherState(SpeedMatcherState.INIT_THROTTLE);
            startSpeedMatchStateTimer();
        } else {
            cleanUpSpeedMatcher();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Helper Functions">
    /**
     * Gets the interpolated CV value for the given speed step in the speed
     * table
     *
     * @param speedStep the SpeedTableStep to get the speed for
     * @return the target speed for the given speed step in KPH
     */
    private int getInterpolatedSpeedTableCVValue(SpeedTableStep speedStep) {
        SpeedTableStep maxStep;
        SpeedTableStep minStep;
        int maxStepCVValue;
        int minStepCVValue;

        if (speedStep.getSpeedStep() >= SpeedTableStep.STEP19.getSpeedStep()) {
            maxStep = SpeedTableStep.STEP28;
            minStep = SpeedTableStep.STEP19;
            maxStepCVValue = STEP28_VALUE;
            minStepCVValue = step19CVValue;
        } else if (speedStep.getSpeedStep() >= SpeedTableStep.STEP10.getSpeedStep()) {
            maxStep = SpeedTableStep.STEP19;
            minStep = SpeedTableStep.STEP10;
            maxStepCVValue = step19CVValue;
            minStepCVValue = step10CVValue;
        } else {
            maxStep = SpeedTableStep.STEP10;
            minStep = SpeedTableStep.STEP1;
            maxStepCVValue = step10CVValue;
            minStepCVValue = STEP1_VALUE;
        }

        return Math.round(minStepCVValue + ((((float) (maxStepCVValue - minStepCVValue)) / (maxStep.getSpeedStep() - minStep.getSpeedStep())) * (speedStep.getSpeedStep() - minStep.getSpeedStep())));
    }

    /**
     * Helper function for speed matching a given speed step
     *
     * @param speedStep      the SpeedTableStep to speed match
     * @param targetSpeedKPH the target speed in KPH
     * @param maxCVValue     the maximum allowable value for the CV
     * @param minCVValue     the minimum allowable value for the CV
     * @param nextState      the SpeedMatcherState to advance to if speed
     *                       matching is complete
     */
    private void speedMatchSpeedTableStep(SpeedTableStep speedStep, float targetSpeedKPH, int maxCVValue, int minCVValue, SpeedMatcherState nextState) {
        if (stepDuration == 0) {
            statusLabel.setText(Bundle.getMessage("StatSettingSpeed", speedStep.getCV() + " (Speed Step " + String.valueOf(speedStep.getSpeedStep()) + ")"));
            logger.info("Setting CV {} (speed step {}) to {} KPH ({} MPH)", speedStep.getCV(), speedStep.getSpeedStep(), String.valueOf(targetSpeedKPH), String.valueOf(Speed.kphToMph(targetSpeedKPH)));
            setThrottle(true, speedStep.getSpeedStep());
            setSpeedMatchStateTimerDuration(8000);
            stepDuration = 1;
        } else {
            setSpeedMatchError(targetSpeedKPH);

            if (Math.abs(speedMatchError) < ALLOWED_SPEED_MATCH_ERROR) {
                initNextSpeedMatcherState(nextState);
            } else {
                speedMatchCVValue = getNextSpeedMatchValue(lastSpeedMatchCVValue, maxCVValue, minCVValue);

                if (((speedMatchCVValue == maxCVValue) || (speedMatchCVValue == minCVValue)) && (speedMatchCVValue == lastSpeedMatchCVValue)) {
                    statusLabel.setText(Bundle.getMessage("StatSetSpeedFail", speedStep.getCV() + " (Speed Step " + String.valueOf(speedStep.getSpeedStep()) + ")"));
                    logger.info("Unable to achieve desired speed for CV {} (Speed Step {})", speedStep.getCV(), String.valueOf(speedStep.getSpeedStep()));
                    abort();
                    return;
                }

                lastSpeedMatchCVValue = speedMatchCVValue;
                writeSpeedTableStep(speedStep, speedMatchCVValue);
            }
        }
    }

    /**
     * Aborts the speed matching process programmatically
     */
    private void abort() {
        initNextSpeedMatcherState(SpeedMatcherState.CLEAN_UP);
    }

    /**
     * Stops the speed matching process due to user input
     */
    private void userStop() {
        initNextSpeedMatcherState(SpeedMatcherState.USER_STOPPED);
    }

    /**
     * Sets up the speed match state by clearing the speed match error, clearing
     * the step duration, setting the timer duration, and setting the next state
     *
     * @param nextState - next SpeedMatcherState to set
     */
    protected void initNextSpeedMatcherState(SpeedMatcherState nextState) {
        resetSpeedMatchError();
        stepDuration = 0;
        speedMatcherState = nextState;
        setSpeedMatchStateTimerDuration(1800);
    }
    //</editor-fold>

    //debugging logger
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BasicESUTableSpeedMatcher.class);
}
