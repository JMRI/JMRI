package apps.configurexml;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.managers.ManagerDefaultSelector;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistence of ManagerDefaultsConfigPane objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2010
 */
public class ManagerDefaultsConfigPaneXml extends jmri.configurexml.AbstractXmlAdapter {

    public ManagerDefaultsConfigPaneXml() {
    }

    /**
     * Arrange for ManagerDefaultSelector to be stored
     *
     * @param o Object to store, ignored
     * @return null after others arranged
     */
    @Override
    public Element store(Object o) {
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerPref(InstanceManager.getDefault(ManagerDefaultSelector.class));
        }
        return null;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("load(Element) should not have been invoked");
        return false;
    }

    /**
     * Update static data from XML file
     *
     * @param element Top level Element to unpack.
     * @param o       ignored
     */
    @Override
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ManagerDefaultsConfigPaneXml.class);

}
