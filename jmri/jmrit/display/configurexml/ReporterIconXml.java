package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.ReporterIcon;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.ReporterIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004
 * @version $Revision: 1.1 $
 */
public class ReporterIconXml implements XmlAdapter {

    public ReporterIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * ReporterIcon
     * @param o Object to store, of type ReporterIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        ReporterIcon p = (ReporterIcon)o;

        Element element = new Element("reportericon");

        // include contents
        element.addAttribute("reporter", p.getReporter().getSystemName());
        element.addAttribute("x", ""+p.getX());
        element.addAttribute("y", ""+p.getY());

        element.addAttribute("class", "jmri.jmrit.display.configurexml.ReporterIconXml");

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
        ReporterIcon l = new ReporterIcon();

        l.setReporter(element.getAttribute("reporter").getValue());

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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ReporterIconXml.class.getName());
}