package jmri.jmrix.dccpp.configurexml;

import java.util.List;

import jmri.HasLightMode;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.jmrix.dccpp.DCCppLight;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring DCCppLightManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Dave Duchamp Copyright (c) 2006
 * @author Mark Underwood Copyright (c) 2006
 * @author Chad Francis Copyright (c) 2026
 *
 * Based on XNetLightManagerXml by Dave Duchamp
 */
public class DCCppLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public DCCppLightManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.dccpp.configurexml.DCCppLightManagerXml");
    }

    /**
     * {@inheritDoc}
     * Adds a {@code mode} attribute for any DCC-EX light that is not in the default mode.
     */
    @Override
    public Element store(Object o) {
        Element lights = super.store(o);
        if (lights == null) {
            return null;
        }
        LightManager lm = (LightManager) o;
        for (Element el : lights.getChildren("light")) {
            String sysName = getSystemName(el);
            if (sysName == null) continue;
            Light light = lm.getBySystemName(sysName);
            if (light instanceof HasLightMode && ((HasLightMode) light).getMode() != DCCppLight.STANDARD) {
                el.setAttribute("mode", ((HasLightMode) light).getModeName());
            }
        }
        return lights;
    }

    @Override
    public boolean load(Element lights) {
        return loadLights(lights);
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        return loadLights(shared);
    }

    /**
     * {@inheritDoc}
     * Restores the {@code mode} attribute on DCC-EX lights after the base class creates them.
     */
    @Override
    public boolean loadLights(Element lights) {
        boolean result = super.loadLights(lights);
        LightManager lm = InstanceManager.lightManagerInstance();
        List<Element> lightList = lights.getChildren("light");
        for (Element el : lightList) {
            Attribute modeAttr = el.getAttribute("mode");
            if (modeAttr == null) continue;
            String sysName = getSystemName(el);
            if (sysName == null) continue;
            Light light = lm.getBySystemName(sysName);
            if (light instanceof HasLightMode) {
                ((HasLightMode) light).setModeByName(modeAttr.getValue());
            } else {
                log.warn("Light {} does not support modes; ignoring mode attribute '{}'", sysName, modeAttr.getValue());
            }
        }
        return result;
    }

    private static final Logger log = LoggerFactory.getLogger(DCCppLightManagerXml.class);

}
