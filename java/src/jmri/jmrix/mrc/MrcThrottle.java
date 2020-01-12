package jmri.jmrix.mrc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Date;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to an MRC connection.
 * <p>
 * Addresses of 99 and below are considered short addresses, and over 100 are
 * considered long addresses. This is not the MRC system standard, but is used
 * as an expedient here.
 * <p>
 * Based on Glen Oberhauser's original LnThrottleManager implementation
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class MrcThrottle extends AbstractThrottle implements MrcTrafficListener {

    private MrcTrafficController tc = null;

    /**
     * Throttle Constructor.
     * @param memo system connection memo
     * @param address DCC loco address for throttle
     */
    public MrcThrottle(MrcSystemConnectionMemo memo, DccLocoAddress address) {
        super(memo);
        this.tc = memo.getMrcTrafficController();
        super.speedStepMode = jmri.SpeedStepMode.NMRA_DCC_128;

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
        this.f22 = false;
        this.f23 = false;
        this.f24 = false;
        this.f25 = false;
        this.f26 = false;
        this.f27 = false;
        this.f28 = false;
        this.address = address;
        this.isForward = true;
        if (address.isLongAddress()) {
            addressLo = address.getNumber();
            addressHi = address.getNumber() >> 8;
            addressHi = addressHi + 0xc0; //We add 0xc0 to the high byte.
        } else {
            addressLo = address.getNumber();
        }
        tc.addTrafficListener(MrcInterface.THROTTLEINFO, this);
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

        int data = 0x00
                | (f0 ? 0x10 : 0)
                | (f1 ? 0x01 : 0)
                | (f2 ? 0x02 : 0)
                | (f3 ? 0x04 : 0)
                | (f4 ? 0x08 : 0);

        data = data + 0x80;
        MrcMessage m = MrcMessage.getSendFunction(1, addressLo, addressHi, data);
        if (m != null) {
            tc.sendMrcMessage(m);
        }
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    @Override
    protected void sendFunctionGroup2() {
        int data = 0x00
                | (f8 ? 0x08 : 0)
                | (f7 ? 0x04 : 0)
                | (f6 ? 0x02 : 0)
                | (f5 ? 0x01 : 0);

        data = data + 0xB0;

        MrcMessage m = MrcMessage.getSendFunction(2, addressLo, addressHi, data);
        if (m != null) {
            tc.sendMrcMessage(m);
        }
    }

    /**
     * Send the message to set the state of functions F9, F12, F11, F12.
     */
    @Override
    protected void sendFunctionGroup3() {

        int data = 0x00
                | (f9 ? 0x01 : 0)
                | (f10 ? 0x02 : 0)
                | (f11 ? 0x04 : 0)
                | (f12 ? 0x08 : 0);

        data = data + 0xA0;
        MrcMessage m = MrcMessage.getSendFunction(3, addressLo, addressHi, data);
        if (m != null) {
            tc.sendMrcMessage(m);
        }
    }

    /**
     * Send the message to set the state of functions F13 to F20. MRC Group 4 and 5
     */
    @Override
    protected void sendFunctionGroup4() {
        int data = 0x00
                | (f16 ? 0x08 : 0)
                | (f15 ? 0x04 : 0)
                | (f14 ? 0x02 : 0)
                | (f13 ? 0x01 : 0);

        data = data + 0xD0;

        MrcMessage m = MrcMessage.getSendFunction(4, addressLo, addressHi, data);
        if (m != null) {
            tc.sendMrcMessage(m);
        }

        data = 0x00
                | (f20 ? 0x08 : 0)
                | (f19 ? 0x04 : 0)
                | (f18 ? 0x02 : 0)
                | (f17 ? 0x01 : 0);
        data = data + 0xC0;

        m = MrcMessage.getSendFunction(5, addressLo, addressHi, data);
        if (m != null) {
            tc.sendMrcMessage(m);
        }
    }

    /**
     * Send the message to set the state of functions F21 to F28. MRC Group 6
     */
    @Override
    protected void sendFunctionGroup5() {
        int data = 0x00
                | (f28 ? 0x80 : 0)
                | (f27 ? 0x40 : 0)
                | (f26 ? 0x20 : 0)
                | (f25 ? 0x10 : 0)
                | (f24 ? 0x08 : 0)
                | (f23 ? 0x04 : 0)
                | (f22 ? 0x02 : 0)
                | (f21 ? 0x01 : 0);

        MrcMessage m = MrcMessage.getSendFunction(6, addressLo, addressHi, data);
        if (m != null) {
            tc.sendMrcMessage(m);
        }
    }

    /**
     * Set the speed {@literal &} direction.
     * <p>
     *
     * @param speed Number from 0 to 1, or less than zero for emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;
        MrcMessage m;
        int value;
        if (super.speedStepMode == jmri.SpeedStepMode.NMRA_DCC_128) {
            log.debug("setSpeedSetting= {}", speed); //IN18N
            //MRC use a value between 0-127 no matter what the controller is set to
            value = (int) ((127 - 1) * speed);     // -1 for rescale to avoid estop
            if (value > 0) {
                value = value + 1;  // skip estop
            }
            if (value > 127) {
                value = 127;    // max possible speed
            }
            if (value < 0) {
                value = 1;        // emergency stop
            }
            if (isForward) {
                value = value + 128;
            }
            m = MrcMessage.getSendSpeed128(addressLo, addressHi, value);
        } else {
            value = (int) ((28) * speed); // -1 for rescale to avoid estop
            if (value > 0) {
                value = value + 3; // skip estop
            }
            if (value > 32) {
                value = 31; // max possible speed
            }
            if (value < 0) {
                value = 2; // emergency stop
            }
            m = MrcMessage.getSendSpeed28(addressLo, addressHi, value, isForward);
        }
        tc.sendMrcMessage(m);

        if (oldSpeed != this.speedSetting) {
            notifyPropertyChangeListener(SPEEDSETTING, oldSpeed, this.speedSetting); //IN18N
        }
        record(speed);
    }

    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
        log.debug("setIsForward= {}", forward);
        if (old != isForward) {
            notifyPropertyChangeListener(ISFORWARD, old, isForward); //IN18N
        }
    }

    @Override
    protected void throttleDispose() {
        finishRecord();
    }

    @Override
    public String toString() {
        return getLocoAddress().toString();
    }

    //Might need to look at other packets from handsets to see if they also have control of our loco and adjust from that.
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "fixed number of possible values")
    @Override
    public void notifyRcv(Date timestamp, MrcMessage m) {
        if (m.getMessageClass() != MrcInterface.THROTTLEINFO
                || (m.getMessageClass() == MrcInterface.THROTTLEINFO && (m.getElement(0) == MrcPackets.LOCOSOLECONTROLCODE
                || m.getElement(0) == MrcPackets.LOCODBLCONTROLCODE))) {
            return;
        }
        if (m.getLocoAddress() == address.getNumber()) {
            if (MrcPackets.startsWith(m, MrcPackets.THROTTLEPACKETHEADER)) {
                if (m.getElement(10) == 0x02) {
                    //128
                    log.debug("speed Packet from another controller for our loco"); //IN18N
                    int speed = m.getElement(8);
                    if ((m.getElement(8) & 0x80) == 0x80) {
                        //Forward
                        if (!this.isForward) {
                            this.isForward = true;
                            notifyPropertyChangeListener(ISFORWARD, !isForward, isForward); //IN18N
                        }
                        //speed = m.getElement(8);
                    } else if (this.isForward) {
                        //reverse
                        this.isForward = false;
                        notifyPropertyChangeListener(ISFORWARD, !isForward, isForward); //IN18N
                        //speed = m.getElement(8);
                    }
                    speed = (speed & 0x7f) - 1;
                    if (speed < 0) {
                        speed = 0;
                    }
                    float val = speed / 126.0f;
                    
                    // next line is the FE_FLOATING_POINT_EQUALITY annotated above
                    if (val != this.speedSetting) {
                        notifyPropertyChangeListener(SPEEDSETTING, this.speedSetting, val); //IN18N
                        this.speedSetting = val;
                        record(val);
                    }
                } else if (m.getElement(10) == 0x00) {
                    int value = m.getElement(8) & 0xff;
                    //28 Speed Steps
                    if ((m.getElement(8) & 0x60) == 0x60) {
                        //Forward
                        value = value - 0x60;
                    } else {
                        value = value - 0x40;
                    }
                    if (((value >> 4) & 0x01) == 0x01) {
                        value = value - 0x10;
                        value = (value << 1) + 1;
                    } else {
                        value = value << 1;
                    }
                    value = value - 3; //Turn into user expected 0-28
                    float val = -1;
                    if (value != -1) {
                        if (value < 1) {
                            value = 0;
                        }
                        val = value / 28.0f;
                    }

                    if (val != this.speedSetting) {
                        notifyPropertyChangeListener(SPEEDSETTING, this.speedSetting, val); //IN18N
                        this.speedSetting = val;
                        record(val);
                    }
                }
            } else if (MrcPackets.startsWith(m, MrcPackets.FUNCTIONGROUP1PACKETHEADER)) {
                int data = m.getElement(8) & 0xff;
                if ((data & 0x10) == 0x10) {
                    if (!this.f0) {
                        notifyPropertyChangeListener(Throttle.F0, this.f0, true);
                        this.f0 = true;
                    }
                } else if (this.f0) {
                    notifyPropertyChangeListener(Throttle.F0, this.f0, false);
                    this.f0 = false;
                }
                if ((data & 0x01) == 0x01) {
                    if (!this.f1) {
                        notifyPropertyChangeListener(Throttle.F1, this.f1, true);
                        this.f1 = true;
                    }
                } else if (this.f1) {
                    notifyPropertyChangeListener(Throttle.F1, this.f1, false);
                    this.f1 = false;
                }
                if ((data & 0x02) == 0x02) {
                    if (!this.f2) {
                        notifyPropertyChangeListener(Throttle.F2, this.f2, true);
                        this.f2 = true;
                    }
                } else if (this.f2) {
                    notifyPropertyChangeListener(Throttle.F2, this.f2, false);
                    this.f2 = false;
                }
                if ((data & 0x04) == 0x04) {
                    if (!this.f3) {
                        notifyPropertyChangeListener(Throttle.F3, this.f3, true);
                        this.f3 = true;
                    }
                } else if (this.f3) {
                    notifyPropertyChangeListener(Throttle.F3, this.f3, false);
                    this.f3 = false;
                }
                if ((data & 0x08) == 0x08) {
                    if (!this.f4) {
                        notifyPropertyChangeListener(Throttle.F4, this.f4, true);
                        this.f4 = true;
                    }
                } else if (this.f4) {
                    notifyPropertyChangeListener(Throttle.F4, this.f4, false);
                    this.f4 = false;
                }
                //reverse engineer the function
            } else if (MrcPackets.startsWith(m, MrcPackets.FUNCTIONGROUP2PACKETHEADER)) {
                int data = m.getElement(8) & 0xff;
                if ((data & 0x01) == 0x01) {
                    if (!this.f5) {
                        notifyPropertyChangeListener(Throttle.F5, this.f5, true);
                        this.f5 = true;
                    }
                } else if (this.f5) {
                    notifyPropertyChangeListener(Throttle.F5, this.f5, false);
                    this.f5 = false;
                }
                if ((data & 0x02) == 0x02) {
                    if (!this.f6) {
                        notifyPropertyChangeListener(Throttle.F6, this.f6, true);
                        this.f6 = true;
                    }
                } else if (this.f6) {
                    notifyPropertyChangeListener(Throttle.F6, this.f6, false);
                    this.f6 = false;
                }
                if ((data & 0x04) == 0x04) {
                    if (!this.f7) {
                        notifyPropertyChangeListener(Throttle.F7, this.f7, true);
                        this.f7 = true;
                    }
                } else if (this.f7) {
                    notifyPropertyChangeListener(Throttle.F7, this.f7, false);
                    this.f7 = false;
                }
                if ((data & 0x08) == 0x08) {
                    if (!this.f8) {
                        notifyPropertyChangeListener(Throttle.F8, this.f8, true);
                        this.f8 = true;
                    }
                } else if (this.f8) {
                    notifyPropertyChangeListener(Throttle.F8, this.f8, false);
                    this.f8 = false;
                }
            } else if (MrcPackets.startsWith(m, MrcPackets.FUNCTIONGROUP3PACKETHEADER)) {
                int data = m.getElement(8) & 0xff;
                if ((data & 0x01) == 0x01) {
                    if (!this.f9) {
                        notifyPropertyChangeListener(Throttle.F9, this.f9, true);
                        this.f9 = true;
                    }
                } else if (this.f9) {
                    notifyPropertyChangeListener(Throttle.F9, this.f9, false);
                    this.f9 = false;
                }
                if ((data & 0x02) == 0x02) {
                    if (!this.f10) {
                        notifyPropertyChangeListener(Throttle.F10, this.f10, true);
                        this.f10 = true;
                    }
                } else if (this.f10) {
                    notifyPropertyChangeListener(Throttle.F10, this.f10, false);
                    this.f10 = false;
                }
                if ((data & 0x04) == 0x04) {
                    if (!this.f11) {
                        notifyPropertyChangeListener(Throttle.F11, this.f11, true);
                        this.f11 = true;
                    }
                } else if (this.f11) {
                    notifyPropertyChangeListener(Throttle.F11, this.f11, false);
                    this.f11 = false;
                }
                if ((data & 0x08) == 0x08) {
                    if (!this.f12) {
                        notifyPropertyChangeListener(Throttle.F12, this.f12, true);
                        this.f12 = true;
                    }
                } else if (this.f12) {
                    notifyPropertyChangeListener(Throttle.F12, this.f12, false);
                    this.f12 = false;
                }
            } else if (MrcPackets.startsWith(m, MrcPackets.FUNCTIONGROUP4PACKETHEADER)) {
                int data = m.getElement(8) & 0xff;
                if ((data & 0x01) == 0x01) {
                    if (!this.f13) {
                        notifyPropertyChangeListener(Throttle.F13, this.f13, true);
                        this.f13 = true;
                    }
                } else if (this.f13) {
                    notifyPropertyChangeListener(Throttle.F13, this.f13, false);
                    this.f13 = false;
                }
                if ((data & 0x02) == 0x02) {
                    if (!this.f14) {
                        notifyPropertyChangeListener(Throttle.F14, this.f14, true);
                        this.f14 = true;
                    }
                } else if (this.f14) {
                    notifyPropertyChangeListener(Throttle.F14, this.f14, false);
                    this.f14 = false;
                }
                if ((data & 0x04) == 0x04) {
                    if (!this.f15) {
                        notifyPropertyChangeListener(Throttle.F15, this.f15, true);
                        this.f15 = true;
                    }
                } else if (this.f15) {
                    notifyPropertyChangeListener(Throttle.F15, this.f15, false);
                    this.f15 = false;
                }
                if ((data & 0x08) == 0x08) {
                    if (!this.f16) {
                        notifyPropertyChangeListener(Throttle.F16, this.f16, true);
                        this.f16 = true;
                    }
                } else if (this.f16) {
                    notifyPropertyChangeListener(Throttle.F16, this.f16, false);
                    this.f16 = false;
                }
            } else if (MrcPackets.startsWith(m, MrcPackets.FUNCTIONGROUP5PACKETHEADER)) {
                int data = m.getElement(8) & 0xff;
                if ((data & 0x01) == 0x01) {
                    if (!this.f17) {
                        notifyPropertyChangeListener(Throttle.F17, this.f17, true);
                        this.f17 = true;
                    }
                } else if (this.f17) {
                    notifyPropertyChangeListener(Throttle.F17, this.f17, false);
                    this.f17 = false;
                }
                if ((data & 0x02) == 0x02) {
                    if (!this.f18) {
                        notifyPropertyChangeListener(Throttle.F18, this.f18, true);
                        this.f18 = true;
                    }
                } else if (this.f18) {
                    notifyPropertyChangeListener(Throttle.F18, this.f18, false);
                    this.f18 = false;
                }
                if ((data & 0x04) == 0x04) {
                    if (!this.f19) {
                        notifyPropertyChangeListener(Throttle.F19, this.f19, true);
                        this.f19 = true;
                    }
                } else if (this.f19) {
                    notifyPropertyChangeListener(Throttle.F19, this.f19, false);
                    this.f19 = false;
                }
                if ((data & 0x08) == 0x08) {
                    if (!this.f20) {
                        notifyPropertyChangeListener(Throttle.F20, this.f20, true);
                        this.f20 = true;
                    }
                } else if (this.f20) {
                    notifyPropertyChangeListener(Throttle.F20, this.f20, false);
                    this.f20 = false;
                }
            } else if (MrcPackets.startsWith(m, MrcPackets.FUNCTIONGROUP6PACKETHEADER)) {
                int data = m.getElement(8) & 0xff;
                if ((data & 0x01) == 0x01) {
                    if (!this.f21) {
                        notifyPropertyChangeListener(Throttle.F21, this.f21, true);
                        this.f21 = true;
                    }
                } else if (this.f21) {
                    notifyPropertyChangeListener(Throttle.F21, this.f21, false);
                    this.f21 = false;
                }
                if ((data & 0x02) == 0x02) {
                    if (!this.f22) {
                        notifyPropertyChangeListener(Throttle.F22, this.f22, true);
                        this.f22 = true;
                    }
                } else if (this.f22) {
                    notifyPropertyChangeListener(Throttle.F22, this.f22, false);
                    this.f22 = false;
                }
                if ((data & 0x04) == 0x04) {
                    if (!this.f23) {
                        notifyPropertyChangeListener(Throttle.F23, this.f23, true);
                        this.f23 = true;
                    }
                } else if (this.f23) {
                    notifyPropertyChangeListener(Throttle.F23, this.f23, false);
                    this.f23 = false;
                }
                if ((data & 0x08) == 0x08) {
                    if (!this.f24) {
                        notifyPropertyChangeListener(Throttle.F24, this.f24, true);
                        this.f24 = true;
                    }
                } else if (this.f24) {
                    notifyPropertyChangeListener(Throttle.F24, this.f24, false);
                    this.f24 = false;
                }
                if ((data & 0x10) == 0x10) {
                    if (!this.f25) {
                        notifyPropertyChangeListener(Throttle.F25, this.f25, true);
                        this.f25 = true;
                    }
                } else if (this.f25) {
                    notifyPropertyChangeListener(Throttle.F25, this.f25, false);
                    this.f25 = false;
                }
                if ((data & 0x20) == 0x20) {
                    if (!this.f26) {
                        notifyPropertyChangeListener(Throttle.F26, this.f26, true);
                        this.f26 = true;
                    }
                } else if (this.f26) {
                    notifyPropertyChangeListener(Throttle.F26, this.f26, false);
                    this.f26 = false;
                }
                if ((data & 0x40) == 0x40) {
                    if (!this.f27) {
                        notifyPropertyChangeListener(Throttle.F27, this.f27, true);
                        this.f27 = true;
                    }
                } else if (this.f27) {
                    notifyPropertyChangeListener(Throttle.F27, this.f27, false);
                    this.f27 = false;
                }
                if ((data & 0x80) == 0x80) {
                    if (!this.f28) {
                        notifyPropertyChangeListener(Throttle.F28, this.f28, true);
                        this.f28 = true;
                    }
                } else if (this.f28) {
                    notifyPropertyChangeListener(Throttle.F28, this.f28, false);
                    this.f28 = false;
                }
            } else {
                return;
            }
        } else {
            return;
        }
    }

    @Override
    public void notifyXmit(Date timestamp, MrcMessage m) {/* message(m); */

    }

    @Override
    public void notifyFailedXmit(Date timestamp, MrcMessage m) { /*message(m);*/ }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(MrcThrottle.class);

}
