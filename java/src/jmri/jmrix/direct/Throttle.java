package jmri.jmrix.direct;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to a direct serial
 * connection.
 * <P>
 * Addresses of 99 and below are considered short addresses, and over 100 are
 * considered long addresses.
 * <P>
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
public class Throttle extends AbstractThrottle {

    private jmri.CommandStation tcl = null;

    /**
     * Constructor.
     */
    public Throttle(int address,jmri.CommandStation tc) {
        super(null);
        tcl = tc;

        // cache settings.
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
        this.address = address;
        this.isForward = true;

    }

    int address;  // store integer value for now, ignoring long/short

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    @Override
    protected void sendFunctionGroup1() {
        byte[] result = jmri.NmraPacket.function0Through4Packet(address, (address >= 100),
                getF0(), getF1(), getF2(), getF3(), getF4());

        tcl.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {

        byte[] result = jmri.NmraPacket.function5Through8Packet(address, (address >= 100),
                getF5(), getF6(), getF7(), getF8());

        tcl.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of functions F9, F10, F11, F12.
     */
    @Override
    protected void sendFunctionGroup3() {

        byte[] result = jmri.NmraPacket.function9Through12Packet(address, (address >= 100),
                getF9(), getF10(), getF11(), getF12());

        tcl.sendPacket(result, 1);
    }

    /**
     * Set the speed {@literal &} direction.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point
    @Override
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        int value = (int) ((127 - 1) * speed);     // -1 for rescale to avoid estop
        if (value > 0) {
            value = value + 1;  // skip estop
        }
        if (value > 127) {
            value = 127;    // max possible speed
        }
        if (value < 0) {
            value = 1;        // emergency stop
        }
        String step = "" + value;

        Message m = new Message(1 + step.length());
        int i = 0;  // message index counter
        if (isForward) {
            m.setElement(i++, '>');
        } else {
            m.setElement(i++, '<');
        }

        for (int j = 0; j < step.length(); j++) {
            m.setElement(i++, step.charAt(j));
        }
        if (oldSpeed != this.speedSetting) {
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting);
        }
        record(speed);
        // tcl.sendMessage(m, null);
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
        if (old != isForward) {
            notifyPropertyChangeListener("IsForward", old, isForward);
        }
    }

    @Override
    public LocoAddress getLocoAddress() {
        //log.error("getLocoAddress not fully implemented yet");
        return new DccLocoAddress(address, address > 100);   // always short address if <100
    }

    @Override
    protected void throttleDispose() {
        finishRecord();
    }

    // initialize logging
    // private final static Logger log = LoggerFactory.getLogger(Throttle.class);

}
