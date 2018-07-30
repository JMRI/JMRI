package jmri.jmrix.rfid.merg.concentrator.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring ConcentratorReporterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class ConcentratorReporterManagerXml extends jmri.jmrix.rfid.configurexml.RfidReporterManagerXml {

    public ConcentratorReporterManagerXml() {
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
    public boolean load(Element shared, Element perNode) {
        // load individual sensors
        return loadReporters(shared);
    }

    private static final Logger log = LoggerFactory.getLogger(ConcentratorReporterManagerXml.class);
}
