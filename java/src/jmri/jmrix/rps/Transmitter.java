package jmri.jmrix.rps;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleListener;

/**
 * Represents an RPS transmitter, generally a locomotive.
 * <p>
 * The "ID" is used to identify this transmitter in RPS. The "rosterName" is the
 * name (ID) of the roster entry this was originally created from.
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 */
public class Transmitter implements ThrottleListener {

    Transmitter(String id, boolean polled, int address, boolean longAddress) {
        setId(id);
        setPolled(polled);
        setAddress(address);
        setLongAddress(longAddress);
    }

    public String getId() {
        return id;
    }
    String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getRosterName() {
        return rosterName;
    }
    String rosterName;

    public void setRosterName(String rosterName) {
        this.rosterName = rosterName;
    }

    public boolean isLongAddress() {
        return longAddress;
    }
    boolean longAddress;

    public void setLongAddress(boolean longAddress) {
        this.longAddress = longAddress;
    }

    public int getAddress() {
        return address;
    }
    int address;

    public void setAddress(int address) {
        this.address = address;
    }

    public boolean isPolled() {
        return polled;
    }
    boolean polled;

    public void setPolled(boolean polled) {
        this.polled = polled;
    }

    Measurement lastMeasurement = null;

    public void setLastMeasurement(Measurement last) {
        lastMeasurement = last;
    }

    public Measurement getLastMeasurement() {
        return lastMeasurement;
    }

    // stuff to do F2 poll
    DccThrottle throttle;
    boolean needReqThrottle = true;

    DccThrottle getThrottle() {
        return throttle;
    }

    boolean checkInit() {
        if (throttle != null) {
            return true;
        }
        if (!needReqThrottle) {
            return false;
        }
        // request throttle
        InstanceManager.throttleManagerInstance().requestThrottle(
            new jmri.DccLocoAddress(address, longAddress), this, false);
        return false;
    }

    @Override
    public void notifyThrottleFound(DccThrottle t) {
        needReqThrottle = false;
        throttle = t;
    }

    @Override
    public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
    }
    
    /**
     * {@inheritDoc}
     * @deprecated since 4.15.7; use #notifyDecisionRequired
     */
    @Override
    @Deprecated
    public void notifyStealThrottleRequired(jmri.LocoAddress address) {
        InstanceManager.throttleManagerInstance().responseThrottleDecision(address, this, DecisionType.STEAL );
    }

    /**
     * No steal or share decisions made locally
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void notifyDecisionRequired(jmri.LocoAddress address, DecisionType question) {
    }

}
