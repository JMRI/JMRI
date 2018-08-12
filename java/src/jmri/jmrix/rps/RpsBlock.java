package jmri.jmrix.rps;

import java.util.List;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.SignalHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a Block that can control a locomotive within a specific Block based on
 * an RpsSensor. It sets speed based on aspect of a specific signal
 *
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 */
public class RpsBlock implements java.beans.PropertyChangeListener, jmri.ThrottleListener {

    public RpsBlock(RpsSensor sensor, SignalHead signal, float slow, float fast) {
        this.sensor = sensor;
        this.signal = signal;
        this.slow = slow;
        this.fast = fast;

        this.sensor.addPropertyChangeListener(this);
        this.signal.addPropertyChangeListener(this);
    }

    public RpsBlock(String sensorname, String signalname, float slow, float fast) {
        this((RpsSensor) jmri.InstanceManager.sensorManagerInstance().getSensor(sensorname),
                jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalname),
                slow, fast);
    }

    float slow, fast;
    SignalHead signal = null;
    RpsSensor sensor = null;

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        handleParameterChange(e.getPropertyName(),
                e.getOldValue(), e.getNewValue(),
                e.getSource());
    }

    void handleParameterChange(String property,
            Object oldState, Object newState,
            Object source) {
        if (log.isDebugEnabled()) {
            log.debug("Change " + property + " from " + source);
        }

        if (property.equals("Arriving")) {
            arriving((Integer) newState);
        } else if (property.equals("Leaving")) {
            leaving((Integer) newState);
        } else if (property.equals("Appearance")) {
            appearance();
        } else {
            log.debug("Parameter ignored");
        }
    }

    // arriving and departing just checks who's here
    void arriving(Integer number) {
        acquireThrottle(number);
        updateCurrentThrottles();
    }

    void leaving(Integer number) {
        acquireThrottle(number);
    }

    void appearance() {
        updateCurrentThrottles();
    }

    void acquireThrottle(Integer num) {
        Object o = throttleTable.get(num);
        if (o != null) {
            return;  // already present
        }
    }

    @Override
    public void notifyThrottleFound(DccThrottle t) {
        // put in map
        Integer num = Integer.valueOf(((DccLocoAddress) t.getLocoAddress()).getNumber());
        throttleTable.put(num, t);
    }

    @Override
    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
    }

    @Override
    public void notifyStealThrottleRequired(LocoAddress address){
        // this is an automatically stealing impelementation.
        jmri.InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, true);
    }

    void updateCurrentThrottles() {
        List<Integer> l = sensor.getContents();
        if (l.size() == 0) {
            return;
        }
        if (l.size() > 1) {
            log.warn("More than one address present!");
        }
        for (int i = 0; i < l.size(); i++) {
            Integer num = l.get(i);
            DccThrottle t = throttleTable.get(num);
            if (t != null) {
                updateOneThrottle(t);
            } else {
                log.warn("Throttle not yet available for: " + num);
            }
        }

    }

    void updateOneThrottle(DccThrottle t) {
        // get proper speed
        int app = signal.getAppearance();
        switch (app) {
            case SignalHead.RED:
                t.setSpeedSetting(0.f);
                break;
            case SignalHead.GREEN:
                t.setSpeedSetting(fast);
                break;
            default:
                t.setSpeedSetting(slow);
                break;
        }
    }

    public void dispose() {
        sensor.removePropertyChangeListener(this);
    }

    static java.util.Hashtable<Integer, DccThrottle> throttleTable = new java.util.Hashtable<Integer, DccThrottle>();

    private final static Logger log = LoggerFactory.getLogger(RpsBlock.class);

}


