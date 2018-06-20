package jmri.jmrix.loconet.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring LnReporterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public class LnReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public LnReporterManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element reporters) {
        reporters.setAttribute("class", "jmri.jmrix.loconet.configurexml.LnReporterManagerXml"); // NOI18N
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual Reporters
        return loadReporters(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(LnReporterManagerXml.class);

}
