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
public class DefaultMeter extends AbstractAnalogIO implements Meter {
    
    @Nonnull private final MeterUpdateTask _updateTask;
    @Nonnull private final Unit _unit;
    private final double _min;
    private final double _max;
    private final double _resolution;
    
    public DefaultMeter(
            @Nonnull String sys,
            @Nonnull Unit unit,
            double min, double max, double resolution,
            @Nonnull MeterUpdateTask updateTask) {
        super(sys, true);
        this._unit = unit;
        this._updateTask = updateTask;
        this._min = min;
        this._max = max;
        this._resolution = resolution;
        _updateTask.addMeter(DefaultMeter.this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void enable() {
        log.debug("Enabling meter.");
        _updateTask.enable(this);
    }

    /** {@inheritDoc} */
    @Override
    public void disable() {
        log.debug("Disabling meter.");
        _updateTask.disable(this);
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
        return _unit;
    }

    /** {@inheritDoc} */
    @Override
    public double getMin() {
        return _min;
    }

    /** {@inheritDoc} */
    @Override
    public double getMax() {
        return _max;
    }

    /** {@inheritDoc} */
    @Override
    public double getResolution() {
        return _resolution;
    }

    /** {@inheritDoc} */
    @Override
    public AbsoluteOrRelative getAbsoluteOrRelative() {
        return _unit == Unit.Percent ? AbsoluteOrRelative.RELATIVE : AbsoluteOrRelative.ABSOLUTE;
    }
    
    /** {@inheritDoc} */
    @Override
    public void dispose() {
        _updateTask.removeMeter(this);
        super.dispose();
    }
    
    /**
     * Request an update from the layout.
     */
    @Override
    public void requestUpdateFromLayout() {
        _updateTask.requestUpdateFromLayout();
    }
    
    
    /**
     * Default implementation of a voltage meter.
     */
    public static class DefaultVoltageMeter extends DefaultMeter implements VoltageMeter
    {
        public DefaultVoltageMeter(@Nonnull String sys, Unit unit, double min, double max, double resolution, MeterUpdateTask updateTask) {
            super(sys, unit, min, max, resolution, updateTask);
        }
    }
    
    
    /**
     * Default implementation of a current meter.
     */
    public static class DefaultCurrentMeter extends DefaultMeter implements CurrentMeter
    {
        public DefaultCurrentMeter(@Nonnull String sys, Unit unit, double min, double max, double resolution, MeterUpdateTask updateTask) {
            super(sys, unit, min, max, resolution, updateTask);
        }
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMeter.class);
}
