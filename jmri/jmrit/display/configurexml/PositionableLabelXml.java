package jmri.jmrit.display.configurexml;

import org.jdom.Element;

import jmri.InstanceManager;
import jmri.jmrit.display.PositionableLabel;
import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;

import javax.swing.*;

/**
 * Handle configuration for display.PositionableLabel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.3 $
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
        Element element = new Element("positionablelabel");
        element.addAttribute("class", "jmri.jmrit.display.configurexml.PositionableLabelXml");

        // include contents
        PositionableLabel p = (PositionableLabel)o;
        element.addAttribute("x", ""+p.getX());
        element.addAttribute("y", ""+p.getY());
        element.addAttribute("height", ""+p.getHeight());
        element.addAttribute("width", ""+p.getWidth());
        if (p.isText() && p.getText()!=null) element.addAttribute("text", p.getText());
        if (p.isIcon() && p.getIcon()!=null) element.addAttribute("icon", ((NamedIcon)p.getIcon()).getName());

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
        } else if (element.getAttribute("icon")!=null) {
            String name = element.getAttribute("icon").getValue();
            l = new PositionableLabel(p.catalog.getIconByName(name));
        }
        // find coordinates
        int x = 0;
        int y = 0;
        int height = 10;
        int width = 10;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
            //height = element.getAttribute("height").getIntValue();
            //width = element.getAttribute("width").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert PanelEditor's attribute");
        }
        l.setLocation(x,y);
        //l.setSize(width, height);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        if (element.getAttribute("text")!=null) {
            p.putLabel(l);
        } else if (element.getAttribute("icon")!=null) {
            p.putIcon(l);
        }

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditorXml.class.getName());

}