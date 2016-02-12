// DefaultRailCom.java
package jmri.implementation;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import jmri.DccLocoAddress;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of the {@link jmri.RailCom} interface.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Kevin Dickerson Copyright (C) 2012
 * @version $Revision: 17977 $
 * @since 2.99.3
 */
public class DefaultRailCom extends DefaultIdTag implements jmri.RailCom {

    /**
     *
     */
    private static final long serialVersionUID = -7822601724014273735L;
    private int _currentState = 0x00;

    public DefaultRailCom(String systemName) {
        super(systemName.toUpperCase());
        setWhereLastSeen(null);
    }

    public DefaultRailCom(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
        setWhereLastSeen(null);
    }

    public void setState(int s) throws JmriException {
        this._currentState = s;
    }

    public int getState() {
        return this._currentState;
    }

    int orientation = Sensor.UNKNOWN;

    public void setOrientation(int type) {
        if (type == orientation) {
            return;
        }
        int oldValue = orientation;
        orientation = type;
        firePropertyChange("orientation", oldValue, orientation);
    }

    public int getOrientation() {
        return orientation;
    }

    public String getAddressTypeAsString() {
        switch (addressTypeInt) {
            case SHORT_ADDRESS:
                return "Short";
            case LONG_ADDRESS:
                return "Long";
            case CONSIST_ADDRESS:
                return "Consist";
            default:
                return "No Address";
        }
    }

    public DccLocoAddress getDccLocoAddress() {
        boolean longAddress = false;
        if (addressTypeInt == LONG_ADDRESS) {
            longAddress = true;
        }
        return new DccLocoAddress(Integer.parseInt(getTagID()), longAddress);
    }

    int addressTypeInt = 0;

    public void setAddressType(int type) {
        addressTypeInt = type;
    }

    public int getAddressType() {
        return addressTypeInt;
    }

    int actual_speed = -1;

    public void setActualSpeed(int type) {
        if (type == actual_speed) {
            return;
        }
        int oldValue = actual_speed;
        actual_speed = type;
        firePropertyChange("actualspeed", oldValue, actual_speed);
    }

    public int getActualSpeed() {
        return actual_speed;
    }

    int actual_load = -1;

    public void setActualLoad(int type) {
        if (type == actual_load) {
            return;
        }
        int oldValue = actual_load;
        actual_load = type;
        firePropertyChange("actualload", oldValue, actual_load);
    }

    public int getActualLoad() {
        return actual_load;
    }

    int actual_temperature = -1;

    public void setActualTemperature(int type) {
        if (type == actual_temperature) {
            return;
        }
        int oldValue = actual_temperature;
        actual_temperature = type;
        firePropertyChange("actualtemperature", oldValue, actual_temperature);
    }

    public int getActualTemperature() {
        return actual_temperature;
    }

    int waterLevel = -1;

    public void setWaterLevel(int type) {
        if (type == waterLevel) {
            return;
        }
        int oldValue = waterLevel;
        waterLevel = type;
        firePropertyChange("waterlevel", oldValue, waterLevel);
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    int fuelLevel = -1;

    public void setFuelLevel(int type) {
        if (type == fuelLevel) {
            return;
        }
        int oldValue = fuelLevel;
        fuelLevel = type;
        firePropertyChange("fuellevel", oldValue, fuelLevel);
    }

    public int getFuelLevel() {
        return fuelLevel;
    }

    int location = -1;

    public void setLocation(int type) {
        if (type == location) {
            return;
        }
        int oldValue = location;
        location = type;
        firePropertyChange("location", oldValue, location);
    }

    public int getLocation() {
        return location;
    }

    int routing_no = -1;

    public void setRoutingNo(int type) {
        if (type == routing_no) {
            return;
        }
        int oldValue = routing_no;
        routing_no = type;
        firePropertyChange("routing", oldValue, routing_no);
    }

    public int getRoutingNo() {
        return routing_no;
    }

    int expectedCV = -1;

    public void setExpectedCv(int cv) {
        expectedCV = cv;
    }

    public int getExpectedCv() {
        return expectedCV;
    }

    public void setCvValue(int value) {
        if (expectedCV == -1) {
            log.debug("set cv value called but no CV is expected");
            return;
        }
        int exp = expectedCV;
        expectedCV = -1;
        setCv(exp, value);
    }

    public int getCv(int cv) {
        if (cvValues.containsKey(cv)) {
            return cvValues.get(cv);
        }
        return 0;
    }

    public void setCv(int cv, int value) {
        if (cvValues.containsKey(cv)) {
            if (cvValues.get(cv) == value) {
                firePropertyChange("cvvalue", cv, value);
                return;
            }
        }
        cvValues.put(cv, value);
        firePropertyChange("cvvalue", cv, value);
    }

    public List<Integer> getCVList() {
        int[] arr = new int[cvValues.size()];
        List<Integer> out = new ArrayList<Integer>();
        Enumeration<Integer> en = cvValues.keys();
        int i = 0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        //jmri.util.StringUtil.sort(arr);
        for (i = 0; i < arr.length; i++) {
            out.add(arr[i]);
        }
        return out;
    }

    Hashtable<Integer, Integer> cvValues = new Hashtable<Integer, Integer>();

    public String toString() {
        String comment = "";
        if (getOrientation() == jmri.RailCom.ORIENTA) {
            comment = "Orientation A ";
        } else if (getOrientation() == jmri.RailCom.ORIENTB) {
            comment = "Orientation B ";
        } else if (getOrientation() == Sensor.UNKNOWN) {
            comment = "Unknown Orientation ";
        }
        comment = comment + "Address " + getDccLocoAddress() + " ";

        if (getWaterLevel() != -1) {
            comment = "Water " + getWaterLevel() + " ";
        }
        if (getFuelLevel() != -1) {
            comment = "Fuel " + getFuelLevel() + " ";
        }
        if ((getLocation() != -1)) {
            comment = comment + "Location : " + getLocation() + " ";
        }
        if ((getRoutingNo() != -1)) {
            comment = comment + "Routing No : " + getRoutingNo() + " ";
        }
        if ((getActualTemperature() != -1)) {
            comment = comment + "Temperature : " + getActualTemperature() + " ";
        }
        if ((getActualLoad() != -1)) {
            comment = comment + "Load : " + getActualLoad() + " ";
        }
        if ((getActualSpeed() != -1)) {
            comment = comment + "Speed : " + getActualSpeed();
        }
        return comment;
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultRailCom.class.getName());
}

/* @(#)DefaultRailCom.java */
