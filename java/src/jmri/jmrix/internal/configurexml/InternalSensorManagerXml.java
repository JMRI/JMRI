package jmri.jmrix.internal.configurexml;

import jmri.jmrix.internal.InternalSensorManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring InternalSensorManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2006
 */
public class InternalSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public InternalSensorManagerXml() {
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
    public Element store(Object o) {
        Element sensors = new Element("sensors");

        String defaultState;
        switch (InternalSensorManager.getDefaultStateForNewSensors()) {
            case jmri.Sensor.ACTIVE:
                defaultState = "active";
                break;
            case jmri.Sensor.INACTIVE:
                defaultState = "inactive";
                break;
            case jmri.Sensor.INCONSISTENT:
                defaultState = "inconsistent";
                break;
            default:
                defaultState = "unknown";
        }

        sensors.addContent(new Element("defaultInitialState").addContent(defaultState));

        return store(o, sensors);

    }

    @Override
    public boolean load(Element sharedSensors, Element perNodeSensors) throws jmri.configurexml.JmriConfigureXmlException {
        // load individual sharedSensors
        if (sharedSensors.getChild("defaultInitialState") != null) {
            int defaultState = jmri.Sensor.UNKNOWN;
            String state = sharedSensors.getChild("defaultInitialState").getText();
            if (state.equals("active")) {
                defaultState = jmri.Sensor.ACTIVE;
            } else if (state.equals("inactive")) {
                defaultState = jmri.Sensor.INACTIVE;
            } else if (state.equals("inconsistent")) {
                defaultState = jmri.Sensor.INCONSISTENT;
            }
            InternalSensorManager.setDefaultStateForNewSensors(defaultState);
        }
        boolean load = loadSensors(sharedSensors);

        return load;
    }

    private final static Logger log = LoggerFactory.getLogger(InternalSensorManagerXml.class);
}
