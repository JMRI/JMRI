package jmri.jmrix.loconet;

import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A specialization of the LocoNet Throttle for Intellibox-I foibles.
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
public class Ib1Throttle extends LocoNetThrottle {

    /**
     * Constructor.
     *
     * @param memo system connection.
     * @param slot The LocoNetSlot this throttle will talk on.
     */
    public Ib1Throttle(LocoNetSystemConnectionMemo memo, LocoNetSlot slot) {
        super(memo, slot);
        log.debug("Ib1Throttle created");
    }

    /**
     * Convert a LocoNet speed integer to a float speed value
     *
     * @param lSpeed LocoNet style speed value
     * @return floatSpeed as float 0.0-1.0, or -1.0 to indicate E-Stop
     * The IB provides an integer 0-127
     */
     @Override
     protected float floatSpeed(int lSpeed) {
        log.debug("IB1 floatSpeed {}", lSpeed);
        if (lSpeed == 0) {
            return 0.f;    // stop
        } else if (lSpeed == 1) {
            return -1.f;   // estop
        }
        switch (this.getSpeedStepMode()) {
            case NMRA_DCC_28:
            case MOTOROLA_28:
                /*
                 * 28 speeds are not commensurate with the 126 speeds the IB1 puts on Loconet
                 * Loconet speeds are 2-6, 7-10, 11-15. 16-19 ... 124-127
                 * There are 5 Loconet speeds in first speed, 4 in the second,
                 * 5 in the third, 4 in the fourth, etc.
                 */
                lSpeed -= 1;   // skip estop
                int cycle = (lSpeed-1) / 9;
                int ispeed = (cycle*2) + 1;  // ispeed contains the speed in 1-28 steps
                if ((lSpeed - (cycle * 9)) > 5) {
                    ispeed++;  // add 1 if even step
                }
                return ispeed / 28.f;
           case NMRA_DCC_14:
                /*
                 * The implementation for 14 speed should be simple, but the IB1 did not
                 * implement equally spaced steps
                 */
                cycle = lSpeed / 19;
                ispeed = (cycle*2) + 1;
                if ((lSpeed - (cycle * 19)) > 9) {
                    ispeed++;
                }
                return ispeed / 14.f;  // 9/126 <= speed <= 1 (126 is divisible by 14)
            case NMRA_DCC_128:
                return (lSpeed-1) / 126.f;  // 1/126 <= speed <= 1
            default:
                log.warn("Unhandled speed step: {}", this.getSpeedStepMode());
                break;
        }
        return 0.f;
    }

    /**
     * Computes the integer speed value from a float.
     * @param speed is the floating-point speed value to be converted
     * @return intSpeed an integer which represents the speed step value
     */
    @Override
    protected int intSpeed(float speed) {
        log.debug("IB1 intSpeed {}", speed);

        int lSpeed = jmri.jmrix.AbstractThrottle.intSpeed(speed,127);
        switch (this.getSpeedStepMode()) {
            case NMRA_DCC_14:
                if (lSpeed > 2) {
                    lSpeed--;   // speed from JMRI throttle 1 higher than IB1
                }
                break;
            case NMRA_DCC_28:
            case MOTOROLA_28:
            case NMRA_DCC_128:
                /*
                 * The IB1 works appropriately for these speed step modes
                 */
                break;
            default:
                log.warn("Unhandled speed step: {}", this.getSpeedStepMode());
                break;
        }
        // log.debug("loconet speed is {}", lSpeed);
        return lSpeed;
    }

    @Override
    protected void sendFunctionGroup3() {
        // Special LocoNet messages for Uhlenbrock Intellibox-I version 2.x implementation
        // Intellibox-II uses another implementation for these functions
        // Functions F9 to F11
        log.debug("IB1 sendFunctionGroup3");
        int new_IB1_F9_F11 = ((getF11() ? LnConstants.RE_IB1_F11_MASK : 0)
                | (getF10() ? LnConstants.RE_IB1_F10_MASK : 0)
                | (getF9() ? LnConstants.RE_IB1_F9_MASK : 0));
        LocoNetMessage msg1 = new LocoNetMessage(6);
        msg1.setOpCode(LnConstants.RE_OPC_IB2_SPECIAL);
        msg1.setElement(1, LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN);
        msg1.setElement(2, slot.getSlot());
        msg1.setElement(3, LnConstants.RE_IB1_SPECIAL_F5_F11_TOKEN);
        msg1.setElement(4, new_IB1_F9_F11);
        network.sendLocoNetMessage(msg1);

        // Function F12 (and F20 and F28)
        int new_IB2_F20_F28 = ((getF12() ? LnConstants.RE_IB2_SPECIAL_F12_MASK : 0)
                | (getF20() ? LnConstants.RE_IB2_SPECIAL_F20_MASK : 0)
                | (getF28() ? LnConstants.RE_IB2_SPECIAL_F28_MASK : 0));
        LocoNetMessage msg2 = new LocoNetMessage(6);
        msg2.setOpCode(LnConstants.RE_OPC_IB2_SPECIAL);
        msg2.setElement(1, LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN);
        msg2.setElement(2, slot.getSlot());
        msg2.setElement(3, LnConstants.RE_IB2_SPECIAL_F20_F28_TOKEN);
        msg2.setElement(4, new_IB2_F20_F28);
        network.sendLocoNetMessage(msg2);
    }

    @Override
    protected void sendFunctionGroup4() {
        // Special LocoNet message for Uhlenbrock (IB-I and IB-II) implementation
        // Functions F13 to F19
        log.debug("IB1 sendFunctionGroup4");
        int new_IB2_F13_F19 = ((getF19() ? LnConstants.RE_IB2_F19_MASK : 0)
                | (getF18() ? LnConstants.RE_IB2_F18_MASK : 0)
                | (getF17() ? LnConstants.RE_IB2_F17_MASK : 0)
                | (getF16() ? LnConstants.RE_IB2_F16_MASK : 0)
                | (getF15() ? LnConstants.RE_IB2_F15_MASK : 0)
                | (getF14() ? LnConstants.RE_IB2_F14_MASK : 0)
                | (getF13() ? LnConstants.RE_IB2_F13_MASK : 0));
        LocoNetMessage msg = new LocoNetMessage(6);
        msg.setOpCode(LnConstants.RE_OPC_IB2_SPECIAL);
        msg.setElement(1, LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN);
        msg.setElement(2, slot.getSlot());
        msg.setElement(3, LnConstants.RE_IB2_SPECIAL_F13_F19_TOKEN);
        msg.setElement(4, new_IB2_F13_F19);
        network.sendLocoNetMessage(msg);

        // Function F20 (and F28)
        // F12 is also controlled from this message though IB-II uses RE_OPC_IB2_F9_F12 OPS code for F12 - needed to avoid overridding F12 value
        int new_IB2_F20_F28 = ((getF12() ? LnConstants.RE_IB2_SPECIAL_F12_MASK : 0)
                | (getF20() ? LnConstants.RE_IB2_SPECIAL_F20_MASK : 0)
                | (getF28() ? LnConstants.RE_IB2_SPECIAL_F28_MASK : 0));
        LocoNetMessage msg2 = new LocoNetMessage(6);
        msg2.setOpCode(LnConstants.RE_OPC_IB2_SPECIAL);
        msg2.setElement(1, LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN);
        msg2.setElement(2, slot.getSlot());
        msg2.setElement(3, LnConstants.RE_IB2_SPECIAL_F20_F28_TOKEN);
        msg2.setElement(4, new_IB2_F20_F28);
        network.sendLocoNetMessage(msg2);
    }

    @Override
    protected void sendFunctionGroup5() {
        // Special LocoNet message for Uhlenbrock (IB-I and IB-II) implementation
        // Functions F21 to F27
        log.debug("IB1 sendFunctionGroup5");
        int new_IB2_F21_F27 = ((getF27() ? LnConstants.RE_IB2_F27_MASK : 0)
                | (getF26() ? LnConstants.RE_IB2_F26_MASK : 0)
                | (getF25() ? LnConstants.RE_IB2_F25_MASK : 0)
                | (getF24() ? LnConstants.RE_IB2_F24_MASK : 0)
                | (getF23() ? LnConstants.RE_IB2_F23_MASK : 0)
                | (getF22() ? LnConstants.RE_IB2_F22_MASK : 0)
                | (getF21() ? LnConstants.RE_IB2_F21_MASK : 0));
        LocoNetMessage msg = new LocoNetMessage(6);
        msg.setOpCode(LnConstants.RE_OPC_IB2_SPECIAL);
        msg.setElement(1, LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN);
        msg.setElement(2, slot.getSlot());
        msg.setElement(3, LnConstants.RE_IB2_SPECIAL_F21_F27_TOKEN);
        msg.setElement(4, new_IB2_F21_F27);
        network.sendLocoNetMessage(msg);

        // Function F28 (and F20)
        // F12 is also controlled from this message though IB-II uses RE_OPC_IB2_F9_F12 OPS code for F12 - needed to avoid overridding F12 value
        int new_IB2_F20_F28 = ((getF12() ? LnConstants.RE_IB2_SPECIAL_F12_MASK : 0)
                | (getF20() ? LnConstants.RE_IB2_SPECIAL_F20_MASK : 0)
                | (getF28() ? LnConstants.RE_IB2_SPECIAL_F28_MASK : 0));
        LocoNetMessage msg2 = new LocoNetMessage(6);
        msg2.setOpCode(LnConstants.RE_OPC_IB2_SPECIAL);
        msg2.setElement(1, LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN);
        msg2.setElement(2, slot.getSlot());
        msg2.setElement(3, LnConstants.RE_IB2_SPECIAL_F20_F28_TOKEN);
        msg2.setElement(4, new_IB2_F20_F28);
        network.sendLocoNetMessage(msg2);
    }

    /**
     * Update functions F0 to F8 from the slot
     * Invoked by notifyChangedSlot(). The special LocoNet
     * messages generated here don't (yet) update the slot,
     * leaving all the function bits off.  We therefore don't
     * update those from the slot during message processing.
     */
    @Override
    protected void updateFunctions() {
        for (int i = 0; i <= 8; i++) {
            log.debug("updateFunction({}, {})", i, slot.isFunction(i));
            updateFunction(i,slot.isFunction(i));
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(Ib1Throttle.class);

}
