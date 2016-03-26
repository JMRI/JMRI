package jmri.jmrix.tmcc;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottle;

/**
 * An implementation of DccThrottle.
 * <P>
 * Addresses of 99 and below are considered short addresses, and over 100 are
 * considered long addresses.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2006
 * @version $Revision$
 */
public class SerialThrottle extends AbstractThrottle {

    /**
     * Constructor.
     */
    public SerialThrottle(DccLocoAddress address) {
        //This will need to include system adapter memo once converted
        super(null);

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
        this.f13 = false;
        this.f14 = false;
        this.f15 = false;
        this.f16 = false;
        this.f17 = false;
        this.f18 = false;
        this.f19 = false;
        this.f20 = false;
        this.f21 = false;
        this.address = address;
        this.isForward = true;

    }

    DccLocoAddress address;

    public LocoAddress getLocoAddress() {
        return address;
    }

    public void setF0(boolean f0) {
        this.f0 = f0;
        // aux 2
        sendToLayout(0x000D + address.getNumber() * 128);
    }

    public void setF1(boolean f1) {
        this.f1 = f1;
        // bell
        sendToLayout(0x001D + address.getNumber() * 128);
    }

    public void setF2(boolean f2) {
        this.f2 = f2;
        // horn/whistle 1
        sendToLayout(0x001C + address.getNumber() * 128);
    }

    public void setF3(boolean f3) {
        this.f3 = f3;
        // front coupler
        sendToLayout(0x0005 + address.getNumber() * 128);
    }

    public void setF4(boolean f4) {
        this.f4 = f4;
        // back coupler
        sendToLayout(0x0006 + address.getNumber() * 128);
    }

    public void setF5(boolean f5) {
        this.f5 = f5;
        // 0
        sendToLayout(0x0010 + address.getNumber() * 128);
    }

    public void setF6(boolean f6) {
        this.f6 = f6;
        // 1
        sendToLayout(0x0011 + address.getNumber() * 128);
    }

    public void setF7(boolean f7) {
        this.f7 = f7;
        // 2
        sendToLayout(0x0012 + address.getNumber() * 128);
    }

    public void setF8(boolean f8) {
        this.f8 = f8;
        // 3
        sendToLayout(0x0013 + address.getNumber() * 128);
    }

    public void setF9(boolean f9) {
        this.f9 = f9;
        // 4
        sendToLayout(0x0014 + address.getNumber() * 128);
    }

    public void setF10(boolean f10) {
        this.f10 = f10;
        // 5
        sendToLayout(0x0015 + address.getNumber() * 128);
    }

    public void setF11(boolean f11) {
        this.f11 = f11;
        // 6
        sendToLayout(0x0016 + address.getNumber() * 128);
    }

    public void setF12(boolean f12) {
        this.f12 = f12;
        // 7
        sendToLayout(0x0017 + address.getNumber() * 128);
    }

    public void setF13(boolean f13) {
        this.f13 = f13;
        // 8
        sendToLayout(0x0018 + address.getNumber() * 128);
    }

    public void setF14(boolean f14) {
        this.f14 = f14;
        // 9
        sendToLayout(0x0019 + address.getNumber() * 128);
    }

    public void setF15(boolean f15) {
        this.f15 = f15;
        // aux 1
        sendToLayout(0x0009 + address.getNumber() * 128);
    }

    public void setF16(boolean f16) {
        this.f16 = f16;
        // letoff sound
        sendToLayout(0x001E + address.getNumber() * 128);
    }

    public void setF17(boolean f17) {
        this.f17 = f17;
        // forward direction
        sendToLayout(0x0000 + address.getNumber() * 128);
    }

    public void setF18(boolean f18) {
        this.f18 = f18;
        // reverse direction
        sendToLayout(0x0003 + address.getNumber() * 128);
    }

    public void setF19(boolean f19) {
        this.f19 = f19;
        // toggle direction
        sendToLayout(0x0001 + address.getNumber() * 128);
    }

    public void setF20(boolean f20) {
        this.f20 = f20;
        // boost
        sendToLayout(0x0004 + address.getNumber() * 128);
    }

    public void setF21(boolean f21) {
        this.f21 = f21;
        // brake
        sendToLayout(0x0007 + address.getNumber() * 128);
    }

    /**
     * Set the speed
     * <P>
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
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

        SerialTrafficController.instance().sendSerialMessage(m, null);
        SerialTrafficController.instance().sendSerialMessage(m, null);
        SerialTrafficController.instance().sendSerialMessage(m, null);
        SerialTrafficController.instance().sendSerialMessage(m, null);
        if (oldSpeed != this.speedSetting) {
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting);
        }
        record(speed);
    }

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
        SerialTrafficController.instance().sendSerialMessage(m, null);
        SerialTrafficController.instance().sendSerialMessage(m, null);
        SerialTrafficController.instance().sendSerialMessage(m, null);
        SerialTrafficController.instance().sendSerialMessage(m, null);
        if (old != isForward) {
            notifyPropertyChangeListener("IsForward", old, isForward);
        }
    }

    protected void sendToLayout(int value) {
        SerialTrafficController.instance().sendSerialMessage(new SerialMessage(value), null);
        SerialTrafficController.instance().sendSerialMessage(new SerialMessage(value), null);
        SerialTrafficController.instance().sendSerialMessage(new SerialMessage(value), null);
        SerialTrafficController.instance().sendSerialMessage(new SerialMessage(value), null);
    }

    /*
     * setSpeedStepMode - set the speed step value.
     * <P>
     * Only 32 steps is available
     * <P>
     * @param Mode Ignored, as only 32 is valid
     */
    public void setSpeedStepMode(int Mode) {
        speedStepMode = 32;
    }

    protected void throttleDispose() {
        finishRecord();
    }

}
