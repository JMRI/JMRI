package jmri.jmrix.easydcc;

import jmri.LocoAddress;
import jmri.DccLocoAddress;

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
 * @author	Bob Jacobsen  Copyright (C) 2001, modified 2004 by Kelly Loyd
 * @version     $Revision: 1.3 $
 */
public class EasyDccThrottle extends AbstractThrottle
{
    /**
     * Constructor.
     */
    public EasyDccThrottle(DccLocoAddress address)
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
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    protected void sendFunctionGroup1() {
        byte[] result = jmri.NmraPacket.function0Through4Packet(address.getNumber(), 
                                         address.isLongAddress(),
                                         getF0(), getF1(), getF2(), getF3(), getF4());

        /* Format of EasyDcc 'send' command 
         * S nn xx yy
         * nn = number of times to send - usually 01 is sufficient.
         * xx = Cx for 4 digit or 00 for 2 digit addresses
         * yy = LSB of address for 4 digit, or just 2 digit address
         */
        EasyDccMessage m = new EasyDccMessage(4+3*result.length);
        int i = 0;  // message index counter
        m.setElement(i++, 'S');
        m.setElement(i++, ' ');
        m.setElement(i++, '0');
        m.setElement(i++, '1');

        for (int j = 0; j<result.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[j]&0xFF,i);
            i = i+2;
        }

        EasyDccTrafficController.instance().sendEasyDccMessage(m, null);
    }

    /**
     * Send the message to set the state of
     * functions F5, F6, F7, F8.
     */
    protected void sendFunctionGroup2() {

        byte[] result = jmri.NmraPacket.function5Through8Packet(address.getNumber(), 
                                         address.isLongAddress(),
                                         getF5(), getF6(), getF7(), getF8());

        EasyDccMessage m = new EasyDccMessage(4+3*result.length);
        int i = 0;  // message index counter
        m.setElement(i++, 'S');
        m.setElement(i++, ' ');
        m.setElement(i++, '0');
        m.setElement(i++, '1');

        for (int j = 0; j<result.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[j]&0xFF,i);
            i = i+2;
        }

        EasyDccTrafficController.instance().sendEasyDccMessage(m, null);
    }

    /**
     * Send the message to set the state of
     * functions F9, F10, F11, F12.
     */
    protected void sendFunctionGroup3() {

        byte[] result = jmri.NmraPacket.function9Through12Packet(address.getNumber(), 
                                         address.isLongAddress(),
                                         getF9(), getF10(), getF11(), getF12());

        EasyDccMessage m = new EasyDccMessage(4+3*result.length);
        int i = 0;  // message index counter
        m.setElement(i++, 'S');
        m.setElement(i++, ' ');
        m.setElement(i++, '0');
        m.setElement(i++, '1');

        for (int j = 0; j<result.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[j]&0xFF,i);
            i = i+2;
        }

        EasyDccTrafficController.instance().sendEasyDccMessage(m, null);
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

        byte[] result = jmri.NmraPacket.speedStep128Packet(address.getNumber(), 
                                         address.isLongAddress(), value, isForward);

        EasyDccMessage m = new EasyDccMessage(1+3*result.length);
        // for EasyDCC, sending a speed command involves:
        // Q place in Queue
        // Cx xx (address)
        // yy (speed)
        int i = 0;  // message index counter
        m.setElement(i++, 'Q');

        for (int j = 0; j<result.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[j]&0xFF,i);
            i = i+2;
        }

        EasyDccTrafficController.instance().sendEasyDccMessage(m, null);
    }

    public void setIsForward(boolean forward) {
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
    }

    private DccLocoAddress address;
    
    /**
     * Finished with this throttle.  Right now, this does nothing,
     * but it could set the speed to zero, turn off functions, etc.
     */
    public void release() {
        if (!active) log.warn("release called when not active");
        // KSL 20040409
        // We still need some method to determine when the command station
        // has processed each command, if we stack commands in code, then
        // the second one is lost.
        // accordingly, I have commented out setting the speed to 0 
        // before releasing the locomotive.
        // setSpeedSetting(0);
        int value = 0;
 
        byte[] result = jmri.NmraPacket.speedStep128Packet(address.getNumber(), 
                                         address.isLongAddress(), value, isForward);
    	// KSL 20040409 - this is messy, as I only wanted 
    	// the address to be sent. 
    	EasyDccMessage m = new EasyDccMessage(7);
        // for EasyDCC, release the loco.
        // D = Dequeue
        // Cx xx (address)
        int i = 0;  // message index counter
        m.setElement(i++, 'D');

        if (address.isLongAddress()) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[0]&0xFF,i);
            i = i+2;
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[1]&0xFF,i);
            i = i+2;

        } else { // short address
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(0,i);
            i = i+2;
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[0]&0xFF,i);
            i = i+2;
        }
        
        EasyDccTrafficController.instance().sendEasyDccMessage(m, null);
        
        // KSL 20040409 - 'Releasing' the loco address should not
        // dispose of the throttle, as we might call up another loco
        // and reuse this throttle.
        // dispose();
    }

    /**
     * Dispose when finished with this object.  After this, further usage of
     * this Throttle object will result in a JmriException.
     */
    public void dispose() {
        log.debug("dispose");
        // if this object has registered any listeners, remove those.
        // KSL 20040409 - make sure we release the loco before disposing.
        release();
        
        super.dispose();
    }

    public LocoAddress getLocoAddress() {
        return address;
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccThrottle.class.getName());

}
