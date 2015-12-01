package apps.configurexml;

import apps.PerformFilePanel;
import java.awt.Component;
import jmri.InstanceManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of PerformFileModel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 * @see apps.PerformFilePanel
 */
public class PerformFilePanelXml extends jmri.configurexml.AbstractXmlAdapter {

    public PerformFilePanelXml() {
    }

    /**
     * Arrange for all the model objects to be stored
     *
     * @param o Object to store, of type PerformFilePanel
     * @return null, after updating state so others are stored
     */
    public Element store(Object o) {
        PerformFilePanel p = (PerformFilePanel) o;
        Component[] l = p.getComponents();
        for (int i = 0; i < l.length; i++) {
            if ((l[i] != null) && (l[i].getClass().equals(PerformFilePanel.Item.class))) {
                PerformFilePanel.Item m = (PerformFilePanel.Item) l[i];
                InstanceManager.configureManagerInstance().registerPref(m.getModel());
            }
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
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    static Logger log = LoggerFactory.getLogger(PerformFilePanelXml.class.getName());

}
