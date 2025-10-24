package jmri.jmrix.tmcc;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle.
 * <p>
 * Addresses of 99 and below are considered short addresses, and over 100 are
 * considered long addresses.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006
 */
public class SerialThrottle extends AbstractThrottle {

    /**
     * Constructor.
     *
     * @param memo the connected SerialTrafficController
     * @param address Loco ID
     */
    public SerialThrottle(TmccSystemConnectionMemo memo, DccLocoAddress address) {
        super(memo, 69); // supports 69 functions
        tc = memo.getTrafficController();

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
        synchronized(this) {
            this.speedSetting = 0;
        }
        // Functions default to false
        this.address = address;
        this.isForward = true;
        this.speedStepMode = SpeedStepMode.TMCC1_32;
    }

    private final DccLocoAddress address;
    private final SerialTrafficController tc;

    /**
     * {@inheritDoc}
     */
    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    // Relating SERIAL_FUNCTION_CODES_TMCC1 to SpeedStepMode.TMCC1_32 and TMCC1_100;
    //    and SERIAL_FUNCTION_CODES_TMCC2 to SpeedStepMode.TMCC2_32 and TMCC2_200.
    private long getFnValue(int number) {
                if (getSpeedStepMode() == jmri.SpeedStepMode.TMCC1_32 || getSpeedStepMode() == jmri.SpeedStepMode.TMCC1_100) {
                    if (number < SERIAL_FUNCTION_CODES_TMCC1.length) {
                        return SERIAL_FUNCTION_CODES_TMCC1[number];
                    } else {
                        return 0;
                    }
                } else if (getSpeedStepMode() == jmri.SpeedStepMode.TMCC2_32 || getSpeedStepMode() == jmri.SpeedStepMode.TMCC2_200) {
                     if (number < SERIAL_FUNCTION_CODES_TMCC2.length) {
                         return SERIAL_FUNCTION_CODES_TMCC2[number];
                    } else {
                        return 0;
                    }
                }
            return 0;
        }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setFunction(int func, boolean value) {
        updateFunction(func, value);
        if (func>=0 && func < SERIAL_FUNCTION_CODES_TMCC1.length) {
            if ( getFnValue(func) > 0xFFFF ) {
                // TMCC 2 format
                if (getFnValue(func) > 0xFFFFFF ) {
                    int first =  (int)(getFnValue(func) >> 24);
                    int second = (int)(getFnValue(func) & 0xFFFFFF);
                    // doubles are only sent once, not repeating
                    sendOneWordOnce(first  + address.getNumber() * 512);
                    sendOneWordOnce(second + address.getNumber() * 512);
                } else {
                    // single message
                    sendFnToLayout((int)getFnValue(func) + address.getNumber() * 512, func);
                    }
            } else {
                // TMCC 1 format
                sendFnToLayout((int)getFnValue(func) + address.getNumber() * 128, func);
                }
        } else {
            super.setFunction(func, value);
            }
    }

    // the argument is a long containing 3 bytes. 
    // The first byte is the message opcode
    private void sendOneWordOnce(int word) {
        SerialMessage m = new SerialMessage(word);
        tc.sendSerialMessage(m, null);
    }

    // TMCC 1 Function Keys to trigger with TMCC1_32 and TMCC1_100 speed steps.
    private final static long[] SERIAL_FUNCTION_CODES_TMCC1 = new long[] {
        0x00000D, 0x00001D, 0x00001C, 0x000005, 0x000006, /* Fn0-4 */
        0x000010, 0x000011, 0x000012, 0x000013, 0x000014, /* Fn5-9 */
        0x000015, 0x000016, 0x000017, 0x000018, 0x000019, /* Fn10-14 */
        0x000009, 0x00001E, 0x000000, 0x000003, 0x000001, /* Fn15-19 */
        0x000004, 0x000007, 0x000047, 0x000042, 0x000028, /* Fn20-24 */
        0x000029, 0x00002A, 0x00002B, 0x00001F, /* 25-28 */
        
        0x00002E, // Fn29
        0x00002E, // Fn30
        0x00002E, // Fn31
        0x00002E, // Fn32
        0x00002E, // Fn33
        0x00002E, // Fn34
        0x00002E, // Fn35
        0x00002E, // Fn36
        0x00002E, // Fn37
        0x00002E, // Fn38
        0x00002E, // Fn39
        0x00002E, // Fn40
        0x00002E, // Fn41
        0x00002E, // Fn42
        0x00002E, // Fn43
        0x00002E, // Fn44
        0x00002E, // Fn45
        0x00002E, // Fn46
        0x00002E, // Fn47
        0x00002E, // Fn48
        0x00002E, // Fn49
        0x00002E, // Fn50
        0x00002E, // Fn51
        0x00002E, // Fn52
        0x00002E, // Fn53
        0x00002E, // Fn54
        0x00002E, // Fn55
        0x00002E, // Fn56
        0x00002E, // Fn57
        0x00002E, // Fn58
        0x00002E, // Fn59
        0x00002E, // Fn60
        0x00002E, // Fn61
        0x00002E, // Fn62
        0x00002E, // Fn63
        0x00002E, // Fn64
        0x00002E, // Fn65
        0x00002E, // Fn66
        0x00002E, // Fn67
        0x00002E, // Fn68
    };

    // Translate TMCC1 function numbers to line characters.
    // If the upper byte is zero, it will be replaced by 0xF8
    //    and the address will be set in the low position.
    // If the upper byte is non-zero, that value will be sent,
    //    and the address will be set in the upper (TMCC2) position.
    // If six bytes are specified (with the upper one non-zero), 
    //    this will be interpreted as two commands to be sequentially sent,
    //    with the upper bytes sent first.

    // TMCC 2 Legacy Function Keys to trigger with TMCC2_32 and TMCC2_200 speed steps.
    private final static long[] SERIAL_FUNCTION_CODES_TMCC2 = new long[] {
        0xF8010D, 0xF8011D, 0xF8011C, 0xF80105, 0xF80106, /* Fn0-4 */
        0xF80110, 0xF80111, 0xF80112, 0xF80113, 0xF80114, /* Fn5-9 */
        0xF80115, 0xF80116, 0xF80117, 0xF80118, 0xF80119, /* Fn10-14 */
        0xF80109, 0xF8011E, 0xF80100, 0xF80103, 0xF80101, /* Fn15-19 */
        0xF80104, 0xF80107, 0xF80147, 0xF80142, 0xF80128, /* Fn20-24 */
        0xF80129, 0xF8012A, 0xF8012B, 0xF8011F, /* 25-28 */
        
        0xF80108, // Fn29
        0xF8010A, // Fn30
        0xF8010B, // Fn31
        0xF8010C, // Fn32
        0xF8010E, // Fn33
        0xF8010F, // Fn34
        
        //0xF801FBF801FCL, // Fn35 Start Up Sequence 1 (Delayed Prime Mover, then Immediate Start Up)
        //0xF801FC, // Fn36 Start Up Sequence 2 (Immediate Start Up)
        //0xF801FDF801FEL, // Fn37 Shut Down Sequence 1 (Delay w/ Announcement then Immediate Shut Down)
        //0xF801FE, // Fn38 Shut down Sequence 2 (Immediate Shut Down)

        0xF8012E, // Fn35
        0xF8012E, // Fn36
        0xF8012E, // Fn37
        0xF8012E, // Fn38
        0xF8012E, // Fn39
        0xF8012E, // Fn40
        0xF8012E, // Fn41
        0xF8012E, // Fn42
        0xF8012E, // Fn43
        0xF8012E, // Fn44
        0xF8012E, // Fn45
        0xF8012E, // Fn46
        0xF8012E, // Fn47
        0xF8012E, // Fn48
        0xF8012E, // Fn49
        0xF8012E, // Fn50
        0xF8012E, // Fn51
        0xF8012E, // Fn52
        0xF8012E, // Fn53
        0xF8012E, // Fn54
        0xF8012E, // Fn55
        0xF8012E, // Fn56
        0xF8012E, // Fn57
        0xF8012E, // Fn58
        0xF8012E, // Fn59
        0xF8012E, // Fn60
        0xF8012E, // Fn61
        0xF8012E, // Fn62
        0xF8012E, // Fn63
        0xF8012E, // Fn64
        0xF8012E, // Fn65
        0xF8012E, // Fn66
        0xF8012E, // Fn67
        0xF8012E, // Fn68
    };

    /**
     * Set the speed.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @Override
    public void setSpeedSetting(float speed) {
        float oldSpeed;
        synchronized(this) {
            oldSpeed = this.speedSetting;
            this.speedSetting = speed;
        }
        
        // send to layout option 200 speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC2_200) {

            // TMCC2 Legacy 200 speed step mode
            int value = (int) (199 * speed); // max value to send is 199 in 200 step mode
            if (value > 199) {
                // max possible speed
                value = 199;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xF8);
    
            if (value < 1) {
                // immediate stop
                m.putAsWord(0x0000 + (address.getNumber() << 9) + 0);
            } else {
                // normal speed setting
                m.putAsWord(0x0000 + (address.getNumber() << 9) + value);
            }
            // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
            tc.sendSerialMessage(m, null);
            tc.sendSerialMessage(m, null);
        }

        // send to layout option 100 speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC1_100) {
            
          /** 
            * TMCC1 ERR 100 speed step mode
            * purpose is to increase resolution of 32 bits
            * across 100 throttle 'clicks' by dividing value by 3            
            * and setting top speed at 32
          */
            int value = (int) (99 * speed); // max value to send is 99 in 100 step mode
            if (value > 93) {
                // max possible speed step
                value = 93;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xFE);

            if (value < 1) {
                // immediate stop
                m.putAsWord(0x0060 + address.getNumber() * 128 + 0);
            }
            if (value > 0) {
                // normal speed step setting
                m.putAsWord(0x0060 + address.getNumber() * 128 + value / 3);
            }
                            
            // send to command station (send once for maximum efficiency; add extra sends if layout not responding with only one send)
            tc.sendSerialMessage(m, null);
        }

        // send to layout option TMCC2 32 speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC2_32) {

            // TMCC2 Legacy 32 speed step mode
            int value = (int) (32 * speed);
            if (value > 31) {
                // max possible speed
                value = 31;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xF8);
    
            if (value < 1) {
                // immediate stop
                m.putAsWord(0x0160 + address.getNumber() * 512 + 0);
            } else {
                // normal speed setting
                m.putAsWord(0x0160 + address.getNumber() * 512 + value);
            }
    
            // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
            tc.sendSerialMessage(m, null);
            tc.sendSerialMessage(m, null);           
        }

        // send to layout option TMCC1 32 speed steps
        if (speedStepMode == jmri.SpeedStepMode.TMCC1_32) {

            // TMCC1 32 speed step mode
            int value = (int) (32 * speed);
            if (value > 31) {
                // max possible speed
                value = 31;
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xFE);
    
            if (value < 1) {
                // immediate stop
                m.putAsWord(0x0060 + address.getNumber() * 128 + 0);
            } else {
                // normal speed setting
                m.putAsWord(0x0060 + address.getNumber() * 128 + value);
            }
    
            // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
            tc.sendSerialMessage(m, null);
            tc.sendSerialMessage(m, null);           
        }
                  
        synchronized(this) {
            firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        }
        record(speed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;

        // notify layout
        SerialMessage m = new SerialMessage();
        if (forward) {
            m.putAsWord(0x0000 + address.getNumber() * 128);
        } else {
            m.putAsWord(0x0003 + address.getNumber() * 128);
        }
        tc.sendSerialMessage(m, null);
        tc.sendSerialMessage(m, null);
        tc.sendSerialMessage(m, null);
        tc.sendSerialMessage(m, null);
        firePropertyChange(ISFORWARD, old, isForward);
    }

    /**
     * Send these messages to the layout and repeat
     * while button is on.
     * @param value Content of message to be sent in three bytes
     * @param func  The number of the function being addressed
     */
    protected void sendFnToLayout(int value, int func) {
    /**
     * Commenting out these repeat send lines in case it is
     * necessary to reinstate them after testing. These are
     * holdovers from the original "repeat 4 times to make
     * sure they're accepted" instructions.
     */
        // tc.sendSerialMessage(new SerialMessage(value), null);
        // tc.sendSerialMessage(new SerialMessage(value), null);
        // tc.sendSerialMessage(new SerialMessage(value), null);     
    
        repeatFunctionSendWhileOn(value, func); // 4th send is here
    }

    static final int REPEAT_TIME = 150;

    protected void repeatFunctionSendWhileOn(int value, int func) {
        // Send again if function is still on and repeat in a short while
        if (getFunction(func)) {
            tc.sendSerialMessage(new SerialMessage(value), null);
            jmri.util.ThreadingUtil.runOnLayoutDelayed(() -> {
                repeatFunctionSendWhileOn(value, func);
            }, REPEAT_TIME);
        }
    }

    /*
     * Set the speed step value.
     * <p>
     * Only 32 steps is available
     *
     * @param mode only TMCC1 32, TMCC2 32, TMCC1 100 and TMCC2 200 are allowed
     */
    @Override
    public void setSpeedStepMode(jmri.SpeedStepMode mode) {
        if (mode == jmri.SpeedStepMode.TMCC1_32 || mode == jmri.SpeedStepMode.TMCC2_32 || mode == jmri.SpeedStepMode.TMCC1_100 || mode == jmri.SpeedStepMode.TMCC2_200) {
            super.setSpeedStepMode(mode);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void throttleDispose() {
        finishRecord();
    }

}
