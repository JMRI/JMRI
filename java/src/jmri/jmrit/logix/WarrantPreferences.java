package jmri.jmrit.logix;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
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

/**
 * Hold configuration data for Warrants, includes Speed Map
 * 
 * @author Pete Cressman Copyright (C) 2015
 */
public class WarrantPreferences  {

    public static final String layoutParams = "layoutParams";   // NOI18N
    public static final String LayoutScale = "layoutScale";     // NOI18N
    public static final String SearchDepth = "searchDepth";     // NOI18N
    public static final String SpeedMapParams = "speedMapParams"; // NOI18N
    public static final String RampPrefs = "rampPrefs";         // NOI18N
    public static final String TimeIncrement = "timeIncrement"; // NOI18N
    public static final String ThrottleScale = "throttleScale"; // NOI18N
    public static final String RampIncrement = "rampIncrement"; // NOI18N
    public static final String StepIncrements = "stepIncrements"; // NOI18N
    public static final String SpeedNamePrefs = "speedNames";   // NOI18N
    public static final String Interpretation = "interpretation"; // NOI18N
    public static final String AppearancePrefs = "appearancePrefs"; // NOI18N

    private String  _fileName;
    private float   _scale = 87.1f;
    private int     _searchDepth = 20;
    private float   _throttleScale = 0.5f;
    
    private OrderedHashtable<String, Float> _speedNames;
    private OrderedHashtable<String, String> _headAppearances;
    private int _interpretation = SignalSpeedMap.PERCENT_NORMAL;    // Interpretation of values in speed name table
    
    private int _msIncrTime = 1000;         // time in milliseconds between speed changes ramping up or down
    private float _throttleIncr = 0.04f;    // throttle increment for each ramp speed change
    
    WarrantPreferences(String fileName) {
        openFile(fileName);
    }
    
    public void openFile(String name){
        _fileName = name;
        WarrantPreferencesXml prefsXml = new WarrantPreferencesXml();
        File file = new File(_fileName);
        Element root;
        try {
            root = prefsXml.rootFromFile(file);
        }catch (java.io.FileNotFoundException ea) {
            log.debug("Could not find Warrant preferences file.  Normal if preferences have not been saved before.");
            root = null;
        }catch (Exception eb) {
            log.error("Exception while loading warrant preferences: " + eb);
            root = null;
        }
        if (root != null){
            log.info("Found Warrant preferences file: {}", _fileName);
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
        if (child==null) {
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
        SignalSpeedMap map = jmri.InstanceManager.getOptionalDefault(SignalSpeedMap.class);
        if (map==null) {
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
        setThrottleIncre(map.getStepIncrement());
    }
    
    public boolean loadSpeedMap(Element child) {
        if (child==null) {
            return false;
        }
        Element rampParms = child.getChild(StepIncrements);
        if (rampParms==null) {
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
        if ((a = rampParms.getAttribute(RampIncrement)) != null) {
            try {
                setThrottleIncre(a.getFloatValue());
            } catch (DataConversionException ex) {
                setThrottleIncre(0.05f);
                log.error("Unable to read ramp throttle increment. Setting to default value (0.05).", ex);
            }
        }
        if ((a = rampParms.getAttribute(ThrottleScale)) != null) {
            try {
                setThrottleScale(a.getFloatValue());
            } catch (DataConversionException ex) {
                setThrottleScale(0.70f);
                log.error("Unable to read throttle scale. Setting to default value (0.70f).", ex);
            }
        }
                
        rampParms = child.getChild(SpeedNamePrefs);
        if (rampParms==null) {
            return false;
        }
        if ((a = rampParms.getAttribute("percentNormal")) != null) {
            if (a.getValue().equals("yes")) {
                setInterpretation(1);
            } else {
                setInterpretation(2);               
            }
        }
        if ((a = rampParms.getAttribute(Interpretation)) != null) {
            try {
                setInterpretation(a.getIntValue());
            } catch (DataConversionException ex) {
                setInterpretation(1);
                log.error("Unable to read interpetation of Speed Map. Setting to default value % normal.", ex);
            }
        }
        _speedNames = new OrderedHashtable<String, Float>();
        List<Element> list = rampParms.getChildren();
        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i).getName();
            Float speed = Float.valueOf(0f);
            try {
                speed = Float.valueOf(list.get(i).getText());
            } catch (NumberFormatException nfe) {
                log.error(SpeedNamePrefs+" has invalid content for "+name+" = "+list.get(i).getText());
            }
            if (log.isDebugEnabled()) log.debug("Add "+name+", "+speed+" to AspectSpeed Table");
            _speedNames.put(name, speed);
        }

        rampParms = child.getChild(AppearancePrefs);
        if (rampParms==null) {
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

        XmlFile xmlFile = new XmlFile(){};
        xmlFile.makeBackupFile(_fileName);
        File file=new File(_fileName);
        try {
            File parentDir=file.getParentFile();
            if(!parentDir.exists()){
                if (!parentDir.mkdir()) {
                    log.warn("Could not create parent directory for prefs file :"+_fileName);
                    return;
                }
            }
            if (file.createNewFile()) log.debug("Creating new warrant prefs file: "+_fileName);
        }catch (Exception ea) {
            log.error("Could not create warrant preferences file at "+_fileName+". "+ea);
        }

        try {
            Element root = new Element("warrantPreferences");
            Document doc = XmlFile.newDocument(root);
            if (store(root)) {
                xmlFile.writeXML(file, doc);
            }
        }catch (Exception eb){
            log.warn("Exception in storing warrant xml: "+eb);
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
            rampPrefs.setAttribute(RampIncrement, Float.toString(getThrottleIncre()));
            rampPrefs.setAttribute(ThrottleScale, Float.toString(getThrottleScale()));
            prefs.addContent(rampPrefs);
            
            rampPrefs = new Element(SpeedNamePrefs);
            rampPrefs.setAttribute(Interpretation, Integer.toString(getInterpretation()));
            
            Iterator<Entry<String, Float>> it = getSpeedNameEntryIterator();
            while (it.hasNext()) {
                Entry<String, Float> ent = it.next();
                Element step =  new Element(ent.getKey());
                step.setText(ent.getValue().toString());
                rampPrefs.addContent(step);
            }
            prefs.addContent(rampPrefs);
        
            rampPrefs = new Element(AppearancePrefs);
            Element step =  new Element("SignalHeadStateRed");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateRed")));
            rampPrefs.addContent(step);
            step =  new Element("SignalHeadStateFlashingRed");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateFlashingRed")));
            rampPrefs.addContent(step);
            step =  new Element("SignalHeadStateGreen");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateGreen")));
            rampPrefs.addContent(step);
            step =  new Element("SignalHeadStateFlashingGreen");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateFlashingGreen")));
            rampPrefs.addContent(step);
            step =  new Element("SignalHeadStateYellow");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateYellow")));
            rampPrefs.addContent(step);
            step =  new Element("SignalHeadStateFlashingYellow");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateFlashingYellow")));
            rampPrefs.addContent(step);
            step =  new Element("SignalHeadStateLunar");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateLunar")));
            rampPrefs.addContent(step);
            step =  new Element("SignalHeadStateFlashingLunar");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateFlashingLunar")));
            rampPrefs.addContent(step);
            prefs.addContent(rampPrefs);            
        } catch (Exception ex) {
            log.warn("Exception in storing warrant xml: "+ex);
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
        setSpeedMap();
        setNXFrame();
    }
    private void setNXFrame() {
        NXFrame frame = NXFrame.getInstance();
        frame.setScale(_scale);
        frame.setDepth(_searchDepth);
        frame.setTimeInterval(_msIncrTime);
        frame.setRampIncrement(_throttleIncr);
        frame.updatePanel(_interpretation);
        frame.closeFrame();
    }
    private void setSpeedMap() {
        SignalSpeedMap map = new SignalSpeedMap();
        map.setAspectTable(getSpeedNameEntryIterator(), _interpretation);       
        map.setAppearanceTable(getAppearanceEntryIterator());
        map.setRampParams(_throttleIncr, _msIncrTime);
        map.setDefaultThrottleFactor(_throttleScale);
        map.setLayoutScale(_scale);
        jmri.InstanceManager.setDefault(SignalSpeedMap.class, map);        
    }

    float getScale() {
        return _scale;
    }
    void setScale(float s) {
        _scale = s;
    }
    
    public float getThrottleScale() {
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

    float getThrottleIncre() {
        return _throttleIncr;
    }
    void setThrottleIncre(float ti) {
        _throttleIncr = ti;
    }
    
    Iterator<Entry<String, Float>> getSpeedNameEntryIterator() {
        java.util.Enumeration<String> keys = _speedNames.keys();
        java.util.Vector<Entry<String, Float>> vec = new java.util.Vector<Entry<String, Float>>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            vec.add( new java.util.AbstractMap.SimpleEntry<String, Float>(key, _speedNames.get(key)));
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
        for (int i=0; i<speedNameMap.size(); i++) {
            DataPair<String, Float> dp = speedNameMap.get(i);
            _speedNames.put(dp.getKey(), dp.getValue()); 
        }
    }
    
    Iterator<Entry<String, String>> getAppearanceEntryIterator() {
        java.util.Enumeration<String> keys = _headAppearances.keys();
        java.util.Vector<Entry<String, String>> vec = new java.util.Vector<Entry<String, String>>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            vec.add( new java.util.AbstractMap.SimpleEntry<String, String>(key, _headAppearances.get(key)));
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
        for (int i=0; i<appearanceMap.size(); i++) {
            DataPair<String, String> dp = appearanceMap.get(i);
            _headAppearances.put(dp.getKey(), dp.getValue()); 
        }
    }
    
    int getInterpretation()  {
        return _interpretation;
    }
    void setInterpretation(int interp) {
        _interpretation = interp;
    }
    
    public static class WarrantPreferencesXml extends XmlFile{}

    private final static Logger log = LoggerFactory.getLogger(WarrantPreferences.class.getName());
}
