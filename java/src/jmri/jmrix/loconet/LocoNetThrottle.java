package jmri.jmrix.loconet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.CheckForNull;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.ThrottleListener;

/**
 * An implementation of DccThrottle via AbstractThrottle with code specific to a
 * LocoNet connection.
 * <p>
 * Speed in the Throttle interfaces and AbstractThrottle is a float, but in
 * LocoNet is an int with values from 0 to 127.
 *
 * @author Glen Oberhauser, Bob Jacobsen Copyright (C) 2003, 2004
 * @author Stephen Williams Copyright (C) 2008
 * @author B. Milhaupt, Copyright (C) 2018
 */
public class LocoNetThrottle extends AbstractThrottle implements SlotListener {

    protected LocoNetSlot slot;
    protected LocoNetInterface network;
    protected LnThrottleManager throttleManager;
    protected int address;

    // members to record the last known spd/dirf/snd bytes AS READ FROM THE LAYOUT!!
    protected int layout_spd;
    protected int layout_dirf;
    protected int layout_snd;
    protected int layout_stat1 = 0;

    // slot status to be warned if slot released or dispatched
    protected int slotStatus;
    protected boolean isDisposing = false;
    // set isInitialized to false to enable setting the throttle ID.
    protected boolean isInitialized = false;

    /**
     * Constructor
     *
     * @param memo connection details
     * @param slot The LocoNetSlot this throttle will talk on.
     */
    public LocoNetThrottle(LocoNetSystemConnectionMemo memo, LocoNetSlot slot) {
        super(memo);
        this.slot = slot;
        network = memo.getLnTrafficController();
        throttleManager = (LnThrottleManager)memo.getThrottleManager();

        // save last known layout state for spd/dirf/snd so we can
        // avoid race condition if another LocoNet process queries
        // our slot while we are in the act of changing it.
        layout_spd = slot.speed();
        layout_dirf = slot.dirf();
        layout_snd = slot.snd();

        // cache settings
        this.speedSetting = floatSpeed(slot.speed());
        
        for (int i = 0; i < 29; i++) {
            super.updateFunction(i,slot.isFunction(i));
        }
        
        // for LocoNet throttles, the default is f2 momentary (for the horn)
        // all other functions are continuos (as set in AbstractThrottle).
        super.updateFunctionMomentary(2, true);

        this.address = slot.locoAddr();
        this.isForward = slot.isForward();
        this.slotStatus = slot.slotStatus();

        switch (slot.decoderType()) {
            case LnConstants.DEC_MODE_128:
            case LnConstants.DEC_MODE_128A:
                setSpeedStepMode(SpeedStepMode.NMRA_DCC_128);
                break;
            case LnConstants.DEC_MODE_28:
            case LnConstants.DEC_MODE_28A:
            case LnConstants.DEC_MODE_28TRI:
                setSpeedStepMode(SpeedStepMode.NMRA_DCC_28);
                break;
            case LnConstants.DEC_MODE_14:
                setSpeedStepMode(SpeedStepMode.NMRA_DCC_14);
                break;
            default:
                log.warn("Unhandled decoder type: {}", slot.decoderType());
                break;
        }

        // listen for changes
        slot.addSlotListener(this);

        network.sendLocoNetMessage(slot.writeNullMove());
        
        // start periodically sending the speed, to keep this
        // attached
        startRefresh();
        log.debug("constructed a new throttle using slot {} for loco address {}", slot.getSlot(), slot.locoAddr());
    }

    /**
     * Convert a LocoNet speed integer to a float speed value
     *
     * @param lSpeed LocoNet style speed value
     * @return speed as float 0-&gt;1.0, or -1.0 to indicate E-Stop
     */
    protected float floatSpeed(int lSpeed) {
        log.debug("speed (int) is {}", lSpeed);
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
     * Computes the integer speed value from a float.
     * <p>
     * Values of less than 0 indicate Emergency Stop.
     * <p>
     * Value of 0.0 indicates stop.
     * <p>
     * Values between 0.0+ and 1.0 imply speed step values between 2 and the
     * maximum value allowed for the loco's speed step mode.
     *
     * @param fSpeed is the floating-point speed value to be converted
     * @return an integer which represents the speed step value
     */
    @Override
    protected int intSpeed(float fSpeed) {
        log.debug("intSpeed speed is {}", fSpeed);
        int speed = super.intSpeed(fSpeed);
        if (speed <= 1) {
            return speed; // return idle and emergency stop
        }
        switch (this.getSpeedStepMode()) {
            case NMRA_DCC_28:
            case MOTOROLA_28:
                return (int) ((fSpeed * 28) * 4) + 12;
            case NMRA_DCC_14:
                return (int) ((fSpeed * 14) * 8) + 8;
            case NMRA_DCC_128:
                return speed;
            default:
                log.warn("Unhandled speed step: {}", this.getSpeedStepMode());
                break;
        }
        return speed;
    }

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    // These overides are necassary as protocol 2 functions groups are non standard.
    // F0-f6 are group 1, f7-f13 are group 2, f14-f20 are group 3 and f21-f28 are group 4. There is no group5.
    @Override
    public void setF0(boolean f0) {
        updateFunction(0,f0);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup1();
        } else {
            sendExpFunctionGroup1();
        }
    }

    @Override
    public void setF1(boolean f1) {
        updateFunction(1,f1);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup1();
        } else {
            sendExpFunctionGroup1();
        }
    }

    @Override
    public void setF2(boolean f2) {
        updateFunction(2,f2);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup1();
        } else {
            sendExpFunctionGroup1();
        }
    }

    @Override
    public void setF3(boolean f3) {
        updateFunction(3,f3);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup1();
        } else {
            sendExpFunctionGroup1();
        }
    }

    @Override
    public void setF4(boolean f4) {
        updateFunction(4,f4);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup1();
        } else {
            sendExpFunctionGroup1();
        }
    }

    @Override
    public void setF5(boolean f5) {
        updateFunction(5,f5);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup2();
        } else {
            sendExpFunctionGroup1();
        }
    }

    @Override
    public void setF6(boolean f6) {
        updateFunction(6,f6);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup2();
        } else {
            sendExpFunctionGroup1();
        }
    }

    @Override
    public void setF7(boolean f7) {
        updateFunction(7,f7);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup2();
        } else {
            sendExpFunctionGroup2();
        }
    }

    @Override
    public void setF8(boolean f8) {
        updateFunction(8,f8);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup2();
        } else {
            sendExpFunctionGroup2();
        }
    }

    @Override
    public void setF9(boolean f9) {
        updateFunction(9,f9);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup3();
        } else {
            sendExpFunctionGroup2();
        }
    }

    @Override
    public void setF10(boolean f10) {
        updateFunction(10,f10);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup3();
        } else {
            sendExpFunctionGroup2();
        }
    }

    @Override
    public void setF11(boolean f11) {
        updateFunction(11,f11);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup3();
        } else {
            sendExpFunctionGroup2();
        }
    }

    @Override
    public void setF12(boolean f12) {
        updateFunction(12,f12);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup3();
        } else {
            sendExpFunctionGroup2();
        }
    }

    @Override
    public void setF13(boolean f13) {
        updateFunction(13,f13);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup2();
        }
    }

    @Override
    public void setF14(boolean f14) {
        updateFunction(14,f14);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
    }

    @Override
    public void setF15(boolean f15) {
        updateFunction(15,f15);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
    }

    @Override
    public void setF16(boolean f16) {
        updateFunction(16,f16);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
    }

    @Override
    public void setF17(boolean f17) {
        updateFunction(17,f17);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
    }

    @Override
    public void setF18(boolean f18) {
        updateFunction(18,f18);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
    }

    @Override
    public void setF19(boolean f19) {
        updateFunction(19,f19);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
    }

    @Override
    public void setF20(boolean f20) {
        updateFunction(20,f20);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
    }

    @Override
    public void setF21(boolean f21) {
        updateFunction(21,f21);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
    }

    @Override
    public void setF22(boolean f22) {
        updateFunction(22,f22);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
    }

    @Override
    public void setF23(boolean f23) {
        updateFunction(23,f23);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
    }

    @Override
    public void setF24(boolean f24) {
        updateFunction(24,f24);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
    }

    @Override
    public void setF25(boolean f25) {
        updateFunction(25,f25);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
    }

    @Override
    public void setF26(boolean f26) {
        updateFunction(26,f26);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
    }

    @Override
    public void setF27(boolean f27) {
        updateFunction(27,f27);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
    }

    @Override
    public void setF28(boolean f28) {
        updateFunction(28,f28);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
    }

    /**
     * Send the LocoNet message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4
     * Unfortunately this is used by all throttles to send direction changes, but the expanded slots dont use this 
     * for direction changes, they use speed... And we don't know if the caller wants to send functions or direction.
     */
    @Override
    protected void sendFunctionGroup1() {
        if (slot.getProtocol() == LnConstants.LOCONETPROTOCOL_TWO) {
            sendExpSpeedAndDirection();
            sendExpFunctionGroup1();
            return;
        }
        int new_dirf = ((getIsForward() ? 0 : LnConstants.DIRF_DIR) |
                (getF0() ? LnConstants.DIRF_F0 : 0) |
                (getF1() ? LnConstants.DIRF_F1 : 0) |
                (getF2() ? LnConstants.DIRF_F2 : 0) |
                (getF3() ? LnConstants.DIRF_F3 : 0) |
                (getF4() ? LnConstants.DIRF_F4 : 0));
        log.debug("sendFunctionGroup1 sending {} to LocoNet slot {}", new_dirf, slot.getSlot());
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_LOCO_DIRF);
        msg.setElement(1, slot.getSlot());
        msg.setElement(2, new_dirf);
        network.sendLocoNetMessage(msg);
    }

    /**
     * Send the LocoNet message to set the state of functions F5, F6, F7, F8
     */
    @Override
    protected void sendFunctionGroup2() {
        int new_snd = ((getF8() ? LnConstants.SND_F8 : 0) |
                (getF7() ? LnConstants.SND_F7 : 0) |
                (getF6() ? LnConstants.SND_F6 : 0) |
                (getF5() ? LnConstants.SND_F5 : 0));
        log.debug("sendFunctionGroup2 sending {} to LocoNet slot {}", new_snd, slot.getSlot());
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_LOCO_SND);
        msg.setElement(1, slot.getSlot());
        msg.setElement(2, new_snd);
        network.sendLocoNetMessage(msg);
    }

    /**
     * Sends Function Group 3 values - F9 thru F12, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     */
    @Override
    protected void sendFunctionGroup3() {
        // LocoNet practice is to send F9-F12 as a DCC packet
        byte[] result = jmri.NmraPacket.function9Through12Packet(address, (address >= 128),
                getF9(), getF10(), getF11(), getF12());

        log.debug("sendFunctionGroup3 sending {} to LocoNet slot {}", result, slot.getSlot());
        ((jmri.CommandStation) adapterMemo.get(jmri.CommandStation.class)).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Sends Function Group 4 values - F13 thru F20, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     */
    @Override
    protected void sendFunctionGroup4() {
        // LocoNet practice is to send F13-F20 as a DCC packet
        byte[] result = jmri.NmraPacket.function13Through20Packet(address, (address >= 128),
                getF13(), getF14(), getF15(), getF16(),
                getF17(), getF18(), getF19(), getF20());

        log.debug("sendFunctionGroup4 sending {} to LocoNet slot {}", result, slot.getSlot());
        ((jmri.CommandStation) adapterMemo.get(jmri.CommandStation.class)).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Sends Function Group 5 values - F21 thru F28, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     */
    @Override
    protected void sendFunctionGroup5() {
        // LocoNet practice is to send F21-F28 as a DCC packet
        byte[] result = jmri.NmraPacket.function21Through28Packet(address, (address >= 128),
                getF21(), getF22(), getF23(), getF24(),
                getF25(), getF26(), getF27(), getF28());

        log.debug("sendFunctionGroup5 sending {} to LocoNet slot {}", result, slot.getSlot());
        ((jmri.CommandStation) adapterMemo.get(jmri.CommandStation.class)).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Send the Expanded LocoNet message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4, F5, F6
     */
    protected void sendExpFunctionGroup1() {
            int new_F0F6 = ((getF5() ? 0b00100000 : 0) | (getF6() ? 0b01000000 : 0)
                | (getF0() ? LnConstants.DIRF_F0 : 0)
                | (getF1() ? LnConstants.DIRF_F1 : 0)
                | (getF2() ? LnConstants.DIRF_F2 : 0)
                | (getF3() ? LnConstants.DIRF_F3 : 0)
                | (getF4() ? LnConstants.DIRF_F4 : 0));
            LocoNetMessage msg = new LocoNetMessage(6);
            msg.setOpCode(LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR);
            msg.setElement(1, (slot.getSlot() / 128) | LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6_MASK );
            msg.setElement(2,slot.getSlot() & 0b01111111);
            msg.setElement(3,slot.id() & 0x7F);
            msg.setElement(4, new_F0F6);
            network.sendLocoNetMessage(msg);
    }

    /**
     * Send the Expanded LocoNet message to set the state of functions F7, F8, F8, F9, F10, F11, F12, F13
     */
    protected void sendExpFunctionGroup2() {
            int new_F7F13 = ((getF7() ? 0b00000001 : 0) | (getF8() ? 0b00000010 : 0)
                    | (getF9()  ? 0b00000100 : 0)
                    | (getF10() ? 0b00001000 : 0)
                    | (getF11() ? 0b00010000 : 0)
                    | (getF12() ? 0b00100000 : 0)
                    | (getF13() ? 0b01000000 : 0));
                LocoNetMessage msg = new LocoNetMessage(6);
                msg.setOpCode(LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR);
                msg.setElement(1, (slot.getSlot() / 128) | LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13_MASK );
                msg.setElement(2,slot.getSlot() & 0b01111111);
                msg.setElement(3,slot.id() & 0x7F);
                msg.setElement(4, new_F7F13);
                network.sendLocoNetMessage(msg);
    }

    /**
     * Sends expanded loconet message F14 thru F20
     * Message.
     */
    protected void sendExpFunctionGroup3() {
        int new_F14F20 = ((getF14() ? 0b00000001 : 0) | (getF15() ? 0b00000010 : 0)
                | (getF16()  ? 0b00000100 : 0)
                | (getF17() ? 0b00001000 : 0)
                | (getF18() ? 0b00010000 : 0)
                | (getF19() ? 0b00100000 : 0)
                | (getF20() ? 0b01000000 : 0));
            LocoNetMessage msg = new LocoNetMessage(6);
            msg.setOpCode(LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR);
            msg.setElement(1, (slot.getSlot() / 128) | LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20_MASK );
            msg.setElement(2,slot.getSlot() & 0b01111111);
            msg.setElement(3,slot.id() & 0x7F);
            msg.setElement(4, new_F14F20);
            network.sendLocoNetMessage(msg);
    }

    /**
     * Sends Expanded loconet message F21 thru F28 Message.
     */
    protected void sendExpFunctionGroup4() {
        int new_F14F20 = ((getF21() ? 0b00000001 : 0) |
                (getF22() ? 0b00000010 : 0) |
                (getF23() ? 0b00000100 : 0) |
                (getF24() ? 0b00001000 : 0) |
                (getF25() ? 0b00010000 : 0) |
                (getF26() ? 0b00100000 : 0) |
                (getF27() ? 0b01000000 : 0));
        LocoNetMessage msg = new LocoNetMessage(6);
        msg.setOpCode(LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR);
        if (!getF28()) {
            msg.setElement(1, (slot.getSlot() / 128) | LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF_MASK);
        } else {
            msg.setElement(1, (slot.getSlot() / 128) | LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28ON_MASK);
        }
        msg.setElement(2, slot.getSlot() & 0b01111111);
        msg.setElement(3, slot.id() & 0x7F);
        msg.setElement(4, new_F14F20);
        network.sendLocoNetMessage(msg);
    }

    /**
     * Send the expanded slot command for speed and direction.
     */
    protected void sendExpSpeedAndDirection() {
        LocoNetMessage msg = new LocoNetMessage(6);
        msg.setOpCode(LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR);
        msg.setElement(1, ((slot.getSlot() / 128) & 0x03) | (isForward ? 0x00 : 0x08));
        msg.setElement(2, slot.getSlot() & 0x7f);
        msg.setElement(3, (slot.id() & 0x7f));
        msg.setElement(4, slot.speed());
        network.sendLocoNetMessage(msg);
    }
    /**
     * Send a LocoNet message to set the loco speed speed.
     *
     * @param speed Number from 0 to 1; less than zero is "emergency stop"
     */
    @Override
    public void setSpeedSetting(float speed) {
        setSpeedSetting(speed, false, false);
    }

    /**
     * Set the Speed, ensuring that a LocoNet message is sent to update the slot
     * even if the new speed is effectively the same as the current speed. Note: this
     * can cause an increase in LocoNet traffic.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @Override
    public void setSpeedSettingAgain(float speed) {
        setSpeedSetting(speed, true, true);
    }

    /**
     * Set the speed. No LocoNet message is sent if the new speed would
     * result in a 'duplicate' - ie. a speed setting no different to the one the slot
     * currently has - unless the boolean paramters indicate it should be.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     * @param allowDuplicates boolean - if true, send a LocoNet message no matter what
     * @param allowDuplicatesOnStop boolean - if true, send a LocoNet message if the new speed is
     *                              'idle' or 'emergency stop', even if that matches the
     *                              existing speed.
     *
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public void setSpeedSetting(float speed, boolean allowDuplicates, boolean allowDuplicatesOnStop) {
        log.debug("setSpeedSetting: called with speed {} for LocoNet slot {}", speed, slot.getSlot());
        if (LnConstants.CONSIST_MID == slot.consistStatus()
                || LnConstants.CONSIST_SUB == slot.consistStatus()) {
            // Digitrax slots use the same memory location to store the
            // speed AND the slot to which a locomotive is consisted.
            // if the locomotive is either a CONSIST_MID or a CONSIST_SUB,
            // we need to ignore the request to change the speed
            log.debug("Attempt to change speed on locomotive {} which is a {}", getLocoAddress(), LnConstants.CONSIST_STAT(slot.consistStatus()));
            return;
        }
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        if (speed < 0) {
            this.speedSetting = -1.f;
        }

        int new_spd = intSpeed(speed);

        // decide whether to send a new LocoNet message
        boolean sendLoconetMessage = false;
        if (new_spd != layout_spd) {
            // the new speed is different - send a message
            sendLoconetMessage = true;
        } else if (allowDuplicates) {
            // calling method wants a new mesage sent regardless
            sendLoconetMessage = true;
        } else if (allowDuplicatesOnStop && new_spd <= 1) {
            // calling method wants a new message sent if the speed is idle or estop, which it is
            sendLoconetMessage = true;
        }

        if (sendLoconetMessage) {
            log.debug("setSpeedSetting: sending speed {} to LocoNet slot {}", speed, slot.getSlot());
            if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
                LocoNetMessage msg = new LocoNetMessage(4);
                msg.setOpCode(LnConstants.OPC_LOCO_SPD);
                msg.setElement(1, slot.getSlot());
                log.debug(""setSpeedSetting: float speed: {} LocoNet speed: {}", speed, new_spd);
                msg.setElement(2, new_spd);
                network.sendLocoNetMessage(msg);
            } else {
                LocoNetMessage msg = new LocoNetMessage(6);
                msg.setOpCode(LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR);
                msg.setElement(1, ((slot.getSlot() / 128) & 0x03) | (isForward ? 0x00 : 0x08));
                msg.setElement(2, slot.getSlot() & 0x7f);
                msg.setElement(3, (slot.id() & 0x7f));
                msg.setElement(4, new_spd);
                network.sendLocoNetMessage(msg);
            }
        } else {
            log.debug("setSpeedSetting: not sending LocoNet message to slot {}, new speed == old speed", slot.getSlot());
        }

        // reset timeout
        if (mRefreshTimer != null) {
            mRefreshTimer.stop();
            mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
            mRefreshTimer.start();
            log.debug("Initially starting refresh timer for slot {} address {}", slot.getSlot(), slot.locoAddr());
        }
        firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        record(speed);
    }

    /**
     * Sends a LocoNet message containing the specified direction of travel.
     *
     * LocoNet actually puts forward and backward in the same message as the
     * first function group.
     *
     * @param forward is true for forward movement, else false
     */
    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        log.debug("setIsForward to {}, old value {}", isForward, old);
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
            sendFunctionGroup1();
        } else {
            sendExpSpeedAndDirection();
        }
        if (old != this.isForward) {
            notifyPropertyChangeListener(ISFORWARD, old, this.isForward);
        }
    }

    /**
     * Returns the LocoNetSlot which is used for controlling the loco assoicated
     * with this throttle.
     *
     * @return the LocoNetSlot
     */
    @CheckForNull
    public LocoNetSlot getLocoNetSlot() {
        if (slot == null) return slot;
        log.debug("getLocoNetSlot is returning slot {}", slot.getSlot());
        return slot;
    }

    @Override
    public String toString() {
        return getLocoAddress().toString();
    }

    /**
     * Dispose the LocoNetThrottle when finished with this object.
     *
     * After this is executed, further use of this Throttle object will
     * result in a JmriException.
     */
    @Override
    protected void throttleDispose() {
        if (isDisposing) return;
        log.debug("throttleDispose - disposing of throttle (and setting slot = null)");
        isDisposing = true;

        // Release throttle connections
        if (slot != null) {
            if (slot.slotStatus() != LnConstants.LOCO_COMMON) {
                // Digitrax throttles do not set the slot speed to zero, so do
                // not do so here.

                // Make the slot common, after a little wait
                log.debug("dispatchThrottle is dispatching slot {}", slot);
                network.sendLocoNetMessage(slot.releaseSlot());
            }
            // Can remove the slot listener at any time; any further messages
            // aren't needed.
            slot.removeSlotListener(this);
            // Stop the throttle speed refresh timer
            if (mRefreshTimer != null) {
                mRefreshTimer.stop();
                log.debug("Stopped refresh timer for slot {} address {} as part of throttleDispose", slot.getSlot(), slot.locoAddr());
            mRefreshTimer = null;
            }

            slot = null;
            network = null;

            finishRecord();
            isDisposing = false;
        }
    }

    javax.swing.Timer mRefreshTimer = null;

    /**
     * Starts the "refresh" timer.  The "refresh" timer determines
     * when to send a new LocoNet message to "refresh" the slot's speed
     * setting, so that the slot does not get "purged".
     *
     */
    protected void startRefresh() {
        mRefreshTimer = new javax.swing.Timer(50000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                timeout();
            }
        });
        mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
        mRefreshTimer.start();
        log.debug("Starting refresh timer for slot {} address {}", slot.getSlot(), slot.locoAddr());
    }

    /**
     * Internal routine to resend the speed on a timeout
     */
    synchronized protected void timeout() {
        if (slot != null) {
            log.debug("refresh timer timed-out on slot {}", slot.getSlot());
            // clear the last known layout_spd so that we will actually send the
            // message.
            layout_spd = -1;
            setSpeedSetting(speedSetting);
        }
        else {
            log.debug("refresh timer time-out on a null slot");
        }
    }

    /**
     * Get notified when underlying slot acquisition process fails.  Slot acquisition
     * failure is handled by @link LnThrottleManager, so no code is required here.
     *
     * @param addr Locomotive address
     * @param s reason the acquisition failed
     */
    public void notifyRefused(int addr, String s) {
        // don't do anything here; is handled by LnThrottleManager.
    }


    /**
     * Get notified when underlying slot information changes
     *
     * @param pSlot the slot which was changed
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public void notifyChangedSlot(LocoNetSlot pSlot) {
        if (slot != pSlot) {
            log.error("notified of change in different slot");
        }
        log.debug("notifyChangedSlot executing for slot {}, slotStatus {}", slot.getSlot(), Integer.toHexString(slot.slotStatus()));

        if(!isInitialized && slot.slotStatus() == LnConstants.LOCO_IN_USE){
           log.debug("Attempting to update slot with this JMRI instance's throttle id ({})", throttleManager.getThrottleID());
           network.sendLocoNetMessage(slot.writeThrottleID(throttleManager.getThrottleID()));
           isInitialized = true;
        }

        // Save current layout state of spd/dirf/snd so we won't run amok
        // toggling values if another LocoNet entity accesses the slot while
        // our most recent change request is still in-flight.
        layout_spd = slot.speed();
        layout_dirf = slot.dirf();
        layout_snd = slot.snd();

        // handle change in each state
        if (this.speedSetting != floatSpeed(slot.speed())) {
            float old = this.speedSetting;
            this.speedSetting = floatSpeed(slot.speed());
            log.debug("notifyChangedSlot: old speed: {} new speed: {}", old, this.speedSetting); // NOI18N
            firePropertyChange(SPEEDSETTING, old, this.speedSetting);
        }

        firePropertyChange(ISFORWARD, this.isForward, this.isForward = slot.isForward());

        // Slot status
        if (slotStatus != slot.slotStatus()) {
            int newStat = slot.slotStatus();
            log.debug("Slot status changed from {} to {}", LnConstants.LOCO_STAT(slotStatus), LnConstants.LOCO_STAT(newStat)); // NOI18N
            // PropertyChangeListeners notification: ThrottleConnected from True to False when disconnected
            firePropertyChange("ThrottleConnected", (slotStatus & LnConstants.LOCOSTAT_MASK) == LnConstants.LOCO_IN_USE, // NOI18N
                    !((slotStatus & LnConstants.LOCOSTAT_MASK) == LnConstants.LOCO_IN_USE));
            slotStatus = newStat;
        }

        // It is possible that the slot status change we are being notified of
        // is the slot being set to status COMMON. In which case the slot just
        // got set to null. No point in continuing. In fact to do so causes a NPE.
        if (slot == null) {
            return;
        }

        switch (slot.decoderType()) {
            case LnConstants.DEC_MODE_128:
            case LnConstants.DEC_MODE_128A:
                if(SpeedStepMode.NMRA_DCC_128 != getSpeedStepMode()) {
                   setSpeedStepMode(SpeedStepMode.NMRA_DCC_128);
                }
                break;
            case LnConstants.DEC_MODE_28:
            case LnConstants.DEC_MODE_28A:
            case LnConstants.DEC_MODE_28TRI:
                if(SpeedStepMode.NMRA_DCC_28 != getSpeedStepMode()) {
                   setSpeedStepMode(SpeedStepMode.NMRA_DCC_28);
                }
                break;
            case LnConstants.DEC_MODE_14:
                if(SpeedStepMode.NMRA_DCC_14 != getSpeedStepMode()) {
                   setSpeedStepMode(SpeedStepMode.NMRA_DCC_14);
                }
                break;
            default:
                log.warn("Unhandled decoder type: {}", slot.decoderType());
                break;
        }

        // Functions
        for (int i = 0; i < 29; i++) {
            updateFunction(i,slot.isFunction(i));
        }
        
    }

    /**
     * Set the speed step value and the related speedIncrement value.
     *
     * @param Mode the current speed step mode - default should be 128
     *             speed step mode in most cases
     */
    @Override
    public void setSpeedStepMode(SpeedStepMode Mode) {
        int status = slot.slotStatus();
        log.debug("Speed Step Mode Change to Mode: {} Current mode is: {}", Mode, this.speedStepMode); // NOI18N
        log.debug("Current Slot Mode: {}", LnConstants.DEC_MODE(status)); // NOI18N
        firePropertyChange(SPEEDSTEPS, this.speedStepMode, this.speedStepMode = Mode);
        if (Mode == SpeedStepMode.NMRA_DCC_14) {
            log.debug("14 speed step change"); // NOI18N
            status = status & ((~LnConstants.DEC_MODE_MASK)
                    | LnConstants.STAT1_SL_SPDEX)
                    | LnConstants.DEC_MODE_14;
        } else if (Mode == SpeedStepMode.MOTOROLA_28) {
            log.debug("28-Tristate speed step change");
            status = status & ((~LnConstants.DEC_MODE_MASK)
                    | LnConstants.STAT1_SL_SPDEX)
                    | LnConstants.DEC_MODE_28TRI;
        } else if (Mode == SpeedStepMode.NMRA_DCC_28) {
            log.debug("28 speed step change");
            status = status & ((~LnConstants.DEC_MODE_MASK)
                    | LnConstants.STAT1_SL_SPDEX);
            // | LnConstants.DEC_MODE_28;      // DEC_MODE_28 has a zero value, here for documentation
            // it unfortunately shows a INT_VACUOUS_BIT_OPERATION in SpotBugs
            // and I don't want to annote that around this entire long method
        } else { // default to 128 speed step mode
            log.debug("128 speed step change");
            status = status & ((~LnConstants.DEC_MODE_MASK)
                    | LnConstants.STAT1_SL_SPDEX)
                    | LnConstants.DEC_MODE_128;
        }
        log.debug("New Slot Mode: {}", LnConstants.DEC_MODE(status));
        if (mRefreshTimer != null) // the refresh timer isn't created until
        // after initilization.  We only want to
        // modify the slot after the initilization
        // is complete.
        {
            network.sendLocoNetMessage(slot.writeMode(status));
        }
    }

    /**
     * Get the address controlled by this throttle. If the throttle is controlling.
     *
     * @return a LocoAddress for the address controlled by this throttle
     */
    @Override
    public LocoAddress getLocoAddress() {
        if (slot != null) {
            if ((slot.slotStatus() == LnConstants.LOCO_IN_USE) ||
                (slot.slotStatus() == LnConstants.LOCO_COMMON)) {
                log.debug("getLocoAddress replying address {} for slot {}", address, slot.getSlot());
                return new DccLocoAddress(address, LnThrottleManager.isLongAddress(address));
            }
        }
        log.debug("getLocoAddress replying address {} for slot not in-use or for sub-consisted slot or for null slot", address);
        return new DccLocoAddress(address, LnThrottleManager.isLongAddress(address));
    }

    /**
     * "Dispatch" a LocoNet throttle by setting the slot as "common" then performing
     * a slot move to slot 0.
     * <p>
     * The throttle being dispatched no longer has control of the loco, but other
     * throttles may continue to control the loco.
     *
     * @param t throttle being dispatched
     * @param l throttle listener to remove
     */
    public void dispatchThrottle(DccThrottle t, ThrottleListener l) {
        log.debug("dispatchThrottle - throttle {}", t.getLocoAddress());
        // set status to common & dispatch slot
        // needs to be done one after another with no delay.
        if (t instanceof LocoNetThrottle){
            LocoNetThrottle lnt = (LocoNetThrottle) t;
            LocoNetSlot tSlot = lnt.getLocoNetSlot();
            if (tSlot != null) {
                if (tSlot.slotStatus() != LnConstants.LOCO_COMMON) {
                    network.sendLocoNetMessage(tSlot.writeStatus(LnConstants.LOCO_COMMON));
                    log.debug("dispatchThrottle is dispatching slot {}", tSlot);
                        network.sendLocoNetMessage(tSlot.dispatchSlot());
                }
            }
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LocoNetThrottle.class);

}
