package jmri.jmrix.openlcb.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.openlcb.OlcbConfigurationManager;

import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring OlcbStringIOManagers.
 *
 * @author Bob Jacobsen      Copyright: Copyright (c) 2003
 * @author Daniel Bergqvist  Copyright: Copyright (c) 2021
 */
public class OlcbStringIOManagerXml extends jmri.managers.configurexml.AbstractStringIOManagerConfigXML {

    public OlcbStringIOManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }


    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        boolean result;
        // We tell the StringIO manager(s) that we will be loading StringIOs from XML and they should
        // expect additional property set sequences. This is somewhat tricky in the face of
        // possibly multiple OpenLCB buses registered.
        for (OlcbConfigurationManager cfg : InstanceManager.getList(OlcbConfigurationManager
                .class)) {
            cfg.getStringIOManager().startLoad();
        }

        // load individual sensors
        result = loadStringIOs(shared);

        // Notifies OpenLCB Sensor managers that the loading of XML is complete.
        for (OlcbConfigurationManager cfg : InstanceManager.getList(OlcbConfigurationManager
                .class)) {
            cfg.getStringIOManager().finishLoad();
        }
        
        return result;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbStringIOManagerXml.class);
}
