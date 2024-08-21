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
        this.speedStepMode = SpeedStepMode.TMCC_32;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFunction(int func, boolean value) {
        updateFunction(func, value);
        if (func>=0 && func < SERIAL_FUNCTION_CODES.length) {
            if ( SERIAL_FUNCTION_CODES[func] > 0xFFFF ) {
                // TMCC 2 format
                if (SERIAL_FUNCTION_CODES[func] > 0xFFFFFF ) {
                    int first =  (int)(SERIAL_FUNCTION_CODES[func] >> 24);
                    int second = (int)(SERIAL_FUNCTION_CODES[func] & 0xFFFFFF);
                    // doubles are only sent once, not repeating
                    sendOneWordOnce(first  + address.getNumber() * 512);
                    sendOneWordOnce(second + address.getNumber() * 512);           
                } else {
                    // single message
                    sendFnToLayout((int)SERIAL_FUNCTION_CODES[func] + address.getNumber() * 512, func);
                }
            } else {
                // TMCC 1 format
                sendFnToLayout((int)SERIAL_FUNCTION_CODES[func] + address.getNumber() * 128, func);
            }
        }
        else {
            super.setFunction(func, value);
        }
    }

    // the argument is a long containing 3 bytes. 
    // The first byte is the message opcode
    private void sendOneWordOnce(int word) {
        SerialMessage m = new SerialMessage(word);
        tc.sendSerialMessage(m, null);
    }

    // Translate function number to line characters.
    // If the upper byte is zero, it will be replaces by 0xF8
    //    and the address will be set in the low position.
    // If the upper byte is non-zero, that value will be sent,
    //    and the address will be set in the upper (TMCC2) position.
    //    If six bytes are specified (with the upper one non-zero), 
    //    this will be interpreted as two commands to be sequentially sent,
    //    with the upper bytes sent first.
    private final static long[] SERIAL_FUNCTION_CODES = new long[] {
        0x00000D, 0x00001D, 0x00001C, 0x000005, 0x000006, /* Fn0-4 */
        0x000010, 0x000011, 0x000012, 0x000013, 0x000014, /* Fn5-9 */
        0x000015, 0x000016, 0x000017, 0x000018, 0x000019, /* Fn10-14 */
        0x000009, 0x00001E, 0x000000, 0x000003, 0x000001, /* Fn15-19 */
        0x000004, 0x000007, 0x000047, 0x000042, 0x000028, /* Fn20-24 */
        0x000029, 0x00002A, 0x00002B, /* 25-27 */
        // start of TMCC 2 functions
        0xF801FBF801FCL, // Fn28 Start Up Sequence 1 (Delayed Prime Mover, then Immediate Start Up)
        0xF801FC, // Fn29 Start Up Sequence 2 (Immediate Start Up)
        0xF801FDF801FEL, // Fn30 Shut Down Sequence 1 (Delay w/ Announcement then Immediate Shut Down)
        0xF801FE, // Fn31 Shut down Sequence 2 (Immediate Shut Down)
        0xF90000, // Fn32
        0xF90000, // Fn33
        0xF90000, // Fn34
        0xF90000, // Fn35
        0xF90000, // Fn36
        0xF90000, // Fn37
        0xF90000, // Fn38
        0xF90000, // Fn39
        0xF90000, // Fn40
        0xF90000, // Fn41
        0xF90000, // Fn42
        0xF90000, // Fn43
        0xF90000, // Fn44
        0xF90000, // Fn45
        0xF90000, // Fn46
        0xF90000, // Fn47
        0xF90000, // Fn48
        0xF90000, // Fn49
        0xF90000, // Fn50
        0xF90000, // Fn51
        0xF90000, // Fn52
        0xF90000, // Fn53
        0xF90000, // Fn54
        0xF90000, // Fn55
        0xF90000, // Fn56
        0xF90000, // Fn57
        0xF90000, // Fn58
        0xF90000, // Fn59
        0xF90000, // Fn60
        0xF90000, // Fn61
        0xF90000, // Fn62
        0xF90000, // Fn63
        0xF90000, // Fn64
        0xF90000, // Fn65
        0xF90000, // Fn66
        0xF90000, // Fn67
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
        
        // send to layout
        if (speedStepMode == jmri.SpeedStepMode.TMCC_200) {

            // TMCC2 Legacy 200 step mode
            int value = (int) (199 * speed); // max value to send is 199 in 200 step mode
            if (value > 199) {
                value = 199;    // max possible speed
            }
            SerialMessage m = new SerialMessage();
            m.setOpCode(0xF8);
    
            if (value < 0) {
                // immediate stop
                m.putAsWord(0x0000 + (address.getNumber() << 9) + 0);
            } else {
                // normal speed setting
                m.putAsWord(0x0000 + (address.getNumber() << 9) + value);
            }
            // only send twice to advanced command station
            tc.sendSerialMessage(m, null);
            tc.sendSerialMessage(m, null);

        } else {

            // assume TMCC 32 step mode
            int value = (int) (32 * speed);
            if (value > 31) {
                value = 31;    // max possible speed
            }
            SerialMessage m = new SerialMessage();
    
            if (value < 0) {
                // immediate stop
                m.putAsWord(0x0060 + address.getNumber() * 128 + 0);
            } else {
                // normal speed setting
                m.putAsWord(0x0060 + address.getNumber() * 128 + value);
            }
    
            tc.sendSerialMessage(m, null);
            tc.sendSerialMessage(m, null);
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
     * Send these messages to the layout four times
     * to make sure they're accepted.
     * @param value Content of message to be sent in three bytes
     * @param func  The number of the function being addressed
     */
    protected void sendFnToLayout(int value, int func) {
        tc.sendSerialMessage(new SerialMessage(value), null);
        tc.sendSerialMessage(new SerialMessage(value), null);
        tc.sendSerialMessage(new SerialMessage(value), null);
     
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
     * @param mode only TMCC 30 and TMCC 200 are allowed
     */
    @Override
    public void setSpeedStepMode(jmri.SpeedStepMode mode) {
        if (mode == jmri.SpeedStepMode.TMCC_32 || mode == jmri.SpeedStepMode.TMCC_200) {
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
