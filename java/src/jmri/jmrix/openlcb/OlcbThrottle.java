package jmri.jmrix.openlcb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;
import jmri.SystemConnectionMemo;

import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.implementations.VersionedValueListener;
import org.openlcb.implementations.throttle.RemoteTrainNode;
import org.openlcb.implementations.throttle.ThrottleImplementation;
import org.openlcb.implementations.throttle.TractionThrottle;
import org.openlcb.messages.TractionControlRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.openlcb.messages.TractionControlRequestMessage.MPH;

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
            speedStepMode = SpeedStepMode.NMRA_DCC_128;
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
        ot = new TractionThrottle(iface);
        NodeID nid;
        if (address instanceof OpenLcbLocoAddress) {
            nid = ((OpenLcbLocoAddress) address).getNode();
        } else {
            int dccAddress = this.address.getNumber();
            if (this.address.isLongAddress()) {
                nid = new NodeID(new byte[]{6, 1, 0, 0, (byte) (((dccAddress >> 8) & 0xFF) | 0xC0),
                        (byte) (dccAddress & 0xFF)});
            } else {
                nid = new NodeID(new byte[]{6, 1, 0, 0, 0, (byte) (dccAddress & 0xFF)});
            }
        }
        ot.start(new RemoteTrainNode(nid, iface));

        speedListener = new VersionedValueListener<Float>(ot.getSpeed()) {
            @Override
            public void update(Float speedAndDir) {
                float newSpeed;
                float direction = Math.copySign(1.0f, speedAndDir);
                if (speedAndDir.isNaN()) {
                    // e-stop
                    newSpeed = -1.0f;
                    direction = isForward ? 1.0f : -1.0f;
                } else {
                    newSpeed = speedAndDir / (126 * (float) MPH);
                    if (direction < 0) {
                        newSpeed = -newSpeed;
                    }
                }
                float oldSpeed;
                boolean oldDir;
                synchronized(OlcbThrottle.this) {
                    oldSpeed = speedSetting;
                    oldDir = isForward;
                    speedSetting = newSpeed;
                    isForward = direction > 0;
                    log.debug("Speed listener update old {} new {}", oldSpeed, speedSetting);
                    firePropertyChange(SPEEDSETTING, oldSpeed, speedSetting);
                    if (oldDir != isForward) {
                        firePropertyChange(ISFORWARD, oldDir, isForward);
                    }
                }
            }
        };
        for (int i = 0; i <= 28; i++) {
            int finalI = i;
            fnListeners.add(new VersionedValueListener<Boolean>(ot.getFunction(finalI)) {
                @Override
                public void update(Boolean state) {
                   updateFunction(finalI, state);
                }
            });
        }
    }

    final TractionThrottle ot;

    final DccLocoAddress address;
    VersionedValueListener<Float> speedListener;
    List<VersionedValueListener<Boolean>> fnListeners = new ArrayList<>();

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
            speedListener.setFromOwner(getSpeedAndDir());
        } else {
            speedListener.setFromOwner(Float.NaN);
        }
        log.debug("Speed set update old {} new {} int", oldSpeed, speedSetting);

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
            speedListener.setFromOwner(getSpeedAndDir());
        }
        firePropertyChange(ISFORWARD, old, isForward);
    }

    /**
     * @return the speed and direction as an OpenLCB value.
     */
    private float getSpeedAndDir() {
        float sp = speedSetting * 126 * (float)MPH;
        if (speedSetting < 0) {
            // e-stop is encoded as negative speed setting.
            sp = 0;
        }
        return Math.copySign(sp, isForward ? 1.0f : -1.0f);
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public void setFunction(int functionNum, boolean newState) {
        updateFunction(functionNum, newState);
        // send to OpenLCB
        fnListeners.get(functionNum).setFromOwner(newState);
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public void throttleDispose() {
        log.debug("throttleDispose() called for address {}", address);
        speedListener.release();
        for (VersionedValueListener<Boolean> l: fnListeners) {
            l.release();
        }
        ot.release();
        finishRecord();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(OlcbThrottle.class);

}
