package apps.configurexml;

import apps.PerformActionPanel;

import jmri.InstanceManager;
import jmri.configurexml.XmlAdapter;
import java.awt.Component;

import org.jdom.Element;

/**
 * Handle XML persistance of PerformActionModel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.2 $
 * @see apps.PerformActionPanel
 */
public class PerformActionPanelXml implements XmlAdapter {

    public PerformActionPanelXml() {
    }

    /**
     * Arrange for all the model objects to be stored
     * @param o Object to store, of type GuiLafConfigPane
     * @return null, after updating state so others are stored
     */
    public Element store(Object o) {
        PerformActionPanel p = (PerformActionPanel) o;
        Component[] l = p.getComponents();
        for (int i = 0; i<l.length; i++) {
            if ( (l[i]!= null) && (l[i].getClass().equals(PerformActionPanel.Item.class))) {
                PerformActionPanel.Item m = (PerformActionPanel.Item) l[i];
                InstanceManager.configureManagerInstance().registerPref(m.updatedModel());
            }
        }
        return null;
    }

    /**
     * Create object from XML file
     * @param element Top level Element to unpack.
      */
    public void load(Element e) {
        log.error("load(Element) should not have been invoked");
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PerformActionPanelXml.class.getName());

}