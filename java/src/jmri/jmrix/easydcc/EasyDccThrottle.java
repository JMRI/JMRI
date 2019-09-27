package jmri.jmrix.easydcc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle with code specific to an EasyDCC connection.
 * <p>
 * Addresses of 99 and below are considered short addresses, and over 100 are
 * considered long addresses.
 * <p>
 * Based on Glen Oberhauser's original LnThrottleManager implementation and NCEThrottle
 *
 * @author Bob Jacobsen Copyright (C) 2001, modified 2004 by Kelly Loyd
 */
public class EasyDccThrottle extends AbstractThrottle {

    /**
     * Constructor.
     *
     * @param memo the connected EasyDccTrafficController
     * @param address Loco ID
     */
    public EasyDccThrottle(EasyDccSystemConnectionMemo memo, DccLocoAddress address) {
        super(memo);
        super.speedStepMode = SpeedStepMode.NMRA_DCC_128;
        tc = memo.getTrafficController();

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
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

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     */
    @Override
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
        EasyDccMessage m = new EasyDccMessage(4 + 3 * result.length);
        int i = 0;  // message index counter
        m.setElement(i++, 'S');
        m.setElement(i++, ' ');
        m.setElement(i++, '0');
        m.setElement(i++, '1');

        for (int j = 0; j < result.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[j] & 0xFF, i);
            i = i + 2;
        }
        tc.sendEasyDccMessage(m, null);
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {

        byte[] result = jmri.NmraPacket.function5Through8Packet(address.getNumber(),
                address.isLongAddress(),
                getF5(), getF6(), getF7(), getF8());

        EasyDccMessage m = new EasyDccMessage(4 + 3 * result.length);
        int i = 0;  // message index counter
        m.setElement(i++, 'S');
        m.setElement(i++, ' ');
        m.setElement(i++, '0');
        m.setElement(i++, '1');

        for (int j = 0; j < result.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[j] & 0xFF, i);
            i = i + 2;
        }
        tc.sendEasyDccMessage(m, null);
    }

    /**
     * Send the message to set the state of functions F9, F10, F11, F12.
     */
    @Override
    protected void sendFunctionGroup3() {

        byte[] result = jmri.NmraPacket.function9Through12Packet(address.getNumber(),
                address.isLongAddress(),
                getF9(), getF10(), getF11(), getF12());

        EasyDccMessage m = new EasyDccMessage(4 + 3 * result.length);
        int i = 0;  // message index counter
        m.setElement(i++, 'S');
        m.setElement(i++, ' ');
        m.setElement(i++, '0');
        m.setElement(i++, '1');

        for (int j = 0; j < result.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[j] & 0xFF, i);
            i = i + 2;
        }
        tc.sendEasyDccMessage(m, null);
    }

    /**
     * Set the speed {@literal &} direction.
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;

        byte[] result;

        if (super.speedStepMode == SpeedStepMode.NMRA_DCC_128) {
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
            result = jmri.NmraPacket.speedStep128Packet(address.getNumber(),
                    address.isLongAddress(), value, isForward);
        } else {

            /* [A Crosland 05Feb12] There is a potential issue in the way
             * the float speed value is converted to integer speed step.
             * A max speed value of 1 is first converted to int 28 then incremented
             * to 29 which is too large. The next highest speed value also
             * results in a value of 28. So two discrete throttle steps
             * both map to speed step 28.
             *
             * This is compounded by the bug in speedStep28Packet() which
             * cannot generate a DCC packet with speed step 28.
             *
             * Suggested correct code is
             *   value = (int) ((31-3) * speed); // -3 for rescale to avoid stop and estop x2
             *   if (value > 0) value = value + 3; // skip stop and estop x2
             *   if (value > 31) value = 31; // max possible speed
             *   if (value < 0) value = 2; // emergency stop
             *   bl = jmri.NmraPacket.speedStep28Packet(true, address.getNumber(),
             *     address.isLongAddress(), value, isForward);
             */
            int value = (int) ((28) * speed);     // -1 for rescale to avoid estop
            if (value > 0) {
                value = value + 1;   // skip estop
            }
            if (value > 28) {
                value = 28;     // max possible speed
            }
            if (value < 0) {
                value = 1;         // emergency stop
            }
            result = jmri.NmraPacket.speedStep28Packet(address.getNumber(),
                    address.isLongAddress(), value, isForward);
        }

        EasyDccMessage m = new EasyDccMessage(1 + 3 * result.length);
        // for EasyDCC, sending a speed command involves:
        // Q place in Queue
        // Cx xx (address)
        // yy (speed)
        int i = 0;  // message index counter
        m.setElement(i++, 'Q');

        for (int j = 0; j < result.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(result[j] & 0xFF, i);
            i = i + 2;
        }

        tc.sendEasyDccMessage(m, null);

        if (oldSpeed != this.speedSetting) {
            notifyPropertyChangeListener(SPEEDSETTING, oldSpeed, this.speedSetting);
        }
        record(speed);
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
        if (old != isForward) {
            notifyPropertyChangeListener(ISFORWARD, old, isForward);
        }
    }

    private DccLocoAddress address;
    EasyDccTrafficController tc;

    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    @Override
    protected void throttleDispose() {
        active = false;
        finishRecord();
    }

}
