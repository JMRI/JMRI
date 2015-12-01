package apps.configurexml;

import apps.CreateButtonPanel;
import java.awt.Component;
import jmri.InstanceManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of CreateButtonPanel objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 * @see apps.CreateButtonPanel
 */
public class CreateButtonPanelXml extends jmri.configurexml.AbstractXmlAdapter {

    public CreateButtonPanelXml() {
    }

    /**
     * Arrange for all the model objects to be stored
     *
     * @param o Object to store, of type CreateButtonPanel
     * @return null, after updating state so others are stored
     */
    public Element store(Object o) {
        CreateButtonPanel p = (CreateButtonPanel) o;
        Component[] l = p.getComponents();
        for (int i = 0; i < l.length; i++) {
            if ((l[i] != null) && (l[i].getClass().equals(CreateButtonPanel.Item.class))) {
                CreateButtonPanel.Item m = (CreateButtonPanel.Item) l[i];
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
    static Logger log = LoggerFactory.getLogger(CreateButtonPanelXml.class.getName());

}
