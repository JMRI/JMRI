package jmri.implementation;

import javax.annotation.Nonnull;

import jmri.JmriException;
import jmri.CurrentMeter;
import jmri.Meter;
import jmri.VoltageMeter;

/**
 * Abstract base class for current meter objects.
 *
 * @author Mark Underwood    (C) 2015
 * @author Daniel Bergqvist  (C) 2020
 */
public class DefaultMeter extends AbstractNamedBean implements Meter {
    
    private final MeterUpdateTask _updateTask;
    private final Unit _unit;
    private double _value = 0.0;
    private final double _min;
    private final double _max;
    private final double _resolution;
    
    public DefaultMeter(@Nonnull String sys, Unit unit, double min, double max, double resolution) {
        this(sys, unit, min, max, resolution, null);
    }
    
    public DefaultMeter(@Nonnull String sys, Unit unit, double min, double max, double resolution, MeterUpdateTask updateTask) {
        super(sys);
        this._unit = unit;
        this._updateTask = updateTask;
        this._min = min;
        this._max = max;
        this._resolution = resolution;
    }
    
    @Override
    public void enable() {
        if (_updateTask != null) {
            log.debug("Enabling meter.");
            _updateTask.enable(this);
        }
    }

    @Override
    public void disable() {
        if (_updateTask != null) {
            log.debug("Disabling meter.");
            _updateTask.disable(this);
        }
    }

    @Override
    public void setState(int s) throws JmriException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getState() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameMeter");
    }

    @Override
    public Unit getUnit() {
        return _unit;
    }

    @Override
    public void setCommandedAnalogValue(double v) {
        _value = v;
    }

    @Override
    public double getCommandedAnalogValue() {
        return _value;
    }

    @Override
    public double getMin() {
        return _min;
    }

    @Override
    public double getMax() {
        return _max;
    }

    @Override
    public double getResolution() {
        return _resolution;
    }

    @Override
    public AbsoluteOrRelative getAbsoluteOrRelative() {
        return _unit == Unit.Percent ? AbsoluteOrRelative.RELATIVE : AbsoluteOrRelative.ABSOLUTE;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMeter.class);

    
    
    public static class DefaultVoltageMeter extends DefaultMeter implements VoltageMeter
    {
        public DefaultVoltageMeter(@Nonnull String sys, Unit unit, double min, double max, double resolution) {
            super(sys, unit, min, max, resolution, null);
        }

        public DefaultVoltageMeter(@Nonnull String sys, Unit unit, double min, double max, double resolution, MeterUpdateTask updateTask) {
            super(sys, unit, min, max, resolution, updateTask);
        }
    }
    
    
    public static class DefaultCurrentMeter extends DefaultMeter implements CurrentMeter
    {
        public DefaultCurrentMeter(@Nonnull String sys, Unit unit, double min, double max, double resolution) {
            super(sys, unit, min, max, resolution, null);
        }

        public DefaultCurrentMeter(@Nonnull String sys, Unit unit, double min, double max, double resolution, MeterUpdateTask updateTask) {
            super(sys, unit, min, max, resolution, updateTask);
        }
    }
    
    
}
