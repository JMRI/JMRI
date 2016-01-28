package jmri.jmrix.rfid.merge.concentrator.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring ConcentratorReporterManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @author Matthew Harris Copyright (C) 2011
 * @version $Revision$
 * @since 2.11.4
 */
public class ConcentratorReporterManagerXml extends jmri.jmrix.rfid.configurexml.RfidReporterManagerXml {

    public ConcentratorReporterManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual sensors
        return loadReporters(shared);
    }

    private static final Logger log = LoggerFactory.getLogger(ConcentratorReporterManagerXml.class.getName());
}
