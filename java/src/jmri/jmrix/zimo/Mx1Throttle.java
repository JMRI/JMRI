package jmri.jmrix.zimo;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to an Mx1 connection.
 * <p>
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class Mx1Throttle extends AbstractThrottle implements Mx1Listener {

    private Mx1TrafficController tc = null;
    //private Mx1Interface network;

    /**
     * Create a new throttle.
     *
     * @param memo    the system connection the throttle is associated with
     * @param address the address for the throttle
     */
    public Mx1Throttle(Mx1SystemConnectionMemo memo, DccLocoAddress address) {
        super(memo);
        this.tc = memo.getMx1TrafficController();
        super.speedStepMode = jmri.SpeedStepMode.NMRA_DCC_128;

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
        synchronized(this) {
            this.speedSetting = 0;
        }
        // Functions default to false
        this.address = address;
        this.isForward = true;
        if (address.isLongAddress()) {
            addressLo = address.getNumber();
            addressHi = address.getNumber() >> 8;
            addressHi = addressHi + 0xc0; //We add 0xc0 to the high byte.
        } else {
            addressLo = address.getNumber();
        }
        tc.addMx1Listener(~0, this);
    }

    DccLocoAddress address;

    int addressLo = 0x00;
    int addressHi = 0x00;

    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    @Override
    protected void sendFunctionGroup1() {
        sendSpeedCmd();
        /*int data = 0x00 |
         ( f0 ? 0x10 : 0) |
         ( f1 ? 0x01 : 0) |
         ( f2 ? 0x02 : 0) |
         ( f3 ? 0x04 : 0) |
         ( f4 ? 0x08 : 0);
        
         data = data + 0x80;*/
 /*Mx1Message m = Mx1Message.getSendFunction(1, addressLo, addressHi, data);
         if(m!=null)
         tc.sendMx1Message(m);*/
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {
        sendSpeedCmd();
        // Always need speed command before function group command to reset consist pointer
        /*int data = 0x00 |
         (f8 ? 0x08 : 0) |
         (f7 ? 0x04 : 0) |
         (f6 ? 0x02 : 0) |
         (f5 ? 0x01 : 0);
        
         data = data + 0xB0;*/
    }

    /**
     * Send the message to set the state of functions F9, F12, F11, F12.
     */
    @Override
    protected void sendFunctionGroup3() {
        sendSpeedCmd();
        /*int data = 0x00 |
         ( f9 ? 0x01 : 0) |
         ( f10 ? 0x02 : 0) |
         ( f11 ? 0x04 : 0) |
         ( f12 ? 0x08 : 0);
        
         data = data + 0xA0;*/
    }

    /**
     * Send the message to set the state of functions F13 to F20 in function
     * Group 4 and 5
     */
    @Override
    protected void sendFunctionGroup4() {
        // The NCE USB doesn't support the NMRA packet format
        // Always need speed command before function group command to reset consist pointer
//         int data = 0x00
//                 | (f16 ? 0x08 : 0)
//                 | (f15 ? 0x04 : 0)
//                 | (f14 ? 0x02 : 0)
//                 | (f13 ? 0x01 : 0);
// 
//         data = data + 0xD0;

        /*Mx1Message m = Mx1Message.getSendFunction(4, addressLo, addressHi, data);
         if(m!=null)
         tc.sendMx1Message(m);*/
//         data = 0x00
//                 | (f20 ? 0x08 : 0)
//                 | (f19 ? 0x04 : 0)
//                 | (f18 ? 0x02 : 0)
//                 | (f17 ? 0x01 : 0);
//         data = data + 0xC0;

        /*m = Mx1Message.getSendFunction(5, addressLo, addressHi, data);
         if(m!=null)
         tc.sendMx1Message(m);*/
    }

    /**
     * Send the message to set the state of functions F21 to F28. MRC Group 6
     */
    @Override
    protected void sendFunctionGroup5() {
        /* int data = 0x00 |
         (f28 ? 0x80 : 0) |
         (f27 ? 0x40 : 0) |
         (f26 ? 0x20 : 0) |
         (f25 ? 0x10 : 0) |
         (f24 ? 0x08 : 0) |
         (f23 ? 0x04 : 0) |
         (f22 ? 0x02 : 0) |
         (f21 ? 0x01 : 0); */

 /*Mx1Message m = Mx1Message.getSendFunction(6, addressLo, addressHi, data);
         if(m!=null)
         tc.sendMx1Message(m);   */
    }

    /**
     * Set the speed and direction.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public synchronized void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        sendSpeedCmd();
        firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        record(speed);
    }

    void sendSpeedCmd() {
        Mx1Message m;
        int value = 0;
        int cData1 = (isForward ? 0x20 : 0x00);
        cData1 = cData1 + (getFunction(0) ? 0x10 : 0x00);
        synchronized(this) {
            if (super.speedStepMode == jmri.SpeedStepMode.NMRA_DCC_128) {
                //m = Mx1Message.getSendSpeed128(addressLo, addressHi, value);
                value = (int) ((127 - 1) * speedSetting); // -1 for rescale to avoid estop
                if (value > 0) {
                    value = value + 1;  // skip estop
                }
                if (value > 127) {
                    value = 127;    // max possible speed
                }
                if (value < 0) {
                    value = 1;        // emergency stop
                }
                value = (value & 0x7F);
                cData1 = cData1 + 0xc;
            } else if (super.speedStepMode == jmri.SpeedStepMode.NMRA_DCC_28) {
                value = (int) ((28) * speedSetting); // -1 for rescale to avoid estop
                if (value > 0) {
                    value = value + 3; // skip estop
                }
                if (value > 32) {
                    value = 31; // max possible speed
                }
                if (value < 0) {
                    value = 2; // emergency stop
                }
                int speedC = (value & 0x1F) >> 1;
                int c = (value & 0x01) << 4; // intermediate speed step

                speedC = speedC + c;
                value = (isForward ? 0x60 : 0x40) | speedC;
                cData1 = cData1 + 0x8;
            }
        }
        m = Mx1Message.getLocoControl(address.getNumber(), value, true, cData1, getFunction1to8(), getFunction9to12());
        tc.sendMx1Message(m, this);
    }

    int getFunction1to8() {

        int data = 0x00
                | (getFunction(1) ? 0x01 : 0)
                | (getFunction(2) ? 0x02 : 0)
                | (getFunction(3) ? 0x04 : 0)
                | (getFunction(4) ? 0x08 : 0)
                | (getFunction(5) ? 0x10 : 0)
                | (getFunction(6) ? 0x20 : 0)
                | (getFunction(7) ? 0x40 : 0)
                | (getFunction(8) ? 0x80 : 0);

        return data;
    }

    int getFunction9to12() {
        int data = 0x00
                | (getFunction(9) ? 0x01 : 0)
                | (getFunction(10) ? 0x02 : 0)
                | (getFunction(11) ? 0x04 : 0)
                | (getFunction(12) ? 0x08 : 0);
        return data;
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        synchronized(this) {
            setSpeedSetting(speedSetting);  // send the command
        }
        log.debug("setIsForward= {}", forward);
        firePropertyChange(ISFORWARD, old, isForward);
    }

    @Override
    public void throttleDispose() {
        finishRecord();
    }

    @Override
    public void message(Mx1Message m) {

    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(Mx1Throttle.class);

}
