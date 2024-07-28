package jmri.jmrix.bachrus.speedmatcher;

import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.Timer;

import jmri.*;

/**
 * Abstract class defining the basic operations of a speed matcher. All speed
 * matcher implementations must extend this class.
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public abstract class SpeedMatcher implements ThrottleListener, ProgListener {

    //<editor-fold defaultstate="collapsed" desc="Constants">
    //PID Controller Values
    protected final float Kp = 0.275f;
    protected final float Ti = 240;
    protected final float Td = 5;
    protected final float Ki = Kp / Ti;
    protected final float Kd = Kp * Td;

    //Other Constants
    protected final int INITIAL_MOMENTUM = 0;
    protected final int REVERSE_TRIM_MAX = 255;
    protected final int REVERSE_TRIM_MIN = 1;

    protected final float ALLOWED_SPEED_MATCH_ERROR = 0.75f;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Enums">
    public enum SpeedTableStep {
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
            this.cv = String.valueOf(speedStep + 66);
        }

        /**
         * Gets the speed step as an int
         *
         * @return int speed step
         */
        public int getSpeedStep() {
            return this.speedStep;
        }

        /**
         * Gets the string CV of the SpeedTableStep
         *
         * @return string CV
         */
        public String getCV() {
            return this.cv;
        }

        /**
         * Gets the next SpeedTableStep
         *
         * @return next SpeedTableStep
         */
        public abstract SpeedTableStep getNext();

        /**
         * Gets the previous SpeedTableStep
         *
         * @return previous SpeedTableStep
         */
        public abstract SpeedTableStep getPrevious();
    }

    protected enum SpeedMatcherCV {
        VSTART(2, Bundle.getMessage("CVVStart")),
        VMID(6, Bundle.getMessage("CVVMid")),
        VHIGH(5, Bundle.getMessage("CVVHigh")),
        ACCEL(3, Bundle.getMessage("CVAccel")),
        DECEL(4, Bundle.getMessage("CVDecel")),
        FORWARDTRIM(66, Bundle.getMessage("CVFwdTrim")),
        REVERSETRIM(95, Bundle.getMessage("CVReverseTrim"));

        private final String name;
        private final String cv;

        private SpeedMatcherCV(int cv, String name) {
            this.cv = String.valueOf(cv);
            this.name = name;
        }

        /**
         * Gets the string CV value for the SpeedMatcherCV
         *
         * @return string CV value
         */
        public String getCV() {
            return this.cv;
        }

        /**
         * Gets the string name of the SpeedMatcherCV
         *
         * @return string name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Gets the string display name of the SpeedMatcherCV
         *
         * @return string display name
         */
        public String getCVDisplayName() {
            return Bundle.getMessage("CVDisplayName", cv, name);
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
    protected float currentSpeedKPH = 0;

    protected DccLocoAddress dccLocoAddress;

    protected AddressedProgrammer opsModeProgrammer = null;
    protected PowerManager powerManager = null;

    protected JLabel statusLabel;
    protected JButton startStopButton;

    protected ProgrammerState programmerState = ProgrammerState.IDLE;

    private DccThrottle throttle = null;
    private float throttleIncrement;

    private Timer speedMatchStateTimer;
    //</editor-fold>

    /**
     * Constructor for the abstract SpeedMatcher at the core of any Speed
     * Matcher
     *
     * @param config SpeedMatcherConfig for initializing the SpeedMatcher
     */
    public SpeedMatcher(SpeedMatcherConfig config) {
        this.dccLocoAddress = config.dccLocoAddress;
        this.powerManager = config.powerManager;

        this.trimReverseSpeed = config.trimReverseSpeed;

        this.warmUpForwardSeconds = config.warmUpForwardSeconds;
        this.warmUpReverseSeconds = config.warmUpReverseSeconds;

        this.statusLabel = config.statusLabel;
        this.startStopButton = config.startStopButton;
    }

    //<editor-fold defaultstate="collapsed" desc="Public APIs">   
    /**
     * Starts the speed matching process
     *
     * @return true if speed matching started successfully, false otherwise
     */
    public abstract boolean startSpeedMatcher();

    /**
     * Stops the speed matching process
     */
    public abstract void stopSpeedMatcher();

    /**
     * Indicates if the speed matcher is idle (not currently speed matching)
     *
     * @return true if idle, false otherwise
     */
    public abstract boolean isSpeedMatcherIdle();

    /**
     * Updates the locomotive's current speed in the speed matcher
     *
     * @param currentSpeedKPH the locomotive's current speed in KPH
     */
    public void updateCurrentSpeed(float currentSpeedKPH) {
        this.currentSpeedKPH = currentSpeedKPH;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Protected APIs">
    /**
     * Validates the speed matcher's configuration
     *
     * @return true if the configuration is valid, false otherwise
     */
    protected abstract boolean validate();

    /**
     * Cleans up the speed matcher when speed matching is stopped or is finished
     */
    protected void cleanUpSpeedMatcher() {
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

        startStopButton.setText(Bundle.getMessage("SpeedMatchStartBtn"));
    }

    /**
     * Shared code to initialize the speed matcher's programmer and throttle and
     * start the speed matching timer. Expected to be called in an implementing
     * speed matcher's Start function.
     *
     * @param timerActionListener callback to fire when the timer times out
     * @return true if initialization and start is successful, false otherwise
     */
    protected boolean initializeAndStartSpeedMatcher(ActionListener timerActionListener) {
        //Setup speed match timer
        speedMatchStateTimer = new javax.swing.Timer(4000, timerActionListener);
        speedMatchStateTimer.setRepeats(false); //timer is used without repeats to improve time accuracy when changing the delay

        if (!getOpsModeProgrammer()) {
            return false;
        }

        statusLabel.setText(Bundle.getMessage("StatRequestingThrottle"));
        logger.info("Requesting Throttle");
        speedMatchStateTimer.start();
        boolean throttleRequestOK = InstanceManager.throttleManagerInstance().requestThrottle(dccLocoAddress, this, true);
        if (!throttleRequestOK) {
            logger.error("Loco Address in use, throttle request failed.");
            statusLabel.setText(Bundle.getMessage("StatThrottleReqFailed"));
        }
        return throttleRequestOK;
    }

    /**
     * Starts the speed match state timer
     */
    protected void startSpeedMatchStateTimer() {
        if (speedMatchStateTimer != null) {
            speedMatchStateTimer.start();
        }
    }

    /**
     * Stops the speed match state timer
     */
    protected void stopSpeedMatchStateTimer() {
        if (speedMatchStateTimer != null) {
            speedMatchStateTimer.stop();
        }
    }

    /**
     * Sets the duration for the speed match timer
     *
     * @param timerDuration timer duration in milliseconds
     */
    protected void setSpeedMatchStateTimerDuration(int timerDuration) {
        if (speedMatchStateTimer != null) {
            speedMatchStateTimer.setInitialDelay(timerDuration);
        }
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

        logger.info("Set throttle to {} speed step {}", isForward ? "forward" : "reverse", speedStep);

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
        speedMatchError = speedTarget - currentSpeedKPH;
    }

    /**
     * Gets the next value to try for speed matching using a PID controller
     *
     * @param lastValue the last speed match CV value tried
     * @param max       the maximum value
     * @param min       the minimum value
     * @return the next value to try for speed matching [min:max]
     */
    protected int getNextSpeedMatchValue(int lastValue, int max, int min) {
        speedMatchIntegral += speedMatchError;
        speedMatchDerivative = speedMatchError - lastSpeedMatchError;

        int value = (lastValue + Math.round((Kp * speedMatchError) + (Ki * speedMatchIntegral) + (Kd * speedMatchDerivative)));

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
     * Starts writing vStart (CV 2) using the ops mode programmer
     *
     * @param value vStart value (0-255 inclusive)
     */
    protected synchronized void writeVStart(int value) {
        programmerState = ProgrammerState.WRITE2;
        writeCV(SpeedMatcherCV.VSTART, value);
    }

    /**
     * Starts writing vMid (CV 6) using the ops mode programmer
     *
     * @param value vMid value (0-255 inclusive)
     */
    protected synchronized void writeVMid(int value) {
        programmerState = ProgrammerState.WRITE6;
        writeCV(SpeedMatcherCV.VMID, value);
    }

    /**
     * Starts writing vHigh (CV 5) using the ops mode programmer
     *
     * @param value vHigh value (0-255 inclusive)
     */
    protected synchronized void writeVHigh(int value) {
        programmerState = ProgrammerState.WRITE5;
        writeCV(SpeedMatcherCV.VHIGH, value);
    }

    /**
     * Starts writing acceleration momentum (CV 3) using the ops mode programmer
     *
     * @param value acceleration value (0-255 inclusive)
     */
    protected synchronized void writeMomentumAccel(int value) {
        programmerState = ProgrammerState.WRITE3;
        writeCV(SpeedMatcherCV.ACCEL, value);
    }

    /**
     * Starts writing deceleration momentum (CV 4) using the ops mode programmer
     *
     * @param value deceleration value (0-255 inclusive)
     */
    protected synchronized void writeMomentumDecel(int value) {
        programmerState = ProgrammerState.WRITE4;
        writeCV(SpeedMatcherCV.DECEL, value);
    }

    /**
     * Starts writing forward trim (CV 66) using the ops mode programmer
     *
     * @param value forward trim value (0-255 inclusive)
     */
    protected synchronized void writeForwardTrim(int value) {
        programmerState = ProgrammerState.WRITE66;
        writeCV(SpeedMatcherCV.FORWARDTRIM, value);
    }

    /**
     * Starts writing reverse trim (CV 95) using the ops mode programmer
     *
     * @param value reverse trim value (0-255 inclusive)
     */
    protected synchronized void writeReverseTrim(int value) {
        programmerState = ProgrammerState.WRITE95;
        writeCV(SpeedMatcherCV.REVERSETRIM, value);
    }

    /**
     * Starts writing a Speed Table Step CV (CV 67-94) using the ops mode
     * programmer
     *
     * @param step  the SpeedTableStep to set
     * @param value speed table step value (0-255 inclusive)
     */
    protected synchronized void writeSpeedTableStep(SpeedTableStep step, int value) {
        programmerState = ProgrammerState.WRITE_SPEED_TABLE_STEP;
        statusLabel.setText(Bundle.getMessage("ProgSetCV", step.getCV() + " (Speed Step " + String.valueOf(step.getSpeedStep()) + ")", value));
        startOpsModeWrite(step.getCV(), value);
    }

    /**
     * Starts writing a CV using the ops mode programmer and sets the status
     * label
     *
     * @param cv    CV to write to
     * @param value value to write (0-255 inclusive)
     */
    private synchronized void writeCV(SpeedMatcherCV cv, int value) {
        statusLabel.setText(Bundle.getMessage("ProgSetCV", cv.getCVDisplayName(), value));
        startOpsModeWrite(cv.getCV(), value);
    }

    /**
     * Starts writing a CV using the ops mode programmer
     *
     * @param cv    string CV to write to
     * @param value value to write (0-255 inclusive)
     */
    private void startOpsModeWrite(String cv, int value) {
        try {
            logger.info("Setting CV {} to {}", cv, value);
            opsModeProgrammer.writeCV(cv, value, this);
        } catch (ProgrammerException e) {
            logger.error("Exception writing CV {} {}", cv, e.toString());
        }
    }

    //<editor-fold defaultstate="collapsed" desc="ProgListener Overrides">
    /**
     * Called when the programmer has completed its operation
     *
     * @param value  value from a read operation, or value written on a write
     * @param status denotes the completion code. Note that this is a bitwise
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
            logger.error("Status not OK during {}: {}", programmerState.toString(), status);
            statusLabel.setText("Error using programmer");
            programmerState = ProgrammerState.IDLE;
            cleanUpSpeedMatcher();
        }
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Helper Functions">
    /**
     * Acquires an ops mode programmer for use in the speed matcher
     *
     * @return true if the ops mode programmer was successfully acquired, false
     *         otherwise
     */
    private boolean getOpsModeProgrammer() {
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
            statusLabel.setText(Bundle.getMessage("StatProgrammerReqFailed"));
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="ThrottleListener Overrides">
    /**
     * Called when a throttle is found Implementers must override, call super,
     * and start speed matcher in implementation
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
            logger.error("Exception during power on: {}", e.toString());
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

    //debugging logger
    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SpeedMatcher.class);
}
