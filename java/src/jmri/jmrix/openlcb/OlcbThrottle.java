package jmri.jmrix.openlcb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottle;
import jmri.SystemConnectionMemo;
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
        synchronized(this) {
            this.speedSetting = 0;
        }
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

    final ThrottleImplementation oti;

    final DccLocoAddress address;

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
    public String toString() {
        return getLocoAddress().toString();
    }

    /**
     * Set the speed and direction
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public synchronized void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        if (speed > 1.0) {
            log.warn("Speed was set too high: {}", speed);
        }
        this.speedSetting = speed;

        // send to OpenLCB
        if (speed >= 0.0) {
            oti.setSpeed(speed * 100.0, isForward);
        } else {
            oti.doEmergencyStop();
        }

        // notify 
        firePropertyChange(SPEEDSETTING, oldSpeed, this.speedSetting);
        record(speed);
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        synchronized(this) {
            setSpeedSetting(speedSetting);  // send the command
        }
        firePropertyChange(ISFORWARD, old, isForward);
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public void setFunction(int functionNum, boolean newState) {
        updateFunction(functionNum, newState);
        // send to OpenLCB
        oti.setFunction(functionNum, (newState ? 1 : 0));
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public void throttleDispose() {
        log.debug("throttleDispose() called");
        finishRecord();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(OlcbThrottle.class);

}
