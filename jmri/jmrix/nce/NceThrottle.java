package jmri.jmrix.nce;

import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle with code specific to an NCE connection.
 * <P>
 * Addresses of 99 and below are considered short addresses, and
 * over 100 are considered long addresses.  This is not the NCE system
 * standard, but is used as an expedient here.
 * <P>
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version     $Revision: 1.4 $
 */
public class NceThrottle extends AbstractThrottle
{
    /**
     * Constructor
     */
    public NceThrottle(int address)
    {
        super();

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

    }


    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4
     */
    protected void sendFunctionGroup1() {
        byte[] result = jmri.NmraPacket.function0Through4Packet(address, (address>=100),
                                         getF0(), getF1(), getF2(), getF3(), getF4());

        NceMessage m = new NceMessage(5+3*result.length);
        int i = 0;  // message index counter
        m.setElement(i++, 'S');
        m.setElement(i++, ' ');
        m.setElement(i++, 'C');
        m.setElement(i++, '0');
        m.setElement(i++, '5');

        for (int j = 0; j<result.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[j]&0xFF,i);
            i = i+2;
        }

        NceTrafficController.instance().sendNceMessage(m, null);
    }

    /**
     * Send the message to set the state of
     * functions F5, F6, F7, F8
     */
    protected void sendFunctionGroup2() {

        byte[] result = jmri.NmraPacket.function5Through8Packet(address, (address>=100),
                                         getF5(), getF6(), getF7(), getF8());

        NceMessage m = new NceMessage(5+3*result.length);
        int i = 0;  // message index counter
        m.setElement(i++, 'S');
        m.setElement(i++, ' ');
        m.setElement(i++, 'C');
        m.setElement(i++, '0');
        m.setElement(i++, '5');

        for (int j = 0; j<result.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[j]&0xFF,i);
            i = i+2;
        }

        NceTrafficController.instance().sendNceMessage(m, null);
    }

    /**
     * Send the message to set the state of
     * functions F9, F10, F11, F12
     */
    protected void sendFunctionGroup3() {

        byte[] result = jmri.NmraPacket.function9Through12Packet(address, (address>=100),
                                         getF9(), getF10(), getF11(), getF12());

        NceMessage m = new NceMessage(5+3*result.length);
        int i = 0;  // message index counter
        m.setElement(i++, 'S');
        m.setElement(i++, ' ');
        m.setElement(i++, 'C');
        m.setElement(i++, '0');
        m.setElement(i++, '5');

        for (int j = 0; j<result.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[j]&0xFF,i);
            i = i+2;
        }

        NceTrafficController.instance().sendNceMessage(m, null);
    }

    /**
     * Set the speed & direction
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

        byte[] result = jmri.NmraPacket.speedStep128Packet(address, (address>=100), value, isForward);

        NceMessage m = new NceMessage(1+3*result.length);
        int i = 0;  // message index counter
        m.setElement(i++, 'Q');

        for (int j = 0; j<result.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[j]&0xFF,i);
            i = i+2;
        }

        NceTrafficController.instance().sendNceMessage(m, null);
    }

    public void setIsForward(boolean forward) {
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
    }

    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     *
     * This is quite problematic, because a using object doesn't know when
     * it's the last user.
     */
    public void dispose() {
        log.debug("dispose");

        // if this object has registered any listeners, remove those.
        super.dispose();
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceThrottle.class.getName());

}