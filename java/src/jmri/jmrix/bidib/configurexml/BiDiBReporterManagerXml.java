package jmri.jmrix.bidib.configurexml;

import jmri.InstanceManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.bidib.BiDiBReporterManager;
import jmri.managers.ProxyReporterManager;

/**
 * Provides load and store functionality for configuring BiDiBReporterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @author Eckart Meyer Copyright (C) 2019
 * @since 4.5.4
 */
public class BiDiBReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public BiDiBReporterManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual reporters
        boolean result = loadReporters(shared);

        if (result) {
            ProxyReporterManager rm = (ProxyReporterManager)InstanceManager.getDefault(jmri.ReporterManager.class);
            //log.debug("Reporter Manager List: {}", rm.getManagerList());
            BiDiBReporterManager mgr = (BiDiBReporterManager)rm.getManagerList().get(0);
            if (mgr != null ) {
                mgr.updateAll();
            }
        }
        
        return result;
    }

    private static final Logger log = LoggerFactory.getLogger(BiDiBReporterManagerXml.class);
}
