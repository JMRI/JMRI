package jmri.jmrix.bachrus.speedmatcher.basic;

import jmri.DccThrottle;
import jmri.jmrix.bachrus.Speed;
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcherConfig;

/**
 * This is a simple speed matcher which will speed match a locomotive to a given
 * start and top speed using the complex speed table. Speed steps 1, 10, 19, and
 * 28 will be set according to values interpolated linearly between the given
 * start and to speeds. Values for the remaining CVs will interpolated between
 * these 4 CVs. This is done to reduce the time the speed match takes and to
 * increase likelihood of success.
 *
 * @author toddt
 */
public class BasicSpeedTableSpeedMatcher extends BasicSpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Constants">
    private final int INITIAL_STEP1_VALUE = 1;
    private final int INITIAL_STEP28_VALUE = 255;
    private final int INITIAL_TRIM_VALUE = 128;
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

    private int speedMatchCVValue = INITIAL_STEP28_VALUE;
    private int lastSpeedMatchCVValue = INITIAL_STEP28_VALUE;

    private int reverseTrimValue = INITIAL_TRIM_VALUE;
    private int lastReverseTrimValue = INITIAL_TRIM_VALUE;

    private final float targetStep28SpeedKPH;
    private final float targetStep19SpeedKPH;
    private final float targetStep10SpeedKPH;
    private final float targetStep1SpeedKPH;

    private int Step28CVValue;
    private int Step19CVValue;
    private int Step10CVValue;
    private int Step1CVValue;

    private SpeedMatcherState speedMatcherState = SpeedMatcherState.IDLE;
    //</editor-fold>

    public BasicSpeedTableSpeedMatcher(SpeedMatcherConfig config) {
        super(config);

        targetStep28SpeedKPH = targetTopSpeedKPH;
        targetStep19SpeedKPH = GetSpeedForSpeedStep(SpeedTableStep.STEP19);
        targetStep10SpeedKPH = GetSpeedForSpeedStep(SpeedTableStep.STEP10);
        targetStep1SpeedKPH = targetStartSpeedKPH;
    }

    //<editor-fold defaultstate="collapsed" desc="SpeedMatcher Overrides">
    @Override
    public boolean Start() {
        if (!super.Validate()) {
            return false;
        }

        //reset instance variables
        speedMatchCVValue = INITIAL_STEP28_VALUE;
        lastSpeedMatchCVValue = INITIAL_STEP28_VALUE;
        reverseTrimValue = INITIAL_TRIM_VALUE;
        lastReverseTrimValue = INITIAL_TRIM_VALUE;

        speedMatcherState = SpeedMatcherState.WAIT_FOR_THROTTLE;

        if (!super.InitializeAndStartSpeedMatcher(e -> speedMatchTimeout())) {
            CleanUp();
            return false;
        }

        startStopButton.setText(Bundle.getMessage("btnStopSpeedMatch"));

        return true;
    }

    @Override
    public void Stop() {
        if (!IsIdle()) {
            logger.info("Speed matching manually stopped");
            UserStop();
        } else {
            CleanUp();
        }
    }

    @Override
    public boolean IsIdle() {
        return speedMatcherState == SpeedMatcherState.IDLE;
    }

    @Override
    protected void CleanUp() {
        speedMatcherState = SpeedMatcherState.IDLE;
        super.CleanUp();
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Speed Matcher State">
    private synchronized void speedMatchTimeout() {
        switch (speedMatcherState) {
            case WAIT_FOR_THROTTLE:
                CleanUp();
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
                    writeMomentumAccel(0);
                    initNextSpeedMatcherState(SpeedMatcherState.INIT_DECEL);
                }
                break;

            case INIT_DECEL:
                //set deceleration mementum to 0 (CV 4)
                if (programmerState == ProgrammerState.IDLE) {
                    writeMomentumDecel(0);
                    initNextSpeedMatcherState(SpeedMatcherState.INIT_SPEED_TABLE);
                }
                break;

            case INIT_SPEED_TABLE:
                //initialize every speed table step to its value (so Speed Table Step 1 = 1, etc.)
                //except Speed Table Step 28 = 255
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        initSpeedTableStepValue = INITIAL_STEP1_VALUE;
                        initSpeedTableStep = SpeedTableStep.STEP1;
                        stepDuration = 1;
                    }

                    if (initSpeedTableStep == SpeedTableStep.STEP28) {
                        initSpeedTableStepValue = INITIAL_STEP28_VALUE;
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
                    writeForwardTrim(INITIAL_TRIM_VALUE);
                    initNextSpeedMatcherState(SpeedMatcherState.INIT_REVERSE_TRIM);
                }
                break;

            case INIT_REVERSE_TRIM:
                //set reverse trim to 128 (CV 95)
                if (programmerState == ProgrammerState.IDLE) {
                    writeReverseTrim(INITIAL_TRIM_VALUE);
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
                    SpeedMatchSpeedStepInner(SpeedTableStep.STEP28, targetStep28SpeedKPH, 255, 20, SpeedMatcherState.RE_INIT_SPEED_TABLE_TOP_THIRD);
                    Step28CVValue = speedMatchCVValue;
                }
                break;

            case RE_INIT_SPEED_TABLE_TOP_THIRD:
                //Re-initialize Speed Steps 19-27 based off value for Step 28
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        initSpeedTableStep = SpeedTableStep.STEP27;
                        stepDuration = 1;
                    }

                    writeSpeedTableStep(initSpeedTableStep, Step28CVValue);

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
                    SpeedMatchSpeedStepInner(SpeedTableStep.STEP19, targetStep19SpeedKPH, Step28CVValue - 9, 19, SpeedMatcherState.RE_INIT_SPEED_TABLE_MIDDLE_THIRD);
                    Step19CVValue = speedMatchCVValue;
                }
                break;

            case RE_INIT_SPEED_TABLE_MIDDLE_THIRD:
                //Re-initialize Speed Steps 10-18 based off value for Step 19
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        initSpeedTableStep = SpeedTableStep.STEP18;
                        stepDuration = 1;
                    }

                    writeSpeedTableStep(initSpeedTableStep, Step19CVValue);

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
                    SpeedMatchSpeedStepInner(SpeedTableStep.STEP10, targetStep10SpeedKPH, Step19CVValue - 9, 10, SpeedMatcherState.RE_INIT_SPEED_TABLE_BOTTOM_THIRD);
                    Step10CVValue = speedMatchCVValue;
                }
                break;

            case RE_INIT_SPEED_TABLE_BOTTOM_THIRD:
                //Re-initialize Speed Steps 1-9 based off value for Step 10
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        initSpeedTableStep = SpeedTableStep.STEP9;
                        stepDuration = 1;
                    }

                    writeSpeedTableStep(initSpeedTableStep, Step10CVValue);

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
                    SpeedMatchSpeedStepInner(SpeedTableStep.STEP1, targetStep1SpeedKPH, Step10CVValue - 9, 1, SpeedMatcherState.INTERPOLATE_SPEED_TABLE);
                    Step1CVValue = speedMatchCVValue;
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

                    int interpolatedSpeedStepCVValue = GetInterpolatedSpeedTableCVValue(interpolationSpeedTableStep);
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
                        setSpeedMatchStateTimerDuration(15000);
                        stepDuration = 1;
                    } else {
                        setSpeedMatchError(targetTopSpeedKPH);

                        if ((speedMatchError < 1) && (speedMatchError > -1)) {
                            initNextSpeedMatcherState(SpeedMatcherState.COMPLETE);
                        } else {
                            reverseTrimValue = getNextSpeedMatchValue(lastReverseTrimValue, 255, 1);

                            if (((lastReverseTrimValue == 1) || (lastReverseTrimValue == 255)) && (reverseTrimValue == lastReverseTrimValue)) {
                                statusLabel.setText(Bundle.getMessage("StatSetReverseTripFail"));
                                logger.info("Unable to trim reverse to match forward");
                                Abort();
                                break;
                            } else {
                                lastReverseTrimValue = reverseTrimValue;
                                writeReverseTrim(reverseTrimValue);
                            }
                            setSpeedMatchStateTimerDuration(8000);
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
                    statusLabel.setText("User stopped speed matching");
                    setThrottle(true, 0);
                    initNextSpeedMatcherState(SpeedMatcherState.CLEAN_UP);
                }
                break;

            case CLEAN_UP:
                //wrap it up
                if (programmerState == ProgrammerState.IDLE) {
                    CleanUp();
                }
                break;

            default:
                CleanUp();
                logger.error("Unexpected speed match timeout");
                break;
        }

        if (speedMatcherState != SpeedMatcherState.IDLE) {
            startSpeedMatchStateTimer();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Programmer">
    /**
     * Starts writing a Speed Table Step CV (CV 67-94) using the ops mode
     * programmer
     *
     * @param step  the SpeedTableStep to set
     * @param value speed table step value (0-255 inclusive)
     */
    private synchronized void writeSpeedTableStep(SpeedTableStep step, int value) {
        programmerState = ProgrammerState.WRITE_SPEED_TABLE_STEP;
        String message = Bundle.getMessage("ProgSetCV", step.getCV() + " (Speed Step " + String.valueOf(step.getSpeedStep()) + ")", value);
        statusLabel.setText(message);
        logger.info(message);
        startOpsModeWrite(step.getCV(), value);
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
            CleanUp();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Helper Functions">
    private float GetSpeedForSpeedStep(SpeedTableStep speedStep) {
        return targetStartSpeedKPH + (((targetTopSpeedKPH - targetStartSpeedKPH) / 27) * (speedStep.getSpeedStep() - 1));
    }

    private int GetInterpolatedSpeedTableCVValue(SpeedTableStep speedStep) {
        SpeedTableStep maxStep;
        SpeedTableStep minStep;
        int maxStepCVValue;
        int minStepCVValue;

        if (initSpeedTableStep.getSpeedStep() >= SpeedTableStep.STEP19.getSpeedStep()) {
            maxStep = SpeedTableStep.STEP28;
            minStep = SpeedTableStep.STEP19;
            maxStepCVValue = Step28CVValue;
            minStepCVValue = Step19CVValue;
        } else if (initSpeedTableStep.getSpeedStep() >= SpeedTableStep.STEP10.getSpeedStep()) {
            maxStep = SpeedTableStep.STEP19;
            minStep = SpeedTableStep.STEP10;
            maxStepCVValue = Step19CVValue;
            minStepCVValue = Step10CVValue;
        } else {
            maxStep = SpeedTableStep.STEP10;
            minStep = SpeedTableStep.STEP1;
            maxStepCVValue = Step10CVValue;
            minStepCVValue = Step1CVValue;
        }

        return Math.round(minStepCVValue + ((((float) (maxStepCVValue - minStepCVValue)) / (maxStep.getSpeedStep() - minStep.getSpeedStep())) * (speedStep.getSpeedStep() - minStep.getSpeedStep())));
    }

    private void SpeedMatchSpeedStepInner(SpeedTableStep speedStep, float targetSpeedKPH, int maxCVValue, int minCVValue, SpeedMatcherState nextState) {
        if (stepDuration == 0) {
            statusLabel.setText(Bundle.getMessage("StatSettingSpeed", speedStep.getCV() + " (Speed Step " + String.valueOf(speedStep.getSpeedStep()) + ")"));
            logger.info("Setting speed step " + speedStep.getSpeedStep() + " to " + String.valueOf(targetSpeedKPH) + " KPH ( " + String.valueOf(Speed.kphToMph(targetSpeedKPH)) + " MPH)");
            setThrottle(true, speedStep.getSpeedStep());
            setSpeedMatchStateTimerDuration(8000);
            stepDuration = 1;
        } else {
            setSpeedMatchError(targetSpeedKPH);

            if ((speedMatchError < 1) && (speedMatchError > -1)) {
                initNextSpeedMatcherState(nextState);
            } else {
                speedMatchCVValue = getNextSpeedMatchValue(lastSpeedMatchCVValue, maxCVValue, minCVValue);

                if (((speedMatchCVValue == maxCVValue) || (speedMatchCVValue == minCVValue)) && (speedMatchCVValue == lastSpeedMatchCVValue)) {
                    statusLabel.setText(Bundle.getMessage("StatSetSpeedFail", speedStep.getCV() + " (Speed Step " + String.valueOf(speedStep.getSpeedStep()) + ")"));
                    logger.info("Unable to achieve desired speed for CV " + speedStep.getCV() + " (Speed Step " + String.valueOf(speedStep.getSpeedStep()) + ")");
                    Abort();
                    return;
                }

                lastSpeedMatchCVValue = speedMatchCVValue;
                writeSpeedTableStep(speedStep, speedMatchCVValue);
                setSpeedMatchStateTimerDuration(8000);
            }
        }
    }

    private void Abort() {
        initNextSpeedMatcherState(SpeedMatcherState.CLEAN_UP);
    }

    private void UserStop() {
        initNextSpeedMatcherState(SpeedMatcherState.USER_STOPPED);
    }

    /**
     * Sets up the speed match state by clearing the speed match error, clearing
     * the step duration, setting the timer duration to 500 ms, and setting the
     * next state
     *
     * @param nextState - next SpeedMatcherState to set
     */
    protected void initNextSpeedMatcherState(SpeedMatcherState nextState) {
        resetSpeedMatchError();
        stepDuration = 0;
        speedMatcherState = nextState;
        setSpeedMatchStateTimerDuration(1200);
    }
    //</editor-fold>
}
