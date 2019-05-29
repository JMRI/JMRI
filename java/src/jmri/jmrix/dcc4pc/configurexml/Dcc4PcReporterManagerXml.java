package jmri.jmrix.dcc4pc.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring Dcc4PcReporterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Kevin Dickerson Copyright: (c) 2012
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 */
public class Dcc4PcReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public Dcc4PcReporterManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element reporter) {
        reporter.setAttribute("class", "jmri.jmrix.dcc4pc.configurexml.Dcc4PcReporterManagerXml");
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // create the master object
        //Dcc4PcReporterManager.instance();
        // load individual sensors
        return loadReporters(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(Dcc4PcReporterManagerXml.class);
}
