package apps.configurexml;

import apps.*;
import java.awt.*;
import jmri.*;
import jmri.configurexml.XmlAdapter;
import org.jdom.Element;

/**
 * Handle XML persistance of PerformFileModel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.1 $
 * @see apps.PerformFilePanel
 */
public class PerformFilePanelXml implements XmlAdapter {

    public PerformFilePanelXml() {
    }

    /**
     * Arrange for all the model objects to be stored
     * @param o Object to store, of type GuiLafConfigPane
     * @return null, after updating state so others are stored
     */
    public Element store(Object o) {
        PerformFilePanel p = (PerformFilePanel) o;
        Component[] l = p.getComponents();
        for (int i = 0; i<l.length; i++) {
            if ( (l[i]!= null) && (l[i].getClass().equals(PerformFilePanel.Item.class))) {
                PerformFilePanel.Item m = (PerformFilePanel.Item) l[i];
                InstanceManager.configureManagerInstance().registerPref(m.getModel());
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
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PerformFilePanelXml.class.getName());

}