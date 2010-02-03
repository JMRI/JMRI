package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.MemoryIcon;
import jmri.util.NamedBeanHandle;
import jmri.Memory;
import org.jdom.Attribute;
import org.jdom.Element;
import java.util.List;


/**
 * Handle configuration for display.MemoryIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004
 * @version $Revision: 1.26 $
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
        element.setAttribute("memory", p.getMemory().getName());
        if (p.getOriginalX()!=0)
            element.setAttribute("x", ""+p.getOriginalX());
        else
            element.setAttribute("x", ""+p.getX());
        //element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));

        storeTextInfo(p, element);
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
        
        element.setAttribute("selectable", (p.isSelectable()?"yes":"no"));

        element.setAttribute("class", "jmri.jmrit.display.configurexml.MemoryIconXml");
        if (p.getDefaultIcon()!=null)
            element.setAttribute("defaulticon", p.getDefaultIcon().getURL());

		// include contents
		java.util.HashMap<String, NamedIcon> map = p.getMap();
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
     * @param o  PanelEditor as an Object
     */
    @SuppressWarnings("unchecked")
	public void load(Element element, Object o) {
        // get object class and determine editor being used
        String className = o.getClass().getName();
		int lastDot = className.lastIndexOf(".");
		Editor ed = null;
		//PanelEditor pe = null;
		//LayoutEditor le = null;
        MemoryIcon l;
		String shortClass = className.substring(lastDot+1,className.length());
		if (shortClass.equals("PanelEditor")) {
			ed = (PanelEditor) o;
            l = new MemoryIcon("", ed);
		}
		else if (shortClass.equals("LayoutEditor")) {
			ed = (LayoutEditor) o;
            l = new jmri.jmrit.display.layoutEditor.MemoryIcon("   ", (LayoutEditor)ed);
            ((LayoutEditor)ed).memoryLabelList.add((jmri.jmrit.display.layoutEditor.MemoryIcon)l);
		}
		else if (o instanceof jmri.jmrit.display.Editor) {
			ed = (Editor) o;
            l = new MemoryIcon("", ed);
        }
        else {
			log.error("Unrecognizable class - "+className);
            return;
		}
        loadTextInfo(l, element);
        String name;
        Attribute attr = element.getAttribute("memory"); 
        if (attr == null) {
            log.error("incorrect information for a memory location; must use memory name");
            return;
        } else {
            name = attr.getValue();
        }
        
        Memory m = jmri.InstanceManager.memoryManagerInstance().getMemory(name);
        
        if (m!=null) {
            l.setMemory(new NamedBeanHandle<Memory>(name, m));
        } else {
            log.error("Memory named '"+attr.getValue()+"' not found.");
            return;
        }
        //l.setMemory(jmri.InstanceManager.memoryManagerInstance().getMemory(
            //element.getAttribute("memory").getValue()));
        
         // find display level
        //int level = PanelEditor.MEMORIES.intValue();
        try {
            int level = element.getAttribute("level").getIntValue();
            l.setDisplayLevel(level);
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        
        Attribute a = element.getAttribute("selectable");
        if (a!=null && a.getValue().equals("yes")) l.setSelectable(true);
        else l.setSelectable(false);
        
        // get the icon pairs
        List<Element> items = element.getChildren();
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = items.get(i);
            String icon = item.getAttribute("icon").getValue();
            String keyValue = item.getAttribute("value").getValue();
        	l.addKeyAndIcon(NamedIcon.getIconByName(icon), keyValue);
		}
        a = element.getAttribute("justification");
        if(a!=null)
            l.setJustification(a.getValue());
        
        // find coordinates
        int x = 0;
        int y = 0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        if ((l.getJustification()==0x00) || (l.getFixedWidth()!=0))
            l.setLocation(x,y);
        else
            l.setOriginalLocation(x,y);
 
         // find display level
        int level = Editor.MEMORIES;
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);
        ed.putItem(l);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryIconXml.class.getName());
}