package jmri.jmrix.openlcb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.jmrix.AbstractThrottle;
import jmri.jmrix.SystemConnectionMemo;
import org.openlcb.OlcbInterface;
import org.openlcb.implementations.throttle.ThrottleImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle for OpenLCB.
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class OlcbThrottle extends AbstractThrottle {
        
    /**
     * Constructor
     * @param address Dcc loco address
     * @param memo system connection memo
     * @param mgr config manager
     * @deprecated since 4.13.4
     */
    @Deprecated
    public OlcbThrottle(DccLocoAddress address, SystemConnectionMemo memo, OlcbConfigurationManager mgr) {
           this(address,memo);
        jmri.util.Log4JUtil.deprecationWarning(log, "OlcbThrottle(..)");        
    }

    /**
     * Constructor
     * @param address Dcc loco address
     * @param memo system connection memo
     */
    public OlcbThrottle(DccLocoAddress address, SystemConnectionMemo memo) {
        super(memo);
        OlcbInterface iface = memo.get(OlcbInterface.class);

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
        this.isForward = true;

        this.address = address;

        // create OpenLCB library object that does the magic & activate
        if (iface.getNodeStore() == null) {
            log.error("Failed to access Mimic Node Store");
        }
        if (iface.getDatagramService() == null) {
            log.error("Failed to access Datagram Service");
        }
        if (address instanceof OpenLcbLocoAddress) {
            oti = new ThrottleImplementation(
                    ((OpenLcbLocoAddress) address).getNode(),
                    iface.getNodeStore(),
                    iface.getDatagramService()
            );
        } else {
            oti = new ThrottleImplementation(
                    this.address.getNumber(),
                    this.address.isLongAddress(),
                    iface.getNodeStore(),
                    iface.getDatagramService()
            );
        }
        oti.start();
    }

    ThrottleImplementation oti;

    DccLocoAddress address;

    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    @Override
    public String toString() {
        return getLocoAddress().toString();
    }

    /**
     * Set the speed {@literal &} direction
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        if (speed > 1.0) {
            log.warn("Speed was set too high: " + speed);
        }
        this.speedSetting = speed;

        // send to OpenLCB
        if (speed >= 0.0) {
            oti.setSpeed(speed * 100.0, isForward);
        } else {
            oti.doEmergencyStop();
        }

        // notify 
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

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    @Override
    public void setF0(boolean f0) {
        boolean old = this.f0;
        this.f0 = f0;
        doSetFunction(0, Throttle.F0, old, this.f0);
    }

    @Override
    public void setF1(boolean f1) {
        boolean old = this.f1;
        this.f1 = f1;
        doSetFunction(1, Throttle.F1, old, this.f1);
    }

    @Override
    public void setF2(boolean f2) {
        boolean old = this.f2;
        this.f2 = f2;
        doSetFunction(2, Throttle.F2, old, this.f2);
    }

    @Override
    public void setF3(boolean f3) {
        boolean old = this.f3;
        this.f3 = f3;
        doSetFunction(3, Throttle.F3, old, this.f3);
    }

    @Override
    public void setF4(boolean f4) {
        boolean old = this.f4;
        this.f4 = f4;
        doSetFunction(4, Throttle.F4, old, this.f4);
    }

    @Override
    public void setF5(boolean f5) {
        boolean old = this.f5;
        this.f5 = f5;
        doSetFunction(5, Throttle.F5, old, this.f5);
    }

    @Override
    public void setF6(boolean f6) {
        boolean old = this.f6;
        this.f6 = f6;
        doSetFunction(6, Throttle.F6, old, this.f6);
    }

    @Override
    public void setF7(boolean f7) {
        boolean old = this.f7;
        this.f7 = f7;
        doSetFunction(7, Throttle.F7, old, this.f7);
    }

    @Override
    public void setF8(boolean f8) {
        boolean old = this.f8;
        this.f8 = f8;
        doSetFunction(8, Throttle.F8, old, this.f8);
    }

    @Override
    public void setF9(boolean f9) {
        boolean old = this.f9;
        this.f9 = f9;
        doSetFunction(9, Throttle.F9, old, this.f9);
    }

    @Override
    public void setF10(boolean f10) {
        boolean old = this.f10;
        this.f10 = f10;
        doSetFunction(10, Throttle.F10, old, this.f10);
    }

    @Override
    public void setF11(boolean f11) {
        boolean old = this.f11;
        this.f11 = f11;
        doSetFunction(11, Throttle.F11, old, this.f11);
    }

    @Override
    public void setF12(boolean f12) {
        boolean old = this.f12;
        this.f12 = f12;
        doSetFunction(12, Throttle.F12, old, this.f12);
    }

    @Override
    public void setF13(boolean f13) {
        boolean old = this.f13;
        this.f13 = f13;
        doSetFunction(13, Throttle.F13, old, this.f13);
    }

    @Override
    public void setF14(boolean f14) {
        boolean old = this.f14;
        this.f14 = f14;
        doSetFunction(14, Throttle.F14, old, this.f14);
    }

    @Override
    public void setF15(boolean f15) {
        boolean old = this.f15;
        this.f15 = f15;
        doSetFunction(15, Throttle.F15, old, this.f15);
    }

    @Override
    public void setF16(boolean f16) {
        boolean old = this.f16;
        this.f16 = f16;
        doSetFunction(16, Throttle.F16, old, this.f16);
    }

    @Override
    public void setF17(boolean f17) {
        boolean old = this.f17;
        this.f17 = f17;
        doSetFunction(17, Throttle.F17, old, this.f17);
    }

    @Override
    public void setF18(boolean f18) {
        boolean old = this.f18;
        this.f18 = f18;
        doSetFunction(18, Throttle.F18, old, this.f18);
    }

    @Override
    public void setF19(boolean f19) {
        boolean old = this.f19;
        this.f19 = f19;
        doSetFunction(19, Throttle.F19, old, this.f19);
    }

    @Override
    public void setF20(boolean f20) {
        boolean old = this.f20;
        this.f20 = f20;
        doSetFunction(20, Throttle.F20, old, this.f20);
    }

    @Override
    public void setF21(boolean f21) {
        boolean old = this.f21;
        this.f21 = f21;
        doSetFunction(21, Throttle.F21, old, this.f21);
    }

    @Override
    public void setF22(boolean f22) {
        boolean old = this.f22;
        this.f22 = f22;
        doSetFunction(22, Throttle.F22, old, this.f22);
    }

    @Override
    public void setF23(boolean f23) {
        boolean old = this.f23;
        this.f23 = f23;
        doSetFunction(23, Throttle.F23, old, this.f23);
    }

    @Override
    public void setF24(boolean f24) {
        boolean old = this.f24;
        this.f24 = f24;
        doSetFunction(24, Throttle.F24, old, this.f24);
    }

    @Override
    public void setF25(boolean f25) {
        boolean old = this.f25;
        this.f25 = f25;
        doSetFunction(25, Throttle.F25, old, this.f25);
    }

    @Override
    public void setF26(boolean f26) {
        boolean old = this.f26;
        this.f26 = f26;
        doSetFunction(26, Throttle.F26, old, this.f26);
    }

    @Override
    public void setF27(boolean f27) {
        boolean old = this.f27;
        this.f27 = f27;
        doSetFunction(27, Throttle.F27, old, this.f27);
    }

    @Override
    public void setF28(boolean f28) {
        boolean old = this.f28;
        this.f28 = f28;
        doSetFunction(28, Throttle.F28, old, this.f28);
    }

    protected void doSetFunction(int n, String name, boolean oldValue, boolean newValue) {
        // send to OpenLCB
        oti.setFunction(n, (newValue ? 1 : 0));

        if (oldValue != newValue) {
            notifyPropertyChangeListener(name, oldValue, newValue);
        }
    }

    @Override
    protected void throttleDispose() {
        log.debug("throttleDispose() called");
        finishRecord();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(OlcbThrottle.class);

}
