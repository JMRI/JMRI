package jmri.time.implementation;

import java.time.LocalDateTime;
import java.util.TimerTask;

import jmri.time.Rate;
import jmri.time.rate.IntegerRate;
import jmri.time.DateProvider;
import jmri.util.TimerUtil;

/**
 * The system date and time.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class SystemDateTime extends AbstractTimeProvider implements DateProvider {

    // System date and time always has the rate 1:1.
    private static final Rate RATE = new IntegerRate(1);
    private static final int _100_MILLISECONDS = 100;

    private final TimerTask _timerTask;
    private LocalDateTime _lastDateTime;
    private int _lastSec;
    private int _lastMin;


    /**
     * Creates an instance of SystemDateTime.
     * This constructor must only be called from ProxyTimeProviderManager and
     * the caller must call {@ #init()} afterwards.
     * @param systemName the system name
     */
    public SystemDateTime(String systemName) {
        super(systemName);

        _timerTask = new TimerTask() {
            @Override
            public void run() {
                var newDateTime = LocalDateTime.now();
                int sec = newDateTime.getSecond();
                if (sec != _lastSec) {
                    int min = newDateTime.getMinute();
                    SystemDateTime.this.firePropertyChange(PROPERTY_CHANGE_SECONDS, _lastSec, sec);
                    if (min != _lastMin) {
                        SystemDateTime.this.firePropertyChange(PROPERTY_CHANGE_MINUTES, _lastMin, min);
                    }
                    SystemDateTime.this.firePropertyChange(PROPERTY_CHANGE_DATETIME, _lastDateTime, newDateTime);
                    _lastDateTime = newDateTime;
                    _lastSec = sec;
                    _lastMin = min;
                }
            }
        };
    }

    public SystemDateTime init() {
        _lastDateTime = LocalDateTime.now();
        _lastSec = _lastDateTime.getSecond();
        _lastMin = _lastDateTime.getMinute();
        TimerUtil.schedule(_timerTask, System.currentTimeMillis() % _100_MILLISECONDS, _100_MILLISECONDS);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public LocalDateTime getTime() {
        return LocalDateTime.now();
    }

    /** {@inheritDoc} */
    @Override
    public Rate getRate() {
        return RATE;    // System date and time always has the rate 1:1.
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRunning() {
        return true;    // System date and time is always running.
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

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (_timerTask != null) _timerTask.cancel();
    }

}
