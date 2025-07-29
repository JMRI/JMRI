package jmri.implementation;

import java.util.TimerTask;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.util.TimerUtil;

/**
 * Abstract base class for average meter objects.
 *
 * @author Mark Underwood    (C) 2015
 * @author Daniel Bergqvist  (C) 2025
 */
public class DefaultAverageMeter extends AbstractAnalogIO implements AverageMeter {

    private static final int DELAY = 250;
    private final int SIZE = 100;
    private final double[] _measurements = new double[SIZE];

    private int _numMeasurements = 1;
    private TimerTask _updateTask;
    private Meter _meter;
    private int _time = 1000;   // 1 second

    public DefaultAverageMeter(@Nonnull String sys) {
        this(sys, null);
    }

    public DefaultAverageMeter(@Nonnull String sys, String user) {
        super(sys, user, false);
        DefaultAverageMeter.this.setTime(1000);
    }

    /** {@inheritDoc} */
    @Override
    public void enable() {
        log.debug("Enabling meter.");
    }

    /** {@inheritDoc} */
    @Override
    public void disable() {
        log.debug("Disabling meter.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Override this if the meter can send value to the layout.
     */
    @Override
    protected void sendValueToLayout(double value) throws JmriException {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    protected boolean cutOutOfBoundsValues() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void setState(int s) throws JmriException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public int getState() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameMeter");
    }

    /** {@inheritDoc} */
    @Override
    public Unit getUnit() {
        if (_meter != null) {
            return _meter.getUnit();
        } else {
            return Unit.NoPrefix;
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getMin() {
        if (_meter != null) {
            return _meter.getMin();
        } else {
            return 0.0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getMax() {
        if (_meter != null) {
            return _meter.getMax();
        } else {
            return 0.0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getResolution() {
        if (_meter != null) {
            return _meter.getResolution();
        } else {
            return 1.0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public AbsoluteOrRelative getAbsoluteOrRelative() {
        if (_meter != null) {
            return _meter.getAbsoluteOrRelative();
        } else {
            return AbsoluteOrRelative.RELATIVE;
        }
    }

    /**
     * Request an update from the layout.
     */
    @Override
    public void requestUpdateFromLayout() {
        _meter.requestUpdateFromLayout();
    }

    @Override
    public int getTime() {
        return _time;
    }

    @Override
    public void setTime(int time) {
        this._time = time;
        _numMeasurements = (int) Math.round(((double)_time) / DELAY);
    }

    @Override
    public Meter getMeter() {
        return _meter;
    }

    @Override
    public void setMeter(Meter meter) {
        if (meter == this._meter) {
            return;
        }
        if (_meter != null) {
            _updateTask.cancel();
        }
        this._meter = meter;
        if (_meter != null) {
            _updateTask = new MyTimerTask();
            TimerUtil.scheduleOnGUIThread(_updateTask, DELAY, DELAY);
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getKnownAnalogValue() {
        double sum = 0.0;
        int start = SIZE - _numMeasurements;
        for (int i=start; i < SIZE; i++) {
            sum += _measurements[i];
        }
        return sum / _numMeasurements;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (_meter != null) {
            _updateTask.cancel();
            _meter.dispose();
        }
        super.dispose();
    }


    private class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            System.arraycopy(_measurements, 1, _measurements, 0, SIZE-1);
            _measurements[SIZE-1] = _meter.getKnownAnalogValue();
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultAverageMeter.class);

}
