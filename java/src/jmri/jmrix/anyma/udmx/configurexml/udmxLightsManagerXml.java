package jmri.jmrix.anyma.udmx.configurexml;

import jmri.managers.configurexml.AbstractLightManagerConfigXML;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring udmxLightManagers.
 *
 * @author George Warner Copyright (c) 2017
 */
public class udmxLightsManagerXml extends AbstractLightManagerConfigXML {

    public udmxLightsManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.lenz.configurexml.XNetTurnoutManagerXml");
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element perNode) {
        // load individual lights
        return loadLights(perNode);
    }

    private final static Logger log = LoggerFactory.getLogger(udmxLightsManagerXml.class);

}
