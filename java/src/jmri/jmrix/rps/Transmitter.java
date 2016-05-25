// Transmitter.java
package jmri.jmrix.rps;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleListener;

/**
 * Represents a RPS transmitter, generally a locomotive.
 * <p>
 * The "ID" is used to identify this transmitter in RPS. The "rosterName" is the
 * name (ID) of the roster entry this was originally created from.
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 * @version $Revision$
 */
public class Transmitter implements ThrottleListener {

    Transmitter(String id, boolean polled, int address, boolean longAddress) {
        setID(id);
        setPolled(polled);
        setAddress(address);
        setLongAddress(longAddress);
    }

    public String getID() {
        return id;
    }
    String id;

    public void setID(String id) {
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
        InstanceManager.throttleManagerInstance().requestThrottle(address, longAddress, this);
        return false;
    }

    public void notifyThrottleFound(DccThrottle t) {
        needReqThrottle = false;
        throttle = t;
    }

    public void notifyFailedThrottleRequest(jmri.DccLocoAddress address, String reason) {
    }
}
