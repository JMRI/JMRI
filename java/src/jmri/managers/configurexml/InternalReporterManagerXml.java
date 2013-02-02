package jmri.managers.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring InternalReporterManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2010
 * @version $Revision$
 */
public class InternalReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public InternalReporterManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element reporters) {
        // load individual reporters
        return loadReporters(reporters);
    }

    static Logger log = Logger.getLogger(InternalReporterManagerXml.class.getName());
}
