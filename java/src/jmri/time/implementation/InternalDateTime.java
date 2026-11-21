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
        implements TimeSetter, DateProvider, CanSetRate, StartStopTimeProvider {

    private static final int _100_MILLISECONDS = 100;

    // We cannot have _time synchronized with _lock since that could lead to deadlock.
    // So _time is volatile instead. This works since ZonedDateTime is immutable.
    private volatile ZonedDateTime _time = ZonedDateTime.now();

    // Changing the rate catches up the time first, so the clock doesn't jump
    private final ChangeableDoubleRate _rate = new ChangeableDoubleRate(1.0) {
        @Override
        public void setRate(double rate) {
            synchronized(_lock) {
                double oldRate = getRate();
                ZonedDateTime oldTime = _lastUpdatedDateTime;
                catchUpTime();
                resetLast();
                super.setRate(rate);
                if (oldTime != null) {
                    timeIsUpdated(oldTime);
                }
                InternalDateTime.this.firePropertyChange(PROPERTY_CHANGE_RATE, oldRate, rate);
            }
        }
    };
    private boolean _isRunning;
    private boolean _isLockedFromRunning;
    private final TimerTask _timerTask;
    private ZonedDateTime _lastDateTime;
    private ZonedDateTime _lastUpdatedDateTime;
    private int _lastSec;
    private int _lastMin;
    private long _startTimeMillisec;
    private final Object _lock = new Object();
    private boolean _isTimerTaskStarted;
    private boolean _isTimeSet;


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
        _startTimeMillisec = System.currentTimeMillis();
        _lastDateTime = _time;
        _lastUpdatedDateTime = _time;
        _lastSec = _lastDateTime.getSecond();
        _lastMin = _lastDateTime.getMinute();
    }

    /** {@inheritDoc} */
    @Override
    public ZonedDateTime getTime() {
        return _time;
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

    // Bring _time up to date before the run state or the rate changes
    private void catchUpTime() {
        if (_isRunning && _isTimerTaskStarted && !_isLockedFromRunning && _rate.getRate() >= 0.0001) {
            long time = System.currentTimeMillis() - _startTimeMillisec;
            _time = _lastDateTime.plus(Math.round(time * _rate.getRate()), ChronoUnit.MILLIS);
        }
    }

    @Override
    public void start() throws UnsupportedOperationException {
        boolean wasRunning;
        synchronized(_lock) {
            wasRunning = _isRunning;
            if (!_isRunning) {
                if (!_isTimeSet && !_isTimerTaskStarted) {
                    _time = ZonedDateTime.now();
                }
                resetLast();
            }
            if (!_isTimerTaskStarted && !_isLockedFromRunning) {
                TimerUtil.schedule(_timerTask, _100_MILLISECONDS, _100_MILLISECONDS);
               _isTimerTaskStarted = true;
            }
            _isRunning = true;
        }
        firePropertyChange(PROPERTY_CHANGE_RUN, wasRunning, true);
    }

    @Override
    public void stop() throws UnsupportedOperationException {
        boolean wasRunning;
        ZonedDateTime oldTime;
        synchronized(_lock) {
            wasRunning = _isRunning;
            oldTime = _lastUpdatedDateTime;
            catchUpTime();
            resetLast();
            _isRunning = false;
        }
        if (oldTime != null) {
            timeIsUpdated(oldTime);
        }
        firePropertyChange(PROPERTY_CHANGE_RUN, wasRunning, false);
    }

    @Override
    public boolean canStartAndStop() {
        return true;
    }

    @Override
    public void setTime(LocalTime time) throws UnsupportedOperationException {
        synchronized(_lock) {
            ZonedDateTime oldTime = this._time;
            _time = ZonedDateTime.of(_time.toLocalDate(), time, _time.getZone());
            _isTimeSet = true;
            resetLast();
            timeIsUpdated(oldTime);
        }
    }

    @Override
    public void setDateTime(ZonedDateTime time) throws UnsupportedOperationException {
        synchronized(_lock) {
            ZonedDateTime oldTime = this._time;
            this._time = time;
            _isTimeSet = true;
            resetLast();
            timeIsUpdated(oldTime);
        }
    }

    @Override
    public boolean canSetDateTime() {
        return true;
    }

    @Override
    public boolean canSetRate() {
        return true;
    }

}
