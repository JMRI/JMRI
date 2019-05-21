package jmri.jmrix.roco.z21.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring Z21ReporterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @author Matthew Harris Copyright (C) 2011
 * @author Paul Bender Copyright (C) 2016
 * @since 4.5.4
 */
public class Z21ReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public Z21ReporterManagerXml() {
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

    private static final Logger log = LoggerFactory.getLogger(Z21ReporterManagerXml.class);
}
