package jmri.jmrix.tmcc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
        if (func>=0 && func <=67) {
            if ( SERIAL_FUNCTION_CODES[func] > 0xFFFF ) {
                // TMCC 2 format
                sendToLayout(SERIAL_FUNCTION_CODES[func] + address.getNumber() * 256);
            } else {
                // TMCC 1 format
                sendToLayout(SERIAL_FUNCTION_CODES[func] + address.getNumber() * 128);
            }
        }
        else {
            super.setFunction(func, value);
        }
    }

    private final static int[] SERIAL_FUNCTION_CODES = new int[] {
        0x00000D, 0x00001D, 0x00001C, 0x000005, 0x000006, /* Fn0-4 */
        0x000010, 0x000011, 0x000012, 0x000013, 0x000014, /* Fn5-9 */
        0x000015, 0x000016, 0x000017, 0x000018, 0x000019, /* Fn10-14 */
        0x000009, 0x00001E, 0x000000, 0x000003, 0x000001, /* Fn15-19 */
        0x000004, 0x000007, 0x000047, 0x000042, 0x000028, /* Fn20-24 */
        0x000029, 0x00002A, 0x00002B, /* 25-27 */
        // start of TMCC 2 functions
        0xF9002D, // Fn28 Locomotive Re-Fueling Sound
        0xF900F6, // Fn29 Brake Squeal Sound
        0xF900F7, // FN30 Auger Sound
        0xF90000, //
        0xF90000, // Fn32
        0xF90000, 0xF90000, 0xF90000, 0xF90000, 0xF90000, /* 33 - 37 */
        0xF90000, 0xF90000, 0xF90000, 0xF90000, 0xF90000, /* 38 - 42 */
        0xF90000, 0xF90000, 0xF90000, 0xF90000, 0xF90000, /* 43 - 47 */
        0xF90000, 0xF90000, 0xF90000, 0xF90000, 0xF90000, /* 48 - 52 */
        0xF90000, 0xF90000, 0xF90000, 0xF90000, 0xF90000, /* 53 - 57 */
        0xF90000, 0xF90000, 0xF90000, 0xF90000, 0xF90000, /* 58 - 62 */
        0xF90000, 0xF90000, 0xF90000, 0xF90000, 0xF90000, /* 63 - 67 */
    };

    /**
     * Set the speed.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public void setSpeedSetting(float speed) {
        float oldSpeed;
        synchronized(this) {
            oldSpeed = this.speedSetting;
            this.speedSetting = speed;
        }
        int value = (int) (32 * speed);     // -1 for rescale to avoid estop
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
     */
    protected void sendToLayout(int value) {
        tc.sendSerialMessage(new SerialMessage(value), null);
        tc.sendSerialMessage(new SerialMessage(value), null);
        tc.sendSerialMessage(new SerialMessage(value), null);
        tc.sendSerialMessage(new SerialMessage(value), null);
    }

    /*
     * Set the speed step value.
     * <p>
     * Only 32 steps is available
     *
     * @param Mode ignored, as only 32 is valid
     */
    @Override
    public void setSpeedStepMode(jmri.SpeedStepMode Mode) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void throttleDispose() {
        finishRecord();
    }

}
