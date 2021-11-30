package jmri.implementation;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of the {@link jmri.RailCom} interface.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 *
 * @author Kevin Dickerson Copyright (C) 2012
 * @since 2.99.3
 */
public class DefaultRailCom extends DefaultIdTag implements jmri.RailCom {

    private int currentState = 0x00;

    public DefaultRailCom(String systemName) {
        super(systemName);
        setWhereLastSeen(null);
    }

    public DefaultRailCom(String systemName, String userName) {
        super(systemName, userName);
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

    @Override
    public void setOrientation(int type) {
        setProperty("orientation", type);
    }

    @Override
    public int getOrientation() {
        Integer t = (Integer)getProperty("orientation");
        return ( t != null ? t : Sensor.UNKNOWN );
    }

    @Override
    public void setActualSpeed(int type) {
        setProperty("actualspeed", type);
    }

    @Override
    public int getActualSpeed() {
        Integer t = (Integer)getProperty("actualspeed");
        return ( t != null ? t : -1 );
    }

    @Override
    public void setActualLoad(int type) {
        setProperty("actualload", type);
    }

    @Override
    public int getActualLoad() {
        Integer t = (Integer)getProperty("actualload");
        return ( t != null ? t : -1 );
    }

    @Override
    public void setActualTemperature(int type) {
        setProperty("actualtemperature", type);
    }

    @Override
    public int getActualTemperature() {
        Integer t = (Integer)getProperty("actualtemperature");
        return ( t != null ? t : -1 );
    }

    @Override
    public void setWaterLevel(int type) {
        setProperty("waterlevel", type);
    }

    @Override
    public int getWaterLevel() {
        Integer t = (Integer)getProperty("waterlevel");
        return ( t != null ? t : -1 );
    }

    @Override
    public void setFuelLevel(int type) {
        setProperty("fuellevel", type);
    }

    @Override
    public int getFuelLevel() {
        Integer t = (Integer)getProperty("fuellevel");
        return ( t != null ? t : -1 );
    }

    @Override
    public void setLocation(int type) {
        setProperty("location", type);
    }

    @Override
    public int getLocation() {
        Integer t = (Integer)getProperty("location");
        return ( t != null ? t : -1 );
    }

    @Override
    public void setRoutingNo(int type) {
        setProperty("routing", type);
    }

    @Override
    public int getRoutingNo() {
        Integer t = (Integer)getProperty("routing");
        return ( t != null ? t : -1 );
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
        setCV(exp, value);
    }

    @Override
    public int getCV(int cv) {
        if (cvValues.containsKey(cv)) {
            return cvValues.get(cv);
        }
        return 0;
    }

    @Override
    public void setCV(int cv, int value) {
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
    public String toReportString() {
        StringBuilder sb = new StringBuilder(200);
        switch (getOrientation()) {
            case ORIENTA:
                sb.append("Orientation A ");
                break;
            case ORIENTB:
                sb.append( "Orientation B ");
                break;
            case UNKNOWN:
            default:
                sb.append( "Unknown Orientation ");
                break;
        }
        sb.append("Address ").append(getLocoAddress()).append(" ");

        if (getWaterLevel() != -1) {
            sb.append("Water ").append(getWaterLevel()).append(" ");
        }
        if (getFuelLevel() != -1) {
            sb.append("Fuel ").append(getFuelLevel()).append(" ");
        }
        if ((getLocation() != -1)) {
            sb.append("Location : ").append(getLocation()).append(" ");
        }
        if ((getRoutingNo() != -1)) {
            sb.append("Routing No : ").append(getRoutingNo()).append(" ");
        }
        if ((getActualTemperature() != -1)) {
            sb.append("Temperature : ").append(getActualTemperature()).append(" ");
        }
        if ((getActualLoad() != -1)) {
            sb.append("Load : ").append(getActualLoad()).append(" ");
        }
        if ((getActualSpeed() != -1)) {
            sb.append("Speed : ").append(getActualSpeed()).append(" ");
        }
        return sb.toString();
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultRailCom.class);
}
