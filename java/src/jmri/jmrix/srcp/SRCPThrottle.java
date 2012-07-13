package jmri.jmrix.srcp;

import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle with code specific to an SRCP connection.
 * <P>
 * Addresses of 99 and below are considered short addresses, and
 * over 100 are considered long addresses.  This is not the NCE system
 * standard, but is used as an expedient here.
 *
 * @author	Bob Jacobsen  Copyright (C) 2001,2008
 * @version     $Revision$
 */
public class SRCPThrottle extends AbstractThrottle
{
    /**
     * Constructor.
     */
    public SRCPThrottle(SRCPSystemConnectionMemo memo, DccLocoAddress address)
    {
        super(memo);
        super.speedStepMode = SpeedStepMode128;

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
        this.speedSetting = 0;
        this.f0           = false;
        this.f1           = false;
        this.f2           = false;
        this.f3           = false;
        this.f4           = false;
        this.f5           = false;
        this.f6           = false;
        this.f7           = false;
        this.f8           = false;
        this.f9           = false;
        this.f10           = false;
        this.f11           = false;
        this.f12           = false;
        this.address      = address;
        this.isForward    = true;

        // send allocation message
        String msg = "INIT 1 GL "
            +(address.getNumber())
            +" N 1 128 5\n";
        memo.getTrafficController()
                .sendSRCPMessage(new SRCPMessage(msg), null);
    }


    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    protected void sendFunctionGroup1() {
        sendUpdate();
    }

    /**
     * Send the message to set the state of
     * functions F5, F6, F7, F8.
     */
    protected void sendFunctionGroup2() {
        sendUpdate();
    }

    /**
     * Send the message to set the state of
     * functions F9, F10, F11, F12.
     */
    protected void sendFunctionGroup3() {
        sendUpdate();
    }

    /**
     * Set the speed & direction.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        sendUpdate();
        if (oldSpeed != this.speedSetting)
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting );
    }

    public void setIsForward(boolean forward) {
        boolean old = isForward; 
        isForward = forward;
        sendUpdate();
        if (old != isForward)
            notifyPropertyChangeListener("IsForward", old, isForward );
    }

    private DccLocoAddress address;
    
    /**
     * Send the complete status
     */
    void sendUpdate() {
        String msg = "SET 1 GL ";
        
        // address
        msg+=(address.getNumber());
        
        // direction and speed
        msg+=(isForward ? " 1" : " 0" );
        msg+=" "+((int)(speedSetting*100));
        msg+=" 100";
        
        // now add the functions
        msg += f0 ? " 1" : " 0";
        msg += f1 ? " 1" : " 0";
        msg += f2 ? " 1" : " 0";
        msg += f3 ? " 1" : " 0";
        msg += f4 ? " 1" : " 0";
        msg += f5 ? " 1" : " 0";
        msg += f6 ? " 1" : " 0";
        msg += f7 ? " 1" : " 0";
        msg += f8 ? " 1" : " 0";
        msg += f9 ? " 1" : " 0";
        msg += f10 ? " 1" : " 0";
        msg += f11 ? " 1" : " 0";
        msg += f12 ? " 1" : " 0";
        msg += f13 ? " 1" : " 0";
        msg += f14 ? " 1" : " 0";
        msg += f15 ? " 1" : " 0";
        msg += f16 ? " 1" : " 0";
        msg += f17 ? " 1" : " 0";
        msg += f18 ? " 1" : " 0";
        msg += f19 ? " 1" : " 0";
        msg += f20 ? " 1" : " 0";
        msg += f21 ? " 1" : " 0";
        msg += f22 ? " 1" : " 0";
        msg += f23 ? " 1" : " 0";
        msg += f24 ? " 1" : " 0";
        msg += f25 ? " 1" : " 0";
        msg += f26 ? " 1" : " 0";
        msg += f27 ? " 1" : " 0";
        msg += f28 ? " 1" : " 0";
        
        // send the result
        SRCPMessage m = new SRCPMessage(msg+"\n");

        ((SRCPSystemConnectionMemo)adapterMemo).getTrafficController().sendSRCPMessage(m, null);
    }

    public LocoAddress getLocoAddress() {
        return address;
    }

    protected void throttleDispose(){ finishRecord(); }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPThrottle.class.getName());

}
