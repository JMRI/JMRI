package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A specialization of the LocoNet Throttle for Intellibox-II foibles.
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
public class Ib2Throttle extends LocoNetThrottle {

    /**
     * Constructor.
     *
     * @param memo system connection.
     * @param slot The LocoNetSlot this throttle will talk on.
     */
    public Ib2Throttle(LocoNetSystemConnectionMemo memo, LocoNetSlot slot) {
        super(memo, slot);
        log.debug("Ib2Throttle created");
    }

    @Override
    protected void sendFunctionGroup3() {
        // Special LocoNet message for Uhlenbrock Intellibox-II implementation
        // Intellibox-I uses another implementation for these functions
        // Functions F9 to F12
        log.debug("IB1 sendFunctionGroup3");
        int new_IB2_F9_F12 = ((getFunction(12) ? LnConstants.RE_IB2_F12_MASK : 0)
                | (getFunction(11) ? LnConstants.RE_IB2_F11_MASK : 0)
                | (getFunction(10) ? LnConstants.RE_IB2_F10_MASK : 0)
                | (getFunction(9) ? LnConstants.RE_IB2_F9_MASK : 0));
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.RE_OPC_IB2_F9_F12);
        msg.setElement(1, slot.getSlot());
        msg.setElement(2, new_IB2_F9_F12);
        network.sendLocoNetMessage(msg);
    }

    @Override
    protected void sendFunctionGroup4() {
        // Special LocoNet message for Uhlenbrock (IB-I and IB-II) implementation
        // Functions F13 to F19
        log.debug("IB1 sendFunctionGroup4");
        int new_IB2_F13_F19 = ((getFunction(19) ? LnConstants.RE_IB2_F19_MASK : 0)
                | (getFunction(18) ? LnConstants.RE_IB2_F18_MASK : 0)
                | (getFunction(17) ? LnConstants.RE_IB2_F17_MASK : 0)
                | (getFunction(16) ? LnConstants.RE_IB2_F16_MASK : 0)
                | (getFunction(15) ? LnConstants.RE_IB2_F15_MASK : 0)
                | (getFunction(14) ? LnConstants.RE_IB2_F14_MASK : 0)
                | (getFunction(13) ? LnConstants.RE_IB2_F13_MASK : 0));
        LocoNetMessage msg = new LocoNetMessage(6);
        msg.setOpCode(LnConstants.OPC_EXP_SLOT_MOVE_RE_OPC_IB2_SPECIAL);
        msg.setElement(1, LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN);
        msg.setElement(2, slot.getSlot());
        msg.setElement(3, LnConstants.RE_IB2_SPECIAL_F13_F19_TOKEN);
        msg.setElement(4, new_IB2_F13_F19);
        network.sendLocoNetMessage(msg);

        // Function F20 (and F28)
        // F12 is also controlled from this message though IB-II uses RE_OPC_IB2_F9_F12 OPS code for F12 - needed to avoid overridding F12 value
        int new_IB2_F20_F28 = ((getFunction(12) ? LnConstants.RE_IB2_SPECIAL_F12_MASK : 0)
                | (getFunction(20) ? LnConstants.RE_IB2_SPECIAL_F20_MASK : 0)
                | (getFunction(28) ? LnConstants.RE_IB2_SPECIAL_F28_MASK : 0));
        LocoNetMessage msg2 = new LocoNetMessage(6);
        msg2.setOpCode(LnConstants.OPC_EXP_SLOT_MOVE_RE_OPC_IB2_SPECIAL);
        msg2.setElement(1, LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN);
        msg2.setElement(2, slot.getSlot());
        msg2.setElement(3, LnConstants.RE_IB2_SPECIAL_F20_F28_TOKEN);
        msg2.setElement(4, new_IB2_F20_F28);
        network.sendLocoNetMessage(msg2);
    }

    @Override
    protected void sendFunctionGroup5() {
        // Functions F21 to F27
        log.debug("IB1 sendFunctionGroup5");
        int new_IB2_F21_F27 = ((getFunction(27) ? LnConstants.RE_IB2_F27_MASK : 0)
                | (getFunction(26) ? LnConstants.RE_IB2_F26_MASK : 0)
                | (getFunction(25) ? LnConstants.RE_IB2_F25_MASK : 0)
                | (getFunction(24) ? LnConstants.RE_IB2_F24_MASK : 0)
                | (getFunction(23) ? LnConstants.RE_IB2_F23_MASK : 0)
                | (getFunction(22) ? LnConstants.RE_IB2_F22_MASK : 0)
                | (getFunction(21) ? LnConstants.RE_IB2_F21_MASK : 0));
        LocoNetMessage msg = new LocoNetMessage(6);
        msg.setOpCode(LnConstants.OPC_EXP_SLOT_MOVE_RE_OPC_IB2_SPECIAL);
        msg.setElement(1, LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN);
        msg.setElement(2, slot.getSlot());
        msg.setElement(3, LnConstants.RE_IB2_SPECIAL_F21_F27_TOKEN);
        msg.setElement(4, new_IB2_F21_F27);
        network.sendLocoNetMessage(msg);

        // Function F28 (and F20)
        // F12 is also controlled from this message though IB-II uses RE_OPC_IB2_F9_F12 OPS code for F12 - needed to avoid overridding F12 value
        int new_IB2_F20_F28 = ((getFunction(12) ? LnConstants.RE_IB2_SPECIAL_F12_MASK : 0)
                | (getFunction(20) ? LnConstants.RE_IB2_SPECIAL_F20_MASK : 0)
                | (getFunction(28) ? LnConstants.RE_IB2_SPECIAL_F28_MASK : 0));
        LocoNetMessage msg2 = new LocoNetMessage(6);
        msg2.setOpCode(LnConstants.OPC_EXP_SLOT_MOVE_RE_OPC_IB2_SPECIAL);
        msg2.setElement(1, LnConstants.RE_IB2_SPECIAL_FUNCS_TOKEN);
        msg2.setElement(2, slot.getSlot());
        msg2.setElement(3, LnConstants.RE_IB2_SPECIAL_F20_F28_TOKEN);
        msg2.setElement(4, new_IB2_F20_F28);
        network.sendLocoNetMessage(msg2);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(Ib2Throttle.class);

}
