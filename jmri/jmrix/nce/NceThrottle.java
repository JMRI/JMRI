package jmri.jmrix.nce;

import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle with code specific to an NCE connection.
 */
public class NceThrottle extends AbstractThrottle
{
    private float speedSetting;
    private float speedIncrement;
    private int address;
    private boolean isForward;
    private boolean f0, f1, f2, f3, f4, f5, f6, f7, f8;

    /**
     * Constructor
     */
    public NceThrottle()
    {
        // cache settings
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
        this.address      = 0;
        this.isForward    = true;

    }


    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4
     */
    private void sendLowerFunctions() {
        int bytes = 0x80 |
                    (getF0() ? 0x10 : 0) |
                    (getF1() ? 0x01 : 0) |
                    (getF2() ? 0x02 : 0) |
                    (getF3() ? 0x04 : 0) |
                    (getF4() ? 0x08 : 0);
    }

    /**
     * Send the message to set the state of
     * functions F5, F6, F7, F8
     */
    private void sendHigherFunctions() {
        int bytes =  0xB0 |
                    (getF8() ? 0x08 : 0) |
                    (getF7() ? 0x04 : 0) |
                    (getF6() ? 0x02 : 0) |
                    (getF5() ? 0x01 : 0);
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

        NceMessage m = new NceMessage();
        int i = 0;  // message index counter
        m.setElement(i++, 'Q');
        m.setElement(i++, ' ');
        m.setElement(i++, 'C');
        m.setElement(i++, '0');
        m.setElement(i++, '5');
        m.setElement(i++, ' ');

        m.addIntAsTwoHex(0,i);
        i = i+2;
        NceTrafficController.instance().sendNceMessage(m, null);
    }

    public void setIsForward(boolean forward) {
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
    }

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    public void setF0(boolean f0) {
        this.f0 = f0;
        sendLowerFunctions();
    }

    public void setF1(boolean f1) {
        this.f1 = f1;
        sendLowerFunctions();
    }

    public void setF2(boolean f2) {
        this.f2 = f2;
        sendLowerFunctions();
    }

    public void setF3(boolean f3) {
        this.f3 = f3;
        sendLowerFunctions();
    }

    public void setF4(boolean f4) {
        this.f4 = f4;
        sendLowerFunctions();
    }

    public void setF5(boolean f5) {
        this.f5 = f5;
        sendHigherFunctions();
    }

    public void setF6(boolean f6) {
        this.f6 = f6;
        sendHigherFunctions();
    }

    public void setF7(boolean f7) {
        this.f7 = f7;
        sendHigherFunctions();
    }

    public void setF8(boolean f8) {
        this.f8 = f8;
        sendHigherFunctions();
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