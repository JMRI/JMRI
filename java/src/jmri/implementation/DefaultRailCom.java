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
 * @since 2.99.3
 */
public class DefaultRailCom extends DefaultIdTag implements jmri.RailCom {

    private int currentState = 0x00;

    public DefaultRailCom(String systemName) {
        super(systemName.toUpperCase());
        setWhereLastSeen(null);
    }

    public DefaultRailCom(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
        setWhereLastSeen(null);
    }

    @Override
    public void setState(int s) throws JmriException {
        this.currentState = s;
    }

    @Override
    public int getState() {
        return this.currentState;
    }

    int orientation = Sensor.UNKNOWN;

    @Override
    public void setOrientation(int type) {
        if (type == orientation) {
            return;
        }
        int oldValue = orientation;
        orientation = type;
        firePropertyChange("orientation", oldValue, orientation);
    }

    @Override
    public int getOrientation() {
        return orientation;
    }

    @Override
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

    @Override
    public DccLocoAddress getDccLocoAddress() {
        boolean longAddress = false;
        if (addressTypeInt == LONG_ADDRESS) {
            longAddress = true;
        }
        return new DccLocoAddress(Integer.parseInt(getTagID()), longAddress);
    }

    int addressTypeInt = 0;

    @Override
    public void setAddressType(int type) {
        addressTypeInt = type;
    }

    @Override
    public int getAddressType() {
        return addressTypeInt;
    }

    private int actualSpeed = -1;

    @Override
    public void setActualSpeed(int type) {
        if (type == actualSpeed) {
            return;
        }
        int oldValue = actualSpeed;
        actualSpeed = type;
        firePropertyChange("actualspeed", oldValue, actualSpeed);
    }

    @Override
    public int getActualSpeed() {
        return actualSpeed;
    }

    private int actualLoad = -1;

    @Override
    public void setActualLoad(int type) {
        if (type == actualLoad) {
            return;
        }
        int oldValue = actualLoad;
        actualLoad = type;
        firePropertyChange("actualload", oldValue, actualLoad);
    }

    @Override
    public int getActualLoad() {
        return actualLoad;
    }

    private int actualTemperature = -1;

    @Override
    public void setActualTemperature(int type) {
        if (type == actualTemperature) {
            return;
        }
        int oldValue = actualTemperature;
        actualTemperature = type;
        firePropertyChange("actualtemperature", oldValue, actualTemperature);
    }

    @Override
    public int getActualTemperature() {
        return actualTemperature;
    }

    private int waterLevel = -1;

    @Override
    public void setWaterLevel(int type) {
        if (type == waterLevel) {
            return;
        }
        int oldValue = waterLevel;
        waterLevel = type;
        firePropertyChange("waterlevel", oldValue, waterLevel);
    }

    @Override
    public int getWaterLevel() {
        return waterLevel;
    }

    private int fuelLevel = -1;

    @Override
    public void setFuelLevel(int type) {
        if (type == fuelLevel) {
            return;
        }
        int oldValue = fuelLevel;
        fuelLevel = type;
        firePropertyChange("fuellevel", oldValue, fuelLevel);
    }

    @Override
    public int getFuelLevel() {
        return fuelLevel;
    }

    private int location = -1;

    @Override
    public void setLocation(int type) {
        if (type == location) {
            return;
        }
        int oldValue = location;
        location = type;
        firePropertyChange("location", oldValue, location);
    }

    @Override
    public int getLocation() {
        return location;
    }

    private int routingNo = -1;

    @Override
    public void setRoutingNo(int type) {
        if (type == routingNo) {
            return;
        }
        int oldValue = routingNo;
        routingNo = type;
        firePropertyChange("routing", oldValue, routingNo);
    }

    @Override
    public int getRoutingNo() {
        return routingNo;
    }

    private int expectedCV = -1;

    @Override
    public void setExpectedCv(int cv) {
        expectedCV = cv;
    }

    @Override
    public int getExpectedCv() {
        return expectedCV;
    }

    @Override
    public void setCvValue(int value) {
        if (expectedCV == -1) {
            log.debug("set cv value called but no CV is expected");
            return;
        }
        int exp = expectedCV;
        expectedCV = -1;
        setCv(exp, value);
    }

    @Override
    public int getCv(int cv) {
        if (cvValues.containsKey(cv)) {
            return cvValues.get(cv);
        }
        return 0;
    }

    @Override
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

    @Override
    public List<Integer> getCVList() {
        int[] arr = new int[cvValues.size()];
        List<Integer> out = new ArrayList<>();
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

    Hashtable<Integer, Integer> cvValues = new Hashtable<>();

    @Override
    public String toString() {
        String comment;
        switch (getOrientation()) {
            case ORIENTA:
                comment = "Orientation A ";
                break;
            case ORIENTB:
                comment = "Orientation B ";
                break;
            case UNKNOWN:
                comment = "Unknown Orientation ";
                break;
            default:
                comment = "Unknown Orientation ";
                break;
        }
        comment = comment + "Address " + getDccLocoAddress() + " ";

        if (getWaterLevel() != -1) {
            comment = comment + "Water " + getWaterLevel() + " ";
        }
        if (getFuelLevel() != -1) {
            comment = comment + "Fuel " + getFuelLevel() + " ";
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
