package jmri.jmrix.bachrus.speedmatcher.basic;

import jmri.DccThrottle;
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcherConfig;

/**
 * This is a simple speed matcher which will speed match a locomotive to a given
 * start and top speed using the complex speed table. The speed for every speed
 * step between 1 and 28 will be interpolated linearly between the start and
 * high speed and each step in the table will be set accordingly.
 *
 * @author toddt
 */
public class BasicSpeedTableSpeedMatcher extends BasicSpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Constants">
    private final int INITIAL_STEP1_VALUE = 1;
    private final int INITIAL_STEP28_VALUE = 255;
    private final int INITIAL_TRIM_VALUE = 128;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    private SpeedTableStep currentSpeedTableStep = SpeedTableStep.STEP28;
    private int currentSpeedTableStepValue = INITIAL_STEP28_VALUE;
    private int lastSpeedTableStepValue = INITIAL_STEP28_VALUE;
    private int reverseTrimValue = INITIAL_TRIM_VALUE;
    private int lastReverseTrimValue = INITIAL_TRIM_VALUE;
    
    private final float targetCurrentSpeedStepSpeedKPH;

    private SpeedMatcherState speedMatcherState = SpeedMatcherState.IDLE;
    //</editor-fold>

    public BasicSpeedTableSpeedMatcher(SpeedMatcherConfig config) {
        super(config);
        
        this.targetCurrentSpeedStepSpeedKPH = this.targetTopSpeedKPH;
    }

    //<editor-fold defaultstate="collapsed" desc="SpeedMatcher Overrides">
    @Override
    public boolean StartSpeedMatch() {
        if (!super.Validate()) {
            return false;
        }
        
        //reset instance variables
        currentSpeedTableStep = SpeedTableStep.STEP28;
        currentSpeedTableStepValue = INITIAL_STEP28_VALUE;
        lastSpeedTableStepValue = INITIAL_STEP28_VALUE;
        reverseTrimValue = INITIAL_TRIM_VALUE;
        lastReverseTrimValue = INITIAL_TRIM_VALUE;

        speedMatcherState = SpeedMatcherState.WAIT_FOR_THROTTLE;
        
        if (!super.InitializeAndStartSpeedMatcher(e -> speedMatchTimeout())) {
            CleanUp();
            return false;
        }
        
        return true;
    }

    @Override
    public void StopSpeedMatch() {
        logger.info("Speed matching manually stopped");
        Abort();
    }

    @Override
    public boolean IsIdle() {
        return speedMatcherState == SpeedMatcherState.IDLE;
    }

    @Override
    public void CleanUp() {
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

            case INIT_ACCEL:
                //set acceleration momentum to 0 (CV 3)
                if (programmerState == ProgrammerState.IDLE) {
                    writeMomentumAccel(0);
                    setupNextSpeedMatchState(true, 0);
                }
                break;

            case INIT_DECEL:
                //set deceleration mementum to 0 (CV 4)
                if (programmerState == ProgrammerState.IDLE) {
                    writeMomentumDecel(0);
                    setupNextSpeedMatchState(true, 0);
                }
                break;

            case INIT_SPEED_TABLE_STEPS:
                //TODO: TRW - implementation
//                if (programmerState == programmerState.IDLE) {
//                    String speedTableStepValue = currentSpeedTableStep == SpeedTableStep.STEP28 ? INITIAL_STEP28 : INITIAL_SPEED_TABLE_STEP;
//                    writeSpeedTableStep(currentSpeedTableStep, speedTableStepValue);
//                    setupNextSpeedMatchState(true, 0);
//                }
                break;

            case INIT_FORWARD_TRIM:
                //set forward trim to 128 (CV 66)
                if (programmerState == ProgrammerState.IDLE) {
                    writeForwardTrim(INITIAL_TRIM_VALUE);
                    setupNextSpeedMatchState(true, 0);
                }
                break;

            case INIT_REVERSE_TRIM:
                //set reverse trim to 128 (CV 95)
                if (programmerState == ProgrammerState.IDLE) {
                    writeReverseTrim(INITIAL_TRIM_VALUE);
                    setupNextSpeedMatchState(true, 0);
                }
                break;

            case FORWARD_WARM_UP:
                //Run 4 minutes at high speed forward
                statusLabel.setText(Bundle.getMessage("StatForwardWarmUp", warmUpForwardSeconds - stepDuration));

                if (stepDuration == 0) {
                    setupSpeedMatchState(true, 28, 5000);
                } else if (stepDuration >= warmUpForwardSeconds) {
                    setupNextSpeedMatchState(true, 28);
                } else {
                    stepDuration += 5;
                }

                break;

            case FORWARD_SPEED_MATCH:
                //TODO: TRW - implementation
                break;

            case REVERSE_WARM_UP:
                //Run 2 minutes at high speed in reverse
                statusLabel.setText(Bundle.getMessage("StatReverseWarmUp", warmUpReverseSeconds - stepDuration));

                if (stepDuration == 0) {
                    setupSpeedMatchState(false, 28, 5000);
                } else if (stepDuration >= warmUpReverseSeconds) {
                    setupNextSpeedMatchState(false, 28);
                } else {
                    stepDuration += 5;
                }

                break;

            case REVERSE_SPEED_MATCH_TRIM:
                //Use PID controller logic to adjust reverse trim until high speed reverse speed matches forward
                if (programmerState == ProgrammerState.IDLE) {
                    if (stepDuration == 0) {
                        statusLabel.setText(Bundle.getMessage("StatSettingReverseTrim"));
                        setupSpeedMatchState(false, 28, 15000);
                        stepDuration = 1;
                    } else {
                        setSpeedMatchError(targetTopSpeedKPH);

                        if ((speedMatchError < 0.5) && (speedMatchError > -0.5)) {
                            setupNextSpeedMatchState(true, 0);
                        } else {
                            reverseTrimValue = getNextSpeedMatchValue(lastReverseTrimValue);

                            if (((lastReverseTrimValue == 1) || (lastReverseTrimValue == 255)) && (reverseTrimValue == lastReverseTrimValue)) {
                                statusLabel.setText(Bundle.getMessage("StatSetReverseTripFail"));
                                logger.debug("Unable to trim reverse to match forward");
                                Abort();
                                break;
                            } else {
                                lastReverseTrimValue = reverseTrimValue;
                                writeReverseTrim(reverseTrimValue);
                            }
                            speedMatchStateTimer.setInitialDelay(8000);
                        }
                    }
                }
                break;

            case CLEAN_UP:
                //wrap it up
                if (programmerState == ProgrammerState.IDLE) {
                    CleanUp();
                    statusLabel.setText(Bundle.getMessage("StatSpeedMatchComplete"));
                }
                break;

            default:
                CleanUp();
                logger.error("Unexpected speed match timeout");
                break;
        }
    }

    protected enum SpeedMatcherState {
        IDLE {
            @Override
            protected SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher) {
                return this;
            }
        },
        WAIT_FOR_THROTTLE {
            @Override
            protected SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher) {
                return SpeedMatcherState.IDLE;
            }
        },
        INIT_ACCEL {
            @Override
            protected SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher) {
                return SpeedMatcherState.INIT_DECEL;
            }
        },
        INIT_DECEL {
            @Override
            protected SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher) {
                return SpeedMatcherState.INIT_SPEED_TABLE_STEPS;
            }
        },
        INIT_SPEED_TABLE_STEPS {
            @Override
            protected SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher) {
                //TODO: TRW - Implementation
                //                if (programmerState == programmerState.IDLE) {
                //                    String speedTableStepValue = currentSpeedTableStep == SpeedTableStep.STEP28 ? INITIAL_STEP28 : INITIAL_SPEED_TABLE_STEP;
                //                    writeSpeedTableStep(currentSpeedTableStep, speedTableStepValue);
                //                    setupNextSpeedMatchState(true, 0);
                //                }
                {
                    return this;
                }
            }
        },
        INIT_FORWARD_TRIM {
            @Override
            protected SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher) {
                return SpeedMatcherState.INIT_REVERSE_TRIM;
            }
        },
        INIT_REVERSE_TRIM {
            @Override
            protected SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher) {
                if (speedMatcher.warmUpForwardSeconds > 0) {
                    return SpeedMatcherState.FORWARD_WARM_UP;
                } else {
                    return SpeedMatcherState.FORWARD_SPEED_MATCH;
                }
            }
        },
        FORWARD_WARM_UP {
            @Override
            protected SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher) {
                return SpeedMatcherState.FORWARD_SPEED_MATCH;
            }
        },
        FORWARD_SPEED_MATCH {
            @Override
            protected SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher) {
                //TODO: TRW - Implementation
//                if (currentSpeedTableStep == SpeedTableStep.STEP28) {
//                    currentSpeedTableStep = SpeedTableStep.STEP1;
//                    if (speedMatcher.trimReverseSpeed) {
//                        if (speedMatcher.warmUpReverseSeconds > 0) {
//                            return SpeedMatcherState.REVERSE_WARM_UP;
//                        } 
//                        else {
//                            return SpeedMatcherState.REVERSE_SPEED_MATCH_TRIM;
//                        }
//                    } else {
//                        return SpeedMatcherState.CLEAN_UP;
//                    }
//                }
                return this;
            }
        },
        REVERSE_WARM_UP {
            @Override
            protected SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher) {
                return SpeedMatcherState.REVERSE_SPEED_MATCH_TRIM;
            }
        },
        REVERSE_SPEED_MATCH_TRIM {
            @Override
            protected SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher) {
                return SpeedMatcherState.CLEAN_UP;
            }
        },
        CLEAN_UP {
            @Override
            protected SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher) {
                return SpeedMatcherState.IDLE;
            }
        };

        protected abstract SpeedMatcherState nextState(BasicSpeedTableSpeedMatcher speedMatcher);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Programmer">
    //<editor-fold defaultstate="collapsed" desc="ProgListener Overrides">
    /**
     * Starts writing a Speed Table Step CV (CV 67-94) using the ops mode
     * programmer
     *
     * @param step  the SpeedTableStep to set
     * @param value speed table step value (0-255 inclusive)
     */
    private synchronized void writeSpeedTableStep(SpeedTableStep step, int value) {
        programmerState = ProgrammerState.WRITE_SPEED_TABLE_STEP;
        statusLabel.setText(String.format("Setting Speed Table Step %s to %s", step.getName(), value));
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
            setupNextSpeedMatchState(true, 0);
            speedMatchStateTimer.start();
        } else {
            CleanUp();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Helper Functions">
    private void Abort() {
        speedMatcherState = SpeedMatcherState.CLEAN_UP;
        setupSpeedMatchState(true, 0, 1500);
    }

    private void setupNextSpeedMatchState(boolean isForward, int speedStep) {
        speedMatcherState = speedMatcherState.nextState(this);
        setupSpeedMatchState(isForward, speedStep, 1500);
    }
    //</editor-fold>
}
