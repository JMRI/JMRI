package jmri.jmrix.mqtt.configurexml;

import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring MqttTurnoutManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Lionel Jeanson Copyright: Copyright (c) 2017
 */
public class MqttLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public MqttLightManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.mqtt.configurexml.MqttLightManagerXml");
    }

    @Override
    public boolean load(Element shared) {
        // load individual turnouts
        return loadLights(shared);
    }
}
