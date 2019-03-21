package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.ListIterator;

/**
*/

public class RampData {

    private int _timeInterval;
    private float _throttleInterval;
    private ArrayList<Float> _settings;
    private float _rampLength;
    private boolean _upRamp;

    static float INCRE_RATE = 1.10f;  // multiplier to increase throttle increments

    RampData(float throttleIncre, int timeIncre) {
        _throttleInterval = throttleIncre; 
        _timeInterval = timeIncre;
    }
    
    protected boolean isUpRamp() {
        return _upRamp;
    }

    protected void makeThrottleSettings(float fromSet, float toSet) {
        _upRamp = (toSet >= fromSet);
        _settings = new ArrayList<Float>();
        float lowSetting;
        float highSetting;
        float throttleIncre = _throttleInterval;
        if (_upRamp) {
            lowSetting = fromSet;
            highSetting = toSet;
        } else {
            lowSetting = toSet;
            highSetting = fromSet;
        }
        while (lowSetting < highSetting) {
            _settings.add(Float.valueOf(lowSetting));
            lowSetting += throttleIncre;
            throttleIncre *= INCRE_RATE;
        }
        _settings.add(Float.valueOf(highSetting));
    }

    protected void setRampLength(float rampLength) {
        _rampLength =  rampLength;
    }

    protected float getRampLength() {
        return _rampLength;
    }

    protected float getMaxSpeed() {
        if (_settings == null) {
            throw new IllegalArgumentException("Null array of throttle settings"); 
        }
        return _settings.get(_settings.size() - 1).floatValue();
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

}