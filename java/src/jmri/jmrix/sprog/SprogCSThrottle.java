package jmri.jmrix.sprog;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to a SPROG Command
 * Station connection.
 * <p>
 * Updated by Andrew Crosland February 2012 to enable 28 step speed packets
 *
 * @author Andrew Crosland Copyright (C) 2006, 2012
 */
public class SprogCSThrottle extends AbstractThrottle {

    /**
     * Constructor.
     * @param memo system connection.
     * @param address Loco Address.
     */
    public SprogCSThrottle(SprogSystemConnectionMemo memo, LocoAddress address) {
        super(memo);
        
        if (address instanceof DccLocoAddress) {
            this.address = ((DccLocoAddress) address);
        }
        else {
            log.error("{} is not a DccLocoAddress",address);
        }

        // cache settings.
        synchronized(this) {
            this.speedSetting = 0;
        }
        // Functions default to false
        this.isForward = true;
        this.speedStepMode = SpeedStepMode.NMRA_DCC_128;
        

        //@TODO - this needs a little work. Current implementation looks like it
        //should support other modes, but doesn't in practice.  
        //@see AbstractThrottleManager.supportedSpeedModes()
        // Find our command station
        if ((memo != null) && (memo.get(jmri.CommandStation.class) != null)) {
            commandStation = memo.get(jmri.CommandStation.class);
        } else {
            commandStation = (SprogCommandStation) jmri.InstanceManager.getNullableDefault(jmri.CommandStation.class);
        }

    }

    private final SprogCommandStation commandStation;

    DccLocoAddress address;

    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4 by
     * adding it to the S queue
     */
    @Override
    protected void sendFunctionGroup1() {
        commandStation.function0Through4Packet(address,
                getF0(), getF0Momentary(),
                getF1(), getF1Momentary(),
                getF2(), getF2Momentary(),
                getF3(), getF3Momentary(),
                getF4(), getF4Momentary());

    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8 by# adding
     * it to the S queue
     */
    @Override
    protected void sendFunctionGroup2() {
        commandStation.function5Through8Packet(address,
                getF5(), getF5Momentary(),
                getF6(), getF6Momentary(),
                getF7(), getF7Momentary(),
                getF8(), getF8Momentary());
    }

    /**
     * Send the message to set the state of functions F9, F10, F11, F12 by
     * adding it to the S queue
     */
    @Override
    protected void sendFunctionGroup3() {
        commandStation.function9Through12Packet(address,
                getF9(), getF9Momentary(),
                getF10(), getF10Momentary(),
                getF11(), getF11Momentary(),
                getF12(), getF12Momentary());
    }

    /**
     * Send the message to set the state of functions F13 - F20
     * adding it to the S queue
     */
    @Override
    protected void sendFunctionGroup4() {
        commandStation.function13Through20Packet(address,
                getF13(), getF13Momentary(),
                getF14(), getF14Momentary(),
                getF15(), getF15Momentary(),
                getF16(), getF16Momentary(),
                getF17(), getF17Momentary(),
                getF18(), getF18Momentary(),
                getF19(), getF19Momentary(),
                getF20(), getF20Momentary());
    }

    /**
     * Send the message to set the state of functions F21 - F28
     * adding it to the S queue
     */
    @Override
    protected void sendFunctionGroup5() {
        commandStation.function21Through28Packet(address,
                getF21(), getF21Momentary(),
                getF22(), getF22Momentary(),
                getF23(), getF23Momentary(),
                getF24(), getF24Momentary(),
                getF25(), getF25Momentary(),
                getF26(), getF26Momentary(),
                getF27(), getF27Momentary(),
                getF28(), getF28Momentary());
    }

    /**
     * Set the speed and direction.
     * <p>
     * This intentionally skips the emergency stop value of 1 in 128 step mode
     * and the stop and estop values 1-3 in 28 step mode.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @Override
    public synchronized void setSpeedSetting(float speed) {
        SpeedStepMode mode = getSpeedStepMode();
        if (mode == SpeedStepMode.NMRA_DCC_28) {
            // 28 step mode speed commands are 
            // stop, estop, stop, estop, 4, 5, ..., 31
            float oldSpeed = this.speedSetting;
            this.speedSetting = speed;
            int value = (int) ((31 - 3) * speed);     // -1 for rescale to avoid estop
            if (value > 0) {
                value = value + 3;  // skip estopx2 and stop
            }
            if (value > 31) {
                value = 31;      // max possible speed
            }
            if (value < 0) {
                value = 1;        // emergency stop
            }
            commandStation.setSpeed(SpeedStepMode.NMRA_DCC_28, address, value, isForward);
            firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        } else {
            // 128 step mode speed commands are
            // stop, estop, 2, 3, ..., 127
            float oldSpeed = this.speedSetting;
            this.speedSetting = speed;
            int value = (int) ((127 - 1) * speed);     // -1 for rescale to avoid estop
            if (value > 0) {
                value = value + 1;  // skip estop
            }
            if (value > 127) {
                value = 127;    // max possible speed
            }
            if (value < 0) {
                value = 1;        // emergency stop
            }
            commandStation.setSpeed(SpeedStepMode.NMRA_DCC_128, address, value, isForward);
            firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        }
        record(speed);
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        synchronized(this) {
            setSpeedSetting(speedSetting);  // Update the speed setting
        }
        firePropertyChange(ISFORWARD, old, isForward);
    }

    @Override
    public void throttleDispose() {
        active = false;
        commandStation.release(address);
        finishRecord();
    }

    private final static Logger log = LoggerFactory.getLogger(SprogCSThrottle.class);

}
