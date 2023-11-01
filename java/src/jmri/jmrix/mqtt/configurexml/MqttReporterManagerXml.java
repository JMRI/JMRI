package jmri.jmrix.mqtt.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring MqttReporterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2023
 */
public class MqttReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public MqttReporterManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // load individual sensors
        return loadReporters(shared);
    }

//    private final static Logger log = LoggerFactory.getLogger(MqttReporterManagerXml.class);
}
