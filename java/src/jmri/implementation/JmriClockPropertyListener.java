package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.util.Date;
import jmri.Conditional;
import jmri.InstanceManager;
import jmri.Timebase;

/**
 * A service class for monitoring a bound property in one of the JMRI Named
 * beans For use with properties having two states which are determined by
 * containment in an interval (e.g. Fast Clock ranges).
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Pete Cressman Copyright (C) 2009
 * @since 2.5.1
 */
public class JmriClockPropertyListener extends JmriSimplePropertyListener {

    static int SIZE = 10;
    int numRanges = 0;
    int[] _beginTimes = new int[SIZE];
    int[] _endTimes = new int[SIZE];
    boolean[] _rangeList = new boolean[SIZE];
    Timebase _fastClock;
    int _currentMinutes;

    @SuppressWarnings("deprecation")
    JmriClockPropertyListener(String propName, int type, String name, Conditional.Type varType,
            Conditional client, int beginTime, int endTime) {
        super(propName, type, name, varType, client);
        _beginTimes[0] = fixMidnight(beginTime);
        _endTimes[0] = fixMidnight(endTime);
        _rangeList[0] = false;
        numRanges = 1;
        _fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        Date currentTime = _fastClock.getTime();
        _currentMinutes = (currentTime.getHours() * 60) + currentTime.getMinutes();
    }

    private int fixMidnight(int time) {
        if (time > 24 * 60) {
            time -= 24 * 60;
        }
        return time;
    }

    public void setRange(int beginTime, int endTime) {
        if (numRanges >= _rangeList.length) {
            int[] temp = new int[numRanges + SIZE];
            System.arraycopy(_beginTimes, 0, temp, 0, _beginTimes.length);
            _beginTimes = temp;
            temp = new int[numRanges + SIZE];
            System.arraycopy(_endTimes, 0, temp, 0, _endTimes.length);
            _endTimes = temp;
            boolean[] bools = new boolean[numRanges + SIZE];
            System.arraycopy(_rangeList, 0, bools, 0, _rangeList.length);
            _rangeList = bools;
        }
        _beginTimes[numRanges] = fixMidnight(beginTime);
        _endTimes[numRanges] = fixMidnight(endTime);
        _rangeList[numRanges] = false;
        numRanges++;
    }

    /**
     * Check if have entered/exited one of the Fast Clock Ranges
     * <p>
     * This method is invoked when the minute listener fires.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Date currentTime = _fastClock.getTime();
        //int oldMinutes = _currentMinutes;
        _currentMinutes = (currentTime.getHours() * 60) + currentTime.getMinutes();
        // check if we have entered or left one of the ranges
        boolean[] newRangeList = new boolean[_rangeList.length];
        for (int i = 0; i < numRanges; i++) {
            // check if entered or left desired time range
            if (_beginTimes[i] < _endTimes[i]) {
                // range not crossing midnight, test ends of range
                if ((_beginTimes[i] <= _currentMinutes) && (_currentMinutes <= _endTimes[i])) {
                    newRangeList[i] = true;
                } else {
                    newRangeList[i] = false;
                }
            } else { // range crosses midnight
                if ((_beginTimes[i] <= _currentMinutes) || (_currentMinutes <= _endTimes[i])) {
                    newRangeList[i] = true;
                } else {
                    newRangeList[i] = false;
                }
            }
            //log.debug("currentMinutes= "+_currentMinutes+" beginTime= "+_beginTimes[i]+
            //          " endTime="+_endTimes[i]+"new= "+newRangeList[i]+" old= "+_rangeList[i]);
        }
        // check for changes
        for (int i = 0; i < numRanges; i++) {
            if (_rangeList[i] != newRangeList[i]) {
                _rangeList = newRangeList;
                super.propertyChange(evt);
            }
        }
    }
}
