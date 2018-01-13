package jmri.jmrix.loconet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nullable;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle via AbstractThrottle with code specific to a
 * LocoNet connection.
 * <P>
 * Speed in the Throttle interfaces and AbstractThrottle is a float, but in
 * LocoNet is an int with values from 0 to 127.
 * <P>
 * @author Glen Oberhauser, Bob Jacobsen Copyright (C) 2003, 2004
 * @author Stephen Williams Copyright (C) 2008
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
     * @param memo connection details
     *
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
     * @param lSpeed LocoNet style speed value
     * @return speed as float 0-&gt;1.0
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

    @Override
    protected void sendFunctionGroup3() {
        // LocoNet practice is to send F9-F12 as a DCC packet
        byte[] result = jmri.NmraPacket.function9Through12Packet(address, (address >= 100),
                getF9(), getF10(), getF11(), getF12());

        log.debug("sendFunctionGroup3 sending {} to LocoNet slot {}", result, slot.getSlot());
        ((jmri.CommandStation) adapterMemo.get(jmri.CommandStation.class)).sendPacket(result, 4); // repeat = 4
    }

    @Override
    protected void sendFunctionGroup4() {
        // LocoNet practice is to send F13-F20 as a DCC packet
        byte[] result = jmri.NmraPacket.function13Through20Packet(address, (address >= 100),
                getF13(), getF14(), getF15(), getF16(),
                getF17(), getF18(), getF19(), getF20());

        log.debug("sendFunctionGroup4 sending {} to LocoNet slot {}", result, slot.getSlot());
        ((jmri.CommandStation) adapterMemo.get(jmri.CommandStation.class)).sendPacket(result, 4); // repeat = 4
    }

    @Override
    protected void sendFunctionGroup5() {
        // LocoNet practice is to send F21-F28 as a DCC packet
        byte[] result = jmri.NmraPacket.function21Through28Packet(address, (address >= 100),
                getF21(), getF22(), getF23(), getF24(),
                getF25(), getF26(), getF27(), getF28());

        log.debug("sendFunctionGroup5 sending {} to LocoNet slot {}", result, slot.getSlot());
        ((jmri.CommandStation) adapterMemo.get(jmri.CommandStation.class)).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Set the speed.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public void setSpeedSetting(float speed) {
        log.debug("setSpeedSetting: sending speed {} to LocoNet slot {}", speed, slot.getSlot());
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
        if (new_spd != layout_spd) {
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_LOCO_SPD);
            msg.setElement(1, slot.getSlot());
            log.debug("setSpeedSetting: float speed: " + speed + " LocoNet speed: " + new_spd);
            msg.setElement(2, new_spd);
            network.sendLocoNetMessage(msg);
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
     * LocoNet actually puts forward and backward in the same message as the
     * first function group.
     */
    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        log.debug("setIsForward to {}, old value {}", isForward, old);
        sendFunctionGroup1();
        if (old != this.isForward) {
            notifyPropertyChangeListener("IsForward", old, this.isForward); // NOI18N
        }
    }

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
     * Dispose when finished with this object. After this, further usage of this
     * Throttle object will result in a JmriException.
     */
    @Override
    protected void throttleDispose() {
        if (isDisposing) return;
        log.debug("throttleDispose - disposing of throttle (and setting slot = null)");
        isDisposing = true;
        
        // stop timeout
        if (mRefreshTimer != null) {
            mRefreshTimer.stop();
            log.debug("Stopped refresh timer for slot {} address {} as part of throttleDispose", slot.getSlot(), slot.locoAddr());
        }

        // release connections
        if (slot != null) {
            // TODO: stopping a slot upon release is a SUBTRACTIVE change - is it justified?
            setSpeedSetting(0); // stop the loco (if it is not already stopped).
            log.debug("Stopping loco address {} slot {} during dispose", slot.locoAddr(), slot.getSlot());
            network.sendLocoNetMessage(slot.releaseSlot());  // a blind release, since the slot listener is being removed, we cannot get any reply message.
            slot.removeSlotListener(this);
            slot.notifySlotListeners();
            log.debug("Releasing loco address {} slot {} during dispose", slot.locoAddr(), slot.getSlot());
        }

        mRefreshTimer = null;
        slot = null;
        network = null;

        finishRecord();
        isDisposing = false;
    }

    javax.swing.Timer mRefreshTimer = null;

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
     * get notified when underlying slot acquisition process fails
     */
    public void notifyRefused(int addr, String s) {
        // don't do anything here; is handled by LnThrottleManager.
        return;
    }

    
    /**
     * Get notified when underlying slot information changes
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
     * setSpeedStepMode - set the speed step value and the related
     *                    speedIncrement value.
     * <P>
     * specific implementations should override this function
     * <P>
     * @param Mode - the current speed step mode - default should be 128
     *              speed step mode in most cases
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
            // it unfortunately shows a INT_VACUOUS_BIT_OPERATION in Findbugs
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

    @Override
    public LocoAddress getLocoAddress() {
        if (slot != null) {
            if (slot.slotStatus() == LnConstants.LOCO_IN_USE) {
                switch (slot.consistStatus()) {
                    case LnConstants.CONSIST_NO:
                    case LnConstants.CONSIST_TOP:
                        log.debug("getLocoAddress replying address {} for slot {}", address, slot.getSlot());
                        return new DccLocoAddress(address, LnThrottleManager.isLongAddress(address));
                    default:
                        break;
                    }
            }
        }
        log.debug("getLocoAddress replying address {} for slot not in-use or for sub-consisted slot or for null slot", address);
        return new DccLocoAddress(address, LnThrottleManager.isLongAddress(65536));
    }
    
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LocoNetThrottle.class);

}
