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
public abstract class SpeedMatcher implements ThrottleListener, ProgListener{

    //<editor-fold defaultstate="collapsed" desc="Constants">
    //PID Coontroller Values
    protected final float kP = 0.75f;
    protected final float kI = 0.3f;
    protected final float kD = 0.4f;
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Enums">
    protected enum SpeedTableStep {
        STEP1("67", "1"),
        STEP2("68", "2"),
        STEP3("69", "3"),
        STEP4("70", "4"),
        STEP5("71", "5"),
        STEP6("72", "6"),
        STEP7("73", "7"),
        STEP8("74", "8"),
        STEP9("75", "9"),
        STEP10("76", "10"),
        STEP11("77", "11"),
        STEP12("78", "12"),
        STEP13("79", "13"),
        STEP14("80", "14"),
        STEP15("81", "15"),
        STEP16("82", "16"),
        STEP17("83", "17"),
        STEP18("84", "18"),
        STEP19("85", "19"),
        STEP20("86", "20"),
        STEP21("87", "21"),
        STEP22("88", "22"),
        STEP23("89", "23"),
        STEP24("90", "24"),
        STEP25("91", "25"),
        STEP26("92", "26"),
        STEP27("93", "27"),
        STEP28("94", "28");

        private final String cv;
        private final String name;

        private SpeedTableStep(String cv, String name) {
            this.cv = cv;
            this.name = name;
        }

        public String getCV() {
            return this.cv;
        }

        public String getName() {
            return this.name;
        }
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

    protected DccThrottle throttle = null;
    protected float throttleIncrement;
    protected AddressedProgrammer opsModeProgrammer = null;
    protected PowerManager powerManager = null;
    protected Timer speedMatchStateTimer;

    protected Logger logger;
    protected JLabel statusLabel;
    protected JButton startStopButton;
    
    protected ProgrammerState programmerState = ProgrammerState.IDLE;
    
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
    
    /**
     * Sets up the speed match state by setting the throttle direction and
     * speed, clearing the speed match error, clearing the step elapsed seconds,
     * and setting the timer initial delay
     *
     * @param isForward    - throttle direction - true for forward, false for
     *                     reverse
     * @param speedStep    - throttle speed step
     * @param initialDelay - initial delay for the timer in milliseconds
     */
    protected void setupSpeedMatchState(boolean isForward, int speedStep, int initialDelay) {
        throttle.setIsForward(isForward);
        throttle.setSpeedSetting(speedStep * throttleIncrement);
        speedMatchError = 0;
        stepDuration = 0;
        speedMatchStateTimer.setInitialDelay(initialDelay);
    }


    protected void stopSpeedMatchStateTimer() {
        if (speedMatchStateTimer != null) {
            speedMatchStateTimer.stop();
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
     * @return the next value to try for speed matching (1-255 inclusive)
     */
    protected int getNextSpeedMatchValue(int lastValue) {
        speedMatchIntegral += speedMatchError;
        speedMatchDerivative = speedMatchError - lastSpeedMatchError;

        int value = (lastValue + Math.round((kP * speedMatchError) + (kI * speedMatchIntegral) + (kD * speedMatchDerivative)));

        if (value > 255) {
            value = 255;
        } else if (value < 1) {
            value = 1;
        }

        return value;
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Programmer">
    /**
     * Starts writing acceleration momentum (CV 3) using the ops mode programmer
     * @param value acceleration value (0-255 inclusive)
     */ 
    protected synchronized void writeMomentumAccel(int value){
        programmerState = ProgrammerState.WRITE3;
        statusLabel.setText(Bundle.getMessage("ProgSetAccel", value));
        startOpsModeWrite("3", value);
    }

    /**
     * Starts writing deceleration momentum (CV 4) using the ops mode programmer
     * @param value deceleration value (0-255 inclusive)
     */
    protected synchronized void writeMomentumDecel(int value) {
        programmerState = ProgrammerState.WRITE4;
        statusLabel.setText(Bundle.getMessage("ProgSetDecel", value));
        startOpsModeWrite("4", value);
    }

    /**
     * Starts writing forward trim (CV 66) using the ops mode programmer
     * @param value forward trim value (0-255 inclusive)
     */
    protected synchronized void writeForwardTrim(int value) {
        programmerState = ProgrammerState.WRITE66;
        statusLabel.setText(Bundle.getMessage("ProgSetForwardTrim", value));
        startOpsModeWrite("66", value);
    }

    /**
     * Starts writing reverse trim (CV 95) using the ops mode programmer
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

    //<editor-fold defaultstate="collapsed" desc="ProgListener Overrides">
    /**
     * Called when the programmer (ops mode or service mode) has completed its
     * operation
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
     * Called when a throttle is found 
     * Must override, call super, and start speed matcher in implementation
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