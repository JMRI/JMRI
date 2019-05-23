package jmri.jmrix.jmriclient.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring
 * JMRIClientReporterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @author Paul Bender Copyright: Copyright (c) 2015 
 */
public class JMRIClientReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public JMRIClientReporterManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element reporters) {
        reporters.setAttribute("class", "jmri.jmrix.jmriclient.configurexml.JMRIClientReporterManagerXml");
    }

    @Override
    public void load(Element element, Object o) throws jmri.configurexml.JmriConfigureXmlException {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual reporters 
        return loadReporters(shared);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(JMRIClientReporterManagerXml.class);
}
