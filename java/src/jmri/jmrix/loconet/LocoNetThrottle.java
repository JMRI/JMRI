package jmri.jmrix.loconet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nullable;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.ThrottleListener;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * <p>
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
        this.f0 = slot.isF0();
        this.f1 = slot.isF1();
        this.f2 = slot.isF2();
        this.f3 = slot.isF3();
        this.f4 = slot.isF4();
        this.f5 = slot.isF5();
        this.f6 = slot.isF6();
        this.f7 = slot.isF7();
        this.f8 = slot.isF8();

        // extended values
        this.f8 = slot.isF8();
        this.f9 = slot.isF9();
        this.f10 = slot.isF10();
        this.f11 = slot.isF11();
        this.f12 = slot.isF12();
        this.f13 = slot.isF13();
        this.f14 = slot.isF14();
        this.f15 = slot.isF15();
        this.f16 = slot.isF16();
        this.f17 = slot.isF17();
        this.f18 = slot.isF18();
        this.f19 = slot.isF19();
        this.f20 = slot.isF20();
        this.f21 = slot.isF21();
        this.f22 = slot.isF22();
        this.f23 = slot.isF23();
        this.f24 = slot.isF24();
        this.f25 = slot.isF25();
        this.f26 = slot.isF26();
        this.f27 = slot.isF27();
        this.f28 = slot.isF28();

        this.address = slot.locoAddr();
        this.isForward = slot.isForward();
        this.slotStatus = slot.slotStatus();

        switch (slot.decoderType()) {
            case LnConstants.DEC_MODE_128:
            case LnConstants.DEC_MODE_128A:
                setSpeedStepMode(DccThrottle.SpeedStepMode128);
                break;
            case LnConstants.DEC_MODE_28:
            case LnConstants.DEC_MODE_28A:
            case LnConstants.DEC_MODE_28TRI:
                setSpeedStepMode(DccThrottle.SpeedStepMode28);
                break;
            case LnConstants.DEC_MODE_14:
                setSpeedStepMode(DccThrottle.SpeedStepMode14);
                break;
            default:
                log.warn("Unhandled decoder type: {}", slot.decoderType());
                break;
        }

        // listen for changes
        slot.addSlotListener(this);

        if ( slot.getSlot() < 128 ) {
            // perform the null slot move for low numbered slots
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_MOVE_SLOTS);
            msg.setElement(1, slot.getSlot());
            msg.setElement(2, slot.getSlot());
            network.sendLocoNetMessage(msg);
        } else {
            // or the null move for higher numbered slots
            LocoNetMessage msg = new LocoNetMessage(6);
            msg.setOpCode(0xd4);
            msg.setElement(1, (slot.getSlot() / 128) | 0b00111000 );
            msg.setElement(2, slot.getSlot() & 0b01111111);
            msg.setElement(3, (slot.getSlot() / 128) & 0b00000111 );
            msg.setElement(4, slot.getSlot() & 0b01111111);
            network.sendLocoNetMessage(msg);
        }

        // start periodically sending the speed, to keep this
        // attached
        startRefresh();
        log.debug("constructed a new throttle using slot {} for loco address {}", slot.getSlot(), slot.locoAddr());
    }

    /**
     * Convert a LocoNet speed integer to a float speed value
     * <p>
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
        if (getSpeedStepMode() == DccThrottle.SpeedStepMode28) {
            if (lSpeed <= 15) //Value less than 15 is in the stop/estop range bracket
            {
                return 0.f;
            }
            return (((lSpeed - 12) / 4f) / 28.f);
        } else if (getSpeedStepMode() == DccThrottle.SpeedStepMode14) {
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
     * <p>
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
            case DccThrottle.SpeedStepMode28:
            case DccThrottle.SpeedStepMode28Mot:
                return (int) ((fSpeed * 28) * 4) + 12;
            case DccThrottle.SpeedStepMode14:
                return (int) ((fSpeed * 14) * 8) + 8;
            case DccThrottle.SpeedStepMode128:
                return speed;
            default:
                log.warn("Unhandled speed step: {}", this.getSpeedStepMode());
                break;
        }
        return speed;
    }

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    @Override
    public void setF0(boolean f0) {
        boolean old = this.f0;
        this.f0 = f0;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup1();
        } else {
            sendExpFunctionGroup1();
        }
        if (old != this.f0) {
            notifyPropertyChangeListener(Throttle.F0, old, this.f0);
        }
    }

    @Override
    public void setF1(boolean f1) {
        boolean old = this.f1;
        this.f1 = f1;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup1();
        } else {
            sendExpFunctionGroup1();
        }
        if (old != this.f1) {
            notifyPropertyChangeListener(Throttle.F1, old, this.f1);
        }
    }

    @Override
    public void setF2(boolean f2) {
        boolean old = this.f2;
        this.f2 = f2;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup1();
        } else {
            sendExpFunctionGroup1();
        }
        if (old != this.f2) {
            notifyPropertyChangeListener(Throttle.F2, old, this.f2);
        }
    }

    @Override
    public void setF3(boolean f3) {
        boolean old = this.f3;
        this.f3 = f3;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup1();
        } else {
            sendExpFunctionGroup1();
        }
        if (old != this.f3) {
            notifyPropertyChangeListener(Throttle.F3, old, this.f3);
        }
    }

    @Override
    public void setF4(boolean f4) {
        boolean old = this.f4;
        this.f4 = f4;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup1();
        } else {
            sendExpFunctionGroup1();
        }
        if (old != this.f4) {
            notifyPropertyChangeListener(Throttle.F4, old, this.f4);
        }
    }

    @Override
    public void setF5(boolean f5) {
        boolean old = this.f5;
        this.f5 = f5;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup2();
        } else {
            sendExpFunctionGroup1();
        }
        if (old != this.f5) {
            notifyPropertyChangeListener(Throttle.F5, old, this.f5);
        }
    }

    @Override
    public void setF6(boolean f6) {
        boolean old = this.f6;
        this.f6 = f6;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup2();
        } else {
            sendExpFunctionGroup1();
        }
        if (old != this.f6) {
            notifyPropertyChangeListener(Throttle.F6, old, this.f6);
        }
    }

    @Override
    public void setF7(boolean f7) {
        boolean old = this.f7;
        this.f7 = f7;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup2();
        } else {
            sendExpFunctionGroup2();
        }
        if (old != this.f7) {
            notifyPropertyChangeListener(Throttle.F7, old, this.f7);
        }
    }

    @Override
    public void setF8(boolean f8) {
        boolean old = this.f8;
        this.f8 = f8;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup2();
        } else {
            sendExpFunctionGroup2();
        }
        if (old != this.f8) {
            notifyPropertyChangeListener(Throttle.F8, old, this.f8);
        }
    }

    @Override
    public void setF9(boolean f9) {
        boolean old = this.f9;
        this.f9 = f9;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup3();
        } else {
            sendExpFunctionGroup2();
        }
        if (old != this.f9) {
            notifyPropertyChangeListener(Throttle.F9, old, this.f9);
        }
    }

    @Override
    public void setF10(boolean f10) {
        boolean old = this.f10;
        this.f10 = f10;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup3();
        } else {
            sendExpFunctionGroup2();
        }
        if (old != this.f10) {
            notifyPropertyChangeListener(Throttle.F10, old, this.f10);
        }
    }

    @Override
    public void setF11(boolean f11) {
        boolean old = this.f11;
        this.f11 = f11;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup3();
        } else {
            sendExpFunctionGroup2();
        }
        if (old != this.f11) {
            notifyPropertyChangeListener(Throttle.F11, old, this.f11);
        }
    }

    @Override
    public void setF12(boolean f12) {
        boolean old = this.f12;
        this.f12 = f12;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup3();
        } else {
            sendExpFunctionGroup2();
        }
        if (old != this.f12) {
            notifyPropertyChangeListener(Throttle.F12, old, this.f12);
        }
    }

    @Override
    public void setF13(boolean f13) {
        boolean old = this.f13;
        this.f13 = f13;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup2();
        }
        if (old != this.f13) {
            notifyPropertyChangeListener(Throttle.F13, old, this.f13);
        }
    }

    @Override
    public void setF14(boolean f14) {
        boolean old = this.f14;
        this.f14 = f14;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
        if (old != this.f14) {
            notifyPropertyChangeListener(Throttle.F14, old, this.f14);
        }
    }

    @Override
    public void setF15(boolean f15) {
        boolean old = this.f15;
        this.f15 = f15;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
        if (old != this.f15) {
            notifyPropertyChangeListener(Throttle.F15, old, this.f15);
        }
    }

    @Override
    public void setF16(boolean f16) {
        boolean old = this.f16;
        this.f16 = f16;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
        if (old != this.f16) {
            notifyPropertyChangeListener(Throttle.F16, old, this.f16);
        }
    }

    @Override
    public void setF17(boolean f17) {
        boolean old = this.f17;
        this.f17 = f17;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
        if (old != this.f17) {
            notifyPropertyChangeListener(Throttle.F17, old, this.f17);
        }
    }

    @Override
    public void setF18(boolean f18) {
        boolean old = this.f18;
        this.f18 = f18;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
        if (old != this.f18) {
            notifyPropertyChangeListener(Throttle.F18, old, this.f18);
        }
    }

    @Override
    public void setF19(boolean f19) {
        boolean old = this.f19;
        this.f19 = f19;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
        if (old != this.f19) {
            notifyPropertyChangeListener(Throttle.F19, old, this.f19);
        }
    }

    @Override
    public void setF20(boolean f20) {
        boolean old = this.f20;
        this.f20 = f20;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup4();
        } else {
            sendExpFunctionGroup3();
        }
        if (old != this.f20) {
            notifyPropertyChangeListener(Throttle.F20, old, this.f20);
        }
    }

    @Override
    public void setF21(boolean f21) {
        boolean old = this.f21;
        this.f21 = f21;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
        if (old != this.f21) {
            notifyPropertyChangeListener(Throttle.F21, old, this.f21);
        }
    }

    @Override
    public void setF22(boolean f22) {
        boolean old = this.f22;
        this.f22 = f22;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
        if (old != this.f22) {
            notifyPropertyChangeListener(Throttle.F22, old, this.f22);
        }
    }

    @Override
    public void setF23(boolean f23) {
        boolean old = this.f23;
        this.f23 = f23;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
        if (old != this.f23) {
            notifyPropertyChangeListener(Throttle.F23, old, this.f23);
        }
    }

    @Override
    public void setF24(boolean f24) {
        boolean old = this.f24;
        this.f24 = f24;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
        if (old != this.f24) {
            notifyPropertyChangeListener(Throttle.F24, old, this.f24);
        }
    }

    @Override
    public void setF25(boolean f25) {
        boolean old = this.f25;
        this.f25 = f25;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
        if (old != this.f25) {
            notifyPropertyChangeListener(Throttle.F25, old, this.f25);
        }
    }

    @Override
    public void setF26(boolean f26) {
        boolean old = this.f26;
        this.f26 = f26;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
        if (old != this.f26) {
            notifyPropertyChangeListener(Throttle.F26, old, this.f26);
        }
    }

    @Override
    public void setF27(boolean f27) {
        boolean old = this.f27;
        this.f27 = f27;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
        if (old != this.f27) {
            notifyPropertyChangeListener(Throttle.F27, old, this.f27);
        }
    }

    @Override
    public void setF28(boolean f28) {
        boolean old = this.f28;
        this.f28 = f28;
        if (slot.getSlot() < 128 ) {
            sendFunctionGroup5();
        } else {
            sendExpFunctionGroup4();
        }
        if (old != this.f28) {
            notifyPropertyChangeListener(Throttle.F28, old, this.f28);
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
        // this is actually quite silly as anyone calling setFn or Setdir has already sent the message in
        // those routines so this results in 2 sends
        if (slot.getSlot() > 127) {
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
     * Set the Speed, ensuring that a Loconet message is sent to update the slot
     * even if the new speed is effectively the same as the current speed. Note: this
     * can cause an increase in Loconet traffic.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @Override
    public void setSpeedSettingAgain(float speed) {
        setSpeedSetting(speed, true, true);
    }

    /**
     * Set the speed. No Loconet message is sent if the new speed would
     * result in a 'duplicate' - ie. a speed setting no different to the one the slot
     * currently has - unless the boolean paramters indicate it should be.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     * @param allowDuplicates boolean - if true, send a Loconet message no matter what
     * @param allowDuplicatesOnStop boolean - if true, send a Loconet message if the new speed is
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
            if (slot.getSlot() < 128) {
                LocoNetMessage msg = new LocoNetMessage(4);
                msg.setOpCode(LnConstants.OPC_LOCO_SPD);
                msg.setElement(1, slot.getSlot());
                log.debug("setSpeedSetting: float speed: " + speed + " LocoNet speed: " + new_spd);
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
        if (mRefreshTimer != null) { // got NullPointerException sometimes
            mRefreshTimer.stop();
            mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
            mRefreshTimer.start();
            log.debug("Initially starting refresh timer for slot {} address {}", slot.getSlot(), slot.locoAddr());
        }
        if (oldSpeed != this.speedSetting) {
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting); // NOI18N
        }
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
        if (slot.getSlot() < 128) {
            sendFunctionGroup1();
        } else {
            sendExpSpeedAndDirection();
        }
        if (old != this.isForward) {
            notifyPropertyChangeListener("IsForward", old, this.isForward); // NOI18N
        }
    }

    /**
     * Returns the LocoNetSlot which is used for controlling the loco assoicated 
     * with this throttle.
     * 
     * @return the LocoNetSlot 
     */
    @Nullable
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
           //if (slot.getSlot() < 128) {
               network.sendLocoNetMessage(slot.writeThrottleID(throttleManager.getThrottleID()));
           //}
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
            Float newSpeed = Float.valueOf(floatSpeed(slot.speed()));
            log.debug("notifyChangedSlot: old speed: " + this.speedSetting + " new Speed: " + newSpeed); // NOI18N
            notifyPropertyChangeListener("SpeedSetting", Float.valueOf(this.speedSetting), newSpeed); // NOI18N
            this.speedSetting = newSpeed.floatValue();
        }

        boolean temp;
        if (this.isForward != slot.isForward()) {
            temp = this.isForward;
            this.isForward = slot.isForward();
            notifyPropertyChangeListener("IsForward", Boolean.valueOf(temp), Boolean.valueOf(slot.isForward())); // NOI18N
        }

        // Slot status
        if (slotStatus != slot.slotStatus()) {
            int newStat = slot.slotStatus();
            if (log.isDebugEnabled()) {
                log.debug("Slot status changed from " + LnConstants.LOCO_STAT(slotStatus) + " to " + LnConstants.LOCO_STAT(newStat)); // NOI18N
            }
            // PropertyChangeListeners notification: ThrottleConnected from True to False when disconnected
            notifyPropertyChangeListener("ThrottleConnected", (slotStatus & LnConstants.LOCOSTAT_MASK) == LnConstants.LOCO_IN_USE, // NOI18N
                    !((slotStatus & LnConstants.LOCOSTAT_MASK) == LnConstants.LOCO_IN_USE));
            slotStatus = newStat;
        }

        // It is possible that the slot status change we are being notified of
        // is the slot being set to status COMMON. In which case the slot just
        // got set to null. No point in continuing. In fact to do so causes a NPE.
        if (slot == null) {
            return;
        }

        // Functions
        if (this.f0 != slot.isF0()) {
            temp = this.f0;
            this.f0 = slot.isF0();
            notifyPropertyChangeListener(Throttle.F0, Boolean.valueOf(temp), Boolean.valueOf(slot.isF0()));
        }
        if (this.f1 != slot.isF1()) {
            temp = this.f1;
            this.f1 = slot.isF1();
            notifyPropertyChangeListener(Throttle.F1, Boolean.valueOf(temp), Boolean.valueOf(slot.isF1()));
        }
        if (this.f2 != slot.isF2()) {
            temp = this.f2;
            this.f2 = slot.isF2();
            notifyPropertyChangeListener(Throttle.F2, Boolean.valueOf(temp), Boolean.valueOf(slot.isF2()));
        }
        if (this.f3 != slot.isF3()) {
            temp = this.f3;
            this.f3 = slot.isF3();
            notifyPropertyChangeListener(Throttle.F3, Boolean.valueOf(temp), Boolean.valueOf(slot.isF3()));
        }
        if (this.f4 != slot.isF4()) {
            temp = this.f4;
            this.f4 = slot.isF4();
            notifyPropertyChangeListener(Throttle.F4, Boolean.valueOf(temp), Boolean.valueOf(slot.isF4()));
        }
        if (this.f5 != slot.isF5()) {
            temp = this.f5;
            this.f5 = slot.isF5();
            notifyPropertyChangeListener(Throttle.F5, Boolean.valueOf(temp), Boolean.valueOf(slot.isF5()));
        }
        if (this.f6 != slot.isF6()) {
            temp = this.f6;
            this.f6 = slot.isF6();
            notifyPropertyChangeListener(Throttle.F6, Boolean.valueOf(temp), Boolean.valueOf(slot.isF6()));
        }
        if (this.f7 != slot.isF7()) {
            temp = this.f7;
            this.f7 = slot.isF7();
            notifyPropertyChangeListener(Throttle.F7, Boolean.valueOf(temp), Boolean.valueOf(slot.isF7()));
        }
        if (this.f8 != slot.isF8()) {
            temp = this.f8;
            this.f8 = slot.isF8();
            notifyPropertyChangeListener(Throttle.F8, Boolean.valueOf(temp), Boolean.valueOf(slot.isF8()));
        }

        // extended slot
        if (this.f9 != slot.isF9()) {
            temp = this.f9;
            this.f9 = slot.isF9();
            notifyPropertyChangeListener(Throttle.F9, Boolean.valueOf(temp), Boolean.valueOf(slot.isF9()));
        }
        if (this.f10 != slot.isF10()) {
            temp = this.f10;
            this.f10 = slot.isF10();
            notifyPropertyChangeListener(Throttle.F10, Boolean.valueOf(temp), Boolean.valueOf(slot.isF10()));
        }
        if (this.f11 != slot.isF11()) {
            temp = this.f11;
            this.f11 = slot.isF11();
            notifyPropertyChangeListener(Throttle.F11, Boolean.valueOf(temp), Boolean.valueOf(slot.isF11()));
        }
        if (this.f12 != slot.isF12()) {
            temp = this.f12;
            this.f12 = slot.isF12();
            notifyPropertyChangeListener(Throttle.F12, Boolean.valueOf(temp), Boolean.valueOf(slot.isF12()));
        }
        if (this.f13 != slot.isF13()) {
            temp = this.f13;
            this.f13 = slot.isF13();
            notifyPropertyChangeListener(Throttle.F13, Boolean.valueOf(temp), Boolean.valueOf(slot.isF13()));
        }
        if (this.f14 != slot.isF14()) {
            temp = this.f14;
            this.f14 = slot.isF14();
            notifyPropertyChangeListener(Throttle.F14, Boolean.valueOf(temp), Boolean.valueOf(slot.isF14()));
        }
        if (this.f15 != slot.isF15()) {
            temp = this.f15;
            this.f15 = slot.isF15();
            notifyPropertyChangeListener(Throttle.F15, Boolean.valueOf(temp), Boolean.valueOf(slot.isF15()));
        }
        if (this.f16 != slot.isF16()) {
            temp = this.f16;
            this.f16 = slot.isF16();
            notifyPropertyChangeListener(Throttle.F16, Boolean.valueOf(temp), Boolean.valueOf(slot.isF16()));
        }
        if (this.f17 != slot.isF17()) {
            temp = this.f17;
            this.f17 = slot.isF17();
            notifyPropertyChangeListener(Throttle.F17, Boolean.valueOf(temp), Boolean.valueOf(slot.isF17()));
        }
        if (this.f18 != slot.isF18()) {
            temp = this.f18;
            this.f18 = slot.isF18();
            notifyPropertyChangeListener(Throttle.F18, Boolean.valueOf(temp), Boolean.valueOf(slot.isF18()));
        }
        if (this.f19 != slot.isF19()) {
            temp = this.f19;
            this.f19 = slot.isF19();
            notifyPropertyChangeListener(Throttle.F19, Boolean.valueOf(temp), Boolean.valueOf(slot.isF19()));
        }
        if (this.f20 != slot.isF20()) {
            temp = this.f20;
            this.f20 = slot.isF20();
            notifyPropertyChangeListener(Throttle.F20, Boolean.valueOf(temp), Boolean.valueOf(slot.isF20()));
        }
        if (this.f21 != slot.isF21()) {
            temp = this.f21;
            this.f21 = slot.isF21();
            notifyPropertyChangeListener(Throttle.F21, Boolean.valueOf(temp), Boolean.valueOf(slot.isF21()));
        }
        if (this.f22 != slot.isF22()) {
            temp = this.f22;
            this.f22 = slot.isF22();
            notifyPropertyChangeListener(Throttle.F22, Boolean.valueOf(temp), Boolean.valueOf(slot.isF22()));
        }
        if (this.f23 != slot.isF23()) {
            temp = this.f23;
            this.f23 = slot.isF23();
            notifyPropertyChangeListener(Throttle.F23, Boolean.valueOf(temp), Boolean.valueOf(slot.isF23()));
        }
        if (this.f24 != slot.isF24()) {
            temp = this.f24;
            this.f24 = slot.isF24();
            notifyPropertyChangeListener(Throttle.F24, Boolean.valueOf(temp), Boolean.valueOf(slot.isF24()));
        }
        if (this.f25 != slot.isF25()) {
            temp = this.f25;
            this.f25 = slot.isF25();
            notifyPropertyChangeListener(Throttle.F25, Boolean.valueOf(temp), Boolean.valueOf(slot.isF25()));
        }
        if (this.f26 != slot.isF26()) {
            temp = this.f26;
            this.f26 = slot.isF26();
            notifyPropertyChangeListener(Throttle.F26, Boolean.valueOf(temp), Boolean.valueOf(slot.isF26()));
        }
        if (this.f27 != slot.isF27()) {
            temp = this.f27;
            this.f27 = slot.isF27();
            notifyPropertyChangeListener(Throttle.F27, Boolean.valueOf(temp), Boolean.valueOf(slot.isF27()));
        }
        if (this.f28 != slot.isF28()) {
            temp = this.f28;
            this.f28 = slot.isF28();
            notifyPropertyChangeListener(Throttle.F28, Boolean.valueOf(temp), Boolean.valueOf(slot.isF28()));
        }
    }

    /**
     * Set the speed step value and the related speedIncrement value.
     *
     * @param Mode the current speed step mode - default should be 128
     *             speed step mode in most cases
     */
    @Override
    public void setSpeedStepMode(int Mode) {
        int status = slot.slotStatus();
        if (log.isDebugEnabled()) {
            log.debug("Speed Step Mode Change to Mode: " + Mode // NOI18N
                    + " Current mode is: " + this.speedStepMode); // NOI18N
            log.debug("Current Slot Mode: " + LnConstants.DEC_MODE(status)); // NOI18N
        }
        if (speedStepMode != Mode) {
            notifyPropertyChangeListener("SpeedSteps", this.speedStepMode, // NOI18N
                    this.speedStepMode = Mode);
        }
        if (Mode == DccThrottle.SpeedStepMode14) {
            speedIncrement = SPEED_STEP_14_INCREMENT;
            log.debug("14 speed step change"); // NOI18N
            status = status & ((~LnConstants.DEC_MODE_MASK)
                    | LnConstants.STAT1_SL_SPDEX)
                    | LnConstants.DEC_MODE_14;
        } else if (Mode == DccThrottle.SpeedStepMode28Mot) {
            speedIncrement = SPEED_STEP_28_INCREMENT;
            log.debug("28-Tristate speed step change");
            status = status & ((~LnConstants.DEC_MODE_MASK)
                    | LnConstants.STAT1_SL_SPDEX)
                    | LnConstants.DEC_MODE_28TRI;
        } else if (Mode == DccThrottle.SpeedStepMode28) {
            speedIncrement = SPEED_STEP_28_INCREMENT;
            log.debug("28 speed step change");
            status = status & ((~LnConstants.DEC_MODE_MASK)
                    | LnConstants.STAT1_SL_SPDEX);
            // | LnConstants.DEC_MODE_28;      // DEC_MODE_28 has a zero value, here for documentation
            // it unfortunately shows a INT_VACUOUS_BIT_OPERATION in SpotBugs
            // and I don't want to annote that around this entire long method
        } else { // default to 128 speed step mode
            speedIncrement = SPEED_STEP_128_INCREMENT;
            log.debug("128 speed step change");
            status = status & ((~LnConstants.DEC_MODE_MASK)
                    | LnConstants.STAT1_SL_SPDEX)
                    | LnConstants.DEC_MODE_128;
        }
        if (log.isDebugEnabled()) {
            log.debug("New Slot Mode: " + LnConstants.DEC_MODE(status));
        }
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
        // set status to common
        if (t instanceof LocoNetThrottle){
            LocoNetThrottle lnt = (LocoNetThrottle) t;
            LocoNetSlot tSlot = lnt.getLocoNetSlot();
            if (tSlot.slotStatus() != LnConstants.LOCO_COMMON) {
                log.debug("dispatchThrottle is writing slot {} status to {}",
                        tSlot,
                        LnConstants.LOCO_COMMON);
                network.sendLocoNetMessage(
                        tSlot.releaseSlot());
            }

            jmri.util.ThreadingUtil.runOnLayoutDelayed( ()-> {
                // and dispatch to slot 0
                    log.debug("dispatchThrottle is dispatching slot {}", tSlot);
                    network.sendLocoNetMessage(tSlot.dispatchSlot());
                },
                32);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LocoNetThrottle.class);

}
