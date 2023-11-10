package jmri.jmrix.bidib.configurexml;

import jmri.InstanceManager;

import jmri.jmrix.bidib.BiDiBLightManager;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring BiDiBLightManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Dave Duchamp Copyright (c) 2006
 * @author Eckart Meyer Copyright (C) 2019
 */
public class BiDiBLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public BiDiBLightManagerXml() {
        super();
    }
    
    @Override
    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.bidib.configurexml.BiDiBLightManagerXml");
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.debug("load {} {}", shared, perNode);
        // We tell the sensor managers that we will be loading sensors from XML and they should
        // expect additional property set sequences. This is somewhat tricky in the face of
        // possibly multiple connections registered.
        for (BiDiBSystemConnectionMemo memo : InstanceManager.getList(BiDiBSystemConnectionMemo.class)) {
            if (!memo.getDisabled()) {
                ((BiDiBLightManager)memo.getLightManager()).startLoad();
            }
        }
        // load individual lights
        boolean result = loadLights(shared);
        
        // Notifies sensor managers that the loading of XML is complete.
        for (BiDiBSystemConnectionMemo memo : InstanceManager.getList(BiDiBSystemConnectionMemo.class)) {
            if (!memo.getDisabled()) {
                ((BiDiBLightManager)memo.getLightManager()).finishLoad();
            }
        }

        return result;
    }
    
    /* 
     * Find the system connection memo object when we only have the XML element <LIGHTS>...</LIGHTS>.
     * This depends on the fact that there is a separate <LIGHTS> element for each connection.
     * We scan the <LIGHT> elements and check if it is an instance of a BiDiBLight. The BiDiBLight object gives us the
     * system connection memo object. Use the first found Light object and return.
     * 
     * @param lights XML element containing the lights to be configured
     * @return the BiDiBSystemConnectionMemo object
     */
/* UNUSED
    private BiDiBSystemConnectionMemo findSystemConnectionMemo(Element lights) {
        for (BiDiBSystemConnectionMemo memo : InstanceManager.getList(BiDiBSystemConnectionMemo.class)) {
            log.debug("*** found memo: {}", memo.getUserName());
        }

        List<Element> lightList = lights.getChildren("light");
        ProxyLightManager mgr = (ProxyLightManager)InstanceManager.getDefault(jmri.LightManager.class);
        for(Element e : lightList) {
            String sysName = getSystemName(e);
            if (sysName != null  &&  !sysName.isEmpty()) {
                Light lgt = mgr.getBySystemName(sysName);
                if (lgt != null  &&  lgt instanceof BiDiBLight) {
                    return ((BiDiBLight)lgt).getMemo();
                }
            }
        }
        return null;
    }
*/

    private final static Logger log = LoggerFactory.getLogger(BiDiBLightManagerXml.class);

}
