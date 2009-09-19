package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
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
 * @version $Revision: 1.9 $
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
        if (p.getOriginalX()!=0)
            element.setAttribute("x", ""+p.getOriginalX());
        else
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
            if(!p.getBackground().equals(new Color(238, 238, 238))){
                element.setAttribute("redBack", ""+p.getBackground().getRed());
                element.setAttribute("greenBack", ""+p.getBackground().getGreen());
                element.setAttribute("blueBack", ""+p.getBackground().getBlue());
            }
        }
        if(p.getBorderSize()!=0){
            element.setAttribute("boarderSize", ""+p.getBorderSize());
            //Need to add in a bit about border color.
        }
        
        element.setAttribute("selectable", (p.isSelectable()?"yes":"no"));
        if(p.getJustification()!=0x00){
            String just;
            switch (p.getJustification()){
                case 0x02 : just="right";
                            break;
                case 0x04 : just ="centre";
                            break;
                default :   just="left";
                            break;
                }
            element.setAttribute("justification", just);
        }
        
        if (p.getFixedWidth()!=0)
            element.setAttribute("fixedWidth", ""+p.getFixedWidth());
        if (p.getFixedHeight()!=0)
            element.setAttribute("fixedHeight", ""+p.getFixedHeight());
            
        if (p.getMargin()!=0)
            element.setAttribute("margin", ""+p.getMargin());
        
        if (p.getBorderSize()!=0){
            element.setAttribute("borderWidth", ""+p.getBorderSize());
            element.setAttribute("redBorder", ""+p.getBorderColor().getRed());
            element.setAttribute("greenBorder", ""+p.getBorderColor().getGreen());
            element.setAttribute("blueBorder", ""+p.getBorderColor().getBlue());
        }    
        element.setAttribute("class", "jmri.jmrit.display.configurexml.LayoutMemoryIconXml");
        if (p.getDefaultIcon()!=null)
            element.setAttribute("defaulticon", p.getDefaultIcon().getName());

		// include contents
		java.util.HashMap<String,NamedIcon> map = p.getMap();
		if (map!=null) {
		    java.util.Iterator<String> iterator = map.keySet().iterator();
    	    while (iterator.hasNext()) {
    		    String key = iterator.next().toString();
    		    String value = map.get(key).getName();
    		    Element e2 = new Element("memorystate");
    		    e2.setAttribute("value", key);
    		    e2.setAttribute("icon", value);
    		    element.addContent(e2);
    	    }
        }
        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the memoryicon element, then
     * all the value-icon pairs
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    @SuppressWarnings("unchecked")
	public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor)o;
        LayoutMemoryIcon l = new LayoutMemoryIcon();
		p.memoryLabelList.add(l);

        l.setMemory(element.getAttribute("memory").getValue());

        Attribute a = element.getAttribute("defaulticon");
        if (a!=null) l.setDefaultIcon(NamedIcon.getIconByName(a.getValue()));
        
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
        
        a = element.getAttribute("justification");
        if(a!=null)
            l.setJustification(a.getValue());

        // get the icon pairs
        List<Element> items = element.getChildren();
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = items.get(i);
            String icon = item.getAttribute("icon").getValue();
            String keyValue = item.getAttribute("value").getValue();
        	l.addKeyAndIcon(NamedIcon.getIconByName(icon), keyValue);
		}
		
        //find if we have a fixed width memory icon
        int fixedWidth=0;
        int fixedHeight=0;
        try {
            fixedWidth=element.getAttribute("fixedWidth").getIntValue();
            fixedHeight=element.getAttribute("fixedHeight").getIntValue();
            l.setFixedSize(fixedWidth, fixedHeight);
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }
        
        int margin=0;
        try {
            margin=element.getAttribute("margin").getIntValue();
            l.setMargin(margin);
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
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
        // if the justification is set to left or the memory area is of a fixed width we simply set the x,y location as is.
        if ((l.getJustification()==0x00) || (fixedWidth!=0))
            l.setLocation(x,y);
        else
            l.setOriginalLocation(x,y);
 
         // find display level
        int level = LayoutEditor.LABELS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);
        if ((fixedWidth==0) && (margin==0))
            l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        else if ((fixedWidth==0) && (margin!=0))
            l.setSize(l.getPreferredSize().width+(margin*2), l.getPreferredSize().height+(margin*2));
        else
            l.setSize(fixedWidth, fixedHeight);
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
        
        try {
            int red = element.getAttribute("redBack").getIntValue();
            int blue = element.getAttribute("blueBack").getIntValue();
            int green = element.getAttribute("greenBack").getIntValue();
            l.setBackground(new Color(red, green, blue));
            l.setOpaque(true);
         } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }
        try {
            l.setBorderSize(element.getAttribute("borderSize").getIntValue());
            int red = element.getAttribute("redBorder").getIntValue();
            int blue = element.getAttribute("blueBorder").getIntValue();
            int green = element.getAttribute("greenBorder").getIntValue();
            l.setBorderColor(new Color(red, green, blue));
            //l.setBorder(new LineBorder(l.getBorderColor(), l.getBorderSize()));
            
            //l.setBorderWidth(new Color(red, green, blue));
            //l.setOpaque(true);
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutMemoryIconXml.class.getName());
}