package jmri.jmrix.srcp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.Throttle;
import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle with code specific to an SRCP connection.
 * <p>
 * Addresses of 99 and below are considered short addresses, and over 100 are
 * considered long addresses. This is not the NCE system standard, but is used
 * as an expedient here.
 *
 * @author	Bob Jacobsen Copyright (C) 2001,2008
 */
public class SRCPThrottle extends AbstractThrottle {

    /**
     * Constructor.
     *
     * @param memo    the memo containing the connection
     * @param address the address to use
     */
    public SRCPThrottle(SRCPBusConnectionMemo memo, DccLocoAddress address) {
        // default to 128 speed steps with 28 functions and NMRA protocl.
        this(memo, address, "N", SpeedStepMode.NMRA_DCC_128, 28);
    }

    public SRCPThrottle(SRCPBusConnectionMemo memo, DccLocoAddress address,
            String protocol, SpeedStepMode mode, int functions) {
        super(memo);
        if (!protocol.equals("N")) {
            throw new IllegalArgumentException("Protocol " + protocol + " not supported");
        }
        setSpeedStepMode(mode);

        bus = memo.getBus();

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
        this.speedSetting = 0;
        this.f0 = false;
        this.f1 = false;
        this.f2 = false;
        this.f3 = false;
        this.f4 = false;
        this.f5 = false;
        this.f6 = false;
        this.f7 = false;
        this.f8 = false;
        this.f9 = false;
        this.f10 = false;
        this.f11 = false;
        this.f12 = false;
        this.f13 = false;
        this.f14 = false;
        this.f15 = false;
        this.f16 = false;
        this.f17 = false;
        this.f18 = false;
        this.f19 = false;
        this.f20 = false;
        this.f21 = false;
        this.f22 = false;
        this.f23 = false;
        this.f24 = false;
        this.f26 = false;
        this.f27 = false;
        this.f28 = false;
        this.address = address;
        this.isForward = true;

        // send allocation message
        String msg = "INIT " + bus + " GL "
                + (address.getNumber())
                + " " + protocol + " "
                + (address.isLongAddress() ? " 2 " : " 1 ")
                + maxsteps + " "
                + functions + "\n";
        memo.getTrafficController()
                .sendSRCPMessage(new SRCPMessage(msg), null);
    }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    @Override
    protected void sendFunctionGroup1() {
        sendUpdate();
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {
        sendUpdate();
    }

    /**
     * Send the message to set the state of functions F9, F10, F11, F12.
     */
    @Override
    protected void sendFunctionGroup3() {
        sendUpdate();
    }

    /**
     * Send the message to set the state of functions F13, F14, F15, F16, F17,
     * F18, F19, and F20.
     */
    @Override
    protected void sendFunctionGroup4() {
        sendUpdate();
    }

    /**
     * Send the message to set the state of functions F21, F22, F23, F24, F25,
     * F26, F27 and F28.
     */
    @Override
    protected void sendFunctionGroup5() {
        sendUpdate();
    }

    /**
     * Set the speed {@literal &} direction.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        sendUpdate();
        if (oldSpeed != this.speedSetting) {
            notifyPropertyChangeListener(SPEEDSETTING, oldSpeed, this.speedSetting);
        }
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        sendUpdate();
        if (old != isForward) {
            notifyPropertyChangeListener(Throttle.ISFORWARD, old, isForward);
        }
    }

    private DccLocoAddress address;
    private int bus;
    private int maxsteps;

    /**
     * Send the complete status
     */
    void sendUpdate() {
        String msg = "SET " + bus + " GL ";

        // address
        msg += (address.getNumber());

        // direction and speed
        msg += (isForward ? " 1" : " 0");
        msg += " " + ((int) (speedSetting * maxsteps));
        msg += " ";
        msg += maxsteps;

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
        SRCPMessage m = new SRCPMessage(msg + "\n");

        ((SRCPBusConnectionMemo) adapterMemo).getTrafficController().sendSRCPMessage(m, null);
    }

    @Override	
    public void setSpeedStepMode(SpeedStepMode Mode) {	
        super.setSpeedStepMode(Mode);	
        switch (Mode) {	
            case NMRA_DCC_14:	
            case NMRA_DCC_27:	
            case NMRA_DCC_28:	
            case NMRA_DCC_128:
                maxsteps = Mode.numSteps;
                break;	
            default:	
                maxsteps = 126;	
                break;
        }	
    }

    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    @Override
    protected void throttleDispose() {
        finishRecord();
    }

}
