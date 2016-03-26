package jmri.jmrit.display.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SecurityElementIcon;
import jmri.jmrix.loconet.SecurityElement;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.SecurityElementIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision$
 */
public class SecurityElementIconXml extends AbstractXmlAdapter {

    public SecurityElementIconXml() {
    }

    /**
     * Default implementation for storing the contents of a SecurityElementIcon
     *
     * @param o Object to store, of type SecurityElementIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        SecurityElementIcon p = (SecurityElementIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("securityelementicon");

        // include contents
        SecurityElement s = p.getSecurityElement();
        element.setAttribute("number", "" + s.getNumber());
        if (!p.getRightBoundAX()) {
            element.setAttribute("AX", "leftbound");
        }

        element.setAttribute("x", "" + p.getX());
        element.setAttribute("y", "" + p.getY());
        element.setAttribute("class", "jmri.jmrit.display.configurexml.SecurityElementIconXml");
        return element;
    }

    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    public void load(Element element, Object o) {
        Editor ed = (Editor) o;
        SecurityElementIcon l = new SecurityElementIcon(ed);

        l.setSecurityElement(element.getAttribute("number").getValue());

        if (element.getAttribute("AX") != null) {
            if (element.getAttribute("AX").getValue().equals("leftbound")) {
                l.setRightBoundAX(false);
            }
        }

        // find coordinates
        int x = 0;
        int y = 0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setLocation(x, y);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(Editor.SECURITY);
        ed.putItem(l);
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutIconXml.class.getName());

}
