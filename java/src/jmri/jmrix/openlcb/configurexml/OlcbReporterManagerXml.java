package jmri.jmrix.openlcb.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.openlcb.OlcbConfigurationManager;
import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring OlcbReporterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008, 2010
 * @author Balazs Racz Copyright: Copyright (c) 2023
 * @since 5.3.5
 */
public class OlcbReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public OlcbReporterManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", this.getClass().getName());
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // We tell the Reporter managers that we will be loading reporters from XML and they should
        // expect additional property set sequences. This is somewhat tricky in the face of
        // possibly multiple OpenLCB buses registered.
        for (OlcbConfigurationManager cfg : InstanceManager.getList(OlcbConfigurationManager
                .class)) {
            cfg.getReporterManager().startLoad();
        }

        // load individual turnouts
        boolean ret = loadReporters(shared);

        // Notifies OpenLCB turnout managers that the loading of XML is complete.
        for (OlcbConfigurationManager cfg : InstanceManager.getList(OlcbConfigurationManager
                .class)) {
            cfg.getReporterManager().finishLoad();
        }
        return ret;
    }

//    private final static Logger log = LoggerFactory.getLogger(OlcbReporterManagerXml.class);
}
