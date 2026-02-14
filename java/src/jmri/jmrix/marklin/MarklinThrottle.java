package jmri.jmrix.marklin;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;
import jmri.jmrix.marklin.mcan.address.AddressSpace;
import jmri.jmrix.marklin.mcan.address.AddressTranslator;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.cs2.Cs2DecoderCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to an TAMS connection.
 * <p>
 * Based on Glen Oberhauser's original LnThrottle implementation
 * <p>
 * Now leverages the mcan sub-package for message decoding.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class MarklinThrottle extends AbstractThrottle implements MarklinListener {

    /**
     * Constructor.
     * @param memo system connection.
     * @param address loco address.
     */
    public MarklinThrottle(MarklinSystemConnectionMemo memo, LocoAddress address) {
        super(memo);
        tc = memo.getTrafficController();

        synchronized(this) {
            this.speedSetting = 0;
        }
        // Functions default to false
        this.address = address;
        this.isForward = true;

        setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_128);
        tc.addMarklinListener(this);
        tc.sendMarklinMessage(MarklinMessage.getQryLocoSpeed(getCANAddress()), this);
        tc.sendMarklinMessage(MarklinMessage.getQryLocoDirection(getCANAddress()), this);
        for (int i = 0; i <= 28; i++) {
            tc.sendMarklinMessage(MarklinMessage.getQryLocoFunction(getCANAddress(), i), this);
        }
    }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4. To
     * send function group 1 we have to also send speed, direction etc.
     */
    @Override
    protected void sendFunctionGroup1() {

        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 0, (getFunction(0) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 1, (getFunction(1) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 2, (getFunction(2) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 3, (getFunction(3) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 4, (getFunction(4) ? 0x01 : 0x00)), this);

    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {

        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 5, (getFunction(5) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 6, (getFunction(6) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 7, (getFunction(7) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 8, (getFunction(8) ? 0x01 : 0x00)), this);

    }

    @Override
    protected void sendFunctionGroup3() {

        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 9, (getFunction(9) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 10, (getFunction(10) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 11, (getFunction(11) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 12, (getFunction(12) ? 0x01 : 0x00)), this);

    }

    @Override
    protected void sendFunctionGroup4() {

        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 13, (getFunction(13) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 14, (getFunction(14) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 15, (getFunction(15) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 16, (getFunction(16) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 17, (getFunction(17) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 18, (getFunction(18) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 19, (getFunction(19) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 20, (getFunction(20) ? 0x01 : 0x00)), this);

    }

    @Override
    protected void sendFunctionGroup5() {

        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 21, (getFunction(21) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 22, (getFunction(22) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 23, (getFunction(23) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 24, (getFunction(24) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 25, (getFunction(25) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 26, (getFunction(26) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 27, (getFunction(27) ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 28, (getFunction(28) ? 0x01 : 0x00)), this);

    }

    /**
     * Set the speed and direction.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public synchronized void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;

        int value = (int) ((1000) * this.speedSetting);
        if (value > 1000) {
            value = 1000;    // max possible speed
        }
        
        if (this.speedSetting > 0 && value == 0) {
            value = 1;      // ensure non-zero input results in non-zero output
        }
        
        if (value < 0) {
            //Emergency Stop
            tc.sendMarklinMessage(MarklinMessage.setLocoEmergencyStop(getCANAddress()), this);
        } else {
            tc.sendMarklinMessage(MarklinMessage.setLocoSpeed(getCANAddress(), value), this);
        }
        log.debug("Float speed = {} Int speed = {}", speed, value);
        firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        record(speed);
    }

    /**
     * Convert a Marklin speed integer to a float speed value
     * @param lSpeed Marklin-format speed value
     * @return 0.0 - 1.0 speed value
     */
    protected float floatSpeed(int lSpeed) {
        if (lSpeed == 0) {
            return 0.f;
        }
        return ((lSpeed) / 1000.f);
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        setSpeedSetting(0.0f); //Stop the loco first before changing direction.
        tc.sendMarklinMessage(MarklinMessage.setLocoDirection(getCANAddress(), (forward ? 0x01 : 0x02)), this);
        firePropertyChange(ISFORWARD, old, isForward);
    }

    private LocoAddress address;

    MarklinTrafficController tc;

    @Override
    public void setSpeedStepMode(SpeedStepMode Mode) {
        if (log.isDebugEnabled()) {
            log.debug("Speed Step Mode Change to Mode: {} Current mode is: {}", Mode, this.speedStepMode);
        }
        boolean isLong = adapterMemo.get(jmri.ThrottleManager.class).canBeLongAddress(address.getNumber());
        switch (address.getProtocol()) {
            case DCC:
                if (Mode == SpeedStepMode.NMRA_DCC_28 && isLong) {
                    tc.sendMarklinMessage(MarklinMessage.setLocoSpeedSteps(getCANAddress(), MarklinConstants.STEPLONG28), this);
                } else if (Mode == SpeedStepMode.NMRA_DCC_28 && !isLong) {
                    tc.sendMarklinMessage(MarklinMessage.setLocoSpeedSteps(getCANAddress(), MarklinConstants.STEPSHORT28), this);
                } else if (Mode == SpeedStepMode.NMRA_DCC_128 && isLong) {
                    tc.sendMarklinMessage(MarklinMessage.setLocoSpeedSteps(getCANAddress(), MarklinConstants.STEPLONG128), this);
                } else if (Mode == SpeedStepMode.NMRA_DCC_128 && !isLong) {
                    tc.sendMarklinMessage(MarklinMessage.setLocoSpeedSteps(getCANAddress(), MarklinConstants.STEPSHORT128), this);
                }
                break;
            default:
                Mode = SpeedStepMode.NMRA_DCC_28;
                break;
        }
        super.setSpeedStepMode(Mode);
    }

    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    @Override
    public void throttleDispose() {
        active = false;
         finishRecord();
    }

    @Override
    public void message(MarklinMessage m) {
        // messages are ignored
    }

    @Override
    public void reply(MarklinReply m) {
        Cs2ClassifiedFrame frame = MarklinCanBridge.parse(m);
        if (frame == null) {
            return;
        }

        if (frame instanceof Cs2DecoderCommands.LocoDirectionReplyFrame) {
            Cs2DecoderCommands.LocoDirectionReplyFrame f = (Cs2DecoderCommands.LocoDirectionReplyFrame) frame;
            if (f.locoAddress() != getCANAddress()) {
                if (log.isDebugEnabled()) {
                    log.debug("Addressed packet is not for us {} {}", f.locoAddress(), getCANAddress());
                }
                return;
            }
            log.debug("Loco Direction {}", f.direction());
            synchronized(this) {
                switch (f.direction()) {
                    case 0x00:
                        return;
                    case 0x01:
                        if (!isForward) {
                            speedSetting = 0.0f;
                            super.setSpeedSetting(speedSetting);
                            isForward = true;
                            firePropertyChange(ISFORWARD, false, isForward);
                        }
                        return;
                    case 0x02:
                        if (isForward) {
                            speedSetting = 0.0f;
                            super.setSpeedSetting(speedSetting);
                            isForward = false;
                            firePropertyChange(ISFORWARD, true, isForward);
                        }
                        return;
                    case 0x03:
                        speedSetting = 0.0f;
                        super.setSpeedSetting(speedSetting);
                        isForward = !isForward;
                        firePropertyChange(ISFORWARD, !isForward, isForward);
                        return;
                    default:
                        log.error("No Match Found for loco direction {}", f.direction());
                        return;
                }
            }
        }

        if (frame instanceof Cs2DecoderCommands.LocoSpeedReplyFrame) {
            Cs2DecoderCommands.LocoSpeedReplyFrame f = (Cs2DecoderCommands.LocoSpeedReplyFrame) frame;
            if (f.locoAddress() != getCANAddress()) {
                return;
            }
            float newSpeed = floatSpeed(f.speed());
            log.debug("Speed raw {} float {}", f.speed(), newSpeed);
            super.setSpeedSetting(newSpeed);
        }

        if (frame instanceof Cs2DecoderCommands.LocoFunctionReplyFrame) {
            Cs2DecoderCommands.LocoFunctionReplyFrame f = (Cs2DecoderCommands.LocoFunctionReplyFrame) frame;
            if (f.locoAddress() != getCANAddress()) {
                return;
            }
            updateFunction(f.function(), f.active());
        }
    }

    int getCANAddress() {
        AddressSpace space;
        switch (address.getProtocol()) {
            case DCC:       space = AddressSpace.DCC_LOCO;  break;
            case SELECTRIX: space = AddressSpace.SX2_LOCO;  break;
            case MFX:       space = AddressSpace.MFX_LOCO;  break;
            case MOTOROLA:  space = AddressSpace.MM_LOCO;   break;
            default:        space = AddressSpace.DCC_LOCO;  break;
        }
        return AddressTranslator.locoToCanAddress(space, address.getNumber());
    }

    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(MarklinThrottle.class);

}
