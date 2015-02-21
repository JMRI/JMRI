package jmri.jmrit.logix;

/**
 * @author Pete Cressman Copyright (C) 2015
 * @version $Revision: 28030 $
 *
 * Hold configuration data for Warrants, includes Speed Map
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import jmri.DccThrottle;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.XmlFile;
import jmri.jmrit.logix.WarrantPreferencesPanel.DataPair;
import jmri.util.OrderedHashtable;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarrantPreferences {

    public static final String layoutParams = "layoutParams"; 	// NOI18N
    public static final String LayoutScale = "layoutScale"; 	// NOI18N
    public static final String SearchDepth = "searchDepth"; 	// NOI18N
    public static final String SpeedMapParams = "speedMapParams"; // NOI18N
    public static final String RampPrefs = "rampPrefs";			// NOI18N
    public static final String TimeIncrement = "timeIncrement"; // NOI18N
    public static final String ThrottleScale = "throttleScale";	// NOI18N
    public static final String StepIncrements = "stepIncrements"; // NOI18N
    public static final String SpeedNamePrefs = "speedNames";	// NOI18N
    public static final String PercentNormal = "percentNormal"; // NOI18N
    public static final String AppearancePrefs = "appearancePrefs";	// NOI18N
    public static final String ThrottleStepMode14 = "throttleStepMode14";
    public static final String ThrottleStepMode27 = "throttleStepMode27";
    public static final String ThrottleStepMode28 = "throttleStepMode28";
    public static final String ThrottleStepMode128 = "throttleStepMode128";

    private String _fileName;
    private float _scale = 87.1f;
    private int _searchDepth = 20;
    private float _throttleScale = 0.81f;

    private OrderedHashtable<String, Float> _speedNames;
    private OrderedHashtable<String, String> _headAppearances;
    private OrderedHashtable<String, Integer> _stepIncrements;
    private boolean _percentNormal = true;	// Interpret speed name table to be percent of normal speed
    private int _msIncrTime = 750;			// time in milliseconds between speed changes ramping up or down

    WarrantPreferences(String fileName) {
        openFile(fileName);
    }

    public void openFile(String name) {
        _fileName = name;
        WarrantPreferencesXml prefsXml = new WarrantPreferencesXml();
        File file = new File(_fileName);
        Element root;
        try {
            root = prefsXml.rootFromFile(file);
        } catch (java.io.FileNotFoundException ea) {
            log.info("Could not find Warrant preferences file.  Normal if preferences have not been saved before.");
            root = null;
        } catch (Exception eb) {
            log.error("Exception while loading warrant preferences: " + eb);
            root = null;
        }
        if (root != null) {
            loadLayoutParams(root.getChild(layoutParams));
            if (!loadSpeedMap(root.getChild(SpeedMapParams))) {
                loadSpeedMapFromOldXml();
                log.error("Unable to read ramp parameters. Setting to default values.");
            }
        } else {
            loadSpeedMapFromOldXml();
        }
    }

    public void loadLayoutParams(Element child) {
        if (child == null) {
            return;
        }
        Attribute a;
        if ((a = child.getAttribute(LayoutScale)) != null) {
            try {
                setScale(a.getFloatValue());
            } catch (DataConversionException ex) {
                setScale(87.1f);
                log.error("Unable to read layout scale. Setting to default value.", ex);
            }
        }
        if ((a = child.getAttribute(SearchDepth)) != null) {
            try {
                setSearchDepth(a.getIntValue());
            } catch (DataConversionException ex) {
                setSearchDepth(20);
                log.error("Unable to read route search depth. Setting to default value (20).", ex);
            }
        }
    }

    private void loadSpeedMapFromOldXml() {
        SignalSpeedMap map = SignalSpeedMap.getMap();
        if (map == null) {
            log.error("Cannot find signalSpeeds.xml file.");
            return;
        }
        Iterator<String> it = map.getValidSpeedNames().iterator();
        _speedNames = new jmri.util.OrderedHashtable<String, Float>();
        while (it.hasNext()) {
            String name = it.next();
            _speedNames.put(name, Float.valueOf(map.getSpeed(name)));
        }

        Enumeration<String> en = map.getAppearanceIterator();
        _headAppearances = new OrderedHashtable<String, String>();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            _headAppearances.put(name, map.getAppearanceSpeed(name));
        }
        setTimeIncre(map.getStepDelay());

        _stepIncrements = new OrderedHashtable<String, Integer>();
        _stepIncrements.put(Bundle.getMessage(ThrottleStepMode14), map.getNumStepsFromMode(DccThrottle.SpeedStepMode14));
        _stepIncrements.put(Bundle.getMessage(ThrottleStepMode27), map.getNumStepsFromMode(DccThrottle.SpeedStepMode27));
        _stepIncrements.put(Bundle.getMessage(ThrottleStepMode28), map.getNumStepsFromMode(DccThrottle.SpeedStepMode28));
        _stepIncrements.put(Bundle.getMessage(ThrottleStepMode128), map.getNumStepsFromMode(DccThrottle.SpeedStepMode128));
    }

    public boolean loadSpeedMap(Element child) {
        if (child == null) {
            return false;
        }
        Element rampParms = child.getChild(StepIncrements);
        if (rampParms == null) {
            return false;
        }
        Attribute a;
        if ((a = rampParms.getAttribute(TimeIncrement)) != null) {
            try {
                setTimeIncre(a.getIntValue());
            } catch (DataConversionException ex) {
                setTimeIncre(750);
                log.error("Unable to read ramp time increment. Setting to default value (750ms).", ex);
            }
        }
        if ((a = rampParms.getAttribute(ThrottleScale)) != null) {
            try {
                setThrottleScale(a.getFloatValue());
            } catch (DataConversionException ex) {
                setThrottleScale(0.81f);
                log.error("Unable to read throttle scale. Setting to default value (0.81f).", ex);
            }
        }

        _stepIncrements = new OrderedHashtable<String, Integer>();
        List<Element> list = rampParms.getChildren();
        for (int i = 0; i < list.size(); i++) {
            String name = Bundle.getMessage(list.get(i).getName());
            Integer incr = Integer.valueOf(0);
            try {
                incr = new Integer(list.get(i).getText());
            } catch (NumberFormatException nfe) {
                log.error(StepIncrements + " has invalid content for " + name + " = " + list.get(i).getText());
            }
            if (log.isDebugEnabled()) {
                log.debug("Add " + name + ", " + incr + " to AspectSpeed Table");
            }
            _stepIncrements.put(name, incr);
        }

        rampParms = child.getChild(SpeedNamePrefs);
        if (rampParms == null) {
            return false;
        }
        if ((a = rampParms.getAttribute(PercentNormal)) != null) {
            setPercentNormal(a.getValue().equals("yes"));
        }
        _speedNames = new OrderedHashtable<String, Float>();
        list = rampParms.getChildren();
        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i).getName();
            Float speed = Float.valueOf(0f);
            try {
                speed = new Float(list.get(i).getText());
            } catch (NumberFormatException nfe) {
                log.error(SpeedNamePrefs + " has invalid content for " + name + " = " + list.get(i).getText());
            }
            if (log.isDebugEnabled()) {
                log.debug("Add " + name + ", " + speed + " to AspectSpeed Table");
            }
            _speedNames.put(name, speed);
        }

        rampParms = child.getChild(AppearancePrefs);
        if (rampParms == null) {
            return false;
        }
        _headAppearances = new OrderedHashtable<String, String>();
        list = rampParms.getChildren();
        for (int i = 0; i < list.size(); i++) {
            String name = Bundle.getMessage(list.get(i).getName());
            String speed = list.get(i).getText();
            _headAppearances.put(name, speed);
        }

        setSpeedMap();
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
                    log.warn("Could not create parent directory for prefs file :" + _fileName);
                    return;
                }
            }
            if (file.createNewFile()) {
                log.debug("Creating new warrant prefs file: " + _fileName);
            }
        } catch (Exception ea) {
            log.error("Could not create warrant preferences file at " + _fileName + ". " + ea);
        }

        try {
            Element root = new Element("warrantPreferences");
            Document doc = XmlFile.newDocument(root);
            if (store(root)) {
                xmlFile.writeXML(file, doc);
            }
        } catch (Exception eb) {
            log.warn("Exception in storing warrant xml: " + eb);
        }
    }

    public boolean store(Element root) {
        Element prefs = new Element(layoutParams);
        try {
            prefs.setAttribute(LayoutScale, Float.toString(getScale()));
            prefs.setAttribute(SearchDepth, Integer.toString(getSearchDepth()));
            root.addContent(prefs);

            prefs = new Element(SpeedMapParams);
            Element rampPrefs = new Element(StepIncrements);
            rampPrefs.setAttribute(TimeIncrement, Integer.toString(getTimeIncre()));
            rampPrefs.setAttribute(ThrottleScale, Float.toString(getThrottleScale()));

            Element step = new Element(ThrottleStepMode14);
            step.setText(Integer.toString(_stepIncrements.get(Bundle.getMessage(ThrottleStepMode14))));
            rampPrefs.addContent(step);
            step = new Element(ThrottleStepMode27);
            step.setText(Integer.toString(_stepIncrements.get(Bundle.getMessage(ThrottleStepMode27))));
            rampPrefs.addContent(step);
            step = new Element(ThrottleStepMode28);
            step.setText(Integer.toString(_stepIncrements.get(Bundle.getMessage(ThrottleStepMode28))));
            rampPrefs.addContent(step);
            step = new Element(ThrottleStepMode128);
            step.setText(Integer.toString(_stepIncrements.get(Bundle.getMessage(ThrottleStepMode128))));
            rampPrefs.addContent(step);
            prefs.addContent(rampPrefs);

            rampPrefs = new Element(SpeedNamePrefs);
            rampPrefs.setAttribute(PercentNormal, isPercentNormal() ? "yes" : "no");

            Iterator<Entry<String, Float>> it = getSpeedNameEntryIterator();
            while (it.hasNext()) {
                Entry<String, Float> ent = it.next();
                step = new Element(ent.getKey());
                step.setText(ent.getValue().toString());
                rampPrefs.addContent(step);
            }
            prefs.addContent(rampPrefs);

            rampPrefs = new Element(AppearancePrefs);
            step = new Element("SignalHeadStateRed");
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
        } catch (Exception ex) {
            log.warn("Exception in storing warrant xml: " + ex);
            ex.printStackTrace();
            return false;
        }
        root.addContent(prefs);
        return true;
    }

    /**
     * Apply to classes that use this data
     */
    public void apply() {
        setNXFrame();
        setSpeedMap();
    }

    private void setNXFrame() {
        NXFrame frame = NXFrame.getInstance();
        frame.setScale(_scale);
        frame.setDepth(_searchDepth);
        frame.setThrottleScale(_throttleScale);
    }

    private void setSpeedMap() {
        SignalSpeedMap map = new SignalSpeedMap();
        map.setAspectTable(getSpeedNameEntryIterator(), _percentNormal);
        map.setAppearanceTable(getAppearanceEntryIterator());
        map.setStepIncrementTable(_stepIncrements, _msIncrTime);
        map.setMap(map);
    }

    float getScale() {
        return _scale;
    }

    void setScale(float s) {
        _scale = s;
    }

    float getThrottleScale() {
        return _throttleScale;
    }

    void setThrottleScale(float f) {
        _throttleScale = f;
    }

    int getSearchDepth() {
        return _searchDepth;
    }

    void setSearchDepth(int d) {
        _searchDepth = d;
    }

    int getTimeIncre() {
        return _msIncrTime;
    }

    void setTimeIncre(int t) {
        _msIncrTime = t;
    }

    Iterator<Entry<String, Float>> getSpeedNameEntryIterator() {
        java.util.Enumeration<String> keys = _speedNames.keys();
        java.util.Vector<Entry<String, Float>> vec = new java.util.Vector<Entry<String, Float>>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            vec.add(new java.util.AbstractMap.SimpleEntry<String, Float>(key, _speedNames.get(key)));
        }
        return vec.iterator();
    }

    int getSpeedNamesSize() {
        return _speedNames.size();
    }

    Float getSpeedNameValue(String key) {
        return _speedNames.get(key);
    }

    void setSpeedNames(ArrayList<DataPair<String, Float>> speedNameMap) {
        _speedNames = new jmri.util.OrderedHashtable<String, Float>();
        for (int i = 0; i < speedNameMap.size(); i++) {
            DataPair<String, Float> dp = speedNameMap.get(i);
            _speedNames.put(dp.getKey(), dp.getValue());
        }
    }

    Iterator<Entry<String, String>> getAppearanceEntryIterator() {
        java.util.Enumeration<String> keys = _headAppearances.keys();
        java.util.Vector<Entry<String, String>> vec = new java.util.Vector<Entry<String, String>>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            vec.add(new java.util.AbstractMap.SimpleEntry<String, String>(key, _headAppearances.get(key)));
        }
        return vec.iterator();
    }

    int getAppeaancesSize() {
        return _headAppearances.size();
    }

    String getAppearanceValue(String key) {
        return _headAppearances.get(key);
    }

    void setAppearances(ArrayList<DataPair<String, String>> appearanceMap) {
        _headAppearances = new jmri.util.OrderedHashtable<String, String>();
        for (int i = 0; i < appearanceMap.size(); i++) {
            DataPair<String, String> dp = appearanceMap.get(i);
            _headAppearances.put(dp.getKey(), dp.getValue());
        }
    }

    Iterator<Entry<String, Integer>> getStepIncrementEntryIterator() {
        java.util.Enumeration<String> keys = _stepIncrements.keys();
        java.util.Vector<Entry<String, Integer>> vec = new java.util.Vector<Entry<String, Integer>>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            vec.add(new java.util.AbstractMap.SimpleEntry<String, Integer>(key, _stepIncrements.get(key)));
        }
        return vec.iterator();
    }

    int getStepIncrementSize() {
        return _stepIncrements.size();
    }

    Integer getStepIncrementValue(String key) {
        return _stepIncrements.get(key);
    }

    void setStepIncrements(ArrayList<DataPair<String, Integer>> stepIncrMap) {
        _stepIncrements = new jmri.util.OrderedHashtable<String, Integer>();
        for (int i = 0; i < stepIncrMap.size(); i++) {
            DataPair<String, Integer> dp = stepIncrMap.get(i);
            _stepIncrements.put(dp.getKey(), dp.getValue());
        }
    }

    boolean isPercentNormal() {
        return _percentNormal;
    }

    void setPercentNormal(boolean x) {
        _percentNormal = x;
    }

    public static class WarrantPreferencesXml extends XmlFile {
    }

    private static Logger log = LoggerFactory.getLogger(WarrantPreferences.class.getName());
}
