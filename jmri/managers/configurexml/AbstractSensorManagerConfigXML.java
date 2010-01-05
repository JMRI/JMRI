package jmri.managers.configurexml;

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
 * @version $Revision: 1.5 $
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
        if (tm!=null) {
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
                            .setAttribute("systemName", sname)
                            .setAttribute("inverted", inverted);
                log.debug("store sensor "+sname);

                // store common part
                storeCommon(s, elem);
                
                sensors.addContent(elem);

            }
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

        for (int i=0; i<sensorList.size(); i++) {
            if (sensorList.get(i).getAttribute("systemName") == null) {
                creationErrorEncountered (org.apache.log4j.Level.ERROR,
                                      "Unexpected missing system name while loading sensors",
                                      null,null,null);
                result = false;
                break;
            }
            String sysName = sensorList.get(i).getAttribute("systemName").getValue();
            boolean inverted = false;
            
            String userName = null;
            if (sensorList.get(i).getAttribute("userName") != null)
                userName = sensorList.get(i).getAttribute("userName").getValue();

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
            
            s.setInverted(inverted);
        }
        return result;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractSensorManagerConfigXML.class.getName());
}