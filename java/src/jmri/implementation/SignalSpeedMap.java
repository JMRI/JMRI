// SignalSpeedMap.java

package jmri.implementation;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import org.jdom.Element;
import org.jdom.JDOMException;

 /**
 * Default implementation to map Signal aspects or appearances to speed requirements.
 * <p>
 * A singleton class for use by all SignalHeads and SignalMasts
 *
 * @author	Pete Cressman Copyright (C) 2010
 * @version     $Revision$
 */
public class SignalSpeedMap {

    static private SignalSpeedMap _map;
    static private Hashtable<String, Float> _table = new jmri.util.OrderedHashtable<String, Float>();
    static private Hashtable<String, String> _headTable = new jmri.util.OrderedHashtable<String, String>();
    static private boolean _percentNormal;
    static private int _sStepDelay;
    static private int _numSteps;

    static public SignalSpeedMap getMap() {
        if (_map == null) {
            loadMap();
        }
        return _map;
    }
    
    static void loadMap() {
        _map = new SignalSpeedMap();

        String path = "xml" + File.separator
                + "signals" + File.separator
                + "signalSpeeds.xml";
        jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile(){};
        Element root;
        try {
            root = xf.rootFromName(path);
            Element e = root.getChild("interpretation");
            String sval = e.getText().toUpperCase();
            if (sval.equals("PERCENTNORMAL")) {
                _percentNormal = true;
            }
            else if (sval.equals("PERCENTTHROTTLE")) {
                _percentNormal = false;
            }
            else {
                throw new JDOMException("invalid content for interpretation: "+sval);
            }
            if (log.isDebugEnabled()) log.debug("_percentNormal "+_percentNormal);

            e = root.getChild("msPerIncrement");
            _sStepDelay = 250;
            try {
                _sStepDelay = Integer.parseInt(e.getText());
            } catch (NumberFormatException nfe) {
                throw new JDOMException("invalid content for msPerIncrement: "+e.getText());
            }
            if (_sStepDelay < 200) {
                _sStepDelay = 200;
                log.warn("\"msPerIncrement\" must be at lewast 200 milliseconds.");
            }
            if (log.isDebugEnabled()) log.debug("_sStepDelay = "+_sStepDelay);

            e = root.getChild("stepsPerIncrement");
            _numSteps = 1;
            try {
                _numSteps = Integer.parseInt(e.getText());
            } catch (NumberFormatException nfe) {
                throw new JDOMException("invalid content for msPerIncrement: "+e.getText());
            }
            if (_numSteps < 1) {
                _numSteps = 1;
            }
            if (log.isDebugEnabled()) log.debug("_numSteps = "+_numSteps);

            @SuppressWarnings("unchecked")
            List<Element> list = root.getChild("aspectSpeeds").getChildren();
            for (int i = 0; i < list.size(); i++) {
                String name = list.get(i).getName();
                Float speed = Float.valueOf(0f);
                try {
                    speed = new Float(list.get(i).getText());
                } catch (NumberFormatException nfe) {
                    log.error("invalid content for "+name+" = "+list.get(i).getText());
                    throw new JDOMException("invalid content for "+name+" = "+list.get(i).getText());
                }
                if (log.isDebugEnabled()) log.debug("Add "+name+", "+speed+" to AspectSpeed Table");
                _table.put(name, speed);
            }

            @SuppressWarnings("unchecked")
            List<Element>l = root.getChild("appearanceSpeeds").getChildren();
            for (int i = 0; i < l.size(); i++) {
                String name = l.get(i).getName();
                String speed = l.get(i).getText();
                _headTable.put(Bundle.getMessage(name), speed);
                if (log.isDebugEnabled()) log.debug("Add "+name+"="+Bundle.getMessage(name)+", "+speed+" to AppearanceSpeed Table");
            }
        } catch (org.jdom.JDOMException e) {
            log.error("error reading file \"" + path + "\" due to: " + e);
        } catch (java.io.FileNotFoundException e) {
                log.error("signalSpeeds file (" + path + ") doesn't exist in XmlFile search path.");
                throw new IllegalArgumentException("signalSpeeds file (" + path + ") doesn't exist in XmlFile search path.");
        } catch (java.io.IOException ioe) {
            log.error("error reading file \"" + path + "\" due to: " + ioe);
        }
    }

    public boolean checkSpeed(String name) {
        return _table.get(name) != null;
    }

    /**
    * @return speed from SignalMast Aspect name
    */
    public String getAspectSpeed(String aspect, jmri.SignalSystem system) {
        if (log.isDebugEnabled()) log.debug("getAspectSpeed: aspect="+aspect+", speed="+
                                            system.getProperty(aspect, "speed"));
        return (String)system.getProperty(aspect, "speed");
    }
    /**
    * @return speed from SignalMast Aspect name
    */
    public String getAspectExitSpeed(String aspect, jmri.SignalSystem system) {
        if (log.isDebugEnabled()) log.debug("getAspectExitSpeed: aspect="+aspect+", speed2="+
                                            system.getProperty(aspect, "speed2"));
        return (String)system.getProperty(aspect, "speed2");
    }
    /**
    * @return speed from SignalHead Appearance name
    */
    public String getAppearanceSpeed(String name) throws NumberFormatException {
        if (log.isDebugEnabled()) log.debug("getAppearanceSpeed Appearance= "+name+
                                            ", speed="+_headTable.get(name));
        return _headTable.get(name); 
    }

    public java.util.Vector<String> getValidSpeedNames() {
        java.util.Enumeration<String> e = _table.keys();
        java.util.Vector<String> v = new java.util.Vector<String>();
        while (e.hasMoreElements()) {
            v.add(e.nextElement());
        }
        return v;
    }

    public float getSpeed(String name) {
        if ( !checkSpeed(name)) {
            // not a valid aspect
            log.warn("attempting to set invalid speed: "+name);
            //java.util.Enumeration<String> e = _table.keys();
            throw new IllegalArgumentException("attempting to get speed from invalid name: "+name);
        }
        Float speed = _table.get(name);
        if (speed==null) {
        	return 0.0f;
        }        	
        return speed.floatValue();
    }
    
    public String getNamedSpeed(float speed){
        java.util.Enumeration<String> e = _table.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            if(_table.get(key)==Float.valueOf(speed)) {
                return key;
            }
        }
        return null;
    }

    public boolean isRatioOfNormalSpeed() {
        return _percentNormal;
    }

    public int getStepDelay() {
        return _sStepDelay;
    }

    public int getNumSteps() {
        return _numSteps;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalSpeedMap.class.getName());
}

/* @(#)SignalSpeedMap.java */
