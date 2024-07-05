package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import jmri.DccThrottle;
import jmri.jmrix.bachrus.Speed;

/**
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class SpeedStepScaleSpeedTableSpeedMatcher extends SpeedStepScaleSpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Constants">
    private final int INITIAL_STEP1 = 1;
    private final int INITIAL_STEP28 = 255;
    private final int INITIAL_TRIM = 128;

    private final int TOP_SPEED_STEP_MAX = 255;
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

    private int speedMatchCVValue = INITIAL_STEP28;
    private int lastSpeedMatchCVValue = INITIAL_STEP28;

    private int reverseTrimValue = INITIAL_TRIM;
    private int lastReverseTrimValue = INITIAL_TRIM;

    private SpeedMatcherState speedMatcherState = SpeedMatcherState.IDLE;
    
    private float measuredMaxSpeedKPH = 0;
    //</editor-fold>

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
        reverseTrimValue = INITIAL_TRIM;
        lastReverseTrimValue = INITIAL_TRIM;
        measuredMaxSpeedKPH =  0;

        speedMatcherState = SpeedMatcherState.WAIT_FOR_THROTTLE;

        if (!initializeAndStartSpeedMatcher(e -> speedMatchTimeout())) {
            cleanUpSpeedMatcher();
            return false;
        }

        startStopButton.setText(Bundle.getMessage("btnStopSpeedMatch"));

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
                    statusLabel.setText(statusMessage);
                    logger.info(statusMessage);
                    initNextSpeedMatcherState(SpeedMatcherState.FORWARD_SPEED_MATCH_STEP28);
                }                
                break;
                
            case FORWARD_SPEED_MATCH_STEP28:
                //TODO: TRW - implementation
                break;
                
            case SET_UPPER_SPEED_STEPS:
                //TODO: TRW - implementation
                break;
                
            case FORWARD_SPEED_MATCH:
                //TODO: TRW - implementation
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
                        //TODO: TRW - Figure out correct throttle position
                        //setThrottle(false, 28);
                        setSpeedMatchStateTimerDuration(8000);
                        stepDuration = 1;
                    } else {
                        //TODO: TRW - Figure out correct speed to target
                        //setSpeedMatchError(targetTopSpeedKPH);

                        if (Math.abs(speedMatchError) < ALLOWED_SPEED_MATCH_ERROR) {
                            initNextSpeedMatcherState(SpeedMatcherState.COMPLETE);
                        } else {
                            reverseTrimValue = getNextSpeedMatchValue(lastReverseTrimValue, REVERSE_TRIM_MAX, REVERSE_TRIM_MIN);

                            if (((lastReverseTrimValue == REVERSE_TRIM_MAX) || (lastReverseTrimValue == REVERSE_TRIM_MIN)) && (reverseTrimValue == lastReverseTrimValue)) {
                                statusLabel.setText(Bundle.getMessage("StatSetReverseTripFail"));
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
                    statusLabel.setText("User stopped speed matching");
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
    private void SpeedMatchSpeedStepInner(SpeedTableStep speedStep, int maxCVValue, int minCVValue, SpeedMatcherState nextState) {
        float targetSpeedKPH = getSpeedStepScaleSpeedInKPH(speedStep);

        if (stepDuration == 0) {
            statusLabel.setText(Bundle.getMessage("StatSettingSpeed", speedStep.getCV() + " (Speed Step " + String.valueOf(speedStep.getSpeedStep()) + ")"));
            logger.info("Setting CV {} (speed step {}) to {} KPH ({} MPH)", speedStep.getCV(), speedStep.getSpeedStep(), String.valueOf(targetSpeedKPH), String.valueOf(Speed.kphToMph(targetSpeedKPH)));
            setThrottle(true, speedStep.getSpeedStep());
            setSpeedMatchStateTimerDuration(8000);
            stepDuration = 1;
        }else {
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
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SpeedStepScaleSpeedTableSpeedMatcher.class);
}
