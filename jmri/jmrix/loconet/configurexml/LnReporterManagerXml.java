package jmri.jmrix.loconet.configurexml;

import org.jdom.Element;
import jmri.InstanceManager;
import jmri.jmrix.loconet.LnReporterManager;

/**
 * Provides load and store functionality for
 * configuring LnReporterManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.1 $
 */
public class LnReporterManagerXml extends jmri.configurexml.AbstractReporterManagerConfigXML {

    public LnReporterManagerXml() {
        super();
    }

    public void setStoreElementClass(Element reporters) {
        reporters.addAttribute("class","jmri.jmrix.loconet.configurexml.LnReporterManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element reporters) {
        // create and/or access the master object
        LnReporterManager mgr = LnReporterManager.instance();

        // load individual Reporters
        loadReporters(reporters);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnReporterManagerXml.class.getName());

}