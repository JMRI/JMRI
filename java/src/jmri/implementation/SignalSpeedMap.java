package jmri.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import jmri.util.FileUtil;
import jmri.util.OrderedHashtable;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 /**
 * Default implementation to map Signal aspects or appearances to speed requirements.
 * <p>
 * The singleton instance is referenced from the InstanceManager by SignalHeads and SignalMasts
 *
 * @author  Pete Cressman Copyright (C) 2010
 */
public class SignalSpeedMap 
            implements jmri.InstanceManagerAutoDefault // auto-initialize in InstanceManager
        {

    private Hashtable<String, Float> _table;
    private Hashtable<String, String> _headTable;
    private int _interpretation;
    private int _sStepDelay;     // ramp step time interval
    private int _numSteps = 4;   // num throttle steps per ramp step - deprecated
    private float _stepIncrement = 0.04f;       // ramp step throttle increment
    private float _throttleFactor = 0.75f;
    private float _scale;
    
    static public final int PERCENT_NORMAL = 1;
    static public final int PERCENT_THROTTLE = 2;
    static public final int SPEED_MPH = 3;
    static public final int SPEED_KMPH = 4;
        
    public SignalSpeedMap() {
        loadMap();
    }
        
    void loadMap() {
        URL path = FileUtil.findURL("signalSpeeds.xml", new String[] {"", "xml/signals"});
        jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile(){};
        try {
            loadRoot(xf.rootFromURL(path));
        } catch (org.jdom2.JDOMException e) {
            log.error("error reading file \"" + path + "\" due to: " + e);
        } catch (java.io.FileNotFoundException e) {
                log.error("signalSpeeds file (" + path + ") doesn't exist in XmlFile search path.");
                throw new IllegalArgumentException("signalSpeeds file (" + path + ") doesn't exist in XmlFile search path.");
        } catch (java.io.IOException ioe) {
            log.error("error reading file \"" + path + "\" due to: " + ioe);
        }
    }

    public void loadRoot(@Nonnull Element root) {
        try {
            Element e = root.getChild("interpretation");
            String sval = e.getText().toUpperCase();
            if (sval.equals("PERCENTNORMAL")) {
                _interpretation = PERCENT_NORMAL;
            }
            else if (sval.equals("PERCENTTHROTTLE")) {
                _interpretation = PERCENT_THROTTLE;
            }
            else {
                throw new JDOMException("invalid content for interpretation: "+sval);
            }
            if (log.isDebugEnabled()) log.debug("_interpretation= "+_interpretation);

            e = root.getChild("msPerIncrement");
            _sStepDelay = 1000;
            try {
                _sStepDelay = Integer.parseInt(e.getText());
            } catch (NumberFormatException nfe) {
                throw new JDOMException("invalid content for msPerIncrement: "+e.getText());
            }
            if (_sStepDelay < 200) {
                _sStepDelay = 200;
                log.warn("\"msPerIncrement\" must be at least 200 milliseconds.");
            }
            if (log.isDebugEnabled()) log.debug("_sStepDelay = "+_sStepDelay);

            e = root.getChild("stepsPerIncrement");
            try {
                _numSteps = Integer.parseInt(e.getText());
            } catch (NumberFormatException nfe) {
                throw new JDOMException("invalid content for stepsPerIncrement: "+e.getText());
            }
            if (_numSteps < 1) {
                _numSteps = 1;
            }

            List<Element> list = root.getChild("aspectSpeeds").getChildren();
            _table = new OrderedHashtable<String, Float>();
            for (int i = 0; i < list.size(); i++) {
                String name = list.get(i).getName();
                Float speed = Float.valueOf(0f);
                try {
                    speed = Float.valueOf(list.get(i).getText());
                } catch (NumberFormatException nfe) {
                    log.error("invalid content for "+name+" = "+list.get(i).getText());
                    throw new JDOMException("invalid content for "+name+" = "+list.get(i).getText());
                }
                if (log.isDebugEnabled()) log.debug("Add "+name+", "+speed+" to AspectSpeed Table");
                _table.put(name, speed);
            }

            _headTable = new OrderedHashtable<String, String>();
            List<Element>l = root.getChild("appearanceSpeeds").getChildren();
            for (int i = 0; i < l.size(); i++) {
                String name = l.get(i).getName();
                String speed = l.get(i).getText();
                _headTable.put(Bundle.getMessage(name), speed);
                if (log.isDebugEnabled()) log.debug("Add "+name+"="+Bundle.getMessage(name)+", "+speed+" to AppearanceSpeed Table");               
            }
       } catch (org.jdom2.JDOMException e) {
            log.error("error reading speed map elements due to: " + e);
        }       
    }
    public boolean checkSpeed(String name) {
        if (name==null) {return false; }
        return _table.get(name) != null;
    }

    /**
    * @return speed from SignalMast Aspect name
    */
    public String getAspectSpeed(@Nonnull String aspect, @Nonnull jmri.SignalSystem system) {
        if (log.isDebugEnabled()) log.debug("getAspectSpeed: aspect="+aspect+", speed="+
                                            system.getProperty(aspect, "speed"));
        return (String)system.getProperty(aspect, "speed");
    }
    /**
    * @return speed from SignalMast Aspect name
    */
    public String getAspectExitSpeed(@Nonnull String aspect, @Nonnull jmri.SignalSystem system) {
        if (log.isDebugEnabled()) log.debug("getAspectExitSpeed: aspect="+aspect+", speed2="+
                                            system.getProperty(aspect, "speed2"));
        return (String)system.getProperty(aspect, "speed2");
    }
    /**
    * @return speed from SignalHead Appearance name
    */
    public String getAppearanceSpeed(@Nonnull String name) throws NumberFormatException {
        if (log.isDebugEnabled()) log.debug("getAppearanceSpeed Appearance= "+name+
                                            ", speed="+_headTable.get(name));
        return _headTable.get(name); 
    }
    public Enumeration<String> getAppearanceIterator() {
        return _headTable.keys();       
    }
    public Enumeration<String> getSpeedIterator() {
        return _table.keys();       
    }

    public java.util.Vector<String> getValidSpeedNames() {
        java.util.Enumeration<String> e = _table.keys();
        java.util.Vector<String> v = new java.util.Vector<String>();
        while (e.hasMoreElements()) {
            v.add(e.nextElement());
        }
        return v;
    }

    public float getSpeed(@Nonnull String name) {
        if ( !checkSpeed(name)) {
            // not a valid aspect
            log.warn("attempting to set invalid speed: "+name);
            //java.util.Enumeration<String> e = _table.keys();
            throw new IllegalArgumentException("attempting to get speed from invalid name: \""+name+"\"");
        }
        Float speed = _table.get(name);
        if (speed==null) {
            return 0.0f;
        }           
        return speed.floatValue();
    }
    
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification="want to detect lack of exact match in == speed test")
    public String getNamedSpeed(float speed){
        java.util.Enumeration<String> e = _table.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            
            if ( _table.get(key) == speed) {
                return key;
            }

        }
        return null;
    }

    @Deprecated
    public boolean isRatioOfNormalSpeed() {
        return (_interpretation==PERCENT_NORMAL);
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

    @Deprecated
    public int getNumSteps() {
        return _numSteps;
    }

    public void setAspectTable(@Nonnull Iterator<Entry<String, Float>> iter, int interpretation) {
        _table = new OrderedHashtable<String, Float>();
        while (iter.hasNext() ) {
            Entry<String, Float> ent = iter.next();
            _table.put(ent.getKey(), ent.getValue());
        }
        _interpretation = interpretation;
    }
    public void setAppearanceTable(@Nonnull Iterator<Entry<String, String>> iter) {
        _headTable = new OrderedHashtable<String, String>();
        while (iter.hasNext() ) {
            Entry<String, String> ent = iter.next();
            _headTable.put(ent.getKey(), ent.getValue());
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

    static private final Logger log = LoggerFactory.getLogger(SignalSpeedMap.class.getName());
}

