package jmri.jmrix.internal.configurexml;

import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring
 * InternalMeterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen      Copyright (C) 2006
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class InternalMeterManagerXml extends jmri.managers.configurexml.AbstractMeterManagerXml {

    public InternalMeterManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element meters) {
        meters.setAttribute("class", this.getClass().getName());
    }

//    private final static Logger log = LoggerFactory.getLogger(InternalMeterManagerXml.class);

}
