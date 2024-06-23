package jmri.jmrix.bachrus.speedmatcher.basic;

import jmri.DccThrottle;
import jmri.jmrix.bachrus.Speed;
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcherConfig;

/**
 * This is a simple speed matcher which will speed match a locomotive to a given
 * start and top speed using the simple VStart, VMid, and VHigh CVs.
 *
 * @author Todd Wegter
 */
public class BasicSimpleCVSpeedMatcher extends BasicSpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Constants">
    private final int INITIAL_VSTART = 1;
    private final int INITIAL_VMID = 2;
    private final int INITIAL_VHIGH = 255;
    private final int INITIAL_TRIM = 128;

    private final int VHIGH_MAX = 255;
    private final int VHIGH_MIN = INITIAL_VMID + 1;
    private final int VMID_MIN = INITIAL_VSTART + 1;
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
        INIT_VMID,
        INIT_VHIGH,
        INIT_FORWARD_TRIM,
        INIT_REVERSE_TRIM,
        POST_INIT,
        FORWARD_WARM_UP,
        FORWARD_SPEED_MATCH_VHIGH,
        FORWARD_SPEED_MATCH_VMID,
        FORWARD_SPEED_MATCH_VSTART,
        REVERSE_WARM_UP,
        REVERSE_SPEED_MATCH_TRIM,
        COMPLETE,
        USER_STOPPED,
        CLEAN_UP,
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    private int vHigh = INITIAL_VHIGH;
    private int lastVHigh = INITIAL_VHIGH;
    private int vMid = INITIAL_VSTART;
    private int lastVMid = INITIAL_VSTART;
    private int vMidMax;
    private int vStart = INITIAL_VSTART;
    private int lastVStart = INITIAL_VSTART;
    private int vStartMax;
    private int reverseTrimValue = INITIAL_TRIM;
    private int lastReverseTrimValue = INITIAL_TRIM;

    private final float targetMidSpeedKPH;

    private SpeedMatcherState speedMatcherState = SpeedMatcherState.IDLE;

    //</editor-fold>
    public BasicSimpleCVSpeedMatcher(SpeedMatcherConfig config) {
        super(config);

        this.targetMidSpeedKPH = this.targetStartSpeedKPH + ((this.targetTopSpeedKPH - this.targetStartSpeedKPH) / 2);
    }

    //<editor-fold defaultstate="collapsed" desc="SpeedMatcher Overrides">
    @Override
    public boolean Start() {
        if (!super.Validate()) {
            return false;
        }

        //reset instance variables
        vStart = INITIAL_VSTART;
        lastVStart = INITIAL_VSTART;
        vMid = INITIAL_VSTART;
        lastVMid = INITIAL_VSTART;
        vHigh = INITIAL_VHIGH;
        lastVHigh = INITIAL_VHIGH;
        reverseTrimValue = INITIAL_TRIM;
        lastReverseTrimValue = INITIAL_TRIM;

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
    /**
     * Timer timeout handler for the speed match timer
     */
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
                    initNextSpeedMatcherState(SpeedMatcherState.INIT_VMID);
                }
                break;

            case INIT_VMID:
                //set vMid to 1 (CV 6)
                if (programmerState == ProgrammerState.IDLE) {
                    writeVMid(INITIAL_VMID);
                    initNextSpeedMatcherState(SpeedMatcherState.INIT_VHIGH);
                }
                break;

            case INIT_VHIGH:
                //set vHigh to 255 (CV 5)
                if (programmerState == ProgrammerState.IDLE) {
                    writeVHigh(INITIAL_VHIGH);
                    initNextSpeedMatcherState(SpeedMatcherState.INIT_FORWARD_TRIM);
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
                //Run specified forward warm up time at high speed forward
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
                //Use PID Controller logic to adjust vHigh to achieve desired speed
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        statusLabel.setText(Bundle.getMessage("StatSettingSpeed", "5 (vHigh)"));
                        logger.info("Setting CV 5 (vHigh) to " + String.valueOf(targetTopSpeedKPH) + " KPH ( " + String.valueOf(Speed.kphToMph(targetTopSpeedKPH)) + " MPH)");
                        setThrottle(true, 28);
                        setSpeedMatchStateTimerDuration(8000);
                        stepDuration = 1;
                    } else {
                        setSpeedMatchError(targetTopSpeedKPH);

                        if (Math.abs(speedMatchError) < ALLOWED_SPEED_MATCH_ERROR) {
                            initNextSpeedMatcherState(SpeedMatcherState.FORWARD_SPEED_MATCH_VMID);
                        } else {
                            vHigh = getNextSpeedMatchValue(lastVHigh, VHIGH_MAX, VHIGH_MIN);

                            if (((vHigh == VHIGH_MAX) || (vHigh == VHIGH_MIN)) && (vHigh == lastVHigh)) {
                                statusLabel.setText(Bundle.getMessage("StatSetSpeedFail", "5 (vHigh)"));
                                logger.info("Unable to achieve desired speed for CV 5 (vHigh)");
                                Abort();
                                break;
                            }

                            lastVHigh = vHigh;
                            writeVHigh(vHigh);
                        }
                    }
                }
                break;

            case FORWARD_SPEED_MATCH_VMID:
                //Use PID Controller logic to adjust vMid to achieve desired speed
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        vMid = INITIAL_VSTART + ((vHigh - INITIAL_VSTART) / 2);
                        lastVMid = vMid;
                        vMidMax = vHigh - 1;
                        writeVMid(vMid);

                        statusLabel.setText(Bundle.getMessage("StatSettingSpeed", "6 (vMid)"));
                        logger.info("Setting CV 6 (vMid) to " + String.valueOf(targetMidSpeedKPH) + " KPH ( " + String.valueOf(Speed.kphToMph(targetMidSpeedKPH)) + " MPH)");
                        setSpeedMatchStateTimerDuration(8000);
                        setThrottle(true, 14);
                        stepDuration = 1;

                    } else {
                        setSpeedMatchError(targetMidSpeedKPH);

                        if (Math.abs(speedMatchError) < ALLOWED_SPEED_MATCH_ERROR) {
                            initNextSpeedMatcherState(SpeedMatcherState.FORWARD_SPEED_MATCH_VSTART);
                        } else {
                            vMid = getNextSpeedMatchValue(lastVMid, vMidMax, VMID_MIN);

                            if (((vMid == vMidMax) || (vMid == VMID_MIN)) && (vMid == lastVMid)) {
                                statusLabel.setText(Bundle.getMessage("StatSetSpeedFail", "6 (vMid)"));
                                logger.info("Unable to achieve desired speed for CV 6 (vMid)");
                                Abort();
                                break;
                            }

                            lastVMid = vMid;
                            writeVMid(vMid);
                        }
                    }
                }
                break;

            case FORWARD_SPEED_MATCH_VSTART: {
                //Use PID Controller to adjust vStart to achieve desired speed
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        vStartMax = vMid - 1;
                        statusLabel.setText(Bundle.getMessage("StatSettingSpeed", "2 (vStart)"));
                        logger.info("Setting CV 2 (vStart) to " + String.valueOf(targetStartSpeedKPH) + " KPH ( " + String.valueOf(Speed.kphToMph(targetStartSpeedKPH)) + " MPH)");
                        setThrottle(true, 1);
                        setSpeedMatchStateTimerDuration(8000);
                        stepDuration = 1;
                    } else {
                        setSpeedMatchError(targetStartSpeedKPH);

                        if (Math.abs(speedMatchError) < ALLOWED_SPEED_MATCH_ERROR) {
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
                        } else {
                            vStart = getNextSpeedMatchValue(lastVStart, vStartMax, VSTART_MIN);

                            if (((vStart == vStartMax) || (vStart == VSTART_MIN)) && (vStart == lastVStart)) {
                                statusLabel.setText(Bundle.getMessage("StatSetSpeedFail", "2 (vStart)"));
                                logger.info("Unable to achieve desired speed for CV 2 (vStart)");
                                Abort();
                                break;
                            }

                            lastVStart = vStart;
                            writeVStart(vStart);
                        }
                    }
                }
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
                                statusLabel.setText(Bundle.getMessage("StatSetReverseTripFail"));
                                logger.info("Unable to trim reverse to match forward");
                                Abort();
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
        setSpeedMatchStateTimerDuration(1800);
    }

    //</editor-fold>
}
