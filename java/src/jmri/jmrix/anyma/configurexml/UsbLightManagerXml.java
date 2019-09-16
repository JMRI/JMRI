package jmri.jmrix.anyma.configurexml;

import javax.annotation.Nonnull;
import jmri.managers.configurexml.AbstractLightManagerConfigXML;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring AnymaDMX_LightManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author George Warner Copyright (c) 2017-2018
 * @since 4.9.6
 */
public class UsbLightManagerXml extends AbstractLightManagerConfigXML {

    /**
     * constructor
     */
    public UsbLightManagerXml() {
        super();
        log.debug("*	AnymaDMX_LightManagerXml constructor called");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStoreElementClass(@Nonnull Element lights) {
        log.debug("*	AnymaDMX_LightManagerXml.setStoreElementClass() called");
        lights.setAttribute("class", this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@Nonnull Element element, @Nonnull Object o) {
        log.error("Invalid AnymaDMX_LightManagerXml.load() method called");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean load(@Nonnull Element shared, @Nonnull Element perNode) {
        log.debug("*	AnymaDMX_LightManagerXml.load() called");
        // load individual lights
        return loadLights(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(UsbLightManagerXml.class);
}
