package jmri.jmrit.display.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.AnalogClock2Display;
import jmri.jmrit.display.AnalogClock2Display.Colors;
import jmri.jmrit.display.Editor;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.AnalogClock2Display objects.
 *
 * @author Howard G. Penny Copyright (c) 2005
 */
public class AnalogClock2DisplayXml
        extends AbstractXmlAdapter {

    public AnalogClock2DisplayXml() {
    }

    /**
     * Default implementation for storing the contents of an AnalogClock2Display
     *
     * @param o Object to store, of type TurnoutIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        AnalogClock2Display p = (AnalogClock2Display) o;
        if (!p.isActive()) {
            return null; // if flagged as inactive, don't store
        }

        Element element = new Element("fastclock");

        // include contents
        element.setAttribute("x", "" + p.getX());
        element.setAttribute("y", "" + p.getY());
        element.setAttribute("scale", "" + p.getScale());
        element.setAttribute("color", "" + p.getColor().name());
        String link = p.getUrl();
        if (link != null && link.trim().length() > 0) {
            element.setAttribute("link", link);
        }

        element.setAttribute("class",
                "jmri.jmrit.display.configurexml.AnalogClock2DisplayXml");

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create an AnalogClock2Display, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // get object class and create the clock object
        Editor ed = (Editor) o;
        AnalogClock2Display l = new AnalogClock2Display(ed);

        // find coordinates
        int x = 0;
        int y = 0;
        double scale = 1.0;
        Colors color = Colors.Black;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
            if (element.getAttribute("scale") != null) {
                scale = element.getAttribute("scale").getDoubleValue();
            }
            if (element.getAttribute("color") != null) {
                color = Colors.valueOf(element.getAttribute("color").getValue());
            }
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        if (element.getAttribute("link") != null) {
            l.setUrl(element.getAttribute("link").getValue());
        }
        l.setOpaque(false);
        l.update();
        l.setLocation(x, y);
        if (scale != 1.0 && 10.0 > scale && scale > 0.1) {
            l.setScale(scale);
        }
        l.setColor(color);

        // add the clock to the panel
        l.setDisplayLevel(Editor.CLOCK);
        ed.putItem(l);
    }

    private static final Logger log = LoggerFactory.getLogger(AnalogClock2DisplayXml.class.getName());
}
