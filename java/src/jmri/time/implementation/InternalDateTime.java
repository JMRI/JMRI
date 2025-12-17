package jmri.time.implementation;

import java.time.*;
import java.time.temporal.*;
import java.util.TimerTask;

import jmri.time.*;
import jmri.time.rate.ChangeableDoubleRate;
import jmri.util.TimerUtil;

/**
 * The system date and time.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class InternalDateTime extends AbstractTimeProvider
        implements TimeSetter, DateProvider, DateSetter, CanSetRate, StartStopTimeProvider, RateSetter {

    private static final int _100_MILLISECONDS = 100;

    private LocalDateTime _time = LocalDateTime.now();
    private final ChangeableDoubleRate _rate = new ChangeableDoubleRate(1.0);
    private boolean _isRunning;
    private boolean _isLockedFromRunning;
    private final TimerTask _timerTask;
    private LocalDateTime _lastDateTime;
    private LocalDateTime _lastUpdatedDateTime;
    private int _lastSec;
    private int _lastMin;
    private long _startTimeMillisec;
    private final Object _lock = new Object();


    private TimerTask getTimerTask() {
        /*
        Lägg till starta/stoppa klockan.
        Lägg till ändra hastighet (rate) på klockan.
        */

        return new TimerTask() {
            @Override
            public void run() {
                synchronized(_lock) {
                    if (!_isRunning || _isLockedFromRunning) return;
                    if (_rate.getRate() < 0.0001) return;   // We don't want to divide by zero later

                    long time = System.currentTimeMillis() - _startTimeMillisec;
                    long diff = Math.round(time * _rate.getRate());
                    _time = _lastDateTime.plus(diff, ChronoUnit.MILLIS);
                    int sec = _time.getSecond();
                    if (sec != _lastSec) {
                        int min = _time.getMinute();
                        InternalDateTime.this.firePropertyChange(PROPERTY_CHANGE_SECONDS, _lastSec, sec);
                        if (min != _lastMin) {
                            InternalDateTime.this.firePropertyChange(PROPERTY_CHANGE_MINUTES, _lastMin, min);
                        }
                        InternalDateTime.this.firePropertyChange(PROPERTY_CHANGE_DATETIME, _lastUpdatedDateTime, _time);
                        _lastSec = sec;
                        _lastMin = min;
                        _lastUpdatedDateTime = _time;
                    }
                }
            }
        };
    }

    /**
     * Creates an instance of SystemDateTime.
     * the caller must call {@link #init()} afterwards.
     * @param systemName the system name
     */
    public InternalDateTime(String systemName) {
        super(systemName);
        _timerTask = getTimerTask();
    }

    /**
     * Creates an instance of SystemDateTime.
     * the caller must call {@link #init()} afterwards.
     * @param systemName  the system name
     * @param userName    the user name
     */
    public InternalDateTime(String systemName, String userName) {
        super(systemName, userName);
        _timerTask = getTimerTask();
    }

    public InternalDateTime init() {
        _time = LocalDateTime.now();
        resetLast();
        _startTimeMillisec = System.currentTimeMillis();
        TimerUtil.schedule(_timerTask, System.currentTimeMillis() % _100_MILLISECONDS, _100_MILLISECONDS);
        return this;
    }

    /**
     * Lock the clock from running.
     * Used by LoadAndStore tests.
     */
    public void lockFromRunning() {
        synchronized(_lock) {
            _isLockedFromRunning = true;
        }
    }

    private void resetLast() {
        _lastDateTime = _time;
        _lastUpdatedDateTime = _time;
        _lastSec = _lastDateTime.getSecond();
        _lastMin = _lastDateTime.getMinute();
    }

    /** {@inheritDoc} */
    @Override
    public LocalDateTime getTime() {
        synchronized(_lock) {
           return _time;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Rate getRate() {
        synchronized(_lock) {
            return _rate;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRunning() {
        synchronized(_lock) {
            return _isRunning;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasWeekday() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasDayOfMonth() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasMonth() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasYear() {
        return true;
    }

    @Override
    public void start() throws UnsupportedOperationException {
        synchronized(_lock) {
            _isRunning = true;
        }
    }

    @Override
    public void stop() throws UnsupportedOperationException {
        synchronized(_lock) {
            _isRunning = false;
        }
    }

    @Override
    public boolean canStartAndStop() {
        return true;
    }

    @Override
    public void setTime(LocalTime time) throws UnsupportedOperationException {
        synchronized(_lock) {
            LocalDateTime oldTime = this._time;
            _time = LocalDateTime.of(_time.toLocalDate(), time);
            resetLast();
            timeIsUpdated(oldTime);
        }
    }

    @Override
    public void setDateTime(LocalDateTime time) throws UnsupportedOperationException {
        synchronized(_lock) {
            LocalDateTime oldTime = this._time;
            this._time = time;
            resetLast();
            timeIsUpdated(oldTime);
        }
    }

    @Override
    public boolean canSetDateTime() {
        return true;
    }

    @Override
    public void setWeekday(int dayOfWeek) throws UnsupportedOperationException {
        synchronized(_lock) {
            LocalDateTime oldTime = this._time;
            _time = _time.with(ChronoField.DAY_OF_WEEK, dayOfWeek);
            resetLast();
            timeIsUpdated(oldTime);
        }
    }

    @Override
    public void setDayOfMonth(int day) throws UnsupportedOperationException {
        synchronized(_lock) {
            LocalDateTime oldTime = this._time;
            _time = _time.withDayOfMonth(day);
            resetLast();
            timeIsUpdated(oldTime);
        }
    }

    @Override
    public void setMonth(int month) throws UnsupportedOperationException {
        synchronized(_lock) {
            LocalDateTime oldTime = this._time;
            _time = _time.withMonth(month);
            resetLast();
            timeIsUpdated(oldTime);
        }
    }

    @Override
    public void setYear(int year) throws UnsupportedOperationException {
        synchronized(_lock) {
            LocalDateTime oldTime = this._time;
            _time = _time.withYear(year);
            resetLast();
            timeIsUpdated(oldTime);
        }
    }

    @Override
    public boolean canSetDate() {
        return true;
    }

    @Override
    public boolean canSetRate() {
        return true;
    }

    @Override
    public void setRate(double rate) {
        synchronized(_lock) {
            double oldRate = _rate.getRate();
            _rate.setRate(rate);
            firePropertyChange(PROPERTY_CHANGE_RATE, oldRate, _rate.getRate());
        }
    }

}
