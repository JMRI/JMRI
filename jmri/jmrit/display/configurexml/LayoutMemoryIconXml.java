package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.LayoutMemoryIcon;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import java.util.List;
import java.awt.Color;

/**
 * This module handles configuration for display.LayoutMemoryIcon objects for a LayoutEditor.
 *   This routine is almost identical to MemoryIconXml.java, written by Bob Jacobsen.  
 *   Differences are related to the hard interdependence between MemoryIconXml.java and 
 *   PanelEditor.java, which made it impossible to use MemoryIconXml.java directly with 
 *   LayoutEditor. Rectifying these differences is especially important when storing and
 *   loading a saved panel.
 *
 * @author David Duchamp Copyright (c) 2007
 * @version $Revision: 1.2 $
 */
public class LayoutMemoryIconXml implements XmlAdapter {

    public LayoutMemoryIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LayoutMemoryIcon
     * @param o Object to store, of type LayoutMemoryIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        LayoutMemoryIcon p = (LayoutMemoryIcon)o;

        Element element = new Element("memoryicon");

        // include attributes
        element.setAttribute("memory", p.getMemory().getSystemName());
        element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        if (p.isText() && p.getText()!=null) {
            element.setAttribute("size", ""+p.getFont().getSize());
            element.setAttribute("style", ""+p.getFont().getStyle());
            if (!p.getForeground().equals(Color.black)) {
                element.setAttribute("red", ""+p.getForeground().getRed());
                element.setAttribute("green", ""+p.getForeground().getGreen());
                element.setAttribute("blue", ""+p.getForeground().getBlue());
            }
        }
        element.setAttribute("selectable", (p.isSelectable()?"yes":"no"));

        element.setAttribute("class", "jmri.jmrit.display.configurexml.LayoutMemoryIconXml");
        if (p.getDefaultIcon()!=null)
            element.setAttribute("defaulticon", p.getDefaultIcon().getName());

		// include contents
		java.util.HashMap map = p.getMap();
		if (map!=null) {
		    java.util.Iterator iterator = map.keySet().iterator();
    	    while (iterator.hasNext()) {
    		    String key = iterator.next().toString();
    		    String value = ((NamedIcon)map.get(key)).getName();
    		    Element e2 = new Element("memorystate");
    		    e2.setAttribute("value", key);
    		    e2.setAttribute("icon", value);
    		    element.addContent(e2);
    	    }
        }
        return element;
    }


    public void load(Element element) {
        log.error("Invalid method called");
    }

    /**
     * Load, starting with the memoryicon element, then
     * all the value-icon pairs
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor)o;
        String name;
        LayoutMemoryIcon l = new LayoutMemoryIcon();
		p.memoryLabelList.add((Object)l);

        l.setMemory(element.getAttribute("memory").getValue());

        Attribute a = element.getAttribute("defaulticon");
        if (a!=null) l.setDefaultIcon(CatalogPane.getIconByName(a.getValue()));
        
        a = element.getAttribute("selectable");
        if (a!=null && a.getValue().equals("yes")) l.setSelectable(true);
        else l.setSelectable(false);
        
        a = element.getAttribute("size");
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
        
        // get the icon pairs
        List items = element.getChildren();
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = (Element)items.get(i);
            String icon = item.getAttribute("icon").getValue();
            String keyValue = item.getAttribute("value").getValue();
        	l.addKeyAndIcon(CatalogPane.getIconByName(icon), keyValue);
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
 
         // find display level
        int level = LayoutEditor.LABELS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);

        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        p.putLabel(l);
    
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
        
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutMemoryIconXml.class.getName());
}