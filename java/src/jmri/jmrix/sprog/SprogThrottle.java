package jmri.jmrix.sprog;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to an SPROG connection.
 * <p>
 * Based on the {@link jmri.jmrix.nce.NceThrottle} implementation.
 * <p>
 * Updated by Andrew Crosland February 2012 to enable 28 step speed packets
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 */
public class SprogThrottle extends AbstractThrottle {

    /**
     * Constructor.
     */
    public SprogThrottle(SprogSystemConnectionMemo memo, DccLocoAddress address) {
        super(memo);
        station = memo.getCommandStation();

        // cache settings.
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

    }

    SprogCommandStation station = null;

    DccLocoAddress address;

    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    @Override
    protected void sendFunctionGroup1() {
        byte[] result = jmri.NmraPacket.function0Through4Packet(
                address.getNumber(), address.isLongAddress(),
                getF0(), getF1(), getF2(), getF3(), getF4());

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {

        byte[] result = jmri.NmraPacket.function5Through8Packet(
                address.getNumber(), address.isLongAddress(),
                getF5(), getF6(), getF7(), getF8());

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F9, F10, F11, F12.
     */
    @Override
    protected void sendFunctionGroup3() {

        byte[] result = jmri.NmraPacket.function9Through12Packet(
                address.getNumber(), address.isLongAddress(),
                getF9(), getF10(), getF11(), getF12());

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F13 F14, F15, F16.
     */
    @Override
    protected void sendFunctionGroup4() {

        byte[] result = jmri.NmraPacket.function13Through20Packet(
                address.getNumber(), address.isLongAddress(),
                getF13(), getF14(), getF15(), getF16(),
                getF17(), getF18(), getF19(), getF20());

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F17 F18, F19, F20.
     */
    @Override
    protected void sendFunctionGroup5() {

        byte[] result = jmri.NmraPacket.function21Through28Packet(
                address.getNumber(), address.isLongAddress(),
                getF21(), getF22(), getF23(), getF24(),
                getF25(), getF26(), getF27(), getF28());

        station.sendPacket(result, 1);
    }

    /**
     * Set the speed step value and the related
     * speedIncrement value.
     *
     * @param Mode  the current speed step mode - default should be 128 speed
     *             step mode in most cases
     */
    @Override
    public void setSpeedStepMode(SpeedStepMode Mode) {
        SprogMessage m;
        int mode = address.isLongAddress()
                ? SprogConstants.LONG_ADD : 0;
        try {
            mode |= (InstanceManager.getDefault(jmri.PowerManager.class).getPower() == SprogPowerManager.ON)
                    ? SprogConstants.POWER_BIT : 0;
        } catch (Exception e) {
            log.error("Exception from InstanceManager.getDefault(jmri.PowerManager.class): " + e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Speed Step Mode Change to Mode: " + Mode
                    + " Current mode is: " + this.speedStepMode);
        }
        if (Mode == SpeedStepMode.NMRA_DCC_14) {
            mode += 0x200;
        } else if (Mode == SpeedStepMode.NMRA_DCC_27) {
            log.error("Requested Speed Step Mode 27 not supported Current mode is: "
                    + this.speedStepMode);
            return;
        } else if (Mode == SpeedStepMode.NMRA_DCC_28) {
            mode += 0x400;
        } else { // default to 128 speed step mode
            mode += 0x800;
        }
        m = new SprogMessage("M h" + Integer.toHexString(mode));
        ((SprogSystemConnectionMemo)adapterMemo).getSprogTrafficController().sendSprogMessage(m, null);
        if ((speedStepMode != Mode) && (Mode != SpeedStepMode.NMRA_DCC_27)) {
            notifyPropertyChangeListener(SPEEDSTEPS, this.speedStepMode,
                    this.speedStepMode = Mode);
        }
    }

    /**
     * Set the speed {@literal &} direction.
     * <p>
     * This intentionally skips the emergency stop value of 1 in 128 step mode
     * and the stop and estop values 1-3 in 28 step mode.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @Override
    public void setSpeedSetting(float speed) {
        SpeedStepMode mode = getSpeedStepMode();
        if (mode == SpeedStepMode.NMRA_DCC_28) {
            // 28 step mode speed commands are 
            // stop, estop, stop, estop, 4, 5, ..., 31
            float oldSpeed = this.speedSetting;
            this.speedSetting = speed;
            int value = Math.round((31 - 3) * speed);     // -3 for rescale to avoid estopx2 and stop

            log.debug("Speed: " + speed + " value: " + value);

            if (value > 0) {
                value = value + 3;  // skip estopx2 and stop
            }
            if (value > 31) {
                value = 31;      // max possible speed
            }
            if (value < 0) {
                value = 0;        // emergency stop
            }
            String step = "" + value;

            SprogMessage m = new SprogMessage(1 + step.length());
            int i = 0;  // message index counter
            if (isForward) {
                m.setElement(i++, '>');
            } else {
                m.setElement(i++, '<');
            }

            for (int j = 0; j < step.length(); j++) {
                m.setElement(i++, step.charAt(j));
            }

            ((SprogSystemConnectionMemo)adapterMemo).getSprogTrafficController().sendSprogMessage(m, null);
            if (Math.abs(oldSpeed - this.speedSetting) > 0.0001) {
                notifyPropertyChangeListener(SPEEDSETTING, oldSpeed, this.speedSetting);
            }
        } else {
            // 128 step mode speed commands are
            // stop, estop, 2, 3, ..., 127
            float oldSpeed = this.speedSetting;
            this.speedSetting = speed;
            int value = Math.round((127 - 1) * speed);     // -1 for rescale to avoid estop
            if (value > 0) {
                value = value + 1;  // skip estop
            }
            if (value > 127) {
                value = 127;    // max possible speed
            }
            if (value < 0) {
                value = 1;        // emergency stop
            }
            String step = "" + value;

            SprogMessage m = new SprogMessage(1 + step.length());
            int i = 0;  // message index counter
            if (isForward) {
                m.setElement(i++, '>');
            } else {
                m.setElement(i++, '<');
            }

            for (int j = 0; j < step.length(); j++) {
                m.setElement(i++, step.charAt(j));
            }

            ((SprogSystemConnectionMemo)adapterMemo).getSprogTrafficController().sendSprogMessage(m, null);
            if (Math.abs(oldSpeed - this.speedSetting) > 0.0001) {
                notifyPropertyChangeListener(SPEEDSETTING, oldSpeed, this.speedSetting);
            }
        }
        record(speed);
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
        if (old != isForward) {
            notifyPropertyChangeListener(ISFORWARD, old, isForward);
        }
    }

    @Override
    protected void throttleDispose() {
        finishRecord();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SprogThrottle.class);

}
