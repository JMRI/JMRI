package jmri.jmrix.ecos.configurexml;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring EcosReporterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2012
 */
@API(status = EXPERIMENTAL)
public class EcosReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public EcosReporterManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element reporters) {
        reporters.setAttribute("class", "jmri.jmrix.ecos.configurexml.EcosReporterManagerXml");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual Reporters
        return loadReporters(shared);
    }

//    private final static Logger log = LoggerFactory.getLogger(EcosReporterManagerXml.class);

}
