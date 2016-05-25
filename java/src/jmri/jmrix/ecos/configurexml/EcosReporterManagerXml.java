package jmri.jmrix.ecos.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring EcosReporterManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2012
 * @version $Revision: 17977 $
 */
public class EcosReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public EcosReporterManagerXml() {
        super();
    }

    public void setStoreElementClass(Element reporters) {
        reporters.setAttribute("class", "jmri.jmrix.ecos.configurexml.EcosReporterManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual Reporters
        return loadReporters(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(EcosReporterManagerXml.class.getName());

}
