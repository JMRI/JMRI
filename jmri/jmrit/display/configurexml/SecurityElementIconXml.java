package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.SecurityElementIcon;
import jmri.jmrix.loconet.SecurityElement;
import org.jdom.Element;

/**
 * Handle configuration for display.SecurityElementIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.2 $
 */
public class SecurityElementIconXml implements XmlAdapter {

    public SecurityElementIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * SecurityElementIcon
     * @param o Object to store, of type SecurityElementIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element element = new Element("securityelementicon");

        // include contents
        SecurityElementIcon p = (SecurityElementIcon)o;
        SecurityElement s = p.getSecurityElement();
        element.addAttribute("number", ""+s.getNumber());
        if (!p.getRightBoundAX())
            element.addAttribute("AX", "leftbound");

        element.addAttribute("x", ""+p.getX());
        element.addAttribute("y", ""+p.getY());
        element.addAttribute("class", "jmri.jmrit.display.configurexml.SecurityElementIconXml");
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

        SecurityElementIcon l = new SecurityElementIcon();

        l.setSecurityElement(element.getAttribute("number").getValue());

        if (element.getAttribute("AX")!=null) {
            if (element.getAttribute("AX").getValue().equals("leftbound"))
                l.setRightBoundAX(false);
        }

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

        // no putSecurityElement exists, so code is here
        p.target.add(l, PanelEditor.SECURITY);
        p.contents.add(l);
        p.target.revalidate();

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutIconXml.class.getName());

}