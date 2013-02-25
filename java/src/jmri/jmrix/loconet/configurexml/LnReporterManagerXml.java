package jmri.jmrix.loconet.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring LnReporterManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision$
 */
public class LnReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public LnReporterManagerXml() {
        super();
    }

    public void setStoreElementClass(Element reporters) {
        reporters.setAttribute("class","jmri.jmrix.loconet.configurexml.LnReporterManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element reporters) {

        // load individual Reporters
        return loadReporters(reporters);
    }

    static Logger log = LoggerFactory.getLogger(LnReporterManagerXml.class.getName());

}
