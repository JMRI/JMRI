package jmri.jmrix.internal.configurexml;

import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring
 * InternalTimeProviderManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2006
 */
public class InternalTimeProviderManagerXml extends jmri.managers.configurexml.AbstractTimeProviderManagerConfigXML {

    public InternalTimeProviderManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element timeProviders) {
        timeProviders.setAttribute("class", this.getClass().getName());
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual time providers
        loadTimeProviders(shared);
        return true;
    }

//    private final static Logger log = LoggerFactory.getLogger(InternalTimeProviderManagerXml.class);

}
