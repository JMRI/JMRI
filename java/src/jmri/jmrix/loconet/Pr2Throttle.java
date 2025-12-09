package jmri.jmrix.loconet;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle via AbstractThrottle with code specific to a
 * PR2 connection.
 * <p>
 * Speed in the Throttle interfaces and AbstractThrottle is a float, but in
 * LocoNet is an int with values from 0 to 127.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class Pr2Throttle extends AbstractThrottle {

    private final int addr;
    DccLocoAddress address;

    /**
     * Constructor
     * @param memo a LocoNetSystemConnectionMemo to associate with this throttle
     * @param address a DccLocoAddress to associate with this throttle
     */
    public Pr2Throttle(LocoNetSystemConnectionMemo memo, DccLocoAddress address) {
        super(memo);
        this.address = address;
        addr = address.getNumber();
        setSpeedStepMode(SpeedStepMode.NMRA_DCC_28);
    }

    /**
     * Convert a LocoNet speed integer to a float speed value.
     *
     * @param lSpeed loconet speed value
     * @return speed as float 0-&gt;1.0
     */
    protected float floatSpeed(int lSpeed) {
        if (lSpeed == 0) {
            return 0.f;
        } else if (lSpeed == 1) {
            return -1.f;   // estop
        }
        if (getSpeedStepMode() == SpeedStepMode.NMRA_DCC_28) {
            if (lSpeed <= 15) //Value less than 15 is in the stop/estop range bracket
            {
                return 0.f;
            }
            return (((lSpeed - 12) / 4f) / 28.f);
        } else if (getSpeedStepMode() == SpeedStepMode.NMRA_DCC_14) {
            if (lSpeed <= 15) //Value less than 15 is in the stop/estop range bracket
            {
                return 0.f;
            }
            return ((lSpeed - 8) / 8f) / 14.f;
        } else {
            return ((lSpeed - 1) / 126.f);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does not support 128 speed steps.
     */
    @Override
    // This is a specific implementation for the PR2 that seems to
    // return different values from the super class.  This is an attempt at only
    // outputting those speeds to the Pr2 that generate discrete DCC speeds
    // 14/28 speed steps are the same as LocoNetThrottle.intSpeed
    protected int intSpeed(float fSpeed) {
        int speed;
        if (fSpeed < 0.) {
            return 1;
        }  // emergency stop
        if (fSpeed == 0.) {
            return 0;
        } // idle (zero speed)
        
        switch (this.getSpeedStepMode()) {
            case NMRA_DCC_28:
            case MOTOROLA_28:
                speed = (int) ((fSpeed * 28) * 4) + 12;
                // ensure we never send a non-zero speed to loconet 
                // that we reinterpret as 0 in floatSpeed() later
                if (speed < 16) {
                    speed = 16;
                }
                return speed;

            case NMRA_DCC_14:
                speed = (int) ((fSpeed * 14) * 8) + 8;
                // ensure we never send a non-zero speed to loconet 
                // that we reinterpret as 0 in floatSpeed() later
                if (speed < 16) {
                    speed = 16;
                }
                return speed;

            default:
                // includes the 128 case
                log.warn("Unhandled speed step mode: {}", this.getSpeedStepMode());
                return super.intSpeed(fSpeed);
        }
    }

    public void writeData() {
        // convert contents
        int stat = 0;
        int speed;
        synchronized(this) {
            speed = intSpeed(speedSetting);
        }
        int dirf = 0; // contains dir, f0, f4-1
        if (getFunction(0)) {
            dirf |= (1 << 4);
        }
        if (getFunction(1)) {
            dirf |= (1 << 0);
        }
        if (getFunction(2)) {
            dirf |= (1 << 1);
        }
        if (getFunction(3)) {
            dirf |= (1 << 2);
        }
        if (getFunction(4)) {
            dirf |= (1 << 3);
        }
        if (!getIsForward()) {
            dirf |= (1 << 5);  // note sign of bit
        }
        // contains f11-5
        int f11 = 0;
        if (getFunction(5)) {
            f11 |= (1 << 0);
        }
        if (getFunction(6)) {
            f11 |= (1 << 1);
        }
        if (getFunction(7)) {
            f11 |= (1 << 2);
        }
        if (getFunction(8)) {
            f11 |= (1 << 3);
        }
        if (getFunction(9)) {
            f11 |= (1 << 4);
        }
        if (getFunction(10)) {
            f11 |= (1 << 5);
        }
        if (getFunction(11)) {
            f11 |= (1 << 6);
        }

        // contains F19-F13
        int f19 = 0;

        // contains F27-F21
        int f27 = 0;

        // contains F28, F20, F12
        int f28 = 0;
        if (getFunction(12)) {
            f28 |= (1 << 4);
        }

        LocoNetMessage l = new LocoNetMessage(21);
        l.setOpCode(LnConstants.OPC_EXP_WR_SL_DATA);
        int i = 1;
        l.setElement(i++, 21);      // length
        l.setElement(i++, 0);       // EXP_MAST
        l.setElement(i++, 1);       // EXP_SLOT
        l.setElement(i++, stat & 0x7F);     // EXPD_STAT
        l.setElement(i++, addr & 0x7F);     // EXPD_ADRL
        l.setElement(i++, (addr / 128) & 0x7F); // EXPD_ADRH
        l.setElement(i++, 0);               // EXPD_FLAGS
        l.setElement(i++, speed & 0x7F);    // EXPD_SPD
        l.setElement(i++, f28 & 0x7F);      // EXPD_F28F20F12
        l.setElement(i++, dirf & 0x7F);     // EXPD_DIR_F0F4_F1
        l.setElement(i++, f11 & 0x7F);      // EXPD_F11_F5
        l.setElement(i++, f19 & 0x7F);      // EXPD_F19_F13
        l.setElement(i++, f27 & 0x7F);      // EXPD_F27_F21
        // rest are zero

        ((LocoNetSystemConnectionMemo) adapterMemo).getLnTrafficController().sendLocoNetMessage(l);
    }

    /**
     * Send the LocoNet message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4. Invoked by AbstractThrottle when needed.
     */
    @Override
    protected void sendFunctionGroup1() {
        writeData();
    }

    /**
     * Send the LocoNet message to set the state of functions F5, F6, F7, F8.
     * Invoked by AbstractThrottle when needed.
     */
    @Override
    protected void sendFunctionGroup2() {
        writeData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendFunctionGroup3() {
        writeData();
    }

    /**
     * Set the speed.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @Override
    public synchronized void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        if (speed < 0) {
            this.speedSetting = -1.f;
        }

        writeData();
        firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting); // NOI18N
        record(speed);
    }

    /**
     * LocoNet actually puts forward and backward in the same message as the
     * first function group.
     */
    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        sendFunctionGroup1();
        firePropertyChange(ISFORWARD, old, isForward); // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getLocoAddress().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void throttleDispose() {
        finishRecord();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(Pr2Throttle.class);

}
