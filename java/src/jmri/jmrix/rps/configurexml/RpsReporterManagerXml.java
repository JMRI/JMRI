package jmri.jmrix.rps.configurexml;

import jmri.jmrix.rps.RpsReporterManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring RpsReporterManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @since 2.3.1
 */
public class RpsReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public RpsReporterManagerXml() {
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
        // create the master object
        RpsReporterManager.instance();
        // load individual sensors
        return loadReporters(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(RpsReporterManagerXml.class);
}
