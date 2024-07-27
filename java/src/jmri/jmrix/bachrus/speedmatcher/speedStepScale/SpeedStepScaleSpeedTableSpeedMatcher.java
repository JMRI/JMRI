package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import jmri.DccThrottle;
import jmri.jmrix.bachrus.Speed;

/**
 * This is a speed step scale speed matcher which will speed match a locomotive
 * such that its speed in mph/kph will be equal to its speed step in 128 speed
 * step mode. This uses the complex speed table, and the locomotive's speed will
 * plateau at either its actual top speed or the set max speed, whichever is
 * lower.
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class SpeedStepScaleSpeedTableSpeedMatcher extends SpeedStepScaleSpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Constants">
    private final int INITIAL_STEP1 = 1;
    private final int INITIAL_STEP28 = 255;
    private final int INITIAL_TRIM = 128;

    private final int TOP_SPEED_STEP_MAX = 255;
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
        READ_MAX_SPEED,
        FORWARD_SPEED_MATCH_STEP28,
        SET_UPPER_SPEED_STEPS,
        FORWARD_SPEED_MATCH,
        POST_SPEED_MATCH,
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
    private SpeedTableStep speedMatchSpeedTableStep;
    private int speedMatchMaxSpeedStep;

    private int step28CVValue = INITIAL_STEP28;
    private float speedStepTargetSpeedKPH;

    private int speedMatchCVValue = INITIAL_STEP28;
    private int lastSpeedMatchCVValue = INITIAL_STEP28;
    private int lastSpeedTableStepCVValue = INITIAL_STEP28;

    private int reverseTrimValue = INITIAL_TRIM;
    private int lastReverseTrimValue = INITIAL_TRIM;

    private SpeedMatcherState speedMatcherState = SpeedMatcherState.IDLE;
    //</editor-fold>

    /**
     * Constructs the SpeedStepScaleSpeedTableSpeedMatcher from a
     * SpeedStepScaleSpeedMatcherConfig
     *
     * @param config SpeedStepScaleSpeedMatcherConfig
     */
    public SpeedStepScaleSpeedTableSpeedMatcher(SpeedStepScaleSpeedMatcherConfig config) {
        super(config);
    }

    //<editor-fold defaultstate="collapsed" desc="SpeedMatcherOverrides">
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
        lastSpeedTableStepCVValue = INITIAL_STEP28;
        reverseTrimValue = INITIAL_TRIM;
        lastReverseTrimValue = INITIAL_TRIM;
        measuredMaxSpeedKPH = 0;
        speedMatchMaxSpeedKPH = 0;

        speedMatcherState = SpeedMatcherState.WAIT_FOR_THROTTLE;

        actualMaxSpeedField.setText(String.format("___"));

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
                    nextState = SpeedMatcherState.READ_MAX_SPEED;
                }
                initNextSpeedMatcherState(nextState);
                break;
            }

            case FORWARD_WARM_UP:
                //Run 4 minutes at high speed forward
                statusLabel.setText(Bundle.getMessage("StatForwardWarmUp", warmUpForwardSeconds - stepDuration));

                if (stepDuration >= warmUpForwardSeconds) {
                    initNextSpeedMatcherState(SpeedMatcherState.READ_MAX_SPEED);
                } else {
                    if (stepDuration == 0) {
                        setSpeedMatchStateTimerDuration(5000);
                        setThrottle(true, 28);
                    }
                    stepDuration += 5;
                }
                break;

            case READ_MAX_SPEED:
                //Run 10 second at high speed forward and record the speed
                if (stepDuration == 0) {
                    statusLabel.setText("Recording locomotive's maximum speed");
                    setSpeedMatchStateTimerDuration(10000);
                    setThrottle(true, 28);
                    stepDuration = 1;
                } else {
                    measuredMaxSpeedKPH = currentSpeedKPH;

                    String statusMessage = String.format("Measured maximum speed = %.1f KPH (%.1f MPH)", measuredMaxSpeedKPH, Speed.kphToMph(measuredMaxSpeedKPH));
                    logger.info(statusMessage);
                    
                    float speedMatchMaxSpeed;

                    if (measuredMaxSpeedKPH > targetMaxSpeedKPH) {
                        speedMatchMaxSpeedStep = targetMaxSpeedStep.getSpeedTableStep().getSpeedStep();
                        speedMatchMaxSpeed = targetMaxSpeedStep.getSpeed();
                        speedMatchMaxSpeedKPH = targetMaxSpeedKPH;
                    } else {
                        float measuredMaxSpeed = speedUnit == Speed.Unit.MPH ? Speed.kphToMph(measuredMaxSpeedKPH) : measuredMaxSpeedKPH;
                        speedMatchMaxSpeedStep = getNextLowestSpeedTableStepForSpeed(measuredMaxSpeed);
                        speedMatchMaxSpeed = getSpeedForSpeedTableStep(speedMatchMaxSpeedStep);
                        speedMatchMaxSpeedKPH = speedUnit == Speed.Unit.MPH ? Speed.mphToKph(speedMatchMaxSpeed): speedMatchMaxSpeed;
                    }
                    
                    actualMaxSpeedField.setText(String.format("%.1f", speedMatchMaxSpeed));
                    
                    initNextSpeedMatcherState(SpeedMatcherState.FORWARD_SPEED_MATCH_STEP28);
                }
                break;

            case FORWARD_SPEED_MATCH_STEP28:
                //Use PID Controller to adjust speed table step 28 to max speed
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        speedMatchSpeedTableStep = SpeedTableStep.STEP28;
                    }
                    speedMatchSpeedStepInner(TOP_SPEED_STEP_MAX, speedMatchMaxSpeedStep, SpeedMatcherState.SET_UPPER_SPEED_STEPS, true);
                    step28CVValue = speedMatchCVValue;
                }
                break;

            case SET_UPPER_SPEED_STEPS:
                //Set Speed table steps 27 through lowestMaxSpeedStep to step28CVValue
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        speedMatchSpeedTableStep = SpeedTableStep.STEP27;
                        stepDuration = 1;
                    }

                    writeSpeedTableStep(speedMatchSpeedTableStep, step28CVValue);

                    speedMatchSpeedTableStep = speedMatchSpeedTableStep.getPrevious();

                    if (speedMatchSpeedTableStep.getSpeedStep() < speedMatchMaxSpeedStep) {
                        initNextSpeedMatcherState(SpeedMatcherState.FORWARD_SPEED_MATCH);
                    }
                }
                break;

            case FORWARD_SPEED_MATCH:
                //Use PID Controller to adjust table speed steps lowestMaxSpeedStep through 1 to the appropriate speed
                if (programmerState == ProgrammerState.IDLE) {
                    speedMatchSpeedStepInner(lastSpeedTableStepCVValue, speedMatchSpeedTableStep.getSpeedStep(), SpeedMatcherState.POST_SPEED_MATCH);
                }
                break;

            case POST_SPEED_MATCH: {
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
                        setThrottle(false, speedMatchMaxSpeedStep);
                        setSpeedMatchStateTimerDuration(8000);
                        stepDuration = 1;
                    } else {
                        setSpeedMatchError(speedMatchMaxSpeedKPH);

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
     * Helper function for speed matching the current speedMatchSpeedTableStep
     *
     * @param maxCVValue the maximum allowable value for the CV
     * @param minCVValue the minimum allowable value for the CV
     * @param nextState  the SpeedMatcherState to advance to if speed matching
     *                   is complete
     */
    private void speedMatchSpeedStepInner(int maxCVValue, int minCVValue, SpeedMatcherState nextState) {
        speedMatchSpeedStepInner(maxCVValue, minCVValue, nextState, false);
    }

    /**
     * Helper function for speed matching the current speedMatchSpeedTableStep
     *
     * @param maxCVValue     the maximum allowable value for the CV
     * @param minCVValue     the minimum allowable value for the CV
     * @param nextState      the SpeedMatcherState to advance to if speed
     *                       matching is complete
     * @param forceNextState set to true to force speedMatcherState to the next
     *                       state when speed matching the current
     *                       speedMatchSpeedTableStep is complete
     */
    private void speedMatchSpeedStepInner(int maxCVValue, int minCVValue, SpeedMatcherState nextState, boolean forceNextState) {
        if (stepDuration == 0) {
            speedStepTargetSpeedKPH = getSpeedStepScaleSpeedInKPH(speedMatchSpeedTableStep.getSpeedStep());

            statusLabel.setText(Bundle.getMessage("StatSettingSpeed", speedMatchSpeedTableStep.getCV() + " (Speed Step " + String.valueOf(speedMatchSpeedTableStep.getSpeedStep()) + ")"));
            logger.info("Setting CV {} (speed step {}) to {} KPH ({} MPH)", speedMatchSpeedTableStep.getCV(), speedMatchSpeedTableStep.getSpeedStep(), String.valueOf(speedStepTargetSpeedKPH), String.valueOf(Speed.kphToMph(speedStepTargetSpeedKPH)));

            setThrottle(true, speedMatchSpeedTableStep.getSpeedStep());

            writeSpeedTableStep(speedMatchSpeedTableStep, speedMatchCVValue);

            setSpeedMatchStateTimerDuration(8000);
            stepDuration = 1;
        } else {
            setSpeedMatchError(speedStepTargetSpeedKPH);

            if (Math.abs(speedMatchError) < ALLOWED_SPEED_MATCH_ERROR) {
                lastSpeedTableStepCVValue = speedMatchCVValue;

                if (forceNextState) {
                    initNextSpeedMatcherState(nextState);
                    return;
                }

                speedMatchSpeedTableStep = speedMatchSpeedTableStep.getPrevious();

                if (speedMatchSpeedTableStep != null) {
                    initNextSpeedMatcherState(speedMatcherState);
                } else {
                    initNextSpeedMatcherState(nextState);
                }
            } else {
                speedMatchCVValue = getNextSpeedMatchValue(lastSpeedMatchCVValue, maxCVValue, minCVValue);

                if (((speedMatchCVValue == maxCVValue) || (speedMatchCVValue == minCVValue)) && (speedMatchCVValue == lastSpeedMatchCVValue)) {
                    statusLabel.setText(Bundle.getMessage("StatSetSpeedFail", speedMatchSpeedTableStep.getCV() + " (Speed Step " + String.valueOf(speedMatchSpeedTableStep.getSpeedStep()) + ")"));
                    logger.info("Unable to achieve desired speed for CV {} (Speed Step {})", speedMatchSpeedTableStep.getCV(), String.valueOf(speedMatchSpeedTableStep.getSpeedStep()));
                    abort();
                    return;
                }

                lastSpeedMatchCVValue = speedMatchCVValue;
                writeSpeedTableStep(speedMatchSpeedTableStep, speedMatchCVValue);
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
     * @param nextState next SpeedMatcherState to set
     */
    protected void initNextSpeedMatcherState(SpeedMatcherState nextState) {
        resetSpeedMatchError();
        stepDuration = 0;
        speedMatcherState = nextState;
        setSpeedMatchStateTimerDuration(1800);
    }
    //</editor-fold>

    //debugging logger
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SpeedStepScaleSpeedTableSpeedMatcher.class);
}
