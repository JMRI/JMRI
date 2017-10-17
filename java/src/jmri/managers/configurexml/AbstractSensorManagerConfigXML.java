package jmri.managers.configurexml;

import java.util.List;
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
 * <P>
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
     * Default implementation for storing the contents of a SensorManager
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
        SensorManager tm = (SensorManager) o;
        if (tm.getDefaultSensorDebounceGoingActive() > 0 || tm.getDefaultSensorDebounceGoingInActive() > 0) {
            Element elem = new Element("globalDebounceTimers");
            elem.addContent(new Element("goingActive").addContent(String.valueOf(tm.getDefaultSensorDebounceGoingActive())));
            elem.addContent(new Element("goingInActive").addContent(String.valueOf(tm.getDefaultSensorDebounceGoingInActive())));
            sensors.addContent(elem);
        }

        java.util.Iterator<String> iter = tm.getSystemNameList().iterator();
        //TODO: dead code strip this
        //List<String> snl = tm.getSystemNameList();
        //AlphanumComparator ac = new AlphanumComparator();
        //Collections.sort(snl, (String s1, String s2) -> ac.compare(s1, s2));
        //java.util.Iterator<String> iter = snl.iterator();

        // don't return an element if there are not sensors to include
        if (!iter.hasNext()) {
            return null;
        }
        // store the sensors
        while (iter.hasNext()) {
            String sname = iter.next();
            log.debug("system name is " + sname);
            Sensor s = tm.getBySystemName(sname);

            String inverted = s.getInverted() ? "true" : "false";

            Element elem = new Element("sensor")
                    .setAttribute("inverted", inverted);
            elem.addContent(new Element("systemName").addContent(sname));

            // store common part
            storeCommon(s, elem);

            log.debug("store sensor " + sname);
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
            if (tm.isPullResistanceConfigurable()) {
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
    @SuppressWarnings("unchecked")
    public boolean loadSensors(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        boolean result = true;
        List<Element> sensorList = sensors.getChildren("sensor");
        if (log.isDebugEnabled()) {
            log.debug("Found " + sensorList.size() + " sensors");
        }
        SensorManager tm = InstanceManager.sensorManagerInstance();
        long goingActive = 0L;
        long goingInActive = 0L;
        if (sensors.getChild("globalDebounceTimers") != null) {
            Element timer = sensors.getChild("globalDebounceTimers");
            try {
                if (timer.getChild("goingActive") != null) {
                    String active = timer.getChild("goingActive").getText();
                    goingActive = Long.valueOf(active);
                    tm.setDefaultSensorDebounceGoingActive(goingActive);
                }
            } catch (NumberFormatException ex) {
                log.error(ex.toString());
            }

            try {
                if (timer.getChild("goingInActive") != null) {
                    String inActive = timer.getChild("goingInActive").getText();
                    goingInActive = Long.valueOf(inActive);
                    tm.setDefaultSensorDebounceGoingInActive(goingInActive);
                }
            } catch (NumberFormatException ex) {
                log.error(ex.toString());
            }

        }

        for (int i = 0; i < sensorList.size(); i++) {
            String sysName = getSystemName(sensorList.get(i));
            if (sysName == null) {
                handleException("Unexpected missing system name while loading sensors",
                        null, null, null, null);
                result = false;
                break;
            }
            boolean inverted = false;

            String userName = getUserName(sensorList.get(i));

            checkNameNormalization(sysName, userName, tm);

            if (sensorList.get(i).getAttribute("inverted") != null) {
                if (sensorList.get(i).getAttribute("inverted").getValue().equals("true")) {
                    inverted = true;
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("create sensor: (" + sysName + ")");
            }

            Sensor s;

            try {
                s = tm.newSensor(sysName, userName);
            } catch (IllegalArgumentException e) {
                handleException("Could not create sensor", null, sysName, userName, null);
                result = false;
                continue;
            }

            // load common parts
            loadCommon(s, sensorList.get(i));

            if (sensorList.get(i).getChild("debounceTimers") != null) {
                Element timer = sensorList.get(i).getChild("debounceTimers");
                try {
                    if (timer.getChild("goingActive") != null) {
                        String active = timer.getChild("goingActive").getText();
                        s.setSensorDebounceGoingActiveTimer(Long.valueOf(active));
                    }
                } catch (NumberFormatException ex) {
                    log.error(ex.toString());
                }

                try {
                    if (timer.getChild("goingInActive") != null) {
                        String inActive = timer.getChild("goingInActive").getText();
                        s.setSensorDebounceGoingInActiveTimer(Long.valueOf(inActive));
                    }
                } catch (NumberFormatException ex) {
                    log.error(ex.toString());
                }
            }

            if (sensorList.get(i).getChild("useGlobalDebounceTimer") != null) {
                if (sensorList.get(i).getChild("useGlobalDebounceTimer").getText().equals("yes")) {
                    s.setUseDefaultTimerSettings(true);
                }
            }
            s.setInverted(inverted);

            if (sensorList.get(i).getChild("pullResistance") != null) {
                String pull = sensorList.get(i).getChild("pullResistance")
                        .getText();
                log.debug("setting pull to {} for sensor {}", pull, s);
                s.setPullResistance(jmri.Sensor.PullResistance.getByShortName(pull));
            }
        }
        return result;
    }

    @Override
    public int loadOrder() {
        return InstanceManager.sensorManagerInstance().getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractSensorManagerConfigXML.class);
}
