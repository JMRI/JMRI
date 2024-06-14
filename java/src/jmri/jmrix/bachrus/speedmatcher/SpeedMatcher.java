package jmri.jmrix.bachrus.speedmatcher;

import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.Timer;

import jmri.*;

import org.slf4j.Logger;

/**
 *
 * @author toddt
 */
public abstract class SpeedMatcher implements ThrottleListener, ProgListener {

    //<editor-fold defaultstate="collapsed" desc="Constants">
    //PID Controller Values
    //TODO: TRW - tune to reduce overshoot
    protected final float kP = 0.75f;
    protected final float kI = 0.3f;
    protected final float kD = 0.0f; //0.4f;    
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Enums">
    protected enum SpeedTableStep {
        STEP1(1) {
            @Override
            public SpeedTableStep getPrevious() {
                return null;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP2;
            }
        },
        STEP2(2) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP1;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP3;
            }
        },
        STEP3(3) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP2;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP4;
            }
        },
        STEP4(4) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP3;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP5;
            }
        },
        STEP5(5) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP4;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP6;
            }
        },
        STEP6(6) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP5;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP7;
            }
        },
        STEP7(7) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP6;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP8;
            }
        },
        STEP8(8) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP7;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP9;
            }
        },
        STEP9(9) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP8;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP10;
            }
        },
        STEP10(10) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP9;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP11;
            }
        },
        STEP11(11) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP10;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP12;
            }
        },
        STEP12(12) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP11;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP13;
            }
        },
        STEP13(13) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP12;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP14;
            }
        },
        STEP14(14) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP13;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP15;
            }
        },
        STEP15(15) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP14;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP16;
            }
        },
        STEP16(16) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP15;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP17;
            }
        },
        STEP17(17) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP16;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP18;
            }
        },
        STEP18(18) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP17;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP19;
            }
        },
        STEP19(19) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP18;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP20;
            }
        },
        STEP20(20) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP19;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP21;
            }
        },
        STEP21(21) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP20;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP22;
            }
        },
        STEP22(22) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP21;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP23;
            }
        },
        STEP23(23) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP22;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP24;
            }
        },
        STEP24(24) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP23;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP25;
            }
        },
        STEP25(25) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP24;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP26;
            }
        },
        STEP26(26) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP25;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP27;
            }
        },
        STEP27(27) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP26;
            }
            @Override
            public SpeedTableStep getNext() {
                return STEP28;
            }
        },
        STEP28(28) {
            @Override
            public SpeedTableStep getPrevious() {
                return STEP27;
            }
            @Override
            public SpeedTableStep getNext() {
                return null;
            }
        };

        private final int speedStep;
        private final String cv;

        private SpeedTableStep(int speedStep) {
            this.speedStep = speedStep;
            this.cv = String.valueOf(speedStep + 28);
        }
        
        public int getSpeedStep() {
            return this.speedStep;
        }
        
        public String getCV() {
            return this.cv;
        }
        
        public abstract SpeedTableStep getNext();
        public abstract SpeedTableStep getPrevious();
        
        public int get128StepEquivalent() {
            return Math.round(this.speedStep * (long)4.571428571428571);
        }
    }

    protected enum ProgrammerState {
        IDLE,
        WRITE2,
        WRITE3,
        WRITE4,
        WRITE5,
        WRITE6,
        WRITE66,
        WRITE95,
        WRITE_SPEED_TABLE_STEP,
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    protected float speedMatchIntegral = 0;
    protected float speedMatchDerivative = 0;
    protected float lastSpeedMatchError = 0;
    protected float speedMatchError = 0;

    protected boolean trimReverseSpeed;

    protected int warmUpForwardSeconds = 240;
    protected int warmUpReverseSeconds = 120;

    protected int stepDuration = 0;
    protected float currentSpeed = 0;

    protected DccLocoAddress dccLocoAddress;

    protected AddressedProgrammer opsModeProgrammer = null;
    protected PowerManager powerManager = null;

    protected Logger logger;
    protected JLabel statusLabel;
    protected JButton startStopButton;

    protected ProgrammerState programmerState = ProgrammerState.IDLE;

    private DccThrottle throttle = null;
    private float throttleIncrement;

    private Timer speedMatchStateTimer;

    //</editor-fold>
    
    public SpeedMatcher(SpeedMatcherConfig config) {
        this.dccLocoAddress = config.dccLocoAddress;
        this.powerManager = config.powerManager;

        this.trimReverseSpeed = config.trimReverseSpeed;

        this.warmUpForwardSeconds = config.warmUpForwardSeconds;
        this.warmUpReverseSeconds = config.warmUpReverseSeconds;

        this.logger = config.logger;
        this.statusLabel = config.statusLabel;
        this.startStopButton = config.startStopButton;
    }

    //<editor-fold defaultstate="collapsed" desc="Public APIs">   
    public abstract boolean Start();

    public abstract void Stop();

    public abstract boolean IsIdle();

    public void UpdateCurrentSpeed(float currentSpeedKPH) {
        this.currentSpeed = currentSpeedKPH;
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Protected APIs">
    protected abstract boolean Validate();

    protected void CleanUp() {
        //stop the timer
        if (speedMatchStateTimer != null) {
            speedMatchStateTimer.stop();
        }

        //release throttle
        if (throttle != null) {
            throttle.setSpeedSetting(0.0F);
            InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
            throttle = null;
        }

        //release ops mode programmer
        if (opsModeProgrammer != null) {
            InstanceManager.getDefault(AddressedProgrammerManager.class).releaseAddressedProgrammer(opsModeProgrammer);
            opsModeProgrammer = null;
        }

        startStopButton.setText(Bundle.getMessage("btnStartSpeedMatch"));
    }

    protected boolean InitializeAndStartSpeedMatcher(ActionListener timerActionListener) {
        //Setup speed match timer
        speedMatchStateTimer = new javax.swing.Timer(4000, timerActionListener);
        speedMatchStateTimer.setRepeats(false); //timer is used without repeats to improve time accuracy when changing the delay

        if (!GetOpsModeProgrammer()) {
            return false;
        }

        return GetThrottle();
    }

    protected void startSpeedMatchStateTimer() {
        if (speedMatchStateTimer != null) {
            speedMatchStateTimer.start();
        }
    }

    protected void stopSpeedMatchStateTimer() {
        if (speedMatchStateTimer != null) {
            speedMatchStateTimer.stop();
        }
    }

    protected void setSpeedMatchStateTimerDuration(int timerDuration) {
        speedMatchStateTimer.setInitialDelay(timerDuration);
    }

    /**
     * Sets the speed matcher's throttle direction and speed safely within
     * timers to protect against executing a throttle change to close to setting
     * a CV
     *
     * @param isForward true for forward, false for revers
     * @param speedStep 0-28 or 0-128 depending on mode
     */
    protected void setThrottle(boolean isForward, int speedStep) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        throttle.setIsForward(isForward);
        throttle.setSpeedSetting(speedStep * throttleIncrement);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sets the PID controller's speed match error for speed matching
     *
     * @param speedTarget - target speed in KPH
     */
    protected void setSpeedMatchError(float speedTarget) {
        speedMatchError = speedTarget - currentSpeed;
    }

    /**
     * Gets the next value to try for speed matching using a PID controller
     *
     * @param lastValue - the last speed match CV value tried
     * @param max       - the maximum value
     * @param min       - the minimum value
     * @return the next value to try for speed matching [min:max]
     */
    protected int getNextSpeedMatchValue(int lastValue, int max, int min) {
        speedMatchIntegral += speedMatchError;
        speedMatchDerivative = speedMatchError - lastSpeedMatchError;

        int value = (lastValue + Math.round((kP * speedMatchError) + (kI * speedMatchIntegral) + (kD * speedMatchDerivative)));

        if (value > max) {
            value = max;
        } else if (value < min) {
            value = min;
        }

        return value;
    }
    
    /**
     * Resets the PID controller's speed match error, integral, and derivative
     */
    protected void resetSpeedMatchError() {
        speedMatchIntegral = 0;
        speedMatchDerivative = 0;
        lastSpeedMatchError = 0;
        speedMatchError = 0;
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Programmer">
    /**
     * Starts writing acceleration momentum (CV 3) using the ops mode programmer
     *
     * @param value acceleration value (0-255 inclusive)
     */
    protected synchronized void writeMomentumAccel(int value) {
        programmerState = ProgrammerState.WRITE3;
        statusLabel.setText(Bundle.getMessage("ProgSetAccel", value));
        startOpsModeWrite("3", value);
    }

    /**
     * Starts writing deceleration momentum (CV 4) using the ops mode programmer
     *
     * @param value deceleration value (0-255 inclusive)
     */
    protected synchronized void writeMomentumDecel(int value) {
        programmerState = ProgrammerState.WRITE4;
        statusLabel.setText(Bundle.getMessage("ProgSetDecel", value));
        startOpsModeWrite("4", value);
    }

    /**
     * Starts writing forward trim (CV 66) using the ops mode programmer
     *
     * @param value forward trim value (0-255 inclusive)
     */
    protected synchronized void writeForwardTrim(int value) {
        programmerState = ProgrammerState.WRITE66;
        statusLabel.setText(Bundle.getMessage("ProgSetForwardTrim", value));
        startOpsModeWrite("66", value);
    }

    /**
     * Starts writing reverse trim (CV 95) using the ops mode programmer
     *
     * @param value reverse trim value (0-255 inclusive)
     */
    protected synchronized void writeReverseTrim(int value) {
        programmerState = ProgrammerState.WRITE95;
        statusLabel.setText(Bundle.getMessage("ProgSetReverseTrim", value));
        startOpsModeWrite("95", value);
    }

    protected void startOpsModeWrite(String cv, int value) {
        try {
            opsModeProgrammer.writeCV(cv, value, this);
        } catch (ProgrammerException e) {
            logger.error("Exception writing CV " + cv + " " + e);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="ProgListener Overrides">
    /**
     * Called when the programmer (ops mode or service mode) has completed its
     * operation
     *
     * @param value  Value from a read operation, or value written on a write
     * @param status Denotes the completion code. Note that this is a bitwise
     *               combination of the various states codes defined in this
     *               interface. (see ProgListener.java for possible values)
     */
    @Override
    public void programmingOpReply(int value, int status) {
        if (status == 0) { //OK
            switch (programmerState) {
                case IDLE:
                    logger.debug("unexpected reply in IDLE state");
                    break;

                case WRITE2:
                case WRITE3:
                case WRITE4:
                case WRITE5:
                case WRITE6:
                case WRITE66:
                case WRITE95:
                case WRITE_SPEED_TABLE_STEP:
                    programmerState = ProgrammerState.IDLE;
                    break;

                default:
                    programmerState = ProgrammerState.IDLE;
                    logger.warn("Unhandled programmer state: {}", programmerState);
                    break;
            }
        } else {
            // Error during programming
            logger.error("Status not OK during " + programmerState.toString() + ": " + status);
            //profileAddressField.setText("Error");
            statusLabel.setText("Error using programmer");
            programmerState = ProgrammerState.IDLE;
            CleanUp();
        }
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Helper Functions">
    private boolean GetOpsModeProgrammer() {
        logger.info("Requesting Programmer");
        //get OPS MODE Programmer
        if (InstanceManager.getNullableDefault(AddressedProgrammerManager.class) != null) {
            if (InstanceManager.getDefault(AddressedProgrammerManager.class).isAddressedModePossible(dccLocoAddress)) {
                opsModeProgrammer = InstanceManager.getDefault(AddressedProgrammerManager.class).getAddressedProgrammer(dccLocoAddress);
            }
        }

        if (opsModeProgrammer != null) {
            return true;
        } else {
            logger.error("Programmer request failed.");
            statusLabel.setText("Programmer request failed");
            return false;
        }
    }

    private boolean GetThrottle() {
        statusLabel.setText("Requesting Throttle");
        logger.info("Requesting Throttle");
        speedMatchStateTimer.start();
        boolean throttleRequestOK = InstanceManager.throttleManagerInstance().requestThrottle(dccLocoAddress, this, true);
        if (!throttleRequestOK) {
            logger.error("Loco Address in use, throttle request failed.");
            statusLabel.setText("Loco Address in use, throttle request failed");
        }
        return throttleRequestOK;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="ThrottleListener Overrides">
    /**
     * Called when a throttle is found Must override, call super, and start
     * speed matcher in implementation
     *
     * @param t the requested DccThrottle
     */
    @Override
    public void notifyThrottleFound(DccThrottle t) {
        stopSpeedMatchStateTimer();

        throttle = t;
        logger.info("Throttle acquired");
        throttle.setSpeedStepMode(SpeedStepMode.NMRA_DCC_28);
        if (throttle.getSpeedStepMode() != SpeedStepMode.NMRA_DCC_28) {
            logger.error("Failed to set 28 step mode");
            statusLabel.setText(Bundle.getMessage("ThrottleError28"));
            InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
            return;
        }

        // turn on power
        try {
            powerManager.setPower(PowerManager.ON);
        } catch (JmriException e) {
            logger.error("Exception during power on: " + e.toString());
            return;
        }

        throttleIncrement = throttle.getSpeedIncrement();
    }

    /**
     * Called when we must decide whether to steal the throttle for the
     * requested address. This is an automatically stealing implementation, so
     * the throttle will be automatically stolen
     *
     * @param address  the requested address
     * @param question the question being asked, steal / cancel, share / cancel,
     *                 steal / share / cancel
     */
    @Override
    public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
        InstanceManager.throttleManagerInstance().responseThrottleDecision(address, this, DecisionType.STEAL);
    }

    /**
     * Called when a throttle could not be obtained
     *
     * @param address the requested address
     * @param reason  the reason the throttle could not be obtained
     */
    @Override
    public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
    }
    //</editor-fold>
}
