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
     */
    public OlcbThrottle(DccLocoAddress address, SystemConnectionMemo memo) {
        super(memo);
        OlcbInterface iface = memo.get(OlcbInterface.class);

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
        this.speedSetting = 0;
        // Functions default to false
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
        doSetFunction(0, f0);
    }

    @Override
    public void setF1(boolean f1) {
        doSetFunction(1, f1);
    }

    @Override
    public void setF2(boolean f2) {
        doSetFunction(2, f2);
    }

    @Override
    public void setF3(boolean f3) {
        doSetFunction(3, f3);
    }

    @Override
    public void setF4(boolean f4) {
        doSetFunction(4, f4);
    }

    @Override
    public void setF5(boolean f5) {
        doSetFunction(5, f5);
    }

    @Override
    public void setF6(boolean f6) {
        doSetFunction(6, f6);
    }

    @Override
    public void setF7(boolean f7) {
        doSetFunction(7, f7);
    }

    @Override
    public void setF8(boolean f8) {
        doSetFunction(8, f8);
    }

    @Override
    public void setF9(boolean f9) {
        doSetFunction(9, f9);
    }

    @Override
    public void setF10(boolean f10) {
        doSetFunction(10, f10);
    }

    @Override
    public void setF11(boolean f11) {
        doSetFunction(11, f11);
    }

    @Override
    public void setF12(boolean f12) {
        doSetFunction(12, f12);
    }

    @Override
    public void setF13(boolean f13) {
        doSetFunction(13, f13);
    }

    @Override
    public void setF14(boolean f14) {
        doSetFunction(14, f14);
    }

    @Override
    public void setF15(boolean f15) {
        doSetFunction(15, f15);
    }

    @Override
    public void setF16(boolean f16) {
        doSetFunction(16, f16);
    }

    @Override
    public void setF17(boolean f17) {
        doSetFunction(17, f17);
    }

    @Override
    public void setF18(boolean f18) {
        doSetFunction(18, f18);
    }

    @Override
    public void setF19(boolean f19) {
        doSetFunction(19, f19);
    }

    @Override
    public void setF20(boolean f20) {
        doSetFunction(20, f20);
    }

    @Override
    public void setF21(boolean f21) {
        doSetFunction(21, f21);
    }

    @Override
    public void setF22(boolean f22) {
        doSetFunction(22, f22);
    }

    @Override
    public void setF23(boolean f23) {
        doSetFunction(23, f23);
    }

    @Override
    public void setF24(boolean f24) {
        doSetFunction(24, f24);
    }

    @Override
    public void setF25(boolean f25) {
        doSetFunction(25, f25);
    }

    @Override
    public void setF26(boolean f26) {
        doSetFunction(26, f26);
    }

    @Override
    public void setF27(boolean f27) {
        doSetFunction(27, f27);
    }

    @Override
    public void setF28(boolean f28) {
        doSetFunction(28, f28);
    }
    
    protected void doSetFunction(int n, boolean newValue) {
        updateFunction(n, newValue);
        // send to OpenLCB
        oti.setFunction(n, (newValue ? 1 : 0));
    }

    @Override
    protected void throttleDispose() {
        log.debug("throttleDispose() called");
        finishRecord();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(OlcbThrottle.class);

}
