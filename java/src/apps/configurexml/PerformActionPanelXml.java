package apps.configurexml;

import apps.PerformActionPanel;
import java.awt.Component;
import jmri.InstanceManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of PerformActionPanel objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 * @see apps.PerformActionPanel
 */
public class PerformActionPanelXml extends jmri.configurexml.AbstractXmlAdapter {

    public PerformActionPanelXml() {
    }

    /**
     * Arrange for all the model objects to be stored
     *
     * @param o Object to store, of type PerformActionPanel
     * @return null, after updating state so others are stored
     */
    public Element store(Object o) {
        PerformActionPanel p = (PerformActionPanel) o;
        Component[] l = p.getComponents();
        for (int i = 0; i < l.length; i++) {
            if ((l[i] != null) && (l[i].getClass().equals(PerformActionPanel.Item.class))) {
                PerformActionPanel.Item m = (PerformActionPanel.Item) l[i];
                InstanceManager.configureManagerInstance().registerPref(m.updatedModel());
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
    static Logger log = LoggerFactory.getLogger(PerformActionPanelXml.class.getName());

}
