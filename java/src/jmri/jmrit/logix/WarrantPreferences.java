package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.XmlFile;
import jmri.jmrit.logix.WarrantPreferencesPanel.DataPair;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.spi.PreferencesManager;
import jmri.util.FileUtil;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hold configuration data for Warrants, includes Speed Map
 *
 * @author Pete Cressman Copyright (C) 2015
 */
@ServiceProvider(service = PreferencesManager.class)
public class WarrantPreferences extends AbstractPreferencesManager {

    public static final String LAYOUT_PARAMS = "layoutParams"; // NOI18N
    public static final String LAYOUT_SCALE = "layoutScale"; // NOI18N
    public static final String SEARCH_DEPTH = "searchDepth"; // NOI18N
    public static final String SPEED_MAP_PARAMS = "speedMapParams"; // NOI18N
    public static final String RAMP_PREFS = "rampPrefs";         // NOI18N
    public static final String TIME_INCREMENT = "timeIncrement"; // NOI18N
    public static final String THROTTLE_SCALE = "throttleScale"; // NOI18N
    public static final String RAMP_INCREMENT = "rampIncrement"; // NOI18N
    public static final String STEP_INCREMENTS = "stepIncrements"; // NOI18N
    public static final String SPEED_NAME_PREFS = "speedNames";   // NOI18N
    public static final String SPEED_NAMES = SPEED_NAME_PREFS;
    public static final String INTERPRETATION = "interpretation"; // NOI18N
    public static final String APPEARANCE_PREFS = "appearancePrefs"; // NOI18N
    public static final String APPEARANCES = "appearances"; // NOI18N
    public static final String SHUT_DOWN = "shutdown"; // NOI18N
    public static final String NO_MERGE = "NO_MERGE";
    public static final String PROMPT   = "PROMPT";
    public static final String MERGE_ALL = "MERGE_ALL";

    private String _fileName;
    private float _scale = 87.1f;
    private int _searchDepth = 20;      // How many tree nodes (blocks) to walk in finding routes
    private float _throttleScale = 0.90f;  // factor to approximate throttle setting to track speed

    private final LinkedHashMap<String, Float> _speedNames = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> _headAppearances = new LinkedHashMap<>();
    private int _interpretation = SignalSpeedMap.PERCENT_NORMAL;    // Interpretation of values in speed name table

    private int _msIncrTime = 1000;          // time in milliseconds between speed changes ramping up or down
    private float _throttleIncr = 0.0238f;  // throttle increment for each ramp speed change - 3 steps

    public enum Shutdown {NO_MERGE, PROMPT, MERGE_ALL}
    private Shutdown _shutdown = Shutdown.PROMPT;     // choice for handling session RosterSpeedProfiles
    /**
     * Get the default instance.
     *
     * @return the default instance, creating it if necessary
     */
    public static WarrantPreferences getDefault() {
        return InstanceManager.getOptionalDefault(WarrantPreferences.class).orElseGet(() -> {
            WarrantPreferences preferences = InstanceManager.setDefault(WarrantPreferences.class, new WarrantPreferences());
            try {
                preferences.initialize(ProfileManager.getDefault().getActiveProfile());
            } catch (InitializationException ex) {
                log.error("Error initializing default WarrantPreferences", ex);
            }
            return preferences;
        });
    }

    public void openFile(String name) {
        _fileName = name;
        WarrantPreferencesXml prefsXml = new WarrantPreferencesXml();
        File file = new File(_fileName);
        Element root;
        try {
            root = prefsXml.rootFromFile(file);
        } catch (java.io.FileNotFoundException ea) {
            log.debug("Could not find Warrant preferences file.  Normal if preferences have not been saved before.");
            root = null;
        } catch (IOException | JDOMException eb) {
            log.error("Exception while loading warrant preferences: " + eb);
            root = null;
        }
        if (root != null) {
//            log.info("Found Warrant preferences file: {}", _fileName);
            loadLayoutParams(root.getChild(LAYOUT_PARAMS));
            if (!loadSpeedMap(root.getChild(SPEED_MAP_PARAMS))) {
                loadSpeedMapFromOldXml();
                log.error("Unable to read ramp parameters. Setting to default values.");
            }
        } else {
            loadSpeedMapFromOldXml();
        }
    }

    public void loadLayoutParams(Element layoutParm) {
        if (layoutParm == null) {
            return;
        }
        Attribute a;
        if ((a = layoutParm.getAttribute(LAYOUT_SCALE)) != null) {
            try {
                setLayoutScale(a.getFloatValue());
            } catch (DataConversionException ex) {
                setLayoutScale(87.1f);
                log.error("Unable to read layout scale. Setting to default value.", ex);
            }
        }
        if ((a = layoutParm.getAttribute(SEARCH_DEPTH)) != null) {
            try {
                _searchDepth = a.getIntValue();
            } catch (DataConversionException ex) {
                _searchDepth = 20;
                log.error("Unable to read route search depth. Setting to default value (20).", ex);
            }
        }
        Element shutdown = layoutParm.getChild(SHUT_DOWN);
        if (shutdown != null) {
            String choice = shutdown.getText();
            if (MERGE_ALL.equals(choice)) {
                _shutdown = Shutdown.MERGE_ALL;
            } else if (NO_MERGE.equals(choice)) {
                _shutdown = Shutdown.NO_MERGE;
            } else {
                _shutdown = Shutdown.PROMPT;
            }
        }
    }

    // Avoid firePropertyChange until SignalSpeedMap is completely loaded
    private void loadSpeedMapFromOldXml() {
        SignalSpeedMap map = jmri.InstanceManager.getNullableDefault(SignalSpeedMap.class);
        if (map == null) {
            log.error("Cannot find signalSpeeds.xml file.");
            return;
        }
        Iterator<String> it = map.getValidSpeedNames().iterator();
        LinkedHashMap<String, Float> names = new LinkedHashMap<>();
        while (it.hasNext()) {
            String name = it.next();
            names.put(name, map.getSpeed(name));
        }
        this.setSpeedNames(names);  // OK, no firePropertyChange

        Enumeration<String> en = map.getAppearanceIterator();
        LinkedHashMap<String, String> heads = new LinkedHashMap<>();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            heads.put(name, map.getAppearanceSpeed(name));
        }
        this.setAppearances(heads);  // no firePropertyChange
        this._msIncrTime = map.getStepDelay();
        this._throttleIncr = map.getStepIncrement();
    }

    // Avoid firePropertyChange until SignalSpeedMap is completely loaded
    private boolean loadSpeedMap(Element child) {
        if (child == null) {
            return false;
        }
        Element rampParms = child.getChild(STEP_INCREMENTS);
        if (rampParms == null) {
            return false;
        }
        Attribute a;
        if ((a = rampParms.getAttribute(TIME_INCREMENT)) != null) {
            try {
                this._msIncrTime = a.getIntValue();
            } catch (DataConversionException ex) {
                this._msIncrTime = 500;
                log.error("Unable to read ramp time increment. Setting to default value (500ms).", ex);
            }
        }
        if ((a = rampParms.getAttribute(RAMP_INCREMENT)) != null) {
            try {
                this._throttleIncr = a.getFloatValue();
            } catch (DataConversionException ex) {
                this._throttleIncr = 0.03f;
                log.error("Unable to read ramp throttle increment. Setting to default value (0.03).", ex);
            }
        }
        if ((a = rampParms.getAttribute(THROTTLE_SCALE)) != null) {
            try {
                _throttleScale = a.getFloatValue();
            } catch (DataConversionException ex) {
                _throttleScale = .90f;
                log.error("Unable to read throttle scale. Setting to default value (0.90f).", ex);
            }
        }

        rampParms = child.getChild(SPEED_NAME_PREFS);
        if (rampParms == null) {
            return false;
        }
        if ((a = rampParms.getAttribute("percentNormal")) != null) {
            if (a.getValue().equals("yes")) {
                _interpretation = 1;
            } else {
                _interpretation = 2;
            }
        }
        if ((a = rampParms.getAttribute(INTERPRETATION)) != null) {
            try {
                _interpretation = a.getIntValue();
            } catch (DataConversionException ex) {
                _interpretation = 1;
                log.error("Unable to read interpetation of Speed Map. Setting to default value % normal.", ex);
            }
        }
        HashMap<String, Float> map = new LinkedHashMap<>();
        List<Element> list = rampParms.getChildren();
        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i).getName();
            Float speed = 0f;
            try {
                speed = Float.valueOf(list.get(i).getText());
            } catch (NumberFormatException nfe) {
                log.error("Speed names has invalid content for {} = {}", name, list.get(i).getText());
            }
            log.debug("Add {}, {} to AspectSpeed Table", name, speed);
            map.put(name, speed);
        }
        this.setSpeedNames(map);    // no firePropertyChange

        rampParms = child.getChild(APPEARANCE_PREFS);
        if (rampParms == null) {
            return false;
        }
        LinkedHashMap<String, String> heads = new LinkedHashMap<>();
        list = rampParms.getChildren();
        for (int i = 0; i < list.size(); i++) {
            String name = Bundle.getMessage(list.get(i).getName());
            String speed = list.get(i).getText();
            heads.put(name, speed);
        }
        this.setAppearances(heads); // no firePropertyChange

        // Now set SignalSpeedMap members.
        SignalSpeedMap speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);
        speedMap.setRampParams(_throttleIncr, _msIncrTime);
        speedMap.setDefaultThrottleFactor(_throttleScale);
        speedMap.setLayoutScale(_scale);
        speedMap.setAspects(new HashMap<>(this._speedNames), _interpretation);
        speedMap.setAppearances(new HashMap<>(this._headAppearances));
        return true;
    }

    public void save() {
        if (_fileName == null) {
            log.error("_fileName null. Could not create warrant preferences file.");
            return;
        }

        XmlFile xmlFile = new XmlFile() {
        };
        xmlFile.makeBackupFile(_fileName);
        File file = new File(_fileName);
        try {
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdir()) {
                    log.warn("Could not create parent directory for prefs file :{}", _fileName);
                    return;
                }
            }
            if (file.createNewFile()) {
                log.debug("Creating new warrant prefs file: {}", _fileName);
            }
        } catch (IOException ea) {
            log.error("Could not create warrant preferences file at {}.", _fileName, ea);
        }

        try {
            Element root = new Element("warrantPreferences");
            Document doc = XmlFile.newDocument(root);
            if (store(root)) {
                xmlFile.writeXML(file, doc);
            }
        } catch (IOException eb) {
            log.warn("Exception in storing warrant xml: {}", eb);
        }
    }

    public boolean store(Element root) {
        Element prefs = new Element(LAYOUT_PARAMS);
        try {
            prefs.setAttribute(LAYOUT_SCALE, Float.toString(getLayoutScale()));
            prefs.setAttribute(SEARCH_DEPTH, Integer.toString(getSearchDepth()));
            Element shutdownPref = new Element(SHUT_DOWN);
            shutdownPref.setText(_shutdown.toString());
            prefs.addContent(shutdownPref);
            root.addContent(prefs);

            prefs = new Element(SPEED_MAP_PARAMS);
            Element rampPrefs = new Element(STEP_INCREMENTS);
            rampPrefs.setAttribute(TIME_INCREMENT, Integer.toString(getTimeIncrement()));
            rampPrefs.setAttribute(RAMP_INCREMENT, Float.toString(getThrottleIncrement()));
            rampPrefs.setAttribute(THROTTLE_SCALE, Float.toString(getThrottleScale()));
            prefs.addContent(rampPrefs);

            rampPrefs = new Element(SPEED_NAME_PREFS);
            rampPrefs.setAttribute(INTERPRETATION, Integer.toString(getInterpretation()));

            Iterator<Entry<String, Float>> it = getSpeedNameEntryIterator();
            while (it.hasNext()) {
                Entry<String, Float> ent = it.next();
                Element step = new Element(ent.getKey());
                step.setText(ent.getValue().toString());
                rampPrefs.addContent(step);
            }
            prefs.addContent(rampPrefs);

            rampPrefs = new Element(APPEARANCE_PREFS);
            Element step = new Element("SignalHeadStateRed");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateRed")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateFlashingRed");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateFlashingRed")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateGreen");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateGreen")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateFlashingGreen");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateFlashingGreen")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateYellow");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateYellow")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateFlashingYellow");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateFlashingYellow")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateLunar");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateLunar")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateFlashingLunar");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateFlashingLunar")));
            rampPrefs.addContent(step);
            prefs.addContent(rampPrefs);
        } catch (RuntimeException ex) {
            log.warn("Exception in storing warrant xml.", ex);
            return false;
        }
        root.addContent(prefs);
        return true;
    }

    /**
     * Get the layout scale.
     *
     * @return the scale
     */
    public final float getLayoutScale() {
        return _scale;
    }

    /**
     * Set the layout scale.
     *
     * @param scale the scale
     */
    public void setLayoutScale(float scale) {
        float oldScale = this._scale;
        _scale = scale;
        this.firePropertyChange(LAYOUT_SCALE, oldScale, scale);
    }

    public float getThrottleScale() {
        return _throttleScale;
    }

    public void setThrottleScale(float scale) {
        float oldScale = this._throttleScale;
        _throttleScale = scale;
        this.firePropertyChange(THROTTLE_SCALE, oldScale, scale);
    }

    int getSearchDepth() {
        return _searchDepth;
    }

    void setSearchDepth(int depth) {
        int oldDepth = this._searchDepth;
        _searchDepth = depth;
        this.firePropertyChange(SEARCH_DEPTH, oldDepth, depth);
    }

    Iterator<Entry<String, Float>> getSpeedNameEntryIterator() {
        List<Entry<String, Float>> vec = new java.util.ArrayList<>();
        _speedNames.entrySet().forEach((entry) -> {
            vec.add(new DataPair<>(entry.getKey(), entry.getValue()));
        });
        return vec.iterator();
    }

    Float getSpeedNameValue(String key) {
        return _speedNames.get(key);
    }

    @Nonnull
    @CheckReturnValue
    public HashMap<String, Float> getSpeedNames() {
        return new HashMap<>(this._speedNames);
    }

    // Only called directly at load time
    private void setSpeedNames(@Nonnull HashMap<String, Float> map) {
        _speedNames.clear();
        _speedNames.putAll(map);
    }

    // Called when preferences is updated from panel
    protected void setSpeedNames(ArrayList<DataPair<String, Float>> speedNameMap) {
        LinkedHashMap<String, Float> map = new LinkedHashMap<>();
        for (int i = 0; i < speedNameMap.size(); i++) {
            DataPair<String, Float> dp = speedNameMap.get(i);
            map.put(dp.getKey(), dp.getValue());
        }
        LinkedHashMap<String, Float> old = new LinkedHashMap<>(_speedNames);
        this.setSpeedNames(map);
        this.firePropertyChange(SPEED_NAMES, old, new LinkedHashMap<>(_speedNames));
    }

    Iterator<Entry<String, String>> getAppearanceEntryIterator() {
        List<Entry<String, String>> vec = new ArrayList<>();
        _headAppearances.entrySet().stream().forEach((entry) -> {
            vec.add(new DataPair<>(entry.getKey(), entry.getValue()));
        });
        return vec.iterator();
    }

    String getAppearanceValue(String key) {
        return _headAppearances.get(key);
    }

    /**
     * Get a map of signal head appearances.
     *
     * @return a map of appearances or an empty map if none are defined
     */
    @Nonnull
    @CheckReturnValue
    public HashMap<String, String> getAppearances() {
        return new HashMap<>(this._headAppearances);
    }

    // Only called directly at load time
    private void setAppearances(HashMap<String, String> map) {
        this._headAppearances.clear();
        this._headAppearances.putAll(map);
     }

    // Called when preferences are updated
    protected void setAppearances(ArrayList<DataPair<String, String>> appearanceMap) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < appearanceMap.size(); i++) {
            DataPair<String, String> dp = appearanceMap.get(i);
            map.put(dp.getKey(), dp.getValue());
        }
        LinkedHashMap<String, String> old = new LinkedHashMap<>(this._headAppearances);
        this.setAppearances(map);
        this.firePropertyChange(APPEARANCES, old, new LinkedHashMap<>(this._headAppearances));
    }

    public int getInterpretation() {
        return _interpretation;
    }

    void setInterpretation(int interp) {
        int oldInterpretation = this._interpretation;
        _interpretation = interp;
        this.firePropertyChange(INTERPRETATION, oldInterpretation, interp);
    }

    /**
     * Get the time increment.
     *
     * @return the time increment in milliseconds
     */
    public final int getTimeIncrement() {
        return _msIncrTime;
    }

    /**
     * Set the time increment.
     *
     * @param increment the time increment in milliseconds
     */
    public void setTimeIncrement(int increment) {
        int oldIncrement = this._msIncrTime;
        this._msIncrTime = increment;
        this.firePropertyChange(TIME_INCREMENT, oldIncrement, increment);
    }

    /**
     * Get the throttle increment.
     *
     * @return the throttle increment
     */
    public final float getThrottleIncrement() {
        return _throttleIncr;
    }

    /**
     * Set the throttle increment.
     *
     * @param increment the throttle increment
     */
    public void setThrottleIncrement(float increment) {
        float oldIncrement = this._throttleIncr;
        this._throttleIncr = increment;
        this.firePropertyChange(RAMP_INCREMENT, oldIncrement, increment);

    }

    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!this.isInitialized(profile) && !this.isInitializing(profile)) {
            this.setInitializing(profile, true);
            this.openFile(FileUtil.getUserFilesPath() + "signal" + File.separator + "WarrantPreferences.xml");
            this.setInitialized(profile, true);
        }
    }

    public void setShutdown(Shutdown set) {
        _shutdown = set;
    }
    public Shutdown getShutdown() {
        return _shutdown;
    }

    @Override
    public void savePreferences(Profile profile) {
        this.save();
    }

    public static class WarrantPreferencesXml extends XmlFile {
    }

    private final static Logger log = LoggerFactory.getLogger(WarrantPreferences.class);
}
