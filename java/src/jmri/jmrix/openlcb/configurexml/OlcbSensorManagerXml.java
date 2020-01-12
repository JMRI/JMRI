package jmri.jmrix.openlcb.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.openlcb.OlcbConfigurationManager;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring OlcbSensorManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008, 2010
 * @since 2.3.1
 */
public class OlcbSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public OlcbSensorManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        boolean result = true;
        // We tell the Sensor managers that we will be loading Sensors from XML and they should
        // expect additional property set sequences. This is somewhat tricky in the face of
        // possibly multiple OpenLCB buses registered.
        for (OlcbConfigurationManager cfg : InstanceManager.getList(OlcbConfigurationManager
                .class)) {
            cfg.getSensorManager().startLoad();
        }

        // load individual sensors
        result = loadSensors(shared);

        // Notifies OpenLCB Sensor managers that the loading of XML is complete.
        for (OlcbConfigurationManager cfg : InstanceManager.getList(OlcbConfigurationManager
                .class)) {
            cfg.getSensorManager().finishLoad();
        }
        
        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbSensorManagerXml.class);
}
