package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * This class holds a list of throttle setting to make a smooth acceleration or
 * deceleration. It supplies iterators to cycle through the settings.
 * Used when speed changes are called for by signaled speeds, block speed limits
 * or user controls for speed halts and resumes.  Also used to make NXWarrants.
 *  
 * @author Pete Cressman Copyright (C) 2019
*/

public class RampData {

    private final int _timeInterval;
    private final float _throttleInterval;
    private ArrayList<Float> _settings;
    private boolean _upRamp;
    private final SpeedUtil _speedUtil;
    private final float _fromSpeed;
    private final float _toSpeed;

    private static final float INCRE_RATE = 1.085f;  // multiplier to increase throttle increments

    RampData(SpeedUtil util, float throttleIncre, int timeIncre, float fromSet, float toSet) {
        _throttleInterval = throttleIncre; 
        _timeInterval = timeIncre;
        _speedUtil = util;
        _fromSpeed = fromSet;
        _toSpeed = toSet;
        makeThrottleSettings();
    }
    
    protected boolean isUpRamp() {
        return _upRamp;
    }

    private void makeThrottleSettings() {
        _upRamp = (_toSpeed >= _fromSpeed);
        _settings = new ArrayList<>();
        float lowSetting;
        float highSetting;
        float momentumTime;
        if (_upRamp) {
            lowSetting = _fromSpeed;
            highSetting = _toSpeed;
        } else {
            lowSetting = _toSpeed;
            highSetting = _fromSpeed;
        }
        float low = 0.0f;
        float throttleIncre = _throttleInterval;
        while (low < lowSetting ) {
            throttleIncre *= INCRE_RATE;
            low += throttleIncre;
        }
        _settings.add(lowSetting);
        lowSetting += throttleIncre;
        while (lowSetting < highSetting) {
            _settings.add(lowSetting);
            momentumTime = _speedUtil.getMomentumTime(lowSetting, lowSetting + throttleIncre*INCRE_RATE);
            if (momentumTime <= _timeInterval) {
                throttleIncre *= INCRE_RATE;
            }  // if time of momentum change exceeds _throttleInterval, don't increase throttleIncre
            lowSetting += throttleIncre;
        }
        _settings.add(highSetting);
    }

    protected float getRampLength() {
        float rampLength = 0;
        float nextSetting;
        float prevSetting;
        float momentumTime = 0;
        float dist;
        if (_upRamp) {
            ListIterator<Float> iter = speedIterator(true);
            prevSetting = iter.next(); // first setting is current speed
            nextSetting = prevSetting;
            while (iter.hasNext()) {
                nextSetting = iter.next();
                dist = _speedUtil.getDistanceOfSpeedChange(prevSetting, nextSetting, _timeInterval);
                rampLength += dist;
                momentumTime = _speedUtil.getMomentumTime(prevSetting, nextSetting);
                prevSetting = nextSetting;
            }
        } else {
            ListIterator<Float> iter = speedIterator(false);
            prevSetting = iter.previous(); // first setting is current speed
            nextSetting = prevSetting;
            while (iter.hasPrevious()) {
                nextSetting = iter.previous();
                dist = _speedUtil.getDistanceOfSpeedChange(prevSetting, nextSetting, _timeInterval);
                rampLength += dist;
                momentumTime = _speedUtil.getMomentumTime(prevSetting, nextSetting);
                prevSetting = nextSetting;
            }
        }
        // distance of the last speed increment is only distance needed for momentum.
        // _speedUtil.getDistanceOfSpeedChange will not return a distance greater than that needed by momentum
        if (_timeInterval > momentumTime) {
            rampLength -= _speedUtil.getTrackSpeed(nextSetting) * (_timeInterval - momentumTime);  
        }
        return rampLength;
    }

    protected int getNumSteps() {
        return _settings.size() - 1;
    }

    protected int getRamptime() {
        return (_settings.size() - 1) * _timeInterval;
    }

    protected float getMaxSpeed() {
        if (_settings == null) {
            throw new IllegalArgumentException("Null array of throttle settings"); 
        }
        return _settings.get(_settings.size() - 1);
    }

    protected ListIterator<Float> speedIterator(boolean up) {
        if (up) {
            return _settings.listIterator(0);
        } else {
            return _settings.listIterator(_settings.size());
            
        }
    }

    protected int getRampTimeIncrement() {
        return _timeInterval;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RampData.class);
}
