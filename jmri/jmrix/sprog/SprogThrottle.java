package jmri.jmrix.sprog;

import jmri.jmrix.AbstractThrottle;
import jmri.util.StringUtil;

/**
 * An implementation of DccThrottle with code specific to an SPROG connection.
 * <P>
 * Addresses of 99 and below are considered short addresses, and
 * over 100 are considered long addresses.
 * <P>
 * Based on the {@link jmri.jmrix.nce.NceThrottle} implementation.
 *
 * @author	Bob Jacobsen  Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class SprogThrottle extends AbstractThrottle
{
    /**
     * Constructor.
     */
    public SprogThrottle(int address)
    {
        super();

        // cache settings.
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

    }

    SprogCommandStation station = new SprogCommandStation();

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    protected void sendFunctionGroup1() {
        byte[] result = jmri.NmraPacket.function0Through4Packet(address, (address>=100),
                                         getF0(), getF1(), getF2(), getF3(), getF4());

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of
     * functions F5, F6, F7, F8.
     */
    protected void sendFunctionGroup2() {

        byte[] result = jmri.NmraPacket.function5Through8Packet(address, (address>=100),
                                         getF5(), getF6(), getF7(), getF8());

        station.sendPacket(result, 1);
    }

    /**
     * Send the message to set the state of
     * functions F9, F10, F11, F12.
     */
    protected void sendFunctionGroup3() {

        byte[] result = jmri.NmraPacket.function9Through12Packet(address, (address>=100),
                                         getF9(), getF10(), getF11(), getF12());

        station.sendPacket(result, 1);
    }

    /**
     * Set the speed & direction.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    public void setSpeedSetting(float speed) {
        this.speedSetting = speed;
        int value = (int)((127-1)*speed);     // -1 for rescale to avoid estop
        if (value>0) value = value+1;  // skip estop
        if (value>127) value = 127;    // max possible speed
        if (value<0) value = 1;        // emergency stop

        String step = ""+value;

        SprogMessage m = new SprogMessage(1+step.length());
        int i = 0;  // message index counter
        if (isForward) m.setElement(i++, '>');
        else           m.setElement(i++, '<');

        for (int j = 0; j<step.length(); j++) {
            m.setElement(i++, step.charAt(j));
        }

        SprogTrafficController.instance().sendSprogMessage(m, null);
    }

    public void setIsForward(boolean forward) {
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
    }

    /**
     * Finished with this throttle.  Right now, this does nothing
     * except notify the SprogThrottleManager
     * but it could set the speed to zero, turn off functions, etc.
     */
    public void release() {
        if (!active) log.warn("release called when not active");
        SprogThrottleManager.instance().release();
        dispose();
    }

    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    public void dispose() {
        // if this object has registered any listeners, remove those.
        super.dispose();
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogThrottle.class.getName());

}