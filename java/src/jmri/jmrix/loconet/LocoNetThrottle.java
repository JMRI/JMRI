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

    // with extended slots the slots may not have been updated by the echo
    // before the next message needs sending.So we must save and send what
    // we believe to be the correct speed and direction.
    // remember in expanded mode 2 throttle cannot be in control of a loco

    protected int new_spd;
    protected long new_spd_lastupdated;
    protected boolean new_isFwd;
    protected long new_isFwd_lastupdated;

    // slot status to be warned if slot released or dispatched
    protected int slotStatus;
    protected boolean isDisposing = false;

    /**
     * Constructor
     *
     * @param memo connection details
     * @param slot The LocoNetSlot this throttle will talk on.
     */
    public LocoNetThrottle(LocoNetSystemConnectionMemo memo, LocoNetSlot slot) {
        super(memo, 69); // supports up to F68
        this.slot = slot;
        slot.setIsInitialized(false);
        network = memo.getLnTrafficController();
        throttleManager = (LnThrottleManager)memo.getThrottleManager();

        // save last known layout state for spd/dirf/snd so we can
        // avoid race condition if another LocoNet process queries
        // our slot while we are in the act of changing it.
        layout_spd = slot.speed();
        layout_dirf = slot.dirf();
        layout_snd = slot.snd();

        // cache settings
        synchronized(this) {
            this.speedSetting = floatSpeed(slot.speed());
        }
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
            case NMRA_DCC_128:
                return speed;
            default:
                log.warn("Unhandled speed step: {}", this.getSpeedStepMode());
                break;
        }
        return speed;
    }

    /**
     * Constants to represent Function Groups.
     * <p>
     * The are the same groupings for both normal Functions and Momentary.
     */
    private static final int[] EXP_FUNCTION_GROUPS = new int[]{
            1, 1, 1, 1, 1, 1, 1, /** 0-6 */
            2, 2, 2, 2, 2, 2, 2, /** 7 - 13 */
            3, 3, 3, 3, 3, 3, 3, /** 14 -20 */
            4, 4, 4, 4, 4, 4, 4, 4, /** 21 - 28 */
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, // 29 - 69
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, // 29 - 69
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, // 29 - 69
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, // 29 - 69
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5  // 29 - 69
    };

    /**
     * Send whole (DCC) Function Group for a particular function number.
     * @param functionNum Function Number
     * @param momentary False to send normal function status, true to send momentary.
     */
    @Override
    protected void sendFunctionGroup(int functionNum, boolean momentary){
        if (slot.getProtocol() != LnConstants.LOCONETPROTOCOL_TWO) {
            super.sendFunctionGroup(functionNum, momentary);
            return;
        }
        switch (EXP_FUNCTION_GROUPS[functionNum]) {
            case 1:
                if (momentary) sendMomentaryFunctionGroup1(); else sendExpFunctionGroup1();
                break;
            case 2:
                if (momentary) sendMomentaryFunctionGroup2(); else sendExpFunctionGroup2();
                break;
            case 3:
                if (momentary) sendMomentaryFunctionGroup3(); else sendExpFunctionGroup3();
                break;
            case 4:
                if (momentary) sendMomentaryFunctionGroup4(); else sendExpFunctionGroup4();
                break;
            case 5:
                // send as regular function operations
                super.sendFunctionGroup(functionNum, momentary);
                break;
            default:
                break;
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
        int new_dirf = ((getIsForward() ? 0 : LnConstants.DIRF_DIR)
                | (getFunction(0) ? LnConstants.DIRF_F0 : 0)
                | (getFunction(1) ? LnConstants.DIRF_F1 : 0)
                | (getFunction(2) ? LnConstants.DIRF_F2 : 0)
                | (getFunction(3) ? LnConstants.DIRF_F3 : 0)
                | (getFunction(4) ? LnConstants.DIRF_F4 : 0));
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
        int new_snd = ((getFunction(8) ? LnConstants.SND_F8 : 0)
                | (getFunction(7) ? LnConstants.SND_F7 : 0)
                | (getFunction(6) ? LnConstants.SND_F6 : 0)
                | (getFunction(5) ? LnConstants.SND_F5 : 0));
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
                getFunction(9), getFunction(10), getFunction(11), getFunction(12));

        log.debug("sendFunctionGroup3 sending {} to LocoNet slot {}", result, slot.getSlot());
        adapterMemo.get(jmri.CommandStation.class).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Sends Function Group 4 values - F13 thru F20, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     */
    @Override
    protected void sendFunctionGroup4() {
        // LocoNet practice is to send F13-F20 as a DCC packet
        byte[] result = jmri.NmraPacket.function13Through20Packet(address, (address >= 128),
                getFunction(13), getFunction(14), getFunction(15), getFunction(16),
                getFunction(17), getFunction(18), getFunction(19), getFunction(20));

        log.debug("sendFunctionGroup4 sending {} to LocoNet slot {}", result, slot.getSlot());
        adapterMemo.get(jmri.CommandStation.class).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Sends Function Group 5 values - F21 thru F28, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     */
    @Override
    protected void sendFunctionGroup5() {
        // LocoNet practice is to send F21-F28 as a DCC packet
        byte[] result = jmri.NmraPacket.function21Through28Packet(address, (address >= 128),
                getFunction(21), getFunction(22), getFunction(23), getFunction(24),
                getFunction(25), getFunction(26), getFunction(27), getFunction(28));

        log.debug("sendFunctionGroup5 sending {} to LocoNet slot {}", result, slot.getSlot());
        adapterMemo.get(jmri.CommandStation.class).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Sends Function Group 6 values - F29 thru F36, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     */
    @Override
    protected void sendFunctionGroup6() {
        // LocoNet practice is to send as a DCC packet
        int i = 29;
        byte[] result = jmri.NmraPacket.function29Through36Packet(address, (address >= 128),
                getFunction(i), getFunction(i+1), getFunction(i+2), getFunction(i+3),
                getFunction(i+4), getFunction(i+5), getFunction(i+6), getFunction(i+7));

        log.debug("sendFunctionGroup6 sending {} to LocoNet slot {}", result, slot.getSlot());
        adapterMemo.get(jmri.CommandStation.class).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Sends Function Group 7 values - F37 thru F44, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     */
    @Override
    protected void sendFunctionGroup7() {
        // LocoNet practice is to send as a DCC packet
        int i = 37;
        byte[] result = jmri.NmraPacket.function37Through44Packet(address, (address >= 128),
                getFunction(i), getFunction(i+1), getFunction(i+2), getFunction(i+3),
                getFunction(i+4), getFunction(i+5), getFunction(i+6), getFunction(i+7));

        log.debug("sendFunctionGroup7 sending {} to LocoNet slot {}", result, slot.getSlot());
        adapterMemo.get(jmri.CommandStation.class).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Sends Function Group 8 values - F45 thru F52, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     */
    @Override
    protected void sendFunctionGroup8() {
        // LocoNet practice is to send as a DCC packet
        int i = 45;
        byte[] result = jmri.NmraPacket.function45Through52Packet(address, (address >= 128),
                getFunction(i), getFunction(i+1), getFunction(i+2), getFunction(i+3),
                getFunction(i+4), getFunction(i+5), getFunction(i+6), getFunction(i+7));

        log.debug("sendFunctionGroup8 sending {} to LocoNet slot {}", result, slot.getSlot());
        adapterMemo.get(jmri.CommandStation.class).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Sends Function Group 9 values - F53 thru F60, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     */
    @Override
    protected void sendFunctionGroup9() {
        // LocoNet practice is to send as a DCC packet
        int i = 53;
        byte[] result = jmri.NmraPacket.function53Through60Packet(address, (address >= 128),
                getFunction(i), getFunction(i+1), getFunction(i+2), getFunction(i+3),
                getFunction(i+4), getFunction(i+5), getFunction(i+6), getFunction(i+7));

        log.debug("sendFunctionGroup9 sending {} to LocoNet slot {}", result, slot.getSlot());
        adapterMemo.get(jmri.CommandStation.class).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Sends Function Group 10 values - F61 thru F68, using an "OPC_IMM_PACKET" LocoNet
     * Message.
     */
    @Override
    protected void sendFunctionGroup10() {
        // LocoNet practice is to send as a DCC packet
        int i = 61;
        byte[] result = jmri.NmraPacket.function61Through68Packet(address, (address >= 128),
                getFunction(i), getFunction(i+1), getFunction(i+2), getFunction(i+3),
                getFunction(i+4), getFunction(i+5), getFunction(i+6), getFunction(i+7));

        log.debug("sendFunctionGroup10 sending {} to LocoNet slot {}", result, slot.getSlot());
        adapterMemo.get(jmri.CommandStation.class).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Send the Expanded LocoNet message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4, F5, F6
     */
    protected void sendExpFunctionGroup1() {
            int new_F0F6 = ((getFunction(5) ? 0b00100000 : 0) | (getFunction(6) ? 0b01000000 : 0)
                | (getFunction(0) ? LnConstants.DIRF_F0 : 0)
                | (getFunction(1) ? LnConstants.DIRF_F1 : 0)
                | (getFunction(2) ? LnConstants.DIRF_F2 : 0)
                | (getFunction(3) ? LnConstants.DIRF_F3 : 0)
                | (getFunction(4) ? LnConstants.DIRF_F4 : 0));
            LocoNetMessage msg = new LocoNetMessage(6);
            msg.setOpCode(LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR);
            msg.setElement(1, (slot.getSlot() / 128) | LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6 );
            msg.setElement(2,slot.getSlot() & 0b01111111);
            msg.setElement(3,slot.id() & 0x7F);
            msg.setElement(4, new_F0F6);
            network.sendLocoNetMessage(msg);
    }

    /**
     * Send the Expanded LocoNet message to set the state of functions F7, F8, F8, F9, F10, F11, F12, F13
     */
    protected void sendExpFunctionGroup2() {
            int new_F7F13 = ((getFunction(7) ? 0b00000001 : 0) | (getFunction(8) ? 0b00000010 : 0)
                    | (getFunction(9)  ? 0b00000100 : 0)
                    | (getFunction(10) ? 0b00001000 : 0)
                    | (getFunction(11) ? 0b00010000 : 0)
                    | (getFunction(12) ? 0b00100000 : 0)
                    | (getFunction(13) ? 0b01000000 : 0));
                LocoNetMessage msg = new LocoNetMessage(6);
                msg.setOpCode(LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR);
                msg.setElement(1, (slot.getSlot() / 128) | LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13 );
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
        int new_F14F20 = ((getFunction(14) ? 0b00000001 : 0) | (getFunction(15) ? 0b00000010 : 0)
                | (getFunction(16)  ? 0b00000100 : 0)
                | (getFunction(17) ? 0b00001000 : 0)
                | (getFunction(18) ? 0b00010000 : 0)
                | (getFunction(19) ? 0b00100000 : 0)
                | (getFunction(20) ? 0b01000000 : 0));
            LocoNetMessage msg = new LocoNetMessage(6);
            msg.setOpCode(LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR);
            msg.setElement(1, (slot.getSlot() / 128) | LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20 );
            msg.setElement(2,slot.getSlot() & 0b01111111);
            msg.setElement(3,slot.id() & 0x7F);
            msg.setElement(4, new_F14F20);
            network.sendLocoNetMessage(msg);
    }

    /**
     * Sends Expanded loconet message F21 thru F28 Message.
     */
    protected void sendExpFunctionGroup4() {
        int new_F2128 = ((getFunction(21) ? 0b00000001 : 0) |
                (getFunction(22) ? 0b00000010 : 0) |
                (getFunction(23) ? 0b00000100 : 0) |
                (getFunction(24) ? 0b00001000 : 0) |
                (getFunction(25) ? 0b00010000 : 0) |
                (getFunction(26) ? 0b00100000 : 0) |
                (getFunction(27) ? 0b01000000 : 0));
        LocoNetMessage msg = new LocoNetMessage(6);
        msg.setOpCode(LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR);
        if (!getFunction(28)) {
            msg.setElement(1, (slot.getSlot() / 128) | LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF);
        } else {
            msg.setElement(1, (slot.getSlot() / 128) | LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28ON);
        }
        msg.setElement(2, slot.getSlot() & 0b01111111);
        msg.setElement(3, slot.id() & 0x7F);
        msg.setElement(4, new_F2128);
        network.sendLocoNetMessage(msg);
    }

    /**
     * Send the expanded slot command for speed and direction on change of speed
     * Note we send our stored values as slot is updated via an echo
     * and may not have been updated yet when sending rapid commands
     * @param speed the speed to set
     */
    protected void sendExpSpeedAndDirection(int speed) {
        boolean isFwd;
        if (slot.getLastUpdateTime() <  new_isFwd_lastupdated) {
            isFwd = new_isFwd;
        } else {
            isFwd = slot.isForward();
        }
        // save last speed update for change of direction;
        new_spd = speed;
        new_spd_lastupdated = System.currentTimeMillis();
        LocoNetMessage msg = new LocoNetMessage(6);
        msg.setOpCode(LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR);
        msg.setElement(1, ((slot.getSlot() / 128) & 0x03) | (isFwd ? 0x00 : 0x08));
        msg.setElement(2, slot.getSlot() & 0x7f);
        msg.setElement(3, (slot.id() & 0x7f));
        msg.setElement(4, speed);
        network.sendLocoNetMessage(msg);
    }

    /**
     * Send the expanded slot command for speed and direction on change of direction
     * Note we send our stored speed if slot has not yet been updated by the echo
     * @param isFwd new direction
     */
    protected void sendExpSpeedAndDirection(boolean isFwd) {
        int speed;
        if (slot.getLastUpdateTime() <  new_spd_lastupdated) {
            speed = new_spd;
        } else {
            speed = slot.speed();
        }
        // save last speed update for change of direction;
        new_isFwd = isFwd;
        new_isFwd_lastupdated = System.currentTimeMillis();
        LocoNetMessage msg = new LocoNetMessage(6);
        msg.setOpCode(LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR);
        msg.setElement(1, ((slot.getSlot() / 128) & 0x03) | (isFwd ? 0x00 : 0x08));
        msg.setElement(2, slot.getSlot() & 0x7f);
        msg.setElement(3, (slot.id() & 0x7f));
        msg.setElement(4, speed);
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
    @Override
    public void setSpeedSetting(float speed, boolean allowDuplicates, boolean allowDuplicatesOnStop) {
        log.debug("setSpeedSetting: called with speed {} for LocoNet slot {} allowDup {} allowDupOnStop {}",
                    speed, slot.getSlot(), allowDuplicates, allowDuplicatesOnStop);
        if (LnConstants.CONSIST_MID == slot.consistStatus()
                || LnConstants.CONSIST_SUB == slot.consistStatus()) {
            // Digitrax slots use the same memory location to store the
            // speed AND the slot to which a locomotive is consisted.
            // if the locomotive is either a CONSIST_MID or a CONSIST_SUB,
            // we need to ignore the request to change the speed
            log.debug("Attempt to change speed on locomotive {} which is a {}", getLocoAddress(), LnConstants.CONSIST_STAT(slot.consistStatus()));
            return;
        }
        float oldSpeed;
        synchronized(this) {
            oldSpeed = this.speedSetting;
            this.speedSetting = speed;
            if (speed < 0) {
                this.speedSetting = -1.f;
            }
        }

        new_spd = intSpeed(speed);

        // decide whether to send a new LocoNet message
        boolean sendLoconetMessage = false;
        if (new_spd != layout_spd ) {
            // the new speed is different - send a message
            sendLoconetMessage = true;
        } else if (allowDuplicates) {
            // calling method wants a new message sent regardless
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
                log.debug("setSpeedSetting: float speed: {} LocoNet speed: {}", speed, new_spd);
                msg.setElement(2, new_spd);
                network.sendLocoNetMessage(msg);
            } else {
                sendExpSpeedAndDirection(new_spd);
            }

            // reset timeout - but only if something sent on net
            if (mRefreshTimer != null) {
                mRefreshTimer.stop();
                mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
                mRefreshTimer.start();
                log.debug("Initially starting refresh timer for slot {} address {}", slot.getSlot(), slot.locoAddr());
            }
        } else {
            log.debug("setSpeedSetting: not sending LocoNet speed message to slot {}, new({})==old({})", slot.getSlot(), new_spd, layout_spd);
        }
        synchronized(this) {
            firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        }
        log.debug("about to invoke record({})", speed);
        record(speed);
    }

    /**
     * Send a LocoNet message containing the specified direction of travel.
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
            sendExpSpeedAndDirection(forward);
        }
        firePropertyChange(ISFORWARD, old, this.isForward);
    }

    /**
     * Get the LocoNetSlot which is used for controlling the loco assoicated
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
    public void throttleDispose() {
        if (isDisposing) return;
        log.debug("throttleDispose - disposing of throttle (and setting slot = null)");
        isDisposing = true;

        // Release throttle connections
        if (slot != null) {
            if (slot.slotStatus() == LnConstants.LOCO_IN_USE  ) {
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
     * Start the "refresh" timer.  The "refresh" timer determines
     * when to send a new LocoNet message to "refresh" the slot's speed
     * setting, so that the slot does not get "purged".
     *
     */
    protected void startRefresh() {
        mRefreshTimer = new javax.swing.Timer(50000, e -> timeout());
        mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
        mRefreshTimer.start();
        log.debug("Starting refresh timer for slot {} address {}", slot.getSlot(), slot.locoAddr());
    }

    /**
     * Internal routine to resend the speed on a timeout
     */
    protected synchronized void timeout() {
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
        log.debug("notifyChangedSlot executing for slot {}, slotStatus {}", slot.getSlot(), Integer.toHexString(slot.slotStatus()));
        if (slot != pSlot) {
            log.error("notified of change in different slot");
        }

        if(!slot.getIsInitilized() && slot.slotStatus() == LnConstants.LOCO_IN_USE){
           log.debug("Attempting to update slot with this JMRI instance's throttle id ({})", throttleManager.getThrottleID());
           network.sendLocoNetMessage(slot.writeThrottleID(throttleManager.getThrottleID()));
           // finally we are done...
           slot.setIsInitialized(true);
           throttleManager.notifyComplete(this, slot);
        }

        // Save current layout state of spd/dirf/snd so we won't run amok
        // toggling values if another LocoNet entity accesses the slot while
        // our most recent change request is still in-flight.
        layout_spd = slot.speed();
        layout_dirf = slot.dirf();
        layout_snd = slot.snd();

        // handle change in each state
        synchronized(this) {
            if (this.speedSetting != floatSpeed(slot.speed())) {
                float old = this.speedSetting;
                this.speedSetting = floatSpeed(slot.speed());
                log.debug("notifyChangedSlot: old speed: {} new speed: {}", old, this.speedSetting); // NOI18N
                firePropertyChange(SPEEDSETTING, old, this.speedSetting);
            }
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
        updateFunctions();

        log.debug("notifyChangedSlot ends");
    }

    /**
     * update the F0-F29 functions.
     * Invoked by notifyChangedSlot(), this nominally updates from the slot.
     */
    protected void updateFunctions() {
        for (int i = 0; i < 29; i++) {
            log.debug("updateFunction({}, {})", i, slot.isFunction(i));
            if (i==20 && log.isTraceEnabled()) log.trace("Tracing back F20", new Exception("traceback"));
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
        if (slot.getIsInitilized() )
            // check that the throttle is completely initialized.
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
