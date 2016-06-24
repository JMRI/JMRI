package jmri.managers.configurexml;

import java.util.ArrayList;
import java.util.List;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.implementation.LightControl;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the abstract base and store functionality for configuring
 * LightManagers, working with AbstractLightManagers.
 * <P>
 * Typically, a subclass will just implement the load(Element sensors) class,
 * relying on implementation here to load the individual lights. Note that these
 * are stored explicitly, so the resolution mechanism doesn't need to see *Xml
 * classes for each specific Light or AbstractLight subclass at store time.
 * <P>
 * Based on AbstractSensorManagerConfigXML.java
 *
 * @author Dave Duchamp Copyright (c) 2004, 2008, 2010
 */
public abstract class AbstractLightManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    public AbstractLightManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a LightManager
     *
     * @param o Object to store, of type LightManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element lights = new Element("lights");
        setStoreElementClass(lights);
        LightManager tm = (LightManager) o;
        if (tm != null) {
            java.util.Iterator<String> iter
                    = tm.getSystemNameList().iterator();

            // don't return an element if there are not lights to include
            if (!iter.hasNext()) {
                return null;
            }

            // store the lights
            while (iter.hasNext()) {
                String sname = iter.next();
                if (sname == null) {
                    log.error("System name null during store");
                    break;
                }
                log.debug("system name is " + sname);
                Light lgt = tm.getBySystemName(sname);
                Element elem = new Element("light")
                        .setAttribute("systemName", sname);
                elem.addContent(new Element("systemName").addContent(sname));

                // store common parts
                storeCommon(lgt, elem);

                // write variable intensity attributes
                elem.setAttribute("minIntensity", "" + lgt.getMinIntensity());
                elem.setAttribute("maxIntensity", "" + lgt.getMaxIntensity());

                // write transition attribute
                elem.setAttribute("transitionTime", "" + lgt.getTransitionTime());

                // save child lightcontrol entries
                ArrayList<LightControl> lcList = lgt.getLightControlList();
                Element lcElem = null;
                for (int i = 0; i < lcList.size(); i++) {
                    LightControl lc = lcList.get(i);
                    if (lc != null) {
                        lcElem = new Element("lightcontrol");
                        int type = lc.getControlType();
                        lcElem.setAttribute("controlType", "" + type);
                        if (type == Light.SENSOR_CONTROL) {
                            lcElem.setAttribute("controlSensor", lc.getControlSensorName());
                            lcElem.setAttribute("sensorSense", "" + lc.getControlSensorSense());
                        } else if (type == Light.FAST_CLOCK_CONTROL) {
                            lcElem.setAttribute("fastClockOnHour", "" + lc.getFastClockOnHour());
                            lcElem.setAttribute("fastClockOnMin", "" + lc.getFastClockOnMin());
                            lcElem.setAttribute("fastClockOffHour", "" + lc.getFastClockOffHour());
                            lcElem.setAttribute("fastClockOffMin", "" + lc.getFastClockOffMin());
                        } else if (type == Light.TURNOUT_STATUS_CONTROL) {
                            lcElem.setAttribute("controlTurnout", lc.getControlTurnoutName());
                            lcElem.setAttribute("turnoutState", "" + lc.getControlTurnoutState());
                        } else if (type == Light.TIMED_ON_CONTROL) {
                            lcElem.setAttribute("timedControlSensor", lc.getControlTimedOnSensorName());
                            lcElem.setAttribute("duration", "" + lc.getTimedOnDuration());
                        }
                        if (type == Light.TWO_SENSOR_CONTROL) {
                            lcElem.setAttribute("controlSensor", lc.getControlSensorName());
                            lcElem.setAttribute("controlSensor2", lc.getControlSensor2Name());
                            lcElem.setAttribute("sensorSense", "" + lc.getControlSensorSense());
                        }
                        elem.addContent(lcElem);
                    }
                }
                lights.addContent(elem);
            }
        }
        return lights;
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param lights The top-level element being created
     */
    abstract public void setStoreElementClass(Element lights);

    /**
     * Utility method to load the individual Light objects. If there's no
     * additional info needed for a specific light type, invoke this with the
     * parent of the set of Light elements.
     *
     * @param lights Element containing the Light elements to load.
     */
    @SuppressWarnings("unchecked")
    public boolean loadLights(Element lights) {
        boolean result = true;
        List<Element> lightList = lights.getChildren("light");
        if (log.isDebugEnabled()) {
            log.debug("Found " + lightList.size() + " lights");
        }
        LightManager tm = InstanceManager.lightManagerInstance();

        for (int i = 0; i < lightList.size(); i++) {

            String sysName = getSystemName(lightList.get(i));
            if (sysName == null) {
                log.warn("unexpected null in systemName " + lightList.get(i) + " " + lightList.get(i).getAttributes());
                result = false;
                break;
            }

            String userName = getUserName(lightList.get(i));

            if (log.isDebugEnabled()) {
                log.debug("create light: (" + sysName + ")("
                        + (userName == null ? "<null>" : userName) + ")");
            }
            Light lgt = tm.newLight(sysName, userName);
            if (lgt != null) {
                // load common parts
                loadCommon(lgt, lightList.get(i));

                // variable intensity, transition attributes
                double value;
                value = Double.parseDouble(lightList.get(i).getAttribute("minIntensity").getValue());
                lgt.setMinIntensity(value);

                value = Double.parseDouble(lightList.get(i).getAttribute("maxIntensity").getValue());
                lgt.setMaxIntensity(value);

                value = Double.parseDouble(lightList.get(i).getAttribute("transitionTime").getValue());
                lgt.setTransitionTime(value);

                // provide for legacy light control - panel files written by 2.9.5 or before
                if (lightList.get(i).getAttribute("controlType") != null) {
                    // this is a legacy Light - create a LightControl from the input
                    String temString = lightList.get(i).getAttribute("controlType").getValue();
                    int type = Light.NO_CONTROL;
                    try {
                        type = Integer.parseInt(temString);
                    } catch (NumberFormatException e) {
                        log.error("error when converting control type in legacy Light load support");
                        type = Light.NO_CONTROL;
                    }
                    if (type != Light.NO_CONTROL) {
                        // this legacy light has a control - capture it
                        LightControl lc = new LightControl(lgt);
                        lc.setControlType(type);
                        if (type == Light.SENSOR_CONTROL) {
                            lc.setControlSensorName(lightList.get(i).
                                    getAttribute("controlSensor").getValue());
                            try {
                                lc.setControlSensorSense(Integer.parseInt(lightList.get(i).
                                        getAttribute("sensorSense").getValue()));
                            } catch (NumberFormatException e) {
                                log.error("error when converting control sensor sense in legacy Light load");
                            }
                        } else if (type == Light.FAST_CLOCK_CONTROL) {
                            int onHour = 0;
                            int onMin = 0;
                            int offHour = 0;
                            int offMin = 0;
                            try {
                                onHour = Integer.parseInt(lightList.get(i).
                                        getAttribute("fastClockOnHour").getValue());
                                onMin = Integer.parseInt(lightList.get(i).
                                        getAttribute("fastClockOnMin").getValue());
                                offHour = Integer.parseInt(lightList.get(i).
                                        getAttribute("fastClockOffHour").getValue());
                                offMin = Integer.parseInt(lightList.get(i).
                                        getAttribute("fastClockOffMin").getValue());
                            } catch (NumberFormatException e) {
                                log.error("error when converting fast clock items in legacy Light load");
                            }
                            lc.setFastClockControlSchedule(onHour, onMin, offHour, offMin);
                        } else if (type == Light.TURNOUT_STATUS_CONTROL) {
                            lc.setControlTurnout(lightList.get(i).
                                    getAttribute("controlTurnout").getValue());
                            try {
                                lc.setControlTurnoutState(Integer.parseInt(lightList.get(i).
                                        getAttribute("turnoutState").getValue()));
                            } catch (NumberFormatException e) {
                                log.error("error when converting turnout state in legacy Light load");
                            }
                        } else if (type == Light.TIMED_ON_CONTROL) {
                            lc.setControlTimedOnSensorName(lightList.get(i).
                                    getAttribute("timedControlSensor").getValue());
                            try {
                                lc.setTimedOnDuration(Integer.parseInt(lightList.get(i).
                                        getAttribute("duration").getValue()));
                            } catch (NumberFormatException e) {
                                log.error("error when converting timed sensor items in legacy Light load");
                            }

                        }
                        lgt.addLightControl(lc);
                    }
                }

                // load lightcontrol children, if any
                List<Element> lightControlList = lightList.get(i).getChildren("lightcontrol");
                for (int n = 0; n < lightControlList.size(); n++) {
                    boolean noErrors = true;
                    Element elem = lightControlList.get(n);
                    LightControl lc = new LightControl(lgt);
                    String tem = elem.getAttribute("controlType").getValue();
                    int type = Light.NO_CONTROL;
                    try {
                        type = Integer.parseInt(tem);
                        lc.setControlType(type);
                    } catch (NumberFormatException e) {
                        log.error("error when converting control type while loading light " + sysName);
                        noErrors = false;
                    }
                    if (type == Light.SENSOR_CONTROL) {
                        lc.setControlSensorName(elem.getAttribute("controlSensor").getValue());
                        try {
                            lc.setControlSensorSense(Integer.parseInt(elem.
                                    getAttribute("sensorSense").getValue()));
                        } catch (NumberFormatException e) {
                            log.error("error when converting control sensor sense while loading light " + sysName);
                            noErrors = false;
                        }
                    } else if (type == Light.FAST_CLOCK_CONTROL) {
                        int onHour = 0;
                        int onMin = 0;
                        int offHour = 0;
                        int offMin = 0;
                        try {
                            onHour = Integer.parseInt(elem.
                                    getAttribute("fastClockOnHour").getValue());
                            onMin = Integer.parseInt(elem.
                                    getAttribute("fastClockOnMin").getValue());
                            offHour = Integer.parseInt(elem.
                                    getAttribute("fastClockOffHour").getValue());
                            offMin = Integer.parseInt(elem.
                                    getAttribute("fastClockOffMin").getValue());
                            lc.setFastClockControlSchedule(onHour, onMin, offHour, offMin);
                        } catch (NumberFormatException e) {
                            log.error("error when converting fast clock items while loading light " + sysName);
                            noErrors = false;
                        }
                    } else if (type == Light.TURNOUT_STATUS_CONTROL) {
                        lc.setControlTurnout(elem.getAttribute("controlTurnout").getValue());
                        try {
                            lc.setControlTurnoutState(Integer.parseInt(elem.
                                    getAttribute("turnoutState").getValue()));
                        } catch (NumberFormatException e) {
                            log.error("error when converting turnout state while loading light " + sysName);
                            noErrors = false;
                        }
                    } else if (type == Light.TIMED_ON_CONTROL) {
                        lc.setControlTimedOnSensorName(elem.getAttribute("timedControlSensor").getValue());
                        try {
                            lc.setTimedOnDuration(Integer.parseInt(elem.
                                    getAttribute("duration").getValue()));
                        } catch (NumberFormatException e) {
                            log.error("error when converting timed sensor items while loading light " + sysName);
                            noErrors = false;
                        }
                    } else if (type == Light.TWO_SENSOR_CONTROL) {
                        lc.setControlSensorName(elem.getAttribute("controlSensor").getValue());
                        lc.setControlSensor2Name(elem.getAttribute("controlSensor2").getValue());
                        try {
                            lc.setControlSensorSense(Integer.parseInt(elem.
                                    getAttribute("sensorSense").getValue()));
                        } catch (NumberFormatException e) {
                            log.error("error when converting control sensor2 sense while loading light " + sysName);
                            noErrors = false;
                        }
                    }
                    if (noErrors) {
                        lgt.addLightControl(lc);
                    }
                }

                // done, start it working
                lgt.activateLight();
            } else {
                log.error("failed to create Light: " + sysName);
                result = false;
            }
        }
        return result;
    }

    public int loadOrder() {
        return InstanceManager.lightManagerInstance().getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractLightManagerConfigXML.class.getName());
}
