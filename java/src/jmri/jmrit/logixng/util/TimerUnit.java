package jmri.jmrit.logixng.util;

import java.text.MessageFormat;

/**
 * Units for timer classes.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public enum TimerUnit {
    
    MilliSeconds(1, Bundle.getMessage("TimerUnit_UnitMilliSeconds"), Bundle.getMessage("TimerUnit_TimeMilliSeconds")),
    Seconds(1000, Bundle.getMessage("TimerUnit_UnitSeconds"), Bundle.getMessage("TimerUnit_TimeSeconds")),
    Minutes(1000 * 60, Bundle.getMessage("TimerUnit_UnitMinutes"), Bundle.getMessage("TimerUnit_TimeMinutes")),
    Hours(1000 * 60 * 60, Bundle.getMessage("TimerUnit_UnitHours"), Bundle.getMessage("TimerUnit_TimeHours"));
    
    private final long _multiply;
    private final String _text;
    private final String _timeText;
    
    private TimerUnit(long multiply, String text, String timeText) {
        this._multiply = multiply;
        this._text = text;
        this._timeText = timeText;
    }
    
    public long getMultiply() {
        return _multiply;
    }
    
    @Override
    public String toString() {
        return _text;
    }
    
    public String getTimeWithUnit(int time) {
        return MessageFormat.format(_timeText, time);
    }
    
}
