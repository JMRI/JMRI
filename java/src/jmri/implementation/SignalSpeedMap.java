package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InstanceManagerAutoInitialize;
import jmri.beans.Bean;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.FileUtil;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation to map Signal aspects or appearances to speed
 * requirements.
 * <p>
 * The singleton instance is referenced from the InstanceManager by SignalHeads
 * and SignalMasts
 *
 * @author Pete Cressman Copyright (C) 2010
 */
public class SignalSpeedMap extends Bean implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize // auto-initialize in InstanceManager
{

    private final HashMap<String, Float> _table = new LinkedHashMap<>();
    private final HashMap<String, String> _headTable = new LinkedHashMap<>();
    private int _interpretation;
    private int _sStepDelay;     // ramp step time interval
    private int _numSteps = 4;   // num throttle steps per ramp step - deprecated
    private float _stepIncrement = 0.04f;       // ramp step throttle increment
    private float _throttleFactor = 0.75f;
    private float _scale = 87.1f;

    static public final int PERCENT_NORMAL = 1;
    static public final int PERCENT_THROTTLE = 2;
    static public final int SPEED_MPH = 3;
    static public final int SPEED_KMPH = 4;
    private PropertyChangeListener warrantPreferencesListener = null;

    public SignalSpeedMap() {
        loadMap();
        this.warrantPreferencesListener = (PropertyChangeEvent evt) -> {
            WarrantPreferences preferences = WarrantPreferences.getDefault();
            SignalSpeedMap map = SignalSpeedMap.this;
            switch (evt.getPropertyName()) {
                case WarrantPreferences.APPEARANCES:
                    map.setAppearances(preferences.getAppearances());
                    break;
                case WarrantPreferences.LAYOUT_SCALE:
                    map.setLayoutScale(preferences.getLayoutScale());
                    break;
                case WarrantPreferences.SPEED_NAMES:
                case WarrantPreferences.INTERPRETATION:
                    map.setAspects(preferences.getSpeedNames(), preferences.getInterpretation());
                    break;
                case WarrantPreferences.THROTTLE_SCALE:
                    map.setDefaultThrottleFactor(preferences.getThrottleScale());
                    break;
                case WarrantPreferences.TIME_INCREMENT:
                case WarrantPreferences.RAMP_INCREMENT:
                    map.setRampParams(preferences.getThrottleIncrement(), preferences.getTimeIncrement());
                    break;
                default:
                // ignore other properties
            }
        };
    }

    @Override
    public void initialize() {
        InstanceManager.getOptionalDefault(WarrantPreferences.class).ifPresent((wp) -> {
            wp.addPropertyChangeListener(this.warrantPreferencesListener);
        });
        InstanceManager.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (evt.getPropertyName().equals(InstanceManager.getDefaultsPropertyName(WarrantPreferences.class))) {
                InstanceManager.getDefault(WarrantPreferences.class).addPropertyChangeListener(this.warrantPreferencesListener);
            }
        });
    }

    void loadMap() {
        URL path = FileUtil.findURL("signalSpeeds.xml", new String[]{"", "xml/signals"});
        jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile() {
        };
        try {
            loadRoot(xf.rootFromURL(path));
        } catch (java.io.FileNotFoundException e) {
            log.warn("signalSpeeds file ({}) doesn't exist in XmlFile search path.", path);
            throw new IllegalArgumentException("signalSpeeds file (" + path + ") doesn't exist in XmlFile search path.");
        } catch (org.jdom2.JDOMException | java.io.IOException e) {
            log.error("error reading file \"{}\" due to: {}", path, e);
        }
    }

    public void loadRoot(@Nonnull Element root) {
        try {
            Element e = root.getChild("interpretation");
            String sval = e.getText().toUpperCase();
            switch (sval) {
                case "PERCENTNORMAL":
                    _interpretation = PERCENT_NORMAL;
                    break;
                case "PERCENTTHROTTLE":
                    _interpretation = PERCENT_THROTTLE;
                    break;
                default:
                    throw new JDOMException("invalid content for interpretation: " + sval);
            }
            log.debug("_interpretation= {}", _interpretation);

            e = root.getChild("msPerIncrement");
            _sStepDelay = 1000;
            try {
                _sStepDelay = Integer.parseInt(e.getText());
            } catch (NumberFormatException nfe) {
                throw new JDOMException("invalid content for msPerIncrement: " + e.getText());
            }
            if (_sStepDelay < 200) {
                _sStepDelay = 200;
                log.warn("\"msPerIncrement\" must be at least 200 milliseconds.");
            }
            log.debug("_sStepDelay = {}", _sStepDelay);

            e = root.getChild("stepsPerIncrement");
            try {
                _numSteps = Integer.parseInt(e.getText());
            } catch (NumberFormatException nfe) {
                throw new JDOMException("invalid content for stepsPerIncrement: " + e.getText());
            }
            if (_numSteps < 1) {
                _numSteps = 1;
            }

            List<Element> list = root.getChild("aspectSpeeds").getChildren();
            _table.clear();
            for (int i = 0; i < list.size(); i++) {
                String name = list.get(i).getName();
                Float speed;
                try {
                    speed = Float.valueOf(list.get(i).getText());
                } catch (NumberFormatException nfe) {
                    log.error("invalid content for {} = {}", name, list.get(i).getText());
                    throw new JDOMException("invalid content for " + name + " = " + list.get(i).getText());
                }
                log.debug("Add {}, {} to AspectSpeed Table", name, speed);
                _table.put(name, speed);
            }

            synchronized (this._headTable) {
                _headTable.clear();
                List<Element> l = root.getChild("appearanceSpeeds").getChildren();
                for (int i = 0; i < l.size(); i++) {
                    String name = l.get(i).getName();
                    String speed = l.get(i).getText();
                    _headTable.put(Bundle.getMessage(name), speed);
                    log.debug("Add {}={}, {} to AppearanceSpeed Table", name, Bundle.getMessage(name), speed);
                }
            }
        } catch (org.jdom2.JDOMException e) {
            log.error("error reading speed map elements due to: {}", e);
        }
    }

    public boolean checkSpeed(String name) {
        if (name == null) {
            return false;
        }
        return _table.get(name) != null;
    }

    /**
     * @param aspect appearance (not called head in US) to check
     * @param system system name of head
     * @return speed from SignalMast Aspect name
     */
    public String getAspectSpeed(@Nonnull String aspect, @Nonnull jmri.SignalSystem system) {
        String property = (String) system.getProperty(aspect, "speed");
        log.debug("getAspectSpeed: aspect={}, speed={}", aspect, property);
        return property;
    }

    /**
     * @param aspect appearance (not called head in US) to check
     * @param system system name of head
     * @return speed2 from SignalMast Aspect name
     */
    public String getAspectExitSpeed(@Nonnull String aspect, @Nonnull jmri.SignalSystem system) {
        String property = (String) system.getProperty(aspect, "speed2");
        log.debug("getAspectSpeed: aspect={}, speed2={}", aspect, property);
        return property;
    }

    /**
     * Get speed for a given signal head appearance.
     *
     * @param name appearance default name
     * @return speed from SignalHead Appearance name
     */
    public String getAppearanceSpeed(@Nonnull String name) throws NumberFormatException {
        String speed = _headTable.get(name);
        log.debug("getAppearanceSpeed Appearance={}, speed={}", name, speed);
        return speed;
    }

    public Enumeration<String> getAppearanceIterator() {
        return Collections.enumeration(_headTable.keySet());
    }

    public Enumeration<String> getSpeedIterator() {
        return Collections.enumeration(_table.keySet());
    }

    public Vector<String> getValidSpeedNames() {
        return new Vector<>(this._table.keySet());
    }

    public float getSpeed(@Nonnull String name) throws IllegalArgumentException {
        if (!checkSpeed(name)) {
            // not a valid aspect
            log.warn("attempting to get speed for invalid name: '{}'", name);
            //java.util.Enumeration<String> e = _table.keys();
            throw new IllegalArgumentException("attempting to get speed from invalid name: \"" + name + "\"");
        }
        Float speed = _table.get(name);
        if (speed == null) {
            return 0.0f;
        }
        return speed;
    }

    public String getNamedSpeed(float speed) {
        Enumeration<String> e = this.getSpeedIterator();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            if (_table.get(key).equals(speed)) {
                return key;
            }
        }
        return null;
    }

    public int getInterpretation() {
        return _interpretation;
    }

    public int getStepDelay() {
        return _sStepDelay;
    }

    public float getStepIncrement() {
        return _stepIncrement;
    }

    public void setAspects(@Nonnull HashMap<String, Float> map, int interpretation) {
        HashMap<String, Float> oldMap = new HashMap<>(this._table);
        int oldInterpretation = this._interpretation;
        this._table.clear();
        this._table.putAll(map);
        this._interpretation = interpretation;
        if (interpretation != oldInterpretation) {
            this.firePropertyChange("interpretation", oldInterpretation, interpretation);
        }
        if (!map.equals(oldMap)) {
            this.firePropertyChange("aspects", oldMap, new HashMap<>(map));
        }
    }

    public void setAspectTable(@Nonnull Iterator<Entry<String, Float>> iter, int interpretation) {
        _table.clear();
        while (iter.hasNext()) {
            Entry<String, Float> ent = iter.next();
            _table.put(ent.getKey(), ent.getValue());
        }
        _interpretation = interpretation;
    }

    public void setAppearances(@Nonnull HashMap<String, String> map) {
        synchronized (this._headTable) {
            HashMap<String, String> old = new HashMap<>(_headTable);
            _headTable.clear();
            _headTable.putAll(map);
            if (!map.equals(old)) {
                this.firePropertyChange("Appearances", old, new HashMap<>(_headTable));
            }
        }
    }

    public void setAppearanceTable(@Nonnull Iterator<Entry<String, String>> iter) {
        synchronized (this._headTable) {
            _headTable.clear();
            while (iter.hasNext()) {
                Entry<String, String> ent = iter.next();
                _headTable.put(ent.getKey(), ent.getValue());
            }
        }
    }

    public void setRampParams(float throttleIncr, int msIncrTime) {
        _sStepDelay = msIncrTime;
        _stepIncrement = throttleIncr;
    }

    public void setDefaultThrottleFactor(float f) {
        _throttleFactor = f;
    }

    public float getDefaultThrottleFactor() {
        return _throttleFactor;
    }

    public void setLayoutScale(float s) {
        _scale = s;
    }

    public float getLayoutScale() {
        return _scale;
    }

    static private final Logger log = LoggerFactory.getLogger(SignalSpeedMap.class);
}
