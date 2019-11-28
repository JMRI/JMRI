package jmri.jmrix.loconet;

import java.util.EnumSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.CheckForNull;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.Throttle;
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

	// for LocoNet throttles, the default is f2 momentary (for the horn)
	// all other functions are continuos (as set in AbstractThrottle).
        this.f2Momentary = true;

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

        // perform the null slot move
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_MOVE_SLOTS);
        msg.setElement(1, slot.getSlot());
        msg.setElement(2, slot.getSlot());
        network.sendLocoNetMessage(msg);

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

    /**
     * Send the LocoNet message to set the state of locomotive direction and
     * functions F0, F1, F2, F3, F4
     */
    @Override
    protected void sendFunctionGroup1() {
        int new_dirf = ((getIsForward() ? 0 : LnConstants.DIRF_DIR)
                | (getF0() ? LnConstants.DIRF_F0 : 0)
                | (getF1() ? LnConstants.DIRF_F1 : 0)
                | (getF2() ? LnConstants.DIRF_F2 : 0)
                | (getF3() ? LnConstants.DIRF_F3 : 0)
                | (getF4() ? LnConstants.DIRF_F4 : 0));
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
        int new_snd = ((getF8() ? LnConstants.SND_F8 : 0)
                | (getF7() ? LnConstants.SND_F7 : 0)
                | (getF6() ? LnConstants.SND_F6 : 0)
                | (getF5() ? LnConstants.SND_F5 : 0));
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
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_LOCO_SPD);
            msg.setElement(1, slot.getSlot());
            log.debug("setSpeedSetting: float speed: " + speed + " LocoNet speed: " + new_spd);
            msg.setElement(2, new_spd);
            network.sendLocoNetMessage(msg);
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
        if (oldSpeed != this.speedSetting) {
            notifyPropertyChangeListener(SPEEDSETTING, oldSpeed, this.speedSetting); // NOI18N
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
        sendFunctionGroup1();
        if (old != this.isForward) {
            notifyPropertyChangeListener(ISFORWARD, old, this.isForward); // NOI18N
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
            Float newSpeed = Float.valueOf(floatSpeed(slot.speed()));
            log.debug("notifyChangedSlot: old speed: " + this.speedSetting + " new Speed: " + newSpeed); // NOI18N
            notifyPropertyChangeListener(SPEEDSETTING, Float.valueOf(this.speedSetting), newSpeed); // NOI18N
            this.speedSetting = newSpeed.floatValue();
        }

        boolean temp;
        if (this.isForward != slot.isForward()) {
            temp = this.isForward;
            this.isForward = slot.isForward();
            notifyPropertyChangeListener(ISFORWARD, Boolean.valueOf(temp), Boolean.valueOf(slot.isForward())); // NOI18N
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
    public void setSpeedStepMode(SpeedStepMode Mode) {
        int status = slot.slotStatus();
        if (log.isDebugEnabled()) {
            log.debug("Speed Step Mode Change to Mode: " + Mode // NOI18N
                    + " Current mode is: " + this.speedStepMode); // NOI18N
            log.debug("Current Slot Mode: " + LnConstants.DEC_MODE(status)); // NOI18N
        }
        if (speedStepMode != Mode) {
            notifyPropertyChangeListener(SPEEDSTEPS, this.speedStepMode, // NOI18N
                    this.speedStepMode = Mode);
        }
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
