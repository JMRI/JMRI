package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.RfidIcon;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.RfidIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004
 * @version $Revision: 1.1 $
 */
public class RfidIconXml implements XmlAdapter {

    public RfidIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * RfidIcon
     * @param o Object to store, of type RfidIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        RfidIcon p = (RfidIcon)o;

        Element element = new Element("rfidicon");

        // include contents
        element.addAttribute("x", ""+p.getX());
        element.addAttribute("y", ""+p.getY());

        element.addAttribute("class", "jmri.jmrit.display.configurexml.RfidIconXml");

        return element;
    }


    public void load(Element element) {
        log.error("Invalid method called");
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  PanelEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        PanelEditor p = (PanelEditor)o;
        String name;
        RfidIcon l = new RfidIcon();

        // find coordinates
        int x = 0;
        int y = 0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setLocation(x,y);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(p.LABELS);
        p.putLabel(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RfidIconXml.class.getName());
}