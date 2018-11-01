package jmri.jmrit.timetable;

import jmri.jmrit.logix.WarrantPreferences;

/**
 * Define the content of a Layout record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class Layout {

    public Layout(int layoutId) {
        _layoutId = layoutId;
        setScaleMK();
    }

    public Layout(int layoutId, String layoutName, int fastClock, int throttles, boolean metric) {
        _layoutId = layoutId;
        _layoutName = layoutName;
        _fastClock = fastClock;
        _throttles = throttles;
        _metric = metric;
        setScaleMK();
    }

    private int _layoutId = 0;
    private String _layoutName = "";
    private int _fastClock = 4;
    private int _throttles = 0;
    private boolean _metric = false;

    private float _scale = 87.1f;
    private float _scaleMK;          // Scale mile (real feet) or km (real meter)

    private WarrantPreferences _wp = null;

    /**
     * Calculate the length of a scale mile or scale kilometer.
     * The values are adjusted for scale and fast clock ratio.
     * The resulting value is the real feet or meters.
     */
    public void setScaleMK() {
        float distance = (_metric) ? 1000 : 5280;
        if (_wp == null) {
            try {
                _wp = jmri.jmrit.logix.WarrantPreferences.getDefault();
                _scale = _wp.getLayoutScale();
            } catch (java.lang.NullPointerException ex) {
                log.debug("Use HO as the default scale");  // NOI18N
            }
        }
        _scaleMK = distance / _scale / _fastClock;
        log.debug("scaleMK = {}, scale = {}", _scaleMK, _scale);  // NOI18N
    }

    public float getScaleMK() {
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

    public float getScale() {
        return _scale;
    }

    public int getFastClock() {
        return _fastClock;
    }

    public void setFastClock(int newClock) {
        _fastClock = newClock;
    }

    public int getThrottles() {
        return _throttles;
    }

    public void setThrottles(int newThrottles) {
        _throttles = newThrottles;
    }

    public boolean getMetric() {
        return _metric;
    }

    public void setMetric(boolean newMetric) {
        _metric = newMetric;
    }

    public String toString() {
        return _layoutName;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Layout.class);
}
