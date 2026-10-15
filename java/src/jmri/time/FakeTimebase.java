package jmri.time;

import java.time.ZonedDateTime;

/**
 * A fake Timebase.
 * This class is used temporary to figure out how to implement the new
 * TimeProvider interface.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public class FakeTimebase {


    public enum ClockInitialRunState { DO_NOTHING, DO_START, DO_STOP }


    private ZonedDateTime _time;
    private double _rate;
    private boolean _isRunning;
    private ClockInitialRunState _clockInitialRunState;
    private boolean _correctHardware;
//    private boolean _updateHardware;
    private boolean _master;
//    private boolean _update;
    private String _masterName;
    private boolean _setRateAtStart;
    private double _startRate;
    private double _usertRate;
    private boolean _showStopButton;
    private int _startClockOption;
    private boolean _startSetTime;
//    private ZonedDateTime _startSetTimeTime;
    private boolean _synchronize;
//    private boolean _synchronizeUpdate;
    private boolean _12HourDisplayDisplay;
//    private boolean _12HourDisplayUpdate;


    public ZonedDateTime getTime() {
        return _time;
    }

    public void setTime(ZonedDateTime time) {
        _time = time;
    }

    public double getRate() {
        return _rate;
    }

    public void setRate(double rate) {
        _rate = rate;
    }

    public boolean isRunning() {
        return _isRunning;
    }

    public void setRunning(boolean isRunning) {
        _isRunning = isRunning;
    }

    public boolean getInternalMaster() {
        return _master;
    }

    public void setInternalMaster(boolean master, boolean update) {
        _master = master;
//        _update = update;
    }

    public ClockInitialRunState getClockInitialRunState() {
        return _clockInitialRunState;
    }

    public void setClockInitialRunState(ClockInitialRunState clockInitialRunState) {
        _clockInitialRunState = clockInitialRunState;
    }

    public boolean getCorrectHardware() {
        return _correctHardware;
    }

    public void setCorrectHardware(boolean correct, boolean update) {
        _correctHardware = correct;
//        _updateHardware = update;
    }

    public String getMasterName() {
        return _masterName;
    }

    public void setMasterName(String masterName) {
        _masterName = masterName;
    }

    public boolean getSetRateAtStart() {
        return _setRateAtStart;
    }

    public void setSetRateAtStart(boolean setRateAtStart) {
        _setRateAtStart = setRateAtStart;
    }

    public double getStartRate() {
        return _startRate;
    }

    public void setStartRate(double startRate) {
        _startRate = startRate;
    }

    public double userGetRate() {
        return _usertRate;
    }

    public void userSetRate(double rate) {
        _usertRate = rate;
    }

    public boolean getShowStopButton() {
        return _showStopButton;
    }

    public void setShowStopButton(boolean showStopButton) {
        _showStopButton = showStopButton;
    }

    public int getStartClockOption() {
        return _startClockOption;
    }

    public void setStartClockOption(int startClockOption) {
        _startClockOption = startClockOption;
    }

    public boolean getStartSetTime() {
        return _startSetTime;
    }

    public void setStartSetTime(boolean startSetTime, ZonedDateTime startSetTimeTime) {
        _startSetTime = startSetTime;
//        _startSetTimeTime = startSetTimeTime;
    }

    public boolean getSynchronize() {
        return _synchronize;
    }

    public void setSynchronize(boolean synchronize, boolean update) {
        _synchronize = synchronize;
//        _synchronizeUpdate = update;
    }

    public boolean use12HourDisplay() {
        return _12HourDisplayDisplay;
    }

    public void set12HourDisplay(boolean display, boolean update) {
        _12HourDisplayDisplay = display;
//        _12HourDisplayUpdate = update;
    }

    public void initializeClock() {
    }

    public void initializeHardwareClock() {
    }






/*
    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public boolean getStartSetTime() {
        return _sstartSetTime;
    }

    public void seStartSetTime(boolean startSetTime) {
        _startSetTime = startSetTime;
    }

    public ZonedDateTime getTime() {
        return _time;
    }

    public void setTime(ZonedDateTime time) {
        _time = time;
    }

    public ZonedDateTime getTime() {
        return _time;
    }

    public void setTime(ZonedDateTime time) {
        _time = time;
    }

    public ZonedDateTime getTime() {
        return _time;
    }

    public void setTime(ZonedDateTime time) {
        _time = time;
    }

    public ZonedDateTime getTime() {
        return _time;
    }

    public void setTime(ZonedDateTime time) {
        _time = time;
    }
*/
}
