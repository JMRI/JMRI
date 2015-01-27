// SignalSpeedMap.java

package jmri.implementation;

import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import jmri.DccThrottle;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.FileUtil;
import jmri.util.OrderedHashtable;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    static private Hashtable<String, Float> _table;
    static private Hashtable<String, String> _headTable;
    static private Hashtable<Integer, Integer> _stepIncrementTable;
    static private boolean _percentNormal;
    static private int _sStepDelay;
    
    public SignalSpeedMap() {}
    
    static public SignalSpeedMap getMap() {
        if (_map == null) {
            loadMap();
        }
        return _map;
    }
    
    static void loadMap() {
        _map = new SignalSpeedMap();

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

    static public void loadRoot(Element root) {
        try {
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
                log.warn("\"msPerIncrement\" must be at least 200 milliseconds.");
            }
            if (log.isDebugEnabled()) log.debug("_sStepDelay = "+_sStepDelay);

            e = root.getChild("stepsPerIncrement");
            int numSteps = 1;
            try {
                numSteps = Integer.parseInt(e.getText());
            } catch (NumberFormatException nfe) {
                throw new JDOMException("invalid content for stepsPerIncrement: "+e.getText());
            }
            if (numSteps < 1) {
                numSteps = 1;
            }
            _stepIncrementTable = new OrderedHashtable<Integer, Integer>();
            _stepIncrementTable.put(Integer.valueOf(DccThrottle.SpeedStepMode14), Integer.valueOf(numSteps));
            _stepIncrementTable.put(Integer.valueOf(DccThrottle.SpeedStepMode27), Integer.valueOf(2*numSteps));
            _stepIncrementTable.put(Integer.valueOf(DccThrottle.SpeedStepMode28), Integer.valueOf(2*numSteps));
            _stepIncrementTable.put(Integer.valueOf(DccThrottle.SpeedStepMode128), Integer.valueOf(4*numSteps));

            List<Element> list = root.getChild("aspectSpeeds").getChildren();
            _table = new OrderedHashtable<String, Float>();
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
    public Enumeration<String> getAppearanceIterator() {
        return _headTable.keys();     	
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
            throw new IllegalArgumentException("attempting to get speed from invalid name: \""+name+"\"");
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

    @Deprecated
    public int getNumSteps() {
        Integer steps = _stepIncrementTable.get(Integer.valueOf(jmri.DccThrottle.SpeedStepMode14));
        if (steps!=null) {
        	return steps.intValue();
        } else {
        	return 4;
        }
    }
    public int getNumStepsFromMode(int throttleStepMode) {
        Integer steps = _stepIncrementTable.get(Integer.valueOf(throttleStepMode));    	
        if (steps!=null) {
        	return steps.intValue();
        } else {
        	return 4;
        }
    }

	public void setAspectTable(Iterator<Entry<String, Float>> iter, boolean _percentNormal) {
		_table = new OrderedHashtable<String, Float>();
		while (iter.hasNext() ) {
			Entry<String, Float> ent = iter.next();
			_table.put(ent.getKey(), ent.getValue());
		}
	}
	public void setAppearanceTable(Iterator<Entry<String, String>> iter) {
		_headTable = new OrderedHashtable<String, String>();
		while (iter.hasNext() ) {
			Entry<String, String> ent = iter.next();
			_headTable.put(ent.getKey(), ent.getValue());
		}
	}
	public void setStepIncrementTable(OrderedHashtable<String, Integer> stepIncrementTable, int msIncrTime) {
		_sStepDelay = msIncrTime;
		_stepIncrementTable = new OrderedHashtable<Integer, Integer>();
        Integer steps = stepIncrementTable.get(WarrantPreferences.ThrottleStepMode14);
        if (steps==null) {
        	steps = 1;
        }
    	_stepIncrementTable.put(Integer.valueOf(DccThrottle.SpeedStepMode14), Integer.valueOf(steps));
        steps = stepIncrementTable.get(WarrantPreferences.ThrottleStepMode27);
        if (steps==null) {
        	steps = 2;
        }
    	_stepIncrementTable.put(Integer.valueOf(DccThrottle.SpeedStepMode27), Integer.valueOf(steps));
        steps = stepIncrementTable.get(WarrantPreferences.ThrottleStepMode28);
        if (steps==null) {
        	steps = 2;
        }
    	_stepIncrementTable.put(Integer.valueOf(DccThrottle.SpeedStepMode28), Integer.valueOf(steps));
        steps = stepIncrementTable.get(WarrantPreferences.ThrottleStepMode128);
        if (steps==null) {
        	steps = 4;
        }
    	_stepIncrementTable.put(Integer.valueOf(DccThrottle.SpeedStepMode128), Integer.valueOf(steps));
	}

	public void setMap(SignalSpeedMap map) {
		_map = map;
	}
    static Logger log = LoggerFactory.getLogger(SignalSpeedMap.class.getName());
}

/* @(#)SignalSpeedMap.java */
