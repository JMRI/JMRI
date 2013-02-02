package jmri.jmrix.dcc4pc.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring Dcc4PcReporterManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Kevin Dickerson Copyright: (c) 2012
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @version $Revision: 17977 $
 */
public class Dcc4PcReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public Dcc4PcReporterManagerXml() {
        super();
    }

    public void setStoreElementClass(Element reporter) {
        reporter.setAttribute("class","jmri.jmrix.dcc4pc.configurexml.Dcc4PcReporterManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element reporter){
        // create the master object
        //Dcc4PcReporterManager.instance();
        // load individual sensors
        return loadReporters(reporter);
    }

    static Logger log = Logger.getLogger(Dcc4PcReporterManagerXml.class.getName());
}
