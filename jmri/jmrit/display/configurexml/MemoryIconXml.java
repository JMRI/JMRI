package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.MemoryIcon;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import com.sun.java.util.collections.List;
import java.awt.Color;

/**
 * Handle configuration for display.MemoryIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004
 * @version $Revision: 1.8 $
 */
public class MemoryIconXml implements XmlAdapter {

    public MemoryIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * MemoryIcon
     * @param o Object to store, of type MemoryIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        MemoryIcon p = (MemoryIcon)o;

        Element element = new Element("memoryicon");

        // include attributes
        element.addAttribute("memory", p.getMemory().getSystemName());
        element.addAttribute("x", ""+p.getX());
        element.addAttribute("y", ""+p.getY());
        element.addAttribute("level", String.valueOf(p.getDisplayLevel()));
        if (p.isText() && p.getText()!=null) {
            element.addAttribute("size", ""+p.getFont().getSize());
            element.addAttribute("style", ""+p.getFont().getStyle());
            if (!p.getForeground().equals(Color.black)) {
                element.addAttribute("red", ""+p.getForeground().getRed());
                element.addAttribute("green", ""+p.getForeground().getGreen());
                element.addAttribute("blue", ""+p.getForeground().getBlue());
            }
        }
        element.addAttribute("selectable", (p.isSelectable()?"yes":"no"));

        element.addAttribute("class", "jmri.jmrit.display.configurexml.MemoryIconXml");
        if (p.getDefaultIcon()!=null)
            element.addAttribute("defaulticon", p.getDefaultIcon().getName());

		// include contents
		com.sun.java.util.collections.HashMap map = p.getMap();
		if (map!=null) {
		    com.sun.java.util.collections.Iterator iterator = map.keySet().iterator();
    	    while (iterator.hasNext()) {
    		    String key = iterator.next().toString();
    		    String value = ((NamedIcon)map.get(key)).getName();
    		    Element e2 = new Element("memorystate");
    		    e2.addAttribute("value", key);
    		    e2.addAttribute("icon", value);
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
     * @param o  PanelEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        PanelEditor p = (PanelEditor)o;
        String name;
        MemoryIcon l = new MemoryIcon();

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
        int level = PanelEditor.MEMORIES.intValue();
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MemoryIconXml.class.getName());
}