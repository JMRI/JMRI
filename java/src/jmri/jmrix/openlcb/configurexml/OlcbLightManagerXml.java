package jmri.jmrix.openlcb.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.openlcb.OlcbConfigurationManager;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring OlcbLightManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Jeff Collell
 */
public class OlcbLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public OlcbLightManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", this.getClass().getName());
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        boolean result = true;
        // We tell the Light managers that we will be loading Lights from XML and they should
        // expect additional property set sequences. This is somewhat tricky in the face of
        // possibly multiple OpenLCB buses registered.
        for (OlcbConfigurationManager cfg : InstanceManager.getList(OlcbConfigurationManager
                .class)) {
            cfg.getLightManager().startLoad();
        }

        // load individual lights
        result = loadLights(shared);

        // Notifies OpenLCB Light managers that the loading of XML is complete.
        for (OlcbConfigurationManager cfg : InstanceManager.getList(OlcbConfigurationManager
                .class)) {
            cfg.getLightManager().finishLoad();
        }
        
        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbLightManagerXml.class);
}
