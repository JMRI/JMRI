package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.PositionableLabel;
import java.awt.Color;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * Handle configuration for display.PositionableLabel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.10 $
 */
public class PositionableLabelXml implements XmlAdapter {

    public PositionableLabelXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * PositionableLabel
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        PositionableLabel p = (PositionableLabel)o;

        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("positionablelabel");
        element.addAttribute("class", "jmri.jmrit.display.configurexml.PositionableLabelXml");

        // include contents
        element.addAttribute("x", String.valueOf(p.getX()));
        element.addAttribute("y", String.valueOf(p.getY()));
        element.addAttribute("level", String.valueOf(p.getDisplayLevel()));
        if (p.isText() && p.getText()!=null) {
            element.addAttribute("text", p.getText());
            element.addAttribute("size", ""+p.getFont().getSize());
            element.addAttribute("style", ""+p.getFont().getStyle());
            if (!p.getForeground().equals(Color.black)) {
                element.addAttribute("red", ""+p.getForeground().getRed());
                element.addAttribute("green", ""+p.getForeground().getGreen());
                element.addAttribute("blue", ""+p.getForeground().getBlue());
            }
        }
        if (p.isIcon() && p.getIcon()!=null) {
            NamedIcon icon = (NamedIcon)p.getIcon();
            element.addAttribute("icon", icon.getName());
            element.addAttribute("rotate", String.valueOf(icon.getRotation()));
        }

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
        PositionableLabel l = null;
        if (element.getAttribute("text")!=null) {
            l = new PositionableLabel(element.getAttribute("text").getValue());
            Attribute a = element.getAttribute("size");
            try {
                if (a!=null) l.setFontSize(a.getFloatValue());
            } catch (DataConversionException ex) {
                log.warn("invalid size attribute value");
            }
            a = element.getAttribute("style");
            try {
                if (a!=null) l.setFontStyle(a.getIntValue(), 0);  // label is created plain, so don't need to drop
            } catch (DataConversionException ex) {
                log.warn("invalid style attribute value");
            }

        } else if (element.getAttribute("icon")!=null) {
            String name = element.getAttribute("icon").getValue();
            NamedIcon icon = CatalogPane.getIconByName(name);
            l = new PositionableLabel(icon);
            try {
                Attribute a = element.getAttribute("rotate");
                if (a!=null) {
                    int rotation = element.getAttribute("rotate").getIntValue();
                    icon.setRotation(rotation, l);
                }
            } catch (org.jdom.DataConversionException e) {}
        }
        // find coordinates
        int x = 0;
        int y = 0;
        int height = 10;
        int width = 10;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert PanelEditor's attribute");
        }
        // find display level
        int level = PanelEditor.LABELS.intValue();
        if (element.getAttribute("icon")!=null) level = PanelEditor.ICONS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);

        // set color if needed
        try {
            int red = element.getAttribute("red").getIntValue();
            int blue = element.getAttribute("blue").getIntValue();
            int green = element.getAttribute("green").getIntValue();
            l.setForeground(new Color(red, green, blue));
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }

        // and activate the result
        l.setLocation(x,y);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        p.putLabel(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditorXml.class.getName());

}