package jmri.jmrix.anyma_dmx.usb.configurexml;

import jmri.managers.configurexml.AbstractLightManagerConfigXML;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring AnymaDMX_LightManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class UsbLightManagerXml extends AbstractLightManagerConfigXML {

    public UsbLightManagerXml() {
        super();
        log.info("*	AnymaDMX_LightManagerXml constructor called");
    }

    @Override
    public void setStoreElementClass(Element lights) {
        log.info("*	AnymaDMX_LightManagerXml.setStoreElementClass() called");
        lights.setAttribute("class", this.getClass().getName());
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid AnymaDMX_LightManagerXml.load() method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.info("*	AnymaDMX_LightManagerXml.load() called");
        // load individual lights
        return loadLights(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(UsbLightManagerXml.class);
}
