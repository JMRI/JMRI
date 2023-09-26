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
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class SprogThrottle extends AbstractThrottle {

    /**
     * Constructor.
     * @param memo system connection.
     * @param address Loco address.
     */
    public SprogThrottle(SprogSystemConnectionMemo memo, DccLocoAddress address) {
        super(memo, SprogConstants.MAX_FUNCTIONS);
        station = memo.getCommandStation();

        // cache settings.
        synchronized (this) {
            this.speedSetting = 0;
        }
        // Functions default to false
        this.address = address;
        this.isForward = true;
        this.speedStepMode = SpeedStepMode.NMRA_DCC_128;

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
                getFunction(0), getFunction(1), getFunction(2), getFunction(3), getFunction(4));

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {

        byte[] result = jmri.NmraPacket.function5Through8Packet(
                address.getNumber(), address.isLongAddress(),
                getFunction(5), getFunction(6), getFunction(7), getFunction(8));

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F9, F10, F11, F12.
     */
    @Override
    protected void sendFunctionGroup3() {

        byte[] result = jmri.NmraPacket.function9Through12Packet(
                address.getNumber(), address.isLongAddress(),
                getFunction(9), getFunction(10), getFunction(11), getFunction(12));

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F13 - F20.
     */
    @Override
    protected void sendFunctionGroup4() {

        byte[] result = jmri.NmraPacket.function13Through20Packet(
                address.getNumber(), address.isLongAddress(),
                getFunction(13), getFunction(14), getFunction(15), getFunction(16),
                getFunction(17), getFunction(18), getFunction(19), getFunction(20));

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F21 - F28.
     */
    @Override
    protected void sendFunctionGroup5() {

        byte[] result = jmri.NmraPacket.function21Through28Packet(
                address.getNumber(), address.isLongAddress(),
                getFunction(21), getFunction(22), getFunction(23), getFunction(24),
                getFunction(25), getFunction(26), getFunction(27), getFunction(28));

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F29 - F36.
     */
    @Override
    protected void sendFunctionGroup6() {

        byte[] result = jmri.NmraPacket.function29Through36Packet(
                address.getNumber(), address.isLongAddress(),
                getFunctionNoWarn(29), getFunctionNoWarn(30), getFunctionNoWarn(31), getFunctionNoWarn(32),
                getFunctionNoWarn(33), getFunctionNoWarn(34), getFunctionNoWarn(35), getFunctionNoWarn(36)
        );

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F37 - F44.
     */
    @Override
    protected void sendFunctionGroup7() {

        byte[] result = jmri.NmraPacket.function37Through44Packet(
                address.getNumber(), address.isLongAddress(),
                getFunctionNoWarn(37), getFunctionNoWarn(38), getFunctionNoWarn(39), getFunctionNoWarn(40),
                getFunctionNoWarn(41), getFunctionNoWarn(42), getFunctionNoWarn(43), getFunctionNoWarn(44)
        );

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F45 - F52.
     */
    @Override
    protected void sendFunctionGroup8() {

        byte[] result = jmri.NmraPacket.function45Through52Packet(
                address.getNumber(), address.isLongAddress(),
                getFunctionNoWarn(45), getFunctionNoWarn(46), getFunctionNoWarn(47), getFunctionNoWarn(48),
                getFunctionNoWarn(49), getFunctionNoWarn(50), getFunctionNoWarn(51), getFunctionNoWarn(52)
        );

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F53 - F60.
     */
    @Override
    protected void sendFunctionGroup9() {

        byte[] result = jmri.NmraPacket.function53Through60Packet(
                address.getNumber(), address.isLongAddress(),
                getFunctionNoWarn(53), getFunctionNoWarn(54), getFunctionNoWarn(55), getFunctionNoWarn(56),
                getFunctionNoWarn(57), getFunctionNoWarn(58), getFunctionNoWarn(59), getFunctionNoWarn(60)
        );

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F61 - F68.
     */
    @Override
    protected void sendFunctionGroup10() {

        byte[] result = jmri.NmraPacket.function61Through68Packet(
                address.getNumber(), address.isLongAddress(),
                getFunctionNoWarn(61), getFunctionNoWarn(62), getFunctionNoWarn(63), getFunctionNoWarn(64),
                getFunctionNoWarn(65), getFunctionNoWarn(66), getFunctionNoWarn(67), getFunctionNoWarn(68)
        );

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
            log.error("Exception from InstanceManager.getDefault(jmri.PowerManager.class)", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Speed Step Mode Change to Mode: {} Current mode is: {}", Mode, this.speedStepMode);
        }
        if (Mode == SpeedStepMode.NMRA_DCC_14) {
            mode += 0x200;
        } else if (Mode == SpeedStepMode.NMRA_DCC_27) {
            log.error("Requested Speed Step Mode 27 not supported Current mode is: {}", this.speedStepMode);
            return;
        } else if (Mode == SpeedStepMode.NMRA_DCC_28) {
            mode += 0x400;
        } else { // default to 128 speed step mode
            mode += 0x800;
        }
        m = new SprogMessage("M h" + Integer.toHexString(mode));
        ((SprogSystemConnectionMemo)adapterMemo).getSprogTrafficController().sendSprogMessage(m, null);
        if (Mode != SpeedStepMode.NMRA_DCC_27) {
            firePropertyChange(SPEEDSTEPS, this.speedStepMode, this.speedStepMode = Mode);
        }
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
            int value = Math.round((31 - 3) * speed);     // -3 for rescale to avoid estopx2 and stop
            if (this.speedSetting > 0 && value == 0) {
                value = 1;          // ensure non-zero input results in non-zero output
            }

            log.debug("Speed: {} value: {}", speed, value);

            if (value > 0) {
                value = value + 3;  // skip estopx2 and stop
            }
            if (value > 31) {
                value = 31;      // max possible speed
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
            firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        } else {
            // 128 step mode speed commands are
            // stop, estop, 2, 3, ..., 127
            float oldSpeed = this.speedSetting;
            this.speedSetting = speed;
            int value = Math.round((127 - 1) * speed);     // -1 for rescale to avoid estop
            if (this.speedSetting > 0 && value == 0) {
                value = 1;          // ensure non-zero input results in non-zero output
            }
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
            firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        }
        record(speed);
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        synchronized (this) {
            setSpeedSetting(speedSetting);  // send the command
        }
        firePropertyChange(ISFORWARD, old, isForward);
    }

    @Override
    public void throttleDispose() {
        finishRecord();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SprogThrottle.class);

}
