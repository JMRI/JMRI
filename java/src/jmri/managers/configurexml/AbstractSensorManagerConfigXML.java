package jmri.managers.configurexml;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.Sensor;

import java.util.List;
import org.jdom.Element;

/**
 * Provides the abstract base and store functionality for
 * configuring SensorManagers, working with
 * AbstractSensorManagers.
 * <P>
 * Typically, a subclass will just implement the load(Element sensors)
 * class, relying on implementation here to load the individual sensors.
 * Note that these are stored explicitly, so the
 * resolution mechanism doesn't need to see *Xml classes for each
 * specific Sensor or AbstractSensor subclass at store time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @version $Revision$
 */
public abstract class AbstractSensorManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    public AbstractSensorManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a
     * SensorManager
     * @param o Object to store, of type SensorManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element sensors = new Element("sensors");
        setStoreElementClass(sensors);
        SensorManager tm = (SensorManager) o;
        if(tm.getDefaultSensorDebounceGoingActive()>0 || tm.getDefaultSensorDebounceGoingInActive()>0){
            Element elem = new Element("globalDebounceTimers");
            elem.addContent(new Element("goingActive").addContent(String.valueOf(tm.getDefaultSensorDebounceGoingActive())));
            elem.addContent(new Element("goingInActive").addContent(String.valueOf(tm.getDefaultSensorDebounceGoingInActive())));
            sensors.addContent(elem);
        }
        java.util.Iterator<String> iter =
                                tm.getSystemNameList().iterator();

        // don't return an element if there are not sensors to include
        if (!iter.hasNext()) return null;
        // store the sensors
        while (iter.hasNext()) {
            String sname = iter.next();
            if (sname==null) log.error("System name null during store");
            log.debug("system name is "+sname);
            Sensor s = tm.getBySystemName(sname);

            String inverted = s.getInverted() ? "true" : "false";

            Element elem = new Element("sensor")
                        .setAttribute("systemName", sname) // deprecated for 2.9.* series
                        .setAttribute("inverted", inverted);
            elem.addContent(new Element("systemName").addContent(sname));
            log.debug("store sensor "+sname);
            if(s.useDefaultTimerSettings()){
                elem.addContent(new Element("useGlobalDebounceTimer").addContent("yes"));
            } else {
                if(s.getSensorDebounceGoingActiveTimer()>0 || s.getSensorDebounceGoingInActiveTimer()>0){
                    Element timer = new Element("debounceTimers");
                    timer.addContent(new Element("goingActive").addContent(String.valueOf(s.getSensorDebounceGoingActiveTimer())));
                    timer.addContent(new Element("goingInActive").addContent(String.valueOf(s.getSensorDebounceGoingInActiveTimer())));
                    elem.addContent(timer);
                }
            }
            // store common part
            storeCommon(s, elem);

            sensors.addContent(elem);

        }
        return sensors;
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param sensors The top-level element being created
     */
    abstract public void setStoreElementClass(Element sensors);

    /**
     * Create a SensorManager object of the correct class, then
     * register and fill it.
     * @param sensors Top level Element to unpack.
     */
    abstract public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException;

    /**
     * Utility method to load the individual Sensor objects.
     * If there's no additional info needed for a specific sensor type,
     * invoke this with the parent of the set of Sensor elements.
     * @param sensors Element containing the Sensor elements to load.
     * @return true if succeeded
     */
    @SuppressWarnings("unchecked")
	public boolean loadSensors(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
    	boolean result = true;
        List<Element> sensorList = sensors.getChildren("sensor");
        if (log.isDebugEnabled()) log.debug("Found "+sensorList.size()+" sensors");
        SensorManager tm = InstanceManager.sensorManagerInstance();
        long goingActive = 0L;
        long goingInActive = 0L;
        if (sensors.getChild("globalDebounceTimers")!=null){
            Element timer = sensors.getChild("globalDebounceTimers");
            try{
                if(timer.getChild("goingActive")!=null){
                    String active = timer.getChild("goingActive").getText();
                    goingActive = Long.valueOf(active);
                    tm.setDefaultSensorDebounceGoingActive(goingActive);
                }
            } catch (NumberFormatException ex) {
                log.error(ex.toString());
            }
            
            try{
                if(timer.getChild("goingInActive")!=null){
                    String inActive = timer.getChild("goingInActive").getText();
                    goingInActive = Long.valueOf(inActive);
                    tm.setDefaultSensorDebounceGoingInActive(goingInActive);
                }
            } catch (NumberFormatException ex) {
                log.error(ex.toString());
            }
            
        }
    
        for (int i=0; i<sensorList.size(); i++) {
            String sysName = getSystemName(sensorList.get(i));
            if (sysName == null) {
                creationErrorEncountered (org.apache.log4j.Level.ERROR,
                                      "Unexpected missing system name while loading sensors",
                                      null,null,null);
                result = false;
                break;
            }
            boolean inverted = false;
            
            String userName = getUserName(sensorList.get(i));

            if (sensorList.get(i).getAttribute("inverted") != null)
                if (sensorList.get(i).getAttribute("inverted").getValue().equals("true"))
                    inverted = true;

            if (log.isDebugEnabled()) log.debug("create sensor: ("+sysName+")");
            Sensor s = tm.newSensor(sysName, userName);
            
            if (s==null){
                creationErrorEncountered (org.apache.log4j.Level.WARN,
                                      "Could not create sensor",
                                      sysName,userName,null);
            	result = false;
            	continue;
            }

            // load common parts
            loadCommon(s, sensorList.get(i));
            
            if(sensorList.get(i).getChild("debounceTimers")!=null){
                Element timer = sensorList.get(i).getChild("debounceTimers");
                try{
                    if(timer.getChild("goingActive")!=null){
                        String active = timer.getChild("goingActive").getText();
                        s.setSensorDebounceGoingActiveTimer(Long.valueOf(active));
                    }
                } catch (NumberFormatException ex) {
                    log.error(ex.toString());
                }
                
                try{
                    if(timer.getChild("goingInActive")!=null){
                        String inActive = timer.getChild("goingInActive").getText();
                        s.setSensorDebounceGoingInActiveTimer(Long.valueOf(inActive));
                    }
                } catch (NumberFormatException ex) {
                    log.error(ex.toString());
                }
            }
            
            if (sensorList.get(i).getChild("useGlobalDebounceTimer")!=null){
                if(sensorList.get(i).getChild("useGlobalDebounceTimer").getText().equals("yes")){
                    s.useDefaultTimerSettings(true);
                }
            }
            s.setInverted(inverted);
        }
        return result;
    }
    
    public int loadOrder(){
        return InstanceManager.sensorManagerInstance().getXMLOrder();
    }

    static Logger log = Logger.getLogger(AbstractSensorManagerConfigXML.class.getName());
}
