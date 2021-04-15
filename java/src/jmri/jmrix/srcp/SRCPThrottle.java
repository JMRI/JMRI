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
 * @author Bob Jacobsen Copyright (C) 2001,2008
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
        synchronized(this) {
            this.speedSetting = 0;
        }
        // Functions default to false
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
     * Set the speed and direction.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public synchronized void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        sendUpdate();
        firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        record(speed);
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        sendUpdate();
        firePropertyChange(Throttle.ISFORWARD, old, isForward);
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
        synchronized(this) {
            msg += " " + ((int) (speedSetting * maxsteps));
        }
        msg += " ";
        msg += maxsteps;

        // now add the functions
        msg += getFunction(0) ? " 1" : " 0";
        msg += getFunction(1) ? " 1" : " 0";
        msg += getFunction(2) ? " 1" : " 0";
        msg += getFunction(3) ? " 1" : " 0";
        msg += getFunction(4) ? " 1" : " 0";
        msg += getFunction(5) ? " 1" : " 0";
        msg += getFunction(6) ? " 1" : " 0";
        msg += getFunction(7) ? " 1" : " 0";
        msg += getFunction(8) ? " 1" : " 0";
        msg += getFunction(9) ? " 1" : " 0";
        msg += getFunction(10) ? " 1" : " 0";
        msg += getFunction(11) ? " 1" : " 0";
        msg += getFunction(12) ? " 1" : " 0";
        msg += getFunction(13) ? " 1" : " 0";
        msg += getFunction(14) ? " 1" : " 0";
        msg += getFunction(15) ? " 1" : " 0";
        msg += getFunction(16) ? " 1" : " 0";
        msg += getFunction(17) ? " 1" : " 0";
        msg += getFunction(18) ? " 1" : " 0";
        msg += getFunction(19) ? " 1" : " 0";
        msg += getFunction(20) ? " 1" : " 0";
        msg += getFunction(21) ? " 1" : " 0";
        msg += getFunction(22) ? " 1" : " 0";
        msg += getFunction(23) ? " 1" : " 0";
        msg += getFunction(24) ? " 1" : " 0";
        msg += getFunction(25) ? " 1" : " 0";
        msg += getFunction(26) ? " 1" : " 0";
        msg += getFunction(27) ? " 1" : " 0";
        msg += getFunction(28) ? " 1" : " 0";

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
    public void throttleDispose() {
        finishRecord();
    }

}
