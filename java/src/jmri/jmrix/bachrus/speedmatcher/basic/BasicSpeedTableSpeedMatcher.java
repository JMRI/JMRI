package jmri.jmrix.bachrus.speedmatcher.basic;

import jmri.DccThrottle;
import jmri.jmrix.bachrus.Speed;

/**
 * This is a simple speed matcher which will speed match a locomotive to a given
 * start and top speed using the complex speed table. Speed steps 1, 10, 19, and
 * 28 will be set according to values interpolated linearly between the given
 * start and to speeds. Values for the remaining CVs will interpolated between
 * these 4 CVs. This is done to reduce the time the speed match takes and to
 * increase likelihood of success.
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class BasicSpeedTableSpeedMatcher extends BasicSpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Constants">
    private final int INITIAL_STEP1 = 1;
    private final int INITIAL_STEP28 = 255;
    private final int INITIAL_TRIM = 128;

    private final int STEP28_MAX = 255;
    private final int STEP28_MIN = 28;
    private final int STEP19_MIN = 19;
    private final int STEP10_MIN = 10;
    private final int STEP1_MIN = 1;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Enums">
    protected enum SpeedMatcherState {
        IDLE,
        WAIT_FOR_THROTTLE,
        INIT_THROTTLE,
        INIT_ACCEL,
        INIT_DECEL,
        INIT_SPEED_TABLE,
        INIT_FORWARD_TRIM,
        INIT_REVERSE_TRIM,
        POST_INIT,
        FORWARD_WARM_UP,
        FORWARD_SPEED_MATCH_STEP28,
        RE_INIT_SPEED_TABLE_TOP_THIRD,
        FORWARD_SPEED_MATCH_STEP19,
        RE_INIT_SPEED_TABLE_MIDDLE_THIRD,
        FORWARD_SPEED_MATCH_STEP10,
        RE_INIT_SPEED_TABLE_BOTTOM_THIRD,
        FORWARD_SPEED_MATCH_STEP1,
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

    private int speedMatchCVValue = INITIAL_STEP28;
    private int lastSpeedMatchCVValue = INITIAL_STEP28;

    private int reverseTrimValue = INITIAL_TRIM;
    private int lastReverseTrimValue = INITIAL_TRIM;

    private final float targetStep28SpeedKPH;
    private final float targetStep19SpeedKPH;
    private final float targetStep10SpeedKPH;
    private final float targetStep1SpeedKPH;

    private int step28CVValue;
    private int step19CVValue;
    private int step10CVValue;
    private int step1CVValue;

    private SpeedMatcherState speedMatcherState = SpeedMatcherState.IDLE;
    //</editor-fold>

    /**
     * Constructs the BasicSpeedTableSpeedMatcher from a BasicSpeedMatcherConfig
     *
     * @param config SpeedStepScaleSpeedMatcherConfig
     */
    public BasicSpeedTableSpeedMatcher(BasicSpeedMatcherConfig config) {
        super(config);

        targetStep28SpeedKPH = targetTopSpeedKPH;
        targetStep19SpeedKPH = getSpeedForSpeedStep(SpeedTableStep.STEP19, targetStartSpeedKPH, targetTopSpeedKPH);
        targetStep10SpeedKPH = getSpeedForSpeedStep(SpeedTableStep.STEP10, targetStartSpeedKPH, targetTopSpeedKPH);
        targetStep1SpeedKPH = targetStartSpeedKPH;
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
        speedMatchCVValue = INITIAL_STEP28;
        lastSpeedMatchCVValue = INITIAL_STEP28;
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
                    initNextSpeedMatcherState(SpeedMatcherState.INIT_SPEED_TABLE);
                }
                break;

            case INIT_SPEED_TABLE:
                //initialize every speed table step to 1 except speed table step 28 = 255
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        initSpeedTableStepValue = INITIAL_STEP1;
                        initSpeedTableStep = SpeedTableStep.STEP1;
                        stepDuration = 1;
                    }

                    if (initSpeedTableStep == SpeedTableStep.STEP28) {
                        initSpeedTableStepValue = INITIAL_STEP28;
                    }

                    writeSpeedTableStep(initSpeedTableStep, initSpeedTableStepValue);

                    initSpeedTableStep = initSpeedTableStep.getNext();
                    if (initSpeedTableStep == null) {
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
                    nextState = SpeedMatcherState.FORWARD_SPEED_MATCH_STEP28;
                }
                initNextSpeedMatcherState(nextState);
                break;
            }

            case FORWARD_WARM_UP:
                //Run 4 minutes at high speed forward
                statusLabel.setText(Bundle.getMessage("StatForwardWarmUp", warmUpForwardSeconds - stepDuration));

                if (stepDuration >= warmUpForwardSeconds) {
                    initNextSpeedMatcherState(SpeedMatcherState.FORWARD_SPEED_MATCH_STEP28);
                } else {
                    if (stepDuration == 0) {
                        setSpeedMatchStateTimerDuration(5000);
                        setThrottle(true, 28);
                    }
                    stepDuration += 5;
                }
                break;

            case FORWARD_SPEED_MATCH_STEP28:
                //Use PID Controller to adjust Speed Step 28 to the max speed
                if (programmerState == ProgrammerState.IDLE) {
                    speedMatchSpeedStepInner(SpeedTableStep.STEP28, targetStep28SpeedKPH, STEP28_MAX, STEP28_MIN, SpeedMatcherState.RE_INIT_SPEED_TABLE_TOP_THIRD);
                    step28CVValue = speedMatchCVValue;
                }
                break;

            case RE_INIT_SPEED_TABLE_TOP_THIRD:
                //Re-initialize Speed Steps 19-27 based off value for Step 28
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        initSpeedTableStep = SpeedTableStep.STEP27;
                        stepDuration = 1;
                    }

                    writeSpeedTableStep(initSpeedTableStep, step28CVValue);

                    if (initSpeedTableStep == SpeedTableStep.STEP19) {
                        initNextSpeedMatcherState(SpeedMatcherState.FORWARD_SPEED_MATCH_STEP19);
                    } else {
                        initSpeedTableStep = initSpeedTableStep.getPrevious();
                    }
                }
                break;

            case FORWARD_SPEED_MATCH_STEP19:
                //Use PID Controller to adjust Speed Step 19 to the interpolated speed
                if (programmerState == ProgrammerState.IDLE) {
                    speedMatchSpeedStepInner(SpeedTableStep.STEP19, targetStep19SpeedKPH, step28CVValue - 9, STEP19_MIN, SpeedMatcherState.RE_INIT_SPEED_TABLE_MIDDLE_THIRD);
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
                    speedMatchSpeedStepInner(SpeedTableStep.STEP10, targetStep10SpeedKPH, step19CVValue - 9, STEP10_MIN, SpeedMatcherState.RE_INIT_SPEED_TABLE_BOTTOM_THIRD);
                    step10CVValue = speedMatchCVValue;
                }
                break;

            case RE_INIT_SPEED_TABLE_BOTTOM_THIRD:
                //Re-initialize Speed Steps 1-9 based off value for Step 10
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        initSpeedTableStep = SpeedTableStep.STEP9;
                        stepDuration = 1;
                    }

                    writeSpeedTableStep(initSpeedTableStep, step10CVValue);

                    if (initSpeedTableStep == SpeedTableStep.STEP1) {
                        initNextSpeedMatcherState(SpeedMatcherState.FORWARD_SPEED_MATCH_STEP1);
                    } else {
                        initSpeedTableStep = initSpeedTableStep.getPrevious();
                    }

                }
                break;

            case FORWARD_SPEED_MATCH_STEP1:
                //Use PID Controller to adjust Speed Step 1 to the minimum speed
                if (programmerState == ProgrammerState.IDLE) {
                    speedMatchSpeedStepInner(SpeedTableStep.STEP1, targetStep1SpeedKPH, step10CVValue - 9, STEP1_MIN, SpeedMatcherState.INTERPOLATE_SPEED_TABLE);
                    step1CVValue = speedMatchCVValue;
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
            maxStepCVValue = step28CVValue;
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
            minStepCVValue = step1CVValue;
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
    private void speedMatchSpeedStepInner(SpeedTableStep speedStep, float targetSpeedKPH, int maxCVValue, int minCVValue, SpeedMatcherState nextState) {
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
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BasicSpeedTableSpeedMatcher.class);
}
