package jmri.jmrix.loconet;

import org.apache.log4j.Logger;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.Throttle;
import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle via AbstractThrottle with code specific
 * to a LocoNet connection.
 * <P>
 * Speed in the Throttle interfaces and AbstractThrottle is a float, but in LocoNet is an int
 * with values from 0 to 127.
 * <P>
 * @author  Glen Oberhauser, Bob Jacobsen  Copyright (C) 2003, 2004
 * @author  Stephen Williams  Copyright (C) 2008
 * @version $Revision$
 */
public class LocoNetThrottle extends AbstractThrottle implements SlotListener {
    private LocoNetSlot slot;
    private LocoNetInterface network;
    private int address;

    // members to record the last known spd/dirf/snd bytes AS READ FROM THE LAYOUT!!
    private int layout_spd;
    private int layout_dirf;
    private int layout_snd;
    
    // slot status to be warned if slot released or dispatched
    private int slotStatus;

    /**
     * Constructor
     * @param slot The LocoNetSlot this throttle will talk on.
     */
    public LocoNetThrottle(LocoNetSystemConnectionMemo memo, LocoNetSlot slot) {
        super(memo);
        this.slot = slot;
        network = memo.getLnTrafficController();
        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_MOVE_SLOTS);
        msg.setElement(1, slot.getSlot());
        msg.setElement(2, slot.getSlot());
        network.sendLocoNetMessage(msg);

        // save last known layout state for spd/dirf/snd so we can
        // avoid race condition if another LocoNet process queries
        // our slot while we are in the act of changing it.
        layout_spd  = slot.speed();
        layout_dirf = slot.dirf();
        layout_snd  = slot.snd();


        // cache settings
        this.speedSetting = floatSpeed(slot.speed());
        this.f0           = slot.isF0();
        this.f1           = slot.isF1();
        this.f2           = slot.isF2();
        this.f3           = slot.isF3();
        this.f4           = slot.isF4();
        this.f5           = slot.isF5();
        this.f6           = slot.isF6();
        this.f7           = slot.isF7();
        this.f8           = slot.isF8();

        // extended values
        this.f8           = slot.isF8();
        this.f9           = slot.isF9();
        this.f10          = slot.isF10();
        this.f11          = slot.isF11();
        this.f12          = slot.isF12();
        this.f13          = slot.isF13();
        this.f14          = slot.isF14();
        this.f15          = slot.isF15();
        this.f16          = slot.isF16();
        this.f17          = slot.isF17();
        this.f18          = slot.isF18();
        this.f19          = slot.isF19();
        this.f20          = slot.isF20();
        this.f21          = slot.isF21();
        this.f22          = slot.isF22();
        this.f23          = slot.isF23();
        this.f24          = slot.isF24();
        this.f25          = slot.isF25();
        this.f26          = slot.isF26();
        this.f27          = slot.isF27();
        this.f28          = slot.isF28();

        this.address      = slot.locoAddr();
        this.isForward    = slot.isForward();
        this.slotStatus   = slot.slotStatus();

        switch(slot.decoderType())
        {
            case LnConstants.DEC_MODE_128:
            case LnConstants.DEC_MODE_128A: 
            	setSpeedStepMode( DccThrottle.SpeedStepMode128 ); 
            	break;
            case LnConstants.DEC_MODE_28:
            case LnConstants.DEC_MODE_28A:
            case LnConstants.DEC_MODE_28TRI: 
            	setSpeedStepMode( DccThrottle.SpeedStepMode28 );
            	break;
            case LnConstants.DEC_MODE_14: 
            	setSpeedStepMode( DccThrottle.SpeedStepMode14 );
            	break;
        }

        // listen for changes
        slot.addSlotListener(this);

        // start periodically sending the speed, to keep this
        // attached
        startRefresh();

    }


    /**
     * Convert a LocoNet speed integer to a float speed value
     */
    protected float floatSpeed(int lSpeed) {
        if (lSpeed == 0) return 0.f;
        else if (lSpeed == 1) return -1.f;   // estop
        else return ( (lSpeed-1)/126.f);
    }

    /**
     * Convert a float speed value to a LocoNet speed integer
     */
    protected int intSpeed(float fSpeed) {
      if (fSpeed == 0.f)
        return 0;
      else if (fSpeed < 0.f)
        return 1;   // estop
        // add the 0.5 to handle float to int round for positive numbers
      return (int)(fSpeed * 126.f + 0.5) + 1 ;
    }

    /**
     * Send the LocoNet message to set the state of locomotive
     * direction and functions F0, F1, F2, F3, F4
     */
    protected void sendFunctionGroup1()
    {
        int new_dirf = ((getIsForward() ? 0 : LnConstants.DIRF_DIR) |
                        (getF0() ? LnConstants.DIRF_F0 : 0) |
                        (getF1() ? LnConstants.DIRF_F1 : 0) |
                        (getF2() ? LnConstants.DIRF_F2 : 0) |
                        (getF3() ? LnConstants.DIRF_F3 : 0) |
                        (getF4() ? LnConstants.DIRF_F4 : 0));
        if (new_dirf != layout_dirf) {
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_LOCO_DIRF);
            msg.setElement(1, slot.getSlot());
            msg.setElement(2, new_dirf);
            network.sendLocoNetMessage(msg);
        }
    }

    /**
     * Send the LocoNet message to set the state of
     * functions F5, F6, F7, F8
     */
    protected void sendFunctionGroup2()
    {
        int new_snd = ((getF8() ? LnConstants.SND_F8 : 0) |
                       (getF7() ? LnConstants.SND_F7 : 0) |
                       (getF6() ? LnConstants.SND_F6 : 0) |
                       (getF5() ? LnConstants.SND_F5 : 0));
        if (new_snd != layout_snd) {
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_LOCO_SND);
            msg.setElement(1, slot.getSlot());
            msg.setElement(2, new_snd);
            network.sendLocoNetMessage(msg);
        }
    }

    protected void sendFunctionGroup3() {
        byte[] result = jmri.NmraPacket.function9Through12Packet(address, (address>=100),
                                         getF9(), getF10(), getF11(), getF12());

        ((jmri.CommandStation)adapterMemo.get(jmri.CommandStation.class)).sendPacket(result, 4); // repeat = 4
    }

    /**
     * Set the speed.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    public void setSpeedSetting(float speed)
    {
        if( LnConstants.CONSIST_MID==slot.consistStatus()||
           LnConstants.CONSIST_SUB==slot.consistStatus() )
        {
           // Digitrax slots use the same memory location to store the
           // speed AND the slot to which a locomotive is consisted.
           // if the locomotive is either a CONSIST_MID or a CONSIST_SUB,
           // we need to ignore the request to change the speed
           if(log.isDebugEnabled()) log.debug("Attempt to change speed on locomoitve " + getLocoAddress() + " which is a " + LnConstants.CONSIST_STAT(slot.consistStatus())); 
           return;
        }
    	float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        if (speed<0) this.speedSetting = -1.f;

        int new_spd = intSpeed( speed );
        if (new_spd != layout_spd) {
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_LOCO_SPD);
            msg.setElement(1, slot.getSlot());
            log.debug( "setSpeedSetting: float speed: " + speed + " LocoNet speed: " + new_spd );
            msg.setElement(2, new_spd);
            network.sendLocoNetMessage(msg);
        }
        
        // reset timeout
        if (mRefreshTimer != null) { // got NullPointerException sometimes
        	mRefreshTimer.stop();
        	mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
        	mRefreshTimer.start();
        }
        if (oldSpeed != this.speedSetting)
        	notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting );
        record(speed);
    }

    /**
     * LocoNet actually puts forward and backward in the same message
     * as the first function group.
     */
    public void setIsForward(boolean forward)
    {
    	boolean old = isForward;
        isForward = forward;
        sendFunctionGroup1();
        if (old != this.isForward)
        	notifyPropertyChangeListener("IsForward", old, this.isForward);
    }

    public LocoNetSlot getLocoNetSlot(){
        return slot;
    }

    public String toString() {
        return getLocoAddress().toString();
    }
    
    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    protected void throttleDispose() {
        // stop timeout
        if (mRefreshTimer != null)
        	mRefreshTimer.stop();

        // release connections
        if (slot != null)
        	slot.removeSlotListener(this);

        mRefreshTimer = null;
        slot = null;
        network = null;
        
        finishRecord();
     }

    javax.swing.Timer mRefreshTimer = null;

    protected void startRefresh() {
        mRefreshTimer = new javax.swing.Timer(50000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                timeout();
            }
        });
        mRefreshTimer.setRepeats(true);     // refresh until stopped by dispose
        mRefreshTimer.start();
    }

    /**
     * Internal routine to resend the speed on a timeout
     */
    synchronized protected void timeout() {
    	// clear the last known layout_spd so that we will actually send the
    	// message.
    	layout_spd = -1;
    	setSpeedSetting(speedSetting);
    }

    /**
     * Get notified when underlying slot information changes
     */    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    public void notifyChangedSlot(LocoNetSlot pSlot) {
        if (slot!=pSlot) log.error("notified of change in different slot");

        // Save current layout state of spd/dirf/snd so we won't run amok
        // toggling values if another LocoNet entity accesses the slot while
        // our most recent change request is still in-flight.
        layout_spd  = slot.speed();
        layout_dirf = slot.dirf();
        layout_snd  = slot.snd();
        
        // handle change in each state
        if (this.speedSetting != floatSpeed(slot.speed())) {
          Float newSpeed = new Float( floatSpeed(slot.speed() ) ) ;
          log.debug( "notifyChangedSlot: old speed: " + this.speedSetting + " new Speed: " + newSpeed );
          notifyPropertyChangeListener("SpeedSetting", new Float(this.speedSetting), newSpeed );
          this.speedSetting = newSpeed.floatValue() ;
        }

        boolean temp;
        if (this.isForward != slot.isForward()) {
            temp = this.isForward;
            this.isForward = slot.isForward();
            notifyPropertyChangeListener("IsForward", Boolean.valueOf(temp), Boolean.valueOf(slot.isForward()));
        }
        
        // Slot status        
        if (slotStatus != slot.slotStatus()) {
        	int newStat = slot.slotStatus();
        	if (log.isDebugEnabled())
        		log.debug("Slot status changed from "+LnConstants.LOCO_STAT(slotStatus)+" to "+LnConstants.LOCO_STAT(newStat) );
        	// PropertyChangeListeners notification: ThrottleConnected from True to False when disconnected
        	notifyPropertyChangeListener("ThrottleConnected", (slotStatus & LnConstants.LOCOSTAT_MASK) == LnConstants.LOCO_IN_USE,
        														! ( (slotStatus & LnConstants.LOCOSTAT_MASK) == LnConstants.LOCO_IN_USE) );
        	slotStatus = newStat;
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

    /*
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
        int status=slot.slotStatus();
        if(log.isDebugEnabled()) {
             log.debug("Speed Step Mode Change to Mode: " + Mode +
                " Current mode is: " + this.speedStepMode);
                log.debug("Current Slot Mode: " +LnConstants.DEC_MODE(status));
        }
        if(speedStepMode!=Mode)
                notifyPropertyChangeListener("SpeedSteps", this.speedStepMode,
                                              this.speedStepMode=Mode );
        if(Mode==DccThrottle.SpeedStepMode14) {
                speedIncrement=SPEED_STEP_14_INCREMENT;
                log.debug("14 speed step change");
                status=status&((~LnConstants.DEC_MODE_MASK)|
                                LnConstants.STAT1_SL_SPDEX)
                             | LnConstants.DEC_MODE_14;
        }
        else if(Mode==DccThrottle.SpeedStepMode28Mot) {
                speedIncrement=SPEED_STEP_28_INCREMENT;
                log.debug("28-Tristate speed step change");
                status=status&((~LnConstants.DEC_MODE_MASK)|
                                LnConstants.STAT1_SL_SPDEX)
                             | LnConstants.DEC_MODE_28TRI;
             }
        else if(Mode==DccThrottle.SpeedStepMode28) {
                speedIncrement=SPEED_STEP_28_INCREMENT;
                log.debug("28 speed step change");
                status=status&((~LnConstants.DEC_MODE_MASK)|
                                LnConstants.STAT1_SL_SPDEX)
                             | LnConstants.DEC_MODE_28;         // DEC_MODE_28 has a zero value, here for documentation
                                                                // but it unfortunately shows a INT_VACUOUS_BIT_OPERATION
                                                                // in Findbugs
             }
        else { // default to 128 speed step mode
                speedIncrement=SPEED_STEP_128_INCREMENT;
                log.debug("128 speed step change");
                status=status&((~LnConstants.DEC_MODE_MASK)|
                                LnConstants.STAT1_SL_SPDEX)
                             | LnConstants.DEC_MODE_128;
             }
        if(log.isDebugEnabled()) 
              log.debug("New Slot Mode: " +LnConstants.DEC_MODE(status));
        if(mRefreshTimer!=null) // the refresh timer isn't created until
                                // after initilization.  We only want to
                                // modify the slot after the initilization 
                                // is complete.
           network.sendLocoNetMessage(slot.writeMode(status));
    }



    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, LnThrottleManager.isLongAddress(address));
    }

    // initialize logging
    static Logger log = Logger.getLogger(LocoNetThrottle.class.getName());

}
