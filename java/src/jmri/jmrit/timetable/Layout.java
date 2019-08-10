package jmri.jmrit.timetable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import jmri.Scale;
import jmri.ScaleManager;
import jmri.jmrit.timetable.swing.TimeTableFrame;

/**
 * Define the content of a Layout record.
 * <p>
 * The fast clock, scale and metric values affect the scale mile / scale km.
 * When these are changed, the stop times for all of the trains have to be
 * re-calculated.  Depending on the schedule limits, this can result in
 * calculation errors.  When this occurs, exceptions occur which trigger
 * rolling back the changes.
 * @author Dave Sand Copyright (C) 2018
 */
public class Layout implements VetoableChangeListener {

    /**
     * Create a new layout with default values.
     */
    public Layout() {
        _layoutId = _dm.getNextId("Layout");  // NOI18N
        _dm.addLayout(_layoutId, this);
        _scale.addVetoableChangeListener("ScaleRatio", this);  // NOI18N
        setScaleMK();
    }

    public Layout(int layoutId, String layoutName, Scale scale, int fastClock, int throttles, boolean metric) {
        _layoutId = layoutId;
        setLayoutName(layoutName);
        setScale(scale);
        setFastClock(fastClock);
        setThrottles(throttles);
        setMetric(metric);
    }

    TimeTableDataManager _dm = TimeTableDataManager.getDataManager();

    private final int _layoutId;
    private String _layoutName = "New Layout";  // NOI18N
    private Scale _scale = ScaleManager.getScale("HO");  // NOI18N
    private int _fastClock = 4;
    private int _throttles = 0;
    private boolean _metric = false;

    private double _ratio = 87.1;
    private double _scaleMK;          // Scale mile (real feet) or km (real meter)

    /**
     * Calculate the length of a scale mile or scale kilometer.
     * The values are adjusted for scale and fast clock ratio.
     * The resulting value is the real feet or meters.
     * The final step is to re-calculate the train times.
     * @throws IllegalArgumentException The calculate can throw an exception which will get re-thrown.
     */
    public void setScaleMK() throws IllegalArgumentException {
        double distance = (_metric) ? 1000 : 5280;
        _scaleMK = distance / _ratio / _fastClock;
        log.debug("scaleMK = {}, scale = {}", _scaleMK, _scale);  // NOI18N

        try {
            _dm.calculateLayoutTrains(getLayoutId(), false);
            _dm.calculateLayoutTrains(getLayoutId(), true);
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
    }

    public double getScaleMK() {
        return _scaleMK;
    }

    public int getLayoutId() {
        return _layoutId;
    }

    public String getLayoutName() {
        return _layoutName;
    }

    public void setLayoutName(String newName) {
        _layoutName = newName;
    }

    public double getRatio() {
        return _ratio;
    }

    public Scale getScale() {
        return _scale;
    }

    public void setScale(Scale newScale) {
        _scale.removeVetoableChangeListener("ScaleRatio", this);  // NOI18N
        if (newScale == null) {
            newScale = ScaleManager.getScale("HO");  // NOI18N
            log.warn("No scale found, defaulting to HO");  // NOI18N
        }

        Scale oldScale = _scale;
        double oldRatio = _ratio;
        _scale = newScale;
        _ratio = newScale.getScaleRatio();

        try {
            // Update the smile/skm which includes stop recalcs
            setScaleMK();
        } catch (IllegalArgumentException ex) {
            _scale = oldScale;  // roll back scale and ratio
            _ratio = oldRatio;
            setScaleMK();
            throw ex;
        }
        _scale.addVetoableChangeListener("ScaleRatio", this);  // NOI18N
    }

    public int getFastClock() {
        return _fastClock;
    }

    /**
     * Set a new fast clock speed, update smile/skm.
     * @param newClock The value to be used.
     * @throws IllegalArgumentException (CLOCK_LT_1) if the value is less than 1.
     * will also re-throw a recalc error.
     */
    public void setFastClock(int newClock) throws IllegalArgumentException {
        if (newClock < 1) {
            throw new IllegalArgumentException(_dm.CLOCK_LT_1);
        }
        int oldClock = _fastClock;
        _fastClock = newClock;

        try {
            // Update the smile/skm which includes stop recalcs
            setScaleMK();
        } catch (IllegalArgumentException ex) {
            _fastClock = oldClock;  // roll back
            setScaleMK();
            throw ex;
        }
    }

    public int getThrottles() {
        return _throttles;
    }

    /**
     * Set the new value for throttles.
     * @param newThrottles The new throttle count.
     * @throws IllegalArgumentException (THROTTLES_USED, THROTTLES_LT_0) when the
     * new count is less than train references or a negative number was passed.
     */
    public void setThrottles(int newThrottles) throws IllegalArgumentException {
        if (newThrottles < 0) {
            throw new IllegalArgumentException(_dm.THROTTLES_LT_0);
        }
        for (Schedule schedule : _dm.getSchedules(_layoutId, true)) {
            for (Train train : _dm.getTrains(schedule.getScheduleId(), 0, true)) {
                if (train.getThrottle() > newThrottles) {
                    throw new IllegalArgumentException(_dm.THROTTLES_IN_USE);
                }
            }
        }
        _throttles = newThrottles;
    }

    public boolean getMetric() {
        return _metric;
    }

    /**
     * Set metric flag, update smile/skm.
     * @param newMetric True for metric units.
     * @throws IllegalArgumentException if there was a recalc error.
     */
    public void setMetric(boolean newMetric) throws IllegalArgumentException {
        boolean oldMetric = _metric;
        _metric = newMetric;

        try {
            // Update the smile/skm which includes stop recalcs
            setScaleMK();
        } catch (IllegalArgumentException ex) {
            _metric = oldMetric;  // roll back
            setScaleMK();
            throw ex;
        }
    }

    @Override
    public String toString() {
        return _layoutName;
    }

    /**
     * Listen for ratio changes to my current scale.  Verify that the new ratio
     * is neither too small nor too large.  Too large can cause train times to move
     * outside of the schedule window.  If the new ratio is invalid, the change
     * will be vetoed.
     * @param evt The scale ratio property change event.
     * @throws PropertyVetoException The message will depend on the actual error.
     */
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        log.debug("scale change event: layout = {}, evt = {}", _layoutName, evt);
        double newRatio = (Double) evt.getNewValue();
        if (newRatio < 1.0) {
            throw new PropertyVetoException("Ratio is less than 1", evt);
        }

        double oldRatio = _ratio;
        _ratio = newRatio;

        try {
            // Update the smile/skm which includes stop recalcs
            setScaleMK();
        } catch (IllegalArgumentException ex) {
            // Roll back the ratio change
            _ratio = oldRatio;
            setScaleMK();
            throw new PropertyVetoException("New ratio causes calc errors", evt);
        }

        TimeTableFrame frame = jmri.InstanceManager.getNullableDefault(TimeTableFrame.class);
        if (frame != null) {
            frame.setShowReminder(true);
        } else {
            // Save any changes
            jmri.jmrit.timetable.configurexml.TimeTableXml.doStore();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Layout.class);
}
