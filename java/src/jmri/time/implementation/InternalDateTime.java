package jmri.time.implementation;

import java.time.*;
import java.time.temporal.ChronoField;

import jmri.time.*;
import jmri.time.rate.ChangeableDoubleRate;

/**
 * The system date and time.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class InternalDateTime extends AbstractTimeProvider
        implements TimeSetter, DateProvider, DateSetter, CanSetRate, StartStopTimeProvider, RateSetter {

    private LocalDateTime _time = LocalDateTime.now();
    private final ChangeableDoubleRate _rate = new ChangeableDoubleRate(1.0);
    private boolean _isRunning;


    public InternalDateTime(String systemName) {
        super(systemName);
    }

    public InternalDateTime(String systemName, String userName) {
        super(systemName, userName);
    }

    /** {@inheritDoc} */
    @Override
    public LocalDateTime getTime() {
        return _time;
    }

    /** {@inheritDoc} */
    @Override
    public Rate getRate() {
        return _rate;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRunning() {
        return _isRunning;
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
        _isRunning = true;
    }

    @Override
    public void stop() throws UnsupportedOperationException {
        _isRunning = false;
    }

    @Override
    public boolean canStartAndStop() {
        return true;
    }

    @Override
    public void setTime(LocalTime time) throws UnsupportedOperationException {
        LocalDateTime oldTime = this._time;
        _time = LocalDateTime.of(_time.toLocalDate(), time);
        timeIsUpdated(oldTime);
    }

    public void setDateTime(LocalDateTime time) throws UnsupportedOperationException {
        LocalDateTime oldTime = this._time;
        this._time = time;
        timeIsUpdated(oldTime);
    }

    @Override
    public boolean canSetTime() {
        return true;
    }

    @Override
    public boolean canSetDateTime() {
        return true;
    }

    @Override
    public void setWeekday(int dayOfWeek) throws UnsupportedOperationException {
        LocalDateTime oldTime = this._time;
        _time = _time.with(ChronoField.DAY_OF_WEEK, dayOfWeek);
        timeIsUpdated(oldTime);
    }

    @Override
    public void setDayOfMonth(int day) throws UnsupportedOperationException {
        LocalDateTime oldTime = this._time;
        _time = _time.withDayOfMonth(day);
        timeIsUpdated(oldTime);
    }

    @Override
    public void setMonth(int month) throws UnsupportedOperationException {
        LocalDateTime oldTime = this._time;
        _time = _time.withMonth(month);
        timeIsUpdated(oldTime);
    }

    @Override
    public void setYear(int year) throws UnsupportedOperationException {
        LocalDateTime oldTime = this._time;
        _time = _time.withYear(year);
        timeIsUpdated(oldTime);
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
        _rate.setRate(rate);
    }

}
