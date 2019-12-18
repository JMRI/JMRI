package jmri.managers.configurexml;

import java.util.List;
import java.util.SortedSet;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the abstract base and store functionality for configuring
 * SensorManagers, working with AbstractSensorManagers.
 * <p>
 * Typically, a subclass will just implement the load(Element sensors) class,
 * relying on implementation here to load the individual sensors. Note that
 * these are stored explicitly, so the resolution mechanism doesn't need to see
 * *Xml classes for each specific Sensor or AbstractSensor subclass at store
 * time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 */
public abstract class AbstractSensorManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    public AbstractSensorManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a SensorManager.
     *
     * @param o Object to store, of type SensorManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element sensors = new Element("sensors");
        return store(o, sensors);
    }

    public Element store(Object o, Element sensors) {
        setStoreElementClass(sensors);
        SensorManager sm = (SensorManager) o;
        if (sm.getDefaultSensorDebounceGoingActive() > 0 || sm.getDefaultSensorDebounceGoingInActive() > 0) {
            Element elem = new Element("globalDebounceTimers");
            elem.addContent(new Element("goingActive").addContent(String.valueOf(sm.getDefaultSensorDebounceGoingActive())));
            elem.addContent(new Element("goingInActive").addContent(String.valueOf(sm.getDefaultSensorDebounceGoingInActive())));
            sensors.addContent(elem);
        }
        SortedSet<Sensor> sensorList = sm.getNamedBeanSet();
        // don't return an element if there are no sensors to include
        if (sensorList.isEmpty()) {
            return null;
        }
        // store the sensors
        for (Sensor s : sensorList) {
            String sName = s.getSystemName();
            log.debug("system name is {}", sName);
            String inverted = (s.getInverted() ? "true" : "false");

            Element elem = new Element("sensor").setAttribute("inverted", inverted);
            elem.addContent(new Element("systemName").addContent(sName));

            // store common part
            storeCommon(s, elem);

            log.debug("store Sensor {}", sName);
            if (s.getUseDefaultTimerSettings()) {
                elem.addContent(new Element("useGlobalDebounceTimer").addContent("yes"));
            } else {
                if (s.getSensorDebounceGoingActiveTimer() > 0 || s.getSensorDebounceGoingInActiveTimer() > 0) {
                    Element timer = new Element("debounceTimers");
                    timer.addContent(new Element("goingActive").addContent(String.valueOf(s.getSensorDebounceGoingActiveTimer())));
                    timer.addContent(new Element("goingInActive").addContent(String.valueOf(s.getSensorDebounceGoingInActiveTimer())));
                    elem.addContent(timer);
                }
            }
            if (sm.isPullResistanceConfigurable()) {
                // store the sensor's value for pull resistance.
                elem.addContent(new Element("pullResistance").addContent(s.getPullResistance().getShortName()));
            }

            sensors.addContent(elem);
        }
        return sensors;
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param sensors The top-level element being created
     */
    abstract public void setStoreElementClass(Element sensors);

    /**
     * Create a SensorManager object of the correct class, then register and
     * fill it.
     *
     * @param sharedSensors  Shared top level Element to unpack.
     * @param perNodeSensors Per-node top level Element to unpack.
     * @return true if successful
     * @throws jmri.configurexml.JmriConfigureXmlException if error during load
     */
    @Override
    abstract public boolean load(Element sharedSensors, Element perNodeSensors) throws JmriConfigureXmlException;

    /**
     * Utility method to load the individual Sensor objects. If there's no
     * additional info needed for a specific sensor type, invoke this with the
     * parent of the set of Sensor elements.
     *
     * @param sensors Element containing the Sensor elements to load.
     * @return true if succeeded
     */
    public boolean loadSensors(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        boolean result = true;
        List<Element> sensorList = sensors.getChildren("sensor");
        log.debug("Found {} sensors", sensorList.size());
        SensorManager tm = InstanceManager.sensorManagerInstance();
        tm.setDataListenerMute(true);
        long goingActive = 0L;
        long goingInActive = 0L;
        if (sensors.getChild("globalDebounceTimers") != null) {
            Element timer = sensors.getChild("globalDebounceTimers");
            try {
                if (timer.getChild("goingActive") != null) {
                    String active = timer.getChild("goingActive").getText();
                    goingActive = Long.parseLong(active);
                    tm.setDefaultSensorDebounceGoingActive(goingActive);
                }
            } catch (NumberFormatException ex) {
                log.error(ex.toString());
            }

            try {
                if (timer.getChild("goingInActive") != null) {
                    String inActive = timer.getChild("goingInActive").getText();
                    goingInActive = Long.parseLong(inActive);
                    tm.setDefaultSensorDebounceGoingInActive(goingInActive);
                }
            } catch (NumberFormatException ex) {
                log.error(ex.toString());
            }
        }

        for (Element sen : sensorList) {
            String sysName = getSystemName(sen);
            if (sysName == null) {
                handleException("Unexpected missing system name while loading sensors",
                        null, null, null, null);
                result = false;
                break;
            }
            boolean inverted = false;

            String userName = getUserName(sen);

            checkNameNormalization(sysName, userName, tm);

            if (sen.getAttribute("inverted") != null) {
                if (sen.getAttribute("inverted").getValue().equals("true")) {
                    inverted = true;
                }
            }

            log.debug("create sensor: ({})", sysName);

            Sensor s;

            try {
                s = tm.newSensor(sysName, userName);
            } catch (IllegalArgumentException e) {
                handleException("Could not create sensor", null, sysName, userName, null);
                result = false;
                continue;
            }

            // load common parts
            loadCommon(s, sen);

            if (sen.getChild("debounceTimers") != null) {
                Element timer = sen.getChild("debounceTimers");
                try {
                    if (timer.getChild("goingActive") != null) {
                        String active = timer.getChild("goingActive").getText();
                        s.setSensorDebounceGoingActiveTimer(Long.parseLong(active));
                    }
                } catch (NumberFormatException ex) {
                    log.error(ex.toString());
                }

                try {
                    if (timer.getChild("goingInActive") != null) {
                        String inActive = timer.getChild("goingInActive").getText();
                        s.setSensorDebounceGoingInActiveTimer(Long.parseLong(inActive));
                    }
                } catch (NumberFormatException ex) {
                    log.error(ex.toString());
                }
            }

            if (sen.getChild("useGlobalDebounceTimer") != null) {
                if (sen.getChild("useGlobalDebounceTimer").getText().equals("yes")) {
                    s.setUseDefaultTimerSettings(true);
                }
            }
            s.setInverted(inverted);

            if (sen.getChild("pullResistance") != null) {
                String pull = sen.getChild("pullResistance")
                        .getText();
                log.debug("setting pull to {} for sensor {}", pull, s);
                s.setPullResistance(jmri.Sensor.PullResistance.getByShortName(pull));
            }
        }
        tm.setDataListenerMute(false);
        return result;
    }

    @Override
    public int loadOrder() {
        return InstanceManager.sensorManagerInstance().getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractSensorManagerConfigXML.class);

}
