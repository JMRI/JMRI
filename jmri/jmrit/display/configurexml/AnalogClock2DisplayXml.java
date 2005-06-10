// AnalogClock2DisplayXml.java

package jmri.jmrit.display.configurexml;

import org.jdom.*;
import jmri.configurexml.*;
import jmri.jmrit.display.*;

/**
 * Handle configuration for display.AnalogClock2Display objects.
 *
 * @author  Howard G. Penny  Copyright (c) 2005
 * @version $Revision: 1.1 $
 */
public class AnalogClock2DisplayXml
    implements XmlAdapter {

    public AnalogClock2DisplayXml() {
    }

    /**
     * Default implementation for storing the contents of an
     * AnalogClock2Display
     * @param o Object to store, of type TurnoutIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        AnalogClock2Display p = (AnalogClock2Display) o;
        if (!p.isActive()) {
            return null; // if flagged as inactive, don't store
        }

        Element element = new Element("fastclock");

        // include contents
        element.addAttribute("x", "" + p.getX());
        element.addAttribute("y", "" + p.getY());

        element.addAttribute("class",
            "jmri.jmrit.display.configurexml.AnalogClock2DisplayXml");

        return element;
    }

    public void load(Element element) {
        log.error("Invalid method called");
    }

    /**
     * Create an AnalogClock2Display, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  PanelEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        PanelEditor p = (PanelEditor) o;

        AnalogClock2Display l = new AnalogClock2Display(p);

        // find coordinates
        int x = 0;
        int y = 0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        }
        catch (org.jdom.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        int level = PanelEditor.CLOCK.intValue();
        l.setOpaque(false);
        l.update();
        l.setDisplayLevel(level);
        l.setLocation(x, y);

        p.putClock(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.
        getInstance(AnalogClock2DisplayXml.class.getName());
}
