package jmri.jmrix.marklin;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.Throttle;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to an TAMS connection.
 * <p>
 * Based on Glen Oberhauser's original LnThrottle implementation
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class MarklinThrottle extends AbstractThrottle implements MarklinListener {

    /**
     * Constructor.
     */
    public MarklinThrottle(MarklinSystemConnectionMemo memo, LocoAddress address) {
        super(memo);
        tc = memo.getTrafficController();

        this.speedSetting = 0;
        this.f0 = false;
        this.f1 = false;
        this.f2 = false;
        this.f3 = false;
        this.f4 = false;
        this.f5 = false;
        this.f6 = false;
        this.f7 = false;
        this.f8 = false;
        this.f9 = false;
        this.f10 = false;
        this.f11 = false;
        this.f12 = false;
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

        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 0, (f0 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 1, (f1 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 2, (f2 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 3, (f3 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 4, (f4 ? 0x01 : 0x00)), this);

    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {

        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 5, (f5 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 6, (f6 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 7, (f7 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 8, (f8 ? 0x01 : 0x00)), this);

    }

    @Override
    protected void sendFunctionGroup3() {

        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 9, (f9 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 10, (f10 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 11, (f11 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 12, (f12 ? 0x01 : 0x00)), this);

    }

    @Override
    protected void sendFunctionGroup4() {

        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 13, (f13 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 14, (f14 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 15, (f15 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 16, (f16 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 17, (f17 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 18, (f18 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 19, (f19 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 20, (f20 ? 0x01 : 0x00)), this);

    }

    @Override
    protected void sendFunctionGroup5() {

        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 21, (f21 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 22, (f22 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 23, (f23 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 24, (f24 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 25, (f25 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 26, (f26 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 27, (f27 ? 0x01 : 0x00)), this);
        tc.sendMarklinMessage(MarklinMessage.setLocoFunction(getCANAddress(), 28, (f28 ? 0x01 : 0x00)), this);

    }

    /**
     * Set the speed {@literal &} direction.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;

        int value = (int) ((1000) * this.speedSetting);
        if (value > 1000) {
            value = 1000;    // max possible speed
        }
        if (value < 0) {
            //Emergency Stop
            tc.sendMarklinMessage(MarklinMessage.setLocoEmergencyStop(getCANAddress()), this);
        } else {
            tc.sendMarklinMessage(MarklinMessage.setLocoSpeed(getCANAddress(), value), this);
        }
        if (log.isDebugEnabled()) {
            log.debug("Float speed = " + speed + " Int speed = " + value);
        }
        if (oldSpeed != this.speedSetting) {
            notifyPropertyChangeListener(SPEEDSETTING, oldSpeed, this.speedSetting);
        }
    }

    /**
     * Convert a Marklin speed integer to a float speed value
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
        if (old != isForward) {
            notifyPropertyChangeListener(ISFORWARD, old, isForward);
        }
    }

    private LocoAddress address;

    MarklinTrafficController tc;

    @Override
    public void setSpeedStepMode(SpeedStepMode Mode) {
        if (log.isDebugEnabled()) {
            log.debug("Speed Step Mode Change to Mode: " + Mode
                    + " Current mode is: " + this.speedStepMode);
        }
        boolean isLong = ((jmri.ThrottleManager) adapterMemo.get(jmri.ThrottleManager.class)).canBeLongAddress(address.getNumber());
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
    protected void throttleDispose() {
        active = false;
    }

    @Override
    public void message(MarklinMessage m) {
        // messages are ignored
    }

    @Override
    public void reply(MarklinReply m) {
        if (m.getPriority() == MarklinConstants.PRIO_1 && m.getCommand() >= MarklinConstants.MANCOMMANDSTART && m.getCommand() <= MarklinConstants.MANCOMMANDEND) {
            if (m.getAddress() != getCANAddress()) {
                if (log.isDebugEnabled()) {
                    log.debug("Addressed packet is not for us " + m.getAddress() + " " + getCANAddress());
                }
                return;
            }
            if (m.getCommand() == MarklinConstants.LOCODIRECTION) {
                if (log.isDebugEnabled()) {
                    log.debug("Loco Direction " + m.getElement(9));
                }
                //The CS2 sets the speed of the loco to Zero when changing direction, however it doesn't appear to broadcast it out.
                switch (m.getElement(9)) {
                    case 0x00:
                        return; //No change
                    case 0x01:
                        if (!isForward) {
                            speedSetting = 0.0f;
                            super.setSpeedSetting(speedSetting);
                            notifyPropertyChangeListener(ISFORWARD, isForward, true);
                            isForward = true;
                        }
                        return;
                    case 0x02:
                        if (isForward) {
                            speedSetting = 0.0f;
                            super.setSpeedSetting(speedSetting);
                            notifyPropertyChangeListener(ISFORWARD, isForward, false);
                            isForward = false;
                        }
                        return;
                    case 0x03:
                        speedSetting = 0.0f;
                        super.setSpeedSetting(speedSetting);
                        notifyPropertyChangeListener(ISFORWARD, isForward, !isForward);
                        isForward = !isForward;
                        return;
                    default:
                        log.error("No Match Found for loco direction " + m.getElement(9));
                        return;
                }
            }
            if (m.getCommand() == MarklinConstants.LOCOSPEED) {
                int speed = m.getElement(9);
                speed = (speed << 8) + (m.getElement(10));
                Float newSpeed = Float.valueOf(floatSpeed(speed));
                if (log.isDebugEnabled()) {
                    log.debug("Speed raw " + speed + " float " + newSpeed);
                }
                super.setSpeedSetting(newSpeed);
            }
            if (m.getCommand() == MarklinConstants.LOCOFUNCTION) {
                int function = m.getElement(9);
                int state = m.getElement(10);
                boolean functionresult = true;
                if (state == 0) {
                    functionresult = false;
                }
                switch (function) {
                    case 0:
                        if (this.f0 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F0, this.f0, functionresult);
                            this.f0 = functionresult;
                        }
                        break;
                    case 1:
                        if (this.f1 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F1, this.f1, functionresult);
                            this.f1 = functionresult;
                        }
                        break;
                    case 2:
                        if (this.f2 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F2, this.f2, functionresult);
                            this.f2 = functionresult;
                        }
                        break;
                    case 3:
                        if (this.f3 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F3, this.f3, functionresult);
                            this.f3 = functionresult;
                        }
                        break;
                    case 4:
                        if (this.f4 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F4, this.f4, functionresult);
                            this.f4 = functionresult;
                        }
                        break;
                    case 5:
                        if (this.f5 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F5, this.f5, functionresult);
                            this.f5 = functionresult;
                        }
                        break;
                    case 6:
                        if (this.f6 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F6, this.f6, functionresult);
                            this.f6 = functionresult;
                        }
                        break;
                    case 7:
                        if (this.f7 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F7, this.f7, functionresult);
                            this.f7 = functionresult;
                        }
                        break;
                    case 8:
                        if (this.f8 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F8, this.f8, functionresult);
                            this.f8 = functionresult;
                        }
                        break;
                    case 9:
                        if (this.f9 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F9, this.f9, functionresult);
                            this.f9 = functionresult;
                        }
                        break;
                    case 10:
                        if (this.f10 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F10, this.f10, functionresult);
                            this.f10 = functionresult;
                        }
                        break;
                    case 11:
                        if (this.f11 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F11, this.f11, functionresult);
                            this.f11 = functionresult;
                        }
                        break;
                    case 12:
                        if (this.f12 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F12, this.f12, functionresult);
                            this.f12 = functionresult;
                        }
                        break;
                    case 13:
                        if (this.f13 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F13, this.f13, functionresult);
                            this.f13 = functionresult;
                        }
                        break;
                    case 14:
                        if (this.f14 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F14, this.f14, functionresult);
                            this.f14 = functionresult;
                        }
                        break;
                    case 15:
                        if (this.f15 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F15, this.f15, functionresult);
                            this.f15 = functionresult;
                        }
                        break;
                    case 16:
                        if (this.f16 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F16, this.f16, functionresult);
                            this.f16 = functionresult;
                        }
                        break;
                    case 17:
                        if (this.f17 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F17, this.f17, functionresult);
                            this.f17 = functionresult;
                        }
                        break;
                    case 18:
                        if (this.f18 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F18, this.f18, functionresult);
                            this.f18 = functionresult;
                        }
                        break;
                    case 19:
                        if (this.f19 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F19, this.f19, functionresult);
                            this.f19 = functionresult;
                        }
                        break;
                    case 20:
                        if (this.f20 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F20, this.f20, functionresult);
                            this.f20 = functionresult;
                        }
                        break;
                    case 21:
                        if (this.f21 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F21, this.f21, functionresult);
                            this.f21 = functionresult;
                        }
                        break;
                    case 22:
                        if (this.f22 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F22, this.f22, functionresult);
                            this.f22 = functionresult;
                        }
                        break;
                    case 23:
                        if (this.f23 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F23, this.f23, functionresult);
                            this.f23 = functionresult;
                        }
                        break;
                    case 24:
                        if (this.f24 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F24, this.f24, functionresult);
                            this.f24 = functionresult;
                        }
                        break;
                    case 25:
                        if (this.f25 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F25, this.f25, functionresult);
                            this.f25 = functionresult;
                        }
                        break;
                    case 26:
                        if (this.f26 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F26, this.f26, functionresult);
                            this.f26 = functionresult;
                        }
                        break;
                    case 27:
                        if (this.f27 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F27, this.f27, functionresult);
                            this.f27 = functionresult;
                        }
                        break;
                    case 28:
                        if (this.f28 != functionresult) {
                            notifyPropertyChangeListener(Throttle.F28, this.f28, functionresult);
                            this.f28 = functionresult;
                        }
                        break;
                    default:
                        break;
                }
                return;
            }
        }
    }

    int getCANAddress() {
        switch (address.getProtocol()) {
            case DCC:
                return MarklinConstants.DCCSTART + address.getNumber();
            case MOTOROLA:
                return address.getNumber();
            case SELECTRIX:
                return MarklinConstants.SX2START + address.getNumber();
            case MFX:
                return MarklinConstants.MFXSTART + address.getNumber();
            default:
                return MarklinConstants.DCCSTART + address.getNumber();
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(MarklinThrottle.class);

}
