package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.MultiSensorIcon;
import org.jdom.Attribute;
import org.jdom.Element;

import java.util.List;

/**
 * Handle configuration for display.MultiSensorIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.16 $
 */
public class MultiSensorIconXml extends PositionableLabelXml {

    public MultiSensorIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * MultiSensorIcon
     * @param o Object to store, of type MultiSensorIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        MultiSensorIcon p = (MultiSensorIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("multisensoricon");
        storeCommonAttributes(p, element);

        element.setAttribute("inactive", p.getInactiveIcon().getURL());
        element.setAttribute("unknown", p.getUnknownIcon().getURL());
        element.setAttribute("inconsistent", p.getInconsistentIcon().getURL());
        element.setAttribute("rotate", String.valueOf(p.getUnknownIcon().getRotation()));
        element.setAttribute("updown", p.getUpDown()?"true":"false");

        for (int i = 0; i < p.getNumEntries(); i++) {
            Element e = new Element("multisensoriconentry");
            e.setAttribute("sensor", p.getSensorName(i));
            e.setAttribute("icon", p.getSensorIcon(i).getURL());
            element.addContent(storeIcon("active", p.getSensorIcon(i)));
            element.addContent(e);
        }
        element.addContent(storeIcon("inactive", p.getInactiveIcon()));
        element.addContent(storeIcon("unknown", p.getUnknownIcon()));
        element.addContent(storeIcon("inconsistent", p.getInconsistentIcon()));
        
        element.setAttribute("class", "jmri.jmrit.display.configurexml.MultiSensorIconXml");
        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  PanelEditor or LayoutEditor as an Object
     */
    @SuppressWarnings("unchecked")
	public void load(Element element, Object o) {
		// get object class and determine editor being used
		String className = o.getClass().getName();
		int lastDot = className.lastIndexOf(".");
		PanelEditor pe = null;
		LayoutEditor le = null;
		String shortClass = className.substring(lastDot+1,className.length());
		if (shortClass.equals("PanelEditor")) {
			pe = (PanelEditor) o;
		}
		else if (shortClass.equals("LayoutEditor")) {
			le = (LayoutEditor) o;
		}
		else {
			log.error("Unrecognizable class - "+className);
		}
        // create the objects
        String name;

        MultiSensorIcon l = new MultiSensorIcon();

        NamedIcon inactive;
        name = element.getAttribute("inactive").getValue();
        l.setInactiveIcon(inactive = NamedIcon.getIconByName(name));

        NamedIcon unknown;
        name = element.getAttribute("unknown").getValue();
        l.setUnknownIcon(unknown = NamedIcon.getIconByName(name));

        NamedIcon inconsistent;
        name = element.getAttribute("inconsistent").getValue();
        l.setInconsistentIcon(inconsistent = NamedIcon.getIconByName(name));

        int rotation = 0;
        try {
            Attribute a = element.getAttribute("rotate");
            if (a!=null) {
                rotation = element.getAttribute("rotate").getIntValue();
                inactive.setRotation(rotation, l);
                inconsistent.setRotation(rotation, l);
                unknown.setRotation(rotation, l);
            }
        } catch (org.jdom.DataConversionException e) {}

        Attribute a = element.getAttribute("updown");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setUpDown(true);
        else
            l.setUpDown(false);
            
		if (pe!=null){
            loadCommonAttributes(l, PanelEditor.SENSORS.intValue(), element);
		} else if (le!=null) {
            l.setPanel(le);
            loadCommonAttributes(l, LayoutEditor.SENSORS.intValue(), element);
        }

        // get the icon pairs & load
        List<Element> items = element.getChildren();
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = items.get(i);
            if (item.getAttribute("sensor")!=null) {
                String sensor = item.getAttribute("sensor").getValue();
                String icon = item.getAttribute("icon").getValue();
                NamedIcon nicon = NamedIcon.getIconByName(icon);
                if (rotation!=0) nicon.setRotation(rotation, l);

                NamedIcon aicon = loadIcon( l,"active", item);
                if (aicon!=null) { 
                    l.addEntry(sensor, aicon); 
                } else {
                    l.addEntry(sensor, nicon);
                }
            }
        }
		
        NamedIcon icon = loadIcon( l,"inactive", element);
        if (icon!=null) { l.setInactiveIcon(icon); }

        icon = loadIcon( l,"unknown", element);
        if (icon!=null) { l.setUnknownIcon(icon); }

        icon = loadIcon( l,"inconsistent", element);
        if (icon!=null) { l.setInconsistentIcon(icon); }

		// add multi-sensor to the panel
		if (pe!=null)
			pe.putLabel(l);
		else if (le!=null)
			le.putMultiSensor(l);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MultiSensorIconXml.class.getName());

}