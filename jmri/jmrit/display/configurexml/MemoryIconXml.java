package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.MemoryIcon;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import java.util.List;
import java.awt.Color;

/**
 * Handle configuration for display.MemoryIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004
 * @version $Revision: 1.10 $
 */
public class MemoryIconXml extends PositionableLabelXml {

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
        element.setAttribute("memory", p.getMemory().getSystemName());
        element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));

        storeTextInfo(p, element);
        
        element.setAttribute("selectable", (p.isSelectable()?"yes":"no"));

        element.setAttribute("class", "jmri.jmrit.display.configurexml.MemoryIconXml");
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
        
        loadTextInfo(l, element);
        
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
            
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MemoryIconXml.class.getName());
}