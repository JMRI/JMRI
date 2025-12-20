package jmri.jmrix.openlcb.configurexml;

import org.jdom2.Element;

import jmri.Meter;
import jmri.MeterManager;
import jmri.jmrix.openlcb.OlcbMeterManager;
/**
 * Provides load and store functionality for configuring
 * OlcbMeterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen      Copyright (C) 2006
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class OlcbMeterManagerXml extends jmri.managers.configurexml.AbstractMeterManagerXml {

    public OlcbMeterManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element meters) {
        meters.setAttribute("class", this.getClass().getName());
    }

    // store the meter element.
    // This adds the actual meter subclass as an element
    @Override
    protected Element storeMeter(Meter m) {
        String mName = m.getSystemName();
        
        Element elem = new Element("meter");
        elem.addContent(new Element("systemName").addContent(mName));

        // store the type of the Meter itself
        elem.setAttribute("class", m.getClass().getName());
        
        // store common part
        storeCommon(m, elem);
        
        return elem;
    }

    @Override
    protected void loadMeter(String sysName, String userName, Element el, MeterManager mm) {
        log.debug("get Meter: ({})({})", sysName, (userName == null ? "<null>" : userName));
        Meter m = mm.getBySystemName(sysName);
        
        if (m == null) {
            // have to create the specific meter object
            if (el.getAttributeValue("class").contains("Voltage")) {
                m = OlcbMeterManager.createVoltageMeter(sysName);
            } else if (el.getAttributeValue("class").contains("Current")) {
                m = OlcbMeterManager.createCurrentMeter(sysName);                
            } else {
                log.error("Unexpected class when restoring OpenLCB meter: {}", 
                        el.getChild("class").getContent());
                return;
            }
        }

        m.setUserName(userName);
        // load common parts
        loadCommon(m, el);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbMeterManagerXml.class);

}
